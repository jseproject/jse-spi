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

class ABRIterationLoop extends Quantize/* implements IterationLoop */{
	/********************************************************************
	 *
	 *  calc_target_bits()
	 *
	 *  calculates target bits for ABR encoding
	 *
	 *  mt 2000/05/31
	 *
	 * @return analog_silence_bits | (max_frame_bits << 32)
	 ********************************************************************/
	private static final long calc_target_bits(final LAME_InternalFlags gfc,
			final float pe[][]/*[2][2]*/,
			final float ms_ener_ratio[]/*[2]*/,
			final int targ_bits[][]/*[2][2]*//*, int[] analog_silence_bits, int[] max_frame_bits*/)
	{
		final SessionConfig cfg = gfc.cfg;
		final EncResult eov = gfc.ov_enc;
		final III_GrInfo[][] tt = gfc.l3_side.tt;
		final int mode_gr = cfg.mode_gr;// java
		final int framesize = 576 * mode_gr;

		eov.bitrate_index = cfg.vbr_max_bitrate_index;
		final long tmp = Reservoir.ResvFrameBegin( gfc/*, &mean_bits */);
		final int max_frame_bits = (int)tmp;
		int mean_bits = (int)(tmp >> 32);

		eov.bitrate_index = 1;
		mean_bits = Bitstream.getframebits( gfc ) - (cfg.sideinfo_len << 3);
		final int channels_out = cfg.channels_out;// java
		final int analog_silence_bits = mean_bits / (mode_gr * channels_out);

		mean_bits = cfg.vbr_avg_bitrate_kbps * framesize * 1000;
		if( (gfc.sv_qnt.substep_shaping & 1) != 0 ) {
			mean_bits *= 1.09f;
		}
		mean_bits /= cfg.samplerate;
		mean_bits -= cfg.sideinfo_len << 3;
		mean_bits /= (mode_gr * channels_out);

		/*
		   res_factor is the percentage of the target bitrate that should
		   be used on average.  the remaining bits are added to the
		   bitreservoir and used for difficult to encode frames.

		   Since we are tracking the average bitrate, we should adjust
		   res_factor "on the fly", increasing it if the average bitrate
		   is greater than the requested bitrate, and decreasing it
		   otherwise.  Reasonable ranges are from .9 to 1.0

		   Until we get the above suggestion working, we use the following
		   tuning:
		   compression ratio    res_factor
		   5.5  (256kbps)         1.0      no need for bitreservoir
		   11   (128kbps)         .93      7% held for reservoir

		   with linear interpolation for other values.

		 */
		float res_factor = .93f + .07f * (11.0f - cfg.compression_ratio) / (11.0f - 5.5f);
		if( res_factor < .90f ) {
			res_factor = .90f;
		}
		if( res_factor > 1.00f ) {
			res_factor = 1.00f;
		}
		final int bits = (int)(res_factor * mean_bits);// java

		for( int gr = 0; gr < mode_gr; gr++ ) {
			final int[] targ_bits_gr = targ_bits[gr];// java
			final III_GrInfo[] tt_gr = tt[gr];// java
			final float[] pe_gr = pe[gr];// java
			int sum = 0;
			int ch = 0;
			do {
				targ_bits_gr[ch] = bits;

				if( pe_gr[ch] > 700 ) {
					int add_bits = (int)((pe_gr[ch] - 700) / 1.4f);

					final III_GrInfo cod_info = tt_gr[ch];
					targ_bits_gr[ch] = bits;

					/* short blocks use a little extra, no matter what the pe */
					if( cod_info.block_type == Encoder.SHORT_TYPE ) {
						final int v = mean_bits >> 1;// java
						if( add_bits < v ) {
							add_bits = v;
						}
					}
					/* at most increase bits by 1.5*average */
					final int v = (mean_bits * 3) >> 1;// java
					if( add_bits > v ) {
						add_bits = v;
					} else if( add_bits < 0 ) {
						add_bits = 0;
					}

					targ_bits_gr[ch] += add_bits;
				}
				if( targ_bits_gr[ch] > Util.MAX_BITS_PER_CHANNEL ) {
					targ_bits_gr[ch] = Util.MAX_BITS_PER_CHANNEL;
				}
				sum += targ_bits_gr[ch];
			} while( ++ch < channels_out );               /* for ch */
			if( sum > Util.MAX_BITS_PER_GRANULE ) {
				ch = 0;
				do {
					targ_bits_gr[ch] *= Util.MAX_BITS_PER_GRANULE;
					targ_bits_gr[ch] /= sum;
				} while( ++ch < channels_out );
			}
		}                   /* for gr */

		if( gfc.ov_enc.mode_ext == Encoder.MPG_MD_MS_LR ) {
			mean_bits *= channels_out;
			for( int gr = 0; gr < mode_gr; gr++ ) {
				QuantizePVT.reduce_side( targ_bits[gr], ms_ener_ratio[gr], mean_bits, Util.MAX_BITS_PER_GRANULE );
			}
		}

		/*  sum target bits */
		int totbits = 0;
		for( int gr = 0; gr < mode_gr; gr++ ) {
			final int[] targ_bits_gr = targ_bits[gr];// java
			int ch = 0;
			do {
				int v = targ_bits_gr[ch];
				if( v > Util.MAX_BITS_PER_CHANNEL ) {
					v = Util.MAX_BITS_PER_CHANNEL;
					targ_bits_gr[ch] = v;
				}
				totbits += v;
			} while( ++ch < channels_out );
		}

		/*  repartion target bits if needed */
		if( totbits > max_frame_bits && totbits > 0 ) {
			for( int gr = 0; gr < mode_gr; gr++ ) {
				final int[] targ_bits_gr = targ_bits[gr];// java
				int ch = 0;
				do {
					int v = targ_bits_gr[ch];
					v *= max_frame_bits;
					v /= totbits;
					targ_bits_gr[ch] = v;
				} while( ++ch < channels_out );
			}
		}
		return (long)analog_silence_bits | ((long)max_frame_bits << 32);
	}
	/********************************************************************
	 *
	 *  ABR_iteration_loop()
	 *
	 *  encode a frame with a disired average bitrate
	 *
	 *  mt 2000/05/31
	 *
	 ********************************************************************/
	//private static final void ABR_iteration_loop(final LAME_InternalFlags gfc, final float pe[][]/*[2][2]*/,
    //               final float ms_ener_ratio[]/*[2]*/, final III_PsyRatio ratio[][]/*[2][2]*/)
	//@Override
	static final void iteration(final LAME_InternalFlags gfc, final float pe[][]/*[2][2]*/,
								final float ms_ener_ratio[]/*[2]*/, final III_PsyRatio ratio[][]/*[2][2]*/)
	{
		final SessionConfig cfg = gfc.cfg;
		final EncResult eov = gfc.ov_enc;
		final float l3_xmin[] = new float[Encoder.SFBMAX];
		final float xrpow[] = new float[576];
		final int targ_bits[][] = new int[2][2];
		final III_GrInfo[][] tt = gfc.l3_side.tt;

		int mean_bits = 0;

		long tmp = calc_target_bits( gfc, pe, ms_ener_ratio, targ_bits/*, &analog_silence_bits, &max_frame_bits */);
		final int analog_silence_bits = (int)tmp;
		/*  encode granules */
		final int mode_gr = cfg.mode_gr;// java
		final int channels_out = cfg.channels_out;// java
		for( int gr = 0; gr < mode_gr; gr++ ) {
			final int[] targ_bits_gr = targ_bits[gr];// java
			final III_PsyRatio[] pratio_gr = ratio[gr];// java
			final III_GrInfo[] tt_gr = tt[gr];// java

			if( gfc.ov_enc.mode_ext == Encoder.MPG_MD_MS_LR ) {
				gfc.l3_side.ms_convert( gr );
			}
			int ch = 0;
			do {
				float adjust, masking_lower_db;
				final III_GrInfo cod_info = tt_gr[ch];

				if( cod_info.block_type != Encoder.SHORT_TYPE ) { /* NORM, START or STOP type */
					/* adjust = 1.28/(1+exp(3.5-pe[gr][ch]/300.))-0.05; */
					adjust = 0;
					masking_lower_db = gfc.sv_qnt.mask_adjust - adjust;
				} else {
					/* adjust = 2.56/(1+exp(3.5-pe[gr][ch]/300.))-0.14; */
					adjust = 0;
					masking_lower_db = gfc.sv_qnt.mask_adjust_short - adjust;
				}
				gfc.sv_qnt.masking_lower = (float)Math.pow( 10.0, (double)(masking_lower_db * 0.1f) );


				/*  cod_info, scalefac and xrpow get initialized in init_outer_loop */
				init_outer_loop( gfc, cod_info );
				if( init_xrpow( gfc, cod_info, xrpow ) ) {
					/*  xr contains energy we will have to encode
					 *  calculate the masking abilities
					 *  find some good quantization in outer_loop
					 */
					final int ath_over = QuantizePVT.calc_xmin( gfc, pratio_gr[ch], cod_info, l3_xmin );
					if( 0 == ath_over ) {
						targ_bits_gr[ch] = analog_silence_bits;
					}

					outer_loop( gfc, cod_info, l3_xmin, xrpow, ch, targ_bits_gr[ch] );
				}
				iteration_finish_one( gfc, gr, ch );
			} while( ++ch < channels_out );              /* ch */
		}                   /* gr */

		/*  find a bitrate which can refill the resevoir to positive size. */
		for( eov.bitrate_index = cfg.vbr_min_bitrate_index;
				eov.bitrate_index <= cfg.vbr_max_bitrate_index; eov.bitrate_index++ ) {
			tmp = Reservoir.ResvFrameBegin( gfc/*, &mean_bits */);
			mean_bits = (int)(tmp >> 32);
			if( ((int)tmp) >= 0 ) {
				break;
			}
		}

		Reservoir.ResvFrameEnd( gfc, mean_bits );
	}
}
