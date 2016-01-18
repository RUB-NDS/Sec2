/*
 * Copyright 2011 Sec2 Consortium
 *
 * This source code is part of the "Sec2" project and as this remains property
 * of the project partners. Content and concepts have to be treated as
 * CONFIDENTIAL. Publication or partly disclosure without explicit written
 * permission is prohibited.
 * For details on "Sec2" and its contributors visit
 *
 *        http://www.sec2.org
 */
package org.sec2.securityprovider.serviceparameter;

/**
 * Marker interface to express Service parameters.
 *
 * @author Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1 Jul 25, 2011
 */
public interface IServiceParameter {

    /**
     * Default message if non valid service parameters are handled.
     */
    String NO_SERVICE_PARAMETER =
            "Handled parameter is no valid service parameter";
}
