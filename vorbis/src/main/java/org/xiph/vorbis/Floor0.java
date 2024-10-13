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

import java.util.Arrays;

/**
 * floor backend 0 implementation<br>
 * export hooks<p>
 * <pre>
 * const vorbis_func_floor floor0_exportbundle = {
 * NULL, &floor0_unpack,
 * &floor0_look, &floor0_free_info,
 * &floor0_free_look,
 * &floor0_inverse1, &floor0_inverse2
 * };
 * </pre>
 */
class Floor0 extends FuncFloor {
    private static class LookFloor0 extends LookFloor {
        private int ln = 0;
        private int m = 0;
        private int[][] linearmap = null;
        private final int[] n = new int[2];

        private InfoFloor0 vi = null;

        //FIXME never used private int bits = 0;
        //FIXME never used private int frames = 0;
    }

    //
    Floor0() {
        super(false);
    }
    /************************************/
    // lsp.c
	/* Note that the lpc-lsp conversion finds the roots of polynomial with
	   an iterative root polisher (CACM algorithm 283).  It *is* possible
	   to confuse this algorithm into not converging; that should only
	   happen with absurdly closely spaced roots (very sharp peaks in the
	   LPC f response) which in turn should be impossible in our use of
	   the code.  If this *does* happen anyway, it's a bug in the floor
	   finder; find the cause of the confusion (probably a single bin
	   spike or accidental near-float-limit resolution problems) and
	   correct it. */


	/* three possible LSP to f curve functions; the exact computation
	   (float), a lookup based float implementation, and an integer
	   implementation.  The float lookup is likely the optimal choice on
	   any machine with an FPU.  The integer implementation is *not* fixed
	   point (due to the need for a large dynamic range and thus a
	   separately tracked exponent) and thus much more complex than the
	   relatively simple float implementations. It's mostly for future
	   work on a fully fixed point implementation for processors like the
	   ARM family. */

    /******************* lookup.c **************************/
	/* define either of these (preferably FLOAT_LOOKUP) to have faster
	   but less precise implementation. */
/*	private static final boolean FLOAT_LOOKUP = false;
	private static final boolean INT_LOOKUP = true;
	private static final int FROMdB_LOOKUP_SZ = 35;
	//private static final int FROMdB2_LOOKUP_SZ = 32;
	private static final int FROMdB_SHIFT = 5;
	private static final int FROMdB2_SHIFT = 3;
	private static final int FROMdB2_MASK = 31;
	//
	private static final float FROMdB_LOOKUP[] = {// [FROMdB_LOOKUP_SZ]
                     1.f,   0.6309573445f,   0.3981071706f,   0.2511886432f,
		   0.1584893192f,            0.1f,  0.06309573445f,  0.03981071706f,
		  0.02511886432f,  0.01584893192f,           0.01f, 0.006309573445f,
		 0.003981071706f, 0.002511886432f, 0.001584893192f,          0.001f,
		0.0006309573445f,0.0003981071706f,0.0002511886432f,0.0001584893192f,
		         0.0001f,6.309573445e-05f,3.981071706e-05f,2.511886432e-05f,
		1.584893192e-05f,          1e-05f,6.309573445e-06f,3.981071706e-06f,
		2.511886432e-06f,1.584893192e-06f,          1e-06f,6.309573445e-07f,
		3.981071706e-07f,2.511886432e-07f,1.584893192e-07f,
	};
	private static final float FROMdB2_LOOKUP[] = {// [FROMdB2_LOOKUP_SZ]
		0.9928302478f,   0.9786445908f,   0.9646616199f,   0.9508784391f,
		0.9372921937f,     0.92390007f,   0.9106992942f,   0.8976871324f,
		0.8848608897f,   0.8722179097f,   0.8597555737f,   0.8474713009f,
		 0.835362547f,   0.8234268041f,   0.8116616003f,   0.8000644989f,
		0.7886330981f,   0.7773650302f,   0.7662579617f,    0.755309592f,
		0.7445176537f,   0.7338799116f,   0.7233941627f,   0.7130582353f,
		0.7028699885f,   0.6928273125f,   0.6829281272f,   0.6731703824f,
		0.6635520573f,   0.6540711597f,   0.6447257262f,   0.6355138211f,
	};*/
/*#ifdef FLOAT_LOOKUP

	private static final int COS_LOOKUP_SZ = 128;
	private static final float COS_LOOKUP[] = {// [COS_LOOKUP_SZ+1]
		+1.0000000000000f,+0.9996988186962f,+0.9987954562052f,+0.9972904566787f,
		+0.9951847266722f,+0.9924795345987f,+0.9891765099648f,+0.9852776423889f,
		+0.9807852804032f,+0.9757021300385f,+0.9700312531945f,+0.9637760657954f,
		+0.9569403357322f,+0.9495281805930f,+0.9415440651830f,+0.9329927988347f,
		+0.9238795325113f,+0.9142097557035f,+0.9039892931234f,+0.8932243011955f,
		+0.8819212643484f,+0.8700869911087f,+0.8577286100003f,+0.8448535652497f,
		+0.8314696123025f,+0.8175848131516f,+0.8032075314806f,+0.7883464276266f,
		+0.7730104533627f,+0.7572088465065f,+0.7409511253550f,+0.7242470829515f,
		+0.7071067811865f,+0.6895405447371f,+0.6715589548470f,+0.6531728429538f,
		+0.6343932841636f,+0.6152315905806f,+0.5956993044924f,+0.5758081914178f,
		+0.5555702330196f,+0.5349976198871f,+0.5141027441932f,+0.4928981922298f,
		+0.4713967368260f,+0.4496113296546f,+0.4275550934303f,+0.4052413140050f,
		+0.3826834323651f,+0.3598950365350f,+0.3368898533922f,+0.3136817403989f,
		+0.2902846772545f,+0.2667127574749f,+0.2429801799033f,+0.2191012401569f,
		+0.1950903220161f,+0.1709618887603f,+0.1467304744554f,+0.1224106751992f,
		+0.0980171403296f,+0.0735645635997f,+0.0490676743274f,+0.0245412285229f,
		+0.0000000000000f,-0.0245412285229f,-0.0490676743274f,-0.0735645635997f,
		-0.0980171403296f,-0.1224106751992f,-0.1467304744554f,-0.1709618887603f,
		-0.1950903220161f,-0.2191012401569f,-0.2429801799033f,-0.2667127574749f,
		-0.2902846772545f,-0.3136817403989f,-0.3368898533922f,-0.3598950365350f,
		-0.3826834323651f,-0.4052413140050f,-0.4275550934303f,-0.4496113296546f,
		-0.4713967368260f,-0.4928981922298f,-0.5141027441932f,-0.5349976198871f,
		-0.5555702330196f,-0.5758081914178f,-0.5956993044924f,-0.6152315905806f,
		-0.6343932841636f,-0.6531728429538f,-0.6715589548470f,-0.6895405447371f,
		-0.7071067811865f,-0.7242470829515f,-0.7409511253550f,-0.7572088465065f,
		-0.7730104533627f,-0.7883464276266f,-0.8032075314806f,-0.8175848131516f,
		-0.8314696123025f,-0.8448535652497f,-0.8577286100003f,-0.8700869911087f,
		-0.8819212643484f,-0.8932243011955f,-0.9039892931234f,-0.9142097557035f,
		-0.9238795325113f,-0.9329927988347f,-0.9415440651830f,-0.9495281805930f,
		-0.9569403357322f,-0.9637760657954f,-0.9700312531945f,-0.9757021300385f,
		-0.9807852804032f,-0.9852776423889f,-0.9891765099648f,-0.9924795345987f,
		-0.9951847266722f,-0.9972904566787f,-0.9987954562052f,-0.9996988186962f,
		-1.0000000000000f,
	};
	private static final int INVSQ_LOOKUP_SZ = 32;
	private static final float INVSQ_LOOKUP[] = {// [INVSQ_LOOKUP_SZ + 1]
		1.414213562373f,1.392621247646f,1.371988681140f,1.352246807566f,
		1.333333333333f,1.315191898443f,1.297771369046f,1.281025230441f,
		1.264911064067f,1.249390095109f,1.234426799697f,1.219988562661f,
		1.206045378311f,1.192569588000f,1.179535649239f,1.166919931983f,
		1.154700538379f,1.142857142857f,1.131370849898f,1.120224067222f,
		1.109400392450f,1.098884511590f,1.088662107904f,1.078719779941f,
		1.069044967650f,1.059625885652f,1.050451462878f,1.041511287847f,
		1.032795558989f,1.024295039463f,1.016001016002f,1.007905261358f,
		1.000000000000f,
	};
	private static final int INVSQ2EXP_LOOKUP_MIN = -32;
	//private static final int INVSQ2EXP_LOOKUP_MAX = 32;
	// [INVSQ2EXP_LOOKUP_MAX - INVSQ2EXP_LOOKUP_MIN + 1]
	private static final float INVSQ2EXP_LOOKUP[] = {
		         65536.f,    46340.95001f,         32768.f,    23170.47501f,
		         16384.f,     11585.2375f,          8192.f,    5792.618751f,
		          4096.f,    2896.309376f,          2048.f,    1448.154688f,
		          1024.f,    724.0773439f,           512.f,     362.038672f,
		           256.f,     181.019336f,           128.f,    90.50966799f,
		            64.f,      45.254834f,            32.f,      22.627417f,
		            16.f,     11.3137085f,             8.f,    5.656854249f,
		             4.f,    2.828427125f,             2.f,    1.414213562f,
		             1.f,   0.7071067812f,            0.5f,   0.3535533906f,
		           0.25f,   0.1767766953f,          0.125f,  0.08838834765f,
		         0.0625f,  0.04419417382f,        0.03125f,  0.02209708691f,
		       0.015625f,  0.01104854346f,      0.0078125f, 0.005524271728f,
		     0.00390625f, 0.002762135864f,    0.001953125f, 0.001381067932f,
		   0.0009765625f, 0.000690533966f,  0.00048828125f, 0.000345266983f,
		 0.000244140625f,0.0001726334915f,0.0001220703125f,8.631674575e-05f,
		6.103515625e-05f,4.315837288e-05f,3.051757812e-05f,2.157918644e-05f,
		1.525878906e-05f,
	};
	*//** interpolated lookup based cos function, domain 0 to PI only *//*
	private static float coslook(float a) {
		double d = (double)(a * (.31830989 * (float)COS_LOOKUP_SZ));
		int i = (int)Math.floor( d );

		return COS_LOOKUP[i] + ((float)d - (float)i) * (COS_LOOKUP[i + 1] - COS_LOOKUP[i]);
	}

	*//** interpolated 1./sqrt(p) where .5 <= p < 1. *//*
	private static float invsqlook(float a) {
		double d = (double)(a * (2.f * (float)INVSQ_LOOKUP_SZ) - (float)INVSQ_LOOKUP_SZ);
		int i = (int)Math.floor( d );
		return INVSQ_LOOKUP[i] + ((float)d - (float)i) * (INVSQ_LOOKUP[i + 1] - INVSQ_LOOKUP[i]);
	}

	*//** interpolated 1./sqrt(p) where .5 <= p < 1. *//*
	private static float invsq2explook(int a) {
		return INVSQ2EXP_LOOKUP[a - INVSQ2EXP_LOOKUP_MIN];
	}

	*//** interpolated lookup based fromdB function, domain -140dB to 0dB only *//*
	private static float fromdBlook(float a) {
		int i = (int)Math.floor( a * ((float)(-(1 << FROMdB2_SHIFT))) );
		return (i < 0) ? 1.f:
			((i >= (FROMdB_LOOKUP_SZ << FROMdB_SHIFT)) ? 0.f:
			FROMdB_LOOKUP[i >> FROMdB_SHIFT] * FROMdB2_LOOKUP[i & FROMdB2_MASK]);
	}

//#endif // FLOAT_LOOKUP
*/
/*#ifdef INT_LOOKUP

	private static final int INVSQ_LOOKUP_I_SHIFT = 10;
	private static final int INVSQ_LOOKUP_I_MASK = 1023;
	private static final int INVSQ_LOOKUP_I[] = {// [64 + 1]
		92682,   91966,   91267,   90583,
		89915,   89261,   88621,   87995,
		87381,   86781,   86192,   85616,
		85051,   84497,   83953,   83420,
		82897,   82384,   81880,   81385,
		80899,   80422,   79953,   79492,
		79039,   78594,   78156,   77726,
		77302,   76885,   76475,   76072,
		75674,   75283,   74898,   74519,
		74146,   73778,   73415,   73058,
		72706,   72359,   72016,   71679,
		71347,   71019,   70695,   70376,
		70061,   69750,   69444,   69141,
		68842,   68548,   68256,   67969,
		67685,   67405,   67128,   66855,
		66585,   66318,   66054,   65794,
		65536,
	};

	private static final int COS_LOOKUP_I_SHIFT = 9;
	private static final int COS_LOOKUP_I_MASK = 511;
	//private static final int COS_LOOKUP_I_SZ = 128;
	private static final int COS_LOOKUP_I[] = {// [COS_LOOKUP_I_SZ + 1]
		 16384,   16379,   16364,   16340,
		 16305,   16261,   16207,   16143,
		 16069,   15986,   15893,   15791,
		 15679,   15557,   15426,   15286,
		 15137,   14978,   14811,   14635,
		 14449,   14256,   14053,   13842,
		 13623,   13395,   13160,   12916,
		 12665,   12406,   12140,   11866,
		 11585,   11297,   11003,   10702,
		 10394,   10080,    9760,    9434,
		  9102,    8765,    8423,    8076,
		  7723,    7366,    7005,    6639,
		  6270,    5897,    5520,    5139,
		  4756,    4370,    3981,    3590,
		  3196,    2801,    2404,    2006,
		  1606,    1205,     804,     402,
		     0,    -401,    -803,   -1204,
		 -1605,   -2005,   -2403,   -2800,
		 -3195,   -3589,   -3980,   -4369,
		 -4755,   -5138,   -5519,   -5896,
		 -6269,   -6638,   -7004,   -7365,
		 -7722,   -8075,   -8422,   -8764,
		 -9101,   -9433,   -9759,  -10079,
		-10393,  -10701,  -11002,  -11296,
		-11584,  -11865,  -12139,  -12405,
		-12664,  -12915,  -13159,  -13394,
		-13622,  -13841,  -14052,  -14255,
		-14448,  -14634,  -14810,  -14977,
		-15136,  -15285,  -15425,  -15556,
		-15678,  -15790,  -15892,  -15985,
		-16068,  -16142,  -16206,  -16260,
		-16304,  -16339,  -16363,  -16378,
		-16383,
	};
	*//** interpolated 1./sqrt(p) where .5 <= a < 1. (.100000... to .111111...) in
     16.16 format

     returns in m.8 format *//*
	private static int invsqlook_i(int a, int e) {
		final int i = (a & 0x7fff) >> (INVSQ_LOOKUP_I_SHIFT - 1);
		final int d = (a & INVSQ_LOOKUP_I_MASK) << (16 - INVSQ_LOOKUP_I_SHIFT);   0.16
		int val = INVSQ_LOOKUP_I[i]-                                        1.16
				(((INVSQ_LOOKUP_I[i] - INVSQ_LOOKUP_I[i + 1]) *             0.16
				d) >> 16);                                           result 1.16

		e += 32;
		if( (e & 1) != 0 ) val = (val * 5792) >> 13;  multiply val by 1/sqrt(2)
		e = (e >> 1) - 8;

		return (val >> e);
	}

	*//** interpolated lookup based fromdB function, domain -140dB to 0dB only
     * a is in n.12 format *//*
	private static float fromdBlook_i(int a) {
		int i = (-a) >> (12 - FROMdB2_SHIFT);
		return (i < 0) ? 1.f:
			((i >= (FROMdB_LOOKUP_SZ << FROMdB_SHIFT)) ? 0.f:
			FROMdB_LOOKUP[i >> FROMdB_SHIFT] * FROMdB2_LOOKUP[i & FROMdB2_MASK]);
	}

	*//** interpolated lookup based cos function, domain 0 to PI only
     * a is in 0.16 format, where 0==0, 2^^16-1==PI, return 0.14 *//*
	private static int coslook_i(int a) {
		int i = a >> COS_LOOKUP_I_SHIFT;
		int d = a & COS_LOOKUP_I_MASK;
		return COS_LOOKUP_I[i] - ((d * (COS_LOOKUP_I[i] - COS_LOOKUP_I[i + 1])) >>
				COS_LOOKUP_I_SHIFT);
	}

	private static final int MLOOP_1[] = {// [64]
		0,10,11,11, 12,12,12,12, 13,13,13,13, 13,13,13,13,
		14,14,14,14, 14,14,14,14, 14,14,14,14, 14,14,14,14,
		15,15,15,15, 15,15,15,15, 15,15,15,15, 15,15,15,15,
		15,15,15,15, 15,15,15,15, 15,15,15,15, 15,15,15,15,
	};

	private static final int MLOOP_2[] = {// [64]
		0,4,5,5, 6,6,6,6, 7,7,7,7, 7,7,7,7,
		8,8,8,8, 8,8,8,8, 8,8,8,8, 8,8,8,8,
		9,9,9,9, 9,9,9,9, 9,9,9,9, 9,9,9,9,
		9,9,9,9, 9,9,9,9, 9,9,9,9, 9,9,9,9,
	};

	private static final int MLOOP_3[] = { 0,1,2,2,3,3,3,3 };// [8]
#endif// INT_LOOKUP
*/    //

    /**
     * side effect: changes *lsp to cosines of lsp
     */
    private static void lsp_to_curve(final float[] curve, final int[] map, final int n, final int ln,
                                     final float[] lsp, final int m, final float amp, final float ampoffset) {
/*if( FLOAT_LOOKUP ) {
		int i;
		final float wdel = (float)Math.PI / (float)ln;
		//fpu_control fpu;

		//fpu_setround( &fpu );
		for( i = 0; i < m; i++ ) lsp[i] = coslook( lsp[i] );

		i = 0;
		while( i < n ) {
			final int k = map[i];
			final int qexp;
			float p = .7071067812f;
			float q = .7071067812f;
			final float w = coslook( wdel * k );
			//float[] ftmp = lsp;
			int ftmp = 0;
			int c = m >> 1;

			while( c-- != 0 ) {
				q *= lsp[ftmp + 0] - w;
				p *= lsp[ftmp + 1] - w;
				ftmp += 2;
			}

			if( (m & 1) != 0 ) {
				// odd order filter; slightly assymetric
				// the last coefficient
				q *= lsp[ftmp + 0] - w;
				q *= q;
				p *= p * (1.f - w * w);
			} else {
				// even order filter; still symmetric
				q *= q * (1.f + w);
				p *= p * (1.f - w);
			}

			// TODO check Java equivalent q = frexp( p + q, &qexp );
			q += p;
			qexp = Math.getExponent( q );
			q = q - ((int) q);
			q = fromdBlook( amp *
					invsqlook( q ) *
					invsq2explook( qexp + m) -
					ampoffset );

			do {
				curve[i++] *= q;
			} while( map[i] == k );
		}
		//fpu_restore( fpu );
} else*/
/*if( INT_LOOKUP ) {
		 0 <= m < 256

		 set up for using all int later
		int i;
		final int ampoffseti = (int) Math.rint( ampoffset * 4096.f );
		final int ampi = (int) Math.rint( amp * 16.f );
		final int[] ilsp = new int[m];
		for( i = 0; i < m; i++ )
			ilsp[i] = coslook_i( (int)(lsp[i] / (float)Math.PI * 65536.f + .5f) );

		i = 0;
		while( i < n ) {
			int j, k = map[i];
			int pi = 46341;  2**-.5 in 0.16
			int qi = 46341;// FIXME pi and qi is uint. may be need long?
			int qexp = 0, shift;
			final int wi = coslook_i( (k << 16) / ln );

			qi *= Math.abs( ilsp[0] - wi );
			pi *= Math.abs( ilsp[1] - wi );

			for( j = 3; j < m; j += 2 ) {
				if( (shift = MLOOP_1[(pi | qi) >>> 25]) == 0 )
					if( (shift = MLOOP_2[(pi | qi) >>> 19]) == 0 )
						shift = MLOOP_3[(pi | qi) >>> 16];
				qi = (qi >>> shift) * Math.abs( ilsp[j - 1] - wi );
				pi = (pi >>> shift) * Math.abs( ilsp[j] - wi);
				qexp += shift;
			}
			if( (shift = MLOOP_1[(pi | qi) >>> 25]) == 0 )
				if( (shift = MLOOP_2[(pi | qi) >>> 19]) == 0 )
					shift = MLOOP_3[(pi | qi) >>> 16];

			pi,qi normalized collectively, both tracked using qexp

			if( (m & 1) != 0 ) {
				 odd order filter; slightly assymetric
				 the last coefficient
				qi = (qi >>> shift) * Math.abs( ilsp[j - 1] - wi );
				pi = (pi >>> shift) << 14;
				qexp += shift;

				if( (shift = MLOOP_1[(pi | qi) >>> 25]) != 0 )
					if( (shift = MLOOP_2[(pi | qi) >>> 19]) != 0 )
						shift = MLOOP_3[(pi | qi) >>> 16];

				pi >>>= shift;
				qi >>>= shift;
				qexp += shift - 14 * ((m + 1) >> 1);

				pi = ((pi * pi) >>> 16);
				qi = ((qi * qi) >>> 16);
				qexp = (qexp << 1) + m;

				pi *= (1 << 14) - ((wi * wi) >> 14);
				qi += pi >>> 14;

			} else {
				 even order filter; still symmetric

				 p*=p(1-w), q*=q(1+w), let normalization drift because it isn't
					 worth tracking step by step

				pi >>>= shift;
				qi >>>= shift;
				qexp += shift - 7 * m;

				pi = ((pi * pi) >>> 16);
				qi = ((qi * qi) >>> 16);
				qexp = (qexp << 1) + m;

				pi *= (1 << 14) - wi;
				qi *= (1 << 14) + wi;
				qi = (qi + pi) >>> 14;

			}


			 we've let the normalization drift because it wasn't important;
			   however, for the lookup, things must be normalized again.  We
			   need at most one right shift or a number of left shifts

			if( (qi & 0xffff0000) != 0 ) {  checks for 1.xxxxxxxxxxxxxxxx
				qi >>>= 1; qexp++;
			} else
				while( qi != 0 && (qi & 0x8000) == 0 ) {  checks for 0.0xxxxxxxxxxxxxxx or less
					qi <<= 1; qexp--;
				}

			amp = fromdBlook_i( ampi *          n.4
					invsqlook_i( qi, qexp ) -   m.8, m+n<=8
					ampoffseti );                              8.12[0]

			curve[i] *= amp;
			while( map[++i] == k ) curve[i] *= amp;
		}
} else*/
        {
		/* old, nonoptimized but simple version for any poor sap who needs to
		   figure out what the hell this code does, or wants the other
		   fraction of a dB precision */
            // added
            final double damp = (double) amp;
            final double dampoffset = (double) ampoffset;
            //
            final double wdel = Math.PI / (double) ln;
            for (int i = 0; i < m; i++) {
                lsp[i] = 2.f * (float) Math.cos((double) lsp[i]);
            }

            int i = 0;
            while (i < n) {
                int j;
                final int k = map[i];
                float p = .5f;
                float q = .5f;
                final float w = 2.f * (float) Math.cos(wdel * (double) k);
                for (j = 1; j < m; j += 2) {
                    q *= w - lsp[j - 1];
                    p *= w - lsp[j];
                }
                if (j == m) {
                    /* odd order filter; slightly assymetric */
                    /* the last coefficient */
                    q *= w - lsp[j - 1];
                    p *= p * (4.f - w * w);
                    q *= q;
                } else {
                    /* even order filter; still symmetric */
                    p *= p * (2.f - w);
                    q *= q * (2.f + w);
                }

                //q = Codec.fromdB( amp / (float)Math.sqrt(p + q) - ampoffset );
                p += q;// java: extracted in place
                q = (float) (Math.exp((damp / Math.sqrt((double) p) - dampoffset) * .11512925));

                curve[i] *= q;
                while (map[++i] == k) {
                    curve[i] *= q;
                }
            }
        }
    }

/*	// XXX never used functions:
	// Laguerre_With_Deflation
	// Newton_Raphson
	// comp
	// vorbis_lpc_to_lsp
	private static void cheby(float[] g, int ord) {
		int i, j;

		g[0] *= .5f;
		for( i = 2; i <= ord; i++ ) {
			for( j = ord; j >= i; j-- ) {
				g[j - 2] -= g[j];
				g[j]     += g[j];
			}
		}
	}

	//private static int comp(final float a, final float b) {
	//	return (a < b ? 1 : 0) - (a > b ? 1 : 0);
	//}

	*//** Newton-Raphson-Maehly actually functioned as a decent root finder,
     but there are root sets for which it gets into limit cycles
     (exacerbated by zero suppression) and fails.  We can't afford to
     fail, even if the failure is 1 in 100,000,000, so we now use
     Laguerre and later polish with Newton-Raphson (which can then
     afford to fail) *//*

	private static final double EPSILON = 10e-7;
	private static int Laguerre_With_Deflation(float[] a, int ord, float[] r) {
		int i, m;
		final double[] defl = new double[ord + 1];
		for( i = 0; i <= ord; i++ ) defl[i] = a[i];
		int defl_offset = 0;

		for( m = ord; m > 0; m-- ) {
			double dnew = 0.f, delta;

			 iterate a root
			while( true ) {
				double p = defl[defl_offset + m], pp = 0.f, ppp = 0.f, denom;

				 eval the polynomial and its first two derivatives
				for( i = m; i > 0; i-- ) {
					ppp = dnew * ppp + pp;
					pp  = dnew * pp  + p;
					p   = dnew * p   + defl[defl_offset + i - 1];
				}

				 Laguerre's method
				denom = (m - 1) * ((m - 1) * pp * pp - m * p * ppp);
				if( denom < 0 )
					return (-1);   complex root!  The LPC generator handed us a bad filter

				if( pp > 0 ) {
					denom = pp + Math.sqrt( denom );
					if( denom < EPSILON ) denom = EPSILON;
				} else {
					denom = pp - Math.sqrt( denom );
					if( denom > -EPSILON ) denom = -EPSILON;
				}

				delta  = m * p / denom;
				dnew   -= delta;

				if( delta < 0.f ) delta *= -1;

				if( Math.abs( delta / dnew ) < 10e-12 ) break;
			}

			r[m - 1] = (float)dnew;

			 forward deflation

			for( i = m; i > 0; i-- )
				defl[defl_offset + i - 1] += dnew * defl[defl_offset + i];
			defl_offset++;

		}
		return (0);
	}

	*//** for spit-and-polish only *//*
	private static int Newton_Raphson(float[] a, int ord, float[] r) {
		int i, k, count = 0;
		double error = 1.f;
		final double[] root = new double[ord];

		for( i = 0; i < ord; i++ ) root[i] = (double) r[i];

		while( error > 1e-20 ) {
			error = 0;

			for( i = 0; i < ord; i++ ) {  Update each point.
				double pp = 0., delta;
				double rooti = root[i];
				double p = a[ord];
				for( k = ord - 1; k >= 0; k-- ) {

					pp = pp * rooti + p;
					p  = p  * rooti + a[k];
				}

				delta = p / pp;
				root[i] -= delta;
				error += delta * delta;
			}

			if( count > 40 ) return (-1);

			count++;
		}

		 Replaced the original bubble sort with a real sort.  With your
		 help, we can eliminate the bubble sort in our lifetime. --Monty

		for( i = 0; i < ord; i++ ) r[i] = (float) root[i];
		return (0);
	}

	private static int comp(const void *a, const void *b) {
		return (*(float *)a < *(float *)b) - (*(float *)a > *(float *)b);
	}

	*/

    /**
     * Convert lpc coefficients to lsp coefficients
     *//*
	private static int lpc_to_lsp(float[] lpc, float[] lsp, int m) {
		final int order2 = (m + 1) >> 1;
		final int g1_order, g2_order;
		float[] g1 = new float[order2 + 1];
		float[] g2 = new float[order2 + 1];
		float[] g1r = new float[order2 + 1];
		float[] g2r = new float[order2 + 1];
		int i;

		 even and odd are slightly different base cases
		g1_order = (m + 1) >> 1;
		g2_order = (m)     >> 1;

		 Compute the lengths of the x polynomials.
		 Compute the first half of K & R F1 & F2 polynomials.
		 Compute half of the symmetric and antisymmetric polynomials.
		 Remove the roots at +1 and -1.

		g1[g1_order] = 1.f;
		for( i = 1; i <= g1_order; i++ ) g1[g1_order - i] = lpc[i - 1] + lpc[m - i];
		g2[g2_order] = 1.f;
		for( i = 1; i <= g2_order; i++ ) g2[g2_order - i] = lpc[i - 1] - lpc[m - i];

		if( g1_order > g2_order ) {
			for( i = 2; i <= g2_order; i++ ) g2[g2_order - i] += g2[g2_order - i + 2];
		} else {
			for( i = 1; i <= g1_order; i++ ) g1[g1_order - i] -= g1[g1_order - i + 1];
			for( i = 1; i <= g2_order; i++ ) g2[g2_order - i] += g2[g2_order - i + 1];
		}

		 Convert into polynomials in cos(alpha)
		cheby( g1, g1_order );
		cheby( g2, g2_order );

		 Find the roots of the 2 even polynomials.
		if( Laguerre_With_Deflation( g1, g1_order, g1r ) != 0 ||
			Laguerre_With_Deflation( g2, g2_order, g2r ) != 0 )
			return (-1);

		Newton_Raphson( g1, g1_order, g1r );  if it fails, it leaves g1r alone
		Newton_Raphson( g2, g2_order, g2r );  if it fails, it leaves g2r alone

		FastQSortAlgorithm.inverseSort( g1r, 0, g1_order );
		FastQSortAlgorithm.inverseSort( g2r, 0, g2_order );

		for( i = 0; i < g1_order; i++ )
			lsp[i << 1] = (float) Math.acos( (double) g1r[i] );

		for( i = 0; i < g2_order; i++ )
			lsp[(i << 1) + 1] = (float) Math.acos( (double) g2r[i] );
		return (0);
	}*/

    // use vorbis_info_floor = null
    // static void floor0_free_info(vorbis_info_floor *i)
	/*private static void floor0_free_info(vorbis_info_floor *i) {
		InfoFloor0 info = (InfoFloor0)i;
		if( info != null ) {
			info.clear();
			info = null;
		}
	}*/
    @Override
    final void pack(final InfoFloor i, final Buffer opg) {
    }

    @Override
    // vorbis_info_floor *floor0_unpack (vorbis_info *vi,oggpack_buffer *opb)
    final InfoFloor unpack(final Info vi, final Buffer opb) {
        final CodecSetupInfo ci = vi.codec_setup;

        final InfoFloor0 info = new InfoFloor0();
        info.order = opb.pack_read(8);
        info.rate = opb.pack_read(16);
        info.barkmap = opb.pack_read(16);
        info.ampbits = opb.pack_read(6);
        info.ampdB = opb.pack_read(8);
        info.numbooks = opb.pack_read(4) + 1;

        if (info.order < 1) {
            return null;// goto err_out;
        }
        if (info.rate < 1) {
            return null;// goto err_out;
        }
        if (info.barkmap < 1) {
            return null;// goto err_out;
        }
        if (info.numbooks < 1) {
            return null;// goto err_out;
        }

        final StaticCodebook[] bp = ci.book_param;// java
        for (int j = 0, je = info.numbooks; j < je; j++) {
            final int books = opb.pack_read(8);
            info.books[j] = books;
            if (books < 0 || books >= ci.books) {
                return null;// goto err_out;
            }
            if (bp[books].maptype == 0) {
                return null;// goto err_out;
            }
            if (bp[books].dim < 1) {
                return null;// goto err_out;
            }
        }
        return (info);

//err_out:
//		floor0_free_info( info );
//		return (NULL);
    }

    @Override
    // vorbis_look_floor *floor0_look(vorbis_dsp_state *vd, vorbis_info_floor *i)
    final LookFloor look(final DspState vd, final InfoFloor i) {
        final InfoFloor0 info = (InfoFloor0) i;
        final LookFloor0 look = new LookFloor0();

        // (void)vd;

        look.m = info.order;
        look.ln = info.barkmap;
        look.vi = info;

        look.linearmap = new int[2][];

        return look;
    }

    @Override
    // void *floor0_inverse1(vorbis_block *vb,vorbis_look_floor *i)
    final Object inverse1(final Block vb, final LookFloor i) {
        final LookFloor0 look = (LookFloor0) i;
        final InfoFloor0 info = look.vi;

        final int ampraw = vb.opb.pack_read(info.ampbits);
        if (ampraw > 0) { /* also handles the -1 out of data case */
            final int maxval = (1 << info.ampbits) - 1;
            final float amp = (float) ampraw / maxval * info.ampdB;
            final int booknum = vb.opb.pack_read(Codec.ilog(info.numbooks));

            if (booknum != -1 && booknum < info.numbooks) { /* be paranoid */
                final CodecSetupInfo ci = vb.vd.vi.codec_setup;
                final Codebook b = ci.fullbooks[info.books[booknum]];
                float last = 0.f;

				/* the additional b->dim is a guard against any possible stack
				 smash; b->dim is provably more than we can overflow the
				 vector */
                final int m = look.m;// java
                final int dim = b.dim;// java
                final float[] lsp = new float[m + dim + 1];

                if (b.decodev_set(lsp, vb.opb, m) == -1) {
                    return null;// goto eop;
                }
                for (int j = 0; j < m; ) {
                    for (int k = 0; j < m && k < dim; k++, j++) {
                        lsp[j] += last;
                    }
                    last = lsp[j - 1];
                }

                lsp[m] = amp;
                return (lsp);
            }
        }
//eop:
        return (null);
    }

    /**
     * initialize Bark scale and normalization lookups.  We could do this
     * with static tables, but Vorbis allows a number of possible
     * combinations, so it's best to do it computationally.
     * <p>
     * The below is authoritative in terms of defining scale mapping.
     * Note that the scale depends on the sampling rate as well as the
     * linear block and mapping sizes
     */
    private static void map_lazy_init(final Block vb, final Object infoX, final LookFloor0 look) {
        if (look.linearmap[vb.W] == null) {
            // final DspState   vd = vb.vd;
            // final Info        vi = vd.vi;
            // final CodecSetupInfo   ci = vi.codec_setup;
            final InfoFloor0 info = (InfoFloor0) infoX;
            final int W = vb.W;
            final int n = vb.vd.vi.codec_setup.blocksizes[W] >> 1;// ci.blocksizes[W] >> 1;
            int j;
			/* we choose a scaling constant so that:
			   floor(bark(rate/2-1)*C)=mapped-1
			 floor(bark(rate/2)*C)=mapped */
            final float scale = look.ln / Codec.toBARK(info.rate / 2.f);

			/* the mapping from a linear scale to a smaller bark scale is
			   straightforward.  We do *not* make sure that the linear mapping
			   does not skip bark-scale bins; the decoder simply skips them and
			   the encoder may do what it wishes in filling them.  They're
			   necessary in some mapping combinations to keep the scale spacing
			   accurate */
            final int[] buff = new int[n + 1];// java
            look.linearmap[W] = buff;
            final float v = ((float) info.rate / 2.f) / (float) n;// java
            for (j = 0; j < n; j++) {
                int val = (int) Math.floor(Codec.toBARK(v * (float) j) * scale); /* bark numbers represent band edges */
                if (val >= look.ln) {
                    val = look.ln - 1; /* guard against the approximation */
                }
                buff[j] = val;
            }
            buff[j] = -1;
            look.n[W] = n;
        }
    }

    @Override
    // int floor0_inverse2(vorbis_block *vb,vorbis_look_floor *i,void *memo,float *out)
    final boolean inverse2(final Block vb, final LookFloor i, final Object memo, final float[] out) {
        final LookFloor0 look = (LookFloor0) i;
        final InfoFloor0 info = look.vi;

        map_lazy_init(vb, info, look);

        if (memo != null) {
            final float[] lsp = (float[]) memo;
            final float amp = lsp[look.m];

            /* take the coefficients back to a spectral envelope curve */
            lsp_to_curve(out,
                    look.linearmap[vb.W],
                    look.n[vb.W],
                    look.ln,
                    lsp, look.m, amp, (float) info.ampdB);
            return true;
        }
        Arrays.fill(out, 0, look.n[vb.W], 0);
        return false;
    }
}
