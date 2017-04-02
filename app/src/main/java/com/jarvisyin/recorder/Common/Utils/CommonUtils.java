package com.jarvisyin.recorder.Common.Utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.jarvisyin.recorder.Common.Utils.Trinea.DigestUtils;
import com.jarvisyin.recorder.R;

import java.io.File;
import java.io.FileOutputStream;


/**
 * Created by jarvisyin on 16/2/19.
 */
public class CommonUtils {

    /**
     * 提供点击事件,点击后 activity.finish();
     *
     * @param activity
     * @return
     */
    public static View.OnClickListener getFinishActivityListener(final Activity activity) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
            }
        };
    }

    /**
     * 获取视频文件的第一张图片
     *
     * @param filePath
     * @return
     */
    public static Bitmap getVideoThumbnail(String filePath) {
        if (TextUtils.isEmpty(filePath)) return null;
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    public static String pwdEncrypt(String pwd) {
        return DigestUtils.md5(pwd + "z3AFKGsQEBcVwm4ycNXxKt1mi=lSH=,X");
    }



    /**
     * 账号每4位用空格分开（不加*号）
     *
     * @param number
     * @return
     */
    public static String cardNumberWithSpace(String number) {
        if (number == null || number.length() < 5) {
            return number;
        }
        StringBuffer sb = new StringBuffer();
        sb.append(String.valueOf(number));
        for (int i = 0, length = number.length() / 4 + 1; i < length; i++) {
            if (i != 0) {
                sb.insert(4 * i + i - 1, " ");
            }
        }
        return sb.toString();
    }

    /**
     * 保存图片
     *
     * @param fileName      图片名字
     * @param fileParentDir 图片所在目录的路径
     * @param bitmap        需保存的图片
     */
    public static void saveImage(String fileName, String fileParentDir, Bitmap bitmap) throws Exception {

        File f = new File(fileParentDir, fileName);
        if (f.exists()) {
            f.delete();
        }

        FileOutputStream out = new FileOutputStream(f);
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
        out.flush();
        out.close();

    }

    /**
     * 获得当前进程的名字
     *
     * @param context
     * @return 进程号
     */
    public static String getCurProcessName(Context context) {

        int pid = android.os.Process.myPid();

        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager
                .getRunningAppProcesses()) {

            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    public static void shakeEditText(FragmentActivity mActivity, TextView editText) {
        Animation shake = AnimationUtils.loadAnimation(mActivity, R.anim.shake);//加载动画资源文件
        ((View) editText.getParent()).startAnimation(shake); //给组件播放动画效果
    }


}
