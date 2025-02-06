package org.xiph.vorbis;

/**
 * residue decodepart interface<p>
 * c-variant:<br>
 * <code>int (*decodepart)(codebook *, float *, Buffer *, int )</code>
 */
interface DecodePart {
    int decodepart(Codebook book, float[] a, int offset, Buffer b, int n);
}
