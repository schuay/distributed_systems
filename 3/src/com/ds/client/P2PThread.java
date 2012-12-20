package com.ds.client;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.AdvertisementFactory;
import net.jxta.endpoint.ByteArrayMessageElement;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.IDFactory;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.pipe.InputPipe;
import net.jxta.pipe.OutputPipe;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.pipe.PipeService;
import net.jxta.platform.Module;
import net.jxta.platform.ModuleClassID;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import net.jxta.protocol.ModuleClassAdvertisement;
import net.jxta.protocol.ModuleImplAdvertisement;
import net.jxta.protocol.ModuleSpecAdvertisement;
import net.jxta.protocol.PipeAdvertisement;

import com.ds.channels.Channel;
import com.ds.loggers.Log;
import com.ds.util.CommandMatcher;

public class P2PThread implements DiscoveryListener, PipeMsgListener, Runnable {

    private static final String subgroup_name = "TimeProvider";
    private static final String subgroup_desc = "A service that provides signed timestamps to peers";
    private static final PeerGroupID subgroup_id = IDFactory.newPeerGroupID(PeerGroupID.defaultNetPeerGroupID, subgroup_name.getBytes());

    private static final String unicast_name = "TimeProvider";
    private static final String service_name = "TimeProviderService";

    private static final List<CommandMatcher> matchers;

    private PeerGroup subgroup;
    private PipeService pipe_service;
    private PipeID unicast_id;
    private PipeID service_id;
    private DiscoveryService discovery;
    private ModuleSpecAdvertisement mdadv;

    private final String peer_name;
    private final PeerID peer_id;
    private final File conf;
    private final NetworkManager manager;
    private InputPipe unicast_pipe;
    private InputPipe service_pipe;

    private final List<String> peers = new ArrayList<String>();
    private final List<String> activePeers = new ArrayList<String>();
    private final BlockingQueue<P2PTask> q;

    private String currentUser = null;
    private PrivateKey currentKey = null;

    /**
     * Encapsulates the user, timestamp, and signature.
     */
    private static class TimeReply {

    }

    static {
        List<CommandMatcher> l = new ArrayList<CommandMatcher>();
        l.add(new CommandMatcher(CommandMatcher.Type.GETTIMESTAMP, "^!getTimestamp ([0-9]+) ([0-9]+)$"));
        l.add(new CommandMatcher(CommandMatcher.Type.TIMESTAMP, "^!timestamp ([0-9]+) ([0-9]+) ([0-9]+) ([a-zA-Z0-9/+]+=)$"));
        l.add(new CommandMatcher(CommandMatcher.Type.LOGIN, "^!login$"));
        l.add(new CommandMatcher(CommandMatcher.Type.LOGOUT, "^!logout$"));
        matchers = Collections.unmodifiableList(l);
    }

    @Override
    public void run() {
        Thread advertisementThread = null;
        try {
            start();

            advertisementThread = createAdvertisementThread();
            advertisementThread.start();

            while (true) {
                P2PTask request = q.take();
                processTimeRequest(request);
            }

        } catch (IOException e) {
            Log.e(e.getMessage());
        } catch (PeerGroupException e) {
            Log.e(e.getMessage());
        } catch (InterruptedException e) {
            Log.e(e.getMessage());
        } finally {
            if (advertisementThread != null) {
                advertisementThread.interrupt();

                boolean interrupted;
                do {
                    try {
                        interrupted = false;
                        advertisementThread.join();
                    } catch (InterruptedException e) {
                        interrupted = true;
                    }
                } while (interrupted);
            }

            unicast_pipe.close();
            service_pipe.close();

            pipe_service.stopApp();
            discovery.stopApp();

            subgroup.stopApp();

            manager.stopNetwork();
        }
    }

    /**
     * Processes a time request sent by the currently logged in local user.
     * Chooses two random known peers and requests a signed timestamp.
     * Once it has been received, the signed bids are forwarded to the
     * processing thread.
     *
     * @param request
     */
    private void processTimeRequest(P2PTask request) {
        switch (request.getType()) {
        case GET_TIMESTAMP: {
            P2PGetTimestampTask t = (P2PGetTimestampTask)request;

            Log.i("Received local time request");

            String msg = String.format("!getTimestamp %d %d",
                    t.getCommand().getAuctionId(),
                    t.getCommand().getAmount());

            /* Get two random peers or exit if less than 2 exist. */

            String p1, p2;
            synchronized (activePeers) {
                if (activePeers.size() < 2) {
                    Log.w("Less than two known peers, cannot retrieve signed timestamp");
                    return;
                }

                int i = new Random().nextInt(activePeers.size());
                p1 = activePeers.get(i);
                p2 = activePeers.get((i + 1) % activePeers.size());
            }

            send_to_peer(msg, p1);
            send_to_peer(msg, p2);

            break;
        }
        case LOG_IN: {
            P2PLoginTask t = (P2PLoginTask)request;

            currentUser = t.getUser();
            currentKey = t.getKey();

            broadcast("!login");
            break;
        }
        case LOG_OUT: {
            currentUser = null;
            currentKey = null;

            broadcast("!logout");
            break;
        }
        default:
            Log.w("Received unknown task");
            break;
        }
    }

    private void onMessageFrom(String message, String from) {
        CommandMatcher matcher = null;
        List<String> matches = null;
        for (int i = 0; i < matchers.size(); i++) {
            matcher = matchers.get(i);
            matches = matcher.match(message);
            if (matches != null) {
                break;
            }
        }

        if (matches == null) {
            Log.w("Invalid command '%s'", message);
            return;
        }

        Log.i("Received command: %s", message);

        switch (matcher.getType()) {
        case GETTIMESTAMP:
            if (currentUser == null) {
                Log.w("Timestamp request received while logged out");
                return;
            }

            long time = System.currentTimeMillis();
            String response = String.format("!timestamp %s %s %d %s",
                    matches.get(0),
                    matches.get(1),
                    time,
                    "DEADBEEF=" /* TODO: Signature */);

            send_to_peer(response, from);
            break;
        case TIMESTAMP:
            break;
        case LOGIN:
            synchronized(activePeers) {
                activePeers.add(from);
            }
            break;
        case LOGOUT:
            synchronized(activePeers) {
                activePeers.remove(from);
            }
            break;
        default:
            Log.w("Unexpected P2P command");
        }
    }

    /**
     * Sends a message to all known peers. If a peer turns out to be unreachable,
     * remove it from our known list.
     */
    private void broadcast(String message) {
        List<String> ps;
        synchronized (peers) {
            ps = Collections.unmodifiableList(peers);
        }

        List<String> unreachable = new ArrayList<String>();
        for (String peer : ps) {
            try {
                send_to_peer(message, peer);
            } catch (Throwable t) {
                Log.e(t.getMessage());
                unreachable.add(peer);
            }
        }

        synchronized (peers) {
            peers.removeAll(unreachable);
        }
        synchronized (activePeers) {
            activePeers.removeAll(unreachable);
        }
    }

    private Thread createAdvertisementThread() {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while(true) {
                        discovery.getRemoteAdvertisements(null, DiscoveryService.ADV, "Name", "TimeProvider", 1, null);
                        Thread.sleep(1000);
                    }
                } catch(InterruptedException e) {
                    Log.w("Interrupted while fetching advertisements");
                }
            }
        });
    }

    public P2PThread(Data data) throws IOException {
        this.q = data.getTimeRetrieverQueue();

        // Add a random number to make it easier to identify by name, will also make sure the ID is unique
        peer_name = "Peer " + new Random().nextInt(1000000);

        // This is what you will be looking for in Wireshark instead of an IP, hint: filter by "jxta"
        peer_id = IDFactory.newPeerID(PeerGroupID.defaultNetPeerGroupID, peer_name.getBytes());

        // Here the local peer cache will be saved, if you have multiple peers this must be unique
        conf = new File("." + System.getProperty("file.separator") + peer_name);

        // Most documentation you will find use a deprecated network manager setup, use this one instead
        // ADHOC is usually a good starting point, other alternatives include Edge and Rendezvous
        manager = new NetworkManager(
                NetworkManager.ConfigMode.ADHOC,
                peer_name, conf.toURI());

        // Settings Configuration
        NetworkConfigurator configurator;
        configurator = manager.getConfigurator();
        configurator.setTcpPort(data.getPort());
        configurator.setTcpEnabled(true);
        configurator.setTcpIncoming(true);
        configurator.setTcpOutgoing(true);
        configurator.setUseMulticast(true);
        configurator.setPeerID(peer_id);

        // JXTA logs a lot, you can configure it setting level here
        Logger.getLogger("net.jxta").setLevel(Level.OFF);
    }

    public void start() throws PeerGroupException, IOException {
        // Launch the missiles, if you have logging on and see no exceptions
        // after this is ran, then you probably have at least the jars setup correctly.
        PeerGroup net_group = manager.startNetwork();

        // Connect to our subgroup (all groups are subgroups of Netgroup)
        // If the group does not exist, it will be automatically created
        // Note this is suggested deprecated, not sure what the better way is
        ModuleImplAdvertisement mAdv = null;
        try {
            mAdv = net_group.getAllPurposePeerGroupImplAdvertisement();
        } catch (Exception ex) {
            System.err.println(ex.toString());
        }
        subgroup = net_group.newGroup(subgroup_id, mAdv, subgroup_name, subgroup_desc);

        // A simple check to see if connecting to the group worked
        if (Module.START_OK != subgroup.startApp(new String[0]))
            System.err.println("Cannot start child peergroup");

        // We will create to listeners that will listen for
        // unicast advertisements. All messages will be handled by Hello in the
        // pipeMsgEvent method.

        unicast_id = IDFactory.newPipeID(subgroup.getPeerGroupID(), unicast_name.getBytes());

        pipe_service = subgroup.getPipeService();
        unicast_pipe = pipe_service.createInputPipe(get_advertisement(unicast_id), this);

        // In order to for other peers to find this one (and say hello) we will
        // advertise a Hello Service.
        discovery = subgroup.getDiscoveryService();
        discovery.addDiscoveryListener(this);

        ModuleClassAdvertisement mcadv = (ModuleClassAdvertisement)
                AdvertisementFactory.newAdvertisement(ModuleClassAdvertisement.getAdvertisementType());

        mcadv.setName("TimeProvider");
        mcadv.setDescription("A service that provides signed timestamps to peers");

        ModuleClassID mcID = IDFactory.newModuleClassID();

        mcadv.setModuleClassID(mcID);

        // Let the group know of this service "module" / collection
        discovery.publish(mcadv);
        discovery.remotePublish(mcadv);

        mdadv = (ModuleSpecAdvertisement)AdvertisementFactory.newAdvertisement(
                ModuleSpecAdvertisement.getAdvertisementType());
        mdadv.setName("TimeProvider");
        mdadv.setVersion("1.0");
        mdadv.setCreator("Distributed Systems");
        mdadv.setModuleSpecID(IDFactory.newModuleSpecID(mcID));
        mdadv.setSpecURI("http://www.jxta.org/Ex1");

        service_id = IDFactory.newPipeID(subgroup.getPeerGroupID(), service_name.getBytes());
        PipeAdvertisement pipeadv = get_advertisement(service_id);
        mdadv.setPipeAdvertisement(pipeadv);

        // Let the group know of the service
        discovery.publish(mdadv);
        discovery.remotePublish(mdadv);

        // Start listening for discovery events, received by the discoveryEvent method
        service_pipe = pipe_service.createInputPipe(pipeadv, this);
    }

    private static PipeAdvertisement get_advertisement(PipeID id) {
        PipeAdvertisement adv = (PipeAdvertisement )AdvertisementFactory.
                newAdvertisement(PipeAdvertisement.getAdvertisementType());
        adv.setPipeID(id);
        adv.setType(PipeService.UnicastType);
        adv.setName("TimeProviderX");
        adv.setDescription("A service that provides signed timestamps to peers");
        return adv;
    }

    @Override
    public void discoveryEvent(DiscoveryEvent event) {
        // Found another peer! Let's say hello shall we!
        // Reformatting to create a real peer id string
        String found_peer_id = "urn:jxta:" + event.getSource().toString().substring(7);
        addPeer(found_peer_id);

        /* TODO: Request state from peer. */
    }

    /**
     * Adds a peer to our internal peer list.
     * @param peer
     */
    private void addPeer(String peer) {
        synchronized(peers) {
            if (!peers.contains(peer)) {
                Log.i("Found new peer: %s", peer);
                peers.add(peer);
            }
        }
    }

    private void send_to_peer(String message, String found_peer_id) {
        // This is where having the same ID is important or else we wont be
        // able to open a pipe and send messages
        PipeAdvertisement adv = get_advertisement(unicast_id);

        // Send message to all peers in "ps", just one in our case
        Set<PeerID> ps = new HashSet<PeerID>();
        try {
            ps.add((PeerID)IDFactory.fromURI(new URI(found_peer_id)));
        }
        catch (URISyntaxException e) {
            // The JXTA peer ids need to be formatted as proper urns
            e.printStackTrace();
        }

        // A pipe we can use to send messages with
        OutputPipe sender = null;
        try {
            sender = pipe_service.createOutputPipe(adv, ps, 10000);
        }
        catch (IOException e) {
            // Thrown if there was an error opening the connection, check firewall settings
            e.printStackTrace();
        }

        Message msg = new Message();
        MessageElement fromElem = null;
        MessageElement msgElem = null;
        try {
            fromElem = new ByteArrayMessageElement("From", null, peer_id.toString().getBytes(Channel.CHARSET), null);
            msgElem = new ByteArrayMessageElement("Msg", null, message.getBytes(Channel.CHARSET), null);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        msg.addMessageElement(fromElem);
        msg.addMessageElement(msgElem);

        try {
            sender.send(msg);
        } catch (IOException e) {
            // Check, firewall, settings.
            e.printStackTrace();
        } finally {
            sender.close();
        }
    }

    @Override
    public void pipeMsgEvent(PipeMsgEvent event) {
        // Someone is sending us a message!
        try {
            Message msg = event.getMessage();
            byte[] msgBytes = msg.getMessageElement("Msg").getBytes(true);
            byte[] fromBytes = msg.getMessageElement("From").getBytes(true);
            String from = new String(fromBytes);
            String message = new String(msgBytes);

            onMessageFrom(message, from);
        } catch (Exception e) {
            // You will notice that JXTA is not very specific with exceptions...
            e.printStackTrace();
        }
    }
}