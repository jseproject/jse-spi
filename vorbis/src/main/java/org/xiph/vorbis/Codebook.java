package org.xiph.vorbis;

class Codebook implements Comparator {
    /**
     * codebook dimensions (elements per vector)
     */
    int dim = 0;
    /**
     * codebook entries
     */
    int entries = 0;
    /**
     * populated codebook entries
     */
    int used_entries = 0;
    StaticCodebook c = null;

	/* for encode, the below are entry-ordered, fully populated<br>
	    for decode, the below are ordered by bitreversed codeword and only
	   used entries are populated */
    /**
     * list of dim*entries actual entry values
     */
    float[] valuelist = null;
    /**
     * list of bitstream codewords for each entry
     * c-version: ogg_uint32_t *codelist;
     * java to compare must use: long val = (((long)codelist[offset]) & 0xffffffffL);
     */
    int[] codelist;

    /* only used if sparseness collapsed */
    int[] dec_index = null;
    // FIXME is this a bug? char* dec_codelengths, may be int* ?
    int[] dec_codelengths = null;
    int[] dec_firsttable;// c-version: ogg_uint32_t *dec_firsttable;
    int dec_firsttablen = 0;
    int dec_maxlength = 0;

    /* The current encoder uses only centered, integer-only lattice books. */
    int quantvals = 0;
    int minval = 0;
    int delta = 0;

    // sharedbook.c

    /**
     * given a list of word lengths, generate a list of codewords.  Works
     * for length ordered or unordered, always assigns the lowest valued
     * codewords first.  Extended to handle unused entries (length 0)
     */
    private static int[] _make_words(final byte[] l, final int n, final int sparsecount) {
        int i, count = 0;
        final int[] marker = new int[33];// already zeroed, uint32
        final int[] r = new int[sparsecount != 0 ? sparsecount : n];

        for (i = 0; i < n; i++) {
            final int length = l[i];
            if (length > 0) {
                int entry = marker[length];// uint32

				/* when we claim a node for an entry, we also claim the nodes
				 below it (pruning off the imagined tree that may have dangled
				 from it) as well as blocking the use of any nodes directly
				 above for leaves */

                /* update ourself */
                if (length < 32 && (entry >>> length) != 0) {
                    /* error condition; the lengths must specify an overpopulated tree */
                    // r = null;
                    return (null);
                }
                r[count++] = entry;

				/* Look to see if the next shorter marker points to the node
				 above. if so, update it and repeat.  */
                {
                    for (int j = length; j > 0; j--) {

                        if ((marker[j] & 1) != 0) {
                            /* have to jump branches */
                            if (j == 1) {
                                marker[1]++;
                            } else {
                                marker[j] = marker[j - 1] << 1;
                            }
                            break; /* invariant says next upper marker would already
								  have been moved if it was on the same path */
                        }
                        marker[j]++;
                    }
                }

				/* prune the tree; the implicit invariant says all the longer
				 markers were dangling from our just-taken node.  Dangle them
				 from our *new* node. */
                for (int j = length + 1; j < 33; j++) {
                    if ((marker[j] >>> 1) == entry) {
                        entry = marker[j];
                        marker[j] = marker[j - 1] << 1;
                    } else {
                        break;
                    }
                }
            } else if (sparsecount == 0) {
                count++;
            }
        }

        /* any underpopulated tree must be rejected. */
		/* Single-entry codebooks are a retconned extension to the spec.
		   They have a single codeword '0' of length 1 that results in an
		   underpopulated tree.  Shield that case from the underformed tree check. */
        if (!(count == 1 && marker[2] == 2)) {
            for (i = 1; i < 33; i++) {
                if ((marker[i] & (0xffffffff >>> (32 - i))) != 0) {
                    // r = null;
                    return (null);
                }
            }
        }

		/* bitreverse the words because our bitwise packer/unpacker is LSb
		 endian */
        for (i = 0, count = 0; i < n; i++) {
            int temp = 0;// uint32
            for (int j = 0; j < l[i]; j++) {
                temp <<= 1;
                temp |= (r[count] >>> j) & 1;
            }

            if (sparsecount != 0) {
                if (l[i] != 0) {
                    r[count++] = temp;
                }
            } else {
                r[count++] = temp;
            }
        }

        return (r);
    }

    // codebook.c

    /**
     * returns the number of bits
     */
    final int encode(final int a, final Buffer b) {
        if (a < 0 || a >= this.c.entries) {
            return (0);
        }
        b.pack_write(this.codelist[a], this.c.lengthlist[a]);
        return (this.c.lengthlist[a]);
    }

    /**
     * the 'eliminate the decode tree' optimization actually requires the
     * codewords to be MSb first, not LSb.  This is an annoying inelegancy
     * (and one of the first places where carefully thought out design
     * turned out to be wrong; Vorbis II and future Ogg codecs should go
     * to an MSb bitpacker), but not actually the huge hit it appears to
     * be.  The first-stage decode table catches most words so that
     * bitreverse is not in the main execution path.
     */
    private static int bitreverse(int x) {// uint32
        x = ((x >>> 16) & 0x0000ffff) | ((x << 16) & 0xffff0000);
        x = ((x >>> 8) & 0x00ff00ff) | ((x << 8) & 0xff00ff00);
        x = ((x >>> 4) & 0x0f0f0f0f) | ((x << 4) & 0xf0f0f0f0);
        x = ((x >>> 2) & 0x33333333) | ((x << 2) & 0xcccccccc);
        return ((x >>> 1) & 0x55555555) | ((x << 1) & 0xaaaaaaaa);
    }

    final int decode_packed_entry_number(final Buffer b) {
        int read = this.dec_maxlength;
        int lo, hi;
        int lok = b.pack_look(this.dec_firsttablen);

        if (lok >= 0) {
            final int entry = this.dec_firsttable[lok];// FIXME is this correct? hidden convert uint32 to long
            if ((entry & 0x80000000) != 0) {
                lo = (entry >>> 15) & 0x7fff;
                hi = this.used_entries - (entry & 0x7fff);
            } else {
                b.pack_adv(this.dec_codelengths[entry - 1]);
                return (entry - 1);
            }
        } else {
            lo = 0;
            hi = this.used_entries;
        }

		/* Single entry codebooks use a firsttablen of 1 and a
		 dec_maxlength of 1.  If a single-entry codebook gets here (due to
		 failure to read one bit above), the next look attempt will also
		 fail and we'll correctly kick out instead of trying to walk the
		 underformed tree */

        lok = b.pack_look(read);

        while (lok < 0 && read > 1) {
            lok = b.pack_look(--read);
        }
        if (lok < 0) {
            return -1;
        }

        /* bisect search for the codeword in the ordered list */
        {
            final int testword = bitreverse(lok) + Integer.MIN_VALUE;// uint32 comparing

            final int[] cd = this.codelist;// java
            while (hi - lo > 1) {
                final int p = (hi - lo) >> 1;
                final int test = ((cd[lo + p] + Integer.MIN_VALUE) > testword) ? 1 : 0;
                lo += p & (test - 1);
                hi -= p & (-test);
            }

            if (this.dec_codelengths[lo] <= read) {
                b.pack_adv(this.dec_codelengths[lo]);
                return (lo);
            }
        }

        b.pack_adv(read);

        return (-1);
    }

    /**
     * Decode side is specced and easier, because we don't need to find
     * matches using different criteria; we simply read and map.  There are
     * two things we need to do 'depending':<p>
     * <p>
     * We may need to support interleave.  We don't really, but it's
     * convenient to do it here rather than rebuild the vector later.<p>
     * <p>
     * Cascades may be additive or multiplicitive; this is not inherent in
     * the codebook, but set in the code using the codebook.  Like
     * interleaving, it's easiest to do it here.<br>
     * addmul==0 -> declarative (set the value)<br>
     * addmul==1 -> additive<br>
     * addmul==2 -> multiplicitive<br>
     *
     * @return returns the [original, not compacted] entry number or -1 on eof
     */
    final int decode(final Buffer b) {
        if (this.used_entries > 0) {
            final int packed_entry = decode_packed_entry_number(b);
            if (packed_entry >= 0) {
                return (this.dec_index[packed_entry]);
            }
        }

        /* if there's no dec_index, the codebook unpacking isn't collapsed */
        return (-1);
    }

    // sharedbook.c
    final void clear() {
		/* static book is not cleared; we're likely called on the lookup and
		 the static codebook belongs to the info struct */
		/*
		if( b.valuelist != null ) b.valuelist = null;
		if( b.codelist != null ) b.codelist = null;

		if( b.dec_index != null ) b.dec_index = null;
		if( b.dec_codelengths != null ) b.dec_codelengths = null;
		if( b.dec_firsttable != null ) b.dec_firsttable = null;
		*/
        dim = 0;
        entries = 0;
        used_entries = 0;
        c = null;

        valuelist = null;
        codelist = null;

        dec_index = null;
        dec_codelengths = null;
        dec_firsttable = null;
        dec_firsttablen = 0;
        dec_maxlength = 0;

        quantvals = 0;
        minval = 0;
        delta = 0;
        // memset(b,0,sizeof(*b));
    }

    final int init_encode(final StaticCodebook s) {

        clear();// memset(c,0,sizeof(*c));
        this.c = s;
        this.entries = s.entries;
        this.used_entries = s.entries;
        this.dim = s.dim;
        this.codelist = _make_words(s.lengthlist, s.entries, 0);
        //this.valuelist=_book_unquantize(s,s.entries,NULL); FIXME why valuelist not initialized?
        this.quantvals = s._book_maptype1_quantvals();
        this.minval = (int) Math.rint(Codec._float32_unpack(s.q_min));
        this.delta = (int) Math.rint(Codec._float32_unpack(s.q_delta));

        return (0);
    }

    /**
     * array to sort
     */
    private int[] d;

    private final Comparator sort_comparator_set(final int[] a) {
        this.d = a;
        return this;
    }

    @Override
    public int compare(final int a, final int b) {
        final int[] data = this.d;// uint32 comparing
        final int d1 = data[a] + Integer.MIN_VALUE;
        final int d2 = data[b] + Integer.MIN_VALUE;
        if (d1 > d2) {
            return 1;
        }
        if (d1 < d2) {
            return -1;
        }
        return 0;
    }

    //private final Comparator sort32a = this;

    /**
     * decode codebook arrangement is more heavily optimized than encode
     */
    final int init_decode(final StaticCodebook s) {
        clear();

        /* count actually used entries and find max length */
        int n = 0;
        final int s_entries = s.entries;// java
        final byte[] lengthlist = s.lengthlist;// java
        for (int i = 0; i < s_entries; i++) {
            if (lengthlist[i] > 0) {
                n++;
            }
        }

        this.entries = s_entries;
        this.used_entries = n;
        this.dim = s.dim;

        if (n > 0) {
			/* two different remappings go on here.

			First, we collapse the likely sparse codebook down only to
			actually represented values/words.  This collapsing needs to be
			indexed as map-valueless books are used to encode original entry
			positions as integers.

			Second, we reorder all vectors, including the entry index above,
			by sorted bitreversed codeword to allow treeless decode. */

            /* perform sort */
            int[] codes = _make_words(lengthlist, s_entries, this.used_entries);

            if (codes == null) {// goto err_out;
                clear();
                return (-1);
            }

            final int[] codep = new int[n];

            for (int i = 0; i < n; i++) {
                codes[i] = bitreverse(codes[i]);
                codep[i] = i;
            }

            FastQSortAlgorithm.sort(codep, 0, n, sort_comparator_set(codes));

            final int[] sortindex = new int[n];
            final int[] code_list = new int[n];// java
            this.codelist = code_list;
            /* the index is a reverse index */
            for (int i = 0; i < n; i++) {
                sortindex[codep[i]] = i;
            }

            for (int i = 0; i < n; i++) {
                code_list[sortindex[i]] = codes[i];
            }
            codes = null;

            this.valuelist = s._book_unquantize(n, sortindex);
            this.dec_index = new int[n];

            int i;
            for (n = 0, i = 0; i < s_entries; i++) {
                if (lengthlist[i] > 0) {
                    this.dec_index[sortindex[n++]] = i;
                }
            }

            final int[] dec_code_lengths = new int[n];// java
            this.dec_codelengths = dec_code_lengths;
            this.dec_maxlength = 0;
            for (n = 0, i = 0; i < s_entries; i++) {
                if (lengthlist[i] > 0) {
                    dec_code_lengths[sortindex[n++]] = lengthlist[i];
                    if (s.lengthlist[i] > this.dec_maxlength) {
                        this.dec_maxlength = s.lengthlist[i];
                    }
                }
            }

            if (n == 1 && this.dec_maxlength == 1) {
				/* special case the 'single entry codebook' with a single bit
				 fastpath table (that always returns entry 0 )in order to use
				 unmodified decode paths. */
                this.dec_firsttablen = 1;
                this.dec_firsttable = new int[2];
                this.dec_firsttable[0] = this.dec_firsttable[1] = 1;

            } else {
                this.dec_firsttablen = Codec.ilog(this.used_entries) - 4; /* this is magic */
                if (this.dec_firsttablen < 5) {
                    this.dec_firsttablen = 5;
                }
                if (this.dec_firsttablen > 8) {
                    this.dec_firsttablen = 8;
                }

                final int tabn = 1 << this.dec_firsttablen;
                final int[] dec_first_table = new int[tabn];// java
                this.dec_firsttable = dec_first_table;

                for (i = 0; i < n; i++) {
                    final int length = dec_code_lengths[i];// java
                    if (length <= this.dec_firsttablen) {
                        final int orig = bitreverse(code_list[i]);
                        for (int j = 0, je = (1 << (this.dec_firsttablen - length)); j < je; j++) {
                            dec_first_table[orig | (j << length)] = i + 1;
                        }
                    }
                }

				/* now fill in 'unused' entries in the firsttable with hi/lo search
				hints for the non-direct-hits */
                {
                    // correct variant:
                    // long mask = (0xfffffffe << (31 - this.m_dec_firsttablen)) & 0xffffffffL;
                    // but mask uses only to mask low 32 bits, so:
                    final int mask = 0xfffffffe << (31 - this.dec_firsttablen);// uint32
                    int lo = 0, hi = 0;

                    for (i = 0; i < tabn; i++) {
                        final int word = i << (32 - this.dec_firsttablen);// uint32
                        final int bit_rev = bitreverse(word);// java
                        if (dec_first_table[bit_rev] == 0) {
                            final int uint_word = word + Integer.MIN_VALUE;// java word copy for uint32 comparing
                            while ((lo + 1) < n && (code_list[lo + 1] + Integer.MIN_VALUE) <= uint_word) {
                                lo++;
                            }
                            while (hi < n && uint_word >= ((code_list[hi] & mask) + Integer.MIN_VALUE)) {
                                hi++;
                            }

							/* we only actually have 15 bits per hint to play with here.
							In order to overflow gracefully (nothing breaks, efficiency
							just drops), encode as the difference from the extremes. */
                            {
                                int loval = lo;// uint32
                                int hival = n - hi;// uint32

                                if (loval > 0x7fff) {
                                    loval = 0x7fff;
                                }
                                if (hival > 0x7fff) {
                                    hival = 0x7fff;
                                }
                                dec_first_table[bit_rev] = 0x80000000 | (loval << 15) | hival;
                            }
                        }
                    }
                }
            }
        }

        return (0);
//err_out:
//		clear( c );
//		return (-1);
    }

    // FIXME never used private final int codeword(int entry) {
    //	if( this.c != null ) /* only use with encode; decode optimizations are allowed to break this */
    //		return this.m_codelist[entry];// FIXME is this a bug? using hidden convert from uint32 to long
    //	return -1;
    //}

    // FIXME never used private final int codelen(int entry) {
    //	if( this.c != null ) /* only use with encode; decode optimizations are allowed to break this */
    //		return this.c.lengthlist[entry];
    //	return -1;
    //}

    /**
     * unlike the others, we guard against n not being an integer number
     * of <dim> internally rather than in the upper layer (called only by
     * floor0)
     */
    final int decodev_set(final float[] a, final Buffer b, final int n) {
        if (this.used_entries > 0) {
            final int size = this.dim;// java
            final float list[] = this.valuelist;// java
            for (int i = 0; i < n; ) {
                int entry = decode_packed_entry_number(b);
                if (entry == -1) {
                    return (-1);
                }
                entry *= size;
                final int end = entry + size;
                while (i < n && entry < end) {
                    a[i++] = list[entry++];
                }
            }
        } else {
            for (int i = 0; i < n; ) {
                a[i++] = 0.f;
            }
        }
        return (0);
    }

    final int decodevv_add(final float[][] a, final int offset, final int ch,
                           final Buffer b, int n) {
        int chptr = 0;
        if (this.used_entries > 0) {
            n += offset;
            n /= ch;
            final int size = this.dim;// java
            final float list[] = this.valuelist;// java
            for (int i = offset / ch; i < n; ) {
                int entry = decode_packed_entry_number(b);
                if (entry == -1) {
                    return (-1);
                }
                {
                    entry *= size;
                    final int end = entry + size;
                    while (i < n && entry < end) {
                        a[chptr++][i] += list[entry++];
                        if (chptr == ch) {
                            chptr = 0;
                            i++;
                        }
                    }
                }
            }
        }
        return (0);
    }
}
