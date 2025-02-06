package org.xiph.vorbis;

/**
 * struct ovectl_ratemanage2_arg
 * <p>
 * The ovectl_ratemanage2_arg structure is used with vorbis_encode_ctl() and
 * the OV_ECTL_RATEMANAGE2_GET and OV_ECTL_RATEMANAGE2_SET calls in order to
 * query and modify specifics of the encoder's bitrate management
 * configuration.
 */
public class EncCtlRateManageArg2 {
    /**
     * nonzero if bitrate management is active
     */
    public boolean management_active = false;
    /**
     * Lower allowed bitrate limit in kilobits per second
     */
    public int bitrate_limit_min_kbps = 0;
    /**
     * Upper allowed bitrate limit in kilobits per second
     */
    public int bitrate_limit_max_kbps = 0;
    /**
     * Size of the bitrate reservoir in bits
     */
    public int bitrate_limit_reservoir_bits = 0;
    /**
     * Regulates the bitrate reservoir's preferred fill level in a range from 0.0
     * to 1.0; 0.0 tries to bank bits to buffer against future bitrate spikes, 1.0
     * buffers against future sudden drops in instantaneous bitrate. Default is
     * 0.1
     */
    public double bitrate_limit_reservoir_bias = 0.0;
    /**
     * Average bitrate setting in kilobits per second
     */
    public int bitrate_average_kbps = 0;
    /**
     * Slew rate limit setting for average bitrate adjustment; sets the minimum
     * time in seconds the bitrate tracker may swing from one extreme to the
     * other when boosting or damping average bitrate.
     */
    public double bitrate_average_damping = 0.0;
}
