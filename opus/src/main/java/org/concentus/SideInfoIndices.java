package org.concentus;

class SideInfoIndices {

    final byte[] GainsIndices = new byte[SilkConstants.MAX_NB_SUBFR];
    final byte[] LTPIndex = new byte[SilkConstants.MAX_NB_SUBFR];
    final byte[] NLSFIndices = new byte[SilkConstants.MAX_LPC_ORDER + 1];
    short lagIndex = 0;
    byte contourIndex = 0;
    byte signalType = 0;
    byte quantOffsetType = 0;
    byte NLSFInterpCoef_Q2 = 0;
    byte PERIndex = 0;
    byte LTP_scaleIndex = 0;
    byte Seed = 0;

    void Reset() {
        Arrays.MemSet(GainsIndices, (byte) 0, SilkConstants.MAX_NB_SUBFR);
        Arrays.MemSet(LTPIndex, (byte) 0, SilkConstants.MAX_NB_SUBFR);
        Arrays.MemSet(NLSFIndices, (byte) 0, SilkConstants.MAX_LPC_ORDER + 1);
        lagIndex = 0;
        contourIndex = 0;
        signalType = 0;
        quantOffsetType = 0;
        NLSFInterpCoef_Q2 = 0;
        PERIndex = 0;
        LTP_scaleIndex = 0;
        Seed = 0;
    }

    /// <summary>
    /// Overwrites this struct with values from another one. Equivalent to C struct assignment this = other
    /// </summary>
    /// <param name="other"></param>
    void Assign(SideInfoIndices other) {
        System.arraycopy(other.GainsIndices, 0, this.GainsIndices, 0, SilkConstants.MAX_NB_SUBFR);
        System.arraycopy(other.LTPIndex, 0, this.LTPIndex, 0, SilkConstants.MAX_NB_SUBFR);
        System.arraycopy(other.NLSFIndices, 0, this.NLSFIndices, 0, SilkConstants.MAX_LPC_ORDER + 1);
        this.lagIndex = other.lagIndex;
        this.contourIndex = other.contourIndex;
        this.signalType = other.signalType;
        this.quantOffsetType = other.quantOffsetType;
        this.NLSFInterpCoef_Q2 = other.NLSFInterpCoef_Q2;
        this.PERIndex = other.PERIndex;
        this.LTP_scaleIndex = other.LTP_scaleIndex;
        this.Seed = other.Seed;
    }
}
