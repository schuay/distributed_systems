package com.ds.management;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        /* Main loop. */

        BufferedReader stdin = null;
        try {
            stdin = new BufferedReader(new InputStreamReader(System.in));
            String input;
            while ((input = stdin.readLine()) != null) {

                /* Parse incoming command. */

                List<String> matches = null;
                for (CommandMatcher matcher : matchers) {
                    matches = matcher.match(input);
                    if (matches != null) {
                        break;
                    }
                }

                if (matches == null) {
                    Log.w("Invalid command '%s'", input);
                    continue;
                }

                /* TODO: If command != '!end', handle command. */

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
        }
    }

    private static class CommandMatcher {

        enum Type {
            LOGIN
        }

        private final Type type;
        private final Pattern pattern;

        public CommandMatcher(Type type, String regex) {
            this.type = type;
            pattern = Pattern.compile(regex);
        }

        public List<String> match(String input) {
            List<String> groups = null;
            Matcher m = pattern.matcher(input);

            if (m.matches()) {
                groups = new ArrayList<String>();
                for (int i = 1; i < m.groupCount(); i++) {
                    groups.add(m.group(i));
                }
            }

            return groups;
        }

        public Type getType() {
            return type;
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
