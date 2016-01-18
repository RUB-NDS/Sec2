package de.adesso.mobile.android.sec2.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.util.List;

import org.apache.http.conn.HttpHostConnectException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.mapper.CannotResolveClassException;
import com.thoughtworks.xstream.mapper.MapperWrapper;

import de.adesso.mobile.android.sec2.model.NoticeList;
import de.adesso.mobile.android.sec2.model.NoticeListItem;
import de.adesso.mobile.android.sec2.util.LogHelper;

/**
 * API
 * @author hoppe
 */
public class API {

    private static final Class<?> c = API.class;

    public static List<NoticeListItem> unmarshall(String exist, String database, String android_id) throws XStreamException, IOException,
            FileNotFoundException, HttpHostConnectException, ConnectException {
        //        "http://10.0.2.2:50001/exist/rest/db/calendar/sec2test3.xml"
        final String xmlString = OSI.httpGet(exist, database, android_id);
        LogHelper.logV(c, "unmarshall");
        LogHelper.logV("Exist: " + exist + "  Database: " + database);
        return serializeFromXml(xmlString);
    }

    public static Boolean marshall(NoticeList noticeList, String exist, String database, String android_id) throws XStreamException, IOException,
            FileNotFoundException, HttpHostConnectException, ConnectException {
        //        NoticeListXStream noticeListXStream = new NoticeListXStream(noticeList);
        OSI.uploadData(serializeToXml(noticeList), exist, database, android_id);
        return true;
    }

    private static List<NoticeListItem> serializeFromXml(String xml) throws IOException, FileNotFoundException {

        XStream xstream = new XStream(new DomDriver("UTF-8")) {

            protected MapperWrapper wrapMapper(MapperWrapper next) {
                return new MapperWrapper(next) {

                    public boolean shouldSerializeMember(Class definedIn, String fieldName) {
                        try {
                            return definedIn != Object.class || realClass(fieldName) != null;
                        } catch (CannotResolveClassException cnrce) {
                            return false;
                        }
                    }
                };
            }
        };
        xstream.processAnnotations(NoticeList.class);

        NoticeList xmlX = (NoticeList) xstream.fromXML(xml);
        LogHelper.logE("From XML:\n");
        LogHelper.logE(xmlX.toString());
        LogHelper.logE("ENDE\n");
        return xmlX.noticeListItem;
    }

    private static String serializeToXml(NoticeList noticeList) throws IOException, FileNotFoundException {
        XStream xstream = new XStream(new DomDriver("UTF-8")) {

            protected MapperWrapper wrapMapper(MapperWrapper next) {
                return new MapperWrapper(next) {

                    public boolean shouldSerializeMember(Class definedIn, String fieldName) {
                        try {
                            return definedIn != Object.class || realClass(fieldName) != null;
                        } catch (CannotResolveClassException cnrce) {
                            return false;
                        }
                    }
                };
            }
        };
        xstream.processAnnotations(NoticeList.class);
        xstream.autodetectAnnotations(true);

        LogHelper.logV("serializeToXml:\n" + xstream.toXML(noticeList));
        return xstream.toXML(noticeList);
    }

}
