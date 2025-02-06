package org.xiph.vorbis;

import java.util.Arrays;

/**
 * floor backend 1 implementation<br>
 * export hooks<p>
 * <pre>
 * const vorbis_func_floor floor1_exportbundle = {
 * &floor1_pack, &floor1_unpack,
 * &floor1_look,
 * &floor1_free_info, &floor1_free_look,
 * &floor1_inverse1, &floor1_inverse2
 * };
 * </pre>
 */
class Floor1 extends FuncFloor implements Comparator {
    //private static final boolean TRAIN_FLOOR1 = false;

    /**
     * floor 1 fixed at -140dB to 0dB range
     */
    //private static final int floor1_rangedB = 140;

    private static class lsfit_acc {
        private int x0 = 0;
        private int x1 = 0;

        private int xa = 0;
        private int ya = 0;
        private int x2a = 0;
        //private int y2a = 0;// FIXME never used y2a and y2b
        private int xya = 0;
        private int an = 0;

        private int xb = 0;
        private int yb = 0;
        private int x2b = 0;
        //private int y2b = 0;
        private int xyb = 0;
        private int bn = 0;

        //
        private final void clear() {
            x0 = 0;
            x1 = 0;

            xa = 0;
            ya = 0;
            x2a = 0;
            //y2a = 0;
            xya = 0;
            an = 0;

            xb = 0;
            yb = 0;
            x2b = 0;
            //y2b = 0;
            xyb = 0;
            bn = 0;
        }
    }

    //
    Floor1() {
        super(true);
    }

    /***********************************************/
    // change call floor1_free_info to InfoFloor1 = null;
	/*static void floor1_free_info(vorbis_info_floor *i)
		InfoFloor1 info = (vorbis_info_floor1)i;
		if( info != null ) {
			memset(info,0,sizeof(*info));
			_ogg_free(info);
		}
	}*/

    // change call floor1_free_look to vorbis_look_floor1 = null;
	/*static void floor1_free_look(vorbis_look_floor *i){
		vorbis_look_floor1 *look=(vorbis_look_floor1 *)i;
		if(look){
		//fprintf(stderr,"floor 1 bit usage %f:%f (%f total)\n",
		//		(float)look->phrasebits/look->frames,
		//		(float)look->postbits/look->frames,
		//		(float)(look->postbits+look->phrasebits)/look->frames);

		memset(look,0,sizeof(*look));
		_ogg_free(look);
		}
	}*/
    @Override
    // static void floor1_pack(vorbis_info_floor *i,oggpack_buffer *opb)
    final void pack(final InfoFloor i, final Buffer opb) {
        final InfoFloor1 info = (InfoFloor1) i;
        final int[] postlist = info.postlist;// java
        final int maxposit = postlist[1];
        int maxclass = -1;

        /* save out partitions */
        final int partitions = info.partitions;// java
        opb.pack_write(partitions, 5); /* only 0 to 31 legal */
        final int[] partitionclass = info.partitionclass;// java
        for (int j = 0; j < partitions; j++) {
            final int k = partitionclass[j];
            opb.pack_write(k, 4); /* only 0 to 15 legal */
            if (maxclass < k) {
                maxclass = k;
            }
        }

        /* save out partition classes */
        final int[] class_dim = info.class_dim;// java
        final int[] class_subs = info.class_subs;// java
        final int[] class_book = info.class_book;// java
        final int[][] class_subbook = info.class_subbook;// java
        for (int j = 0; j <= maxclass; j++) {
            opb.pack_write(class_dim[j] - 1, 3); /* 1 to 8 */
            int count = class_subs[j];
            opb.pack_write(count, 2); /* 0 to 3 */
            if (count != 0) {
                opb.pack_write(class_book[j], 8);
            }
            count = 1 << count;
            final int[] class_subbook_j = class_subbook[j];// java
            for (int k = 0; k < count; k++) {
                opb.pack_write(class_subbook_j[k] + 1, 8);
            }
        }

        /* save out the post list */
        opb.pack_write(info.mult - 1, 2);     /* only 1,2,3,4 legal now */
		/* maxposit cannot legally be less than 1; this is encode-side, we
		 can assume our setup is OK */
        opb.pack_write(Codec.ilog(maxposit - 1), 4);
        final int rangebits = Codec.ilog(maxposit - 1);

        int count = 2;
        for (int j = 0, k = 2; j < partitions; j++) {
            count += class_dim[partitionclass[j]];
            for (; k < count; k++) {
                opb.pack_write(postlist[k], rangebits);
            }
        }
    }

    /**
     * array to sort
     */
    private int[] mData;

    private Comparator sort_comparator_set(final int[] dim) {
        mData = dim;
        return this;
    }

    @Override
    public final int compare(final int a, final int b) {
        final int[] data = mData;
        return data[a] - data[b];
    }

    //private final Comparator icomp = this;

    @Override
    // vorbis_info_floor *floor1_unpack(vorbis_info *vi,oggpack_buffer *opb){
    final InfoFloor unpack(final Info vi, final Buffer opb) {

        int maxclass = -1;

        final InfoFloor1 info = new InfoFloor1();
        /* read partitions */
        final int partitions = opb.pack_read(5); /* only 0 to 31 legal */
        info.partitions = partitions;
        final int[] partitionclass = info.partitionclass;// java
        for (int j = 0; j < partitions; j++) {
            final int k = opb.pack_read(4); /* only 0 to 15 legal */
            partitionclass[j] = k;
            if (k < 0) {// goto err_out;
                //info = null;// floor1_free_info( info );
                return (null);
            }
            if (maxclass < k) {
                maxclass = k;
            }
        }

        /* read partition classes */
        final CodecSetupInfo ci = vi.codec_setup;
        final int[] class_dim = info.class_dim;// java
        final int[] class_subs = info.class_subs;// java
        final int[] class_book = info.class_book;// java
        final int[][] class_subbook = info.class_subbook;// java
        for (int j = 0; j <= maxclass; j++) {
            class_dim[j] = opb.pack_read(3) + 1; /* 1 to 8 */
            int count = opb.pack_read(2); /* 0,1,2,3 bits */
            class_subs[j] = count;
            if (count < 0) {// goto err_out;
                //info = null;// floor1_free_info( info );
                return (null);
            }
            if (count != 0) {
                class_book[j] = opb.pack_read(8);
            }
            if (class_book[j] < 0 || class_book[j] >= ci.books) {// goto err_out;
                //info = null;// floor1_free_info( info );
                return (null);
            }
            count = 1 << count;
            final int sb_j[] = class_subbook[j];
            for (int k = 0; k < count; k++) {
                sb_j[k] = opb.pack_read(8) - 1;
                if (sb_j[k] < -1 || sb_j[k] >= ci.books) {// goto err_out;
                    //info = null;// floor1_free_info( info );
                    return (null);
                }
            }
        }

        /* read the post list */
        info.mult = opb.pack_read(2) + 1;     /* only 1,2,3,4 legal now */
        final int rangebits = opb.pack_read(4);
        if (rangebits < 0) {// goto err_out;
            //info = null;// floor1_free_info( info );
            return (null);
        }

        int count = 2;
        final int[] postlist = info.postlist;// java
        for (int j = 0, k = 2; j < partitions; j++) {
            count += class_dim[partitionclass[j]];
            if (count > InfoFloor.VIF_POSIT + 2) {// goto err_out;
                //info = null;// floor1_free_info( info );
                return (null);
            }
            for (; k < count; k++) {
                final int t = postlist[k] = opb.pack_read(rangebits);
                if (t < 0 || t >= (1 << rangebits)) {// goto err_out;
                    //info = null;// floor1_free_info( info );
                    return (null);
                }
            }
        }
        postlist[0] = 0;
        postlist[1] = 1 << rangebits;

		/* don't allow repeated values in post list as they'd result in
		   zero-length segments */
        {
            final int[] sortpointer = new int[count];
            for (int j = 0; j < count; j++) {
                sortpointer[j] = j;
            }
            FastQSortAlgorithm.sort(sortpointer, 0, sortpointer.length, sort_comparator_set(postlist));

            for (int j = 1; j < count; j++) {
                if (postlist[sortpointer[j - 1]] == postlist[sortpointer[j]]) {// goto err_out;
                    //info = null;// floor1_free_info( info );
                    return (null);
                }
            }
        }

        return (info);
/*
err_out:
		floor1_free_info( info );
		return (null);
*/
    }

    @Override
    // static vorbis_look_floor *floor1_look(vorbis_dsp_state *vd, vorbis_info_floor *in){
    final LookFloor look(final DspState vd, final InfoFloor in) {

        final InfoFloor1 info = (InfoFloor1) in;
        final LookFloor1 look = new LookFloor1();
        int n = 0;

        // (void)vd;

        look.vi = info;
        final int[] postlist = info.postlist;// java
        look.n = postlist[1];

		/* we drop each position value in-between already decoded values,
		 and use linear interpolation to predict each new value past the
		 edges.  The positions are read in the order of the position
		 list... we precompute the bounding positions in the lookup.  Of
		 course, the neighbors can change (if a position is declined), but
		 this is an initial mapping */
        final int[] partitionclass = info.partitionclass;// java
        int[] tmp = info.class_dim;// java
        for (int i = 0, ie = info.partitions; i < ie; i++) {
            n += tmp[partitionclass[i]];
        }
        n += 2;
        look.posts = n;

        final int[] sortpointer = new int[n];
        /* also store a sorted position index */
        for (int i = 0; i < n; i++) {
            sortpointer[i] = i;
        }
        FastQSortAlgorithm.sort(sortpointer, 0, n, sort_comparator_set(postlist));

        /* points from sort order back to range number */
        final int[] forward_index = look.forward_index;// java
        for (int i = 0; i < n; i++) {
            forward_index[i] = sortpointer[i];
        }
        /* points from range order to sorted position */
        tmp = look.reverse_index;// java
        for (int i = 0; i < n; i++) {
            tmp[forward_index[i]] = i;
        }
        /* we actually need the post values too */
        tmp = look.sorted_index;// java
        for (int i = 0; i < n; i++) {
            tmp[i] = postlist[forward_index[i]];
        }

        /* quantize values to multiplier spec */
        switch (info.mult) {
            case 1: /* 1024 -> 256 */
                look.quant_q = 256;
                break;
            case 2: /* 1024 -> 128 */
                look.quant_q = 128;
                break;
            case 3: /* 1024 -> 86 */
                look.quant_q = 86;
                break;
            case 4: /* 1024 -> 64 */
                look.quant_q = 64;
                break;
        }

		/* discover our neighbors for decode where we don't use fit flags
		(that would push the neighbors outward) */
        final int[] loneighbor = look.loneighbor;// java
        final int[] hineighbor = look.hineighbor;// java
        for (int i = 2; i < n; i++) {
            int lo = 0;
            int hi = 1;
            int lx = 0;
            int hx = look.n;
            final int currentx = postlist[i];
            for (int j = 0; j < i; j++) {
                final int x = postlist[j];
                if (x > lx && x < currentx) {
                    lo = j;
                    lx = x;
                }
                if (x < hx && x > currentx) {
                    hi = j;
                    hx = x;
                }
            }
            i -= 2;
            loneighbor[i] = lo;
            hineighbor[i] = hi;
            i += 2;
        }

        return (look);
    }

    private static int render_point(final int x0, final int x1, int y0, int y1, final int x) {
        y0 &= 0x7fff; /* mask off flag */
        y1 &= 0x7fff;

        {
            final int dy = y1 - y0;
            final int adx = x1 - x0;
            final int ady = (dy < 0) ? -dy : dy;
            final int err = ady * (x - x0);

            final int off = err / adx;
            if (dy < 0) {
                return (y0 - off);
            }
            return (y0 + off);
        }
    }

    private static int dBquant(final float x) {
        final int i = (int) (x * 7.3142857f + 1023.5f);
        if (i > 1023) {
            return (1023);
        }
        if (i < 0) {
            return (0);
        }
        return i;
    }

    private static final float FLOOR1_fromdB_LOOKUP[] = {// [256]
            1.0649863e-07F, 1.1341951e-07F, 1.2079015e-07F, 1.2863978e-07F,
            1.3699951e-07F, 1.4590251e-07F, 1.5538408e-07F, 1.6548181e-07F,
            1.7623575e-07F, 1.8768855e-07F, 1.9988561e-07F, 2.128753e-07F,
            2.2670913e-07F, 2.4144197e-07F, 2.5713223e-07F, 2.7384213e-07F,
            2.9163793e-07F, 3.1059021e-07F, 3.3077411e-07F, 3.5226968e-07F,
            3.7516214e-07F, 3.9954229e-07F, 4.2550680e-07F, 4.5315863e-07F,
            4.8260743e-07F, 5.1396998e-07F, 5.4737065e-07F, 5.8294187e-07F,
            6.2082472e-07F, 6.6116941e-07F, 7.0413592e-07F, 7.4989464e-07F,
            7.9862701e-07F, 8.5052630e-07F, 9.0579828e-07F, 9.6466216e-07F,
            1.0273513e-06F, 1.0941144e-06F, 1.1652161e-06F, 1.2409384e-06F,
            1.3215816e-06F, 1.4074654e-06F, 1.4989305e-06F, 1.5963394e-06F,
            1.7000785e-06F, 1.8105592e-06F, 1.9282195e-06F, 2.0535261e-06F,
            2.1869758e-06F, 2.3290978e-06F, 2.4804557e-06F, 2.6416497e-06F,
            2.8133190e-06F, 2.9961443e-06F, 3.1908506e-06F, 3.3982101e-06F,
            3.6190449e-06F, 3.8542308e-06F, 4.1047004e-06F, 4.3714470e-06F,
            4.6555282e-06F, 4.9580707e-06F, 5.2802740e-06F, 5.6234160e-06F,
            5.9888572e-06F, 6.3780469e-06F, 6.7925283e-06F, 7.2339451e-06F,
            7.7040476e-06F, 8.2047000e-06F, 8.7378876e-06F, 9.3057248e-06F,
            9.9104632e-06F, 1.0554501e-05F, 1.1240392e-05F, 1.1970856e-05F,
            1.2748789e-05F, 1.3577278e-05F, 1.4459606e-05F, 1.5399272e-05F,
            1.6400004e-05F, 1.7465768e-05F, 1.8600792e-05F, 1.9809576e-05F,
            2.1096914e-05F, 2.2467911e-05F, 2.3928002e-05F, 2.5482978e-05F,
            2.7139006e-05F, 2.8902651e-05F, 3.0780908e-05F, 3.2781225e-05F,
            3.4911534e-05F, 3.7180282e-05F, 3.9596466e-05F, 4.2169667e-05F,
            4.4910090e-05F, 4.7828601e-05F, 5.0936773e-05F, 5.4246931e-05F,
            5.7772202e-05F, 6.1526565e-05F, 6.5524908e-05F, 6.9783085e-05F,
            7.4317983e-05F, 7.9147585e-05F, 8.4291040e-05F, 8.9768747e-05F,
            9.5602426e-05F, 0.00010181521F, 0.00010843174F, 0.00011547824F,
            0.00012298267F, 0.00013097477F, 0.00013948625F, 0.00014855085F,
            0.00015820453F, 0.00016848555F, 0.00017943469F, 0.00019109536F,
            0.00020351382F, 0.00021673929F, 0.00023082423F, 0.00024582449F,
            0.00026179955F, 0.00027881276F, 0.00029693158F, 0.00031622787F,
            0.00033677814F, 0.00035866388F, 0.00038197188F, 0.00040679456F,
            0.00043323036F, 0.00046138411F, 0.00049136745F, 0.00052329927F,
            0.00055730621F, 0.00059352311F, 0.00063209358F, 0.00067317058F,
            0.00071691700F, 0.00076350630F, 0.00081312324F, 0.00086596457F,
            0.00092223983F, 0.00098217216F, 0.0010459992F, 0.0011139742F,
            0.0011863665F, 0.0012634633F, 0.0013455702F, 0.0014330129F,
            0.0015261382F, 0.0016253153F, 0.0017309374F, 0.0018434235F,
            0.0019632195F, 0.0020908006F, 0.0022266726F, 0.0023713743F,
            0.0025254795F, 0.0026895994F, 0.0028643847F, 0.0030505286F,
            0.0032487691F, 0.0034598925F, 0.0036847358F, 0.0039241906F,
            0.0041792066F, 0.0044507950F, 0.0047400328F, 0.0050480668F,
            0.0053761186F, 0.0057254891F, 0.0060975636F, 0.0064938176F,
            0.0069158225F, 0.0073652516F, 0.0078438871F, 0.0083536271F,
            0.0088964928F, 0.009474637F, 0.010090352F, 0.010746080F,
            0.011444421F, 0.012188144F, 0.012980198F, 0.013823725F,
            0.014722068F, 0.015678791F, 0.016697687F, 0.017782797F,
            0.018938423F, 0.020169149F, 0.021479854F, 0.022875735F,
            0.024362330F, 0.025945531F, 0.027631618F, 0.029427276F,
            0.031339626F, 0.033376252F, 0.035545228F, 0.037855157F,
            0.040315199F, 0.042935108F, 0.045725273F, 0.048696758F,
            0.051861348F, 0.055231591F, 0.058820850F, 0.062643361F,
            0.066714279F, 0.071049749F, 0.075666962F, 0.080584227F,
            0.085821044F, 0.091398179F, 0.097337747F, 0.10366330F,
            0.11039993F, 0.11757434F, 0.12521498F, 0.13335215F,
            0.14201813F, 0.15124727F, 0.16107617F, 0.17154380F,
            0.18269168F, 0.19456402F, 0.20720788F, 0.22067342F,
            0.23501402F, 0.25028656F, 0.26655159F, 0.28387361F,
            0.30232132F, 0.32196786F, 0.34289114F, 0.36517414F,
            0.38890521F, 0.41417847F, 0.44109412F, 0.46975890F,
            0.50028648F, 0.53279791F, 0.56742212F, 0.60429640F,
            0.64356699F, 0.68538959F, 0.72993007F, 0.77736504F,
            0.82788260F, 0.88168307F, 0.9389798F, 1.F,
    };

    private static void render_line(int n, int x0, final int x1, int y0, final int y1, final float[] d) {
        final int dy = y1 - y0;
        final int adx = x1 - x0;
        int ady = (dy < 0) ? -dy : dy;
        final int base = dy / adx;
        final int sy = (dy < 0 ? base - 1 : base + 1);
        final int dx = base * adx;
        int err = 0;

        ady -= (dx < 0) ? -dx : dx;

        if (n > x1) {
            n = x1;
        }

        final float[] lookup = FLOOR1_fromdB_LOOKUP;// java
        if (x0 < n) {
            d[x0] *= lookup[y0];
        }

        while (++x0 < n) {
            err = err + ady;
            if (err >= adx) {
                err -= adx;
                y0 += sy;
            } else {
                y0 += base;
            }
            d[x0] *= lookup[y0];
        }
    }

    private static void render_line0(int n, int x0, final int x1, int y0, final int y1, final int[] d) {
        final int dy = y1 - y0;
        final int adx = x1 - x0;
        int ady = (dy < 0) ? -dy : dy;
        final int base = dy / adx;
        final int sy = (dy < 0 ? base - 1 : base + 1);
        final int dx = base * adx;
        int err = 0;

        ady -= (dx < 0) ? -dx : dx;

        if (n > x1) {
            n = x1;
        }

        if (x0 < n) {
            d[x0] = y0;
        }

        while (++x0 < n) {
            err = err + ady;
            if (err >= adx) {
                err -= adx;
                y0 += sy;
            } else {
                y0 += base;
            }
            d[x0] = y0;
        }
    }

    /**
     * the floor has already been filtered to only include relevant sections
     */
    private static int accumulate_fit(final float[] flr,
                                      final float[] mdct, int offset,
                                      final int x0, int x1, final lsfit_acc a,
                                      final int n, final InfoFloor1 info) {
        int i;

        int xa = 0, ya = 0, x2a = 0,/* y2a = 0,*/ xya = 0, na = 0;
        int xb = 0, yb = 0, x2b = 0,/* y2b = 0,*/ xyb = 0, nb = 0;

        a.clear();// memset( a, 0, sizeof(*a) );
        a.x0 = x0;
        a.x1 = x1;
        if (x1 >= n) {
            x1 = n - 1;
        }

        final float twofitatten = info.twofitatten;// java
        for (i = x0, offset += x0; i <= x1; i++) {
            final int quantized = dBquant(flr[i]);
            if (quantized != 0) {
                if (mdct[offset++] + twofitatten >= flr[i]) {
                    xa += i;
                    ya += quantized;
                    x2a += i * i;
                    //y2a += quantized * quantized;
                    xya += i * quantized;
                    na++;
                } else {
                    xb += i;
                    yb += quantized;
                    x2b += i * i;
                    //y2b += quantized * quantized;
                    xyb += i * quantized;
                    nb++;
                }
            }
        }

        a.xa = xa;
        a.ya = ya;
        a.x2a = x2a;
        //a.y2a = y2a;// never read
        a.xya = xya;
        a.an = na;

        a.xb = xb;
        a.yb = yb;
        a.x2b = x2b;
        //a.y2b = y2b;// never read
        a.xyb = xyb;
        a.bn = nb;

        return (na);
    }

    private static boolean fit_line(final lsfit_acc[] a, final int offset, int fits,
                                    final int[] y0_y1, final InfoFloor1 info) {
        int y0 = y0_y1[0];
        int y1 = y0_y1[1];

        double xb = 0, yb = 0, x2b = 0,/* y2b = 0,*/ xyb = 0, bn = 0;// FIXME y2b unused
        int i;
        fits += offset;
        final int x0 = a[offset + 0].x0;
        final int x1 = a[fits - 1].x1;

        final float twofitweight = info.twofitweight;// java
        for (i = offset; i < fits; i++) {
            final double weight =
                    (double) (((float) (a[i].bn + a[i].an)) * twofitweight / ((float) (a[i].an + 1))) + 1.;

            xb += (double) a[i].xb + (double) a[i].xa * weight;
            yb += (double) a[i].yb + (double) a[i].ya * weight;
            x2b += (double) a[i].x2b + (double) a[i].x2a * weight;
            //y2b += (double)a[i].y2b + (double)a[i].y2a * weight;
            xyb += (double) a[i].xyb + (double) a[i].xya * weight;
            bn += (double) a[i].bn + (double) a[i].an * weight;
        }

        if (y0 >= 0) {
            xb += x0;
            yb += y0;
            x2b += x0 * x0;
            //y2b +=  y0 * y0;
            xyb += y0 * x0;
            bn++;
        }

        if (y1 >= 0) {
            xb += x1;
            yb += y1;
            x2b += x1 * x1;
            //y2b +=  y1 *  y1;
            xyb += y1 * x1;
            bn++;
        }

        {
            final double denom = (bn * x2b - xb * xb);

            if (denom > 0.) {
                final double d = (yb * x2b - xyb * xb) / denom;
                final double b = (bn * xyb - xb * yb) / denom;
                y0 = (int) Math.rint(d + b * x0);
                y1 = (int) Math.rint(d + b * x1);

                /* limit to our range! */
                if (y0 > 1023) {
                    y0 = 1023;
                }
                if (y1 > 1023) {
                    y1 = 1023;
                }
                if (y0 < 0) {
                    y0 = 0;
                }
                if (y1 < 0) {
                    y1 = 0;
                }

                y0_y1[0] = y0;
                y0_y1[1] = y1;
                return false;
            } else {
                y0_y1[0] = 0;// y0 = 0;
                y0_y1[1] = 0;// y1 = 0;
                return true;
            }
        }
    }

    private static boolean inspect_error(int x0, final int x1, int y0, int y1, final float[] mask,
                                         final float[] logfft, final int mdct,// XXX logfft can be removed and changed to mask
                                         final InfoFloor1 info) {
        final int dy = y1 - y0;
        final int adx = x1 - x0;
        int ady = (dy < 0) ? -dy : dy;
        final int base = dy / adx;
        final int sy = (dy < 0 ? base - 1 : base + 1);
        int err = 0;
        int val = dBquant(mask[x0]);
        int mse = 0;
        int n = 0;

        y1 = base * adx;
        ady -= (y1 < 0) ? -y1 : y1;

        mse = (y0 - val);
        mse *= mse;
        n++;
        final float twofitatten = info.twofitatten;// java
        if (logfft[mdct + x0] + twofitatten >= mask[x0]) {
            if (y0 + info.maxover < val) {
                return true;
            }
            if (y0 - info.maxunder > val) {
                return true;
            }
        }

        while (++x0 < x1) {
            err += ady;
            if (err >= adx) {
                err -= adx;
                y0 += sy;
            } else {
                y0 += base;
            }

            val = dBquant(mask[x0]);
            int dval = y0 - val;
            dval *= dval;
            mse += dval;
            n++;
            if (logfft[mdct + x0] + twofitatten >= mask[x0]) {
                if (val != 0) {
                    if (y0 + info.maxover < val) {
                        return true;
                    }
                    if (y0 - info.maxunder > val) {
                        return true;
                    }
                }
            }
        }

        if (info.maxover * info.maxover / n > info.maxerr) {
            return false;
        }
        if (info.maxunder * info.maxunder / n > info.maxerr) {
            return false;
        }
        if (mse / n > info.maxerr) {
            return true;
        }
        return false;
    }

    private static int post_Y(final int[] A, final int[] B, final int pos) {
        if (A[pos] < 0) {
            return B[pos];
        }
        if (B[pos] < 0) {
            return A[pos];
        }

        return (A[pos] + B[pos]) >> 1;
    }

    static int[] fit(final Block vb, final LookFloor1 look,
                     final float[] logfft,// XXX logfft can be removed and changed to logmask
                     final int logmdct,   /* in */
                            final float[] logmask) {
        int i, j;
        final InfoFloor1 info = look.vi;
        final int n = look.n;
        final int posts = look.posts;
        int nonzero = 0;
        final lsfit_acc[] fits = new lsfit_acc[InfoFloor.VIF_POSIT + 1];
        for (i = 0; i < InfoFloor.VIF_POSIT + 1; i++) {
            fits[i] = new lsfit_acc();
        }
        final int[] fit_valueA = new int[InfoFloor.VIF_POSIT + 2]; /* index by range list position */
        final int[] fit_valueB = new int[InfoFloor.VIF_POSIT + 2]; /* index by range list position */

        final int[] loneighbor = new int[InfoFloor.VIF_POSIT + 2]; /* sorted index of range list position (+2) */
        final int[] hineighbor = new int[InfoFloor.VIF_POSIT + 2];
        int[] output = null;
        final int[] memo = new int[InfoFloor.VIF_POSIT + 2];

        for (i = 0; i < posts; i++) {
            fit_valueA[i] = -200; /* mark all unused */
        }
        for (i = 0; i < posts; i++) {
            fit_valueB[i] = -200; /* mark all unused */
        }
        for (i = 0; i < posts; i++) {
            loneighbor[i] = 0; /* 0 for the implicit 0 post */
        }
        for (i = 0; i < posts; i++) {
            hineighbor[i] = 1; /* 1 for the implicit post at n */
        }
        for (i = 0; i < posts; i++) {
            memo[i] = -1;      /* no neighbor yet */
        }

		/* quantize the relevant floor points and collect them into line fit
		structures (one per minimal division) at the same time */
        if (posts == 0) {
            nonzero += accumulate_fit(logmask, logfft, logmdct, 0, n, fits[0], n, info);
        } else {
            for (i = 0; i < posts - 1; i++) {
                nonzero += accumulate_fit(logmask, logfft, logmdct, look.sorted_index[i],
                        look.sorted_index[i + 1], fits[i],
                        n, info);
            }
        }

        if (nonzero != 0) {
            /* start by fitting the implicit base case.... */
            //int y0 = -200;
            //int y1 = -200;
            final int[] y0_y1 = {-200, -200};
            fit_line(fits, 0, posts - 1, y0_y1, info);

            fit_valueA[0] = y0_y1[0];// y0
            fit_valueB[0] = y0_y1[0];// y0
            fit_valueB[1] = y0_y1[1];// y1
            fit_valueA[1] = y0_y1[1];// y1

            /* Non degenerate case */
			/* start progressive splitting.  This is a greedy, non-optimal
			algorithm, but simple and close enough to the best
			answer. */
            final int[] reverse_index = look.reverse_index;// java
            for (i = 2; i < posts; i++) {
                final int sortpos = reverse_index[i];
                final int ln = loneighbor[sortpos];
                final int hn = hineighbor[sortpos];

                /* eliminate repeat searches of a particular range with a memo */
                if (memo[ln] != hn) {
                    /* haven't performed this error search yet */
                    final int lsortpos = reverse_index[ln];
                    final int hsortpos = reverse_index[hn];
                    memo[ln] = hn;

                    {
                        /* A note: we want to bound/minimize *local*, not global, error */
                        final int lx = info.postlist[ln];
                        final int hx = info.postlist[hn];
                        final int ly = post_Y(fit_valueA, fit_valueB, ln);
                        final int hy = post_Y(fit_valueA, fit_valueB, hn);

                        if (ly == -1 || hy == -1) {
                            //System.err.println("ERROR: We want to bound/minimize *local*, not global");
                            //System.exit(1);
                            // Naoko: FIXED. throw exception instead of exit(1).
                            throw new IllegalStateException("ERROR: We want to bound/minimize *local*, not global");
                        }

                        if (inspect_error(lx, hx, ly, hy, logmask, logfft, logmdct, info)) {
                            /* outside error bounds/begin search area.  Split it. */
                            int ly0 = -200;
                            int ly1 = -200;
                            int hy0 = -200;
                            int hy1 = -200;
                            y0_y1[0] = ly0;
                            y0_y1[1] = ly1;
                            final boolean ret0 = fit_line(fits, lsortpos, sortpos - lsortpos, y0_y1, info);
                            ly0 = y0_y1[0];
                            ly1 = y0_y1[1];
                            y0_y1[0] = hy0;
                            y0_y1[1] = hy1;
                            final boolean ret1 = fit_line(fits, sortpos, hsortpos - sortpos, y0_y1, info);
                            hy0 = y0_y1[0];
                            hy1 = y0_y1[1];

                            if (ret0) {
                                ly0 = ly;
                                ly1 = hy0;
                            }
                            if (ret1) {
                                hy0 = ly1;
                                hy1 = hy;
                            }

                            if (ret0 && ret1) {
                                fit_valueA[i] = -200;
                                fit_valueB[i] = -200;
                            } else {
                                /* store new edge values */
                                fit_valueB[ln] = ly0;
                                if (ln == 0) {
                                    fit_valueA[ln] = ly0;
                                }
                                fit_valueA[i] = ly1;
                                fit_valueB[i] = hy0;
                                fit_valueA[hn] = hy1;
                                if (hn == 1) {
                                    fit_valueB[hn] = hy1;
                                }

                                if (ly1 >= 0 || hy0 >= 0) {
                                    /* store new neighbor values */
                                    for (j = sortpos - 1; j >= 0; j--) {
                                        if (hineighbor[j] == hn) {
                                            hineighbor[j] = i;
                                        } else {
                                            break;
                                        }
                                    }
                                    for (j = sortpos + 1; j < posts; j++) {
                                        if (loneighbor[j] == ln) {
                                            loneighbor[j] = i;
                                        } else {
                                            break;
                                        }
                                    }
                                }
                            }
                        } else {
                            fit_valueA[i] = -200;
                            fit_valueB[i] = -200;
                        }
                    }
                }
            }

            output = new int[posts];

            output[0] = post_Y(fit_valueA, fit_valueB, 0);
            output[1] = post_Y(fit_valueA, fit_valueB, 1);

			/* fill in posts marked as not using a fit; we will zero
			back out to 'unused' when encoding them so long as curve
			interpolation doesn't force them into use */
            final int[] look_loneighbor = look.loneighbor;// java
            final int[] look_hineighbor = look.hineighbor;// java
            final int[] postlist = info.postlist;// java
            for (i = 2; i < posts; i++) {
                final int ln = look_loneighbor[i - 2];
                final int hn = look_hineighbor[i - 2];
                final int x0 = postlist[ln];
                final int x1 = postlist[hn];
                final int y0 = output[ln];
                final int y1 = output[hn];

                final int predicted = render_point(x0, x1, y0, y1, postlist[i]);
                final int vx = post_Y(fit_valueA, fit_valueB, i);

                if (vx >= 0 && predicted != vx) {
                    output[i] = vx;
                } else {
                    output[i] = predicted | 0x8000;
                }
            }
        }

        return (output);

    }

    static int[] floor1_interpolate_fit(final Block vb, final LookFloor1 look,
                                        final int[] A, final int[] B,
                                        final int del) {
        final int posts = look.posts;
        int[] output = null;

        if (A != null && B != null) {
            output = new int[posts];

            /* overly simpleminded--- look again post 1.2 */
            for (int i = 0; i < posts; i++) {
                final int a = A[i];
                final int b = B[i];
                output[i] = ((65536 - del) * (a & 0x7fff) + del * (b & 0x7fff) + 32768) >> 16;
                if ((a & 0x8000) != 0 && (b & 0x8000) != 0) {
                    output[i] |= 0x8000;
                }
            }
        }

        return (output);
    }

    static boolean floor1_encode(final Buffer opb, final Block vb,
                                 final LookFloor1 look,
                                 final int[] post, final int[] ilogmask) {
        /* quantize values to multiplier spec */
        if (post != null) {
            final InfoFloor1 info = look.vi;
            final int posts = look.posts;
            final int mult = info.mult;// java
            for (int i = 0; i < posts; i++) {
                int val = post[i] & 0x7fff;
                switch (mult) {
                    case 1: /* 1024 -> 256 */
                        val >>= 2;
                        break;
                    case 2: /* 1024 -> 128 */
                        val >>= 3;
                        break;
                    case 3: /* 1024 -> 86 */
                        val /= 12;
                        break;
                    case 4: /* 1024 -> 64 */
                        val >>= 4;
                        break;
                }
                post[i] = val | (post[i] & 0x8000);
            }

            final int[] out = new int[InfoFloor.VIF_POSIT + 2];
            out[0] = post[0];
            out[1] = post[1];

            /* find prediction values for each post and subtract them */
            final int[] postlist = info.postlist;// java
            for (int i = 2; i < posts; i++) {
                final int ln = look.loneighbor[i - 2];
                final int hn = look.hineighbor[i - 2];
                final int x0 = postlist[ln];
                final int x1 = postlist[hn];
                final int y0 = post[ln];
                final int y1 = post[hn];

                final int predicted = render_point(x0, x1, y0, y1, postlist[i]);

                if ((post[i] & 0x8000) != 0 || (predicted == post[i])) {
                    post[i] = predicted | 0x8000; /* in case there was roundoff jitter
												in interpolation */
                    out[i] = 0;
                } else {
                    final int headroom = (look.quant_q - predicted < predicted ?
                            look.quant_q - predicted : predicted);

                    int val = post[i] - predicted;

					/* at this point the 'deviation' value is in the range +/- max
					range, but the real, unique range can always be mapped to
					only [0-maxrange).  So we want to wrap the deviation into
					this limited range, but do it in the way that least screws
					an essentially gaussian probability distribution. */

                    if (val < 0) {
                        if (val < -headroom) {
                            val = headroom - val - 1;
                        } else {
                            val = -1 - (val << 1);
                        }
                    } else if (val >= headroom) {
                        val = val + headroom;
                    } else {
                        val <<= 1;
                    }

                    out[i] = val;
                    post[ln] &= 0x7fff;
                    post[hn] &= 0x7fff;
                }
            }

            /* we have everything we need. pack it out */
            /* mark nontrivial floor */
            opb.pack_write(1, 1);

            /* beginning/end post */
            look.frames++;
            final int ii = Codec.ilog(look.quant_q - 1);
            look.postbits += ii << 1;
            opb.pack_write(out[0], ii);
            opb.pack_write(out[1], ii);

            final CodecSetupInfo ci = vb.vd.vi.codec_setup;
            final StaticCodebook[] sbooks = ci.book_param;
            final Codebook[] books = ci.fullbooks;
            /* partition by partition */
            final int[] partitionclass = info.partitionclass;// java
            final int[] class_dim = info.class_dim;// java
            final int[] class_subs = info.class_subs;// java
            final int[][] class_subbook = info.class_subbook;// java
            for (int i = 0, j = 2, ie = info.partitions; i < ie; i++) {
                final int iclass = partitionclass[i];// class renamed to iclass
                final int cdim = class_dim[iclass];
                final int csubbits = class_subs[iclass];
                final int[] subbook = class_subbook[iclass];
                final int bookas[] = new int[8];//{0,0,0,0,0,0,0,0};// [8]

                /* generate the partition's first stage cascade value */
                if (csubbits != 0) {
                    final int[] maxval = new int[8]; /* ={0,0,0,0,0,0,0,0}; gcc's static analysis
														issues a warning without
														initialization */
                    final int csub = 1 << csubbits;
                    for (int k = 0; k < csub; k++) {
                        final int booknum = subbook[k];
                        if (booknum < 0) {
                            maxval[k] = 1;
                        } else {
                            maxval[k] = sbooks[booknum].entries;
                        }
                    }
                    int cval = 0;
                    int cshift = 0;
                    for (int k = 0; k < cdim; k++) {
                        for (int l = 0; l < csub; l++) {
                            final int val = out[j + k];
                            if (val < maxval[l]) {
                                bookas[k] = l;
                                break;
                            }
                        }
                        cval |= bookas[k] << cshift;
                        cshift += csubbits;
                    }
                    /* write it */
                    look.phrasebits +=
                            books[info.class_book[iclass]].encode(cval, opb);

/*if( TRAIN_FLOOR1 ) {
						java.io.PrintStream of = null;
						try {
							of = new java.io.PrintStream( new java.io.FileOutputStream(
								String.format("line_%dx%d_class%d.vqd", vb.m_pcmend / 2, posts - 2, iclass),
								true ) );
							of.printf( "%d\n", cval );
						} catch( Exception e ) {
						} finally {
							if( of != null ) of.close();
						}
//}*/
                }

                /* write post values */
                for (int k = 0; k < cdim; k++) {
                    final int book = subbook[bookas[k]];
                    if (book >= 0) {
                        /* hack to allow training with 'bad' books */
                        if (out[j + k] < books[book].entries) {
                            look.postbits += books[book].encode(out[j + k], opb);
						/*else
							fprintf( stderr, "+!" );*/
                        }

/*if( TRAIN_FLOOR1 ) {
							java.io.PrintStream of = null;
							try {
								of = new java.io.PrintStream( new java.io.FileOutputStream(
									String.format("line_%dx%d_%dsub%d.vqd", vb.m_pcmend / 2, posts - 2, bookas[k]),
									true ) );
								of.printf( "%d\n", out[j + k] );
							} catch(Exception e) {
							} finally {
								if( of != null ) of.close();
							}
//}*/
                    }
                }
                j += cdim;
            }

            {
                /* generate quantized floor equivalent to what we'd unpack in decode */
                /* render the lines */
                int hx = 0;
                int lx = 0;
                int ly = post[0] * info.mult;
                final int n = ci.blocksizes[vb.W] >> 1;

                final int[] forward_index = look.forward_index;// java
                for (int j = 1, je = look.posts; j < je; j++) {
                    final int current = forward_index[j];
                    int hy = post[current] & 0x7fff;
                    if (hy == post[current]) {

                        hy *= info.mult;
                        hx = postlist[current];

                        render_line0(n, lx, hx, ly, hy, ilogmask);

                        lx = hx;
                        ly = hy;
                    }
                }
                for (int j = hx, je = vb.pcmend >> 1; j < je; j++) {
                    ilogmask[j] = ly; /* be certain */
                }
                return true;
            }
        } else {
            opb.pack_write(0, 1);
            Arrays.fill(ilogmask, 0, vb.pcmend >>> 1, 0);
            return false;
        }
    }

    @Override
    // static void *floor1_inverse1(vorbis_block *vb,vorbis_look_floor *in)
    final Object inverse1(final Block vb, final LookFloor in) {
        /* unpack wrapped/predicted values from stream */
        final Buffer opb = vb.opb;// java
        if (opb.pack_read(1) == 1) {
            final LookFloor1 look = (LookFloor1) in;
            final int[] fit_value = new int[look.posts];

            final int ii = Codec.ilog(look.quant_q - 1);
            fit_value[0] = opb.pack_read(ii);
            fit_value[1] = opb.pack_read(ii);

            final InfoFloor1 info = look.vi;
            final Codebook[] books = vb.vd.vi.codec_setup.fullbooks;
            /* partition by partition */
            final int[] partitionclass = info.partitionclass;// java
            final int[] class_dim = info.class_dim;// java
            final int[] class_subs = info.class_subs;// java
            final int[][] class_subbook = info.class_subbook;// java
            for (int i = 0, j = 2, ie = info.partitions; i < ie; i++) {
                final int iclass = partitionclass[i];
                final int cdim = class_dim[iclass];
                final int csubbits = class_subs[iclass];
                final int[] subbook = class_subbook[iclass];
                final int csub = 1 << csubbits;
                int cval = 0;

                /* decode the partition's first stage cascade value */
                if (csubbits != 0) {
                    cval = books[info.class_book[iclass]].decode(opb);

                    if (cval == -1) {
                        return null;// goto eop;
                    }
                }

                for (int k = 0; k < cdim; k++) {
                    final int book = subbook[cval & (csub - 1)];
                    cval >>= csubbits;
                    if (book >= 0) {
                        if ((fit_value[j + k] = books[book].decode(opb)) == -1) {
                            return null;// goto eop;
                        }
                    } else {
                        fit_value[j + k] = 0;
                    }
                }
                j += cdim;
            }

            /* unwrap positive values and reconsitute via linear interpolation */
            final int[] postlist = info.postlist;// java
            final int[] loneighbor = look.loneighbor;// java
            final int[] hineighbor = look.hineighbor;// java
            for (int k = 0, i = 2, ie = look.posts; i < ie; i++, k++) {
                final int loneighbor_k = loneighbor[k];// java
                final int hineighbor_k = hineighbor[k];// java
                final int predicted = render_point(postlist[loneighbor_k],
                        postlist[hineighbor_k],
                        fit_value[loneighbor_k],
                        fit_value[hineighbor_k],
                        postlist[i]);
                final int hiroom = look.quant_q - predicted;
                final int loroom = predicted;
                final int room = (hiroom < loroom ? hiroom : loroom) << 1;
                int val = fit_value[i];

                if (val != 0) {
                    if (val >= room) {
                        if (hiroom > loroom) {
                            val = val - loroom;
                        } else {
                            val = -1 - (val - hiroom);
                        }
                    } else {
                        if ((val & 1) != 0) {
                            val = -((val + 1) >> 1);
                        } else {
                            val >>= 1;
                        }
                    }

                    fit_value[i] = (val + predicted) & 0x7fff;
                    fit_value[loneighbor_k] &= 0x7fff;
                    fit_value[hineighbor_k] &= 0x7fff;

                } else {
                    fit_value[i] = predicted | 0x8000;
                }

            }

            return (fit_value);
        }
//eop:
        return null;
    }

    @Override
    final boolean inverse2(final Block vb, final LookFloor in, final Object memo, final float[] out) {
        final int n = vb.vd.vi.codec_setup.blocksizes[vb.W] >> 1;

        if (memo != null) {
            final LookFloor1 look = (LookFloor1) in;
            final InfoFloor1 info = look.vi;
            /* render the lines */
            final int[] fit_value = (int[]) memo;
            int hx = 0;
            int lx = 0;
            final int mult = info.mult;// java
            int ly = fit_value[0] * mult;
            /* guard lookup against out-of-range values */
            ly = (ly < 0 ? 0 : ly > 255 ? 255 : ly);

            final int[] forward_index = look.forward_index;// java
            final int[] postlist = info.postlist;// java
            for (int j = 1, je = look.posts; j < je; j++) {
                final int current = forward_index[j];
                int hy = fit_value[current] & 0x7fff;
                if (hy == fit_value[current]) {

                    hx = postlist[current];
                    hy *= mult;
                    /* guard lookup against out-of-range values */
                    hy = (hy < 0 ? 0 : hy > 255 ? 255 : hy);

                    render_line(n, lx, hx, ly, hy, out);

                    lx = hx;
                    ly = hy;
                }
            }
            final float[] lookup = FLOOR1_fromdB_LOOKUP;// java
            for (int j = hx; j < n; j++) {
                out[j] *= lookup[ly]; /* be certain */
            }
            return true;
        }
        Arrays.fill(out, 0, n, 0);
        return false;
    }
}
