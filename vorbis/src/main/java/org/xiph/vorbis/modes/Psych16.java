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

package org.xiph.vorbis.modes;

import org.xiph.vorbis.AdjBlock;
import org.xiph.vorbis.AdjStereo;
import org.xiph.vorbis.Att3;
import org.xiph.vorbis.Noise3;
import org.xiph.vorbis.NoiseGuard;

/**
 * 16kHz settings
 */

public class Psych16 {
    /**
     * stereo mode by base quality level
     */
    protected static final AdjStereo _psy_stereo_modes_16[] = {// [4]
            /*  0   1   2   3   4   5   6   7   8   9  10  11  12  13  14  */
            new AdjStereo(
                    new int[]{4, 4, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3},
                    new int[]{6, 5, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4},
                    new float[]{2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 4, 4},
                    new float[]{99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99}),
            new AdjStereo(
                    new int[]{4, 4, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3},
                    new int[]{6, 5, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4},
                    new float[]{2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 4, 4, 4, 4, 4},
                    new float[]{99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99}),
            new AdjStereo(
                    new int[]{3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3},
                    new int[]{5, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3},
                    new float[]{4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4},
                    new float[]{99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99}),
            new AdjStereo(
                    new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                    new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                    new float[]{8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8},
                    new float[]{99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99}),
    };

    protected static final double _psy_lowpass_16[] = {6.5, 8, 30., 99.};// [4]

    protected static final Att3 _psy_tone_masteratt_16[] = {// [4]
            new Att3(new int[]{30, 25, 12}, 0, 0),  /* 0 */
            new Att3(new int[]{25, 22, 12}, 0, 0),  /* 0 */
            new Att3(new int[]{20, 12, 0}, 0, 0),  /* 0 */
            new Att3(new int[]{15, 0, -14}, 0, 0), /* 0 */
    };

    protected static final AdjBlock _tonemask_adj_16[] = {// [4]
            /* adjust for mode zero */
            /* 63     125     250     500       1     2     4     8    16 */
            new AdjBlock(new int[]{-20, -20, -20, -20, -20, -16, -10, 0, 0, 0, 0, 10, 0, 0, 0, 0, 0}), /* 0 */
            new AdjBlock(new int[]{-20, -20, -20, -20, -20, -16, -10, 0, 0, 0, 0, 10, 0, 0, 0, 0, 0}), /* 1 */
            new AdjBlock(new int[]{-20, -20, -20, -20, -20, -16, -10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}), /* 2 */
            new AdjBlock(new int[]{-30, -30, -30, -30, -30, -26, -20, -10, -5, 0, 0, 0, 0, 0, 0, 0, 0}), /* 2 */
    };

    protected static final Noise3 _psy_noisebias_16_short[] = {// [4]
            /*  63     125     250     500      1k       2k      4k      8k     16k*/
            new Noise3(
                    new int[][]{{-15, -15, -15, -15, -15, -10, -10, -5, 4, 10, 10, 10, 10, 12, 12, 14, 20},
                            {-15, -15, -15, -15, -15, -10, -10, -5, 0, 0, 4, 5, 5, 6, 8, 8, 15},
                            {-30, -30, -30, -30, -30, -24, -20, -14, -10, -6, -8, -8, -6, -6, -6, -6, -6}}),

            new Noise3(
                    new int[][]{{-15, -15, -15, -15, -15, -10, -10, -5, 4, 6, 6, 6, 6, 8, 10, 12, 20},
                            {-15, -15, -15, -15, -15, -15, -15, -10, -5, -5, -5, 4, 5, 6, 8, 8, 15},
                            {-30, -30, -30, -30, -30, -24, -20, -14, -10, -10, -10, -10, -10, -10, -10, -10, -10}}),

            new Noise3(
                    new int[][]{{-15, -15, -15, -15, -15, -12, -10, -8, 0, 2, 4, 4, 5, 5, 5, 8, 12},
                            {-20, -20, -20, -20, -16, -12, -20, -14, -10, -10, -8, 0, 0, 0, 0, 2, 5},
                            {-30, -30, -30, -30, -26, -26, -26, -26, -26, -26, -26, -26, -26, -24, -20, -20, -20}}),

            new Noise3(
                    new int[][]{{-15, -15, -15, -15, -15, -12, -10, -8, -5, -5, -5, -5, -5, 0, 0, 0, 6},
                            {-30, -30, -30, -30, -26, -22, -20, -14, -12, -12, -10, -10, -10, -10, -10, -10, -6},
                            {-30, -30, -30, -30, -26, -26, -26, -26, -26, -26, -26, -26, -26, -24, -20, -20, -20}}),
    };

    protected static final Noise3 _psy_noisebias_16_impulse[] = {// [4]
            /*  63     125     250     500      1k       2k      4k      8k     16k*/
            new Noise3(
                    new int[][]{{-15, -15, -15, -15, -15, -10, -10, -5, 4, 10, 10, 10, 10, 12, 12, 14, 20},
                            {-15, -15, -15, -15, -15, -10, -10, -5, 0, 0, 4, 5, 5, 6, 8, 8, 15},
                            {-30, -30, -30, -30, -30, -24, -20, -14, -10, -6, -8, -8, -6, -6, -6, -6, -6}}),

            new Noise3(
                    new int[][]{{-15, -15, -15, -15, -15, -10, -10, -5, 4, 4, 4, 4, 5, 5, 6, 8, 15},
                            {-15, -15, -15, -15, -15, -15, -15, -10, -5, -5, -5, 0, 0, 0, 0, 4, 10},
                            {-30, -30, -30, -30, -30, -24, -20, -14, -10, -10, -10, -10, -10, -10, -10, -10, -10}}),

            new Noise3(
                    new int[][]{{-15, -15, -15, -15, -15, -12, -10, -8, 0, 0, 0, 0, 0, 0, 0, 4, 10},
                            {-20, -20, -20, -20, -16, -12, -20, -14, -10, -10, -10, -10, -10, -10, -10, -7, -5},
                            {-30, -30, -30, -30, -26, -26, -26, -26, -26, -26, -26, -26, -26, -24, -20, -20, -20}}),

            new Noise3(
                    new int[][]{{-15, -15, -15, -15, -15, -12, -10, -8, -5, -5, -5, -5, -5, 0, 0, 0, 6},
                            {-30, -30, -30, -30, -26, -22, -20, -18, -18, -18, -20, -20, -20, -20, -20, -20, -16},
                            {-30, -30, -30, -30, -26, -26, -26, -26, -26, -26, -26, -26, -26, -24, -20, -20, -20}}),
    };

    protected static final Noise3 _psy_noisebias_16[] = {// [4]
            /*  63     125     250     500      1k       2k      4k      8k     16k*/
            new Noise3(
                    new int[][]{{-10, -10, -10, -10, -5, -5, -5, 0, 4, 6, 8, 8, 10, 10, 10, 14, 20},
                            {-10, -10, -10, -10, -10, -5, -2, -2, 0, 0, 0, 4, 5, 6, 8, 8, 15},
                            {-30, -30, -30, -30, -30, -24, -20, -14, -10, -6, -8, -8, -6, -6, -6, -6, -6}}),

            new Noise3(
                    new int[][]{{-10, -10, -10, -10, -5, -5, -5, 0, 4, 6, 6, 6, 6, 8, 10, 12, 20},
                            {-15, -15, -15, -15, -15, -10, -5, -5, 0, 0, 0, 4, 5, 6, 8, 8, 15},
                            {-30, -30, -30, -30, -30, -24, -20, -14, -10, -6, -8, -8, -6, -6, -6, -6, -6}}),

            new Noise3(
                    new int[][]{{-15, -15, -15, -15, -15, -12, -10, -8, 0, 2, 4, 4, 5, 5, 5, 8, 12},
                            {-20, -20, -20, -20, -16, -12, -20, -10, -5, -5, 0, 0, 0, 0, 0, 2, 5},
                            {-30, -30, -30, -30, -26, -26, -26, -26, -26, -26, -26, -26, -26, -24, -20, -20, -20}}),

            new Noise3(
                    new int[][]{{-15, -15, -15, -15, -15, -12, -10, -8, -5, -5, -5, -5, -5, 0, 0, 0, 6},
                            {-30, -30, -30, -30, -26, -22, -20, -14, -12, -12, -10, -10, -10, -10, -10, -10, -6},
                            {-30, -30, -30, -30, -26, -26, -26, -26, -26, -26, -26, -26, -26, -24, -20, -20, -20}}),
    };

    protected static final NoiseGuard _psy_noiseguards_16[] = {// [4]
            new NoiseGuard(10, 10, -1),
            new NoiseGuard(10, 10, -1),
            new NoiseGuard(20, 20, -1),
            new NoiseGuard(20, 20, -1),
    };

    protected static final double _noise_thresh_16[] = {.3, .5, .5, .5};// [4]

    protected static final int _noise_start_16[] = {256, 256, 9999};// [3]

    protected static final int _noise_part_16[] = {8, 8, 8, 8};// [4]

    protected static final int _psy_ath_floater_16[] = {// [4]
            -100, -100, -100, -105,
    };

    protected static final int _psy_ath_abs_16[] = {// [4]
            -130, -130, -130, -140,
    };
}
