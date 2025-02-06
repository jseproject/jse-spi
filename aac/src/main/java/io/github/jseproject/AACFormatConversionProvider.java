package io.github.jseproject;

import org.tritonus.share.sampled.convert.TMatrixFormatConversionProvider;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.util.Arrays;

public class AACFormatConversionProvider extends TMatrixFormatConversionProvider {

    private static final AudioFormat[] INPUT_FORMATS = new AudioFormat[11 * 8];
    static {
        for (int i = 0; i < 8; i ++) {
            INPUT_FORMATS[11 * i] =
                    new AudioFormat(AACEncoding.AAC, 8000.0f, -1, i, -1, -1, true);
            INPUT_FORMATS[11 * i + 1] =
                    new AudioFormat(AACEncoding.AAC, 11025.0f, -1, i, -1, -1, true);
            INPUT_FORMATS[11 * i + 2] =
                    new AudioFormat(AACEncoding.AAC, 12000.0f, -1, i, -1, -1, true);
            INPUT_FORMATS[11 * i + 3] =
                    new AudioFormat(AACEncoding.AAC, 16000.0f, -1, i, -1, -1, true);
            INPUT_FORMATS[11 * i + 4] =
                    new AudioFormat(AACEncoding.AAC, 22050.0f, -1, i, -1, -1, true);
            INPUT_FORMATS[11 * i + 5] =
                    new AudioFormat(AACEncoding.AAC, 24000.0f, -1, i, -1, -1, true);
            INPUT_FORMATS[11 * i + 6] =
                    new AudioFormat(AACEncoding.AAC, 44100.0f, -1, i, -1, -1, true);
            INPUT_FORMATS[11 * i + 7] =
                    new AudioFormat(AACEncoding.AAC, 48000.0f, -1, i, -1, -1, true);
            INPUT_FORMATS[11 * i + 8] =
                    new AudioFormat(AACEncoding.AAC, 64000.0f, -1, i, -1, -1, true);
            INPUT_FORMATS[11 * i + 9] =
                    new AudioFormat(AACEncoding.AAC, 88200.0f, -1, i, -1, -1, true);
            INPUT_FORMATS[11 * i + 10] =
                    new AudioFormat(AACEncoding.AAC, 96000.0f, -1, i, -1, -1, true);
        }
    }

    private static final AudioFormat[] OUTPUT_FORMATS = new AudioFormat[] {
            new AudioFormat(8000.0f, 16, 1, true, false),
            new AudioFormat(8000.0f, 16, 1, true, true),
            new AudioFormat(8000.0f, 16, 2, true, false),
            new AudioFormat(8000.0f, 16, 2, true, true),
            new AudioFormat(11025.0f, 16, 1, true, false),
            new AudioFormat(11025.0f, 16, 1, true, true),
            new AudioFormat(11025.0f, 16, 2, true, false),
            new AudioFormat(11025.0f, 16, 2, true, true),
            new AudioFormat(12000.0f, 16, 1, true, false),
            new AudioFormat(12000.0f, 16, 1, true, true),
            new AudioFormat(12000.0f, 16, 2, true, false),
            new AudioFormat(12000.0f, 16, 2, true, true),
            new AudioFormat(16000.0f, 16, 1, true, false),
            new AudioFormat(16000.0f, 16, 1, true, true),
            new AudioFormat(16000.0f, 16, 2, true, false),
            new AudioFormat(16000.0f, 16, 2, true, true),
            new AudioFormat(22050.0f, 16, 1, true, false),
            new AudioFormat(22050.0f, 16, 1, true, true),
            new AudioFormat(22050.0f, 16, 2, true, false),
            new AudioFormat(22050.0f, 16, 2, true, true),
            new AudioFormat(24000.0f, 16, 1, true, false),
            new AudioFormat(24000.0f, 16, 1, true, true),
            new AudioFormat(24000.0f, 16, 2, true, false),
            new AudioFormat(24000.0f, 16, 2, true, true),
            new AudioFormat(44100.0f, 16, 1, true, false),
            new AudioFormat(44100.0f, 16, 1, true, true),
            new AudioFormat(44100.0f, 16, 2, true, false),
            new AudioFormat(44100.0f, 16, 2, true, true),
            new AudioFormat(48000.0f, 16, 1, true, false),
            new AudioFormat(48000.0f, 16, 1, true, true),
            new AudioFormat(48000.0f, 16, 2, true, false),
            new AudioFormat(48000.0f, 16, 2, true, true),
            new AudioFormat(64000.0f, 16, 1, true, false),
            new AudioFormat(64000.0f, 16, 1, true, true),
            new AudioFormat(64000.0f, 16, 2, true, false),
            new AudioFormat(64000.0f, 16, 2, true, true),
            new AudioFormat(88200.0f, 16, 1, true, false),
            new AudioFormat(88200.0f, 16, 1, true, true),
            new AudioFormat(88200.0f, 16, 2, true, false),
            new AudioFormat(88200.0f, 16, 2, true, true),
            new AudioFormat(96000.0f, 16, 1, true, false),
            new AudioFormat(96000.0f, 16, 1, true, true),
            new AudioFormat(96000.0f, 16, 2, true, false),
            new AudioFormat(96000.0f, 16, 2, true, true),
    };

    private static final boolean t = true;
    private static final boolean f = false;

    private static final boolean[][] CONVERSIONS = new boolean[11 * 8][44];
    static {
        for (int i = 0; i < 8; i ++) {
            CONVERSIONS[11 * i] =
                    new boolean[] {t,t,t,t, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f};
            CONVERSIONS[11 * i + 1] =
                    new boolean[] {f,f,f,f, t,t,t,t, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f};
            CONVERSIONS[11 * i + 2] =
                    new boolean[] {f,f,f,f, f,f,f,f, t,t,t,t, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f};
            CONVERSIONS[11 * i + 3] =
                    new boolean[] {f,f,f,f, f,f,f,f, f,f,f,f, t,t,t,t, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f};
            CONVERSIONS[11 * i + 4] =
                    new boolean[] {f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, t,t,t,t, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f};
            CONVERSIONS[11 * i + 5] =
                    new boolean[] {f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, t,t,t,t, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f};
            CONVERSIONS[11 * i + 6] =
                    new boolean[] {f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, t,t,t,t, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f};
            CONVERSIONS[11 * i + 7] =
                    new boolean[] {f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, t,t,t,t, f,f,f,f, f,f,f,f, f,f,f,f};
            CONVERSIONS[11 * i + 8] =
                    new boolean[] {f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, t,t,t,t, f,f,f,f, f,f,f,f};
            CONVERSIONS[11 * i + 9] =
                    new boolean[] {f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, t,t,t,t, f,f,f,f};
            CONVERSIONS[11 * i + 10] =
                    new boolean[] {f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, f,f,f,f, t,t,t,t};
        }
    }

    public AACFormatConversionProvider() {
        super(Arrays.asList(INPUT_FORMATS), Arrays.asList(OUTPUT_FORMATS), CONVERSIONS);
    }

    @Override
    public AudioInputStream getAudioInputStream(AudioFormat targetFormat, AudioInputStream audioInputStream) {
        if (audioInputStream instanceof AACAudioInputStream && isConversionSupported(targetFormat, audioInputStream.getFormat()))
            return new DecodedAACAudioInputStream(targetFormat, (AACAudioInputStream) audioInputStream);
        else throw new IllegalArgumentException("conversion not supported");
    }

}
