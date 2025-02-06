package org.concentus;

/// <summary>
/// Decoder super struct
/// </summary>
class SilkDecoder {

    final SilkChannelDecoder[] channel_state = new SilkChannelDecoder[SilkConstants.DECODER_NUM_CHANNELS];
    final StereoDecodeState sStereo = new StereoDecodeState();
    int nChannelsAPI = 0;
    int nChannelsInternal = 0;
    int prev_decode_only_middle = 0;

    SilkDecoder() {
        for (int c = 0; c < SilkConstants.DECODER_NUM_CHANNELS; c++) {
            channel_state[c] = new SilkChannelDecoder();
        }
    }

    void Reset() {
        for (int c = 0; c < SilkConstants.DECODER_NUM_CHANNELS; c++) {
            channel_state[c].Reset();
        }
        sStereo.Reset();
        nChannelsAPI = 0;
        nChannelsInternal = 0;
        prev_decode_only_middle = 0;
    }
}
