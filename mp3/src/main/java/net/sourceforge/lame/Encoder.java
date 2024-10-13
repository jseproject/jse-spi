/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 1999-2010 The LAME Project
 * Copyright (c) 1999-2008 JavaZOOM
 * Copyright (c) 2001-2002 Naoki Shibata
 * Copyright (c) 2001 Jonathan Dee
 * Copyright (c) 2000-2017 Robert Hegemann
 * Copyright (c) 2000-2008 Gabriel Bouvigne
 * Copyright (c) 2000-2005 Alexander Leidinger
 * Copyright (c) 2000 Don Melton
 * Copyright (c) 1999-2005 Takehiro Tominaga
 * Copyright (c) 1999-2001 Mark Taylor
 * Copyright (c) 1999 Albert L. Faber
 * Copyright (c) 1988, 1993 Ron Mayer
 * Copyright (c) 1998 Michael Cheng
 * Copyright (c) 1997 Jeff Tsay
 * Copyright (c) 1995-1997 Michael Hipp
 * Copyright (c) 1993-1994 Tobias Bading,
 *                         Berlin University of Technology
 *
 * - This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * - This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * - You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package net.sourceforge.lame;

// encoder.h

class Encoder {
	/***********************************************************************
	*
	*  encoder and decoder delays
	*
	***********************************************************************/

	/*
	 * layer III enc.dec delay:  1056 (1057?)   (observed)
	 * layer  II enc.dec delay:   480  (481?)   (observed)
	 *
	 * polyphase 256-16             (dec or enc)        = 240
	 * mdct      256+32  (9*32)     (dec or enc)        = 288
	 * total:    512+16
	 *
	 * My guess is that delay of polyphase filterbank is actualy 240.5
	 * (there are technical reasons for this, see postings in mp3encoder).
	 * So total Encode+Decode delay = ENCDELAY + 528 + 1
	 */

	/*
	 * ENCDELAY  The encoder delay.
	 *
	 * Minimum allowed is MDCTDELAY (see below)
	 *
	 * The first 96 samples will be attenuated, so using a value less than 96
	 * will result in corrupt data for the first 96-ENCDELAY samples.
	 *
	 * suggested: 576
	 * set to 1160 to sync with FhG.
	 */

	static final int ENCDELAY = 576;

	/*
	 * make sure there is at least one complete frame after the
	 * last frame containing real data
	 *
	 * Using a value of 288 would be sufficient for a
	 * a very sophisticated decoder that can decode granule-by-granule instead
	 * of frame by frame.  But lets not assume this, and assume the decoder
	 * will not decode frame N unless it also has data for frame N+1
	 *
	 */
	/*#define POSTDELAY   288*/
	static final int POSTDELAY = 1152;

	/*
	 * delay of the MDCT used in mdct.c
	 * original ISO routines had a delay of 528!
	 * Takehiro's routines:
	 */
	static final int MDCTDELAY = 48;
	static final int FFTOFFSET = (224 + MDCTDELAY);

	/**
	 * Most decoders, including the one we use, have a delay of 528 samples.
	 */
	static final int DECDELAY  = 528;

	/** number of subbands */
	static final int SBLIMIT   = 32;

	/** parition bands bands */
	static final int CBANDS    = 64;

	/** number of critical bands/scale factor bands where masking is computed*/
	static final int SBPSY_l   = 21;
	static final int SBPSY_s   = 12;

	/** total number of scalefactor bands encoded */
	static final int SBMAX_l   = 22;
	static final int SBMAX_s   = 13;
	static final int PSFB21    = 6;
	static final int PSFB12    = 6;

	/** max scalefactor band, max(SBMAX_l, SBMAX_s*3, (SBMAX_s-3)*3+8) */
	static final int SFBMAX = (SBMAX_s * 3);

	/* FFT sizes */
	static final int BLKSIZE    = 1024;
	static final int HBLKSIZE   = (BLKSIZE/2 + 1);
	static final int BLKSIZE_s  = 256;
	static final int HBLKSIZE_s = (BLKSIZE_s/2 + 1);


	/* #define switch_pe        1800 */
	static final int NORM_TYPE  = 0;
	static final int START_TYPE = 1;
	static final int SHORT_TYPE = 2;
	static final int STOP_TYPE  = 3;

	/*
	 * Mode Extention:
	 * When we are in stereo mode, there are 4 possible methods to store these
	 * two channels. The stereo modes -m? are using a subset of them.
	 *
	 *  -ms: MPG_MD_LR_LR
	 *  -mj: MPG_MD_LR_LR and MPG_MD_MS_LR
	 *  -mf: MPG_MD_MS_LR
	 *  -mi: all
	 */
	/*
	#define MPG_MD_LR_LR  0
	#define MPG_MD_LR_I   1
	#define MPG_MD_MS_LR  2
	#define MPG_MD_MS_I   3
	*/
	//enum MPEGChannelMode {
	private static final int MPG_MD_LR_LR = 0;
	//private static final int MPG_MD_LR_I  = 1;// FIXME never uses MPG_MD_LR_I
	static final int MPG_MD_MS_LR = 2;
	//private static final int MPG_MD_MS_I  = 3;// FIXME never uses MPG_MD_MS_I
	//};

	/**
	 * auto-adjust of ATH, useful for low volume
	 * Gabriel Bouvigne 3 feb 2001
	 *
	 * modifies some values in
	 *   gfp.internal_flags.ATH
	 *   (gfc.ATH)
	 */
	private static final void adjust_ATH(final LAME_InternalFlags gfc) {
		final ATH ath = gfc.ATH;// java
		if( ath.use_adjust == 0 ) {
			ath.adjust_factor = 1.0f; /* no adjustment */
			return;
		}

		final SessionConfig cfg = gfc.cfg;
		/* jd - 2001 mar 12, 27, jun 30 */
		/* loudness based on equal loudness curve; */
		/* use granule with maximum combined loudness */
		float max_pow = gfc.ov_psy.loudness_sq[0][0];
		float gr2_max = gfc.ov_psy.loudness_sq[1][0];
		if( cfg.channels_out == 2 ) {
			max_pow += gfc.ov_psy.loudness_sq[0][1];
			gr2_max += gfc.ov_psy.loudness_sq[1][1];
		} else {
			max_pow += max_pow;
			gr2_max += gr2_max;
		}
		if( cfg.mode_gr == 2 ) {
			max_pow = (max_pow >= gr2_max ? max_pow : gr2_max);
		}
		max_pow *= 0.5f;     /* max_pow approaches 1.0 for full band noise */

		/* jd - 2001 mar 31, jun 30 */
		/* user tuning of ATH adjustment region */
		max_pow *= ath.aa_sensitivity_p;

		/*  adjust ATH depending on range of maximum value
		*/

		/* jd - 2001 feb27, mar12,20, jun30, jul22 */
		/* continuous curves based on approximation */
		/* to GB's original values. */
		/* For an increase in approximate loudness, */
		/* set ATH adjust to adjust_limit immediately */
		/* after a delay of one frame. */
		/* For a loudness decrease, reduce ATH adjust */
		/* towards adjust_limit gradually. */
		/* max_pow is a loudness squared or a power. */
		if( max_pow > 0.03125 ) { /* ((1 - 0.000625)/ 31.98) from curve below */
			if( ath.adjust_factor >= 1.0f ) {
				ath.adjust_factor = 1.0f;
			} else {
				/* preceding frame has lower ATH adjust; */
				/* ascend only to the preceding adjust_limit */
				/* in case there is leading low volume */
				if( ath.adjust_factor < ath.adjust_limit ) {
					ath.adjust_factor = ath.adjust_limit;
				}
			}
			ath.adjust_limit = 1.0f;
		} else {              /* adjustment curve */
			/* about 32 dB maximum adjust (0.000625) */
			final float adj_lim_new = 31.98f * max_pow + 0.000625f;
			if( ath.adjust_factor >= adj_lim_new ) { /* descend gradually */
				ath.adjust_factor *= adj_lim_new * 0.075f + 0.925f;
				if( ath.adjust_factor < adj_lim_new ) { /* stop descent */
					ath.adjust_factor = adj_lim_new;
				}
			} else {          /* ascend */
				if( ath.adjust_limit >= adj_lim_new ) {
					ath.adjust_factor = adj_lim_new;
				} else {      /* preceding frame has lower ATH adjust; */
					/* ascend only to the preceding adjust_limit */
					if( ath.adjust_factor < ath.adjust_limit ) {
						ath.adjust_factor = ath.adjust_limit;
					}
				}
			}
			ath.adjust_limit = adj_lim_new;
		}
	}

	/***********************************************************************
	 *
	 *  some simple statistics
	 *
	 *  bitrate index 0: free bitrate . not allowed in VBR mode
	 *  : bitrates, kbps depending on MPEG version
	 *  bitrate index 15: forbidden
	 *
	 *  mode_ext:
	 *  0:  LR
	 *  1:  LR-i
	 *  2:  MS
	 *  3:  MS-i
	 *
	 ***********************************************************************/
	private static final void updateStats(final LAME_InternalFlags gfc ) {
		final SessionConfig cfg = gfc.cfg;
		final EncResult eov = gfc.ov_enc;
		final int bitrate_index = eov.bitrate_index;// java
		/* count bitrate indices */
		eov.bitrate_channelmode_hist[bitrate_index][4]++;
		eov.bitrate_channelmode_hist[15][4]++;

		/* count 'em for every mode extension in case of 2 channel encoding */
		final int channels_out = cfg.channels_out;// java
		if( channels_out == 2 ) {
			eov.bitrate_channelmode_hist[bitrate_index][eov.mode_ext]++;
			eov.bitrate_channelmode_hist[15][eov.mode_ext]++;
		}
		final III_GrInfo[][] tt = gfc.l3_side.tt;// java
		final int[][] bitrate_blocktype_hist = eov.bitrate_blocktype_hist;// java
		for( int gr = 0; gr < cfg.mode_gr; ++gr ) {
			final III_GrInfo[] tt_gr = tt[gr];// java
			int ch = 0;
			do {
				int bt = tt_gr[ch].block_type;
				if( tt_gr[ch].mixed_block_flag ) {
					bt = 4;
				}
				bitrate_blocktype_hist[bitrate_index][bt]++;
				bitrate_blocktype_hist[bitrate_index][5]++;
				bitrate_blocktype_hist[15][bt]++;
				bitrate_blocktype_hist[15][5]++;
			} while( ++ch < channels_out );
		}
	}

	private static final void lame_encode_frame_init(final LAME_InternalFlags gfc, final float inbuf[/* 2 */][] ) {
		final SessionConfig cfg = gfc.cfg;

		if( ! gfc.lame_encode_frame_init ) {
			final float primebuff0[] = new float[286 + 1152 + 576];// java: already zeroed
			final float primebuff1[] = new float[286 + 1152 + 576];// java: already zeroed
			final int mode_gr = cfg.mode_gr;// java
			final int framesize = 576 * mode_gr;
			final int channels_out = cfg.channels_out;// java
			/* prime the MDCT/polyphase filterbank with a short block */
			gfc.lame_encode_frame_init = true;
			for( int i = 0, j = 0, ie = 286 + 576 * (1 + mode_gr); i < ie; ++i ) {
				if( i < framesize ) {
					primebuff0[i] = 0;
					if( channels_out == 2 ) {
						primebuff1[i] = 0;
					}
				} else {
					primebuff0[i] = inbuf[0][j];
					if( channels_out == 2 ) {
						primebuff1[i] = inbuf[1][j];
					}
					++j;
				}
			}
			/* polyphase filtering / mdct */
			final III_GrInfo[][] tt = gfc.l3_side.tt;// java
			for( int gr = 0; gr < mode_gr; gr++ ) {
				final III_GrInfo[] tt_gr = tt[gr];// java
				int ch = 0;
				do {
					tt_gr[ch].block_type = Encoder.SHORT_TYPE;
				} while( ++ch < channels_out );
			}
			NewMDCT.mdct_sub48( gfc, primebuff0, primebuff1 );
		}
	}

	/************************************************************************
	*
	* encodeframe()           Layer 3
	*
	* encode a single frame
	*
	************************************************************************
	lame_encode_frame()


	                       gr 0            gr 1
	inbuf:           |--------------|--------------|--------------|


	Polyphase (18 windows, each shifted 32)
	gr 0:
	window1          <----512---.
	window18                 <----512---.

	gr 1:
	window1                         <----512---.
	window18                                <----512---.



	MDCT output:  |--------------|--------------|--------------|

	FFT's                    <---------1024---------.
	                                         <---------1024-------.



	    inbuf = buffer of PCM data size=MP3 framesize
	    encoder acts on inbuf[ch][0], but output is delayed by MDCTDELAY
	    so the MDCT coefficints are from inbuf[ch][-MDCTDELAY]

	    psy-model FFT has a 1 granule delay, so we feed it data for the
	    next granule.
	    FFT is centered over granule:  224+576+224
	    So FFT starts at:   576-224-MDCTDELAY

	    MPEG2:  FFT ends at:  BLKSIZE+576-224-MDCTDELAY      (1328)
	    MPEG1:  FFT ends at:  BLKSIZE+2*576-224-MDCTDELAY    (1904)

	    MPEG2:  polyphase first window:  [0..511]
	                      18th window:   [544..1055]          (1056)
	    MPEG1:            36th window:   [1120..1631]         (1632)
	            data needed:  512+framesize-32

	    A close look newmdct.c shows that the polyphase filterbank
	    only uses data from [0..510] for each window.  Perhaps because the window
	    used by the filterbank is zero for the last point, so Takehiro's
	    code doesn't bother to compute with it.

	    FFT starts at 576-224-MDCTDELAY (304)  = 576-FFTOFFSET

	*/

	// typedef float chgrdata[2][2];

	private static final float fircoef[] = {// [9] = {
			-0.0207887f * 5, -0.0378413f * 5, -0.0432472f * 5, -0.031183f * 5,
			7.79609e-18f * 5, 0.0467745f * 5, 0.10091f * 5, 0.151365f * 5,
			0.187098f * 5
		};
	/**
	 *
	 * @param gfc Context
	 * @param inbuf_l Input
	 * @param inbuf_r Input
	 * @param mp3buf Output
	 * @param mp3buf_size
	 * @return
	 */
	static final int lame_encode_mp3_frame(final LAME_InternalFlags gfc, final float[] inbuf_l, final float[] inbuf_r, final byte[] mp3buf, final int mp3buf_offset, final int mp3buf_size)
	{
		final SessionConfig cfg = gfc.cfg;
		final III_PsyRatio masking_LR[][] = new III_PsyRatio[2][2]; /*LR masking & energy */
		masking_LR[0][0] = new III_PsyRatio();
		masking_LR[0][1] = new III_PsyRatio();
		masking_LR[1][0] = new III_PsyRatio();
		masking_LR[1][1] = new III_PsyRatio();
		final III_PsyRatio masking_MS[][] = new III_PsyRatio[2][2]; /*MS masking & energy */
		masking_MS[0][0] = new III_PsyRatio();
		masking_MS[0][1] = new III_PsyRatio();
		masking_MS[1][0] = new III_PsyRatio();
		masking_MS[1][1] = new III_PsyRatio();
		III_PsyRatio masking[][] = new III_PsyRatio[2][]; /*pointer to selected maskings */
		final float inbuf[][] = new float[2][];

		final float tot_ener[][] = new float[2][4];
		final float ms_ener_ratio[/* 2 */] = { .5f, .5f };
		final float pe[][] = new float[2][2];// = { {0.f, 0.f}, {0.f, 0.f} };// java: already zeroed
		final float pe_MS[][] = new float[2][2];// = { {0.f, 0.f}, {0.f, 0.f}};// java: already zeroed
		float pe_use[][] = new float[2][];

		inbuf[0] = inbuf_l;
		inbuf[1] = inbuf_r;

		if( ! gfc.lame_encode_frame_init ) {
			/*first run? */
			lame_encode_frame_init( gfc, inbuf );
		}
		/********************** padding *****************************/
		/* padding method as described in
		 * "MPEG-Layer3 / Bitstream Syntax and Decoding"
		 * by Martin Sieler, Ralph Sperschneider
		 *
		 * note: there is no padding for the very first frame
		 *
		 * Robert Hegemann 2000-06-22
		 */
		gfc.ov_enc.padding = false;
		if( (gfc.sv_enc.slot_lag -= gfc.sv_enc.frac_SpF) < 0 ) {
			gfc.sv_enc.slot_lag += cfg.samplerate;
			gfc.ov_enc.padding = true;
		}

		final int mode_gr = cfg.mode_gr;// java
		final int channels_out = cfg.channels_out;// java
		/****************************************
		*   Stage 1: psychoacoustic model       *
		****************************************/
		{
			/* psychoacoustic model
			 * psy model has a 1 granule (576) delay that we must compensate for
			 * (mt 6/99).
			 */
			// final int bufp;// {0, 0}; /* address of beginning of left & right granule */
			final int blocktype[] = new int[2];

			for( int gr = 0; gr < mode_gr; gr++ ) {

				// for( ch = 0; ch < cfg.channels_out; ch++ ) {
					final int bufp = (576 - FFTOFFSET) + gr * 576;// &inbuf[ch][576 + gr * 576 - FFTOFFSET];
				//}
				final float[] tot_ener_gr = tot_ener[gr];// java
				final int ret = PsyModel.L3psycho_anal_vbr( gfc, inbuf, bufp, gr,
						masking_LR, masking_MS,
						pe[gr], pe_MS[gr], tot_ener_gr, blocktype );
				if( ret != 0 ) {
					return -4;
				}

				if( cfg.mode == LAME.JOINT_STEREO ) {
					ms_ener_ratio[gr] = tot_ener_gr[2] + tot_ener_gr[3];
					if( ms_ener_ratio[gr] > 0 ) {
						ms_ener_ratio[gr] = tot_ener_gr[3] / ms_ener_ratio[gr];
					}
				}

				/* block type flags */
				int ch = 0;
				do {
					final III_GrInfo cod_info = gfc.l3_side.tt[gr][ch];
					cod_info.block_type = blocktype[ch];
					cod_info.mixed_block_flag = false;
				} while( ++ch < channels_out );
			}
		}

		/* auto-adjust of ATH, useful for low volume */
		adjust_ATH( gfc );

		/****************************************
		*   Stage 2: MDCT                       *
		****************************************/

		/* polyphase filtering / mdct */
		NewMDCT.mdct_sub48( gfc, inbuf[0], inbuf[1] );

		/****************************************
		*   Stage 3: MS/LR decision             *
		****************************************/

		/* Here will be selected MS or LR coding of the 2 stereo channels */
		gfc.ov_enc.mode_ext = MPG_MD_LR_LR;

		if( cfg.force_ms ) {
			gfc.ov_enc.mode_ext = MPG_MD_MS_LR;
		} else if( cfg.mode == LAME.JOINT_STEREO ) {
			/* ms_ratio = is scaled, for historical reasons, to look like
			   a ratio of side_channel / total.
			   0 = signal is 100% mono
			   .5 = L & R uncorrelated
			 */

			/* [0] and [1] are the results for the two granules in MPEG-1,
			 * in MPEG-2 it's only a faked averaging of the same value
			 * _prev is the value of the last granule of the previous frame
			 * _next is the value of the first granule of the next frame
			 */

			float sum_pe_MS = 0;
			float sum_pe_LR = 0;
			for( int gr = 0; gr < mode_gr; gr++ ) {
				final float[] pe_MS_gr = pe_MS[gr];// java
				final float[] pe_gr = pe[gr];// java
				int ch = 0;
				do {
					sum_pe_MS += pe_MS_gr[ch];
					sum_pe_LR += pe_gr[ch];
				} while( ++ch < channels_out );
			}

			/* based on PE: M/S coding would not use much more bits than L/R */
			if( sum_pe_MS <= /*1.00f * */sum_pe_LR ) {
				final III_GrInfo[][] tt = gfc.l3_side.tt;// java
				final III_GrInfo[] gi0 = tt[0];//[0];
				final III_GrInfo[] gi1 = tt[mode_gr - 1];//[0];

				if( gi0[0].block_type == gi0[1].block_type && gi1[0].block_type == gi1[1].block_type ) {
					gfc.ov_enc.mode_ext = MPG_MD_MS_LR;
				}
			}
		}

		/* bit and noise allocation */
		if( gfc.ov_enc.mode_ext == MPG_MD_MS_LR ) {
			masking = masking_MS; /* use MS masking */
			pe_use = pe_MS;
		} else {
			masking = masking_LR; /* use LR masking */
			pe_use = pe;
		}

		/****************************************
		*   Stage 4: quantization loop          *
		****************************************/

		if( cfg.vbr == LAME.vbr_off || cfg.vbr == LAME.vbr_abr ) {
			final float[] pefirbuf = gfc.sv_enc.pefirbuf;// java
			int i = 0;
			do {
				pefirbuf[i] = pefirbuf[i + 1];
			} while( ++i < 18 );

			float f = 0.0f;
			for( int gr = 0; gr < mode_gr; gr++) {
				for( int ch = 0; ch < channels_out; ch++) {
					f += pe_use[gr][ch];
				}
			}
			pefirbuf[18] = f;

			f = pefirbuf[9];
			i = 0;
			do {
				f += (pefirbuf[i] + pefirbuf[18 - i]) * fircoef[i];
			} while( ++i < 9 );

			f = (670 * 5 * mode_gr * channels_out) / f;
			for( int gr = 0; gr < mode_gr; gr++ ) {
				int ch = 0;
				do {
					pe_use[gr][ch] *= f;
				} while( ++ch < channels_out );
			}
		}
		switch( cfg.vbr )
		{
		default:
		case LAME.vbr_off:
			CBRIterationLoop.iteration( gfc, pe_use, ms_ener_ratio, masking );
			break;
		case LAME.vbr_abr:
			ABRIterationLoop.iteration( gfc, pe_use, ms_ener_ratio, masking );
			break;
		case LAME.vbr_rh:
			VBRIterationLoop.iteration( gfc, pe_use, ms_ener_ratio, masking );
			break;
		case LAME.vbr_mt:
		case LAME.vbr_mtrh:
			VBRNewIterationLoop.iteration( gfc, pe_use, ms_ener_ratio, masking );
			break;
		}

		/****************************************
		*   Stage 5: bitstream formatting       *
		****************************************/

		/*  write the frame to the bitstream  */
		Bitstream.format_bitstream( gfc );

		/* copy mp3 bit buffer into array */
		final int mp3count = Bitstream.copy_buffer( gfc, mp3buf, mp3buf_offset, mp3buf_size, true );

		if( cfg.write_lame_tag ) {
			VBRTag.AddVbrFrame( gfc );
		}

		++gfc.ov_enc.frame_number;

		updateStats( gfc );

		return mp3count;
	}
}