package org.unibl.etf.blbustracker.utils.languageutil;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class LatinUtils
{
    public static final Map<String, String> latinChars;

    static
    {
        latinChars = new HashMap<>();
        latinChars.put("š", "s");
        latinChars.put("Š", "S");

        latinChars.put("đ", "dj");
        latinChars.put("Đ", "DJ");

        latinChars.put("č", "c");
        latinChars.put("Č", "C");

        latinChars.put("ć", "c");
        latinChars.put("Ć", "C");

        latinChars.put("ž", "z");
        latinChars.put("Ž", "Z");

    }

    // pattern \s matches any white space
    private static final String LATIN_PATTERN = "([a-zA-Z0-9|ŠšĐđČčĆćŽž)(-.]|\\s)+";

    //my custom is input latin word checker
    public static boolean isInputLatin(String input)
    {
        Pattern pattern = Pattern.compile(LATIN_PATTERN);
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }


    //converts string with chars "šđčćž" and replaces them with "sdccz" respec
    public static String stripAccent(String word)
    {
        if (word != null)
            for (Map.Entry<String, String> charEntry : latinChars.entrySet())
            {
                if (word.contains(charEntry.getKey()))
                {
                    word = word.replace(charEntry.getKey(), charEntry.getValue());
                }
            }
        return word;
    }
}
