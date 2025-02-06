package org.concentus;

class VorbisLayout {

    VorbisLayout(int streams, int coupled_streams, short[] map) {
        nb_streams = streams;
        nb_coupled_streams = coupled_streams;
        mapping = map;
    }

    int nb_streams;
    int nb_coupled_streams;
    short[] mapping;

    /* Index is nb_channel-1*/
    static final VorbisLayout[] vorbis_mappings = {
        new VorbisLayout(1, 0, new short[]{0}), /* 1: mono */
        new VorbisLayout(1, 1, new short[]{0, 1}), /* 2: stereo */
        new VorbisLayout(2, 1, new short[]{0, 2, 1}), /* 3: 1-d surround */
        new VorbisLayout(2, 2, new short[]{0, 1, 2, 3}), /* 4: quadraphonic surround */
        new VorbisLayout(3, 2, new short[]{0, 4, 1, 2, 3}), /* 5: 5-channel surround */
        new VorbisLayout(4, 2, new short[]{0, 4, 1, 2, 3, 5}), /* 6: 5.1 surround */
        new VorbisLayout(4, 3, new short[]{0, 4, 1, 2, 3, 5, 6}), /* 7: 6.1 surround */
        new VorbisLayout(5, 3, new short[]{0, 6, 1, 2, 3, 4, 5, 7}), /* 8: 7.1 surround */};
}
