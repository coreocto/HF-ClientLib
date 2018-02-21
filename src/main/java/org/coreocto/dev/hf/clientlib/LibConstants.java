package org.coreocto.dev.hf.clientlib;

import java.nio.charset.Charset;

public class LibConstants {
    public static final String SPACE = " ";
    public static final String REGEX_SPACE = "\\s";
    public static final String ENCODING_UTF8 = "UTF-8";
    public static final String REGEX_NON_WORD = "\\W";
    public static final String REGEX_SPLIT_CHARS = "[\\r?\\n|\\p{Blank}|\\p{Cntrl}]+";
    public static final String EMPTY_STRING = "";
    public static final Charset CHARSET_UTF8 = Charset.forName(ENCODING_UTF8);
}
