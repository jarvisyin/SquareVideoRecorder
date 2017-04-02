package com.jarvisyin.recorder.Common.Widget.ActionBar;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * 除了 VideoPlayerFragment, PersonalFragment, GoodDetailFragment
 * SellerFragment,ShopFragment外的页面都显示为:
 * 0xFF000000.(若原本为白底黑字?)
 *
 * VideoPlayerFragment, PersonalFragment显示为透明(PS:5.0一下的原生系统将会呈现出渐变的效果)
 *
 * GoodDetailFragment, SellerFragment, ShopFragment随着滚动,渐渐地有透明变成黑色
 *
 * Created by jarvisyin on 16/8/17.
 */
public class TopView extends View {
    public static int statusBarHeight = 0;


    public TopView(Context context) {
        super(context);
        init(context);
    }

    public TopView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TopView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        if (statusBarHeight == 0
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                ) {
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                //statusBarHeight = getResources().getDimensionPixelSize(resourceId);
            }
        }
        if (getBackground() == null) {
            setBackgroundColor(0xFF000000);
        }
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        params.height = statusBarHeight;
        super.setLayoutParams(params);
    }
}
