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

	//private static final void CBR_iteration_loop(final InternalFlags gfc, final float pe[][]/*[2][2]*/,
	//		final float ms_ener_ratio[]/*[2]*/, final III_PsyRatio ratio[][]/*[2][2]*/)
	//@Override
	static final void iteration(final InternalFlags gfc, final float pe[][]/*[2][2]*/,
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
