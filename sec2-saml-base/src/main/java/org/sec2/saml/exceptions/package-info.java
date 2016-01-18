/**
 * Contains exceptions that can occur in the SAML engine
 * and the KeyserverConnector.
 *
 * All exceptions extend
 * {@link org.sec2.saml.exceptions.AbstractSelfLoggingException} which provides
 * support to log the exception via SLF4J. The class
 * {@link org.sec2.saml.exceptions.SAMLEngineException}
 * is the base for the other exceptions in this package.
 *
 * All exceptions have a constructor for a message only, a throwable only,
 * a message and a throwable, and for all fields available. A protected
 * constructor is provided to allow subclasses to set a prefix.
 *
 * Package is maintained by: NDS
 * Responsible developer(s):
 * Dennis Felsch - dennis.felsch@rub.de
 */
package org.sec2.saml.exceptions;
