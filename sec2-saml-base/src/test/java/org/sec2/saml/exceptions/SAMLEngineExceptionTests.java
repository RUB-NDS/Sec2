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
package org.sec2.saml.exceptions;

/**
* Tests for the SAMLEngineException.
*
* @author  Dennis Felsch - dennis.felsch@rub.de
* @version 0.1
*
* December 03, 2012
 */
public final class SAMLEngineExceptionTests
            extends AbstractSelfLoggingExceptionTests<SAMLEngineException> {

    /** {@inheritDoc } */ @Override
    protected Class<SAMLEngineException> getClassLiteral() {
        return SAMLEngineException.class;
    }
}
