package de.adesso.mobile.android.sec2.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.webkit.MimeTypeMap;

public class Filehelper {

    public static String getMimeType(File file) {
        final String lastPathSegment = Uri.parse(file.getAbsolutePath()).getLastPathSegment().toLowerCase();
        final String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                MimeTypeMap.getFileExtensionFromUrl(lastPathSegment.lastIndexOf(".") != -1 ? lastPathSegment.substring(lastPathSegment.lastIndexOf("."))
                        : lastPathSegment));
        return mimeType != null ? mimeType : "";
    }

    public static Drawable mapMimeTypeDrawable(File file, Context context) {
        if (getMimeType(file).equals("application/vnd.android.package-archive")) {
            return getApkImage(file, context);
        }
        if (getMimeType(file).equals("text/plain")) return context.getResources().getDrawable(
                context.getResources().getIdentifier("file_txt", "drawable", context.getPackageName()));
        if (getMimeType(file).equals("image/jpeg")) {
            return new BitmapDrawable(IconCache.getInstance().getImageOrLoadingImage(file.getAbsolutePath()));
            //            return context.getResources().getDrawable(context.getResources().getIdentifier("file_image", "drawable", context.getPackageName()));
        }
        if (getMimeType(file).equals("image/jpg")) {
            return new BitmapDrawable(IconCache.getInstance().getImageOrLoadingImage(file.getAbsolutePath()));
            //            return context.getResources().getDrawable(context.getResources().getIdentifier("file_image", "drawable", context.getPackageName()));
        }
        if (getMimeType(file).equals("image/png")) {
            return new BitmapDrawable(IconCache.getInstance().getImageOrLoadingImage(file.getAbsolutePath()));
            //            return context.getResources().getDrawable(context.getResources().getIdentifier("file_image", "drawable", context.getPackageName()));
        }
        if (getMimeType(file).equals("audio/mpeg")) return context.getResources().getDrawable(
                context.getResources().getIdentifier("file_sound", "drawable", context.getPackageName()));
        if (getMimeType(file).equals("application/pdf")) return context.getResources().getDrawable(
                context.getResources().getIdentifier("file_txt2", "drawable", context.getPackageName()));
        if (getMimeType(file).equals("video/x-msvideo")) return context.getResources().getDrawable(
                context.getResources().getIdentifier("file_video", "drawable", context.getPackageName()));
        if (getMimeType(file).equals("video/mpeg")) return context.getResources().getDrawable(
                context.getResources().getIdentifier("file_video", "drawable", context.getPackageName()));
        if (getMimeType(file).equals("application/xhtml+xml")) return context.getResources().getDrawable(
                context.getResources().getIdentifier("file_html", "drawable", context.getPackageName()));
        if (getMimeType(file).equals("application/zip")) return context.getResources().getDrawable(
                context.getResources().getIdentifier("file_zip", "drawable", context.getPackageName()));
        if (getMimeType(file).equals("application/x-tar")) return context.getResources().getDrawable(
                context.getResources().getIdentifier("file_zip", "drawable", context.getPackageName()));
        return context.getResources().getDrawable(context.getResources().getIdentifier("file", "drawable", context.getPackageName()));
    }

    public static Drawable getApkImage(File file, Context context) {
        PackageInfo info = context.getPackageManager().getPackageArchiveInfo(file.getAbsolutePath(), 0);
        info.applicationInfo.sourceDir = file.getAbsolutePath();
        info.applicationInfo.publicSourceDir = file.getAbsolutePath();
        //        LogHelper.logE("info.applicationInfo.className: " + info.applicationInfo.className);
        //        LogHelper.logE("info.applicationInfo.sourceDir: " + info.applicationInfo.sourceDir);
        //        LogHelper.logE("info.applicationInfo.dataDir: " + info.applicationInfo.dataDir);
        //        LogHelper.logE("info.applicationInfo.name: " + info.applicationInfo.name);
        //        LogHelper.logE("info.applicationInfo.packageName: " + info.applicationInfo.packageName);
        //        LogHelper.logE("info.applicationInfo.publicSourceDir: " + info.applicationInfo.publicSourceDir);
        //        LogHelper.logE("info.applicationInfo.enabled: " + info.applicationInfo.enabled);
        //        LogHelper.logE(Uri.parse(file.getAbsolutePath()).getLastPathSegment() + " info.applicationInfo.icon: " + info.applicationInfo.icon);
        return context.getPackageManager().getApplicationIcon(info.applicationInfo);
    }

    public static Drawable getThumb(File file) {

        //        Bitmap thumb = IconCache.getInstance().getImage(file.getAbsolutePath());

        //        Bitmap inJustDecodeBoundsBitmap = null;
        //        BitmapFactory.Options opts = new BitmapFactory.Options();
        //        opts.inJustDecodeBounds = true;
        //        int targetWidth = 55;
        //        int targetHeight = 55;
        //        int targetSampleSize = 1;
        //        inJustDecodeBoundsBitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
        //        LogHelper.logE("file: " + Uri.parse(file.getAbsolutePath()).getLastPathSegment() + " opts.outWidth: " + opts.outWidth + " opts.outHeight: "
        //                + opts.outHeight);
        //        while (targetWidth * targetSampleSize < opts.outWidth && targetHeight * targetSampleSize < opts.outHeight) {
        //            targetSampleSize++;
        //        }
        //        targetSampleSize++;
        //        opts.inJustDecodeBounds = false;
        //        opts.inSampleSize = targetSampleSize;
        //        while (inJustDecodeBoundsBitmap == null) {
        //            try {
        //                inJustDecodeBoundsBitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
        //            } catch (OutOfMemoryError ome) {
        //                targetSampleSize++;
        //            }
        //        }
        //        LogHelper.logE("file: " + Uri.parse(file.getAbsolutePath()).getLastPathSegment() + " opts.outWidth: " + opts.outWidth + " opts.outHeight: "
        //                + opts.outHeight);
        //
        //        return new BitmapDrawable(inJustDecodeBoundsBitmap);

        return new BitmapDrawable(IconCache.getInstance().getImageOrLoadingImage(file.getAbsolutePath()));
    }

    public static Bitmap loadBitmap(String filePath) {
        Bitmap loadBitmap = null;
        int downsample = 1;
        while (loadBitmap == null) {
            try {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inSampleSize = downsample;
                loadBitmap = BitmapFactory.decodeFile(filePath, opts);
            } catch (OutOfMemoryError ome) {
                downsample++;
            }
        }
        return loadBitmap;
    }

    /**
     * saveBitmap
     */
    public static void saveBitmap(final Bitmap bitmap, final String path, final String fileName, final CompressFormat format) throws IOException {
        File dir = new File(path);
        dir.mkdirs();

        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(dir, fileName)));
        bitmap.compress(format, 90, bos);
        bos.flush();
        bos.close();
    }

    //    public static int mapMimeType(File file, Context context) {
    //        LogHelper.logE(context.getPackageName());
    //        if (getMimeType(file).equals("application/vnd.android.package-archive")) {
    //            getAPK(file, context);
    //            return context.getResources().getIdentifier("file", "drawable", context.getPackageName());
    //        }
    //        if (getMimeType(file).equals("text/plain")) return context.getResources().getIdentifier("file_txt", "drawable", context.getPackageName());
    //        if (getMimeType(file).equals("image/jpeg")) return context.getResources().getIdentifier("file_image", "drawable", context.getPackageName());
    //        if (getMimeType(file).equals("image/jpg")) return context.getResources().getIdentifier("file_image", "drawable", context.getPackageName());
    //        if (getMimeType(file).equals("image/png")) return context.getResources().getIdentifier("file_image", "drawable", context.getPackageName());
    //        if (getMimeType(file).equals("audio/mpeg")) return context.getResources().getIdentifier("file_sound", "drawable", context.getPackageName());
    //        if (getMimeType(file).equals("application/pdf")) return context.getResources().getIdentifier("file_txt2", "drawable", context.getPackageName());
    //        if (getMimeType(file).equals("video/x-msvideo")) return context.getResources().getIdentifier("file_video", "drawable", context.getPackageName());
    //        if (getMimeType(file).equals("video/mpeg")) return context.getResources().getIdentifier("file_video", "drawable", context.getPackageName());
    //        if (getMimeType(file).equals("application/xhtml+xml")) return context.getResources().getIdentifier("file_html", "drawable", context.getPackageName());
    //        if (getMimeType(file).equals("application/zip")) return context.getResources().getIdentifier("file_zip", "drawable", context.getPackageName());
    //        if (getMimeType(file).equals("application/x-tar")) return context.getResources().getIdentifier("file_zip", "drawable", context.getPackageName());
    //        return context.getResources().getIdentifier("file", "drawable", context.getPackageName());
    //    }

}
