package com.ds.management;

import com.ds.loggers.Log;

public class ManagementMain {

    public static void main(String[] args) {

        /* Handle command-line arguments. */

        ParsedArgs parsedArgs = null;
        try {
            parsedArgs = new ParsedArgs(args);
            Log.i("Analytics Binding Name: %s", parsedArgs.getAnalyticsBindingName());
            Log.i("Billing Binding Name: %s", parsedArgs.getBillingBindingName());
        } catch (IllegalArgumentException e) {
            System.err.printf("Usage: java %s <Analytics Binding Name> <Billing Binding Name>%n",
                    ManagementMain.class.getName());
            return;
        }
    }

    /**
     * Parses command-line arguments.
     */
    private static class ParsedArgs {

        private final String analyticsBindingName;
        private final String billingBindingName;

        public ParsedArgs(String[] args) {
            if (args.length != 2) {
                throw new IllegalArgumentException();
            }

            try {
                analyticsBindingName = args[0];
                billingBindingName = args[1];
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException();
            }
        }

        public String getAnalyticsBindingName() {
            return analyticsBindingName;
        }

        public String getBillingBindingName() {
            return billingBindingName;
        }
    }
}
