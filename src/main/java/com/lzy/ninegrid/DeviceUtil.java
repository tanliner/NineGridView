package com.lzy.ninegrid;

import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

/**
 * @author tanlin
 * @version 1.0
 * @desc
 * @since 2021/3/4 14:39
 */
public class DeviceUtil {
    private DeviceUtil() {}
    public static void getDeviceSize(Context context, Point out) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) {
            return;
        }
        Display d = wm.getDefaultDisplay();
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        d.getMetrics(dm);
        out.x = dm.widthPixels;
        out.y = dm.heightPixels;
    }

    public static int getScreenWidth(Context context) {
        Point out = new Point();
        getDeviceSize(context, out);
        return out.x;
    }

    public static int getScreenHeight(Context context) {
        Point out = new Point();
        getDeviceSize(context, out);
        return out.y;
    }
}
