/*
 * (C) Copyright 2014 Amaury Crickx
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.bitsinharmony.recognito.implementations;

import com.bitsinharmony.recognito.BaseRecognito;
import com.bitsinharmony.recognito.VoicePrint;
import com.bitsinharmony.recognito.converters.sound.JavaXSoundFileConverter;

import java.util.Map;


public class Recognito<K> extends BaseRecognito<K> {

    /**
     * Default constructor
     * @param sampleRate the sample rate, at least 8000.0 Hz (preferably higher)
     */
    public Recognito(float sampleRate) {
        super(sampleRate);
    }

    /**
     * Constructor taking previously extracted voice prints directly into the system
     * @param sampleRate the sample rate, at least 8000.0 Hz (preferably higher)
     * @param voicePrintsByUserKey a {@code Map} containing user keys and their respective {@code VoicePrint}
     */
    public Recognito(float sampleRate, Map<K, VoicePrint> voicePrintsByUserKey) {
        super(sampleRate, voicePrintsByUserKey);
    }

    @Override
    protected void initAudioFileConverter() {
        this.converter = new JavaXSoundFileConverter();
    }

}
