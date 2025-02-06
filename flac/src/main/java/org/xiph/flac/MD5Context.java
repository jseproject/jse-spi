package org.xiph.flac;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

class MD5Context {
/*
	typedef union {
		FLAC__byte *p8;
		FLAC__int16 *p16;
		FLAC__int32 *p32;
	} FLAC__multibyte;
*/
	private final int in[] = new int[16];
	private final int buf[] = new int[4];
	private final int bytes[] = new int[2];
	private byte[] internal_buf;// FLAC__multibyte
	//private int capacity;// java: changed to internal_buf.length

	private final void clear() {
		int i = 16;
		int[] b = this.in;
		do{ b[--i] = 0; } while( i > 0 );
		b = buf;
		b[0] = 0; b[1] = 0; b[2] = 0; b[3] = 0;
		b = bytes;
		b[0] = 0; b[1] = 0;
		internal_buf = null;
		//capacity = 0;
	}

	/* The four core functions - F1 is optimized somewhat */

	private static int MD5STEP_F1(int w, final int x, final int y, final int z, final int in, final int s) {
		w += (z ^ (x & (y ^ z))) + in;
		return (w << s | w >>> (32 - s)) + x;
	}

	private static int MD5STEP_F2(int w, final int x, final int y, final int z, final int in, final int s) {
		w += (y ^ (z & (x ^ y))) + in;
		return (w << s | w >>> (32 - s)) + x;
	}

	private static int MD5STEP_F3(int w, final int x, final int y, final int z, final int in, final int s) {
		w += (x ^ y ^ z) + in;
		return (w << s | w >>> (32 - s)) + x;
	}

	private static int MD5STEP_F4(int w, final int x, final int y, final int z, final int in, final int s) {
		w += (y ^ (x | ~z)) + in;
		return (w << s | w >>> (32 - s)) + x;
	}

	/**
	 * The core of the MD5 algorithm, this alters an existing MD5 hash to
	 * reflect the addition of 16 longwords of new data.  MD5Update blocks
	 * the data and converts bytes into longwords for this routine.
	 */
	private static void MD5Transform(final int buf[], final int in[])
	{
		int a = buf[0];
		int b = buf[1];
		int c = buf[2];
		int d = buf[3];

		a = MD5STEP_F1( a, b, c, d, in[0] + 0xd76aa478, 7 );
		d = MD5STEP_F1( d, a, b, c, in[1] + 0xe8c7b756, 12 );
		c = MD5STEP_F1( c, d, a, b, in[2] + 0x242070db, 17 );
		b = MD5STEP_F1( b, c, d, a, in[3] + 0xc1bdceee, 22 );
		a = MD5STEP_F1( a, b, c, d, in[4] + 0xf57c0faf, 7 );
		d = MD5STEP_F1( d, a, b, c, in[5] + 0x4787c62a, 12 );
		c = MD5STEP_F1( c, d, a, b, in[6] + 0xa8304613, 17 );
		b = MD5STEP_F1( b, c, d, a, in[7] + 0xfd469501, 22 );
		a = MD5STEP_F1( a, b, c, d, in[8] + 0x698098d8, 7 );
		d = MD5STEP_F1( d, a, b, c, in[9] + 0x8b44f7af, 12 );
		c = MD5STEP_F1( c, d, a, b, in[10] + 0xffff5bb1, 17 );
		b = MD5STEP_F1( b, c, d, a, in[11] + 0x895cd7be, 22 );
		a = MD5STEP_F1( a, b, c, d, in[12] + 0x6b901122, 7 );
		d = MD5STEP_F1( d, a, b, c, in[13] + 0xfd987193, 12 );
		c = MD5STEP_F1( c, d, a, b, in[14] + 0xa679438e, 17 );
		b = MD5STEP_F1( b, c, d, a, in[15] + 0x49b40821, 22 );

		a = MD5STEP_F2( a, b, c, d, in[1] + 0xf61e2562, 5 );
		d = MD5STEP_F2( d, a, b, c, in[6] + 0xc040b340, 9 );
		c = MD5STEP_F2( c, d, a, b, in[11] + 0x265e5a51, 14 );
		b = MD5STEP_F2( b, c, d, a, in[0] + 0xe9b6c7aa, 20 );
		a = MD5STEP_F2( a, b, c, d, in[5] + 0xd62f105d, 5 );
		d = MD5STEP_F2( d, a, b, c, in[10] + 0x02441453, 9 );
		c = MD5STEP_F2( c, d, a, b, in[15] + 0xd8a1e681, 14 );
		b = MD5STEP_F2( b, c, d, a, in[4] + 0xe7d3fbc8, 20 );
		a = MD5STEP_F2( a, b, c, d, in[9] + 0x21e1cde6, 5 );
		d = MD5STEP_F2( d, a, b, c, in[14] + 0xc33707d6, 9 );
		c = MD5STEP_F2( c, d, a, b, in[3] + 0xf4d50d87, 14 );
		b = MD5STEP_F2( b, c, d, a, in[8] + 0x455a14ed, 20 );
		a = MD5STEP_F2( a, b, c, d, in[13] + 0xa9e3e905, 5 );
		d = MD5STEP_F2( d, a, b, c, in[2] + 0xfcefa3f8, 9 );
		c = MD5STEP_F2( c, d, a, b, in[7] + 0x676f02d9, 14 );
		b = MD5STEP_F2( b, c, d, a, in[12] + 0x8d2a4c8a, 20 );

		a = MD5STEP_F3( a, b, c, d, in[5] + 0xfffa3942, 4 );
		d = MD5STEP_F3( d, a, b, c, in[8] + 0x8771f681, 11 );
		c = MD5STEP_F3( c, d, a, b, in[11] + 0x6d9d6122, 16 );
		b = MD5STEP_F3( b, c, d, a, in[14] + 0xfde5380c, 23 );
		a = MD5STEP_F3( a, b, c, d, in[1] + 0xa4beea44, 4 );
		d = MD5STEP_F3( d, a, b, c, in[4] + 0x4bdecfa9, 11 );
		c = MD5STEP_F3( c, d, a, b, in[7] + 0xf6bb4b60, 16 );
		b = MD5STEP_F3( b, c, d, a, in[10] + 0xbebfbc70, 23 );
		a = MD5STEP_F3( a, b, c, d, in[13] + 0x289b7ec6, 4 );
		d = MD5STEP_F3( d, a, b, c, in[0] + 0xeaa127fa, 11 );
		c = MD5STEP_F3( c, d, a, b, in[3] + 0xd4ef3085, 16 );
		b = MD5STEP_F3( b, c, d, a, in[6] + 0x04881d05, 23 );
		a = MD5STEP_F3( a, b, c, d, in[9] + 0xd9d4d039, 4 );
		d = MD5STEP_F3( d, a, b, c, in[12] + 0xe6db99e5, 11 );
		c = MD5STEP_F3( c, d, a, b, in[15] + 0x1fa27cf8, 16 );
		b = MD5STEP_F3( b, c, d, a, in[2] + 0xc4ac5665, 23 );

		a = MD5STEP_F4( a, b, c, d, in[0] + 0xf4292244, 6 );
		d = MD5STEP_F4( d, a, b, c, in[7] + 0x432aff97, 10 );
		c = MD5STEP_F4( c, d, a, b, in[14] + 0xab9423a7, 15 );
		b = MD5STEP_F4( b, c, d, a, in[5] + 0xfc93a039, 21 );
		a = MD5STEP_F4( a, b, c, d, in[12] + 0x655b59c3, 6 );
		d = MD5STEP_F4( d, a, b, c, in[3] + 0x8f0ccc92, 10 );
		c = MD5STEP_F4( c, d, a, b, in[10] + 0xffeff47d, 15 );
		b = MD5STEP_F4( b, c, d, a, in[1] + 0x85845dd1, 21 );
		a = MD5STEP_F4( a, b, c, d, in[8] + 0x6fa87e4f, 6 );
		d = MD5STEP_F4( d, a, b, c, in[15] + 0xfe2ce6e0, 10 );
		c = MD5STEP_F4( c, d, a, b, in[6] + 0xa3014314, 15 );
		b = MD5STEP_F4( b, c, d, a, in[13] + 0x4e0811a1, 21 );
		a = MD5STEP_F4( a, b, c, d, in[4] + 0xf7537e82, 6 );
		d = MD5STEP_F4( d, a, b, c, in[11] + 0xbd3af235, 10 );
		c = MD5STEP_F4( c, d, a, b, in[2] + 0x2ad7d2bb, 15 );
		b = MD5STEP_F4( b, c, d, a, in[9] + 0xeb86d391, 21 );

		buf[0] += a;
		buf[1] += b;
		buf[2] += c;
		buf[3] += d;
	}

	private static void memcpy(final byte[] src, int src_offset, final int[] dst, final int dst_byte_offset, int len) {
		int t = dst_byte_offset & 3;
		if( t > 0 ) {
			int v = dst[dst_byte_offset >> 2];
			v |= 0xffffffff << (t << 3);
			for( ; t > 0 && len > 0; t--, len-- ) {
				v &= (src[src_offset++] & 0xff) << (t << 3);
			}
			dst[dst_byte_offset >> 2] = v;
		}
		t = len >> 2;
		ByteBuffer.wrap( src, src_offset, len ).order( ByteOrder.LITTLE_ENDIAN ).asIntBuffer().get( dst, dst_byte_offset, t );
		int i = (t << 2);
		len -= i;
		if( len > 0 ) {
			len = 4 - len;
			int v = dst[++t];
			v |= 0xffffffff << (len << 3);
			for( ; len > 0; len-- ) {
				v &= (src[i++] & 0xff) << (len << 3);
			}
			dst[t] = v;
		}
	}
	private static void memclear(final int[] dst, int dst_byte_offset, int len) {//, int val) {
		int t = dst_byte_offset & 3;
		dst_byte_offset >>= 2;
		if( t > 0 ) {
			t = 4 - t;
			dst[dst_byte_offset++] &= 0xffffffff >>> (t << 3);
		}
		t = len >> 2;
		int i;
		for( i = t; i > 0; i-- ) {
			dst[dst_byte_offset++] = 0;
		}
		i = (t << 2);
		len -= i;
		if( len > 0 ) {
			len = 4 - len;
			dst[++t] &= 0xffffffff >>> (len << 3);
		}
	}

	/**
	 * Update context to reflect the concatenation of another buffer full
	 * of bytes.
	 */
	private final void MD5Update(final byte[] buff, int len)
	{
		/* Update byte count */

		int t = this.bytes[0];
		if( (this.bytes[0] = t + len) < t ) {
			this.bytes[1]++;	/* Carry from low to high */
		}

		t = 64 - (t & 0x3f);	/* Space available in ctx->in (at least 1) */
		if( t > len ) {
			memcpy( buff, 0, this.in, 64 - t, len );
			return;
		}
		/* First chunk is an odd size */
		memcpy( buff, 0, this.in, 64 - t, t );
		MD5Transform( this.buf, this.in );
		int buf_offset = t;
		len -= t;

		/* Process data in 64-byte chunks */
		final int[] b = this.buf;// java
		final int[] i = this.in;// java
		while( len >= 64 ) {
			ByteBuffer.wrap( buff, buf_offset, 64 ).order( ByteOrder.LITTLE_ENDIAN ).asIntBuffer().get( i, 0, 64 / 4 );
			MD5Transform( b, i );
			buf_offset += 64;
			len -= 64;
		}

		/* Handle any remaining bytes of data. */
		memcpy( buff, buf_offset, i, 0, len );
	}

	/**
	 * Start MD5 accumulation.  Set bit count to 0 and buffer to mysterious
	 * initialization constants.
	 */
	final void MD5Init()
	{
		int[] b = this.buf;// java
		b[0] = 0x67452301;
		b[1] = 0xefcdab89;
		b[2] = 0x98badcfe;
		b[3] = 0x10325476;

		b = this.bytes;
		b[0] = 0;
		b[1] = 0;

		this.internal_buf = null;
		//this.capacity = 0;
	}

	/**
	 * Final wrapup - pad to 64-byte boundary with the bit pattern
	 * 1 0* (64-bit count of bits processed, MSB-first)
	 */
	final void MD5Final(final byte digest[])
	{
		final int bytes0 = this.bytes[0];// java
		int count = bytes0 & 0x3f;	/* Number of bytes in ctx->in */
		//FLAC__byte *p = (FLAC__byte *)ctx.in + count;

		/* Set the first char of padding to 0x80.  There is always room. */
		//*p++ = 0x80;
		int offset = count >> 2;
		final int s = (offset << 2) - count;
		final int[] input = this.in;// java
		input[offset] &= ~(0xff << (s << 3));
		input[offset] |= 0x80 << (s << 3);
		offset = count + 1;

		/* Bytes of padding needed to make 56 bytes (-8..55) */
		count = 56 - offset;

		if( count < 0 ) {	/* Padding forces an extra block */
			memclear( input, offset, count + 8 );
			MD5Transform( this.buf, input );
			count = 56;
			offset = 0;
		}
		memclear( input, offset, count );

		/* Append length in bits and transform */
		input[14] = bytes0 << 3;
		input[15] = this.bytes[1] << 3 | bytes0 >>> 29;
		MD5Transform( this.buf, input );

		ByteBuffer.wrap( digest ).order( ByteOrder.LITTLE_ENDIAN ).asIntBuffer().put( this.buf, 0, 4 );
		this.clear();
		/* if( null != this.internal_buf ) {
			// free( this.internal_buf );
			this.internal_buf = null;
			//this.capacity = 0;
		} */
		//memset( ctx, 0, sizeof(ctx) );	/* In case it's sensitive */
	}

	/**
	 * Convert the incoming audio signal to a byte stream
	 */
	private static void format_input_(final byte[] mbuf, final int signal[][], final int channels, final int samples, final int bytes_per_sample)
	{
		//FLAC__byte *buf_ = mbuf->p8;
		//FLAC__int16 *buf16 = mbuf->p16;
		//FLAC__int32 *buf32 = mbuf->p32;
		int buf = 0;

		/* Storage in the output buffer, buf, is little endian. */

	//#define BYTES_CHANNEL_SELECTOR(bytes, channels)   (bytes * 100 + channels)

		/* First do the most commonly used combinations. */
		switch( bytes_per_sample * 100 + channels ) {//switch (BYTES_CHANNEL_SELECTOR (bytes_per_sample, channels)) {
			/* One byte per sample. */
			case( 1 * 100 + 1 ):// case (BYTES_CHANNEL_SELECTOR (1, 1)):
				for( int sample = 0; sample < samples; sample++ ) {
					mbuf[buf++] = (byte)signal[0][sample];
				}
				return;

			case( 1 * 100 + 2 ):// case (BYTES_CHANNEL_SELECTOR (1, 2)):
				for( int sample = 0; sample < samples; sample++ ) {
					mbuf[buf++] = (byte)signal[0][sample];
					mbuf[buf++] = (byte)signal[1][sample];
				}
				return;

			case( 1 * 100 + 4 ):// case (BYTES_CHANNEL_SELECTOR (1, 4)):
				for( int sample = 0; sample < samples; sample++ ) {
					mbuf[buf++] = (byte)signal[0][sample];
					mbuf[buf++] = (byte)signal[1][sample];
					mbuf[buf++] = (byte)signal[2][sample];
					mbuf[buf++] = (byte)signal[3][sample];
				}
				return;

			case( 1 * 100 + 6 ):// case (BYTES_CHANNEL_SELECTOR (1, 6)):
				for( int sample = 0; sample < samples; sample++ ) {
					mbuf[buf++] = (byte)signal[0][sample];
					mbuf[buf++] = (byte)signal[1][sample];
					mbuf[buf++] = (byte)signal[2][sample];
					mbuf[buf++] = (byte)signal[3][sample];
					mbuf[buf++] = (byte)signal[4][sample];
					mbuf[buf++] = (byte)signal[5][sample];
				}
				return;

			case( 1 * 100 + 8 ):// case (BYTES_CHANNEL_SELECTOR (1, 8)):
				for( int sample = 0; sample < samples; sample++ ) {
					mbuf[buf++] = (byte)signal[0][sample];
					mbuf[buf++] = (byte)signal[1][sample];
					mbuf[buf++] = (byte)signal[2][sample];
					mbuf[buf++] = (byte)signal[3][sample];
					mbuf[buf++] = (byte)signal[4][sample];
					mbuf[buf++] = (byte)signal[5][sample];
					mbuf[buf++] = (byte)signal[6][sample];
					mbuf[buf++] = (byte)signal[7][sample];
				}
				return;

			/* Two bytes per sample. */
			case( 2 * 100 + 1 ):// case (BYTES_CHANNEL_SELECTOR (2, 1)):
				for( int sample = 0; sample < samples; sample++ ) {
					int a_word = signal[0][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
				}
				return;

			case( 2 * 100 + 2 ):// case (BYTES_CHANNEL_SELECTOR (2, 2)):
				for( int sample = 0; sample < samples; sample++ ) {
					int a_word = signal[0][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
					a_word = signal[1][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
				}
				return;

			case( 2 * 100 + 4 ):// case (BYTES_CHANNEL_SELECTOR (2, 4)):
				for( int sample = 0; sample < samples; sample++ ) {
					int a_word = signal[0][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
					a_word = signal[1][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
					a_word = signal[2][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
					a_word = signal[3][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
				}
				return;

			case( 2 * 100 + 6 ):// case (BYTES_CHANNEL_SELECTOR (2, 6)):
				for( int sample = 0; sample < samples; sample++ ) {
					int a_word = signal[0][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
					a_word = signal[1][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
					a_word = signal[2][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
					a_word = signal[3][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
					a_word = signal[4][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
					a_word = signal[5][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
				}
				return;

			case( 2 * 100 + 8 ):// case (BYTES_CHANNEL_SELECTOR (2, 8)):
				for( int sample = 0; sample < samples; sample++ ) {
					int a_word = signal[0][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
					a_word = signal[1][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
					a_word = signal[2][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
					a_word = signal[3][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
					a_word = signal[4][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
					a_word = signal[5][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
					a_word = signal[6][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
					a_word = signal[7][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
				}
				return;

			/* Three bytes per sample. */
			case( 3 * 100 + 1 ):// case (BYTES_CHANNEL_SELECTOR (3, 1)):
				for( int sample = 0; sample < samples; sample++ ) {
					int a_word = signal[0][sample];
					mbuf[buf++] = (byte)a_word; a_word >>= 8;
					mbuf[buf++] = (byte)a_word; a_word >>= 8;
					mbuf[buf++] = (byte)a_word;
				}
				return;

			case( 3 * 100 + 2 ):// case (BYTES_CHANNEL_SELECTOR (3, 2)):
				for( int sample = 0; sample < samples; sample++ ) {
					int a_word = signal[0][sample];
					mbuf[buf++] = (byte)a_word; a_word >>= 8;
					mbuf[buf++] = (byte)a_word; a_word >>= 8;
					mbuf[buf++] = (byte)a_word;
					a_word = signal[1][sample];
					mbuf[buf++] = (byte)a_word; a_word >>= 8;
					mbuf[buf++] = (byte)a_word; a_word >>= 8;
					mbuf[buf++] = (byte)a_word;
				}
				return;

			/* Four bytes per sample. */
			case( 4 * 100 + 1 ):// case (BYTES_CHANNEL_SELECTOR (4, 1)):
				for( int sample = 0; sample < samples; sample++ ) {
					int a_word = signal[0][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
				}
				return;

			case( 4 * 100 + 2 ):// case (BYTES_CHANNEL_SELECTOR (4, 2)):
				for( int sample = 0; sample < samples; sample++ ) {
					int a_word = signal[0][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
					a_word = signal[1][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
				}
				return;

			case( 4 * 100 + 4 ):// case (BYTES_CHANNEL_SELECTOR (4, 4)):
				for( int sample = 0; sample < samples; sample++ ) {
					int a_word = signal[0][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
					a_word = signal[1][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
					a_word = signal[2][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
					a_word = signal[3][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
				}
				return;

			case( 4 * 100 + 6 ):// case (BYTES_CHANNEL_SELECTOR (4, 6)):
				for( int sample = 0; sample < samples; sample++ ) {
					int a_word = signal[0][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
					a_word = signal[1][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
					a_word = signal[2][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
					a_word = signal[3][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
					a_word = signal[4][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
					a_word = signal[5][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
				}
				return;

			case( 4 * 100 + 8 ):// case (BYTES_CHANNEL_SELECTOR (4, 8)):
				for( int sample = 0; sample < samples; sample++ ) {
					int a_word = signal[0][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
					a_word = signal[1][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
					a_word = signal[2][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
					a_word = signal[3][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
					a_word = signal[4][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
					a_word = signal[5][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
					a_word = signal[6][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
					a_word = signal[7][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
				}
				return;

			default:
				break;
		}

		/* General version. */
		switch( bytes_per_sample ) {
			case 1:
				for( int sample = 0; sample < samples; sample++ ) {
					for( int channel = 0; channel < channels; channel++ ) {
						mbuf[buf++] = (byte)signal[channel][sample];
					}
				}
				return;

			case 2:
				for( int sample = 0; sample < samples; sample++ ) {
					for( int channel = 0; channel < channels; channel++ ) {
						int a_word = signal[channel][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
					}
				}
				return;

			case 3:
				for( int sample = 0; sample < samples; sample++ ) {
					for( int channel = 0; channel < channels; channel++ ) {
						int a_word = signal[channel][sample];
						mbuf[buf++] = (byte)a_word; a_word >>= 8;
						mbuf[buf++] = (byte)a_word; a_word >>= 8;
						mbuf[buf++] = (byte)a_word;
					}
				}
				return;

			case 4:
				for( int sample = 0; sample < samples; sample++ ) {
					for( int channel = 0; channel < channels; channel++ ) {
						int a_word = signal[channel][sample]; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word; a_word >>= 8; mbuf[buf++] = (byte)a_word;
					}
				}
				return;

			default:
				break;
		}
	}
	/**
	 * Convert the incoming audio signal to a byte stream and MD5Update it.
	 */
	final boolean MD5Accumulate(final int signal[][], final int channels, final int samples, final int bytes_per_sample)
	{
		/* overflow check */
		if( channels > Format.SIZE_MAX ) {
			return false;
		}
		if( channels * bytes_per_sample > Integer.MAX_VALUE / samples ) {
			return false;
		}

		final int bytes_needed = channels * samples * bytes_per_sample;

		if( this.internal_buf == null || this.internal_buf.length /*this.capacity*/ < bytes_needed ) {
			try {
				this.internal_buf = this.internal_buf == null ? new byte[bytes_needed] : Arrays.copyOf( this.internal_buf, bytes_needed );
			} catch( final OutOfMemoryError e ) {
				// this.capacity = 0;
				return false;
			}
			// this.capacity = bytes_needed;
		}

		format_input_( this.internal_buf, signal, channels, samples, bytes_per_sample );

		MD5Update( this.internal_buf, bytes_needed );

		return true;
	}
}
