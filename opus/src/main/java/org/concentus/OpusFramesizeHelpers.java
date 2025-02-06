package org.concentus;

class OpusFramesizeHelpers {

    static int GetOrdinal(OpusFramesize size) {
        switch (size) {
            case OPUS_FRAMESIZE_ARG:
                return 1;
            case OPUS_FRAMESIZE_2_5_MS:
                return 2;
            case OPUS_FRAMESIZE_5_MS:
                return 3;
            case OPUS_FRAMESIZE_10_MS:
                return 4;
            case OPUS_FRAMESIZE_20_MS:
                return 5;
            case OPUS_FRAMESIZE_40_MS:
                return 6;
            case OPUS_FRAMESIZE_60_MS:
                return 7;
            case OPUS_FRAMESIZE_VARIABLE:
                return 8;
        }

        return -1;
    }
}
