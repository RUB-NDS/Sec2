package de.adesso.mobile.android.sec2.util;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.adesso.mobile.android.sec2.model.Sec2File;

/**
 * Creates an XML document representing a file. The document looks as follows:
 * 
 * <?xml version="1.0" encoding="UTF-8"?>
 * <file>
 *  <name>[subject]</name>
 *  <encryptionParts xmlns:sec2="http://sec2.org/2012/03/middleware/enc/v1" sec2:groups=[Groups]>
 *   <encrypt>[Textpart to encrypt]</encrypt>
 *  </encryptionParts>
 *  <plaintext>[Plaintext]</plaintext>
 *  <creation>[Creationdate]</creation>
 *  <lock>[Lock status]</lock>
</notice>

 * @author hoppe
 *
 */
public final class FileDomDocumentCreator extends AbstractDomDocumentCreator {

    private static final long serialVersionUID = -1122239917281281394L;
    //The namespace
    private static final String NS_URI = "http://sec2.org/2012/03/middleware";
    //The prefix incl. ":"
    private static final String PREFIX = "sec2:";
    //The tagname "notice"
    private static final String FILE = "file";
    //The tagname "subject"
    private static final String NAME = "name";
    //The tagname "plaintext"
    private static final String PLAINTEXT = "plaintext";
    //The tagname "plaintextParts"
    private static final String PLAINTEXT_PARTS = "plaintextParts";
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

    private Sec2File sec2File = null;

    /**
     * The standard constructor
     */
    public FileDomDocumentCreator() {}

    public FileDomDocumentCreator(final Sec2File sec2File) {
        this.sec2File = sec2File;
    }

    public FileDomDocumentCreator(final String name, final Sec2File sec2File) {
        this.sec2File = sec2File;
        setDocumentName(name);
    }

    /**
     * @return the file
     */
    public Sec2File getSec2File() {
        return sec2File;
    }

    /**
     * @param file - The file to set
     */
    public void setFile(final Sec2File sec2File) {
        this.sec2File = sec2File;
    }

    @Override
    public String createDomDocument(final CheckedGroupHandler groupHandler)
            throws ParserConfigurationException, TransformerException {
        final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .newDocument();
        Element root = null;
        Element encParts = null;
        Element plaintextParts = null;
        Element element = null;
        StringBuilder sb = null;
        final int numberOfCheckedGroups = groupHandler.getNumberOfCheckedGroups();
        int numberOfCheckedGroupsFound = 0;
        int i = 0;
        final String[] partsToEncrypt = sec2File.getPartsToEncrypt();
        final String[] plainText = sec2File.getPlainText();

        document.setXmlStandalone(true);
        root = document.createElement(FILE);
        document.appendChild(root);
        element = document.createElement(NAME);
        element.setTextContent(sec2File.getName());
        root.appendChild(element);

        if (partsToEncrypt.length > 0) {
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
                for (; i < partsToEncrypt.length; i++) {
                    element = document.createElement(ENC);
                    element.setTextContent(partsToEncrypt[i]);
                    encParts.appendChild(element);
                }
            } else {
                for (; i < partsToEncrypt.length; i++) {
                    plainText[i] = Sec2File.PLACE_HOLDER_PATTERN;
                }
            }
        }

        plaintextParts = document.createElement(PLAINTEXT_PARTS);
        root.appendChild(plaintextParts);
        for (int j = 0; j < plainText.length; j++) {
            element = document.createElement(PLAINTEXT);
            element.setTextContent(plainText[j]);
            plaintextParts.appendChild(element);
        }

        element = document.createElement(CREATION_DATE);
        element.setTextContent(Long.toString(sec2File.getCreationDate().getTimeInMillis()));
        root.appendChild(element);
        element = document.createElement(LOCK);
        element.setTextContent(sec2File.getLock().toString());
        root.appendChild(element);

        return convertDomToString(new DOMSource(document));
    }
}
