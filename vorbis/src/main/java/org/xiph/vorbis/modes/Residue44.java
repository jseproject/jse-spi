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

import org.xiph.vorbis.InfoResidue0;
import org.xiph.vorbis.StaticBookBlock;
import org.xiph.vorbis.StaticCodebook;
import org.xiph.vorbis.MappingTemplate;
import org.xiph.vorbis.ResidueTemplate;
import org.xiph.vorbis.books.ResBooksStereo1;
import org.xiph.vorbis.books.ResBooksStereo2;
import org.xiph.vorbis.books.ResBooksStereo3;
import org.xiph.vorbis.books.ResBooksStereo4;

/**
 * toplevel residue templates for 32/44.1/48kHz
 */

public class Residue44 {

    private static final InfoResidue0 _residue_44_low = new InfoResidue0(
            0, -1, -1, 9, -1, -1,
            /* 0   1   2   3   4   5   6   7  */
            new int[]{0},
            new int[]{-1},
            new int[]{0, 1, 2, 2, 4, 8, 16, 32},
            new int[]{0, 0, 0, 999, 4, 8, 16, 32}
    );

    protected static final InfoResidue0 _residue_44_mid = new InfoResidue0(
            0, -1, -1, 10, -1, -1,
            /* 0   1   2   3   4   5   6   7   8  */
            new int[]{0},
            new int[]{-1},
            new int[]{0, 1, 1, 2, 2, 4, 8, 16, 32},
            new int[]{0, 0, 999, 0, 999, 4, 8, 16, 32}
    );

    protected static final InfoResidue0 _residue_44_high = new InfoResidue0(
            0, -1, -1, 10, -1, -1,
            /* 0   1   2   3   4   5   6   7   8  */
            new int[]{0},
            new int[]{-1},
            new int[]{0, 1, 2, 4, 8, 16, 32, 71, 157},
            new int[]{0, 1, 2, 3, 4, 8, 16, 71, 157}
    );

    private static final StaticBookBlock _resbook_44s_n1 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null}, {null, null, ResBooksStereo4._44cn1_s_p1_0}, {null, null, ResBooksStereo4._44cn1_s_p2_0},
                    {null, null, ResBooksStereo4._44cn1_s_p3_0}, {null, null, ResBooksStereo4._44cn1_s_p4_0}, {null, null, ResBooksStereo4._44cn1_s_p5_0},
                    {ResBooksStereo4._44cn1_s_p6_0, ResBooksStereo4._44cn1_s_p6_1}, {ResBooksStereo4._44cn1_s_p7_0, ResBooksStereo4._44cn1_s_p7_1},
                    {ResBooksStereo4._44cn1_s_p8_0, ResBooksStereo4._44cn1_s_p8_1, ResBooksStereo4._44cn1_s_p8_2}
            }
    );

    private static final StaticBookBlock _resbook_44sm_n1 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null}, {null, null, ResBooksStereo4._44cn1_sm_p1_0}, {null, null, ResBooksStereo4._44cn1_sm_p2_0},
                    {null, null, ResBooksStereo4._44cn1_sm_p3_0}, {null, null, ResBooksStereo4._44cn1_sm_p4_0}, {null, null, ResBooksStereo4._44cn1_sm_p5_0},
                    {ResBooksStereo4._44cn1_sm_p6_0, ResBooksStereo4._44cn1_sm_p6_1}, {ResBooksStereo4._44cn1_sm_p7_0, ResBooksStereo4._44cn1_sm_p7_1},
                    {ResBooksStereo4._44cn1_sm_p8_0, ResBooksStereo4._44cn1_sm_p8_1, ResBooksStereo4._44cn1_sm_p8_2}
            }
    );

    private static final StaticBookBlock _resbook_44s_0 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null}, {null, null, ResBooksStereo3._44c0_s_p1_0}, {null, null, ResBooksStereo3._44c0_s_p2_0},
                    {null, null, ResBooksStereo3._44c0_s_p3_0}, {null, null, ResBooksStereo3._44c0_s_p4_0}, {null, null, ResBooksStereo3._44c0_s_p5_0},
                    {ResBooksStereo3._44c0_s_p6_0, ResBooksStereo3._44c0_s_p6_1}, {ResBooksStereo3._44c0_s_p7_0, ResBooksStereo3._44c0_s_p7_1},
                    {ResBooksStereo4._44c0_s_p8_0, ResBooksStereo4._44c0_s_p8_1, ResBooksStereo4._44c0_s_p8_2}
            }
    );

    private static final StaticBookBlock _resbook_44sm_0 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null}, {null, null, ResBooksStereo4._44c0_sm_p1_0}, {null, null, ResBooksStereo4._44c0_sm_p2_0},
                    {null, null, ResBooksStereo4._44c0_sm_p3_0}, {null, null, ResBooksStereo4._44c0_sm_p4_0}, {null, null, ResBooksStereo4._44c0_sm_p5_0},
                    {ResBooksStereo4._44c0_sm_p6_0, ResBooksStereo4._44c0_sm_p6_1}, {ResBooksStereo4._44c0_sm_p7_0, ResBooksStereo4._44c0_sm_p7_1},
                    {ResBooksStereo4._44c0_sm_p8_0, ResBooksStereo4._44c0_sm_p8_1, ResBooksStereo4._44c0_sm_p8_2}
            }
    );

    private static final StaticBookBlock _resbook_44s_1 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null}, {null, null, ResBooksStereo4._44c1_s_p1_0}, {null, null, ResBooksStereo4._44c1_s_p2_0},
                    {null, null, ResBooksStereo4._44c1_s_p3_0}, {null, null, ResBooksStereo4._44c1_s_p4_0}, {null, null, ResBooksStereo4._44c1_s_p5_0},
                    {ResBooksStereo4._44c1_s_p6_0, ResBooksStereo4._44c1_s_p6_1}, {ResBooksStereo4._44c1_s_p7_0, ResBooksStereo4._44c1_s_p7_1},
                    {ResBooksStereo4._44c1_s_p8_0, ResBooksStereo4._44c1_s_p8_1, ResBooksStereo4._44c1_s_p8_2}
            }
    );

    private static final StaticBookBlock _resbook_44sm_1 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null}, {null, null, ResBooksStereo4._44c1_sm_p1_0}, {null, null, ResBooksStereo4._44c1_sm_p2_0},
                    {null, null, ResBooksStereo4._44c1_sm_p3_0}, {null, null, ResBooksStereo4._44c1_sm_p4_0}, {null, null, ResBooksStereo4._44c1_sm_p5_0},
                    {ResBooksStereo4._44c1_sm_p6_0, ResBooksStereo4._44c1_sm_p6_1}, {ResBooksStereo4._44c1_sm_p7_0, ResBooksStereo4._44c1_sm_p7_1},
                    {ResBooksStereo4._44c1_sm_p8_0, ResBooksStereo4._44c1_sm_p8_1, ResBooksStereo4._44c1_sm_p8_2}
            }
    );

    private static final StaticBookBlock _resbook_44s_2 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null}, {null, null, ResBooksStereo1._44c2_s_p1_0}, {null, null, ResBooksStereo2._44c2_s_p2_0}, {null, null, ResBooksStereo2._44c2_s_p3_0},
                    {null, null, ResBooksStereo2._44c2_s_p4_0}, {null, null, ResBooksStereo2._44c2_s_p5_0}, {null, null, ResBooksStereo2._44c2_s_p6_0},
                    {ResBooksStereo2._44c2_s_p7_0, ResBooksStereo2._44c2_s_p7_1}, {ResBooksStereo2._44c2_s_p8_0, ResBooksStereo2._44c2_s_p8_1},
                    {ResBooksStereo2._44c2_s_p9_0, ResBooksStereo2._44c2_s_p9_1, ResBooksStereo2._44c2_s_p9_2}
            }
    );

    private static final StaticBookBlock _resbook_44s_3 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null}, {null, null, ResBooksStereo2._44c3_s_p1_0}, {null, null, ResBooksStereo2._44c3_s_p2_0}, {null, null, ResBooksStereo2._44c3_s_p3_0},
                    {null, null, ResBooksStereo2._44c3_s_p4_0}, {null, null, ResBooksStereo2._44c3_s_p5_0}, {null, null, ResBooksStereo2._44c3_s_p6_0},
                    {ResBooksStereo2._44c3_s_p7_0, ResBooksStereo2._44c3_s_p7_1}, {ResBooksStereo2._44c3_s_p8_0, ResBooksStereo2._44c3_s_p8_1},
                    {ResBooksStereo2._44c3_s_p9_0, ResBooksStereo2._44c3_s_p9_1, ResBooksStereo2._44c3_s_p9_2}
            }
    );

    private static final StaticBookBlock _resbook_44s_4 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null}, {null, null, ResBooksStereo2._44c4_s_p1_0}, {null, null, ResBooksStereo2._44c4_s_p2_0}, {null, null, ResBooksStereo2._44c4_s_p3_0},
                    {null, null, ResBooksStereo2._44c4_s_p4_0}, {null, null, ResBooksStereo2._44c4_s_p5_0}, {null, null, ResBooksStereo2._44c4_s_p6_0},
                    {ResBooksStereo2._44c4_s_p7_0, ResBooksStereo2._44c4_s_p7_1}, {ResBooksStereo2._44c4_s_p8_0, ResBooksStereo2._44c4_s_p8_1},
                    {ResBooksStereo2._44c4_s_p9_0, ResBooksStereo2._44c4_s_p9_1, ResBooksStereo2._44c4_s_p9_2}
            }
    );

    private static final StaticBookBlock _resbook_44s_5 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null}, {null, null, ResBooksStereo2._44c5_s_p1_0}, {null, null, ResBooksStereo2._44c5_s_p2_0}, {null, null, ResBooksStereo2._44c5_s_p3_0},
                    {null, null, ResBooksStereo2._44c5_s_p4_0}, {null, null, ResBooksStereo2._44c5_s_p5_0}, {null, null, ResBooksStereo2._44c5_s_p6_0},
                    {ResBooksStereo2._44c5_s_p7_0, ResBooksStereo2._44c5_s_p7_1}, {ResBooksStereo2._44c5_s_p8_0, ResBooksStereo2._44c5_s_p8_1},
                    {ResBooksStereo2._44c5_s_p9_0, ResBooksStereo2._44c5_s_p9_1, ResBooksStereo2._44c5_s_p9_2}
            }
    );

    private static final StaticBookBlock _resbook_44s_6 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null}, {null, null, ResBooksStereo2._44c6_s_p1_0}, {null, null, ResBooksStereo2._44c6_s_p2_0}, {null, null, ResBooksStereo2._44c6_s_p3_0},
                    {null, null, ResBooksStereo2._44c6_s_p4_0},
                    {ResBooksStereo2._44c6_s_p5_0, ResBooksStereo2._44c6_s_p5_1},
                    {ResBooksStereo2._44c6_s_p6_0, ResBooksStereo2._44c6_s_p6_1},
                    {ResBooksStereo2._44c6_s_p7_0, ResBooksStereo2._44c6_s_p7_1},
                    {ResBooksStereo2._44c6_s_p8_0, ResBooksStereo2._44c6_s_p8_1},
                    {ResBooksStereo2._44c6_s_p9_0, ResBooksStereo2._44c6_s_p9_1, ResBooksStereo3._44c6_s_p9_2}
            }
    );

    private static final StaticBookBlock _resbook_44s_7 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null}, {null, null, ResBooksStereo3._44c7_s_p1_0}, {null, null, ResBooksStereo3._44c7_s_p2_0}, {null, null, ResBooksStereo3._44c7_s_p3_0},
                    {null, null, ResBooksStereo3._44c7_s_p4_0},
                    {ResBooksStereo3._44c7_s_p5_0, ResBooksStereo3._44c7_s_p5_1},
                    {ResBooksStereo3._44c7_s_p6_0, ResBooksStereo3._44c7_s_p6_1},
                    {ResBooksStereo3._44c7_s_p7_0, ResBooksStereo3._44c7_s_p7_1},
                    {ResBooksStereo3._44c7_s_p8_0, ResBooksStereo3._44c7_s_p8_1},
                    {ResBooksStereo3._44c7_s_p9_0, ResBooksStereo3._44c7_s_p9_1, ResBooksStereo3._44c7_s_p9_2}
            }
    );

    private static final StaticBookBlock _resbook_44s_8 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null}, {null, null, ResBooksStereo3._44c8_s_p1_0}, {null, null, ResBooksStereo3._44c8_s_p2_0}, {null, null, ResBooksStereo3._44c8_s_p3_0},
                    {null, null, ResBooksStereo3._44c8_s_p4_0},
                    {ResBooksStereo3._44c8_s_p5_0, ResBooksStereo3._44c8_s_p5_1},
                    {ResBooksStereo3._44c8_s_p6_0, ResBooksStereo3._44c8_s_p6_1},
                    {ResBooksStereo3._44c8_s_p7_0, ResBooksStereo3._44c8_s_p7_1},
                    {ResBooksStereo3._44c8_s_p8_0, ResBooksStereo3._44c8_s_p8_1},
                    {ResBooksStereo3._44c8_s_p9_0, ResBooksStereo3._44c8_s_p9_1, ResBooksStereo3._44c8_s_p9_2}
            }
    );

    private static final StaticBookBlock _resbook_44s_9 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null}, {null, null, ResBooksStereo3._44c9_s_p1_0}, {null, null, ResBooksStereo3._44c9_s_p2_0}, {null, null, ResBooksStereo3._44c9_s_p3_0},
                    {null, null, ResBooksStereo3._44c9_s_p4_0},
                    {ResBooksStereo3._44c9_s_p5_0, ResBooksStereo3._44c9_s_p5_1},
                    {ResBooksStereo3._44c9_s_p6_0, ResBooksStereo3._44c9_s_p6_1},
                    {ResBooksStereo3._44c9_s_p7_0, ResBooksStereo3._44c9_s_p7_1},
                    {ResBooksStereo3._44c9_s_p8_0, ResBooksStereo3._44c9_s_p8_1},
                    {ResBooksStereo3._44c9_s_p9_0, ResBooksStereo3._44c9_s_p9_1, ResBooksStereo3._44c9_s_p9_2}
            }
    );

    private static final ResidueTemplate _res_44s_n1[] = {
            new ResidueTemplate(2, 0, 32, _residue_44_low,
                    ResBooksStereo4._huff_book__44cn1_s_short, ResBooksStereo4._huff_book__44cn1_sm_short,
                    _resbook_44s_n1, _resbook_44sm_n1),

            new ResidueTemplate(2, 0, 32, _residue_44_low,
                    ResBooksStereo4._huff_book__44cn1_s_long, ResBooksStereo4._huff_book__44cn1_sm_long,
                    _resbook_44s_n1, _resbook_44sm_n1)
    };

    private static final ResidueTemplate _res_44s_0[] = {
            new ResidueTemplate(2, 0, 16, _residue_44_low,
                    ResBooksStereo4._huff_book__44c0_s_short, ResBooksStereo4._huff_book__44c0_sm_short,
                    _resbook_44s_0, _resbook_44sm_0),

            new ResidueTemplate(2, 0, 32, _residue_44_low,
                    ResBooksStereo3._huff_book__44c0_s_long, ResBooksStereo4._huff_book__44c0_sm_long,
                    _resbook_44s_0, _resbook_44sm_0)
    };

    private static final ResidueTemplate _res_44s_1[] = {
            new ResidueTemplate(2, 0, 16, _residue_44_low,
                    ResBooksStereo4._huff_book__44c1_s_short, ResBooksStereo4._huff_book__44c1_sm_short,
                    _resbook_44s_1, _resbook_44sm_1),

            new ResidueTemplate(2, 0, 32, _residue_44_low,
                    ResBooksStereo4._huff_book__44c1_s_long, ResBooksStereo4._huff_book__44c1_sm_long,
                    _resbook_44s_1, _resbook_44sm_1)
    };

    private static final ResidueTemplate _res_44s_2[] = {
            new ResidueTemplate(2, 0, 16, _residue_44_mid,
                    ResBooksStereo2._huff_book__44c2_s_short, ResBooksStereo2._huff_book__44c2_s_short,
                    _resbook_44s_2, _resbook_44s_2),

            new ResidueTemplate(2, 0, 32, _residue_44_mid,
                    ResBooksStereo1._huff_book__44c2_s_long, ResBooksStereo1._huff_book__44c2_s_long,
                    _resbook_44s_2, _resbook_44s_2)
    };

    private static final ResidueTemplate _res_44s_3[] = {
            new ResidueTemplate(2, 0, 16, _residue_44_mid,
                    ResBooksStereo2._huff_book__44c3_s_short, ResBooksStereo2._huff_book__44c3_s_short,
                    _resbook_44s_3, _resbook_44s_3),

            new ResidueTemplate(2, 0, 32, _residue_44_mid,
                    ResBooksStereo2._huff_book__44c3_s_long, ResBooksStereo2._huff_book__44c3_s_long,
                    _resbook_44s_3, _resbook_44s_3)
    };

    private static final ResidueTemplate _res_44s_4[] = {
            new ResidueTemplate(2, 0, 16, _residue_44_mid,
                    ResBooksStereo2._huff_book__44c4_s_short, ResBooksStereo2._huff_book__44c4_s_short,
                    _resbook_44s_4, _resbook_44s_4),

            new ResidueTemplate(2, 0, 32, _residue_44_mid,
                    ResBooksStereo2._huff_book__44c4_s_long, ResBooksStereo2._huff_book__44c4_s_long,
                    _resbook_44s_4, _resbook_44s_4)
    };

    private static final ResidueTemplate _res_44s_5[] = {
            new ResidueTemplate(2, 0, 16, _residue_44_mid,
                    ResBooksStereo2._huff_book__44c5_s_short, ResBooksStereo2._huff_book__44c5_s_short,
                    _resbook_44s_5, _resbook_44s_5),

            new ResidueTemplate(2, 0, 32, _residue_44_mid,
                    ResBooksStereo2._huff_book__44c5_s_long, ResBooksStereo2._huff_book__44c5_s_long,
                    _resbook_44s_5, _resbook_44s_5)
    };

    private static final ResidueTemplate _res_44s_6[] = {
            new ResidueTemplate(2, 0, 16, _residue_44_high,
                    ResBooksStereo3._huff_book__44c6_s_short, ResBooksStereo3._huff_book__44c6_s_short,
                    _resbook_44s_6, _resbook_44s_6),

            new ResidueTemplate(2, 0, 32, _residue_44_high,
                    ResBooksStereo2._huff_book__44c6_s_long, ResBooksStereo2._huff_book__44c6_s_long,
                    _resbook_44s_6, _resbook_44s_6)
    };

    private static final ResidueTemplate _res_44s_7[] = {
            new ResidueTemplate(2, 0, 16, _residue_44_high,
                    ResBooksStereo3._huff_book__44c7_s_short, ResBooksStereo3._huff_book__44c7_s_short,
                    _resbook_44s_7, _resbook_44s_7),

            new ResidueTemplate(2, 0, 32, _residue_44_high,
                    ResBooksStereo3._huff_book__44c7_s_long, ResBooksStereo3._huff_book__44c7_s_long,
                    _resbook_44s_7, _resbook_44s_7)
    };

    private static final ResidueTemplate _res_44s_8[] = {
            new ResidueTemplate(2, 0, 16, _residue_44_high,
                    ResBooksStereo3._huff_book__44c8_s_short, ResBooksStereo3._huff_book__44c8_s_short,
                    _resbook_44s_8, _resbook_44s_8),

            new ResidueTemplate(2, 0, 32, _residue_44_high,
                    ResBooksStereo3._huff_book__44c8_s_long, ResBooksStereo3._huff_book__44c8_s_long,
                    _resbook_44s_8, _resbook_44s_8)
    };

    private static final ResidueTemplate _res_44s_9[] = {
            new ResidueTemplate(2, 0, 16, _residue_44_high,
                    ResBooksStereo3._huff_book__44c9_s_short, ResBooksStereo3._huff_book__44c9_s_short,
                    _resbook_44s_9, _resbook_44s_9),

            new ResidueTemplate(2, 0, 32, _residue_44_high,
                    ResBooksStereo3._huff_book__44c9_s_long, ResBooksStereo3._huff_book__44c9_s_long,
                    _resbook_44s_9, _resbook_44s_9)
    };

    protected static final MappingTemplate _mapres_template_44_stereo[] = {
            new MappingTemplate(Setup._map_nominal, _res_44s_n1), /* -1 */
            new MappingTemplate(Setup._map_nominal, _res_44s_0), /* 0 */
            new MappingTemplate(Setup._map_nominal, _res_44s_1), /* 1 */
            new MappingTemplate(Setup._map_nominal, _res_44s_2), /* 2 */
            new MappingTemplate(Setup._map_nominal, _res_44s_3), /* 3 */
            new MappingTemplate(Setup._map_nominal, _res_44s_4), /* 4 */
            new MappingTemplate(Setup._map_nominal, _res_44s_5), /* 5 */
            new MappingTemplate(Setup._map_nominal, _res_44s_6), /* 6 */
            new MappingTemplate(Setup._map_nominal, _res_44s_7), /* 7 */
            new MappingTemplate(Setup._map_nominal, _res_44s_8), /* 8 */
            new MappingTemplate(Setup._map_nominal, _res_44s_9), /* 9 */
    };
}
