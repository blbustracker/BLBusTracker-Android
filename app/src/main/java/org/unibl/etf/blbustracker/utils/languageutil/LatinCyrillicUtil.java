package org.unibl.etf.blbustracker.utils.languageutil;

public class LatinCyrillicUtil
{
    //convert both inputs to latin and try to match them
    public static boolean isMatched(String userInput, String serverInput)
    {
        if (userInput == null || serverInput == null)
            return false;

        userInput = userInput.trim();
        serverInput = serverInput.trim();

        String latinServerInput = LatinUtils.stripAccent(serverInput);
        String latinUserInput;

        if (LatinUtils.isInputLatin(userInput))
            latinUserInput = LatinUtils.stripAccent(userInput);
        else if (CyrillicUtils.isCyrillic(userInput))
            latinUserInput = CyrillicUtils.convertCyrToLat(userInput);
        else
            latinUserInput = userInput;

        return latinServerInput.contains(latinUserInput);
    }
}
