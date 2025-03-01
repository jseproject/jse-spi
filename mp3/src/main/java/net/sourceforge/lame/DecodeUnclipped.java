package net.sourceforge.lame;

class DecodeUnclipped extends DCT64 implements Synth {

	@Override
	public final int synth_1to1_mono(final MpStrTag mp, final float[] bandPtr, final int boffset, final Object outp, final int[] pnt) {
	//private static final int synth_1to1_mono_unclipped(final MpStrTag mp, final float[] bandPtr, final float[] out, final int[] pnt) {
		/* versions: clipped (when TYPE == short) and unclipped (when TYPE == real) of synth_1to1_mono* functions */
		final float[] out = (float[]) outp;
		final float samples_tmp[] = new float[64];
		int tmp1 = 0;// samples_tmp[tmp1]
		final int pnt1[] = { 0 };

		final int ret = synth_1to1( mp, bandPtr, boffset, 0, samples_tmp, pnt1 );
		// ret = synth_1to1_unclipped( mp, bandPtr, 0, samples_tmp, pnt1 );
		int outoffset = pnt[0];// java: sample counter// / (Float.SIZE / 8);

		do {// for( int i = 0; i < 32; i++ ) {
			out[outoffset] = samples_tmp[ tmp1 ];
			outoffset++;
			tmp1 += 2;
		} while( tmp1 < 32 * 2 );
		pnt[0] += 32;// java: sample counter// * (Float.SIZE / 8);// 32 * sizeof( TYPE );

		return ret;
	//}
	}

	@Override
	public final int synth_1to1(final MpStrTag mp, final float[] bandPtr, final int boffset, final int channel, final Object outp, final int[] pnt) {
	// private static final int synth_1to1_unclipped(final MpStrTag mp, final float[] bandPtr, final int channel, final float[] out, final int[] pnt) {
		final float[] out = (float[]) outp;
		final int step = 2;
		int samples = pnt[0];// java: sample counter// / (Float.SIZE / 8);

		float buf[][];
		final int clip = 0;

		int bo = mp.synth_bo;

		if( 0 == channel ) {
			bo--;
			bo &= 0xf;
			buf = mp.synth_buffs[0];
		} else {
			samples++;
			buf = mp.synth_buffs[1];
		}

		int b0 = 0;// buf0[ b0 ]
		final float[] buf0;
		int bo1;
		if( (bo & 0x1) != 0 ) {
			buf0 = buf[0];
			bo1 = bo;
			dct64( buf[1], ((bo + 1) & 0xf), buf[0], bo, bandPtr, boffset );
		} else {
			buf0 = buf[1];
			bo1 = bo + 1;
			dct64( buf[0], bo, buf[1], bo + 1, bandPtr, boffset );
		}

		mp.synth_bo = bo;

		{
			final float[] decwin = sDecwin;
			int window = 16 - bo1;
			int j = window + 0x20 * 16;
			do {// for( int j = 16; j != 0; j--, window += 0x11, samples += step ) {
				float sum = decwin[ window++ ] * buf0[ b0++ ];
				sum -= decwin[ window++ ] * buf0[ b0++ ];
				sum += decwin[ window++ ] * buf0[ b0++ ];
				sum -= decwin[ window++ ] * buf0[ b0++ ];
				sum += decwin[ window++ ] * buf0[ b0++ ];
				sum -= decwin[ window++ ] * buf0[ b0++ ];
				sum += decwin[ window++ ] * buf0[ b0++ ];
				sum -= decwin[ window++ ] * buf0[ b0++ ];
				sum += decwin[ window++ ] * buf0[ b0++ ];
				sum -= decwin[ window++ ] * buf0[ b0++ ];
				sum += decwin[ window++ ] * buf0[ b0++ ];
				sum -= decwin[ window++ ] * buf0[ b0++ ];
				sum += decwin[ window++ ] * buf0[ b0++ ];
				sum -= decwin[ window++ ] * buf0[ b0++ ];
				sum += decwin[ window++ ] * buf0[ b0++ ];
				sum -= decwin[ window   ] * buf0[ b0++ ];

				out[samples] = sum;
				window += 0x11; samples += step;
			} while( window < j );

			{
				float sum  = decwin[ window + 0x0] * buf0[ b0 + 0x0];
				sum += decwin[ window + 0x2] * buf0[ b0 + 0x2];
				sum += decwin[ window + 0x4] * buf0[ b0 + 0x4];
				sum += decwin[ window + 0x6] * buf0[ b0 + 0x6];
				sum += decwin[ window + 0x8] * buf0[ b0 + 0x8];
				sum += decwin[ window + 0xA] * buf0[ b0 + 0xA];
				sum += decwin[ window + 0xC] * buf0[ b0 + 0xC];
				sum += decwin[ window + 0xE] * buf0[ b0 + 0xE];
				out[samples] = sum;
				window -= 0x20; samples += step;
			}
			window += bo1 << 1;

			j = window - 0x20 * 15;
			do {// for( j = 15; j != 0; j--, window -= 0x20, samples += step ) {
				float sum = -decwin[ window ] * buf0[ --b0 ];
				int w = window - 0xF;
				sum -= decwin[ w++ ] * buf0[ --b0 ];
				sum -= decwin[ w++ ] * buf0[ --b0 ];
				sum -= decwin[ w++ ] * buf0[ --b0 ];
				sum -= decwin[ w++ ] * buf0[ --b0 ];
				sum -= decwin[ w++ ] * buf0[ --b0 ];
				sum -= decwin[ w++ ] * buf0[ --b0 ];
				sum -= decwin[ w++ ] * buf0[ --b0 ];
				sum -= decwin[ w++ ] * buf0[ --b0 ];
				sum -= decwin[ w++ ] * buf0[ --b0 ];
				sum -= decwin[ w++ ] * buf0[ --b0 ];
				sum -= decwin[ w++ ] * buf0[ --b0 ];
				sum -= decwin[ w++ ] * buf0[ --b0 ];
				sum -= decwin[ w++ ] * buf0[ --b0 ];
				sum -= decwin[ w++ ] * buf0[ --b0 ];
				sum -= decwin[ w   ] * buf0[ --b0 ];

				out[samples] = sum;
				window -= 0x20; samples += step;
			} while( window > j );
		}
		pnt[0] += 64;// java: sample counter// * (Float.SIZE / 8);// 64 * sizeof(real);

		return clip;
	// }
	}

}
