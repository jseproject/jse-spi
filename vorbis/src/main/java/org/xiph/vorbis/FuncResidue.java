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
 * Residue backend generic
 */
abstract class FuncResidue {
    final boolean is_pack_supported;
    final boolean is_class_supported;
    final boolean is_forward_supported;

    //
    FuncResidue(final boolean isPack, final boolean isClass, final boolean isForward) {
        is_pack_supported = isPack;
        is_class_supported = isClass;
        is_forward_supported = isForward;
    }

    //
    abstract void pack(InfoResidue vr, Buffer opb);

    abstract InfoResidue unpack(Info vi, Buffer opb);

    abstract LookResidue look(DspState vd, InfoResidue vr);

    //abstract void free_info(vorbis_info_residue *);// use vorbis_info_residue = null
    abstract void free_look(LookResidue i);// used only to debug output

    abstract int[][] fclass(Block vb, LookResidue vl, int[][] in, boolean[] nonzero, int ch);

    abstract int forward(Buffer opb, Block vb, LookResidue vl,
                         int[][] in, boolean[] nonzero, int ch, int[][] partword, int submap);

    abstract int inverse(Block vb, LookResidue vl, float[][] in, boolean[] nonzero, int ch);

    // general
    class LookResidue0 extends LookResidue {
        InfoResidue0 info;

        int parts = 0;
        int stages = 0;
        Codebook[] fullbooks = null;
        Codebook phrasebook = null;
        Codebook[][] partbooks = null;

        int partvals = 0;
        int[][] decodemap = null;

        int postbits = 0;
        int phrasebits = 0;
        int frames = 0;
/*
#if defined(TRAIN_RES) || defined(TRAIN_RESAUX)
		int        train_seq;
		int      *training_data[8][64];
		float      training_max[8][64];
		float      training_min[8][64];
		float     tmin;
		float     tmax;
		int       submap;
#endif
*/
    }

    /**
     * a truncated packet here just means 'stop working'; it's not an error
     * <pre>
     * static int _01inverse(Block *vb,vorbis_look_residue *vl,
     * 	float **in,int ch,
     * 	long (*decodepart)(codebook *, float *,
     * 	oggpack_buffer *,int))
     * </pre>
     */
    static int _01inverse(final Block vb, final Object vl,
                          final float[][] in, final int ch,
                          final DecodePart decodepart) {

        final LookResidue0 look = (LookResidue0) vl;
        final InfoResidue0 info = look.info;

        /* move all this setup out later */
        final int samples_per_partition = info.grouping;
        final int partitions_per_word = look.phrasebook.dim;
        final int max = vb.pcmend >> 1;
        final int end = (info.end < max ? info.end : max);
        final int begin = info.begin;// java
        int n = end - begin;

        if (n > 0) {
            int partvals = n / samples_per_partition;
            final int partwords = (partvals + partitions_per_word - 1) / partitions_per_word;
            partvals *= samples_per_partition;// java
            partvals += begin;
            final int[][][] partword = new int[ch][partwords][];

            final int[][] decodemap = look.decodemap;// java
            final Codebook[][] partbooks = look.partbooks;// java
            final Codebook phrasebook = look.phrasebook;// java
            final int[] secondstages = info.secondstages;// java
            for (int s = 0, stages = look.stages; s < stages; s++) {
                final int s1 = (1 << s);// java
				/* each loop decodes on partition codeword containing
					partitions_per_word partitions */
                for (int i = begin, l = 0; i < partvals; l++) {
                    if (s == 0) {
                        /* fetch the partition word for each channel */
                        for (int j = 0; j < ch; j++) {
                            final int temp = phrasebook.decode(vb.opb);

                            if (temp == -1 || temp >= info.partvals) {
                                return 0;// goto eopbreak;
                            }
                            partword[j][l] = decodemap[temp];
                            if (partword[j][l] == null) {
                                return 0;// goto errout;
                            }
                        }
                    }

                    /* now we decode residual values for the partitions */
                    for (int k = 0; k < partitions_per_word && i < partvals; k++, i += samples_per_partition) {
                        for (int j = 0; j < ch; j++) {
                            n = partword[j][l][k];
                            if ((secondstages[n] & s1) != 0) {
                                final Codebook stagebook = partbooks[n][s];
                                if (stagebook != null) {
                                    if (decodepart.decodepart(stagebook, in[j], i, vb.opb,
                                            samples_per_partition) == -1) {
                                        return 0;// goto eopbreak;
                                    }
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
