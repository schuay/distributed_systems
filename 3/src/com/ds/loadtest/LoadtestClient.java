package com.ds.loadtest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.ds.common.Command;
import com.ds.common.ResponseAuctionList;
import com.ds.loggers.Log;
import com.ds.util.LoadTestProperties;

class LoadtestClient {
    private final Timer timer = new Timer();
    private final LoadTestProperties prop;
    private final Socket sock;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final int id;
    private final long millisStart;
    private final Random rng;
    private String[] auctions;

    LoadtestClient(int id, LoadTestProperties prop, Socket sock, long millisStart)
            throws IOException {
        this.id = id;
        this.prop = prop;
        this.sock = sock;
        this.millisStart = millisStart;
        this.out = new ObjectOutputStream(sock.getOutputStream());
        this.in = new ObjectInputStream(sock.getInputStream());
        this.rng = new Random(new Date().getTime());
    }

    void shutdown() {
        timer.cancel();
        try { in.close(); } catch (IOException e) {}
        try { out.close(); } catch (IOException e) {}
        try { sock.close(); } catch (IOException e) {}
    }

    void start() throws IOException, ClassNotFoundException {
        auctions = new String[0];
        out.writeObject(Command.parse(String.format("!login loadtest%d", id)));
        in.readObject();
        timer.scheduleAtFixedRate(new StartAuctionTask(), 0,
                60 * 1000 / prop.getAuctionsPerMin());
        timer.scheduleAtFixedRate(new UpdateAuctionListTask(), 0,
                prop.getUpdateIntervalSec() * 1000);
        timer.scheduleAtFixedRate(new BidTask(), 0,
                60 * 1000 / prop.getBidsPerMin());
    }

    private class StartAuctionTask extends TimerTask {
        private final long auctionId = 0;
        @Override
        public void run() {
            try {
                out.writeObject(Command.parse(String.format("!create %d loadtest%d_%d",
                        prop.getAuctionDuration(), id, auctionId)));
                in.readObject();
            } catch (Exception e) {
                Log.w(e.getLocalizedMessage());
            }
        }
    }

    private class UpdateAuctionListTask extends TimerTask {
        @Override
        public void run() {
            try {
                out.writeObject(Command.parse("!list"));
                auctions = ((ResponseAuctionList)in.readObject()).toString()
                        .split(String.format("%n"));
            } catch (Exception e) {
                Log.w(e.getLocalizedMessage());
            }
        }
    }

    private class BidTask extends TimerTask {
        @Override
        public void run() {
            if (auctions.length == 0) {
                return;
            }

            try {
                int auctionId = Integer.parseInt(auctions[rng.nextInt(auctions.length)]
                        .split("\\.")[0]);
                out.writeObject(Command.parse(String.format("!bid %d %d",
                        auctionId, new Date().getTime() - millisStart)));
                in.readObject();
            } catch (Exception e) {
                Log.w(e.getLocalizedMessage());
            }
        }
    }
}
