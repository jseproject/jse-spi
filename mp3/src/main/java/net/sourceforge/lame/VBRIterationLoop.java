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

class VBRIterationLoop extends Quantize/* implements IterationLoop */ {
	/*********************************************************************
	 *
	 *      VBR_prepare()
	 *
	 *  2000-09-04 Robert Hegemann
	 *
	 *  * converts LR to MS coding when necessary
	 *  * calculates allowed/adjusted quantization noise amounts
	 *  * detects analog silent frames
	 *
	 *  some remarks:
	 *  - lower masking depending on Quality setting
	 *  - quality control together with adjusted ATH MDCT scaling
	 *    on lower quality setting allocate more noise from
	 *    ATH masking, and on higher quality setting allocate
	 *    less noise from ATH masking.
	 *  - experiments show that going more than 2dB over GPSYCHO's
	 *    limits ends up in very annoying artefacts
	 *
	 *********************************************************************/

/* RH: this one needs to be overhauled sometime */
	private static final boolean VBR_old_prepare(final LAME_InternalFlags gfc,
		final float pe[][]/*[2][2]*/, final float ms_ener_ratio[/*2*/],
		final III_PsyRatio ratio[][]/*[2][2]*/,
		final float l3_xmin[][][]/*[2][2][SFBMAX]*/,
		final int frameBits[]/*[16]*/, final int min_bits[][]/*[2][2]*/, final int max_bits[][]/*[2][2]*/, final int bands[][]/*[2][2]*/)
	{
		final SessionConfig cfg = gfc.cfg;
		final EncResult eov = gfc.ov_enc;
		final III_GrInfo[][] tt = gfc.l3_side.tt;

		float adjust = 0.0f;
		boolean analog_silence = true;
		int bits = 0;

		eov.bitrate_index = cfg.vbr_max_bitrate_index;
		final int avg = ((int) Reservoir.ResvFrameBegin( gfc/*, &avg*/ )) / cfg.mode_gr;

		get_framebits( gfc, frameBits );

		final QntStateVar sv_qnt = gfc.sv_qnt;// java
		final int mode_gr = cfg.mode_gr;// java
		final int channels_out = cfg.channels_out;// java
		for( int gr = 0; gr < mode_gr; gr++ ) {
			final int mxb = QuantizePVT.on_pe( gfc, pe, max_bits[gr], avg, gr, false );
			if( gfc.ov_enc.mode_ext == Encoder.MPG_MD_MS_LR ) {
				gfc.l3_side.ms_convert( gr );
				QuantizePVT.reduce_side( max_bits[gr], ms_ener_ratio[gr], avg, mxb );
			}
			final III_GrInfo[] tt_gr = tt[gr];// java
			final float[] pe_gr = pe[gr];// java
			final III_PsyRatio[] ratio_gr = ratio[gr];// java
			final float[][] l3_xmin_gr = l3_xmin[gr];// java
			final int[] bands_gr = bands[gr];// java
			for( int ch = 0; ch < channels_out; ++ch ) {
				final III_GrInfo cod_info = tt_gr[ch];

				float masking_lower_db;
				if( cod_info.block_type != Encoder.SHORT_TYPE ) { /* NORM, START or STOP type */
					adjust = 1.28f / (1f + (float)Math.exp( (double)(3.5f - pe_gr[ch] / 300.f) )) - 0.05f;
					masking_lower_db = sv_qnt.mask_adjust - adjust;
				} else {
					adjust = 2.56f / (1f + (float)Math.exp( (double)(3.5f - pe_gr[ch] / 300.f) )) - 0.14f;
					masking_lower_db = sv_qnt.mask_adjust_short - adjust;
				}
				sv_qnt.masking_lower = (float)Math.pow( 10.0, (double)(masking_lower_db * 0.1f) );

				init_outer_loop( gfc, cod_info );
				bands_gr[ch] = QuantizePVT.calc_xmin( gfc, ratio_gr[ch], cod_info, l3_xmin_gr[ch] );
				if( bands_gr[ch] != 0 ) {
					analog_silence = false;
				}

				min_bits[gr][ch] = 126;

				bits += max_bits[gr][ch];
			}
		}
		final int max_frame_bits = frameBits[cfg.vbr_max_bitrate_index];// java
		for( int gr = 0; gr < mode_gr; gr++ ) {
			final int[] max_bits_gr = max_bits[gr];// java
			final int[] min_bits_gr = min_bits[gr];// java
			for( int ch = 0; ch < channels_out; ch++ ) {
				if( bits > max_frame_bits && bits > 0 ) {
					int v = max_bits_gr[ch];// java
					v *= max_frame_bits;
					v /= bits;
					max_bits_gr[ch] = v;
				}
				if( min_bits_gr[ch] > max_bits_gr[ch] ) {
					min_bits_gr[ch] = max_bits_gr[ch];
				}
			}               /* for ch */
		}                   /* for gr */

		return analog_silence;
	}

	private static final void bitpressure_strategy(final LAME_InternalFlags gfc,
			final float l3_xmin[][][]/*[2][2][SFBMAX]*/, final int min_bits[][]/*[2][2]*/, final int max_bits[][]/*[2][2]*/)
	{
		final SessionConfig cfg = gfc.cfg;
		final III_GrInfo[][] tt = gfc.l3_side.tt;
		final int mode_gr = cfg.mode_gr;// java
		final int channels_out = cfg.channels_out;// java

		for( int gr = 0; gr < mode_gr; gr++ ) {
			final III_GrInfo[] tt_gr = tt[gr];// java
			final float[][] l3_xmin_gr = l3_xmin[gr];// java
			final int[] max_bits_gr = max_bits[gr];// java
			final int[] min_bits_gr = min_bits[gr];// java
			for( int ch = 0; ch < channels_out; ch++ ) {
				final III_GrInfo gi = tt_gr[ch];
				final float[] xmin = l3_xmin_gr[ch];// java
				int pxmin = 0;
				for( int sfb = 0, psy_lmax = gi.psy_lmax; sfb < psy_lmax; sfb++ ) {
					xmin[pxmin++] *= 1.f + .029f * sfb * sfb / Encoder.SBMAX_l / Encoder.SBMAX_l;
				}

				if( gi.block_type == Encoder.SHORT_TYPE ) {
					for(int sfb = gi.sfb_smin; sfb < Encoder.SBMAX_s; sfb++ ) {
						final float v = 1.f + .029f * sfb * sfb / Encoder.SBMAX_s / Encoder.SBMAX_s;// java
						xmin[pxmin++] *= v;
						xmin[pxmin++] *= v;
						xmin[pxmin++] *= v;
					}
				}
				final int v1 = min_bits_gr[ch];
				final int v2 = (int)(0.9f * max_bits_gr[ch]);
				max_bits_gr[ch] = (v1 >= v2 ? v1 : v2);
			}
		}
	}

	/*********************************************************************
	 *
	 *      VBR_encode_granule()
	 *
	 *  2000-09-04 Robert Hegemann
	 *
	 *********************************************************************/
	private static final void VBR_encode_granule(final LAME_InternalFlags gfc, final III_GrInfo cod_info, final float[] l3_xmin, /* allowed distortion of the scalefactor */
                   final float xrpow[/*576*/], /* coloured magnitudes of spectral values */
                   final int ch, int min_bits, int max_bits)
	{
		final III_GrInfo bst_cod_info = new III_GrInfo();
		final float bst_xrpow[] = new float[576];
		final int Max_bits = max_bits;
		int real_bits = max_bits + 1;
		int this_bits = (max_bits + min_bits) >> 1;
		int found = 0;
		final boolean sfb21_extra = gfc.sv_qnt.sfb21_extra;

		final int[] buf = bst_cod_info.l3_enc;
		int i = buf.length;
		do {
			buf[--i] = 0;
		} while( i > 0 );

		/*  search within round about 40 bits of optimal */
		int dbits;
		do {

			if( this_bits > Max_bits - 42 ) {
				gfc.sv_qnt.sfb21_extra = false;
			} else {
				gfc.sv_qnt.sfb21_extra = sfb21_extra;
			}

			final int over = outer_loop( gfc, cod_info, l3_xmin, xrpow, ch, this_bits );

			/*  is quantization as good as we are looking for ?
			 *  in this case: is no scalefactor band distorted?
			 */
			if( over <= 0 ) {
				found = 1;
				/*  now we know it can be done with "real_bits"
				 *  and maybe we can skip some iterations
				 */
				real_bits = cod_info.part2_3_length;

				/*  store best quantization so far */
				bst_cod_info.copyFrom( cod_info );
				System.arraycopy( xrpow, 0, bst_xrpow, 0, 576 );

				/*  try with fewer bits */
				max_bits = real_bits - 32;
				dbits = max_bits - min_bits;
				this_bits = (max_bits + min_bits) >> 1;
			} else {
				/*  try with more bits */
				min_bits = this_bits + 32;
				dbits = max_bits - min_bits;
				this_bits = (max_bits + min_bits) >> 1;

				if( found != 0 ) {
					found = 2;
					/*  start again with best quantization so far */
					cod_info.copyFrom( bst_cod_info );
					System.arraycopy( bst_xrpow, 0, xrpow, 0, 576 );
				}
			}
		} while( dbits > 12 );

		gfc.sv_qnt.sfb21_extra = sfb21_extra;

		/*  found=0 => nothing found, use last one
		 *  found=1 => we just found the best and left the loop
		 *  found=2 => we restored a good one and have now l3_enc to restore too
		 */
		if( found == 2 ) {
			System.arraycopy( bst_cod_info.l3_enc, 0, cod_info.l3_enc, 0, 576 );
		}
	}

	/************************************************************************
	 *
	 *      VBR_iteration_loop()
	 *
	 *  tries to find out how many bits are needed for each granule and channel
	 *  to get an acceptable quantization. An appropriate bitrate will then be
	 *  choosed for quantization.  rh 8/99
	 *
	 *  Robert Hegemann 2000-09-06 rewrite
	 *
	 ************************************************************************/

	//private static final void VBR_old_iteration_loop(final LAME_InternalFlags gfc, final float pe[][]/*[2][2]*/,
	//		final float ms_ener_ratio[]/*[2]*/, final III_PsyRatio ratio[][]/*[2][2]*/)
	//@Override
	static final void iteration(final LAME_InternalFlags gfc, final float pe[][]/*[2][2]*/,
								final float ms_ener_ratio[]/*[2]*/, final III_PsyRatio ratio[][]/*[2][2]*/)
	{
		final SessionConfig cfg = gfc.cfg;
		final EncResult eov = gfc.ov_enc;
		final float l3_xmin[][][] = new float[2][2][Encoder.SFBMAX];

		final float xrpow[] = new float[576];
		final int bands[][] = new int[2][2];
		final int frameBits[] = new int[15];
		final int min_bits[][] = new int[2][2];
		final int max_bits[][] = new int[2][2];
		final III_GrInfo[][] tt = gfc.l3_side.tt;
		final int mode_gr = cfg.mode_gr;// java
		final int channels_out = cfg.channels_out;// java

		final boolean analog_silence = VBR_old_prepare( gfc, pe, ms_ener_ratio, ratio,
						 l3_xmin, frameBits, min_bits, max_bits, bands );

		/*---------------------------------*/
		int mean_bits;
		for( ;; ) {

			/*  quantize granules with lowest possible number of bits */

			int used_bits = 0;

			for( int gr = 0; gr < mode_gr; gr++ ) {
				final III_GrInfo[] tt_gr = tt[gr];// java
				final float[][] l3_xmin_gr = l3_xmin[gr];// java
				final int[] max_bits_gr = max_bits[gr];// java
				final int[] min_bits_gr = min_bits[gr];// java
				for( int ch = 0; ch < channels_out; ch++ ) {
					final III_GrInfo cod_info = tt_gr[ch];// java

					/*  init_outer_loop sets up cod_info, scalefac and xrpow */
					if( ! init_xrpow( gfc, cod_info, xrpow ) || max_bits_gr[ch] == 0 ) {
						/*  xr contains no energy
						 *  l3_enc, our encoding data, will be quantized to zero
						 */
						continue; /* with next channel */
					}

					VBR_encode_granule( gfc, cod_info, l3_xmin_gr[ch], xrpow,
							ch, min_bits_gr[ch], max_bits_gr[ch] );

					/*  do the 'substep shaping' */
					if( (gfc.sv_qnt.substep_shaping & 1) != 0 ) {
						trancate_smallspectrums( gfc, tt_gr[ch], l3_xmin_gr[ch], xrpow );
					}

					final int ret = cod_info.part2_3_length + cod_info.part2_length;
					used_bits += ret;
				}           /* for ch */
			}               /* for gr */

			/*  find lowest bitrate able to hold used bits */
			if( analog_silence && ! cfg.enforce_min_bitrate ) {
				eov.bitrate_index = 1;
			} else {
				eov.bitrate_index = cfg.vbr_min_bitrate_index;
			}

			for( ; eov.bitrate_index < cfg.vbr_max_bitrate_index; eov.bitrate_index++ ) {
				if( used_bits <= frameBits[eov.bitrate_index] ) {
					break;
				}
			}
			final long tmp = Reservoir.ResvFrameBegin( gfc/*, &mean_bits */);
			final int bits = (int)tmp;
			mean_bits = (int)(tmp >> 32);

			if( used_bits <= bits ) {
				break;
			}

			bitpressure_strategy( gfc, l3_xmin, min_bits, max_bits );

		}                   /* breaks adjusted */
		/*--------------------------------------*/

		for( int gr = 0; gr < mode_gr; gr++ ) {
			for( int ch = 0; ch < channels_out; ch++ ) {
				iteration_finish_one( gfc, gr, ch );
			}               /* for ch */
		}                   /* for gr */
		Reservoir.ResvFrameEnd( gfc, mean_bits );
	}
}
