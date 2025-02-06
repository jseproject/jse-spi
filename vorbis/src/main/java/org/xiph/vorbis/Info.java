package org.xiph.vorbis;

import java.io.UnsupportedEncodingException;

import org.xiph.vorbis.modes.Psych44;
import org.xiph.vorbis.modes.Setup11;
import org.xiph.vorbis.modes.Setup16;
import org.xiph.vorbis.modes.Setup22;
import org.xiph.vorbis.modes.Setup32;
import org.xiph.vorbis.modes.Setup44;
import org.xiph.vorbis.modes.Setup44p51;
import org.xiph.vorbis.modes.Setup44u;
import org.xiph.vorbis.modes.Setup8;
import org.xiph.vorbis.modes.SetupX;

/**
 * vorbis_info contains all the setup information specific to the
 * specific compression/decompression mode in progress (eg,
 * psychoacoustic settings, channel setup, options, codebook
 * etc). vorbis_info and substructures are in backends.h.
 */
public class Info {
    static final int VI_TRANSFORMB = 1;
    private static final int VI_WINDOWB = 1;
    private static final int VI_TIMEB = 1;
    private static final int VI_FLOORB = 2;
    private static final int VI_RESB = 3;
    private static final int VI_MAPB = 1;
    //
    static final int PACKETBLOBS = 15;
    //
    public int version = 0;
    public int channels = 0;
    public int rate = 0;

	/* The below bitrate declarations are *hints*.
	 Combinations of the three values carry the following implications:

	 all three set to the same value:
	   implies a fixed rate bitstream
	 only nominal set:
	   implies a VBR stream that averages the nominal bitrate.  No hard
	   upper/lower limit
	 upper and or lower set:
	   implies a VBR bitstream that obeys the bitrate limits. nominal
	   may also be set to give a nominal rate.
	 none set:
	   the coder does not care to speculate.
	*/

    int bitrate_upper = 0;
    int bitrate_nominal = 0;
    int bitrate_lower = 0;
    int bitrate_window = 0;

    CodecSetupInfo codec_setup = null;

    /************************* encoder settings *************************/
    /* a few static coder conventions */
    private static final InfoMode _mode_template[] = {// [2]
            new InfoMode(0, 0, 0, 0),
            new InfoMode(1, 0, 0, 1)
    };

    private static final EncSetupDataTemplate setup_list[] = {
            Setup44.setup_44_stereo,
            Setup44p51.setup_44_51,
            Setup44u.setup_44_uncoupled,

            Setup32.setup_32_stereo,
            Setup32.setup_32_uncoupled,

            Setup22.setup_22_stereo,
            Setup22.setup_22_uncoupled,
            Setup16.setup_16_stereo,
            Setup16.setup_16_uncoupled,

            Setup11.setup_11_stereo,
            Setup11.setup_11_uncoupled,
            Setup8.setup_8_stereo,
            Setup8.setup_8_uncoupled,

            SetupX.setup_X_stereo,
            SetupX.setup_X_uncoupled,
            SetupX.setup_XX_stereo,
            SetupX.setup_XX_uncoupled,
            null
    };

    private final void m_clear() {
        version = 0;
        channels = 0;
        rate = 0;
        bitrate_upper = 0;
        bitrate_nominal = 0;
        bitrate_lower = 0;
        bitrate_window = 0;
        codec_setup = null;
    }

    // info.c

    /**
     * blocksize 0 is guaranteed to be short, 1 is guaranteed to be long.
     * They may be equal, but short will never ge greater than long
     */
    final int blocksize(final int zo) {
        final CodecSetupInfo ci = this.codec_setup;
        return ci != null ? ci.blocksizes[zo] : -1;
    }

    /**
     * used by synthesis, which has a full, alloced vi
     */
    public final void init() {
        m_clear();
        this.codec_setup = new CodecSetupInfo();
    }

    public final void clear() {
		/*final CodecSetupInfo ci = this.m_codec_setup;
		int i;

		if( ci != null ) {

			for( i = 0; i < ci.modes; i++ )
				if( ci.mode_param[i] != null ) ci.mode_param[i] = null;

			for( i = 0; i < ci.maps; i++ ) // unpack does the range checking
				if( ci.map_param[i] != null ) // this may be cleaning up an aborted
									// unpack, in which case the below type
									// cannot be trusted
					//Registry._mapping_P[ ci.map_type[i] ].free_info( ci.map_param[i] );
					ci.map_param[i] = null;

			for( i = 0; i < ci.floors; i++ ) // unpack does the range checking
				if( ci.floor_param[i] != null ) // this may be cleaning up an aborted
									// unpack, in which case the below type
									// cannot be trusted
					//Registry._floor_P[ ci.floor_type[i] ].free_info( ci.floor_param[i] );
					ci.floor_param[i] = null;

			for( i = 0; i < ci.residues; i++ ) // unpack does the range checking
				if( ci.residue_param[i] != null ) // this may be cleaning up an aborted
									// unpack, in which case the below type
									// cannot be trusted
					//Registry._residue_P[ ci.residue_type[i] ].free_info( ci.residue_param[i] );
					ci.residue_param[i] = null;

			for( i = 0; i < ci.books; i++ ) {
				if( ci.book_param[i] != null ) {
					// knows if the book was not alloced
					ci.book_param[i].destroy();
				}
				if( ci.fullbooks != null )
					ci.fullbooks[i].book_clear();
			}
			if( ci.fullbooks != null )
				ci.fullbooks = null;

			for( i = 0; i < ci.psys; i++ )
				ci.psy_param[i] = null;

			//ci = null;
		}*/

        m_clear();
    }

    /**
     * Header packing/unpacking
     */
    private final int _unpack_info(final Buffer opb) {
        final CodecSetupInfo ci = this.codec_setup;
        if (ci == null) {
            return (Codec.OV_EFAULT);
        }

        this.version = opb.pack_read(32);
        if (this.version != 0) {
            return (Codec.OV_EVERSION);
        }

        this.channels = opb.pack_read(8);
        this.rate = opb.pack_read(32);

        this.bitrate_upper = opb.pack_read(32);
        this.bitrate_nominal = opb.pack_read(32);
        this.bitrate_lower = opb.pack_read(32);

        final int[] blocksizes = ci.blocksizes;// java
        blocksizes[0] = 1 << opb.pack_read(4);
        blocksizes[1] = 1 << opb.pack_read(4);

        if (this.rate < 1 || this.channels < 1 || blocksizes[0] < 64 ||
                blocksizes[1] < blocksizes[0] || blocksizes[1] > 8192 ||

                opb.pack_read(1) != 1) {/* EOP check */
//err_out:
            clear();
            return (Codec.OV_EBADHEADER);
        }
        return (0);
    }

    /**
     * all of the real encoding details are here.  The modes, books,
     * everything
     */
    private final int _unpack_books(final Buffer opb) {
        final CodecSetupInfo ci = this.codec_setup;

        /* codebooks */
        ci.books = opb.pack_read(8) + 1;
        if (ci.books <= 0) {
            clear();
            return (Codec.OV_EBADHEADER);
        }
        for (int i = 0, ie = ci.books; i < ie; i++) {
            ci.book_param[i] = StaticCodebook.unpack(opb);
            if (ci.book_param[i] == null) {
                clear();
                return (Codec.OV_EBADHEADER);
            }
        }

        /* time backend settings; hooks are unused */
        {
            final int times = opb.pack_read(6) + 1;
            if (times <= 0) {
                clear();
                return (Codec.OV_EBADHEADER);
            }
            for (int i = 0; i < times; i++) {
                final int test = opb.pack_read(16);
                if (test < 0 || test >= VI_TIMEB) {
                    clear();
                    return (Codec.OV_EBADHEADER);
                }
            }
        }

        /* floor backend settings */
        ci.floors = opb.pack_read(6) + 1;
        if (ci.floors <= 0) {
            clear();
            return (Codec.OV_EBADHEADER);
        }
        final FuncFloor[] floor_p = Codec._floor_P;// java
        for (int i = 0, ie = ci.floors; i < ie; i++) {
            final int t = opb.pack_read(16);
            ci.floor_type[i] = t;
            if (t < 0 || t >= VI_FLOORB) {
                clear();
                return (Codec.OV_EBADHEADER);
            }
            ci.floor_param[i] = floor_p[t].unpack(this, opb);
            if (ci.floor_param[i] == null) {
                clear();
                return (Codec.OV_EBADHEADER);
            }
        }

        /* residue backend settings */
        ci.residues = opb.pack_read(6) + 1;
        if (ci.residues <= 0) {
            clear();
            return (Codec.OV_EBADHEADER);
        }
        final FuncResidue[] residue_p = Codec._residue_P;// java
        for (int i = 0; i < ci.residues; i++) {
            final int t = opb.pack_read(16);
            ci.residue_type[i] = t;
            if (t < 0 || t >= VI_RESB) {
                clear();
                return (Codec.OV_EBADHEADER);
            }
            ci.residue_param[i] = residue_p[t].unpack(this, opb);
            if (ci.residue_param[i] == null) {
                clear();
                return (Codec.OV_EBADHEADER);
            }
        }

        /* map backend settings */
        ci.maps = opb.pack_read(6) + 1;
        if (ci.maps <= 0) {
            clear();
            return (Codec.OV_EBADHEADER);
        }
        final FuncMapping[] mapping_p = Codec._mapping_P;// java
        for (int i = 0, ie = ci.maps; i < ie; i++) {
            final int t = opb.pack_read(16);
            ci.map_type[i] = t;
            if (t < 0 || t >= VI_MAPB) {
                clear();
                return (Codec.OV_EBADHEADER);
            }
            ci.map_param[i] = mapping_p[t].unpack(this, opb);
            if (ci.map_param[i] == null) {
                clear();
                return (Codec.OV_EBADHEADER);
            }
        }

        /* mode settings */
        ci.modes = opb.pack_read(6) + 1;
        if (ci.modes <= 0) {
            clear();
            return (Codec.OV_EBADHEADER);
        }
        for (int i = 0, ie = ci.modes; i < ie; i++) {
            final InfoMode vim = new InfoMode();
            vim.blockflag = opb.pack_read(1);
            vim.windowtype = opb.pack_read(16);
            vim.transformtype = opb.pack_read(16);
            vim.mapping = opb.pack_read(8);
            ci.mode_param[i] = vim;

            if (vim.windowtype >= VI_WINDOWB ||
                    vim.transformtype >= VI_WINDOWB ||
                    vim.mapping >= ci.maps ||
                    vim.mapping < 0) {
                clear();
                return (Codec.OV_EBADHEADER);
            }
        }

        if (opb.pack_read(1) != 1) {/* top level EOP check */
            clear();
            return (Codec.OV_EBADHEADER);
        }
        return (0);
//err_out:
//		vorbis_info_clear( vi );
//		return (Codec.OV_EBADHEADER);
    }

    static final void _v_writestring(final Buffer o, final byte[] s, int bytes) {

        for (int i = 0; bytes-- > 0; ) {
            o.pack_write(s[i++], 8);
        }
    }

    static final void _v_readstring(final Buffer o, final String[] strings, final int offset, int bytes) {
        final byte[] buf = new byte[bytes];
        for (int i = 0; bytes-- > 0; ) {
            buf[i++] = (byte) o.pack_read(8);
        }
        try {
            strings[offset] = new String(buf, Comment.TEXT_ENCODING);
        } catch (final UnsupportedEncodingException e) {
        }
    }

    static final void _v_readstring(final Buffer o, final byte[] buf, int bytes) {
        for (int i = 0; bytes-- > 0; ) {
            buf[i++] = (byte) o.pack_read(8);
        }
    }

    /* pack side **********************************************************/
    /* helpers */
    final int _pack_info(final Buffer opb) {
        final CodecSetupInfo ci = this.codec_setup;
        if (ci == null ||
                ci.blocksizes[0] < 64 ||
                ci.blocksizes[1] < ci.blocksizes[0]) {
            return (Codec.OV_EFAULT);
        }

        /* preamble */
        opb.pack_write(0x01, 8);
        _v_writestring(opb, Comment.vorbis, 6);

        /* basic information about the stream */
        opb.pack_write(0x00, 32);
        opb.pack_write(this.channels, 8);
        opb.pack_write(this.rate, 32);

        opb.pack_write(this.bitrate_upper, 32);
        opb.pack_write(this.bitrate_nominal, 32);
        opb.pack_write(this.bitrate_lower, 32);

        opb.pack_write(Codec.ilog(ci.blocksizes[0] - 1), 4);
        opb.pack_write(Codec.ilog(ci.blocksizes[1] - 1), 4);
        opb.pack_write(1, 1);

        return (0);
    }

    final int _pack_books(final Buffer opb) {
        final CodecSetupInfo ci = this.codec_setup;
        if (ci == null) {
            return (Codec.OV_EFAULT);
        }

        opb.pack_write(0x05, 8);
        _v_writestring(opb, Comment.vorbis, 6);

        /* books */
        opb.pack_write(ci.books - 1, 8);
        final StaticCodebook[] book_param = ci.book_param;// java
        for (int i = 0, ie = ci.books; i < ie; i++) {
            if (book_param[i].pack(opb) != 0) {
                return -1;//goto err_out;
            }
        }

        /* times; hook placeholders */
        opb.pack_write(0, 6);
        opb.pack_write(0, 16);

        /* floors */
        opb.pack_write(ci.floors - 1, 6);
        int[] tmp = ci.floor_type;// java
        final FuncFloor[] floor_p = Codec._floor_P;// java
        final InfoFloor[] floor_param = ci.floor_param;// java
        for (int i = 0, ie = ci.floors; i < ie; i++) {
            final int t = tmp[i];
            opb.pack_write(t, 16);
            final FuncFloor f = floor_p[t];
            if (f.is_pack_supported) {
                f.pack(floor_param[i], opb);
            } else {
                return -1;//goto err_out;
            }
        }

        /* residues */
        opb.pack_write(ci.residues - 1, 6);
        tmp = ci.residue_type;// java
        final FuncResidue[] residue_p = Codec._residue_P;// java
        final InfoResidue[] residue_param = ci.residue_param;// java
        for (int i = 0, ie = ci.residues; i < ie; i++) {
            final int type = tmp[i];// java
            opb.pack_write(type, 16);
            // FIXME why no checking for null? see residue0_exportbundle
            residue_p[type].pack(residue_param[i], opb);
        }

        /* maps */
        opb.pack_write(ci.maps - 1, 6);
        tmp = ci.map_type;// java
        final FuncMapping[] mapping_p = Codec._mapping_P;// java
        final InfoMapping[] map_param = ci.map_param;// java
        for (int i = 0, ie = ci.maps; i < ie; i++) {
            final int type = tmp[i];// java
            opb.pack_write(type, 16);
            mapping_p[type].pack(this, map_param[i], opb);
        }

        /* modes */
        opb.pack_write(ci.modes - 1, 6);
        final InfoMode[] mode_param = ci.mode_param;// java
        for (int i = 0, ie = ci.modes; i < ie; i++) {
            final InfoMode vim = mode_param[i];
            opb.pack_write(vim.blockflag, 1);
            opb.pack_write(vim.windowtype, 16);
            opb.pack_write(vim.transformtype, 16);
            opb.pack_write(vim.mapping, 8);
        }
        opb.pack_write(1, 1);

        return (0);
//err_out:
//		return (-1);
    }

    /**
     * Is this packet a vorbis ID header?
     */
    public static final boolean synthesis_idheader(final Packet op) {
        if (op != null) {
            final Buffer opb = new Buffer();
            opb.pack_readinit(op.packet_base, op.packet, op.bytes);

            if (!op.b_o_s) {
                return false; /* Not the initial packet */
            }

            if (opb.pack_read(8) != 1) {
                return false; /* not an ID header */
            }

            final byte buffer[] = new byte[6];// already zeroed
            _v_readstring(opb, buffer, 6);
            if (buffer[0] != 'v' || buffer[1] != 'o' || buffer[2] != 'r' ||
                    buffer[3] != 'b' || buffer[4] != 'i' || buffer[5] != 's') {
                return false; /* not vorbis */
            }

            return true;
        }

        return false;
    }

    /**
     * The Vorbis header is in three packets; the initial small packet in
     * the first page that identifies basic parameters, a second packet
     * with bitstream comments and a third packet that holds the
     * codebook.
     */
    public static final int synthesis_headerin(final Info vi, final Comment vc, final Packet op) {

        if (op != null) {
            final Buffer opb = new Buffer();
            opb.pack_readinit(op.packet_base, op.packet, op.bytes);

            /* Which of the three types of header is this? */
            /* Also verify header-ness, vorbis */
            {
                final byte buffer[] = new byte[6];// already zeroed
                final int packtype = opb.pack_read(8);
                _v_readstring(opb, buffer, 6);
                if (buffer[0] != 'v' || buffer[1] != 'o' || buffer[2] != 'r' ||
                        buffer[3] != 'b' || buffer[4] != 'i' || buffer[5] != 's') {
                    /* not a vorbis header */
                    return (Codec.OV_ENOTVORBIS);
                }
                switch (packtype) {
                    case 0x01: /* least significant *bit* is read first */
                        if (!op.b_o_s) {
                            /* Not the initial packet */
                            return (Codec.OV_EBADHEADER);
                        }
                        if (vi.rate != 0) {
                            /* previously initialized info header */
                            return (Codec.OV_EBADHEADER);
                        }

                        return (vi._unpack_info(opb));

                    case 0x03: /* least significant *bit* is read first */
                        if (vi.rate == 0) {
                            /* um... we didn't get the initial header */
                            return (Codec.OV_EBADHEADER);
                        }
                        if (vc.vendor != null) {
                            /* previously initialized comment header */
                            return (Codec.OV_EBADHEADER);
                        }

                        return (vc._unpack_comment(opb));

                    case 0x05: /* least significant *bit* is read first */
                        if (vi.rate == 0 || vc.vendor == null) {
                            /* um... we didn;t get the initial header or comments yet */
                            return (Codec.OV_EBADHEADER);
                        }
                        if (vi.codec_setup == null) {
                            /* improperly initialized vorbis_info */
                            return (Codec.OV_EFAULT);
                        }
                        if (vi.codec_setup.books > 0) {
                            /* previously initialized setup header */
                            return (Codec.OV_EBADHEADER);
                        }

                        return (vi._unpack_books(opb));

                    default:
                        /* Not a valid vorbis header type */
                        return (Codec.OV_EBADHEADER);
                    //break;
                }
            }
        }
        return (Codec.OV_EBADHEADER);
    }

    // bitrate.c

    /**
     * compute bitrate tracking setup
     */
    final void bitrate_init(final BitrateManagerState bm) {
        final CodecSetupInfo ci = this.codec_setup;
        final BitrateManagerInfo bi = ci.bi;

        bm.clear();// zeroed

        if (bi != null && (bi.reservoir_bits > 0)) {
            final int ratesamples = this.rate;
            final int halfsamples = ci.blocksizes[0] >> 1;

            bm.short_per_long = ci.blocksizes[1] / ci.blocksizes[0];
            bm.managed = 1;

            bm.avg_bitsper = (int) Math.rint(1. * bi.avg_rate * halfsamples / ratesamples);
            bm.min_bitsper = (int) Math.rint(1. * bi.min_rate * halfsamples / ratesamples);
            bm.max_bitsper = (int) Math.rint(1. * bi.max_rate * halfsamples / ratesamples);

            bm.avgfloat = (double) (PACKETBLOBS / 2);

			/* not a necessary fix, but one that leads to a more balanced
			typical initialization */
            {
                final int desired_fill = (int) (bi.reservoir_bits * bi.reservoir_bias);
                bm.minmax_reservoir = desired_fill;
                bm.avg_reservoir = desired_fill;
            }

        }
    }

    // psy.c
    final LookPsyGlobal _global_look() {
        final CodecSetupInfo ci = this.codec_setup;
        final InfoPsyGlobal gi = ci.psy_g_param;
        final LookPsyGlobal look = new LookPsyGlobal();

        look.channels = this.channels;

        look.ampmax = -9999.f;
        look.gi = gi;
        return (look);
    }

    // synthesys.c
    public final int packet_blocksize(final Packet op) {
        final CodecSetupInfo ci = this.codec_setup;
        if (ci == null || ci.modes <= 0) {
            /* codec setup not properly intialized */
            return (Codec.OV_EFAULT);
        }

        final Buffer opb = new Buffer();

        opb.pack_readinit(op.packet_base, op.packet, op.bytes);

        /* Check the packet type */
        if (opb.pack_read(1) != 0) {
            /* Oops.  This is not an audio data packet */
            return (Codec.OV_ENOTAUDIO);
        }

        /* read our mode and pre/post windowsize */
        final int mode = opb.pack_read(Codec.ilog(ci.modes - 1));
        if (mode == -1 || null == ci.mode_param[mode]) {
            return (Codec.OV_EBADPACKET);
        }
        return (ci.blocksizes[ci.mode_param[mode].blockflag]);
    }

    public final int synthesis_halfrate(final boolean flag) {
        /* set / clear half-sample-rate mode */
        final CodecSetupInfo ci = this.codec_setup;

        /* right now, our MDCT can't handle < 64 sample windows. */
        if (ci.blocksizes[0] <= 64 && flag) {
            return -1;
        }
        ci.halfrate_flag = flag ? 1 : 0;
        return 0;
    }

    /**
     * @return 1 or 0
     */
    public final int synthesis_halfrate_p() {
        return this.codec_setup.halfrate_flag;
    }

    // vorbisenc.c
    private final void encode_floor_setup(final double s,// FIXME why int s?
                                          final StaticCodebook[][] books,
                                          final InfoFloor1[] in,
                                          final int[] x) {
        final int is = (int) s;
        final InfoFloor1 f = new InfoFloor1(in[x[is]]);
        final CodecSetupInfo ci = this.codec_setup;

        /* books */
        {
            final int partitions = f.partitions;
            int maxclass = -1;
            int maxbook = -1;
            int[] tmp = f.partitionclass;// java
            for (int i = 0; i < partitions; i++) {
                if (tmp[i] > maxclass) {
                    maxclass = tmp[i];
                }
            }
            int ci_books = ci.books;// java
            tmp = f.class_book;// java
            final int[][] class_subbook = f.class_subbook;// java
            final int[] class_subs = f.class_subs;// java
            for (int i = 0; i <= maxclass; i++) {
                int t = tmp[i];// java
                if (t > maxbook) {
                    maxbook = t;
                }
                t += ci_books;
                tmp[i] = t;
                final int sb[] = class_subbook[i];
                for (int k = 0, ke = (1 << class_subs[i]); k < ke; k++) {
                    if (sb[k] > maxbook) {
                        maxbook = sb[k];
                    }
                    if (sb[k] >= 0) {
                        sb[k] += ci_books;
                    }
                }
            }

            final StaticCodebook[] book_param = ci.book_param;// java
            for (int i = 0; i <= maxbook; i++) {
                book_param[ci_books++] = (StaticCodebook) books[x[is]][i];
            }
            ci.books = ci_books;
        }

        /* for now, we're only using floor 1 */
        ci.floor_type[ci.floors] = 1;
        ci.floor_param[ci.floors] = f;
        ci.floors++;

        return;
    }

    private final void encode_global_psych_setup(final double s,
                                                 final InfoPsyGlobal[] in,
                                                 final double[] x) {
        int is = (int) s;
        double ds = s - is;
        final CodecSetupInfo ci = this.codec_setup;
        final InfoPsyGlobal g = ci.psy_g_param;

        g.copyFrom(in[(int) x[is]]);

        ds = x[is] * (1. - ds) + (double) x[is + 1] * ds;
        is = (int) ds;
        ds -= is;
        if (ds == 0 && is > 0) {
            is--;
            ds = 1.;
        }

        /* interpolate the trigger threshholds */
        final double ds1 = 1. - ds;// java
        InfoPsyGlobal p = in[is];// java
        final float[] in_is_preecho_thresh = p.preecho_thresh;// java
        final float[] in_is_postecho_thresh = p.postecho_thresh;// java
        p = in[is + 1];// java
        final float[] in_is_preecho_thresh1 = p.preecho_thresh;// java
        final float[] in_is_postecho_thresh1 = p.postecho_thresh;// java
        final float[] g_preecho_thresh = g.preecho_thresh;// java
        final float[] g_postecho_thresh = g.postecho_thresh;// java
        for (int i = 0; i < 4; i++) {
            g_preecho_thresh[i] = (float) (in_is_preecho_thresh[i] * ds1 + in_is_preecho_thresh1[i] * ds);
            g_postecho_thresh[i] = (float) (in_is_postecho_thresh[i] * ds1 + in_is_postecho_thresh1[i] * ds);
        }
        g.ampmax_att_per_sec = (float) ci.hi.amplitude_track_dBpersec;
        return;
    }

    private final void encode_global_stereo(final HighLevelEncodeSetup hi, final AdjStereo[] p) {
        final float s = (float) hi.stereo_point_setting;
        final int is = (int) s;
        final double ds = s - is;
        final CodecSetupInfo ci = this.codec_setup;
        final InfoPsyGlobal g = ci.psy_g_param;

        final int[] sliding_lowpass0 = g.sliding_lowpass[0];// java
        final int[] sliding_lowpass1 = g.sliding_lowpass[1];// java
        final int blocksizes0 = ci.blocksizes[0];// java
        final int blocksizes1 = ci.blocksizes[1];// java
        if (p != null) {
            AdjStereo a = p[is];// java
            System.arraycopy(a.pre, 0, g.coupling_prepointamp, 0, PACKETBLOBS);
            System.arraycopy(a.post, 0, g.coupling_postpointamp, 0, PACKETBLOBS);
            final float[] a_kHz = a.kHz;// java
            final float[] a_lowpasskHz = a.lowpasskHz;// java
            a = p[is + 1];// java
            final float[] a1_kHz = a.kHz;// java
            final float[] a1_lowpasskHz = a.lowpasskHz;// java

            final double ds1 = 1. - ds;// java
            float k1 = 1000.f / this.rate;// java
            final float k0 = k1 * blocksizes0;// java
            k1 *= blocksizes1;// java
            final int[] coupling_pointlimit0 = g.coupling_pointlimit[0];// java
            final int[] coupling_pointlimit1 = g.coupling_pointlimit[1];// java
            final int[] coupling_pkHz = g.coupling_pkHz;// java
            if (hi.managed) {
                /* interpolate the kHz threshholds */
                for (int i = 0; i < PACKETBLOBS; i++) {
                    float kHz = (float) (a_kHz[i] * ds1 + (double) a1_kHz[i] * ds);
                    coupling_pointlimit0[i] = (int) (kHz * k0);
                    coupling_pointlimit1[i] = (int) (kHz * k1);
                    coupling_pkHz[i] = (int) kHz;

                    kHz = (float) (a_lowpasskHz[i] * ds1 + (double) a1_lowpasskHz[i] * ds);
                    sliding_lowpass0[i] = (int) (kHz * k0);
                    sliding_lowpass1[i] = (int) (kHz * k1);
                }
            } else {
                float kHz = (float) (a_kHz[PACKETBLOBS / 2] * ds1 +
                        (double) a1_kHz[PACKETBLOBS / 2] * ds);
                int kHz_k0 = (int) (kHz * k0);
                int kHz_k1 = (int) (kHz * k1);
                for (int i = 0; i < PACKETBLOBS; i++) {
                    coupling_pointlimit0[i] = kHz_k0;
                    coupling_pointlimit1[i] = kHz_k1;
                    coupling_pkHz[i] = (int) kHz;
                }

                kHz = (float) (a_lowpasskHz[PACKETBLOBS / 2] * ds1 +
                        (double) a1_lowpasskHz[PACKETBLOBS / 2] * ds);
                kHz_k0 = (int) (kHz * k0);
                kHz_k1 = (int) (kHz * k1);
                for (int i = 0; i < PACKETBLOBS; i++) {
                    sliding_lowpass0[i] = kHz_k0;
                    sliding_lowpass1[i] = kHz_k1;
                }
            }
        } else {
            for (int i = 0; i < PACKETBLOBS; i++) {
                sliding_lowpass0[i] = blocksizes0;
                sliding_lowpass1[i] = blocksizes1;
            }
        }
        return;
    }

    private final void encode_psyset_setup(final double s,
                                           final int[] nn_start, final int[] nn_partition,
                                           final double[] nn_thresh, final int block) {
        final CodecSetupInfo ci = this.codec_setup;

        if (block >= ci.psys) {
            ci.psys = block + 1;
        }

        final InfoPsy p = new InfoPsy(Psych44._psy_info_template);
        ci.psy_param[block] = p;
        p.blockflag = block >> 1;

        if (ci.hi.noise_normalize_p) {
            final int is = (int) s;
            p.normal_p = true;
            p.normal_start = nn_start[is];
            p.normal_partition = nn_partition[is];
            p.normal_thresh = nn_thresh[is];
        }

        return;
    }

    private final void encode_tonemask_setup(final double s, final int block,
                                             final Att3[] att, final int[] max, final AdjBlock[] in) {
        final int is = (int) s;
        final double ds = s - is;
        final InfoPsy p = this.codec_setup.psy_param[block];

		/* 0 and 2 are only used by bitmanagement, but there's no harm to always
		  filling the values in here */
        final double ds1 = 1. - ds;// java
        final Att3 a = att[is];// java
        final Att3 a1 = att[is + 1];// java
        final int[] att_is = a.att;// java
        final int[] att_is1 = a1.att;// java
        final float[] tone_masteratt = p.tone_masteratt;// java
        tone_masteratt[0] = (float) (att_is[0] * ds1 + (double) att_is1[0] * ds);
        tone_masteratt[1] = (float) (att_is[1] * ds1 + (double) att_is1[1] * ds);
        tone_masteratt[2] = (float) (att_is[2] * ds1 + (double) att_is1[2] * ds);
        p.tone_centerboost = (float) (a.boost * ds1 + (double) a1.boost * ds);
        p.tone_decay = (float) (a.decay * ds1 + (double) a1.decay * ds);

        p.max_curve_dB = (float) (max[is] * ds1 + (double) max[is + 1] * ds);

        final int[] inblock = in[is].block;// java
        final int[] inblock1 = in[is + 1].block;// java
        final float[] toneatt = p.toneatt;// java
        for (int i = 0; i < LookPsy.P_BANDS; i++) {
            toneatt[i] = (float) (inblock[i] * ds1 + (double) inblock1[i] * ds);
        }
        return;
    }

    private final void encode_compand_setup(final double s, final int block,
                                            final CompandBlock[] in, final double[] x) {
        int is = (int) s;
        double ds = s - is;
        final CodecSetupInfo ci = this.codec_setup;
        final InfoPsy p = ci.psy_param[block];

        ds = x[is] * (1. - ds) + x[is + 1] * ds;
        is = (int) ds;
        ds -= is;
        if (ds == 0 && is > 0) {
            is--;
            ds = 1.;
        }

        /* interpolate the compander settings */
        final double ds1 = 1. - ds;// java
        final int[] data = in[is].data;// java
        final int[] data1 = in[is + 1].data;// java
        final float[] noisecompand = p.noisecompand;// java
        for (int i = 0; i < LookPsy.NOISE_COMPAND_LEVELS; i++) {
            noisecompand[i] = (float) (data[i] * ds1 + (double) data1[i] * ds);
        }
        return;
    }

    private final void encode_peak_setup(final double s, final int block,
                                         final int[] suppress) {
        final int is = (int) s;
        final double ds = s - is;
        final InfoPsy p = this.codec_setup.psy_param[block];

        p.tone_abs_limit = (float) (suppress[is] * (1. - ds) + (double) suppress[is + 1] * ds);

        return;
    }

    private final void encode_noisebias_setup(final double s, final int block,
                                              final int[] suppress, final Noise3[] in, final NoiseGuard[] guard,
                                              final double userbias) {

        final int is = (int) s;
        final double ds = s - is;
        final double ds1 = 1. - ds;// java
        final InfoPsy p = this.codec_setup.psy_param[block];

        p.noisemaxsupp = (float) (suppress[is] * ds1 + (double) suppress[is + 1] * ds);
        p.noisewindowlomin = guard[block].lo;
        p.noisewindowhimin = guard[block].hi;
        p.noisewindowfixed = guard[block].fixed;

        final int[][] data = in[is].data;// java
        final int[][] data1 = in[is + 1].data;// java
        final float[][] noiseoff = p.noiseoff;// java
        for (int j = 0; j < LookPsy.P_NOISECURVES; j++) {
            final float[] nj = noiseoff[j];// java
            final int[] d = data[j];// java
            final int[] d1 = data1[j];// java
            for (int i = 0; i < LookPsy.P_BANDS; i++) {
                nj[i] = (float) (d[i] * ds1 + (double) d1[i] * ds);
            }
        }

		/* impulse blocks may take a user specified bias to boost the
		nominal/high noise encoding depth */
        for (int j = 0; j < LookPsy.P_NOISECURVES; j++) {
            final float[] nj = noiseoff[j];// java
            final float min = nj[0] + 6; /* the lowest it can go */
            for (int i = 0; i < LookPsy.P_BANDS; i++) {
                float n = nj[i];// java
                n += userbias;
                if (n < min) {
                    n = min;
                }
                nj[i] = n;
            }
        }

        return;
    }

    private final void encode_ath_setup(final int block) {
        final CodecSetupInfo ci = this.codec_setup;
        final InfoPsy p = ci.psy_param[block];

        p.ath_adjatt = (float) ci.hi.ath_floating_dB;
        p.ath_maxatt = (float) ci.hi.ath_absolute_dB;
        return;
    }


    private static final int book_dup_or_new(final CodecSetupInfo ci, final StaticCodebook book) {
        final StaticCodebook[] cb = ci.book_param;// java
        for (int i = 0, ie = ci.books; i < ie; i++) {
            if (cb[i] == book) {
                return (i);
            }
        }

        return (ci.books++);
    }

    private final void encode_blocksize_setup(final double s,
                                              final int[] shortb, final int[] longb) {

        final CodecSetupInfo ci = this.codec_setup;
        final int is = (int) s;

        final int blockshort = shortb[is];
        final int blocklong = longb[is];
        ci.blocksizes[0] = blockshort;
        ci.blocksizes[1] = blocklong;
    }

    private final void encode_residue_setup(
            final int number, final int block,
            final ResidueTemplate res) {

        final CodecSetupInfo ci = this.codec_setup;

        final InfoResidue0 r = new InfoResidue0(res.res);
        ci.residue_param[number] = r;

        if (ci.residues <= number) {
            ci.residues = number + 1;
        }

        r.grouping = res.grouping;
        ci.residue_type[number] = res.res_type;

        /* fill in all the books */
        {
            int booklist = 0;
            final int partitions = r.partitions;// java

            // java: if-else changed
            final StaticCodebook[][] books;
            final StaticCodebook book_aux;
            if (ci.hi.managed) {// FIXME don't cover the test
                books = res.books_base_managed.books;
                book_aux = res.book_aux_managed;
            } else {
                books = res.books_base.books;
                book_aux = res.book_aux;
            }

            final int[] secondstages = r.secondstages;// java
            for (int i = 0; i < partitions; i++) {
                int s = secondstages[i];// java
                final StaticCodebook[] b = books[i];// java
                for (int k = 0; k < 4; k++) {
                    if (b[k] != null) {
                        s |= (1 << k);
                    }
                }
                secondstages[i] = s;
            }

            r.groupbook = book_dup_or_new(ci, book_aux);
            final StaticCodebook[] book_param = ci.book_param;// java
            book_param[r.groupbook] = (StaticCodebook) book_aux;

            final int[] r_booklist = r.booklist;// java
            for (int i = 0; i < partitions; i++) {
                final StaticCodebook[] bi = books[i];// java
                for (int k = 0; k < 4; k++) {
                    final StaticCodebook b = bi[k];// java
                    if (b != null) {
                        final int bookid = book_dup_or_new(ci, b);
                        r_booklist[booklist++] = bookid;
                        book_param[bookid] = (StaticCodebook) b;
                    }
                }
            }

        }

        /* lowpass setup/pointlimit */
        {
            double freq = ci.hi.lowpass_kHz * 1000.;
            final InfoFloor1 f = (InfoFloor1) ci.floor_param[block]; /* by convention */
            final double nyq = this.rate * 0.5;
            final int blocksize = ci.blocksizes[block] >>> 1;

            /* lowpass needs to be set in the floor and the residue. */
            if (freq > nyq) {
                freq = nyq;
            }
			/* in the floor, the granularity can be very fine; it doesn't alter
			  the encoding structure, only the samples used to fit the floor
			  approximation */
            f.n = (int) (freq / nyq * blocksize);

			/* this res may by limited by the maximum pointlimit of the mode,
			  not the lowpass. the floor is always lowpass limited. */
            switch (res.limit_type) {
                case 1: /* point stereo limited */
                    if (ci.hi.managed) {
                        freq = ci.psy_g_param.coupling_pkHz[PACKETBLOBS - 1] * 1000.;
                    } else {
                        freq = ci.psy_g_param.coupling_pkHz[PACKETBLOBS / 2] * 1000.;
                    }
                    if (freq > nyq) {
                        freq = nyq;
                    }
                    break;
                case 2: /* LFE channel; lowpass at ~ 250Hz */
                    freq = 250;
                    break;
                default:
                    /* already set */
                    break;
            }

			/* in the residue, we're constrained, physically, by partition
			  boundaries.  We still lowpass 'wherever', but we have to round up
			  here to next boundary, or the vorbis spec will round it *down* to
			  previous boundary in encode/decode */
            if (ci.residue_type[number] == 2) {
				/* residue 2 bundles together multiple channels; used by stereo
				  and surround.  Count the channels in use */
				/* Multiple maps/submaps can point to the same residue.  In the case
				  of residue 2, they all better have the same number of
				  channels/samples. */
                int ch = 0;
                final int nchannels = this.channels;// java
                for (int i = 0, maps = ci.maps; i < maps && ch == 0; i++) {
                    final InfoMapping0 mi = (InfoMapping0) ci.map_param[i];
                    final int[] residuesubmap = mi.residuesubmap;// java
                    final int[] chmuxlist = mi.chmuxlist;// java
                    for (int j = 0, je = mi.submaps; j < je && ch == 0; j++) {
                        if (residuesubmap[j] == number) {
                            for (int k = 0; k < nchannels; k++) {
                                if (chmuxlist[k] == j) {
                                    ch++;
                                }
                            }
                        }
                    }
                }

                r.end = (int) ((freq / nyq * blocksize * ch) / r.grouping + .9) * /* round up only if we're well past */
                        r.grouping;
                /* the blocksize and grouping may disagree at the end */
                ch *= blocksize;// java
                if (r.end > ch) {
                    r.end = ch / r.grouping * r.grouping;
                }

            } else {

                r.end = (int) ((freq / nyq * blocksize) / r.grouping + .9) * /* round up only if we're well past */
                        r.grouping;
                /* the blocksize and grouping may disagree at the end */
                if (r.end > blocksize) {
                    r.end = blocksize / r.grouping * r.grouping;
                }

            }

            if (r.end == 0) {
                r.end = r.grouping; /* LFE channel */
            }

        }
    }

    /**
     * we assume two maps in this encoder
     */
    private final void encode_map_n_res_setup(final double s,
                                              final MappingTemplate[] maps) {

        final CodecSetupInfo ci = this.codec_setup;
        int modes = 2;
        final int is = (int) s;
        final InfoMapping0[] map = maps[is].map;
        final InfoMode[] mode = _mode_template;
        final ResidueTemplate[] res = maps[is].res;

        if (ci.blocksizes[0] == ci.blocksizes[1]) {
            modes = 1;
        }

        for (int i = 0; i < modes; i++) {

            ci.mode_param[i] = new InfoMode(mode[i]);

            if (i >= ci.modes) {
                ci.modes = i + 1;
            }

            ci.map_type[i] = 0;
            final InfoMapping0 mi = map[i];// java
            ci.map_param[i] = new InfoMapping0(mi);

            if (i >= ci.maps) {
                ci.maps = i + 1;
            }

            final int[] residuesubmap = mi.residuesubmap;// java
            for (int j = 0, submaps = mi.submaps; j < submaps; j++) {
                final int r = residuesubmap[j];// java
                encode_residue_setup(r, i, res[r]);
            }
        }
    }

    private final double setting_to_approx_bitrate() {
        final HighLevelEncodeSetup hi = this.codec_setup.hi;
        final EncSetupDataTemplate setup = (EncSetupDataTemplate) hi.setup;

        final double[] r = setup.rate_mapping;

        if (r == null) {
            return (-1);// FIXME is this correct return?
        }

        final int is = (int) hi.base_setting;
        final double ds = hi.base_setting - (double) is;
        return ((r[is] * (1. - ds) + r[is + 1] * ds) * this.channels);
    }

    // added HighLevelEncodeSetup hi, return void
    private static final void get_setup_template(final int ch, final int srate,
                                                 double req, final boolean q_or_bitrate,
                                                 final HighLevelEncodeSetup hi) {

        if (q_or_bitrate) {
            req /= ch;
        }

        EncSetupDataTemplate st;// jjava
        for (int i = 0; (st = setup_list[i]) != null; i++) {// java: while changed to for
            if (st.coupling_restriction == -1 ||
                    st.coupling_restriction == ch) {
                if (srate >= st.samplerate_min_restriction &&
                        srate <= st.samplerate_max_restriction) {
                    final int mappings = st.mappings;
                    final double[] map = (q_or_bitrate ?
                            st.rate_mapping :
                            st.quality_mapping);

					/* the template matches.  Does the requested quality mode
					  fall within this template's modes? */
                    if (req < map[0]) {
                        ++i;
                        continue;
                    }
                    if (req > map[mappings]) {
                        ++i;
                        continue;
                    }
                    int j = 0;
                    for (; j < mappings; j++) {
                        if (req >= map[j] && req < map[j + 1]) {
                            break;
                        }
                    }
                    /* an all-points match */
                    if (j == mappings) {
                        hi.base_setting = (double) j - .001;
                    } else {
                        final double low = map[j];
                        final double high = map[j + 1];
                        final double del = ((req - low) / (high - low));
                        hi.base_setting = (double) j + del;
                    }

                    //return (setup_list[i]);
                    hi.setup = st;
                    return;
                }
            }
        }

        //return null;
        hi.setup = null;
    }

    /**
     * This function performs the last stage of three-step encoding setup, as
     * described in the API overview under managed bitrate modes.
     * <p>
     * Before this function is called, the {@link Info}  struct should be
     * initialized by using vorbis_info_init() from the libvorbis API, one of
     * {@link #encode_setup_managed} or {@link #encode_setup_vbr} called to
     * initialize the high-level encoding setup, and {@link #encode_ctl}
     * called if necessary to make encoding setup changes.
     * vorbis_encode_setup_init() finalizes the highlevel encoding structure into
     * a complete encoding setup after which the application may make no further
     * setup changes.
     * <p>
     * After encoding, vorbis_info_clear() should be called.
     *
     * @return Zero for success, and negative values for failure.
     * @retval 0           Success.
     * @retval OV_EFAULT  Internal logic fault; indicates a bug or heap/stack corruption.
     * @retval OV_EINVAL   Attempt to use vorbis_encode_setup_init() without first
     * calling one of vorbis_encode_setup_managed() or vorbis_encode_setup_vbr() to
     * initialize the high-level encoding setup
     */
    public final int encode_setup_init() {
        final CodecSetupInfo ci = this.codec_setup;
        // final HighLevelEncodeSetup hi = ci.hi;

        if (ci == null) {// FIXME check after using
            return (Codec.OV_EINVAL);
        }
        final HighLevelEncodeSetup hi = ci.hi;
        int i0 = 0;
        if (!hi.impulse_block_p) {
            i0 = 1;
        }

        /* too low/high an ATH floater is nonsensical, but doesn't break anything */
        if (hi.ath_floating_dB > -80) {
            hi.ath_floating_dB = -80;
        }
        if (hi.ath_floating_dB < -200) {
            hi.ath_floating_dB = -200;
        }

		/* again, bound this to avoid the app shooting itself int he foot
		  too badly */
        if (hi.amplitude_track_dBpersec > 0.) {
            hi.amplitude_track_dBpersec = 0.;
        }
        if (hi.amplitude_track_dBpersec < -99999.) {
            hi.amplitude_track_dBpersec = -99999.;
        }

		/* get the appropriate setup template; matches the fetch in previous
		  stages */
        final EncSetupDataTemplate setup = (EncSetupDataTemplate) hi.setup;
        if (setup == null) {
            return (Codec.OV_EINVAL);
        }

        hi.set_in_stone = 1;
		/* choose block sizes from configured sizes as well as paying
		  attention to long_block_p and short_block_p.  If the configured
		  short and long blocks are the same length, we set long_block_p
		  and unset short_block_p */
        encode_blocksize_setup(hi.base_setting,
                setup.blocksize_short,
                setup.blocksize_long);
        final boolean singleblock = (ci.blocksizes[0] == ci.blocksizes[1]);

		/* floor setup; choose proper floor params.  Allocated on the floor
		  stack in order; if we alloc only a single long floor, it's 0 */
        final int[][] floor_mapping_list = setup.floor_mapping_list;// java
        for (int i = 0, ie = setup.floor_mappings; i < ie; i++) {
            encode_floor_setup(hi.base_setting,
                    setup.floor_books,
                    setup.floor_params,
                    floor_mapping_list[i]);
        }

        /* setup of [mostly] short block detection and stereo*/
        encode_global_psych_setup(hi.trigger_setting,
                setup.global_params,
                setup.global_mapping);
        encode_global_stereo(hi, setup.stereo_modes);

        /* basic psych setup and noise normalization */
        encode_psyset_setup(hi.base_setting,
                setup.psy_noise_normal_start[0],
                setup.psy_noise_normal_partition[0],
                setup.psy_noise_normal_thresh,
                0);
        encode_psyset_setup(hi.base_setting,
                setup.psy_noise_normal_start[0],
                setup.psy_noise_normal_partition[0],
                setup.psy_noise_normal_thresh,
                1);
        if (!singleblock) {
            encode_psyset_setup(hi.base_setting,
                    setup.psy_noise_normal_start[1],
                    setup.psy_noise_normal_partition[1],
                    setup.psy_noise_normal_thresh,
                    2);
            encode_psyset_setup(hi.base_setting,
                    setup.psy_noise_normal_start[1],
                    setup.psy_noise_normal_partition[1],
                    setup.psy_noise_normal_thresh,
                    3);
        }

        /* tone masking setup */
        encode_tonemask_setup(hi.block[i0].tone_mask_setting, 0,
                setup.psy_tone_masteratt,
                setup.psy_tone_0dB,
                setup.psy_tone_adj_impulse);
        encode_tonemask_setup(hi.block[1].tone_mask_setting, 1,
                setup.psy_tone_masteratt,
                setup.psy_tone_0dB,
                setup.psy_tone_adj_other);
        if (!singleblock) {
            encode_tonemask_setup(hi.block[2].tone_mask_setting, 2,
                    setup.psy_tone_masteratt,
                    setup.psy_tone_0dB,
                    setup.psy_tone_adj_other);
            encode_tonemask_setup(hi.block[3].tone_mask_setting, 3,
                    setup.psy_tone_masteratt,
                    setup.psy_tone_0dB,
                    setup.psy_tone_adj_long);
        }

        /* noise companding setup */
        encode_compand_setup(hi.block[i0].noise_compand_setting, 0,
                setup.psy_noise_compand,
                setup.psy_noise_compand_short_mapping);
        encode_compand_setup(hi.block[1].noise_compand_setting, 1,
                setup.psy_noise_compand,
                setup.psy_noise_compand_short_mapping);
        if (!singleblock) {
            encode_compand_setup(hi.block[2].noise_compand_setting, 2,
                    setup.psy_noise_compand,
                    setup.psy_noise_compand_long_mapping);
            encode_compand_setup(hi.block[3].noise_compand_setting, 3,
                    setup.psy_noise_compand,
                    setup.psy_noise_compand_long_mapping);
        }

        /* peak guarding setup  */
        encode_peak_setup(hi.block[i0].tone_peaklimit_setting, 0,
                setup.psy_tone_dBsuppress);
        encode_peak_setup(hi.block[1].tone_peaklimit_setting, 1,
                setup.psy_tone_dBsuppress);
        if (!singleblock) {
            encode_peak_setup(hi.block[2].tone_peaklimit_setting, 2,
                    setup.psy_tone_dBsuppress);
            encode_peak_setup(hi.block[3].tone_peaklimit_setting, 3,
                    setup.psy_tone_dBsuppress);
        }

        /* noise bias setup */
        encode_noisebias_setup(hi.block[i0].noise_bias_setting, 0,
                setup.psy_noise_dBsuppress,
                setup.psy_noise_bias_impulse,
                setup.psy_noiseguards,
                (i0 == 0 ? hi.impulse_noisetune : 0.));
        encode_noisebias_setup(hi.block[1].noise_bias_setting, 1,
                setup.psy_noise_dBsuppress,
                setup.psy_noise_bias_padding,
                setup.psy_noiseguards, 0.);
        if (!singleblock) {
            encode_noisebias_setup(hi.block[2].noise_bias_setting, 2,
                    setup.psy_noise_dBsuppress,
                    setup.psy_noise_bias_trans,
                    setup.psy_noiseguards, 0.);
            encode_noisebias_setup(hi.block[3].noise_bias_setting, 3,
                    setup.psy_noise_dBsuppress,
                    setup.psy_noise_bias_long,
                    setup.psy_noiseguards, 0.);
        }

        encode_ath_setup(0);
        encode_ath_setup(1);
        if (!singleblock) {
            encode_ath_setup(2);
            encode_ath_setup(3);
        }

        encode_map_n_res_setup(hi.base_setting, setup.maps);

        /* set bitrate readonlies and management */
        if (hi.bitrate_av > 0) {
            this.bitrate_nominal = hi.bitrate_av;
        } else {
            this.bitrate_nominal = (int) setting_to_approx_bitrate();
        }

        this.bitrate_lower = hi.bitrate_min;
        this.bitrate_upper = hi.bitrate_max;
        if (hi.bitrate_av != 0) {
            this.bitrate_window = hi.bitrate_reservoir / hi.bitrate_av;// FIXME why double?
        } else {
            this.bitrate_window = 0;// FIXME why double 0.?
        }

        if (hi.managed) {
            ci.bi.avg_rate = hi.bitrate_av;
            ci.bi.min_rate = hi.bitrate_min;
            ci.bi.max_rate = hi.bitrate_max;

            ci.bi.reservoir_bits = hi.bitrate_reservoir;
            ci.bi.reservoir_bias = hi.bitrate_reservoir_bias;

            ci.bi.slew_damp = hi.bitrate_av_damp;

        }

        return (0);

    }

    private final void encode_setup_setting(final int nchannels, final int srate) {
        final CodecSetupInfo ci = this.codec_setup;
        final HighLevelEncodeSetup hi = ci.hi;
        final EncSetupDataTemplate setup = hi.setup;

        this.version = 0;
        this.channels = nchannels;
        this.rate = srate;

        hi.impulse_block_p = true;
        hi.noise_normalize_p = true;

        final int is = (int) hi.base_setting;
        final double ds = hi.base_setting - is;
        final double ds1 = 1. - ds;// java

        hi.stereo_point_setting = hi.base_setting;

        if (!hi.lowpass_altered) {
            hi.lowpass_kHz =
                    setup.psy_lowpass[is] * ds1 + setup.psy_lowpass[is + 1] * ds;
        }

        hi.ath_floating_dB = setup.psy_ath_float[is] * ds1 +
                setup.psy_ath_float[is + 1] * ds;
        hi.ath_absolute_dB = setup.psy_ath_abs[is] * ds1 +
                setup.psy_ath_abs[is + 1] * ds;

        hi.amplitude_track_dBpersec = -6.;
        hi.trigger_setting = hi.base_setting;

        final HighLevelByBlockType[] block = hi.block;// java
        for (int i = 0; i < 4; i++) {
            final HighLevelByBlockType bi = block[i];// java
            bi.tone_mask_setting = hi.base_setting;
            bi.tone_peaklimit_setting = hi.base_setting;
            bi.noise_bias_setting = hi.base_setting;
            bi.noise_compand_setting = hi.base_setting;
        }
    }

    /**
     * This function performs step-one of a three-step variable bitrate
     * (quality-based) encode setup.  It functions similarly to the one-step setup
     * performed by {@link #encode_init_vbr} but allows an application to
     * make further encode setup tweaks using {@link #encode_ctl} before
     * finally calling {@link #encode_setup_init} to complete the setup
     * process.
     * <p>
     * Before this function is called, the {@link Info} struct should be
     * initialized by using {@link #init} from the libvorbis API.  After
     * encoding, {@link #clear} should be called.
     *
     * @param nchannels The number of channels to be encoded.
     * @param srate     The sampling rate of the source audio.
     * @param quality   Desired quality level, currently from -0.1 to 1.0 (lo to hi).
     * @return Zero for success, and negative values for failure.
     * @retval 0          Success
     * @retval OV_EFAULT  Internal logic fault; indicates a bug or heap/stack corruption.
     * @retval OV_EINVAL  Invalid setup request, eg, out of range argument.
     * @retval OV_EIMPL   Unimplemented mode; unable to comply with quality level request.
     */
    public final int encode_setup_vbr(final int nchannels, final int srate, float quality) {
        if (srate <= 0) {
            return Codec.OV_EINVAL;
        }

        final HighLevelEncodeSetup hi = this.codec_setup.hi;

        quality += .0000001f;
        if (quality >= 1.f) {
            quality = .9999f;
        }

        hi.req = quality;
        /*hi.setup = */
        get_setup_template(nchannels, srate, quality, false, hi);
        if (hi.setup == null) {
            return Codec.OV_EIMPL;
        }

        encode_setup_setting(nchannels, srate);
        hi.managed = false;
        hi.coupling_p = true;

        return 0;
    }

    /**
     * This is the primary function within libvorbisenc for setting up variable
     * bitrate ("quality" based) modes.
     * <p>
     * Before this function is called, the vorbis_info struct should be
     * initialized by using vorbis_info_init() from the libvorbis API. After
     * encoding, vorbis_info_clear() should be called.
     *
     * @param nchannels    The number of channels to be encoded.
     * @param srate        The sampling rate of the source audio.
     * @param base_quality Desired quality level, currently from -0.1 to 1.0 (lo to hi).
     * @return Zero for success, or a negative number for failure.
     * @retval 0           Success
     * @retval OV_EFAULT   Internal logic fault; indicates a bug or heap/stack corruption.
     * @retval OV_EINVAL   Invalid setup request, eg, out of range argument.
     * @retval OV_EIMPL    Unimplemented mode; unable to comply with quality level request.
     */
    public final int encode_init_vbr(final int nchannels, final int srate,
                                     final float base_quality /* 0. to 1. */) {
        int ret = encode_setup_vbr(nchannels, srate, base_quality);

        if (ret != 0) {
            clear();
            return ret;
        }
        ret = encode_setup_init();
        if (ret != 0) {
            clear();
        }
        return (ret);
    }

    /**
     * This function performs step-one of a three-step bitrate-managed encode
     * setup.  It functions similarly to the one-step setup performed by
     * {@link #encode_init} but allows an application to make further encode setup
     * tweaks using {@link #encode_ctl} before finally calling
     * {@link #encode_setup_init} to complete the setup process.
     * <p>
     * Before this function is called, the {@link Info} struct should be
     * initialized by using vorbis_info_init() from the libvorbis API.  After
     * encoding, vorbis_info_clear() should be called.
     * <p>
     * The max_bitrate, nominal_bitrate, and min_bitrate settings are used to set
     * constraints for the encoded file.  This function uses these settings to
     * select the appropriate encoding mode and set it up.
     *
     * @param nchannels       The number of channels to be encoded.
     * @param srate           The sampling rate of the source audio.
     * @param max_bitrate     Desired maximum bitrate (limit). -1 indicates unset.
     * @param nominal_bitrate Desired average, or central, bitrate. -1 indicates unset.
     * @param min_bitrate     Desired minimum bitrate. -1 indicates unset.
     * @return Zero for success, and negative for failure.
     * @retval 0           Success
     * @retval OV_EFAULT   Internal logic fault; indicates a bug or heap/stack corruption.
     * @retval OV_EINVAL   Invalid setup request, eg, out of range argument.
     * @retval OV_EIMPL    Unimplemented mode; unable to comply with bitrate request.
     */
    public final int encode_setup_managed(final int nchannels, final int srate,
                                          final int max_bitrate, int nominal_bitrate, final int min_bitrate) {
        if (srate <= 0) {
            return Codec.OV_EINVAL;
        }

        final HighLevelEncodeSetup hi = this.codec_setup.hi;
        final int tnominal = nominal_bitrate;// FIXME why double?
        if (nominal_bitrate <= 0) {
            if (max_bitrate > 0) {
                if (min_bitrate > 0) {
                    nominal_bitrate = (max_bitrate + min_bitrate) >>> 1;
                } else {
                    nominal_bitrate = (int) (max_bitrate * .875f);
                }
            } else {
                if (min_bitrate > 0) {
                    nominal_bitrate = min_bitrate;
                } else {
                    return (Codec.OV_EINVAL);
                }
            }
        }

        hi.req = nominal_bitrate;
        /*hi.setup = */
        get_setup_template(nchannels, srate, nominal_bitrate, true, hi);
        if (hi.setup == null) {
            return Codec.OV_EIMPL;
        }

        encode_setup_setting(nchannels, srate);

        /* initialize management with sane defaults */
        hi.coupling_p = true;
        hi.managed = true;
        hi.bitrate_min = min_bitrate;
        hi.bitrate_max = max_bitrate;
        hi.bitrate_av = tnominal;
        hi.bitrate_av_damp = 1.5f; /* full range in no less than 1.5 second */
        hi.bitrate_reservoir = nominal_bitrate << 1;
        hi.bitrate_reservoir_bias = .1; /* bias toward hoarding bits */

        return (0);

    }

    /**
     * This is the primary function within libvorbisenc for setting up managed
     * bitrate modes.
     * <p>
     * Before this function is called, the {@link Info}
     * struct should be initialized by using vorbis_info_init() from the libvorbis
     * API.  After encoding, vorbis_info_clear() should be called.
     * <p>
     * The max_bitrate, nominal_bitrate, and min_bitrate settings are used to set
     * constraints for the encoded file.  This function uses these settings to
     * select the appropriate encoding mode and set it up.
     *
     * @param nchannels       The number of channels to be encoded.
     * @param srate           The sampling rate of the source audio.
     * @param max_bitrate     Desired maximum bitrate (limit). -1 indicates unset.
     * @param nominal_bitrate Desired average, or central, bitrate. -1 indicates unset.
     * @param min_bitrate     Desired minimum bitrate. -1 indicates unset.
     * @return Zero for success, and negative values for failure.
     * @retval 0          Success.
     * @retval OV_EFAULT  Internal logic fault; indicates a bug or heap/stack corruption.
     * @retval OV_EINVAL  Invalid setup request, eg, out of range argument.
     * @retval OV_EIMPL   Unimplemented mode; unable to comply with bitrate request.
     */
    public final int encode_init(final int nchannels, final int srate,
                                 final int max_bitrate, final int nominal_bitrate, final int min_bitrate) {

        int ret = encode_setup_managed(nchannels, srate,
                max_bitrate, nominal_bitrate, min_bitrate);
        if (ret != 0) {
            clear();
            return (ret);
        }

        ret = encode_setup_init();
        if (ret != 0) {
            clear();
        }
        return (ret);
    }

    /**
     * This function implements a generic interface to miscellaneous encoder
     * settings similar to the classic UNIX 'ioctl()' system call.  Applications
     * may use vorbis_encode_ctl() to query or set bitrate management or quality
     * mode details by using one of several request arguments detailed below.
     * vorbis_encode_ctl() must be called after one of
     * vorbis_encode_setup_managed() or vorbis_encode_setup_vbr().  When used
     * to modify settings, {@link #encode_ctl} must be called before
     * {@link #encode_setup_init}.
     *
     * @param number Specifies the desired action; See {@link EncCtlCodes}
     *               "the list of available requests".
     * @param arg    Object pointing to a data structure matching the request
     *               argument.
     * @retval 0          Success. Any further return information (such as the result of a
     * query) is placed into the storage pointed to by Object.
     * @retval OV_EINVAL  Invalid argument, or an attempt to modify a setting after
     * calling vorbis_encode_setup_init().
     * @retval OV_EIMPL   Unimplemented or unknown request
     */
    @SuppressWarnings("deprecation")
    public final int encode_ctl(final int number, final Object arg) {
        //if( vi != null ) {
        final HighLevelEncodeSetup hi = this.codec_setup.hi;
        final int setp = (number & 0xf); /* a read request has a low nibble of 0 */

        if (setp != 0 && hi.set_in_stone != 0) {
            return (Codec.OV_EINVAL);
        }

        switch (number) {

            /* now deprecated *****************/
            case EncCtlCodes.OV_ECTL_RATEMANAGE_GET: {

                final EncCtlRateManageArg ai = (EncCtlRateManageArg) arg;

                ai.management_active = hi.managed;
                ai.bitrate_hard_window = ai.bitrate_av_window = (double) hi.bitrate_reservoir / this.rate;
                ai.bitrate_av_window_center = 1.;
                ai.bitrate_hard_min = hi.bitrate_min;
                ai.bitrate_hard_max = hi.bitrate_max;
                ai.bitrate_av_lo = hi.bitrate_av;
                ai.bitrate_av_hi = hi.bitrate_av;

            }
            return (0);

            /* now deprecated *****************/
            case EncCtlCodes.OV_ECTL_RATEMANAGE_SET: {
                final EncCtlRateManageArg ai = (EncCtlRateManageArg) arg;
                if (ai == null) {
                    hi.managed = false;
                } else {
                    hi.managed = ai.management_active;
                    encode_ctl(EncCtlCodes.OV_ECTL_RATEMANAGE_AVG, arg);
                    encode_ctl(EncCtlCodes.OV_ECTL_RATEMANAGE_HARD, arg);
                }
            }
            return 0;

            /* now deprecated *****************/
            case EncCtlCodes.OV_ECTL_RATEMANAGE_AVG: {
                final EncCtlRateManageArg ai = (EncCtlRateManageArg) arg;
                if (ai == null) {
                    hi.bitrate_av = 0;
                } else {// FIXME is this a bug? (ai.bitrate_av_lo + ai.bitrate_av_hi) * .5;
                    hi.bitrate_av = (ai.bitrate_av_lo + ai.bitrate_av_hi) >>> 1;
                }
            }
            return (0);
            /* now deprecated *****************/
            case EncCtlCodes.OV_ECTL_RATEMANAGE_HARD: {
                final EncCtlRateManageArg ai = (EncCtlRateManageArg) arg;
                if (ai == null) {
                    hi.bitrate_min = 0;
                    hi.bitrate_max = 0;
                } else {
                    hi.bitrate_min = ai.bitrate_hard_min;
                    hi.bitrate_max = ai.bitrate_hard_max;
                    hi.bitrate_reservoir = (int) (ai.bitrate_hard_window * (hi.bitrate_max + hi.bitrate_min) * .5);
                }
                if (hi.bitrate_reservoir < 128) {
                    hi.bitrate_reservoir = 128;// FIXME double 128. is this a bug?
                }
            }
            return (0);

            /* replacement ratemanage interface */
            case EncCtlCodes.OV_ECTL_RATEMANAGE2_GET: {
                final EncCtlRateManageArg2 ai = (EncCtlRateManageArg2) arg;
                if (ai == null) {
                    return Codec.OV_EINVAL;
                }

                ai.management_active = hi.managed;
                ai.bitrate_limit_min_kbps = hi.bitrate_min / 1000;
                ai.bitrate_limit_max_kbps = hi.bitrate_max / 1000;
                ai.bitrate_average_kbps = hi.bitrate_av / 1000;
                ai.bitrate_average_damping = hi.bitrate_av_damp;
                ai.bitrate_limit_reservoir_bits = hi.bitrate_reservoir;
                ai.bitrate_limit_reservoir_bias = hi.bitrate_reservoir_bias;
            }
            return (0);
            case EncCtlCodes.OV_ECTL_RATEMANAGE2_SET: {
                final EncCtlRateManageArg2 ai = (EncCtlRateManageArg2) arg;
                if (ai == null) {
                    hi.managed = false;
                } else {
                    /* sanity check; only catch invariant violations */
                    if (ai.bitrate_limit_min_kbps > 0 &&
                            ai.bitrate_average_kbps > 0 &&
                            ai.bitrate_limit_min_kbps > ai.bitrate_average_kbps) {
                        return Codec.OV_EINVAL;
                    }

                    if (ai.bitrate_limit_max_kbps > 0 &&
                            ai.bitrate_average_kbps > 0 &&
                            ai.bitrate_limit_max_kbps < ai.bitrate_average_kbps) {
                        return Codec.OV_EINVAL;
                    }

                    if (ai.bitrate_limit_min_kbps > 0 &&
                            ai.bitrate_limit_max_kbps > 0 &&
                            ai.bitrate_limit_min_kbps > ai.bitrate_limit_max_kbps) {
                        return Codec.OV_EINVAL;
                    }

                    if (ai.bitrate_average_damping <= 0.) {
                        return Codec.OV_EINVAL;
                    }

                    if (ai.bitrate_limit_reservoir_bits < 0) {
                        return Codec.OV_EINVAL;
                    }

                    if (ai.bitrate_limit_reservoir_bias < 0.) {
                        return Codec.OV_EINVAL;
                    }

                    if (ai.bitrate_limit_reservoir_bias > 1.) {
                        return Codec.OV_EINVAL;
                    }

                    hi.managed = ai.management_active;
                    hi.bitrate_min = ai.bitrate_limit_min_kbps * 1000;
                    hi.bitrate_max = ai.bitrate_limit_max_kbps * 1000;
                    hi.bitrate_av = ai.bitrate_average_kbps * 1000;
                    hi.bitrate_av_damp = ai.bitrate_average_damping;
                    hi.bitrate_reservoir = ai.bitrate_limit_reservoir_bits;
                    hi.bitrate_reservoir_bias = ai.bitrate_limit_reservoir_bias;
                }
            }
            return 0;

            case EncCtlCodes.OV_ECTL_LOWPASS_GET: {
                final double[] farg = (double[]) arg;
                farg[0] = hi.lowpass_kHz;
            }
            return (0);
            case EncCtlCodes.OV_ECTL_LOWPASS_SET: {
                final double[] farg = (double[]) arg;
                hi.lowpass_kHz = farg[0];

                if (hi.lowpass_kHz < 2.) {
                    hi.lowpass_kHz = 2.;
                }
                if (hi.lowpass_kHz > 99.) {
                    hi.lowpass_kHz = 99.;
                }
                hi.lowpass_altered = true;
            }
            return (0);
            case EncCtlCodes.OV_ECTL_IBLOCK_GET: {
                final double[] farg = (double[]) arg;
                farg[0] = hi.impulse_noisetune;
            }
            return (0);
            case EncCtlCodes.OV_ECTL_IBLOCK_SET: {
                final double[] farg = (double[]) arg;
                hi.impulse_noisetune = farg[0];

                if (hi.impulse_noisetune > 0.) {
                    hi.impulse_noisetune = 0.;
                }
                if (hi.impulse_noisetune < -15.) {
                    hi.impulse_noisetune = -15.;
                }
            }
            return (0);
            case EncCtlCodes.OV_ECTL_COUPLING_GET: {
                final boolean[] iarg = (boolean[]) arg;
                iarg[0] = hi.coupling_p;
            }
            return (0);
            case EncCtlCodes.OV_ECTL_COUPLING_SET: {
                final int[] iarg = (int[]) arg;
                hi.coupling_p = (iarg[0] != 0);
                if (hi.setup == null) {
                    return Codec.OV_EIMPL;
                }
				/* Fetching a new template can alter the base_setting, which
				  many other parameters are based on.  Right now, the only
				  parameter drawn from the base_setting that can be altered
				  by an encctl is the lowpass, so that is explictly flagged
				  to not be overwritten when we fetch a new template and
				  recompute the dependant settings */
                /*new_template = */
                get_setup_template(hi.coupling_p ? this.channels : -1,
                        this.rate,
                        hi.req,
                        hi.managed,
                        hi);
                encode_setup_setting(this.channels, this.rate);
            }
            return (0);
        }
        return (Codec.OV_EIMPL);
        //}
        //return (Codec.OV_EINVAL);
    }
}
