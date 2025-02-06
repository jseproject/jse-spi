package com.beatofthedrum.wv;

public class WavPackUtils
{


    ///////////////////////////// local table storage ////////////////////////////

    static long sample_rates [] =
    {
        6000, 8000, 9600, 11025, 12000, 16000, 22050, 24000, 32000, 44100, 48000, 64000, 88200, 96000, 192000
    };

    ///////////////////////////// executable code ////////////////////////////////


    // This function reads data from the specified stream in search of a valid
    // WavPack 4.0 audio block. If this fails in 1 megabyte (or an invalid or
    // unsupported WavPack block is encountered) then an appropriate message is
    // copied to "error" and NULL is returned, otherwise a pointer to a
    // WavPackContext structure is returned (which is used to call all other
    // functions in this module). This can be initiated at the beginning of a
    // WavPack file, or anywhere inside a WavPack file. To determine the exact
    // position within the file use GetSampleIndex().  Also,
    // this function will not handle "correction" files, plays only the first
    // two channels of multi-channel files, and is limited in resolution in some
    // large integer or floating point files (but always provides at least 24 bits
    // of resolution).

    public static WavPackContext OpenFileInput(java.io.DataInputStream infile)
    {
        WavPackContext wpc = new WavPackContext();
        WavPackStream wps = wpc.stream;

        wpc.infile = infile;
        wpc.total_samples = -1;
        wpc.norm_offset = 0;
        wpc.open_flags = 0;


        // open the source file for reading and store the size

        while (wps.wphdr.block_samples == 0)
        {

            wps.wphdr = read_next_header(wpc.infile, wps.wphdr);

            if (wps.wphdr.status == 1)
            {
                wpc.error_message = "not compatible with this version of WavPack file!";
                wpc.error = true;
                return (wpc);
            }

            if (wps.wphdr.block_samples > 0 && wps.wphdr.total_samples != -1)
            {
                wpc.total_samples = wps.wphdr.total_samples;
            }

            // lets put the stream back in the context

            wpc.stream = wps;

            if ((UnpackUtils.unpack_init(wpc)) == Defines.FALSE)
            {
                wpc.error = true;
                return wpc;
            }
        } // end of while

        wpc.config.flags = wpc.config.flags & ~0xff;
        wpc.config.flags = wpc.config.flags | (wps.wphdr.flags & 0xff);

        wpc.config.bytes_per_sample = (int) ((wps.wphdr.flags & Defines.BYTES_STORED) + 1);
        wpc.config.float_norm_exp = wps.float_norm_exp;

        wpc.config.bits_per_sample = (int) ((wpc.config.bytes_per_sample * 8)
            - ((wps.wphdr.flags & Defines.SHIFT_MASK) >> Defines.SHIFT_LSB));

        if ((wpc.config.flags & Defines.FLOAT_DATA) > 0)
        {
            wpc.config.bytes_per_sample = 3;
            wpc.config.bits_per_sample = 24;
        }

        if (wpc.config.sample_rate == 0)
        {
            if (wps.wphdr.block_samples == 0 || (wps.wphdr.flags & Defines.SRATE_MASK) == Defines.SRATE_MASK)
                wpc.config.sample_rate = 44100;
            else
                wpc.config.sample_rate = sample_rates[(int) ((wps.wphdr.flags & Defines.SRATE_MASK)
                    >> Defines.SRATE_LSB)];
        }

        if (wpc.config.num_channels == 0)
        {
            if ((wps.wphdr.flags & Defines.MONO_FLAG) > 0)
            {
                wpc.config.num_channels = 1;
            }
            else
            {
                wpc.config.num_channels = 2;
            }

            wpc.config.channel_mask = 0x5 - wpc.config.num_channels;
        }

        if ((wps.wphdr.flags & Defines.FINAL_BLOCK) == 0)
        {
            if ((wps.wphdr.flags & Defines.MONO_FLAG) != 0)
            {
                wpc.reduced_channels = 1;
            }
            else
            {
                wpc.reduced_channels = 2;
            }
        }

        return wpc;
    }

    // This function obtains general information about an open file and returns
    // a mask with the following bit values:

    // MODE_LOSSLESS:  file is lossless (pure lossless only)
    // MODE_HYBRID:  file is hybrid mode (lossy part only)
    // MODE_FLOAT:  audio data is 32-bit ieee floating point (but will provided
    //               in 24-bit integers for convenience)
    // MODE_HIGH:  file was created in "high" mode (information only)
    // MODE_FAST:  file was created in "fast" mode (information only)

    
    static int GetMode (WavPackContext wpc)
    {
        int mode = 0;
    
        if (null != wpc) 
        {
            if ( (wpc.config.flags & Defines.CONFIG_HYBRID_FLAG) != 0)
                mode |= Defines.MODE_HYBRID;
            else if ((wpc.config.flags & Defines.CONFIG_LOSSY_MODE)==0)
                mode |= Defines.MODE_LOSSLESS;
    
            if (wpc.lossy_blocks != 0)
                mode &= ~Defines.MODE_LOSSLESS;
    
            if ( (wpc.config.flags & Defines.CONFIG_FLOAT_DATA) != 0)
                mode |= Defines.MODE_FLOAT;
    
            if ( (wpc.config.flags & Defines.CONFIG_HIGH_FLAG) != 0)
                mode |= Defines.MODE_HIGH;
    
            if ( (wpc.config.flags & Defines.CONFIG_FAST_FLAG) != 0)
                mode |= Defines.MODE_FAST;
        }
    
        return mode;
    }
    

    // Unpack the specified number of samples from the current file position.
    // Note that "samples" here refers to "complete" samples, which would be
    // 2 longs for stereo files. The audio data is returned right-justified in
    // 32-bit longs in the endian mode native to the executing processor. So,
    // if the original data was 16-bit, then the values returned would be
    // +/-32k. Floating point data will be returned as 24-bit integers (and may
    // also be clipped). The actual number of samples unpacked is returned,
    // which should be equal to the number requested unless the end of fle is
    // encountered or an error occurs.

    public static long UnpackSamples(WavPackContext wpc, int [] buffer, long samples)
    {
        WavPackStream wps = wpc.stream;
        long samples_unpacked = 0, samples_to_unpack;
        int num_channels = wpc.config.num_channels;
        int bcounter = 0;

        int [] temp_buffer = new int[Defines.SAMPLE_BUFFER_SIZE];
        int buf_idx = 0;
        int bytes_returned = 0;

        while (samples > 0)
        {
            if (wps.wphdr.block_samples == 0 || (wps.wphdr.flags & Defines.INITIAL_BLOCK) == 0
                || wps.sample_index >= wps.wphdr.block_index
                + wps.wphdr.block_samples)
            {

                wps.wphdr = read_next_header(wpc.infile, wps.wphdr);

                if (wps.wphdr.status == 1)
                    break;

                if (wps.wphdr.block_samples == 0 || wps.sample_index == wps.wphdr.block_index)
                {
                    if ((UnpackUtils.unpack_init(wpc)) == Defines.FALSE)
                        break;
                }
            }

            if (wps.wphdr.block_samples == 0 || (wps.wphdr.flags & Defines.INITIAL_BLOCK) == 0
                || wps.sample_index >= wps.wphdr.block_index
                + wps.wphdr.block_samples)
                continue;

            if (wps.sample_index < wps.wphdr.block_index)
            {
                samples_to_unpack = wps.wphdr.block_index - wps.sample_index;

                if (samples_to_unpack > samples)
                    samples_to_unpack = samples;

                wps.sample_index += samples_to_unpack;
                samples_unpacked += samples_to_unpack;
                samples -= samples_to_unpack;

                if (wpc.reduced_channels > 0)
                    samples_to_unpack *= wpc.reduced_channels;
                else
                    samples_to_unpack *= num_channels;

                while (samples_to_unpack > 0)
                {
                    temp_buffer[bcounter] = 0;
                    bcounter++;
                    samples_to_unpack--;
                }

                continue;
            }

            samples_to_unpack = wps.wphdr.block_index + wps.wphdr.block_samples - wps.sample_index;

            if (samples_to_unpack > samples)
                samples_to_unpack = samples;

            UnpackUtils.unpack_samples(wpc, temp_buffer, samples_to_unpack);

            if (wpc.reduced_channels > 0)
                bytes_returned = (int) (samples_to_unpack * wpc.reduced_channels);
            else
                bytes_returned = (int) (samples_to_unpack * num_channels);

            System.arraycopy(temp_buffer, 0, buffer, buf_idx, bytes_returned);

            buf_idx += bytes_returned;

            samples_unpacked += samples_to_unpack;
            samples -= samples_to_unpack;

            if (wps.sample_index == wps.wphdr.block_index + wps.wphdr.block_samples)
            {
                if (UnpackUtils.check_crc_error(wpc) > 0)
                    wpc.crc_errors++;
            }

            if (wps.sample_index == wpc.total_samples)
                break;
        }

        return (samples_unpacked);
    }


    // Get total number of samples contained in the WavPack file, or -1 if unknown

    public static long GetNumSamples(WavPackContext wpc)
    {
        // -1 would mean an unknown number of samples

        if( null != wpc)
        {
            return (wpc.total_samples);
        }
        else
        {
            return (long) -1;
        }
    }


    // Get the current sample index position, or -1 if unknown
 
    public static long GetSampleIndex (WavPackContext wpc)
    {
        if (null != wpc)
            return wpc.stream.sample_index;
    
        return (long) -1;
    }
    


    // Get the number of errors encountered so far

    public static long GetNumErrors(WavPackContext wpc)
    {
        if( null != wpc)
        {
            return wpc.crc_errors;
        }
        else
        {
            return (long)0;
        }
    }


    // return if any uncorrected lossy blocks were actually written or read

    
    public static int LossyBlocks (WavPackContext wpc)
    {
        if(null != wpc)
        {
             return wpc.lossy_blocks;
        }
        else
        {
            return 0;
        }
    }
    


    // Returns the sample rate of the specified WavPack file

    public static long GetSampleRate(WavPackContext wpc)
    {
        if ( null != wpc && wpc.config.sample_rate != 0)
        {
            return wpc.config.sample_rate;
        }
        else
        {
            return (long) 44100;
        }
    }


    // Returns the number of channels of the specified WavPack file. Note that
    // this is the actual number of channels contained in the file, but this
    // version can only decode the first two.

    public static int GetNumChannels(WavPackContext wpc)
    {
        if ( null != wpc && wpc.config.num_channels != 0)
        {
            return wpc.config.num_channels;
        }
        else
        {
            return 2;
        }
    }


    // Returns the actual number of valid bits per sample contained in the
    // original file, which may or may not be a multiple of 8. Floating data
    // always has 32 bits, integers may be from 1 to 32 bits each. When this
    // value is not a multiple of 8, then the "extra" bits are located in the
    // LSBs of the results. That is, values are right justified when unpacked
    // into longs, but are left justified in the number of bytes used by the
    // original data.

    public static int GetBitsPerSample(WavPackContext wpc)
    {
        if (null != wpc && wpc.config.bits_per_sample != 0)
        {
            return wpc.config.bits_per_sample;
        }
        else
        {
            return 16;
        }
    }


    // Returns the number of bytes used for each sample (1 to 4) in the original
    // file. This is required information for the user of this module because the
    // audio data is returned in the LOWER bytes of the long buffer and must be
    // left-shifted 8, 16, or 24 bits if normalized longs are required.

    public static int GetBytesPerSample(WavPackContext wpc)
    {
        if ( null != wpc && wpc.config.bytes_per_sample != 0)
        {
            return wpc.config.bytes_per_sample;
        }
        else
        {
            return 2;
        }
    }


    // This function will return the actual number of channels decoded from the
    // file (which may or may not be less than the actual number of channels, but
    // will always be 1 or 2). Normally, this will be the front left and right
    // channels of a multi-channel file.

    public static int GetReducedChannels(WavPackContext wpc)
    {
        if (null != wpc && wpc.reduced_channels != 0)
        {
            return wpc.reduced_channels;
        }
        else if (null != wpc && wpc.config.num_channels != 0)
        {
            return wpc.config.num_channels;
        }
        else
        {
            return 2;
        }
    }

    // Read from current file position until a valid 32-byte WavPack 4.0 header is
    // found and read into the specified pointer. If no WavPack header is found within 1 meg,
    // then an error is returned. No additional bytes are read past the header. 

    static WavPackHeader read_next_header(java.io.DataInputStream infile, WavPackHeader wphdr)
    {
        byte buffer [] = new byte[32]; // 32 is the size of a WavPack Header
        byte temp [] = new byte[32];

        long bytes_skipped = 0;
        int bleft = 0; // bytes left in buffer
        int counter = 0;
        int i = 0;

        while (true)
        {
            for (i = 0; i < bleft; i++)
            {
                buffer[i] = buffer[32 - bleft + i];
            }

            counter = 0;

            try
            {
                if (infile.read(temp, 0, 32 - bleft) != 32 - bleft)
                {
                    wphdr.status = 1;
                    return wphdr;
                }
            }
            catch (Exception e)
            {
                wphdr.status = 1;
                return wphdr;
            }

            for (i = 0; i < 32 - bleft; i++)
            {
                buffer[bleft + i] = temp[i];
            }

            bleft = 32;

            if (buffer[0] == 'w' && buffer[1] == 'v' && buffer[2] == 'p' && buffer[3] == 'k'
                && (buffer[4] & 1) == 0 && buffer[6] < 16 && buffer[7] == 0 && buffer[9] == 4
                && buffer[8] >= (Defines.MIN_STREAM_VERS & 0xff) && buffer[8] <= (Defines.MAX_STREAM_VERS & 0xff))
            {

                wphdr.ckID[0] = 'w';
                wphdr.ckID[1] = 'v';
                wphdr.ckID[2] = 'p';
                wphdr.ckID[3] = 'k';

                wphdr.ckSize = (long) ((buffer[7] & 0xFF) << 24);
                wphdr.ckSize += (long) ((buffer[6] & 0xFF) << 16);
                wphdr.ckSize += (long) ((buffer[5] & 0xFF) << 8);
                wphdr.ckSize += (long) (buffer[4] & 0xFF);

                wphdr.version = (short) (buffer[9] << 8);
                wphdr.version += (short) (buffer[8]);

                wphdr.track_no = buffer[10];
                wphdr.index_no = buffer[11];

                wphdr.total_samples = (long) ((buffer[15] & 0xFF) << 24);
                wphdr.total_samples += (long) ((buffer[14] & 0xFF) << 16);
                wphdr.total_samples += (long) ((buffer[13] & 0xFF) << 8);
                wphdr.total_samples += (long) (buffer[12] & 0xFF);

                wphdr.block_index = (long) ((buffer[19] & 0xFF) << 24);
                wphdr.block_index += (long) ((buffer[18] & 0xFF) << 16);
                wphdr.block_index += (long) ((buffer[17] & 0xFF) << 8);
                wphdr.block_index += (long) (buffer[16]) & 0XFF;

                wphdr.block_samples = (long) ((buffer[23] & 0xFF) << 24);
                wphdr.block_samples += (long) ((buffer[22] & 0xFF) << 16);
                wphdr.block_samples += (long) ((buffer[21] & 0xFF) << 8);
                wphdr.block_samples += (long) (buffer[20] & 0XFF);

                wphdr.flags = (long) ((buffer[27] & 0xFF) << 24);
                wphdr.flags += (long) ((buffer[26] & 0xFF) << 16);
                wphdr.flags += (long) ((buffer[25] & 0xFF) << 8);
                wphdr.flags += (long) (buffer[24] & 0xFF);

                wphdr.crc = (long) ((buffer[31] & 0xFF) << 24);
                wphdr.crc += (long) ((buffer[30] & 0xFF) << 16);
                wphdr.crc += (long) ((buffer[29] & 0xFF) << 8);
                wphdr.crc += (long) (buffer[28] & 0xFF);

                wphdr.status = 0;

                return wphdr;
            }
            else
            {
                counter++;
                bleft--;
            }

            while (bleft > 0 && buffer[counter] != 'w')
            {
                counter++;
                bleft--;
            }

            bytes_skipped = bytes_skipped + counter;

            if (bytes_skipped > 1048576L)
            {
                wphdr.status = 1;
                return wphdr;
            }
        }
    }

    // This function returns a pointer to a string describing the last error
    // generated by WavPack.
    /*
    static String GetErrorMessage(WavPackContext wpc)
    {
        return wpc.error_message;
    }
     */

    // Set configuration for writing WavPack files. This must be done before
    // sending any actual samples. The "config" structure contains the following
    // required information:
    // config.bytes_per_sample     see GetBytesPerSample() for info
    // config.bits_per_sample      see GetBitsPerSample() for info
    // config.num_channels         self evident
    // config.sample_rate          self evident
    // In addition, the following fields and flags may be set:
    // config->flags:
    // --------------
    // o CONFIG_HYBRID_FLAG         select hybrid mode (must set bitrate)
    // o CONFIG_JOINT_STEREO        select joint stereo (must set override also)
    // o CONFIG_JOINT_OVERRIDE      override default joint stereo selection
    // o CONFIG_HYBRID_SHAPE        select hybrid noise shaping (set override &
    //                                                      shaping_weight != 0)
    // o CONFIG_SHAPE_OVERRIDE      override default hybrid noise shaping
    //                               (set CONFIG_HYBRID_SHAPE and shaping_weight)
    // o CONFIG_FAST_FLAG           "fast" compression mode
    // o CONFIG_HIGH_FLAG           "high" compression mode
    // o CONFIG_VERY_HIGH_FLAG      "very high" compression mode
    // o CONFIG_CREATE_WVC          create correction file
    // o CONFIG_OPTIMIZE_WVC        maximize bybrid compression (-cc option)
    // config->bitrate              hybrid bitrate in bits/sample (scaled up 2^8)
    // config->shaping_weight       hybrid noise shaping coefficient (scaled up 2^10)
    // config->block_samples        force samples per WavPack block (0 = use deflt)
    // If the number of samples to be written is known then it should be passed
    // here. If the duration is not known then pass -1. In the case that the size
    // is not known (or the writing is terminated early) then it is suggested that
    // the application retrieve the first block written and let the library update
    // the total samples indication. A function is provided to do this update and
    // it should be done to the "correction" file also. If this cannot be done
    // (because a pipe is being used, for instance) then a valid WavPack will still
    // be created, but when applications want to access that file they will have
    // to seek all the way to the end to determine the actual duration. A return of
    // FALSE indicates an error.
    public static int SetConfiguration(WavPackContext wpc, WavPackConfig config, long total_samples)
    {
        long flags = (config.bytes_per_sample - 1);
        WavPackStream wps = wpc.stream;
        int bps = 0;
        int shift;
        int i;

        if (config.num_channels > 2)
        {
            wpc.error_message = "too many channels!";

            return Defines.FALSE;
        }

        wpc.total_samples = total_samples;
        wpc.config.sample_rate = config.sample_rate;
        wpc.config.num_channels = config.num_channels;
        wpc.config.bits_per_sample = config.bits_per_sample;
        wpc.config.bytes_per_sample = config.bytes_per_sample;
        wpc.config.block_samples = config.block_samples;
        wpc.config.flags = config.flags;

        if ((wpc.config.flags & Defines.CONFIG_VERY_HIGH_FLAG) > 0)
        {
            wpc.config.flags |= Defines.CONFIG_HIGH_FLAG;
        }

        shift = (config.bytes_per_sample * 8) - config.bits_per_sample;

        for (i = 0; i < 15; ++i)
        {
            if (wpc.config.sample_rate == sample_rates[i])
            {
                break;
            }
        }

        flags |= (i << Defines.SRATE_LSB);
        flags |= (shift << Defines.SHIFT_LSB);

        if ((config.flags & Defines.CONFIG_HYBRID_FLAG) != 0)
        {
            flags |= (Defines.HYBRID_FLAG | Defines.HYBRID_BITRATE | Defines.HYBRID_BALANCE);

            if (((wpc.config.flags & Defines.CONFIG_SHAPE_OVERRIDE) != 0) &&
                    ((wpc.config.flags & Defines.CONFIG_HYBRID_SHAPE) != 0) &&
                    (config.shaping_weight != 0))
            {
                wpc.config.shaping_weight = config.shaping_weight;
                flags |= (Defines.HYBRID_SHAPE | Defines.NEW_SHAPING);
            }

            if ((wpc.config.flags & Defines.CONFIG_OPTIMIZE_WVC) != 0)
            {
                flags |= Defines.CROSS_DECORR;
            }

            bps = config.bitrate;
        }
        else
        {
            flags |= Defines.CROSS_DECORR;
        }

        if (((config.flags & Defines.CONFIG_JOINT_OVERRIDE) == 0) ||
                ((config.flags & Defines.CONFIG_JOINT_STEREO) != 0))
        {
            flags |= Defines.JOINT_STEREO;
        }

        if ((config.flags & Defines.CONFIG_CREATE_WVC) != 0)
        {
            wpc.wvc_flag = Defines.TRUE;
        }

        wpc.stream_version = Defines.CUR_STREAM_VERS;

        wps.wphdr.ckID[0] = 'w';
        wps.wphdr.ckID[1] = 'v';
        wps.wphdr.ckID[2] = 'p';
        wps.wphdr.ckID[3] = 'k';

        // 32 is the size of the WavPack header
        wps.wphdr.ckSize = 32 - 8;
        wps.wphdr.total_samples = wpc.total_samples;
        wps.wphdr.version = wpc.stream_version;
        wps.wphdr.flags = flags | Defines.INITIAL_BLOCK | Defines.FINAL_BLOCK;
        wps.bits = bps;

        if (config.num_channels == 1)
        {
            wps.wphdr.flags &= ~(Defines.JOINT_STEREO | Defines.CROSS_DECORR |
                    Defines.HYBRID_BALANCE);
            wps.wphdr.flags |= Defines.MONO_FLAG;
        }

        return Defines.TRUE;
    }

    // Prepare to actually pack samples by determining the size of the WavPack
    // blocks and initializing the stream. Call after SetConfiguration()
    // and before PackSamples(). A return of FALSE indicates an error.
    public static int PackInit(WavPackContext wpc)
    {
        if (wpc.config.block_samples > 0)
        {
            wpc.block_samples = wpc.config.block_samples;
        }
        else
        {
            if ((wpc.config.flags & Defines.CONFIG_HIGH_FLAG) > 0)
            {
                wpc.block_samples = wpc.config.sample_rate;
            }
            else if ((wpc.config.sample_rate % 2) == 0)
            {
                wpc.block_samples = wpc.config.sample_rate / 2;
            }
            else
            {
                wpc.block_samples = wpc.config.sample_rate;
            }

            while ((wpc.block_samples * wpc.config.num_channels) > 150000)
            {
                wpc.block_samples /= 2;
            }

            while ((wpc.block_samples * wpc.config.num_channels) < 40000)
            {
                wpc.block_samples *= 2;
            }
        }

        PackUtils.pack_init(wpc);

        return Defines.TRUE;
    }

    // Pack the specified samples. Samples must be stored in longs in the native
    // endian format of the executing processor. The number of samples specified
    // indicates composite samples (sometimes called "frames"). So, the actual
    // number of data points would be this "sample_count" times the number of
    // channels. Note that samples are immediately packed into the block(s)
    // currently being built. If the predetermined number of sample per block
    // is reached, or the block being built is approaching overflow, then the
    // block will be completed and written. If an application wants to break a
    // block at a specific sample, then it must simply call FlushSamples()
    // to force an early termination. Completed WavPack blocks are send to the
    // function provided in the initial call to OpenFileOutput(). A
    // return of FALSE indicates an error.
    public static int PackSamples(WavPackContext wpc, long[] sample_buffer, long sample_count)
    {
        WavPackStream wps = wpc.stream;
        long flags = wps.wphdr.flags;

        if ((flags & Defines.SHIFT_MASK) != 0)
        {
            int shift = (int) ((flags & Defines.SHIFT_MASK) >> Defines.SHIFT_LSB);
            long[] ptr = sample_buffer;
            long cnt = sample_count;
            int ptrIndex = 0;

            if ((flags & (Defines.MONO_FLAG | Defines.FALSE_STEREO)) != 0)
            {
                while (cnt > 0)
                {
                    ptr[ptrIndex] = ptr[ptrIndex] >>> shift; // was >>
                    ptrIndex++;
                    cnt--;
                }
            }
            else
            {
                while (cnt > 0)
                {
                    ptr[ptrIndex] = ptr[ptrIndex] >>> shift; // was >>
                    ptrIndex++;
                    ptr[ptrIndex] = ptr[ptrIndex] >>> shift; // was >>
                    ptrIndex++;
                    cnt--;
                }
            }
        }

        while (sample_count > 0)
        {
            long samples_to_pack;
            long samples_packed;

            if (wpc.acc_samples == 0)
            {
                flags &= ~Defines.MAG_MASK;
                flags += ((1L << Defines.MAG_LSB) * (((flags & Defines.BYTES_STORED) * 8) + 7));

                wps.wphdr.block_index = wps.sample_index;
                wps.wphdr.flags = flags;
                PackUtils.pack_start_block(wpc);
            }

            if ((wpc.acc_samples + sample_count) > wpc.block_samples)
            {
                samples_to_pack = wpc.block_samples - wpc.acc_samples;
            }
            else
            {
                samples_to_pack = sample_count;
            }

            samples_packed = PackUtils.pack_samples(wpc, sample_buffer, samples_to_pack);
            sample_count -= samples_packed;

            if (((wpc.acc_samples += samples_packed) == wpc.block_samples) ||
                    (samples_packed != samples_to_pack))
            {
                if (finish_block(wpc) == 0)
                {
                    return Defines.FALSE;
                }
            }
        }

        return Defines.TRUE;
    }

    // Flush all accumulated samples into WavPack blocks. This is normally called
    // after all samples have been sent to PackSamples(), but can also be
    // called to terminate a WavPack block at a specific sample (in other words it
    // is possible to continue after this operation). A return of FALSE indicates
    // an error.
    public static int FlushSamples(WavPackContext wpc)
    {
        if ((wpc.acc_samples != 0) && (finish_block(wpc) == 0))
        {
            return Defines.FALSE;
        }

        return Defines.TRUE;
    }

    static int finish_block(WavPackContext wpc)
    {
        WavPackStream wps = wpc.stream;
        long bcount;
        int result = 0;

        result = PackUtils.pack_finish_block(wpc);

        wpc.acc_samples = 0;

        if (result == 0)
        {
            wpc.error_message = "output buffer overflowed!";

            return result;
        }

        bcount = (wps.blockbuff[4] & 0xff) + ((wps.blockbuff[5] & 0xff) << 8) +
                ((wps.blockbuff[6] & 0xff) << 16) + ((wps.blockbuff[7] & 0xff) << 24) + 8;

        try
        {
            wpc.outfile.write(wps.blockbuff, 0, (int) bcount);
        }
        catch (Exception e)
        {
            result = Defines.FALSE;
        }

        if (result == 0)
        {
            wpc.error_message = "can't write WavPack data, disk probably full!";

            return result;
        }

        wpc.filelen += bcount;

        if (wps.block2buff[0] == 'w') // if starts with w then has a WavPack header i.e. it is defined
        {
            bcount = (wps.block2buff[4] & 0xff) + ((wps.block2buff[5] & 0xff) << 8) +
                    ((wps.block2buff[6] & 0xff) << 16) + ((wps.block2buff[7] & 0xff) << 24) + 8;

            try
            {
                wpc.correction_outfile.write(wps.block2buff, 0, (int) bcount);
            }
            catch (Exception e)
            {
                result = Defines.FALSE;
            }

            if (result == 0)
            {
                wpc.error_message = "can't write WavPack data, disk probably full!";

                return result;
            }

            wpc.file2len += bcount;
        }

        return result;
    }

    public static WavPackContext OpenFileOutput(java.io.OutputStream outfile, java.io.OutputStream correction_outfile)
    {
        WavPackContext wpc = new WavPackContext();
        wpc.outfile = outfile;
        wpc.correction_outfile = correction_outfile;
        return wpc;
    }

    public static WavPackContext OpenFileOutput(java.io.OutputStream outfile)
    {
        WavPackContext wpc = new WavPackContext();
        wpc.outfile = outfile;
        return wpc;
    }

    public static long GetOutFileLength(WavPackContext context) {
        return context.filelen;
    }

    public static long GetCorrectionOutFileLength(WavPackContext context) {
        return context.file2len;
    }

}