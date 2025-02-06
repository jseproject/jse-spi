package org.concentus;

class StereoWidthState {

    int XX;
    int XY;
    int YY;
    int smoothed_width;
    int max_follower;

    void Reset() {
        XX = 0;
        XY = 0;
        YY = 0;
        smoothed_width = 0;
        max_follower = 0;
    }
}
