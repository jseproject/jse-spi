package org.xiph.vorbis.modes;

import org.xiph.vorbis.EncSetupDataTemplate;

/**
 * toplevel settings for 44.1/48kHz uncoupled modes
 */

public class Setup44u {

    private static final double rate_mapping_44_un[] = {// [12]
            32000., 48000., 60000., 70000., 80000., 86000.,
            96000., 110000., 120000., 140000., 160000., 240001.
    };

    public static final EncSetupDataTemplate setup_44_uncoupled = new EncSetupDataTemplate(
            11,
            rate_mapping_44_un,
            Setup44.quality_mapping_44,
            -1,
            40000,
            50000,

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

            Residue44u._mapres_template_44_uncoupled
    );
}