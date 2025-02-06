package org.concentus;

public enum OpusSignal {
    OPUS_SIGNAL_UNKNOWN,
    OPUS_SIGNAL_AUTO,
    /// <summary>
    /// Signal being encoded is voice
    /// </summary>
    OPUS_SIGNAL_VOICE,
    /// <summary>
    /// Signal being encoded is music
    /// </summary>
    OPUS_SIGNAL_MUSIC
}
