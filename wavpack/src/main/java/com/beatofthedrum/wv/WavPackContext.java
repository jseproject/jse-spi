package com.beatofthedrum.wv;

public class WavPackContext
{
    WavPackConfig config = new WavPackConfig();
    WavPackStream stream = new WavPackStream();

   
    byte read_buffer[] = new byte[1024];	// was uchar in C
    public String error_message = "";
    public boolean error;

    java.io.DataInputStream infile;
    long total_samples, crc_errors, first_flags;		// was uint32_t in C
    int open_flags, norm_offset;
    int reduced_channels = 0;
    int lossy_blocks;
    int status = 0;	// 0 ok, 1 error

    java.io.OutputStream outfile;
    java.io.OutputStream correction_outfile;
    int wvc_flag;
    long block_samples;
    long acc_samples;
    long filelen;
    long file2len;
    short stream_version;
    public int byte_idx = 0; // holds the current buffer position for the input WAV data
}