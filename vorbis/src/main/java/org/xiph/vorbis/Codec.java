package org.xiph.vorbis;

/**
 * libvorbis encodes in two abstraction layers; first we perform DSP
 * and produce a packet (see docs/analysis.txt).  The packet is then
 * coded into a framed OggSquish bitstream by the second layer (see
 * docs/framing.txt).  Decode is the reverse process; we sync/frame
 * the bitstream and extract individual packets, then decode the
 * packet back into PCM audio.
 * <p>
 * The extra framing/packetizing is used in streaming formats, such as
 * files.  Over the net (such as with UDP), the framing and
 * packetization aren't necessary as they're provided by the transport
 * and the streaming layer is not used
 */
public class Codec {
    /**
     * Vorbis ERRORS and return codes
     ***********************************/

    public static final int OV_FALSE = -1;
    public static final int OV_EOF = -2;
    public static final int OV_HOLE = -3;

    public static final int OV_EREAD = -128;
    public static final int OV_EFAULT = -129;
    public static final int OV_EIMPL = -130;
    public static final int OV_EINVAL = -131;
    public static final int OV_ENOTVORBIS = -132;
    public static final int OV_EBADHEADER = -133;
    public static final int OV_EVERSION = -134;
    public static final int OV_ENOTAUDIO = -135;
    public static final int OV_EBADPACKET = -136;
    public static final int OV_EBADLINK = -137;
    public static final int OV_ENOSEEK = -138;

    /** registry for time, floor, res backends and channel mappings **/
    /**
     * seems like major overkill now; the backend numbers will grow into
     * the infrastructure soon enough
     */
    static final FuncFloor _floor_P[] = {
            new Floor0(),
            new Floor1(),
    };

    static final FuncResidue _residue_P[] = {
            new Residue0(),
            new Residue1(),
            new Residue2(),
    };

    static final FuncMapping _mapping_P[] = {
            new Mapping0(),
    };

    /**** linear scale -> dB, Bark and Mel scales *****/
    // FIXME todB and todB_nn returns different values in different variants!
    /* 20log10(x) */
    //#define VORBIS_IEEE_FLOAT32 1
    //#ifdef VORBIS_IEEE_FLOAT32

	/* extracted in place, see: LookPsy.noise_normalize()
	static float unitnorm(float x) {
		// Bit 31 (the bit that is selected by the mask 0x80000000)
		// represents the sign of the floating-point number
		// 0x3f800000 is the hex representation of the float 1.0f.
		int i = Float.floatToIntBits( x );
		i = (i & 0x80000000) | (0x3f800000);
		return Float.intBitsToFloat( i );
	}*/

    // Segher was off (too high) by ~ .3 decibel.  Center the conversion correctly.
    static float todB(final float x) {
        int i = Float.floatToIntBits(x);
        i &= 0x7fffffff;
        return (float) ((float) i * 7.17711438e-7f - 764.6161886f);
    }

	/* never used: static float todB_nn(float x) {
		int i = Float.floatToIntBits( x );
		i &= 0x7fffffff;
		return (float)((float)i * 7.17711438e-7f - 764.6161886f);
	}*/

//#else
/*
	extracted in place:
	static float unitnorm(float x) {
		if( x < 0 ) return (-1.f);
		return (1.f);
	}

	//#define todB(x)   (*(x)==0?-400.f:log(*(x)**(x))*4.34294480f)
	static float todB(float x) {
		return (x == 0 ? -400.f : (float)Math.log( x * x ) * 4.34294480f);
	}

	//#define todB_nn(x)   (*(x)==0.f?-400.f:log(*(x))*8.6858896f)
	static float todB_nn(float x) {// 20 / ln(10) = 8.68
		return (x == 0.f ? -400.f : (float)Math.log( x ) * 8.6858896f);
	}

//#endif
*/

    // TODO check what is faster variant for fromdB, toBARK, fromBARK, toMEL, fromMEL, toOC, fromOC

	/* extracted in place, see: Floor0.lsp_to_curve
	static float fromdB(float x) {
		return (float)(Math.exp( (double)(x * .11512925f) ));
		//return (float)(Math.exp( (double)x * .11512925 ));
	}*/

    /**
     * The bark scale equations are approximations, since the original
     * table was somewhat hand rolled.  The below are chosen to have the
     * best possible fit to the rolled tables, thus their somewhat odd
     * appearance (these are more accurate and over a longer range than
     * the oft-quoted bark equations found in the texts I have).  The
     * approximations are valid from 0 - 30kHz (nyquist) or so.
     * <p>
     * all f in Hz, z in Bark
     */
    static float toBARK(final float n) {
        //return (13.1f * (float)Math.atan( (double)(.00074f * n) ) +
        //		2.24f * (float)Math.atan( (double)(n * n * 1.85e-8f) ) +
        //		1e-4f * n);
        final double dn = (double) n;
        return (float) (13.1 * Math.atan(.00074 * dn) +
                2.24 * Math.atan(dn * dn * 1.85e-8) +
                1e-4 * dn);
    }

	/* used only in Barkmel
	static float fromBARK(float z) {
		//return (102.f * z - 2.f * (float)Math.pow( z, 2. ) +
		//		.4f * (float)Math.pow( z, 3. ) +
		//		(float)Math.pow( 1.46, z ) - 1.f);
		final double dz = (double)z;
		return (float)(102. * dz - 2. * Math.pow( dz, 2. ) +
				.4 * Math.pow( dz, 3. ) + Math.pow( 1.46, dz ) - 1.);
	}*/

	/* never used:
	static float toMEL(float n) {
		return ((float)Math.log( (double)(1.f + n * .001f) ) * 1442.695f);
		//return (float)(Math.log( 1. + (double)n * .001 ) * 1442.695);
	}

	static float fromMEL(float m) {
		return (1000.f * (float)Math.exp( (double)(m / 1442.695f) ) - 1000.f);
		//return (float)(1000. * Math.exp( ((double)m / 1442.695) ) - 1000.);
	}*/

    /**
     * Frequency to octave.  We arbitrarily declare 63.5 Hz to be octave
     * 0.0
     */

    static float toOC(final float n) {
        return ((float) Math.log((double) n) * 1.442695f - 5.965784f);
        //return (float)(Math.log( (double)n ) * 1.442695 - 5.965784);
    }

    static float fromOC(final float o) {
        return ((float) Math.exp((double) ((o + 5.965784f) * .693147f)));
        //return (float)(Math.exp(((double)o + 5.965784) * .693147));
    }

    /**** pack/unpack helpers ******************************************/

    static int ilog(int v) {
        int ret;
        for (ret = 0; v != 0; ret++) {
            v >>>= 1;
        }
        return ret;
    }

	/* 32 bit float (not IEEE; nonnormalized mantissa +
	   biased exponent) : neeeeeee eeemmmmm mmmmmmmm mmmmmmmm
	   Why not IEEE?  It's just not that important here. */

    //private static final int VQ_FEXP = 10;// never used
    private static final int VQ_FMAN = 21;
    private static final int VQ_FEXP_BIAS = 768; /* bias toward values smaller than 1. */

    /**
     * doesn't currently guard under/overflow
     */
	/*private static int _float32_pack(float val) {
		int sign = 0;
		int exp;
		int mant;
		if( val < 0 ) {
			sign = 0x80000000;
			val= -val;
		}
		exp = (int) Math.floor( Math.log( (double)val ) / Math.log( 2. ) + .001 ); //+epsilon
		mant = (int) Math.rint( ldexp( val, (VQ_FMAN - 1) -exp ) );
		exp = (exp + VQ_FEXP_BIAS) << VQ_FMAN;

		return (sign | exp | mant);
	}*/
    static float _float32_unpack(final int val) {
        double mant = (double) (val & 0x1fffff);
        // final int sign = val & 0x80000000;
        final int exp = (val & 0x7fe00000) >> VQ_FMAN;
        if (val < 0) {// if sign != 0
            mant = -mant;
        }
        return (float) (mant * Math.pow(2.0, (double) (exp - (VQ_FMAN - 1) - VQ_FEXP_BIAS)));
    }
}
