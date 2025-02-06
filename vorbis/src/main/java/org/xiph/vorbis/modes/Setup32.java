package org.xiph.vorbis.modes;

import org.xiph.vorbis.EncSetupDataTemplate;

/**
 * toplevel settings for 32kHz
 */

public class Setup32 {

    private static final double rate_mapping_32[] = {// [12]
            18000., 28000., 35000., 45000., 56000., 60000.,
            75000., 90000., 100000., 115000., 150000., 190000.,
    };

    private static final double rate_mapping_32_un[] = {// [12]
            30000., 42000., 52000., 64000., 72000., 78000.,
            86000., 92000., 110000., 120000., 140000., 190000.,
    };

    private static final double _psy_lowpass_32[] = {// [12]
            12.3, 13., 13., 14., 15., 99., 99., 99., 99., 99., 99., 99.
    };

    public static final EncSetupDataTemplate setup_32_stereo = new EncSetupDataTemplate(
            11,
            rate_mapping_32,
            Setup44.quality_mapping_44,
            2,
            26000,
            40000,

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

            _psy_lowpass_32,

            Psych44._psy_global_44,
            Setup44._global_mapping_44,
            Psych44._psy_stereo_modes_44,

            FloorAll._floor_books,
            FloorAll._floor,
            2,
            Setup44._floor_mapping_44,

            Residue44._mapres_template_44_stereo
    );

    public static final EncSetupDataTemplate setup_32_uncoupled = new EncSetupDataTemplate(
            11,
            rate_mapping_32_un,
            Setup44.quality_mapping_44,
            -1,
            26000,
            40000,

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

            _psy_lowpass_32,

            Psych44._psy_global_44,
            Setup44._global_mapping_44,
            null,

            FloorAll._floor_books,
            FloorAll._floor,
            2,
            Setup44._floor_mapping_44,

            Residue44u._mapres_template_44_uncoupled
    );
}
