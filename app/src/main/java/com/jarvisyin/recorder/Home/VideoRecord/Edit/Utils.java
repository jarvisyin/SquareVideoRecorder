package com.jarvisyin.recorder.Home.VideoRecord.Edit;

import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by jarvisyin on 16/12/5.
 */
public class Utils {

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


}
