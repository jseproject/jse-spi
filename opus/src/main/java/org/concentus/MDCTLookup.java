package org.concentus;

class MDCTLookup {

    int n = 0;

    int maxshift = 0;

    // [porting note] these are pointers to static states defined in tables.cs
    FFTState[] kfft = new FFTState[4];

    short[] trig = null;

    MDCTLookup() {
    }
}
