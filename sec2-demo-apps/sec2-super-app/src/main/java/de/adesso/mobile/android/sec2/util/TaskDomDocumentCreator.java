
package de.adesso.mobile.android.sec2.util;

import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.util.Log;
import de.adesso.mobile.android.sec2.model.Task;

/**
 * Creates an XML document representing the event. The document looks as follows:
 * 
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
 *  <creation>[Creationdate]</creation>
 * </task>
 * @author hoppe
 *
 */
public final class TaskDomDocumentCreator extends AbstractDomDocumentCreator {

    private static final long serialVersionUID = -8122239917281281394L;
    // The namespace
    private static final String NS_URI = "http://sec2.org/2012/03/middleware";
    // The prefix incl. ":"
    private static final String PREFIX = "sec2:";
    // The tagname "notice"
    private static final String TASK = "task";
    // The tagname "subject"
    private static final String SUBJECT = "subject";
    // The tagname "begin"
    private static final String DUE = "due";
    // The tagname "reminder"
    private static final String REMINDER = "reminder";
    // The tagname "isDone"
    private static final String IS_DONE = "isDone";
    // The tagname "priority"
    private static final String PRIORITY = "priority";
    // The tagname "creation"
    private static final String CREATION_DATE = "creation";
    // The tagname "plaintext"
    private static final String PLAINTEXT = "plaintext";
    // The tagname "encryptionParts"
    private static final String ENC_PARTS = "encryptionParts";
    // The tagname "encrypt"
    private static final String ENC = "encrypt";
    // The tagname "lock"
    private static final String LOCK = "lock";
    // The attribute "groups"
    private static final String ATTR_GROUPS = "groups";
    // The attribute "filename"
    private static final String FILENAME = "file";
    
    
    private Task task = null;

    /**
     * The standard constructor
     */
    public TaskDomDocumentCreator() {
    }

    public TaskDomDocumentCreator(final Task task) {
        this.task = task;
    }

    public TaskDomDocumentCreator(final String name, final Task task) {
        this.task = task;
        this.task.setFilename(name);
        setDocumentName(name);
    }

    /**
     * @return the task
     */
    public Task getTask() {
        return task;
    }

    /**
     * @param task - The task to set
     */
    public void setTask(final Task task) {
        this.task = task;
    }

    @Override
    public String createDomDocument(final CheckedGroupHandler groupHandler)
            throws ParserConfigurationException, TransformerException {
        final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .newDocument();
        Element root = null;
        Element encParts = null;
        Element element = null;
        StringBuilder sb = null;
        final int numberOfCheckedGroups = groupHandler.getNumberOfCheckedGroups();
        int numberOfCheckedGroupsFound = 0;
        int i = 0;
        final List<String> partsToEncrypt = task.getPartsToEncrypt();
        String eventText = task.getTaskText();

        document.setXmlStandalone(true);
        root = document.createElement(TASK);
        document.appendChild(root);
        element = document.createElement(SUBJECT);
        element.setTextContent(task.getSubject());
        root.appendChild(element);
        element = document.createElement(DUE);
        element.setTextContent(Long.toString(task.getDueDate().getTimeInMillis()));
        root.appendChild(element);

        if (task.getReminderDate() != null) {
            Log.e("hussa", "kein reminder date");
            element = document.createElement(REMINDER);
            element.setTextContent(Long.toString(task.getReminderDate().getTimeInMillis()));
            root.appendChild(element);
        }
        element = document.createElement(IS_DONE);
        element.setTextContent(task.getIsDone() ? Boolean.TRUE.toString() : Boolean.FALSE
                .toString());
        root.appendChild(element);
        element = document.createElement(PRIORITY);
        element.setTextContent(String.valueOf(task.getPriority().toString()));
        root.appendChild(element);

        if (partsToEncrypt.size() > 0) {
            if (numberOfCheckedGroups > 0) {
                encParts = document.createElement(ENC_PARTS);
                sb = new StringBuilder();
                while (numberOfCheckedGroupsFound <= numberOfCheckedGroups
                        && i < groupHandler.getNumberOfGroups()) {
                    if (groupHandler.isChecked(i)) {
                        sb.append(groupHandler.getId(i));
                        numberOfCheckedGroupsFound++;
                        if (numberOfCheckedGroupsFound < numberOfCheckedGroups) {
                            sb.append(";");
                        }
                    }
                    i++;
                }
                encParts.setAttributeNS(NS_URI, PREFIX + ATTR_GROUPS, sb.toString());
                root.appendChild(encParts);
                i = 0;
                for (; i < partsToEncrypt.size(); i++) {
                    element = document.createElement(ENC);
                    element.setTextContent(partsToEncrypt.get(i));
                    encParts.appendChild(element);
                }
            } else {
                for (; i < partsToEncrypt.size(); i++) {
                    eventText = eventText
                            .replaceFirst(Task.PLACE_HOLDER_PATTERN, partsToEncrypt.get(i));
                }
            }
        }
        element = document.createElement(PLAINTEXT);
        element.setTextContent(eventText);
        root.appendChild(element);
        element = document.createElement(LOCK);
        element.setTextContent(task.getLock().toString());
        root.appendChild(element);
        element = document.createElement(CREATION_DATE);
        element.setTextContent(Long.toString(task.getCreationDate().getTimeInMillis()));
        root.appendChild(element);

        element = document.createElement(FILENAME);
        element.setTextContent(task.getFilename());
        root.appendChild(element);

        return convertDomToString(new DOMSource(document));
    }
}
