package com.jarvisyin.recorder.Common.Utils;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jarvisyin.recorder.AppContext;
import com.jarvisyin.recorder.R;

/**
 * Created by yeyq on 15/5/18.
 */
public class JYToast {

    public static void show(String msg) {
        Toast toast = new Toast(AppContext.getApp());
        View v = LayoutInflater.from(AppContext.getApp()).inflate(R.layout.layout_toast, null);
        TextView tv = (TextView) v.findViewById(R.id.message);
        tv.setText(msg);
        toast.setView(v);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }

    public static void show(int resId) {
        String msg = AppContext.getApp().getResources().getString(resId);
        Toast toast = Toast.makeText(AppContext.getApp(), msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        LinearLayout toastView = (LinearLayout) toast.getView();
        //toastView.setBackgroundResource(R.drawable.shape_rectangle_black_alpha);
        toast.show();
    }
}
