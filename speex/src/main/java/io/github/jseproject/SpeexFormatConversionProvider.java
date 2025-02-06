package io.github.jseproject;

import org.tritonus.share.sampled.convert.TMatrixFormatConversionProvider;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.util.Arrays;

public class SpeexFormatConversionProvider extends TMatrixFormatConversionProvider {

    private static final AudioFormat[] INPUT_FORMATS = new AudioFormat[] {
            new AudioFormat(SpeexEncoding.SPEEX, 32000.0f, -1, 1, -1, -1, false), // 0
            new AudioFormat(SpeexEncoding.SPEEX, 32000.0f, -1, 2, -1, -1, false), // 1

            new AudioFormat(SpeexEncoding.SPEEX, 16000.0f, -1, 1, -1, -1, false), // 18
            new AudioFormat(SpeexEncoding.SPEEX, 16000.0f, -1, 2, -1, -1, false), // 19

            new AudioFormat(SpeexEncoding.SPEEX, 8000.0f, -1, 1, -1, -1, false),  // 36
            new AudioFormat(SpeexEncoding.SPEEX, 8000.0f, -1, 2, -1, -1, false),  // 37
    };

    private static final AudioFormat[] OUTPUT_FORMATS = new AudioFormat[] {
            new AudioFormat(8000.0f, 16, 1, true, false), // 0
            new AudioFormat(8000.0f, 16, 1, true, true),  // 1
            new AudioFormat(8000.0f, 16, 2, true, false), // 2
            new AudioFormat(8000.0f, 16, 2, true, true),  // 3
            new AudioFormat(16000.0f, 16, 1, true, false), // 12
            new AudioFormat(16000.0f, 16, 1, true, true),  // 13
            new AudioFormat(16000.0f, 16, 2, true, false), // 14
            new AudioFormat(16000.0f, 16, 2, true, true),  // 15
            new AudioFormat(32000.0f, 16, 1, true, false), // 24
            new AudioFormat(32000.0f, 16, 1, true, true),  // 25
            new AudioFormat(32000.0f, 16, 2, true, false), // 26
            new AudioFormat(32000.0f, 16, 2, true, true),  // 27
    };

    private static final boolean t = true;
    private static final boolean f = false;

    private static final boolean[][] CONVERSIONS = new boolean[][] {
            new boolean[] {f,f,f,f, f,f,f,f, t,t,f,f},	// 0
            new boolean[] {f,f,f,f, f,f,f,f, f,f,t,t},	// 1

            new boolean[] {f,f,f,f, t,t,f,f, f,f,f,f},	// 18
            new boolean[] {f,f,f,f, f,f,t,t, f,f,f,f},	// 19

            new boolean[] {t,t,f,f, f,f,f,f, f,f,f,f},	// 36
            new boolean[] {f,f,t,t, f,f,f,f, f,f,f,f},	// 37
    };

    public SpeexFormatConversionProvider() {
        super(Arrays.asList(INPUT_FORMATS), Arrays.asList(OUTPUT_FORMATS), CONVERSIONS);
    }

    @Override
    public AudioInputStream getAudioInputStream(AudioFormat targetFormat, AudioInputStream audioInputStream) {
        if (audioInputStream instanceof SpeexAudioInputStream && isConversionSupported(targetFormat, audioInputStream.getFormat()))
            return new DecodedSpeexAudioInputStream(targetFormat, (SpeexAudioInputStream) audioInputStream);
        else throw new IllegalArgumentException("conversion not supported");
    }

}
