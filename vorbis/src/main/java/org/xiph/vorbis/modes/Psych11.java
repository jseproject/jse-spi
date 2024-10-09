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
import org.xiph.vorbis.Att3;
import org.xiph.vorbis.Noise3;

/**
 * 11kHz settings
 */

public class Psych11 {

    protected static final double _psy_lowpass_11[] = {4.5, 5.5, 30.,};// [3]

    protected static final Att3 _psy_tone_masteratt_11[] = {// [3]
            new Att3(new int[]{30, 25, 12}, 0, 0),  /* 0 */
            new Att3(new int[]{30, 25, 12}, 0, 0),  /* 0 */
            new Att3(new int[]{20, 0, -14}, 0, 0),  /* 0 */
    };

    protected static final AdjBlock _tonemask_adj_11[] = {// [3]
            /* adjust for mode zero */
            /* 63     125     250     500     1     2     4     8    16 */
            new AdjBlock(new int[]{-20, -20, -20, -20, -20, -16, -10, 0, 0, 0, 0, 10, 2, 0, 99, 99, 99}), /* 0 */
            new AdjBlock(new int[]{-20, -20, -20, -20, -20, -16, -10, 0, 0, 0, 0, 5, 0, 0, 99, 99, 99}), /* 1 */
            new AdjBlock(new int[]{-20, -20, -20, -20, -20, -16, -10, 0, 0, 0, 0, 0, 0, 0, 99, 99, 99}), /* 2 */
    };

    protected static final Noise3 _psy_noisebias_11[] = {// [3]
            /*  63     125     250     500      1k       2k      4k      8k     16k*/
            new Noise3(
                    new int[][]{{-10, -10, -10, -10, -5, -5, -5, 0, 4, 10, 10, 12, 12, 12, 99, 99, 99},
                            {-15, -15, -15, -15, -10, -10, -5, 0, 0, 4, 4, 5, 5, 10, 99, 99, 99},
                            {-30, -30, -30, -30, -30, -24, -20, -14, -10, -6, -8, -8, -6, -6, 99, 99, 99}}),

            new Noise3(
                    new int[][]{{-10, -10, -10, -10, -5, -5, -5, 0, 4, 10, 10, 12, 12, 12, 99, 99, 99},
                            {-15, -15, -15, -15, -10, -10, -5, -5, -5, 0, 0, 0, 0, 0, 99, 99, 99},
                            {-30, -30, -30, -30, -30, -24, -20, -14, -10, -6, -8, -8, -6, -6, 99, 99, 99}}),

            new Noise3(
                    new int[][]{{-15, -15, -15, -15, -15, -12, -10, -8, 0, 2, 4, 4, 5, 5, 99, 99, 99},
                            {-30, -30, -30, -30, -26, -22, -20, -14, -12, -12, -10, -10, -10, -10, 99, 99, 99},
                            {-30, -30, -30, -30, -26, -26, -26, -26, -26, -26, -26, -26, -26, -24, 99, 99, 99}}),
    };

    protected static final double _noise_thresh_11[] = {.3, .5, .5};// [3]
}
