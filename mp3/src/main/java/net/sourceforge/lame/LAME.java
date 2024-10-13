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

import java.io.IOException;
import java.io.RandomAccessFile;

/* -*- mode: C; mode: fold -*- */

// lame.c

public class LAME {

	private static final int LAME_DEFAULT_QUALITY = 3;

	/** maximum size of albumart image (128KB), which affects LAME_MAXMP3BUFFER
	   as well since lame_encode_buffer() also returns ID3v2 tag data */
	private static final int LAME_MAXALBUMART  = (128 * 1024);

	/** maximum size of mp3buffer needed if you encode at most 1152 samples for
	   each call to lame_encode_buffer.  see lame_encode_buffer() below
	   (LAME_MAXMP3BUFFER is now obsolete)  */
	static final int LAME_MAXMP3BUFFER = (16384 + LAME_MAXALBUMART);

	// typedef enum vbr_mode_e {
		public static final int vbr_off = 0;
		/** obsolete, same as vbr_mtrh */
		public static final int vbr_mt = 1;
		public static final int vbr_rh = 2;
		public static final int vbr_abr = 3;
		public static final int vbr_mtrh = 4;
		/** Don't use this! It's used for sanity checks. */
		static final int vbr_max_indicator = 5;
		/** change this to change the default VBR mode of LAME */
		public static final int vbr_default = vbr_mtrh;
	// }
		/* MPEG modes */
	//typedef enum MPEG_mode_e {
		public static final int STEREO = 0;
		public static final int JOINT_STEREO = 1;
		/** LAME doesn't supports this! */
		public static final int DUAL_CHANNEL = 2;
		public static final int MONO = 3;
		static final int NOT_SET = 4;
		/** Don't use this! It's used for sanity checks. */
		static final int MAX_INDICATOR = 5;
	//} MPEG_mode;

		/* Padding types */
	// typedef enum Padding_type_e {// FIXME never uses Padding_type_e
		//private static final int PAD_NO = 0;
		//private static final int PAD_ALL = 1;
		//private static final int PAD_ADJUST = 2;
		/** Don't use this! It's used for sanity checks. */
		//private static final int PAD_MAX_INDICATOR = 3;
	// } Padding_type;

	/*presets*/
	// typedef enum preset_mode_e {
		/*values from 8 to 320 should be reserved for abr bitrates*/
		/*for abr I'd suggest to directly use the targeted bitrate as a value*/
		//private static final int ABR_8 = 8;
		//private static final int ABR_320 = 320;

		static final int V9 = 410; /*Vx to match Lame and VBR_xx to match FhG*/
		//private static final int VBR_10 = 410;
		static final int V8 = 420;
		//private static final int VBR_20 = 420;
		static final int V7 = 430;
		//private static final int VBR_30 = 430;
		static final int V6 = 440;
		//private static final int VBR_40 = 440;
		static final int V5 = 450;
		//private static final int VBR_50 = 450;
		static final int V4 = 460;
		//private static final int VBR_60 = 460;
		static final int V3 = 470;
		//private static final int VBR_70 = 470;
		static final int V2 = 480;
		//private static final int VBR_80 = 480;
		static final int V1 = 490;
		//private static final int VBR_90 = 490;
		static final int V0 = 500;
		//private static final int VBR_100 = 500;

		/*still there for compatibility*/
		public static final int R3MIX = 1000;
		static final int STANDARD = 1001;
		static final int EXTREME = 1002;
		public static final int INSANE = 1003;
		static final int STANDARD_FAST = 1004;
		static final int EXTREME_FAST = 1005;
		static final int MEDIUM = 1006;
		static final int MEDIUM_FAST = 1007;
	//} preset_mode;

	/* psychoacoustic model */
	/*typedef enum Psy_model_e {// FIXME never uses Psy_model_e
		private static final int PSY_GPSYCHO = 1;
		private static final int PSY_NSPSYTUNE = 2;
	//} Psy_model;*/

	/* buffer considerations */
	//typedef enum buffer_constraint_e {
		public static final int MDB_DEFAULT = 0;
		public static final int MDB_STRICT_ISO = 1;
		public static final int MDB_MAXIMUM = 2;
	//} buffer_constraint;

	private static final float filter_coef(final float x ) {
		if( x > 1.0f ) {
			return 0.0f;
		}
		if( x <= 0.0f ) {
			return 1.0f;
		}

		return (float)Math.cos( Math.PI / 2. * x  );
	}

	private static final void lame_init_params_ppflt(final LAME_InternalFlags gfc ) {
		final SessionConfig cfg = gfc.cfg;

		/***************************************************************/
		/* compute info needed for polyphase filter (filter type==0, default) */
		/***************************************************************/

		int lowpass_band = 32;
		int highpass_band = -1;

		if( cfg.lowpass1 > 0 ) {
			int minband = 999;
			for( int band = 0; band <= 31; band++ ) {
				final float freq = band / 31.0f;
				/* this band and above will be zeroed: */
				if( freq >= cfg.lowpass2 ) {
					lowpass_band = (lowpass_band <= band ? lowpass_band : band);
				}
				if( cfg.lowpass1 < freq && freq < cfg.lowpass2 ) {
					minband = (minband <= band ? minband : band);
				}
			}

			/* compute the *actual* transition band implemented by
			 * the polyphase filter */
			if( minband == 999 ) {
				cfg.lowpass1 = (lowpass_band - .75f) / 31.0f;
			} else {
				cfg.lowpass1 = (minband - .75f) / 31.0f;
			}
			cfg.lowpass2 = lowpass_band / 31.0f;
		}

		/* make sure highpass filter is within 90% of what the effective
		 * highpass frequency will be */
		if( cfg.highpass2 > 0 ) {
			if( cfg.highpass2 < .9f * (.75f / 31.0f) ) {
				cfg.highpass1 = 0;
				cfg.highpass2 = 0;
				//System.err.print("Warning: highpass filter disabled. highpass frequency too small\n");
			}
		}

		if( cfg.highpass2 > 0 ) {
			int maxband = -1;
			for( int band = 0; band <= 31; band++ ) {
				final float freq = band / 31.0f;
				/* this band and below will be zereod */
				if( freq <= cfg.highpass1 ) {
					highpass_band = (highpass_band >= band ? highpass_band : band);
				}
				if( cfg.highpass1 < freq && freq < cfg.highpass2 ) {
					maxband = (maxband >= band ? maxband : band);
				}
			}
			/* compute the *actual* transition band implemented by
			* the polyphase filter */
			cfg.highpass1 = highpass_band / 31.0f;
			if( maxband == -1 ) {
				cfg.highpass2 = (highpass_band + .75f) / 31.0f;
			} else {
				cfg.highpass2 = (maxband + .75f) / 31.0f;
			}
		}

		for( int band = 0; band < 32; band++ ) {
			float fc1, fc2;
			final float freq = band / 31.0f;
			if( cfg.highpass2 > cfg.highpass1 ) {
				fc1 = filter_coef( (cfg.highpass2 - freq) / (cfg.highpass2 - cfg.highpass1 + 1e-20f) );
			} else {
				fc1 = 1.0f;
			}
			if( cfg.lowpass2 > cfg.lowpass1 ) {
				fc2 = filter_coef( (freq - cfg.lowpass1)  / (cfg.lowpass2 - cfg.lowpass1 + 1e-20f) );
			} else {
				fc2 = 1.0f;
			}
			gfc.sv_enc.amp_filter[band] = fc1 * fc2;
		}
	}

	private static final class BandPass {
		//private final int bitrate;     /* only indicative value */
		private final int lowpass;
		//
		private BandPass(/*final int br,*/ final int lp) {
			//bitrate = br;
			lowpass = lp;
		}
	}
	private static final BandPass freq_map[] = {
			new BandPass( 2000 ),// new BandPass( 8, 2000 ),
			new BandPass( 3700 ),// new BandPass( 16, 3700 ),
			new BandPass( 3900 ),// new BandPass( 24, 3900 ),
			new BandPass( 5500 ),// new BandPass( 32, 5500 ),
			new BandPass( 7000 ),// new BandPass( 40, 7000 ),
			new BandPass( 7500 ),// new BandPass( 48, 7500 ),
			new BandPass( 10000 ),// new BandPass( 56, 10000 ),
			new BandPass( 11000 ),// new BandPass( 64, 11000 ),
			new BandPass( 13500 ),// new BandPass( 80, 13500 ),
			new BandPass( 15100 ),// new BandPass( 96, 15100 ),
			new BandPass( 15600 ),// new BandPass( 112, 15600 ),
			new BandPass( 17000 ),// new BandPass( 128, 17000 ),
			new BandPass( 17500 ),// new BandPass( 160, 17500 ),
			new BandPass( 18600 ),// new BandPass( 192, 18600 ),
			new BandPass( 19400 ),// new BandPass( 224, 19400 ),
			new BandPass( 19700 ),// new BandPass( 256, 19700 ),
			new BandPass( 20500 )// new BandPass( 320, 20500 )
		};
	/**
	 *
	 * @param bitrate Input. total bitrate in kbps
	 * @return java: lowerlimit, best lowpass frequency limit for input filter in Hz
	 */
	private static final double optimum_bandwidth(/*final double[] lowerlimit, final double[] upperlimit,*/ final int bitrate ) {
		final int table_index = Util.nearestBitrateFullIndex( bitrate );

		//(void) freq_map[table_index].bitrate;
		// *lowerlimit = freq_map[table_index].lowpass;
		return freq_map[table_index].lowpass;

		/*
		 *  Now we try to choose a good high pass filtering frequency.
		 *  This value is currently not used.
		 *    For fu < 16 kHz:  sqrt(fu*fl) = 560 Hz
		 *    For fu = 18 kHz:  no high pass filtering
		 *  This gives:
		 *
		 *   2 kHz => 160 Hz
		 *   3 kHz => 107 Hz
		 *   4 kHz =>  80 Hz
		 *   8 kHz =>  40 Hz
		 *  16 kHz =>  20 Hz
		 *  17 kHz =>  10 Hz
		 *  18 kHz =>   0 Hz
		 *
		 *  These are ad hoc values and these can be optimized if a high pass is available.
		 */
		/* if( f_low <= 16000)
	        f_high = 16000. * 20. / f_low;
	    else if( f_low <= 18000)
	        f_high = 180. - 0.01 * f_low;
	    else
	        f_high = 0.;*/

	    /*
	     *  When we sometimes have a good highpass filter, we can add the highpass
	     *  frequency to the lowpass frequency
	     */

	    /*if( upperlimit != NULL)
	     *upperlimit = f_high;*/
	    // upperlimit;
	}

	/* set internal feature flags.  USER should not access these since
	 * some combinations will produce strange results */
	private static final void lame_init_qval(final LAME_GlobalFlags gfp) {
		final LAME_InternalFlags gfc = gfp.internal_flags;
		final SessionConfig cfg = gfc.cfg;

		switch( gfp.quality ) {
		default:
		case 9:            /* no psymodel, no noise shaping */
			cfg.noise_shaping = 0;
			cfg.noise_shaping_amp = 0;
			cfg.noise_shaping_stop = 0;
			cfg.use_best_huffman = 0;
			cfg.full_outer_loop = 0;
			break;

		case 8:
			gfp.quality = 7;
			/*lint --fallthrough */
		case 7:            /* use psymodel (for short block and m/s switching), but no noise shapping */
			cfg.noise_shaping = 0;
			cfg.noise_shaping_amp = 0;
			cfg.noise_shaping_stop = 0;
			cfg.use_best_huffman = 0;
			 cfg.full_outer_loop = 0;
			if( cfg.vbr == vbr_mt || cfg.vbr == vbr_mtrh ) {
				cfg.full_outer_loop = -1;
			}
			break;

		case 6:
			if( cfg.noise_shaping == 0 ) {
				cfg.noise_shaping = 1;
			}
			cfg.noise_shaping_amp = 0;
			cfg.noise_shaping_stop = 0;
			/* if( cfg.subblock_gain == -1 ) {
				cfg.subblock_gain = true;
			}*/
			cfg.use_best_huffman = 0;
			cfg.full_outer_loop = 0;
			break;

		case 5:
			if( cfg.noise_shaping == 0 ) {
				cfg.noise_shaping = 1;
			}
			cfg.noise_shaping_amp = 0;
			cfg.noise_shaping_stop = 0;
			/* if( cfg.subblock_gain == -1 ) {
				cfg.subblock_gain = true;
			} */
			cfg.use_best_huffman = 0;
			cfg.full_outer_loop = 0;
			break;

		case 4:
			if( cfg.noise_shaping == 0 ) {
				cfg.noise_shaping = 1;
			}
			cfg.noise_shaping_amp = 0;
			cfg.noise_shaping_stop = 0;
			/* if( cfg.subblock_gain == -1 ) {
				cfg.subblock_gain = true;
			} */
			cfg.use_best_huffman = 1;
			cfg.full_outer_loop = 0;
			break;

		case 3:
			if( cfg.noise_shaping == 0 ) {
				cfg.noise_shaping = 1;
			}
			cfg.noise_shaping_amp = 1;
			cfg.noise_shaping_stop = 1;
			/* if( cfg.subblock_gain == -1 ) {
				cfg.subblock_gain = true;
			} */
			cfg.use_best_huffman = 1;
			cfg.full_outer_loop = 0;
			break;

		case 2:
			if( cfg.noise_shaping == 0 ) {
				cfg.noise_shaping = 1;
			}
			if( gfc.sv_qnt.substep_shaping == 0 ) {
				gfc.sv_qnt.substep_shaping = 2;
			}
			cfg.noise_shaping_amp = 1;
			cfg.noise_shaping_stop = 1;
			/* if( cfg.subblock_gain == -1 ) {
				cfg.subblock_gain = true;
			} */
			cfg.use_best_huffman = 1; /* inner loop */
			cfg.full_outer_loop = 0;
			break;

		case 1:
			if( cfg.noise_shaping == 0 ) {
				cfg.noise_shaping = 1;
			}
			if( gfc.sv_qnt.substep_shaping == 0 ) {
				gfc.sv_qnt.substep_shaping = 2;
			}
			cfg.noise_shaping_amp = 2;
			cfg.noise_shaping_stop = 1;
			/* if( cfg.subblock_gain == -1 ) {
				cfg.subblock_gain = true;
			} */
			cfg.use_best_huffman = 1;
			cfg.full_outer_loop = 0;
			break;

		case 0:
			if( cfg.noise_shaping == 0 ) {
				cfg.noise_shaping = 1;
			}
			if( gfc.sv_qnt.substep_shaping == 0 ) {
				gfc.sv_qnt.substep_shaping = 2;
			}
			cfg.noise_shaping_amp = 2;
			cfg.noise_shaping_stop = 1;
			/* if( cfg.subblock_gain == -1 ) {
				cfg.subblock_gain = true;
			} */
			cfg.use_best_huffman = 1; /*type 2 disabled because of it slowness,
						  in favor of full outer loop search */
			cfg.full_outer_loop = 1;
			break;
		}
	}

	private static final double linear_int(final double a, final double b, final double k) {
		return a + k * (b - a);
	}

	private static final class QMap {
		private final int sr_a; final float qa, qb, ta, tb;// final int lp;// FIXME lp never uses
		//
		private QMap(final int sr, final float a, final float b, final float c, final float d/*, final int lfr*/) {
			this.sr_a = sr;
			this.qa = a;
			this.qb = b;
			this.ta = c;
			this.tb = d;
			//this.lp = lfr;
		}
	};
	private static final QMap m[] = {//[9]
		new QMap( 48000, 0.0f, 6.5f,  0.0f, 6.5f ),//, 23700 ),
		new QMap( 44100, 0.0f, 6.5f,  0.0f, 6.5f ),//, 21780 ),
		new QMap( 32000, 6.5f, 8.0f,  5.2f, 6.5f ),//, 15800 ),
		new QMap( 24000, 8.0f, 8.5f,  5.2f, 6.0f ),//, 11850 ),
		new QMap( 22050, 8.5f, 9.01f, 5.2f, 6.5f ),//, 10892 ),
		new QMap( 16000, 9.01f, 9.4f, 4.9f, 6.5f ),//,  7903 ),
		new QMap( 12000, 9.4f, 9.6f,  4.5f, 6.0f ),//,  5928 ),
		new QMap( 11025, 9.6f, 9.9f,  5.1f, 6.5f ),//,  5446 ),
		new QMap(  8000, 9.9f, 10.f,  4.9f, 6.5f ),//,  3952 )
	};
	/********************************************************************
	 *   initialize internal params based on data in gf
	 *   (globalflags struct filled in by calling program)
	 *
	 *  OUTLINE:
	 *
	 * We first have some complex code to determine bitrate,
	 * output samplerate and mode.  It is complicated by the fact
	 * that we allow the user to set some or all of these parameters,
	 * and need to determine best possible values for the rest of them:
	 *
	 *  1. set some CPU related flags
	 *  2. check if we are mono.mono, stereo.mono or stereo.stereo
	 *  3.  compute bitrate and output samplerate:
	 *          user may have set compression ratio
	 *          user may have set a bitrate
	 *          user may have set a output samplerate
	 *  4. set some options which depend on output samplerate
	 *  5. compute the actual compression ratio
	 *  6. set mode based on compression ratio
	 *
	 *  The remaining code is much simpler - it just sets options
	 *  based on the mode & compression ratio:
	 *
	 *   set allow_diff_short based on mode
	 *   select lowpass filter based on compression ratio & mode
	 *   set the bitrate index, and min/max bitrates for VBR modes
	 *   disable VBR tag if it is not appropriate
	 *   initialize the bitstream
	 *   initialize scalefac_band data
	 *   set sideinfo_len (based on channels, CRC, out_samplerate)
	 *   write an id3v2 tag into the bitstream
	 *   write VBR tag into the bitstream
	 *   set mpeg1/2 flag
	 *   estimate the number of frames (based on a lot of data)
	 *
	 *   now we set more flags:
	 *   nspsytune:
	 *      see code
	 *   VBR modes
	 *      see code
	 *   CBR/ABR
	 *      see code
	 *
	 *  Finally, we set the algorithm flags based on the gfp.quality value
	 *  lame_init_qval(gfp );
	 *
	 ********************************************************************/
	public static final int lame_init_params(final LAME_GlobalFlags gfp) {
		if( ! gfp.is_lame_global_flags_valid() ) {
			return -1;
		}

		final LAME_InternalFlags gfc = gfp.internal_flags;
		if( gfc == null ) {
			return -1;
		}

		if( gfc.is_lame_internal_flags_valid() ) {
			return -1;
		} /* already initialized */

		/* start updating lame internal flags */
		gfc.class_id = LAME_InternalFlags.LAME_ID;
		gfc.lame_init_params_successful = false; /* will be set to one, when we get through until the end */

		if( gfp.num_channels < 1 || 2 < gfp.num_channels ) {
			return -1;
		} /* number of input channels makes no sense */
		if( gfp.samplerate != 0 ) {
			if( Util.SmpFrqIndex( gfp.samplerate ) < 0 ) {
				return -1;
			} /* output sample rate makes no sense */
		}

		final SessionConfig cfg = gfc.cfg;

		cfg.enforce_min_bitrate = gfp.VBR_hard_min;
		cfg.vbr = gfp.VBR;

		cfg.error_protection = gfp.error_protection;
		cfg.copyright = gfp.copyright;
		cfg.original = gfp.original;
		cfg.extension = gfp.extension;
		cfg.emphasis = gfp.emphasis;

		cfg.channels_in = gfp.num_channels;
		if( cfg.channels_in == 1 ) {
			gfp.mode = MONO;
		}
		cfg.channels_out = (gfp.mode == MONO) ? 1 : 2;
		if( gfp.mode != JOINT_STEREO ) {
			gfp.force_ms = false; /* forced mid/side stereo for j-stereo only */
		}
		cfg.force_ms = gfp.force_ms;

		if( cfg.vbr == vbr_off && gfp.VBR_mean_bitrate_kbps != 128 && gfp.brate == 0 ) {
			gfp.brate = gfp.VBR_mean_bitrate_kbps;
		}

		switch( cfg.vbr ) {
		case vbr_off:
		case vbr_mtrh:
		case vbr_mt:
			/* these modes can handle free format condition */
			break;
		default:
			gfp.free_format = false; /* mode can't be mixed with free format */
			break;
		}

		cfg.free_format = gfp.free_format;

		if( cfg.vbr == vbr_off && gfp.brate == 0 ) {
			/* no bitrate or compression ratio specified, use 11.025 */
			if( Quantize.EQ( gfp.compression_ratio, 0 ) ) {
				gfp.compression_ratio = 11.025f;
			} /* rate to compress a CD down to exactly 128000 bps */
		}

		/* find bitrate if user specify a compression ratio */
		if( gfp.VBR == vbr_off && gfp.compression_ratio > 0 ) {

			/* choose a bitrate for the output samplerate which achieves
			 * specified compression ratio
			 */
			gfp.brate = (int)(gfp.samplerate * (cfg.channels_out << 4) / (1.e3f * gfp.compression_ratio));

			/* we need the version for the bitrate table look up */
			cfg.samplerate_index = Util.SmpFrqIndex( gfp.samplerate );
			cfg.version = Util.getVersion( gfp.samplerate );
			// assert( cfg->samplerate_index >= 0 );

			if( ! cfg.free_format ) {
				gfp.brate = Util.FindNearestBitrate( gfp.brate, cfg.version, gfp.samplerate );
			}
		}
		if( gfp.samplerate < 16000 ) {
			gfp.VBR_mean_bitrate_kbps = Math.max( gfp.VBR_mean_bitrate_kbps, 8 );
			gfp.VBR_mean_bitrate_kbps = Math.min( gfp.VBR_mean_bitrate_kbps, 64 );
		} else if( gfp.samplerate < 32000 ) {
			gfp.VBR_mean_bitrate_kbps = Math.max( gfp.VBR_mean_bitrate_kbps, 8 );
			gfp.VBR_mean_bitrate_kbps = Math.min( gfp.VBR_mean_bitrate_kbps, 160 );
		} else {
			gfp.VBR_mean_bitrate_kbps = Math.max( gfp.VBR_mean_bitrate_kbps, 32 );
			gfp.VBR_mean_bitrate_kbps = Math.min( gfp.VBR_mean_bitrate_kbps, 320 );
		}
		/* WORK IN PROGRESS */
		/* mapping VBR scale to internal VBR quality settings */
		if( gfp.samplerate == 0 && (cfg.vbr == vbr_mt || cfg.vbr == vbr_mtrh) ) {
			final float qval = gfp.VBR_q + gfp.VBR_q_frac;

			for( int i = 2; i < 9; ++i ) {
				if( gfp.samplerate == m[i].sr_a ) {
					if( qval < m[i].qa ) {
						double d = qval / m[i].qa;
						d = d * m[i].ta;
						gfp.VBR_q = (int)d;
						gfp.VBR_q_frac = (float)(d - gfp.VBR_q);
					}
				}
				if( gfp.samplerate >= m[i].sr_a ) {
					if( m[i].qa <= qval && qval < m[i].qb ) {
						final float q_ = m[i].qb - m[i].qa;
						final float t_ = m[i].tb - m[i].ta;
						final double d = m[i].ta + t_ * (qval - m[i].qa) / q_;
						gfp.VBR_q = (int)d;
						gfp.VBR_q_frac = (float)(d - gfp.VBR_q);
						gfp.samplerate = m[i].sr_a;
						if( gfp.lowpassfreq == 0 ) {
							gfp.lowpassfreq = -1;
						}
						break;
					}
				}
			}
		}

		/****************************************************************/
		/* if a filter has not been enabled, see if we should add one: */
		/****************************************************************/
		if( gfp.lowpassfreq == 0 ) {
			double  lowpass = 16000;
			switch( cfg.vbr ) {
			case vbr_off: {
				lowpass = optimum_bandwidth( /* &lowpass, &highpass, */ gfp.brate );
				break;
			}
			case vbr_abr: {
				lowpass = optimum_bandwidth( /* &lowpass, &highpass, */ gfp.VBR_mean_bitrate_kbps );
				break;
			}
			case vbr_rh: {
				final int x[] = {// [11] = {
						19500, 19000, 18600, 18000, 17500, 16000, 15600, 14900, 12500, 10000, 3950
					};
				if( 0 <= gfp.VBR_q && gfp.VBR_q <= 9 ) {
					final double a = x[gfp.VBR_q], b = x[gfp.VBR_q + 1], k = gfp.VBR_q_frac;
					lowpass = linear_int( a, b, k );
				} else {
					lowpass = 19500;
				}
				break;
			}
			case vbr_mtrh:
			case vbr_mt: {
				final int x[] = {// [11] = {
						24000, 19500, 18500, 18000, 17500, 17000, 16500, 15600, 15200, 7230, 3950
				};
				if( 0 <= gfp.VBR_q && gfp.VBR_q <= 9 ) {
					final double a = x[gfp.VBR_q], b = x[gfp.VBR_q + 1], k = gfp.VBR_q_frac;
					lowpass = linear_int( a, b, k );
				} else {
					lowpass = 21500;
				}
				break;
			}
			default: {
				final int x[] = {// [11] = {
						19500, 19000, 18500, 18000, 17500, 16500, 15500, 14500, 12500, 9500, 3950
				};
				if( 0 <= gfp.VBR_q && gfp.VBR_q <= 9 ) {
					final double a = x[gfp.VBR_q], b = x[gfp.VBR_q + 1], k = gfp.VBR_q_frac;
					lowpass = linear_int( a, b, k );
				} else {
					lowpass = 19500;
				}
			}
			}

			if( gfp.mode == MONO && (cfg.vbr == vbr_off || cfg.vbr == vbr_abr) ) {
				lowpass *= 1.5;
			}

			gfp.lowpassfreq = (int)lowpass;
		}

		if( cfg.vbr == vbr_mt || cfg.vbr == vbr_mtrh ) {
			gfp.lowpassfreq = Math.min( 24000, gfp.lowpassfreq );
		} else {
			gfp.lowpassfreq = Math.min( 20500, gfp.lowpassfreq );
		}
		gfp.lowpassfreq = Math.min( gfp.samplerate >> 1, gfp.lowpassfreq );

		if( cfg.vbr == vbr_off ) {
			gfp.compression_ratio = gfp.samplerate * (cfg.channels_out << 4) / (1.e3f * gfp.brate );
		}
		if( cfg.vbr == vbr_abr ) {
			gfp.compression_ratio =
					gfp.samplerate * (cfg.channels_out << 4) / (1.e3f * gfp.VBR_mean_bitrate_kbps );
		}

		cfg.disable_reservoir = gfp.disable_reservoir;
		cfg.lowpassfreq = gfp.lowpassfreq;
		cfg.highpassfreq = gfp.highpassfreq;
		cfg.samplerate = gfp.samplerate;
		cfg.mode_gr = cfg.samplerate <= 24000 ? 1 : 2; /* Number of granules per frame */

		/*
		 *  sample freq       bitrate     compression ratio
		 *     [kHz]      [kbps/channel]   for 16 bit input
		 *     44.1            56               12.6
		 *     44.1            64               11.025
		 *     44.1            80                8.82
		 *     22.05           24               14.7
		 *     22.05           32               11.025
		 *     22.05           40                8.82
		 *     16              16               16.0
		 *     16              24               10.667
		 *
		 */
		/*
		 *  For VBR, take a guess at the compression_ratio.
		 *  For example:
		 *
		 *    VBR_q    compression     like
		 *     -        4.4         320 kbps/44 kHz
		 *   0...1      5.5         256 kbps/44 kHz
		 *     2        7.3         192 kbps/44 kHz
		 *     4        8.8         160 kbps/44 kHz
		 *     6       11           128 kbps/44 kHz
		 *     9       14.7          96 kbps
		 *
		 *  for lower bitrates, downsample with --resample
		 */

		switch( cfg.vbr ) {
		case vbr_mt:
		case vbr_rh:
		case vbr_mtrh:
			{
			/*numbers are a bit strange, but they determine the lowpass value */
			final float cmp[] = { 5.7f, 6.5f, 7.3f, 8.2f, 10f, 11.9f, 13f, 14f, 15f, 16.5f };
				gfp.compression_ratio = cmp[gfp.VBR_q];
			}
			break;
		case vbr_abr:
			gfp.compression_ratio =
				cfg.samplerate * (cfg.channels_out << 4) / (1.e3f * gfp.VBR_mean_bitrate_kbps );
			break;
		default:
			gfp.compression_ratio = cfg.samplerate * (cfg.channels_out << 4) / (1.e3f * gfp.brate );
			break;
		}

		/* mode = -1 (not set by user) or
		 * mode = MONO (because of only 1 input channel).
		 * If mode has not been set, then select J-STEREO
		 */
		if( gfp.mode == NOT_SET ) {
			gfp.mode = JOINT_STEREO;
		}

		cfg.mode = gfp.mode;

		/* apply user driven high pass filter */
		if( cfg.highpassfreq > 0 ) {
			cfg.highpass1 = 2.f * cfg.highpassfreq;

			if( gfp.highpasswidth >= 0 ) {
				cfg.highpass2 = 2.f * (cfg.highpassfreq + gfp.highpasswidth );
			} else {/* 0% above on default */
				cfg.highpass2 = (1 + 0.00f) * 2.f * cfg.highpassfreq;
			}

			cfg.highpass1 /= cfg.samplerate;
			cfg.highpass2 /= cfg.samplerate;
		} else {
			cfg.highpass1 = 0;
			cfg.highpass2 = 0;
		}
		/* apply user driven low pass filter */
		cfg.lowpass1 = 0;
		cfg.lowpass2 = 0;
		if( cfg.lowpassfreq > 0 && cfg.lowpassfreq < (cfg.samplerate >> 1)  ) {
			cfg.lowpass2 = 2.f * cfg.lowpassfreq;
			if( gfp.lowpasswidth >= 0 ) {
				cfg.lowpass1 = 2.f * (cfg.lowpassfreq - gfp.lowpasswidth );
				if( cfg.lowpass1 < 0 ) {
					cfg.lowpass1 = 0;
				}
			} else {          /* 0% below on default */
				cfg.lowpass1 = (1 - 0.00f) * 2.f * cfg.lowpassfreq;
			}
			cfg.lowpass1 /= cfg.samplerate;
			cfg.lowpass2 /= cfg.samplerate;
		}

		/**********************************************************************/
		/* compute info needed for polyphase filter (filter type==0, default) */
		/**********************************************************************/
		lame_init_params_ppflt( gfc );

		/*******************************************************
		* samplerate and bitrate index
		*******************************************************/
		cfg.samplerate_index = Util.SmpFrqIndex( cfg.samplerate );
		cfg.version = Util.getVersion( cfg.samplerate );
		// assert( cfg.samplerate_index >= 0 );

		if( cfg.vbr == vbr_off ) {
			if( cfg.free_format ) {
				gfc.ov_enc.bitrate_index = 0;
			} else {
				gfp.brate = Util.FindNearestBitrate( gfp.brate, cfg.version, cfg.samplerate );
				gfc.ov_enc.bitrate_index = Util.BitrateIndex( gfp.brate, cfg.version, cfg.samplerate );
				if( gfc.ov_enc.bitrate_index <= 0 ) {
					/* This never happens, because of preceding FindNearestBitrate!
					 * But, set a sane value, just in case
					 */
					// assert( 0 );
					gfc.ov_enc.bitrate_index = 8;
				}
			}
		} else {
			gfc.ov_enc.bitrate_index = 1;
		}

		Bitstream.init_bit_stream_w( gfc );

		final int j = cfg.samplerate_index + (3 * cfg.version) + (cfg.samplerate < 16000 ? 6 : 0);
		for(int i = 0; i < Encoder.SBMAX_l + 1; i++ ) {
			gfc.scalefac_band.l[i] = QuantizePVT.sfBandIndex[j].l[i];
		}

		for(int i = 0; i < Encoder.PSFB21 + 1; i++ ) {
			final int size = (gfc.scalefac_band.l[22] - gfc.scalefac_band.l[21]) / Encoder.PSFB21;
			final int start = gfc.scalefac_band.l[21] + i * size;
			gfc.scalefac_band.psfb21[i] = start;
		}
		gfc.scalefac_band.psfb21[Encoder.PSFB21] = 576;

		for(int i = 0; i < Encoder.SBMAX_s + 1; i++ ) {
			gfc.scalefac_band.s[i] = QuantizePVT.sfBandIndex[j].s[i];
		}

		for(int i = 0; i < Encoder.PSFB12 + 1; i++ ) {
			final int size = (gfc.scalefac_band.s[13] - gfc.scalefac_band.s[12]) / Encoder.PSFB12;
			final int start = gfc.scalefac_band.s[12] + i * size;
			gfc.scalefac_band.psfb12[i] = start;
		}
		gfc.scalefac_band.psfb12[Encoder.PSFB12] = 192;

		/* determine the mean bitrate for main data */
		if( cfg.mode_gr == 2 ) {
			cfg.sideinfo_len = (cfg.channels_out == 1) ? 4 + 17 : 4 + 32;
		} else {
			cfg.sideinfo_len = (cfg.channels_out == 1) ? 4 + 9 : 4 + 17;
		}

		if( cfg.error_protection ) {
			cfg.sideinfo_len += 2;
		}

		for( int k = 0; k < 19; k++ ) {
			gfc.sv_enc.pefirbuf[k] = 700 * cfg.mode_gr * cfg.channels_out;
		}

		if( gfp.ATHtype == -1 ) {
			gfp.ATHtype = 4;
		}

		boolean isUseTemporalNotInit = true;
		switch( cfg.vbr ) {

		case vbr_mt:
		case vbr_mtrh: {
			if( gfp.strict_ISO < 0 ) {
				gfp.strict_ISO = MDB_MAXIMUM;
			}
			//if( gfp.useTemporal < 0 ) {
			//	gfp.useTemporal = 0; /* off by default for this VBR mode */
			//}
			gfp.useTemporal = false;
			isUseTemporalNotInit = false;

			Presets.apply_preset( gfp, 500 - (gfp.VBR_q * 10), false );
			/*  The newer VBR code supports only a limited
			   subset of quality levels:
			   9-5=5 are the same, uses x^3/4 quantization
			   4-0=0 are the same  5 plus best huffman divide code
			 */
			if( gfp.quality < 0 ) {
				gfp.quality = LAME_DEFAULT_QUALITY;
			}
			if( gfp.quality < 5 ) {
				gfp.quality = 0;
			}
			if( gfp.quality > 7 ) {
				gfp.quality = 7;
			}

			/*  sfb21 extra only with MPEG-1 at higher sampling rates */
			if( gfp.experimentalY ) {
				gfc.sv_qnt.sfb21_extra = false;
			} else {
				gfc.sv_qnt.sfb21_extra = (cfg.samplerate > 44000 );
			}

			break;

		}
		case vbr_rh: {

			Presets.apply_preset( gfp, 500 - (gfp.VBR_q * 10), false );

			/*  sfb21 extra only with MPEG-1 at higher sampling rates */
			if( gfp.experimentalY ) {
				gfc.sv_qnt.sfb21_extra = false;
			} else {
				gfc.sv_qnt.sfb21_extra = (cfg.samplerate > 44000 );
			}

			/*  VBR needs at least the output of GPSYCHO,
			 *  so we have to garantee that by setting a minimum
			 *  quality level, actually level 6 does it.
			 *  down to level 6
			 */
			if( gfp.quality > 6 ) {
				gfp.quality = 6;
			}


			if( gfp.quality < 0 ) {
				gfp.quality = LAME_DEFAULT_QUALITY;
			}

			break;
		}

		default:           /* cbr/abr */  {
			/*  no sfb21 extra with CBR code */
			gfc.sv_qnt.sfb21_extra = false;

			if( gfp.quality < 0 ) {
				gfp.quality = LAME_DEFAULT_QUALITY;
			}

			if( cfg.vbr == vbr_off ) {
				gfp.lame_set_VBR_mean_bitrate_kbps( gfp.brate );
			}
			/* second, set parameters depending on bitrate */
			Presets.apply_preset( gfp, gfp.VBR_mean_bitrate_kbps, false );
			gfp.VBR = cfg.vbr;

			break;
		}
		}

		/*initialize default values common for all modes */

		gfc.sv_qnt.mask_adjust = gfp.maskingadjust;
		gfc.sv_qnt.mask_adjust_short = gfp.maskingadjust_short;

		if( cfg.vbr != vbr_off ) { /* choose a min/max bitrate for VBR */
			/* if the user didn't specify VBR_max_bitrate: */
			cfg.vbr_min_bitrate_index = 1; /* default: allow   8 kbps (MPEG-2) or  32 kbps (MPEG-1) */
			cfg.vbr_max_bitrate_index = 14; /* default: allow 160 kbps (MPEG-2) or 320 kbps (MPEG-1) */
			if( cfg.samplerate < 16000 ) {
				cfg.vbr_max_bitrate_index = 8;
			} /* default: allow 64 kbps (MPEG-2.5) */
			if( gfp.VBR_min_bitrate_kbps != 0 ) {
				gfp.VBR_min_bitrate_kbps =
						Util.FindNearestBitrate( gfp.VBR_min_bitrate_kbps, cfg.version, cfg.samplerate );
				cfg.vbr_min_bitrate_index =
						Util.BitrateIndex( gfp.VBR_min_bitrate_kbps, cfg.version, cfg.samplerate );
				if( cfg.vbr_min_bitrate_index < 0 ) {
					/* This never happens, because of preceding FindNearestBitrate!
					 * But, set a sane value, just in case
					 */
					// assert(0);
					cfg.vbr_min_bitrate_index = 1;
				}
			}
			if( gfp.VBR_max_bitrate_kbps != 0 ) {
				gfp.VBR_max_bitrate_kbps =
						Util.FindNearestBitrate( gfp.VBR_max_bitrate_kbps, cfg.version, cfg.samplerate );
				cfg.vbr_max_bitrate_index =
						Util.BitrateIndex( gfp.VBR_max_bitrate_kbps, cfg.version, cfg.samplerate );
				if( cfg.vbr_max_bitrate_index < 0 ) {
					/* This never happens, because of preceding FindNearestBitrate!
					 * But, set a sane value, just in case
					 */
					// assert(0);
					cfg.vbr_max_bitrate_index = cfg.samplerate < 16000 ? 8 : 14;
				}
			}
			gfp.VBR_min_bitrate_kbps = Tables.bitrate_table[cfg.version][cfg.vbr_min_bitrate_index];
			gfp.VBR_max_bitrate_kbps = Tables.bitrate_table[cfg.version][cfg.vbr_max_bitrate_index];
			gfp.VBR_mean_bitrate_kbps =
					Math.min( Tables.bitrate_table[cfg.version][cfg.vbr_max_bitrate_index], gfp.VBR_mean_bitrate_kbps );
			gfp.VBR_mean_bitrate_kbps =
					Math.max( Tables.bitrate_table[cfg.version][cfg.vbr_min_bitrate_index], gfp.VBR_mean_bitrate_kbps );
		}

		cfg.preset = gfp.preset;
		cfg.write_lame_tag = gfp.write_lame_tag;
		gfc.sv_qnt.substep_shaping = gfp.substep_shaping;
		cfg.noise_shaping = gfp.noise_shaping;
		cfg.subblock_gain = gfp.subblock_gain;
		cfg.use_best_huffman = gfp.use_best_huffman;
		cfg.avg_bitrate = gfp.brate;
		cfg.vbr_avg_bitrate_kbps = gfp.VBR_mean_bitrate_kbps;
		cfg.compression_ratio = gfp.compression_ratio;

		/* initialize internal qval settings */
		lame_init_qval( gfp );

		/*  automatic ATH adjustment on */
		if( gfp.athaa_type < 0 ) {
			gfc.ATH.use_adjust = 3;
		} else {
			gfc.ATH.use_adjust = gfp.athaa_type;
		}

		/* initialize internal adaptive ATH settings  -jd */
		gfc.ATH.aa_sensitivity_p = (float)Math.pow( 10.0, gfp.athaa_sensitivity / -10.0 );

		if( gfp.short_blocks == LAME_GlobalFlags.short_block_not_set ) {
			gfp.short_blocks = LAME_GlobalFlags.short_block_allowed;
		}

		/*Note Jan/2003: Many hardware decoders cannot handle short blocks in regular
		   stereo mode unless they are coupled (same type in both channels)
		   it is a rare event (1 frame per min. or so) that LAME would use
		   uncoupled short blocks, so lets turn them off until we decide
		   how to handle this.  No other encoders allow uncoupled short blocks,
		   even though it is in the standard.  */
		/* rh 20040217: coupling makes no sense for mono and dual-mono streams */
		if( gfp.short_blocks == LAME_GlobalFlags.short_block_allowed
				&& (cfg.mode == JOINT_STEREO || cfg.mode == STEREO) ) {
			gfp.short_blocks = LAME_GlobalFlags.short_block_coupled;
		}

		cfg.short_blocks = gfp.short_blocks;

		if( gfp.lame_get_quant_comp() < 0 ) {
			gfp.lame_set_quant_comp( 1 );
		}
		if( gfp.lame_get_quant_comp_short() < 0 ) {
			gfp.lame_set_quant_comp_short( 0 );
		}

		if( gfp.lame_get_msfix() < 0 ) {
			gfp.lame_set_msfix( 0 );
		}

		/* select psychoacoustic model */
		gfp.lame_set_exp_nspsytune( gfp.lame_get_exp_nspsytune() | 1 );

		if( gfp.ATHtype < 0 ) {
			gfp.ATHtype = 4;
		}

		if( gfp.ATHcurve < 0 ) {
			gfp.ATHcurve = 4;
		}

		if( gfp.interChRatio < 0 ) {
			gfp.interChRatio = 0;
		}

		if( isUseTemporalNotInit ) {// if( gfp.useTemporal < 0 ) {
			gfp.useTemporal = true;
		} /* on by default */

		cfg.interChRatio = gfp.interChRatio;
		cfg.msfix = gfp.msfix;
		cfg.ATH_offset_db = 0 - gfp.ATH_lower_db;
		cfg.ATH_offset_factor = (float)Math.pow( 10., cfg.ATH_offset_db * 0.1 );
		cfg.ATHcurve = gfp.ATHcurve;
		cfg.ATHtype = gfp.ATHtype;
		cfg.ATHonly = gfp.ATHonly;
		cfg.ATHshort = gfp.ATHshort;
		cfg.noATH = gfp.noATH;

		cfg.quant_comp = gfp.quant_comp;
		cfg.quant_comp_short = gfp.quant_comp_short;

		cfg.use_temporal_masking_effect = gfp.useTemporal;
		if( cfg.mode == JOINT_STEREO ) {
			cfg.use_safe_joint_stereo = (gfp.exp_nspsytune & 2) != 0;
		} else {
			cfg.use_safe_joint_stereo = false;
		}
		{
			cfg.adjust_bass_db = (gfp.exp_nspsytune >> 2) & 63;
			if( cfg.adjust_bass_db >= 32.f ) {
				cfg.adjust_bass_db -= 64.f;
			}
			cfg.adjust_bass_db *= 0.25f;

			cfg.adjust_alto_db = (gfp.exp_nspsytune >> 8) & 63;
			if( cfg.adjust_alto_db >= 32.f ) {
				cfg.adjust_alto_db -= 64.f;
			}
			cfg.adjust_alto_db *= 0.25f;

			cfg.adjust_treble_db = (gfp.exp_nspsytune >> 14) & 63;
			if( cfg.adjust_treble_db >= 32.f ) {
				cfg.adjust_treble_db -= 64.f;
			}
			cfg.adjust_treble_db *= 0.25f;

			/*  to be compatible with Naoki's original code, the next 6 bits
			 *  define only the amount of changing treble for sfb21 */
			cfg.adjust_sfb21_db = (gfp.exp_nspsytune >> 20) & 63;
			if( cfg.adjust_sfb21_db >= 32.f ) {
				cfg.adjust_sfb21_db -= 64.f;
			}
			cfg.adjust_sfb21_db *= 0.25f;
			cfg.adjust_sfb21_db += cfg.adjust_treble_db;
		}

		/* Setting up the PCM input data transform matrix, to apply
		 * user defined re-scaling, and or two-to-one channel downmix.
		 */
		{
			final float k[][] /* [2][2] */ = { {1.0f, 0.0f}, {0.0f, 1.0f} };

			/* user selected scaling of the samples */
			k[0][0] *= gfp.scale;
			k[0][1] *= gfp.scale;
			k[1][0] *= gfp.scale;
			k[1][1] *= gfp.scale;
			/* Downsample to Mono if 2 channels in and 1 channel out */
			if( cfg.channels_in == 2 && cfg.channels_out == 1 ) {
				k[0][0] = 0.5f * (k[0][0] + k[1][0] );
				k[0][1] = 0.5f * (k[0][1] + k[1][1] );
				k[1][0] = 0;
				k[1][1] = 0;
			}
			cfg.pcm_transform[0][0] = k[0][0];
			cfg.pcm_transform[0][1] = k[0][1];
			cfg.pcm_transform[1][0] = k[1][0];
			cfg.pcm_transform[1][1] = k[1][1];
		}

		/* padding method as described in
		 * "MPEG-Layer3 / Bitstream Syntax and Decoding"
		 * by Martin Sieler, Ralph Sperschneider
		 *
		 * note: there is no padding for the very first frame
		 *
		 * Robert Hegemann 2000-06-22
		 */
		gfc.sv_enc.slot_lag = gfc.sv_enc.frac_SpF = 0;
		if( cfg.vbr == vbr_off ) {
			gfc.sv_enc.slot_lag = gfc.sv_enc.frac_SpF = (int)(((cfg.version + 1) * 72000L * cfg.avg_bitrate) % cfg.samplerate);
		}

		lame_init_bitstream( gfp );

		QuantizePVT.iteration_init( gfc );
		PsyModel.psymodel_init( gfp );

		cfg.buffer_constraint = Bitstream.get_max_frame_buffer_size_by_constraint( cfg, gfp.strict_ISO );

		/* updating lame internal flags finished successful */
		gfc.lame_init_params_successful = true;
		return 0;
	}

	private static final int update_inbuffer_size(final LAME_InternalFlags gfc, final int nsamples) {
		final EncStateVar esv = gfc.sv_enc;
		if( esv.in_buffer_0 == null || esv.in_buffer_nsamples < nsamples ) {
			esv.in_buffer_0 = new float[ nsamples ];
			esv.in_buffer_1 = new float[ nsamples ];
			esv.in_buffer_nsamples = nsamples;
		}
		if( esv.in_buffer_0 == null || esv.in_buffer_1 == null ) {
			esv.in_buffer_0 = null;
			esv.in_buffer_1 = null;
			esv.in_buffer_nsamples = 0;
			//System.err.print("Error: can't allocate in_buffer buffer\n");
			return -2;
		}
		return 0;
	}

	private static final int calcNeeded(final SessionConfig cfg) {;
		int pcm_samples_per_frame = 576 * cfg.mode_gr;

		int mf_needed = Encoder.BLKSIZE + pcm_samples_per_frame - Encoder.FFTOFFSET; /* amount needed for FFT */
		pcm_samples_per_frame += 512 - 32;
		mf_needed = (mf_needed >= pcm_samples_per_frame ? mf_needed : pcm_samples_per_frame);

		return mf_needed;
	}

	/**
	 * THE MAIN LAME ENCODING INTERFACE
	 * mt 3/00
	 *
	 * input pcm data, output (maybe) mp3 frames.
	 * This routine handles all buffering, resampling and filtering for you.
	 * The required mp3buffer_size can be computed from num_samples,
	 * samplerate and encoding rate, but here is a worst case estimate:
	 *
	 * mp3buffer_size in bytes = 1.25*num_samples + 7200
	 *
	 * return code = number of bytes output in mp3buffer.  can be 0
	 *
	 * NOTE: this routine uses LAME's internal PCM data representation,
	 * 'sample_t'.  It should not be used by any application.
	 * applications should use lame_encode_buffer(),
	 *                         lame_encode_buffer_float()
	 *                         lame_encode_buffer_int()
	 * etc... depending on what type of data they are working with.
	 */
	private static final int lame_encode_buffer_sample_t(final LAME_InternalFlags gfc,
		int nsamples, final byte[] mp3buf, int mp3buf_offset, final int mp3buf_size)
	{
		if( gfc.class_id != LAME_InternalFlags.LAME_ID ) {
			return -3;
		}

		if( nsamples == 0 ) {
			return 0;
		}

		/* copy out any tags that may have been written into bitstream */
		//{   /* if user specifed buffer size = 0, dont check size */
			int buf_size = mp3buf_size == 0 ? Integer.MAX_VALUE : mp3buf_size;
			int mp3size = Bitstream.copy_buffer( gfc, mp3buf, mp3buf_offset, buf_size, false );
		//}
		if( mp3size < 0 ) {
			return mp3size;
		}  /* not enough buffer space */
		mp3buf_offset += mp3size;

		final SessionConfig cfg = gfc.cfg;
		final EncStateVar esv = gfc.sv_enc;
		final int pcm_samples_per_frame = 576 * cfg.mode_gr;
		final float mfbuf[][] = new float[2][];
		final float in_buffer[][] = new float[2][];

		in_buffer[0] = esv.in_buffer_0;
		in_buffer[1] = esv.in_buffer_1;

		final int mf_needed = calcNeeded( cfg );

		mfbuf[0] = esv.mfbuf[0];
		mfbuf[1] = esv.mfbuf[1];

		int in_buffer_ptr = 0;// java: in_buffer[in_buffer_ptr]
		while( nsamples > 0 ) {
			//final int n_in = 0;    /* number of input samples processed with fill_buffer */
			//final int n_out = 0;   /* number of samples output with fill_buffer */
			/* n_in <> n_out if we are resampling */

			// in_buffer_ptr[0] = in_buffer[0];
			// in_buffer_ptr[1] = in_buffer[1];
			/* copy in new samples into mfbuf, with resampling */
			final long tmp = Util.fill_buffer( gfc, mfbuf, in_buffer, in_buffer_ptr, nsamples/*, &n_in, &n_out*/ );
			final int n_out = (int)tmp;
			final int n_in = (int)(tmp >> 32);

			/* update in_buffer counters */
			nsamples -= n_in;
			/*in_buffer[0] += n_in;
			if( cfg.channels_out == 2 ) {
				in_buffer[1] += n_in;
			}*/
			in_buffer_ptr += n_in;

			/* update mfbuf[] counters */
			esv.mf_size += n_out;

			/* lame_encode_flush may have set gfc.mf_sample_to_encode to 0
			 * so we have to reinitialize it here when that happened.
			 */
			if( esv.mf_samples_to_encode < 1 ) {
				esv.mf_samples_to_encode = Encoder.ENCDELAY + Encoder.POSTDELAY;
			}
			esv.mf_samples_to_encode += n_out;

			if( esv.mf_size >= mf_needed ) {
				/* encode the frame.  */
				/* mp3buf              = pointer to current location in buffer */
				/* mp3buf_size         = size of original mp3 output buffer */
				/*                     = 0 if we should not worry about the */
				/*                       buffer size because calling program is  */
				/*                       to lazy to compute it */
				/* mp3size             = size of data written to buffer so far */
				/* mp3buf_size-mp3size = amount of space avalable  */

				buf_size = mp3buf_size - mp3size;
				if( mp3buf_size == 0 ) {
					buf_size = Integer.MAX_VALUE;
				}

				final int ret = Encoder.lame_encode_mp3_frame( gfc, mfbuf[0], mfbuf[1], mp3buf, mp3buf_offset, buf_size );

				if( ret < 0 ) {
					return ret;
				}
				mp3buf_offset += ret;
				mp3size += ret;

				/* shift out old samples */
				esv.mf_size -= pcm_samples_per_frame;
				esv.mf_samples_to_encode -= pcm_samples_per_frame;
				for( int ch = 0, channels_out = cfg.channels_out, mf_size = esv.mf_size; ch < channels_out; ch++ ) {
					final float[] mfbuf_ch = mfbuf[ch];// java
					for( int i = 0, j = pcm_samples_per_frame; i < mf_size; i++, j++ ) {
						mfbuf_ch[i] = mfbuf_ch[j];
					}
				}
			}
		}

		return mp3size;
	}

	// private static enum PCMSampleType {
	private static final int pcm_short_type = 0;
	private static final int pcm_int_type = 1;
	private static final int pcm_long_type = 2;
	private static final int pcm_float_type = 3;
	private static final int pcm_double_type = 4;
	//}

	/**
	 * java: r = null for interleaved
	 */
	private static final void lame_copy_inbuffer(final LAME_InternalFlags gfc,
		final Object l, final Object r, final int offset, final int nsamples,
		final int /* PCMSampleType */ pcm_type, final int jump, final float s)
	{
		final SessionConfig cfg = gfc.cfg;
		final EncStateVar esv = gfc.sv_enc;
		final float[] ib0 = esv.in_buffer_0;
		final float[] ib1 = esv.in_buffer_1;
		final float k[][] = new float[2][2];

		/* Apply user defined re-scaling */
		k[0][0] = s * cfg.pcm_transform[0][0];
		k[0][1] = s * cfg.pcm_transform[0][1];
		k[1][0] = s * cfg.pcm_transform[1][0];
		k[1][1] = s * cfg.pcm_transform[1][1];

		/* make a copy of input buffer, changing type to sample_t */
		switch( pcm_type  ) {
		case pcm_short_type:
			{
				final short[] bl = (short[])l;
				int bli = offset;
				final short[] br;
				int bri = offset;
				if( r != null ) {
					br = (short[])r;
				} else {
					br = bl;
					bri++;
				}
				for( int i = 0; i < nsamples; i++ ) {
					final float xl = (float)bl[bli];
					final float xr = (float)br[bri];
					final float u = xl * k[0][0] + xr * k[0][1];
					final float v = xl * k[1][0] + xr * k[1][1];
					ib0[i] = u;
					ib1[i] = v;
					bli += jump;
					bri += jump;
				}
			}
			break;
		case pcm_int_type:
			{
				final int[] bl = (int[])l;
				int bli = offset;
				final int[] br;
				int bri = offset;
				if( r != null ) {
					br = (int[])r;
				} else {
					br = bl;
					bri++;
				}
				for( int i = 0; i < nsamples; i++ ) {
					final float xl = (float)bl[bli];
					final float xr = (float)br[bri];
					final float u = xl * k[0][0] + xr * k[0][1];
					final float v = xl * k[1][0] + xr * k[1][1];
					ib0[i] = u;
					ib1[i] = v;
					bli += jump;
					bri += jump;
				}
			}
			break;
		case pcm_long_type:
			{
				final long[] bl = (long[])l;
				int bli = offset;
				final long[] br;
				int bri = offset;
				if( r != null ) {
					br = (long[])r;
				} else {
					br = bl;
					bri++;
				}
				for( int i = 0; i < nsamples; i++ ) {
					final float xl = (float)bl[bli];
					final float xr = (float)br[bri];
					final float u = xl * k[0][0] + xr * k[0][1];
					final float v = xl * k[1][0] + xr * k[1][1];
					ib0[i] = u;
					ib1[i] = v;
					bli += jump;
					bri += jump;
				}
			}
			break;
		case pcm_float_type:
			{
				final float[] bl = (float[])l;
				int bli = offset;
				final float[] br;
				int bri = offset;
				if( r != null ) {
					br = (float[])r;
				} else {
					br = bl;
					bri++;
				}
				for( int i = 0; i < nsamples; i++ ) {
					final float xl = (float)bl[bli];
					final float xr = (float)br[bri];
					final float u = xl * k[0][0] + xr * k[0][1];
					final float v = xl * k[1][0] + xr * k[1][1];
					ib0[i] = u;
					ib1[i] = v;
					bli += jump;
					bri += jump;
				}
			}
			break;
		case pcm_double_type:
			{
				final double[] bl = (double[])l;
				int bli = offset;
				final double[] br;
				int bri = offset;
				if( r != null ) {
					br = (double[])r;
				} else {
					br = bl;
					bri++;
				}
				for( int i = 0; i < nsamples; i++ ) {
					final float xl = (float)bl[bli];
					final float xr = (float)br[bri];
					final float u = xl * k[0][0] + xr * k[0][1];
					final float v = xl * k[1][0] + xr * k[1][1];
					ib0[i] = u;
					ib1[i] = v;
					bli += jump;
					bri += jump;
				}
			}
			break;
		}
	}

	private static final int lame_encode_buffer_template(final LAME_GlobalFlags gfp,
		final Object buffer_l, final Object buffer_r, final int offset, final int nsamples,
		final byte[] mp3buf, final int mp3buf_offset, final int mp3buf_size, final int /* PCMSampleType */ pcm_type, final int aa, final float norm)
	{
		if( gfp.is_lame_global_flags_valid() ) {
			final LAME_InternalFlags gfc = gfp.internal_flags;
			if( gfc.is_lame_internal_flags_valid() ) {
				final SessionConfig cfg = gfc.cfg;

				if( nsamples == 0 ) {
					return 0;
				}

				if( update_inbuffer_size( gfc, nsamples ) != 0 ) {
					return -2;
				}
				/* make a copy of input buffer, changing type to sample_t */
				if( cfg.channels_in > 1 ) {
					/* if( buffer_l == null || buffer_r == null ) {
						return 0;
					}*/
					if( buffer_l == null ) {// java variant: buffer_r may be null for interleaved
						return 0;
					}
					lame_copy_inbuffer( gfc, buffer_l, buffer_r, offset, nsamples, pcm_type, aa, norm );
				} else {
					if( buffer_l == null ) {
						return 0;
					}
					lame_copy_inbuffer( gfc, buffer_l, buffer_l, offset, nsamples, pcm_type, aa, norm );
				}

				return lame_encode_buffer_sample_t( gfc, nsamples, mp3buf, mp3buf_offset, mp3buf_size );
			}
		}
		return -3;
	}

	/**
	 * input pcm data, output (maybe) mp3 frames.
	 * This routine handles all buffering, resampling and filtering for you.
	 *
	 * return code     number of bytes output in mp3buf. Can be 0
	 *                 -1:  mp3buf was too small
	 *                 -2:  malloc() problem
	 *                 -3:  lame_init_params() not called
	 *                 -4:  psycho acoustic problems
	 *
	 * The required mp3buf_size can be computed from num_samples,
	 * samplerate and encoding rate, but here is a worst case estimate:
	 *
	 * mp3buf_size in bytes = 1.25*num_samples + 7200
	 *
	 * I think a tighter bound could be:  (mt, March 2000)
	 * MPEG1:
	 *    num_samples*(bitrate/8)/samplerate + 4*1152*(bitrate/8)/samplerate + 512
	 * MPEG2:
	 *    num_samples*(bitrate/8)/samplerate + 4*576*(bitrate/8)/samplerate + 256
	 *
	 * but test first if you use that!
	 *
	 * set mp3buf_size = 0 and LAME will not check if mp3buf_size is
	 * large enough.
	 *
	 * NOTE:
	 * if gfp->num_channels=2, but gfp->mode = 3 (mono), the L & R channels
	 * will be averaged into the L channel before encoding only the L channel
	 * This will overwrite the data in buffer_l[] and buffer_r[].
	 *
	 */
	public static final int lame_encode_buffer(final LAME_GlobalFlags gfp,
		final short pcm_l[], final short pcm_r[], final int nsamples,
		final byte[] mp3buf, final int mp3buf_offset, final int mp3buf_size)
	{
		return lame_encode_buffer_template( gfp, pcm_l, pcm_r, 0, nsamples, mp3buf, mp3buf_offset, mp3buf_size, pcm_short_type, 1, 1.0f );
	}

	/** as lame_encode_buffer, but for 'float's.
	 * !! NOTE: !! data must still be scaled to be in the same range as
	 * short int, +/- 32768
	 */
	public static final int lame_encode_buffer_float(final LAME_GlobalFlags gfp,
		final float pcm_l[], final float pcm_r[], final int nsamples,
		final byte[] mp3buf, final int mp3buf_offset, final int mp3buf_size)
	{
		/* input is assumed to be normalized to +/- 32768 for full scale */
		return lame_encode_buffer_template( gfp, pcm_l, pcm_r, 0, nsamples, mp3buf, mp3buf_offset, mp3buf_size, pcm_float_type, 1, 1.0f );
	}

	/** as lame_encode_buffer, but for 'float's.
	 * !! NOTE: !! data must be scaled to +/- 1 full scale
	 */
	public static final int lame_encode_buffer_ieee_float(final LAME_GlobalFlags gfp,
		final float pcm_l[], final float pcm_r[], final int nsamples,
		final byte[] mp3buf, final int mp3buf_offset, final int mp3buf_size)
	{
		/* input is assumed to be normalized to +/- 1.0 for full scale */
		return lame_encode_buffer_template(gfp, pcm_l, pcm_r, 0, nsamples, mp3buf, mp3buf_offset, mp3buf_size, pcm_float_type, 1, 32767.0f );
	}

	public static final int lame_encode_buffer_interleaved_ieee_float(final LAME_GlobalFlags gfp,
		final float pcm[], final int nsamples,
		final byte[] mp3buf, final int mp3buf_offset, final int mp3buf_size ) {
		/* input is assumed to be normalized to +/- 1.0 for full scale */
		return lame_encode_buffer_template( gfp, pcm, null, 0, nsamples, mp3buf, mp3buf_offset, mp3buf_size, pcm_float_type, 2, 32767.0f  );
	}

	/** as lame_encode_buffer, but for 'double's.
	 * !! NOTE: !! data must be scaled to +/- 1 full scale
	 */
	public static final int lame_encode_buffer_ieee_double(final LAME_GlobalFlags gfp,
		final double pcm_l[], final double pcm_r[], final int nsamples,
		final byte[] mp3buf, final int mp3buf_offset, final int mp3buf_size)
	{
		/* input is assumed to be normalized to +/- 1.0 for full scale */
		return lame_encode_buffer_template(gfp, pcm_l, pcm_r, 0, nsamples, mp3buf, mp3buf_offset, mp3buf_size, pcm_double_type, 1, 32767.0f );
	}

	public static final int lame_encode_buffer_interleaved_ieee_double(final LAME_GlobalFlags gfp,
		final double pcm[], final int nsamples,
		final byte[] mp3buf, final int mp3buf_offset, final int mp3buf_size)
	{
		/* input is assumed to be normalized to +/- 1.0 for full scale */
		return lame_encode_buffer_template( gfp, pcm, null, 0, nsamples, mp3buf, mp3buf_offset, mp3buf_size, pcm_double_type, 2, 32767.0f );
	}

	public static final int lame_encode_buffer_int(final LAME_GlobalFlags gfp,
		final int pcm_l[], final int pcm_r[], final int offset, final int nsamples,
		final byte[] mp3buf, final int mp3buf_offset, final int mp3buf_size)
	{
		/* input is assumed to be normalized to +/- MAX_INT for full scale */
		final float norm = (1.0f / (1L << (Integer.SIZE - 16)));
		return lame_encode_buffer_template( gfp, pcm_l, pcm_r, offset, nsamples, mp3buf, mp3buf_offset, mp3buf_size, pcm_int_type, 1, norm );
	}

	/** Same as lame_encode_buffer_long(), but with correct scaling.
	 * !! NOTE: !! data must still be scaled to be in the same range as
	 * type 'long'.   Data should be in the range:  +/- 2^(8*size(long)-1)
	 *
	 */
	public static final int lame_encode_buffer_long2(final LAME_GlobalFlags gfp,
		final long pcm_l[], final long pcm_r[], final int nsamples,
		final byte[] mp3buf, final int mp3buf_offset, final int mp3buf_size)
	{
		/* input is assumed to be normalized to +/- MAX_LONG for full scale */
		final float norm = (1.0f / (1L << (Long.SIZE - 16)));
		return lame_encode_buffer_template( gfp, pcm_l, pcm_r, 0, nsamples, mp3buf, mp3buf_offset, mp3buf_size, pcm_long_type, 1, norm );
	}

	/** as lame_encode_buffer, but for long's
	 * !! NOTE: !! data must still be scaled to be in the same range as
	 * short int, +/- 32768
	 *
	 * This scaling was a mistake (doesn't allow one to exploit full
	 * precision of type 'long'.  Use lame_encode_buffer_long2() instead.
	 *
	 */
	public static final int lame_encode_buffer_long(final LAME_GlobalFlags gfp,
		final long pcm_l[], final long pcm_r[], final int nsamples,
		final byte[] mp3buf, final int mp3buf_offset, final int mp3buf_size)
	{
		/* input is assumed to be normalized to +/- 32768 for full scale */
		return lame_encode_buffer_template( gfp, pcm_l, pcm_r, 0, nsamples, mp3buf, mp3buf_offset, mp3buf_size, pcm_long_type, 1, 1.0f );
	}

	/**
	 * as above, but input has L & R channel data interleaved.
	 * NOTE:
	 * num_samples = number of samples in the L (or R)
	 * channel, not the total number of samples in pcm[]
	 */
	public static final int lame_encode_buffer_interleaved(final LAME_GlobalFlags gfp,
		final short pcm[], final int nsamples,
		final byte[] mp3buf, final int mp3buf_offset, final int mp3buf_size)
	{
		/* input is assumed to be normalized to +/- MAX_SHORT for full scale */
		return lame_encode_buffer_template( gfp, pcm, null, 0, nsamples, mp3buf, mp3buf_offset, mp3buf_size, pcm_short_type, 2, 1.0f );
	}


	public static final int lame_encode_buffer_interleaved_int(final LAME_GlobalFlags gfp,
		final int pcm[], final int nsamples,
		final byte[] mp3buf, final int mp3buf_offset, final int mp3buf_size)
	{
	    /* input is assumed to be normalized to +/- MAX(int) for full scale */
	    final float norm = (float)(1.0 / (1L << (Integer.SIZE - 16)));
	    return lame_encode_buffer_template(gfp, pcm, null, 0, nsamples, mp3buf, mp3buf_offset, mp3buf_size, pcm_int_type, 2, norm);
	}


	/*****************************************************************
	 Flush mp3 buffer, pad with ancillary data so last frame is complete.
	 Reset reservoir size to 0
	 but keep all PCM samples and MDCT data in memory
	 This option is used to break a large file into several mp3 files
	 that when concatenated together will decode with no gaps
	 Because we set the reservoir=0, they will also decode seperately
	 with no errors.
	*********************************************************************/
	public static final int lame_encode_flush_nogap(final LAME_GlobalFlags gfp, final byte[] mp3buffer, int mp3buffer_size) {
		int rc = -3;
		if( gfp.is_lame_global_flags_valid() ) {
			final LAME_InternalFlags gfc = gfp.internal_flags;
			if( gfc.is_lame_internal_flags_valid() ) {
				Bitstream.flush_bitstream( gfc  );
				/* if user specifed buffer size = 0, dont check size */
				if( mp3buffer_size == 0 ) {
					mp3buffer_size = Integer.MAX_VALUE;
				}
				rc = Bitstream.copy_buffer( gfc, mp3buffer, 0, mp3buffer_size, true );
			}
		}
		return rc;
	}

	/** called by lame_init_params.  You can also call this after flush_nogap
	   if you want to write new id3v2 and Xing VBR tags into the bitstream */
	public static final int lame_init_bitstream(final LAME_GlobalFlags gfp) {
		if( gfp.is_lame_global_flags_valid() ) {
			final LAME_InternalFlags gfc = gfp.internal_flags;
			if( gfc != null ) {
				gfc.ov_enc.frame_number = 0;

				if( gfp.write_id3tag_automatic ) {
					ID3Tag.id3tag_write_v2( gfp );
				}
				/* initialize histogram data optionally used by frontend */
				int[][] buf = gfc.ov_enc.bitrate_channelmode_hist;
				for( int i = 0; i < buf.length; i++ ) {
					final int[] b = buf[i];
					for( int j = 0; j < b.length; j++ ) {
						b[j] = 0;
					}
				}
				buf = gfc.ov_enc.bitrate_blocktype_hist;
				for( int i = 0; i < buf.length; i++ ) {
					final int[] b = buf[i];
					for( int j = 0; j < b.length; j++ ) {
						b[j] = 0;
					}
				}

				/* Write initial VBR Header to bitstream and init VBR data */
				if( gfc.cfg.write_lame_tag ) {
					VBRTag.InitVbrTag( gfp );
				}

				return 0;
			}
		}
		return -3;
	}

	/*****************************************************************/
	/* flush internal PCM sample buffers, then mp3 buffers           */
	/* then write id3 v1 tags into bitstream.                        */
	/*****************************************************************/
	public static final int lame_encode_flush(final LAME_GlobalFlags gfp, final byte[] mp3buffer, final int mp3buffer_size ) {
		if( ! gfp.is_lame_global_flags_valid() ) {
			return -3;
		}
		final LAME_InternalFlags gfc = gfp.internal_flags;
		if( ! gfc.is_lame_internal_flags_valid() ) {
			return -3;
		}
		final SessionConfig cfg = gfc.cfg;
		final EncStateVar esv = gfc.sv_enc;

		/* Was flush already called? */
		if( esv.mf_samples_to_encode < 1 ) {
			return 0;
		}
		final int pcm_samples_per_frame = 576 * cfg.mode_gr;
		final int mf_needed = calcNeeded( cfg );

		final int samples_to_encode = esv.mf_samples_to_encode - Encoder.POSTDELAY;

		final short buffer[][] = new short[2][1152];// java: already zeroed
		int mp3count = 0;

		/* we always add POSTDELAY=288 padding to make sure granule with real
		 * data can be complety decoded (because of 50% overlap with next granule */
		int end_padding = pcm_samples_per_frame - (samples_to_encode % pcm_samples_per_frame );
		if( end_padding < 576 ) {
			end_padding += pcm_samples_per_frame;
		}
		gfc.ov_enc.encoder_padding = end_padding;

		int offset = 0;
		int imp3 = 0;
		int frames_left = (samples_to_encode + end_padding) / pcm_samples_per_frame;
		while( frames_left > 0 && imp3 >= 0 ) {
			final int frame_num = gfc.ov_enc.frame_number;
			int bunch = mf_needed - esv.mf_size;

			if( bunch > 1152 ) {
				bunch = 1152;
			}
			if( bunch < 1 ) {
				bunch = 1;
			}

			int mp3buffer_size_remaining = mp3buffer_size - mp3count;

			/* if user specifed buffer size = 0, dont check size */
			if( mp3buffer_size == 0 ) {
				mp3buffer_size_remaining = 0;
			}

			/* send in a frame of 0 padding until all internal sample buffers
			 * are flushed
			 */
			imp3 = lame_encode_buffer( gfp, buffer[0], buffer[1], bunch,
					mp3buffer, offset, mp3buffer_size_remaining );

			offset += imp3;
			mp3count += imp3;
			{/* even a single pcm sample can produce several frames!
			  * for example: 1 Hz input file resampled to 8 kHz mpeg2.5
			  */
				final int new_frames = gfc.ov_enc.frame_number - frame_num;
				if( new_frames > 0 ) {
					frames_left -=  new_frames;
				}
	        }

		}
		/* Set esv.mf_samples_to_encode to 0, so we may detect
		 * and break loops calling it more than once in a row.
		 */
		esv.mf_samples_to_encode = 0;

		if( imp3 < 0 ) {
			/* some type of fatal error */
			return imp3;
		}

		int mp3buffer_size_remaining = mp3buffer_size - mp3count;
		/* if user specifed buffer size = 0, dont check size */
		if( mp3buffer_size == 0 ) {
			mp3buffer_size_remaining = Integer.MAX_VALUE;
		}

		/* mp3 related stuff.  bit buffer might still contain some mp3 data */
		Bitstream.flush_bitstream( gfc );
		imp3 = Bitstream.copy_buffer( gfc, mp3buffer, offset, mp3buffer_size_remaining, true );
		if( imp3 < 0 ) {
			/* some type of fatal error */
			return imp3;
		}
		offset += imp3;
		mp3count += imp3;
		mp3buffer_size_remaining = mp3buffer_size - mp3count;
		/* if user specifed buffer size = 0, dont check size */
		if( mp3buffer_size == 0 ) {
			mp3buffer_size_remaining =  Integer.MAX_VALUE;
		}

		if( gfp.write_id3tag_automatic ) {
			/* write a id3 tag to the bitstream */
			ID3Tag.id3tag_write_v1( gfp );

			imp3 = Bitstream.copy_buffer( gfc, mp3buffer, offset, mp3buffer_size_remaining, false );

			if( imp3 < 0 ) {
				return imp3;
			}
			mp3count += imp3;
		}
		return mp3count;
	}

	/***********************************************************************
	 *
	 *      lame_close ()
	 *
	 *  frees internal buffers
	 *
	 ***********************************************************************/
	public static final int lame_close(final LAME_GlobalFlags gfp) {
		int ret = 0;
		if( gfp != null && gfp.class_id == LAME_InternalFlags.LAME_ID ) {
			final LAME_InternalFlags gfc = gfp.internal_flags;
			gfp.class_id = 0;
			if( null == gfc || gfc.class_id != LAME_InternalFlags.LAME_ID ) {
				ret = -3;
			}
			if( null != gfc ) {
				gfc.lame_init_params_successful = false;
				gfc.class_id = 0;
				/* this routine will free all malloc'd data in gfc, and then free gfc: */
				gfc.freegfc();
				gfp.internal_flags = null;
			}
			if( gfp.lame_allocated_gfp ) {
				gfp.lame_allocated_gfp = false;
			}
		}
		return ret;
	}

	/*****************************************************************/
	/* flush internal mp3 buffers, and free internal buffers         */
	/*****************************************************************/
	/*
	 * OBSOLETE:
	 * lame_encode_finish combines lame_encode_flush() and lame_close() in
	 * one call.  However, once this call is made, the statistics routines
	 * will no longer work because the data will have been cleared, and
	 * lame_mp3_tags_fid() cannot be called to add data to the VBR header
	 */
	/*private static final int lame_encode_finish(final LAME_GlobalFlags gfp, final byte[] mp3buffer, final int mp3buffer_size)
	{
		final int ret = lame_encode_flush( gfp, mp3buffer, mp3buffer_size );

		lame_close( gfp );

		return ret;
	}*/

	/*****************************************************************/
	/* write VBR Xing header, and ID3 version 1 tag, if asked for    */
	/*****************************************************************/
	/**
	 * OPTIONAL:
	 * lame_mp3_tags_fid will rewrite a Xing VBR tag to the mp3 file with file
	 * pointer fid.  These calls perform forward and backwards seeks, so make
	 * sure fid is a real file.  Make sure lame_encode_flush has been called,
	 * and all mp3 data has been written to the file before calling this
	 * function.
	 * NOTE:
	 * if VBR  tags are turned off by the user, or turned off by LAME because
	 * the output is not a regular file, this call does nothing
	 * NOTE:
	 * LAME wants to read from the file to skip an optional ID3v2 tag, so
	 * make sure you opened the file for writing and reading.
	 * NOTE:
	 * You can call lame_get_lametag_frame instead, if you want to insert
	 * the lametag yourself.
	*/
	public static final void lame_mp3_tags_fid(final LAME_GlobalFlags gfp, final RandomAccessFile fpStream ) {
		if( ! gfp.is_lame_global_flags_valid() ) {
			return;
		}
		final LAME_InternalFlags gfc = gfp.internal_flags;
		if( ! gfc.is_lame_internal_flags_valid() ) {
			return;
		}
		final SessionConfig cfg = gfc.cfg;
		if( ! cfg.write_lame_tag ) {
			return;
		}
		/* Write Xing header again */
		try {
			if( fpStream != null ) {
				fpStream.seek( 0 );
				final int rc = VBRTag.PutVbrTag( gfp, fpStream );
				switch( rc ) {
				default:
					/* OK */
					break;
				case -1:
					//System.err.print("Error: could not update LAME tag.\n");
					break;
				case -2:
					//System.err.print("Error: could not update LAME tag, file not seekable.\n");
					break;
				case -3:
					//System.err.print("Error: could not update LAME tag, file not readable.\n");
					break;
				}
			}
		} catch(final IOException ie) {
		}
	}

	private static final int lame_init_internal_flags(final LAME_InternalFlags gfc)
	{
		if( null == gfc ) {
			return -1;
		}

		gfc.cfg.vbr_min_bitrate_index = 1; /* not  0 ????? */
		gfc.cfg.vbr_max_bitrate_index = 13; /* not 14 ????? */

		gfc.sv_qnt.OldValue[0] = 180;
		gfc.sv_qnt.OldValue[1] = 180;
		gfc.sv_qnt.CurrentStep[0] = 4;
		gfc.sv_qnt.CurrentStep[1] = 4;
		gfc.sv_qnt.masking_lower = 1;

		/* The reason for
		 *       int mf_samples_to_encode = ENCDELAY + POSTDELAY;
		 * ENCDELAY = internal encoder delay.  And then we have to add POSTDELAY=288
		 * because of the 50% MDCT overlap.  A 576 MDCT granule decodes to
		 * 1152 samples.  To synthesize the 576 samples centered under this granule
		 * we need the previous granule for the first 288 samples (no problem), and
		 * the next granule for the next 288 samples (not possible if this is last
		 * granule).  So we need to pad with 288 samples to make sure we can
		 * encode the 576 samples we are interested in.
		 */
		gfc.sv_enc.mf_samples_to_encode = Encoder.ENCDELAY + Encoder.POSTDELAY;
		gfc.sv_enc.mf_size = Encoder.ENCDELAY - Encoder.MDCTDELAY; /* we pad input with this many 0's */
		gfc.ov_enc.encoder_padding = 0;
		gfc.ov_enc.encoder_delay = Encoder.ENCDELAY;

		gfc.ATH = new ATH();
		//if( null == gfc.ATH )
		//	return -2;      /* maybe error codes should be enumerated in lame.h ?? */

		return 0;
	}

	/** initialize mp3 encoder */
	private static final int lame_init_old(final LAME_GlobalFlags gfp) {
		// gfp.clear();// java: don't do it, because init is being called only after creating of the gfp

		gfp.class_id = LAME_InternalFlags.LAME_ID;

		/* Global flags.  set defaults here for non-zero values */
		/* see lame.h for description */
		/* set integer values to -1 to mean that LAME will compute the
		 * best value, UNLESS the calling program as set it
		 * (and the value is no longer -1)
		 */
		gfp.strict_ISO = MDB_MAXIMUM;

		gfp.mode = NOT_SET;
		gfp.original = true;
		gfp.samplerate = 44100;
		gfp.num_channels = 2;
		gfp.num_samples = Util.MAX_U_32_NUM;

		gfp.write_lame_tag = true;
		gfp.quality = -1;
		gfp.short_blocks = LAME_GlobalFlags.short_block_not_set;
		gfp.subblock_gain = -1;

		gfp.lowpassfreq = 0;
		gfp.highpassfreq = 0;
		gfp.lowpasswidth = -1;
		gfp.highpasswidth = -1;

		gfp.VBR = vbr_off;
		gfp.VBR_q = 4;
		gfp.VBR_mean_bitrate_kbps = 128;
		gfp.VBR_min_bitrate_kbps = 0;
		gfp.VBR_max_bitrate_kbps = 0;
		gfp.VBR_hard_min = false;

		gfp.quant_comp = -1;
		gfp.quant_comp_short = -1;

		gfp.msfix = -1;

		gfp.attackthre = -1;
		gfp.attackthre_s = -1;

		gfp.scale = 1;

		gfp.ATHcurve = -1;
		gfp.ATHtype = -1;  /* default = -1 = set in lame_init_params */
		/* 2 = equal loudness curve */
		gfp.athaa_sensitivity = 0.0f; /* no offset */
		gfp.athaa_type = -1;
		gfp.useTemporal = true;// -1;// java: used local flag to indicate uninit state
		gfp.interChRatio = -1;

		gfp.preset = 0;

		gfp.write_id3tag_automatic = true;

		gfp.internal_flags = new LAME_InternalFlags();

		if( lame_init_internal_flags( gfp.internal_flags ) < 0 ) {
			gfp.internal_flags = null;
			return -1;
		}
		return 0;
	}

	public static final LAME_GlobalFlags lame_init() {
		Util.init_log_table();

		final LAME_GlobalFlags gfp = new LAME_GlobalFlags();

		if( lame_init_old( gfp ) != 0 ) {
			return null;
		}

		gfp.lame_allocated_gfp = true;
		return gfp;
	}
}