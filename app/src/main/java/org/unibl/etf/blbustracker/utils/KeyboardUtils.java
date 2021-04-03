package org.unibl.etf.blbustracker.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public abstract class KeyboardUtils
{

    /**
     * Hide/Close Keyboard
     */
    public static void hideKeyboard(View view)
    {
        if (view == null)
            return;
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }

    //Note: view can also be a EditText or AutoCompleteEditText,...
    public static void showKeyBoard(View view)
    {
        if (view == null)
            return;
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
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
