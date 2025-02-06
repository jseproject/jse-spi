package com.beatofthedrum.wv;

class Bitstream
{
    int end, ptr;	// was uchar in c
    long file_bytes, sr;	// was uint32_t in C
    int error, bc;
    java.io.DataInputStream file;
    int bitval = 0;
    byte[] buf = new byte[1024];
    int buf_index = 0;
    int start_index = 0;
    int active = 0; // if 0 then this bitstream is not being used
}