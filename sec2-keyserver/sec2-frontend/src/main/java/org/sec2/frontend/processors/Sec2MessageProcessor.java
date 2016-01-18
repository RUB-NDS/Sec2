/*
 * Copyright 2012 Ruhr-University Bochum, Chair for Network and Data Security
 *
 * This source code is part of the "Sec2" project and as this remains property
 * of the project partners. Content and concepts have to be treated as
 * CONFIDENTIAL. Publication or partly disclosure without explicit
 * written permission is prohibited.
 * For details on "Sec2" and its contributors process
 *
 *        http://nds.rub.de/research/projects/sec2/
 */
package org.sec2.frontend.processors;

import java.util.HashMap;
import java.util.Map;
import org.sec2.frontend.KeyserverFrontendConfig;
import org.sec2.frontend.exceptions.BackendProcessException;
import org.sec2.saml.xml.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Declares a process-function for BackendJobs and dispatches them to the
 * corresponding processor.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * November 28, 2012
 */
public class Sec2MessageProcessor implements ISec2RequestMessageProcessor {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(
            Sec2MessageProcessor.class);

    /**
     * A mapping between Sec2Objects and their corresponding processors.
     */
    private Map<Class<? extends Sec2RequestMessage>,
            ISec2RequestMessageProcessor> processors;

    /**
     * Constructor. Sets the mapping between Sec2Objects and their corresponding
     * processors.
     */
    public Sec2MessageProcessor() {
        // TODO: Set mapping via config file (optional)
        processors = new HashMap<Class<? extends Sec2RequestMessage>,
                ISec2RequestMessageProcessor>();
        processors.put(AddUsersToGroup.class, new AddUsersToGroupProcessor());
        processors.put(ConfirmUser.class, new ConfirmUserProcessor());
        processors.put(CreateGroup.class, new CreateGroupProcessor());
        processors.put(DeleteGroup.class, new DeleteGroupProcessor());
        processors.put(GetGroup.class, new GetGroupProcessor());
        processors.put(GetGroupMembers.class, new GetGroupMembersProcessor());
        processors.put(GetGroupsForUser.class, new GetGroupsForUserProcessor());
        processors.put(GetUserInfoByID.class, new GetUserInfoByIDProcessor());
        processors.put(GetUserInfoByMail.class,
                new GetUserInfoByMailProcessor());
        processors.put(GetKnownUsersForUser.class,
                new GetKnownUsersForUserProcessor());
        processors.put(RegisterUser.class, new RegisterUserProcessor());
        processors.put(RemoveUsersFromGroup.class,
                new RemoveUsersFromGroupProcessor());
        processors.put(UpdateGroup.class, new UpdateGroupProcessor());
        processors.put(UpdateUser.class, new UpdateUserProcessor());
    }

    /**
     * Dispatches the job to the corresponding processor.
     *
     * @param job The BackendJob to process
     * @return The payload of the response
     * @throws BackendProcessException if the keyserver fails to process a
     *          request in the backend.
     */
    @Override
    public final Sec2ResponseMessage process(final BackendJob job)
            throws BackendProcessException {
        if (job == null) {
            throw new IllegalArgumentException(
                    "Parameter job must not be null");
        }
        ISec2RequestMessageProcessor processor =
                getProcessor(job.getSec2Object().getClass());
        if (processor == null) {
            log.error("No processor registered for '{}'",
                    job.getSec2Object().getClass().getName());
            throw new BackendProcessException(
                    // Error in keyserver's configuration, don't tell the user
                    KeyserverFrontendConfig.DEFAULT_ERROR_MSG,
                    BackendProcessException.Impact.PROCESSING_ERROR,
                    job.getClientID(), job.getRequestID());
        }
        Sec2ResponseMessage response;
        try {
            response = processor.process(job);
        } catch (ClassCastException e) {
            log.error("Processor '{}' is not able to process '{}'-objects",
                    processor.getClass().getName(),
                    job.getSec2Object().getClass().getName());
            throw new BackendProcessException(
                    // Error in keyserver's configuration, don't tell the user
                    KeyserverFrontendConfig.DEFAULT_ERROR_MSG,
                    BackendProcessException.Impact.PROCESSING_ERROR,
                    job.getClientID(), job.getRequestID());
        }
        return response;
    }

    /**
     * Returns the processor that processes a concrete Sec2RequestMessage.
     *
     * @param clazz the class literal of the concrete Sec2RequestMessage
     * @return the processor that processes a concrete Sec2RequestMessage
     */
    protected final ISec2RequestMessageProcessor getProcessor(
            final Class<? extends Sec2RequestMessage> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException(
                    "Parameter clazz must not be null");
        }
        ISec2RequestMessageProcessor processor = null;
        log.debug("Looking up a processor for '{}'", clazz.getName());
        // Find a processor based on the interfaces the type implements
        for (Class clazzInterface : clazz.getInterfaces()) {
            processor = processors.get(clazzInterface);
            if (processor != null) {
                log.debug("Processor '{}' is registered to process '{}' based "
                        + "on interface '{}'", processor.getClass().getName(),
                            clazz.getName(), clazzInterface.getName());
                break;
            }
        }
        return processor;
    }
}
