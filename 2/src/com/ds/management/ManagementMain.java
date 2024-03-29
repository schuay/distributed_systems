package com.ds.management;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.util.ArrayList;
import java.util.List;

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

        /* Configure matchers. */

        List<CommandMatcher> matchers = new ArrayList<CommandMatcher>();
        matchers.add(new CommandMatcher(CommandMatcher.Type.LOGIN, "^!login\\s+(\\S+)\\s+(\\S+)\\s*$"));
        matchers.add(new CommandMatcher(CommandMatcher.Type.LOGOUT, "^!logout\\s*$"));
        matchers.add(new CommandMatcher(CommandMatcher.Type.SUBSCRIBE, "^!subscribe\\s+(.+)$"));
        matchers.add(new CommandMatcher(CommandMatcher.Type.UNSUBSCRIBE, "^!unsubscribe\\s+(.+)$"));
        matchers.add(new CommandMatcher(CommandMatcher.Type.AUTO, "^!auto\\s*$"));
        matchers.add(new CommandMatcher(CommandMatcher.Type.HIDE, "^!hide\\s*$"));
        matchers.add(new CommandMatcher(CommandMatcher.Type.PRINT, "^!print\\s*$"));
        matchers.add(new CommandMatcher(CommandMatcher.Type.STEPS, "^!steps\\s*$"));
        matchers.add(new CommandMatcher(CommandMatcher.Type.ADD_STEP, "^!addStep\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s*$"));
        matchers.add(new CommandMatcher(CommandMatcher.Type.REM_STEP, "^!removeStep\\s+(\\S+)\\s+(\\S+)\\s*$"));
        matchers.add(new CommandMatcher(CommandMatcher.Type.BILL, "^!bill\\s+(\\S+)\\s*$"));
        matchers.add(new CommandMatcher(CommandMatcher.Type.EXIT, "^!exit\\s*$"));

        /* Main loop. */

        BufferedReader stdin = null;
        Data data = null;
        try {
            data = new Data(parsedArgs);
            State state = new StateLoggedOut(data);
            stdin = new BufferedReader(new InputStreamReader(System.in));

            System.out.print(state.getPrefix());
            System.out.flush();

            String input;
            while ((input = stdin.readLine()) != null) {

                /* Parse incoming command. */

                CommandMatcher matcher = null;
                List<String> matches = null;
                for (int i = 0; i < matchers.size(); i++) {
                    matcher = matchers.get(i);
                    matches = matcher.match(input);
                    if (matches != null) {
                        break;
                    }
                }

                if (matches == null) {
                    Log.w("Invalid command '%s'", input);
                    continue;
                }

                /* If command != '!exit', handle command. */

                if (matcher.getType() == CommandMatcher.Type.EXIT) {
                    break;
                }

                state = state.processCommand(matcher.getType(), matches);

                /* Prepare for next input. */

                System.out.print(state.getPrefix());
                System.out.flush();
            }
        } catch (Throwable t) {
            Log.e(t.getMessage());
        } finally {
            /* Why does Java insist on making finally clauses so freaking ugly? */

            if (stdin != null) {
                try {
                    stdin.close();
                } catch (IOException e) {
                    Log.e(e.getMessage());
                }
            }

            if (data != null) {
                data.getAnalSub().shutdown();
            }
        }
    }

    public static class Data {

        private final AnalyticsSubscriber analSub;
        private final BillingConnection billSub;

        public Data(ParsedArgs args) throws NotBoundException, IOException {
            analSub = new AnalyticsSubscriber(args.getAnalyticsBindingName());
            billSub = new BillingConnection(args.getBillingBindingName());
        }

        public AnalyticsSubscriber getAnalSub() {
            return analSub;
        }

        public BillingConnection getBillConn() {
            return billSub;
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
