package org.xiph.flac;

/** CONSTANT subframe.  (c.f. <A HREF="../format.html#subframe_constant">format specification</A>)
 */
public class Subframe_Constant /* extends Subframe */ {
	/** The constant signal value. */
	public long value = 0;

	/*public Subframe_Constant() {
		type = FLAC__SUBFRAME_TYPE_CONSTANT;
	}*/

	// stream_encoder_framing.c
	final boolean add_constant(final int subframe_bps, final int wasted_bits, final BitWriter bw)
	{
		final boolean ok =
			bw.write_raw_uint32(
				Format.FLAC__SUBFRAME_TYPE_CONSTANT_BYTE_ALIGNED_MASK | (wasted_bits != 0 ? 1 : 0),
				Format.FLAC__SUBFRAME_ZERO_PAD_LEN + Format.FLAC__SUBFRAME_TYPE_LEN + Format.FLAC__SUBFRAME_WASTED_BITS_FLAG_LEN)
				&&
				(wasted_bits != 0 ? bw.write_unary_unsigned( wasted_bits - 1 ) : true)
				&&
				bw.write_raw_int64( this.value, subframe_bps );

		return ok;
	}
}
