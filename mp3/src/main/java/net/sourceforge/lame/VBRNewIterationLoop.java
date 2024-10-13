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

class VBRNewIterationLoop extends Quantize/* implements IterationLoop */{
	/**
	 * @return java: (analog_silence << 31) | (max_resv)
	 */
	private static final int VBR_new_prepare(final LAME_InternalFlags gfc,
		final float pe[][]/*[2][2]*/, final III_PsyRatio ratio[][]/*[2][2]*/,
		final float l3_xmin[][][]/*[2][2][SFBMAX]*/, final int frameBits[]/*[16]*/, final int max_bits[][]/*[2][2]*///,
		)//final int[] max_resv)
	{
		final SessionConfig cfg = gfc.cfg;
		final EncResult eov = gfc.ov_enc;

		int analog_silence = 0x80000000;
		int bits = 0;

		int avg, max_resv, maximum_framebits;
		if( ! cfg.free_format ) {
			eov.bitrate_index = cfg.vbr_max_bitrate_index;
			avg = (int)(Reservoir.ResvFrameBegin( gfc/*, &avg*/ ) >> 32);
			max_resv = gfc.sv_enc.ResvMax;

			get_framebits( gfc, frameBits );
			maximum_framebits = frameBits[cfg.vbr_max_bitrate_index];
		} else {
			eov.bitrate_index = 0;
			final long tmp = Reservoir.ResvFrameBegin( gfc/*, &avg*/ );
			maximum_framebits = (int)tmp;
			avg = (int)(tmp >> 32);
			frameBits[0] = maximum_framebits;
			max_resv = gfc.sv_enc.ResvMax;
		}

		final III_GrInfo[][] tt = gfc.l3_side.tt;// java
		final QntStateVar sv_qnt = gfc.sv_qnt;// java
		final int mode_gr = cfg.mode_gr;// java
		final int channels_out = cfg.channels_out;// java
		for( int gr = 0; gr < mode_gr; gr++ ) {
			final int[] max_bits_gr = max_bits[gr];// java
			QuantizePVT.on_pe( gfc, pe, max_bits_gr, avg, gr, false );
			if( gfc.ov_enc.mode_ext == Encoder.MPG_MD_MS_LR ) {
				gfc.l3_side.ms_convert( gr );
			}
			final III_GrInfo[] tt_gr = tt[gr];// java
			final III_PsyRatio[] ratio_gr = ratio[gr];// java
			final float[][] l3_xmin_gr = l3_xmin[gr];// java
			for( int ch = 0; ch < channels_out; ++ch ) {
				sv_qnt.masking_lower = (float)Math.pow( 10.0, (double)(sv_qnt.mask_adjust * 0.1f) );
				final III_GrInfo cod_info = tt_gr[ch];
				init_outer_loop( gfc, cod_info );
				if( 0 != QuantizePVT.calc_xmin( gfc, ratio_gr[ch], cod_info, l3_xmin_gr[ch] ) ) {
					analog_silence = 0;
				}

				bits += max_bits_gr[ch];
			}
		}
		for( int gr = 0; gr < mode_gr; gr++ ) {
			final int[] max_bits_gr = max_bits[gr];// java
			for( int ch = 0; ch < channels_out; ch++ ) {
				if( bits > maximum_framebits && bits > 0 ) {
					int v = max_bits_gr[ch];// java
					v *= maximum_framebits;
					v /= bits;
					max_bits_gr[ch] = v;
				}
			}               /* for ch */
		}                   /* for gr */
		if( analog_silence != 0 ) {
			max_resv = 0;
		}
		return (analog_silence) | (max_resv);
	}
	//void VBR_new_iteration_loop(lame_internal_flags * gfc, const FLOAT pe[2][2],
	//				const FLOAT ms_ener_ratio[2], const III_psy_ratio ratio[2][2])
	// @Override
	static final void iteration(final LAME_InternalFlags gfc, final float pe[][], final float ms_ratio[], final III_PsyRatio ratio[][] ) {
		final SessionConfig cfg = gfc.cfg;
		final EncResult eov = gfc.ov_enc;
		final float l3_xmin[][][] = new float[2][2][Encoder.SFBMAX];

		final float xrpow[][][] = new float[2][2][576];// java: already zeroed
		final int frameBits[] = new int[15];
		final int max_bits[][] = new int[2][2];
		final III_GrInfo[][] tt = gfc.l3_side.tt;

		// ms_ener_ratio; /* not used */

		final int analog_silence = VBR_new_prepare( gfc, pe, ratio, l3_xmin, frameBits, max_bits/*, &pad*/ );
		final int pad = analog_silence & 0x7fffffff;// java: analog_silence highest bit

		final int mode_gr = cfg.mode_gr;// java
		final int channels_out = cfg.channels_out;// java
		for( int gr = 0; gr < mode_gr; gr++ ) {
			final III_GrInfo[] tt_gr = tt[gr];// java
			final int[] max_bits_gr = max_bits[gr];// java
			for( int ch = 0; ch < channels_out; ch++ ) {
				final III_GrInfo cod_info = tt_gr[ch];

				/*  init_outer_loop sets up cod_info, scalefac and xrpow */
				if( ! init_xrpow( gfc, cod_info, xrpow[gr][ch] ) ) {
					max_bits_gr[ch] = 0; /* silent granule needs no bits */
				}
			}               /* for ch */
		}                   /* for gr */

		/*  quantize granules with lowest possible number of bits */

		final int used_bits = VBRQuantize.VBR_encode_frame( gfc, xrpow, l3_xmin, max_bits );

		if( ! cfg.free_format ) {
			int i, j;

			/*  find lowest bitrate able to hold used bits */
			if( analog_silence < 0 && ! cfg.enforce_min_bitrate ) {// java analog_silence highest bit
				/*  we detected analog silence and the user did not specify
				 *  any hard framesize limit, so start with smallest possible frame
				 */
				i = 1;
			} else {
				i = cfg.vbr_min_bitrate_index;
			}

			final int vbr_max_bitrate_index = cfg.vbr_max_bitrate_index;// java
			for( ; i < vbr_max_bitrate_index; i++ ) {
				if( used_bits <= frameBits[i] ) {
					break;
				}
			}
			if( i > vbr_max_bitrate_index ) {
				i = vbr_max_bitrate_index;
			}
			if( pad > 0 ) {
				for( j = vbr_max_bitrate_index; j > i; --j ) {
					final int unused = frameBits[j] - used_bits;
					if( unused <= pad ) {
						break;
					}
				}
				eov.bitrate_index = j;
			} else {
				eov.bitrate_index = i;
			}
		} else {
/* #if 0
			static int mmm = 0;
			int     fff = getFramesize_kbps( gfc, used_bits );
			int     hhh = getFramesize_kbps( gfc, MAX_BITS_PER_GRANULE * cfg.mode_gr );
			if( mmm < fff )
			mmm = fff;
			printf( "demand=%3d kbps  max=%3d kbps   limit=%3d kbps\n", fff, mmm, hhh );
#endif */
			eov.bitrate_index = 0;
		}
		if( used_bits <= frameBits[eov.bitrate_index] ) {
			/* update Reservoire status */
			final long tmp = Reservoir.ResvFrameBegin( gfc/*, &mean_bits*/ );
			final int mean_bits = (int)(tmp >> 32);
			for( int gr = 0; gr < mode_gr; gr++ ) {
				final III_GrInfo[] tt_gr = tt[gr];// java
				for( int ch = 0; ch < channels_out; ch++ ) {
					final III_GrInfo cod_info = tt_gr[ch];
					Reservoir.ResvAdjust( gfc, cod_info );
				}
			}
			Reservoir.ResvFrameEnd( gfc, mean_bits );
			return;
		}// else {
			/* SHOULD NOT HAPPEN INTERNAL ERROR */
			//System.err.print("INTERNAL ERROR IN VBR NEW CODE, please send bug report\n");
			//System.exit( -1 );
			//return;
		throw new InternalError("INTERNAL ERROR IN VBR NEW CODE");
		//}
	}
}
