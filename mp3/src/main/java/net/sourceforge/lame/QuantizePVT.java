package net.sourceforge.lame;

// quantize_pvt.c

class QuantizePVT {
	/** ix always <= 8191+15.    see count_bits() */
	static final int IXMAX_VAL = 8206;

	/* buggy Winamp decoder cannot handle values > 8191 */
	/* #define IXMAX_VAL 8191 */

	private static final int PRECALC_SIZE = (IXMAX_VAL + 2);

	private static final int Q_MAX = (256 + 1);
	/** minimum possible number of
	 * -cod_info->global_gain + ((scalefac[] + (cod_info->preflag ? pretab[sfb] : 0))
	 * << (cod_info->scalefac_scale + 1)) + cod_info->subblock_gain[cod_info->window[sfb]] * 8;
	 * for long block, 0+((15+3)<<2) = 18*4 = 72
	 * for short block, 0+(15<<2)+7*8 = 15*4+56 = 116
	 */
	static final int Q_MAX2 = 116;

	private static final int NSATHSCALE = 100;  /* Assuming dynamic range=96dB, this value should be 92 */

	/**
	  The following table is used to implement the scalefactor
	  partitioning for MPEG2 as described in section
	  2.4.3.2 of the IS. The indexing corresponds to the
	  way the tables are presented in the IS:

	  [table_number][row_in_table][column of nr_of_sfb]
	*/
	static final int nr_of_sfb_block[][][] = {// [6][3][4] = {
		{
			{6, 5, 5, 5},
			{9, 9, 9, 9},
			{6, 9, 9, 9}
		}, {
			{6, 5, 7, 3},
			{9, 9, 12, 6},
			{6, 9, 12, 6}
		}, {
			{11, 10, 0, 0},
			{18, 18, 0, 0},
			{15, 18, 0, 0}
		}, {
			{7, 7, 7, 0},
			{12, 12, 12, 0},
			{6, 15, 12, 0}
		}, {
			{6, 6, 6, 3},
			{12, 9, 9, 6},
			{6, 12, 9, 6}
		}, {
			{8, 8, 5, 0},
			{15, 12, 9, 0},
			{6, 18, 9, 0}
		}
	};

	/** Table B.6: layer3 preemphasis */
	static final int pretab[] = {// [SBMAX_l] = {
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		1, 1, 1, 1, 2, 2, 3, 3, 3, 2, 0
	};

	/**
	  Here are MPEG1 Table B.8 and MPEG2 Table B.1
	  -- Layer III scalefactor bands.
	  Index into this using a method such as:
	    idx  = fr_ps.header.sampling_frequency
	           + (fr_ps.header.version * 3)
	*/
	static final ScaleFacStruct sfBandIndex[] = {// [9] = {
		new ScaleFacStruct(                   /* Table B.2.b: 22.05 kHz */
			new int[]{0, 6, 12, 18, 24, 30, 36, 44, 54, 66, 80, 96, 116, 140, 168, 200, 238, 284, 336, 396, 464, 522, 576},
			new int[] {0, 4, 8, 12, 18, 24, 32, 42, 56, 74, 100, 132, 174, 192},
			new int[] {0, 0, 0, 0, 0, 0, 0}, /*  sfb21 pseudo sub bands */
			new int[] {0, 0, 0, 0, 0, 0, 0} /*  sfb12 pseudo sub bands */
		),
		new ScaleFacStruct(                   /* Table B.2.c: 24 kHz */ /* docs: 332. mpg123(broken): 330 */
			new int[]{0, 6, 12, 18, 24, 30, 36, 44, 54, 66, 80, 96, 114, 136, 162, 194, 232, 278, 332, 394, 464, 540, 576},
			new int[]{0, 4, 8, 12, 18, 26, 36, 48, 62, 80, 104, 136, 180, 192},
			new int[]{0, 0, 0, 0, 0, 0, 0}, /*  sfb21 pseudo sub bands */
			new int[]{0, 0, 0, 0, 0, 0, 0} /*  sfb12 pseudo sub bands */
		),
		new ScaleFacStruct(                   /* Table B.2.a: 16 kHz */
			new int[]{0, 6, 12, 18, 24, 30, 36, 44, 54, 66, 80, 96, 116, 140, 168, 200, 238, 284, 336, 396, 464, 522, 576},
			new int[]{0, 4, 8, 12, 18, 26, 36, 48, 62, 80, 104, 134, 174, 192},
			new int[]{0, 0, 0, 0, 0, 0, 0}, /*  sfb21 pseudo sub bands */
			new int[]{0, 0, 0, 0, 0, 0, 0} /*  sfb12 pseudo sub bands */
		),
		new ScaleFacStruct(                   /* Table B.8.b: 44.1 kHz */
			new int[]{0, 4, 8, 12, 16, 20, 24, 30, 36, 44, 52, 62, 74, 90, 110, 134, 162, 196, 238, 288, 342, 418, 576},
			new int[]{0, 4, 8, 12, 16, 22, 30, 40, 52, 66, 84, 106, 136, 192},
			new int[]{0, 0, 0, 0, 0, 0, 0}, /*  sfb21 pseudo sub bands */
			new int[]{0, 0, 0, 0, 0, 0, 0} /*  sfb12 pseudo sub bands */
		),
		new ScaleFacStruct(                   /* Table B.8.c: 48 kHz */
			new int[]{0, 4, 8, 12, 16, 20, 24, 30, 36, 42, 50, 60, 72, 88, 106, 128, 156, 190, 230, 276, 330, 384, 576},
			new int[]{0, 4, 8, 12, 16, 22, 28, 38, 50, 64, 80, 100, 126, 192},
			new int[]{0, 0, 0, 0, 0, 0, 0}, /*  sfb21 pseudo sub bands */
			new int[]{0, 0, 0, 0, 0, 0, 0} /*  sfb12 pseudo sub bands */
		),
		new ScaleFacStruct(                   /* Table B.8.a: 32 kHz */
			new int[]{0, 4, 8, 12, 16, 20, 24, 30, 36, 44, 54, 66, 82, 102, 126, 156, 194, 240, 296, 364, 448, 550, 576},
			new int[]{0, 4, 8, 12, 16, 22, 30, 42, 58, 78, 104, 138, 180, 192},
			new int[]{0, 0, 0, 0, 0, 0, 0}, /*  sfb21 pseudo sub bands */
			new int[]{0, 0, 0, 0, 0, 0, 0} /*  sfb12 pseudo sub bands */
		),
		new ScaleFacStruct(                   /* MPEG-2.5 11.025 kHz */
			new int[]{0, 6, 12, 18, 24, 30, 36, 44, 54, 66, 80, 96, 116, 140, 168, 200, 238, 284, 336, 396, 464, 522, 576},
			new int[]{0 / 3, 12 / 3, 24 / 3, 36 / 3, 54 / 3, 78 / 3, 108 / 3, 144 / 3, 186 / 3, 240 / 3, 312 / 3, 402 / 3, 522 / 3, 576 / 3},
			new int[]{0, 0, 0, 0, 0, 0, 0}, /*  sfb21 pseudo sub bands */
			new int[]{0, 0, 0, 0, 0, 0, 0} /*  sfb12 pseudo sub bands */
		),
		new ScaleFacStruct(                   /* MPEG-2.5 12 kHz */
			new int[]{0, 6, 12, 18, 24, 30, 36, 44, 54, 66, 80, 96, 116, 140, 168, 200, 238, 284, 336, 396, 464, 522, 576},
			new int[]{0 / 3, 12 / 3, 24 / 3, 36 / 3, 54 / 3, 78 / 3, 108 / 3, 144 / 3, 186 / 3, 240 / 3, 312 / 3, 402 / 3, 522 / 3, 576 / 3},
			new int[]{0, 0, 0, 0, 0, 0, 0}, /*  sfb21 pseudo sub bands */
			new int[]{0, 0, 0, 0, 0, 0, 0} /*  sfb12 pseudo sub bands */
		),
		new ScaleFacStruct(                   /* MPEG-2.5 8 kHz */
			new int[]{0, 12, 24, 36, 48, 60, 72, 88, 108, 132, 160, 192, 232, 280, 336, 400, 476, 566, 568, 570, 572, 574, 576},
			new int[]{0 / 3, 24 / 3, 48 / 3, 72 / 3, 108 / 3, 156 / 3, 216 / 3, 288 / 3, 372 / 3, 480 / 3, 486 / 3, 492 / 3, 498 / 3, 576 / 3},
			new int[]{0, 0, 0, 0, 0, 0, 0}, /*  sfb21 pseudo sub bands */
			new int[]{0, 0, 0, 0, 0, 0, 0} /*  sfb12 pseudo sub bands */
		)
	};

	static final float pow20[] = new float[Q_MAX + Q_MAX2 + 1];
	static final float ipow20[] = new float[Q_MAX];
	static final float pow43[] = new float[PRECALC_SIZE];
	/* initialized in first call to iteration_init */
	// static final boolean TAKEHIRO_IEEE754_HACK = false;// java: true leads to another result vs c-version
//#ifdef TAKEHIRO_IEEE754_HACK
//	static final float adj43asm[] = new float[PRECALC_SIZE];
//#else
	static final float adj43[] = new float[PRECALC_SIZE];
//#endif

	//machine.h
	/*
	 * 3 different types of pow() functions:
	 *   - table lookup
	 *   - pow()
	 *   - exp()   on some machines this is claimed to be faster than pow()
	 */
	private static final float POW20(final int x) {
		// assert(0 <= (x+Q_MAX2) && x < Q_MAX);
		return pow20[x + Q_MAX2];
	}
	/*#define POW20(x)  pow(2.0,((double)(x)-210)*.25) */
	/*#define POW20(x)  exp( ((double)(x)-210)*(.25*LOG2) ) */

	/* static final float IPOW20(final int x) {// java: extracted inplace
		// assert(0 <= x && x < Q_MAX);
		return ipow20[x];
	} */
	/*#define IPOW20(x)  exp( -((double)(x)-210)*.1875*LOG2 ) */
	/*#define IPOW20(x)  pow(2.0,-((double)(x)-210)*.1875) */

	/**
	compute the ATH for each scalefactor band
	cd range:  0..96db

	Input:  3.3kHz signal  32767 amplitude  (3.3kHz is where ATH is smallest = -5db)
	longblocks:  sfb=12   en0/bw=-11db    max_en0 = 1.3db
	shortblocks: sfb=5           -9db              0db

	Input:  1 1 1 1 1 1 1 -1 -1 -1 -1 -1 -1 -1 (repeated)
	longblocks:  amp=1      sfb=12   en0/bw=-103 db      max_en0 = -92db
	            amp=32767   sfb=12           -12 db                 -1.4db

	Input:  1 1 1 1 1 1 1 -1 -1 -1 -1 -1 -1 -1 (repeated)
	shortblocks: amp=1      sfb=5   en0/bw= -99                    -86
	            amp=32767   sfb=5           -9  db                  4db


	MAX energy of largest wave at 3.3kHz = 1db
	AVE energy of largest wave at 3.3kHz = -11db
	Let's take AVE:  -11db = maximum signal in sfb=12.
	Dynamic range of CD: 96db.  Therefor energy of smallest audible wave
	in sfb=12  = -11  - 96 = -107db = ATH at 3.3kHz.

	ATH formula for this wave: -5db.  To adjust to LAME scaling, we need
	ATH = ATH_formula  - 103  (db)
	ATH = ATH * 2.5e-10      (ener)

	*/
	private static final float ATHmdct(final SessionConfig cfg, final float f) {
		float ath = Util.ATHformula( cfg, f );

		if( cfg.ATHfixpoint > 0 ) {
			ath -= cfg.ATHfixpoint;
		} else {
			ath -= NSATHSCALE;
		}
		ath += cfg.ATH_offset_db;

		/* modify the MDCT scaling for the ATH and convert to energy */
		ath = (float)Math.pow( 10.0, (double)(ath * 0.1f) );
		return ath;
	}

	private static final void compute_ath(final InternalFlags gfc) {
		final SessionConfig cfg = gfc.cfg;
		final float[] ATH_l = gfc.ATH.l;
		final float[] ATH_psfb21 = gfc.ATH.psfb21;
		final float[] ATH_s = gfc.ATH.s;
		final float[] ATH_psfb12 = gfc.ATH.psfb12;
		final float samp_freq = cfg.samplerate;
		final ScaleFacStruct scalefac_band = gfc.scalefac_band;// java

		for(int sfb = 0; sfb < Encoder.SBMAX_l; sfb++ ) {
			final int start = scalefac_band.l[sfb];
			final int end = scalefac_band.l[sfb + 1];
			ATH_l[sfb] = Float.MAX_VALUE;
			for( int i = start; i < end; i++ ) {
				final float freq = i * samp_freq / (2 * 576);
				final float ATH_f = ATHmdct( cfg, freq ); /* freq in kHz */
				final float v = ATH_l[sfb];
				ATH_l[sfb] = (v <= ATH_f ? v : ATH_f);
			}
		}

		for(int sfb = 0; sfb < Encoder.PSFB21; sfb++ ) {
			final int start = scalefac_band.psfb21[sfb];
			final int end = scalefac_band.psfb21[sfb + 1];
			ATH_psfb21[sfb] = Float.MAX_VALUE;
			for( int i = start; i < end; i++ ) {
				final float freq = i * samp_freq / (2 * 576);
				final float ATH_f = ATHmdct( cfg, freq ); /* freq in kHz */
				final float v = ATH_psfb21[sfb];
				ATH_psfb21[sfb] = (v <= ATH_f ? v : ATH_f);
			}
		}

		for(int sfb = 0; sfb < Encoder.SBMAX_s; sfb++ ) {
			final int start = scalefac_band.s[sfb];
			final int end = scalefac_band.s[sfb + 1];
			ATH_s[sfb] = Float.MAX_VALUE;
			for( int i = start; i < end; i++ ) {
				final float freq = i * samp_freq / (2 * 192);
				final float ATH_f = ATHmdct( cfg, freq ); /* freq in kHz */
				final float v = ATH_s[sfb];
				ATH_s[sfb] = (v <= ATH_f ? v : ATH_f);
			}
			ATH_s[sfb] *= (scalefac_band.s[sfb + 1] - scalefac_band.s[sfb]);
		}

		for(int sfb = 0; sfb < Encoder.PSFB12; sfb++ ) {
			final int start = scalefac_band.psfb12[sfb];
			final int end = scalefac_band.psfb12[sfb + 1];
			ATH_psfb12[sfb] = Float.MAX_VALUE;
			for( int i = start; i < end; i++ ) {
				final float freq = i * samp_freq / (2 * 192);
				final float ATH_f = ATHmdct( cfg, freq ); /* freq in kHz */
				final float v = ATH_psfb12[sfb];
				ATH_psfb12[sfb] = (v <= ATH_f ? v : ATH_f);
			}
			/*not sure about the following */
			ATH_psfb12[sfb] *= (scalefac_band.s[13] - scalefac_band.s[12]);
		}

		/*  no-ATH mode:
		 *  reduce ATH to -200 dB
		 */

		if( cfg.noATH ) {
			for(int sfb = 0; sfb < Encoder.SBMAX_l; sfb++ ) {
				ATH_l[sfb] = 1E-20f;
			}
			for(int sfb = 0; sfb < Encoder.PSFB21; sfb++ ) {
				ATH_psfb21[sfb] = 1E-20f;
			}
			for(int sfb = 0; sfb < Encoder.SBMAX_s; sfb++ ) {
				ATH_s[sfb] = 1E-20f;
			}
			for(int sfb = 0; sfb < Encoder.PSFB12; sfb++ ) {
				ATH_psfb12[sfb] = 1E-20f;
			}
		}

		/*  work in progress, don't rely on it too much */
		gfc.ATH.floor = 10.f * (float)Math.log10( (double)ATHmdct( cfg, -1.f ) );

		/*
		   {   FLOAT g=10000, t=1e30, x;
		   for(  f = 100; f < 10000; f++  ) {
		   x = ATHmdct( cfg, f );
		   if(  t > x ) t = x, g = f;
		   }
		   printf("min=%g\n", g);
		   } */
	}

	private static final float payload_long[][] = {// [2][4] = {
		{-0.000f, -0.000f, -0.000f, +0.000f}, {-0.500f, -0.250f, -0.025f, +0.500f}
	};
	private static final float payload_short[][] = {// [2][4] = {
		{-0.000f, -0.000f, -0.000f, +0.000f}, {-2.000f, -1.000f, -0.050f, +0.500f}
	};

	/************************************************************************/
	/*  initialization for iteration_loop */
	/************************************************************************/
	static final void iteration_init(final InternalFlags gfc) {
		final SessionConfig cfg = gfc.cfg;
		final III_SideInfo l3_side = gfc.l3_side;

		if( ! gfc.iteration_init_init ) {
			gfc.iteration_init_init = true;

			l3_side.main_data_begin = 0;
			compute_ath( gfc );

			pow43[0] = 0.0f;
			for( int i = 1; i < PRECALC_SIZE; i++ ) {
				pow43[i] = (float)Math.pow( (double)i, 4.0 / 3.0 );
			}

/*if( TAKEHIRO_IEEE754_HACK ) {
			adj43[0] = 0.0f;
			for( i = 1; i < PRECALC_SIZE; i++ ) {
				adj43[i] = (float)(i - 0.5 - Math.pow( (0.5 * (double)(pow43[i - 1] + pow43[i])), 0.75 ));
			}
} else {*/
			int i = 0;
			for( ; i < PRECALC_SIZE - 1; i++ ) {
				adj43[i] = (float)((i + 1) - Math.pow( (0.5 * (double)(pow43[i] + pow43[i + 1])), 0.75 ));
			}
			adj43[i] = 0.5f;
//}
			for( i = 0; i < Q_MAX; i++ ) {
				ipow20[i] = (float)Math.pow( 2.0, (double) ((i - 210) * -0.1875f) );
			}
			for( i = 0; i <= Q_MAX + Q_MAX2; i++ ) {
				pow20[i] = (float)Math.pow( 2.0, (double) ((i - 210 - Q_MAX2) * 0.25f) );
			}

			Takehiro.huffman_init( gfc );
			Quantize.init_xrpow_core_init( gfc );

			final int sel = 1;/* RH: all modes like vbr-new (cfg.vbr == vbr_mt || cfg.vbr == vbr_mtrh) ? 1 : 0;*/

			/* long */
			float db = cfg.adjust_bass_db + payload_long[sel][0];
			float adjust = (float)Math.pow( 10., (double)(db * 0.1f) );
			for( i = 0; i <= 6; ++i ) {
				gfc.sv_qnt.longfact[i] = adjust;
			}
			db = cfg.adjust_alto_db + payload_long[sel][1];
			adjust = (float)Math.pow( 10., (double)(db * 0.1f) );
			for( ; i <= 13; ++i ) {
				gfc.sv_qnt.longfact[i] = adjust;
			}
			db = cfg.adjust_treble_db + payload_long[sel][2];
			adjust = (float)Math.pow( 10., (double)(db * 0.1f) );
			for( ; i <= 20; ++i ) {
				gfc.sv_qnt.longfact[i] = adjust;
			}
			db = cfg.adjust_sfb21_db + payload_long[sel][3];
			adjust = (float)Math.pow( 10., (double)(db * 0.1f) );
			for(; i < Encoder.SBMAX_l; ++i ) {
				gfc.sv_qnt.longfact[i] = adjust;
			}

			/* short */
			db = cfg.adjust_bass_db + payload_short[sel][0];
			adjust = (float)Math.pow( 10., (double)(db * 0.1f) );
			for( i = 0; i <= 2; ++i ) {
				gfc.sv_qnt.shortfact[i] = adjust;
			}
			db = cfg.adjust_alto_db + payload_short[sel][1];
			adjust = (float)Math.pow( 10., (double)(db * 0.1f) );
			for( ; i <= 6; ++i ) {
				gfc.sv_qnt.shortfact[i] = adjust;
			}
			db = cfg.adjust_treble_db + payload_short[sel][2];
			adjust = (float)Math.pow( 10., (double)(db * 0.1f) );
			for( ; i <= 11; ++i ) {
				gfc.sv_qnt.shortfact[i] = adjust;
			}
			db = cfg.adjust_sfb21_db + payload_short[sel][3];
			adjust = (float)Math.pow( 10., (double)(db * 0.1f) );
			for(; i < Encoder.SBMAX_s; ++i ) {
				gfc.sv_qnt.shortfact[i] = adjust;
			}
		}
	}

	/************************************************************************
	 * allocate bits among 2 channels based on PE
	 * mt 6/99
	 * bugfixes rh 8/01: often allocated more than the allowed 4095 bits
	 ************************************************************************/
	static final int on_pe(final InternalFlags gfc, final float pe[][/*2*/], final int targ_bits[/*2*/], final int mean_bits, final int gr, final boolean cbr)
	{
		final SessionConfig cfg = gfc.cfg;
		int extra_bits = 0;
		final int add_bits[] = new int[2];// java: already zeroed = {0, 0};

		/* allocate targ_bits for granule */
		final long tmp = Reservoir.ResvMaxBits( gfc, mean_bits,/* &tbits, &extra_bits,*/ cbr );
		final int tbits = (int)tmp;
		extra_bits = (int)(tmp >> 32);
		int max_bits = tbits + extra_bits;/* maximum allowed bits for this granule */
		if( max_bits > Util.MAX_BITS_PER_GRANULE ) {
			max_bits = Util.MAX_BITS_PER_GRANULE;
		}

		final int channels_out = cfg.channels_out;// java
		int bits = 0;
		for( int ch = 0; ch < channels_out; ++ch ) {
			/******************************************************************
			 * allocate bits for each channel
			 ******************************************************************/
			int v = tbits / channels_out;
			targ_bits[ch] = (Util.MAX_BITS_PER_CHANNEL <= v ? Util.MAX_BITS_PER_CHANNEL : v);

			add_bits[ch] = (int)(targ_bits[ch] * pe[gr][ch] / 700.0f - targ_bits[ch]);

			/* at most increase bits by 1.5*average */
			v = (mean_bits * 3) >> 2;
			if( add_bits[ch] > v ) {
				add_bits[ch] = v;
			}
			if( add_bits[ch] < 0) {
				add_bits[ch] = 0;
			}

			if( add_bits[ch] + targ_bits[ch] > Util.MAX_BITS_PER_CHANNEL ) {
				v = Util.MAX_BITS_PER_CHANNEL - targ_bits[ch];
				add_bits[ch] = (0 >= v ? 0 : v);
			}

			bits += add_bits[ch];
		}
		if( bits > extra_bits && bits > 0 ) {
			for( int ch = 0; ch < channels_out; ++ch ) {
				add_bits[ch] = extra_bits * add_bits[ch] / bits;
			}
		}

		for( int ch = 0; ch < channels_out; ++ch ) {
			targ_bits[ch] += add_bits[ch];
			extra_bits -= add_bits[ch];
		}

		bits = 0;
		for( int ch = 0; ch < channels_out; ++ch ) {
			bits += targ_bits[ch];
		}
		if( bits > Util.MAX_BITS_PER_GRANULE ) {
			for( int ch = 0; ch < channels_out; ++ch ) {
				targ_bits[ch] *= Util.MAX_BITS_PER_GRANULE;
				targ_bits[ch] /= bits;
			}
		}

		return max_bits;
	}

	static final void reduce_side(final int targ_bits[/*2*/], final float ms_ener_ratio, final int mean_bits, final int max_bits) {
		/*  ms_ener_ratio = 0:  allocate 66/33  mid/side  fac=.33
		 *  ms_ener_ratio =.5:  allocate 50/50 mid/side   fac= 0 */
		/* 75/25 split is fac=.5 */
		/* float fac = .50*(.5-ms_ener_ratio[gr])/.5; */
		float fac = .33f * (.5f - ms_ener_ratio) / .5f;
		if( fac < 0f ) {
			fac = 0f;
		}
		if( fac > .5f ) {
			fac = .5f;
		}

		/* number of bits to move from side channel to mid channel */
		/*    move_bits = fac*targ_bits[1];  */
		int move_bits = (int)(fac * .5f * (targ_bits[0] + targ_bits[1]));

		if( move_bits > Util.MAX_BITS_PER_CHANNEL - targ_bits[0] ) {
			move_bits = Util.MAX_BITS_PER_CHANNEL - targ_bits[0];
		}
		if( move_bits < 0 ) {
			move_bits = 0;
		}

		if( targ_bits[1] >= 125 ) {
			/* dont reduce side channel below 125 bits */
			if( targ_bits[1] - move_bits > 125 ) {

				/* if mid channel already has 2x more than average, dont bother */
				/* mean_bits = bits per granule (for both channels) */
				if( targ_bits[0] < mean_bits ) {
					targ_bits[0] += move_bits;
				}
				targ_bits[1] -= move_bits;
			} else {
				targ_bits[0] += targ_bits[1] - 125;
				targ_bits[1] = 125;
			}
		}

		move_bits = targ_bits[0] + targ_bits[1];
		if( move_bits > max_bits ) {
			targ_bits[0] = (max_bits * targ_bits[0]) / move_bits;
			targ_bits[1] = (max_bits * targ_bits[1]) / move_bits;
		}
	}

	/**
	 *  Robert Hegemann 2001-04-27:
	 *  this adjusts the ATH, keeping the original noise floor
	 *  affects the higher frequencies more than the lower ones
	 */
	static final float athAdjust(final float a, final float x, final float athFloor, final float ATHfixpoint) {
		/*  work in progress */
		final float o = 90.30873362f;
		final float p = (ATHfixpoint < 1.f) ? 94.82444863f : ATHfixpoint;
		float u = ((float)Math.log10( (double)x ) * (10.0f));// Util.FAST_LOG10_X( x, 10.0f );
		final float v = a * a;
		float w = 0.0f;
		u -= athFloor;      /* undo scaling */
		if( v > 1E-20f ) {
			w = 1.f + ((float)Math.log10( (double)v ) * (10.0f / o));// Util.FAST_LOG10_X( v, 10.0f / o );
		}
		if( w < 0 ) {
			w = 0.f;
		}
		u *= w;
		u += athFloor + o - p; /* redo scaling */

		return (float)Math.pow( 10., (double)(0.1f * u) );
	}

	/*************************************************************************/
	/*            calc_xmin                                                  */
	/*************************************************************************/

	/**
	  Calculate the allowed distortion for each scalefactor band,
	  as determined by the psychoacoustic model.
	  xmin(sb) = ratio(sb) * en(sb) / bw(sb)

	  returns number of sfb's with energy > ATH
	*/
	static final int calc_xmin(final InternalFlags gfc,
							   final III_PsyRatio ratio, final III_GrInfo cod_info, final float[] pxmin)
	{
		int ipxmin = 0;// to pxmin
		final SessionConfig cfg = gfc.cfg;
		int j = 0, ath_over = 0;
		final ATH ATH = gfc.ATH;
		final float[] xr = cod_info.xr;
		final float[] ratio_en_l = ratio.en.l;// java
		final float[] longfact = gfc.sv_qnt.longfact;// java
		final int[] cod_info_width = cod_info.width;// java
		final boolean[] energy_above_cutoff = cod_info.energy_above_cutoff;// java

		int gsfb = 0;
		for( final int psy_lmax = cod_info.psy_lmax; gsfb < psy_lmax; gsfb++ ) {
			float xmin = athAdjust( ATH.adjust_factor, ATH.l[gsfb], ATH.floor, cfg.ATHfixpoint );
			xmin *= longfact[gsfb];

			final int width = cod_info_width[gsfb];
			final float rh1 = xmin / width;
			float rh2 = (float) Util.DBL_EPSILON;
			float en0 = 0.0f;
			for( int l = 0; l < width; ++l ) {
				final float xa = xr[j++];
				final float x2 = xa * xa;
				en0 += x2;
				rh2 += (x2 < rh1) ? x2 : rh1;
			}
			if( en0 > xmin ) {
				ath_over++;
			}

			float rh3;
			if( en0 < xmin ) {
				rh3 = en0;
			} else if( rh2 < xmin ) {
				rh3 = xmin;
			} else {
				rh3 = rh2;
			}
			xmin = rh3;
			{
				final float e = ratio_en_l[gsfb];
				if( e > 1e-12f ) {
					float x = en0 * ratio.thm.l[gsfb] / e;
					x *= longfact[gsfb];
					if( xmin < x ) {
						xmin = x;
					}
				}
			}
			xmin = (xmin >= (float) Util.DBL_EPSILON ? xmin : (float) Util.DBL_EPSILON);
			energy_above_cutoff[gsfb] = (en0 > xmin + 1e-14f);
			pxmin[ipxmin++] = xmin;
		}                   /* end of long block loop */

		/*use this function to determine the highest non-zero coeff */
		int max_nonzero = 0;
		for( int k = 575; k > 0; --k ) {
			final float v = xr[k];
			if( (v >= 0 ? v : -v) > 1e-12f ) {
				max_nonzero = k;
				break;
			}
		}
		if( cod_info.block_type != Encoder.SHORT_TYPE ) { /* NORM, START or STOP type, but not SHORT */
			max_nonzero |= 1; /* only odd numbers */
		} else {
			max_nonzero /= 6; /* 3 short blocks */
			max_nonzero *= 6;
			max_nonzero += 5;
		}

		if( ! gfc.sv_qnt.sfb21_extra && cfg.samplerate < 44000 ) {
			final int sfb_l = (cfg.samplerate <= 8000) ? 17 : 21;
			final int sfb_s = (cfg.samplerate <= 8000) ?  9 : 12;
			int limit;// = 575;
			if( cod_info.block_type != Encoder.SHORT_TYPE ) { /* NORM, START or STOP type, but not SHORT */
				limit = gfc.scalefac_band.l[sfb_l] - 1;
			} else {
				limit = 3 * gfc.scalefac_band.s[sfb_s] - 1;
			}
			if( max_nonzero > limit ) {
				max_nonzero = limit;
			}
		}
		cod_info.max_nonzero_coeff = max_nonzero;

		final float[] shortfact = gfc.sv_qnt.shortfact;// java
		final float[][] ratio_en_s = ratio.en.s;// java
		for( int sfb = cod_info.sfb_smin, psymax = cod_info.psymax; gsfb < psymax; sfb++, gsfb += 3 ) {
			float tmpATH = athAdjust( ATH.adjust_factor, ATH.s[sfb], ATH.floor, cfg.ATHfixpoint );
			tmpATH *= shortfact[sfb];

			final int width = cod_info_width[gsfb];
			for( int b = 0; b < 3; b++ ) {
				float en0 = 0.0f, xmin = tmpATH;

				final float rh1 = tmpATH / width;
				float rh2 = (float) Util.DBL_EPSILON;
				for( int l = 0; l < width; ++l ) {
					final float xa = xr[j++];
					final float x2 = xa * xa;
					en0 += x2;
					rh2 += (x2 < rh1) ? x2 : rh1;
				}
				if( en0 > tmpATH ) {
					ath_over++;
				}
				float rh3;
				if( en0 < tmpATH ) {
					rh3 = en0;
				} else if( rh2 < tmpATH ) {
					rh3 = tmpATH;
				} else {
					rh3 = rh2;
				}
				xmin = rh3;
				{
					final float e = ratio_en_s[sfb][b];
					if( e > 1e-12f ) {
						float x = en0 * ratio.thm.s[sfb][b] / e;
						x *= shortfact[sfb];
						if( xmin < x ) {
							xmin = x;
						}
					}
				}
				xmin = (xmin >= (float) Util.DBL_EPSILON ? xmin : (float) Util.DBL_EPSILON);
				energy_above_cutoff[gsfb + b] = (en0 > xmin + 1e-14f);
				pxmin[ipxmin++] = xmin;
			}               /* b */
			if( cfg.use_temporal_masking_effect ) {
				int ipxmin3 = ipxmin - 3;// java
				if( pxmin[ipxmin3] > pxmin[ipxmin3 + 1] ) {
					pxmin[ipxmin3 + 1] += (pxmin[ipxmin3] - pxmin[ipxmin3 + 1]) * gfc.cd_psy.decay;
				}
				ipxmin3++;
				if( pxmin[ipxmin3] > pxmin[ipxmin3 + 1] ) {
					pxmin[ipxmin3 + 1] += (pxmin[ipxmin3] - pxmin[ipxmin3 + 1]) * gfc.cd_psy.decay;
				}
			}
		}                   /* end of short block sfb loop */

		return ath_over;
	}

	private static final float calc_noise_core_c(final III_GrInfo cod_info, final int[] startline, int l, final float step) {
		float noise = 0;
		int j = startline[0];
		final int[] ix = cod_info.l3_enc;
		final float[] xr = cod_info.xr;// java

		if( j > cod_info.count1 ) {
			while( l-- != 0 ) {
				float temp = xr[j];
				j++;
				noise += temp * temp;
				temp = xr[j];
				j++;
				noise += temp * temp;
			}
		} else if( j > cod_info.big_values ) {
			final float ix01[] = { 0, step };
			while( l-- != 0 ) {
				float temp = xr[j];
				if( temp < 0 ) {
					temp = -temp;
				}
				temp -= ix01[ ix[j] ];
				j++;
				noise += temp * temp;
				temp = xr[j];
				if( temp < 0 ) {
					temp = -temp;
				}
				temp -= ix01[ ix[j] ];
				j++;
				noise += temp * temp;
			}
		} else {
			while( l-- != 0 ) {
				float temp = xr[j];
				if( temp < 0 ) {
					temp = -temp;
				}
				temp -= pow43[ ix[j] ] * step;
				j++;
				noise += temp * temp;
				temp = xr[j];
				if( temp < 0 ) {
					temp = -temp;
				}
				temp -= pow43[ ix[j] ] * step;
				j++;
				noise += temp * temp;
			}
		}

		startline[0] = j;
		return noise;
	}

	/*************************************************************************/
	/*            calc_noise                                                 */
	/*************************************************************************/

	/* -oo dB  =>  -1.00 */
	/* - 6 dB  =>  -0.97 */
	/* - 3 dB  =>  -0.80 */
	/* - 2 dB  =>  -0.64 */
	/* - 1 dB  =>  -0.38 */
	/*   0 dB  =>   0.00 */
	/* + 1 dB  =>  +0.49 */
	/* + 2 dB  =>  +1.06 */
	/* + 3 dB  =>  +1.68 */
	/* + 6 dB  =>  +3.69 */
	/* +10 dB  =>  +6.45 */
	static final int calc_noise(final III_GrInfo cod_info,
								final float[] l3_xmin,
								final float[] distort, final CalcNoiseResult res, final CalcNoiseData prev_noise)
	{
		int xoffset = 0;// to l3_xmin
		int doffset = 0;// to distort

		int over = 0;
		float over_noise_db = 0;
		float tot_noise_db = 0; /*    0 dB relative to masking */
		float max_noise = -20.0f; /* -200 dB relative to masking */
		int j = 0;
		final int[] tmpj = new int[1];// java helper
		final int[] scalefac = cod_info.scalefac;
		int soffset = 0;// to scalefac

		res.over_SSD = 0;

		for( int sfb = 0; sfb < cod_info.psymax; sfb++ ) {
			final int s = cod_info.global_gain - ((scalefac[soffset++] + (cod_info.preflag ? pretab[sfb] : 0))
					<< (cod_info.scalefac_scale + 1))
					- (cod_info.subblock_gain[ cod_info.window[sfb] ] << 3);
			final float r_l3_xmin = 1.f / l3_xmin[xoffset++];
			float distort_ = 0.0f;
			float noise = 0.0f;

			if( prev_noise != null && (prev_noise.step[sfb] == s) ) {
				/* use previously computed values */
				j += cod_info.width[sfb];
				distort_ = r_l3_xmin * prev_noise.noise[sfb];

				noise = prev_noise.noise_log[sfb];
			} else {
				final float step = POW20( s );
				int l = cod_info.width[sfb] >> 1;

				if( (j + cod_info.width[sfb]) > cod_info.max_nonzero_coeff ) {
					final int usefullsize = cod_info.max_nonzero_coeff - j + 1;
					if( usefullsize > 0 ) {
						l = usefullsize >> 1;
					} else {
						l = 0;
					}
				}

				tmpj[0] = j;
				noise = calc_noise_core_c( cod_info, tmpj, l, step );
				j = tmpj[0];

				if( prev_noise != null ) {
					/* save noise values */
					prev_noise.step[sfb] = s;
					prev_noise.noise[sfb] = noise;
				}

				distort_ = r_l3_xmin * noise;

				/* multiplying here is adding in dB, but can overflow */
				noise = (float)Math.log10( (double)(distort_ >= 1E-20f ? distort_ : 1E-20f) );// Util.FAST_LOG10( (distort_ >= 1E-20f ? distort_ : 1E-20f) );

				if( prev_noise != null ) {
					/* save noise values */
					prev_noise.noise_log[sfb] = noise;
				}
			}
			distort[doffset++] = distort_;

			if( prev_noise != null ) {
				/* save noise values */
				prev_noise.global_gain = cod_info.global_gain;
			}

			/*tot_noise *= Max(noise, 1E-20); */
			tot_noise_db += noise;

			if( noise > 0.0f ) {
				int tmp = (int) (noise * 10 + .5f);
				tmp = (tmp >= 1 ? tmp : 1);
				res.over_SSD += tmp * tmp;

				over++;
				/* multiplying here is adding in dB -but can overflow */
				/*over_noise *= noise; */
				over_noise_db += noise;
			}
			max_noise = (max_noise >= noise ? max_noise : noise);
		}

		res.over_count = over;
		res.tot_noise = tot_noise_db;
		res.over_noise = over_noise_db;
		res.max_noise = max_noise;

		return over;
	}
}