package org.xiph.flac;

/** LPC subframe.  (c.f. <A HREF="../format.html#subframe_lpc">format specification</A>)
 */
public class Subframe_LPC /* extends Subframe */ {
	/** The residual coding method. */
	public final EntropyCodingMethod entropy_coding_method = new EntropyCodingMethod();

	/** The FIR order. */
	public int order = 0;

	/** Quantized FIR filter coefficient precision in bits. */
	public int qlp_coeff_precision = 0;

	/** The qlp coeff shift needed. */
	public int quantization_level = 0;

	/** FIR filter coefficients. */
	public final int qlp_coeff[] = new int[Format.FLAC__MAX_LPC_ORDER];

	/** Warmup samples to prime the predictor, length == order. */
	public final long warmup[] = new long[Format.FLAC__MAX_LPC_ORDER];

	/** The residual signal, length == (blocksize minus order) samples. */
	public int[] residual = null;

	/*public Subframe_LPC() {
		type = FLAC__SUBFRAME_TYPE_LPC;
	}*/

	/*public Subframe_LPC(int[] res) {
		residual = res;
	}*/

	// stream_encoder_framing.c
	final boolean add_lpc(final int residual_samples, final int subframe_bps, final int wasted_bits, final BitWriter bw)
	{
		final int fir_order = this.order;// java
		if( ! bw.write_raw_uint32(
				Format.FLAC__SUBFRAME_TYPE_LPC_BYTE_ALIGNED_MASK | ((fir_order - 1) << 1) | (wasted_bits != 0 ? 1 : 0),
				Format.FLAC__SUBFRAME_ZERO_PAD_LEN + Format.FLAC__SUBFRAME_TYPE_LEN + Format.FLAC__SUBFRAME_WASTED_BITS_FLAG_LEN ) ) {
			return false;
		}
		if( wasted_bits != 0 ) {
			if( ! bw.write_unary_unsigned( wasted_bits - 1 ) ) {
				return false;
			}
		}

		for( int i = 0; i < fir_order; i++ ) {
			if( ! bw.write_raw_int64( this.warmup[i], subframe_bps ) ) {
				return false;
			}
		}

		if( ! bw.write_raw_uint32( this.qlp_coeff_precision - 1, Format.FLAC__SUBFRAME_LPC_QLP_COEFF_PRECISION_LEN ) ) {
			return false;
		}
		if( ! bw.write_raw_int32( this.quantization_level, Format.FLAC__SUBFRAME_LPC_QLP_SHIFT_LEN ) ) {
			return false;
		}
		final int[] coeffs = this.qlp_coeff;// java
		for( int i = 0; i < fir_order; i++ ) {
			if( ! bw.write_raw_int32( coeffs[i], this.qlp_coeff_precision ) ) {
				return false;
			}
		}

		if( ! Subframe.add_entropy_coding_method_( bw, this.entropy_coding_method ) ) {
			return false;
		}
		switch( this.entropy_coding_method.type ) {
			case Format.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE:
			case Format.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE2:
				if( ! Subframe.add_residual_partitioned_rice_(
					bw,
					this.residual,
					residual_samples,
					fir_order,
					this.entropy_coding_method./*data.*/partitioned_rice.contents.parameters,
					this.entropy_coding_method./*data.*/partitioned_rice.contents.raw_bits,
					this.entropy_coding_method./*data.*/partitioned_rice.order,
					/*is_extended=*/this.entropy_coding_method.type == Format.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE2
				) ) {
					return false;
				}
				break;
			default:
				//FLAC__ASSERT(0);
		}

		return true;
	}
}
