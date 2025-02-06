package org.concentus;

class SilkResamplerState {

    final int[] sIIR = new int[SilkConstants.SILK_RESAMPLER_MAX_IIR_ORDER];
    /* this must be the first element of this struct FIXME why? */
    final int[] sFIR_i32 = new int[SilkConstants.SILK_RESAMPLER_MAX_FIR_ORDER]; // porting note: these two fields were originally a union, so that means only 1 will ever be used at a time.
    final short[] sFIR_i16 = new short[SilkConstants.SILK_RESAMPLER_MAX_FIR_ORDER];

    final short[] delayBuf = new short[48];
    int resampler_function = 0;
    int batchSize = 0;
    int invRatio_Q16 = 0;
    int FIR_Order = 0;
    int FIR_Fracs = 0;
    int Fs_in_kHz = 0;
    int Fs_out_kHz = 0;
    int inputDelay = 0;

    /// <summary>
    /// POINTER
    /// </summary>
    short[] Coefs = null;

    void Reset() {
        Arrays.MemSet(sIIR, 0, SilkConstants.SILK_RESAMPLER_MAX_IIR_ORDER);
        Arrays.MemSet(sFIR_i32, 0, SilkConstants.SILK_RESAMPLER_MAX_FIR_ORDER);
        Arrays.MemSet(sFIR_i16, (short) 0, SilkConstants.SILK_RESAMPLER_MAX_FIR_ORDER);
        Arrays.MemSet(delayBuf, (short) 0, 48);
        resampler_function = 0;
        batchSize = 0;
        invRatio_Q16 = 0;
        FIR_Order = 0;
        FIR_Fracs = 0;
        Fs_in_kHz = 0;
        Fs_out_kHz = 0;
        inputDelay = 0;
        Coefs = null;
    }

    void Assign(SilkResamplerState other) {
        resampler_function = other.resampler_function;
        batchSize = other.batchSize;
        invRatio_Q16 = other.invRatio_Q16;
        FIR_Order = other.FIR_Order;
        FIR_Fracs = other.FIR_Fracs;
        Fs_in_kHz = other.Fs_in_kHz;
        Fs_out_kHz = other.Fs_out_kHz;
        inputDelay = other.inputDelay;
        Coefs = other.Coefs;
        System.arraycopy(other.sIIR, 0, this.sIIR, 0, SilkConstants.SILK_RESAMPLER_MAX_IIR_ORDER);
        System.arraycopy(other.sFIR_i32, 0, this.sFIR_i32, 0, SilkConstants.SILK_RESAMPLER_MAX_FIR_ORDER);
        System.arraycopy(other.sFIR_i16, 0, this.sFIR_i16, 0, SilkConstants.SILK_RESAMPLER_MAX_FIR_ORDER);
        System.arraycopy(other.delayBuf, 0, this.delayBuf, 0, 48);
    }
}
