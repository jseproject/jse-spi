package org.concentus;

class DecodeIndices {

    /* Decode side-information parameters from payload */
    static void silk_decode_indices(
            SilkChannelDecoder psDec, /* I/O  State                                       */
            EntropyCoder psRangeDec, /* I/O  Compressor data structure                   */
            int FrameIndex, /* I    Frame number                                */
            int decode_LBRR, /* I    Flag indicating LBRR data is being decoded  */
            int condCoding /* I    The type of conditional coding to use       */
    ) {
        int i, k, Ix;
        int decode_absolute_lagIndex, delta_lagIndex;
        short[] ec_ix = new short[psDec.LPC_order];
        short[] pred_Q8 = new short[psDec.LPC_order];

        /**
         * ****************************************
         */
        /* Decode signal type and quantizer offset */
        /**
         * ****************************************
         */
        if (decode_LBRR != 0 || psDec.VAD_flags[FrameIndex] != 0) {
            Ix = psRangeDec.dec_icdf(SilkTables.silk_type_offset_VAD_iCDF, 8) + 2;
        } else {
            Ix = psRangeDec.dec_icdf(SilkTables.silk_type_offset_no_VAD_iCDF, 8);
        }
        psDec.indices.signalType = (byte) Inlines.silk_RSHIFT(Ix, 1);
        psDec.indices.quantOffsetType = (byte) (Ix & 1);

        /**
         * *************
         */
        /* Decode gains */
        /**
         * *************
         */
        /* First subframe */
        if (condCoding == SilkConstants.CODE_CONDITIONALLY) {
            /* Conditional coding */
            psDec.indices.GainsIndices[0] = (byte) psRangeDec.dec_icdf(SilkTables.silk_delta_gain_iCDF, 8);
        } else {
            /* Independent coding, in two stages: MSB bits followed by 3 LSBs */
            psDec.indices.GainsIndices[0] = (byte) Inlines.silk_LSHIFT(psRangeDec.dec_icdf(SilkTables.silk_gain_iCDF[psDec.indices.signalType], 8), 3);
            psDec.indices.GainsIndices[0] += (byte) psRangeDec.dec_icdf(SilkTables.silk_uniform8_iCDF, 8);
        }

        /* Remaining subframes */
        for (i = 1; i < psDec.nb_subfr; i++) {
            psDec.indices.GainsIndices[i] = (byte) psRangeDec.dec_icdf(SilkTables.silk_delta_gain_iCDF, 8);
        }

        /**
         * *******************
         */
        /* Decode LSF Indices */
        /**
         * *******************
         */
        psDec.indices.NLSFIndices[0] = (byte) psRangeDec.dec_icdf(psDec.psNLSF_CB.CB1_iCDF, (psDec.indices.signalType >> 1) * psDec.psNLSF_CB.nVectors, 8);
        NLSF.silk_NLSF_unpack(ec_ix, pred_Q8, psDec.psNLSF_CB, psDec.indices.NLSFIndices[0]);
        Inlines.OpusAssert(psDec.psNLSF_CB.order == psDec.LPC_order);
        for (i = 0; i < psDec.psNLSF_CB.order; i++) {
            Ix = psRangeDec.dec_icdf(psDec.psNLSF_CB.ec_iCDF, (ec_ix[i]), 8);
            if (Ix == 0) {
                Ix -= psRangeDec.dec_icdf(SilkTables.silk_NLSF_EXT_iCDF, 8);
            } else if (Ix == 2 * SilkConstants.NLSF_QUANT_MAX_AMPLITUDE) {
                Ix += psRangeDec.dec_icdf(SilkTables.silk_NLSF_EXT_iCDF, 8);
            }
            psDec.indices.NLSFIndices[i + 1] = (byte) (Ix - SilkConstants.NLSF_QUANT_MAX_AMPLITUDE);
        }

        /* Decode LSF interpolation factor */
        if (psDec.nb_subfr == SilkConstants.MAX_NB_SUBFR) {
            psDec.indices.NLSFInterpCoef_Q2 = (byte) psRangeDec.dec_icdf(SilkTables.silk_NLSF_interpolation_factor_iCDF, 8);
        } else {
            psDec.indices.NLSFInterpCoef_Q2 = 4;
        }

        if (psDec.indices.signalType == SilkConstants.TYPE_VOICED) {
            /**
             * ******************
             */
            /* Decode pitch lags */
            /**
             * ******************
             */
            /* Get lag index */
            decode_absolute_lagIndex = 1;
            if (condCoding == SilkConstants.CODE_CONDITIONALLY && psDec.ec_prevSignalType == SilkConstants.TYPE_VOICED) {
                /* Decode Delta index */
                delta_lagIndex = (short) psRangeDec.dec_icdf(SilkTables.silk_pitch_delta_iCDF, 8);
                if (delta_lagIndex > 0) {
                    delta_lagIndex = delta_lagIndex - 9;
                    psDec.indices.lagIndex = (short) (psDec.ec_prevLagIndex + delta_lagIndex);
                    decode_absolute_lagIndex = 0;
                }
            }
            if (decode_absolute_lagIndex != 0) {
                /* Absolute decoding */
                psDec.indices.lagIndex = (short) (psRangeDec.dec_icdf(SilkTables.silk_pitch_lag_iCDF, 8) * Inlines.silk_RSHIFT(psDec.fs_kHz, 1));
                psDec.indices.lagIndex += (short) psRangeDec.dec_icdf(psDec.pitch_lag_low_bits_iCDF, 8);
            }
            psDec.ec_prevLagIndex = psDec.indices.lagIndex;

            /* Get countour index */
            psDec.indices.contourIndex = (byte) psRangeDec.dec_icdf(psDec.pitch_contour_iCDF, 8);

            /**
             * *****************
             */
            /* Decode LTP gains */
            /**
             * *****************
             */
            /* Decode PERIndex value */
            psDec.indices.PERIndex = (byte) psRangeDec.dec_icdf(SilkTables.silk_LTP_per_index_iCDF, 8);

            for (k = 0; k < psDec.nb_subfr; k++) {
                psDec.indices.LTPIndex[k] = (byte) psRangeDec.dec_icdf(SilkTables.silk_LTP_gain_iCDF_ptrs[psDec.indices.PERIndex], 8);
            }

            /**
             * *******************
             */
            /* Decode LTP scaling */
            /**
             * *******************
             */
            if (condCoding == SilkConstants.CODE_INDEPENDENTLY) {
                psDec.indices.LTP_scaleIndex = (byte) psRangeDec.dec_icdf(SilkTables.silk_LTPscale_iCDF, 8);
            } else {
                psDec.indices.LTP_scaleIndex = 0;
            }
        }
        psDec.ec_prevSignalType = psDec.indices.signalType;

        /**
         * ************
         */
        /* Decode seed */
        /**
         * ************
         */
        psDec.indices.Seed = (byte) psRangeDec.dec_icdf(SilkTables.silk_uniform4_iCDF, 8);
    }
}
