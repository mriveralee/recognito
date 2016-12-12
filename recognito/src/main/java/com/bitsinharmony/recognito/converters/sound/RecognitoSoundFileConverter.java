package com.bitsinharmony.recognito.converters.sound;

import com.bitsinharmony.recognito.exceptions.RecognitoUnsupportedAudioFileException;

import java.io.File;
import java.io.IOException;

/**
 * Created by mlrivera on 12/11/16.
 */
public interface RecognitoSoundFileConverter {

    double[] convertFileToDoubleArray(File voiceSampleFile, float sampleRate)
            throws RecognitoUnsupportedAudioFileException, IOException;

}
