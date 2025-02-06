package org.concentus;

public class OpusConstants {

    /// <summary>
    /// Auto/default setting
    /// </summary>
    public static final int OPUS_AUTO = -1000;

    /// <summary>
    /// Maximum bitrate
    /// </summary>
    public static final int OPUS_BITRATE_MAX = -1;

    // from analysis.c
    public static final int NB_FRAMES = 8;
    public static final int NB_TBANDS = 18;
    public static final int NB_TOT_BANDS = 21;
    public static final int NB_TONAL_SKIP_BANDS = 9;
    public static final int ANALYSIS_BUF_SIZE = 720;
    /* 15 ms at 48 kHz */
    public static final int DETECT_SIZE = 200;

    public static final int MAX_ENCODER_BUFFER = 480;
}
