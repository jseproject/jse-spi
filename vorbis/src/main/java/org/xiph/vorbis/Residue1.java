package org.xiph.vorbis;

/**
 * <pre>
 * const vorbis_func_residue residue1_exportbundle = {
 * &res0_pack,
 * &res0_unpack,
 * &res0_look,
 * &res0_free_info,
 * &res0_free_look,
 * &res1_class,
 * &res1_forward,
 * &res1_inverse
 * };
 * </pre>
 */
class Residue1 extends Residue0 implements EncodePart {

    Residue1(final boolean isPack, final boolean isClass, final boolean isForward) {
        super(isPack, isClass, isForward);
    }

    Residue1() {
        super(true, true, true);
    }

    // codebook.c
    // long vorbis_book_decodev_add(codebook *book,float *a,oggpack_buffer *b,int n)
    @Override
    /** decode vector / dim granularity gaurding is done in the upper layer */
    public final int decodepart(final Codebook book, final float[] a, final int offset, final Buffer b, int n) {
        if (book.used_entries > 0) {
            final float[] valuelist = book.valuelist;// java
            final int dim = book.dim;// java
            n += offset;// java
            for (int i = offset; i < n; ) {
                final int entry = book.decode_packed_entry_number(b);
                if (entry == -1) {
                    return (-1);
                }
                int t = entry * dim;// java book.valuelist + entry * book.dim;
                for (final int j = t + dim; i < n && t < j; ) {
                    a[i++] += valuelist[t++];
                }
            }
        }
        return (0);
    }

    // res0.c
    // static int local_book_besterror(codebook *book,int *a)

    /**
     * break an abstraction and copy some code for performance purposes
     */
    private static int local_book_besterror(final Codebook book, final int[] a, int offset) {
        final int dim = book.dim;
        final int minval = book.minval;
        final int del = book.delta;
        final int qv = book.quantvals;
        final int ze = (qv >> 1);
        int index = 0;
        /* assumes integer/centered encoder codebook maptype 1 no more than dim 8 */
        final int[] p = new int[8];// = {0,0,0,0,0,0,0,0};

        if (del != 1) {
            for (int i = 0, o = dim; i < dim; i++) {
                final int v = (a[--o + offset] - minval + (del >> 1)) / del;
                final int m = (v < ze ? ((ze - v) << 1) - 1 : ((v - ze) << 1));
                index = index * qv + (m < 0 ? 0 : (m >= qv ? qv - 1 : m));
                p[o] = v * del + minval;
            }
        } else {
            for (int i = 0, o = dim; i < dim; i++) {
                final int v = a[--o + offset] - minval;
                final int m = (v < ze ? ((ze - v) << 1) - 1 : ((v - ze) << 1));
                index = index * qv + (m < 0 ? 0 : (m >= qv ? qv - 1 : m));
                p[o] = v * del + minval;
            }
        }

        final byte[] lengthlist = book.c.lengthlist;
        if (lengthlist[index] <= 0) {
            int best = -1;
            /* assumes integer/centered encoder codebook maptype 1 no more than dim 8 */
            final int[] e = new int[8];//={0,0,0,0,0,0,0,0};
            final int delta = book.delta;// java
            final int maxval = book.minval + delta * (book.quantvals - 1);
            for (int i = 0, ie = book.entries; i < ie; i++) {
                if (lengthlist[i] > 0) {
                    int ithis = 0;
                    for (int j = 0, o = offset; j < dim; j++, o++) {
                        final int val = (e[j] - a[o]);
                        ithis += val * val;
                    }
                    if (best == -1 || ithis < best) {
                        System.arraycopy(e, 0, p, 0, p.length);
                        best = ithis;
                        index = i;
                    }
                }
                /* assumes the value patterning created by the tools in vq/ */
                int j = 0;
                while (e[j] >= maxval) {
                    e[j++] = 0;
                }
                if (e[j] >= 0) {
                    e[j] += delta;
                }
                e[j] = -e[j];
            }
        }

        if (index > -1) {
            for (int i = 0; i < dim; i++) {
                a[offset++] -= p[i];
            }
        }

        return (index);
    }

    @Override
//#ifdef TRAIN_RES
//	static int _encodepart(oggpack_buffer *opb,int *vec, int n, codebook *book,long *acc)
//#else
//	static int _encodepart(oggpack_buffer *opb,int *vec, int n, codebook *book)
//#endif
    public final int encodepart(final Buffer opb,
                                final int[] vec, final int offset, final int n, final Codebook book) {//, final int[] acc) {
        int bits = 0;
        final int dim = book.dim;
        final int step = (n / dim) * dim + offset;

        for (int i = offset; i < step; i += dim) {
            final int entry = local_book_besterror(book, vec, i);

/* if( TRAIN_RES ) {
		if( entry >= 0 )
			acc[entry]++;
} */

            bits += book.encode(entry, opb);

        }

        return (bits);
    }

    // static long **_01class(vorbis_block *vb,vorbis_look_residue *vl, int **in,int ch)
    private static int[][] _01class(final Block vb, final Object vl, final int[][] in, final int ch) {
        final LookResidue0 look = (LookResidue0) vl;
        final InfoResidue0 info = look.info;

        /* move all this setup out later */
        final int samples_per_partition = info.grouping;
        final int possible_partitions1 = info.partitions - 1;
        final int n = info.end - info.begin;

        final int partvals = n / samples_per_partition;
        final int[][] partword = new int[ch][n / samples_per_partition];// already zeroed
        final float scale = 100.f / samples_per_partition;

		/* we find the partition type for each partition of each
		channel.  We'll go back and do the interleaved encoding in a
		bit.  For now, clarity */

        final int[] classmetric1 = info.classmetric1;// java
        final int[] classmetric2 = info.classmetric2;// java
        for (int i = 0, offset = info.begin; i < partvals; i++, offset += samples_per_partition) {
            final int endk = samples_per_partition + offset;
            for (int j = 0; j < ch; j++) {
                final int[] in_j = in[j];
                int max = 0;
                int ent = 0;
                for (int k = offset; k < endk; k++) {
                    int val = in_j[k];
                    if (val < 0) {
                        val = -val;
                    }
                    if (val > max) {
                        max = val;
                    }
                    ent += val;
                }
                ent *= scale;

                int k = 0;
                for (; k < possible_partitions1; k++) {
                    final int v = classmetric2[k];// java
                    if (max <= classmetric1[k] &&
                            (v < 0 || ent < v)) {
                        break;
                    }
                }

                partword[j][i] = k;
            }
        }

/* if( TRAIN_RESAUX ) {
		{
			java.io.PrintStream of = null;
			try {
				for( i = 0; i < ch; i++ ) {
					of = new java.io.PrintStream( new java.io.FileOutputStream(
						String.format("resaux_%d.vqd", look.train_seq), true ) );
					for( j = 0; j < partvals; j++ )
						of.printf( "%d, ", partword[i][j] );
					of.printf( "\n" );
				}
			} catch(Exception e) {
			} finally {
				if( of != null ) of.close();
			}
		}
} */
        look.frames++;

        return (partword);
    }

    @Override
        // long **res1_class(vorbis_block *vb,vorbis_look_residue *vl,int **in,int *nonzero,int ch)
    int[][] fclass(final Block vb, final LookResidue vl,
                   final int[][] in, final boolean[] nonzero, final int ch) {
        int used = 0;
        for (int i = 0; i < ch; i++) {
            if (nonzero[i]) {
                in[used++] = in[i];
            }
        }

        if (used != 0) {
            return (_01class(vb, vl, in, used));
        }
        //else
        //return (0);
        return null;
    }

    static final int _01forward(final Buffer opb,
                                final Object vl,
                                final int[][] in, final int ch,
                                final int[][] partword,

//#ifdef TRAIN_RES
//						int (*encode)(oggpack_buffer *,int *,int,codebook *,long *),
//						int submap
// #else
//						int (*encode)(oggpack_buffer *,int *,int,codebook *)
                                final EncodePart encode
// #endif
    ) {
        final LookResidue0 look = (LookResidue0) vl;
        final InfoResidue0 info = look.info;

/* if( TRAIN_RES ) {
		look.submap = submap;
} */

        /* move all this setup out later */
        final int samples_per_partition = info.grouping;
        final int possible_partitions = info.partitions;
        final int partitions_per_word = look.phrasebook.dim;
        final int n = info.end - info.begin;

        final int partvals = n / samples_per_partition;
        final int[] resbits = new int[128];// already zeroed
        final int[] resvals = new int[128];// already zeroed

/* if( TRAIN_RES ) {
		for( i = 0; i < ch; i++ )
			for( j = info.begin; j < info.end; j++ ) {
				if( in[i][j] > look.tmax ) look.tmax = in[i][j];
				if( in[i][j] < look.tmin ) look.tmin = in[i][j];
			}
}*/

		/* we code the partition words for each channel, then the residual
		words for a partition per channel until we've written all the
		residual words for that partition word.  Then write the next
		partition channel words... */
        final int entries = look.phrasebook.entries;// java
        final int[] secondstages = info.secondstages;// java
        final Codebook[][] partbooks = look.partbooks;// java
        for (int s = 0, stages = look.stages; s < stages; s++) {

            for (int i = 0; i < partvals; ) {

                /* first we encode a partition codeword for each channel */
                if (s == 0) {
                    for (int j = 0; j < ch; j++) {
                        int val = partword[j][i];
                        for (int k = 1 + i, ke = partitions_per_word + i; k < ke; k++) {
                            val *= possible_partitions;
                            if (k < partvals) {
                                val += partword[j][k];
                            }
                        }

                        /* training hack */
                        if (val < entries) {
                            look.phrasebits += look.phrasebook.encode(val, opb);
//#if 0 /*def TRAIN_RES*/
//						else
//							System.err.print("!");
//#endif
                        }

                    }
                }

                /* now we encode interleaved residual values for the partitions */
                for (int k = 0; k < partitions_per_word && i < partvals; k++, i++) {
                    final int offset = i * samples_per_partition + info.begin;

                    for (int j = 0; j < ch; j++) {
                        final int pw = partword[j][i];// jaav
                        if (s == 0) {
                            resvals[pw] += samples_per_partition;
                        }
                        if ((secondstages[pw] & (1 << s)) != 0) {
                            final Codebook statebook = partbooks[pw][s];
                            if (statebook != null) {
                                final int ret;

/* if( TRAIN_RES ) {
								int[] accumulator = look.training_data[s][ partword[j][i] ];
								{
									int l;
									int[] samples = in[j];
									for( l = 0; l < samples_per_partition; l++ ) {
										if( samples[offset + l] < look.training_min[s][ partword[j][i] ] )
											look.training_min[s][ partword[j][i] ] = samples[offset + l];
										if( samples[offset + l] > look.training_max[s][ partword[j][i] ] )
											look.training_max[s][ partword[j][i] ] = samples[offset + l];
									}
								}
}

								ret = encode.encodepart( opb, in[j], offset, samples_per_partition,
												statebook, null );// accumulator);
#else */
                                ret = encode.encodepart(opb, in[j], offset, samples_per_partition,
                                        statebook);
//#endif

                                look.postbits += ret;
                                resbits[pw] += ret;
                            }
                        }
                    }
                }
            }
        }

        return (0);
    }

    @Override
        // int res1_forward(oggpack_buffer *opb,vorbis_block *vb,vorbis_look_residue *vl,
        // 		int **in,int *nonzero,int ch, long **partword, int submap)
    int forward(final Buffer opb, final Block vb, final LookResidue vl,
                final int[][] in, final boolean[] nonzero, final int ch, final int[][] partword, final int submap) {
        // (void)vb;
        int used = 0;
        for (int i = 0; i < ch; i++) {
            if (nonzero[i]) {
                in[used++] = in[i];
            }
        }

        if (used != 0) {
//#ifdef TRAIN_RES
            // return _01forward( opb, vl, in, used, partword, _encodepart, submap);
            // return _01forward( opb, vb, vl, in, used, partword, this, submap );
//#else
//			(void)submap;
            return _01forward(opb, vl, in, used, partword, this);
//#endif
        }//} else {
        return (0);
        //}
    }

    @Override
    int inverse(final Block vb, final LookResidue vl,
                final float[][] in, final boolean[] nonzero, final int ch) {
        int used = 0;
        for (int i = 0; i < ch; i++) {
            if (nonzero[i]) {
                in[used++] = in[i];
            }
        }
        if (used != 0) {
            // return (_01inverse( vb, vl, in, used, vorbis_book_decodev_add ) );
            return (_01inverse(vb, vl, in, used, this));
        } //else
        return (0);
    }
}
