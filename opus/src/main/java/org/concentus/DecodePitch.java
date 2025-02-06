package org.concentus;

class DecodePitch {

    static void silk_decode_pitch(
            short lagIndex, /* I                                                                */
            byte contourIndex, /* O                                                                */
            int[] pitch_lags, /* O    4 pitch values                                              */
            int Fs_kHz, /* I    sampling frequency (kHz)                                    */
            int nb_subfr /* I    number of sub frames                                        */
    ) {
        int lag, k, min_lag, max_lag;
        byte[][] Lag_CB_ptr;

        if (Fs_kHz == 8) {
            if (nb_subfr == SilkConstants.PE_MAX_NB_SUBFR) {
                Lag_CB_ptr = SilkTables.silk_CB_lags_stage2;
            } else {
                Inlines.OpusAssert(nb_subfr == SilkConstants.PE_MAX_NB_SUBFR >> 1);
                Lag_CB_ptr = SilkTables.silk_CB_lags_stage2_10_ms;
            }
        } else if (nb_subfr == SilkConstants.PE_MAX_NB_SUBFR) {
            Lag_CB_ptr = SilkTables.silk_CB_lags_stage3;
        } else {
            Inlines.OpusAssert(nb_subfr == SilkConstants.PE_MAX_NB_SUBFR >> 1);
            Lag_CB_ptr = SilkTables.silk_CB_lags_stage3_10_ms;
        }

        min_lag = Inlines.silk_SMULBB(SilkConstants.PE_MIN_LAG_MS, Fs_kHz);
        max_lag = Inlines.silk_SMULBB(SilkConstants.PE_MAX_LAG_MS, Fs_kHz);
        lag = min_lag + lagIndex;

        for (k = 0; k < nb_subfr; k++) {
            pitch_lags[k] = lag + Lag_CB_ptr[k][contourIndex];
            pitch_lags[k] = Inlines.silk_LIMIT(pitch_lags[k], min_lag, max_lag);
        }
    }
}
