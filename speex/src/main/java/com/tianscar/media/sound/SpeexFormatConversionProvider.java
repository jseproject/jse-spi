/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 1999-2004 Wimba S.A.
 * Copyright (c) 2002-2004 Xiph.org Foundation
 * Copyright (c) 2002-2004 Jean-Marc Valin
 * Copyright (c) 1993, 2002 David Rowe
 * Copyright (c) 1992-1994	Jutta Degener, Carsten Bormann
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Neither the name of the Xiph.org Foundation nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE FOUNDATION OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.tianscar.media.sound;

import org.tritonus.share.sampled.convert.TMatrixFormatConversionProvider;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.util.Arrays;

public class SpeexFormatConversionProvider extends TMatrixFormatConversionProvider {

    private static final AudioFormat[] INPUT_FORMATS = new AudioFormat[] {
            new AudioFormat(SpeexEncoding.SPEEX, 32000.0f, -1, 1, -1, 32000.0f, false), // 0
            new AudioFormat(SpeexEncoding.SPEEX, 32000.0f, -1, 2, -1, 32000.0f, false), // 1

            new AudioFormat(SpeexEncoding.SPEEX, 16000.0f, -1, 1, -1, 16000.0f, false), // 18
            new AudioFormat(SpeexEncoding.SPEEX, 16000.0f, -1, 2, -1, 16000.0f, false), // 19

            new AudioFormat(SpeexEncoding.SPEEX, 8000.0f, -1, 1, -1, 8000.0f, false),  // 36
            new AudioFormat(SpeexEncoding.SPEEX, 8000.0f, -1, 2, -1, 8000.0f, false),  // 37
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

    /*
     *	One row for each source format.
     */
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
