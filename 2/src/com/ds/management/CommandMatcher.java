package com.ds.management;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A convenience class for matching strings against regex.
 */
class CommandMatcher {

    public enum Type {
        LOGIN,
        LOGOUT,
        SUBSCRIBE,
        UNSUBSCRIBE,
        AUTO,
        HIDE,
        PRINT,
        END
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
            for (int i = 0; i < m.groupCount(); i++) {
                groups.add(m.group(1 + i)); /* group(0) is the entire string. */
            }
        }

        return groups;
    }

    public Type getType() {
        return type;
    }
}
