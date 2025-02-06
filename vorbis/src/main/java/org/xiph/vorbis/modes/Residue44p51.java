package org.xiph.vorbis.modes;

import org.xiph.vorbis.InfoMapping0;
import org.xiph.vorbis.InfoResidue0;
import org.xiph.vorbis.StaticBookBlock;
import org.xiph.vorbis.StaticCodebook;
import org.xiph.vorbis.MappingTemplate;
import org.xiph.vorbis.ResidueTemplate;
import org.xiph.vorbis.books.ResBooks51_1;
import org.xiph.vorbis.books.ResBooks51_10;
import org.xiph.vorbis.books.ResBooks51_11;
import org.xiph.vorbis.books.ResBooks51_12;
import org.xiph.vorbis.books.ResBooks51_13;
import org.xiph.vorbis.books.ResBooks51_14;
import org.xiph.vorbis.books.ResBooks51_15;
import org.xiph.vorbis.books.ResBooks51_2;
import org.xiph.vorbis.books.ResBooks51_3;
import org.xiph.vorbis.books.ResBooks51_4;
import org.xiph.vorbis.books.ResBooks51_5;
import org.xiph.vorbis.books.ResBooks51_6;
import org.xiph.vorbis.books.ResBooks51_7;
import org.xiph.vorbis.books.ResBooks51_8;
import org.xiph.vorbis.books.ResBooks51_9;

/**
 * toplevel residue templates for 32/44.1/48kHz uncoupled
 */

public class Residue44p51 {

    private static final InfoResidue0 _residue_44p_lo = new InfoResidue0(
            0, -1, -1, 7, -1, -1,
            /* 0   1   2   3   4   5   6   7   8  */
            new int[]{0},
            new int[]{-1},
            new int[]{0, 1, 2, 7, 17, 31},
            new int[]{0, 0, 99, 7, 17, 31}
    );

    private static final InfoResidue0 _residue_44p = new InfoResidue0(
            0, -1, -1, 8, -1, -1,
            /* 0   1   2   3   4   5   6   7   8  */
            new int[]{0},
            new int[]{-1},
            new int[]{0, 1, 1, 2, 7, 17, 31},
            new int[]{0, 0, 99, 99, 7, 17, 31}
    );

    private static final InfoResidue0 _residue_44p_hi = new InfoResidue0(
            0, -1, -1, 8, -1, -1,
            /* 0   1   2   3   4   5   6   7   8  */
            new int[]{0},
            new int[]{-1},
            new int[]{0, 1, 2, 4, 7, 17, 31},
            new int[]{0, 1, 2, 4, 7, 17, 31}
    );

    private static final InfoResidue0 _residue_44p_lfe = new InfoResidue0(
            0, -1, -1, 2, -1, -1,
            /* 0   1   2   3   4   5   6   7   8  */
            new int[]{0},
            new int[]{-1},
            new int[]{32},
            new int[]{-1}
    );

    private static final StaticBookBlock _resbook_44p_n1 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null},
                    {null, ResBooks51_14._44pn1_p1_0},

                    {ResBooks51_14._44pn1_p2_0, ResBooks51_14._44pn1_p2_1, null},
                    {ResBooks51_14._44pn1_p3_0, ResBooks51_15._44pn1_p3_1, null},
                    {ResBooks51_15._44pn1_p4_0, ResBooks51_15._44pn1_p4_1, null},

                    {ResBooks51_15._44pn1_p5_0, ResBooks51_15._44pn1_p5_1, ResBooks51_15._44pn1_p4_1},
                    {ResBooks51_15._44pn1_p6_0, ResBooks51_15._44pn1_p6_1, ResBooks51_15._44pn1_p6_2},
            }
    );

    private static final StaticBookBlock _resbook_44p_0 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null},
                    {null, ResBooks51_1._44p0_p1_0},

                    {ResBooks51_1._44p0_p2_0, ResBooks51_1._44p0_p2_1, null},
                    {ResBooks51_1._44p0_p3_0, ResBooks51_1._44p0_p3_1, null},
                    {ResBooks51_1._44p0_p4_0, ResBooks51_1._44p0_p4_1, null},

                    {ResBooks51_1._44p0_p5_0, ResBooks51_1._44p0_p5_1, ResBooks51_1._44p0_p4_1},
                    {ResBooks51_1._44p0_p6_0, ResBooks51_1._44p0_p6_1, ResBooks51_1._44p0_p6_2},
            }
    );

    private static final StaticBookBlock _resbook_44p_1 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null},
                    {null, ResBooks51_1._44p1_p1_0},

                    {ResBooks51_1._44p1_p2_0, ResBooks51_1._44p1_p2_1, null},
                    {ResBooks51_1._44p1_p3_0, ResBooks51_2._44p1_p3_1, null},
                    {ResBooks51_2._44p1_p4_0, ResBooks51_2._44p1_p4_1, null},

                    {ResBooks51_2._44p1_p5_0, ResBooks51_2._44p1_p5_1, ResBooks51_2._44p1_p4_1},
                    {ResBooks51_2._44p1_p6_0, ResBooks51_2._44p1_p6_1, ResBooks51_2._44p1_p6_2},
            }
    );

    private static final StaticBookBlock _resbook_44p_2 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null},
                    {null, null, ResBooks51_2._44p2_p1_0},
                    {null, ResBooks51_2._44p2_p2_0, null},

                    {ResBooks51_2._44p2_p3_0, ResBooks51_2._44p2_p3_1, null},
                    {ResBooks51_2._44p2_p4_0, ResBooks51_3._44p2_p4_1, null},
                    {ResBooks51_3._44p2_p5_0, ResBooks51_3._44p2_p5_1, null},

                    {ResBooks51_3._44p2_p6_0, ResBooks51_3._44p2_p6_1, ResBooks51_3._44p2_p5_1},
                    {ResBooks51_3._44p2_p7_0, ResBooks51_3._44p2_p7_1, ResBooks51_3._44p2_p7_2, ResBooks51_3._44p2_p7_3}
            }
    );

    private static final StaticBookBlock _resbook_44p_3 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null},
                    {null, null, ResBooks51_3._44p3_p1_0},
                    {null, ResBooks51_3._44p3_p2_0, null},

                    {ResBooks51_3._44p3_p3_0, ResBooks51_3._44p3_p3_1, null},
                    {ResBooks51_3._44p3_p4_0, ResBooks51_4._44p3_p4_1, null},
                    {ResBooks51_4._44p3_p5_0, ResBooks51_4._44p3_p5_1, null},

                    {ResBooks51_4._44p3_p6_0, ResBooks51_4._44p3_p6_1, ResBooks51_4._44p3_p5_1},
                    {ResBooks51_4._44p3_p7_0, ResBooks51_4._44p3_p7_1, ResBooks51_4._44p3_p7_2, ResBooks51_4._44p3_p7_3}
            }
    );

    private static final StaticBookBlock _resbook_44p_4 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null},
                    {null, null, ResBooks51_4._44p4_p1_0},
                    {null, ResBooks51_4._44p4_p2_0, null},

                    {ResBooks51_4._44p4_p3_0, ResBooks51_4._44p4_p3_1, null},
                    {ResBooks51_4._44p4_p4_0, ResBooks51_5._44p4_p4_1, null},
                    {ResBooks51_5._44p4_p5_0, ResBooks51_5._44p4_p5_1, null},

                    {ResBooks51_5._44p4_p6_0, ResBooks51_5._44p4_p6_1, ResBooks51_5._44p4_p5_1},
                    {ResBooks51_5._44p4_p7_0, ResBooks51_5._44p4_p7_1, ResBooks51_5._44p4_p7_2, ResBooks51_5._44p4_p7_3}
            }
    );

    private static final StaticBookBlock _resbook_44p_5 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null},
                    {null, null, ResBooks51_5._44p5_p1_0},
                    {null, ResBooks51_6._44p5_p2_0, null},

                    {ResBooks51_6._44p5_p3_0, ResBooks51_6._44p5_p3_1, null},
                    {ResBooks51_6._44p5_p4_0, ResBooks51_6._44p5_p4_1, null},
                    {ResBooks51_7._44p5_p5_0, ResBooks51_7._44p5_p5_1, null},

                    {ResBooks51_7._44p5_p6_0, ResBooks51_7._44p5_p6_1, ResBooks51_7._44p5_p5_1},
                    {ResBooks51_7._44p5_p7_0, ResBooks51_7._44p5_p7_1, ResBooks51_7._44p5_p7_2, ResBooks51_7._44p5_p7_3}
            }
    );

    private static final StaticBookBlock _resbook_44p_6 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null},
                    {null, null, ResBooks51_7._44p6_p1_0},
                    {null, ResBooks51_7._44p6_p2_0, null},

                    {ResBooks51_7._44p6_p3_0, ResBooks51_7._44p6_p3_1, null},
                    {ResBooks51_7._44p6_p4_0, ResBooks51_8._44p6_p4_1, null},
                    {ResBooks51_8._44p6_p5_0, ResBooks51_8._44p6_p5_1, null},

                    {ResBooks51_8._44p6_p6_0, ResBooks51_8._44p6_p6_1, ResBooks51_8._44p6_p5_1},
                    {ResBooks51_8._44p6_p7_0, ResBooks51_8._44p6_p7_1, ResBooks51_8._44p6_p7_2, ResBooks51_8._44p6_p7_3}
            }
    );

    private static final StaticBookBlock _resbook_44p_7 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null},
                    {null, null, ResBooks51_8._44p7_p1_0},
                    {null, ResBooks51_9._44p7_p2_0, null},

                    {ResBooks51_9._44p7_p3_0, ResBooks51_9._44p7_p3_1, null},
                    {ResBooks51_9._44p7_p4_0, ResBooks51_9._44p7_p4_1, null},
                    {ResBooks51_10._44p7_p5_0, ResBooks51_10._44p7_p5_1, null},

                    {ResBooks51_10._44p7_p6_0, ResBooks51_10._44p7_p6_1, ResBooks51_10._44p7_p5_1},
                    {ResBooks51_10._44p7_p7_0, ResBooks51_10._44p7_p7_1, ResBooks51_10._44p7_p7_2, ResBooks51_10._44p7_p7_3}
            }
    );

    private static final StaticBookBlock _resbook_44p_8 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null},
                    {null, null, ResBooks51_10._44p8_p1_0},
                    {null, ResBooks51_10._44p8_p2_0, null},

                    {ResBooks51_10._44p8_p3_0, ResBooks51_10._44p8_p3_1, null},
                    {ResBooks51_10._44p8_p4_0, ResBooks51_11._44p8_p4_1, null},
                    {ResBooks51_11._44p8_p5_0, ResBooks51_11._44p8_p5_1, null},

                    {ResBooks51_11._44p8_p6_0, ResBooks51_11._44p8_p6_1, ResBooks51_11._44p8_p5_1},
                    {ResBooks51_11._44p8_p7_0, ResBooks51_12._44p8_p7_1, ResBooks51_12._44p8_p7_2, ResBooks51_12._44p8_p7_3}
            }
    );

    private static final StaticBookBlock _resbook_44p_9 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null},
                    {null, null, ResBooks51_12._44p9_p1_0},
                    {null, ResBooks51_12._44p9_p2_0, null},

                    {ResBooks51_12._44p9_p3_0, ResBooks51_12._44p9_p3_1, null},
                    {ResBooks51_12._44p9_p4_0, ResBooks51_13._44p9_p4_1, null},
                    {ResBooks51_13._44p9_p5_0, ResBooks51_13._44p9_p5_1, null},

                    {ResBooks51_13._44p9_p6_0, ResBooks51_13._44p9_p6_1, ResBooks51_13._44p9_p5_1},
                    {ResBooks51_14._44p9_p7_0, ResBooks51_14._44p9_p7_1, ResBooks51_14._44p9_p7_2, ResBooks51_14._44p9_p7_3}
            }
    );

    private static final StaticBookBlock _resbook_44p_ln1 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {ResBooks51_14._44pn1_l0_0, ResBooks51_14._44pn1_l0_1, null},
                    {ResBooks51_14._44pn1_l1_0, ResBooks51_15._44pn1_p6_1, ResBooks51_15._44pn1_p6_2},
            }
    );

    private static final StaticBookBlock _resbook_44p_l0 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {ResBooks51_1._44p0_l0_0, ResBooks51_1._44p0_l0_1, null},
                    {ResBooks51_1._44p0_l1_0, ResBooks51_1._44p0_p6_1, ResBooks51_1._44p0_p6_2},
            }
    );

    private static final StaticBookBlock _resbook_44p_l1 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {ResBooks51_1._44p1_l0_0, ResBooks51_1._44p1_l0_1, null},
                    {ResBooks51_1._44p1_l1_0, ResBooks51_2._44p1_p6_1, ResBooks51_2._44p1_p6_2},
            }
    );

    private static final StaticBookBlock _resbook_44p_l2 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {ResBooks51_2._44p2_l0_0, ResBooks51_2._44p2_l0_1, null},
                    {ResBooks51_2._44p2_l1_0, ResBooks51_3._44p2_p7_2, ResBooks51_3._44p2_p7_3},
            }
    );

    private static final StaticBookBlock _resbook_44p_l3 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {ResBooks51_3._44p3_l0_0, ResBooks51_3._44p3_l0_1, null},
                    {ResBooks51_3._44p3_l1_0, ResBooks51_4._44p3_p7_2, ResBooks51_4._44p3_p7_3},
            }
    );

    private static final StaticBookBlock _resbook_44p_l4 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {ResBooks51_4._44p4_l0_0, ResBooks51_4._44p4_l0_1, null},
                    {ResBooks51_4._44p4_l1_0, ResBooks51_5._44p4_p7_2, ResBooks51_5._44p4_p7_3},
            }
    );
    private static final StaticBookBlock _resbook_44p_l5 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {ResBooks51_5._44p5_l0_0, ResBooks51_5._44p5_l0_1, null},
                    {ResBooks51_5._44p5_l1_0, ResBooks51_7._44p5_p7_2, ResBooks51_7._44p5_p7_3},
            }
    );

    private static final StaticBookBlock _resbook_44p_l6 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {ResBooks51_7._44p6_l0_0, ResBooks51_7._44p6_l0_1, null},
                    {ResBooks51_7._44p6_l1_0, ResBooks51_8._44p6_p7_2, ResBooks51_8._44p6_p7_3},
            }
    );
    // FIXME unused _resbook_44p_l7, _resbook_44p_l8, _resbook_44p_l9
	/*private static final StaticBookBlock _resbook_44p_l7 = new StaticBookBlock(
		new StaticCodebook[][]{
		{ ResBooks51_8._44p7_l0_0, ResBooks51_8._44p7_l0_1, null},
		{ ResBooks51_8._44p7_l1_0, ResBooks51_10._44p7_p7_2, ResBooks51_10._44p7_p7_3},
		}
	);

	private static final StaticBookBlock _resbook_44p_l8 = new StaticBookBlock(
		new StaticCodebook[][]{
		{ ResBooks51_10._44p8_l0_0, ResBooks51_10._44p8_l0_1, null},
		{ ResBooks51_10._44p8_l1_0, ResBooks51_12._44p8_p7_2, ResBooks51_12._44p8_p7_3},
		}
	);

	private static final StaticBookBlock _resbook_44p_l9 = new StaticBookBlock(
		new StaticCodebook[][]{
		{ ResBooks51_12._44p9_l0_0, ResBooks51_12._44p9_l0_1, null},
		{ ResBooks51_12._44p9_l1_0, ResBooks51_14._44p9_p7_2, ResBooks51_14._44p9_p7_3},
		}
	);*/

    private static final InfoMapping0 _map_nominal_51[] = {// [2]
            new InfoMapping0(2, new int[]{0, 0, 0, 0, 0, 1}, new int[]{0, 2}, new int[]{0, 2}, 4, new int[]{0, 3, 0, 0}, new int[]{2, 4, 1, 3}),
            new InfoMapping0(2, new int[]{0, 0, 0, 0, 0, 1}, new int[]{1, 2}, new int[]{1, 2}, 4, new int[]{0, 3, 0, 0}, new int[]{2, 4, 1, 3})
    };

    private static final InfoMapping0 _map_nominal_51u[] = {// [2]
            new InfoMapping0(2, new int[]{0, 0, 0, 0, 0, 1}, new int[]{0, 2}, new int[]{0, 2}, 0, new int[]{0}, new int[]{0}),
            new InfoMapping0(2, new int[]{0, 0, 0, 0, 0, 1}, new int[]{1, 2}, new int[]{1, 2}, 0, new int[]{0}, new int[]{0})
    };

    private static final ResidueTemplate _res_44p51_n1[] = {
            new ResidueTemplate(2, 0, 30, _residue_44p_lo,
                    ResBooks51_15._huff_book__44pn1_short, ResBooks51_15._huff_book__44pn1_short,
                    _resbook_44p_n1, _resbook_44p_n1),

            new ResidueTemplate(2, 0, 30, _residue_44p_lo,
                    ResBooks51_14._huff_book__44pn1_long, ResBooks51_14._huff_book__44pn1_long,
                    _resbook_44p_n1, _resbook_44p_n1),

            new ResidueTemplate(1, 2, 6, _residue_44p_lfe,
                    ResBooks51_14._huff_book__44pn1_lfe, ResBooks51_14._huff_book__44pn1_lfe,
                    _resbook_44p_ln1, _resbook_44p_ln1)
    };

    private static final ResidueTemplate _res_44p51_0[] = {
            new ResidueTemplate(2, 0, 15, _residue_44p_lo,
                    ResBooks51_1._huff_book__44p0_short, ResBooks51_1._huff_book__44p0_short,
                    _resbook_44p_0, _resbook_44p_0),

            new ResidueTemplate(2, 0, 30, _residue_44p_lo,
                    ResBooks51_1._huff_book__44p0_long, ResBooks51_1._huff_book__44p0_long,
                    _resbook_44p_0, _resbook_44p_0),

            new ResidueTemplate(1, 2, 6, _residue_44p_lfe,
                    ResBooks51_1._huff_book__44p0_lfe, ResBooks51_1._huff_book__44p0_lfe,
                    _resbook_44p_l0, _resbook_44p_l0)
    };

    private static final ResidueTemplate _res_44p51_1[] = {
            new ResidueTemplate(2, 0, 15, _residue_44p_lo,
                    ResBooks51_2._huff_book__44p1_short, ResBooks51_2._huff_book__44p1_short,
                    _resbook_44p_1, _resbook_44p_1),

            new ResidueTemplate(2, 0, 30, _residue_44p_lo,
                    ResBooks51_1._huff_book__44p1_long, ResBooks51_1._huff_book__44p1_long,
                    _resbook_44p_1, _resbook_44p_1),

            new ResidueTemplate(1, 2, 6, _residue_44p_lfe,
                    ResBooks51_1._huff_book__44p1_lfe, ResBooks51_1._huff_book__44p1_lfe,
                    _resbook_44p_l1, _resbook_44p_l1)
    };

    private static final ResidueTemplate _res_44p51_2[] = {
            new ResidueTemplate(2, 0, 15, _residue_44p,
                    ResBooks51_3._huff_book__44p2_short, ResBooks51_3._huff_book__44p2_short,
                    _resbook_44p_2, _resbook_44p_2),

            new ResidueTemplate(2, 0, 30, _residue_44p,
                    ResBooks51_2._huff_book__44p2_long, ResBooks51_2._huff_book__44p2_long,
                    _resbook_44p_2, _resbook_44p_2),

            new ResidueTemplate(1, 2, 6, _residue_44p_lfe,
                    ResBooks51_2._huff_book__44p2_lfe, ResBooks51_2._huff_book__44p2_lfe,
                    _resbook_44p_l2, _resbook_44p_l2)
    };

    private static final ResidueTemplate _res_44p51_3[] = {
            new ResidueTemplate(2, 0, 15, _residue_44p,
                    ResBooks51_4._huff_book__44p3_short, ResBooks51_4._huff_book__44p3_short,
                    _resbook_44p_3, _resbook_44p_3),

            new ResidueTemplate(2, 0, 30, _residue_44p,
                    ResBooks51_3._huff_book__44p3_long, ResBooks51_3._huff_book__44p3_long,
                    _resbook_44p_3, _resbook_44p_3),

            new ResidueTemplate(1, 2, 6, _residue_44p_lfe,
                    ResBooks51_3._huff_book__44p3_lfe, ResBooks51_3._huff_book__44p3_lfe,
                    _resbook_44p_l3, _resbook_44p_l3)
    };

    private static final ResidueTemplate _res_44p51_4[] = {
            new ResidueTemplate(2, 0, 15, _residue_44p,
                    ResBooks51_5._huff_book__44p4_short, ResBooks51_5._huff_book__44p4_short,
                    _resbook_44p_4, _resbook_44p_4),

            new ResidueTemplate(2, 0, 30, _residue_44p,
                    ResBooks51_4._huff_book__44p4_long, ResBooks51_4._huff_book__44p4_long,
                    _resbook_44p_4, _resbook_44p_4),

            new ResidueTemplate(1, 2, 6, _residue_44p_lfe,
                    ResBooks51_4._huff_book__44p4_lfe, ResBooks51_4._huff_book__44p4_lfe,
                    _resbook_44p_l4, _resbook_44p_l4)
    };

    private static final ResidueTemplate _res_44p51_5[] = {
            new ResidueTemplate(2, 0, 15, _residue_44p_hi,
                    ResBooks51_7._huff_book__44p5_short, ResBooks51_7._huff_book__44p5_short,
                    _resbook_44p_5, _resbook_44p_5),

            new ResidueTemplate(2, 0, 30, _residue_44p_hi,
                    ResBooks51_5._huff_book__44p5_long, ResBooks51_5._huff_book__44p5_long,
                    _resbook_44p_5, _resbook_44p_5),

            new ResidueTemplate(1, 2, 6, _residue_44p_lfe,
                    ResBooks51_5._huff_book__44p5_lfe, ResBooks51_5._huff_book__44p5_lfe,
                    _resbook_44p_l5, _resbook_44p_l5)
    };

    private static final ResidueTemplate _res_44p51_6[] = {
            new ResidueTemplate(2, 0, 15, _residue_44p_hi,
                    ResBooks51_8._huff_book__44p6_short, ResBooks51_8._huff_book__44p6_short,
                    _resbook_44p_6, _resbook_44p_6),

            new ResidueTemplate(2, 0, 30, _residue_44p_hi,
                    ResBooks51_7._huff_book__44p6_long, ResBooks51_7._huff_book__44p6_long,
                    _resbook_44p_6, _resbook_44p_6),

            new ResidueTemplate(1, 2, 6, _residue_44p_lfe,
                    ResBooks51_7._huff_book__44p6_lfe, ResBooks51_7._huff_book__44p6_lfe,
                    _resbook_44p_l6, _resbook_44p_l6)
    };

    private static final ResidueTemplate _res_44p51_7[] = {
            new ResidueTemplate(2, 0, 15, _residue_44p_hi,
                    ResBooks51_10._huff_book__44p7_short, ResBooks51_10._huff_book__44p7_short,
                    _resbook_44p_7, _resbook_44p_7),

            new ResidueTemplate(2, 0, 30, _residue_44p_hi,
                    ResBooks51_8._huff_book__44p7_long, ResBooks51_8._huff_book__44p7_long,
                    _resbook_44p_7, _resbook_44p_7),

            new ResidueTemplate(1, 2, 6, _residue_44p_lfe,
                    ResBooks51_7._huff_book__44p6_lfe, ResBooks51_7._huff_book__44p6_lfe,
                    _resbook_44p_l6, _resbook_44p_l6)
    };

    private static final ResidueTemplate _res_44p51_8[] = {
            new ResidueTemplate(2, 0, 15, _residue_44p_hi,
                    ResBooks51_12._huff_book__44p8_short, ResBooks51_12._huff_book__44p8_short,
                    _resbook_44p_8, _resbook_44p_8),

            new ResidueTemplate(2, 0, 30, _residue_44p_hi,
                    ResBooks51_10._huff_book__44p8_long, ResBooks51_10._huff_book__44p8_long,
                    _resbook_44p_8, _resbook_44p_8),

            new ResidueTemplate(1, 2, 6, _residue_44p_lfe,
                    ResBooks51_7._huff_book__44p6_lfe, ResBooks51_7._huff_book__44p6_lfe,
                    _resbook_44p_l6, _resbook_44p_l6)
    };

    private static final ResidueTemplate _res_44p51_9[] = {
            new ResidueTemplate(2, 0, 15, _residue_44p_hi,
                    ResBooks51_14._huff_book__44p9_short, ResBooks51_14._huff_book__44p9_short,
                    _resbook_44p_9, _resbook_44p_9),

            new ResidueTemplate(2, 0, 30, _residue_44p_hi,
                    ResBooks51_12._huff_book__44p9_long, ResBooks51_12._huff_book__44p9_long,
                    _resbook_44p_9, _resbook_44p_9),

            new ResidueTemplate(1, 2, 6, _residue_44p_lfe,
                    ResBooks51_7._huff_book__44p6_lfe, ResBooks51_7._huff_book__44p6_lfe,
                    _resbook_44p_l6, _resbook_44p_l6)
    };

    protected static final MappingTemplate _mapres_template_44_51[] = {
            new MappingTemplate(_map_nominal_51, _res_44p51_n1), /* -1 */
            new MappingTemplate(_map_nominal_51, _res_44p51_0), /* 0 */
            new MappingTemplate(_map_nominal_51, _res_44p51_1), /* 1 */
            new MappingTemplate(_map_nominal_51, _res_44p51_2), /* 2 */
            new MappingTemplate(_map_nominal_51, _res_44p51_3), /* 3 */
            new MappingTemplate(_map_nominal_51, _res_44p51_4), /* 4 */
            new MappingTemplate(_map_nominal_51u, _res_44p51_5), /* 5 */
            new MappingTemplate(_map_nominal_51u, _res_44p51_6), /* 6 */
            new MappingTemplate(_map_nominal_51u, _res_44p51_7), /* 7 */
            new MappingTemplate(_map_nominal_51u, _res_44p51_8), /* 8 */
            new MappingTemplate(_map_nominal_51u, _res_44p51_9), /* 9 */
    };
}
