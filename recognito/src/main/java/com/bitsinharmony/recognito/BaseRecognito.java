package com.bitsinharmony.recognito;

import com.bitsinharmony.recognito.distances.EuclideanDistanceCalculator;
import com.bitsinharmony.recognito.enhancements.Normalizer;
import com.bitsinharmony.recognito.exceptions.RecognitoUnsupportedAudioFileException;
import com.bitsinharmony.recognito.features.LpcFeaturesExtractor;
import com.bitsinharmony.recognito.converters.sound.RecognitoSoundFileConverter;
import com.bitsinharmony.recognito.vad.AutocorrellatedVoiceActivityDetector;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Front class for accessing Recognito's speaker recognition features
 * <p>
 * {@code BaseRecognito} holds a set of voice prints associated with user keys and allows
 * execution of different tasks on them :
 * </p>
 * <ul>
 * <li>Create a voice print from an audio sample and store it with an associated user key</li>
 * <li>Merge a new voice sample into an existing voice print</li>
 * <li>Speaker recognition : analyse voice characteristics from an unknown sample and return a {@code List}
 * of {@code MatchResult}s sorted by distance. A likelihood ratio is provided within each {@code MatchResult}.</li>
 * </ul>
 * <p>
 * {@code BaseRecognito} expects all voice samples to be comprised of a single channel (i.e. mono). Using a stereo sample
 * whose channels are identical will merely double processing time. Using a real stereo sample will make
 * the processing less accurate while doubling processing time.<br/>
 * </p>
 * <p>
 * It is up to the user to manage persistence of the created voice print objects. Persisted voice prints
 * may be passed into an alternate {@code Recognito} constructor as a {@code Map} of user keys pointing to a voice print.
 * </p>
 * <p>
 * For methods taking a file handle :<br/>
 * Supporting each and every file formats is a real pain and not the primary goal of Recognito. As such,
 * the conversion capabilities of the javax.sound.sampled package are used internally.
 * Depending on your particular JVM implementation, some file types may or may not be supported.
 * If you're looking for MP3 or Ogg support, check Javazoom SPI's. This said, the higher the sample quality, the better the results.
 * In case you may choose, {@code Recognito}'s preferred file format is PCM 16bit mono 16000 Hz (WAV files are PCM)<br/>
 * You may also want to check http://sox.sourceforge.net for dedicated conversion software.
 * </p>
 * <p>
 * Please note the sample rate is actually twice the highest audio frequency of the sample. E.g. a sample rate of 8KHz means
 * that the highest frequency available in the sample is 4KHz. So you can't resample at a higher frequency and expect
 * the voice samples to be comparable, some frequencies will be missing. You may downsample, but if don't know how to do that
 * correctly, you're better off using dedicated software. For the purpose of extracting voice prints,
 * 16KHz appears to be the most interesting choice.
 * </p>
 * <p>
 * The likelihood ratio available within the {@code MatchResult}s is calculated as the relative distance between the given voice sample,
 * a known {@code VoicePrint} and a so called Universal Model. The universal model in {@code Recognito} is by default created as an average of all
 * {@code VoicePrint}s available in the system. The closer you are to the known {@code VoicePrint}, the higher the likelihood.
 * Each time a new sample is sent to {@code BaseRecognito}'s create or merge methods, the extracted features are added to the model.
 * You may create your own model by merging a selected set of voice samples into a single {@code VoicePrint}.
 * Once done, you may set this model once and for all in {@code BaseRecognito}, it won't be updated afterwards.
 * A Universal Model is language dependent. At this very moment, it doesn't look realistic that {@code Recognito} would provide
 * generic models for each and every language. Furthermore, the recording system you're using will also severely impact the relevance of a
 * generic model in that the sonic characteristics will be quite different from one system to another. In other words, providing generic
 * models would most probably create likelihood ratios that are unreasonably high because far from your own recordings and thus irrelevant.
 * </p>
 * <p>
 * Threading : usage of {@code BaseRecognito} is thread safe, see methods documentation for details
 * </p>
 * @param <K> {@code BaseRecognito} is genericized in order to allow the user to specify its own type of user keys.
 * The constraints on user keys are the same as those for a {@code java.util.Map} key
 * @author Amaury Crickx
 * @see {@link java.util.Map} for the constraints on Key objects
 */

public abstract class BaseRecognito<K> {

    protected static final float MIN_SAMPLE_RATE = 8000.0F;

    protected final ConcurrentHashMap<K, VoicePrint> store;
    protected final float sampleRate;
    protected final AtomicBoolean universalModelWasSetByUser;
    protected VoicePrint universalModel;
    protected RecognitoSoundFileConverter converter;

    public BaseRecognito(float sampleRate) {
        this.store = new ConcurrentHashMap();
        this.universalModelWasSetByUser = new AtomicBoolean();
        if(sampleRate < 8000.0F) {
            throw new IllegalArgumentException("Sample rate should be at least 8000 Hz");
        } else {
            this.sampleRate = sampleRate;
        }
        initAudioFileConverter();
    }

    public BaseRecognito(float sampleRate, Map<K, VoicePrint> voicePrintsByUserKey) {
        this(sampleRate);
        Iterator it = voicePrintsByUserKey.values().iterator();
        if(it.hasNext()) {
            VoicePrint print = (VoicePrint)it.next();
            this.universalModel = new VoicePrint(print);

            while(it.hasNext()) {
                this.universalModel.merge((VoicePrint)it.next());
            }
        }

        this.store.putAll(voicePrintsByUserKey);
    }

    /**
     * Use this method to initialized the audio convert that Recognito will use
     * Something like this.converter = new JavaXSoundFileConverter
     */
    protected void initAudioFileConverter() {
        throw new UnsupportedOperationException("Implement this method to initialize your converter of choice!");
    }

    public VoicePrint getUniversalModel() {
        return new VoicePrint(this.universalModel);
    }

    public synchronized void setUniversalModel(VoicePrint universalModel) {
        if(universalModel == null) {
            throw new IllegalArgumentException("The universal model may not be null");
        } else {
            this.universalModelWasSetByUser.set(false);
            this.universalModel = universalModel;
        }
    }

    public synchronized VoicePrint createVoicePrint(K userKey, double[] voiceSample) {
        if(userKey == null) {
            throw new NullPointerException("The userKey is null");
        } else if(this.store.containsKey(userKey)) {
            throw new IllegalArgumentException("The userKey already exists: [" + userKey + "]");
        } else {
            double[] features = this.extractFeatures(voiceSample, this.sampleRate);
            VoicePrint voicePrint = new VoicePrint(features);
            synchronized(this) {
                if(!this.universalModelWasSetByUser.get()) {
                    if(this.universalModel == null) {
                        this.universalModel = new VoicePrint(voicePrint);
                    } else {
                        this.universalModel.merge(features);
                    }
                }
            }

            this.store.put(userKey, voicePrint);
            return voicePrint;
        }
    }

    public VoicePrint mergeVoiceSample(K userKey, double[] voiceSample) {
        if(userKey == null) {
            throw new NullPointerException("The userKey is null");
        } else {
            VoicePrint original = (VoicePrint)this.store.get(userKey);
            if(original == null) {
                throw new IllegalArgumentException("No voice print linked to this user key [" + userKey + "]");
            } else {
                double[] features = this.extractFeatures(voiceSample, this.sampleRate);
                synchronized(this) {
                    if(!this.universalModelWasSetByUser.get()) {
                        this.universalModel.merge(features);
                    }
                }

                original.merge(features);
                return original;
            }
        }
    }

    public List<MatchResult<K>> identify(double[] voiceSample) {
        if(this.store.isEmpty()) {
            throw new IllegalStateException("There is no voice print enrolled in the system yet");
        } else {
            VoicePrint voicePrint = new VoicePrint(this.extractFeatures(voiceSample, this.sampleRate));
            EuclideanDistanceCalculator calculator = new EuclideanDistanceCalculator();
            ArrayList matches = new ArrayList(this.store.size());
            double distanceFromUniversalModel = voicePrint.getDistance(calculator, this.universalModel);
            Iterator var7 = this.store.entrySet().iterator();

            while(var7.hasNext()) {
                Map.Entry entry = (Map.Entry)var7.next();
                double distance = ((VoicePrint)entry.getValue()).getDistance(calculator, voicePrint);
                int likelihood = 100 - (int)(distance / (distance + distanceFromUniversalModel) * 100.0D);
                matches.add(new MatchResult(entry.getKey(), likelihood, distance));
            }

            Collections.sort(matches, new Comparator() {
                @Override
                public int compare(Object o1, Object o2) {
                    MatchResult<K> m1 = (MatchResult<K>) o1;
                    MatchResult<K> m2 = (MatchResult<K>) o2;
                    return Double.compare(m1.getDistance(), m2.getDistance());
                }
            });
            return matches;
        }
    }

    public VoicePrint createVoicePrint(K userKey, File voiceSampleFile) throws RecognitoUnsupportedAudioFileException, IOException {
        double[] audioSample = this.convertFileToDoubleArray(voiceSampleFile);
        return this.createVoicePrint(userKey, audioSample);
    }

    public VoicePrint mergeVoiceSample(K userKey, File voiceSampleFile) throws RecognitoUnsupportedAudioFileException, IOException {
        double[] audioSample = this.convertFileToDoubleArray(voiceSampleFile);
        return this.mergeVoiceSample(userKey, audioSample);
    }

    public List<MatchResult<K>> identify(File voiceSampleFile) throws RecognitoUnsupportedAudioFileException, IOException {
        double[] audioSample = this.convertFileToDoubleArray(voiceSampleFile);
        return this.identify(audioSample);
    }

    private double[] convertFileToDoubleArray(File voiceSampleFile) throws RecognitoUnsupportedAudioFileException, IOException {
        if (converter == null) {
            throw new IllegalStateException("You must set a Sound Converter!");
        }
        return converter.convertFileToDoubleArray(voiceSampleFile, this.sampleRate);
    }

    protected double[] extractFeatures(double[] voiceSample, float sampleRate) {
        AutocorrellatedVoiceActivityDetector voiceDetector = new AutocorrellatedVoiceActivityDetector();
        Normalizer normalizer = new Normalizer();
        LpcFeaturesExtractor lpcExtractor = new LpcFeaturesExtractor(sampleRate, 20);
        voiceDetector.removeSilence(voiceSample, sampleRate);
        normalizer.normalize(voiceSample, sampleRate);
        double[] lpcFeatures = (double[])lpcExtractor.extractFeatures(voiceSample);
        return lpcFeatures;
    }


}
