package org.unibl.etf.blbustracker.utils.languageutil;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import org.unibl.etf.blbustracker.ChoseLanguageActivity;
import org.unibl.etf.blbustracker.utils.Utils;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class Translator
{
    public static final Map<String, String> dictionary;
    private String currentLang;

    public Translator(Context context)
    {
        SharedPreferences sharedPreferences = Utils.getSharedPreferences(context);
        currentLang = sharedPreferences.getString(ChoseLanguageActivity.SELECTED_LANGUAGE, ChoseLanguageActivity.LANGUAGE_SR);
    }

    public Translator(String language)
    {
        currentLang = language;
    }

    public Translator()
    {
        currentLang = ChoseLanguageActivity.LANGUAGE_SR;
    }

    public String translateInput(String input)
    {
        return translateInput(input, currentLang);
    }

    public String translateInput(String input, String language)
    {
        if (input == null)
            input = "";
        String translatedInput = input;

        if (ChoseLanguageActivity.LANGUAGE_SR_CYRILLIC.equals(language))
        {
            for (Map.Entry<String, String> entry : dictionary.entrySet()) //converting to cyrillic
            {
                if (translatedInput.contains(entry.getKey()))
                    translatedInput = translatedInput.replaceAll(entry.getKey(), entry.getValue());
            }
        } else
        {
            for (Map.Entry<String, String> entry : dictionary.entrySet())   // converting to latin
            {
                if (translatedInput.contains(entry.getValue()))
                    translatedInput = translatedInput.replaceAll(entry.getValue(), entry.getKey());
            }
        }
        return translatedInput;
    }

    public static Resources getLocalizedResources(Context context, String desiredLocale)
    {
        Locale locale = new Locale(desiredLocale);
        Configuration conf = context.getResources().getConfiguration();
        conf = new Configuration(conf);
        conf.setLocale(locale);
        Context localizedContext = context.createConfigurationContext(conf);
        return localizedContext.getResources();
    }

    //for getting String.xml value for desuredLocale
    public static String getLocalizedString(Context context, int id, String desiredLocale)
    {
        Resources resources = getLocalizedResources(context, desiredLocale);
        return resources.getString(id);
    }

    static
    {
        dictionary = new LinkedHashMap<>(); // put elements in insertion order
        dictionary.put("Dj", "Ђ");
        dictionary.put("Lj", "Љ");
        dictionary.put("Nj", "Њ");
        dictionary.put("Dž", "Џ");
        dictionary.put("A", "А");
        dictionary.put("B", "Б");
        dictionary.put("V", "В");
        dictionary.put("G", "Г");
        dictionary.put("D", "Д");
        dictionary.put("E", "Е");
        dictionary.put("Ž", "Ж");
        dictionary.put("Z", "З");
        dictionary.put("I", "И");
        dictionary.put("J", "Ј");
        dictionary.put("K", "К");
        dictionary.put("L", "Л");
        dictionary.put("M", "М");
        dictionary.put("N", "Н");
        dictionary.put("O", "О");
        dictionary.put("P", "П");
        dictionary.put("R", "Р");
        dictionary.put("S", "С");
        dictionary.put("T", "Т");
        dictionary.put("Ć", "Ћ");
        dictionary.put("U", "У");
        dictionary.put("F", "Ф");
        dictionary.put("H", "Х");
        dictionary.put("C", "Ц");
        dictionary.put("Č", "Ч");
        dictionary.put("Š", "Ш");

        dictionary.put("dj", "ђ");
        dictionary.put("lj", "љ");
        dictionary.put("nj", "њ");
        dictionary.put("dž", "џ");
        dictionary.put("a", "а");
        dictionary.put("b", "б");
        dictionary.put("v", "в");
        dictionary.put("g", "г");
        dictionary.put("d", "д");
        dictionary.put("e", "е");
        dictionary.put("ž", "ж");
        dictionary.put("z", "з");
        dictionary.put("i", "и");
        dictionary.put("j", "ј");
        dictionary.put("k", "к");
        dictionary.put("l", "л");
        dictionary.put("m", "м");
        dictionary.put("n", "н");
        dictionary.put("o", "о");
        dictionary.put("p", "п");
        dictionary.put("r", "р");
        dictionary.put("s", "с");
        dictionary.put("t", "т");
        dictionary.put("ć", "ћ");
        dictionary.put("u", "у");
        dictionary.put("f", "ф");
        dictionary.put("h", "х");
        dictionary.put("c", "ц");
        dictionary.put("č", "ч");
        dictionary.put("š", "ш");
    }
}
