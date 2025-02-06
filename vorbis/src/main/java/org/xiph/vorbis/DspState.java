package org.xiph.vorbis;

import java.util.Arrays;

/**
 * vorbis_dsp_state buffers the current vorbis audio
 * analysis/synthesis state.  The DSP state belongs to a specific
 * logical bitstream
 */
public class DspState {
    private static final int BLOCKTYPE_IMPULSE = 0;
    private static final int BLOCKTYPE_PADDING = 1;
    private static final int BLOCKTYPE_TRANSITION = 0;
    private static final int BLOCKTYPE_LONG = 1;
    //
    private int analysisp = 0;
    Info vi = null;

    //public float[][] pcm = null; moved to pcm_data.pcm
    //public int pcmret = 0; moved to pcm_data.pcmret
    private final PcmHelperStruct pcm_data = new PcmHelperStruct();// helper object
    private int pcm_storage = 0;
    private int pcm_current = 0;
    private int pcm_returned = 0;

    private int preextrapolate = 0;
    private int eofflag = 0;

    /**
     * previous window size
     */
    private int lW = 0;
    /**
     * current window size
     */
    private int W = 0;
    private int nW = 0;
    private int centerW = 0;

    private long granulepos = 0;
    public long sequence = 0;
    /* FIXME never read glue_bits, time_bits, floor_bits, res_bits
    private long glue_bits = 0;
    private long time_bits = 0;
    private long floor_bits = 0;
    private long res_bits = 0;
    */
    PrivateState backend_state = null;

    //
    final void m_clear() {
        analysisp = 0;
        vi = null;

        pcm_data.clear();
        pcm_storage = 0;
        pcm_current = 0;
        pcm_returned = 0;

        preextrapolate = 0;
        eofflag = 0;

        lW = 0;
        W = 0;
        nW = 0;
        centerW = 0;

        granulepos = 0;
        sequence = 0;
		/*
		m_glue_bits = 0;
		m_time_bits = 0;
		m_floor_bits = 0;
		m_res_bits = 0;
		 */
        backend_state = null;
    }

    /********************************************************************
     function: PCM data vector blocking, windowing and dis/reassembly

     Handle windowing, overlap-add, etc of the PCM vectors.  This is made
     more amusing by Vorbis' current two allowed block sizes.
     ********************************************************************/

/* pcm accumulator examples (not exhaustive):

 <-------------- lW ---------------->
                   <--------------- W ---------------->
:            .....|.....       _______________         |
:        .'''     |     '''_---      |       |\        |
:.....'''         |_____--- '''......|       | \_______|
:.................|__________________|_______|__|______|
                  |<------ Sl ------>|      > Sr <     |endW
                  |beginSl           |endSl  |  |endSr
                  |beginW            |endlW  |beginSr


                      |< lW >|
                   <--------------- W ---------------->
                  |   |  ..  ______________            |
                  |   | '  `/        |     ---_        |
                  |___.'___/`.       |         ---_____|
                  |_______|__|_______|_________________|
                  |      >|Sl|<      |<------ Sr ----->|endW
                  |       |  |endSl  |beginSr          |endSr
                  |beginW |  |endlW
                  mult[0] |beginSl                     mult[n]

 <-------------- lW ----------------->
                          |<--W-->|
:            ..............  ___  |   |
:        .'''             |`/   \ |   |
:.....'''                 |/`....\|...|
:.........................|___|___|___|
                          |Sl |Sr |endW
                          |   |   |endSr
                          |   |beginSr
                          |   |endSl
                          |beginSl
                          |beginW
*/
    public final int block_init(final Block vb) {
        vb.clear();
        vb.vd = this;
        // already 0? vb.localalloc = 0;
        // already null? vb.localstore = null;
        if (this.analysisp != 0) {
            final BlockInternal vbi =
                    vb.m_internal = new BlockInternal();
            vbi.ampmax = -9999;

            final Buffer[] packetblob = vbi.packetblob;// java
            for (int i = 0; i < Info.PACKETBLOBS; i++) {
                if (i == Info.PACKETBLOBS / 2) {
                    packetblob[i] = vb.opb;
                } else {
                    packetblob[i] = new Buffer();
                }
                packetblob[i].pack_writeinit();
            }
        }

        return (0);
    }

    /**
     * Analysis side code, but directly related to blocking.  Thus it's
     * here and not in analysis.c (which is for analysis transforms only).
     * The init is here because some of it is shared
     */
    private final boolean _shared_init(final Info vinfo, final boolean encp) {
        final CodecSetupInfo ci = vinfo.codec_setup;

        if (ci == null ||
                ci.modes <= 0 ||
                ci.blocksizes[0] < 64 ||
                ci.blocksizes[1] < ci.blocksizes[0]) {
            return true;
        }

        final int hs = ci.halfrate_flag;

        m_clear();
        final PrivateState b = this.backend_state = new PrivateState();

        this.vi = vinfo;
        b.modebits = Codec.ilog(ci.modes - 1);

        b.transform[0] = new MDCTLookup[Info.VI_TRANSFORMB];
        b.transform[1] = new MDCTLookup[Info.VI_TRANSFORMB];

        /* MDCT is tranform 0 */
        final int[] blocksizes = ci.blocksizes;// java

        b.transform[0][0] = new MDCTLookup();
        b.transform[1][0] = new MDCTLookup();
        b.transform[0][0].init(blocksizes[0] >>> hs);
        b.transform[1][0].init(blocksizes[1] >>> hs);

        /* Vorbis I uses only window type 0 */
		/* note that the correct computation below is technically:
		   b->window[0]=ov_ilog(ci->blocksizes[0]-1)-6;
		   b->window[1]=ov_ilog(ci->blocksizes[1]-1)-6;
		but since blocksizes are always powers of two,
		the below is equivalent.
		 */
        b.window[0] = Codec.ilog(blocksizes[0]) - 7;
        b.window[1] = Codec.ilog(blocksizes[1]) - 7;

        final int books = ci.books;// java
        final StaticCodebook[] book_param = ci.book_param;// java
        if (encp) { /* encode/decode differ here */

            /* analysis always needs an fft */
            SmallFT.drft_init(b.fft_look[0], blocksizes[0]);
            SmallFT.drft_init(b.fft_look[1], blocksizes[1]);

            /* finish the codebooks */
            if (ci.fullbooks == null) {
                final Codebook[] fullbooks = new Codebook[books];
                ci.fullbooks = fullbooks;
                for (int i = 0; i < books; i++) {
                    fullbooks[i] = new Codebook();
                    fullbooks[i].init_encode(book_param[i]);
                }
            }

            final LookPsy[] psy = new LookPsy[ci.psys];
            b.psy = psy;
            final InfoPsy[] psy_param = ci.psy_param;// java
            for (int i = 0; i < ci.psys; i++) {
                psy[i] = new LookPsy();
                final InfoPsy p = psy_param[i];
                psy[i]._init(
                        p, ci.psy_g_param, blocksizes[p.blockflag] >> 1, vinfo.rate);
            }

            this.analysisp = 1;
        } else {
            /* finish the codebooks */
            if (ci.fullbooks == null) {
                ci.fullbooks = new Codebook[books];
                for (int i = 0; i < books; i++) {
                    ci.fullbooks[i] = new Codebook();
                    if (book_param[i] == null ||
                            ci.fullbooks[i].init_decode(book_param[i]) != 0) {// abort_books:
                        for (i = 0; i < books; i++) {
                            if (book_param[i] != null) {
                                book_param[i].destroy();
                                book_param[i] = null;
                            }
                        }
                        clear();
                        return true;
                    }
                    /* decode codebooks are now standalone after init */
                    book_param[i].destroy();
                    book_param[i] = null;
                }
            }
        }

		/* initialize the storage vectors. blocksize[1] is small for encode,
		   but the correct size for decode */
        this.pcm_storage = blocksizes[1];
        this.pcm_data.pcm = new float[vinfo.channels][this.pcm_storage];
        //this.m_pcmret = new int[vi.channels]; in java it integer

        /* all 1 (large block) or 0 (small block) */
        /* explicitly set for the sake of clarity */
        this.lW = 0; /* previous window size */
        this.W = 0;  /* current window size */

        /* all vector indexes */
        this.centerW = blocksizes[1] >>> 1;// / 2;

        this.pcm_current = this.centerW;

        /* initialize all the backend lookups */
        final LookFloor[] flr = new LookFloor[ci.floors];
        b.flr = flr;
        final InfoFloor[] floor_param = ci.floor_param;// java
        final FuncFloor[] floor_p = Codec._floor_P;
        int[] tmp = ci.floor_type;// java
        for (int i = 0, ie = ci.floors; i < ie; i++) {
            flr[i] = floor_p[tmp[i]].look(this, floor_param[i]);
        }

        final LookResidue[] residue = new LookResidue[ci.residues];
        b.residue = residue;
        final InfoResidue[] residue_param = ci.residue_param;// java
        final FuncResidue[] residue_p = Codec._residue_P;// java
        tmp = ci.residue_type;
        for (int i = 0, ie = ci.residues; i < ie; i++) {
            residue[i] = residue_p[tmp[i]].look(this, residue_param[i]);
        }

        return false;
    }

    /**
     * arbitrary settings and spec-mandated numbers get filled in here
     */
    public final boolean analysis_init(final Info vinfo) {

        if (_shared_init(vinfo, true)) {
            return true;
        }
        final PrivateState b = backend_state;
        b.psy_g_look = vinfo._global_look();

        /* Initialize the envelope state storage */
        b.ve = new EnvelopeLookup();
        b.ve._init(vinfo);

        vinfo.bitrate_init(b.bms);

		/* compressed audio packets start after the headers
		 with sequence number 3 */
        this.sequence = 3;

        return false;
    }

    public final void clear() {
        //if( v != null ) {
        final Info vinfo = this.vi;
        final CodecSetupInfo ci = (vinfo != null ? vinfo.codec_setup : null);
        PrivateState b = this.backend_state;

        if (b != null) {

            if (b.ve != null) {
                b.ve._clear();
                b.ve = null;
            }

            if (b.transform[0] != null) {
                b.transform[0][0].clear();
                b.transform[0][0] = null;
                b.transform[0] = null;
            }
            if (b.transform[1] != null) {
                b.transform[1][0].clear();
                b.transform[1][0] = null;
                b.transform[1] = null;
            }

            if (b.flr != null) {
                if (ci != null) {
                    for (int i = 0; i < ci.floors; i++) {
                        //Registry._floor_P[ci.floor_type[i]].free_look( b.flr[i] );
                        b.flr[i] = null;
                    }
                }
                b.flr = null;
            }
            if (b.residue != null) {
                if (ci != null) {
                    for (int i = 0; i < ci.residues; i++) {
                        Codec._residue_P[ci.residue_type[i]].free_look(b.residue[i]);
                    }
                }
                b.residue = null;
            }
            if (b.psy != null) {
                if (ci != null) {
                    for (int i = 0; i < ci.psys; i++) {
                        b.psy[i]._clear();
                    }
                }
                b.psy = null;
            }

            if (b.psy_g_look != null) {
                b.psy_g_look = null;
            }
            b.bms.clear();

            b.fft_look[0].clear();
            b.fft_look[1].clear();

        }

        if (this.pcm_data.pcm != null) {
            if (vinfo != null) {
                for (int i = 0; i < vinfo.channels; i++) {
                    if (this.pcm_data.pcm[i] != null) {
                        this.pcm_data.pcm[i] = null;
                    }
                }
            }
            this.pcm_data.pcm = null;
            this.pcm_data.pcmret = 0;// if( pcmret != null ) pcmret = null;
        }

        if (b != null) {
            /* free header, header1, header2 */
            if (b.header != null) {
                b.header = null;
            }
            if (b.header1 != null) {
                b.header1 = null;
            }
            if (b.header2 != null) {
                b.header2 = null;
            }
            b = null;
        }

        m_clear();
        //}
    }

    public final PcmHelperStruct analysis_buffer(final int vals) {

        final PrivateState b = this.backend_state;

        /* free header, header1, header2 */
        b.header = null;
        b.header1 = null;
        b.header2 = null;

		/* Do we have enough storage space for the requested buffer? If not,
		   expand the PCM (and envelope) storage */

        if (this.pcm_current + vals >= this.pcm_storage) {
            this.pcm_storage = this.pcm_current + (vals << 1);

            final float[][] pcm = this.pcm_data.pcm;// java
            for (int i = 0, ie = this.vi.channels; i < ie; i++) {
                if (pcm[i] == null) {
                    pcm[i] = new float[this.pcm_storage];
                } else {
                    pcm[i] = Arrays.copyOf(pcm[i], this.pcm_storage);
                }
            }
        }

        //for( i = 0; i < vi.channels; i++ )
        this.pcm_data.pcmret = this.pcm_current;//this.m_pcmret[i] = this.m_pcm_current;

        return this.pcm_data;//return (this.m_pcmret);
    }

    // lpc.c

    /**
     * Autocorrelation LPC coeff generation algorithm invented by
     * N. Levinson in 1947, modified by J. Durbin in 1959.<p>
     * <p>
     * Input : n elements of time doamin data<br>
     * Output: m lpc coefficients, excitation energy
     */
    private static float lpc_from_data(final float[] data, final int offset,
                                       final float[] lpci, int n, final int m) {
        final double[] aut = new double[m + 1];
        final double[] lpc = new double[m];

        /* autocorrelation, p+1 lag coefficients */
        int j = m + 1;
        n += offset;
        while (j-- > 0) {
            double d = 0; /* double needed for accumulator depth */
            for (int i = j + offset, k = offset; i < n; i++, k++) {
                d += (double) data[i] * data[k];
            }
            aut[j] = d;
        }

        /* Generate lpc coefficients from autocorr values */

        /* set our noise floor to about -100dB */
        double error = aut[0] * (1. + 1e-10);
        final double epsilon = 1e-9 * aut[0] + 1e-10;

        for (int i = 0; i < m; i++) {
            double r = -aut[i + 1];

            if (error < epsilon) {
                Arrays.fill(lpc, i, m, 0.0);
                break;// goto done;
            }

			/* Sum up this iteration's reflection coefficient; note that in
			   Vorbis we don't save it.  If anyone wants to recycle this code
			   and needs reflection coefficients, save the results of 'r' from
			   each iteration. */

            int k = i;
            for (j = 0; j < i; j++, k--) {
                r -= lpc[j] * aut[k];
            }
            r /= error;

            /* Update LPC coefficients and total error */

            lpc[i] = r;
            k = (i >>> 1);
            for (j = 0; j < k; j++) {
                final double tmp = lpc[j];

                n = i - 1 - j;
                lpc[j] += r * lpc[n];
                lpc[n] += r * tmp;
            }
            if ((i & 1) != 0) {
                lpc[j] += lpc[j] * r;
            }

            error *= 1.0 - r * r;

        }

//done:

        /* slightly damp the filter */
        {
            final double g = .99;
            double damp = g;
            for (j = 0; j < m; j++) {
                lpc[j] *= damp;
                damp *= g;
            }
        }

        for (j = 0; j < m; j++) {
            lpci[j] = (float) lpc[j];
        }

		/* we need the error value to know how big an impulse to hit the
		 filter with later */

        return (float) error;
    }

    private static void lpc_predict(final float[] coeff,
                                    final float[] prime, final int offset,
                                    final int m, final float[] data, int data_offset, final int n) {

		/* in: coeff[0...m-1] LPC coefficients
			 prime[0...m-1] initial values (allocated size of n+m-1)
		out: data[0...n-1] data samples */

        final float[] work = new float[m + n];

        if (prime == null) {
            for (int i = 0; i < m; i++) {
                work[i] = 0.f;
            }
        } else {
            for (int i = 0, j = offset; i < m; i++, j++) {
                work[i] = prime[j];
            }
        }

        for (int i = 0; i < n; i++) {
            float y = 0.f;
            int o = i;
            int p = m;
            for (int j = 0; j < m; j++) {
                y -= work[o++] * coeff[--p];
            }

            data[data_offset++] = work[o] = y;
        }
    }

    private final void _preextrapolate_helper() {
        final int order = 16;
        final float[] lpc = new float[order];
        final float[] work = new float[this.pcm_current];
        final int delta = this.pcm_current - this.centerW;

        this.preextrapolate = 1;

        if (delta > (order << 1)) { /* safety */
            final float[][] pcm = this.pcm_data.pcm;// java
            for (int i = 0, channels = this.vi.channels; i < channels; i++) {
                final float[] v_pvm_i = pcm[i];
                /* need to run the extrapolation in reverse! */
                for (int j = 0, je = this.pcm_current, k = this.pcm_current - 1; j < je; j++, k--) {
                    work[j] = v_pvm_i[k];
                }

                /* prime as above */
                lpc_from_data(work, 0, lpc, delta, order);

/* #if 0
				if( this.m_vi.channels == 2 ) {
					if( i == 0 )
						Analysis._analysis_output("predataL", 0, work, 0, this.m_pcm_current - this.m_centerW, false, false, 0 );
					else
						Analysis._analysis_output("predataR", 0, work, 0, this.m_pcm_current - this.m_centerW, false, false, 0 );
				} else {
					Analysis._analysis_output("predata", 0, work, 0, this.m_pcm_current - this.m_centerW, false, false, 0 );
				}
#endif */

                /* run the predictor filter */
                lpc_predict(lpc, work, delta - order,
                        order, work, delta, this.centerW);

                for (int j = 0, je = this.pcm_current, k = this.pcm_current - 1; j < je; j++, k--) {
                    v_pvm_i[k] = work[j];
                }
            }
        }
    }

    /**
     * call with val<=0 to set eof
     */
    public final int analysis_wrote(final int vals) {
        final Info vinfo = this.vi;
        final CodecSetupInfo ci = vinfo.codec_setup;

        final int blocksizes1 = ci.blocksizes[1];// java
        if (vals <= 0) {
            final int order = 32;
            final float[] lpc = new float[order];

            /* if it wasn't done earlier (very short sample) */
            if (this.preextrapolate == 0) {
                _preextrapolate_helper();
            }

			/* We're encoding the end of the stream.  Just make sure we have
			   [at least] a few full blocks of zeroes at the end. */
			/* actually, we don't want zeroes; that could drop a large
			   amplitude off a cliff, creating spread spectrum noise that will
			   suck to encode.  Extrapolate for the sake of cleanliness. */

            final int b3 = blocksizes1 * 3;// java
            analysis_buffer(b3);
            final int eof_flag = this.pcm_current;
            this.eofflag = eof_flag;
            this.pcm_current += b3;

            final float[][] pcm = this.pcm_data.pcm;// java
            for (int i = 0, channels = vinfo.channels; i < channels; i++) {
                final float[] v_pcm_i = pcm[i];
                if (eof_flag > (order << 1)) {
                    /* extrapolate with LPC to fill in */
                    /* make a predictor filter */
                    int n = eof_flag;
                    if (n > blocksizes1) {
                        n = blocksizes1;
                    }
                    lpc_from_data(v_pcm_i, eof_flag - n, lpc, n, order);

                    /* run the predictor filter */
                    lpc_predict(lpc, v_pcm_i, eof_flag - order, order,
                            v_pcm_i, eof_flag, this.pcm_current - eof_flag);
                } else {
					/* not enough data to extrapolate (unlikely to happen due to
					   guarding the overlap, but bulletproof in case that
					   assumtion goes away). zeroes will do. */
                    Arrays.fill(v_pcm_i, eof_flag, this.pcm_current, 0);
                }
            }
        } else {

            if (this.pcm_current + vals > this.pcm_storage) {
                return (Codec.OV_EINVAL);
            }

            this.pcm_current += vals;

			/* we may want to reverse extrapolate the beginning of a stream
			   too... in case we're beginning on a cliff! */
            /* clumsy, but simple.  It only runs once, so simple is good. */
            if (this.preextrapolate == 0 && this.pcm_current - this.centerW > blocksizes1) {
                _preextrapolate_helper();
            }

        }
        return (0);
    }

    // envelope.c
/* #if 0
	private static int seq = 0;
	private static long totalshift = -1024;
#endif */

    private final int _envelope_search() {
        final CodecSetupInfo ci = this.vi.codec_setup;
        final InfoPsyGlobal gi = ci.psy_g_param;
        final EnvelopeLookup ve = this.backend_state.ve;

        final int searchstep = ve.searchstep;// java
        int first = ve.current / searchstep;
        final int last = this.pcm_current / searchstep - EnvelopeLookup.VE_WIN;
        if (first < 0) {
            first = 0;
        }

        /* make sure we have enough storage to match the PCM */
        if (last + EnvelopeLookup.VE_WIN + EnvelopeLookup.VE_POST > ve.storage) {
            ve.storage = last + EnvelopeLookup.VE_WIN + EnvelopeLookup.VE_POST; /* be sure */
            ve.mark = Arrays.copyOf(ve.mark, ve.storage);
        }

        final boolean[] mark = ve.mark;// java
        final float[][] pcm = this.pcm_data.pcm;// java
        for (int j = first, js = searchstep * j, jp = j + EnvelopeLookup.VE_POST; j < last; j++, js += searchstep) {
            int ret = 0;

            ve.m_stretch++;
            if (ve.m_stretch > EnvelopeLookup.VE_MAXSTRETCH * 2) {
                ve.m_stretch = EnvelopeLookup.VE_MAXSTRETCH * 2;
            }

            for (int i = 0, ib = 0, ie = ve.ch; i < ie; i++, ib += EnvelopeLookup.VE_BANDS) {
                ret |= ve._amp(gi, pcm[i], js, ve.band, ve.filter, ib);
            }

            mark[jp++] = false;
            if ((ret & 1) != 0) {
                mark[j] = true;
                mark[j + 1] = true;
            }

            if ((ret & 2) != 0) {
                mark[j] = true;
                if (j > 0) {
                    mark[j - 1] = true;
                }
            }

            if ((ret & 4) != 0) {
                ve.m_stretch = -1;
            }
        }

        ve.current = last * searchstep;

        {
            final int center_W = this.centerW;
            final int[] blocksizes = ci.blocksizes;// java
            final int testW = center_W + ((((blocksizes[this.W] + blocksizes[0]) >>> 1) + blocksizes[1]) >>> 1);

            int j = ve.cursor;

            while (j < ve.current - (searchstep)) {/* account for postecho working back one window */
                if (j >= testW) {
                    return (1);
                }

                ve.cursor = j;

                if (mark[j / searchstep]) {
                    if (j > center_W) {

/* if( false ) {
						if( j > ve.m_curmark ) {
							final float[] marker = new float[this.m_pcm_current];// already zeroed
							int l, m;
							System.err.printf("mark! seq=%d, cursor:%fs time:%fs\n",
									seq,
									(totalshift + ve.m_cursor) / 44100.f,
									(totalshift + j) / 44100.f);
							Analysis._analysis_output_always("pcmL", seq, this.m_pcm[0], 0, this.m_pcm_current, false, false, totalshift);
							Analysis._analysis_output_always("pcmR", seq, this.m_pcm[1], 0, this.m_pcm_current, false, false, totalshift);

							Analysis._analysis_output_always("markL", seq, this.m_pcm[0], 0, j, false, false, totalshift);
							Analysis._analysis_output_always("markR", seq, this.m_pcm[1], 0, j, false, false, totalshift);

							for( m = 0; m < EnvelopeLookup.VE_BANDS; m++ ) {
								String buf = String.format("delL%d", m );
								//for( l = 0; l < last; l++ )
								//	marker[l * ve.searchstep] = ve.filter[m].markers[l] * .1;// FIXME envelope_filter_state have no markers!
								Analysis._analysis_output_always( buf, seq, marker, 0, this.m_pcm_current, false, false, totalshift);
							}

							for( m = 0; m < EnvelopeLookup.VE_BANDS; m++ ) {
								String buf = String.format("delR%d", m );
								//for( l = 0; l < last; l++ )
								//	marker[l * ve.searchstep] = ve.filter[m + VE_BANDS].markers[l] * .1;
								Analysis._analysis_output_always( buf, seq, marker, 0, this.m_pcm_current, false, false, totalshift);
							}

							for( l = 0; l < last; l++ )
								marker[l * ve.searchstep] = ve.mark[l] * .4f;
							Analysis._analysis_output_always("mark", seq, marker, 0, this.pcm_current, false, false, totalshift);

							seq++;

						}
} */

                        ve.curmark = j;
                        if (j >= testW) {
                            return (1);
                        }
                        return (0);
                    }
                }
                j += searchstep;
            }
        }

        return (-1);
    }

    // envelope.c
    private final boolean _envelope_mark() {
        final EnvelopeLookup ve = this.backend_state.ve;
        final CodecSetupInfo ci = this.vi.codec_setup;
        final int center_W = this.centerW;
        int beginW = center_W - (ci.blocksizes[this.W] >>> 2);
        int endW = center_W + (ci.blocksizes[this.W] >>> 2);
        if (this.W != 0) {
            beginW -= ci.blocksizes[this.lW] >>> 2;
            endW += ci.blocksizes[this.nW] >>> 2;
        } else {
            beginW -= ci.blocksizes[0] >>> 2;
            endW += ci.blocksizes[0] >>> 2;
        }

        if (ve.curmark >= beginW && ve.curmark < endW) {
            return true;
        }
        {
            final int first = beginW / ve.searchstep;
            final int last = endW / ve.searchstep;
            final boolean[] mark = ve.mark;// java
            for (int i = first; i < last; i++) {
                if (mark[i]) {
                    return true;
                }
            }
        }
        return false;
    }

    private final float _ampmax_decay(float amp) {
        final CodecSetupInfo ci = this.vi.codec_setup;

        final int n = ci.blocksizes[this.W] >>> 1;
        final float secs = (float) n / vi.rate;

        amp += secs * ci.psy_g_param.ampmax_att_per_sec;
        if (amp < -9999) {
            amp = -9999;
        }
        return (amp);
    }

    // block.c

    /**
     * do the deltas, envelope shaping, pre-echo and determine the size of
     * the next block on which to continue analysis
     */
    public final boolean analysis_blockout(final Block vb) {

        /* check to see if we're started... */
        if (this.preextrapolate == 0) {
            return false;
        }

        /* check to see if we're done... */
        if (this.eofflag == -1) {
            return false;
        }

        final Info vinfo = this.vi;
        final CodecSetupInfo ci = vinfo.codec_setup;
        final PrivateState b = this.backend_state;
        final LookPsyGlobal g = b.psy_g_look;
        final int[] blocksizes = ci.blocksizes;// java
        final int beginW = this.centerW - (blocksizes[this.W] >>> 1);
        final BlockInternal vbi = vb.m_internal;

		/* By our invariant, we have lW, W and centerW set.  Search for
		   the next boundary so we can determine nW (the next window size)
		   which lets us compute the shape of the current block's window */

		/* we do an envelope search even on a single blocksize; we may still
		   be throwing more bits at impulses, and envelope search handles
		   marking impulses too. */
        {
            final int bp = _envelope_search();
            if (bp == -1) {

                if (this.eofflag == 0) {
                    return false; /* not enough data currently to search for a
													full long block */
                }
                this.nW = 0;
            } else {

                if (blocksizes[0] == blocksizes[1]) {
                    this.nW = 0;
                } else {
                    this.nW = bp;
                }
            }
        }

        final int centerNext = this.centerW + ((blocksizes[this.W] + blocksizes[this.nW]) >>> 2);

        {
            /* center of next block + next block maximum right side. */

            final int blockbound = centerNext + (blocksizes[this.nW] >>> 1);
            if (this.pcm_current < blockbound) {
                return false; /* not enough data yet;
																although this check is
																less strict that the
																_ve_envelope_search,
																the search is not run
																if we only use one
																block size */
            }
        }

		/* fill in the block.  Note that for a short window, lW and nW are *short*
		   regardless of actual settings in the stream */

        //Block._block_ripcord( vb );
        vb.lW = this.lW;
        vb.W = this.W;
        vb.nW = this.nW;

        if (this.W != 0) {
            if (this.lW == 0 || this.nW == 0) {
                vbi.blocktype = BLOCKTYPE_TRANSITION;
                /* System.err.print("-"); */
            } else {
                vbi.blocktype = BLOCKTYPE_LONG;
                /* System.err.print("_"); */
            }
        } else {
            if (_envelope_mark()) {
                vbi.blocktype = BLOCKTYPE_IMPULSE;
                /* System.err.print("|"); */

            } else {
                vbi.blocktype = BLOCKTYPE_PADDING;
                /* System.err.print("."); */

            }
        }

        vb.vd = this;
        vb.sequence = this.sequence++;
        vb.granulepos = this.granulepos;
        final int pcmend = blocksizes[this.W];
        vb.pcmend = pcmend;
        final int delay_size = pcmend + beginW;// java

        /* copy the vectors; this uses the local storage in vb */

        /* this tracks 'strongest peak' for later psychoacoustics */
        /* moved to the global psy state; clean this mess up */
        if (vbi.ampmax > g.ampmax) {
            g.ampmax = vbi.ampmax;
        }
        g.ampmax = _ampmax_decay(g.ampmax);
        vbi.ampmax = g.ampmax;

        final float[][] b_pcm = new float[vinfo.channels][];
        vb.pcm = b_pcm;
        final float[][] pcmdelay = new float[vinfo.channels][];
        vbi.pcmdelay = pcmdelay;
        final float[][] pcm = this.pcm_data.pcm;// java
        for (int i = 0; i < vinfo.channels; i++) {
            pcmdelay[i] = new float[delay_size];
            System.arraycopy(pcm[i], 0, pcmdelay[i], 0, delay_size);
            b_pcm[i] = new float[pcmend];
            System.arraycopy(pcmdelay[i], beginW, b_pcm[i], 0, pcmend);

			/* before we added the delay
			   vb.pcm[i] = _vorbis_block_alloc( vb, vb.pcmend * sizeof(*vb.pcm[i]) );
			   memcpy( vb.pcm[i], v.pcm[i] + beginW, ci.blocksizes[v.W] * sizeof(*vb.pcm[i]) );
			*/

        }

		/* handle eof detection: eof==0 means that we've not yet received EOF
                           eof>0  marks the last 'real' sample in pcm[]
                           eof<0  'no more to do'; doesn't get here */

        if (this.eofflag != 0) {
            if (this.centerW >= this.eofflag) {
                this.eofflag = -1;
                vb.eofflag = true;
                return true;
            }
        }

        /* advance storage vectors and clean up */
        {
            final int new_centerNext = blocksizes[1] >>> 1;
            final int movementW = centerNext - new_centerNext;

            if (movementW > 0) {

                b.ve._shift(movementW);
                this.pcm_current -= movementW;

                for (int i = 0; i < vinfo.channels; i++) {
                    final float[] v_pcm_i = pcm[i];
                    System.arraycopy(v_pcm_i, movementW, v_pcm_i, 0, this.pcm_current);
                }

                this.lW = this.W;
                this.W = this.nW;
                this.centerW = new_centerNext;

                if (this.eofflag != 0) {
                    this.eofflag -= movementW;
                    if (this.eofflag <= 0) {
                        this.eofflag = -1;
                    }
                    /* do not add padding to end of stream! */
                    if (this.centerW >= this.eofflag) {
                        this.granulepos += movementW - (this.centerW - this.eofflag);
                    } else {
                        this.granulepos += movementW;
                    }
                } else {
                    this.granulepos += movementW;
                }
            }
        }

        /* done */
        return true;
    }

    public final int synthesis_restart() {
        if (this.backend_state == null) {
            return -1;
        }
        final Info vinfo = this.vi;
        if (vinfo == null) {
            return -1;
        }
        final CodecSetupInfo ci = vinfo.codec_setup;
        if (ci == null) {
            return -1;
        }
        final int hs = ci.halfrate_flag;

        this.centerW = ci.blocksizes[1] >> (hs + 1);
        this.pcm_current = this.centerW >> hs;

        this.pcm_returned = -1;
        this.granulepos = -1;
        this.sequence = -1;
        this.eofflag = 0;
        this.backend_state.sample_count = -1;

        return (0);
    }

    public final boolean synthesis_init(final Info vinfo) {
        if (_shared_init(vinfo, false)) {
            clear();
            return true;
        }
        synthesis_restart();
        return false;
    }

    /**
     * Unlike in analysis, the window is only partially applied for each
     * block.  The time domain envelope is not yet handled at the point of
     * calling (as it relies on the previous block).
     */
    public final int synthesis_blockin(final Block vb) {
        if (vb == null) {
            return (Codec.OV_EINVAL);
        }
        if (this.pcm_current > this.pcm_returned && this.pcm_returned != -1) {
            return (Codec.OV_EINVAL);
        }
        final CodecSetupInfo ci = this.vi.codec_setup;
        final PrivateState b = this.backend_state;
        int hs = ci.halfrate_flag;

        this.lW = this.W;
        this.W = vb.W;
        this.nW = -1;

        if ((this.sequence == -1) || (this.sequence + 1 != vb.sequence)) {
            this.granulepos = -1; /* out of sequence; lose count */
            b.sample_count = -1;
        }

        this.sequence = vb.sequence;

        final int[] blocksizes = ci.blocksizes;// java
        if (vb.pcm != null) {  /* no pcm to process if vorbis_synthesis_trackonly was called on block */
            final int n = blocksizes[this.W] >> (++hs);// hs + 1
            final int n0 = blocksizes[0] >> (hs);
            final int n1 = blocksizes[1] >> (hs--);

            final int thisCenter;
            final int prevCenter;
			/*
			this.m_glue_bits += vb.m_glue_bits;
			this.m_time_bits += vb.m_time_bits;
			this.m_floor_bits += vb.m_floor_bits;
			this.m_res_bits += vb.m_res_bits;
			 */
            if (this.centerW != 0) {
                thisCenter = n1;
                prevCenter = 0;
            } else {
                thisCenter = 0;
                prevCenter = n1;
            }

			/* v.pcm is now used like a two-stage double buffer.  We don't want
			   to have to constantly shift *or* adjust memory usage.  Don't
			   accept a new block until the old is shifted out */
            final float[][] data_pcm = this.pcm_data.pcm;// java
            final float[][] vb_pcm = vb.pcm;// java
            final float[] w;// java
            if (this.lW != 0 && this.W != 0) {
                w = Window._window_get(b.window[1] - hs);
            } else {
                w = Window._window_get(b.window[0] - hs);
            }
            final int m = (n1 - n0) >>> 1;// java
            final int h = ((n1 + n0) >>> 1);// java
            for (int j = 0, channels = this.vi.channels; j < channels; j++) {
                final float[] v_pcm_j = data_pcm[j];
                final float[] vb_pcm_j = vb_pcm[j];
                /* the overlap/add section */
                if (this.lW != 0) {
                    if (this.W != 0) {
                        /* large/large */
                        // final float[] w = Window._window_get( window[1] - hs );
                        for (int i = 0, pi = prevCenter, k = n1; i < n1; i++, pi++) {
                            v_pcm_j[pi] = v_pcm_j[pi] * w[--k] + vb_pcm_j[i] * w[i];
                        }

                    } else {
                        /* large/small */
                        // final float[] w = Window._window_get( window[0] - hs );
                        for (int i = 0, pi = prevCenter + m, k = n0; i < n0; i++, pi++) {
                            v_pcm_j[pi] = v_pcm_j[pi] * w[--k] + vb_pcm_j[i] * w[i];
                        }
                    }
                } else {
                    if (this.W != 0) {
                        /* small/large */
                        // final float[] w = Window._window_get( window[0] - hs );
                        int p = m;
                        int pi = prevCenter;
                        for (int i = 0, k = n0; i < n0; i++, pi++) {
                            v_pcm_j[pi] = v_pcm_j[pi] * w[--k] + vb_pcm_j[p++] * w[i];
                        }
                        for (final int pe = h + p; p < pe; ) {
                            v_pcm_j[pi++] = vb_pcm_j[p++];
                        }
                    } else {
                        /* small/small */
                        // final float[] w = Window._vorbis_window_get( window[0] - hs );
                        for (int i = 0, pi = prevCenter, k = n0; i < n0; i++, pi++) {
                            v_pcm_j[pi] = v_pcm_j[pi] * w[--k] + vb_pcm_j[i] * w[i];
                        }
                    }
                }

                /* the copy section */
                {
                    for (int i = n, k = thisCenter, ie = n + n; i < ie; ) {
                        v_pcm_j[k++] = vb_pcm_j[i++];
                    }
                }
            }

            if (this.centerW != 0) {
                this.centerW = 0;
            } else {
                this.centerW = n1;
            }

			/* deal with initial packet state; we do this using the explicit
			   pcm_returned==-1 flag otherwise we're sensitive to first block
			   being short or long */

            if (this.pcm_returned == -1) {
                this.pcm_returned = thisCenter;
                this.pcm_current = thisCenter;
            } else {
                this.pcm_returned = prevCenter;
                this.pcm_current = prevCenter + (((blocksizes[this.lW] + blocksizes[this.W]) >>> 2) >> hs);
            }

        }

		/* track the frame number... This is for convenience, but also
		   making sure our last packet doesn't end with added padding.  If
		   the last packet is partial, the number of samples we'll have to
		   return will be past the vb.granulepos.

		   This is not foolproof!  It will be confused if we begin
		   decoding at the last page after a seek or hole.  In that case,
		   we don't have a starting point to judge where the last frame
		   is.  For this reason, vorbisfile will always try to make sure
		   it reads the last two marked pages in proper sequence */

        if (b.sample_count == -1) {
            b.sample_count = 0;
        } else {
            b.sample_count += (blocksizes[this.lW] + blocksizes[this.W]) >>> 2;
        }

        if (this.granulepos == -1) {
            if (vb.granulepos != -1) { /* only set if we have a position to set to */

                this.granulepos = vb.granulepos;

                /* is this a short page? */
                if (b.sample_count > this.granulepos) {
					/* corner case; if this is both the first and last audio page,
					   then spec says the end is cut, not beginning */
                    int extra = (int) (b.sample_count - vb.granulepos);// FIXME converting from int64 to int32

					/* we use ogg_int64_t for granule positions because a
					   uint64 isn't universally available.  Unfortunately,
					   that means granposes can be 'negative' and result in
					   extra being negative */
                    if (extra < 0) {
                        extra = 0;
                    }

                    if (vb.eofflag) {
                        /* trim the end */
						/* no preceding granulepos; assume we started at zero (we'd
						   have to in a short single-page stream) */
						/* granulepos could be -1 due to a seek, but that would result
						   in a long count, not short count */

						/* Guard against corrupt/malicious frames that set EOP and
						   a backdated granpos; don't rewind more samples than we
						   actually have */
                        final int v = (this.pcm_current - this.pcm_returned) << hs;// java
                        if (extra > v) {
                            extra = v;
                        }

                        this.pcm_current -= extra >> hs;
                    } else {
                        /* trim the beginning */
                        this.pcm_returned += extra >> hs;
                        if (this.pcm_returned > this.pcm_current) {
                            this.pcm_returned = this.pcm_current;
                        }
                    }
                }
            }
        } else {
            this.granulepos += (blocksizes[this.lW] + blocksizes[this.W]) >>> 2;
            if (vb.granulepos != -1 && this.granulepos != vb.granulepos) {

                if (this.granulepos > vb.granulepos) {
                    int extra = (int) (this.granulepos - vb.granulepos);// FIXME converting from int64 to int32

                    if (extra != 0) {
                        if (vb.eofflag) {
                            /* partial last frame.  Strip the extra samples off */

							/* Guard against corrupt/malicious frames that set EOP and
							   a backdated granpos; don't rewind more samples than we
							   actually have */
                            final int v = (this.pcm_current - this.pcm_returned) << hs;// java
                            if (extra > v) {
                                extra = v;
                            }

							/* we use ogg_int64_t for granule positions because a
							   uint64 isn't universally available.  Unfortunately,
							   that means granposes can be 'negative' and result in
							   extra being negative */
                            if (extra < 0) {
                                extra = 0;
                            }

                            this.pcm_current -= extra >> hs;
                        } /* else {Shouldn't happen *unless* the bitstream is out of
					   spec.  Either way, believe the bitstream } */
                    }
                } /* else {Shouldn't happen *unless* the bitstream is out of
				   spec.  Either way, believe the bitstream } */
                this.granulepos = vb.granulepos;
            }
        }

        /* Update, cleanup */

        if (vb.eofflag) {
            this.eofflag = 1;
        }
        return (0);

    }

    // int vorbis_synthesis_pcmout(vorbis_dsp_state *v,float ***pcm)

    /**
     * pcm==NULL indicates we just want the pending samples, no more<br>
     * java: is_get_pcm = false equals pcm = NULL
     */
    public final PcmHelperStruct synthesis_pcmout(final boolean is_get_pcm) {
        //Info vi = v.vi;

        if (this.pcm_returned > -1 && this.pcm_returned < this.pcm_current) {
            if (is_get_pcm) {
                //for( int i = 0; i < vi.channels; i++ )
                this.pcm_data.pcmret = this.pcm_returned;//this.pcmret[i] = this.pcm_returned;

            }
            this.pcm_data.samples = (this.pcm_current - this.pcm_returned);
            return this.pcm_data;//return (this.pcm_current - this.pcm_returned);
        }
        this.pcm_data.samples = 0;
        return this.pcm_data;//return (0);
    }

    public final int synthesis_read(final int n) {
        if (n != 0 && this.pcm_returned + n > this.pcm_current) {
            return (Codec.OV_EINVAL);
        }
        this.pcm_returned += n;
        return (0);
    }

    // int vorbis_synthesis_lapout(vorbis_dsp_state *v, float ***pcm)

    /**
     * intended for use with a specific vorbisfile feature; we want access
     * to the [usually synthetic/postextrapolated] buffer and lapping at
     * the end of a decode cycle, specifically, a half-short-block worth.
     * This funtion works like pcmout above, except it will also expose
     * this implicit buffer data not normally decoded.
     */
    public final PcmHelperStruct synthesis_lapout(final boolean is_get_pcm) {

        if (this.pcm_returned < 0) {
            // return 0;
            this.pcm_data.samples = 0;
            return this.pcm_data;
        }

        final Info vinfo = this.vi;
        final CodecSetupInfo ci = vinfo.codec_setup;
        final int hs = ci.halfrate_flag + 1;

        final int n = ci.blocksizes[this.W] >> (hs/* + 1*/);
        final int n0 = ci.blocksizes[0] >> (hs/* + 1*/);
        final int n1 = ci.blocksizes[1] >> (hs/* + 1*/);

		/* our returned data ends at pcm_returned; because the synthesis pcm
		   buffer is a two-fragment ring, that means our data block may be
		   fragmented by buffering, wrapping or a short block not filling
		   out a buffer.  To simplify things, we unfragment if it's at all
		   possibly needed. Otherwise, we'd need to call lapout more than
		   once as well as hold additional dsp state.  Opt for
		   simplicity. */

        /* centerW was advanced by blockin; it would be the center of the
         *next* block */
        final float[][] pcm = this.pcm_data.pcm;// java
        if (this.centerW == n1) {
            /* the data buffer wraps; swap the halves */
            /* slow, sure, small */
            for (int j = 0, channels = vinfo.channels; j < channels; j++) {
                final float[] p = pcm[j];
                for (int i = 0, k = n1; i < n1; i++, k++) {
                    final float temp = p[i];
                    p[i] = p[k];
                    p[k] = temp;
                }
            }

            this.pcm_current -= n1;
            this.pcm_returned -= n1;
            this.centerW = 0;
        }

        /* solidify buffer into contiguous space */
        if ((this.lW ^ this.W) == 1) {
            /* long/short or short/long */
            int m = (n1 - n0) >>> 1;// java
            this.pcm_returned += m;
            this.pcm_current += m;
            final int h = ((n1 + n0) >>> 1) - 1;// java
            for (int j = 0, channels = vinfo.channels; j < channels; j++, m++) {
                final float[] s = pcm[j];
                for (int i = h, di = m; i >= 0; ) {
                    s[di--] = s[i--];
                }
            }
        } else {
            if (this.lW == 0) {
                /* short/short */
                int m = n1 - n0;// java
                this.pcm_returned += m;
                this.pcm_current += m;
                final int n0_1 = n0 - 1;
                for (int j = 0, channels = vinfo.channels; j < channels; j++, m++) {
                    final float[] s = pcm[j];
                    for (int i = n0_1, di = m; i >= 0; ) {
                        s[di--] = s[i--];
                    }
                }
            }
        }
        if (is_get_pcm) {
            //for( i = 0; i < vi.channels; i++ )
            this.pcm_data.pcmret = this.pcm_returned;//this.pcmret[i] = this.pcm_returned;
        }

        //return (n1 + n - this.pcm_returned);
        this.pcm_data.samples = (n1 + n - this.pcm_returned);
        return this.pcm_data;
    }

    final float[] window(final int w) {
        final int window = this.backend_state.window[w];

        if (window - 1 < 0) {
            return null;
        }
        return Window._window_get(window - this.vi.codec_setup.halfrate_flag);
    }

    // info.c
    public final int analysis_headerout(
            final Comment vc,
            final Packet op,
            final Packet op_comm,
            final Packet op_code) {
        int ret = Codec.OV_EIMPL;
        final Info vinfo = this.vi;
        final Buffer opb = new Buffer();
        PrivateState b = this.backend_state;

        if (b == null || vi.channels <= 0 || vi.channels > 256) {
            b = null;
            ret = Codec.OV_EFAULT;
        } else {

            /* first header packet **********************************************/

            opb.pack_writeinit();
            if (vinfo._pack_info(opb) == 0) {

                /* build the packet */
                if (b.header != null) {
                    b.header = null;
                }
                b.header = new byte[opb.pack_bytes()];
                System.arraycopy(opb.buffer, 0, b.header, 0, b.header.length);
                op.packet_base = b.header;
                op.packet = 0;
                op.bytes = opb.pack_bytes();
                op.b_o_s = true;
                op.e_o_s = false;
                op.granulepos = 0;
                op.packetno = 0;

                /* second header packet (comments) **********************************/

                opb.pack_reset();
                if (vc._pack_comment(opb) == 0) {

                    if (b.header1 != null) {
                        b.header1 = null;
                    }
                    b.header1 = new byte[opb.pack_bytes()];
                    System.arraycopy(opb.buffer, 0, b.header1, 0, b.header1.length);
                    op_comm.packet_base = b.header1;
                    op_comm.packet = 0;
                    op_comm.bytes = opb.pack_bytes();
                    op_comm.b_o_s = false;
                    op_comm.e_o_s = false;
                    op_comm.granulepos = 0;
                    op_comm.packetno = 1;

                    /* third header packet (modes/codebooks) ****************************/

                    opb.pack_reset();
                    if (vinfo._pack_books(opb) == 0) {

                        if (b.header2 != null) {
                            b.header2 = null;
                        }
                        b.header2 = new byte[opb.pack_bytes()];
                        System.arraycopy(opb.buffer, 0, b.header2, 0, b.header2.length);
                        op_code.packet_base = b.header2;
                        op_code.packet = 0;
                        op_code.bytes = opb.pack_bytes();
                        op_code.b_o_s = false;
                        op_code.e_o_s = false;
                        op_code.granulepos = 0;
                        op_code.packetno = 2;

                        opb.pack_writeclear();
                        return (0);
                    }
                }
            }
        }
//err_out:
        op.clear();
        op_comm.clear();
        op_code.clear();

        if (b != null) {
            if (vi.channels > 0) {
                opb.pack_writeclear();
            }
            b.header = null;
            b.header1 = null;
            b.header2 = null;
        }
        return (ret);
    }

    // bitrate.c
    public final boolean bitrate_flushpacket(final Packet op) {
        final BitrateManagerState bm = this.backend_state.bms;
        final Block vb = bm.vb;
        int choice = Info.PACKETBLOBS / 2;
        if (vb == null) {
            return false;
        }

        if (op != null) {
            final BlockInternal vbi = vb.m_internal;

            if (vb.bitrate_managed()) {
                choice = bm.choice;
            }

            final Buffer opb = vbi.packetblob[choice];// java
            op.packet_base = opb.pack_get_buffer();
            op.packet = 0;
            op.bytes = opb.pack_bytes();
            op.b_o_s = false;
            op.e_o_s = vb.eofflag;
            op.granulepos = vb.granulepos;
            op.packetno = vb.sequence; /* for sake of completeness */
        }

        bm.vb = null;
        return true;
    }
}
