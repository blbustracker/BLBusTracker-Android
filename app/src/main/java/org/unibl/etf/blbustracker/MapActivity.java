package org.unibl.etf.blbustracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.akexorcist.localizationactivity.ui.LocalizationActivity;
import com.google.android.gms.security.ProviderInstaller;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import org.unibl.etf.blbustracker.phoneoptions.LocaleManager;
import org.unibl.etf.blbustracker.uncaughtexceptionhandler.CustomUncaughtExceptionHandler;
import org.unibl.etf.blbustracker.uncaughtexceptionhandler.ReportCrash;
import org.unibl.etf.blbustracker.utils.KeyboardUtils;
import org.unibl.etf.blbustracker.utils.Utils;

public class MapActivity extends LocalizationActivity implements DrawerLayout.DrawerListener
{
    // used in method onSupportNavigateUp(...)
    private AppBarConfiguration mAppBarConfiguration;

    //needed for implementing open/close navigation drawer
    private DrawerLayout drawerLayout;

    NavigationView navigationView;

    //Button for openning/closing navigation drawer
    private FloatingActionButton drawerFloatingButton;

    /**
     * this method is only called when application is started (it's not called when it is minimized or when tab is changed)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        String locale = checkForSavedLocale();
        if (locale != null)
            setLanguage(locale);

        super.onCreate(savedInstanceState);
        //        checkProviderInstaller(); // for fixing volley on android <=4.4

        ReportCrash.sendReportToServer(this);   // check if there was a crash in privious run
        Thread.setDefaultUncaughtExceptionHandler(new CustomUncaughtExceptionHandler(this));    // start watching for crashes

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        initToolbar();

        drawerLayout = findViewById(R.id.drawer_layout);  //init drawerLayour
        drawerLayout.addDrawerListener(this);   // onDrawerOpened close/hide keyboard

        initFloatingDrawerButton(drawerLayout);

        this.navigationView = findViewById(R.id.nav_view);

        //Passing each menu ID as a set of Ids because each
        //menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_map_view, R.id.nav_announcements, R.id.nav_route_schedule,
                R.xml.fragment_settings, R.id.nav_report_problem, R.id.nav_about)
                .setOpenableLayout(drawerLayout)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController); // use implemented listener

    }

    private void initToolbar()
    {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setVisibility(View.GONE); //hide toolbar
        toolbar.setNavigationIcon(R.drawable.ic_back_button);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
    }

    /**
     * checking language sent from ChoseLanguageActivity
     */
    private String checkForSavedLocale()
    {
        SharedPreferences sharedPreferences = Utils.getSharedPreferences(getBaseContext());
        String locale = sharedPreferences.getString(LocaleManager.SELECTED_LANGUAGE, "");

        if (!"".equals(locale))
            return locale;

        Intent intent = getIntent();
        locale = intent.getStringExtra(LocaleManager.SELECTED_LANGUAGE);

        return locale;
    }

    // initialize floating button for navigation drawer (open/close drawer)
    private void initFloatingDrawerButton(DrawerLayout drawer)
    {
        drawerFloatingButton = findViewById(R.id.drawer_fab);
        drawerFloatingButton.setOnClickListener(view ->
        {
            if (drawer.isDrawerOpen(GravityCompat.START))
            {
                drawer.closeDrawer(GravityCompat.START);
            } else
            {
                drawer.openDrawer(GravityCompat.START);
            }
        });
    }

    /**
     * If Navigation Drawer is open, close it when "back button" is pressed, otherwise close the app
     */
    @Override
    public void onBackPressed()
    {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
        {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else
        {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    /**
     * for android version < 4.4
     */
    private void checkProviderInstaller()
    {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH)
        {
            try
            {
                ProviderInstaller.installIfNeeded(this);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDrawerSlide(@NonNull View drawerView, float slideOffset)
    {
    }

    @Override
    public void onDrawerOpened(@NonNull View drawerView)
    {
        KeyboardUtils.hideKeyboard(drawerView);
    }

    @Override
    public void onDrawerClosed(@NonNull View drawerView)
    {
    }

    @Override
    public void onDrawerStateChanged(int newState)
    {
    }


}
