package org.xiph.vorbis;

/**
 * @deprecated This is a deprecated interface. Please use vorbis_encode_ctl()
 * with the {@link EncCtlRateManageArg2} struct and
 * {@link EncCtlCodes#OV_ECTL_RATEMANAGE2_GET} and
 * {@link EncCtlCodes#OV_ECTL_RATEMANAGE2_SET} calls in new code.
 * <p>
 * The {@link EncCtlRateManageArg} structure is used with vorbis_encode_ctl()
 * and the {@link EncCtlCodes#OV_ECTL_RATEMANAGE_GET},
 * {@link EncCtlCodes#OV_ECTL_RATEMANAGE_SET},
 * {@link EncCtlCodes#OV_ECTL_RATEMANAGE_AVG},
 * {@link EncCtlCodes#OV_ECTL_RATEMANAGE_HARD} calls in order to
 * query and modify specifics of the encoder's bitrate management
 * configuration.
 */
@Deprecated
public class EncCtlRateManageArg {
    /**
     * < nonzero if bitrate management is active
     */
    public boolean management_active = false;
    /**
     * hard lower limit (in kilobits per second) below which the stream bitrate
     * will never be allowed for any given bitrate_hard_window seconds of time.
     */
    public int bitrate_hard_min = 0;
    /**
     * hard upper limit (in kilobits per second) above which the stream bitrate
     * will never be allowed for any given bitrate_hard_window seconds of time.
     */
    public int bitrate_hard_max = 0;
    /**
     * the window period (in seconds) used to regulate the hard bitrate minimum
     * and maximum
     */
    public double bitrate_hard_window = 0.0;
    /**
     * soft lower limit (in kilobits per second) below which the average bitrate
     * tracker will start nudging the bitrate higher.
     */
    public int bitrate_av_lo = 0;
    /**
     * soft upper limit (in kilobits per second) above which the average bitrate
     * tracker will start nudging the bitrate lower.
     */
    public int bitrate_av_hi = 0;
    /**
     * the window period (in seconds) used to regulate the average bitrate
     * minimum and maximum.
     */
    public double bitrate_av_window = 0.0;
    /**
     * Regulates the relative centering of the average and hard windows; in
     * libvorbis 1.0 and 1.0.1, the hard window regulation overlapped but
     * followed the average window regulation. In libvorbis 1.1 a bit-reservoir
     * interface replaces the old windowing interface; the older windowing
     * interface is simulated and this field has no effect.
     */
    public double bitrate_av_window_center = 0.0;
}
