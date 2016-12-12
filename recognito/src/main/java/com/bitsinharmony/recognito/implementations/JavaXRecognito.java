package com.bitsinharmony.recognito.implementations;

import com.bitsinharmony.recognito.BaseRecognito;
import com.bitsinharmony.recognito.VoicePrint;
import com.bitsinharmony.recognito.converters.sound.JavaXSoundFileConverter;

import java.util.Map;

/**
 * Created by mlrivera on 12/11/16.
 */
public class JavaXRecognito<K> extends BaseRecognito<K> {


    public JavaXRecognito(float sampleRate) {
        super(sampleRate);
    }

    public JavaXRecognito(float sampleRate, Map<K, VoicePrint> voicePrintsByUserKey) {
        super(sampleRate, voicePrintsByUserKey);
    }

    @Override
    protected void initAudioFileConverter() {
        this.converter = new JavaXSoundFileConverter();
    }
}