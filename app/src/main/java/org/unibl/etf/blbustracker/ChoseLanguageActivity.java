package org.unibl.etf.blbustracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.unibl.etf.blbustracker.phoneoptions.LocaleManager;
import org.unibl.etf.blbustracker.utils.Utils;

//Activity for choosing language on start of the application
public class ChoseLanguageActivity extends AppCompatActivity implements View.OnClickListener
{
    private SharedPreferences sharedPreferences;
    private RadioGroup radioGroup;
    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        sharedPreferences = Utils.getSharedPreferences(getBaseContext());
        String savedLocale = sharedPreferences.getString(LocaleManager.SELECTED_LANGUAGE, "");
        if(!"".equals(savedLocale))
        {
            startActivityWithLocale(savedLocale);
            return;

        }
        setContentView(R.layout.activity_chose_language);

        radioGroup = (RadioGroup) findViewById(R.id.radio_group);
        radioGroup.check(R.id.english);
        submitButton = (Button) findViewById(R.id.submit);
        submitButton.setOnClickListener(this);

        TextView selectLangeuge = (TextView) findViewById(R.id.select_language_text);
        ((RadioButton)findViewById(R.id.english)).setOnClickListener( v -> {
            submitButton.setText("submit");
            selectLangeuge.setText("Select language");
        });
        ((RadioButton)findViewById(R.id.srpski)).setOnClickListener( v -> {
            submitButton.setText("izaberi");
            selectLangeuge.setText("Izaberite jezik");
        });
        ((RadioButton)findViewById(R.id.srpski_cry)).setOnClickListener( v -> {
            submitButton.setText("изабери");
            selectLangeuge.setText("Изаберите језик");
        });

    }

    @Override
    public void onClick(View v)
    {
        int radioButtonId = radioGroup.getCheckedRadioButtonId();
        RadioButton radioButton=findViewById(radioButtonId);
        String language = radioButton.getText().toString();

        String locale;
        switch (language)
        {
            case LocaleManager.LANGUAGE_EN_NAME:
                locale = "en";
                break;
            case LocaleManager.LANGUAGE_SR_NAME:
                locale = "bs";
                break;
            default:
                locale = "sr";
        }

        saveLocale(locale);
        startActivityWithLocale(locale);
    }


    /**
     * Start MapActivity with the given language
     * @param locale language for MapActivity
     */
    private void startActivityWithLocale(String locale)
    {
        Intent intent = new Intent(getApplicationContext(), MapActivity.class);
        intent.putExtra(LocaleManager.SELECTED_LANGUAGE, locale);
        startActivity(intent);
        finish();   //turning off this activity, so you cant get back here
    }

    /**
     * Save the given language
     * @param locale
     */
    private void saveLocale(String locale)
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(LocaleManager.SELECTED_LANGUAGE,locale);
        editor.commit();
    }

    @Override
    protected void onDestroy()
    {
        submitButton =null;
        sharedPreferences=null;
        radioGroup=null;
        super.onDestroy();
    }

}
