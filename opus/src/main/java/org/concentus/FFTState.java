package org.concentus;

class FFTState {

    int nfft = 0;
    short scale = 0;
    int scale_shift = 0;
    int shift = 0;
    short[] factors = new short[2 * KissFFT.MAXFACTORS];
    short[] bitrev = null;
    short[] twiddles = null;
}
