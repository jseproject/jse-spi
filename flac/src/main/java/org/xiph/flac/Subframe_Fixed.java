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

/** FIXED subframe.  (c.f. <A HREF="../format.html#subframe_fixed">format specification</A>)
 */
public class Subframe_Fixed /* extends Subframe */ {
	/** The residual coding method. */
	public final EntropyCodingMethod entropy_coding_method = new EntropyCodingMethod();

	/** The polynomial order. */
	public int order = 0;

	/** Warmup samples to prime the predictor, length == order. */
	public final long warmup[] = new long[Format.FLAC__MAX_FIXED_ORDER];

	/** The residual signal, length == (blocksize minus order) samples. */
	public int[] residual = null;

	/*public Subframe_Fixed() {
		type = FLAC__SUBFRAME_TYPE_FIXED;
	}*/

	/*public Subframe_Fixed(int[] res) {
		residual = res;
	}*/

	// stream_encoder_framing.c
	final boolean add_fixed(final int residual_samples, final int subframe_bps, final int wasted_bits, final BitWriter bw)
	{
		if( ! bw.write_raw_uint32(
				Format.FLAC__SUBFRAME_TYPE_FIXED_BYTE_ALIGNED_MASK | (this.order << 1) | (wasted_bits != 0 ? 1 : 0),
				Format.FLAC__SUBFRAME_ZERO_PAD_LEN + Format.FLAC__SUBFRAME_TYPE_LEN + Format.FLAC__SUBFRAME_WASTED_BITS_FLAG_LEN ) ) {
			return false;
		}
		if( wasted_bits != 0 ) {
			if( ! bw.write_unary_unsigned( wasted_bits - 1 ) ) {
				return false;
			}
		}

		for( int i = 0; i < this.order; i++ ) {
			if( ! bw.write_raw_int64( this.warmup[i], subframe_bps ) ) {
				return false;
			}
		}

		final EntropyCodingMethod method = this.entropy_coding_method;// java
		if( ! Subframe.add_entropy_coding_method_( bw, method ) ) {
			return false;
		}
		switch( method.type ) {
			case Format.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE:
			case Format.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE2:
				if( ! Subframe.add_residual_partitioned_rice_(
					bw,
					this.residual,
					residual_samples,
					this.order,
					method./*data.*/partitioned_rice.contents.parameters,
					method./*data.*/partitioned_rice.contents.raw_bits,
					method./*data.*/partitioned_rice.order,
					/*is_extended=*/method.type == Format.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE2
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
