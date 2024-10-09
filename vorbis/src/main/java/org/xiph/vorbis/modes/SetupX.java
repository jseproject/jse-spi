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
 * catch-all toplevel settings for q modes only
 */

public class SetupX {

    private static final double rate_mapping_X[] = {// [12]
            -1., -1., -1., -1., -1., -1.,
            -1., -1., -1., -1., -1., -1.
    };

    public static final EncSetupDataTemplate setup_X_stereo = new EncSetupDataTemplate(
            11,
            rate_mapping_X,
            Setup44.quality_mapping_44,
            2,
            50000,
            200000,

            Setup44.blocksize_short_44,
            Setup44.blocksize_long_44,

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
            Setup44._psy_compand_short_mapping,
            Setup44._psy_compand_long_mapping,

            new int[][]{Psych44._noise_start_short_44, Psych44._noise_start_long_44},
            new int[][]{Psych44._noise_part_short_44, Psych44._noise_part_long_44},
            Psych44._noise_thresh_44,

            Psych44._psy_ath_floater,
            Psych44._psy_ath_abs,

            Psych44._psy_lowpass_44,

            Psych44._psy_global_44,
            Setup44._global_mapping_44,
            Psych44._psy_stereo_modes_44,

            FloorAll._floor_books,
            FloorAll._floor,
            2,
            Setup44._floor_mapping_44,

            Residue44._mapres_template_44_stereo
    );

    public static final EncSetupDataTemplate setup_X_uncoupled = new EncSetupDataTemplate(
            11,
            rate_mapping_X,
            Setup44.quality_mapping_44,
            -1,
            50000,
            200000,

            Setup44.blocksize_short_44,
            Setup44.blocksize_long_44,

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
            Setup44._psy_compand_short_mapping,
            Setup44._psy_compand_long_mapping,

            new int[][]{Psych44._noise_start_short_44, Psych44._noise_start_long_44},
            new int[][]{Psych44._noise_part_short_44, Psych44._noise_part_long_44},
            Psych44._noise_thresh_44,

            Psych44._psy_ath_floater,
            Psych44._psy_ath_abs,

            Psych44._psy_lowpass_44,

            Psych44._psy_global_44,
            Setup44._global_mapping_44,
            null,

            FloorAll._floor_books,
            FloorAll._floor,
            2,
            Setup44._floor_mapping_44,

            Residue44u._mapres_template_44_uncoupled
    );

    public static final EncSetupDataTemplate setup_XX_stereo = new EncSetupDataTemplate(
            2,
            rate_mapping_X,
            Setup8.quality_mapping_8,
            2,
            0,
            8000,

            Setup8.blocksize_8,
            Setup8.blocksize_8,

            Psych8._psy_tone_masteratt_8,
            Psych44._psy_tone_0dB,
            Psych44._psy_tone_suppress,

            Psych8._tonemask_adj_8,
            null,
            Psych8._tonemask_adj_8,

            Psych8._psy_noiseguards_8,
            Psych8._psy_noisebias_8,
            Psych8._psy_noisebias_8,
            null,
            null,
            Psych44._psy_noise_suppress,

            Psych8._psy_compand_8,
            Setup8._psy_compand_8_mapping,
            null,

            new int[][]{Psych8._noise_start_8, Psych8._noise_start_8},
            new int[][]{Psych8._noise_part_8, Psych8._noise_part_8},
            Psych44._noise_thresh_5only,

            Psych8._psy_ath_floater_8,
            Psych8._psy_ath_abs_8,

            Psych8._psy_lowpass_8,

            Psych44._psy_global_44,
            Setup8._global_mapping_8,
            Psych8._psy_stereo_modes_8,

            FloorAll._floor_books,
            FloorAll._floor,
            1,
            Setup8._floor_mapping_8,

            Residue8._mapres_template_8_stereo
    );

    public static final EncSetupDataTemplate setup_XX_uncoupled = new EncSetupDataTemplate(
            2,
            rate_mapping_X,
            Setup8.quality_mapping_8,
            -1,
            0,
            8000,

            Setup8.blocksize_8,
            Setup8.blocksize_8,

            Psych8._psy_tone_masteratt_8,
            Psych44._psy_tone_0dB,
            Psych44._psy_tone_suppress,

            Psych8._tonemask_adj_8,
            null,
            Psych8._tonemask_adj_8,

            Psych8._psy_noiseguards_8,
            Psych8._psy_noisebias_8,
            Psych8._psy_noisebias_8,
            null,
            null,
            Psych44._psy_noise_suppress,

            Psych8._psy_compand_8,
            Setup8._psy_compand_8_mapping,
            null,

            new int[][]{Psych8._noise_start_8, Psych8._noise_start_8},
            new int[][]{Psych8._noise_part_8, Psych8._noise_part_8},
            Psych44._noise_thresh_5only,

            Psych8._psy_ath_floater_8,
            Psych8._psy_ath_abs_8,

            Psych8._psy_lowpass_8,

            Psych44._psy_global_44,
            Setup8._global_mapping_8,
            Psych8._psy_stereo_modes_8,

            FloorAll._floor_books,
            FloorAll._floor,
            1,
            Setup8._floor_mapping_8,

            Residue8._mapres_template_8_uncoupled
    );
}
