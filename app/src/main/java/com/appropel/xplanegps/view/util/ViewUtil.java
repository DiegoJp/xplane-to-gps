package com.appropel.xplanegps.view.util;

import android.graphics.Rect;
import android.view.View;

/**
 * View utilities.
 */
public final class ViewUtil
{
    private ViewUtil()
    {
        // Utility class.
    }

    /**
     * Returns true if the two views intersect.
     * @param firstView first view
     * @param secondView second view
     * @return true if the given views intersect
     */
    public static boolean intersects(final View firstView, final View secondView)
    {
        int[] firstPosition = new int[2];
        int[] secondPosition = new int[2];

        firstView.getLocationOnScreen(firstPosition);
        secondView.getLocationOnScreen(secondPosition);

        // Rect constructor parameters: left, top, right, bottom
        Rect rectFirstView = new Rect(firstPosition[0], firstPosition[1],
                firstPosition[0] + firstView.getMeasuredWidth(), firstPosition[1] + firstView.getMeasuredHeight());
        Rect rectSecondView = new Rect(secondPosition[0], secondPosition[1],
                secondPosition[0] + secondView.getMeasuredWidth(), secondPosition[1] + secondView.getMeasuredHeight());
        return Rect.intersects(rectFirstView, rectSecondView);
    }
}
