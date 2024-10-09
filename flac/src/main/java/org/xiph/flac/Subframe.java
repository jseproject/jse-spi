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

/** FLAC subframe structure.  (c.f. <A HREF="../format.html#subframe">format specification</A>)
 */
public class Subframe {

	public int /*FLAC__SubframeType*/ type;
	/*union {
		Subframe_Constant constant;
		Subframe_Fixed fixed;
		Subframe_LPC lpc;
		Subframe_Verbatim verbatim;
	} data;*/
	public Object data = null;// TODO optimize using subframe types
	public int wasted_bits;

	// stream_encoder_framing.c
	static boolean add_entropy_coding_method_(final BitWriter bw, final EntropyCodingMethod method)
	{
		if( ! bw.write_raw_uint32( method.type, Format.FLAC__ENTROPY_CODING_METHOD_TYPE_LEN ) ) {
			return false;
		}
		switch( method.type ) {
			case Format.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE:
			case Format.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE2:
				if( ! bw.write_raw_uint32( method./*data.*/partitioned_rice.order, Format.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE_ORDER_LEN ) ) {
					return false;
				}
				break;
			default:
				//FLAC__ASSERT(0);
		}
		return true;
	}

	static boolean add_residual_partitioned_rice_(final BitWriter bw, final int residual[], final int residual_samples, final int predictor_order, final int rice_parameters[], final int raw_bits[], final int partition_order, final boolean is_extended)
	{
		final int plen = is_extended ? Format.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE2_PARAMETER_LEN : Format.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE_PARAMETER_LEN;
		final int pesc = is_extended ? Format.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE2_ESCAPE_PARAMETER : Format.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE_ESCAPE_PARAMETER;

		if( partition_order == 0 ) {

			if( raw_bits[0] == 0 ) {
				if( ! bw.write_raw_uint32( rice_parameters[0], plen ) ) {
					return false;
				}
				if( ! bw.write_rice_signed_block( residual, 0, residual_samples, rice_parameters[0] ) ) {
					return false;
				}
			}
			else {
				//FLAC__ASSERT(rice_parameters[0] == 0);
				if( ! bw.write_raw_uint32( pesc, plen ) ) {
					return false;
				}
				if( ! bw.write_raw_uint32( raw_bits[0], Format.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE_RAW_LEN ) ) {
					return false;
				}
				for( int i = 0; i < residual_samples; i++ ) {
					if( ! bw.write_raw_int32( residual[i], raw_bits[0] ) ) {
						return false;
					}
				}
			}
			return true;
		}
		else {
			int k = 0, k_last = 0;
			final int default_partition_samples = (residual_samples + predictor_order) >> partition_order;
			for( int i = 0, ie = (1 << partition_order); i < ie; i++ ) {
				int partition_samples = default_partition_samples;
				if( i == 0 ) {
					partition_samples -= predictor_order;
				}
				k += partition_samples;
				if( raw_bits[i] == 0 ) {
					if( ! bw.write_raw_uint32( rice_parameters[i], plen ) ) {
						return false;
					}
					if( ! bw.write_rice_signed_block( residual, k_last, k - k_last, rice_parameters[i] ) ) {
						return false;
					}
				}
				else {
					if( ! bw.write_raw_uint32( pesc, plen ) ) {
						return false;
					}
					if( ! bw.write_raw_uint32( raw_bits[i], Format.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE_RAW_LEN ) ) {
						return false;
					}
					for( int j = k_last; j < k; j++ ) {
						if( ! bw.write_raw_int32( residual[j], raw_bits[i] ) ) {
							return false;
						}
					}
				}
				k_last = k;
			}
			return true;
		}
	}
}
