package com.ds.server;

class ParsedArgs {
    private final int tcpPort;
    private final String analyticsBindingName;
    private final String billingBindingName;

    public ParsedArgs(String[] args) {
        if (args.length != 3) {
            throw new IllegalArgumentException();
        }

        try {
            tcpPort = Integer.parseInt(args[0]);
            analyticsBindingName = args[1];
            billingBindingName = args[2];
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException();
        }
    }

    public int getTcpPort() {
        return tcpPort;
    }

    public String getAnalyticsBindingName() {
        return analyticsBindingName;
    }

    public String getBillingBindingName() {
        return billingBindingName;
    }
}