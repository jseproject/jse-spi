package org.concentus;

/// <summary>
/// Noise shaping analysis state
/// </summary>
class SilkShapeState {

    byte LastGainIndex = 0;
    int HarmBoost_smth_Q16 = 0;
    int HarmShapeGain_smth_Q16 = 0;
    int Tilt_smth_Q16 = 0;

    void Reset() {
        LastGainIndex = 0;
        HarmBoost_smth_Q16 = 0;
        HarmShapeGain_smth_Q16 = 0;
        Tilt_smth_Q16 = 0;
    }
}
