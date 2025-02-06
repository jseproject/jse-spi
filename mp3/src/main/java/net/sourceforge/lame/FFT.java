package net.sourceforge.lame;

// fft.c

class FFT {

	// private static final int TRI_SIZE = (5 - 1);  /* 1024 =  4**5 */

	/* fft.c    */

	private static final float costab[] = {// [TRI_SIZE * 2] = {
		9.238795325112867e-01f, 3.826834323650898e-01f,
		9.951847266721969e-01f, 9.801714032956060e-02f,
		9.996988186962042e-01f, 2.454122852291229e-02f,
		9.999811752826011e-01f, 6.135884649154475e-03f
	};

	private static final int rv_tbl[] = {// java: or char
		0x00, 0x80, 0x40, 0xc0, 0x20, 0xa0, 0x60, 0xe0,
		0x10, 0x90, 0x50, 0xd0, 0x30, 0xb0, 0x70, 0xf0,
		0x08, 0x88, 0x48, 0xc8, 0x28, 0xa8, 0x68, 0xe8,
		0x18, 0x98, 0x58, 0xd8, 0x38, 0xb8, 0x78, 0xf8,
		0x04, 0x84, 0x44, 0xc4, 0x24, 0xa4, 0x64, 0xe4,
		0x14, 0x94, 0x54, 0xd4, 0x34, 0xb4, 0x74, 0xf4,
		0x0c, 0x8c, 0x4c, 0xcc, 0x2c, 0xac, 0x6c, 0xec,
		0x1c, 0x9c, 0x5c, 0xdc, 0x3c, 0xbc, 0x7c, 0xfc,
		0x02, 0x82, 0x42, 0xc2, 0x22, 0xa2, 0x62, 0xe2,
		0x12, 0x92, 0x52, 0xd2, 0x32, 0xb2, 0x72, 0xf2,
		0x0a, 0x8a, 0x4a, 0xca, 0x2a, 0xaa, 0x6a, 0xea,
		0x1a, 0x9a, 0x5a, 0xda, 0x3a, 0xba, 0x7a, 0xfa,
		0x06, 0x86, 0x46, 0xc6, 0x26, 0xa6, 0x66, 0xe6,
		0x16, 0x96, 0x56, 0xd6, 0x36, 0xb6, 0x76, 0xf6,
		0x0e, 0x8e, 0x4e, 0xce, 0x2e, 0xae, 0x6e, 0xee,
		0x1e, 0x9e, 0x5e, 0xde, 0x3e, 0xbe, 0x7e, 0xfe
	};

	private static final void fht(final float[] fz, final int foffset, int n) {
		int tri = 0;
		final float[] tab = costab;

		n <<= 1;            /* to get BLKSIZE, because of 3DNow! ASM routine */
		final int fn = n + foffset;
		int k4 = 4;
		do {
			final int kx = k4 >> 1;
			final int k1 = k4;
			final int k2 = k4 << 1;
			final int k3 = k2 + k1;
			k4 = k2 << 1;
			int fi = foffset;
			int gi = fi + kx;
			do {
				float f1 = fz[fi + 0] - fz[fi + k1];
				float f0 = fz[fi + 0] + fz[fi + k1];
				float f3 = fz[fi + k2] - fz[fi + k3];
				float f2 = fz[fi + k2] + fz[fi + k3];
				fz[fi + k2] = f0 - f2;
				fz[fi + 0] = f0 + f2;
				fz[fi + k3] = f1 - f3;
				fz[fi + k1] = f1 + f3;
				f1 = fz[gi + 0] - fz[gi + k1];
				f0 = fz[gi + 0] + fz[gi + k1];
				f3 = Util.SQRT2 * fz[gi + k3];
				f2 = Util.SQRT2 * fz[gi + k2];
				fz[gi + k2] = f0 - f2;
				fz[gi + 0] = f0 + f2;
				fz[gi + k3] = f1 - f3;
				fz[gi + k1] = f1 + f3;
				gi += k4;
				fi += k4;
			} while( fi < fn );
			float c1 = tab[tri + 0];
			float s1 = tab[tri + 1];
			for( int i = 1; i < kx; i++ ) {
				float c2 = 1 - (2 * s1) * s1;
				final float s2 = (2 * s1) * c1;
				fi = i + foffset;
				gi = k1 - i + foffset;
				do {
					float b = s2 * fz[fi + k1] - c2 * fz[gi + k1];
					float a = c2 * fz[fi + k1] + s2 * fz[gi + k1];
					final float f1 = fz[fi + 0] - a;
					final float f0 = fz[fi + 0] + a;
					final float g1 = fz[gi + 0] - b;
					final float g0 = fz[gi + 0] + b;
					b = s2 * fz[fi + k3] - c2 * fz[gi + k3];
					a = c2 * fz[fi + k3] + s2 * fz[gi + k3];
					final float f3 = fz[fi + k2] - a;
					final float f2 = fz[fi + k2] + a;
					final float g3 = fz[gi + k2] - b;
					final float g2 = fz[gi + k2] + b;
					b = s1 * f2 - c1 * g3;
					a = c1 * f2 + s1 * g3;
					fz[fi + k2] = f0 - a;
					fz[fi + 0] = f0 + a;
					fz[gi + k3] = g1 - b;
					fz[gi + k1] = g1 + b;
					b = c1 * g2 - s1 * f3;
					a = s1 * g2 + c1 * f3;
					fz[gi + k2] = g0 - a;
					fz[gi + 0] = g0 + a;
					fz[fi + k3] = f1 - b;
					fz[fi + k1] = f1 + b;
					gi += k4;
					fi += k4;
				} while( fi < fn );
				c2 = c1;
				c1 = c2 * tab[tri + 0] - s1 * tab[tri + 1];
				s1 = c2 * tab[tri + 1] + s1 * tab[tri + 0];
			}
			tri += 2;
		} while( k4 < n );
	}
/*
	#define ch01(index)  (buffer[chn][index])

	#define ms00(f) (window_s[i       ] * f(i + k))
	#define ms10(f) (window_s[0x7f - i] * f(i + k + 0x80))
	#define ms20(f) (window_s[i + 0x40] * f(i + k + 0x40))
	#define ms30(f) (window_s[0x3f - i] * f(i + k + 0xc0))

	#define ms01(f) (window_s[i + 0x01] * f(i + k + 0x01))
	#define ms11(f) (window_s[0x7e - i] * f(i + k + 0x81))
	#define ms21(f) (window_s[i + 0x41] * f(i + k + 0x41))
	#define ms31(f) (window_s[0x3e - i] * f(i + k + 0xc1))
*/
	static final void fft_short(final InternalFlags gfc,
			final float x_real[/*3*/][/*BLKSIZE_s*/], final int chn, final float buffer[/*2*/][], final int boffset)
	{
		final float[] window_s = gfc.cd_psy.window_s;
		final float[] window = gfc.cd_psy.window;

		final float[] buf = buffer[chn];// java
		int b = 0;
		do {
			final float[] x = x_real[b];
			int xi = Encoder.BLKSIZE_s / 2;// x[xi]
			final int k = (576 / 3) * (b + 1) + boffset;
			int j = Encoder.BLKSIZE_s / 8 - 1;
			do {
				final int i = rv_tbl[j << 2];
				final int ik = i + k;

				float f0 = (window_s[i       ] * buf[ik]);// ms00(ch01);
				float w  = (window_s[0x7f - i] * buf[ik + 0x80]);// ms10(ch01);
				float f1 = f0 - w;
				f0 = f0 + w;
				float f2 = (window_s[i + 0x40] * buf[ik + 0x40]);// ms20(ch01);
				w        = (window_s[0x3f - i] * buf[ik + 0xc0]);// ms30(ch01);
				float f3 = f2 - w;
				f2 = f2 + w;

				x[--xi] = f1 - f3;
				x[--xi] = f0 - f2;
				x[--xi] = f1 + f3;
				x[--xi] = f0 + f2;

				f0 = (window_s[i + 0x01] * buf[ik + 0x01]);// ms01(ch01);
				w  = (window_s[0x7e - i] * buf[ik + 0x81]);// ms11(ch01);
				f1 = f0 - w;
				f0 = f0 + w;
				f2 = (window_s[i + 0x41] * buf[ik + 0x41]);// ms21(ch01);
				w  = (window_s[0x3e - i] * buf[ik + 0xc1]);// ms31(ch01);
				f3 = f2 - w;
				f2 = f2 + w;

				x[Encoder.BLKSIZE_s / 2 + xi++] = f0 + f2;
				x[Encoder.BLKSIZE_s / 2 + xi++] = f1 + f3;
				x[Encoder.BLKSIZE_s / 2 + xi++] = f0 - f2;
				x[Encoder.BLKSIZE_s / 2 + xi  ] = f1 - f3;
				xi -= 3;
			} while( --j >= 0 );

			//gfc.fft_fht( x, Encoder.BLKSIZE_s / 2 );
			fht( x, xi, Encoder.BLKSIZE_s / 2 );
			/* BLKSIZE_s/2 because of 3DNow! ASM routine */
		} while( ++b < 3 );
	}
/*
	#define ml00(f) (window[i        ] * f(i))
	#define ml10(f) (window[i + 0x200] * f(i + 0x200))
	#define ml20(f) (window[i + 0x100] * f(i + 0x100))
	#define ml30(f) (window[i + 0x300] * f(i + 0x300))

	#define ml01(f) (window[i + 0x001] * f(i + 0x001))
	#define ml11(f) (window[i + 0x201] * f(i + 0x201))
	#define ml21(f) (window[i + 0x101] * f(i + 0x101))
	#define ml31(f) (window[i + 0x301] * f(i + 0x301))
*/
	static final void fft_long(final InternalFlags gfc, final float x[/*BLKSIZE*/], final int chn,
                               final float buffer[/*2*/][], final int boffset)
	{
		final float[] buf = buffer[chn];// java
		int jj = Encoder.BLKSIZE / 8 - 1;
		int xoffset = Encoder.BLKSIZE / 2;

		final float[] window_s = gfc.cd_psy.window_s;
		final float[] window = gfc.cd_psy.window;

		do {
			final int i = rv_tbl[jj];
			final int bi = boffset + i;
			float f0 = (window[i        ] * buf[bi]);// ml00(ch01);
			float w  = (window[i + 0x200] * buf[bi + 0x200]);// ml10(ch01);
			float f1 = f0 - w;
			f0 = f0 + w;
			float f2 = (window[i + 0x100] * buf[bi + 0x100]);// ml20(ch01);
			w        = (window[i + 0x300] * buf[bi + 0x300]);// ml30(ch01);
			float f3 = f2 - w;
			f2 = f2 + w;

			x[--xoffset] = f1 - f3;
			x[--xoffset] = f0 - f2;
			x[--xoffset] = f1 + f3;
			x[--xoffset] = f0 + f2;

			f0 = (window[i + 0x001] * buf[bi + 0x001]);// ml01(ch01);
			w  = (window[i + 0x201] * buf[bi + 0x201]);// ml11(ch01);
			f1 = f0 - w;
			f0 = f0 + w;
			f2 = (window[i + 0x101] * buf[bi + 0x101]);// ml21(ch01);
			w  = (window[i + 0x301] * buf[bi + 0x301]);// ml31(ch01);
			f3 = f2 - w;
			f2 = f2 + w;

			x[Encoder.BLKSIZE / 2 + xoffset++] = f0 + f2;
			x[Encoder.BLKSIZE / 2 + xoffset++] = f1 + f3;
			x[Encoder.BLKSIZE / 2 + xoffset++] = f0 - f2;
			x[Encoder.BLKSIZE / 2 + xoffset  ] = f1 - f3;
			xoffset -= 3;
		} while( --jj >= 0 );

		//gfc.fft_fht( x, Encoder.BLKSIZE / 2 );
		fht( x, xoffset, Encoder.BLKSIZE / 2 );
		/* BLKSIZE/2 because of 3DNow! ASM routine */
	}

	static final void init_fft(final InternalFlags gfc ) {
		/* The type of window used here will make no real difference, but */
		/* in the interest of merging nspsytune stuff - switch to blackman window */
		for(int i = 0; i < Encoder.BLKSIZE; i++ ) {
			/* blackman window */
			gfc.cd_psy.window[i] = (float)(0.42 - 0.5 * Math.cos(2.0 * Math.PI * (i + .5) / Encoder.BLKSIZE) +
						0.08 * Math.cos(4. * Math.PI * (i + .5) / Encoder.BLKSIZE));
		}

		for(int i = 0; i < Encoder.BLKSIZE_s / 2; i++ ) {
			gfc.cd_psy.window_s[i] = (float)(0.5 * (1.0 - Math.cos(2.0 * Math.PI * (i + 0.5) / Encoder.BLKSIZE_s)));
		}
	}
}