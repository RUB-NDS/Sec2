package de.adesso.mobile.android.sec2.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import de.adesso.mobile.android.sec2.util.LogHelper;

/**
 * OSI
 * @author hoppe
 */
public abstract class OSI {

    private static final Class<?> c = OSI.class;

    // "http://10.0.2.2:50001/exist/rest/db/calendar/sec2test.xml"

    // adress of the server running jetty and exist-db 
    //    public static final String SERVER_URI = "http://10.0.2.2:50001";
    //    //path to the database
    //    public static final String COLLECTION_URI = SERVER_URI + "/exist/rest/db/calendar";
    //    // name of the xml-file that will store the information
    //    public static final String RESOURCE_URI = COLLECTION_URI + "/sec2.xml";
    //
    //    public static final String RESOURCE_URI_UPLOAD = "http://10.0.2.2:50001/exist/rest/db/calendar/sec2test3.xml";

    /**
     * httpGet
     */
    public static String httpGet(final String exist, String database, String android_id) throws IOException, HttpHostConnectException, ConnectException {
        String getUrl = "http://" + exist + ":50001/exist/rest/db/calendar/" + android_id + ".xml";
        LogHelper.logV(c, "httpGet: " + getUrl);

        DefaultHttpClient httpClient = new DefaultHttpClient();
        ClientConnectionManager ccm = httpClient.getConnectionManager();
        HttpParams params = httpClient.getParams();

        httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(params, ccm.getSchemeRegistry()), params);
        httpClient.setRedirectHandler(new DefaultRedirectHandler());

        HttpGet get = new HttpGet(getUrl);

        get.setHeader("Location", database + ":50082");
        get.setHeader("Connection", "close");

        HttpResponse response = httpClient.execute(get);

        HttpEntity httpEntity = response.getEntity();

        return EntityUtils.toString(httpEntity);
    }

    public static String httpGetSSL(final String exist, String database, String android_id) throws IOException, HttpHostConnectException, ConnectException {
        String getUrl = "https://" + exist + ":50001/exist/rest/db/calendar/" + android_id + ".xml";
        LogHelper.logV(c, "httpGetSSL: " + getUrl);

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        //        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpParams params = httpClient.getParams();
        ClientConnectionManager ccm = new SingleClientConnManager(params, schemeRegistry);

        httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(params, schemeRegistry), params);
        httpClient.setRedirectHandler(new DefaultRedirectHandler());

        HttpGet get = new HttpGet(getUrl);

        get.setHeader("Location", database + ":50082");
        get.setHeader("Connection", "close");

        HttpResponse response = httpClient.execute(get);

        HttpEntity httpEntity = response.getEntity();

        return EntityUtils.toString(httpEntity);
    }

    /**
     * httpPost
     */
    public static String httpPost(final String url) throws IOException {

        LogHelper.logV(c, "httpPost: " + url);

        DefaultHttpClient httpClient = new DefaultHttpClient();
        ClientConnectionManager ccm = httpClient.getConnectionManager();
        HttpParams params = httpClient.getParams();

        httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(params, ccm.getSchemeRegistry()), params);
        httpClient.setRedirectHandler(new DefaultRedirectHandler());

        HttpPost post = new HttpPost(url);

        post.setHeader("Location", "localhost:50082");
        post.setHeader("Connection", "close");
        post.setHeader("Content-Type", "text/xml");

        HttpResponse response = httpClient.execute(post);

        HttpEntity httpEntity = response.getEntity();
        return EntityUtils.toString(httpEntity);
    }

    public static void uploadData(String sec2Xml, String exist, String database, String android_id) throws UnsupportedEncodingException, IOException,
            FileNotFoundException {
        URL u = new URL("http://" + exist + ":50001/exist/rest/db/calendar/" + android_id + ".xml");
        HttpURLConnection connect = (HttpURLConnection) u.openConnection();
        connect.setRequestMethod("PUT");
        connect.setRequestProperty("Location", database + ":50082");
        connect.setRequestProperty("Connection", "close");
        connect.setDoOutput(true);
        connect.setRequestProperty("Content-Type", "text/xml");
        Writer writer = new OutputStreamWriter(connect.getOutputStream(), "UTF-8");
        writer.write(sec2Xml);
        writer.close();
        connect.connect();
        connect.getInputStream();
        connect.disconnect();

    }

    //    protected static String readInputStream(InputStream is) throws UnsupportedEncodingException, IOException {
    //        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
    //        String line;
    //        StringBuilder out = new StringBuilder();
    //        while ((line = reader.readLine()) != null) {
    //            Log.d("", line);
    //            out.append(line);
    //            out.append("\r\n");
    //        }
    //        return out.toString();
    //
    //    }

    //    public static void uploadData(NoticeListXStream noticeListXStream, String exist, String database, String android_id) throws UnsupportedEncodingException,
    //            IOException, FileNotFoundException {
    //        URL u = new URL("http://" + exist + ":50001/exist/rest/db/calendar/" + android_id + ".xml");
    //        HttpURLConnection connect = (HttpURLConnection) u.openConnection();
    //        connect.setRequestMethod("PUT");
    //        connect.setRequestProperty("Location", database + ":50082");
    //        connect.setRequestProperty("Connection", "close");
    //        connect.setDoOutput(true);
    //        connect.setRequestProperty("Content-Type", "text/xml");
    //        serializeToXml(noticeListXStream, android_id);
    //
    //        String sec2Xml = readInputStream(new FileInputStream(new File(Environment.getExternalStorageDirectory() + "/" + android_id + ".xml")));
    //        Writer writer = new OutputStreamWriter(connect.getOutputStream(), "UTF-8");
    //        writer.write(sec2Xml);
    //        writer.close();
    //        //        LogHelper.logV(sec2Xml);
    //        connect.connect();
    //        System.out.println(readInputStream(connect.getInputStream()));
    //        connect.disconnect();
    //
    //    }

    //    private static void serializeToXml(NoticeListXStream noticeListXStream, String android_id) throws IOException, FileNotFoundException {
    //        File newxmlfile = new File(Environment.getExternalStorageDirectory() + "/" + android_id + ".xml");
    //        if (!newxmlfile.exists()) {
    //            LogHelper.logV("Create new XML");
    //            newxmlfile.createNewFile();
    //        } else {
    //            LogHelper.logV("XML already exists");
    //        }
    //        FileOutputStream fileos = null;
    //        fileos = new FileOutputStream(newxmlfile);
    //
    //        XmlSerializer serializer = Xml.newSerializer();
    //        serializer.setOutput(fileos, "UTF-8");
    //        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
    //        serializer.startDocument("UTF-8", Boolean.valueOf(true));
    //        serializer.startTag(null, "noticelist");
    //        for (int i = 0; i < noticeListXStream.noticeXStream.size(); i++) {
    //
    //            serializer.startTag(null, "notice");
    //            serializer.startTag(null, "nid");
    //            serializer.text(String.valueOf(noticeListXStream.noticeXStream.get(i).nid));
    //            serializer.endTag(null, "nid");
    //            serializer.startTag(null, "subject");
    //            serializer.text(String.valueOf(noticeListXStream.noticeXStream.get(i).subject));
    //            serializer.endTag(null, "subject");
    //            serializer.startTag(null, "date");
    //            serializer.text(String.valueOf(noticeListXStream.noticeXStream.get(i).date));
    //            serializer.endTag(null, "date");
    //            serializer.startTag(null, "lock");
    //            serializer.text(String.valueOf(noticeListXStream.noticeXStream.get(i).lock));
    //            serializer.endTag(null, "lock");
    //
    //            for (int j = 0; j < noticeListXStream.noticeXStream.get(i).noticeSelectionXStream.size(); j++) {
    //                serializer.startTag(null, "content");
    //                serializer.attribute(null, "encryption", Boolean.toString(noticeListXStream.noticeXStream.get(i).noticeSelectionXStream.get(j).encryption));
    //                if (noticeListXStream.noticeXStream.get(i).noticeSelectionXStream.get(j).encryption) {
    //                    serializer.startTag("", "xenc:EncryptedData");
    //                    serializer.attribute("", "xmlns:xenc", "http://www.w3.org/2001/04/xmlenc#");
    //                    serializer.startTag("", "xenc:EncryptionMethod");
    //                    serializer.attribute("", "Algorithm", "http://www.w3.org/2001/04/xmlenc#aes128-cbc");
    //                    serializer.endTag("", "xenc:EncryptionMethod");
    //                    serializer.startTag("", "xenc:EncryptionProperties");
    //                    serializer.startTag("", "xenc:EncryptionProperty");
    //                    serializer.startTag("", "ds:KeyInfo");
    //                    serializer.attribute("", "xmlns:ds", "http://www.w3.org/2000/09/xmldsig#");
    //                    serializer.startTag("", "ds:KeyName");
    //                    serializer.text("Group 1");
    //                    serializer.endTag("", "ds:KeyName");
    //                    serializer.endTag("", "ds:KeyInfo");
    //                    serializer.endTag("", "xenc:EncryptionProperty");
    //                    serializer.endTag("", "xenc:EncryptionProperties");
    //
    //                    serializer.endTag("", "xenc:EncryptedData");
    //                    serializer.startTag("", "section");
    //                    serializer.text(noticeListXStream.noticeXStream.get(i).noticeSelectionXStream.get(j).section);
    //                    serializer.endTag("", "section");
    //                } else {
    //                    serializer.startTag("", "section");
    //                    serializer.text(noticeListXStream.noticeXStream.get(i).noticeSelectionXStream.get(j).section);
    //                    serializer.endTag("", "section");
    //                }
    //                serializer.endTag(null, "content");
    //            }
    //            serializer.endTag(null, "notice");
    //
    //        }
    //        serializer.endTag(null, "noticelist");
    //        serializer.endDocument();
    //        //        LogHelper.logV("serializeToXml" + serializer.toString());
    //        //write xml data into the FileOutputStream
    //        serializer.flush();
    //
    //        //finally we close the file stream
    //        fileos.close();
    //
    //        //        } catch (IllegalArgumentException e) {
    //        //            Log.e("IllegalArgumentException", "Illegal Argument");
    //        //            e.printStackTrace();
    //        //
    //        //        } catch (IllegalStateException e) {
    //        //            Log.e("IllegalStateException", "Illegal State");
    //        //            e.printStackTrace();
    //        //        } catch (IOException e) {
    //        //            Log.e("IOException", "exception in createNewFile() method");
    //        //            e.printStackTrace();
    //        //        }
    //    }
}
