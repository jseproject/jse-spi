package org.concentus;

/// <summary>
/// Structure for controlling decoder operation and reading decoder status
/// </summary>
class DecControlState {

    /* I:   Number of channels; 1/2                                                         */
    int nChannelsAPI = 0;

    /* I:   Number of channels; 1/2                                                         */
    int nChannelsInternal = 0;

    /* I:   Output signal sampling rate in Hertz; 8000/12000/16000/24000/32000/44100/48000  */
    int API_sampleRate = 0;

    /* I:   Internal sampling rate used, in Hertz; 8000/12000/16000                         */
    int internalSampleRate = 0;

    /* I:   Number of samples per packet in milliseconds; 10/20/40/60                       */
    int payloadSize_ms = 0;

    /* O:   Pitch lag of previous frame (0 if unvoiced), measured in samples at 48 kHz      */
    int prevPitchLag = 0;

    void Reset() {
        nChannelsAPI = 0;
        nChannelsInternal = 0;
        API_sampleRate = 0;
        internalSampleRate = 0;
        payloadSize_ms = 0;
        prevPitchLag = 0;
    }
}
