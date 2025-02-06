package org.xiph.vorbis;

/**
 * for fft transform
 */
class DRFTLookup {
    int n = 0;
    float[] trigcache = null;
    int[] splitcache = null;

    //
    final void clear() {
        //if( l != null ) {
        trigcache = null;
        splitcache = null;
        n = 0;
        //}
    }
}
