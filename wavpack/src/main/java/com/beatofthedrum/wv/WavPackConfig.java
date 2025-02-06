package com.beatofthedrum.wv;

public class WavPackConfig
{
    public int bits_per_sample, bytes_per_sample;
    public int num_channels;
    int float_norm_exp;
    public long flags, sample_rate;	// was uint32_t in C
    long channel_mask;	            // was uint32_t in C
    int bitrate;
    int shaping_weight;
    int block_samples;
}