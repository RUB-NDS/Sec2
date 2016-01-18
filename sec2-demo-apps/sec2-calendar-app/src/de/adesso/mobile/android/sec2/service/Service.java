package de.adesso.mobile.android.sec2.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;

import javax.net.ssl.SSLException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.xml.sax.SAXParseException;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;

import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.io.StreamException;

import de.adesso.mobile.android.sec2.adapter.FileChooserAdapter;
import de.adesso.mobile.android.sec2.dialog.WaitingDialog;
import de.adesso.mobile.android.sec2.model.FileChooser;
import de.adesso.mobile.android.sec2.model.NoticeList;
import de.adesso.mobile.android.sec2.model.NoticeListItem;
import de.adesso.mobile.android.sec2.util.AlertHelper;
import de.adesso.mobile.android.sec2.util.Filehelper;
import de.adesso.mobile.android.sec2.util.IconCache;
import de.adesso.mobile.android.sec2.util.LogHelper;

/**
 * Service
 * @author mschmitz
 */
public abstract class Service {

    @SuppressWarnings ("unused")
    private static final Class<?> c = Service.class;

    /**
     * handleExceptions
     */
    public static void handleExceptions(final Context context, final Exception exception) {

        if (exception == null) {
            return;
        }

        if (exception instanceof XStreamException || exception instanceof SAXParseException || exception instanceof StreamException) {
            //            AlertHelper.showAlertDialog(context, "XStreamException", "Error while Parsing XML-File");
            return;
        }

        if (exception instanceof IOException) {
            //            AlertHelper.showAlertDialog(context, "IOException", "Couldn't instanciate xml on sdcard");
            return;
        }

        if (exception instanceof FileNotFoundException) {
            //            AlertHelper.showAlertDialog(context, "FileNotFoundException", "Couldn't find sec2.xml on sdcard");
            return;
        }

        if (exception instanceof HttpHostConnectException || exception instanceof ConnectException || exception instanceof SocketException) {
            //            AlertHelper.showAlertDialog(context, "HttpHostConnectException", "");
            return;
        }

        if (exception instanceof UnknownHostException || exception instanceof SocketException || exception instanceof ClientProtocolException
                || exception instanceof SSLException || exception instanceof ConnectTimeoutException || exception instanceof SocketTimeoutException) {
            AlertHelper.showWirelessDialog(context, null);
            return;
        }
    }

    /**
     * GetXmlFromDb
     * @author hoppe
     */
    public static class GetXmlFromExist extends AsyncTask<Object, Void, List<NoticeListItem>> {

        private static final Class<?> c = GetXmlFromExist.class;

        protected Context context;
        protected Exception exception;
        protected WaitingDialog wd;

        /**
         * Constructor
         */
        public GetXmlFromExist(final Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            wd = new WaitingDialog(context, null);
            wd.setMessage("Download Notices");
            wd.show();
        }

        @Override
        protected List<NoticeListItem> doInBackground(final Object... params) {
            try {
                LogHelper.logV("" + (String) params[0] + " " + (String) params[1] + " " + (String) params[2]);
                return API.unmarshall((String) params[0], (String) params[1], (String) params[2]);
            } catch (Exception e) {
                exception = e;
                LogHelper.logW(c, exception);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final List<NoticeListItem> result) {
            wd.hide();
            wd.dismiss();
            if (exception == null) onPostExecuteWithoutException(result);
            else onPostExecuteWithException(result);

        }

        /**
         * onPostExecuteWithException
         */
        protected void onPostExecuteWithException(final List<NoticeListItem> result) {
            handleExceptions(context, exception);
        }

        /**
         * onPostExecuteWithoutException
         */
        protected void onPostExecuteWithoutException(final List<NoticeListItem> result) {}

    }

    /**
     * PushXmlToExist
     * @author hoppe
     */
    public static class PushXmlToExist extends AsyncTask<Object, Void, Boolean> {

        private static final Class<?> c = PushXmlToExist.class;

        protected Context context;
        protected Exception exception;
        protected WaitingDialog wd;

        /**
         * Constructor
         */
        public PushXmlToExist(final Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            wd = new WaitingDialog(context, null);
            wd.setMessage("Upload Notices");
            wd.show();
        }

        @Override
        protected Boolean doInBackground(final Object... params) {
            try {
                return API.marshall((NoticeList) params[0], (String) params[1], (String) params[2], (String) params[3]);
            } catch (Exception e) {
                exception = e;
                LogHelper.logW(c, exception);
            }
            return null;
        }

        protected void onPostExecute(final Boolean result) {
            wd.hide();
            wd.dismiss();
            if (exception == null) onPostExecuteWithoutException(result);
            else onPostExecuteWithException(result);

        }

        /**
         * onPostExecuteWithException
         */
        protected void onPostExecuteWithException(final Boolean result) {
            handleExceptions(context, exception);
        }

        /**
         * onPostExecuteWithoutException
         */
        protected void onPostExecuteWithoutException(final Boolean result) {}

    }

    public static class ImageLoadingTask extends AsyncTask<Object, Void, Void> {

        private static final Class<?> c = ImageLoadingTask.class;

        protected Context context;
        protected Exception exception;
        protected FileChooserAdapter fca;
        protected String pathMain;

        /**
         * Constructor
         */
        public ImageLoadingTask(final Context context, FileChooserAdapter fca, String pathMain) {
            this.context = context;
            this.fca = fca;
            this.pathMain = pathMain;
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {
            fca.notifyDataSetChanged();
            super.onProgressUpdate(values);
        }

        @Override
        protected Void doInBackground(final Object... params) {
            if (!isCancelled()) {
                try {
                    for (int i = 0; i < fca.getCount(); i++) {
                        if (((FileChooser) fca.getItem(i)).file.isFile()) {
                            if (Filehelper.getMimeType(((FileChooser) fca.getItem(i)).file).contains("image")) {
                                ((FileChooser) fca.getItem(i)).image = new BitmapDrawable(IconCache.getInstance().getImage(
                                        ((FileChooser) fca.getItem(i)).file.getAbsolutePath()));
                                publishProgress((Void) null);
                            }
                        }
                    }

                    //                    String[] list = folder.list();
                    //                    ArrayList<String> imageList = new ArrayList<String>();
                    //                    for (int i = 0; i < list.length; i++) {
                    //                        File file = new File(pathMain, list[i]);
                    //                        if (!file.isHidden() && file.isFile()) {
                    //                            if (Filehelper.getMimeType(file).contains("image")) imageList.add(list[i]);
                    //                        }
                    //                    }
                    //                    Collections.sort(imageList, new FileChooserComparator());
                    //                    for (int j = 0; j < imageList.size(); j++) {
                    //                        IconCache.getInstance().getImage(new File(pathMain, imageList.get(j)).getAbsolutePath());
                    //                        //LoadImage and publishPogress
                    //                        publishProgress((Void) null);
                    //                    }

                } catch (Exception e) {
                    exception = e;
                    LogHelper.logW(c, exception);
                }
            }
            return null;
        }

        protected void onPostExecute(final Void result) {
            if (exception == null) onPostExecuteWithoutException(result);
            else onPostExecuteWithException(result);

        }

        /**
         * onPostExecuteWithException
         */
        protected void onPostExecuteWithException(final Void result) {
            handleExceptions(context, exception);
        }

        /**
         * onPostExecuteWithoutException
         */
        protected void onPostExecuteWithoutException(final Void result) {}

    }

}
