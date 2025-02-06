package org.xiph.flac;

/** VERBATIM subframe.  (c.f. <A HREF="../format.html#subframe_verbatim">format specification</A>)
 */
public class Subframe_Verbatim /* extends Subframe*/ {
	/** A pointer to verbatim signal. */
	Object data = null;

	int data_type;// java: for compatibility, data instanceof int[] or long[] can be used

	/*public Subframe_Verbatim() {
		type = FLAC__SUBFRAME_TYPE_VERBATIM;
	}*/

	/*public Subframe_Verbatim(int[] d) {
		data = d;
	}*/

	// stream_encoder_framing.c
	final boolean add_verbatim(final int samples, final int subframe_bps, final int wasted_bits, final BitWriter bw)
	{
		if( ! bw.write_raw_uint32( Format.FLAC__SUBFRAME_TYPE_VERBATIM_BYTE_ALIGNED_MASK | (wasted_bits != 0 ? 1:0),
				Format.FLAC__SUBFRAME_ZERO_PAD_LEN + Format.FLAC__SUBFRAME_TYPE_LEN + Format.FLAC__SUBFRAME_WASTED_BITS_FLAG_LEN ) ) {
			return false;
		}
		if( wasted_bits != 0 ) {
			if(! bw.write_unary_unsigned( wasted_bits - 1 ) ) {
				return false;
			}
		}

		if( this.data_type == Format.FLAC__VERBATIM_SUBFRAME_DATA_TYPE_INT32 ) {
			final int[] signal = (int[])this.data;

			// FLAC__ASSERT(subframe_bps < 33);

			for( int i = 0; i < samples; i++ ) {
				if( ! bw.write_raw_int32( signal[ i ], subframe_bps ) ) {
					return false;
				}
			}
		}
		else {
			final long[] signal = (long[])this.data;

			// FLAC__ASSERT(subframe_bps == 33);

			for( int i = 0; i < samples; i++ ) {
				if( ! bw.write_raw_int64( signal[ i ], subframe_bps ) ) {
					return false;
				}
			}
		}
		return true;
	}
}
