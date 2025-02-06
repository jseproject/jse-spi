package com.beatofthedrum.wv;

class BitsUtils
{
    ////////////////////////// Bitstream functions ////////////////////////////////
    // Open the specified BitStream using the specified buffer pointers. It is
    // assumed that enough buffer space has been allocated for all data that will
    // be written, otherwise an error will be generated.
    static void bs_open_write(Bitstream bs, int buffer_start, int buffer_end)
    {
        bs.error = 0;
        bs.sr = 0;
        bs.bc = 0;
        bs.buf_index = buffer_start;
        bs.start_index = bs.buf_index;
        bs.end = (int) buffer_end;
        bs.active = 1; // indicates that the bitstream is being used
    }

    // This function is only called from the putbit() and putbits() when
    // the buffer is full, which is now flagged as an error.
    static void bs_wrap(Bitstream bs)
    {
        bs.buf_index = bs.start_index;
        bs.error = 1;
    }

    // This function calculates the approximate number of bytes remaining in the
    // bitstream buffer and can be used as an early-warning of an impending overflow.
    static long bs_remain_write(Bitstream bs)
    {
        long bytes_written;

        if (bs.error > 0)
        {
            return (long) -1;
        }

        return bs.end - bs.buf_index;
    }

    // This function forces a flushing write of the standard BitStream, and
    // returns the total number of bytes written into the buffer.
    static long bs_close_write(WavPackStream wps)
    {
        Bitstream bs = wps.wvbits;
        long bytes_written = 0;

        if (bs.error != 0)
        {
            return (long) -1;
        }

        while ((bs.bc != 0) || (((bs.buf_index - bs.start_index) & 1) != 0))
        {
            WordsUtils.putbit_1(wps);
        }

        bytes_written = bs.buf_index - bs.start_index;

        return bytes_written;
    }

    // This function forces a flushing write of the correction BitStream, and
    // returns the total number of bytes written into the buffer.
    static long bs_close_correction_write(WavPackStream wps)
    {
        Bitstream bs = wps.wvcbits;
        long bytes_written = 0;

        if (bs.error != 0)
        {
            return (long) -1;
        }

        while ((bs.bc != 0) || (((bs.buf_index - bs.start_index) & 1) != 0))
        {
            WordsUtils.putbit_correction_1(wps);
        }

        bytes_written = bs.buf_index - bs.start_index;

        return bytes_written;
    }

    static void getbit(Bitstream bs)
    {
        if (bs.bc > 0)
        {
            bs.bc--;
        }
        else
        {
            bs.ptr++;
            bs.buf_index++;
            bs.bc = 7;

            if (bs.ptr == bs.end)
            {
                // wrap call here
                bs_read(bs);
            }
            bs.sr =  (bs.buf[bs.buf_index] & 0xff);
        }

        bs.bitval =  (int)(bs.sr & 1);
        bs.sr = bs.sr >> 1;
    }

    static long getbits(int nbits, Bitstream bs)
    {
        int uns_buf;
        final long value;

        while ((nbits) > bs.bc)
        {
            bs.ptr++;
            bs.buf_index++;

            if (bs.ptr == bs.end)
            {
                bs_read(bs);
            }
            uns_buf = (bs.buf[bs.buf_index] & 0xff);
            bs.sr = bs.sr | (uns_buf << bs.bc); // values in buffer must be unsigned

            bs.sr = bs.sr & 0xFFFFFFFFL;		// sr is an unsigned 32 bit variable
            
            bs.bc += 8;
        }

        value = bs.sr;

        if (bs.bc > 32)
        {
            bs.bc -= (nbits);
            bs.sr = (bs.buf[bs.buf_index] & 0xff) >> (8 - bs.bc);
        }
        else
        {
            bs.bc -= (nbits);
            bs.sr >>= (nbits);
        }

        return (value);
    }

    static Bitstream bs_open_read(byte [] stream, int buffer_start, int buffer_end, java.io.DataInputStream file,
        long file_bytes, int passed)
    {
        //   CLEAR (*bs);
        Bitstream bs = new Bitstream();

        bs.buf = stream;
        bs.buf_index = buffer_start;
        bs.end = buffer_end;
        bs.sr = 0;
        bs.bc = 0;

        if (passed != 0)
        {
            bs.ptr = (bs.end - 1);
            bs.file_bytes = file_bytes;
            bs.file = file;
        }
        else
        {
            /* Strange to set an index to -1, but the very first call to getbit will iterate this */
            bs.buf_index = -1;
            bs.ptr = -1;
        }

        return bs;
    }

    static void bs_read(Bitstream bs)
    {
        if (bs.file_bytes > 0)
        {
            long bytes_read, bytes_to_read;

            bytes_to_read = 1024;

            if (bytes_to_read > bs.file_bytes)
                bytes_to_read = bs.file_bytes;

            try
            {
                bytes_read = bs.file.read(bs.buf, 0, (int) bytes_to_read);
                bs.buf_index = 0;
            }
            catch (Exception e)
            {
                //System.err.println("Big error while reading file: " + e);
                bytes_read = 0;
            }

            if (bytes_read > 0)
            {
                bs.end = (int) (bytes_read);
                bs.file_bytes -= bytes_read;
            }
            else
            {
                for (int i = 0; i < bs.end - bs.buf_index; i++)
                {
                    bs.buf[i] = -1;
                }
                bs.error = 1;
            }
        }
        else
        {
            bs.error = 1;
        }

        if (bs.error > 0)
        {
            for (int i = 0; i < bs.end - bs.buf_index; i++)
            {
                bs.buf[i] = -1;
            }
        }

        bs.ptr = 0;
        bs.buf_index = 0;
    }
}