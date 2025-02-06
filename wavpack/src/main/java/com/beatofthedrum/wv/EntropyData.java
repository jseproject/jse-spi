package com.beatofthedrum.wv;

class EntropyData
{
    long slow_level;
    long median[] = {0,0,0};	// was uint32_t in C, we initialize in order to remove run time errors
    long error_limit; // was uint32_t in C
}