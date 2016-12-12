package com.bitsinharmony.recognito.converters.sound;

import com.bitsinharmony.recognito.exceptions.RecognitoUnsupportedAudioFileException;
import com.bitsinharmony.recognito.utils.JavaXSoundFileHelper;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

/**
 * Created by mlrivera on 12/11/16.
 */
public class JavaXSoundFileConverter implements RecognitoSoundFileConverter {

    /**
     * Converts the given audio file to an array of doubles with values between -1.0 and 1.0
     * @param voiceSampleFile the file to convert
     * @return an array of doubles
     * @throws UnsupportedAudioFileException when the JVM does not support the file format
     * @throws IOException when an I/O exception occurs
     */
    @Override
    public  double[] convertFileToDoubleArray(File voiceSampleFile, float sampleRate) throws RecognitoUnsupportedAudioFileException, IOException {
        try {
            AudioInputStream sample = AudioSystem.getAudioInputStream(voiceSampleFile);
            AudioFormat format = sample.getFormat();
            float diff = Math.abs(format.getSampleRate() - sampleRate);
            if (diff > 5.0F * Math.ulp(0.0F)) {
                throw new IllegalArgumentException(
                        "The sample rate for this file is different than Recognito\'s defined sample rate : ["
                                + format.getSampleRate() + "]");
            } else {
                return JavaXSoundFileHelper.readAudioInputStream(sample);
            }
        } catch (UnsupportedAudioFileException e) {
            throw new RecognitoUnsupportedAudioFileException(e);
        }
    }
}
