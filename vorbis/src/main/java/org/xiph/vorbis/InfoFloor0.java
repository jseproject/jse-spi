/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 2019 Alexey Kuznetsov
 * Copyright (c) 2002-2018 Xiph.Org Foundation
 * Copyright (c) 1994-1996 James Gosling,
 *                         Kevin A. Smith, Sun Microsystems, Inc.
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
 * - Neither the name of the Xiph.org Foundation nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE FOUNDATION
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.xiph.vorbis;

class InfoFloor0 extends InfoFloor {
    int order = 0;
    int rate = 0;
    int barkmap = 0;

    int ampbits = 0;
    int ampdB = 0;

    /**
     * <= 16
     */
    int numbooks = 0;
    final int[] books = new int[16];

    /**
     * encode-only config setting hacks for libvorbis
     */
    float lessthan = 0.0f;
    /**
     * encode-only config setting hacks for libvorbis
     */
    float greaterthan = 0.0f;
	/*
	private void clear() {
		order = 0;
		rate = 0;
		barkmap = 0;
		ampbits = 0;
		ampdB = 0;
		numbooks = 0;
		Arrays.fill( books, 0 );

		lessthan = 0.0f;
		greaterthan = 0.0f;
	}*/
}
