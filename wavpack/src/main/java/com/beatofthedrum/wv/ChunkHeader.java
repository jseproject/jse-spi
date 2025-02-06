package com.beatofthedrum.wv;

class ChunkHeader
{
    char[] ckID = new char[4];
    long ckSize; // was uint32_t in C
}
