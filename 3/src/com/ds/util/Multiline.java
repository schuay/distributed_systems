package com.ds.util;


public class Multiline {

    private Multiline() { }

    public static String encode(String in) {

        /* Sloppy escape.
         * Since we use '#' in place of a '\n' (for transmission as a single line
         * string), escape all '#'.
         */

        return in.replaceAll("#", "\\#").replaceAll("\\n", "#");
    }

    public static String decode(String in) {

        /* Sloppy unescape. */

        return in.replaceAll("\\\\#", "\0").replaceAll("#", "\n").replaceAll("\0", "#");
    }
}
