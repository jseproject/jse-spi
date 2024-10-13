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

package org.xiph.vorbis;

/**
 * <pre>
 * const vorbis_func_residue residue0_exportbundle = {
 * NULL,
 * &res0_unpack,
 * &res0_look,
 * &res0_free_info,
 * &res0_free_look,
 * NULL,
 * NULL,
 * &res0_inverse
 * };
 * </pre>
 */
class Residue0 extends FuncResidue implements DecodePart {
    //private static final boolean TRAIN_RES = false;
    //private static final boolean TRAIN_RESAUX = false;

/* #if defined(TRAIN_RES) || defined(TRAIN_RESAUX)
	static int train_seq = 0;
#endif */

    Residue0(final boolean isPack, final boolean isClass, final boolean isForward) {
        super(isPack, isClass, isForward);
    }

    Residue0() {
        super(false, false, false);
    }

    // codebook.c
    // long vorbis_book_decodevs_add(codebook *book,float *a,oggpack_buffer *b,int n)

    /**
     * returns 0 on OK or -1 on eof
     * decode vector / dim granularity gaurding is done in the upper layer
     */
    @Override
    public int decodepart(final Codebook book, final float[] a, final int offset, final Buffer b, int n) {
        if (book.used_entries > 0) {
            final int dim = book.dim;// java
            final int step = n / dim;
            final int[] entry = new int[step];
            final float[] valuelist = book.valuelist;
            final int[] t = new int[step];

            for (int i = 0; i < step; i++) {
                final int j = book.decode_packed_entry_number(b);
                entry[i] = j;
                if (j == -1) {
                    return (-1);
                }
                t[i] = j * dim;
            }
            n += offset;
            for (int i = 0, o = offset; i < dim; i++, o += step) {
                for (int j = 0, k = o; k < n && j < step; j++, k++) {
                    a[k] += valuelist[t[j] + i];
                }
            }
        }
        return (0);
    }

    // res0.c
    // res0_free_info(vorbis_info_residue *i) -> use vorbis_info_residue = null
	/*private static void res0_free_info(vorbis_info_residue *i) {
		vorbis_info_residue0 *info = (vorbis_info_residue0 *)i;
		if( info ) {
			memset( info, 0, sizeof(*info) );
			_ogg_free( info );
		}
	}*/

    @Override
    // void res0_free_look(vorbis_look_residue *i)
    /** use only to debug. vorbis_look_residue = null */
    final void free_look(final LookResidue i) {
        if (i != null) {

/* if( TRAIN_RES ) {
			{
				int j, k, l;
				final LookResidue0 look = (LookResidue0)i;
				for( j = 0; j < look.parts; j++ ) {
					/// System.err.printf("partition %d: ", j );
					for( k = 0; k < 8; k++ )
						if( look.training_data[k][j] != 0 ) {
							final byte[] buffer = new byte[80];
							java.io.PrintStream of = null;
							try {
								Codebook statebook = look.partbooks[j][k];

								// long and short into the same bucket by current convention
								of = new java.io.PrintStream( new java.io.FileOutputStream(
									String.format("res_sub%d_part%d_pass%d.vqd", look.submap, j, k), true) );

								for( l = 0; l < statebook.entries; l++ )
									of.printf("%d:%ld\n", l, look.training_data[k][j][l] );
							} catch( Exception e ) {
							} finally {
								if( of != null ) of.close();
							}

							/// System.err.printf("%d(%.2f|%.2f) ", k,
								look.training_min[k][j], look.training_max[k][j] );

							look.training_data[k][j] = null;
						}
					/// System.err.printf("\n");
				}
			}
			System.err.printf("min/max residue: %g::%g\n", look.tmin, look.tmax );

			/// System.err.printf("residue bit usage %f:%f (%f total)\n",
			///		(float)look.phrasebits / look.frames,
			///		(float)look.postbits / look.frames,
			///		(float)(look.postbits + look.phrasebits) / look.frames );
} */


            //InfoResidue0 info = look.info;

            /// System.err.printf(
            ///		"%d frames encoded in %d phrasebits and %d residue bits "
            ///		"(%g/frame) \n", look.frames, look.phrasebits,
            ///		look.resbitsflat,
            ///        (look.phrasebits + look.resbitsflat) / (float)look.frames );

            ///for( j = 0; j < look.parts; j++ ) {
            ///	int acc = 0;
            ///	System.err.printf("\t[%d] == ", j );
            ///	for( k = 0; k < look.stages; k++ )
            ///		if( ((info.secondstages[j] >> k) & 1) != 0 ) {
            ///			System.err.printf("%d,", look->resbits[j][k] );
            ///			acc += look.resbits[j][k];
            ///        }

            ///    System.err.printf(":: (%d vals) %1.2fbits/sample\n", look.resvals[j],
            ///		acc != 0 ? (float)acc / (look.resvals[j] * info.grouping) : 0 );
            ///}
            ///System.err.printf("\n");

            //for( j = 0; j < look.parts; j++ )
            //	if( look.partbooks[j] != null ) look.partbooks[j] = null;
            //look.partbooks = null;
            //for( j = 0; j < look.partvals; j++ )
            //	look.decodemap[j] = null;
            //look.decodemap = null;

            //look.clear();
            //look = null;
        }
    }

    private static int icount(int v) {
        int ret = 0;
        while (v != 0) {
            ret += v & 1;
            v >>>= 1;
        }
        return (ret);
    }

    @Override
    // res0_pack(vorbis_info_residue *vr, Buffer opb)
    final void pack(final InfoResidue vr, final Buffer opb) {
        final InfoResidue0 info = (InfoResidue0) vr;
        int j, acc = 0;
        opb.pack_write(info.begin, 24);
        opb.pack_write(info.end, 24);

        opb.pack_write(info.grouping - 1, 24);  /* residue vectors to group and
														code with a partitioned book */
        final int partitions = info.partitions;// java
        opb.pack_write(partitions - 1, 6); /* possible partition choices */
        opb.pack_write(info.groupbook, 8);  /* group huffman book */

		/* secondstages is a bitmask; as encoding progresses pass by pass, a
		bitmask of one indicates this partition class has bits to write
		this pass */
        int[] tmp = info.secondstages;// java
        for (j = 0; j < partitions; j++) {
            final int s = tmp[j];
            if (Codec.ilog(s) > 3) {
                /* yes, this is a minor hack due to not thinking ahead */
                opb.pack_write(s, 3);
                opb.pack_write(1, 1);
                opb.pack_write(s >> 3, 5);
            } else {
                opb.pack_write(s, 4); /* trailing zero */
            }
            acc += icount(s);
        }
        tmp = info.booklist;// java
        for (j = 0; j < acc; j++) {
            opb.pack_write(tmp[j], 8);
        }

    }

    @Override
    // vorbis_info_residue *res0_unpack(vorbis_info *vi,oggpack_buffer *opb)
    /** vorbis_info is for range checking */
    final InfoResidue unpack(final Info vi, final Buffer opb) {
        final InfoResidue0 info = new InfoResidue0();
        final CodecSetupInfo ci = vi.codec_setup;

        info.begin = opb.pack_read(24);
        info.end = opb.pack_read(24);
        info.grouping = opb.pack_read(24) + 1;
        info.partitions = opb.pack_read(6) + 1;
        info.groupbook = opb.pack_read(8);

        /* check for premature EOP */
        if (info.groupbook < 0) {
            return null;// goto errout;
        }

        final int partitions = info.partitions;// java
        int[] tmp = info.secondstages;// java
        int j, acc = 0;
        for (j = 0; j < partitions; j++) {
            int cascade = opb.pack_read(3);
            final int cflag = opb.pack_read(1);
            if (cflag < 0) {
                return null;// goto errout;
            }
            if (cflag != 0) {
                final int c = opb.pack_read(5);
                if (c < 0) {
                    return null;// goto errout;
                }
                cascade |= (c << 3);
            }
            tmp[j] = cascade;

            acc += icount(cascade);
        }
        tmp = info.booklist;// java
        for (j = 0; j < acc; j++) {
            final int book = opb.pack_read(8);
            if (book < 0) {
                return null;// goto errout;
            }
            tmp[j] = book;
        }

        if (info.groupbook >= ci.books) {
            return null;// goto errout;
        }
        final StaticCodebook[] cb = ci.book_param;// java
        for (j = 0; j < acc; j++) {
            final int n = tmp[j];
            if (n >= ci.books) {
                return null;// goto errout;
            }
            if (cb[n].maptype == 0) {
                return null;// goto errout;
            }
        }

		/* verify the phrasebook is not specifying an impossible or
		inconsistent partitioning scheme. */
		/* modify the phrasebook ranging check from r16327; an early beta
		encoder had a bug where it used an oversized phrasebook by
		accident.  These files should continue to be playable, but don't
		allow an exploit */
        {
            final StaticCodebook codebok = cb[info.groupbook];// java
            final int entries = codebok.entries;
            int dim = codebok.dim;
            int partvals = 1;
            if (dim < 1) {
                return null;// goto errout;
            }
            while (dim > 0) {
                partvals *= partitions;
                if (partvals > entries) {
                    return null;// goto errout;
                }
                dim--;
            }
            info.partvals = partvals;
        }

        return (info);
//errout:
        //res0_free_info( info );
        //return null;
    }

    @Override
    // vorbis_look_residue *res0_look(vorbis_dsp_state *vd, vorbis_info_residue *vr)
    final LookResidue look(final DspState vd, final InfoResidue vr) {
        final InfoResidue0 info = (InfoResidue0) vr;
        final LookResidue0 look = new LookResidue0();
        final CodecSetupInfo ci = vd.vi.codec_setup;

        int acc = 0;
        int maxstage = 0;
        look.info = info;

        final int parts = info.partitions;
        look.parts = parts;
        final Codebook[] fullbooks = ci.fullbooks;// java
        look.fullbooks = fullbooks;
        look.phrasebook = fullbooks[info.groupbook];
        final int dim = look.phrasebook.dim;

        final Codebook[][] partbooks = new Codebook[parts][];
        look.partbooks = partbooks;
        final int[] secondstages = info.secondstages;// java
        final int[] booklist = info.booklist;// java
        for (int j = 0; j < parts; j++) {
            final int s = secondstages[j];
            final int stages = Codec.ilog(s);
            if (stages != 0) {
                if (stages > maxstage) {
                    maxstage = stages;
                }
                partbooks[j] = new Codebook[stages];
                for (int k = 0; k < stages; k++) {
                    if ((s & (1 << k)) != 0) {
                        partbooks[j][k] = fullbooks[booklist[acc++]];
/* if( TRAIN_RES ) {
						look.training_data[k][j] = new int[look.partbooks[j][k].entries];
} */
                    }
                }
            }
        }

        int partvals = 1;
        for (int j = 0; j < dim; j++) {
            partvals *= parts;
        }
        look.partvals = partvals;

        look.stages = maxstage;
        final int[][] decodemap = new int[partvals][];
        look.decodemap = decodemap;
        for (int j = 0; j < partvals; j++) {
            int val = j;
            int mult = partvals / parts;
            final int[] d = new int[dim];
            decodemap[j] = d;
            for (int k = 0; k < dim; k++) {
                final int deco = val / mult;
                val -= deco * mult;
                mult /= parts;
                d[k] = deco;
            }
        }
/* if( TRAIN_RES || TRAIN_RESAUX ) {
		{
			// static int train_seq = 0;
			look.train_seq = train_seq++;
		}
} */
        return (look);
    }

    @Override
    int[][] fclass(final Block vb, final LookResidue vl,
                   final int[][] in, final boolean[] nonzero, final int ch) {
        return null;
    }

    @Override
    int forward(final Buffer opb, final Block vb,
                final LookResidue vl, final int[][] in, final boolean[] nonzero, final int ch,
                final int[][] partword, final int submap) {
        return -1;
    }

    @Override
        // int res0_inverse(vorbis_block *vb, vorbis_look_residue *vl, float **in, int *nonzero, int ch)
    int inverse(final Block vb, final LookResidue vl,
                final float[][] in, final boolean[] nonzero, final int ch) {
        int used = 0;
        for (int i = 0; i < ch; i++) {
            if (nonzero[i]) {
                in[used++] = in[i];
            }
        }
        if (used != 0) {
            //return ( _01inverse( vb, vl, in, used, vorbis_book_decodevs_add ) );
            return (_01inverse(vb, vl, in, used, this));
        } else {
            return (0);
        }
    }
}
