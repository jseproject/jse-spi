package org.concentus;

/// <summary>
/// Decoder control
/// </summary>
class SilkDecoderControl {

    /* Prediction and coding parameters */
    final int[] pitchL = new int[SilkConstants.MAX_NB_SUBFR];
    final int[] Gains_Q16 = new int[SilkConstants.MAX_NB_SUBFR];

    /* Holds interpolated and final coefficients */
    final short[][] PredCoef_Q12 = Arrays.InitTwoDimensionalArrayShort(2, SilkConstants.MAX_LPC_ORDER);
    final short[] LTPCoef_Q14 = new short[SilkConstants.LTP_ORDER * SilkConstants.MAX_NB_SUBFR];
    int LTP_scale_Q14 = 0;

    void Reset() {
        Arrays.MemSet(pitchL, 0, SilkConstants.MAX_NB_SUBFR);
        Arrays.MemSet(Gains_Q16, 0, SilkConstants.MAX_NB_SUBFR);
        Arrays.MemSet(PredCoef_Q12[0], (short) 0, SilkConstants.MAX_LPC_ORDER);
        Arrays.MemSet(PredCoef_Q12[1], (short) 0, SilkConstants.MAX_LPC_ORDER);
        Arrays.MemSet(LTPCoef_Q14, (short) 0, SilkConstants.LTP_ORDER * SilkConstants.MAX_NB_SUBFR);
        LTP_scale_Q14 = 0;
    }
}
