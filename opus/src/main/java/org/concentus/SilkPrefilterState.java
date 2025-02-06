package org.concentus;

/// <summary>
/// Prefilter state
/// </summary>
class SilkPrefilterState {

    final short[] sLTP_shp = new short[SilkConstants.LTP_BUF_LENGTH];
    final int[] sAR_shp = new int[SilkConstants.MAX_SHAPE_LPC_ORDER + 1];
    int sLTP_shp_buf_idx = 0;
    int sLF_AR_shp_Q12 = 0;
    int sLF_MA_shp_Q12 = 0;
    int sHarmHP_Q2 = 0;
    int rand_seed = 0;
    int lagPrev = 0;

    SilkPrefilterState() {

    }

    void Reset() {
        Arrays.MemSet(sLTP_shp, (short) 0, SilkConstants.LTP_BUF_LENGTH);
        Arrays.MemSet(sAR_shp, 0, SilkConstants.MAX_SHAPE_LPC_ORDER + 1);
        sLTP_shp_buf_idx = 0;
        sLF_AR_shp_Q12 = 0;
        sLF_MA_shp_Q12 = 0;
        sHarmHP_Q2 = 0;
        rand_seed = 0;
        lagPrev = 0;
    }
}
