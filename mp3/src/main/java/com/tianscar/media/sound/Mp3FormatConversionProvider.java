/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 1999-2010 The LAME Project
 * Copyright (c) 1999-2008 JavaZOOM
 * Copyright (c) 2001-2002 Naoki Shibata
 * Copyright (c) 2001 Jonathan Dee
 * Copyright (c) 2000-2017 Robert Hegemann
 * Copyright (c) 2000-2008 Gabriel Bouvigne
 * Copyright (c) 2000-2005 Alexander Leidinger
 * Copyright (c) 2000 Don Melton
 * Copyright (c) 1999-2005 Takehiro Tominaga
 * Copyright (c) 1999-2001 Mark Taylor
 * Copyright (c) 1999 Albert L. Faber
 * Copyright (c) 1988, 1993 Ron Mayer
 * Copyright (c) 1998 Michael Cheng
 * Copyright (c) 1997 Jeff Tsay
 * Copyright (c) 1995-1997 Michael Hipp
 * Copyright (c) 1993-1994 Tobias Bading,
 *                         Berlin University of Technology
 *
 * - This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * - This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * - You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package com.tianscar.media.sound;

import org.tritonus.share.sampled.convert.TMatrixFormatConversionProvider;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.util.Arrays;

public class Mp3FormatConversionProvider extends TMatrixFormatConversionProvider {
    
    private static final AudioFormat[] INPUT_FORMATS = new AudioFormat[18 * 9];
    static {
        AudioFormat.Encoding[] encodings = new AudioFormat.Encoding[] {
                Mp3Encoding.MPEG2_L1, Mp3Encoding.MPEG2_L2, Mp3Encoding.MPEG2_L3,
                Mp3Encoding.MPEG1_L1, Mp3Encoding.MPEG1_L2, Mp3Encoding.MPEG1_L3,
                Mp3Encoding.MPEG2DOT5_L1, Mp3Encoding.MPEG2DOT5_L2, Mp3Encoding.MPEG2DOT5_L3
        };
        for (int i = 0; i < 9; i ++) {
            AudioFormat.Encoding encoding = encodings[i];
            INPUT_FORMATS[18 * i] = 
                    new AudioFormat(encoding, 32000.0f, -1, 1, -1, 32000.0f, false); // 0
            INPUT_FORMATS[18 * i + 1] =
                    new AudioFormat(encoding, 32000.0f, -1, 2, -1, 32000.0f, false); // 1
            INPUT_FORMATS[18 * i + 2] =
                    new AudioFormat(encoding, 44100.0f, -1, 1, -1, 44100.0f, false); // 2
            INPUT_FORMATS[18 * i + 3] =
                    new AudioFormat(encoding, 44100.0f, -1, 2, -1, 44100.0f, false); // 3
            INPUT_FORMATS[18 * i + 4] =
                    new AudioFormat(encoding, 48000.0f, -1, 1, -1, 48000.0f, false); // 4
            INPUT_FORMATS[18 * i + 5] =
                    new AudioFormat(encoding, 48000.0f, -1, 2, -1, 48000.0f, false); // 5
            INPUT_FORMATS[18 * i + 6] =
                    new AudioFormat(encoding, 16000.0f, -1, 1, -1, 16000.0f, false); // 18
            INPUT_FORMATS[18 * i + 7] =
                    new AudioFormat(encoding, 16000.0f, -1, 2, -1, 16000.0f, false); // 19
            INPUT_FORMATS[18 * i + 8] =
                    new AudioFormat(encoding, 22050.0f, -1, 1, -1, 22050.0f, false); // 20
            INPUT_FORMATS[18 * i + 9] =
                    new AudioFormat(encoding, 22050.0f, -1, 2, -1, 22050.0f, false); // 21
            INPUT_FORMATS[18 * i + 10] =
                    new AudioFormat(encoding, 24000.0f, -1, 1, -1, 24000.0f, false); // 22
            INPUT_FORMATS[18 * i + 11] =
                    new AudioFormat(encoding, 24000.0f, -1, 2, -1, 24000.0f, false); // 23
            INPUT_FORMATS[18 * i + 12] =
                    new AudioFormat(encoding, 8000.0f, -1, 1, -1, 8000.0f, false); // 36
            INPUT_FORMATS[18 * i + 13] =
                    new AudioFormat(encoding, 8000.0f, -1, 2, -1, 8000.0f, false); // 37
            INPUT_FORMATS[18 * i + 14] =
                    new AudioFormat(encoding, 11025.0f, -1, 1, -1, 11025.0f, false); // 38
            INPUT_FORMATS[18 * i + 15] =
                    new AudioFormat(encoding, 11025.0f, -1, 2, -1, 11025.0f, false); // 39
            INPUT_FORMATS[18 * i + 16] =
                    new AudioFormat(encoding, 12000.0f, -1, 1, -1, 12000.0f, false); // 40
            INPUT_FORMATS[18 * i + 17] =
                    new AudioFormat(encoding, 12000.0f, -1, 2, -1, 12000.0f, false); // 41
        }
    }

    private static final AudioFormat[] OUTPUT_FORMATS = new AudioFormat[] {
            new AudioFormat(8000.0f, 16, 1, true, false),
            new AudioFormat(8000.0f, 16, 1, true, true),
            new AudioFormat(8000.0f, 16, 2, true, false),
            new AudioFormat(8000.0f, 16, 2, true, true),
            new AudioFormat(8000.0f, 24, 1, false, false),
            new AudioFormat(8000.0f, 24, 1, false, true),
            new AudioFormat(8000.0f, 24, 2, false, false),
            new AudioFormat(8000.0f, 24, 2, false, true),
            new AudioFormat(8000.0f, 8, 1, false, false),
            new AudioFormat(8000.0f, 8, 2, false, false),
            new AudioFormat(11025.0f, 16, 1, true, false),
            new AudioFormat(11025.0f, 16, 1, true, true),
            new AudioFormat(11025.0f, 16, 2, true, false),
            new AudioFormat(11025.0f, 16, 2, true, true),
            new AudioFormat(11025.0f, 24, 1, false, false),
            new AudioFormat(11025.0f, 24, 1, false, true),
            new AudioFormat(11025.0f, 24, 2, false, false),
            new AudioFormat(11025.0f, 24, 2, false, true),
            new AudioFormat(11025.0f, 8, 1, false, false),
            new AudioFormat(11025.0f, 8, 2, false, false),
            new AudioFormat(12000.0f, 16, 1, true, false),
            new AudioFormat(12000.0f, 16, 1, true, true),
            new AudioFormat(12000.0f, 16, 2, true, false),
            new AudioFormat(12000.0f, 16, 2, true, true),
            new AudioFormat(12000.0f, 24, 1, false, false),
            new AudioFormat(12000.0f, 24, 1, false, true),
            new AudioFormat(12000.0f, 24, 2, false, false),
            new AudioFormat(12000.0f, 24, 2, false, true),
            new AudioFormat(12000.0f, 8, 1, false, false),
            new AudioFormat(12000.0f, 8, 2, false, false),
            new AudioFormat(16000.0f, 16, 1, true, false),
            new AudioFormat(16000.0f, 16, 1, true, true),
            new AudioFormat(16000.0f, 16, 2, true, false),
            new AudioFormat(16000.0f, 16, 2, true, true),
            new AudioFormat(16000.0f, 24, 1, false, false),
            new AudioFormat(16000.0f, 24, 1, false, true),
            new AudioFormat(16000.0f, 24, 2, false, false),
            new AudioFormat(16000.0f, 24, 2, false, true),
            new AudioFormat(16000.0f, 8, 1, false, false),
            new AudioFormat(16000.0f, 8, 2, false, false),
            new AudioFormat(22050.0f, 16, 1, true, false),
            new AudioFormat(22050.0f, 16, 1, true, true),
            new AudioFormat(22050.0f, 16, 2, true, false),
            new AudioFormat(22050.0f, 16, 2, true, true),
            new AudioFormat(22050.0f, 24, 1, false, false),
            new AudioFormat(22050.0f, 24, 1, false, true),
            new AudioFormat(22050.0f, 24, 2, false, false),
            new AudioFormat(22050.0f, 24, 2, false, true),
            new AudioFormat(22050.0f, 8, 1, false, false),
            new AudioFormat(22050.0f, 8, 2, false, false),
            new AudioFormat(24000.0f, 16, 1, true, false),
            new AudioFormat(24000.0f, 16, 1, true, true),
            new AudioFormat(24000.0f, 16, 2, true, false),
            new AudioFormat(24000.0f, 16, 2, true, true),
            new AudioFormat(24000.0f, 24, 1, false, false),
            new AudioFormat(24000.0f, 24, 1, false, true),
            new AudioFormat(24000.0f, 24, 2, false, false),
            new AudioFormat(24000.0f, 24, 2, false, true),
            new AudioFormat(24000.0f, 8, 1, false, false),
            new AudioFormat(24000.0f, 8, 2, false, false),
            new AudioFormat(32000.0f, 16, 1, true, false),
            new AudioFormat(32000.0f, 16, 1, true, true),
            new AudioFormat(32000.0f, 16, 2, true, false),
            new AudioFormat(32000.0f, 16, 2, true, true),
            new AudioFormat(32000.0f, 24, 1, false, false),
            new AudioFormat(32000.0f, 24, 1, false, true),
            new AudioFormat(32000.0f, 24, 2, false, false),
            new AudioFormat(32000.0f, 24, 2, false, true),
            new AudioFormat(32000.0f, 8, 1, false, false),
            new AudioFormat(32000.0f, 8, 2, false, false),
            new AudioFormat(44100.0f, 16, 1, true, false),
            new AudioFormat(44100.0f, 16, 1, true, true),
            new AudioFormat(44100.0f, 16, 2, true, false),
            new AudioFormat(44100.0f, 16, 2, true, true),
            new AudioFormat(44100.0f, 24, 1, false, false),
            new AudioFormat(44100.0f, 24, 1, false, true),
            new AudioFormat(44100.0f, 24, 2, false, false),
            new AudioFormat(44100.0f, 24, 2, false, true),
            new AudioFormat(44100.0f, 8, 1, false, false),
            new AudioFormat(44100.0f, 8, 2, false, false),
            new AudioFormat(48000.0f, 16, 1, true, false),
            new AudioFormat(48000.0f, 16, 1, true, true),
            new AudioFormat(48000.0f, 16, 2, true, false),
            new AudioFormat(48000.0f, 16, 2, true, true),
            new AudioFormat(48000.0f, 24, 1, false, false),
            new AudioFormat(48000.0f, 24, 1, false, true),
            new AudioFormat(48000.0f, 24, 2, false, false),
            new AudioFormat(48000.0f, 24, 2, false, true),
            new AudioFormat(48000.0f, 8, 1, false, false),
            new AudioFormat(48000.0f, 8, 2, false, false),
    };

    private static final boolean t = true;
    private static final boolean f = false;

    /*
     *	One row for each source format.
     */
    private static final boolean[][] CONVERSIONS = new boolean[18 * 9][36];
    static {
        for (int i = 0; i < 9; i ++) {
            CONVERSIONS[18 * i] =
                    new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f,
                            f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, t,t,f,f,t,t,f,f,t,f, f,f,f,f,f,f,f,f,f,f
                            , f,f,f,f,f,f,f,f,f,f};	// 0
            CONVERSIONS[18 * i + 1] =
                    new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f,
                            f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f,t,t,f,t, f,f,f,f,f,f,f,f,f,f
                            , f,f,f,f,f,f,f,f,f,f};	// 1
            CONVERSIONS[18 * i + 2] =
                    new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f,
                            f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, t,t,f,f,t,t,f,f,t,f
                            , f,f,f,f,f,f,f,f,f,f};	// 2
            CONVERSIONS[18 * i + 3] =
                    new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f,
                            f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f,t,t,f,t
                            , f,f,f,f,f,f,f,f,f,f};	// 3
            CONVERSIONS[18 * i + 4] =
                    new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f,
                            f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f
                            , t,t,f,f,t,t,f,f,t,f};	// 4
            CONVERSIONS[18 * i + 5] =
                    new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f,
                            f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f
                            , f,f,t,t,f,f,t,t,f,t};	// 5
            CONVERSIONS[18 * i + 6] =
                    new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, t,t,f,f,t,t,f,f,t,f,
                            f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f
                            , f,f,f,f,f,f,f,f,f,f};	// 18
            CONVERSIONS[18 * i + 7] =
                    new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f,t,t,f,t,
                            f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f
                            , f,f,f,f,f,f,f,f,f,f};	// 19
            CONVERSIONS[18 * i + 8] =
                    new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f,
                            t,t,f,f,t,t,f,f,t,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f
                            , f,f,f,f,f,f,f,f,f,f};	// 20
            CONVERSIONS[18 * i + 9] =
                    new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f,
                            f,f,t,t,f,f,t,t,f,t, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f
                            , f,f,f,f,f,f,f,f,f,f};	// 21
            CONVERSIONS[18 * i + 10] =
                    new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f,
                            f,f,f,f,f,f,f,f,f,f, t,t,f,f,t,t,f,f,t,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f
                            , f,f,f,f,f,f,f,f,f,f};	// 22
            CONVERSIONS[18 * i + 11] =
                    new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f,
                            f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f,t,t,f,t, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f
                            , f,f,f,f,f,f,f,f,f,f};	// 23
            CONVERSIONS[18 * i + 12] =
                    new boolean[] {t,t,f,f,t,t,f,f,t,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f,
                            f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f
                            , f,f,f,f,f,f,f,f,f,f};	// 36
            CONVERSIONS[18 * i + 13] =
                    new boolean[] {f,f,t,t,f,f,t,t,f,t, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f,
                            f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f
                            , f,f,f,f,f,f,f,f,f,f};	// 37
            CONVERSIONS[18 * i + 14] =
                    new boolean[] {f,f,f,f,f,f,f,f,f,f, t,t,f,f,t,t,f,f,t,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f,
                            f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f
                            , f,f,f,f,f,f,f,f,f,f};	// 38
            CONVERSIONS[18 * i + 15] =
                    new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f,t,t,f,t, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f,
                            f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f
                            , f,f,f,f,f,f,f,f,f,f};	// 39
            CONVERSIONS[18 * i + 16] =
                    new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, t,t,f,f,t,t,f,f,t,f, f,f,f,f,f,f,f,f,f,f,
                            f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f
                            , f,f,f,f,f,f,f,f,f,f};	// 40
            CONVERSIONS[18 * i + 17] =
                    new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f,t,t,f,t, f,f,f,f,f,f,f,f,f,f,
                            f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f
                            , f,f,f,f,f,f,f,f,f,f};	// 41
        }
    }

    public Mp3FormatConversionProvider() {
        super(Arrays.asList(INPUT_FORMATS), Arrays.asList(OUTPUT_FORMATS), CONVERSIONS);
    }

    @Override
    public AudioInputStream getAudioInputStream(AudioFormat targetFormat, AudioInputStream audioInputStream) {
        if (audioInputStream instanceof Mp3AudioInputStream && isConversionSupported(targetFormat, audioInputStream.getFormat()))
            return new DecodedMp3AudioInputStream(targetFormat, (Mp3AudioInputStream) audioInputStream);
        else throw new IllegalArgumentException("conversion not supported");
    }

}
