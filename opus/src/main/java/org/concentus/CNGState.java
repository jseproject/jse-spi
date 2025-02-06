package org.concentus;

/// <summary>
/// Struct for CNG
/// </summary>
class CNGState {

    final int[] CNG_exc_buf_Q14 = new int[SilkConstants.MAX_FRAME_LENGTH];
    final short[] CNG_smth_NLSF_Q15 = new short[SilkConstants.MAX_LPC_ORDER];
    final int[] CNG_synth_state = new int[SilkConstants.MAX_LPC_ORDER];
    int CNG_smth_Gain_Q16 = 0;
    int rand_seed = 0;
    int fs_kHz = 0;

    void Reset() {
        Arrays.MemSet(CNG_exc_buf_Q14, 0, SilkConstants.MAX_FRAME_LENGTH);
        Arrays.MemSet(CNG_smth_NLSF_Q15, (short) 0, SilkConstants.MAX_LPC_ORDER);
        Arrays.MemSet(CNG_synth_state, 0, SilkConstants.MAX_LPC_ORDER);
        CNG_smth_Gain_Q16 = 0;
        rand_seed = 0;
        fs_kHz = 0;
    }
}
