package org.xiph.vorbis;

public class InfoResidue0 extends InfoResidue {
    /* block-partitioned VQ coded straight residue */
    int begin = 0;
    int end = 0;

    /* first stage (lossless partitioning) */
    /**
     * group n vectors per partition
     */
    int grouping = 0;
    /**
     * possible codebooks for a partition
     */
    int partitions = 0;
    /**
     * partitions ^ groupbook dim
     */
    int partvals = 0;
    /**
     * huffbook for partitioning
     */
    int groupbook = 0;
    /**
     * expanded out to pointers in lookup
     */
    final int[] secondstages = new int[64];
    /**
     * list of second stage books
     */
    final int[] booklist = new int[512];

    final int[] classmetric1 = new int[64];
    final int[] classmetric2 = new int[64];

    //
    InfoResidue0() {
    }

    InfoResidue0(InfoResidue0 r) {
        begin = r.begin;
        end = r.end;
        grouping = r.grouping;
        partitions = r.partitions;
        partvals = r.partvals;
        groupbook = r.groupbook;
        System.arraycopy(r.secondstages, 0, secondstages, 0, secondstages.length);
        System.arraycopy(r.booklist, 0, booklist, 0, booklist.length);
        System.arraycopy(r.classmetric1, 0, classmetric1, 0, classmetric1.length);
        System.arraycopy(r.classmetric2, 0, classmetric2, 0, classmetric2.length);
    }

    public InfoResidue0(int i_begin,
                        int i_end,
                        int i_grouping,
                        int i_partitions,
                        int i_partvals,
                        int i_groupbook,
                        int[] pi_secondstages,
                        int[] pi_booklist,
                        int[] pi_classmetric1,
                        int[] pi_classmetric2) {
        begin = i_begin;
        end = i_end;
        grouping = i_grouping;
        partitions = i_partitions;
        partvals = i_partvals;
        groupbook = i_groupbook;
        System.arraycopy(pi_secondstages, 0, secondstages, 0, pi_secondstages.length);
        System.arraycopy(pi_booklist, 0, booklist, 0, pi_booklist.length);
        System.arraycopy(pi_classmetric1, 0, classmetric1, 0, pi_classmetric1.length);
        System.arraycopy(pi_classmetric2, 0, classmetric2, 0, pi_classmetric2.length);
    }
}
