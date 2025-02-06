package org.xiph.vorbis;

/**
 * high level configuration information for setting things up
 * step-by-step with the detailed vorbis_encode_ctl interface.
 * There's a fair amount of redundancy such that interactive setup
 * does not directly deal with any vorbis_info or codec_setup_info
 * initialization; it's all stored (until full init) in this highlevel
 * setup, then flushed out to the real codec setup structs later.
 */
public class Noise3 {
    final int[][] data = new int[LookPsy.P_NOISECURVES][17];

    //
    public Noise3(final int[][] i_data) {
        for (int i = 0, ie = i_data.length; i < ie; i++) {
            System.arraycopy(i_data[i], 0, data[i], 0, i_data[i].length);
        }
    }
}
