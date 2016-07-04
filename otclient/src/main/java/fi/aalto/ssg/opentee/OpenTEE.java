package fi.aalto.ssg.opentee;

import fi.aalto.ssg.opentee.imps.OTClient;

/**
 * Factory method wrapper for OpenTEE Android.
 */
public class OpenTEE{
    /**
     * Factory method to create a new TEEClient interface.
     * @return an ITEEClient interface.
     */
    public static ITEEClient newTEEClient(){
        return new OTClient();
    }
}
