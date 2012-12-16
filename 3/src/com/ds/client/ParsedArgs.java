package com.ds.client;

class ParsedArgs {

    private final String host;
    private final int tcpPort;
    private final int udpPort;
    private final String serverPublicKey;
    private final String clientKeyDir;

    public ParsedArgs(String[] args) {
        if (args.length != 5) {
            throw new IllegalArgumentException();
        }

        try {
            host = args[0];
            tcpPort = Integer.parseInt(args[1]);
            udpPort = Integer.parseInt(args[2]);
            serverPublicKey = args[3];
            clientKeyDir = args[4];
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException();
        }
    }

    public String getHost() {
        return host;
    }

    public int getTcpPort() {
        return tcpPort;
    }

    public int getUdpPort() {
        return udpPort;
    }

    public String getServerPublicKey() {
        return serverPublicKey;
    }

    public String getClientKeyDir() {
        return clientKeyDir;
    }

}
