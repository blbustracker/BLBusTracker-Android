package org.unibl.etf.blbustracker.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;

import androidx.annotation.NonNull;

public abstract class KeyboardUtils
{

    //get users keyboard input locale
    public static String getKeyboardLocale(Context context)
    {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        InputMethodSubtype ims = imm.getCurrentInputMethodSubtype();

        String result;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M)
            result = ims.getLanguageTag();
        else
            result = ims.getLocale();

        return result;
    }

    /**
     * Hide/Close Keyboard
     */
    public static void hideKeyboard(@NonNull View view)
    {
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Makes Listener for EditText, which hides/closes keyboard when EditText loses focus
     * (example: user is typing something, then map was clicked, EditText loses focus, call this listener to hide keyboard
     *
     * @return OnFocusChangeListener that closes mobile keyboard
     */
    public static View.OnFocusChangeListener getOnFocusHideKeyboardListener()
    {
        return (view, hasFocus) ->
        {
            if (!hasFocus)
                hideKeyboard(view);
        };
    }
}
