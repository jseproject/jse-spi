package com.beatofthedrum.wv;

class RiffChunkHeader
{
    char[] ckID = new char[4];
    long ckSize; // was uint32_t in C
    char[] formType = new char[4];
}
