/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sec2.pipeline.utils;

import java.util.Iterator;
import org.sec2.extern.javax.xml.namespace.QName;
import org.sec2.extern.javax.xml.stream.events.Attribute;
import org.sec2.pipeline.datatypes.DataType;
import org.sec2.pipeline.exceptions.ExXMLProcessingException;

/**
 *
 * @author Juraj Somorovsky - juraj.somorovsky@rub.de
 * @version 0.1
 */
public abstract class Utils {

    /**
     * Checks element name and namespace
     *
     * @param element
     * @param se
     * @throws ExXMLProcessingException
     */
    public static boolean checkElement(DataType.ELEMENT element,
            final QName qName)
            throws ExXMLProcessingException {
        boolean ret = false;
        if (qName.getNamespaceURI().equals(element.getNamespace())
                && qName.getLocalPart().equals(element.getElementName())) {
            ret = true;
        }
        return ret;
    }

    /**
     * Iterates over the attributes and returns an attribute if the element
     * contains the searched name-namespace
     *
     * @param namespace
     * @param name
     * @param iter
     * @return
     */
    public static String getAttribute(final String namespace, final String name,
            final Iterator<Attribute> iter) {
        while (iter.hasNext()) {
            Attribute a = iter.next();
            if (a.getName().getNamespaceURI().equals(namespace)
                    && a.getName().getLocalPart().equals(name)) {
                return a.getValue();
            }
        }
        return null;
    }

    private Utils() {
    }
}
