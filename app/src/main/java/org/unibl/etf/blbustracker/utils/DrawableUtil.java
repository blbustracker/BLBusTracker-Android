package org.unibl.etf.blbustracker.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import org.unibl.etf.blbustracker.R;
import org.unibl.etf.blbustracker.datahandlers.database.route.Route;

public abstract class DrawableUtil
{
    private static final int MAX_ALPHA = 255;

    //change background color of the bus drawable to matche route color
    public static Drawable getColoredBusDrawable(Route currentData, Context context)
    {
        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.bus_tracking_icon);
        LayerDrawable layerDrawable = (LayerDrawable) drawable;
        GradientDrawable gradientDrawable = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.bus_background);

        int busColorId = ColorUtils.setAlphaComponent(Color.parseColor(currentData.getColor()), MAX_ALPHA); // alpha == transparancy
        gradientDrawable.setColor(busColorId); // change color
        return drawable;
    }


    public static BitmapDescriptor resizeDrawable(int drawableId, int dimension , Context context)
    {
        Drawable image = ContextCompat.getDrawable(context, drawableId);
        Bitmap b = ((BitmapDrawable) image).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b,dimension , dimension, false);
        return BitmapDescriptorFactory.fromBitmap(bitmapResized);
    }
}
