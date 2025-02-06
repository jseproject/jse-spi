package org.concentus;

class K2A {

    /* Step up function, converts reflection coefficients to prediction coefficients */
    static void silk_k2a(
            int[] A_Q24, /* O    Prediction coefficients [order] Q24                         */
            short[] rc_Q15, /* I    Reflection coefficients [order] Q15                         */
            int order /* I    Prediction order                                            */
    ) {
        int k, n;
        int[] Atmp = new int[SilkConstants.SILK_MAX_ORDER_LPC];

        for (k = 0; k < order; k++) {
            for (n = 0; n < k; n++) {
                Atmp[n] = A_Q24[n];
            }
            for (n = 0; n < k; n++) {
                A_Q24[n] = Inlines.silk_SMLAWB(A_Q24[n], Inlines.silk_LSHIFT(Atmp[k - n - 1], 1), rc_Q15[k]);
            }
            A_Q24[k] = 0 - Inlines.silk_LSHIFT((int) rc_Q15[k], 9);
        }
    }

    /* Step up function, converts reflection coefficients to prediction coefficients */
    static void silk_k2a_Q16(
            int[] A_Q24, /* O    Prediction coefficients [order] Q24                         */
            int[] rc_Q16, /* I    Reflection coefficients [order] Q16                         */
            int order /* I    Prediction order                                            */
    ) {
        int k, n;
        int[] Atmp = new int[SilkConstants.SILK_MAX_ORDER_LPC];

        for (k = 0; k < order; k++) {
            for (n = 0; n < k; n++) {
                Atmp[n] = A_Q24[n];
            }
            for (n = 0; n < k; n++) {
                A_Q24[n] = Inlines.silk_SMLAWW(A_Q24[n], Atmp[k - n - 1], rc_Q16[k]);
            }
            A_Q24[k] = 0 - Inlines.silk_LSHIFT(rc_Q16[k], 8);
        }
    }
}
