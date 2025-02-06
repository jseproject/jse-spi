package org.concentus;

class ChannelLayout {

    int nb_channels;
    int nb_streams;
    int nb_coupled_streams;
    final short[] mapping = new short[256];

    void Reset() {
        nb_channels = 0;
        nb_streams = 0;
        nb_coupled_streams = 0;
        Arrays.MemSet(mapping, (short) 0);
    }
}
