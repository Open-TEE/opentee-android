package fi.aalto.ssg.opentee.testapp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Utility class for handling OmniShare Trustlet key blobs.
 *
 * @author Thomas Nyman <thomas.nyman@aalto.fi>
 */
public class Keychain {

    private ByteArrayOutputStream keychainBuilder;
    private int keyCount;

    /**
     * Construct new Keychain.
     *
     * @param capacity The initial capacity of the Keychain in bytes
     */
    public Keychain(int capacity) {
        keychainBuilder = new ByteArrayOutputStream(256);
    }

    /**
     * Construct new Keychain with default capacity.
     */
    public Keychain() {
        this(256);
    }

    /**
     * Append key to Keychain.
     *
     * @param key The key bloc to append
     * @throws IOException
     */
    public void append(byte[] key) throws IOException {
        keychainBuilder.write(key);
        keyCount++;
    }

    /**
     * Get number of keys in Keychain.
     *
     * @return The number of keys in Keychain
     */
    public int getKeyCount() {
        return keyCount;
    }

    public int getKeySize() {
        if (getKeyCount() == 0)
            return 0;
        else {
            return keychainBuilder.toByteArray().length / getKeyCount();
        }
    }

    /**
     * Return full keychain as byte array.
     *
     * @return Keychain byte array
     */
    public byte[] toByteArray(){
        return keychainBuilder.toByteArray();
    }
}
