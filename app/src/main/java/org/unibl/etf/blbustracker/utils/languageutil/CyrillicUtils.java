package org.unibl.etf.blbustracker.utils.languageutil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class CyrillicUtils
{
    public static final List<String> cyrillicChars;
    public static final Map<String, String> cyrToLat = new HashMap<>();
    //    public static final Map<String, String> latToCyr = new HashMap<>();

    public static boolean isCyrillic(String input)
    {
        for (int i = 0; i < input.length(); i++)
        {
            String character = Character.toString(input.charAt(i));
            if (!cyrillicChars.contains(character))
                return false;
        }
        return true;
    }

    //convert Cyrilic string to Latin
    public static String convertCyrToLat(String word)
    {
        if (word != null)
            for (Map.Entry<String, String> charEntry : cyrToLat.entrySet())
            {
                if (word.contains(charEntry.getKey()))
                {
                    word = word.replace(charEntry.getKey(), charEntry.getValue());
                }
            }
        return word;
    }

    private static void addToMap(String lat, String cyr)
    {
        cyrToLat.put(cyr, lat);
        //        latToCyr.put(lat,cyr);
    }


    static
    {
        addToMap("lj", "Љ");
        addToMap("lj", "љ");

        addToMap("dz", "Џ");
        addToMap("dz", "џ");

        addToMap("a", "а");
        addToMap("b", "б");
        addToMap("v", "в");
        addToMap("g", "г");
        addToMap("d", "д");
        addToMap("dj", "ђ");
        addToMap("e", "е");
        addToMap("z", "ж");
        addToMap("z", "з");
        addToMap("i", "и");
        addToMap("j", "ј");
        addToMap("k", "к");
        addToMap("l", "л");
        addToMap("m", "м");
        addToMap("n", "н");
        addToMap("nj", "њ");
        addToMap("o", "о");
        addToMap("p", "п");
        addToMap("r", "р");
        addToMap("s", "с");
        addToMap("t", "т");
        addToMap("c", "ћ");
        addToMap("u", "у");
        addToMap("f", "ф");
        addToMap("h", "х");
        addToMap("c", "ц");
        addToMap("c", "ч");
        addToMap("s", "ш");
        addToMap("a", "А");
        addToMap("b", "Б");
        addToMap("v", "В");
        addToMap("g", "Г");
        addToMap("d", "Д");
        addToMap("dj", "Ђ");
        addToMap("e", "Е");
        addToMap("z", "Ж");
        addToMap("z", "З");
        addToMap("i", "И");
        addToMap("j", "Ј");
        addToMap("k", "К");
        addToMap("l", "Л");
        addToMap("m", "М");
        addToMap("n", "Н");
        addToMap("nj", "Њ");
        addToMap("o", "О");
        addToMap("p", "П");
        addToMap("r", "Р");
        addToMap("s", "С");
        addToMap("t", "Т");
        addToMap("c", "Ћ");
        addToMap("u", "У");
        addToMap("f", "Ф");
        addToMap("h", "Х");
        addToMap("c", "Ц");
        addToMap("c", "Ч");
        addToMap("s", "Ш");

        cyrillicChars = new ArrayList<>(cyrToLat.keySet());
        cyrillicChars.addAll(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", " "));
    }
}
