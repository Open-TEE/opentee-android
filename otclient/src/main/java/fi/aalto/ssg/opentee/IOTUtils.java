package fi.aalto.ssg.opentee;

import fi.aalto.ssg.opentee.exception.CommunicationErrorException;

/**
 * Open-TEE specific util functions for CA.
 */
public interface IOTUtils {
    /* allow push TA from CA */
    void installTA(String taName, byte[] taInBytes) throws CommunicationErrorException;
}
