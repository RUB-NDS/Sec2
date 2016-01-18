/*
 * Copyright 2012 Ruhr-University Bochum, Chair for Network and Data Security
 *
 * This source code is part of the "Sec2" project and as this remains property
 * of the project partners. Content and concepts have to be treated as
 * CONFIDENTIAL. Publication or partly disclosure without explicit
 * written permission is prohibited.
 * For details on "Sec2" and its contributors visit
 *
 *        http://nds.rub.de/research/projects/sec2/
 */
package org.sec2.saml.xml;

import javax.xml.namespace.QName;
import org.opensaml.xml.signature.KeyInfo;
import org.sec2.saml.SAMLBaseConfig;

/**
 * An interface for an XML response message containing information
 * about a group.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * May 18, 2012
 */
public interface GroupResponseType extends GroupBaseType {

    /** Local name of the XSI type. */
    String TYPE_LOCAL_NAME = "groupResponseType";

    /** QName of the XSI type. */
    QName TYPE_NAME = new QName(SAMLBaseConfig.SEC2_SAML_NS, TYPE_LOCAL_NAME,
            SAMLBaseConfig.SEC2_SAML_PREFIX);

    /**
     * @return The group's wrapped group key
     */
    KeyInfo getGroupKey();

    /**
     * @param newGroupKey The GroupKey to set
     */
    void setGroupKey(KeyInfo newGroupKey);

    /**
     * @return A list of the IDs of all members
     */
    GroupMemberList getGroupMemberList();

    /**
     * @param members The members to set
     */
    void setGroupMemberList(GroupMemberList members);
}
