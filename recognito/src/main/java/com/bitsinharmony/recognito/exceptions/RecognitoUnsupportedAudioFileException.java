package com.bitsinharmony.recognito.exceptions;

/**
 * Created by mlrivera on 12/11/16.
 */

public class RecognitoUnsupportedAudioFileException extends Exception {

    public RecognitoUnsupportedAudioFileException() {
        // Nothing
    }

    public RecognitoUnsupportedAudioFileException(String msg) {
        super(msg);
    }

    public RecognitoUnsupportedAudioFileException(Throwable cause) {
        super(cause);
    }

    public RecognitoUnsupportedAudioFileException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
