package org.concentus;

/**
 * *********************
 */
/* Encoder control FIX  */
/**
 * *********************
 */
class SilkEncoderControl {

    /* Prediction and coding parameters */
    final int[] Gains_Q16 = new int[SilkConstants.MAX_NB_SUBFR];
    final short[][] PredCoef_Q12 = Arrays.InitTwoDimensionalArrayShort(2, SilkConstants.MAX_LPC_ORDER);
    /* holds interpolated and final coefficients */
    final short[] LTPCoef_Q14 = new short[SilkConstants.LTP_ORDER * SilkConstants.MAX_NB_SUBFR];
    int LTP_scale_Q14 = 0;
    final int[] pitchL = new int[SilkConstants.MAX_NB_SUBFR];

    /* Noise shaping parameters */
    final short[] AR1_Q13 = new short[SilkConstants.MAX_NB_SUBFR * SilkConstants.MAX_SHAPE_LPC_ORDER];
    final short[] AR2_Q13 = new short[SilkConstants.MAX_NB_SUBFR * SilkConstants.MAX_SHAPE_LPC_ORDER];
    final int[] LF_shp_Q14 = new int[SilkConstants.MAX_NB_SUBFR];
    /* Packs two int16 coefficients per int32 value         */
    final int[] GainsPre_Q14 = new int[SilkConstants.MAX_NB_SUBFR];
    final int[] HarmBoost_Q14 = new int[SilkConstants.MAX_NB_SUBFR];
    final int[] Tilt_Q14 = new int[SilkConstants.MAX_NB_SUBFR];
    final int[] HarmShapeGain_Q14 = new int[SilkConstants.MAX_NB_SUBFR];
    int Lambda_Q10 = 0;
    int input_quality_Q14 = 0;
    int coding_quality_Q14 = 0;

    /* Measures */
    int sparseness_Q8 = 0;
    int predGain_Q16 = 0;
    int LTPredCodGain_Q7 = 0;

    /* Residual energy per subframe */
    final int[] ResNrg = new int[SilkConstants.MAX_NB_SUBFR];

    /* Q domain for the residual energy > 0                 */
    final int[] ResNrgQ = new int[SilkConstants.MAX_NB_SUBFR];

    /* Parameters for CBR mode */
    final int[] GainsUnq_Q16 = new int[SilkConstants.MAX_NB_SUBFR];
    byte lastGainIndexPrev = 0;

    void Reset() {
        Arrays.MemSet(Gains_Q16, 0, SilkConstants.MAX_NB_SUBFR);
        Arrays.MemSet(PredCoef_Q12[0], (short) 0, SilkConstants.MAX_LPC_ORDER);
        Arrays.MemSet(PredCoef_Q12[1], (short) 0, SilkConstants.MAX_LPC_ORDER);
        Arrays.MemSet(LTPCoef_Q14, (short) 0, SilkConstants.LTP_ORDER * SilkConstants.MAX_NB_SUBFR);
        LTP_scale_Q14 = 0;
        Arrays.MemSet(pitchL, 0, SilkConstants.MAX_NB_SUBFR);
        Arrays.MemSet(AR1_Q13, (short) 0, SilkConstants.MAX_NB_SUBFR * SilkConstants.MAX_SHAPE_LPC_ORDER);
        Arrays.MemSet(AR2_Q13, (short) 0, SilkConstants.MAX_NB_SUBFR * SilkConstants.MAX_SHAPE_LPC_ORDER);
        Arrays.MemSet(LF_shp_Q14, 0, SilkConstants.MAX_NB_SUBFR);
        Arrays.MemSet(GainsPre_Q14, 0, SilkConstants.MAX_NB_SUBFR);
        Arrays.MemSet(HarmBoost_Q14, 0, SilkConstants.MAX_NB_SUBFR);
        Arrays.MemSet(Tilt_Q14, 0, SilkConstants.MAX_NB_SUBFR);
        Arrays.MemSet(HarmShapeGain_Q14, 0, SilkConstants.MAX_NB_SUBFR);
        Lambda_Q10 = 0;
        input_quality_Q14 = 0;
        coding_quality_Q14 = 0;
        sparseness_Q8 = 0;
        predGain_Q16 = 0;
        LTPredCodGain_Q7 = 0;
        Arrays.MemSet(ResNrg, 0, SilkConstants.MAX_NB_SUBFR);
        Arrays.MemSet(ResNrgQ, 0, SilkConstants.MAX_NB_SUBFR);
        Arrays.MemSet(GainsUnq_Q16, 0, SilkConstants.MAX_NB_SUBFR);
        lastGainIndexPrev = 0;
    }
}
