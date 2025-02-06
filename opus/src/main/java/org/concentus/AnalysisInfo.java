package org.concentus;

class AnalysisInfo {

    boolean enabled = false;
    int valid = 0;
    float tonality = 0;
    float tonality_slope = 0;
    float noisiness = 0;
    float activity = 0;
    float music_prob = 0;
    int bandwidth = 0;

    AnalysisInfo() {
    }

    void Assign(AnalysisInfo other) {
        this.valid = other.valid;
        this.tonality = other.tonality;
        this.tonality_slope = other.tonality_slope;
        this.noisiness = other.noisiness;
        this.activity = other.activity;
        this.music_prob = other.music_prob;
        this.bandwidth = other.bandwidth;
    }

    void Reset() {
        valid = 0;
        tonality = 0;
        tonality_slope = 0;
        noisiness = 0;
        activity = 0;
        music_prob = 0;
        bandwidth = 0;
    }
}
