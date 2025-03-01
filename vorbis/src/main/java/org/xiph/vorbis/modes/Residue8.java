package org.xiph.vorbis.modes;

import org.xiph.vorbis.StaticBookBlock;
import org.xiph.vorbis.StaticCodebook;
import org.xiph.vorbis.MappingTemplate;
import org.xiph.vorbis.ResidueTemplate;
import org.xiph.vorbis.books.ResBooksStereo1;
import org.xiph.vorbis.books.ResBooksUncoupled1;
import org.xiph.vorbis.books.ResBooksUncoupled2;

/**
 * toplevel residue templates 8/11kHz
 */

public class Residue8 {

    private static final StaticBookBlock _resbook_8s_0 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null},
                    {null, null, ResBooksStereo1._8c0_s_p1_0},
                    {null},
                    {null, null, ResBooksStereo1._8c0_s_p3_0},
                    {null, null, ResBooksStereo1._8c0_s_p4_0},
                    {null, null, ResBooksStereo1._8c0_s_p5_0},
                    {null, null, ResBooksStereo1._8c0_s_p6_0},
                    {ResBooksStereo1._8c0_s_p7_0, ResBooksStereo1._8c0_s_p7_1},
                    {ResBooksStereo1._8c0_s_p8_0, ResBooksStereo1._8c0_s_p8_1},
                    {ResBooksStereo1._8c0_s_p9_0, ResBooksStereo1._8c0_s_p9_1, ResBooksStereo1._8c0_s_p9_2}
            }
    );

    private static final StaticBookBlock _resbook_8s_1 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null},
                    {null, null, ResBooksStereo1._8c1_s_p1_0},
                    {null},
                    {null, null, ResBooksStereo1._8c1_s_p3_0},
                    {null, null, ResBooksStereo1._8c1_s_p4_0},
                    {null, null, ResBooksStereo1._8c1_s_p5_0},
                    {null, null, ResBooksStereo1._8c1_s_p6_0},
                    {ResBooksStereo1._8c1_s_p7_0, ResBooksStereo1._8c1_s_p7_1},
                    {ResBooksStereo1._8c1_s_p8_0, ResBooksStereo1._8c1_s_p8_1},
                    {ResBooksStereo1._8c1_s_p9_0, ResBooksStereo1._8c1_s_p9_1, ResBooksStereo1._8c1_s_p9_2}
            }
    );

    private static final ResidueTemplate _res_8s_0[] = {
            new ResidueTemplate(2, 0, 32, Residue44._residue_44_mid,
                    ResBooksStereo1._huff_book__8c0_s_single, ResBooksStereo1._huff_book__8c0_s_single,
                    _resbook_8s_0, _resbook_8s_0),
    };

    private static final ResidueTemplate _res_8s_1[] = {
            new ResidueTemplate(2, 0, 32, Residue44._residue_44_mid,
                    ResBooksStereo1._huff_book__8c1_s_single, ResBooksStereo1._huff_book__8c1_s_single,
                    _resbook_8s_1, _resbook_8s_1),
    };

    public static final MappingTemplate _mapres_template_8_stereo[] = {// [2]
            new MappingTemplate(Setup._map_nominal, _res_8s_0), /* 0 */
            new MappingTemplate(Setup._map_nominal, _res_8s_1), /* 1 */
    };

    private static final StaticBookBlock _resbook_8u_0 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null},
                    {null, null, ResBooksUncoupled1._8u0__p1_0},
                    {null, null, ResBooksUncoupled1._8u0__p2_0},
                    {null, null, ResBooksUncoupled1._8u0__p3_0},
                    {null, null, ResBooksUncoupled2._8u0__p4_0},
                    {null, null, ResBooksUncoupled2._8u0__p5_0},
                    {ResBooksUncoupled2._8u0__p6_0, ResBooksUncoupled2._8u0__p6_1},
                    {ResBooksUncoupled2._8u0__p7_0, ResBooksUncoupled2._8u0__p7_1, ResBooksUncoupled2._8u0__p7_2}
            }
    );

    private static final StaticBookBlock _resbook_8u_1 = new StaticBookBlock(
            new StaticCodebook[][]{
                    {null},
                    {null, null, ResBooksUncoupled2._8u1__p1_0},
                    {null, null, ResBooksUncoupled2._8u1__p2_0},
                    {null, null, ResBooksUncoupled2._8u1__p3_0},
                    {null, null, ResBooksUncoupled2._8u1__p4_0},
                    {null, null, ResBooksUncoupled2._8u1__p5_0},
                    {null, null, ResBooksUncoupled2._8u1__p6_0},
                    {ResBooksUncoupled2._8u1__p7_0, ResBooksUncoupled2._8u1__p7_1},
                    {ResBooksUncoupled2._8u1__p8_0, ResBooksUncoupled2._8u1__p8_1},
                    {ResBooksUncoupled2._8u1__p9_0, ResBooksUncoupled2._8u1__p9_1, ResBooksUncoupled2._8u1__p9_2}
            }
    );

    private static final ResidueTemplate _res_8u_0[] = {
            new ResidueTemplate(1, 0, 32, Residue44u._residue_44_low_un,
                    ResBooksUncoupled2._huff_book__8u0__single, ResBooksUncoupled2._huff_book__8u0__single,
                    _resbook_8u_0, _resbook_8u_0),
    };

    private static final ResidueTemplate _res_8u_1[] = {
            new ResidueTemplate(1, 0, 32, Residue44u._residue_44_mid_un,
                    ResBooksUncoupled2._huff_book__8u1__single, ResBooksUncoupled2._huff_book__8u1__single,
                    _resbook_8u_1, _resbook_8u_1),
    };

    protected static final MappingTemplate _mapres_template_8_uncoupled[] = {// [2]
            new MappingTemplate(Setup._map_nominal_u, _res_8u_0), /* 0 */
            new MappingTemplate(Setup._map_nominal_u, _res_8u_1), /* 1 */
    };
}
