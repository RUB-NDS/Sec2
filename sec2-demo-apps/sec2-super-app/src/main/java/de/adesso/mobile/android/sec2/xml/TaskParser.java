/**
 * 
 */

package de.adesso.mobile.android.sec2.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Xml;
import de.adesso.mobile.android.sec2.model.Lock;
import de.adesso.mobile.android.sec2.model.Priority;
import de.adesso.mobile.android.sec2.model.Task;

/**
 * A handler for parsing the XML representation of a task object. The handler expects an
 * XML representation of the following form:
 * 
 * <pre>
 * {@code
 * <?xml version="1.0" encoding="UTF-8"?>
 * <task>
 *  <subject>[subject]</subject>
 *  <due>[Date of task]</due>
 *  <reminder>[Reminder]</reminder>
 *  <isDone>[Is task done]</isDone>
 *  <priority>[Priority of a task]</priority>
 *  <encryptionParts xmlns:sec2="http://sec2.org/2012/03/middleware/enc/v1" sec2:groups=[Groups]>
 *   <encrypt>[Textpart to encrypt]</encrypt>
 *   <encrypt>[Textpart to encrypt]</encrypt>
 *  </encryptionParts>
 *  <plaintext>[Plaintext]</plaintext>
 *  <lock>[Lock status]</lock>
 * </task>
 * </pre>
 * 
 * Parts in parentheses are optional. The order of tags of one tree-level may be arbitrary.
 * 
 * @author hoppe
 *
 */
public class TaskParser {

    private static final String NAMESPACE = "http://exist.sourceforge.net/NS/exist";
    private static final String ROOT = "result";

    private static final String TASK = "task";
    private static final String SUBJECT = "subject";
    private static final String ENC_PARTS = "encryptionParts";
    private static final String ENC = "encrypt";

    private static final String PLAINTEXT = "plaintext";
    private static final String CREATION = "creation";
    private static final String LOCK = "lock";
    private static final String DUE = "due";
    private static final String REMINDER = "reminder";
    private static final String IS_DONE = "isDone";
    private static final String PRIORITY = "priority";
    
    private static final String FILE = "file";

    public static List<Task> parseList(final InputStream inputStream) throws SAXException,
            IOException {

        final List<Task> tasks = new ArrayList<Task>();

        final Task task = new Task();
        final RootElement root = new RootElement(NAMESPACE, ROOT);
        final Element element = root.getChild(TASK);

        element.setEndElementListener(new EndElementListener() {

            @Override
            public void end() {
                tasks.add((Task) task.clone());
            }
        });

        element.getChild(SUBJECT).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(final String body) {
                task.setSubject(body);
            }
        });

        element.getChild(PLAINTEXT).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(final String body) {
                task.setTaskText(body);
            }
        });

        element.getChild(ENC_PARTS).setStartElementListener(new StartElementListener() {

            @Override
            public void start(final Attributes attributes) {
                task.setPartsToEncrypt(new ArrayList<String>());
            }
        });

        element.getChild(ENC_PARTS).getChild(ENC)
                .setEndTextElementListener(new EndTextElementListener() {

                    @Override
                    public void end(final String body) {
                        task.getPartsToEncrypt().add(body);
                    }
                });

        element.getChild(CREATION).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(final String body) {
                task.setCreationDate(createCalendar(body));
            }
        });

        element.getChild(LOCK).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(final String body) {
                task.setLock(Lock.valueOf(body));
            }
        });

        element.getChild(DUE).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(final String body) {
                task.setDueDate(createCalendar(body));
            }
        });

        element.getChild(REMINDER).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(final String body) {
                task.setReminderDate(createCalendar(body));
            }
        });

        element.getChild(IS_DONE).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(final String body) {
                task.setIsDone(Boolean.parseBoolean(body));
            }
        });

        element.getChild(PRIORITY).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(final String body) {
                task.setPriority(Priority.valueOf(body));
            }
        });
        
        element.getChild(FILE).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(final String body) {
                task.setFilename(body);
            }
        });

        Xml.parse(inputStream, Xml.Encoding.ISO_8859_1, root.getContentHandler());
        return tasks;
    }

    public static List<Task> parseList(final String content) throws SAXException, IOException {

        final List<Task> tasks = new ArrayList<Task>();

        final Task task = new Task();
        final RootElement root = new RootElement(NAMESPACE, ROOT);
        final Element element = root.getChild(TASK);

        element.setEndElementListener(new EndElementListener() {

            @Override
            public void end() {
                tasks.add((Task) task.clone());
            }
        });

        element.getChild(SUBJECT).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(final String body) {
                task.setSubject(body);
            }
        });

        element.getChild(PLAINTEXT).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(final String body) {
                task.setTaskText(body);
            }
        });

        element.getChild(ENC_PARTS).setStartElementListener(new StartElementListener() {

            @Override
            public void start(final Attributes attributes) {
                task.setPartsToEncrypt(new ArrayList<String>());
            }
        });

        element.getChild(ENC_PARTS).getChild(ENC)
                .setEndTextElementListener(new EndTextElementListener() {

                    @Override
                    public void end(final String body) {
                        task.getPartsToEncrypt().add(body);
                    }
                });

        element.getChild(CREATION).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(final String body) {
                task.setCreationDate(createCalendar(body));
            }
        });

        element.getChild(LOCK).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(final String body) {
                task.setLock(Lock.valueOf(body));
            }
        });

        element.getChild(DUE).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(final String body) {
                task.setDueDate(createCalendar(body));
            }
        });

        element.getChild(REMINDER).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(final String body) {
                task.setReminderDate(createCalendar(body));
            }
        });

        element.getChild(IS_DONE).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(final String body) {
                task.setIsDone(Boolean.parseBoolean(body));
            }
        });

        element.getChild(PRIORITY).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(final String body) {
                task.setPriority(Priority.valueOf(body));
            }
        });

        Xml.parse(content, root.getContentHandler());
        return tasks;
    }

    private static GregorianCalendar createCalendar(final String timeInMillis) {
        final GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(Long.valueOf(timeInMillis));
        return calendar;
    }

}
