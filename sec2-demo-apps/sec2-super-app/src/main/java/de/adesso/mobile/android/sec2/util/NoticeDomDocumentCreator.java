package de.adesso.mobile.android.sec2.util;

import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.adesso.mobile.android.sec2.model.Notice;

/**
 * Creates an XML document representing the notice. The document looks as follows:
 * 
 * <?xml version="1.0" encoding="UTF-8"?>
 * <notice>
 *  <subject>[subject]</subject>
 *  <encryptionParts xmlns:sec2="http://sec2.org/2012/03/middleware/enc/v1" sec2:groups=[Groups]>
 *   <encrypt>[Textpart to encrypt]</encrypt>
 *   <encrypt>[Textpart to encrypt]</encrypt>
 *  </encryptionParts>
 *  <plaintext>[Plaintext]</plaintext>
 *  <creation>[Creationdate]</creation>
 *  <lock>[Lock status]</lock>
</notice>

 * @author schuessler
 *
 */
public final class NoticeDomDocumentCreator extends AbstractDomDocumentCreator {

    private static final long serialVersionUID = -8122239917281281394L;
    //The namespace
    private static final String NS_URI = "http://sec2.org/2012/03/middleware";
    //The prefix incl. ":"
    private static final String PREFIX = "sec2:";
    //The tagname "notice"
    private static final String NOTICE = "notice";
    //The tagname "subject"
    private static final String SUBJECT = "subject";
    //The tagname "plaintext"
    private static final String PLAINTEXT = "plaintext";
    //The tagname "creation"
    private static final String CREATION_DATE = "creation";
    //The tagname "encryptionParts"
    private static final String ENC_PARTS = "encryptionParts";
    //The tagname "encrypt"
    private static final String ENC = "encrypt";
    //The tagname "lock"
    private static final String LOCK = "lock";
    //The attribute "groups"
    private static final String ATTR_GROUPS = "groups";
    // The attribute "filename"
    private static final String FILENAME = "file";

    private Notice notice = null;

    /**
     * The standard constructor
     */
    public NoticeDomDocumentCreator() {}

    public NoticeDomDocumentCreator(final Notice notice) {
        this.notice = notice;
    }

    public NoticeDomDocumentCreator(final String name, final Notice notice) {
        this.notice = notice;
        this.notice.setFilename(name);
        setDocumentName(name);
    }

    /**
     * @return the notice
     */
    public Notice getNotice() {
        return notice;
    }

    /**
     * @param notice - The notice to set
     */
    public void setNotice(final Notice notice) {
        this.notice = notice;
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
        final List<String> partsToEncrypt = notice.getPartsToEncrypt();
        String noticeText = notice.getNoticeText();

        document.setXmlStandalone(true);
        root = document.createElement(NOTICE);
        document.appendChild(root);
        element = document.createElement(SUBJECT);
        element.setTextContent(notice.getSubject());
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
                    noticeText = noticeText.replaceFirst(Notice.PLACE_HOLDER_PATTERN,
                            partsToEncrypt.get(i));
                }
            }
        }
        element = document.createElement(PLAINTEXT);
        element.setTextContent(noticeText);
        root.appendChild(element);
        element = document.createElement(CREATION_DATE);
        element.setTextContent(Long.toString(notice.getCreationDate().getTimeInMillis()));
        root.appendChild(element);
        element = document.createElement(LOCK);
        element.setTextContent(notice.getLock().toString());
        root.appendChild(element);

        element = document.createElement(FILENAME);
        element.setTextContent(notice.getFilename());
        root.appendChild(element);

        return convertDomToString(new DOMSource(document));
    }
}
