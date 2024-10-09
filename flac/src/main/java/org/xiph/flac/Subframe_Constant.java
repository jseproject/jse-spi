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
