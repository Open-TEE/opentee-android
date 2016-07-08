package fi.aalto.ssg.opentee;

import java.io.FileNotFoundException;
import java.io.IOException;

import fi.aalto.ssg.opentee.exception.CommunicationErrorException;

/**
 * Open-TEE specific util functions for CA.
 */
public interface IOTUtils {
    /* allow push TA from CA */
    void installTA(String taName, byte[] taInBytes) throws CommunicationErrorException;

    /* push TA under CA application lib folder to Open-TEE */
    boolean installTA(String taFileName) throws CommunicationErrorException;
}
