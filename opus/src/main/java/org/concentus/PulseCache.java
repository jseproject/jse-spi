package org.concentus;

class PulseCache {

    int size = 0;
    short[] index = null;
    short[] bits = null;
    short[] caps = null;

    void Reset() {
        size = 0;
        index = null;
        bits = null;
        caps = null;
    }
}
