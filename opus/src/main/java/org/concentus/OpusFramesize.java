package org.concentus;

public enum OpusFramesize {
    /**
     * Error state
     */
    OPUS_FRAMESIZE_UNKNOWN,
    /// <summary>
    /// Select frame size from the argument (default)
    /// </summary>
    OPUS_FRAMESIZE_ARG,
    /// <summary>
    /// Use 2.5 ms frames
    /// </summary>
    OPUS_FRAMESIZE_2_5_MS,
    /// <summary>
    /// Use 5 ms frames
    /// </summary>
    OPUS_FRAMESIZE_5_MS,
    /// <summary>
    /// Use 10 ms frames
    /// </summary>
    OPUS_FRAMESIZE_10_MS,
    /// <summary>
    /// Use 20 ms frames
    /// </summary>
    OPUS_FRAMESIZE_20_MS,
    /// <summary>
    /// Use 40 ms frames
    /// </summary>
    OPUS_FRAMESIZE_40_MS,
    /// <summary>
    /// Use 60 ms frames
    /// </summary>
    OPUS_FRAMESIZE_60_MS,
    /// <summary>
    /// Do not use - not fully implemented. Optimize the frame size dynamically.
    /// </summary>
    OPUS_FRAMESIZE_VARIABLE
}

