package org.unibl.etf.blbustracker.phoneoptions;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import org.unibl.etf.blbustracker.R;

/**
 * sluzi za provjeravanje permisija npr Storage, Location ili neke koje se zadaju
 * takodje ima za provjeru da li je GPS ukljucen
 */
public class PermissionsHelper
{
    /**
     * Zatrazi permisije za sve permisije prosledjene kao arguement u String[] permissions
     *
     * @param context     getContext()
     * @param permissions niz trazenih permisija npr Manifest.permission.READ_EXTERNAL_STORAGE
     * @param code        neki broj koji jednoznacno oznacava permisiju
     */
    public static void requestPermission(@NonNull Context context, @NonNull String[] permissions, int code)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            ActivityCompat.requestPermissions((Activity) context, permissions, code);
        }
    }

    // slicno kao iznad samo se moze poslati iz fragmenta tj klasa koja je naslijedila fragment (umjesto getContext stavis this)
    public void requestPermission(@NonNull Fragment fragment, @NonNull String[] permissions, int code)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            fragment.requestPermissions(permissions, code);
        }
    }


    /**
     * provjerava da li su dozvoljene sve zadane permisije
     *
     * @param context
     * @param permissions permisije koje se testiraju da li su dozvoljene
     * @return true ako su SVE dozvoljene, u suprotnom false
     */
    public static boolean isPermissionGranted(@NonNull Context context, String[] permissions)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            int granted = -1;
            for (String permission : permissions)
            {
                granted = ActivityCompat.checkSelfPermission(context, permission);
            }
            return granted == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    // da li je dozvoljen Storage (true jeste, false nije)
    public static boolean isStorageGranted(@NonNull Context context)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            int read = ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.READ_EXTERNAL_STORAGE);
            int write = ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return read == PackageManager.PERMISSION_GRANTED &&
                    write == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    // zatrazi permisije za upotrebu Storage, argument getContext()
    public static void requestStorage(@NonNull Context context)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, PermissionCode.STORAGE_PERMISSIONS_CODE);
        }
    }

    // zatrazi permisije za upotrebu Storage, arguement 'this' iz klase koja naslijedjuje Fragment
    public static void requestStorage(@NonNull Fragment fragment)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            fragment.requestPermissions(
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, PermissionCode.STORAGE_PERMISSIONS_CODE);
        }
    }

    // provjerava da je dozvoljeno pracenje lokacija
    // true jeste, false nije
    public static boolean isLocationGranted(@NonNull Context context)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            int coarse = ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION);
            int fine = ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION);
            return coarse == PackageManager.PERMISSION_GRANTED &&
                    fine == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    // zatrazi permisije za pracenje lokacije
    public static void requestLocation(@NonNull Context context)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION}, PermissionCode.LOCATION_PERMISSIONS_CODE);
        }
    }

    // zatrazi permisije za pracenje lokacije
    public static void requestLocation(@NonNull Fragment fragment)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            fragment.requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION}, PermissionCode.LOCATION_PERMISSIONS_CODE);
        }
    }


    /**
     * zatrazi permisije za Storage uz poruku zasto ti trebaju
     *
     * @param fragment
     * @param title
     * @param message
     */
    public static void requestStorageWithAlert(@NonNull Fragment fragment, String title, String message)
    {
        if (fragment.getActivity() != null && ActivityCompat.shouldShowRequestPermissionRationale(fragment.getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE))
        {
            new AlertDialog.Builder(fragment.getActivity())
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(fragment.getString(R.string.ok_button), (dialog, which) ->   // sta se desava ako klikne oke
                    {
                        requestStorage(fragment);
                    })
                    .setNegativeButton(fragment.getString(R.string.cancel_button), (dialog, which) ->   // sta se desava ako klikne cancel
                    {
                        dialog.dismiss();
                    })
                    .create().show();
        } else
            requestStorage(fragment);

    }

    /**
     * zatrazi permisije za Location uz poruku zato ti trebaju
     *
     * @param fragment
     * @param title
     * @param message
     */
    public static void requestLocationWithAlert(@NonNull Fragment fragment, String title, String message)
    {
        if (fragment.getActivity() != null && ActivityCompat.shouldShowRequestPermissionRationale(fragment.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION))
        {
            new AlertDialog.Builder(fragment.getActivity())
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(fragment.getString(R.string.ok_button), (dialog, which) ->   // sta se desava ako klikne oke
                    {
                        requestLocation(fragment);
                    })
                    .setNegativeButton(fragment.getString(R.string.cancel_button), (dialog, which) ->   // sta se desava ako klikne cancel
                    {
                        dialog.dismiss();
                    })
                    .create().show();
        } else
            requestLocation(fragment);

    }


    /**
     * zatrazi da se upali GPS uz poruku
     *
     * @param context
     */
    public static void requestToEnableGPS(Context context)
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.enable_gps_title))
                .setMessage(context.getString(R.string.enable_gps_message))
                .setPositiveButton(context.getString(R.string.yes), (dialog, which) ->
                {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    context.startActivity(intent);
                })
                .setNegativeButton(context.getString(R.string.no), (dialog, which) -> dialog.cancel());

        alertDialog.show();
    }

    /**
     * Provjeri da li je GPS ukljucen
     *
     * @param context
     * @return true ako je ukljucen, false iskljucen
     */
    public static boolean isGPSEnabled(Context context)
    {
        final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            return true;

        return false;
    }

    /**
     * Provjerala da li Korisnim ima Google Play Services koje su potrebne za prikazivanje Google mape
     * Ukoliko nema instalirane, prikazi dialog prozor za instalaciju
     * @return true - user has play services, false user doesnt have play services
     */
    public static boolean isGoogleServicesOK(Activity activity)
    {

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity);

        if (available == ConnectionResult.SUCCESS)
        {
            //everything is fine and the user can make map requests
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available))
        {
            //an error occured but we can resolve it
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(activity, available, PermissionCode.ERROR_DIALOG_REQUEST);
            dialog.show();
        } else
        {
            Toast.makeText(activity, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

}
