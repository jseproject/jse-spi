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

// presets.c

class Presets {
/*
#define SET_OPTION(opt, val, def) if( enforce) \
    (void) lame_set_##opt(gfp, val ); \
    else if( !(fabs(lame_get_##opt(gfp) - def) > 0)) \
    (void) lame_set_##opt(gfp, val );

#define SET__OPTION(opt, val, def) if( enforce) \
    lame_set_##opt(gfp, val ); \
    else if( !(fabs(lame_get_##opt(gfp) - def) > 0)) \
    lame_set_##opt(gfp, val );
*/

	private static final class VBRPresets {
		private final int     vbr_q;
		private final int     quant_comp;
		private final int     quant_comp_s;
		private final boolean expY;
		private float   st_lrm;          /*short threshold */
		private float   st_s;
		private float   masking_adj;
		private float   masking_adj_short;
		private float   ath_lower;
		private float   ath_curve;
		private float   ath_sensitivity;
		private float   interch;
		private final int     safejoint;
		private int     sfb21mod;
		private float   msfix;
		private float   minval;
		private float   ath_fixpoint;
		//
		VBRPresets(final int ivbr_q, final int iquant_comp, final int iquant_comp_s, final boolean iexpY,
				   final float ist_lrm, final float ist_s, final float imasking_adj, final float imasking_adj_short,
				   final float iath_lower, final float iath_curve, final float iath_sensitivity, final float iinterch,
				   final int isafejoint, final int isfb21mod,
				   final float imsfix, final float iminval, final float iath_fixpoint)
		{
			this.vbr_q = ivbr_q;
			this.quant_comp = iquant_comp;
			this.quant_comp_s = iquant_comp_s;
			this.expY = iexpY;
			this.st_lrm = ist_lrm;
			this.st_s = ist_s;
			this.masking_adj = imasking_adj;
			this.masking_adj_short = imasking_adj_short;
			this.ath_lower = iath_lower;
			this.ath_curve = iath_curve;
			this.ath_sensitivity = iath_sensitivity;
			this.interch = iinterch;
			this.safejoint = isafejoint;
			this.sfb21mod = isfb21mod;
			this.msfix = imsfix;
			this.minval = iminval;
			this.ath_fixpoint = iath_fixpoint;
		}
		// #define LERP(m) (p.m = p.m + x * (q.m - p.m))
		private final void lerp(final float x, final VBRPresets q) {
			// this.vbr_q;
			// this.quant_comp;
			// this.quant_comp_s;
			// this.expY;
			this.st_lrm += x * (q.st_lrm - this.st_lrm);
			this.st_s += x * (q.st_s - this.st_s);
			this.masking_adj += x * (q.masking_adj - this.masking_adj);
			this.masking_adj_short += x * (q.masking_adj_short - this.masking_adj_short);
			this.ath_lower += x * (q.ath_lower - this.ath_lower);
			this.ath_curve += x * (q.ath_curve - this.ath_curve);
			this.ath_sensitivity += x * (q.ath_sensitivity - this.ath_sensitivity);
			this.interch += x * (q.interch - this.interch);
			// this.safejoint;
			this.sfb21mod += x * (q.sfb21mod - this.sfb21mod);
			this.msfix += x * (q.msfix - this.msfix);
			this.minval += x * (q.minval - this.minval);
			this.ath_fixpoint += x * (q.ath_fixpoint - this.ath_fixpoint);
		}
	};

/* *INDENT-OFF* */

	/* Switch mappings for VBR mode VBR_RH */
	private static final VBRPresets vbr_old_switch_map[] = {
/*vbr_q  qcomp_l  qcomp_s  expY  st_lrm   st_s  mask adj_l  adj_s  ath_lower  ath_curve  ath_sens  interChR  safejoint sfb21mod  msfix */
new VBRPresets(0,       9,       9, false,   5.20f, 125.0f,      -4.2f,   -6.3f,       4.8f,       1f,          0,   0f,              2,      21,  0.97f, 5, 100),
new VBRPresets(1,       9,       9, false,   5.30f, 125.0f,      -3.6f,   -5.6f,       4.5f,       1.5f,        0,   0f,              2,      21,  1.35f, 5, 100),
new VBRPresets(2,       9,       9, false,   5.60f, 125.0f,      -2.2f,   -3.5f,       2.8f,       2f,          0,   0f,              2,      21,  1.49f, 5, 100),
new VBRPresets(3,       9,       9,  true,   5.80f, 130.0f,      -1.8f,   -2.8f,       2.6f,       3f,         -4,   0f,              2,      20,  1.64f, 5, 100),
new VBRPresets(4,       9,       9,  true,   6.00f, 135.0f,      -0.7f,   -1.1f,       1.1f,       3.5f,       -8,   0f,              2,       0,  1.79f, 5, 100),
new VBRPresets(5,       9,       9,  true,   6.40f, 140.0f,       0.5f,    0.4f,      -7.5f,       4f,        -12,   0.0002f,         0,       0,  1.95f, 5, 100),
new VBRPresets(6,       9,       9,  true,   6.60f, 145.0f,       0.67f,   0.65f,    -14.7f,       6.5f,      -19,   0.0004f,         0,       0,  2.30f, 5, 100),
new VBRPresets(7,       9,       9,  true,   6.60f, 145.0f,       0.8f,    0.75f,    -19.7f,       8f,        -22,   0.0006f,         0,       0,  2.70f, 5, 100),
new VBRPresets(8,       9,       9,  true,   6.60f, 145.0f,       1.2f,    1.15f,    -27.5f,      10f,        -23,   0.0007f,         0,       0,  0f,    5, 100),
new VBRPresets(9,       9,       9,  true,   6.60f, 145.0f,       1.6f,    1.6f,     -36f,        11f,        -25,   0.0008f,         0,       0,  0f,    5, 100),
new VBRPresets(10,      9,       9,  true,   6.60f, 145.0f,       2.0f,    2.0f,     -36f,        12f,        -25,   0.0008f,         0,       0,  0f,    5, 100)
};

	private static final VBRPresets vbr_mt_psy_switch_map[] = {
/*vbr_q  qcomp_l  qcomp_s  expY  st_lrm   st_s  mask adj_l  adj_s  ath_lower  ath_curve  ath_sens  ---  safejoint sfb21mod  msfix */
new VBRPresets(0,       9,       9, false,   4.20f,  25.0f,      -6.8f,   -6.8f,       7.1f,       1f,          0,   0,         2,      31,  1.000f,  5, 100f),
new VBRPresets(1,       9,       9, false,   4.20f,  25.0f,      -4.8f,   -4.8f,       5.4f,       1.4f,       -1,   0,         2,      27,  1.122f,  5,  98f),
new VBRPresets(2,       9,       9, false,   4.20f,  25.0f,      -2.6f,   -2.6f,       3.7f,       2.0f,       -3,   0,         2,      23,  1.288f,  5,  97f),
new VBRPresets(3,       9,       9,  true,   4.20f,  25.0f,      -1.6f,   -1.6f,       2.0f,       2.0f,       -5,   0,         2,      18,  1.479f,  5,  96f),
new VBRPresets(4,       9,       9,  true,   4.20f,  25.0f,      -0.0f,   -0.0f,       0.0f,       2.0f,       -8,   0,         2,      12,  1.698f,  5,  95f),
new VBRPresets(5,       9,       9,  true,   4.20f,  25.0f,       1.3f,    1.3f,      -6f,         3.5f,      -11,   0f,         2,       8,  1.950f,  5,  94.2f),
/*#if 0
new VBRPresets(6,       9,       9,  true,   4.50f, 100.0f,       1.5f,    1.5f,     -24.0f,       6.0f,      -14,   0,         2,       4,  2.239,  3,  93.9f),
new VBRPresets(7,       9,       9,  true,   4.80f, 200.0f,       1.7f,    1.7f,     -28.0f,       9.0f,      -20,   0,         2,       0,  2.570,  1,  93.6f),
#else */
new VBRPresets(6,       9,       9,  true,   4.50f, 100.0f,       2.2f,    2.3f,     -12.0f,       6.0f,      -14,   0,         2,       4,  2.239f,  3,  93.9f),
new VBRPresets(7,       9,       9,  true,   4.80f, 200.0f,       2.7f,    2.7f,     -18.0f,       9.0f,      -17,   0,         2,       0,  2.570f,  1,  93.6f),
// #endif
new VBRPresets(8,       9,       9,  true,   5.30f, 300.0f,       2.8f,    2.8f,     -21.0f,      10.0f,      -23,   0.0002f,    0,       0,  2.951f,  0,  93.3f),
new VBRPresets(9,       9,       9,  true,   6.60f, 300.0f,       2.8f,    2.8f,     -23.0f,      11.0f,      -25,   0.0006f,    0,       0,  3.388f,  0,  93.3f),
new VBRPresets(10,      9,       9,  true,  25.00f, 300.0f,       2.8f,    2.8f,     -25.0f,      12.0f,      -27,   0.0025f,    0,       0,  3.500f,  0,  93.3f)
};

/* *INDENT-ON* */

	private static final VBRPresets[] get_vbr_preset(final int v) {
		switch( v ) {
		case LAME.vbr_mtrh:
		case LAME.vbr_mt:
			return vbr_mt_psy_switch_map;
		default:
			return vbr_old_switch_map;
		}
	}

	private static final void apply_vbr_preset(final LAME_GlobalFlags gfp, final int a, final boolean enforce) {
		final VBRPresets vbr_preset[] = get_vbr_preset( gfp.lame_get_VBR() );
		float x = gfp.VBR_q_frac;
		final VBRPresets p = vbr_preset[a];
		final VBRPresets q = vbr_preset[a + 1];
		final VBRPresets set = p;
		// java: moved to VBRPresets.lerp
		p.lerp( x, q );
		/*
		NOOP( vbr_q );
		NOOP( quant_comp );
		NOOP( quant_comp_s );
		NOOP( expY );
		LERP( st_lrm );
		LERP( st_s );
		LERP( masking_adj );
		LERP( masking_adj_short );
		LERP( ath_lower );
		LERP( ath_curve );
		LERP( ath_sensitivity );
		LERP( interch );
		NOOP( safejoint );
		LERP( sfb21mod );
		LERP( msfix );
		LERP( minval );
		LERP( ath_fixpoint );
		*/

		gfp.lame_set_VBR_q( set.vbr_q );
		if( enforce ) {
			gfp.lame_set_quant_comp( set.quant_comp );
		} else if( !(Math.abs( gfp.lame_get_quant_comp() - (-1) ) > 0) ) {
			gfp.lame_set_quant_comp( set.quant_comp );
		}

		if( enforce ) {
			gfp.lame_set_quant_comp_short( set.quant_comp_s );
		} else if( !(Math.abs( gfp.lame_get_quant_comp_short() - (-1) ) > 0) ) {
			gfp.lame_set_quant_comp_short( set.quant_comp_s );
		}

		if( set.expY ) {
			gfp.lame_set_experimentalY( set.expY );
		}

		if( enforce ) {
			gfp.lame_set_short_threshold_lrm( set.st_lrm );
		} else if( !(Math.abs( gfp.lame_get_short_threshold_lrm() - (-1) ) > 0) ) {
			gfp.lame_set_short_threshold_lrm( set.st_lrm );
		}
		if( enforce ) {
			gfp.lame_set_short_threshold_s( set.st_s );
		} else if( !(Math.abs( gfp.lame_get_short_threshold_s() - (-1) ) > 0) ) {
			gfp.lame_set_short_threshold_s( set.st_s );
		}
		if( enforce ) {
			gfp.lame_set_maskingadjust( set.masking_adj );
		} else if( !(Math.abs( gfp.lame_get_maskingadjust() - (0) ) > 0) ) {
			gfp.lame_set_maskingadjust( set.masking_adj );
		}
		if( enforce ) {
			gfp.lame_set_maskingadjust_short( set.masking_adj_short );
		} else if( !(Math.abs( gfp.lame_get_maskingadjust_short() - (0) ) > 0) ) {
			gfp.lame_set_maskingadjust_short( set.masking_adj_short );
		}
		if( gfp.lame_get_VBR() == LAME.vbr_mt || gfp.lame_get_VBR() == LAME.vbr_mtrh ) {
			gfp.lame_set_ATHtype( 5 );
		}
		if( enforce ) {
			gfp.lame_set_ATHlower( set.ath_lower );
		} else if( !(Math.abs( gfp.lame_get_ATHlower() - (0) ) > 0) ) {
			gfp.lame_set_ATHlower( set.ath_lower );
		}
		if( enforce ) {
			gfp.lame_set_ATHcurve( set.ath_curve );
		} else if( !(Math.abs( gfp.lame_get_ATHcurve() - (-1) ) > 0) ) {
			gfp.lame_set_ATHcurve( set.ath_curve );
		}
		if( enforce ) {
			gfp.lame_set_athaa_sensitivity( set.ath_sensitivity );
		} else if( !(Math.abs( gfp.lame_get_athaa_sensitivity() - (0) ) > 0) ) {
			gfp.lame_set_athaa_sensitivity( set.ath_sensitivity );
		}
		if( set.interch > 0 ) {
			if( enforce ) {
				gfp.lame_set_interChRatio( set.interch );
			} else if( !(Math.abs( gfp.lame_get_interChRatio() - (-1) ) > 0) ) {
				gfp.lame_set_interChRatio( set.interch );
			}
		}

		/* parameters for which there is no proper set/get interface */
		if( set.safejoint > 0 ) {
			gfp.lame_set_exp_nspsytune( gfp.lame_get_exp_nspsytune() | 2 );
		}
		if( set.sfb21mod > 0 ) {
			final int nsp = gfp.lame_get_exp_nspsytune();
			final int val = (nsp >> 20) & 63;
			if( val == 0 ) {
				final int sf21mod = (set.sfb21mod << 20) | nsp;
				gfp.lame_set_exp_nspsytune( sf21mod );
			}
		}

		if( enforce ) {
			gfp.lame_set_msfix( set.msfix );
		} else if( !(Math.abs( gfp.lame_get_msfix() - (-1) ) > 0) ) {
			gfp.lame_set_msfix( set.msfix );
		}

		if( ! enforce ) {
			gfp.VBR_q = a;
			gfp.VBR_q_frac = x;
		}
		gfp.internal_flags.cfg.minval = set.minval;
		gfp.internal_flags.cfg.ATHfixpoint = set.ath_fixpoint;
		{   /* take care of gain adjustments */
			x = Math.abs( gfp.scale );
			final float y = (x > 0.f) ? (10.f * (float)Math.log10( x ) ) : 0.f;
			gfp.internal_flags.cfg.ATHfixpoint = set.ath_fixpoint - y;
		}
	}

	private static final class ABRPresets {
		int     abr_kbps;
		int     quant_comp;
		int     quant_comp_s;
		int     safejoint;
		float   nsmsfix;
		float   st_lrm;      /*short threshold */
		float   st_s;
		float   scale;
		float   masking_adj;
		float   ath_lower;
		float   ath_curve;
		float   interch;
		int     sfscale;
		//
		ABRPresets(final int iabr_kbps, final int iquant_comp, final int iquant_comp_s, final int isafejoint,
				   final float insmsfix, final float ist_lrm, final float ist_s, final float iscale,
				   final float imasking_adj, final float iath_lower, final float iath_curve, final float iinterch,
				   final int isfscale) {
			this.abr_kbps = iabr_kbps;
			this.quant_comp = iquant_comp;
			this.quant_comp_s = iquant_comp_s;
			this.safejoint = isafejoint;
			this.nsmsfix = insmsfix;
			this.st_lrm = ist_lrm;
			this.st_s = ist_s;
			this.scale = iscale;
			this.masking_adj = imasking_adj;
			this.ath_lower = iath_lower;
			this.ath_curve = iath_curve;
			this.interch = iinterch;
			this.sfscale = isfscale;
		}
	}

	/* Switch mappings for ABR mode */
	private static final ABRPresets abr_switch_map[] = {
	/* kbps  quant q_s safejoint nsmsfix st_lrm  st_s  scale   msk ath_lwr ath_curve  interch , sfscale */
new ABRPresets(  8,     9,  9,        0,      0f,  6.60f,  145,  0.95f,    0,  -30.0f,     11f,    0.0012f,        1), /*   8, impossible to use in stereo */
new ABRPresets( 16,     9,  9,        0,      0f,  6.60f,  145,  0.95f,    0,  -25.0f,     11f,    0.0010f,        1), /*  16 */
new ABRPresets( 24,     9,  9,        0,      0f,  6.60f,  145,  0.95f,    0,  -20.0f,     11f,    0.0010f,        1), /*  24 */
new ABRPresets( 32,     9,  9,        0,      0f,  6.60f,  145,  0.95f,    0,  -15.0f,     11f,    0.0010f,        1), /*  32 */
new ABRPresets( 40,     9,  9,        0,      0f,  6.60f,  145,  0.95f,    0,  -10.0f,     11f,    0.0009f,        1), /*  40 */
new ABRPresets( 48,     9,  9,        0,      0f,  6.60f,  145,  0.95f,    0,  -10.0f,     11f,    0.0009f,        1), /*  48 */
new ABRPresets( 56,     9,  9,        0,      0f,  6.60f,  145,  0.95f,    0,   -6.0f,     11f,    0.0008f,        1), /*  56 */
new ABRPresets( 64,     9,  9,        0,      0f,  6.60f,  145,  0.95f,    0,   -2.0f,     11f,    0.0008f,        1), /*  64 */
new ABRPresets( 80,     9,  9,        0,      0f,  6.60f,  145,  0.95f,    0,     .0f,      8f,    0.0007f,        1), /*  80 */
new ABRPresets( 96,     9,  9,        0,   2.50f,  6.60f,  145,  0.95f,    0,    1.0f,      5.5f,  0.0006f,        1), /*  96 */
new ABRPresets(112,     9,  9,        0,   2.25f,  6.60f,  145,  0.95f,    0,    2.0f,      4.5f,  0.0005f,        1), /* 112 */
new ABRPresets(128,     9,  9,        0,   1.95f,  6.40f,  140,  0.95f,    0,    3.0f,      4f,    0.0002f,        1), /* 128 */
new ABRPresets(160,     9,  9,        1,   1.79f,  6.00f,  135,  0.95f,   -2,    5.0f,      3.5f,  0f,             1), /* 160 */
new ABRPresets(192,     9,  9,        1,   1.49f,  5.60f,  125,  0.97f,   -4,    7.0f,      3f,    0f,             0), /* 192 */
new ABRPresets(224,     9,  9,        1,   1.25f,  5.20f,  125,  0.98f,   -6,    9.0f,      2f,    0f,             0), /* 224 */
new ABRPresets(256,     9,  9,        1,   0.97f,  5.20f,  125,  1.00f,   -8,   10.0f,      1f,    0f,             0), /* 256 */
new ABRPresets(320,     9,  9,        1,   0.90f,  5.20f,  125,  1.00f,  -10,   12.0f,      0f,    0f,             0)  /* 320 */
	};

	private static final int apply_abr_preset(final LAME_GlobalFlags gfp, final int preset, final boolean enforce) {

		/* Variables for the ABR stuff */
		final int actual_bitrate = preset;
		final int r = Util.nearestBitrateFullIndex( preset );

		gfp.lame_set_VBR( LAME.vbr_abr );
		gfp.lame_set_VBR_mean_bitrate_kbps( (actual_bitrate) );
		gfp.lame_set_VBR_mean_bitrate_kbps( Math.min( gfp.lame_get_VBR_mean_bitrate_kbps(), 320 ) );
		gfp.lame_set_VBR_mean_bitrate_kbps( Math.max( gfp.lame_get_VBR_mean_bitrate_kbps(), 8 ) );
		gfp.lame_set_brate( gfp.lame_get_VBR_mean_bitrate_kbps() );


		/* parameters for which there is no proper set/get interface */
		if( abr_switch_map[r].safejoint > 0 ) {
			gfp.lame_set_exp_nspsytune( gfp.lame_get_exp_nspsytune() | 2 );
		} /* safejoint */

		if( abr_switch_map[r].sfscale > 0 ) {
			gfp.lame_set_sfscale( true );
		}

		if( enforce ) {
			gfp.lame_set_quant_comp( abr_switch_map[r].quant_comp );
		} else if( !(Math.abs( gfp.lame_get_quant_comp() - (-1) ) > 0) ) {
			gfp.lame_set_quant_comp( abr_switch_map[r].quant_comp );
		}
		if( enforce ) {
			gfp.lame_set_quant_comp_short( abr_switch_map[r].quant_comp_s );
		} else if( !(Math.abs( gfp.lame_get_quant_comp_short() - (-1) ) > 0) ) {
			gfp.lame_set_quant_comp_short( abr_switch_map[r].quant_comp_s );
		}

		if( enforce ) {
			gfp.lame_set_msfix( abr_switch_map[r].nsmsfix );
		} else if( !(Math.abs( gfp.lame_get_msfix() - (-1) ) > 0) ) {
			gfp.lame_set_msfix( abr_switch_map[r].nsmsfix );
		}

		if( enforce ) {
			gfp.lame_set_short_threshold_lrm( abr_switch_map[r].st_lrm );
		} else if( !(Math.abs( gfp.lame_get_short_threshold_lrm() - (-1) ) > 0) ) {
			gfp.lame_set_short_threshold_lrm( abr_switch_map[r].st_lrm );
		}

		if( enforce ) {
			gfp.lame_set_short_threshold_s( abr_switch_map[r].st_s );
		} else if( !(Math.abs( gfp.lame_get_short_threshold_s() - (-1) ) > 0) ) {
			gfp.lame_set_short_threshold_s( abr_switch_map[r].st_s );
		}

		/* ABR seems to have big problems with clipping, especially at low bitrates */
		/* so we compensate for that here by using a scale value depending on bitrate */
		gfp.lame_set_scale( gfp.lame_get_scale() * abr_switch_map[r].scale );

		if( enforce ) {
			gfp.lame_set_maskingadjust( abr_switch_map[r].masking_adj );
		} else if( !(Math.abs( gfp.lame_get_maskingadjust() - (0) ) > 0) ) {
			gfp.lame_set_maskingadjust( abr_switch_map[r].masking_adj );
		}
		if( abr_switch_map[r].masking_adj > 0 ) {
			if( enforce ) {
				gfp.lame_set_maskingadjust_short( abr_switch_map[r].masking_adj * .9f );
			} else if( !(Math.abs( gfp.lame_get_maskingadjust_short() - (0) ) > 0) ) {
				gfp.lame_set_maskingadjust_short( abr_switch_map[r].masking_adj * .9f );
			}
		} else {
			if( enforce ) {
				gfp.lame_set_maskingadjust_short( abr_switch_map[r].masking_adj * 1.1f );
			} else if( !(Math.abs( gfp.lame_get_maskingadjust_short() - (0) ) > 0) ) {
				gfp.lame_set_maskingadjust_short( abr_switch_map[r].masking_adj * 1.1f );
			}
		}

		if( enforce ) {
			gfp.lame_set_ATHlower( abr_switch_map[r].ath_lower );
		} else if( !(Math.abs( gfp.lame_get_ATHlower() - (0) ) > 0) ) {
			gfp.lame_set_ATHlower( abr_switch_map[r].ath_lower );
		}
		if( enforce ) {
			gfp.lame_set_ATHcurve( abr_switch_map[r].ath_curve );
		} else if( !(Math.abs( gfp.lame_get_ATHcurve() - (-1) ) > 0) ) {
			gfp.lame_set_ATHcurve( abr_switch_map[r].ath_curve );
		}

		if( enforce ) {
			gfp.lame_set_interChRatio( abr_switch_map[r].interch );
		} else if( !(Math.abs( gfp.lame_get_interChRatio() - (-1) ) > 0) ) {
			gfp.lame_set_interChRatio( abr_switch_map[r].interch );
		}

		// abr_switch_map[r].abr_kbps;

		gfp.internal_flags.cfg.minval = 5.f * (abr_switch_map[r].abr_kbps / 320.f);

		return preset;
	}

	static final int apply_preset(final LAME_GlobalFlags gfp, int preset, final boolean enforce ) {
		/*translate legacy presets */
		switch( preset ) {
		case LAME.R3MIX: {
			preset = LAME.V3;
			gfp.lame_set_VBR( LAME.vbr_mtrh );
			break;
		}
		case LAME.MEDIUM:
		case LAME.MEDIUM_FAST: {
			preset = LAME.V4;
			gfp.lame_set_VBR( LAME.vbr_mtrh );
			break;
		}
		case LAME.STANDARD:
		case LAME.STANDARD_FAST: {
			preset = LAME.V2;
			gfp.lame_set_VBR( LAME.vbr_mtrh );
			break;
		}
		case LAME.EXTREME:
		case LAME.EXTREME_FAST: {
			preset = LAME.V0;
			gfp.lame_set_VBR( LAME.vbr_mtrh );
			break;
		}
		case LAME.INSANE: {
			preset = 320;
			gfp.preset = preset;
			apply_abr_preset( gfp, preset, enforce );
			gfp.lame_set_VBR( LAME.vbr_off );
			return preset;
		}
		}

		gfp.preset = preset;
		{
			switch( preset ) {
			case LAME.V9:
				apply_vbr_preset( gfp, 9, enforce );
				return preset;
			case LAME.V8:
				apply_vbr_preset( gfp, 8, enforce );
				return preset;
			case LAME.V7:
				apply_vbr_preset( gfp, 7, enforce );
				return preset;
			case LAME.V6:
				apply_vbr_preset( gfp, 6, enforce );
				return preset;
			case LAME.V5:
				apply_vbr_preset( gfp, 5, enforce );
				return preset;
			case LAME.V4:
				apply_vbr_preset( gfp, 4, enforce );
				return preset;
			case LAME.V3:
				apply_vbr_preset( gfp, 3, enforce );
				return preset;
			case LAME.V2:
				apply_vbr_preset( gfp, 2, enforce );
				return preset;
			case LAME.V1:
				apply_vbr_preset( gfp, 1, enforce );
				return preset;
			case LAME.V0:
				apply_vbr_preset( gfp, 0, enforce );
				return preset;
			default:
				break;
			}
		}
		if( 8 <= preset && preset <= 320 ) {
			return apply_abr_preset( gfp, preset, enforce );
		}

		gfp.preset = 0;    /*no corresponding preset found */
		return preset;
	}
}