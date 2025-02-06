package org.xiph.vorbis;

/**
 * This structure encapsulates huffman and VQ style encoding books; it
 * doesn't do anything specific to either.
 * <p>
 * valuelist/quantlist are nonNULL (and q_* significant) only if
 * there's entry->value mapping to be done.
 * <p>
 * If encode-side mapping must be done (and thus the entry needs to be
 * hunted), the auxiliary encode pointer will point to a decision
 * tree.  This is true of both VQ and huffman, but is mostly useful
 * with VQ.
 */
public class StaticCodebook {
    /**
     * codebook dimensions (elements per vector)
     */
    int dim = 0;
    /**
     * codebook entries
     */
    int entries = 0;
    /**
     * codeword lengths in bits
     */
    byte[] lengthlist = null;

    /**
     * mapping<br>
     * 0 = none<br>
     * 1 = implicitly populated values from map column<br>
     * 2 = listed arbitrary values
     */
    int maptype = 0;

    /* The below does a linear, single monotonic sequence mapping. */
    /**
     * packed 32 bit float; quant value 0 maps to minval
     */
    int q_min = 0;
    /**
     * packed 32 bit float; val 1 - val 0 == delta
     */
    int q_delta = 0;
    /**
     * bits: 0 < quant <= 16
     */
    private int q_quant = 0;
    /**
     * bitflag
     */
    private int q_sequencep = 0;
    /**
     * map == 1: (int)(entries^(1/dim)) element column map<br>
     * map == 2: list of dim*entries quantized entry vals
     */
    private int[] quantlist = null;
    private int allocedp = 0;

    //
    StaticCodebook() {
    }

    public StaticCodebook(
            final int i_dim,
            final int i_entries,
            final byte[] pi_lengthlist,

            final int i_maptype,

            final int i_q_min,
            final int i_q_delta,
            final int i_q_quant,
            final int i_q_sequencep,

            final int[] pi_quantlist,
            final int i_allocedp) {
        dim = i_dim;
        entries = i_entries;
        lengthlist = pi_lengthlist;
        maptype = i_maptype;
        q_min = i_q_min;
        q_delta = i_q_delta;
        q_quant = i_q_quant;
        q_sequencep = i_q_sequencep;
        quantlist = pi_quantlist;
        allocedp = i_allocedp;
    }

    private final void m_clear() {
        dim = 0;
        entries = 0;
        lengthlist = null;
        maptype = 0;
        q_min = 0;
        q_delta = 0;
        q_quant = 0;
        q_sequencep = 0;
        quantlist = null;
        allocedp = 0;
    }

    // sharedbook.c

    /**
     * there might be a straightforward one-line way to do the below
     * that's portable and totally safe against roundoff, but I haven't
     * thought of it.  Therefore, we opt on the side of caution
     */
    final int _book_maptype1_quantvals() {
        final int e = this.entries;// java
        if (e < 1) {
            return 0;
        }
        final int d = this.dim;// java
        int vals = (int) Math.floor(Math.pow((double) e, 1. / d));

		/* the above *should* be reliable, but we'll not assume that FP is
		  ever reliable when bitstream sync is at stake; verify via integer
		  means that vals really is the greatest value of dim for which
		  vals^b->bim <= b->entries */
        /* treat the above as an initial guess */
        if (vals < 1) {
            vals = 1;
        }
        while (true) {
            int acc = 1;
            int acc1 = 1;
            int i;
            for (i = 0; i < d; i++) {
                if (e / vals < acc) {
                    break;
                }
                acc *= vals;
                if (Integer.MAX_VALUE / (vals + 1) < acc1) {
                    acc1 = Integer.MAX_VALUE;
                } else {
                    acc1 *= vals + 1;
                }
            }
            if (i >= d && acc <= e && acc1 > e) {
                return (vals);
            } else {
                if (i < d || acc > e) {
                    vals--;
                } else {
                    vals++;
                }
            }
        }
    }

    /**
     * unpack the quantized list of values for encode/decode<p>
     * we need to deal with two map types: in map type 1, the values are
     * generated algorithmically (each column of the vector counts through
     * the values in the quant vector). in map type 2, all the values came
     * in in an explicit list.  Both value lists must be unpacked
     */
    final float[] _book_unquantize(final int n, final int[] sparsemap) {
        if (this.maptype == 1 || this.maptype == 2) {
            final float mindel = Codec._float32_unpack(this.q_min);
            final float delta = Codec._float32_unpack(this.q_delta);
            final int dimension = this.dim;// java
            final float[] r = new float[n * dimension];

            int count = 0;
            final boolean is_qsequencep = this.q_sequencep != 0;
			/* maptype 1 and 2 both use a quantized value vector, but
			different sizes */
            final int[] quant_list = this.quantlist;// java
            final byte[] length_list = this.lengthlist;// java
            switch (this.maptype) {
                case 1:
				/* most of the time, entries%dimensions == 0, but we need to be
				well defined.  We define that the possible vales at each
				scalar is values == entries/dim.  If entries%dim != 0, we'll
				have 'too few' values (values*dim<entries), which means that
				we'll have 'left over' entries; left over entries use zeroed
				values (and are wasted).  So don't generate codebooks like
				that */
                    final int quantvals = _book_maptype1_quantvals();
                    for (int j = 0, je = this.entries; j < je; j++) {
                        if ((sparsemap != null && length_list[j] != 0) || sparsemap == null) {
                            float last = 0.f;
                            int indexdiv = 1;
                            // java: changed
                            int off = sparsemap != null ?
                                    sparsemap[count]
                                    :
                                    count;
                            off *= dimension;
                            for (int k = 0; k < dimension; k++) {
                                final int index = (j / indexdiv) % quantvals;
                                float val = quant_list[index];
                                val = (val <= 0.0F) ? 0.0F - val : val;
                                val *= delta;
                                val += mindel;
                                val += last;
                                if (is_qsequencep) {
                                    last = val;
                                }
                                r[off++] = val;
                                indexdiv *= quantvals;
                            }
                            count++;
                        }
                    }
                    break;
                case 2:// FIXME test never uses maptype 2
                    for (int j = 0, je = this.entries; j < je; j++) {
                        if ((sparsemap != null && length_list[j] != 0) || sparsemap == null) {
                            float last = 0.f;
                            // java: changed
                            int off = sparsemap != null ?
                                    sparsemap[count]
                                    :
                                    count;
                            off *= dimension;
                            for (int k = j * dimension, ke = k + dimension; k < ke; k++) {
                                float val = quant_list[k];
                                val = (val <= 0.0F) ? 0.0F - val : val;
                                val *= delta;
                                val += mindel;
                                val += last;
                                if (is_qsequencep) {
                                    last = val;
                                }
                                r[off++] = val;
                            }
                            count++;
                        }
                    }
                    break;
            }

            return (r);
        }
        return (null);
    }

    final void destroy() {
        if (allocedp != 0) {
            quantlist = null;
            lengthlist = null;
            m_clear();
        } /* otherwise, it is in static memory */
    }

    // codebook.c

    /**
     * packs the given codebook into the bitstream
     */
    final int pack(final Buffer opb) {
        int i, j;
        boolean ordered = false;

        /* first the basic parameters */
        opb.pack_write(0x564342, 24);
        opb.pack_write(this.dim, 16);
        final int entries_count = this.entries;// java
        opb.pack_write(entries_count, 24);

		/* pack the codewords.  There are two packings; length ordered and
		 length random.  Decide between the two now. */
        final byte[] length_list = this.lengthlist;// java
        for (i = 1; i < entries_count; i++) {
            if (length_list[i - 1] == 0 || length_list[i] < length_list[i - 1]) {
                break;
            }
        }
        if (i == entries_count) {
            ordered = true;
        }

        if (ordered) {
			/* length ordered.  We only need to say how many codewords of
			   each length.  The actual codewords are generated
			   deterministically */

            int count = 0;
            opb.pack_write(1, 1);  /* ordered */
            opb.pack_write((int) length_list[0] - 1, 5); /* 1 to 32 */

            for (i = 1; i < entries_count; i++) {
                final int ithis = (int) length_list[i];// this renamed to ihis
                final int last = (int) length_list[i - 1];
                if (ithis > last) {
                    for (j = last; j < ithis; j++) {
                        opb.pack_write(i - count, Codec.ilog(entries_count - count));
                        count = i;
                    }
                }
            }
            opb.pack_write(i - count, Codec.ilog(entries_count - count));

        } else {
			/* length random.  Again, we don't code the codeword itself, just
			   the length.  This time, though, we have to encode each length */
            opb.pack_write(0, 1);   /* unordered */

			/* algortihmic mapping has use for 'unused entries', which we tag
			   here.  The algorithmic mapping happens as usual, but the unused
			   entry has no codeword. */
            for (i = 0; i < entries_count; i++) {
                if (length_list[i] == 0) {
                    break;
                }
            }

            if (i == entries_count) {
                opb.pack_write(0, 1); /* no unused entries */
                for (i = 0; i < entries_count; i++) {
                    opb.pack_write(length_list[i] - 1, 5);
                }
            } else {
                opb.pack_write(1, 1); /* we have unused entries; thus we tag */
                for (i = 0; i < entries_count; i++) {
                    if (length_list[i] == 0) {
                        opb.pack_write(0, 1);
                    } else {
                        opb.pack_write(1, 1);
                        opb.pack_write(length_list[i] - 1, 5);
                    }
                }
            }
        }

		/* is the entry number the desired return value, or do we have a
		 mapping? If we have a mapping, what type? */
        opb.pack_write(this.maptype, 4);
        switch (this.maptype) {
            case 0:
                /* no mapping */
                break;
            case 1:
            case 2:
                /* implicitly populated value mapping */
                /* explicitly populated value mapping */

                if (this.quantlist == null) {
                    /* no quantlist?  error */
                    return (-1);
                }

                /* values that define the dequantization */
                opb.pack_write(this.q_min, 32);
                opb.pack_write(this.q_delta, 32);
                opb.pack_write(this.q_quant - 1, 4);
                opb.pack_write(this.q_sequencep, 1);

            {
                int quantvals;
                switch (this.maptype) {
                    case 1:
					/* a single column of (c->entries.->dim) quantized values for
					   building a full value list algorithmically (square lattice) */
                        quantvals = _book_maptype1_quantvals();
                        break;
                    case 2:
                        /* every value (c->entries*c->dim total) specified explicitly */
                        quantvals = entries_count * this.dim;
                        break;
                    default: /* NOT_REACHABLE */
                        quantvals = -1;
                }

                /* quantized values */
                final int[] quant_list = this.quantlist;// java
                for (i = 0; i < quantvals; i++) {
                    int val = quant_list[i];
                    if (val < 0) {
                        val = -val;
                    }
                    opb.pack_write(val, this.q_quant);
                }

            }
            break;
            default:
                /* error case; we don't have any other map types now */
                return (-1);
        }

        return (0);
    }

    /**
     * unpacks a codebook from the packet buffer into the codebook struct,
     * readies the codebook auxiliary structures for decode
     */
    static final StaticCodebook unpack(final Buffer opb) {
        final StaticCodebook s = new StaticCodebook();
        s.allocedp = 1;

        /* make sure alignment is correct */
        if (opb.pack_read(24) != 0x564342) {// goto _eofout;
            s.destroy();
            return (null);
        }

        /* first the basic parameters */
        s.dim = opb.pack_read(16);
        final int entries = opb.pack_read(24);
        s.entries = entries;
        if (entries == -1) {// goto _eofout;
            s.destroy();
            return (null);
        }

        if (Codec.ilog(s.dim) + Codec.ilog(entries) > 24) {//goto _eofout;
            s.destroy();
            return (null);
        }

        /* codeword ordering.... length ordered or unordered? */
        switch (opb.pack_read(1)) {
            case 0: {
                int unused;
                /* allocated but unused entries? */
                unused = opb.pack_read(1);
                if ((entries * (unused != 0 ? 1 : 5) + 7) >> 3
                        >
                        opb.storage - opb.pack_bytes()) {// goto _eofout;
                    s.destroy();
                    return (null);
                }
                /* unordered */
                final byte[] lengthlist = new byte[entries];
                s.lengthlist = lengthlist;

                /* allocated but unused entries? */
                if (unused != 0) {
                    /* yes, unused entries */

                    for (int i = 0; i < entries; i++) {
                        if (opb.pack_read(1) != 0) {
                            final int num = opb.pack_read(5);
                            if (num == -1) {// goto _eofout;
                                s.destroy();
                                return (null);
                            }
                            lengthlist[i] = (byte) (num + 1);
                        } else {
                            lengthlist[i] = 0;
                        }
                    }
                } else {
                    /* all entries used; no tagging */
                    for (int i = 0; i < entries; i++) {
                        final int num = opb.pack_read(5);
                        if (num == -1) {// goto _eofout;
                            s.destroy();
                            return (null);
                        }
                        lengthlist[i] = (byte) (num + 1);
                    }
                }

                break;
            }
            case 1:
                /* ordered */
            {
                int length = opb.pack_read(5) + 1;
                if (length == 0) {// goto _eofout;
                    s.destroy();
                    return (null);
                }
                final byte[] lengthlist = new byte[entries];
                s.lengthlist = lengthlist;

                for (int i = 0; i < entries; ) {
                    final int num = opb.pack_read(Codec.ilog(entries - i));
                    if (num == -1) {// goto _eofout;
                        s.destroy();
                        return (null);
                    }
                    if (length > 32 || num > entries - i ||
                            (num > 0 && (num - 1) >> (length - 1) > 1)) {
                        s.destroy();
                        return (null);//goto _errout;
                    }
                    if (length > 32) {// goto _errout;
                        s.destroy();
                        return (null);
                    }
                    for (int j = 0; j < num; j++, i++) {
                        lengthlist[i] = (byte) length;
                    }
                    length++;
                }
            }
            break;
            default:
                /* EOF */
                // goto _eofout;
                s.destroy();
                return (null);
        }

        /* Do we have a mapping to unpack? */
        switch ((s.maptype = opb.pack_read(4))) {
            case 0:
                /* no mapping */
                break;
            case 1:
            case 2:
                /* implicitly populated value mapping */
                /* explicitly populated value mapping */

                s.q_min = opb.pack_read(32);
                s.q_delta = opb.pack_read(32);
                final int q_quant = opb.pack_read(4) + 1;
                s.q_quant = q_quant;
                s.q_sequencep = opb.pack_read(1);
                if (s.q_sequencep == -1) {// goto _eofout;
                    s.destroy();
                    return (null);
                }

            {
                int quantvals = 0;
                switch (s.maptype) {
                    case 1:
                        quantvals = (s.dim == 0 ? 0 : s._book_maptype1_quantvals());
                        break;
                    case 2:
                        quantvals = entries * s.dim;
                        break;
                }

                /* quantized values */
                if (((quantvals * q_quant + 7) >> 3) > opb.storage - opb.pack_bytes()) {
                    s.destroy();
                    return (null);// goto _eofout;
                }
                final int[] quantlist = new int[quantvals];
                s.quantlist = quantlist;
                for (int i = 0; i < quantvals; i++) {
                    quantlist[i] = opb.pack_read(q_quant);
                }

                if (quantvals != 0 && quantlist[quantvals - 1] == -1) {//goto _eofout;
                    s.destroy();
                    return (null);
                }
            }
            break;
            default:
                //goto _errout;
                s.destroy();
                return (null);
        }

        /* all set */
        return (s);

//_errout:
//_eofout:
//		destroy( s );
//		return (null);
    }
}
