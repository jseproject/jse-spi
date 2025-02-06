package org.xiph.vorbis.modes;

import org.xiph.vorbis.EncSetupDataTemplate;

/**
 * 8kHz settings
 */

public class Setup8 {

    protected static final int blocksize_8[] = {// [2]
            512, 512
    };

    private static final int _floor_mapping_8a[] = {
            6, 6
    };

    protected static final int _floor_mapping_8[][] = {
            _floor_mapping_8a
    };

    private static final double rate_mapping_8[] = {// [3]
            6000., 9000., 32000.,
    };

    private static final double rate_mapping_8_uncoupled[] = {// [3]
            8000., 14000., 42000.,
    };

    protected static final double quality_mapping_8[] = {// [3]
            -.1, .0, 1.
    };

    protected static final double _psy_compand_8_mapping[] = {0., 1., 1.};// [3]

    protected static final double _global_mapping_8[] = {1., 2., 3.};// [3]

    public static final EncSetupDataTemplate setup_8_stereo = new EncSetupDataTemplate(
            2,
            rate_mapping_8,
            quality_mapping_8,
            2,
            8000,
            9000,

            blocksize_8,
            blocksize_8,

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
            _psy_compand_8_mapping,
            null,

            new int[][]{Psych8._noise_start_8, Psych8._noise_start_8},
            new int[][]{Psych8._noise_part_8, Psych8._noise_part_8},
            Psych44._noise_thresh_5only,

            Psych8._psy_ath_floater_8,
            Psych8._psy_ath_abs_8,

            Psych8._psy_lowpass_8,

            Psych44._psy_global_44,
            _global_mapping_8,
            Psych8._psy_stereo_modes_8,

            FloorAll._floor_books,
            FloorAll._floor,
            1,
            _floor_mapping_8,

            Residue8._mapres_template_8_stereo
    );

    public static final EncSetupDataTemplate setup_8_uncoupled = new EncSetupDataTemplate(
            2,
            rate_mapping_8_uncoupled,
            quality_mapping_8,
            -1,
            8000,
            9000,

            blocksize_8,
            blocksize_8,

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
            _psy_compand_8_mapping,
            null,

            new int[][]{Psych8._noise_start_8, Psych8._noise_start_8},
            new int[][]{Psych8._noise_part_8, Psych8._noise_part_8},
            Psych44._noise_thresh_5only,

            Psych8._psy_ath_floater_8,
            Psych8._psy_ath_abs_8,

            Psych8._psy_lowpass_8,

            Psych44._psy_global_44,
            _global_mapping_8,
            Psych8._psy_stereo_modes_8,

            FloorAll._floor_books,
            FloorAll._floor,
            1,
            _floor_mapping_8,

            Residue8._mapres_template_8_uncoupled
    );
}
