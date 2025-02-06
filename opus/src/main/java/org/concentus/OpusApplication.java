package org.concentus;

public enum OpusApplication {
    OPUS_APPLICATION_UNIMPLEMENTED,
    /// <summary>
    /// Best for most VoIP/videoconference applications where listening quality and intelligibility matter most
    /// </summary>
    OPUS_APPLICATION_VOIP,
    /// <summary>
    /// Best for broadcast/high-fidelity application where the decoded audio should be as close as possible to the input
    /// </summary>
    OPUS_APPLICATION_AUDIO,
    /// <summary>
    /// Only use when lowest-achievable latency is what matters most. Voice-optimized modes cannot be used.
    /// </summary>
    OPUS_APPLICATION_RESTRICTED_LOWDELAY
}
