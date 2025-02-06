package com.beatofthedrum.wv;

class WavPackMetadata
{
    int byte_length;
    byte data[];
    short id;		// was uchar in C
    int hasdata = 0;	// 0 does not have data, 1 has data
    int status = 0;	// 0 ok, 1 error
    long bytecount = 24;// we use this to determine if we have read all the metadata 
                      	// in a block by checking bytecount again the block length
                   	// ckSize is block size minus 8. WavPack header is 32 bytes long so we start at 24
    byte[] temp_data = new byte[64];
}