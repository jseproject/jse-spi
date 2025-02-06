package org.xiph.vorbis;

class LookFloor1 extends LookFloor {
    final int[] sorted_index = new int[InfoFloor.VIF_POSIT + 2];
    final int[] forward_index = new int[InfoFloor.VIF_POSIT + 2];
    final int[] reverse_index = new int[InfoFloor.VIF_POSIT + 2];

    final int[] hineighbor = new int[InfoFloor.VIF_POSIT];
    final int[] loneighbor = new int[InfoFloor.VIF_POSIT];
    int posts = 0;

    int n = 0;
    int quant_q = 0;
    InfoFloor1 vi = null;

    int phrasebits = 0;
    int postbits = 0;
    int frames = 0;
}
