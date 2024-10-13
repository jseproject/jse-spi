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

public class ResidueTemplate {
    final int res_type;
    /**
     * 0 lowpass limited, 1 point stereo limited
     */
    final int limit_type;
    final int grouping;
    final InfoResidue0 res;
    final StaticCodebook book_aux;
    final StaticCodebook book_aux_managed;
    final StaticBookBlock books_base;
    final StaticBookBlock books_base_managed;

    //
    public ResidueTemplate(
            int i_res_type,
            int i_limit_type,
            int i_grouping,
            InfoResidue0 vir_res,
            StaticCodebook sc_book_aux,
            StaticCodebook sc_book_aux_managed,
            StaticBookBlock sb_books_base,
            StaticBookBlock sb_books_base_managed
    ) {
        res_type = i_res_type;
        limit_type = i_limit_type;
        grouping = i_grouping;
        res = vir_res;
        book_aux = sc_book_aux;
        book_aux_managed = sc_book_aux_managed;
        books_base = sb_books_base;
        books_base_managed = sb_books_base_managed;
    }
}
