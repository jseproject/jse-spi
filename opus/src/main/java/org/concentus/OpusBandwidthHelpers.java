package org.concentus;

// Helpers to port over uses of OpusBandwidth as an integer
class OpusBandwidthHelpers {

    static int GetOrdinal(OpusBandwidth bw) {
        switch (bw) {
            case OPUS_BANDWIDTH_NARROWBAND:
                return 1;
            case OPUS_BANDWIDTH_MEDIUMBAND:
                return 2;
            case OPUS_BANDWIDTH_WIDEBAND:
                return 3;
            case OPUS_BANDWIDTH_SUPERWIDEBAND:
                return 4;
            case OPUS_BANDWIDTH_FULLBAND:
                return 5;
        }

        return -1;
    }

    static OpusBandwidth GetBandwidth(int ordinal) {
        switch (ordinal) {
            case 1:
                return OpusBandwidth.OPUS_BANDWIDTH_NARROWBAND;
            case 2:
                return OpusBandwidth.OPUS_BANDWIDTH_MEDIUMBAND;
            case 3:
                return OpusBandwidth.OPUS_BANDWIDTH_WIDEBAND;
            case 4:
                return OpusBandwidth.OPUS_BANDWIDTH_SUPERWIDEBAND;
            case 5:
                return OpusBandwidth.OPUS_BANDWIDTH_FULLBAND;
        }

        return OpusBandwidth.OPUS_BANDWIDTH_AUTO;
    }

    static OpusBandwidth MIN(OpusBandwidth a, OpusBandwidth b) {
        if (GetOrdinal(a) < GetOrdinal(b)) {
            return a;
        }
        return b;
    }

    static OpusBandwidth MAX(OpusBandwidth a, OpusBandwidth b) {
        if (GetOrdinal(a) > GetOrdinal(b)) {
            return a;
        }
        return b;
    }

    static OpusBandwidth SUBTRACT(OpusBandwidth a, int b) {
        return GetBandwidth(GetOrdinal(a) - b);
    }
}
