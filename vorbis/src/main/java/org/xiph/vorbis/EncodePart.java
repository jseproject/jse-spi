package org.xiph.vorbis;

/**
 * residue encodepart interface<p>
 * c-variant:<br>
 * <code>int encode(oggpack_buffer *opb,int *vec, int n, codebook *book,long *acc)</code>
 */
interface EncodePart {
    //#ifdef TRAIN_RES
//	static int _encodepart(oggpack_buffer *opb,int *vec, int n, codebook *book,long *acc)
//#else
//	static int _encodepart(oggpack_buffer *opb,int *vec, int n, codebook *book)
//#endif
    int encodepart(Buffer opb, int[] vec, int offset, int n, Codebook book);//, int[] acc);
}
