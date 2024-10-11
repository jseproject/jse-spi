/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 2011-2019 The JCodec Project
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.tianscar.media.sound;

import org.tritonus.share.sampled.convert.TMatrixFormatConversionProvider;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.util.Arrays;

public class AACFormatConversionProvider extends TMatrixFormatConversionProvider {

    private static final AudioFormat[] INPUT_FORMATS = new AudioFormat[11 * 8];
    static {
        for (int i = 0; i < 8; i ++) {
            INPUT_FORMATS[11 * i] =
                    new AudioFormat(AACEncoding.AAC, 8000.0f, -1, i, -1, 8000.0f, true);
            INPUT_FORMATS[11 * i + 1] =
                    new AudioFormat(AACEncoding.AAC, 11025.0f, -1, i, -1, 11025.0f, true);
            INPUT_FORMATS[11 * i + 2] =
                    new AudioFormat(AACEncoding.AAC, 12000.0f, -1, i, -1, 12000.0f, true);
            INPUT_FORMATS[11 * i + 3] =
                    new AudioFormat(AACEncoding.AAC, 16000.0f, -1, i, -1, 16000.0f, true);
            INPUT_FORMATS[11 * i + 4] =
                    new AudioFormat(AACEncoding.AAC, 22050.0f, -1, i, -1, 22050.0f, true);
            INPUT_FORMATS[11 * i + 5] =
                    new AudioFormat(AACEncoding.AAC, 24000.0f, -1, i, -1, 24000.0f, true);
            INPUT_FORMATS[11 * i + 6] =
                    new AudioFormat(AACEncoding.AAC, 44100.0f, -1, i, -1, 44100.0f, true);
            INPUT_FORMATS[11 * i + 7] =
                    new AudioFormat(AACEncoding.AAC, 48000.0f, -1, i, -1, 48000.0f, true);
            INPUT_FORMATS[11 * i + 8] =
                    new AudioFormat(AACEncoding.AAC, 64000.0f, -1, i, -1, 64000.0f, true);
            INPUT_FORMATS[11 * i + 9] =
                    new AudioFormat(AACEncoding.AAC, 88200.0f, -1, i, -1, 88200.0f, true);
            INPUT_FORMATS[11 * i + 10] =
                    new AudioFormat(AACEncoding.AAC, 96000.0f, -1, i, -1, 96000.0f, true);
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

    /*
     *	One row for each source format.
     */
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
