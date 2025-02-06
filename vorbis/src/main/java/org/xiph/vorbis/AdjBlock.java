package org.xiph.vorbis;

public class AdjBlock {
    final int[] block = new int[LookPsy.P_BANDS];

    //
    public AdjBlock(int[] i_block) {
        System.arraycopy(i_block, 0, block, 0, i_block.length);
    }
}
