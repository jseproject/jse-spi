package org.xiph.vorbis;

/**
 * <pre>
 * const vorbis_func_residue residue2_exportbundle = {
 * &res0_pack,
 * &res0_unpack,
 * &res0_look,
 * &res0_free_info,
 * &res0_free_look,
 * &res2_class,
 * &res2_forward,
 * &res2_inverse
 * };
 * </pre>
 */
class Residue2 extends Residue1 {

    Residue2() {
        super(true, true, true);
    }

    // res0.c

    /**
     * designed for stereo or other modes where the partition size is an
     * integer multiple of the number of channels encoded in the current
     * submap
     */
    private static int[][] _2class(final Block vb, final Object vl, final int[][] in, final int ch) {
        final LookResidue0 look = (LookResidue0) vl;
        final InfoResidue0 info = look.info;

        /* move all this setup out later */
        final int samples_per_partition = info.grouping;
        final int possible_partitions1 = info.partitions - 1;
        final int n = info.end - info.begin;

        final int partvals = n / samples_per_partition;
        final int[][] partword = new int[1][partvals];// already zeroed
        final int[] classmetric1 = info.classmetric1;// java
        final int[] classmetric2 = info.classmetric2;// java

        for (int i = 0, l = info.begin / ch; i < partvals; i++) {
            int magmax = 0;
            int angmax = 0;
            for (int j = 0; j < samples_per_partition; j += ch) {
                int val = in[0][l];
                if (val < 0) {
                    val = -val;
                }
                if (val > magmax) {
                    magmax = val;
                }
                for (int k = 1; k < ch; k++) {
                    val = in[k][l];
                    if (val < 0) {
                        val = -val;
                    }
                    if (val > angmax) {
                        angmax = val;
                    }
                }
                l++;
            }

            int j = 0;
            for (; j < possible_partitions1; j++) {
                if (magmax <= classmetric1[j] &&
                        angmax <= classmetric2[j]) {
                    break;
                }
            }

            partword[0][i] = j;
        }

/* if( TRAIN_RESAUX ) {
		java.io.PrintStream of = null;
		try {
			of = new java.io.PrintStream( new java.io.FileOutputStream(
					String.format("resaux_%d.vqd", look.train_seq), true ) );
			for( i = 0; i < partvals; i++ )
				of.printf("%d, ", partword[0][i] );
			of.print("\n");
		} catch( Exception e ) {
		} finally {
			if( of != null ) of.close();
		}
} */

        look.frames++;

        return (partword);
    }

    @Override
    // long **res2_class(vorbis_block *vb,vorbis_look_residue *vl, int **in,int *nonzero,int ch)
    final int[][] fclass(final Block vb, final LookResidue vl,
                         final int[][] in, final boolean[] nonzero, final int ch) {
        int i, used = 0;
        for (i = 0; i < ch; i++) {
            if (nonzero[i]) {
                used++;
            }
        }
        if (used != 0) {
            return (_2class(vb, vl, in, ch));
        }
        //else
        //	return (0);
        return null;
    }

    @Override
	/* int res2_forward(oggpack_buffer *opb,
			vorbis_block *vb,vorbis_look_residue *vl,
			int **in,int *nonzero,int ch, long **partword,int submap) */
    /** res2 is slightly more different; all the channels are interleaved
     into a single vector and encoded. */
    final int forward(final Buffer opb,
                      final Block vb, final LookResidue vl,
                      final int[][] in, final boolean[] nonzero, final int ch, final int[][] partword, final int submap) {
        int i, j, k, used = 0;
        final int n = vb.pcmend >>> 1;

		/* don't duplicate the code; use a working vector hack for now and
		reshape ourselves into a single channel res1 */
        /* ugly; reallocs for each coupling pass :-( */
        final int[][] work = new int[1][ch * n];
        for (i = 0; i < ch; i++) {
            final int[] pcm = in[i];
            if (nonzero[i]) {
                used++;
            }
            for (j = 0, k = i; j < n; j++, k += ch) {
                work[0][k] = pcm[j];
            }
        }

        if (used != 0) {
//#ifdef TRAIN_RES
            // return _01forward( opb, vl, &work, 1, partword, _encodepart, submap);
            // return _01forward( opb, vl, work, 1, partword, this, submap );
//#else
            // (void)submap;
            return _01forward(opb, vl, work, 1, partword, this);
//#endif
        }
        //} else {
        return (0);
        //}
    }

    @Override
    // int res2_inverse(vorbis_block *vb,vorbis_look_residue *vl,
    //		float **in,int *nonzero,int ch)
    /** duplicate code here as speed is somewhat more important */
    final int inverse(final Block vb, final LookResidue vl,
                      final float[][] in, final boolean[] nonzero, final int ch) {
        final LookResidue0 look = (LookResidue0) vl;
        final InfoResidue0 info = look.info;

        /* move all this setup out later */
        final int samples_per_partition = info.grouping;
        final Codebook phrasebook = look.phrasebook;// java
        final int partitions_per_word = phrasebook.dim;
        final int max = (vb.pcmend * ch) >> 1;
        final int end = (info.end < max ? info.end : max);
        final int n = end - info.begin;

        if (n > 0) {
            int partvals = n / samples_per_partition;
            final int partwords = (partvals + partitions_per_word - 1) / partitions_per_word;
            final int[][] partword = new int[partwords][];

            int i = 0;
            for (; i < ch; i++) {
                if (nonzero[i]) {
                    break;
                }
            }
            if (i == ch) {
                return (0); /* no nonzero vectors */
            }
            final int[] secondstages = info.secondstages;// java
            final Codebook[][] partbooks = look.partbooks;// java
            final int[][] decodemap = look.decodemap;// java
            partvals *= samples_per_partition;// java
            partvals += info.begin;
            for (int s = 0, stages = look.stages; s < stages; s++) {
                i = info.begin;
                for (int l = 0; i < partvals; l++) {

                    if (s == 0) {
                        /* fetch the partition word */
                        final int temp = phrasebook.decode(vb.opb);
                        if (temp == -1 || temp >= info.partvals) {
                            return 0;// goto eopbreak;
                        }
                        partword[l] = decodemap[temp];
                        if (partword[l] == null) {
                            return 0;// goto errout;
                        }
                    }

                    final int s1 = (1 << s);// java
                    final int[] p = partword[l];
                    /* now we decode residual values for the partitions */
                    for (int k = 0; k < partitions_per_word && i < partvals; k++, i += samples_per_partition) {
                        final int p_k = p[k];// java
                        if ((secondstages[p_k] & s1) != 0) {
                            final Codebook stagebook = partbooks[p_k][s];

                            if (stagebook != null) {
                                if (stagebook.decodevv_add(in,
                                        i, ch,
                                        vb.opb, samples_per_partition) == -1) {
                                    return 0;// goto eopbreak;
                                }
                            }
                        }
                    }
                }
            }
        }
//errout:
//eopbreak:
        return (0);
    }
}
