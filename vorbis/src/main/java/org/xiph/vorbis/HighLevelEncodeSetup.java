package org.xiph.vorbis;

/**
 * highlevel encoder setup struct separated out for vorbisenc clarity
 */
class HighLevelEncodeSetup {
    int set_in_stone = 0;
    EncSetupDataTemplate setup = null;
    double base_setting = 0;

    double impulse_noisetune = 0;

    /* bitrate management below all settable */
    float req = 0;
    boolean managed = false;
    int bitrate_min = 0;
    int bitrate_av = 0;
    double bitrate_av_damp = 0;
    int bitrate_max = 0;
    int bitrate_reservoir = 0;
    double bitrate_reservoir_bias = 0;

    boolean impulse_block_p = false;
    boolean noise_normalize_p = false;
    boolean coupling_p = false;

    double stereo_point_setting = 0.0;
    double lowpass_kHz = 0.0;
    boolean lowpass_altered = false;

    double ath_floating_dB = 0.0;
    double ath_absolute_dB = 0.0;

    double amplitude_track_dBpersec = 0.0;
    double trigger_setting = 0.0;

    /**
     * padding, impulse, transition, long
     */
    final HighLevelByBlockType[] block = new HighLevelByBlockType[4];

    //
    HighLevelEncodeSetup() {
        for (int i = 0, ie = block.length; i < ie; i++) {
            block[i] = new HighLevelByBlockType();
        }
    }
}
