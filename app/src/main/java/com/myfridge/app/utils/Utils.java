package com.myfridge.app.utils;

import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.sql.Date;
import java.util.Locale;

public class Utils {

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String parseData(long time){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        TimeZone tz = TimeZone.getDefault();
        calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Date currenTimeZone = new Date(calendar.getTimeInMillis());

        return sdf.format(currenTimeZone);
    }


}
