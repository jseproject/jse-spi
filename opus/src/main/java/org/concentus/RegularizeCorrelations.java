package org.concentus;

class RegularizeCorrelations {

    /* Add noise to matrix diagonal */
    static void silk_regularize_correlations(
            int[] XX, /* I/O  Correlation matrices                                                        */
            int XX_ptr,
            int[] xx, /* I/O  Correlation values                                                          */
            int xx_ptr,
            int noise, /* I    Noise to add                                                                */
            int D /* I    Dimension of XX                                                             */
    ) {
        int i;
        for (i = 0; i < D; i++) {
            Inlines.MatrixSet(XX, XX_ptr, i, i, D, Inlines.silk_ADD32(Inlines.MatrixGet(XX, XX_ptr, i, i, D), noise));
        }
        xx[xx_ptr] += noise;
    }
}
