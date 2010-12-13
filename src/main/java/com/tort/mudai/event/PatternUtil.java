package com.tort.mudai.event;

import java.util.regex.Pattern;

public class PatternUtil {
    public static Pattern compile(String regex){
        return Pattern.compile(regex, Pattern.MULTILINE | Pattern.DOTALL);
    }
}
