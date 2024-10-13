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

class CBRIterationLoop extends Quantize/* implements IterationLoop */{
	/************************************************************************
	 *
	 *      CBR_iteration_loop()
	 *
	 *  author/date??
	 *
	 *  encodes one frame of MP3 data with constant bitrate
	 *
	 ************************************************************************/

	//private static final void CBR_iteration_loop(final LAME_InternalFlags gfc, final float pe[][]/*[2][2]*/,
	//		final float ms_ener_ratio[]/*[2]*/, final III_PsyRatio ratio[][]/*[2][2]*/)
	//@Override
	static final void iteration(final LAME_InternalFlags gfc, final float pe[][]/*[2][2]*/,
								final float ms_ener_ratio[]/*[2]*/, final III_PsyRatio ratio[][]/*[2][2]*/)
	{
		final SessionConfig cfg = gfc.cfg;
		final float l3_xmin[] = new float[Encoder.SFBMAX];
		final float xrpow[] = new float[576];
		final int targ_bits[] = new int[2];
		final III_GrInfo[][] tt = gfc.l3_side.tt;

		final int mean_bits = (int)(Reservoir.ResvFrameBegin( gfc/*, &mean_bits*/ ) >> 32);

		/* quantize! */
		final int mode_gr = cfg.mode_gr;// java
		final int channels_out = cfg.channels_out;// java
		final QntStateVar sv_qnt = gfc.sv_qnt;// java
		int gr = 0;
		do {

			/* calculate needed bits */
			final int max_bits = QuantizePVT.on_pe( gfc, pe, targ_bits, mean_bits, gr, gr != 0 );

			if( gfc.ov_enc.mode_ext == Encoder.MPG_MD_MS_LR ) {
				gfc.l3_side.ms_convert( gr );
				QuantizePVT.reduce_side( targ_bits, ms_ener_ratio[gr], mean_bits, max_bits );
			}

			final III_GrInfo[] tt_gr = tt[gr];// java
			int ch = 0;
			do {
				float adjust, masking_lower_db;
				final III_GrInfo cod_info = tt_gr[ch];

				if( cod_info.block_type != Encoder.SHORT_TYPE ) { /* NORM, START or STOP type */
					/* adjust = 1.28/(1+exp(3.5-pe[gr][ch]/300.))-0.05; */
					adjust = 0;
					masking_lower_db = sv_qnt.mask_adjust - adjust;
				} else {
					/* adjust = 2.56/(1+exp(3.5-pe[gr][ch]/300.))-0.14; */
					adjust = 0;
					masking_lower_db = sv_qnt.mask_adjust_short - adjust;
				}
				sv_qnt.masking_lower = (float)Math.pow( 10.0, (double)(masking_lower_db * 0.1f) );

				/*  init_outer_loop sets up cod_info, scalefac and xrpow */
				init_outer_loop( gfc, cod_info );
				if( init_xrpow( gfc, cod_info, xrpow ) ) {
					/*  xr contains energy we will have to encode
					 *  calculate the masking abilities
					 *  find some good quantization in outer_loop
					 */
					QuantizePVT.calc_xmin( gfc, ratio[gr][ch], cod_info, l3_xmin );
					outer_loop( gfc, cod_info, l3_xmin, xrpow, ch, targ_bits[ch] );
				}

				iteration_finish_one( gfc, gr, ch );
			} while( ++ch < channels_out );  /* for ch */
		} while( ++gr < mode_gr );   /* for gr */

		Reservoir.ResvFrameEnd( gfc, mean_bits );
	}
}
