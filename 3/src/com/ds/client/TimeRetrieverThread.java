package com.ds.client;

import java.util.concurrent.BlockingQueue;

import com.ds.loggers.Log;

class TimeRetrieverThread implements Runnable {

    private final Data data;
    private final BlockingQueue<TimeRequest> q;

    public TimeRetrieverThread(Data data) {
        this.data = data;
        this.q = data.getTimeRetrieverQueue();
    }

    @Override
    public void run() {
        try {
            while (true) {
                TimeRequest request = q.take();

                String msg = String.format("!getTimestamp %d %d",
                        request.getCommand().getAuctionId(),
                        request.getCommand().getAmount());

                TimeReply reply1 = retrieveTimeFrom(request.getUser1(), msg);
                TimeReply reply2 = retrieveTimeFrom(request.getUser2(), msg);

                /* TODO: Put on processor queue. */
            }
        } catch (InterruptedException e) {
            Log.i("Interrupted while waiting for request: %s", e.getMessage());
        }
    }

    private TimeReply retrieveTimeFrom(User user1, String msg) {

        /* TODO */

        return null;
    }

    /**
     * Encapsulates the user, timestamp, and signature.
     */
    private static class TimeReply {

    }

}
