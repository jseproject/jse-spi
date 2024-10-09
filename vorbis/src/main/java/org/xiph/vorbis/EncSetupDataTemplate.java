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

package org.xiph.vorbis;

/**
 * high level configuration information for setting things up
 * step-by-step with the detailed vorbis_encode_ctl interface.
 * There's a fair amount of redundancy such that interactive setup
 * does not directly deal with any vorbis_info or codec_setup_info
 * initialization; it's all stored (until full init) in this highlevel
 * setup, then flushed out to the real codec setup structs later.
 */
public class EncSetupDataTemplate {
    final int mappings;
    final double[] rate_mapping;
    final double[] quality_mapping;
    final int coupling_restriction;
    final int samplerate_min_restriction;
    final int samplerate_max_restriction;

    final int[] blocksize_short;
    final int[] blocksize_long;

    final Att3[] psy_tone_masteratt;
    final int[] psy_tone_0dB;
    final int[] psy_tone_dBsuppress;

    final AdjBlock[] psy_tone_adj_impulse;
    final AdjBlock[] psy_tone_adj_long;
    final AdjBlock[] psy_tone_adj_other;

    final NoiseGuard[] psy_noiseguards;
    final Noise3[] psy_noise_bias_impulse;
    final Noise3[] psy_noise_bias_padding;
    final Noise3[] psy_noise_bias_trans;
    final Noise3[] psy_noise_bias_long;
    final int[] psy_noise_dBsuppress;

    final CompandBlock[] psy_noise_compand;
    final double[] psy_noise_compand_short_mapping;
    final double[] psy_noise_compand_long_mapping;

    final int[][] psy_noise_normal_start = new int[2][];
    final int[][] psy_noise_normal_partition = new int[2][];
    final double[] psy_noise_normal_thresh;

    final int[] psy_ath_float;
    final int[] psy_ath_abs;

    final double[] psy_lowpass;

    final InfoPsyGlobal[] global_params;
    final double[] global_mapping;
    final AdjStereo[] stereo_modes;

    final StaticCodebook[][] floor_books;
    final InfoFloor1[] floor_params;
    final int floor_mappings;
    final int[][] floor_mapping_list;

    final MappingTemplate[] maps;

    //
    public EncSetupDataTemplate(final int i_mappings,
                                final double[] pd_rate_mapping,
                                final double[] pd_quality_mapping,
                                final int i_coupling_restriction,
                                final int i_samplerate_min_restriction,
                                final int i_samplerate_max_restriction,

                                final int[] pi_blocksize_short,
                                final int[] pi_blocksize_long,

                                final Att3[] pa_psy_tone_masteratt,
                                final int[] pi_psy_tone_0dB,
                                final int[] pi_psy_tone_dBsuppress,

                                final AdjBlock[] pvp_psy_tone_adj_impulse,
                                final AdjBlock[] pvp_psy_tone_adj_long,
                                final AdjBlock[] pvp_psy_tone_adj_other,

                                final NoiseGuard[] png_psy_noiseguards,
                                final Noise3[] pn_psy_noise_bias_impulse,
                                final Noise3[] pn_psy_noise_bias_padding,
                                final Noise3[] pn_psy_noise_bias_trans,
                                final Noise3[] pn_psy_noise_bias_long,
                                final int[] pi_psy_noise_dBsuppress,

                                final CompandBlock[] pc_psy_noise_compand,
                                final double[] pd_psy_noise_compand_short_mapping,
                                final double[] pd_psy_noise_compand_long_mapping,

                                final int[][] pi_psy_noise_normal_start,
                                final int[][] pi_psy_noise_normal_partition,
                                final double[] pd_psy_noise_normal_thresh,

                                final int[] pi_psy_ath_float,
                                final int[] pi_psy_ath_abs,

                                final double[] pd_psy_lowpass,

                                final InfoPsyGlobal[] pvip_global_params,
                                final double[] pd_global_mapping,
                                final AdjStereo[] pa_stereo_modes,

                                final StaticCodebook[][] ps_floor_books,
                                final InfoFloor1[] pvi_floor_params,
                                final int i_floor_mappings,
                                final int[][] pi_floor_mapping_list,

                                final MappingTemplate[] pm_maps) {
        mappings = i_mappings;
        rate_mapping = pd_rate_mapping;
        quality_mapping = pd_quality_mapping;
        coupling_restriction = i_coupling_restriction;
        samplerate_min_restriction = i_samplerate_min_restriction;
        samplerate_max_restriction = i_samplerate_max_restriction;

        blocksize_short = pi_blocksize_short;
        blocksize_long = pi_blocksize_long;

        psy_tone_masteratt = pa_psy_tone_masteratt;
        psy_tone_0dB = pi_psy_tone_0dB;
        psy_tone_dBsuppress = pi_psy_tone_dBsuppress;

        psy_tone_adj_impulse = pvp_psy_tone_adj_impulse;
        psy_tone_adj_long = pvp_psy_tone_adj_long;
        psy_tone_adj_other = pvp_psy_tone_adj_other;

        psy_noiseguards = png_psy_noiseguards;
        psy_noise_bias_impulse = pn_psy_noise_bias_impulse;
        psy_noise_bias_padding = pn_psy_noise_bias_padding;
        psy_noise_bias_trans = pn_psy_noise_bias_trans;
        psy_noise_bias_long = pn_psy_noise_bias_long;
        psy_noise_dBsuppress = pi_psy_noise_dBsuppress;

        psy_noise_compand = pc_psy_noise_compand;
        psy_noise_compand_short_mapping = pd_psy_noise_compand_short_mapping;
        psy_noise_compand_long_mapping = pd_psy_noise_compand_long_mapping;

        for (int i = 0, length = pi_psy_noise_normal_start.length; i < length; i++) {
            psy_noise_normal_start[i] = pi_psy_noise_normal_start[i];
        }

        for (int i = 0, length = pi_psy_noise_normal_partition.length; i < length; i++) {
            psy_noise_normal_partition[i] = pi_psy_noise_normal_partition[i];
        }
        psy_noise_normal_thresh = pd_psy_noise_normal_thresh;

        psy_ath_float = pi_psy_ath_float;
        psy_ath_abs = pi_psy_ath_abs;

        psy_lowpass = pd_psy_lowpass;

        global_params = pvip_global_params;
        global_mapping = pd_global_mapping;
        stereo_modes = pa_stereo_modes;

        floor_books = ps_floor_books;
        floor_params = pvi_floor_params;
        floor_mappings = i_floor_mappings;
        floor_mapping_list = pi_floor_mapping_list;

        maps = pm_maps;
    }
}
