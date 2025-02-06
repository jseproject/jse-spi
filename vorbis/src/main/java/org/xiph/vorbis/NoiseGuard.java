package org.xiph.vorbis;

/**
 * high level configuration information for setting things up
 * step-by-step with the detailed vorbis_encode_ctl interface.
 * There's a fair amount of redundancy such that interactive setup
 * does not directly deal with any vorbis_info or codec_setup_info
 * initialization; it's all stored (until full init) in this highlevel
 * setup, then flushed out to the real codec setup structs later.
 */
public class NoiseGuard {
    final int lo;
    final int hi;
    final int fixed;

    //
    public NoiseGuard(int i_lo, int i_hi, int i_fixed) {
        lo = i_lo;
        hi = i_hi;
        fixed = i_fixed;
    }
}
