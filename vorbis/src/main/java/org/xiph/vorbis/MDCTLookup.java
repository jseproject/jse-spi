package org.xiph.vorbis;

/**
 * modified discrete cosine transform
 */
class MDCTLookup {
    /*#define MDCT_INTEGERIZED  <- be warned there could be some hurt left here*/
	/*#ifdef MDCT_INTEGERIZED

	#define DATA_TYPE int
	#define REG_TYPE  register int
	#define TRIGBITS 14
	#define cPI3_8 6270
	#define cPI2_8 11585
	#define cPI1_8 15137

	#define FLOAT_CONV(x) ((int)((x)*(1<<TRIGBITS)+.5))
	#define MULT_NORM(x) ((x)>>TRIGBITS)
	#define HALVE(x) ((x)>>1)

	#else

	#define DATA_TYPE float
	#define REG_TYPE  float
	#define cPI3_8 .38268343236508977175F
	#define cPI2_8 .70710678118654752441F
	#define cPI1_8 .92387953251128675613F

	#define FLOAT_CONV(x) (x)
	#define MULT_NORM(x) (x)
	#define HALVE(x) ((x)*.5f)

	#endif
	*/
    // using #undef MDCT_INTEGERIZED
    // FIXME double or float?
    private static final float cPI3_8 = .38268343236508977175F;
    private static final float cPI2_8 = .70710678118654752441F;
    private static final float cPI1_8 = .92387953251128675613F;
    //
    private int n = 0;
    private int log2n = 0;

    private float[] trig = null;
    private int[] bitrev = null;

    private float scale = 0.0f;

    // mdct.c
/*
function: normalized modified discrete cosine transform
power of two length transform only [64 <= n ]

Original algorithm adapted long ago from _The use of multirate filter
banks for coding of high quality digital audio_, by T. Sporer,
K. Brandenburg and B. Edler, collection of the European Signal
Processing Conference (EUSIPCO), Amsterdam, June 1992, Vol.1, pp
211-214

The below code implements an algorithm that no longer looks much like
that presented in the paper, but the basic structure remains if you
dig deep enough to see it.

This module DOES NOT INCLUDE code to generate/apply the window
function.  Everybody has their own weird favorite including me... I
happen to like the properties of y=sin(.5PI*sin^2(x)), but others may
vehemently disagree.

********************************************************************/

/* this can also be run as an integer transform by uncommenting a
define in mdct.h; the integerization is a first pass and although
it's likely stable for Vorbis, the dynamic range is constrained and
roundoff isn't done (so it's noisy).  Consider it functional, but
only a starting point.  There's no point on a machine with an FPU */

    /**
     * build lookups for trig functions; also pre-figure scaling and
     * some window function algebra.
     */
    final void init(final int order) {
        final int[] bit_rev = new int[order >> 2];
        final float[] T = new float[order + (order >> 2)];

        final int n2 = order >> 1;
        final int log_2n = this.log2n = (int) Math.rint(Math.log((double) order) / Math.log(2.));
        this.n = order;
        this.trig = T;
        this.bitrev = bit_rev;

        /* trig lookups... */
        final double pi_n = Math.PI / order;
        final double pi_n2 = Math.PI / (order << 1);
        int tn = order >>> 2;// java
        for (int i = 0; i < tn; i++) {
            final double pi_n_i4 = pi_n * (i << 2);
            int i2 = i << 1;
            T[i2] = (float) Math.cos(pi_n_i4);
            T[++i2] = (float) -Math.sin(pi_n_i4);
            final double pi_n2_i2_1 = pi_n2 * i2;
            i2 += n2;
            T[i2] = (float) Math.sin(pi_n2_i2_1);
            T[--i2] = (float) Math.cos(pi_n2_i2_1);
        }
        tn >>>= 1;
        for (int i = 0; i < tn; i++) {
            final double pi_n_i4_2 = pi_n * ((i << 2) + 2);
            int ni = order + (i << 1);
            T[ni] = (float) (Math.cos(pi_n_i4_2) * .5);
            T[++ni] = (float) (-Math.sin(pi_n_i4_2) * .5);
        }

        /* bitreverse lookup... */

        {
            final int mask = (1 << (log_2n - 1)) - 1;
            final int msb = 1 << (log_2n - 2);
            for (int i = 0; i < tn; i++) {
                int acc = 0;
                for (int j = 0; (msb >> j) != 0; j++) {
                    if (((msb >> j) & i) != 0) {
                        acc |= 1 << j;
                    }
                }
                bit_rev[i << 1] = ((~acc) & mask) - 1;
                bit_rev[(i << 1) + 1] = acc;

            }
        }
        this.scale = 4.f / order;
    }

    /**
     * 8 point butterfly (in place, 4 register)
     */
    private static void butterfly_8(final float[] x, final int offset) {
        float x1 = x[offset + 6];// java
        float x2 = x[offset + 2];// java
        float r0 = x1 + x2;
        float r1 = x1 - x2;
        x1 = x[offset + 4];// java
        x2 = x[offset];// java
        float r2 = x1 + x2;
        final float r3 = x1 - x2;

        x[offset + 6] = r0 + r2;
        x[offset + 4] = r0 - r2;

        x1 = x[offset + 5];// java
        x2 = x[offset + 1];// java
        r0 = x1 - x2;
        final float x7 = x[offset + 7];// java
        final float x3 = x[offset + 3];// java
        r2 = x7 - x3;
        x[offset] = r1 + r0;
        x[offset + 2] = r1 - r0;

        r0 = x1 + x2;
        r1 = x7 + x3;
        x[offset + 1] = r2 - r3;
        x[offset + 3] = r2 + r3;
        x[offset + 5] = r1 - r0;
        x[offset + 7] = r1 + r0;
    }

    /**
     * 16 point butterfly (in place, 4 register)
     */
    private static void butterfly_16(final float[] x, final int offset) {
        int off0 = offset;
        float x2 = x[off0];// java
        float x1 = x[++off0];// java
        int off8 = offset + 8;
        float x4 = x[off8];// java
        float x3 = x[++off8];// java
        float r0 = x1 - x3;
        float r1 = x2 - x4;

        x4 += x2;
        x[--off8] = x4;
        x3 += x1;
        x[++off8] = x3;
        x[--off0] = ((r0 + r1) * cPI2_8);
        x[++off0] = ((r0 - r1) * cPI2_8);

        x2 = x[++off0];// java
        x1 = x[++off0];// java
        x4 = x[++off8];// java
        x3 = x[++off8];// java
        r0 = x1 - x3;
        r1 = x4 - x2;
        x4 += x2;
        x3 += x1;
        x[--off8] = x4;
        x[++off8] = x3;
        x[--off0] = r0;
        x[++off0] = r1;

        x1 = x[++off0];// java
        x2 = x[++off0];// java
        x3 = x[++off8];// java
        x4 = x[++off8];// java
        r0 = x3 - x1;
        r1 = x4 - x2;
        x3 += x1;
        x4 += x2;
        x[--off8] = x3;
        x[++off8] = x4;
        x[--off0] = ((r0 - r1) * cPI2_8);
        x[++off0] = ((r0 + r1) * cPI2_8);

        x1 = x[++off0];// java
        x2 = x[++off0];// java
        x3 = x[++off8];// java
        x4 = x[++off8];// java
        r0 = x3 - x1;
        r1 = x4 - x2;
        x3 += x1;
        x4 += x2;
        x[--off8] = x3;
        x[++off8] = x4;
        x[--off0] = r0;
        x[++off0] = r1;

        butterfly_8(x, offset);
        butterfly_8(x, ++off0);
    }

    /**
     * 32 point butterfly (in place, 4 register)
     */
    private static void butterfly_32(final float[] x, int offset) {
        int off16 = offset + 31;
        offset += 15;
        float x2 = x[offset];// java
        float x1 = x[--offset];// java
        float x4 = x[off16];// java
        float x3 = x[--off16];// java
        float r0 = x3 - x1;
        float r1 = x4 - x2;

        x3 += x1;
        x4 += x2;
        x[++off16] = x4;
        x[--off16] = x3;
        x[++offset] = r1;
        x[--offset] = r0;

        x2 = x[--offset];// java
        x1 = x[--offset];// java
        x4 = x[--off16];// java
        x3 = x[--off16];// java
        r0 = x3 - x1;
        r1 = x4 - x2;
        x3 += x1;
        x4 += x2;
        x[++off16] = x4;
        x[--off16] = x3;
        x[++offset] = (r0 * cPI3_8 + r1 * cPI1_8);
        x[--offset] = (r0 * cPI1_8 - r1 * cPI3_8);

        x2 = x[--offset];// java
        x1 = x[--offset];// java
        x4 = x[--off16];// java
        x3 = x[--off16];// java
        r0 = x3 - x1;
        r1 = x4 - x2;
        x3 += x1;
        x4 += x2;
        x[++off16] = x4;
        x[--off16] = x3;
        x[++offset] = ((r0 + r1) * cPI2_8);
        x[--offset] = ((r0 - r1) * cPI2_8);

        x2 = x[--offset];// java
        x1 = x[--offset];// java
        x4 = x[--off16];// java
        x3 = x[--off16];// java
        r0 = x3 - x1;
        r1 = x4 - x2;
        x3 += x1;
        x4 += x2;
        x[++off16] = x4;
        x[--off16] = x3;
        x[++offset] = (r1 * cPI3_8 + r0 * cPI1_8);
        x[--offset] = (r0 * cPI3_8 - r1 * cPI1_8);

        x2 = x[--offset];// java
        x1 = x[--offset];// java
        x4 = x[--off16];// java
        x3 = x[--off16];// java
        r0 = x3 - x1;
        r1 = x2 - x4;
        x3 += x1;
        x4 += x2;
        x[++off16] = x4;
        x[--off16] = x3;
        x[++offset] = r0;
        x[--offset] = r1;

        x2 = x[--offset];// java
        x1 = x[--offset];// java
        x4 = x[--off16];// java
        x3 = x[--off16];// java
        r0 = x1 - x3;
        r1 = x2 - x4;
        x3 += x1;
        x4 += x2;
        x[++off16] = x4;
        x[--off16] = x3;
        x[++offset] = (r1 * cPI3_8 - r0 * cPI1_8);
        x[--offset] = (r1 * cPI1_8 + r0 * cPI3_8);

        x2 = x[--offset];// java
        x1 = x[--offset];// java
        x4 = x[--off16];// java
        x3 = x[--off16];// java
        r0 = x1 - x3;
        r1 = x2 - x4;
        x3 += x1;
        x4 += x2;
        x[++off16] = x4;
        x[--off16] = x3;
        x[++offset] = ((r1 - r0) * cPI2_8);
        x[--offset] = ((r1 + r0) * cPI2_8);

        x2 = x[--offset];// java
        x1 = x[--offset];// java
        x4 = x[--off16];// java
        x3 = x[--off16];// java
        r0 = x1 - x3;
        r1 = x2 - x4;
        x3 += x1;
        x4 += x2;
        x[++off16] = x4;
        x[--off16] = x3;
        x[++offset] = (r1 * cPI1_8 - r0 * cPI3_8);
        x[--offset] = (r1 * cPI3_8 + r0 * cPI1_8);

        butterfly_16(x, offset);
        butterfly_16(x, off16);

    }

    /**
     * N point first stage butterfly (in place, 2 register)
     */
    private static void butterfly_first(final float[] T, final float[] x, int offset, final int points) {
        int x1 = offset + points;// - 8;
        int x2 = offset + (points >> 1);// - 8;
        offset += 8;// java

        int t = 0;

        do {

            float t2 = x[--x2];// java
            float t1 = x[--x2];// java
            float t4 = x[--x1];// java
            float t3 = x[--x1];// java
            float r0 = t3 - t1;
            float r1 = t4 - t2;
            t3 += t1;
            t4 += t2;
            x[++x1] = t4;
            x[--x1] = t3;
            t1 = T[t++];// java
            t2 = T[t];// java
            x[++x2] = (r1 * t1 - r0 * t2);
            x[--x2] = (r1 * t2 + r0 * t1);

            t2 = x[--x2];// java
            t1 = x[--x2];// java
            t4 = x[--x1];// java
            t3 = x[--x1];// java
            r0 = t3 - t1;
            r1 = t4 - t2;
            t3 += t1;
            t4 += t2;
            x[++x1] = t4;
            x[--x1] = t3;
            t += 3;
            t1 = T[t++];// java
            t2 = T[t];// java
            x[++x2] = (r1 * t1 - r0 * t2);
            x[--x2] = (r1 * t2 + r0 * t1);

            t2 = x[--x2];// java
            t1 = x[--x2];// java
            t4 = x[--x1];// java
            t3 = x[--x1];// java
            r0 = t3 - t1;
            r1 = t4 - t2;
            t3 += t1;
            t4 += t2;
            x[++x1] = t4;
            x[--x1] = t3;
            t += 3;
            t1 = T[t++];// java
            t2 = T[t];// java
            x[++x2] = (r1 * t1 - r0 * t2);
            x[--x2] = (r1 * t2 + r0 * t1);

            t2 = x[--x2];// java
            t1 = x[--x2];// java
            t4 = x[--x1];// java
            t3 = x[--x1];// java
            r0 = t3 - t1;
            r1 = t4 - t2;
            t3 += t1;
            t4 += t2;
            x[++x1] = t4;
            x[--x1] = t3;
            t += 3;
            t1 = T[t++];// java
            t2 = T[t];// java
            x[++x2] = (r1 * t1 - r0 * t2);
            x[--x2] = (r1 * t2 + r0 * t1);

            // x1 -= 8;
            // x2 -= 8;
            t += 3;// t + 16

        } while (x2 >= offset);
    }

    /**
     * N/stage point generic N stage butterfly (in place, 2 register)
     */
    private static void butterfly_generic(final float[] T, final float[] x, int offset,
                                          final int points, int trigint) {
        int x1 = offset + points;//        - 8;
        int x2 = offset + (points >> 1);// - 8;
        offset += 8;// java

        trigint--;
        int t = 0;

        do {

            float t2 = x[--x2];// java
            float t1 = x[--x2];// java
            float t4 = x[--x1];// java
            float t3 = x[--x1];// java
            float r0 = t3 - t1;
            float r1 = t4 - t2;
            t3 += t1;
            t4 += t2;
            x[++x1] = t4;
            x[--x1] = t3;
            t1 = T[t++];// java
            t2 = T[t];// java
            x[++x2] = (r1 * t1 - r0 * t2);
            x[--x2] = (r1 * t2 + r0 * t1);

            t += trigint;

            t2 = x[--x2];// java
            t1 = x[--x2];// java
            t4 = x[--x1];// java
            t3 = x[--x1];// java
            r0 = t3 - t1;
            r1 = t4 - t2;
            t3 += t1;
            t4 += t2;
            x[++x1] = t4;
            x[--x1] = t3;
            t1 = T[t++];// java
            t2 = T[t];// java
            x[++x2] = (r1 * t1 - r0 * t2);
            x[--x2] = (r1 * t2 + r0 * t1);

            t += trigint;

            t2 = x[--x2];// java
            t1 = x[--x2];// java
            t4 = x[--x1];// java
            t3 = x[--x1];// java
            r0 = t3 - t1;
            r1 = t4 - t2;
            t3 += t1;
            t4 += t2;
            x[++x1] = t4;
            x[--x1] = t3;
            t1 = T[t++];// java
            t2 = T[t];// java
            x[++x2] = (r1 * t1 - r0 * t2);
            x[--x2] = (r1 * t2 + r0 * t1);

            t += trigint;

            t2 = x[--x2];// java
            t1 = x[--x2];// java
            t4 = x[--x1];// java
            t3 = x[--x1];// java
            r0 = x[x1] - t1;
            r1 = x[++x1] - t2;
            t3 += t1;
            t4 += t2;
            x[x1] = t4;
            x[--x1] = t3;
            t1 = T[t++];// java
            t2 = T[t];// java
            x[++x2] = (r1 * t1 - r0 * t2);
            x[--x2] = (r1 * t2 + r0 * t1);

            t += trigint;
            // x1 -= 8;
            // x2 -= 8;

        } while (x2 >= offset);
    }

    private final void butterflies(final float[] x, final int offset, int points) {
        final float[] T = this.trig;
        int stages = this.log2n - 5;

        if (--stages > 0) {
            butterfly_first(T, x, offset, points);
        }

        for (int i = 1; --stages > 0; i++) {
            final int i4 = 4 << i;// java
            final int pi = points >> i;// java
            for (int j = offset, je = offset + pi * (1 << i); j < je; j += pi) {
                butterfly_generic(T, x, j, pi, i4);
            }
        }

        points += offset;
        for (int j = offset; j < points; j += 32) {
            butterfly_32(x, j);
        }

    }

    final void clear() {
        //if( l != null ) {
        n = 0;
        log2n = 0;
        trig = null;
        bitrev = null;
        scale = 0.0f;
        //}
    }

    private final void bitreverse(final float[] x) {
        final int order = this.n;
        final int[] bit_rev = this.bitrev;
        int bit = 0;
        int w0 = 0;
        final int x_offset = (order >> 1);
        int w1 = x_offset;
        final float[] T = this.trig;
        int t = order;

        do {
            int x0 = x_offset + bit_rev[bit++];
            int x1 = x_offset + bit_rev[bit++];
            float t0 = x[x0];// java
            float t1 = x[++x0];// java
            float t2 = x[x1];// java
            float t3 = x[++x1];// java

            float r0 = t1 - t3;
            float r1 = t0 + t2;
            float tt0 = T[t++];// java
            float tt1 = T[t++];// java
            float r2 = (r1 * tt0 + r0 * tt1);
            float r3 = (r1 * tt1 - r0 * tt0);

            // w1    -= 4;

            r0 = 0.5f * (t1 + t3);
            r1 = 0.5f * (t0 - t2);

            x[w0++] = r0 + r2;
            x[w0++] = r1 + r3;
            x[--w1] = r3 - r1;
            x[--w1] = r0 - r2;

            x0 = x_offset + bit_rev[bit++];
            x1 = x_offset + bit_rev[bit++];
            t0 = x[x0];// java
            t1 = x[++x0];// java
            t2 = x[x1];// java
            t3 = x[++x1];// java

            r0 = t1 - t3;
            r1 = t0 + t2;
            tt0 = T[t++];// java
            tt1 = T[t++];// java
            r2 = (r1 * tt0 + r0 * tt1);
            r3 = (r1 * tt1 - r0 * tt0);

            r0 = 0.5f * (t1 + t3);
            r1 = 0.5f * (t0 - t2);

            x[w0++] = r0 + r2;
            x[w0++] = r1 + r3;
            x[--w1] = r3 - r1;
            x[--w1] = r0 - r2;

            // t     += 4;
            // bit   += 4;
            // w0    += 4;

        } while (w0 < w1);
    }

    final void backward(final float[] in, final float[] out) {
        final int order = this.n;
        final int n2 = order >> 1;
        final int n4 = order >> 2;

        /* rotate */

        int iX = n2 - 7;
        int oX = n2 + n4;
        int t = n4;
        final float[] T = this.trig;

        do {
            // oX         -= 4;
            float t1 = T[t++];// java
            float t2 = T[t++];// java
            float i1 = in[iX + 4];// java
            float i2 = in[iX + 6];// java
            out[--oX] = (i1 * t2 - i2 * t1);
            out[--oX] = (-i2 * t2 - i1 * t1);
            t1 = T[t++];// java
            t2 = T[t++];// java
            i1 = in[iX + 0];// java
            i2 = in[iX + 2];// java
            out[--oX] = (i1 * t2 - i2 * t1);
            out[--oX] = (-i2 * t2 - i1 * t1);
            iX -= 8;
            // t          += 4;
        } while (iX >= 0);

        iX = n2 - 8;
        oX = n2 + n4;
        t = n4;

        do {
            // t          -= 4;
            float t1 = T[--t];// java
            float t2 = T[--t];// java
            float i1 = in[iX + 4];// java
            float i2 = in[iX + 6];// java
            out[oX++] = (i1 * t1 + i2 * t2);
            out[oX++] = (i1 * t2 - i2 * t1);
            t1 = T[--t];// java
            t2 = T[--t];// java
            i1 = in[iX];// java
            i2 = in[iX + 2];// java
            out[oX++] = (i1 * t1 + i2 * t2);
            out[oX++] = (i1 * t2 - i2 * t1);
            iX -= 8;
            // oX         += 4;
        } while (iX >= 0);

        butterflies(out, n2, n2);
        bitreverse(out);

        /* roatate + window */

        {
            int oX1 = n2 + n4;
            int oX2 = oX1;
            iX = 0;
            t = n2;

            do {
                // oX1 -= 4;

                float t1 = T[t++];// java
                float t2 = T[t++];// java
                float o1 = out[iX++];// java
                float o2 = out[iX++];// java
                out[--oX1] = (o1 * t2 - o2 * t1);
                out[oX2++] = -(o1 * t1 + o2 * t2);

                t1 = T[t++];// java
                t2 = T[t++];// java
                o1 = out[iX++];// java
                o2 = out[iX++];// java
                out[--oX1] = (o1 * t2 - o2 * t1);
                out[oX2++] = -(o1 * t1 + o2 * t2);

                t1 = T[t++];// java
                t2 = T[t++];// java
                o1 = out[iX++];// java
                o2 = out[iX++];// java
                out[--oX1] = (o1 * t2 - o2 * t1);
                out[oX2++] = -(o1 * t1 + o2 * t2);

                t1 = T[t++];// java
                t2 = T[t++];// java
                o1 = out[iX++];// java
                o2 = out[iX++];// java
                out[--oX1] = (o1 * t2 - o2 * t1);
                out[oX2++] = -(o1 * t1 + o2 * t2);

                // oX2 += 4;
                // iX  +=   8;
                // t   +=   8;
            } while (iX < oX1);

            iX = n2 + n4;
            oX1 = n4;
            oX2 = oX1;

            do {
                // oX1 -= 4;
                // iX  -= 4;

                out[oX2++] = -(out[--oX1] = out[--iX]);
                out[oX2++] = -(out[--oX1] = out[--iX]);
                out[oX2++] = -(out[--oX1] = out[--iX]);
                out[oX2++] = -(out[--oX1] = out[--iX]);

                // oX2 += 4;
            } while (oX2 < iX);

            iX = n2 + n4;
            oX1 = iX;
            oX2 = n2;
            do {
                // oX1 -= 4;
                out[--oX1] = out[iX++];
                out[--oX1] = out[iX++];
                out[--oX1] = out[iX++];
                out[--oX1] = out[iX++];
                // iX += 4;
            } while (oX1 > oX2);
        }
    }

    final void forward(final float[] in, final float[] out) {
        final int order = this.n;
        final int n2 = order >> 1;
        final int n4 = order >> 2;

        final float[] w = new float[order];/* forward needs working space */
        //int w2 = n2;

        /* rotate */

        /* window + rotate + step 1 */

        int x0 = n2 + n4;
        int x1 = x0 + 1;
        int t = n2 - 1;// java: - 1
        final float[] T = this.trig;

        int i = n2;
        final int n8 = (order >> 3) + n2;
        while (i < n8) {
            x0 -= 4;
            // t -= 2;
            final float r0 = in[x0 + 2] + in[x1];
            final float r1 = in[x0] + in[x1 + 2];
            final float t1 = T[t--];// java
            final float t0 = T[t--];// java
            w[i++] = (r1 * t1 + r0 * t0);
            w[i++] = (r1 * t0 - r0 * t1);
            x1 += 4;
        }

        x1 = 1;

        for (final int ie = order + n2 - n8; i < ie; ) {
            // t -= 2;
            x0 -= 4;
            final float r0 = in[x0 + 2] - in[x1];
            final float r1 = in[x0] - in[x1 + 2];
            final float t1 = T[t--];// java
            final float t0 = T[t--];// java
            w[i++] = (r1 * t1 + r0 * t0);
            w[i++] = (r1 * t0 - r0 * t1);
            x1 += 4;
        }

        x0 = order;

        while (i < order) {
            // t -= 2;
            x0 -= 4;
            final float r0 = -in[x0 + 2] - in[x1];
            final float r1 = -in[x0] - in[x1 + 2];
            final float t1 = T[t--];// java
            final float t0 = T[t--];// java
            w[i++] = (r1 * t1 + r0 * t0);
            w[i++] = (r1 * t0 - r0 * t1);
            x1 += 4;
        }

        butterflies(w, n2, n2);
        bitreverse(w);

        /* roatate + window */

        t = n2;
        x0 = n2;

        x1 = 0;

        final float k = this.scale;// java
        for (i = 0; i < n4; i++) {
            x0--;
            final float w0 = w[x1++];// java
            final float w1 = w[x1++];// java
            final float t0 = T[t++];// java
            final float t1 = T[t++];// java
            out[i] = ((w0 * t0 + w1 * t1) * k);
            out[x0] = ((w0 * t1 - w1 * t0) * k);
            // x1 += 2;
            // t += 2;
        }
    }
}
