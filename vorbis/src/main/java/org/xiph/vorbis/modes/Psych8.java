/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 2019 Alexey Kuznetsov
 * Copyright (c) 2002-2018 Xiph.Org Foundation
 * Copyright (c) 1994-1996 James Gosling,
 *                         Kevin A. Smith, Sun Microsystems, Inc.
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

package org.xiph.vorbis.modes;

import org.xiph.vorbis.AdjBlock;
import org.xiph.vorbis.AdjStereo;
import org.xiph.vorbis.Att3;
import org.xiph.vorbis.CompandBlock;
import org.xiph.vorbis.Noise3;
import org.xiph.vorbis.NoiseGuard;

/**
 * 8kHz psychoacoustic settings
 */

public class Psych8 {

    protected static final Att3 _psy_tone_masteratt_8[] = {// [3]
            new Att3(new int[]{32, 25, 12}, 0, 0),  /* 0 */
            new Att3(new int[]{30, 25, 12}, 0, 0),  /* 0 */
            new Att3(new int[]{20, 0, -14}, 0, 0), /* 0 */
    };

    protected static final AdjBlock _tonemask_adj_8[] = {// [3]
            /* adjust for mode zero */
            /* 63     125     250     500     1     2     4     8    16 */
            new AdjBlock(new int[]{-15, -15, -15, -15, -10, -10, -6, 0, 0, 0, 0, 10, 0, 0, 99, 99, 99}), /* 1 */
            new AdjBlock(new int[]{-15, -15, -15, -15, -10, -10, -6, 0, 0, 0, 0, 10, 0, 0, 99, 99, 99}), /* 1 */
            new AdjBlock(new int[]{-15, -15, -15, -15, -10, -10, -6, 0, 0, 0, 0, 0, 0, 0, 99, 99, 99}), /* 1 */
    };

    protected static final Noise3 _psy_noisebias_8[] = {// [3]
            /*  63     125     250     500      1k       2k      4k      8k     16k*/
            new Noise3(
                    new int[][]{{-10, -10, -10, -10, -5, -5, -5, 0, 4, 8, 8, 8, 10, 10, 99, 99, 99},
                            {-10, -10, -10, -10, -5, -5, -5, 0, 0, 4, 4, 4, 4, 4, 99, 99, 99},
                            {-30, -30, -30, -30, -30, -24, -20, -14, -10, -6, -8, -8, -6, -6, 99, 99, 99}}),

            new Noise3(
                    new int[][]{{-10, -10, -10, -10, -5, -5, -5, 0, 4, 8, 8, 8, 10, 10, 99, 99, 99},
                            {-10, -10, -10, -10, -10, -10, -5, -5, -5, 0, 0, 0, 0, 0, 99, 99, 99},
                            {-30, -30, -30, -30, -30, -24, -20, -14, -10, -6, -8, -8, -6, -6, 99, 99, 99}}),

            new Noise3(
                    new int[][]{{-15, -15, -15, -15, -15, -12, -10, -8, 0, 2, 4, 4, 5, 5, 99, 99, 99},
                            {-30, -30, -30, -30, -26, -22, -20, -14, -12, -12, -10, -10, -10, -10, 99, 99, 99},
                            {-30, -30, -30, -30, -26, -26, -26, -26, -26, -26, -26, -26, -26, -24, 99, 99, 99}}),
    };

    /**
     * stereo mode by base quality level
     */
    protected static final AdjStereo _psy_stereo_modes_8[] = {// [3]
            /*  0   1   2   3   4   5   6   7   8   9  10  11  12  13  14  */
            new AdjStereo(
                    new int[]{4, 4, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3},
                    new int[]{6, 5, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4},
                    new float[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                    new float[]{99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99}),
            new AdjStereo(
                    new int[]{4, 4, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3},
                    new int[]{6, 5, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4},
                    new float[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                    new float[]{99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99}),
            new AdjStereo(
                    new int[]{3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3},
                    new int[]{4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4},
                    new float[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                    new float[]{99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99}),
    };

    protected static final NoiseGuard _psy_noiseguards_8[] = {// [2]
            new NoiseGuard(10, 10, -1),
            new NoiseGuard(10, 10, -1),
    };

    protected static final CompandBlock _psy_compand_8[] = {// [2]
            new CompandBlock(new int[]{
                    0, 1, 2, 3, 4, 5, 6, 7,     /* 7dB */
                    8, 8, 9, 9, 10, 10, 11, 11,     /* 15dB */
                    12, 12, 13, 13, 14, 14, 15, 15,     /* 23dB */
                    16, 16, 17, 17, 17, 18, 18, 19,     /* 31dB */
                    19, 19, 20, 21, 22, 23, 24, 25,     /* 39dB */
            }),
            new CompandBlock(new int[]{
                    0, 1, 2, 3, 4, 5, 6, 6,     /* 7dB */
                    7, 7, 6, 6, 5, 5, 4, 4,     /* 15dB */
                    3, 3, 3, 4, 5, 6, 7, 8,     /* 23dB */
                    9, 10, 11, 12, 13, 14, 15, 16,     /* 31dB */
                    17, 18, 19, 20, 21, 22, 23, 24,     /* 39dB */
            }),
    };

    protected static final double _psy_lowpass_8[] = {3., 4., 4.};// [3]

    protected static final int _noise_start_8[] = {// [2]
            64, 64,
    };

    protected static final int _noise_part_8[] = {// [2]
            8, 8,
    };

    protected static final int _psy_ath_floater_8[] = {// [3]
            -100, -100, -105,
    };

    protected static final int _psy_ath_abs_8[] = {// [3]
            -130, -130, -140,
    };
}
