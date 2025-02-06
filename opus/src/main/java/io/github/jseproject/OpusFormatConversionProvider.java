package io.github.jseproject;

import org.tritonus.share.sampled.convert.TMatrixFormatConversionProvider;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.util.Arrays;

public class OpusFormatConversionProvider extends TMatrixFormatConversionProvider {

    private static final AudioFormat[] INPUT_FORMATS = new AudioFormat[] {
            new AudioFormat(OpusEncoding.OPUS, 48000.0f, -1, 1, -1, -1, false), // 4
            new AudioFormat(OpusEncoding.OPUS, 48000.0f, -1, 2, -1, -1, false), // 5

            new AudioFormat(OpusEncoding.OPUS, 16000.0f, -1, 1, -1, -1, false), // 18
            new AudioFormat(OpusEncoding.OPUS, 16000.0f, -1, 2, -1, -1, false), // 19
            new AudioFormat(OpusEncoding.OPUS, 24000.0f, -1, 1, -1, -1, false), // 22
            new AudioFormat(OpusEncoding.OPUS, 24000.0f, -1, 2, -1, -1, false), // 23

            new AudioFormat(OpusEncoding.OPUS, 8000.0f, -1, 1, -1, -1, false),  // 36
            new AudioFormat(OpusEncoding.OPUS, 8000.0f, -1, 2, -1, -1, false),  // 37
            new AudioFormat(OpusEncoding.OPUS, 12000.0f, -1, 1, -1, -1, false), // 40
            new AudioFormat(OpusEncoding.OPUS, 12000.0f, -1, 2, -1, -1, false), // 41
    };

    private static final AudioFormat[] OUTPUT_FORMATS = new AudioFormat[] {
            new AudioFormat(8000.0f, 16, 1, true, false), // 0
            new AudioFormat(8000.0f, 16, 1, true, true),  // 1
            new AudioFormat(8000.0f, 16, 2, true, false), // 2
            new AudioFormat(8000.0f, 16, 2, true, true),  // 3
            /*	24 and 32 bit not yet possible
                            new AudioFormat(8000.0f, 24, 1, true, false),
                            new AudioFormat(8000.0f, 24, 1, true, true),
                            new AudioFormat(8000.0f, 24, 2, true, false),
                            new AudioFormat(8000.0f, 24, 2, true, true),
                            new AudioFormat(8000.0f, 32, 1, true, false),
                            new AudioFormat(8000.0f, 32, 1, true, true),
                            new AudioFormat(8000.0f, 32, 2, true, false),
                            new AudioFormat(8000.0f, 32, 2, true, true),
             */
            new AudioFormat(12000.0f, 16, 1, true, false), // 8
            new AudioFormat(12000.0f, 16, 1, true, true),  // 9
            new AudioFormat(12000.0f, 16, 2, true, false), // 10
            new AudioFormat(12000.0f, 16, 2, true, true),  // 11
            /*	24 and 32 bit not yet possible
                            new AudioFormat(12000.0f, 24, 1, true, false),
                            new AudioFormat(12000.0f, 24, 1, true, true),
                            new AudioFormat(12000.0f, 24, 2, true, false),
                            new AudioFormat(12000.0f, 24, 2, true, true),
                            new AudioFormat(12000.0f, 32, 1, true, false),
                            new AudioFormat(12000.0f, 32, 1, true, true),
                            new AudioFormat(12000.0f, 32, 2, true, false),
                            new AudioFormat(12000.0f, 32, 2, true, true),
             */
            new AudioFormat(16000.0f, 16, 1, true, false), // 12
            new AudioFormat(16000.0f, 16, 1, true, true),  // 13
            new AudioFormat(16000.0f, 16, 2, true, false), // 14
            new AudioFormat(16000.0f, 16, 2, true, true),  // 15
            /*	24 and 32 bit not yet possible
                            new AudioFormat(16000.0f, 24, 1, true, false),
                            new AudioFormat(16000.0f, 24, 1, true, true),
                            new AudioFormat(16000.0f, 24, 2, true, false),
                            new AudioFormat(16000.0f, 24, 2, true, true),
                            new AudioFormat(16000.0f, 32, 1, true, false),
                            new AudioFormat(16000.0f, 32, 1, true, true),
                            new AudioFormat(16000.0f, 32, 2, true, false),
                            new AudioFormat(16000.0f, 32, 2, true, true),
             */
            new AudioFormat(24000.0f, 16, 1, true, false), // 20
            new AudioFormat(24000.0f, 16, 1, true, true),  // 21
            new AudioFormat(24000.0f, 16, 2, true, false), // 22
            new AudioFormat(24000.0f, 16, 2, true, true),  // 23
            /*	24 and 32 bit not yet possible
                            new AudioFormat(24000.0f, 24, 1, true, false),
                            new AudioFormat(24000.0f, 24, 1, true, true),
                            new AudioFormat(24000.0f, 24, 2, true, false),
                            new AudioFormat(24000.0f, 24, 2, true, true),
                            new AudioFormat(24000.0f, 32, 1, true, false),
                            new AudioFormat(24000.0f, 32, 1, true, true),
                            new AudioFormat(24000.0f, 32, 2, true, false),
                            new AudioFormat(24000.0f, 32, 2, true, true),
             */
            new AudioFormat(48000.0f, 16, 1, true, false), // 32
            new AudioFormat(48000.0f, 16, 1, true, true),  // 33
            new AudioFormat(48000.0f, 16, 2, true, false), // 34
            new AudioFormat(48000.0f, 16, 2, true, true),  // 35
            /*	24 and 32 bit not yet possible
                            new AudioFormat(48000.0f, 24, 1, true, false),
                            new AudioFormat(48000.0f, 24, 1, true, true),
                            new AudioFormat(48000.0f, 24, 2, true, false),
                            new AudioFormat(48000.0f, 24, 2, true, true),
                            new AudioFormat(48000.0f, 32, 1, true, false),
                            new AudioFormat(48000.0f, 32, 1, true, true),
                            new AudioFormat(48000.0f, 32, 2, true, false),
                            new AudioFormat(48000.0f, 32, 2, true, true),
             */
    };

    private static final boolean t = true;
    private static final boolean f = false;

    private static final boolean[][] CONVERSIONS = new boolean[][] {
            new boolean[] {f,f,f,f,f,f, f,f,f,f,f,f, f,f,f,f, t,t,f,f},	// 4
            new boolean[] {f,f,f,f,f,f, f,f,f,f,f,f, f,f,f,f, f,f,t,t},	// 5

            new boolean[] {f,f,f,f,f,f, f,f,t,t,f,f, f,f,f,f, f,f,f,f},	// 18
            new boolean[] {f,f,f,f,f,f, f,f,f,f,t,t, f,f,f,f, f,f,f,f},	// 19
            new boolean[] {f,f,f,f,f,f, f,f,f,f,f,f, t,t,f,f, f,f,f,f},	// 22
            new boolean[] {f,f,f,f,f,f, f,f,f,f,f,f, f,f,t,t, f,f,f,f},	// 23

            new boolean[] {t,t,f,f,f,f, f,f,f,f,f,f, f,f,f,f, f,f,f,f},	// 36
            new boolean[] {f,f,t,t,f,f, f,f,f,f,f,f, f,f,f,f, f,f,f,f},	// 37
            new boolean[] {f,f,f,f,t,t, f,f,f,f,f,f, f,f,f,f, f,f,f,f},	// 40
            new boolean[] {f,f,f,f,f,f, t,t,f,f,f,f, f,f,f,f, f,f,f,f},	// 41
    };

    public OpusFormatConversionProvider() {
        super(Arrays.asList(INPUT_FORMATS), Arrays.asList(OUTPUT_FORMATS), CONVERSIONS);
    }

    @Override
    public AudioInputStream getAudioInputStream(AudioFormat targetFormat, AudioInputStream audioInputStream) {
        if (audioInputStream instanceof OpusAudioInputStream && isConversionSupported(targetFormat, audioInputStream.getFormat()))
            return new DecodedOpusAudioInputStream(targetFormat, (OpusAudioInputStream) audioInputStream);
        else throw new IllegalArgumentException("conversion not supported");
    }

}
