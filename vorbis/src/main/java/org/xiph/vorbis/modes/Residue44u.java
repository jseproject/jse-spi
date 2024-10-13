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

package org.xiph.vorbis.modes;

import org.xiph.vorbis.InfoResidue0;
import org.xiph.vorbis.StaticBookBlock;
import org.xiph.vorbis.StaticCodebook;
import org.xiph.vorbis.MappingTemplate;
import org.xiph.vorbis.ResidueTemplate;
import org.xiph.vorbis.books.ResBooksUncoupled2;
import org.xiph.vorbis.books.ResBooksUncoupled3;
import org.xiph.vorbis.books.ResBooksUncoupled4;
import org.xiph.vorbis.books.ResBooksUncoupled5;

/**
 * toplevel residue templates for 32/44.1/48kHz uncoupled
 */

public class Residue44u {

    protected static final InfoResidue0 _residue_44_low_un = new InfoResidue0(
            0, -1, -1, 8, -1, -1,
            new int[]{0},
            new int[]{-1},
            new int[]{0, 1, 1, 2, 2, 4, 28},
            new int[]{-1, 25, -1, 45, -1, -1, -1}
    );

    protected static final InfoResidue0 _residue_44_mid_un = new InfoResidue0(
            0, -1, -1, 10, -1, -1,
            /* 0   1   2   3   4   5   6   7   8   9 */
            new int[]{0},
            new int[]{-1},
            new int[]{0, 1, 1, 2, 2, 4, 4, 16, 60},
            new int[]{-1, 30, -1, 50, -1, 80, -1, -1, -1}
    );

    protected static final InfoResidue0 _residue_44_hi_un = new InfoResidue0(
            0, -1, -1, 10, -1, -1,
            /* 0   1   2   3   4   5   6   7   8   9 */
            new int[]{0},
            new int[]{-1},
            new int[]{0, 1, 2, 4, 8, 16, 32, 71, 157},
            new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1}
    );

    private static final StaticBookBlock _resbook_44u_n1 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null},
                    {null, null, ResBooksUncoupled5._44un1__p1_0},
                    {null, null, ResBooksUncoupled5._44un1__p2_0},
                    {null, null, ResBooksUncoupled5._44un1__p3_0},
                    {null, null, ResBooksUncoupled5._44un1__p4_0},
                    {null, null, ResBooksUncoupled5._44un1__p5_0},
                    {ResBooksUncoupled5._44un1__p6_0, ResBooksUncoupled5._44un1__p6_1},
                    {ResBooksUncoupled5._44un1__p7_0, ResBooksUncoupled5._44un1__p7_1, ResBooksUncoupled5._44un1__p7_2}
            }
    );

    private static final StaticBookBlock _resbook_44u_0 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null},
                    {null, null, ResBooksUncoupled2._44u0__p1_0},
                    {null, null, ResBooksUncoupled2._44u0__p2_0},
                    {null, null, ResBooksUncoupled2._44u0__p3_0},
                    {null, null, ResBooksUncoupled2._44u0__p4_0},
                    {null, null, ResBooksUncoupled2._44u0__p5_0},
                    {ResBooksUncoupled2._44u0__p6_0, ResBooksUncoupled2._44u0__p6_1},
                    {ResBooksUncoupled2._44u0__p7_0, ResBooksUncoupled2._44u0__p7_1, ResBooksUncoupled2._44u0__p7_2}
            }
    );

    private static final StaticBookBlock _resbook_44u_1 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null},
                    {null, null, ResBooksUncoupled2._44u1__p1_0},
                    {null, null, ResBooksUncoupled2._44u1__p2_0},
                    {null, null, ResBooksUncoupled2._44u1__p3_0},
                    {null, null, ResBooksUncoupled2._44u1__p4_0},
                    {null, null, ResBooksUncoupled2._44u1__p5_0},
                    {ResBooksUncoupled2._44u1__p6_0, ResBooksUncoupled2._44u1__p6_1},
                    {ResBooksUncoupled2._44u1__p7_0, ResBooksUncoupled2._44u1__p7_1, ResBooksUncoupled2._44u1__p7_2}
            }
    );

    private static final StaticBookBlock _resbook_44u_2 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null},
                    {null, null, ResBooksUncoupled2._44u2__p1_0},
                    {null, null, ResBooksUncoupled3._44u2__p2_0},
                    {null, null, ResBooksUncoupled3._44u2__p3_0},
                    {null, null, ResBooksUncoupled3._44u2__p4_0},
                    {null, null, ResBooksUncoupled3._44u2__p5_0},
                    {ResBooksUncoupled3._44u2__p6_0, ResBooksUncoupled3._44u2__p6_1},
                    {ResBooksUncoupled3._44u2__p7_0, ResBooksUncoupled3._44u2__p7_1, ResBooksUncoupled3._44u2__p7_2}
            }
    );

    private static final StaticBookBlock _resbook_44u_3 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null},
                    {null, null, ResBooksUncoupled3._44u3__p1_0},
                    {null, null, ResBooksUncoupled3._44u3__p2_0},
                    {null, null, ResBooksUncoupled3._44u3__p3_0},
                    {null, null, ResBooksUncoupled3._44u3__p4_0},
                    {null, null, ResBooksUncoupled3._44u3__p5_0},
                    {ResBooksUncoupled3._44u3__p6_0, ResBooksUncoupled3._44u3__p6_1},
                    {ResBooksUncoupled3._44u3__p7_0, ResBooksUncoupled3._44u3__p7_1, ResBooksUncoupled3._44u3__p7_2}
            }
    );

    private static final StaticBookBlock _resbook_44u_4 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null},
                    {null, null, ResBooksUncoupled3._44u4__p1_0},
                    {null, null, ResBooksUncoupled3._44u4__p2_0},
                    {null, null, ResBooksUncoupled3._44u4__p3_0},
                    {null, null, ResBooksUncoupled3._44u4__p4_0},
                    {null, null, ResBooksUncoupled3._44u4__p5_0},
                    {ResBooksUncoupled3._44u4__p6_0, ResBooksUncoupled3._44u4__p6_1},
                    {ResBooksUncoupled3._44u4__p7_0, ResBooksUncoupled3._44u4__p7_1, ResBooksUncoupled3._44u4__p7_2}
            }
    );

    private static final StaticBookBlock _resbook_44u_5 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null},
                    {null, null, ResBooksUncoupled3._44u5__p1_0},
                    {null, null, ResBooksUncoupled3._44u5__p2_0},
                    {null, null, ResBooksUncoupled3._44u5__p3_0},
                    {null, null, ResBooksUncoupled3._44u5__p4_0},
                    {null, null, ResBooksUncoupled3._44u5__p5_0},
                    {null, null, ResBooksUncoupled3._44u5__p6_0},
                    {ResBooksUncoupled3._44u5__p7_0, ResBooksUncoupled3._44u5__p7_1},
                    {ResBooksUncoupled3._44u5__p8_0, ResBooksUncoupled3._44u5__p8_1},
                    {ResBooksUncoupled3._44u5__p9_0, ResBooksUncoupled3._44u5__p9_1, ResBooksUncoupled4._44u5__p9_2}
            }
    );

    private static final StaticBookBlock _resbook_44u_6 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null},
                    {null, null, ResBooksUncoupled4._44u6__p1_0},
                    {null, null, ResBooksUncoupled4._44u6__p2_0},
                    {null, null, ResBooksUncoupled4._44u6__p3_0},
                    {null, null, ResBooksUncoupled4._44u6__p4_0},
                    {null, null, ResBooksUncoupled4._44u6__p5_0},
                    {null, null, ResBooksUncoupled4._44u6__p6_0},
                    {ResBooksUncoupled4._44u6__p7_0, ResBooksUncoupled4._44u6__p7_1},
                    {ResBooksUncoupled4._44u6__p8_0, ResBooksUncoupled4._44u6__p8_1},
                    {ResBooksUncoupled4._44u6__p9_0, ResBooksUncoupled4._44u6__p9_1, ResBooksUncoupled4._44u6__p9_2}
            }
    );

    private static final StaticBookBlock _resbook_44u_7 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null},
                    {null, null, ResBooksUncoupled4._44u7__p1_0},
                    {null, null, ResBooksUncoupled4._44u7__p2_0},
                    {null, null, ResBooksUncoupled4._44u7__p3_0},
                    {null, null, ResBooksUncoupled4._44u7__p4_0},
                    {null, null, ResBooksUncoupled4._44u7__p5_0},
                    {null, null, ResBooksUncoupled4._44u7__p6_0},
                    {ResBooksUncoupled4._44u7__p7_0, ResBooksUncoupled4._44u7__p7_1},
                    {ResBooksUncoupled4._44u7__p8_0, ResBooksUncoupled4._44u7__p8_1},
                    {ResBooksUncoupled4._44u7__p9_0, ResBooksUncoupled4._44u7__p9_1, ResBooksUncoupled4._44u7__p9_2}
            }
    );

    private static final StaticBookBlock _resbook_44u_8 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null},
                    {null, null, ResBooksUncoupled4._44u8_p1_0},
                    {null, null, ResBooksUncoupled4._44u8_p2_0},
                    {null, null, ResBooksUncoupled4._44u8_p3_0},
                    {null, null, ResBooksUncoupled4._44u8_p4_0},
                    {ResBooksUncoupled4._44u8_p5_0, ResBooksUncoupled4._44u8_p5_1},
                    {ResBooksUncoupled4._44u8_p6_0, ResBooksUncoupled4._44u8_p6_1},
                    {ResBooksUncoupled4._44u8_p7_0, ResBooksUncoupled4._44u8_p7_1},
                    {ResBooksUncoupled4._44u8_p8_0, ResBooksUncoupled4._44u8_p8_1},
                    {ResBooksUncoupled4._44u8_p9_0, ResBooksUncoupled4._44u8_p9_1, ResBooksUncoupled4._44u8_p9_2}
            }
    );

    private static final StaticBookBlock _resbook_44u_9 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null},
                    {null, null, ResBooksUncoupled4._44u9_p1_0},
                    {null, null, ResBooksUncoupled5._44u9_p2_0},
                    {null, null, ResBooksUncoupled5._44u9_p3_0},
                    {null, null, ResBooksUncoupled5._44u9_p4_0},
                    {ResBooksUncoupled5._44u9_p5_0, ResBooksUncoupled5._44u9_p5_1},
                    {ResBooksUncoupled5._44u9_p6_0, ResBooksUncoupled5._44u9_p6_1},
                    {ResBooksUncoupled5._44u9_p7_0, ResBooksUncoupled5._44u9_p7_1},
                    {ResBooksUncoupled5._44u9_p8_0, ResBooksUncoupled5._44u9_p8_1},
                    {ResBooksUncoupled5._44u9_p9_0, ResBooksUncoupled5._44u9_p9_1, ResBooksUncoupled5._44u9_p9_2}
            }
    );

    private static final ResidueTemplate _res_44u_n1[] = {
            new ResidueTemplate(1, 0, 32, _residue_44_low_un,
                    ResBooksUncoupled5._huff_book__44un1__short, ResBooksUncoupled5._huff_book__44un1__short,
                    _resbook_44u_n1, _resbook_44u_n1),

            new ResidueTemplate(1, 0, 32, _residue_44_low_un,
                    ResBooksUncoupled5._huff_book__44un1__long, ResBooksUncoupled5._huff_book__44un1__long,
                    _resbook_44u_n1, _resbook_44u_n1)
    };

    private static final ResidueTemplate _res_44u_0[] = {
            new ResidueTemplate(1, 0, 16, _residue_44_low_un,
                    ResBooksUncoupled2._huff_book__44u0__short, ResBooksUncoupled2._huff_book__44u0__short,
                    _resbook_44u_0, _resbook_44u_0),

            new ResidueTemplate(1, 0, 32, _residue_44_low_un,
                    ResBooksUncoupled2._huff_book__44u0__long, ResBooksUncoupled2._huff_book__44u0__long,
                    _resbook_44u_0, _resbook_44u_0)
    };

    private static final ResidueTemplate _res_44u_1[] = {
            new ResidueTemplate(1, 0, 16, _residue_44_low_un,
                    ResBooksUncoupled2._huff_book__44u1__short, ResBooksUncoupled2._huff_book__44u1__short,
                    _resbook_44u_1, _resbook_44u_1),

            new ResidueTemplate(1, 0, 32, _residue_44_low_un,
                    ResBooksUncoupled2._huff_book__44u1__long, ResBooksUncoupled2._huff_book__44u1__long,
                    _resbook_44u_1, _resbook_44u_1)
    };

    private static final ResidueTemplate _res_44u_2[] = {
            new ResidueTemplate(1, 0, 16, _residue_44_low_un,
                    ResBooksUncoupled3._huff_book__44u2__short, ResBooksUncoupled3._huff_book__44u2__short,
                    _resbook_44u_2, _resbook_44u_2),

            new ResidueTemplate(1, 0, 32, _residue_44_low_un,
                    ResBooksUncoupled2._huff_book__44u2__long, ResBooksUncoupled2._huff_book__44u2__long,
                    _resbook_44u_2, _resbook_44u_2)
    };

    private static final ResidueTemplate _res_44u_3[] = {
            new ResidueTemplate(1, 0, 16, _residue_44_low_un,
                    ResBooksUncoupled3._huff_book__44u3__short, ResBooksUncoupled3._huff_book__44u3__short,
                    _resbook_44u_3, _resbook_44u_3),

            new ResidueTemplate(1, 0, 32, _residue_44_low_un,
                    ResBooksUncoupled3._huff_book__44u3__long, ResBooksUncoupled3._huff_book__44u3__long,
                    _resbook_44u_3, _resbook_44u_3)
    };

    private static final ResidueTemplate _res_44u_4[] = {
            new ResidueTemplate(1, 0, 16, _residue_44_low_un,
                    ResBooksUncoupled3._huff_book__44u4__short, ResBooksUncoupled3._huff_book__44u4__short,
                    _resbook_44u_4, _resbook_44u_4),

            new ResidueTemplate(1, 0, 32, _residue_44_low_un,
                    ResBooksUncoupled3._huff_book__44u4__long, ResBooksUncoupled3._huff_book__44u4__long,
                    _resbook_44u_4, _resbook_44u_4)
    };

    private static final ResidueTemplate _res_44u_5[] = {
            new ResidueTemplate(1, 0, 16, _residue_44_mid_un,
                    ResBooksUncoupled4._huff_book__44u5__short, ResBooksUncoupled4._huff_book__44u5__short,
                    _resbook_44u_5, _resbook_44u_5),

            new ResidueTemplate(1, 0, 32, _residue_44_mid_un,
                    ResBooksUncoupled3._huff_book__44u5__long, ResBooksUncoupled3._huff_book__44u5__long,
                    _resbook_44u_5, _resbook_44u_5)
    };

    private static final ResidueTemplate _res_44u_6[] = {
            new ResidueTemplate(1, 0, 16, _residue_44_mid_un,
                    ResBooksUncoupled4._huff_book__44u6__short, ResBooksUncoupled4._huff_book__44u6__short,
                    _resbook_44u_6, _resbook_44u_6),

            new ResidueTemplate(1, 0, 32, _residue_44_mid_un,
                    ResBooksUncoupled4._huff_book__44u6__long, ResBooksUncoupled4._huff_book__44u6__long,
                    _resbook_44u_6, _resbook_44u_6)
    };

    private static final ResidueTemplate _res_44u_7[] = {
            new ResidueTemplate(1, 0, 16, _residue_44_mid_un,
                    ResBooksUncoupled4._huff_book__44u7__short, ResBooksUncoupled4._huff_book__44u7__short,
                    _resbook_44u_7, _resbook_44u_7),

            new ResidueTemplate(1, 0, 32, _residue_44_mid_un,
                    ResBooksUncoupled4._huff_book__44u7__long, ResBooksUncoupled4._huff_book__44u7__long,
                    _resbook_44u_7, _resbook_44u_7)
    };

    private static final ResidueTemplate _res_44u_8[] = {
            new ResidueTemplate(1, 0, 16, _residue_44_hi_un,
                    ResBooksUncoupled4._huff_book__44u8__short, ResBooksUncoupled4._huff_book__44u8__short,
                    _resbook_44u_8, _resbook_44u_8),

            new ResidueTemplate(1, 0, 32, _residue_44_hi_un,
                    ResBooksUncoupled4._huff_book__44u8__long, ResBooksUncoupled4._huff_book__44u8__long,
                    _resbook_44u_8, _resbook_44u_8)
    };

    private static final ResidueTemplate _res_44u_9[] = {
            new ResidueTemplate(1, 0, 16, _residue_44_hi_un,
                    ResBooksUncoupled4._huff_book__44u9__short, ResBooksUncoupled4._huff_book__44u9__short,
                    _resbook_44u_9, _resbook_44u_9),

            new ResidueTemplate(1, 0, 32, _residue_44_hi_un,
                    ResBooksUncoupled4._huff_book__44u9__long, ResBooksUncoupled4._huff_book__44u9__long,
                    _resbook_44u_9, _resbook_44u_9)
    };

    protected static final MappingTemplate _mapres_template_44_uncoupled[] = {
            new MappingTemplate(Setup._map_nominal_u, _res_44u_n1), /* -1 */
            new MappingTemplate(Setup._map_nominal_u, _res_44u_0), /* 0 */
            new MappingTemplate(Setup._map_nominal_u, _res_44u_1), /* 1 */
            new MappingTemplate(Setup._map_nominal_u, _res_44u_2), /* 2 */
            new MappingTemplate(Setup._map_nominal_u, _res_44u_3), /* 3 */
            new MappingTemplate(Setup._map_nominal_u, _res_44u_4), /* 4 */
            new MappingTemplate(Setup._map_nominal_u, _res_44u_5), /* 5 */
            new MappingTemplate(Setup._map_nominal_u, _res_44u_6), /* 6 */
            new MappingTemplate(Setup._map_nominal_u, _res_44u_7), /* 7 */
            new MappingTemplate(Setup._map_nominal_u, _res_44u_8), /* 8 */
            new MappingTemplate(Setup._map_nominal_u, _res_44u_9), /* 9 */
    };
}
