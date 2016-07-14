package fi.aalto.ssg.opentee.testapp;

import java.util.Arrays;

/**
 * Utility functions for manipulating hexadecimal representations of byte arrays.
 *
 * The original implementations of encodeHex and decodeHex are sourced from the following
 * StackoOerflow answer: https://stackoverflow.com/questions/332079/2197650#
 * The initial implementation of encodeHex can be attributed to user Jemenake, with optimizations
 * by Scott Carey. The implementation of decodeHex can be attributed to user cn1h,
 *
 * @author Thomas Nyman <thomas.nyman@aalto.fi>
 */
public final class HexUtils {

    /**
     * Character array used for hexadecimal conversion
     */
    protected static final char[] hexArray =
            {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

    /**
     * Converts an array of bytes into an array of characters representing the hexadecimal value of
     * each byte in the array in order. The returned array will be double the length of the passed
     * array, as it takes two characters to represent any given byte.
     *
     *
     *
     * @param bytes array of bytes to encode
     * @return character array containing hexadecimal representation of each byte in the byte array
     */
    public static char[] encodeHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return hexChars;
    }

    /**
     * Converts an array of characters representing hexadecimal values into an array of bytes of
     * each character in order.
     *
     * @param hexChars array of characters to decode
     * @return byte array containing the byte representation of each hexadecimal characters in the argument array
     */
    public static byte[] decodeHex(char[] hexChars) {
        byte[] result = new byte[hexChars.length / 2];
        for (int j = 0; j < hexChars.length; j += 2) {
            result[j / 2] = (byte) (
                    Arrays.binarySearch(hexArray, hexChars[j]) * 16 +
                            Arrays.binarySearch(hexArray, hexChars[j + 1])
            );
        }
        return result;
    }

    /**
     * Converts an array of bytes into a String representing the hexadecimal value of each byte in
     * the array in order.
     *
     * @param bytes array of bytes to encode
     * @return String containing hexadecimal representation of each byte in the argument array
     */
    public static String encodeHexString(byte[] bytes) {
        return new String(encodeHex(bytes));
    }

    /**
     * Converts a String representing hexadecimal values into an array of bytes of each character in
     * the String in order.
     *
     * @param hexString String of characters to decode
     * @return byte array containing the byte representation of each hexadecimal characters in the String
     */
    public static byte[] decodeHexString(String hexString) {
        return decodeHex(hexString.toCharArray());
    }
}
