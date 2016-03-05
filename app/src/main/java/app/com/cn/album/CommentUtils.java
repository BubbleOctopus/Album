package app.com.cn.album;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

/**
 * 作者：ArMn on 2016/3/5
 * 邮箱：859686819@qq.com
 */
public class CommentUtils {
    public static  final boolean isDebug = true;

    public static void d(String tag, String log) {
        if (isDebug)
            Log.d(tag, log);
    }


    public static void e(String tag, String log) {
        if (isDebug)
            Log.e(tag, log);
    }

    public static void paddingForStatusBar(View view, boolean isFixedSize) {
        if (isCanHaveTransparentDecor()) {
            int height = getStratusBarHeight(view.getContext());

            view.setPadding(view.getPaddingLeft(), view.getPaddingTop() + height,
                    view.getPaddingRight(), view.getPaddingBottom());

            if (isFixedSize) {
                view.getLayoutParams().height += height;
            }
        }
    }

    public static void marginForStatusBar(View view) {
        if (isCanHaveTransparentDecor()) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            marginLayoutParams.topMargin += getStratusBarHeight(view.getContext());
        }
    }

    public static void paddingForNavBar(View view) {
        if (isCanHaveTransparentDecor()) {
            int height = getNavBarHeight(view.getContext());
            view.setPadding(view.getPaddingLeft(), view.getPaddingTop(),
                    view.getPaddingRight(), view.getPaddingBottom() + height);
        }
    }

    public static void marginForNavBar(View view) {
        if (isCanHaveTransparentDecor()) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            marginLayoutParams.bottomMargin += getNavBarHeight(view.getContext());
        }
    }


    private static boolean isCanHaveTransparentDecor() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    private static int getStratusBarHeight(Context context) {
        return getDimenSize(context, "status_bar_height");
    }

    private static int getNavBarHeight(Context context) {
        boolean hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey();
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
        boolean hasNavBar = !hasMenuKey && !hasBackKey;

        if (hasNavBar) {
            boolean isPortrait = context.getResources().getConfiguration().orientation
                    == Configuration.ORIENTATION_PORTRAIT;

            boolean isTablet = (context.getResources().getConfiguration().screenLayout
                    & Configuration.SCREENLAYOUT_SIZE_MASK) >=
                    Configuration.SCREENLAYOUT_SIZE_LARGE;

            String key = isPortrait ? "navigation_bar_height" :
                    (isTablet ? "navigation_bar_height_landscape" : null);

            return key == null ? 0 : getDimenSize(context, key);
        } else {
            return 0;
        }
    }

    private static int getDimenSize(Context context, String key) {
        int resourceId = context.getResources().getIdentifier(key, "dimen", "android");
        return resourceId > 0 ? context.getResources().getDimensionPixelSize(resourceId) : 0;
    }
}
