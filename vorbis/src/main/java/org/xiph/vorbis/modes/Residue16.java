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

import org.xiph.vorbis.StaticBookBlock;
import org.xiph.vorbis.StaticCodebook;
import org.xiph.vorbis.MappingTemplate;
import org.xiph.vorbis.ResidueTemplate;
import org.xiph.vorbis.books.ResBooksStereo1;
import org.xiph.vorbis.books.ResBooksUncoupled1;

/**
 * toplevel residue templates 16/22kHz
 */

public class Residue16 {

    private static final StaticBookBlock _resbook_16s_0 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null},
                    {null, null, ResBooksStereo1._16c0_s_p1_0},
                    {null},
                    {null, null, ResBooksStereo1._16c0_s_p3_0},
                    {null, null, ResBooksStereo1._16c0_s_p4_0},
                    {null, null, ResBooksStereo1._16c0_s_p5_0},
                    {null, null, ResBooksStereo1._16c0_s_p6_0},
                    {ResBooksStereo1._16c0_s_p7_0, ResBooksStereo1._16c0_s_p7_1},
                    {ResBooksStereo1._16c0_s_p8_0, ResBooksStereo1._16c0_s_p8_1},
                    {ResBooksStereo1._16c0_s_p9_0, ResBooksStereo1._16c0_s_p9_1, ResBooksStereo1._16c0_s_p9_2}
            }
    );

    private static final StaticBookBlock _resbook_16s_1 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null},
                    {null, null, ResBooksStereo1._16c1_s_p1_0},
                    {null},
                    {null, null, ResBooksStereo1._16c1_s_p3_0},
                    {null, null, ResBooksStereo1._16c1_s_p4_0},
                    {null, null, ResBooksStereo1._16c1_s_p5_0},
                    {null, null, ResBooksStereo1._16c1_s_p6_0},
                    {ResBooksStereo1._16c1_s_p7_0, ResBooksStereo1._16c1_s_p7_1},
                    {ResBooksStereo1._16c1_s_p8_0, ResBooksStereo1._16c1_s_p8_1},
                    {ResBooksStereo1._16c1_s_p9_0, ResBooksStereo1._16c1_s_p9_1, ResBooksStereo1._16c1_s_p9_2}
            }
    );

    private static final StaticBookBlock _resbook_16s_2 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null},
                    {null, null, ResBooksStereo1._16c2_s_p1_0},
                    {null, null, ResBooksStereo1._16c2_s_p2_0},
                    {null, null, ResBooksStereo1._16c2_s_p3_0},
                    {null, null, ResBooksStereo1._16c2_s_p4_0},
                    {ResBooksStereo1._16c2_s_p5_0, ResBooksStereo1._16c2_s_p5_1},
                    {ResBooksStereo1._16c2_s_p6_0, ResBooksStereo1._16c2_s_p6_1},
                    {ResBooksStereo1._16c2_s_p7_0, ResBooksStereo1._16c2_s_p7_1},
                    {ResBooksStereo1._16c2_s_p8_0, ResBooksStereo1._16c2_s_p8_1},
                    {ResBooksStereo1._16c2_s_p9_0, ResBooksStereo1._16c2_s_p9_1, ResBooksStereo1._16c2_s_p9_2}
            }
    );

    private static final ResidueTemplate _res_16s_0[] = {
            new ResidueTemplate(2, 0, 32, Residue44._residue_44_mid,
                    ResBooksStereo1._huff_book__16c0_s_single, ResBooksStereo1._huff_book__16c0_s_single,
                    _resbook_16s_0, _resbook_16s_0),
    };

    private static final ResidueTemplate _res_16s_1[] = {
            new ResidueTemplate(2, 0, 32, Residue44._residue_44_mid,
                    ResBooksStereo1._huff_book__16c1_s_short, ResBooksStereo1._huff_book__16c1_s_short,
                    _resbook_16s_1, _resbook_16s_1),

            new ResidueTemplate(2, 0, 32, Residue44._residue_44_mid,
                    ResBooksStereo1._huff_book__16c1_s_long, ResBooksStereo1._huff_book__16c1_s_long,
                    _resbook_16s_1, _resbook_16s_1)
    };

    private static final ResidueTemplate _res_16s_2[] = {
            new ResidueTemplate(2, 0, 32, Residue44._residue_44_high,
                    ResBooksStereo1._huff_book__16c2_s_short, ResBooksStereo1._huff_book__16c2_s_short,
                    _resbook_16s_2, _resbook_16s_2),

            new ResidueTemplate(2, 0, 32, Residue44._residue_44_high,
                    ResBooksStereo1._huff_book__16c2_s_long, ResBooksStereo1._huff_book__16c2_s_long,
                    _resbook_16s_2, _resbook_16s_2)
    };

    protected static final MappingTemplate _mapres_template_16_stereo[] = {// [3]
            new MappingTemplate(Setup._map_nominal, _res_16s_0), /* 0 */
            new MappingTemplate(Setup._map_nominal, _res_16s_1), /* 1 */
            new MappingTemplate(Setup._map_nominal, _res_16s_2), /* 2 */
    };

    private static final StaticBookBlock _resbook_16u_0 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null},
                    {null, null, ResBooksUncoupled1._16u0__p1_0},
                    {null, null, ResBooksUncoupled1._16u0__p2_0},
                    {null, null, ResBooksUncoupled1._16u0__p3_0},
                    {null, null, ResBooksUncoupled1._16u0__p4_0},
                    {null, null, ResBooksUncoupled1._16u0__p5_0},
                    {ResBooksUncoupled1._16u0__p6_0, ResBooksUncoupled1._16u0__p6_1},
                    {ResBooksUncoupled1._16u0__p7_0, ResBooksUncoupled1._16u0__p7_1, ResBooksUncoupled1._16u0__p7_2}
            }
    );

    private static final StaticBookBlock _resbook_16u_1 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null},
                    {null, null, ResBooksUncoupled1._16u1__p1_0},
                    {null, null, ResBooksUncoupled1._16u1__p2_0},
                    {null, null, ResBooksUncoupled1._16u1__p3_0},
                    {null, null, ResBooksUncoupled1._16u1__p4_0},
                    {null, null, ResBooksUncoupled1._16u1__p5_0},
                    {null, null, ResBooksUncoupled1._16u1__p6_0},
                    {ResBooksUncoupled1._16u1__p7_0, ResBooksUncoupled1._16u1__p7_1},
                    {ResBooksUncoupled1._16u1__p8_0, ResBooksUncoupled1._16u1__p8_1},
                    {ResBooksUncoupled1._16u1__p9_0, ResBooksUncoupled1._16u1__p9_1, ResBooksUncoupled1._16u1__p9_2}
            }
    );
    private static final StaticBookBlock _resbook_16u_2 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null},
                    {null, null, ResBooksUncoupled1._16u2_p1_0},
                    {null, null, ResBooksUncoupled1._16u2_p2_0},
                    {null, null, ResBooksUncoupled1._16u2_p3_0},
                    {null, null, ResBooksUncoupled1._16u2_p4_0},
                    {ResBooksUncoupled1._16u2_p5_0, ResBooksUncoupled1._16u2_p5_1},
                    {ResBooksUncoupled1._16u2_p6_0, ResBooksUncoupled1._16u2_p6_1},
                    {ResBooksUncoupled1._16u2_p7_0, ResBooksUncoupled1._16u2_p7_1},
                    {ResBooksUncoupled1._16u2_p8_0, ResBooksUncoupled1._16u2_p8_1},
                    {ResBooksUncoupled1._16u2_p9_0, ResBooksUncoupled1._16u2_p9_1, ResBooksUncoupled1._16u2_p9_2}
            }
    );

    private static final ResidueTemplate _res_16u_0[] = {
            new ResidueTemplate(1, 0, 32, Residue44u._residue_44_low_un,
                    ResBooksUncoupled1._huff_book__16u0__single, ResBooksUncoupled1._huff_book__16u0__single,
                    _resbook_16u_0, _resbook_16u_0),
    };

    private static final ResidueTemplate _res_16u_1[] = {
            new ResidueTemplate(1, 0, 32, Residue44u._residue_44_mid_un,
                    ResBooksUncoupled1._huff_book__16u1__short, ResBooksUncoupled1._huff_book__16u1__short,
                    _resbook_16u_1, _resbook_16u_1),

            new ResidueTemplate(1, 0, 32, Residue44u._residue_44_mid_un,
                    ResBooksUncoupled1._huff_book__16u1__long, ResBooksUncoupled1._huff_book__16u1__long,
                    _resbook_16u_1, _resbook_16u_1)
    };

    private static final ResidueTemplate _res_16u_2[] = {
            new ResidueTemplate(1, 0, 32, Residue44u._residue_44_hi_un,
                    ResBooksUncoupled1._huff_book__16u2__short, ResBooksUncoupled1._huff_book__16u2__short,
                    _resbook_16u_2, _resbook_16u_2),

            new ResidueTemplate(1, 0, 32, Residue44u._residue_44_hi_un,
                    ResBooksUncoupled1._huff_book__16u2__long, ResBooksUncoupled1._huff_book__16u2__long,
                    _resbook_16u_2, _resbook_16u_2)
    };

    protected static final MappingTemplate _mapres_template_16_uncoupled[] = {// [3]
            new MappingTemplate(Setup._map_nominal_u, _res_16u_0), /* 0 */
            new MappingTemplate(Setup._map_nominal_u, _res_16u_1), /* 1 */
            new MappingTemplate(Setup._map_nominal_u, _res_16u_2), /* 2 */
    };
}
