/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 2016 Logan Stromberg
 * Copyright (c) 2007-2008 CSIRO
 * Copyright (c) 2007-2011 Xiph.Org Foundation
 * Copyright (c) 2006-2011 Skype Limited
 * Copyright (c) 2003-2004 Mark Borgerding
 * Copyright (c) 2001-2011 Microsoft Corporation,
 *                         Jean-Marc Valin, Gregory Maxwell,
 *                         Koen Vos, Timothy B. Terriberry
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
 * - Neither the name of Internet Society, IETF or IETF Trust, nor the
 * names of specific contributors, may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.concentus;

class QuantizeLTPGains {

    static void silk_quant_LTP_gains(
            short[] B_Q14, /* I/O  (un)quantized LTP gains [MAX_NB_SUBFR * LTP_ORDER]        */
            byte[] cbk_index, /* O    Codebook Index [MAX_NB_SUBFR]                 */
            BoxedValueByte periodicity_index, /* O    Periodicity Index               */
            BoxedValueInt sum_log_gain_Q7, /* I/O  Cumulative max prediction gain  */
            int[] W_Q18, /* I    Error Weights in Q18 [MAX_NB_SUBFR * LTP_ORDER * LTP_ORDER]           */
            int mu_Q9, /* I    Mu value (R/D tradeoff)         */
            int lowComplexity, /* I    Flag for low complexity         */
            int nb_subfr /* I    number of subframes             */
    ) {
        int j, k, cbk_size;
        byte[] temp_idx = new byte[SilkConstants.MAX_NB_SUBFR];
        short[] cl_ptr_Q5;
        byte[][] cbk_ptr_Q7;
        short[] cbk_gain_ptr_Q7;
        int b_Q14_ptr;
        int W_Q18_ptr;
        int rate_dist_Q14_subfr, rate_dist_Q14, min_rate_dist_Q14;
        int sum_log_gain_tmp_Q7, best_sum_log_gain_Q7, max_gain_Q7, gain_Q7;

        /**
         * ************************************************
         */
        /* iterate over different codebooks with different */
 /* rates/distortions, and choose best */
        /**
         * ************************************************
         */
        min_rate_dist_Q14 = Integer.MAX_VALUE;
        best_sum_log_gain_Q7 = 0;
        for (k = 0; k < 3; k++) {
            /* Safety margin for pitch gain control, to take into account factors
               such as state rescaling/rewhitening. */
            int gain_safety = ((int) ((0.4f) * ((long) 1 << (7)) + 0.5))/*Inlines.SILK_CONST(0.4f, 7)*/;

            cl_ptr_Q5 = SilkTables.silk_LTP_gain_BITS_Q5_ptrs[k];
            cbk_ptr_Q7 = SilkTables.silk_LTP_vq_ptrs_Q7[k];
            cbk_gain_ptr_Q7 = SilkTables.silk_LTP_vq_gain_ptrs_Q7[k];
            cbk_size = SilkTables.silk_LTP_vq_sizes[k];

            /* Set up pointer to first subframe */
            W_Q18_ptr = 0;
            b_Q14_ptr = 0;

            rate_dist_Q14 = 0;
            sum_log_gain_tmp_Q7 = sum_log_gain_Q7.Val;
            for (j = 0; j < nb_subfr; j++) {
                max_gain_Q7 = Inlines.silk_log2lin((((int) ((TuningParameters.MAX_SUM_LOG_GAIN_DB / 6.0f) * ((long) 1 << (7)) + 0.5))/*Inlines.SILK_CONST(TuningParameters.MAX_SUM_LOG_GAIN_DB / 6.0f, 7)*/ - sum_log_gain_tmp_Q7)
                        + ((int) ((7) * ((long) 1 << (7)) + 0.5))/*Inlines.SILK_CONST(7, 7)*/) - gain_safety;

                BoxedValueByte temp_idx_box = new BoxedValueByte(temp_idx[j]);
                BoxedValueInt rate_dist_Q14_subfr_box = new BoxedValueInt(0);
                BoxedValueInt gain_Q7_box = new BoxedValueInt(0);
                VQ_WMat_EC.silk_VQ_WMat_EC(
                        temp_idx_box, /* O    index of best codebook vector                           */
                        rate_dist_Q14_subfr_box, /* O    best weighted quantization error + mu * rate            */
                        gain_Q7_box, /* O    sum of absolute LTP coefficients                        */
                        B_Q14,
                        b_Q14_ptr, /* I    input vector to be quantized                            */
                        W_Q18,
                        W_Q18_ptr, /* I    weighting matrix                                        */
                        cbk_ptr_Q7, /* I    codebook                                                */
                        cbk_gain_ptr_Q7, /* I    codebook effective gains                                */
                        cl_ptr_Q5, /* I    code length for each codebook vector                    */
                        mu_Q9, /* I    tradeoff between weighted error and rate                */
                        max_gain_Q7, /* I    maximum sum of absolute LTP coefficients                */
                        cbk_size /* I    number of vectors in codebook                           */
                );
                rate_dist_Q14_subfr = rate_dist_Q14_subfr_box.Val;
                gain_Q7 = gain_Q7_box.Val;
                temp_idx[j] = temp_idx_box.Val;

                rate_dist_Q14 = Inlines.silk_ADD_POS_SAT32(rate_dist_Q14, rate_dist_Q14_subfr);
                sum_log_gain_tmp_Q7 = Inlines.silk_max(0, sum_log_gain_tmp_Q7
                        + Inlines.silk_lin2log(gain_safety + gain_Q7) - ((int) ((7) * ((long) 1 << (7)) + 0.5))/*Inlines.SILK_CONST(7, 7)*/);

                b_Q14_ptr += SilkConstants.LTP_ORDER;
                W_Q18_ptr += SilkConstants.LTP_ORDER * SilkConstants.LTP_ORDER;
            }

            /* Avoid never finding a codebook */
            rate_dist_Q14 = Inlines.silk_min(Integer.MAX_VALUE - 1, rate_dist_Q14);

            if (rate_dist_Q14 < min_rate_dist_Q14) {
                min_rate_dist_Q14 = rate_dist_Q14;
                periodicity_index.Val = (byte) k;
                System.arraycopy(temp_idx, 0, cbk_index, 0, nb_subfr);
                best_sum_log_gain_Q7 = sum_log_gain_tmp_Q7;
            }

            /* Break early in low-complexity mode if rate distortion is below threshold */
            if (lowComplexity != 0 && (rate_dist_Q14 < SilkTables.silk_LTP_gain_middle_avg_RD_Q14)) {
                break;
            }
        }

        cbk_ptr_Q7 = SilkTables.silk_LTP_vq_ptrs_Q7[periodicity_index.Val];
        for (j = 0; j < nb_subfr; j++) {
            for (k = 0; k < SilkConstants.LTP_ORDER; k++) {
                B_Q14[j * SilkConstants.LTP_ORDER + k] = (short) (Inlines.silk_LSHIFT(cbk_ptr_Q7[cbk_index[j]][k], 7));
            }
        }

        sum_log_gain_Q7.Val = best_sum_log_gain_Q7;
    }
}
