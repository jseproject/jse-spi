package org.xiph.vorbis;

class PrivateState {
    /* local lookup storage */
    /**
     * envelope lookup
     */
    EnvelopeLookup ve = null;
    final int[] window = new int[2];
    /**
     * block, type
     */
    final MDCTLookup[][] transform = new MDCTLookup[2][];
    final DRFTLookup[] fft_look =
            new DRFTLookup[]{new DRFTLookup(), new DRFTLookup()};

    int modebits = 0;
    LookFloor[] flr = null;
    LookResidue[] residue = null;
    LookPsy[] psy = null;
    LookPsyGlobal psy_g_look = null;

    /* local storage, only used on the encoding side.  This way the
     application does not need to worry about freeing some packets'
     memory and not others'; packet storage is always tracked.
     Cleared next call to a _dsp_ function */
    byte[] header = null;
    byte[] header1 = null;
    byte[] header2 = null;

    final BitrateManagerState bms = new BitrateManagerState();

    long sample_count = 0;
}
