package com.bitsinharmony.recognito.converters;

/**
 * Created by mlrivera on 12/11/16.
 */
public abstract class PrimitiveTypeConverter {

    public static short byteArrayToShort(byte[] bytes, int offset, boolean bigEndian) {
        int low, high;
        if (bigEndian) {
            low = bytes[offset + 1];
            high = bytes[offset];
        } else {
            low = bytes[offset ];
            high = bytes[offset + 1];
        }
        return (short) ((high << 8) | (0xFF & low));
    }

}
