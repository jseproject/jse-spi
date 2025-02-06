package org.xiph.vorbis;

/**
 * mode
 **/
class InfoMode {
    int blockflag = 0;
    int windowtype = 0;
    int transformtype = 0;
    int mapping = 0;

    //
    InfoMode() {
    }

    InfoMode(int i_blockflag, int i_windowtype,
             int i_transformtype, int i_mapping) {
        blockflag = i_blockflag;
        windowtype = i_windowtype;
        transformtype = i_transformtype;
        mapping = i_mapping;
    }

    InfoMode(InfoMode m) {
        blockflag = m.blockflag;
        windowtype = m.windowtype;
        transformtype = m.transformtype;
        mapping = m.mapping;
    }
}
