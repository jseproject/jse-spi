package org.xiph.vorbis;

public class InfoFloor1 extends InfoFloor {
    /**
     * 0 to 31
     */
    int partitions = 0;
    /**
     * 0 to 15
     */
    final int[] partitionclass = new int[VIF_PARTS];
    /**
     * 1 to 8
     */
    final int[] class_dim = new int[VIF_CLASS];
    /**
     * 0,1,2,3 (bits: 1<<n poss)
     */
    final int[] class_subs = new int[VIF_CLASS];
    /**
     * subs ^ dim entries
     */
    final int[] class_book = new int[VIF_CLASS];
    /**
     * [VIF_CLASS][subs]
     */
    final int[][] class_subbook = new int[VIF_CLASS][8];

    /**
     * 1 2 3 or 4
     */
    int mult = 0;
    /**
     * first two implicit
     */
    final int[] postlist = new int[VIF_POSIT + 2];

    /* encode side analysis parameters */
    float maxover = 0.0f;
    float maxunder = 0.0f;
    float maxerr = 0.0f;

    float twofitweight = 0.0f;
    float twofitatten = 0.0f;

    int n = 0;

    //
    InfoFloor1() {
    }

    public InfoFloor1(
            int i_partitions,
            int[] pi_partitionclass,
            int[] pi_class_dim,
            int[] pi_class_subs,
            int[] pi_class_book,
            int[][] pi_class_subbook,
            int i_mult,
            int[] pi_postlist,
            float f_maxover,
            float f_maxunder,
            float f_maxerr,
            float f_twofitweight,
            float f_twofitatten,
            int i_n
    ) {
        partitions = i_partitions;
        System.arraycopy(pi_partitionclass, 0, partitionclass, 0, pi_partitionclass.length);
        System.arraycopy(pi_class_dim, 0, class_dim, 0, pi_class_dim.length);
        System.arraycopy(pi_class_subs, 0, class_subs, 0, pi_class_subs.length);
        System.arraycopy(pi_class_book, 0, class_book, 0, pi_class_book.length);
        for (int i = 0; i < pi_class_subbook.length; i++) {
            System.arraycopy(pi_class_subbook[i], 0, class_subbook[i], 0, pi_class_subbook[i].length);
        }
        mult = i_mult;
        System.arraycopy(pi_postlist, 0, postlist, 0, pi_postlist.length);
        maxover = f_maxover;
        maxunder = f_maxunder;
        maxerr = f_maxerr;
        twofitweight = f_twofitweight;
        twofitatten = f_twofitatten;
        n = i_n;
    }

    InfoFloor1(InfoFloor1 vi) {
        partitions = vi.partitions;
        System.arraycopy(vi.partitionclass, 0, partitionclass, 0, vi.partitionclass.length);
        System.arraycopy(vi.class_dim, 0, class_dim, 0, vi.class_dim.length);
        System.arraycopy(vi.class_subs, 0, class_subs, 0, vi.class_subs.length);
        System.arraycopy(vi.class_book, 0, class_book, 0, vi.class_book.length);
        for (int i = 0; i < class_subbook.length; i++) {
            System.arraycopy(vi.class_subbook[i], 0, class_subbook[i], 0, vi.class_subbook[i].length);
        }
        mult = vi.mult;
        System.arraycopy(vi.postlist, 0, postlist, 0, vi.postlist.length);
        maxover = vi.maxover;
        maxunder = vi.maxunder;
        maxerr = vi.maxerr;
        twofitweight = vi.twofitweight;
        twofitatten = vi.twofitatten;
        n = vi.n;
    }
}
