/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 2000-2009 Josh Coalson
 * Copyright (c) 2011-2022 Xiph.Org Foundation
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Neither the name of the Xiph.Org Foundation nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE FOUNDATION OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.xiph.flac;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/* WATCHOUT: assembly routines rely on the order in which these fields are declared */
/* Things should be fastest when this matches the machine word size */
/* WATCHOUT: if you change this you must also change the following #defines down to FLAC__clz_uint32 below to match */
/* WATCHOUT: there are a few places where the code will not work unless uint32_t is >= 32 bits wide */
/*           also, some sections currently only have fast versions for 4 or 8 bytes per word */
class BitReader {
	private static final int FLAC__BYTES_PER_WORD = 4;// java: always 4
	private static final int FLAC__BYTES_PER_WORD_LOG2 = 2;// java: always 2
	private static final int FLAC__BITS_PER_WORD = 32;// java: always 32
	private static final int FLAC__BITS_PER_WORD_LOG2 = 5;// java: always 5
	private static final int FLAC__WORD_ALL_ONES = (0xffffffff);
	/*
	 * This should be at least twice as large as the largest number of words
	 * required to represent any 'number' (in any encoding) you are going to
	 * read.  With FLAC this is on the order of maybe a few hundred bits.
	 * If the buffer is smaller than that, the decoder won't be able to read
	 * in a whole number that is in a variable length encoding (e.g. Rice).
	 * But to be practical it should be at least 1K bytes.
	 *
	 * Increase this number to decrease the number of read callbacks, at the
	 * expense of using more memory.  Or decrease for the reverse effect,
	 * keeping in mind the limit from the first paragraph.  The optimal size
	 * also depends on the CPU cache size and other factors; some twiddling
	 * may be necessary to squeeze out the best performance.
	 */
	private static final int FLAC__BITREADER_DEFAULT_CAPACITY = 65536 / FLAC__BITS_PER_WORD; /* in words */

	private static final byte byte_to_unary_table[] = {
		8, 7, 6, 6, 5, 5, 5, 5, 4, 4, 4, 4, 4, 4, 4, 4,
		3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
		2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
	};
	/** any partially-consumed word at the head will stay right-justified as bits are consumed from the left<br>
	 * any incomplete word at the tail will be left-justified, and bytes from the read callback are added on the right */
	private int[] buffer = null;
	/** byte presentation of the buffer */
	private byte[] bytebuffer = null;// java: added to read
	/** in words */
	private int capacity = 0;
	/** # of completed words in buffer */
	private int words = 0;
	/** # of bytes in incomplete word at buffer[words] */
	private int bytes = 0;
	/** # words ... */
	private int consumed_words = 0;
	/** ... + (# bits of head word) already consumed from the front of buffer */
	private int consumed_bits = 0;
	/** the running frame CRC */
	private int read_crc16 = 0;
	/** the number of words in the current buffer that should not be CRC'd */
	private int crc16_offset;
	/** the number of bits in the current consumed word that should not be CRC'd */
	private int crc16_align;
	/** whether reads are limited */
	private boolean read_limit_set;
	/** the remaining size of what can be read */
	private int read_limit;
	/** the location of the last seen framesync, if it is in the buffer, in bits from front of buffer */
	private int last_seen_framesync;
	private BitReaderReadCallback read_callback = null;
	// private Object client_data = null;// java: don't need. uses read_callback

	// java: extracted in place
	/** counts the # of zero MSBs in a word */
	/* Will never be emitted for MSVC, GCC, Intel compilers */
	/*static inline unsigned int FLAC__clz_soft_uint32(unsigned int word)
	{
		static const unsigned char byte_to_unary_table[] = {
			8, 7, 6, 6, 5, 5, 5, 5, 4, 4, 4, 4, 4, 4, 4, 4,
			3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
			2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
			2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
			1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
			1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
			1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
			1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	    };

		return word > 0xffffff ? byte_to_unary_table[word >> 24] :
			word > 0xffff ? byte_to_unary_table[word >> 16] + 8 :
				word > 0xff ? byte_to_unary_table[word >> 8] + 16 :
					byte_to_unary_table[word] + 24;
	}*/
	/* Used when 64-bit bsr/clz is unavailable; can use 32-bit bsr/clz when possible */
	/* static inline unsigned int FLAC__clz_soft_uint64(FLAC__uint64 word)
	{
		return (FLAC__uint32)(word>>32) ? FLAC__clz_uint32((FLAC__uint32)(word>>32)) :
			FLAC__clz_uint32((FLAC__uint32)word) + 32;
	} */

	/* static inline unsigned int FLAC__clz_uint64(FLAC__uint64 v)
	{
		// Never used with input 0
		FLAC__ASSERT(v > 0);
	#if defined(__GNUC__) && (__GNUC__ >= 4 || (__GNUC__ == 3 && __GNUC_MINOR__ >= 4))
		return __builtin_clzll(v);
	#elif (defined(__INTEL_COMPILER) || defined(_MSC_VER)) && (defined(_M_IA64) || defined(_M_X64))
		{
			unsigned long idx;
			_BitScanReverse64(&idx, v);
			return idx ^ 63U;
		}
	#else
		return FLAC__clz_soft_uint64(v);
	#endif
	} */

	/* These two functions work with input 0 */
	/* static inline unsigned int FLAC__clz2_uint32(FLAC__uint32 v)
	{
		if (!v)
			return 32;
		return FLAC__clz_uint32(v);
	} */

	/* static inline unsigned int FLAC__clz2_uint64(FLAC__uint64 v)
	{
		if (!v)
			return 64;
		return FLAC__clz_uint64(v);
	} */

	/***********************************************************************
	 *
	 * Class constructor/destructor
	 *
	 ***********************************************************************/

	// FLAC__bitreader_new(), FLAC__bitreader_delete(BitReader br)
	BitReader() {
	}

	private void crc16_update_word_(final int word)
	{
		int crc = this.read_crc16;

		for ( ; this.crc16_align < FLAC__BITS_PER_WORD; this.crc16_align += 8 ) {
			final int shift = FLAC__BITS_PER_WORD - 8 - this.crc16_align;// uint32, java: shift >= 0 is added
			crc = CRC.CRC16_UPDATE( (shift >= 0 && shift < FLAC__BITS_PER_WORD ? (word >> shift) & 0xff : 0), crc );
		}

		this.read_crc16 = crc;
		this.crc16_align = 0;
	}

	private void crc16_update_block_()
	{
		if( (this.consumed_words > this.crc16_offset) && (0 != this.crc16_align) ) {
			crc16_update_word_( this.buffer[ this.crc16_offset++ ] );
		}

		/* Prevent OOB read due to wrap-around. */
		if( this.consumed_words > this.crc16_offset ) {
//if( FLAC__BYTES_PER_WORD == 4 ) {
			this.read_crc16 = CRC.crc16_update_words32( this.buffer, this.crc16_offset, this.consumed_words - this.crc16_offset, this.read_crc16 );
/*} else if( FLAC__BYTES_PER_WORD == 8 ) {
 			this.read_crc16 = FLAC__crc16_update_words64( this.buffer, this.crc16_offset, this.consumed_words - this.crc16_offset, this.read_crc16 );
} else {
			unsigned i;

			for( int i = this.crc16_offset; i < this.consumed_words; i++ ) {
				crc16_update_word_( this.buffer[i] );
			}
} */
		}
		this.crc16_offset = 0;
	}

	private final void bitreader_read_from_client_() throws IOException// java: changed, throws IOException instead boolean
	{
//#if WORDS_BIGENDIAN
//#else
//		int preswap_backup;
//#endif
		/* invalidate last seen framesync */
		this.last_seen_framesync = -1;

		/* first shift the unconsumed buffer data toward the front as much as possible */
		if( this.consumed_words > 0 ) {
			crc16_update_block_(); /* CRC consumed words */
			final int start = this.consumed_words;
			final int end = this.words + (this.bytes != 0 ? 1 : 0);
			System.arraycopy( this.buffer, start, this.buffer, 0, /*FLAC__BYTES_PER_WORD * */(end - start) );

			this.words -= start;
			this.consumed_words = 0;
		}

		/*
		 * set the target for reading, taking into account word alignment and endianness
		 */
		int read = ((this.capacity - this.words) << FLAC__BYTES_PER_WORD_LOG2) - this.bytes;
		if( read == 0 ) {
			throw new IOException(); /* no space left, buffer is too small; see note for FLAC__BITREADER_DEFAULT_CAPACITY  */
		}

//		target = ((FLAC__byte*)(this.buffer+this.words)) + this.bytes;

//#if WORDS_BIGENDIAN
//#else
//		preswap_backup = this.buffer[this.words];
//		if( this.bytes )
//			this.buffer[this.words] = SWAP_BE_WORD_TO_HOST(this.buffer[this.words]);
//#endif

		/* read in the data; note that the callback may return a smaller number of bytes */
		if( 0 > (read = this.read_callback.bit_read_callback( this.bytebuffer, read/*, this.client_data*/ )) ) {
			/* Despite the read callback failing, the data in the target
			 * might be used later, when the buffer is rewound. Therefore
			 * we revert the swap that was just done */
//#if WORDS_BIGENDIAN
//#else
//			this.buffer[this.words] = preswap_backup;
//#endif
			throw new IOException();
		}

		int end = read;
		if( this.bytes != 0 ) {// writing bytes in last partial word as BIG ENDIAN
			int start = FLAC__BYTES_PER_WORD - this.bytes;// temp
			int val = this.buffer[this.words];
			val &= FLAC__WORD_ALL_ONES << (start << 3);
			for( int i = --start; i >= 0 && end > 0; i--, end-- ) {
				val |= (((int)this.bytebuffer[i]) & 0xff) << ((start - i) << 3);
			}
			this.buffer[this.words] = val;

		}
		int offset = this.words;
		if( end > 0 ) {
			final int start = read - end;
			if( start > 0 ) {
				offset++;
			}
			if( 0 != (end & ((1 << FLAC__BYTES_PER_WORD_LOG2) - 1)) ) {
				end += FLAC__BYTES_PER_WORD;
			}
			end >>>= FLAC__BYTES_PER_WORD_LOG2;// TODO try to optimize the wrap length parameter
			ByteBuffer.wrap( this.bytebuffer, start, this.bytes + (end << FLAC__BYTES_PER_WORD_LOG2) ).order( ByteOrder.BIG_ENDIAN ).asIntBuffer().get( this.buffer, offset, end );
		}

		end = (this.words << FLAC__BYTES_PER_WORD_LOG2) + this.bytes + read;
		this.words = (end >>> FLAC__BYTES_PER_WORD_LOG2);
		this.bytes = end & (FLAC__BYTES_PER_WORD - 1);
	}

	/***********************************************************************
	 *
	 * Public class methods
	 *
	 ***********************************************************************/

	final boolean init(final BitReaderReadCallback rcb/*, final Object cd*/)
	{
		this.words = this.bytes = 0;
		this.consumed_words = this.consumed_bits = 0;
		this.capacity = FLAC__BITREADER_DEFAULT_CAPACITY;
		this.buffer = new int[this.capacity];
		this.bytebuffer = new byte[this.capacity << 2];// int to byte
		this.read_callback = rcb;
		// this.client_data = cd;
		this.read_limit_set = false;
		this.read_limit = Integer.MAX_VALUE;// java changed -1;
		this.last_seen_framesync = -1;

		return true;
	}

	/* public static void free(final BitReader br)
	{
		br.buffer = null;
		br.bytebuffer = null;
		br.capacity = 0;
		br.words = br.bytes = 0;
		br.consumed_words = br.consumed_bits = 0;
		br.read_callback = null;
		// br.client_data = null;
		br.read_limit_set = false;
		br.read_limit = Integer.MAX_VALUE;// java changed -1;
		br.last_seen_framesync = -1;
	} */

	final boolean clear()
	{
		this.words = this.bytes = 0;
		this.consumed_words = this.consumed_bits = 0;
		this.read_limit_set = false;
		this.read_limit = Integer.MAX_VALUE;// java changed -1;
		this.last_seen_framesync = -1;
		return true;
	}

	final void set_framesync_location()
	{
		this.last_seen_framesync = this.consumed_words * FLAC__BYTES_PER_WORD + this.consumed_bits / 8;
	}

	final boolean rewind_to_after_last_seen_framesync()
	{
		if( this.last_seen_framesync == -1 ) {
			this.consumed_words = this.consumed_bits = 0;
			return false;
		} else {
			this.consumed_words = (this.last_seen_framesync + 1) / FLAC__BYTES_PER_WORD;
			this.consumed_bits  = ((this.last_seen_framesync + 1) % FLAC__BYTES_PER_WORD) << 3;
			return true;
		}
	}

	@SuppressWarnings("boxing")
	void dump(final PrintStream out)
	{
		int i;
		out.printf("bitreader: capacity=%d words=%d bytes=%d consumed: words=%d, bits=%d\n", this.capacity, this.words, this.bytes, this.consumed_words, this.consumed_bits);

		for( i = 0; i < this.words; i++ ) {
			out.printf("%08X: ", i );
			for( int j = 0; j < FLAC__BITS_PER_WORD; j++ ) {
				if( i < this.consumed_words || (i == this.consumed_words && j < this.consumed_bits) ) {
					out.print(".");
				} else {
					out.printf("%01d", (this.buffer[i] & (1 << (FLAC__BITS_PER_WORD - j - 1))) != 0 ? 1 : 0);
				}
			}
			out.print("\n");
		}
		if( this.bytes > 0 ) {
			out.printf("%08X: ", i);
			for( int j = 0, je = this.bytes * 8; j < je; j++ ) {
				if( i < this.consumed_words || (i == this.consumed_words && j < this.consumed_bits) ) {
					out.print(".");
				} else {
					out.printf("%01d", (this.buffer[i] & (1 << (this.bytes * 8 - j - 1))) != 0 ? 1 : 0);
				}
			}
			out.print("\n");
		}
	}

	final void reset_read_crc16(final int seed)// java: short seed
	{
		//FLAC__ASSERT( (br.consumed_bits & 7) == 0 );

		this.read_crc16 = seed;// (int)seed;
		this.crc16_offset = this.consumed_words;
		this.crc16_align = this.consumed_bits;
	}

	final int get_read_crc16()
	{
		//FLAC__ASSERT(0 != br);
		//FLAC__ASSERT(0 != br->buffer);

		/* CRC consumed words up to here */
		crc16_update_block_();

		//FLAC__ASSERT((br->consumed_bits & 7) == 0);
		//FLAC__ASSERT(br->crc16_align <= br->consumed_bits);

		/* CRC any tail bytes in a partially-consumed word */
		final int bits = this.consumed_bits;// java
		if( bits != 0 ) {
			final int tail = this.buffer[this.consumed_words];
			int align = this.crc16_align;// java
			int crc16 = this.read_crc16;// java
			for( ; align < bits; align += 8 ) {
				crc16 = CRC.CRC16_UPDATE( ((tail >> (FLAC__BITS_PER_WORD - 8 - align)) & 0xff), crc16 );
			}
			this.read_crc16 = crc16;
			this.crc16_align = align;
		}
		return this.read_crc16;
	}

	final boolean is_consumed_byte_aligned()
	{
		return ((this.consumed_bits & 7) == 0);
	}

	final int bits_left_for_byte_alignment()
	{
		return 8 - (this.consumed_bits & 7);
	}

	final int get_input_bits_unconsumed()
	{
		return ((this.words - this.consumed_words) << FLAC__BITS_PER_WORD_LOG2) + (this.bytes << 3) - this.consumed_bits;
	}

	final void set_limit(final int limit)
	{
		this.read_limit = limit;
		this.read_limit_set = true;
	}

	final void remove_limit()
	{
		this.read_limit_set = false;
		this.read_limit = Integer.MAX_VALUE;// java changed -1;
	}

	final int limit_remaining()
	{
		// FLAC__ASSERT(this.read_limit_set);
		return this.read_limit;
	}

	final void limit_invalidate()
	{
		this.read_limit = Integer.MAX_VALUE;// java changed -1;
	}

	final int read_raw_uint32(int bits) throws IOException// java: changed. val is returned. if an error, throws exception
	{
		/* WATCHOUT: code does not work with <32bit words; we can make things much faster with this assertion */

		if( bits == 0 ) { /* OPT: investigate if this can ever happen, maybe change to assertion */
			return 0;
		}

		if( this.read_limit_set && this.read_limit < Integer.MAX_VALUE ) {// java changed -1
			if( this.read_limit < bits ) {
				this.read_limit = Integer.MAX_VALUE;// java changed -1;
				throw new IOException();
				// return false;
			} else {
				this.read_limit -= bits;
			}
		}

		while( ((this.words - this.consumed_words) << FLAC__BITS_PER_WORD_LOG2) + (this.bytes << 3) - this.consumed_bits < bits ) {
			bitreader_read_from_client_();
		}
		if( this.consumed_words < this.words ) { /* if we've not consumed up to a partial tail word... */
			/* OPT: taking out the consumed_bits==0 "else" case below might make things faster if less code allows the compiler to inline this function */
			if( this.consumed_bits != 0 ) {
				/* this also works when consumed_bits==0, it's just a little slower than necessary for that case */
				final int n = FLAC__BITS_PER_WORD - this.consumed_bits;
				final int word = this.buffer[this.consumed_words];
				final int mask = this.consumed_bits < FLAC__BITS_PER_WORD ? FLAC__WORD_ALL_ONES >>> this.consumed_bits : 0;
				if( bits < n ) {
					final int shift = n - bits;
					final int val = shift < FLAC__BITS_PER_WORD ? ((word & mask) >>> shift) : 0; /* The result has <= 32 non-zero bits */
					this.consumed_bits += bits;
					return val;
				}
				/* (FLAC__BITS_PER_WORD - br->consumed_bits <= bits) ==> (FLAC__WORD_ALL_ONES >> br->consumed_bits) has no more than 'bits' non-zero bits */
				int val = (word & mask);
				bits -= n;
				this.consumed_words++;
				this.consumed_bits = 0;
				if( bits != 0 ) { /* if there are still bits left to read, there have to be less than 32 so they will all be in the next word */
					final int shift = FLAC__BITS_PER_WORD - bits;// uint32
					val = bits < 32 ? val << bits : 0;
					val |= shift < FLAC__BITS_PER_WORD ? (this.buffer[this.consumed_words] >>> shift) : 0;
					this.consumed_bits = bits;
				}
				return val;
			}
			// else { // br->consumed_bits == 0
				final int word = this.buffer[this.consumed_words];
				if( bits < FLAC__BITS_PER_WORD ) {
					// final int val = word >>> (FLAC__BITS_PER_WORD - bits);
					this.consumed_bits = bits;
					return word >>> (FLAC__BITS_PER_WORD - bits);
				}
				/* at this point bits == FLAC__BITS_PER_WORD == 32; because of previous assertions, it can't be larger */
				// final int val = word;
				this.consumed_words++;
				return word;
			//}
		}
		//else {
			/* in this case we're starting our read at a partial tail word;
			 * the reader has guaranteed that we have at least 'bits' bits
			 * available to read, which makes this case simpler.
			 */
			/* OPT: taking out the consumed_bits==0 "else" case below might make things faster if less code allows the compiler to inline this function */
			if( this.consumed_bits != 0 ) {
				/* this also works when consumed_bits==0, it's just a little slower than necessary for that case */
				final int val = (this.buffer[this.consumed_words] & (FLAC__WORD_ALL_ONES >>> this.consumed_bits)) >>> (FLAC__BITS_PER_WORD - this.consumed_bits - bits);
				this.consumed_bits += bits;
				return val;
			}
			//else {
				// final int val = this.buffer[this.consumed_words] >>> (FLAC__BITS_PER_WORD - bits);
				this.consumed_bits += bits;
				return this.buffer[this.consumed_words] >>> (FLAC__BITS_PER_WORD - bits);
			//}
		//}
	}

	final int read_raw_int32(final int bits) throws IOException// java: changed. val is returned. if an error, throws exception
	{
		/* OPT: inline raw uint32 code here, or make into a macro if possible in the .h file */
		if( bits < 1 ) {
			throw new IOException();
		}
		final int uval = read_raw_uint32( bits );
		/* sign-extend *val assuming it is currently bits wide. */
		/* From: https://graphics.stanford.edu/~seander/bithacks.html#FixedSignExtend */
		final int mask = bits >= 33 ? 0 : 1 << (bits - 1);
		return (uval ^ mask) - mask;
	}

	final long read_raw_uint64(final int bits) throws IOException// java: changed. val is returned. if an error, throws exception
	{
		if( bits > 32 ) {
			long val = ((long)read_raw_uint32( bits - 32 )) << 32;
			val |= ((long)read_raw_uint32( 32 )) & 0xffffffffL;
			return val;
		}
		//else {
			final long val = ((long)read_raw_uint32( bits )) & 0xffffffffL;
		//}
		return val;
	}

	final long read_raw_int64(final int bits) throws IOException// java: changed. val is returned. if an error, throws exception
	{
		/* OPT: inline raw uint64 code here, or make into a macro if possible in the .h file */
		if( bits < 1 ) {
			throw new IOException();
		}
		final long uval = read_raw_uint64( bits );
		/* sign-extend *val assuming it is currently bits wide. */
		/* From: https://graphics.stanford.edu/~seander/bithacks.html#FixedSignExtend */
		final long mask = bits >= 65 ? 0 : 1 << (bits - 1);
		final long val = (uval ^ mask) - mask;
		return val;
	}


	final int read_uint32_little_endian() throws IOException// java: changed. val is returned. if an error, throws exception
	{
		/* this doesn't need to be that fast as currently it is only used for vorbis comments */

		int x32 = read_raw_uint32( 8 );

		int x8 = read_raw_uint32( 8 );
		x32 |= (x8 << 8);

		x8 = read_raw_uint32( 8 );
		x32 |= (x8 << 16);

		x8 = read_raw_uint32( 8 );
		x32 |= (x8 << 24);

		return x32;
	}

	final void skip_bits_no_crc(int bits) throws IOException// java: changed. if an error, throws exception
	{
		/*
		 * OPT: a faster implementation is possible but probably not that useful
		 * since this is only called a couple of times in the metadata readers.
		 */

		if( bits > 0 ) {
			final int n = this.consumed_bits & 7;
			//int x;

			if( n != 0 ) {
				int m = 8 - n;
				if( m > bits ) {
					m = bits;
				}
				/*x = */read_raw_uint32( m );
				bits -= m;
			}
			final int m = bits >>> 3;
			if( m > 0 ) {
				skip_byte_block_aligned_no_crc( m );
				bits &= 7;
			}
			if( bits > 0 ) {
				/*x = */read_raw_uint32( bits );
			}
		}
	}

	final void skip_byte_block_aligned_no_crc(int nvals) throws IOException// java: changed. if an error, throws exception
	{
		//int x;

		if( this.read_limit_set && this.read_limit < Integer.MAX_VALUE ) {// java chenged -1
			if( this.read_limit < nvals << 3 ) {
				this.read_limit = Integer.MAX_VALUE;// java changed -1;
				throw new IOException();
			}
		}

		/* step 1: skip over partial head word to get word aligned */
		while( nvals != 0 && this.consumed_bits != 0 ) { /* i.e. run until we read 'nvals' bytes or we hit the end of the head word */
			/*x = */read_raw_uint32( 8 );
			nvals--;
		}
		if( 0 == nvals )
		 {
			return;// true;
		}
		/* step 2: skip whole words in chunks */
		while( nvals >= FLAC__BYTES_PER_WORD ) {
			if( this.consumed_words < this.words ) {
				this.consumed_words++;
				nvals -= FLAC__BYTES_PER_WORD;
				if( this.read_limit_set ) {
					this.read_limit -= FLAC__BITS_PER_WORD;
				}
			} else {
				bitreader_read_from_client_();
			}
		}
		/* step 3: skip any remainder from partial tail bytes */
		while( nvals != 0 ) {
			/*x = */read_raw_uint32( 8 );
			nvals--;
		}
	}

	final void /*boolean*/ read_byte_block_aligned_no_crc(final byte[] val, int nvals) throws IOException// java: changed. if an error, throws exception
	{
		if( this.read_limit_set && this.read_limit < Integer.MAX_VALUE ) {// java changed -1;
			if( this.read_limit < nvals << 3 ) {
				this.read_limit = Integer.MAX_VALUE;// java changed -1;
				throw new IOException();
			}
		}

		int offset = 0;

		/* step 1: read from partial head word to get word aligned */
		while( nvals != 0 && this.consumed_bits != 0 ) { /* i.e. run until we read 'nvals' bytes or we hit the end of the head word */
			final int x = read_raw_uint32( 8 );
			val[offset++] = (byte)x;
			nvals--;
		}
		if( 0 == nvals ) {
			return;
		}
		/* step 2: read whole words in chunks */
		while( nvals >= FLAC__BYTES_PER_WORD ) {
			if( this.consumed_words < this.words ) {
				final int word = this.buffer[this.consumed_words++];
//if( FLAC__BYTES_PER_WORD == 4 ) {
				val[offset++] = (byte)(word >>> 24);
				val[offset++] = (byte)(word >>> 16);
				val[offset++] = (byte)(word >>> 8);
				val[offset++] = (byte)word;
/*} else if( FLAC__BYTES_PER_WORD == 8 ) {
				val[offset + 0] = (byte)(word >>> 56);
				val[offset + 1] = (byte)(word >>> 48);
				val[offset + 2] = (byte)(word >>> 40);
				val[offset + 3] = (byte)(word >>> 32);
				val[offset + 4] = (byte)(word >>> 24);
				val[offset + 5] = (byte)(word >>> 16);
				val[offset + 6] = (byte)(word >>> 8);
				val[offset + 7] = (byte)word;
} else {
				for( int i = 0; i < FLAC__BYTES_PER_WORD; i++ )
					val[offset + i] = (byte)(word >>> ((FLAC__BYTES_PER_WORD - i - 1) << 3));
}*/
				// offset += FLAC__BYTES_PER_WORD;
				nvals -= FLAC__BYTES_PER_WORD;
				if( this.read_limit_set ) {
					this.read_limit -= FLAC__BITS_PER_WORD;
				}
			} else {
				bitreader_read_from_client_();
			}
		}
		/* step 3: read any remainder from partial tail bytes */
		while( nvals != 0 ) {
			final int x = read_raw_uint32( 8 );
			val[offset++] = (byte)x;
			nvals--;
		}
	}

	final int read_unary_unsigned() throws IOException {// java: changed. val is returned. if an error, throws exception

//if( false ) /* slow but readable version */
/*	{
		int bit;

		val = 0;
		while( true ) {
			if( ! FLAC__bitreader_read_bit( &bit ) )
				return false;
			if( bit != 0 )
				break;
			else
				*val++;
		}
		return true;
	}*/
//else
	{
		int val = 0;
		while( true ) {
			while( this.consumed_words < this.words ) { /* if we've not consumed up to a partial tail word... */
				final int b = this.consumed_bits < FLAC__BITS_PER_WORD ? this.buffer[this.consumed_words] << this.consumed_bits : 0;
				if( b != 0 ) {
					//i = COUNT_ZERO_MSBS( b );
					int i = ( (b & 0xff000000) != 0 ? byte_to_unary_table[b >>> 24] :
						(b & 0xffff0000) != 0 ? byte_to_unary_table[b >>> 16] + 8 :
						(b & 0xffffff00) != 0 ? byte_to_unary_table[b >>> 8] + 16 :
						byte_to_unary_table[b] + 24 );
					val += i;
					i++;
					this.consumed_bits += i;
					if( this.consumed_bits >= FLAC__BITS_PER_WORD ) { /* faster way of testing if(br->consumed_bits == FLAC__BITS_PER_WORD) */
						this.consumed_words++;
						this.consumed_bits = 0;
					}
					return val;
				}
				else {
					val += FLAC__BITS_PER_WORD - this.consumed_bits;
					this.consumed_words++;
					this.consumed_bits = 0;
					/* didn't find stop bit yet, have to keep going... */
				}
			}
			/* at this point we've eaten up all the whole words; have to try
			 * reading through any tail bytes before calling the read callback.
			 * this is a repeat of the above logic adjusted for the fact we
			 * don't have a whole word.  note though if the client is feeding
			 * us data a byte at a time (unlikely), br->consumed_bits may not
			 * be zero.
			 */
			if( (this.bytes << 3) > this.consumed_bits ) {
				final int end = this.bytes << 3;
				final int b = (this.buffer[this.consumed_words] & (FLAC__WORD_ALL_ONES << (FLAC__BITS_PER_WORD - end))) << this.consumed_bits;
				if( b != 0 ) {
					//i = COUNT_ZERO_MSBS( b );
					int i = ( (b & 0xff000000) != 0 ? byte_to_unary_table[b >>> 24] :
						(b & 0xffff0000) != 0 ? byte_to_unary_table[b >>> 16] + 8 :
						(b & 0xffffff00) != 0 ? byte_to_unary_table[b >>> 8] + 16 :
						byte_to_unary_table[b] + 24 );
					val += i;
					i++;
					this.consumed_bits += i;
					//FLAC__ASSERT(br.consumed_bits < FLAC__BITS_PER_WORD);
					return val;
				}
				else {
					val += end - this.consumed_bits;
					this.consumed_bits += end;
					//FLAC__ASSERT(br.consumed_bits < FLAC__BITS_PER_WORD);
					/* didn't find stop bit yet, have to keep going... */
				}
			}
			bitreader_read_from_client_();
		}
}
	}

/* #if 0 // unused
	private final int read_rice_signed(int parameter) throws IOException // java: changed. val is returned. if an error, throws exception
	{
		int lsbs = 0, msbs = 0;
		int uval;

		//FLAC__ASSERT(0 != br);
		//FLAC__ASSERT(0 != br->buffer);
		//FLAC__ASSERT(parameter <= 31);

		// read the unary MSBs and end bit
		msbs = read_unary_unsigned();

		// read the binary LSBs
		lsbs = read_raw_uint32( parameter );

		// compose the value
		uval = (msbs << parameter) | lsbs;
		if( (uval & 1) != 0 )
			return -(uval >>> 1) - 1;
		//else
			return uval >>> 1;

	}
#endif */

	/* this is by far the most heavily used reader call.  it ain't pretty but it's fast */
	final void read_rice_signed_block(final int vals[], int offset, final int nvals, final int parameter) throws IOException// java: changed. if an error, throws exception
	{
		/* try and get br->consumed_words and br->consumed_bits into register;
		 * must remember to flush them back to *br before calling other
		 * bitreader functions that use them, and before returning */

		//FLAC__ASSERT(0 != br);
		//FLAC__ASSERT(0 != br->buffer);
		/* WATCHOUT: code does not work with <32bit words; we can make things much faster with this assertion */
		//FLAC__ASSERT(FLAC__BITS_PER_WORD >= 32);
		//FLAC__ASSERT(parameter < 32);
		/* the above two asserts also guarantee that the binary part never straddles more than 2 words, so we don't have to loop to read it */

		final int limit = Integer.MAX_VALUE >>> parameter; /* Maximal msbs that can occur with residual bounded to int32_t */

		//val = offset;// java: val changed to offset
		final int end = offset + nvals;

		if( parameter == 0 ) {
			while( offset < end ) {
				/* read the unary MSBs and end bit */
				final int msbs = read_unary_unsigned();
				/* Checking limit here would be overzealous: coding UINT32_MAX
				 * with parameter == 0 would take 4GiB */
				vals[offset++] = (msbs >>> 1) ^ -(msbs & 1);
			}

			return;
		}

		//FLAC__ASSERT(parameter > 0);

		int cwords = this.consumed_words;
		int rwords = this.words;

		int b;
		int ucbits; /* keep track of the number of unconsumed bits in word */
		/* if we've not consumed up to a partial tail word... */
		if( cwords >= rwords ) {
			// java: x = 0, so don't using
			//goto process_tail;
			/* at this point we've eaten up all the whole words */
//process_tail:
			do {
				/* read the unary MSBs and end bit */
				final int msbs = read_unary_unsigned();

				/* read the binary LSBs */
				final int lsbs = read_raw_uint32( parameter );

				/* compose the value */
				final int x = (msbs << parameter) | lsbs;
				vals[offset++] = (x >>> 1) ^ -(x & 1);

				cwords = this.consumed_words;
				rwords = this.words;
				ucbits = FLAC__BITS_PER_WORD - this.consumed_bits;
				b = this.buffer[cwords] << this.consumed_bits;
			} while( cwords >= rwords && offset < end );
		} else {
			ucbits = FLAC__BITS_PER_WORD - this.consumed_bits;
			b = this.buffer[cwords] << this.consumed_bits;  /* keep unconsumed bits aligned to left */
		}

main_loop:
		while( offset < end ) {
			/* read the unary MSBs and end bit */
			//x = y = COUNT_ZERO_MSBS( b );
			int x, y;
			x = y = ( b == 0 ? 32 :
				(b & 0xff000000) != 0 ? byte_to_unary_table[b >>> 24] :
				(b & 0xffff0000) != 0 ? byte_to_unary_table[b >>> 16] + 8 :
				(b & 0xffffff00) != 0 ? byte_to_unary_table[b >>> 8] + 16 :
				byte_to_unary_table[b] + 24 );
			if( x == FLAC__BITS_PER_WORD ) {
				x = ucbits;
				do {
					/* didn't find stop bit yet, have to keep going... */
					cwords++;
					if( cwords >= rwords ) {
						/* at this point we've eaten up all the whole words */
//process_tail:
						//goto incomplete_msbs;
						this.consumed_bits = 0;
						this.consumed_words = cwords;
						do {
							/* read the unary MSBs and end bit */
							int msbs = read_unary_unsigned();

							msbs += x;

							/* read the binary LSBs */
							final int lsbs = read_raw_uint32( parameter );

							/* compose the value */
							x = (msbs << parameter) | lsbs;
							vals[offset++] = (x >>> 1) ^ -(x & 1);
							x = 0;

							cwords = this.consumed_words;
							rwords = this.words;
							ucbits = FLAC__BITS_PER_WORD - this.consumed_bits;
							b = this.buffer[cwords] << this.consumed_bits;
						} while( cwords >= rwords && offset < end );
						continue main_loop;
					}
					b = this.buffer[cwords];
					//y = COUNT_ZERO_MSBS( b );
					y = ( b == 0 ? 32 :
						(b & 0xff000000) != 0 ? byte_to_unary_table[b >>> 24] :
						(b & 0xffff0000) != 0 ? byte_to_unary_table[b >>> 16] + 8 :
						(b & 0xffffff00) != 0 ? byte_to_unary_table[b >>> 8] + 16 :
						byte_to_unary_table[b] + 24 );
					x += y;
				} while( y == FLAC__BITS_PER_WORD );
			}
			b <<= y;
			b <<= 1; /* account for stop bit */
			ucbits = (ucbits - x - 1) & (FLAC__BITS_PER_WORD - 1);
			int msbs = x;

			if( x > limit ) {
				throw new IOException();
			}

			/* read the binary LSBs */
			x = b >>> (FLAC__BITS_PER_WORD - parameter);/* parameter < 32, so we can cast to 32-bit uint32_t */

			if( parameter <= ucbits ) {
				ucbits -= parameter;
				b <<= parameter;
			} else {
				/* there are still bits left to read, they will all be in the next word */
				cwords++;
				if( cwords >= rwords ) {
					//goto incomplete_lsbs;
					this.consumed_bits = 0;
					this.consumed_words = cwords;

					/* read the binary LSBs */
					int lsbs = x | read_raw_uint32( parameter - ucbits );

					/* compose the value */
					x = (msbs << parameter) | lsbs;
					vals[offset++] = (x >>> 1) ^ -(x & 1);
					// x = 0;

					cwords = this.consumed_words;
					rwords = this.words;
					ucbits = FLAC__BITS_PER_WORD - this.consumed_bits;
					b = this.buffer[cwords] << this.consumed_bits;
					if( cwords < rwords || offset >= end ) {
						continue main_loop;
					}
					/* at this point we've eaten up all the whole words */
//process_tail:
					do {
						/* read the unary MSBs and end bit */
						msbs = read_unary_unsigned();

						// msbs += x;
						// x = ucbits = 0;// FIXME why?

						/* read the binary LSBs */
						// lsbs = x | FLAC__bitreader_read_raw_uint32( parameter - ucbits );
						lsbs = read_raw_uint32( parameter );

						/* compose the value */
						x = (msbs << parameter) | lsbs;
						vals[offset++] = (x >>> 1) ^ -(x & 1);
						// x = 0;

						cwords = this.consumed_words;
						rwords = this.words;
						ucbits = FLAC__BITS_PER_WORD - this.consumed_bits;
						b = cwords < this.capacity ? this.buffer[cwords] << this.consumed_bits : 0;
					} while( cwords >= rwords && offset < end );
					continue main_loop;
				}
				b = this.buffer[cwords];
				ucbits += FLAC__BITS_PER_WORD - parameter;
				x |= b >>> ucbits;
				b <<= FLAC__BITS_PER_WORD - ucbits;
			}
			// final int lsbs = x;

			/* compose the value */
			x = (msbs << parameter) | x;
			vals[offset++] = (x >>> 1) ^ -(x & 1);
		}

		if( ucbits == 0 && cwords < rwords ) {
			/* don't leave the head word with no unconsumed bits */
			cwords++;
			ucbits = FLAC__BITS_PER_WORD;
		}

		this.consumed_bits = FLAC__BITS_PER_WORD - ucbits;
		this.consumed_words = cwords;
	}

	/** on return, if *val == 0xffffffff then the utf-8 sequence was invalid, but the return value will be true */
	final int read_utf8_uint32(final RawHeaderHelper header) throws IOException// java: changed. val is returned. if an error, throws exception
	{
		final byte[] raw = header.raw_header;
		int rawlen = header.raw_header_len;

		int x = read_raw_uint32( 8 );
		if( raw != null ) {
			raw[ rawlen++ ] = (byte)x;
		}
		int v = 0;
		int i;
		if( (x & 0x80) == 0 ) { /* 0xxxxxxx */
			v = x;
			i = 0;
		}
		else if( (x & 0xC0) != 0 && (x & 0x20) == 0 ) { /* 110xxxxx */
			v = x & 0x1F;
			i = 1;
		}
		else if( (x & 0xE0) != 0 && (x & 0x10) == 0 ) { /* 1110xxxx */
			v = x & 0x0F;
			i = 2;
		}
		else if( (x & 0xF0) != 0 && (x & 0x08) == 0 ) { /* 11110xxx */
			v = x & 0x07;
			i = 3;
		}
		else if( (x & 0xF8) != 0 && (x & 0x04) == 0 ) { /* 111110xx */
			v = x & 0x03;
			i = 4;
		}
		else if( (x & 0xFC) != 0 && (x & 0x02) == 0 ) { /* 1111110x */
			v = x & 0x01;
			i = 5;
		}
		else {
			header.raw_header_len = rawlen;
			return 0xffffffff;
		}
		for( ; i > 0; i-- ) {
			x = read_raw_uint32( 8 );
			if( raw != null ) {
				raw[ rawlen++ ] = (byte)x;
			}
			if( 0 == (x & 0x80) || (x & 0x40) != 0 ) { /* 10xxxxxx */
				header.raw_header_len = rawlen;
				return 0xffffffff;
			}
			v <<= 6;
			v |= (x & 0x3F);
		}
		header.raw_header_len = rawlen;
		return v;
	}

	/** on return, if *val == 0xffffffffffffffff then the utf-8 sequence was invalid, but the return value will be true */
	final long read_utf8_uint64(final RawHeaderHelper header) throws IOException// java: changed. val is returned. if an error, throws exception
	{
		final byte[] raw = header.raw_header;
		int rawlen = header.raw_header_len;

		int x = read_raw_uint32( 8 );
		if( raw != null ) {
			raw[ rawlen++ ] = (byte)x;
		}
		long v = 0;
		int i;
		if( 0 == (x & 0x80) ) { /* 0xxxxxxx */
			v = x;
			i = 0;
		}
		else if( (x & 0xC0) != 0 && 0 == (x & 0x20) ) { /* 110xxxxx */
			v = x & 0x1F;
			i = 1;
		}
		else if( (x & 0xE0) != 0 && 0 == (x & 0x10) ) { /* 1110xxxx */
			v = x & 0x0F;
			i = 2;
		}
		else if( (x & 0xF0) != 0 && 0 == (x & 0x08) ) { /* 11110xxx */
			v = x & 0x07;
			i = 3;
		}
		else if( (x & 0xF8) != 0 && 0 == (x & 0x04) ) { /* 111110xx */
			v = x & 0x03;
			i = 4;
		}
		else if( (x & 0xFC) != 0 && 0 == (x & 0x02) ) { /* 1111110x */
			v = x & 0x01;
			i = 5;
		}
		else if( (x & 0xFE) != 0 && 0 == (x & 0x01) ) { /* 11111110 */
			v = 0;
			i = 6;
		}
		else {
			header.raw_header_len = rawlen;
			return (0xffffffffffffffffL);
		}
		for( ; i > 0; i-- ) {
			x = read_raw_uint32( 8 );
			if( raw != null ) {
				raw[ rawlen++ ] = (byte)x;
			}
			if( 0 == (x & 0x80) || (x & 0x40) != 0 ) { /* 10xxxxxx */
				header.raw_header_len = rawlen;
				return (0xffffffffffffffffL);
			}
			v <<= 6;
			v |= (x & 0x3F);
		}
		header.raw_header_len = rawlen;
		return v;
	}
}
