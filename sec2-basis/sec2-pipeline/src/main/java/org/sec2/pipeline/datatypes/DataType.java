/*
 * Copyright 2011 Ruhr-University Bochum, Chair for Network and Data Security
 * 
 * This source code is part of the "Sec2" project and as this remains property
 * of the project partners. Content and concepts have to be treated as
 * CONFIDENTIAL. Publication or partly disclosure without explicit
 * written permission is prohibited.
 * For details on "Sec2" and its contributors visit
 * 
 *        http://nds.rub.de/research/projects/sec2/
 */
package org.sec2.pipeline.datatypes;

/**
 * 
 * @author Juraj Somorovsky - juraj.somorovsky@rub.de
 * @date Aug 20, 2012
 * @version 0.1
 *
 */
public class DataType {
    
    private DataType() {
        
    }

    public enum ELEMENT {
        
        KEY_INFO("KeyInfo", XMLSecurityConstants.XMLSIG_NS,
                XMLSecurityConstants.XMLSIG_PREFIX),
        KEY_NAME("KeyName", XMLSecurityConstants.XMLSIG_NS,
                XMLSecurityConstants.XMLSIG_PREFIX),
        ENCRYPTION_METHOD("EncryptionMethod", XMLSecurityConstants.XMLENC_NS,
                XMLSecurityConstants.XMLENC_PREFIX, "Algorithm", 
                XMLSecurityConstants.XMLENC_PREFIX),
        ENCRYPTED_KEY("EncryptedKey", XMLSecurityConstants.XMLENC_NS,
                XMLSecurityConstants.XMLENC_PREFIX),
        ENCRYPTED_DATA("EncryptedData", XMLSecurityConstants.XMLENC_NS,
                XMLSecurityConstants.XMLENC_PREFIX),
        CIPHER_DATA("CipherData", XMLSecurityConstants.XMLENC_NS,
                XMLSecurityConstants.XMLENC_PREFIX),
        CIPHER_VALUE("CipherValue", XMLSecurityConstants.XMLENC_NS,
                XMLSecurityConstants.XMLENC_PREFIX);
        
        ELEMENT(String name, String namespace, String namespacePrefix) {
            this.elementName = name;
            this.namespace = namespace;
            this.namespacePrefix = namespacePrefix;
        }
        
        ELEMENT(String name, String namespace, String namespacePrefix, 
                String attribute, String attributePrefix) {
            this.elementName = name;
            this.namespace = namespace;
            this.namespacePrefix = namespacePrefix;
            this.attribute = attribute;
            this.attributePrefix = attributePrefix;
        }

        private String elementName;
        
        private String namespace;
        
        private String namespacePrefix;
        
        private String attribute;
        
        private String attributePrefix;

        public String getElementName() {
            return elementName;
        }

        public void setElementName(String elementName) {
            this.elementName = elementName;
        }

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public String getNamespacePrefix() {
            return namespacePrefix;
        }

        public void setNamespacePrefix(String namespacePrefix) {
            this.namespacePrefix = namespacePrefix;
        }

        public String getAttribute() {
            return attribute;
        }

        public void setAttribute(String attribute) {
            this.attribute = attribute;
        }

        public String getAttributePrefix() {
            return attributePrefix;
        }

        public void setAttributePrefix(String attributePrefix) {
            this.attributePrefix = attributePrefix;
        }
    }
    
    public static boolean checkElement(ELEMENT element) {
        return false;
    }
}
