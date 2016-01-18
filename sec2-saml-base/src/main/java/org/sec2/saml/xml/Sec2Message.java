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

/**
 * A marker interface for all top level XML messages in the sec2 SAML module.
 * Used to distinguish between normal XMLObjects used in OpenSAML and the
 * special Sec2 elements. With these markers the neither client nor keyserver
 * can be fooled to process elements they did not expect.
 * Only for inheritance, should not be implemented directly.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * November 22, 2012
 */
public abstract interface Sec2Message { }
