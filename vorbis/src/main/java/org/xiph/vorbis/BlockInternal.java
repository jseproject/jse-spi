package org.xiph.vorbis;

class BlockInternal {
    /**
     * this is a pointer into local storage
     */
    float[][] pcmdelay = null;
    float ampmax = 0;
    int blocktype = 0;
    /**
     * initialized, must be freed;<br>
     * blob [PACKETBLOBS/2] points to the oggpack_buffer in the
     * main vorbis_block
     */
    final Buffer packetblob[] =
            new Buffer[Info.PACKETBLOBS];
}
