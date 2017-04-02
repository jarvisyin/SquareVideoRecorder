package com.jarvisyin.recorder.Home.VideoRecord.Common.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;

import com.jarvisyin.recorder.Common.Utils.JYLog;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jarvisyin on 16/5/16.
 */
public class VideoUtils {

    private static String TAG = "VideoUtils";

    /**
     * RenderScript 实现高斯模糊,速度快,但在 OpenGL ES 初始化后使用时会抛异常.
     *
     * @param context
     * @param bitmap
     * @param radius
     * @return
     */
    public static Bitmap gaussBlurBitmap(Context context, Bitmap bitmap, float radius) {

        //Let's create an empty bitmap with the same size of the bitmap we want to blur
        //Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Bitmap outBitmap = Bitmap.createBitmap(640, 640, Bitmap.Config.ARGB_8888);

        //Instantiate a new Renderscript
        RenderScript rs = RenderScript.create(context);

        //Create an Intrinsic Blur Script using the Renderscript
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

        //Create the Allocations (in/out) with the Renderscript and the in/out bitmaps
        Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
        Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);

        //Set the radius of the blur
        blurScript.setRadius(radius);

        //Perform the Renderscript
        blurScript.setInput(allIn);
        blurScript.forEach(allOut);

        //Copy the final bitmap created by the out Allocation to the outBitmap
        allOut.copyTo(outBitmap);

        //recycle the original bitmap
        bitmap.recycle();

        //After finishing everything, we destroy the Renderscript.
        rs.destroy();

        return outBitmap;
    }


    /**
     * Java实现高斯模糊,速度慢
     *
     * @param sentBitmap
     * @param radius
     * @param canReuseInBitmap
     * @return
     */
    public static Bitmap gaussBlurBitmap(Bitmap sentBitmap, int radius, boolean canReuseInBitmap) {
        // Stack Blur v1.0 from
        // http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
        //
        // Java Author: Mario Klingemann <mario at quasimondo.com>
        // http://incubator.quasimondo.com
        // created Feburary 29, 2004
        // Android port : Yahel Bouaziz <yahel at kayenko.com>
        // http://www.kayenko.com
        // ported april 5th, 2012

        // This is a compromise between Gaussian Blur and Box blur
        // It creates much better looking blurs than Box Blur, but is
        // 7x faster than my Gaussian Blur implementation.
        //
        // I called it Stack Blur because this describes best how this
        // filter works internally: it creates a kind of moving stack
        // of colors whilst scanning through the image. Thereby it
        // just has to add one new block of color to the right side
        // of the stack and remove the leftmost color. The remaining
        // colors on the topmost layer of the stack are either added on
        // or reduced by one, depending on if they are on the right or
        // on the left side of the stack.
        //
        // If you are using this algorithm in your code please add
        // the following line:
        //
        // Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>

        Bitmap bitmap;
        if (canReuseInBitmap) {
            bitmap = sentBitmap;
        } else {
            bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
        }

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }


    public static Bitmap getBitmap(String imagePath, int toLen) {
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inJustDecodeBounds = true;  //设置为true，只读尺寸信息，不加载像素信息到内存
        BitmapFactory.decodeFile(imagePath, option);  //此时bitmap为空
        option.inJustDecodeBounds = false;
        int bWidth = option.outWidth;
        int bHeight = option.outHeight;

        if (bWidth == bHeight) {
            toLen = toLen * 5 / 4;
        }

        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;// be = 1表示不缩放
        if (bWidth > toLen && bHeight > toLen) {
            int i;
            if (bWidth < bHeight) {
                i = bWidth;
            } else {
                i = bHeight;
            }
            be = i * 2 / toLen;
        }

        if (be < 1)
            be = 1;
        option.inMutable = true;
        option.inSampleSize = be; //设置缩放比例
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, option);
        //Log.i(TAG, "bWidth = " + bWidth + "  bHeight = " + bHeight + "   bitmap.getWidth() = " + bitmap.getWidth() + "   bitmap.getHeight() = " + bitmap.getHeight() + "   option.inSampleSize = " + option.inSampleSize + "  be = " + be);

        bWidth = bitmap.getWidth();
        bHeight = bitmap.getHeight();
        Bitmap result;

        if (bWidth == bHeight) {
            int xy = (int) (bWidth * 0.25f * 0.5f + 0.5f);
            int l = (int) (bWidth * 0.75f + 0.5f);
            result = Bitmap.createBitmap(bitmap, xy, xy, l, l);
        } else if (bWidth < bHeight) {
            result = Bitmap.createBitmap(bitmap, 0, bHeight - bWidth, bWidth, bWidth);
        } else {
            Log.i(TAG, " x = " + (bWidth - bHeight) + "  y = " + 0 + "   width = " + bWidth + "   bHeight = " + bHeight);
            result = Bitmap.createBitmap(bitmap, bWidth - bHeight, 0, bHeight, bHeight);
        }
        bitmap.recycle();

        return result;
    }

    public static Bitmap getBitmap(String imagePath) {
        return getBitmap(imagePath, 640);
    }

    public static Bitmap getBitmap(Context context, int resId, int toLen) {
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inJustDecodeBounds = true;  //设置为true，只读尺寸信息，不加载像素信息到内存
        BitmapFactory.decodeResource(context.getResources(), resId, option);  //此时bitmap为空
        option.inJustDecodeBounds = false;
        int bWidth = option.outWidth;
        int bHeight = option.outHeight;

        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;// be = 1表示不缩放
        if (bWidth > toLen && bHeight > toLen) {
            int i;
            if (bWidth < bHeight) {
                i = bWidth;
            } else {
                i = bHeight;
            }
            be = i * 2 / toLen;
        }

        if (be < 1)
            be = 1;
        option.inMutable = true;
        option.inSampleSize = be; //设置缩放比例
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId, option);
        return bitmap;
    }

    public static Bitmap getTextBitmap(String text) {
        Bitmap bitmap = Bitmap.createBitmap(600, 85, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(69);
        paint.setColor(0xffffffff);
        canvas.drawText(text, 0, 70, paint);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return bitmap;
    }


    public static Bitmap convert(Bitmap a) {

        int w = a.getWidth();
        int h = a.getHeight();

        Bitmap newb = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);// 创建一个新的和SRC长度宽度一样的位图
        Canvas cv = new Canvas(newb);
        android.graphics.Matrix m = new android.graphics.Matrix();
        m.postScale(1, -1);   //镜像垂直翻转

        Bitmap new2 = Bitmap.createBitmap(a, 0, 0, w, h, m, true);
        cv.drawBitmap(new2, new Rect(0, 0, new2.getWidth(), new2.getHeight()), new Rect(0, 0, w, h), null);

        return newb;
    }

    /**
     * Returns the first codec capable of encoding the specified MIME type, or null if no match was
     * found.
     */
    public static MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);

            if (!codecInfo.isEncoder()) {
                continue;
            }

            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }

    public static int getAndSelectVideoTrackIndex(MediaExtractor extractor) {
        for (int index = 0; index < extractor.getTrackCount(); ++index) {
            MediaFormat format = extractor.getTrackFormat(index);
            if (getMimeTypeFor(format).startsWith("video/")) {
                extractor.selectTrack(index);
                return index;
            }
        }
        return -1;
    }

    public static String getMimeTypeFor(MediaFormat format) {
        return format.getString(MediaFormat.KEY_MIME);
    }

    public static Map<String, String> getMediaInfo(MediaFormat format) {
        Map<String, String> map = new HashMap<>();
        try {

            String infoStr = format.toString();
            String[] infos = infoStr.split(",");

            JYLog.i(TAG, infoStr);

            for (String str : infos) {
                if (!str.contains("=")) {
                    continue;
                }

                String[] kv = str.split("=");
                if (kv.length != 2) continue;

                map.put(kv[0].trim(), kv[1].trim());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public static int[] getVideoWH(MediaFormat format) {
        int[] wh = {-1, -1};
        int rotation = 0;

        try {
            wh[0] = format.getInteger(MediaFormat.KEY_WIDTH);
            wh[1] = format.getInteger(MediaFormat.KEY_HEIGHT);
            // TODO: 16/10/19 KEY_ROTATION is not exist in v22 
//            rotation = format.getInteger(MediaFormat.KEY_ROTATION);
            rotation = format.getInteger("rotation");

            if (rotation % 180 == 90) {
                int m = wh[0];
                wh[0] = wh[1];
                wh[1] = m;
            }
            return wh;
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            wh[0] = format.getInteger("width");
            wh[1] = format.getInteger("height");
            rotation = format.getInteger("rotation-degrees");

            if (rotation % 180 == 90) {
                int m = wh[0];
                wh[0] = wh[1];
                wh[1] = m;
            }
            return wh;
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            String infoStr = format.toString();
            String[] infos = infoStr.split(",");

            JYLog.i(TAG, infoStr);

            for (String str : infos) {
                if (!str.contains("=")) {
                    continue;
                }

                String[] kv = str.split("=");
                if (kv.length != 2) continue;

                if ("width".equals(kv[0].trim())) {
                    wh[0] = Integer.valueOf(kv[1].trim());
                } else if ("height".equals(kv[0].trim())) {
                    wh[1] = Integer.valueOf(kv[1].trim());
                } else if ("rotation-degrees".equals(kv[0].trim())) {
                    rotation = Integer.valueOf(kv[1].trim());
                }
            }

            if (wh[0] == -1 || wh[1] == -1) return null;

            if (rotation % 180 == 90) {
                int m = wh[0];
                wh[0] = wh[1];
                wh[1] = m;
            }

            return wh;
        } catch (Exception e) {
            e.printStackTrace();
        }


        return null;
    }

}
