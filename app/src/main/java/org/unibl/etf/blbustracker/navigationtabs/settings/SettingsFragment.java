package org.unibl.etf.blbustracker.navigationtabs.settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.akexorcist.localizationactivity.core.LocalizationActivityDelegate;

import org.unibl.etf.blbustracker.ChoseLanguageActivity;
import org.unibl.etf.blbustracker.R;
import org.unibl.etf.blbustracker.uncaughtexceptionhandler.CustomUncaughtExceptionHandler;

/**
 * layout is located in /res/xml/fragment_settings
 */
public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private LocalizationActivityDelegate localizationDelegate;
    private Activity activity;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public static final String SELECTED_MAP_TYPE = "selected_map_type";
    public static final String SELECTED_MAP_STYLE = "selected_map_style";
    public static final String COLLECT_REPORT = "collect_report";
    public static final String AUTO_SEND_REPORT = "auto_send_report";

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);

        if (context instanceof Activity)
        {
            activity = (Activity) context;
        } else
            activity = getActivity();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        setPreferencesFromResource(R.xml.fragment_settings, rootKey);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        setMapStyleVisibility();
        setAutoReportVisibility();

        localizationDelegate = new LocalizationActivityDelegate(activity);

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ((AppCompatActivity) activity).getSupportActionBar().show();
        return view;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        editor = sharedPreferences.edit();

        switch (key)
        {
            case ChoseLanguageActivity.SELECTED_LANGUAGE: // change language
                String selectedLocale = sharedPreferences.getString(ChoseLanguageActivity.SELECTED_LANGUAGE, "");
                editor.putString(ChoseLanguageActivity.SELECTED_LANGUAGE, selectedLocale);
                editor.commit();
                localizationDelegate.setLanguage(activity, selectedLocale);
                activity.recreate();
                break;

            case SELECTED_MAP_TYPE: // change map type
                String mapType = setMapStyleVisibility();
                editor.putString(SELECTED_MAP_TYPE, mapType);
                break;

            case SELECTED_MAP_STYLE: //change map style
                String mapStyle = sharedPreferences.getString(SELECTED_MAP_STYLE, "normal");
                editor.putString(SELECTED_MAP_STYLE, mapStyle);
                break;

            case COLLECT_REPORT:

                boolean isCollectReportChecked = setAutoReportVisibility();
                initReportWatcher(isCollectReportChecked);
                editor.putBoolean(COLLECT_REPORT, isCollectReportChecked);
                break;

            case AUTO_SEND_REPORT:

                boolean isAutoSendChecked = sharedPreferences.getBoolean(AUTO_SEND_REPORT, false);
                editor.putBoolean(AUTO_SEND_REPORT, isAutoSendChecked);
                break;
        }
        editor.apply();
    }

    private void initReportWatcher(boolean isCollectReportChecked)
    {
        if (isCollectReportChecked)
            Thread.setDefaultUncaughtExceptionHandler(new CustomUncaughtExceptionHandler(getContext()));
        else
            Thread.setDefaultUncaughtExceptionHandler(null);
    }

    private String setMapStyleVisibility()
    {
        String mapType = sharedPreferences.getString(SELECTED_MAP_TYPE, "1");
        if ("1".equals(mapType))
            findPreference(SELECTED_MAP_STYLE).setEnabled(true);
        else
            findPreference(SELECTED_MAP_STYLE).setEnabled(false);

        return mapType;
    }

    private boolean setAutoReportVisibility()
    {
        boolean isCollectReportChecked = sharedPreferences.getBoolean(COLLECT_REPORT, true);
        if (isCollectReportChecked)
            findPreference(AUTO_SEND_REPORT).setEnabled(true);
        else
            findPreference(AUTO_SEND_REPORT).setEnabled(false);

        return isCollectReportChecked;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setVisibility(View.VISIBLE);
        toolbar.setNavigationOnClickListener(v -> {
            Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.nav_map_view);
        });
    }

    @Override
    public void onStop()
    {
        super.onStop();
        getActivity().findViewById(R.id.toolbar).setVisibility(View.GONE);
    }

    @Keep
    public SettingsFragment()
    {

    }

}
