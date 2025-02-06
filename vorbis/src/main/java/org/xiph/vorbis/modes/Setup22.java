package org.xiph.vorbis.modes;

import org.xiph.vorbis.EncSetupDataTemplate;

/**
 * 22kHz settings
 */

public class Setup22 {

    private static final double rate_mapping_22[] = {// [4]
            15000., 20000., 44000., 86000.
    };

    private static final double rate_mapping_22_uncoupled[] = {// [4]
            16000., 28000., 50000., 90000.
    };

    private static final double _psy_lowpass_22[] = {9.5, 11., 30., 99.};// [4]

    public static final EncSetupDataTemplate setup_22_stereo = new EncSetupDataTemplate(
            3,
            rate_mapping_22,
            Setup16.quality_mapping_16,
            2,
            19000,
            26000,

            Setup16.blocksize_16_short,
            Setup16.blocksize_16_long,

            Psych16._psy_tone_masteratt_16,
            Psych44._psy_tone_0dB,
            Psych44._psy_tone_suppress,

            Psych16._tonemask_adj_16,
            Psych16._tonemask_adj_16,
            Psych16._tonemask_adj_16,

            Psych16._psy_noiseguards_16,
            Psych16._psy_noisebias_16_impulse,
            Psych16._psy_noisebias_16_short,
            Psych16._psy_noisebias_16_short,
            Psych16._psy_noisebias_16,
            Psych44._psy_noise_suppress,

            Psych8._psy_compand_8,
            Setup16._psy_compand_16_mapping,
            Setup16._psy_compand_16_mapping,

            new int[][]{Psych16._noise_start_16, Psych16._noise_start_16},
            new int[][]{Psych16._noise_part_16, Psych16._noise_part_16},
            Psych16._noise_thresh_16,

            Psych16._psy_ath_floater_16,
            Psych16._psy_ath_abs_16,

            _psy_lowpass_22,

            Psych44._psy_global_44,
            Setup16._global_mapping_16,
            Psych16._psy_stereo_modes_16,

            FloorAll._floor_books,
            FloorAll._floor,
            2,
            Setup16._floor_mapping_16,

            Residue16._mapres_template_16_stereo
    );

    public static final EncSetupDataTemplate setup_22_uncoupled = new EncSetupDataTemplate(
            3,
            rate_mapping_22_uncoupled,
            Setup16.quality_mapping_16,
            -1,
            19000,
            26000,

            Setup16.blocksize_16_short,
            Setup16.blocksize_16_long,

            Psych16._psy_tone_masteratt_16,
            Psych44._psy_tone_0dB,
            Psych44._psy_tone_suppress,

            Psych16._tonemask_adj_16,
            Psych16._tonemask_adj_16,
            Psych16._tonemask_adj_16,

            Psych16._psy_noiseguards_16,
            Psych16._psy_noisebias_16_impulse,
            Psych16._psy_noisebias_16_short,
            Psych16._psy_noisebias_16_short,
            Psych16._psy_noisebias_16,
            Psych44._psy_noise_suppress,

            Psych8._psy_compand_8,
            Setup16._psy_compand_16_mapping,
            Setup16._psy_compand_16_mapping,

            new int[][]{Psych16._noise_start_16, Psych16._noise_start_16},
            new int[][]{Psych16._noise_part_16, Psych16._noise_part_16},
            Psych16._noise_thresh_16,

            Psych16._psy_ath_floater_16,
            Psych16._psy_ath_abs_16,

            _psy_lowpass_22,

            Psych44._psy_global_44,
            Setup16._global_mapping_16,
            Psych16._psy_stereo_modes_16,

            FloorAll._floor_books,
            FloorAll._floor,
            2,
            Setup16._floor_mapping_16,

            Residue16._mapres_template_16_uncoupled
    );
}
