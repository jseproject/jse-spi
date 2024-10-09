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

import java.util.Arrays;

/** Contents of a Rice partitioned residual
 */
class PartitionedRiceContents {
	/** The Rice parameters for each context. */
	int[] parameters = null;

	/** Widths for escape-coded partitions.  Will be non-zero for escaped
	 * partitions and zero for unescaped partitions.
	 */
	int[] raw_bits = null;

	/** The capacity of the \a parameters and \a raw_bits arrays
	 * specified as an order, i.e. the number of array elements
	 * allocated is 2 ^ \a capacity_by_order.
	 */
	int capacity_by_order = 0;

	final void init()
	{
		this.parameters = null;
		this.raw_bits = null;
		this.capacity_by_order = 0;
	}

	final void clear()
	{
		this.parameters = null;
		this.raw_bits = null;
		init();
	}

	final boolean ensure_size(final int max_partition_order)
	{
		//FLAC__ASSERT(0 != object);

		//FLAC__ASSERT(object->capacity_by_order > 0 || (0 == object->parameters && 0 == object->raw_bits));

		if( this.capacity_by_order < max_partition_order || this.parameters == null || this.raw_bits == null ) {
			try {
				final int size = 1 << max_partition_order;// java
				this.parameters = this.parameters == null ? new int[size] : Arrays.copyOf( this.parameters, size );
				this.raw_bits = this.raw_bits == null ? new int[size] : Arrays.copyOf( this.raw_bits, size );
				Arrays.fill( this.raw_bits, 0 );
				this.capacity_by_order = max_partition_order;
			} catch(final OutOfMemoryError e) {
				return false;
			}
		}

		return true;
	}
}
