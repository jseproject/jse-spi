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
