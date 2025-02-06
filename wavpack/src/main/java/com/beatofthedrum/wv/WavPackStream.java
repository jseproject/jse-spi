package com.beatofthedrum.wv;

class WavPackStream
{
    WavPackHeader wphdr = new WavPackHeader();
    Bitstream wvbits = new Bitstream();

    WordsData w = new WordsData();

    int num_terms = 0;
    int mute_error;
    long sample_index, crc;	// was uint32_t in C

    short int32_sent_bits, int32_zeros, int32_ones, int32_dups;		// was uchar in C
    short float_flags, float_shift, float_max_exp, float_norm_exp;	// was uchar in C
 
    DecorrPass dp1 = new DecorrPass();
    DecorrPass dp2 = new DecorrPass();
    DecorrPass dp3 = new DecorrPass();
    DecorrPass dp4 = new DecorrPass();
    DecorrPass dp5 = new DecorrPass();
    DecorrPass dp6 = new DecorrPass();
    DecorrPass dp7 = new DecorrPass();
    DecorrPass dp8 = new DecorrPass();
    DecorrPass dp9 = new DecorrPass();
    DecorrPass dp10 = new DecorrPass();
    DecorrPass dp11 = new DecorrPass();
    DecorrPass dp12 = new DecorrPass();
    DecorrPass dp13 = new DecorrPass();
    DecorrPass dp14 = new DecorrPass();
    DecorrPass dp15 = new DecorrPass();
    DecorrPass dp16 = new DecorrPass();

    DecorrPass decorr_passes[] = { dp1, dp2, dp3, dp4, dp5, dp6, dp7, dp8, dp9, dp10, dp11, dp12, dp13, dp14, dp15, dp16 };

    Bitstream wvcbits = new Bitstream();
    DeltaData dc = new DeltaData();
    byte[] blockbuff = new byte[Defines.BIT_BUFFER_SIZE + 1];
    int blockend = Defines.BIT_BUFFER_SIZE;
    byte[] block2buff = new byte[Defines.BIT_BUFFER_SIZE + 1];
    int block2end = Defines.BIT_BUFFER_SIZE;
    int bits;
    int lossy_block;

}