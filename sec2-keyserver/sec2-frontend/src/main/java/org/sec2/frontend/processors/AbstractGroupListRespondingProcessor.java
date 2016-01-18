/*
 * Copyright 2013 Ruhr-University Bochum, Chair for Network and Data Security
 *
 * This source code is part of the "Sec2" project and as this remains property
 * of the project partners. Content and concepts have to be treated as
 * CONFIDENTIAL. Publication or partly disclosure without explicit
 * written permission is prohibited.
 * For details on "Sec2" and its contributors visit
 *
 *        http://nds.rub.de/research/projects/sec2/
 */
package org.sec2.frontend.processors;

import java.util.List;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.schema.XSString;
import org.sec2.backend.IGroupInfo;
import org.sec2.saml.engine.SAMLEngine;
import org.sec2.saml.engine.XMLHelper;
import org.sec2.saml.xml.Sec2RequestMessage;
import org.sec2.saml.xml.GroupList;
import org.sec2.saml.xml.GroupName;

/**
 * Abstract prototype of a processor that returns a GroupList object.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @param <T> The type of Sec2RequestMessage to process
 * @version 0.1
 *
 * July 26, 2013
 */
abstract class AbstractGroupListRespondingProcessor<T extends
        Sec2RequestMessage> extends AbstractSec2MessageProcessor<T, GroupList> {

    /**
     * Creates an XML GroupList from a List&lt;IGroupInfo&gt; object.
     *
     * @param backendObject The List&lt;IGroupInfo&gt;
     * @return an XML GroupList
     */
    protected final GroupList createGroupList(
            final List<IGroupInfo> backendObject) {
        // create response payload
        GroupList payload = SAMLEngine.getXMLBuilder(GroupList.class,
                GroupList.DEFAULT_ELEMENT_NAME).buildObject(
                GroupList.DEFAULT_ELEMENT_NAME);

        // set group names
        for (IGroupInfo group : backendObject) {
            XSString groupXML = this.getXsGenerator().buildXSString(
                    GroupName.DEFAULT_ELEMENT_NAME);
            groupXML.setValue(group.getGroupName());
            payload.getGroups().add(groupXML);
        }

        // saves performance if logging is disabled
        if (this.getLogger().isTraceEnabled()) {
            try {
                this.getLogger().trace("GroupList content: {}",
                        XMLHelper.getXMLString(payload));
            } catch (MarshallingException e) {
                // Can't fix this here. But the exception will occur again when
                // the payload is encrypted und will be handled there
                this.getLogger().error(
                        "Generated response payload could not be marshalled");
            }
        }
        return payload;
    }
}
