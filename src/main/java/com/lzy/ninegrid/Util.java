package com.lzy.ninegrid;

import android.graphics.Point;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author tanlin
 * @version 1.0
 * @desc
 * @since 2020-02-18
 */
public class Util {
    private Util() {}

    /**
     *
     * @param nineGridView grid-view
     * @param size the width and height of the single image
     * @return the layout dimens of pixels
     */
    public static Point handleSingleImage(NineGridView nineGridView, Point size) {
        int w = size.x;
        int h = size.y;
        int nW = w;
        int nH = h;

        int maxSingle = nineGridView.getSingleDefWidth();
        int maxNineGridWidth = nineGridView.getGridMaxWidth();
        int spacing = nineGridView.getGridSpacing();

        /*
         * 1/3 > ratio
         * 1/3 <= ratio < 1
         * 1 <= ratio <= 3
         * 3 < ratio
         */
        final float ratio = w * 1.0F / h;
        final float baseRatio = 1.0F / 3;
        if (ratio < baseRatio) {
            nW = (int) (maxSingle * baseRatio);
            nH = maxSingle;
        } else if (ratio >= baseRatio && ratio < 1) {
            nW = fixFloat(maxSingle * ratio);
            nH = maxSingle;
        } else if (ratio >= 1 && ratio <= 3) {
            nW = maxSingle;
            nH = fixFloat(maxSingle / ratio);
        } else {
            nW = maxNineGridWidth;
            nH = fixFloat((maxNineGridWidth - spacing * 2.0F) / 3);
        }
        return new Point(nW, nH);
    }

    private static int fixFloat(float value) {
        return (int)(value + 0.5F);
    }

    /**
     *
     * @param target view
     * @param newWidth new width
     * @param newHeight new height
     */
    public static void updateViewSize(View target, int newWidth, int newHeight) {
        ViewGroup.LayoutParams lp = target.getLayoutParams();
        if (lp != null) {
            lp.width = newWidth;
            lp.height = newHeight;
        }
        target.setLayoutParams(lp);
    }
}
