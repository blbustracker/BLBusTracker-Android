package org.unibl.etf.blbustracker.utils;

import android.app.Activity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;

public abstract class KeyboardUtils
{

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
