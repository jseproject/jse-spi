package org.xiph.vorbis;

public class CompandBlock {
    final int[] data = new int[LookPsy.NOISE_COMPAND_LEVELS];

    //
    public CompandBlock(int[] i_data) {
        System.arraycopy(i_data, 0, data, 0, i_data.length);
    }
}
