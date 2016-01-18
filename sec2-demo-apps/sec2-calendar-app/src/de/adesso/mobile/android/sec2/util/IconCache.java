package de.adesso.mobile.android.sec2.util;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import de.adesso.mobile.android.sec2.R;
import de.adesso.mobile.android.sec2.debug.Debug;

/**
 * IconCache
 * @author mschmitz
 */
public class IconCache {

    private static final Class<?> c = IconCache.class;

    private static IconCache instance = null;

    private static final char[] DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    private Context context;
    public int density;
    private int densityDpi;

    /**
     * private default-constructor
     */
    private IconCache() {}

    /**
     * static method returns the only instance of this class
     */
    public static IconCache getInstance() {
        if (instance == null) {
            instance = new IconCache();
        }
        return instance;
    }

    /**
     * initialize
     * NOTE: must be called once, e.g. in Application.onCreate()
     */
    public void initialize(final Context context) {
        this.context = context;
        density = DisplayMetrics.DENSITY_MEDIUM; // workaround because all bitmaps are 74x74
        switch (density) {
            case DisplayMetrics.DENSITY_LOW:
                LogHelper.logI(c, "density: DENSITY_LOW");
                break;

            case DisplayMetrics.DENSITY_MEDIUM:
                LogHelper.logI(c, "density: DENSITY_MEDIUM");
                break;

            case DisplayMetrics.DENSITY_HIGH:
                LogHelper.logI(c, "density: DENSITY_HIGH");
                break;
        }

        densityDpi = context.getResources().getDisplayMetrics().densityDpi;
    }

    /**
     * drawableToBitmap
     */
    private Bitmap drawableToBitmap(final Drawable drawable) {
        return ((BitmapDrawable) drawable).getBitmap();
    }

    //---------------------------------------------------------------------------------------------

    /**
     * getLoadingImage
     */
    public Bitmap getLoadingImage() {
        return drawableToBitmap(context.getResources().getDrawable(R.drawable.loading));
    }

    /**
     * getImage
     */

    public Bitmap getImage(final String uri) {
        try {
            if (Debug.JUST_LOADINGICONS_ENABLED) {
                return getLoadingImage();
            }

            if (Debug.ICONCACHE_DISABLED) {
                Bitmap bmp = Filehelper.loadBitmap(uri);
                bmp.setDensity(density);
                return bmp;
            }

            return cache(uri);

        } catch (IOException e) {
            LogHelper.logW(c, e);
        }

        // this is the new fall-back for defect images
        return getLoadingImage();
    }

    /**
     * getImageOrLoadingImage
     */
    public Bitmap getImageOrLoadingImage(final String uri) {

        try {
            String fileName = encodeHex(uri.getBytes()) + ".png";
            //            String fileName = DigestUtils.md5Hex(uri) + ".png";
            //            String fileName = uri + ".png";
            //            String fileName = Uri.parse(uri).getLastPathSegment() + ".png";
            String path = context.getCacheDir().getAbsolutePath() + File.separatorChar;
            File file = new File(path + fileName);
            if (file.exists()) {
                Bitmap bitmap = Filehelper.loadBitmap(file.getAbsolutePath());
                bitmap.setDensity(density);
                return bitmap;
            }
        } catch (Exception ignored) {}

        return getLoadingImage();
    }

    //---------------------------------------------------------------------------------------------

    /**
     * cache
     */
    private Bitmap cache(final String uri) throws IOException {
        //        String fileName = DigestUtils.md5Hex(uri) + ".png";
        String fileName = encodeHex(uri.getBytes()) + ".png";
        //        String fileName = Uri.parse(uri).getLastPathSegment() + ".png";
        String path = context.getCacheDir().getAbsolutePath() + File.separatorChar;
        File file = new File(path + fileName);
        Bitmap bitmap = null;
        if (file.exists()) {
            //            LogHelper.logE(c, "File exists: " + path + fileName);
            bitmap = Filehelper.loadBitmap(file.getAbsolutePath());
        } else {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            int targetWidth = 60;
            int targetHeight = 60;
            //            int targetWidth = 55;
            //            int targetHeight = 55;
            int targetSampleSize = 1;
            bitmap = BitmapFactory.decodeFile(uri, opts);
            while (targetWidth * targetSampleSize < opts.outWidth && targetHeight * targetSampleSize < opts.outHeight) {
                targetSampleSize++;
            }
            targetSampleSize--;
            opts.inJustDecodeBounds = false;
            opts.inSampleSize = targetSampleSize;
            while (bitmap == null) {
                try {
                    bitmap = BitmapFactory.decodeFile(uri, opts);
                } catch (OutOfMemoryError ome) {
                    targetSampleSize++;
                }
            }

            Filehelper.saveBitmap(bitmap, path, fileName, CompressFormat.PNG);
            LogHelper.logE(c, "File written: " + path + fileName);
        }
        if (bitmap != null) {
            bitmap.setDensity(density);
        }
        return bitmap;
    }

    /*
     * getFolderSize
     * 
     * returns the size of a defined folder with 
     * subfolders
     */
    public static long getFolderSize(File dir) {
        long size = 0;
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                size += file.length();
            } else size += getFolderSize(file);
        }
        return size;
    }

    /*
     * clearCacheFolder
     * 
     * clears the cache folder
     */
    private static int clearCacheFolder(final File dir, final int numDays, final int maxFileSize) {

        int deletedFiles = 0;
        if (dir != null && dir.isDirectory()) {
            try {
                for (File child : dir.listFiles()) {
                    if (child.isDirectory()) {
                        deletedFiles += clearCacheFolder(child, numDays, maxFileSize);
                    }

                    if ((maxFileSize != -1) && (child.length() > maxFileSize)) {
                        if (child.delete()) {
                            deletedFiles++;
                            continue;
                        }
                    }

                    if (child.lastModified() < new Date().getTime() - numDays * DateUtils.DAY_IN_MILLIS) {
                        if (child.delete()) {
                            deletedFiles++;
                        }
                    }
                }
            } catch (Exception e) {
                LogHelper.logE(c, String.format("Failed to clean the cache, error %s", e.getMessage()));
            }
        }
        return deletedFiles;
    }

    /*
     * Delete the files older than numDays days from the application cache
     * 0 means all files.
     */
    public void clearCache(final Context context) {
        int numDays = 7;
        int numDeletedFiles = 0;
        long dirSize = getFolderSize(context.getCacheDir());
        LogHelper.logI(c, String.format("Cache size: %d", dirSize));

        if (dirSize < 500000) {
            LogHelper.logI(c, String.format("Cache prune: Size < 500kB. Nothing to do"));
            return;
        }

        if (dirSize > 5500000) { // Clear total cache
            LogHelper.logI(c, String.format("Starting cache prune, deleting all files"));
            numDeletedFiles = clearCacheFolder(context.getCacheDir(), 0, -1);
        } else if (dirSize > 4000000) {
            LogHelper.logI(c, String.format("Starting cache prune, deleting files > 12kB and older than %d days", numDays));
            numDeletedFiles = clearCacheFolder(context.getCacheDir(), numDays, 12000);
        } else if (dirSize > 2500000) {
            LogHelper.logI(c, String.format("Starting cache prune, deleting files > 120kB and older than %d days", numDays));
            numDeletedFiles = clearCacheFolder(context.getCacheDir(), numDays, 120000);
        } else {
            LogHelper.logI(c, String.format("Starting cache prune, deleting files older than %d days", numDays));
            numDeletedFiles = clearCacheFolder(context.getCacheDir(), numDays, -1);
        }
        LogHelper.logI(c, String.format("Cache pruning completed, %d files deleted", numDeletedFiles));
    }

    public static String encodeHex(byte[] data) {
        int l = data.length;

        char[] out = new char[l << 1];

        int i = 0;
        for (int j = 0; i < l; i++) {
            out[(j++)] = DIGITS[((0xF0 & data[i]) >>> 4)];
            out[(j++)] = DIGITS[(0xF & data[i])];
        }

        return new String(out);
    }
}
