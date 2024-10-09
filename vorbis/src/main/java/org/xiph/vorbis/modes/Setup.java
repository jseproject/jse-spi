/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 2019 Alexey Kuznetsov
 * Copyright (c) 2002-2018 Xiph.Org Foundation
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

package org.xiph.vorbis.modes;

import org.xiph.vorbis.InfoMapping0;

public class Setup {
    /* a few static coder conventions */
    // FIXME unused _mode_template
	/*private static final InfoMode _mode_template[] = {// [2]
		new InfoMode( 0,0,0,0 ),
		new InfoMode( 1,0,0,1 )
	};*/

	/* mapping conventions:
	only one submap (this would change for efficient 5.1 support for example)*/
    /* Four psychoacoustic profiles are used, one for each blocktype */

    protected static final InfoMapping0 _map_nominal[] = {// [2]
            new InfoMapping0(
                    1, new int[]{0, 0}, new int[]{0}, new int[]{0}, 1, new int[]{0}, new int[]{1}),
            new InfoMapping0(
                    1, new int[]{0, 0}, new int[]{1}, new int[]{1}, 1, new int[]{0}, new int[]{1})
    };

    public static final InfoMapping0 _map_nominal_u[] = {// [2]
            new InfoMapping0(1, new int[]{0, 0, 0, 0, 0, 0}, new int[]{0}, new int[]{0}, 0, new int[]{0}, new int[]{0}),
            new InfoMapping0(1, new int[]{0, 0, 0, 0, 0, 0}, new int[]{1}, new int[]{1}, 0, new int[]{0}, new int[]{0})
    };
}
