/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 2019 Alexey Kuznetsov
 * Copyright (c) 2002-2018 Xiph.Org Foundation
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
 * A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE FOUNDATION
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.tianscar.media.sound;

import org.tritonus.share.sampled.convert.TMatrixFormatConversionProvider;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.util.Arrays;

public class VorbisFormatConversionProvider extends TMatrixFormatConversionProvider {

    private static final AudioFormat[] INPUT_FORMATS = new AudioFormat[] {
            new AudioFormat(VorbisEncoding.VORBIS, 32000.0f, -1, 1, -1, 32000.0f, false), // 0
            new AudioFormat(VorbisEncoding.VORBIS, 32000.0f, -1, 2, -1, 32000.0f, false), // 1
            new AudioFormat(VorbisEncoding.VORBIS, 44100.0f, -1, 1, -1, 44100.0f, false), // 2
            new AudioFormat(VorbisEncoding.VORBIS, 44100.0f, -1, 2, -1, 44100.0f, false), // 3
            new AudioFormat(VorbisEncoding.VORBIS, 48000.0f, -1, 1, -1, 48000.0f, false), // 4
            new AudioFormat(VorbisEncoding.VORBIS, 48000.0f, -1, 2, -1, 48000.0f, false), // 5
            
            new AudioFormat(VorbisEncoding.VORBIS, 16000.0f, -1, 1, -1, 16000.0f, false), // 18
            new AudioFormat(VorbisEncoding.VORBIS, 16000.0f, -1, 2, -1, 16000.0f, false), // 19
            new AudioFormat(VorbisEncoding.VORBIS, 22050.0f, -1, 1, -1, 22050.0f, false), // 20
            new AudioFormat(VorbisEncoding.VORBIS, 22050.0f, -1, 2, -1, 22050.0f, false), // 21
            new AudioFormat(VorbisEncoding.VORBIS, 24000.0f, -1, 1, -1, 24000.0f, false), // 22
            new AudioFormat(VorbisEncoding.VORBIS, 24000.0f, -1, 2, -1, 24000.0f, false), // 23
            
            new AudioFormat(VorbisEncoding.VORBIS, 8000.0f, -1, 1, -1, 8000.0f, false), // 36
            new AudioFormat(VorbisEncoding.VORBIS, 8000.0f, -1, 2, -1, 8000.0f, false), // 37
            new AudioFormat(VorbisEncoding.VORBIS, 11025.0f, -1, 1, -1, 11025.0f, false), // 38
            new AudioFormat(VorbisEncoding.VORBIS, 11025.0f, -1, 2, -1, 11025.0f, false), // 39
            new AudioFormat(VorbisEncoding.VORBIS, 12000.0f, -1, 1, -1, 12000.0f, false), // 40
            new AudioFormat(VorbisEncoding.VORBIS, 12000.0f, -1, 2, -1, 12000.0f, false), // 41
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
            new AudioFormat(11025.0f, 16, 1, true, false), // 4
            new AudioFormat(11025.0f, 16, 1, true, true),  // 5
            new AudioFormat(11025.0f, 16, 2, true, false), // 6
            new AudioFormat(11025.0f, 16, 2, true, true),  // 7
            /*	24 and 32 bit not yet possible
                            new AudioFormat(11025.0f, 24, 1, true, false),
                            new AudioFormat(11025.0f, 24, 1, true, true),
                            new AudioFormat(11025.0f, 24, 2, true, false),
                            new AudioFormat(11025.0f, 24, 2, true, true),
                            new AudioFormat(11025.0f, 32, 1, true, false),
                            new AudioFormat(11025.0f, 32, 1, true, true),
                            new AudioFormat(11025.0f, 32, 2, true, false),
                            new AudioFormat(11025.0f, 32, 2, true, true),
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
            new AudioFormat(22050.0f, 16, 1, true, false), // 16
            new AudioFormat(22050.0f, 16, 1, true, true),  // 17
            new AudioFormat(22050.0f, 16, 2, true, false), // 18
            new AudioFormat(22050.0f, 16, 2, true, true),  // 19
            /*	24 and 32 bit not yet possible
                            new AudioFormat(22050.0f, 24, 1, true, false),
                            new AudioFormat(22050.0f, 24, 1, true, true),
                            new AudioFormat(22050.0f, 24, 2, true, false),
                            new AudioFormat(22050.0f, 24, 2, true, true),
                            new AudioFormat(22050.0f, 32, 1, true, false),
                            new AudioFormat(22050.0f, 32, 1, true, true),
                            new AudioFormat(22050.0f, 32, 2, true, false),
                            new AudioFormat(22050.0f, 32, 2, true, true),
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
            new AudioFormat(32000.0f, 16, 1, true, false), // 24
            new AudioFormat(32000.0f, 16, 1, true, true),  // 25
            new AudioFormat(32000.0f, 16, 2, true, false), // 26
            new AudioFormat(32000.0f, 16, 2, true, true),  // 27
            /*	24 and 32 bit not yet possible
                            new AudioFormat(32000.0f, 24, 1, true, false),
                            new AudioFormat(32000.0f, 24, 1, true, true),
                            new AudioFormat(32000.0f, 24, 2, true, false),
                            new AudioFormat(32000.0f, 24, 2, true, true),
                            new AudioFormat(32000.0f, 32, 1, true, false),
                            new AudioFormat(32000.0f, 32, 1, true, true),
                            new AudioFormat(32000.0f, 32, 2, true, false),
                            new AudioFormat(32000.0f, 32, 2, true, true),
             */
            new AudioFormat(44100.0f, 16, 1, true, false), // 28
            new AudioFormat(44100.0f, 16, 1, true, true),  // 29
            new AudioFormat(44100.0f, 16, 2, true, false), // 30
            new AudioFormat(44100.0f, 16, 2, true, true),  // 31
            /*	24 and 32 bit not yet possible
                            new AudioFormat(44100.0f, 24, 1, true, false),
                            new AudioFormat(44100.0f, 24, 1, true, true),
                            new AudioFormat(44100.0f, 24, 2, true, false),
                            new AudioFormat(44100.0f, 24, 2, true, true),
                            new AudioFormat(44100.0f, 32, 1, true, false),
                            new AudioFormat(44100.0f, 32, 1, true, true),
                            new AudioFormat(44100.0f, 32, 2, true, false),
                            new AudioFormat(44100.0f, 32, 2, true, true),
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

    /*
     *	One row for each source format.
     */
    private static final boolean[][] CONVERSIONS = new boolean[][] {
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,t,t,f,f,f,f, f,f,f,f,f,f},	// 0
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,t,t,f,f, f,f,f,f,f,f},	// 1
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,t,t, f,f,f,f,f,f},	// 2
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, t,t,f,f,f,f},	// 3
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f},	// 4
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,t,t},	// 5

            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 18
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,t,t,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 19
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,t,t,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 20
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,t,t, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 21
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, t,t,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 22
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f,f,f,f,f, f,f,f,f,f,f},	// 23

            new boolean[] {t,t,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 36
            new boolean[] {f,f,t,t,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 37
            new boolean[] {f,f,f,f,t,t,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 38
            new boolean[] {f,f,f,f,f,f,t,t,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 39
            new boolean[] {f,f,f,f,f,f,f,f,t,t, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 40
            new boolean[] {f,f,f,f,f,f,f,f,f,f, t,t,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 41
    };

    public VorbisFormatConversionProvider() {
        super(Arrays.asList(INPUT_FORMATS), Arrays.asList(OUTPUT_FORMATS), CONVERSIONS);
    }

    @Override
    public AudioInputStream getAudioInputStream(AudioFormat targetFormat, AudioInputStream audioInputStream) {
        if (audioInputStream instanceof VorbisAudioInputStream && isConversionSupported(targetFormat, audioInputStream.getFormat()))
            return new DecodedVorbisAudioInputStream(targetFormat, (VorbisAudioInputStream) audioInputStream);
        else throw new IllegalArgumentException("conversion not supported");
    }

}
