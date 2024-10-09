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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

// XXX java: make members public only for testing. FLAC__bitwriter_dump can be commented for regular using
//public
class BitWriter {// XXX public for tests only
	private static final int FLAC__BYTES_PER_WORD = 4;
	private static final int FLAC__BITS_PER_WORD = 32;
	/**
	 * The default capacity here doesn't matter too much.  The buffer always grows
	 * to hold whatever is written to it.  Usually the encoder will stop adding at
	 * a frame or metadata block, then write that out and clear the buffer for the
	 * next one.
	 */
	private static final int FLAC__BITWRITER_DEFAULT_CAPACITY = 32768 / FLAC__BYTES_PER_WORD; /* size in words */

	/** When growing, increment 4K at a time */
	private static final int FLAC__BITWRITER_DEFAULT_INCREMENT = 4096 / FLAC__BYTES_PER_WORD; /* size in words */

	/* java: extracted in place
	private static int FLAC__WORDS_TO_BITS(int words) {
		return ((words) * FLAC__BITS_PER_WORD);
	}
	private static int FLAC__TOTAL_BITS(BitWriter bw) {
		return (FLAC__WORDS_TO_BITS(bw.words) + bw.bits);
	}
	*/

	private int[] buffer = null;
	private final BitWriterHelperStruct byte_buffer = new BitWriterHelperStruct();
	/** accumulator; bits are right-justified; when full, accum is appended to buffer */
	private int accum = 0;
	/** capacity of buffer in words */
	private int capacity = 0;// XXX java: can be replaced by the buffer.length
	/** # of complete words in buffer */
	private int words = 0;
	/** # of used bits in accum */
	private int bits = 0;

	/** WATCHOUT: The current implementation only grows the buffer. */
	private final boolean bitwriter_grow_(final int bits_to_add)
	{
		//FLAC__ASSERT(0 != bw);
		//FLAC__ASSERT(0 != bw->buffer);

		/* calculate total words needed to store 'bits_to_add' additional bits */
		int new_capacity = this.words + ((this.bits + bits_to_add + FLAC__BITS_PER_WORD - 1) / FLAC__BITS_PER_WORD);

		/* it's possible (due to pessimism in the growth estimation that
		 * leads to this call) that we don't actually need to grow
		 */
		if( this.capacity >= new_capacity ) {
			return true;
		}

		if( new_capacity * FLAC__BYTES_PER_WORD > (1 << Format.FLAC__STREAM_METADATA_LENGTH_LEN) ) {
			/* Requested new capacity is larger than the largest possible metadata block,
			 * which is also larger than the largest sane framesize. That means something
			 * went very wrong somewhere and previous checks failed.
			 * To prevent crashing, give up */
			return false;
		}

		/* round up capacity increase to the nearest FLAC__BITWRITER_DEFAULT_INCREMENT */
		if( ((new_capacity - this.capacity) % FLAC__BITWRITER_DEFAULT_INCREMENT) != 0 ) {
			new_capacity += FLAC__BITWRITER_DEFAULT_INCREMENT - ((new_capacity - this.capacity) % FLAC__BITWRITER_DEFAULT_INCREMENT);
		/* make sure we got everything right */
		//FLAC__ASSERT(0 == (new_capacity - bw->capacity) % FLAC__BITWRITER_DEFAULT_INCREMENT);
		//FLAC__ASSERT(new_capacity > bw->capacity);
		//FLAC__ASSERT(new_capacity >= bw->words + ((bw->bits + bits_to_add + FLAC__BITS_PER_WORD - 1) / FLAC__BITS_PER_WORD));
		}

		// try {
			this.buffer = Arrays.copyOf( this.buffer, new_capacity );
			this.byte_buffer.bytebuffer = new byte[new_capacity * FLAC__BYTES_PER_WORD];
			this.capacity = new_capacity;
		//} catch(OutOfMemoryError e) {
		//	return false;
		//}
		return true;
	}

	/***********************************************************************
	 *
	 * Class constructor/destructor
	 *
	 ***********************************************************************/

	// FLAC__bitwriter_new(), FLAC__bitwriter_delete(BitWriter bw)
	BitWriter() {
	}

	/***********************************************************************
	 *
	 * Public class methods
	 *
	 ***********************************************************************/

	final boolean init()
	{
		//FLAC__ASSERT(0 != bw);

		this.words = this.bits = 0;
		this.capacity = FLAC__BITWRITER_DEFAULT_CAPACITY;
		//try {
			this.buffer = new int[this.capacity];
			this.byte_buffer.bytebuffer = new byte[this.capacity * FLAC__BYTES_PER_WORD];
		//} catch( OutOfMemoryError e ) {
		//	return false;
		//}

		return true;
	}

	final void free()
	{
		//FLAC__ASSERT(0 != bw);

		this.buffer = null;
		this.capacity = 0;
		this.words = this.bits = 0;
	}

	final void clear()
	{
		this.words = this.bits = 0;
	}


	@SuppressWarnings("boxing")
	final void dump(final java.io.PrintStream out)
	{
		int i, j;
		//if( bw == null ) {
		//	System.out.print("bitwriter is NULL\n");
		//} else
		{
			out.printf("bitwriter: capacity=%d words=%d bits=%d total_bits=%d\n", this.capacity, this.words, this.bits, (this.words * FLAC__BITS_PER_WORD + this.bits));

			for( i = 0; i < this.words; i++ ) {
				out.printf("%08X: ", i);
				for( j = 0; j < FLAC__BITS_PER_WORD; j++ ) {
					out.printf("%01d", (this.buffer[i] & (1 << (FLAC__BITS_PER_WORD - j - 1))) != 0 ? 1 : 0);
				}
				out.print("\n");
			}
			if( this.bits > 0 ) {
				out.printf("%08X: ", i);
				for( j = 0; j < this.bits; j++ ) {
					out.printf("%01d", (this.accum & (1 << (this.bits - j - 1))) != 0 ? 1 : 0);
				}
				out.print("\n");
			}
		}
	}

	final int get_write_crc16() throws OutOfMemoryError// java: changed. status changed by OutOfMemoryError exception
	{
		//FLAC__ASSERT((bw->bits & 7) == 0); /* assert that we're byte-aligned */

		final BitWriterHelperStruct buf = get_buffer( /* buffer, bytes */ );

		final int crc = CRC.crc16( buf.bytebuffer, buf.bytes );
		release_buffer();
		return crc;
	}

	final int get_write_crc8() throws OutOfMemoryError// java: changed. status changed by OutOfMemoryError exception
	{
		//FLAC__ASSERT((bw->bits & 7) == 0); /* assert that we're byte-aligned */

		final BitWriterHelperStruct buf = get_buffer( /* buffer, bytes */ );

		final int crc = CRC.crc8( buf.bytebuffer, buf.bytes );
		release_buffer();
		return crc;
	}

	/* java: used only for asserts
	private final boolean is_byte_aligned()
	{
		return ((this.bits & 7) == 0);
	}
	*/

	final int get_input_bits_unconsumed()
	{
		// return FLAC__TOTAL_BITS( bw );
		return (this.words * FLAC__BITS_PER_WORD + this.bits);
	}

	/** @return null - error, byte_buffer.bytebuffer - data, byte_buffer.bytes - bytes */
	final BitWriterHelperStruct /* boolean */ get_buffer(/* final byte[][] buffer, int[] bytes */) throws OutOfMemoryError
	{
		//FLAC__ASSERT((bw->bits & 7) == 0);
		/* double protection */
		if( (this.bits & 7) != 0 ) {
			return null;// false;
		}
		/* if we have bits in the accumulator we have to flush those to the buffer first */
		if( this.bits != 0 ) {
			//FLAC__ASSERT(bw->words <= bw->capacity);
			if( this.words == this.capacity && ! bitwriter_grow_( FLAC__BITS_PER_WORD ) ) {
				return null;// false;
			}
			/* append bits as complete word to buffer, but don't change bw->accum or bw->bits */
			this.buffer[this.words] = this.accum << (FLAC__BITS_PER_WORD - this.bits);
		}
		/* now we can just return what we have */
		//buffer[0] = (byte[])this.buffer;
		//bytes[0] = (FLAC__BYTES_PER_WORD * this.words) + (this.bits >>> 3);
		//
		final int full_words = FLAC__BYTES_PER_WORD * this.words;
		int trailing_bytes = this.bits >>> 3;
		//final byte[] buffer = new byte[full_words + trailing_bytes];
		this.byte_buffer.bytes = full_words + trailing_bytes;
		final byte[] bb = this.byte_buffer.bytebuffer;// java
		ByteBuffer.wrap( bb, 0, bb.length ).order( ByteOrder.BIG_ENDIAN ).asIntBuffer().put( this.buffer, 0, this.words );
		if( trailing_bytes != 0 ) {// TODO maybe there is more elegant variant?
			int last_word = this.buffer[this.words] >>> ((FLAC__BYTES_PER_WORD - trailing_bytes) << 3);
			do {
				bb[full_words + (--trailing_bytes)] = (byte)last_word;
				last_word >>>= 8;
			} while( trailing_bytes > 0 );
		}
		return this.byte_buffer;// true;
	}

	final void release_buffer()
	{
		/* nothing to do.  in the future, strict checking of a 'writer-is-in-
		 * get-mode' flag could be added everywhere and then cleared here
		 */
		//(void)bw;
	}

	final boolean write_zeroes(int nbits)
	{
		//FLAC__ASSERT(0 != bw);
		//FLAC__ASSERT(0 != bw->buffer);

		if( nbits == 0 ) {
			return true;
		}
		/* slightly pessimistic size check but faster than "<= bw->words + (bw->bits+bits+FLAC__BITS_PER_WORD-1)/FLAC__BITS_PER_WORD" */
		if( this.capacity <= this.words + nbits && ! bitwriter_grow_( nbits ) ) {
			return false;
		}
		/* first part gets to word alignment */
		if( this.bits != 0 ) {
			int n = FLAC__BITS_PER_WORD - this.bits;
			if( n > nbits ) {
				n = nbits;
			}
			this.accum <<= n;
			nbits -= n;
			this.bits += n;
			if( this.bits == FLAC__BITS_PER_WORD ) {
				this.buffer[this.words++] = this.accum;
				this.bits = 0;
			} else {
				return true;
			}
		}
		/* do whole words */
		final int[] buf = this.buffer;// java
		int nwords = this.words;// java
		while( nbits >= FLAC__BITS_PER_WORD ) {
			buf[nwords++] = 0;
			nbits -= FLAC__BITS_PER_WORD;
		}
		this.words = nwords;
		/* do any leftovers */
		if( nbits > 0 ) {
			this.accum = 0;
			this.bits = nbits;
		}
		return true;
	}

	private final boolean write_raw_uint32_nocheck(final int val, final int nbits)
	{
		/* WATCHOUT: code does not work with <32bit words; we can make things much faster with this assertion */
		// FLAC__ASSERT(FLAC__BITS_PER_WORD >= 32);

		// if( this == null || this.buffer == null )
		//	return false;

		if( nbits > 32 ) {
			return false;
		}

		if( nbits == 0 ) {
			return true;
		}

		// FLAC__ASSERT((nbits == 32) || (val>>nbits == 0));

		/* slightly pessimistic size check but faster than "<= bw->words + (bw->bits+bits+FLAC__BITS_PER_WORD-1)/FLAC__BITS_PER_WORD" */
		if( this.capacity <= this.words + nbits && ! bitwriter_grow_( nbits ) ) {
			return false;
		}

		final int left = FLAC__BITS_PER_WORD - this.bits;
		if( nbits < left ) {
			this.accum <<= nbits;
			this.accum |= val;
			this.bits += nbits;
		}
		else if( this.bits != 0 ) { /* WATCHOUT: if bw->bits == 0, left==FLAC__BITS_PER_WORD and bw->accum<<=left is a NOP instead of setting to 0 */
			this.accum <<= left;
			this.accum |= val >>> (this.bits = nbits - left);
			this.buffer[this.words++] = this.accum;
			this.accum = val; /* unused top bits can contain garbage */
		}
		else { /* at this point bits == FLAC__BITS_PER_WORD == 32  and  bw->bits == 0 */
			this.buffer[this.words++] = val;
		}

		return true;
	}

	final boolean write_raw_uint32(final int val, final int raw_bits)
	{
		/* check that unused bits are unset */
		if( (raw_bits < 32) && (val >> raw_bits != 0) ) {
			return false;
		}

		return write_raw_uint32_nocheck( val, raw_bits );
	}

	final boolean write_raw_int32(int val, final int nbits)
	{
		/* zero-out unused bits */
		if( nbits < 32 ) {
			val &= (~(0xffffffff << nbits));
		}

		return write_raw_uint32_nocheck( val, nbits );
	}

	final boolean write_raw_uint64(final long val, final int nbits)
	{
		/* this could be a little faster but it's not used for much */
		if( nbits > 32 ) {
			return
				write_raw_uint32( (int)(val >>> 32), nbits - 32 ) &&
				write_raw_uint32_nocheck( (int)val, 32 );
		}// else {
			return write_raw_uint32( (int)val, nbits );
		//}
	}

	final boolean write_raw_int64(final long val, final int nbits)
	{
		long uval = val;
		/* zero-out unused bits */
		if( nbits < 64 ) {
			uval &= (~(0xFFFFFFFFFFFFFFFFL << nbits));
		}
		return write_raw_uint64( uval, nbits );
	}

	final boolean write_raw_uint32_little_endian(final int val)
	{
		/* this doesn't need to be that fast as currently it is only used for vorbis comments */

		if( ! write_raw_uint32_nocheck( val & 0xff, 8 ) ) {
			return false;
		}
		if( ! write_raw_uint32_nocheck( (val >>> 8) & 0xff, 8 ) ) {
			return false;
		}
		if( ! write_raw_uint32_nocheck( (val >>> 16) & 0xff, 8 ) ) {
			return false;
		}
		if( ! write_raw_uint32_nocheck( val >>> 24, 8 ) ) {
			return false;
		}

		return true;
	}

	final boolean write_byte_block(final byte vals[], final int nvals)
	{
		/* grow capacity upfront to prevent constant reallocation during writes */
		if( this.capacity <= this.words + nvals / (FLAC__BITS_PER_WORD / 8) + 1 && !bitwriter_grow_( nvals << 3 ) ) {
			return false;
		}

		/* this could be faster but currently we don't need it to be since it's only used for writing metadata */
		for( int i = 0; i < nvals; i++ ) {
			if( ! write_raw_uint32_nocheck( vals[i] & 0xff, 8 ) ) {
				return false;
			}
		}

		return true;
	}

	final boolean write_unary_unsigned(int val)
	{
		if( val < 32 ) {
			return write_raw_uint32_nocheck( 1, ++val );
		}// else {
			return
				write_zeroes( val ) &&
				write_raw_uint32_nocheck( 1, 1 );
		//}
	}

	/* FIXME unused
	private static int rice_bits(int val, int parameter)
	{
		//FLAC__ASSERT(parameter < 32);

		// fold signed to uint32_t; actual formula is: negative(v)? -2v-1 : 2v
		int uval = val;
		uval <<= 1;
		uval ^= (val>>31);

		return 1 + parameter + (uval >> parameter);
	}
	*/

/* #if 0 // UNUSED
	private static int golomb_bits_signed(int val, int parameter)
	{
		int bits, msbs, uval;
		int k;

		//FLAC__ASSERT(parameter > 0);

		// fold signed to unsigned
		if( val < 0 )
			uval = (((-(++val)) << 1) + 1);
		else
			uval = (val << 1);

		k = Format.FLAC__bitmath_ilog2( parameter );
		if( parameter == 1 << k ) {
			//FLAC__ASSERT(k <= 30);

			msbs = uval >>> k;
			bits = 1 + k + msbs;
		}
		else {
			int q, r, d;

			d = (1 << (k + 1)) - parameter;
			q = uval / parameter;
			r = uval - (q * parameter);

			bits = 1 + q + k;
			if( r >= d )
				bits++;
		}
		return bits;
	}

	private static int golomb_bits_unsigned(int uval, int parameter)
	{
		int bits, msbs;
		int k;

		//FLAC__ASSERT(parameter > 0);

		k = Format.FLAC__bitmath_ilog2( parameter );
		if( parameter == 1 << k ) {
			//FLAC__ASSERT(k <= 30);

			msbs = uval >>> k;
			bits = 1 + k + msbs;
		}
		else {
			int q, r, d;

			d = (1 << (k + 1)) - parameter;
			q = uval / parameter;
			r = uval - (q * parameter);

			bits = 1 + q + k;
			if( r >= d )
				bits++;
		}
		return bits;
	}
//#endif /* UNUSED */

	/* FIXME never used
	private final boolean write_rice_signed(int val, int parameter)
	{
		int total_bits, interesting_bits, msbs;
		int uval, pattern;

		//FLAC__ASSERT(0 != bw);
		//FLAC__ASSERT(0 != bw->buffer);
		//FLAC__ASSERT(parameter < 32);

		// fold signed to uint32_t; actual formula is: negative(v)? -2v-1 : 2v
		uval = val;
		uval <<= 1;
		uval ^= (val >> 31);

		msbs = uval >>> parameter;
		interesting_bits = 1 + parameter;
		total_bits = interesting_bits + msbs;
		pattern = 1 << parameter; // the unary end bit
		pattern |= (uval & ((1 << parameter) - 1)); // the binary LSBs

		if( total_bits <= 32 )
			return write_raw_uint32( pattern, total_bits );
		else
			return
				write_zeroes( msbs ) && // write the unary MSBs
				write_raw_uint32( pattern, interesting_bits ); // write the unary end bit and binary LSBs
	}
	*/

	final boolean write_rice_signed_block(final int[] vals, int offset, int nvals, final int parameter)
	{
		final int mask1 = 0xffffffff << parameter; /* we val|=mask1 to set the stop bit above it... */
		final int mask2 = 0xffffffff >>> (31 - parameter); /* ...then mask off the bits above the stop bit with val&=mask2*/
		final int lsbits = 1 + parameter;

		//FLAC__ASSERT(0 != bw);
		//FLAC__ASSERT(0 != bw->buffer);
		//FLAC__ASSERT(parameter < 31);
		/* WATCHOUT: code does not work with <32bit words; we can make things much faster with this assertion */
		//FLAC__ASSERT(FLAC__BITS_PER_WORD >= 32);

		while( nvals != 0 ) {
			/* fold signed to uint32_t; actual formula is: negative(v)? -2v-1 : 2v */
			int uval = vals[offset];
			uval <<= 1;
			uval ^= (vals[offset] >> 31);

			int msbits = uval >>> parameter;
			final int total_bits = lsbits + msbits;

			if( this.bits != 0 && this.bits + total_bits < FLAC__BITS_PER_WORD ) { /* i.e. if the whole thing fits in the current bwword */
				/* ^^^ if bw->bits is 0 then we may have filled the buffer and have no free bwword to work in */
				this.bits += total_bits;
				uval |= mask1; /* set stop bit */
				uval &= mask2; /* mask off unused top bits */
				this.accum <<= total_bits;
				this.accum |= uval;
			}
			else {
				/* slightly pessimistic size check but faster than "<= bw->words + (bw->bits+msbits+lsbits+FLAC__BITS_PER_WORD-1)/FLAC__BITS_PER_WORD" */
				/* OPT: pessimism may cause flurry of false calls to grow_ which eat up all savings before it */
				if( this.capacity <= this.words + this.bits + msbits + 1 /* lsbits always fit in 1 bwword */ && ! bitwriter_grow_( total_bits ) ) {
					return false;
				}

				if( msbits != 0 ) {
					/* first part gets to word alignment */
					if( this.bits != 0 ) {
						final int left = FLAC__BITS_PER_WORD - this.bits;
						if( msbits < left ) {
							this.accum <<= msbits;
							this.bits += msbits;
							//goto break1;
						}
						else {
							this.accum <<= left;
							msbits -= left;
							this.buffer[this.words++] = this.accum;
							this.bits = 0;
							// java: added to avoid goto break1
							/* do whole words */
							while( msbits >= FLAC__BITS_PER_WORD ) {
								this.buffer[this.words++] = 0;
								msbits -= FLAC__BITS_PER_WORD;
							}
							/* do any leftovers */
							if( msbits > 0 ) {
								this.accum = 0;
								this.bits = msbits;
							}
						}
					} else {// java: added to avoid goto break1
						/* do whole words */
						final int[] buf = this.buffer;// java
						int nwords = this.words;// java
						while( msbits >= FLAC__BITS_PER_WORD ) {
							buf[nwords++] = 0;
							msbits -= FLAC__BITS_PER_WORD;
						}
						this.words = nwords;
						/* do any leftovers */
						if( msbits > 0 ) {
							this.accum = 0;
							this.bits = msbits;
						}
					}
				}
	// break1:
				uval |= mask1; /* set stop bit */
				uval &= mask2; /* mask off unused top bits */

				final int left = FLAC__BITS_PER_WORD - this.bits;
				if( lsbits < left ) {
					this.accum <<= lsbits;
					this.accum |= uval;
					this.bits += lsbits;
				}
				else {
					/* if bw->bits == 0, left==FLAC__BITS_PER_WORD which will always
					 * be > lsbits (because of previous assertions) so it would have
					 * triggered the (lsbits<left) case above.
					 */
					//FLAC__ASSERT(bw->bits);
					//FLAC__ASSERT(left < FLAC__BITS_PER_WORD);
					this.accum <<= left;
					this.accum |= uval >>> (this.bits = lsbits - left);
					this.buffer[this.words++] = this.accum;
					this.accum = uval; /* unused top bits can contain garbage */
				}
			}
			offset++;//vals++;
			nvals--;
		}
		return true;
	}

/*#if 0 // UNUSED
	private final boolean write_golomb_signed(int val, int parameter)
	{
		int total_bits, msbs, uval;
		int k;

		//FLAC__ASSERT(0 != bw);
		//FLAC__ASSERT(0 != bw->buffer);
		//FLAC__ASSERT(parameter > 0);

		// fold signed to unsigned
		if( val < 0 )
			uval = (((-(++val)) << 1) + 1);
		else
			uval = (val << 1);

		k = Format.FLAC__bitmath_ilog2( parameter );
		if( parameter == 1 << k ) {
			int pattern;

			//FLAC__ASSERT(k <= 30);

			msbs = uval >>> k;
			total_bits = 1 + k + msbs;
			pattern = 1 << k; // the unary end bit
			pattern |= (uval & ((1 << k) - 1)); // the binary LSBs

			if( total_bits <= 32 ) {
				if( ! write_raw_uint32( pattern, total_bits ) )
					return false;
			}
			else {
				// write the unary MSBs
				if( ! write_zeroes( msbs ) )
					return false;
				// write the unary end bit and binary LSBs
				if( ! write_raw_uint32( pattern, k + 1 ) )
					return false;
			}
		}
		else {
			int q, r, d;

			d = (1 << (k + 1)) - parameter;
			q = uval / parameter;
			r = uval - (q * parameter);
			// write the unary MSBs
			if( ! write_zeroes( q ) )
				return false;
			// write the unary end bit
			if( ! write_raw_uint32( 1, 1 ) )
				return false;
			// write the binary LSBs
			if( r >= d ) {
				if( ! write_raw_uint32( r + d, k + 1 ) )
					return false;
			}
			else {
				if( ! write_raw_uint32( r, k ) )
					return false;
			}
		}
		return true;
	}

	private final boolean write_golomb_unsigned(int uval, int parameter)
	{
		int total_bits, msbs;
		int k;

		//FLAC__ASSERT(0 != bw);
		//FLAC__ASSERT(0 != bw->buffer);
		//FLAC__ASSERT(parameter > 0);

		k = Format.FLAC__bitmath_ilog2( parameter );
		if( parameter == 1 << k ) {
			int pattern;

			//FLAC__ASSERT(k <= 30);

			msbs = uval >>> k;
			total_bits = 1 + k + msbs;
			pattern = 1 << k; // the unary end bit
			pattern |= (uval & ((1 << k) - 1)); // the binary LSBs

			if( total_bits <= 32 ) {
				if( ! write_raw_uint32( pattern, total_bits ) )
					return false;
			}
			else {
				// write the unary MSBs
				if( ! write_zeroes( msbs ) )
					return false;
				// write the unary end bit and binary LSBs
				if( ! write_raw_uint32( pattern, k + 1 ) )
					return false;
			}
		}
		else {
			int q, r, d;

			d = (1 << (k + 1)) - parameter;
			q = uval / parameter;
			r = uval - (q * parameter);
			// write the unary MSBs
			if( ! write_zeroes( q ) )
				return false;
			// write the unary end bit
			if( ! write_raw_uint32( 1, 1 ) )
				return false;
			/// write the binary LSBs
			if( r >= d ) {
				if( ! write_raw_uint32( r + d, k + 1 ) )
					return false;
			}
			else {
				if( ! write_raw_uint32( r, k ) )
					return false;
			}
		}
		return true;
	}
//#endif /* UNUSED */

	final boolean write_utf8_uint32(final int val)
	{
		boolean ok = true;

		//FLAC__ASSERT(0 != bw);
		//FLAC__ASSERT(0 != bw->buffer);

		if( (val & 0x80000000) != 0 ) { /* this version only handles 31 bits */
			return false;
		}

		if( (val & 0xffffff80) == 0 ) {// if( val < 0x80 )
			return write_raw_uint32_nocheck( val, 8 );
		}
		else if( (val & 0xfffff800) == 0 ) {// if( val < 0x800 )
			ok &= write_raw_uint32_nocheck( 0xC0 | (val >>> 6), 8 );
			ok &= write_raw_uint32_nocheck( 0x80 | (val & 0x3F), 8 );
		}
		else if( (val & 0xffff0000) == 0 ) {// if( val < 0x10000 )
			ok &= write_raw_uint32_nocheck( 0xE0 | (val >>> 12), 8 );
			ok &= write_raw_uint32_nocheck( 0x80 | ((val >>> 6) & 0x3F), 8 );
			ok &= write_raw_uint32_nocheck( 0x80 | (val & 0x3F), 8 );
		}
		else if( (val & 0xffe00000) == 0 ) {// if( val < 0x200000 )
			ok &= write_raw_uint32_nocheck( 0xF0 | (val >>> 18), 8 );
			ok &= write_raw_uint32_nocheck( 0x80 | ((val >>> 12) & 0x3F), 8 );
			ok &= write_raw_uint32_nocheck( 0x80 | ((val >>> 6) & 0x3F), 8 );
			ok &= write_raw_uint32_nocheck( 0x80 | (val & 0x3F), 8 );
		}
		else if( (val & 0xfc000000) == 0 ) {// if( val < 0x4000000 )
			ok &= write_raw_uint32_nocheck( 0xF8 | (val >>> 24), 8 );
			ok &= write_raw_uint32_nocheck( 0x80 | ((val >>> 18) & 0x3F), 8 );
			ok &= write_raw_uint32_nocheck( 0x80 | ((val >>> 12) & 0x3F), 8 );
			ok &= write_raw_uint32_nocheck( 0x80 | ((val >>> 6) & 0x3F), 8 );
			ok &= write_raw_uint32_nocheck( 0x80 | (val & 0x3F), 8 );
		}
		else {
			ok &= write_raw_uint32_nocheck( 0xFC | (val >>> 30), 8 );
			ok &= write_raw_uint32_nocheck( 0x80 | ((val >>> 24) & 0x3F), 8 );
			ok &= write_raw_uint32_nocheck( 0x80 | ((val >>> 18) & 0x3F), 8 );
			ok &= write_raw_uint32_nocheck( 0x80 | ((val >>> 12) & 0x3F), 8 );
			ok &= write_raw_uint32_nocheck( 0x80 | ((val >>> 6) & 0x3F), 8 );
			ok &= write_raw_uint32_nocheck( 0x80 | (val & 0x3F), 8 );
		}

		return ok;
	}

	final boolean write_utf8_uint64(final long val)
	{
		boolean ok = true;

		//FLAC__ASSERT(0 != bw);
		//FLAC__ASSERT(0 != bw->buffer);

		if( (val & 0xFFFFFFF000000000L) != 0 ) { /* this version only handles 36 bits */
			return false;
		}

		final int ival = (int) val;
		if( (val & 0xffffffffffffff80L) == 0 ) {// if( val < 0x80 )
			return write_raw_uint32_nocheck( ival, 8);
		}
		else if( (val & 0xfffffffffffff800L) == 0 ) {// if( val < 0x800 )
			ok &= write_raw_uint32_nocheck( 0xC0 | (ival >>> 6), 8 );
			ok &= write_raw_uint32_nocheck( 0x80 | (ival & 0x3F), 8 );
		}
		else if( (val & 0xffffffffffff0000L) == 0 ) {// if( val < 0x10000 )
			ok &= write_raw_uint32_nocheck( 0xE0 | (ival >>> 12), 8 );
			ok &= write_raw_uint32_nocheck( 0x80 | ((ival >>> 6) & 0x3F), 8 );
			ok &= write_raw_uint32_nocheck( 0x80 | (ival & 0x3F), 8 );
		}
		else if( (val & 0xffffffffffe00000L) == 0 ) {// if( val < 0x200000 )
			ok &= write_raw_uint32_nocheck( 0xF0 | (ival >>> 18), 8 );
			ok &= write_raw_uint32_nocheck( 0x80 | ((ival >>> 12) & 0x3F), 8 );
			ok &= write_raw_uint32_nocheck( 0x80 | ((ival >>> 6) & 0x3F), 8 );
			ok &= write_raw_uint32_nocheck( 0x80 | (ival & 0x3F), 8 );
		}
		else if( (val & 0xfffffffffc000000L) == 0 ) {// if( val < 0x4000000 )
			ok &= write_raw_uint32_nocheck( 0xF8 | (ival >>> 24), 8 );
			ok &= write_raw_uint32_nocheck( 0x80 | ((ival >>> 18) & 0x3F), 8 );
			ok &= write_raw_uint32_nocheck( 0x80 | ((ival >>> 12) & 0x3F), 8 );
			ok &= write_raw_uint32_nocheck( 0x80 | ((ival >>> 6) & 0x3F), 8 );
			ok &= write_raw_uint32_nocheck( 0x80 | (ival & 0x3F), 8 );
		}
		else if( (val & 0xffffffff80000000L) == 0 ) {// if( val < 0x80000000 )
			ok &= write_raw_uint32_nocheck( 0xFC | (int)(val >>> 30), 8 );
			ok &= write_raw_uint32_nocheck( 0x80 | ((ival >>> 24) & 0x3F), 8 );
			ok &= write_raw_uint32_nocheck( 0x80 | ((ival >>> 18) & 0x3F), 8 );
			ok &= write_raw_uint32_nocheck( 0x80 | ((ival >>> 12) & 0x3F), 8 );
			ok &= write_raw_uint32_nocheck( 0x80 | ((ival >>> 6) & 0x3F), 8 );
			ok &= write_raw_uint32_nocheck( 0x80 | (ival & 0x3F), 8 );
		}
		else {
			ok &= write_raw_uint32_nocheck( 0xFE, 8 );
			ok &= write_raw_uint32_nocheck( 0x80 | (int)((val >>> 30) & 0x3F), 8 );
			ok &= write_raw_uint32_nocheck( 0x80 | ((ival >>> 24) & 0x3F), 8 );
			ok &= write_raw_uint32_nocheck( 0x80 | ((ival >>> 18) & 0x3F), 8 );
			ok &= write_raw_uint32_nocheck( 0x80 | ((ival >>> 12) & 0x3F), 8 );
			ok &= write_raw_uint32_nocheck( 0x80 | ((ival >>> 6) & 0x3F), 8 );
			ok &= write_raw_uint32_nocheck( 0x80 | (ival & 0x3F), 8 );
		}

		return ok;
	}

	final boolean zero_pad_to_byte_boundary()
	{
		/* 0-pad to byte boundary */
		if( (this.bits & 7) != 0 ) {
			return write_zeroes( 8 - (this.bits & 7) );
		}// else {
			return true;
		//}
	}

}
