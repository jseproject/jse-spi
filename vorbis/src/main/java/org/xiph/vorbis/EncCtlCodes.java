package org.xiph.vorbis;

/**
 * vorbis_encode_ctl() codes
 * <p>
 * These values are passed as the <code> parameter</code> of
 * {@link Info#encode_ctl}.
 * The type of the referent of that function's <code>arg</code> pointer
 * depends on these codes.
 */
public class EncCtlCodes {

    /**
     * Query the current encoder bitrate management setting.
     * <p>
     * Argument: <tt>struct ovectl_ratemanage2_arg *</tt>
     * <p>
     * Used to query the current encoder bitrate management setting. Also used to
     * initialize fields of an ovectl_ratemanage2_arg structure for use with
     * {@link #OV_ECTL_RATEMANAGE2_SET}.
     */
    public static final int OV_ECTL_RATEMANAGE2_GET = 0x14;

    /**
     * Set the current encoder bitrate management settings.
     * <p>
     * Argument: <tt>EncCtlRateManageArg2</tt>
     * <p>
     * Used to set the current encoder bitrate management settings to the values
     * listed in the ovectl_ratemanage2_arg. Passing a NULL pointer will disable
     * bitrate management.
     */
    public static final int OV_ECTL_RATEMANAGE2_SET = 0x15;

    /**
     * Returns the current encoder hard-lowpass setting (kHz) in the double
     * pointed to by arg.
     * <p>
     * Argument: <tt>double[]</tt>
     */
    public static final int OV_ECTL_LOWPASS_GET = 0x20;

    /**
     * Sets the encoder hard-lowpass to the value (kHz) pointed to by arg. Valid
     * lowpass settings range from 2 to 99.
     * <p>
     * Argument: <tt>double[]</tt>
     */
    public static final int OV_ECTL_LOWPASS_SET = 0x21;

    /**
     * Returns the current encoder impulse block setting in the double pointed
     * to by arg.
     * <p>
     * Argument: <tt>double[]</tt>
     */
    public static final int OV_ECTL_IBLOCK_GET = 0x30;

    /**
     * Sets the impulse block bias to the the value pointed to by arg.
     * <p>
     * Argument: <tt>double[]</tt>
     * <p>
     * Valid range is -15.0 to 0.0 [default]. A negative impulse block bias will
     * direct to encoder to use more bits when incoding short blocks that contain
     * strong impulses, thus improving the accuracy of impulse encoding.
     */
    public static final int OV_ECTL_IBLOCK_SET = 0x31;

    /**
     * Returns the current encoder coupling setting in the int pointed
     * to by arg.
     * <p>
     * Argument: <tt>int[]</tt>
     */
    public static final int OV_ECTL_COUPLING_GET = 0x40;

    /**
     * Enables/disables channel coupling in multichannel encoding according to arg.
     * <p>
     * Argument: <tt>int[]</tt>
     * <p>
     * Zero disables channel coupling for multichannel inputs, nonzer enables
     * channel coupling.  Setting has no effect on monophonic encoding or
     * multichannel counts that do not offer coupling.  At present, coupling is
     * available for stereo and 5.1 encoding.
     */
    public static final int OV_ECTL_COUPLING_SET = 0x41;

    /* deprecated rate management supported only for compatibility */

    /**
     * Old interface to querying bitrate management settings.
     * <p>
     * Deprecated after move to bit-reservoir style management in 1.1 rendered
     * this interface partially obsolete.
     * <p>
     *
     * @deprecated Please use {@link #OV_ECTL_RATEMANAGE2_GET} instead.
     * <p>
     * Argument: <tt>EncCtlRateManageArg</tt>
     */
    @Deprecated
    public static final int OV_ECTL_RATEMANAGE_GET = 0x10;

    /**
     * Old interface to modifying bitrate management settings.
     * <p>
     * deprecated after move to bit-reservoir style management in 1.1 rendered
     * this interface partially obsolete.
     * <p>
     *
     * @deprecated Please use {@link #OV_ECTL_RATEMANAGE2_SET} instead.
     * <p>
     * Argument: <tt>EncCtlRateManageArg</tt>
     */
    @Deprecated
    public static final int OV_ECTL_RATEMANAGE_SET = 0x11;

    /**
     * Old interface to setting average-bitrate encoding mode.
     * <p>
     * Deprecated after move to bit-reservoir style management in 1.1 rendered
     * this interface partially obsolete.
     * <p>
     *
     * @deprecated Please use {@link #OV_ECTL_RATEMANAGE2_SET} instead.
     * <p>
     * Argument: <tt>EncCtlRateManageArg</tt>
     */
    @Deprecated
    public static final int OV_ECTL_RATEMANAGE_AVG = 0x12;

    /**
     * Old interface to setting bounded-bitrate encoding modes.
     * <p>
     * deprecated after move to bit-reservoir style management in 1.1 rendered
     * this interface partially obsolete.
     * <p>
     *
     * @deprecated Please use {@link #OV_ECTL_RATEMANAGE2_SET} instead.
     * <p>
     * Argument: <tt>EncCtlRateManageArg</tt>
     */
    @Deprecated
    public static final int OV_ECTL_RATEMANAGE_HARD = 0x13;
}
