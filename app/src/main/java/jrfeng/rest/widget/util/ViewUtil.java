package jrfeng.rest.widget.util;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;

public class ViewUtil {
    public static int measureSize(int spec, int defaultValue) {
        int mode = View.MeasureSpec.getMode(spec);
        int size = View.MeasureSpec.getSize(spec);

        int result = 0;
        switch (mode) {
            case View.MeasureSpec.EXACTLY:
                result = size;
                break;
            case View.MeasureSpec.AT_MOST:
                result = Math.min(size, defaultValue);
                break;
            case View.MeasureSpec.UNSPECIFIED:
                result = defaultValue;
        }

        return result;
    }

    public static int dpToPx(Context context, float dp) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics()
        ));
    }
}
