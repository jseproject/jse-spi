package com.beatofthedrum.wv;

class WordsData
{
    long bitrate_delta[] = new long[2]; // was uint32_t  in C
    long bitrate_acc[] = new long[2]; // was uint32_t  in C
    long pend_data, holding_one, zeros_acc;  // was uint32_t  in C
    int holding_zero, pend_count;

    EntropyData temp_ed1 = new EntropyData();
    EntropyData temp_ed2 = new EntropyData();
    EntropyData c[] = {temp_ed1 , temp_ed2 };

    long[][] median = new long[3][2]; // was uint32_t  in C
    long[] slow_level = new long[2]; // was uint32_t  in C
    long[] error_limit = new long[2]; // was uint32_t  in C

}