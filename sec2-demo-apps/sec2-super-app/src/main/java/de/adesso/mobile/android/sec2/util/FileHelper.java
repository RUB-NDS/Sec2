
package de.adesso.mobile.android.sec2.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.zip.GZIPOutputStream;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.util.Log;
import android.webkit.MimeTypeMap;

/**
 * 
 * a helper class to help with file operations and handling access to the android mediastore
 * 
 */

public final class FileHelper {

    // variables to determine the root path for the file explorer.
    public static final File ROOT_PATH_FOLDER = Environment.getExternalStorageDirectory();
    public static final String ROOT_PATH = ROOT_PATH_FOLDER.getAbsolutePath();

    // variables to determine the sec2 folder path for the file explorer.
    public static final String SEC2_FOLDER_NAME = "Sec2.d";
    public static final File SEC2_FOLDER = new File(ROOT_PATH + File.separator + SEC2_FOLDER_NAME);
    public static final String SEC2_FOLDER_PATH = SEC2_FOLDER.getAbsolutePath();

    // variables to determine the download folder path for the file explorer.
    public static final String DOWNLOAD_FOLDER_NAME = "Download";
    public static final File DOWNLOAD_FOLDER = new File(ROOT_PATH + File.separator
            + DOWNLOAD_FOLDER_NAME);
    public static final String DOWNLOAD_FOLDER_PATH = DOWNLOAD_FOLDER.getAbsolutePath();

    // @see http://www.java2s.com/Code/Java/Network-Protocol/enumMimeType.htm
    public static final String MIME_MEDIA_TYPE = "^(application|audio|example|image|message|model|multipart|text|video)/[a-zA-Z0-9]+([+.-][a-zA-z0-9]+)*$";

    // regex expressions to determine the different mime types
    public static final String MIME_TYPE_IMAGE = "^(image)/[a-zA-Z0-9]+([+.-][a-zA-z0-9]+)*$";
    public static final String MIME_TYPE_VIDEO = "^(video)/[a-zA-Z0-9]+([+.-][a-zA-z0-9]+)*$";
    public static final String MIME_TYPE_AUDIO = "^(audio)/[a-zA-Z0-9]+([+.-][a-zA-z0-9]+)*$";
    public static final String MIME_TYPE_TEXT = "^(text)/[a-zA-Z0-9]+([+.-][a-zA-z0-9]+)*$";
    public static final String MIME_TYPE_APK = "application/vnd.android.package-archive";
    public static final String MIME_TYPE_PDF = "application/pdf";

    /**
     * constructor
     */
    private FileHelper() {

    }

    /**
     * @param file
     * @return Mime Type of the File  
     */
    public static String getMimeType(final File file) {
        final String lastPathSegment = file.getName();// Uri.parse(file.getAbsolutePath()).getLastPathSegment().toLowerCase();
        final String mimeType = MimeTypeMap
                .getSingleton()
                .getMimeTypeFromExtension(
                        MimeTypeMap.getFileExtensionFromUrl(lastPathSegment.lastIndexOf('.') != -1 ? lastPathSegment
                                .substring(lastPathSegment.lastIndexOf('.'))
                                : lastPathSegment));
        return mimeType != null ? mimeType : "*/*";
    }

    /**
     * @param fileName
     * @return Mime Type of the File
     */
    public static String getMimeType(final String fileName) {
        final String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                fileName.lastIndexOf('.') != -1 ? fileName.substring(fileName.lastIndexOf('.') + 1)
                        : fileName);
        return mimeType != null ? mimeType : "*/*";
    }

    /**
     * @param file
     * @param context
     * @return Drawable representing the file
     */
    public static Drawable mapMimeType(final File file, final Context context) {
        final String packageName = context.getPackageName();
        final String fileName = file.getName().toLowerCase(Locale.getDefault());

        final String fileExtension = getMimeType(fileName);
        if (fileExtension.matches(MIME_TYPE_IMAGE)) {
            return context.getResources().getDrawable(
                    context.getResources().getIdentifier("file_image", "drawable", packageName));
        }
        if (fileExtension.matches(MIME_TYPE_VIDEO)) {
            return context.getResources().getDrawable(
                    context.getResources().getIdentifier("file_video", "drawable", packageName));
        }
        if (fileExtension.matches(MIME_TYPE_AUDIO)) {
            return context.getResources().getDrawable(
                    context.getResources().getIdentifier("file_sound", "drawable", packageName));
        }
        if (fileExtension.matches(MIME_TYPE_TEXT)) {
            return context.getResources().getDrawable(
                    context.getResources().getIdentifier("file_txt", "drawable", packageName));
        }
        if (fileExtension.matches(MIME_TYPE_PDF)) {
            return context.getResources().getDrawable(
                    context.getResources().getIdentifier("file_txt2", "drawable", packageName));
        }
        // TODO: @hoppe: (07.03.2013) add more items
        return context.getResources().getDrawable(
                context.getResources().getIdentifier("file", "drawable", packageName));
    }

    /**
     * 
     * @param context
     * @param file 
     * @param mimeType
     * @return BitmapDrawable representing the file
     */
    public static BitmapDrawable getThumbnail(final Context context, final File file,
            final String mimeType) {
        if (mimeType.matches(MIME_TYPE_IMAGE)) {
            return getImageThumbnail(context, file);
        }
        if (mimeType.matches(MIME_TYPE_VIDEO)) {
            return getVideoThumbnail(context, file);
        }
        if (mimeType.matches(MIME_TYPE_APK)) {
            return (BitmapDrawable) getApkThumbnail(context, file);
        }
        return null;
    }

    /**
     * 
     * @param context
     * @param filePath
     * @return Drawable representing the file
     */
    public static Drawable getApkThumbnail(final Context context, final String filePath) {
        final PackageInfo info = context.getPackageManager().getPackageArchiveInfo(filePath, 0);
        if (info != null) {
            info.applicationInfo.sourceDir = filePath;
            info.applicationInfo.publicSourceDir = filePath;
            return context.getPackageManager().getApplicationIcon(info.applicationInfo);
        } else {
            return context.getResources().getDrawable(
                    context.getResources().getIdentifier("file", "drawable",
                            context.getPackageName()));
        }
    }

    /**
     * 
     * @param context
     * @param file
     * @return Drawable representing the file
     */
    public static Drawable getApkThumbnail(final Context context, final File file) {
        return getApkThumbnail(context, file.getAbsolutePath());
    }

    /**
     * 
     * @param fileAbsolutePath
     * @return Bitmap representing the file which can be found at the given path
     */
    public static Bitmap loadBitmap(final String fileAbsolutePath) {
        Bitmap loadBitmap = null;
        int downsample = 1;
        final BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(fileAbsolutePath, opts);
        if (Math.max(opts.outWidth, opts.outHeight) > 75) {
            downsample = Math.max(opts.outWidth, opts.outHeight) / 75 + 1;
        }
        while (loadBitmap == null) {
            try {
                opts.inJustDecodeBounds = false;
                opts.inSampleSize = downsample;
                loadBitmap = BitmapFactory.decodeFile(fileAbsolutePath, opts);
            } catch (final OutOfMemoryError ome) {
                downsample++;
                ome.printStackTrace();
            }
        }
        return loadBitmap;
    }

    /**
    * 
    * @param fileAbsolutePath
    * @return Bitmap representing the file which can be found at the given path
    */
    public static Bitmap loadCachedBitmap(final String fileAbsolutePath) {
        Bitmap loadBitmap = null;
        int downsample = 1;
        final BitmapFactory.Options opts = new BitmapFactory.Options();
        while (loadBitmap == null) {
            try {
                loadBitmap = BitmapFactory.decodeFile(fileAbsolutePath, opts);
            } catch (final OutOfMemoryError ome) {
                downsample++;
                ome.printStackTrace();
            }
        }
        return loadBitmap;
    }

    /**
     * 
     * @param context
     * @param file
     * @return Bitmap representing the file
     */
    public static Bitmap loadBitmap(final Context context, final File file) {
        Bitmap loadBitmap = null;

        final String path = context.getCacheDir().getAbsolutePath() + File.separatorChar;
        final File cacheFile = new File(path + String.valueOf(file.getAbsolutePath().hashCode())
                + ".png");
        if (cacheFile.exists()) {
            loadBitmap = FileHelper.loadCachedBitmap(cacheFile.getAbsolutePath());
        } else {
            loadBitmap = FileHelper.loadBitmap(file.getAbsolutePath());
            try {
                FileHelper.saveBitmap(loadBitmap, path,
                        String.valueOf(file.getAbsolutePath().hashCode()) + ".png",
                        CompressFormat.PNG);
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        return loadBitmap;
    }

    /**
     * 
     * @param context
     * @param file
     * @return BitmapDrawable representing the file
     */
    public static BitmapDrawable loadBitmapDrawable(final Context context, final File file) {
        return new BitmapDrawable(context.getResources(), loadBitmap(context, file));
    }

    /**
     * 
     * @param context
     * @param bmp
     * @return Converted BitmapDrawable from given Bitmap
     */
    public static BitmapDrawable loadBitmapDrawable(final Context context, final Bitmap bmp) {
        return new BitmapDrawable(context.getResources(), bmp);
    }

    /**
     *  
     * @param context
     * @param id
     * @return BitmapDrawable represented by the given id
     */
    public static BitmapDrawable getImageThumbnail(final Context context, final long id) {
        return loadBitmapDrawable(context, MediaStore.Images.Thumbnails.getThumbnail(
                context.getContentResolver(), id, MediaStore.Images.Thumbnails.MICRO_KIND, null));
    }

    /**
     * 
     * @param context
     * @param file
     * @return BitmapDrawable representing the file
     */
    public static BitmapDrawable getImageThumbnail(final Context context, final File file) {
        final String absolutePath = file.getAbsolutePath();
        final ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = null;
        final String[] projection = {
                MediaStore.Images.Media._ID
        };
        cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                MediaStore.Images.Media.DATA + " LIKE ? ", new String[] {
                    absolutePath
                }, null);

        long imageId = -1;
        if (cursor.moveToNext()) {
            imageId = cursor.getLong(0);
            cursor.close();
        }
        if (imageId > -1) {
            return loadBitmapDrawable(context, MediaStore.Images.Thumbnails.getThumbnail(
                    context.getContentResolver(), imageId, MediaStore.Images.Thumbnails.MICRO_KIND,
                    null));
        } else {
            return loadBitmapDrawable(context, file);
        }
    }

    /**
     * 
     * @param context
     * @param file
     * @return BitmapDrawable representing the file
     */
    public static BitmapDrawable getVideoThumbnail(final Context context, final File file) {
        final String absolutePath = file.getAbsolutePath();
        final ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = null;
        final String[] projection = {
                MediaStore.Video.Media._ID
        };
        cursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection,
                MediaStore.Video.Media.DATA + " LIKE ? ", new String[] {
                    absolutePath
                }, null);

        long videoId = -1;
        if (cursor.moveToNext()) {
            videoId = cursor.getLong(0);
            cursor.close();
        }
        if (videoId > -1) {
            return loadBitmapDrawable(context, MediaStore.Video.Thumbnails.getThumbnail(
                    context.getContentResolver(), videoId, MediaStore.Video.Thumbnails.MICRO_KIND,
                    null));
        } else {
            return loadBitmapDrawable(context, file);
        }
    }

    /**
     * 
     * @param context
     * @param folder
     * @return Hashmap containing all ids to determine image representation for all files for the given folder location
     */
    public static HashMap<String, Long> getImageThumbnailIds(final Context context,
            final File folder) {
        final HashMap<String, Long> returnValue = new HashMap<String, Long>();
        final String absolutePath = folder.getAbsolutePath();

        final ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = null;
        final String[] projection = {
                MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA
        };

        cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                MediaStore.Images.Media.DATA + " LIKE ? AND " + MediaStore.Images.Media.DATA
                        + " NOT LIKE ? ",
                new String[] {
                        absolutePath + "/%", absolutePath + "/%/%"
                }, null);

        if (cursor != null) {
            final int columnIndexId = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            final int columnIndexData = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            while (cursor.moveToNext()) {
                returnValue.put(cursor.getString(columnIndexData),
                        Long.valueOf(cursor.getLong(columnIndexId)));
            }
            cursor.close();
        }
        return returnValue;
    }

    /**
     * 
     * @param bitmap
     * @param path
     * @param fileName
     * @param format
     * @throws IOException
     */
    public static void saveBitmap(final Bitmap bitmap, final String path, final String fileName,
            final CompressFormat format) throws IOException {
        final File dir = new File(path);
        dir.mkdirs();

        final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(
                dir, fileName)));
        bitmap.compress(format, 100, bos);
        bos.flush();
        bos.close();
    }

    /**
     * static values to determine size of the buffer needed for Input-/Outputstreams 
     */
    private static final int BUFFER_SIZE = 65536;
    private static final long MAX_FILE_SIZE = 1048576;

    /**
     * @see org.sec2.mwserver.core.util.ICryptoUtils#decodeBase64(byte[]) 
     * 
     * @param base64String
     * @return Base64 decoded byte[] 
     */
    private static byte[] decodeBase64(final String base64String) {
        return Base64.decode(base64String, Base64.DEFAULT);
    }

    /**
     * 
     * @param fileName
     * @param base64String
     * @return Base64 decoded file object
     * @throws IOException
     */
    public static File decodeBase64(final String fileName, final String base64String) {
        FileOutputStream outputStream = null;
        try {
            if (!DOWNLOAD_FOLDER.exists()) {
                DOWNLOAD_FOLDER.mkdir();
            }
            final File file = new File(DOWNLOAD_FOLDER_PATH + File.separator + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            final byte[] fileByteArray = decodeBase64(base64String);

            outputStream = new FileOutputStream(file);
            outputStream.write(fileByteArray);
            outputStream.flush();
            return file;

        } catch (final IOException e) {
            return null;
        } finally {
            closeStream(outputStream);
        }

    }

    /**
     * Method to close a given closeable object
     * @param closeable
     */
    private static void closeStream(final Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (final IOException e) {
            Log.e("FileHelper", "Exception occured while closing stream");
        }
    }

    /**
     * @see org.sec2.mwserver.core.util.ICryptoUtils#encodeBase64(byte[])
     *  
     * @param binaryData
     * @return Base64 encoded String for the given byte[]
     * @throws OutOfMemoryError
     */
    private static String encodeBase64(final byte[] binaryData) throws OutOfMemoryError {
        return Base64.encodeToString(binaryData, Base64.NO_WRAP);
    }

    /**
     * 
     * convenience method to gzip a file
     *  
     * @param file
     */
    public static void compress(final File file) {
        GZIPOutputStream gzipOutputStream = null;
        BufferedInputStream bufferedInputStream = null;
        try {
            final FileOutputStream fileOutputStream = new FileOutputStream(file + ".gz");
            gzipOutputStream = new GZIPOutputStream(fileOutputStream);
            final FileInputStream fileInputStream = new FileInputStream(file);
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            final byte[] buffer = new byte[1024];
            int i;
            while ((i = bufferedInputStream.read(buffer)) >= 0) {
                gzipOutputStream.write(buffer, 0, i);
            }

        } catch (final IOException e) {
            Log.e("FileHelper", "an io exception occured while compressing a file");
        } finally {
            closeStream(bufferedInputStream);
            closeStream(gzipOutputStream);

        }
    }

    /**
     * 
     * convenience method to delete a file
     * 
     * @param file the file that will be deleted
     */
    public static void delete(final File file) {
        if (!file.delete() && file.isDirectory()) {
            final File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                delete(files[i]);
            }
        }
    }

    /**
     * 
     * convenience method to Base64 encode a file using a stream
     * 
     * @param file the file object that will be encoded
     * @throws IOException will be thrown when an exception while reading from that file occurs
     */
    public static void encodeBase64Stream(final File file) throws IOException {
        final BufferedInputStream bufferedInputStream = new BufferedInputStream(
                new FileInputStream(file));
        final Base64OutputStream base64OutputStream = new Base64OutputStream(new FileOutputStream(
                Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                        + "encoded.b64"), Base64.NO_WRAP);
        final byte[] buffer = new byte[BUFFER_SIZE];
        int n = 0;
        while ((n = bufferedInputStream.read(buffer, 0, buffer.length)) > -1) {
            if (n < BUFFER_SIZE) {
                final byte[] buffer2 = new byte[n];
                for (int i = 0; i < buffer2.length; i++) {
                    buffer2[i] = buffer[i];
                }
                base64OutputStream.write(buffer2);
            } else {
                base64OutputStream.write(buffer);
            }
        }
        base64OutputStream.flush();
        bufferedInputStream.close();
        base64OutputStream.close();
    }

    /**
     * 
     * convenience method to Base64 encode a file and get a String[] representation of that file to avoid OutOfMemoryError while encoding
     * 
     * @param file the file object that will be encoded
     * @return a Base64 encoded representation of the file
     * @throws IOException will be thrown when an exception while reading from that file occurs
     */
    public static String[] encodeBase64(final File file) throws IOException {
        final BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        final ArrayList<String> arrayList = new ArrayList<String>();
        final byte[] buffer = new byte[BUFFER_SIZE];
        int n = 0;
        String encodeBase64 = "";
        while ((n = bis.read(buffer, 0, buffer.length)) > -1) {
            if (n < BUFFER_SIZE) {
                final byte[] buffer2 = new byte[n];
                for (int i = 0; i < buffer2.length; i++) {
                    buffer2[i] = buffer[i];
                }
                encodeBase64 = encodeBase64(buffer2);
                arrayList.add(encodeBase64);
            } else {
                encodeBase64 = encodeBase64(buffer);
                arrayList.add(encodeBase64);
            }
        }
        bis.close();
        return arrayList.toArray(new String[arrayList.size()]);
    }

    /**
     * 
     * @param fileName
     * @param list
     * @return Base64 decoded file object
     * @throws IOException
     */
    public static File decodeBase64(final String fileName, final String[] list) throws IOException {
        if (!DOWNLOAD_FOLDER.exists()) {
            DOWNLOAD_FOLDER.mkdirs();
        }
        final File file = new File(DOWNLOAD_FOLDER_PATH + File.separator + fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        final FileOutputStream fileOutputStream = new FileOutputStream(file);
        final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
        for (int i = 0; i < list.length; i++) {
            bufferedOutputStream.write(decodeBase64(list[i]));
        }
        bufferedOutputStream.flush();
        bufferedOutputStream.close();
        return file;
    }

    /**
     * method to determine if the given file object can be send to the cloud
     * 
     * @param file
     * @return true or false if it is allowed
     */
    public static boolean isAllowed(final File file) {
        return file.length() < MAX_FILE_SIZE ? true : false;
    }

    /**
     * method to determine if the file exists for the fiven folderPath and fileName
     * 
     * @param folderPath
     * @param fileName
     * @return true or false if it exists
     */
    public static boolean fileExists(final String folderPath, final String fileName) {
        return new File(folderPath, fileName).exists();
    }

}
