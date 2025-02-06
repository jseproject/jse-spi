package org.concentus;

/// <summary>
/// Struct for TOC (Table of Contents)
/// </summary>
class TOCStruct {

    /// <summary>
    /// Voice activity for packet
    /// </summary>
    int VADFlag = 0;

    /// <summary>
    /// Voice activity for each frame in packet
    /// </summary>
    final int[] VADFlags = new int[SilkConstants.SILK_MAX_FRAMES_PER_PACKET];

    /// <summary>
    /// Flag indicating if packet contains in-band FEC
    /// </summary>
    int inbandFECFlag = 0;

    void Reset() {
        VADFlag = 0;
        Arrays.MemSet(VADFlags, 0, SilkConstants.SILK_MAX_FRAMES_PER_PACKET);
        inbandFECFlag = 0;
    }
}
