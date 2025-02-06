package org.concentus;

/// <summary>
/// Struct for Packet Loss Concealment
/// </summary>
class PLCStruct {

    int pitchL_Q8 = 0;
    /* Pitch lag to use for voiced concealment                          */
    final short[] LTPCoef_Q14 = new short[SilkConstants.LTP_ORDER];
    /* LTP coeficients to use for voiced concealment                    */
    final short[] prevLPC_Q12 = new short[SilkConstants.MAX_LPC_ORDER];
    int last_frame_lost = 0;
    /* Was previous frame lost                                          */
    int rand_seed = 0;
    /* Seed for unvoiced signal generation                              */
    short randScale_Q14 = 0;
    /* Scaling of unvoiced random signal                                */
    int conc_energy = 0;
    int conc_energy_shift = 0;
    short prevLTP_scale_Q14 = 0;
    final int[] prevGain_Q16 = new int[2];
    int fs_kHz = 0;
    int nb_subfr = 0;
    int subfr_length = 0;

    void Reset() {
        pitchL_Q8 = 0;
        Arrays.MemSet(LTPCoef_Q14, (short) 0, SilkConstants.LTP_ORDER);
        Arrays.MemSet(prevLPC_Q12, (short) 0, SilkConstants.MAX_LPC_ORDER);
        last_frame_lost = 0;
        rand_seed = 0;
        randScale_Q14 = 0;
        conc_energy = 0;
        conc_energy_shift = 0;
        prevLTP_scale_Q14 = 0;
        Arrays.MemSet(prevGain_Q16, 0, 2);
        fs_kHz = 0;
        nb_subfr = 0;
        subfr_length = 0;
    }
}
