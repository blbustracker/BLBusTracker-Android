package org.unibl.etf.blbustracker.utils.languageutil;

import org.unibl.etf.blbustracker.ChoseLanguageActivity;

public abstract class LatinCyrillicUtil
{
    private static final Translator translator = new Translator();

    //convert input to latin and see if serverInput contains userInput
    public static boolean isMatched(String userInput, String serverInput)
    {
        userInput = userInput.trim();
        serverInput = serverInput.trim();

        if (!LatinUtils.isInputLatin(userInput))
        {
            userInput = translator.translateInput(userInput, ChoseLanguageActivity.LANGUAGE_SR);
        }
        userInput = LatinUtils.stripAccent(userInput);
        serverInput = LatinUtils.stripAccent(serverInput);

        return serverInput.toLowerCase().contains(userInput.toLowerCase());
    }
}

//        String translateUserInput;
//        String translateServerInput;
//        Translator translator = new Translator();
//        if (LatinUtils.isInputLatin(userInput))
//        {
//            translateUserInput = translator.translateInput(userInput, ChoseLanguageActivity.LANGUAGE_SR);
//            translateServerInput = translator.translateInput(serverInput, ChoseLanguageActivity.LANGUAGE_SR);
//
//            translateUserInput = LatinUtils.stripAccent(translateUserInput);
//            translateServerInput = LatinUtils.stripAccent(translateServerInput);
//        } else if (CyrillicUtils.isCyrillic(userInput))
//        {
//            translateUserInput = translator.translateInput(userInput, ChoseLanguageActivity.LANGUAGE_SR_CYRILLIC);
//            translateServerInput = translator.translateInput(serverInput, ChoseLanguageActivity.LANGUAGE_SR_CYRILLIC);
//        } else
//        {
//            translateUserInput = userInput;
//            translateServerInput = serverInput;
//        }
//        return translateServerInput.toLowerCase().contains(translateUserInput.toLowerCase());

