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

import org.xiph.vorbis.EncSetupDataTemplate;

/**
 * toplevel settings for 44.1/48kHz
 */

public class Setup44 {

    private static final double rate_mapping_44_stereo[] = {// [12]
            22500., 32000., 40000., 48000., 56000., 64000.,
            80000., 96000., 112000., 128000., 160000., 250001.
    };

    protected static final double quality_mapping_44[] = {// [12]
            -.1, .0, .1, .2, .3, .4, .5, .6, .7, .8, .9, 1.0
    };

    protected static final int blocksize_short_44[] = {// [11]
            512, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256
    };

    protected static final int blocksize_long_44[] = {// [11]
            4096, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048
    };

    protected static final double _psy_compand_short_mapping[] = {// [12]
            0.5, 1., 1., 1.3, 1.6, 2., 2., 2., 2., 2., 2., 2.
    };

    protected static final double _psy_compand_long_mapping[] = {// [12]
            3.5, 4., 4., 4.3, 4.6, 5., 5., 5., 5., 5., 5., 5.
    };

    protected static final double _global_mapping_44[] = {// [12]
            /* 1., 1., 1.5, 2., 2., 2.5, 2.7, 3.0, 3.5, 4., 4. */
            0., 1., 1., 1.5, 2., 2., 2.5, 2.7, 3.0, 3.7, 4., 4.
    };

    private static final int _floor_mapping_44a[] = {// [11]
            1, 0, 0, 2, 2, 4, 5, 5, 5, 5, 5
    };

    private static final int _floor_mapping_44b[] = {// [11]
            8, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7
    };

    private static final int _floor_mapping_44c[] = {// [11]
            10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10
    };

    protected static final int _floor_mapping_44[][] = {
            _floor_mapping_44a,
            _floor_mapping_44b,
            _floor_mapping_44c,
    };

    public static final EncSetupDataTemplate setup_44_stereo = new EncSetupDataTemplate(
            11,
            rate_mapping_44_stereo,
            quality_mapping_44,
            2,
            40000,
            50000,

            blocksize_short_44,
            blocksize_long_44,

            Psych44._psy_tone_masteratt_44,
            Psych44._psy_tone_0dB,
            Psych44._psy_tone_suppress,

            Psych44._tonemask_adj_otherblock,
            Psych44._tonemask_adj_longblock,
            Psych44._tonemask_adj_otherblock,

            Psych44._psy_noiseguards_44,
            Psych44._psy_noisebias_impulse,
            Psych44._psy_noisebias_padding,
            Psych44._psy_noisebias_trans,
            Psych44._psy_noisebias_long,
            Psych44._psy_noise_suppress,

            Psych44._psy_compand_44,
            _psy_compand_short_mapping,
            _psy_compand_long_mapping,

            new int[][]{Psych44._noise_start_short_44, Psych44._noise_start_long_44},
            new int[][]{Psych44._noise_part_short_44, Psych44._noise_part_long_44},
            Psych44._noise_thresh_44,

            Psych44._psy_ath_floater,
            Psych44._psy_ath_abs,

            Psych44._psy_lowpass_44,

            Psych44._psy_global_44,
            _global_mapping_44,
            Psych44._psy_stereo_modes_44,

            FloorAll._floor_books,
            FloorAll._floor,
            2,
            _floor_mapping_44,

            Residue44._mapres_template_44_stereo
    );
}
