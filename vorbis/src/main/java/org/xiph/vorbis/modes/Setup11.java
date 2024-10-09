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

import org.xiph.vorbis.EncSetupDataTemplate;

/**
 * 11kHz settings
 */

public class Setup11 {

    private static final int blocksize_11[] = {// [2]
            512, 512
    };

    private static final int _floor_mapping_11a[] = {
            6, 6
    };

    private static final int _floor_mapping_11[][] = {
            _floor_mapping_11a
    };

    private static final double rate_mapping_11[] = {// [3]
            8000., 13000., 44000.,
    };

    private static final double rate_mapping_11_uncoupled[] = {// [3]
            12000., 20000., 50000.,
    };

    private static final double quality_mapping_11[] = {// [3]
            -.1, .0, 1.
    };

    public static final EncSetupDataTemplate setup_11_stereo = new EncSetupDataTemplate(
            2,
            rate_mapping_11,
            quality_mapping_11,
            2,
            9000,
            15000,

            blocksize_11,
            blocksize_11,

            Psych11._psy_tone_masteratt_11,
            Psych44._psy_tone_0dB,
            Psych44._psy_tone_suppress,

            Psych11._tonemask_adj_11,
            null,
            Psych11._tonemask_adj_11,

            Psych8._psy_noiseguards_8,
            Psych11._psy_noisebias_11,
            Psych11._psy_noisebias_11,
            null,
            null,
            Psych44._psy_noise_suppress,

            Psych8._psy_compand_8,
            Setup8._psy_compand_8_mapping,
            null,

            new int[][]{Psych8._noise_start_8, Psych8._noise_start_8},
            new int[][]{Psych8._noise_part_8, Psych8._noise_part_8},
            Psych11._noise_thresh_11,

            Psych8._psy_ath_floater_8,
            Psych8._psy_ath_abs_8,

            Psych11._psy_lowpass_11,

            Psych44._psy_global_44,
            Setup8._global_mapping_8,
            Psych8._psy_stereo_modes_8,

            FloorAll._floor_books,
            FloorAll._floor,
            1,
            _floor_mapping_11,

            Residue8._mapres_template_8_stereo
    );

    public static final EncSetupDataTemplate setup_11_uncoupled = new EncSetupDataTemplate(
            2,
            rate_mapping_11_uncoupled,
            quality_mapping_11,
            -1,
            9000,
            15000,

            blocksize_11,
            blocksize_11,

            Psych11._psy_tone_masteratt_11,
            Psych44._psy_tone_0dB,
            Psych44._psy_tone_suppress,

            Psych11._tonemask_adj_11,
            null,
            Psych11._tonemask_adj_11,

            Psych8._psy_noiseguards_8,
            Psych11._psy_noisebias_11,
            Psych11._psy_noisebias_11,
            null,
            null,
            Psych44._psy_noise_suppress,

            Psych8._psy_compand_8,
            Setup8._psy_compand_8_mapping,
            null,

            new int[][]{Psych8._noise_start_8, Psych8._noise_start_8},
            new int[][]{Psych8._noise_part_8, Psych8._noise_part_8},
            Psych11._noise_thresh_11,

            Psych8._psy_ath_floater_8,
            Psych8._psy_ath_abs_8,

            Psych11._psy_lowpass_11,

            Psych44._psy_global_44,
            Setup8._global_mapping_8,
            Psych8._psy_stereo_modes_8,

            FloorAll._floor_books,
            FloorAll._floor,
            1,
            _floor_mapping_11,

            Residue8._mapres_template_8_uncoupled
    );
}
