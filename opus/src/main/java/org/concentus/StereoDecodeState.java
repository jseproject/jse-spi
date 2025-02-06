package org.concentus;

class StereoDecodeState {

    final short[] pred_prev_Q13 = new short[2];
    final short[] sMid = new short[2];
    final short[] sSide = new short[2];

    void Reset() {
        Arrays.MemSet(pred_prev_Q13, (short) 0, 2);
        Arrays.MemSet(sMid, (short) 0, 2);
        Arrays.MemSet(sSide, (short) 0, 2);
    }
}
