/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 2019 Alexey Kuznetsov
 * Copyright (c) 2002-2018 Xiph.Org Foundation
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Neither the name of the Xiph.org Foundation nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE FOUNDATION
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.xiph.vorbis;

import java.util.Arrays;

/**
 * simplistic, wasteful way of doing this (unique lookup for each
 * mode/submapping); there should be a central repository for
 * identical lookups.  That will require minor work, so I'm putting it
 * off as low priority.
 * <p>
 * Why a lookup for each backend in a given mode?  Because the
 * blocksize is set by the mode, and low backend lookups may require
 * parameters from other areas of the mode/mapping
 * <p>
 * export hooks<p>
 * <pre>
 * const vorbis_func_mapping mapping0_exportbundle = {
 * &mapping0_pack,
 * &mapping0_unpack,
 * &mapping0_free_info,
 * &mapping0_forward,
 * &mapping0_inverse
 * };
 * </pre>
 */
class Mapping0 extends FuncMapping {

    // use InfoMapping = null
    // static void mapping0_free_info(vorbis_info_mapping *i)
	/*static void mapping0_free_info(Object i) {
		InfoMapping0 info = (InfoMapping0)i;
		if( info ) {
			memset( info, 0, sizeof(*info) );
			_ogg_free( info);
		}
	}*/

    @Override
    // void mapping0_pack(vorbis_info *vi, vorbis_info_mapping *vm, oggpack_buffer *opb)
    final void pack(final Info vi, final InfoMapping vm, final Buffer opb) {
        final InfoMapping0 info = (InfoMapping0) vm;

		/* another 'we meant to do it this way' hack...  up to beta 4, we
		packed 4 binary zeros here to signify one submapping in use.  We
		now redefine that to mean four bitflags that indicate use of
		deeper features; bit0:submappings, bit1:coupling,
		bit2,3:reserved. This is backward compatable with all actual uses
		of the beta code. */

        final int submaps = info.submaps;// java
        if (submaps > 1) {
            opb.pack_write(1, 1);
            opb.pack_write(submaps - 1, 4);
        } else {
            opb.pack_write(0, 1);
        }

        final int coupling_steps = info.coupling_steps;// java
        if (coupling_steps > 0) {
            opb.pack_write(1, 1);
            opb.pack_write(coupling_steps - 1, 8);

            final int ilog = Codec.ilog(vi.channels - 1);// java
            final int[] coupling_mag = info.coupling_mag;// java
            final int[] coupling_ang = info.coupling_ang;// java
            for (int i = 0; i < coupling_steps; i++) {
                opb.pack_write(coupling_mag[i], ilog);
                opb.pack_write(coupling_ang[i], ilog);
            }
        } else {
            opb.pack_write(0, 1);
        }

        opb.pack_write(0, 2); /* 2,3:reserved */

        /* we don't write the channel submappings if we only have one... */
        if (submaps > 1) {
            final int[] chmuxlist = info.chmuxlist;// java
            for (int i = 0, ie = vi.channels; i < ie; i++) {
                opb.pack_write(chmuxlist[i], 4);
            }
        }
        final int[] floorsubmap = info.floorsubmap;// java
        final int[] residuesubmap = info.residuesubmap;// java
        for (int i = 0; i < submaps; i++) {
            opb.pack_write(0, 8); /* time submap unused */
            opb.pack_write(floorsubmap[i], 8);
            opb.pack_write(residuesubmap[i], 8);
        }
    }

    @Override
    // static vorbis_info_mapping *mapping0_unpack(vorbis_info *vi, oggpack_buffer *opb)
    /** also responsible for range checking */
    final InfoMapping unpack(final Info vi, final Buffer opb) {
        if (vi.channels <= 0) {
            return null;// goto err_out;
        }

        int b = opb.pack_read(1);
        if (b < 0) {
            return null;// goto err_out;
        }
        final InfoMapping0 info = new InfoMapping0();// already zeroed
        if (b != 0) {
            info.submaps = opb.pack_read(4) + 1;
            if (info.submaps <= 0) {
                return null;// goto err_out;
            }
        } else {
            info.submaps = 1;
        }

        b = opb.pack_read(1);
        if (b < 0) {
            return null;// goto err_out;
        }
        if (b != 0) {
            final int coupling_steps = opb.pack_read(8) + 1;
            info.coupling_steps = coupling_steps;
            if (coupling_steps <= 0) {
                return null;// goto err_out;
            }
            final int ilog = Codec.ilog(vi.channels - 1);// java
            final int[] coupling_mag = info.coupling_mag;// java
            final int[] coupling_ang = info.coupling_ang;// java
            for (int i = 0; i < coupling_steps; i++) {
                /* vi->channels > 0 is enforced in the caller */
                final int testM = coupling_mag[i] = opb.pack_read(ilog);
                final int testA = coupling_ang[i] = opb.pack_read(ilog);

                if (testM < 0 ||
                        testA < 0 ||
                        testM == testA ||
                        testM >= vi.channels ||
                        testA >= vi.channels) {
                    return null;// goto err_out;
                }
            }

        }

        if (opb.pack_read(2) != 0) {
            return null;// goto err_out; /* 2,3:reserved */
        }

        final int submaps = info.submaps;// java
        if (submaps > 1) {
            final int[] chmuxlist = info.chmuxlist;// java
            for (int i = 0, ie = vi.channels; i < ie; i++) {
                b = opb.pack_read(4);
                chmuxlist[i] = b;
                if (b >= submaps || b < 0) {
                    return null;// goto err_out;
                }
            }
        }
        final CodecSetupInfo ci = vi.codec_setup;
        final int[] floorsubmap = info.floorsubmap;// java
        final int[] residuesubmap = info.residuesubmap;// java
        for (int i = 0; i < submaps; i++) {
            opb.pack_read(8); /* time submap unused */
            b = opb.pack_read(8);
            floorsubmap[i] = b;
            if (b >= ci.floors || b < 0) {
                return null;// goto err_out;
            }
            b = opb.pack_read(8);
            residuesubmap[i] = b;
            if (b >= ci.residues || b < 0) {
                return null;// goto err_out;
            }
        }

        return info;

//err_out:
//		mapping0_free_info( info );
//		return (null);
    }

/*#if 0
	private static int seq = 0;
	private static long total = 0;
	private static float FLOOR1_fromdB_LOOKUP[] = {// [256]
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

//#endif*/

    @Override
    // static int mapping0_forward(vorbis_block *vb)
    final int forward(final Block vb) {
        final Info vi = vb.vd.vi;
        final CodecSetupInfo ci = vi.codec_setup;
        final PrivateState b = vb.vd.backend_state;
        final BlockInternal vbi = (BlockInternal) vb.m_internal;
        final int n = vb.pcmend;
        final int n2 = n >>> 1;

        final int channels = vi.channels;// java
        final boolean[] nonzero = new boolean[channels];
        final float[][] gmdct = new float[channels][];
        final int[][] iwork = new int[channels][];
        final int[][][] floor_posts = new int[channels][][];

        float global_ampmax = vbi.ampmax;
        final float[] local_ampmax = new float[channels];
        final int blocktype = vbi.blocktype;

        final int modenumber = vb.W;

        final LookPsy psy_look = b.psy[blocktype + (modenumber != 0 ? 2 : 0)];

        vb.mode = modenumber;

        // final float scale = 4.f / n;// java: moved up from the loop
        // java: added 0.345f to scale_dB
        final float scale_dB = Codec.todB(4.f / n /*scale*/) + .345f + .345f; /* + .345 is a hack; the original
															todB estimation used on IEEE 754
															compliant machines had a bug that
															returned dB values about a third
															of a decibel too high.  The bug
															was harmless because tunings
															implicitly took that into
															account.  However, fixing the bug
															in the estimator requires
															changing all the tunings as well.
															For now, it's easier to sync
															things back up here, and
															recalibrate the tunings in the
															next major model upgrade. */
        final MDCTLookup b_transform_modenumber_0 = b.transform[modenumber][0];// java
        final DRFTLookup b_fft_look_modenumber = b.fft_look[modenumber];// java
        for (int i = 0; i < channels; i++) {

            final float[] pcm = vb.pcm[i];
            final float[] logfft = pcm;

            iwork[i] = new int[n2];
            gmdct[i] = new float[n2];

/*if( false ) {// #if 0
			if( vi.channels == 2 ) {
				if( i == 0 )
					Analysis._analysis_output("pcmL", seq, pcm, 0, n, false, false, total - n2 );
				else
					Analysis._analysis_output("pcmR", seq, pcm, 0, n, false, false, total - n2 );
			} else {
				Analysis._analysis_output("pcm", seq, pcm, 0, n, false, false, total - n2 );
			}
//} #endif*/

            /* window the PCM data */
            Window._apply_window(pcm, b.window, ci.blocksizes, vb.lW, modenumber, vb.nW);

/*if( false ) {// #if 0
			if( vi.channels == 2 ) {
				if( i == 0 )
					Analysis._analysis_output("windowedL", seq, pcm, 0, n, false, false, total - n2 );
				else
					Analysis._analysis_output("windowedR", seq, pcm, 0, n, false, false, total - n2 );
			} else {
				Analysis._analysis_output("windowed", seq, pcm, 0, n, false, false, total - n2 );
			}
}// #endif*/

            /* transform the PCM data */
            /* only MDCT right now.... */
            b_transform_modenumber_0.forward(pcm, gmdct[i]);

            /* FFT yields more accurate tonal estimation (not phase sensitive) */
            SmallFT.drft_forward(b_fft_look_modenumber, pcm);
            logfft[0] = scale_dB + Codec.todB(pcm[0])/* + .345f*/; /* + .345 is a hack; the
												original todB estimation used on
												IEEE 754 compliant machines had a
												bug that returned dB values about
												a third of a decibel too high.
												The bug was harmless because
												tunings implicitly took that into
												account.  However, fixing the bug
												in the estimator requires
												changing all the tunings as well.
												For now, it's easier to sync
												things back up here, and
												recalibrate the tunings in the
												next major model upgrade. */
            float local_ampmax_i = logfft[0];// java
            for (int j = 1, je = n - 1; j < je; j += 2) {
                float temp = pcm[j];// java
                temp *= temp;
                final int k = j + 1;
                float v = pcm[k];// java
                v *= v;
                temp += v;
                temp = logfft[k >> 1] = scale_dB + .5f * Codec.todB(temp)/* + .345f*/; /* +
													.345 is a hack; the original todB
													estimation used on IEEE 754
													compliant machines had a bug that
													returned dB values about a third
													of a decibel too high.  The bug
													was harmless because tunings
													implicitly took that into
													account.  However, fixing the bug
													in the estimator requires
													changing all the tunings as well.
													For now, it's easier to sync
													things back up here, and
													recalibrate the tunings in the
													next major model upgrade. */
                if (temp > local_ampmax_i) {
                    local_ampmax_i = temp;
                }
            }

            if (local_ampmax_i > 0.f) {
                local_ampmax_i = 0.f;
            }
            if (local_ampmax_i > global_ampmax) {
                global_ampmax = local_ampmax_i;
            }
            local_ampmax[i] = local_ampmax_i;// java

/*if( false ) {// #if 0
			if( vi.channels == 2 ) {
				if( i == 0 ) {
					Analysis._analysis_output("fftL", seq, logfft, 0, n2, true, false, 0 );
				} else {
					Analysis._analysis_output("fftR", seq, logfft, 0, n2, true, false, 0 );
				}
			} else {
				Analysis._analysis_output("fft", seq, logfft, 0, n2, true, false, 0 );
			}
}// #endif*/

        }
        final InfoMapping0 info = (InfoMapping0) ci.map_param[modenumber];
        final LookFloor[] flr = b.flr;// java
        final int[] floorsubmap = info.floorsubmap;// java
        final int[] chmuxlist = info.chmuxlist;// java
        {
            final int[] floor_type = ci.floor_type;// java
            final float[] noise = new float[n2];
            final float[] tone = new float[n2];

            for (int i = 0; i < channels; i++) {
				/* the encoder setup assumes that all the modes used by any
				specific bitrate tweaking use the same floor */

                /* the following makes things clearer to *me* anyway */
                final float[] mdct = gmdct[i];
                final float[] logfft = vb.pcm[i];

                // final int logmdct = n2;// float *logmdct  = logfft + n / 2;
                //float *logmask  = logfft;

                vb.mode = modenumber;

                final int fp_i[][] = new int[Info.PACKETBLOBS][];
                floor_posts[i] = fp_i;

                for (int j = 0, k = n2; j < n2; j++, k++) {
                    logfft[k] = Codec.todB(mdct[j]) + .345f; /* + .345 is a hack; the original
												todB estimation used on IEEE 754
												compliant machines had a bug that
												returned dB values about a third
												of a decibel too high.  The bug
												was harmless because tunings
												implicitly took that into
												account.  However, fixing the bug
												in the estimator requires
												changing all the tunings as well.
												For now, it's easier to sync
												things back up here, and
												recalibrate the tunings in the
												next major model upgrade. */
                }

/*if( false ) {// #if 0
				if( vi.channels == 2 ) {
					if( i == 0 )
						Analysis._analysis_output("mdctL", seq, logfft, logmdct, n2, true, false, 0);
					else
						Analysis._analysis_output("mdctR", seq, logfft, logmdct, n2, true, false, 0);
				} else {
					Analysis._analysis_output("mdct", seq, logfft, logmdct, n2, true, false, 0);
				}
}// #endif*/

				/* first step; noise masking.  Not only does 'noise masking'
				 give us curves from which we can decide how much resolution
				 to give noise parts of the spectrum, it also implicitly hands
				 us a tonality estimate (the larger the value in the
				 'noise_depth' vector, the more tonal that area is) */

                psy_look._noisemask(logfft, n2, noise); /* noise does not have by-frequency offset
										bias applied yet */
/*if( false ) {// #if 0
				if( vi.channels == 2 ) {
					if( i == 0 )
						Analysis._analysis_output("noiseL", seq, noise, 0, n2, true, false, 0);
					else
						Analysis._analysis_output("noiseR", seq, noise, 0, n2, true, false, 0);
				} else {
					Analysis._analysis_output("noise", seq, noise, 0, n2, true, false, 0);
				}
}// #endif*/

				/* second step: 'all the other crap'; all the stuff that isn't
				 computed/fit for bitrate management goes in the second psy
				 vector.  This includes tone masking, peak limiting and ATH */

                psy_look._tonemask(logfft, tone, global_ampmax, local_ampmax[i]);

/*if( false ) {// #if 0
				if( vi.channels == 2 ) {
					if( i == 0 )
						Analysis._analysis_output("toneL", seq, tone, 0, n2, true, false, 0);
					else
						Analysis._analysis_output("toneR", seq, tone, 0, n2, true, false, 0);
				} else {
					Analysis._analysis_output("tone", seq, tone, 0, n2, true, false, 0);
				}
}// #endif*/

				/* third step; we offset the noise vectors, overlay tone
				 masking.  We then do a floor1-specific line fit.  If we're
				 performing bitrate management, the line fit is performed
				 multiple times for up/down tweakage on demand. */

                {
//if( false ) {// #if 0
//					final float[] aotuv = new float[psy_look.n];
//}// #endif

                    psy_look._offset_and_mix(noise, tone, 1,
                            logfft,// logmask is 0 offset to logfft
                            mdct, n2);

/*if( false ) {// #if 0
					final float[] aotuv = null;
					if( vi.channels == 2 ) {
						if( i == 0 )
							Analysis._analysis_output("aotuvM1_L", seq, aotuv, 0, psy_look.n, true, true, 0 );
						else
							Analysis._analysis_output("aotuvM1_R", seq, aotuv, 0, psy_look.n, true, true, 0 );
					} else {
						Analysis._analysis_output("aotuvM1", seq, aotuv, 0, psy_look.n, true, true, 0 );
					}
}// #endif*/
                }


/*if( false ) {// #if 0
				if( vi.channels == 2 ) {
					if( i == 0 )
						Analysis._analysis_output("mask1L", seq, logfft, logmask, n / 2, true, false, 0 );
					else
						Analysis._analysis_output("mask1R", seq, logfft, logmask, n / 2, true, false, 0 );
				} else {
					Analysis._analysis_output("mask1", seq, logfft, logmask, n / 2, true, false, 0 );
				}
}// #endif*/

				/* this algorithm is hardwired to floor 1 for now; abort out if
				 we're *not* floor1.  This won't happen unless someone has
				 broken the encode setup lib.  Guard it anyway. */
                // final int submap = info.chmuxlist[i];
                final int floorsubmap_submap = floorsubmap[chmuxlist[i]];// java
                if (floor_type[floorsubmap_submap] != 1) {
                    return (-1);
                }
                final LookFloor1 flr_floorsubmap_submap = (LookFloor1) flr[floorsubmap_submap];// java
                fp_i[Info.PACKETBLOBS / 2] =
                        Floor1.fit(vb, flr_floorsubmap_submap, logfft, n2,
                                logfft);// logmask );// logmask is 0 offset to logfft

				/* are we managing bitrate?  If so, perform two more fits for
				 later rate tweaking (fits represent hi/lo) */
                if (vb.bitrate_managed() &&
                        fp_i[Info.PACKETBLOBS / 2] != null) {
                    /* higher rate by way of lower noise curve */

                    psy_look._offset_and_mix(noise, tone, 2,
                            logfft,// logmask is 0 offset to logfft
                            mdct, n2);

/*if( false ) {// #if 0
					if( vi.channels == 2 ) {
						if( i == 0 )
							Analysis._analysis_output("mask2L", seq, logfft, logmask, n2, true, false, 0 );
						else
							Analysis._analysis_output("mask2R", seq, logfft, logmask, n2, true, false, 0 );
					} else {
						Analysis._analysis_output("mask2", seq, logfft, logmask, n2, true, false, 0 );
					}
}// #endif*/

                    fp_i[Info.PACKETBLOBS - 1] =
                            Floor1.fit(vb, flr_floorsubmap_submap, logfft, n2, logfft);// logmask );// logmask is 0 offset to logfft

                    /* lower rate by way of higher noise curve */
                    psy_look._offset_and_mix(noise, tone, 0,
                            logfft,// logmask is 0 offset to logfft
                            mdct, n2);

/*if( false ) {// #if 0
					if( vi.channels == 2 ) {
						if( i == 0 )
							Analysis._analysis_output("mask0L", seq, logfft, logmask, n2, true, false, 0 );
						else
							Analysis._analysis_output("mask0R", seq, logfft, logmask, n2, true, false, 0 );
					} else {
						Analysis._analysis_output("mask0", seq, logfft, logmask, n2, true, false, 0 );
					}
}// #endif*/

                    fp_i[0] =
                            Floor1.fit(vb, flr_floorsubmap_submap, logfft, n2, logfft);// logmask );// logmask is 0 offset to logfft

					/* we also interpolate a range of intermediate curves for
					   intermediate rates */
                    for (int k = 1; k < Info.PACKETBLOBS / 2; k++) {
                        fp_i[k] =
                                Floor1.floor1_interpolate_fit(vb, flr_floorsubmap_submap, fp_i[0],
                                        fp_i[Info.PACKETBLOBS / 2],
                                        (k << 16) / (Info.PACKETBLOBS / 2));
                    }
                    for (int k = Info.PACKETBLOBS / 2 + 1;
                         k < Info.PACKETBLOBS - 1; k++) {
                        fp_i[k] =
                                Floor1.floor1_interpolate_fit(vb, flr_floorsubmap_submap,
                                        fp_i[Info.PACKETBLOBS / 2],
                                        fp_i[Info.PACKETBLOBS - 1],
                                        ((k - Info.PACKETBLOBS / 2) << 16) /
                                                (Info.PACKETBLOBS / 2));
                    }
                }
            }
        }
        vbi.ampmax = global_ampmax;

		/*
		the next phases are performed once for vbr-only and PACKETBLOB
		times for bitrate managed modes.

		1) encode actual mode being used
		2) encode the floor for each channel, compute coded mask curve/res
		3) normalize and couple.
		4) encode residue
		5) save packet bytes to the packetblob vector

		*/

        /* iterate over the many masking curve fits we've created */

        {
            final int[] sliding_lowpass_modenumber = ci.psy_g_param.sliding_lowpass[modenumber];// java
            final int[] residuesubmap = info.residuesubmap;
            final LookResidue[] b_residue = b.residue;// java
            final int[] residue_type = ci.residue_type;// java
            final FuncResidue[] residue_p = Codec._residue_P;// java

            final int[][] couple_bundle = new int[channels][];
            final boolean[] zerobundle = new boolean[channels];

            for (int k = (vb.bitrate_managed() ?
                    0 : Info.PACKETBLOBS / 2);
                 k <= (
                         vb.bitrate_managed() ?
                                 Info.PACKETBLOBS - 1 :
                                 Info.PACKETBLOBS / 2
                 );
                 k++) {
                final Buffer opb = vbi.packetblob[k];

                /* start out our new packet blob with packet type and mode */
                /* Encode the packet type */
                opb.pack_write(0, 1);
                /* Encode the modenumber */
                /* Encode frame mode, pre,post windowsize, then dispatch */
                opb.pack_write(modenumber, b.modebits);
                if (modenumber != 0) {
                    opb.pack_write(vb.lW, 1);
                    opb.pack_write(vb.nW, 1);
                }

                /* encode floor, compute masking curve, sep out residue */
                for (int i = 0; i < channels; i++) {
                    // final int submap = chmuxlist[i];
                    final int[] ilogmask = iwork[i];

                    nonzero[i] = Floor1.floor1_encode(opb, vb,
                            (LookFloor1) flr[floorsubmap[chmuxlist[i]]],
                            floor_posts[i][k],
                            ilogmask);
/*if( false ) {// #if 0
					{
						final float[] work = new float[n2];
						String buff = String.format("maskI%c%d", i != 0 ?'R':'L',  k);
						for( j = 0; j < n / 2; j++ )
							work[j] = FLOOR1_fromdB_LOOKUP[ iwork[i][j] ];
						Analysis._analysis_output( buff, seq, work, 0, n2, true, true, 0 );
					}
}// #endif*/
                }

				/* our iteration is now based on masking curve, not prequant and
				 coupling.  Only one prequant/coupling step */

                /* quantize/couple */
				/* incomplete implementation that assumes the tree is all depth
				 one, or no tree at all */
                psy_look._couple_quantize_normalize(k, ci.psy_g_param,
                        info, gmdct, iwork, nonzero,
                        sliding_lowpass_modenumber[k],
                        channels);

/*if( false ) {// #if 0
				final float[] work = new float[n2];
				String buff = String.format("res%c%d", i != 0 ?'R':'L',  k);
				for( i = 0; i < vi.channels; i++ ) {
					for( j = 0; j < n / 2; j++ )
						work[j] = iwork[i][j];
					Analysis._analysis_output( buff, seq, work, 0, n2, true, false, 0 );
				}
}// #endif*/

                /* classify and encode by submap */
                for (int i = 0, ie = info.submaps; i < ie; i++) {
                    int ch_in_bundle = 0;

                    for (int j = 0; j < channels; j++) {
                        if (chmuxlist[j] == i) {
							/*zerobundle[ch_in_bundle] = 0;
							if( nonzero[j] ) zerobundle[ch_in_bundle] = 1;*/
                            zerobundle[ch_in_bundle] = nonzero[j];
                            couple_bundle[ch_in_bundle++] = iwork[j];
                        }
                    }
                    // FIXME why no checking for null? see residue0_exportbundle
                    final int resnum = residuesubmap[i];
                    final LookResidue b_residue_resnum = b_residue[resnum];// java
                    final FuncResidue residue = residue_p[residue_type[resnum]];// java
                    final int[][] classifications = residue.
                            fclass(vb, b_residue_resnum, couple_bundle, zerobundle, ch_in_bundle);

                    ch_in_bundle = 0;
                    for (int j = 0; j < channels; j++) {
                        if (chmuxlist[j] == i) {
                            couple_bundle[ch_in_bundle++] = iwork[j];
                        }
                    }

                    // FIXME why no checking for null? see residue0_exportbundle
                    residue.forward(opb, vb, b_residue_resnum,
                            couple_bundle, zerobundle, ch_in_bundle, classifications, i);
                }

                /* ok, done encoding.  Next protopacket. */
            }

        }

/*if( false ) {// #if 0
		seq++;
		total += (ci.blocksizes[vb.W] + ci.blocksizes[vb.nW]) >>> 2;
}// #endif*/
        return (0);
    }

    @Override
    // static int mapping0_inverse(vorbis_block *vb, InfoMapping *l)
    final int inverse(final Block vb, final InfoMapping l) {
        final DspState vd = vb.vd;
        final Info vi = vd.vi;
        final CodecSetupInfo ci = vi.codec_setup;
        final PrivateState b = vd.backend_state;
        final InfoMapping0 info = (InfoMapping0) l;

        final int n = vb.pcmend = ci.blocksizes[vb.W];
        final int n2 = n >>> 1;
        final int channels = vi.channels;// java

        final float[][] pcmbundle = new float[channels][];
        final boolean[] zerobundle = new boolean[channels];

        final boolean[] nonzero = new boolean[channels];
        final Object[] floormemo = new Object[channels];

        final float[][] vb_pcm = vb.pcm;// java
        final LookFloor[] b_flr = vd.backend_state.flr;// java
        final int[] floorsubmap = info.floorsubmap;// java
        final int[] chmuxlist = info.chmuxlist;// java
        final int[] floor_type = ci.floor_type;// java
        final FuncFloor[] floor_p = Codec._floor_P;// java

        /* recover the spectral envelope; store it in the PCM vector for now */
        for (int i = 0; i < channels; i++) {
            // final int submap = info.chmuxlist[i];
            final int submap = floorsubmap[chmuxlist[i]];// java
            floormemo[i] = floor_p[floor_type[submap]].
                    inverse1(vb, b_flr[submap]);

            nonzero[i] = (floormemo[i] != null);
            Arrays.fill(vb_pcm[i], 0, n2, 0);
        }

        final int[] coupling_mag = info.coupling_mag;// java
        final int[] coupling_ang = info.coupling_ang;// java
        /* channel coupling can 'dirty' the nonzero listing */
        for (int i = 0, ie = info.coupling_steps; i < ie; i++) {
            if (nonzero[coupling_mag[i]] ||
                    nonzero[coupling_ang[i]]) {
                nonzero[coupling_mag[i]] = true;
                nonzero[coupling_ang[i]] = true;
            }
        }

        /* recover the residue into our working vectors */
        final int[] residuesubmap = info.residuesubmap;// java
        final int[] residue_type = ci.residue_type;// java
        final LookResidue[] b_residue = b.residue;// java
        final FuncResidue[] residue_p = Codec._residue_P;// java
        for (int i = 0, ie = info.submaps; i < ie; i++) {
            int ch_in_bundle = 0;
            for (int j = 0; j < channels; j++) {
                if (chmuxlist[j] == i) {
                    zerobundle[ch_in_bundle] = nonzero[j];
                    pcmbundle[ch_in_bundle++] = vb_pcm[j];
                }
            }

            final int residue = residuesubmap[i];// java
            residue_p[residue_type[residue]].
                    inverse(vb, b_residue[residue],
                            pcmbundle, zerobundle, ch_in_bundle);
        }

        /* channel coupling */
        for (int i = info.coupling_steps - 1; i >= 0; i--) {
            final float[] pcmM = vb_pcm[coupling_mag[i]];
            final float[] pcmA = vb_pcm[coupling_ang[i]];

            for (int j = 0; j < n2; j++) {
                final float mag = pcmM[j];
                final float ang = pcmA[j];

                if (mag > 0) {
                    if (ang > 0) {
                        pcmM[j] = mag;
                        pcmA[j] = mag - ang;
                    } else {
                        pcmA[j] = mag;
                        pcmM[j] = mag + ang;
                    }
                } else if (ang > 0) {
                    pcmM[j] = mag;
                    pcmA[j] = mag + ang;
                } else {
                    pcmA[j] = mag;
                    pcmM[j] = mag - ang;
                }
            }
        }

        /* compute and apply spectral envelope */
        for (int i = 0; i < channels; i++) {
            final int submap = floorsubmap[chmuxlist[i]];
            floor_p[floor_type[submap]].
                    inverse2(vb, b_flr[submap], floormemo[i], vb_pcm[i]);
        }

        /* transform the PCM data; takes PCM vector, vb; modifies PCM vector */
        /* only MDCT right now.... */
        final MDCTLookup transform = b.transform[vb.W][0];// java
        for (int i = 0; i < channels; i++) {
            final float[] pcm = vb_pcm[i];
            transform.backward(pcm, pcm);
        }

        /* all done! */
        return (0);
    }
}
