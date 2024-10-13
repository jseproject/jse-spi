/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 2019 Alexey Kuznetsov
 * Copyright (c) 2002-2018 Xiph.Org Foundation
 * Copyright (c) 1994-1996 James Gosling,
 *                         Kevin A. Smith, Sun Microsystems, Inc.
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

/**
 * unnormalized* fft transform
 */

/** FFT implementation from OggSquish, minus cosine transforms,
 * minus all but radix 2/4 case.  In Vorbis we only need this
 * cut-down version.
 *
 * To do more than just power-of-two sized vectors, see the full
 * version I wrote for NetLib.
 *
 * Note that the packing is a little strange; rather than the FFT r/i
 * packing following R_0, I_n, R_1, I_1, R_2, I_2 ... R_n-1, I_n-1,
 * it follows R_0, R_1, I_1, R_2, I_2 ... R_n-1, I_n-1, I_n like the
 * FORTRAN version
 */
class SmallFT {
    private static final int ntryh[] = {4, 2, 3, 5};
    private static final float tpi = 6.28318530717958648f;
    private static final int LOOP_DONE = 0;

    //
    private static void drfti1(final int n, final float[] wa, final int[] ifac) {
        int ntry = 0, j = -1;
        int nl = n;
        int nf = 0;

        int state = 101;// L101
        while (state != LOOP_DONE) {
            switch (state) {
                case 101:// L101
                    j++;
                    if (j < 4) {
                        ntry = ntryh[j];
                    } else {
                        ntry += 2;
                    }

                case 104:// L104
                    final int nq = nl / ntry;
                    final int nr = nl - ntry * nq;
                    if (nr != 0) {
                        state = 101;
                        break;
                    }// goto L101;

                    nf++;
                    ifac[nf + 1] = ntry;
                    nl = nq;
                    //if( ntry != 2 ) // goto L107;
                    //if( nf == 1 ) // goto L107;
                    if (ntry == 2 && nf != 1) {
                        for (int i = 1; i < nf; i++) {
                            final int ib = nf - i + 1;
                            ifac[ib + 1] = ifac[ib];
                        }
                        ifac[2] = 2;
                    }

                    //case 107:// L107
                    if (nl != 1) {
                        state = 104;
                        break;
                    }// goto L104;
                    ifac[0] = n;
                    ifac[1] = nf;
                    final float argh = tpi / n;
                    int is = n;// + 0;// fix for wa = wa + n
                    final int nfm1 = nf - 1;
                    int l1 = 1;

                    if (nfm1 == 0) {
                        return;
                    }

                    for (int k1 = 0; k1 < nfm1; k1++) {
                        final int ip = ifac[k1 + 2];
                        int ld = 0;
                        final int l2 = l1 * ip;
                        final int ido = n / l2;
                        final int ipm = ip - 1;

                        for (j = 0; j < ipm; j++) {
                            ld += l1;
                            int i = is;
                            final float argld = (float) ld * argh;
                            float fi = 0.f;
                            for (int ii = 2; ii < ido; ii += 2) {
                                fi += 1.f;
                                final double arg = (double) (fi * argld);
                                wa[i++] = (float) Math.cos(arg);
                                wa[i++] = (float) Math.sin(arg);
                            }
                            is += ido;
                        }
                        l1 = l2;
                    }
                    state = LOOP_DONE;// return
                    break;
            }
        }
    }

    private static void fdrffti(final int n, final float[] wsave, final int[] ifac) {
        if (n == 1) {
            return;
        }
        drfti1(n, wsave, ifac);// (wsave + n) fixed in drfti1
    }

    private static void dradf2(final int ido, final int l1, final float[] cc, final float[] ch,
                               final float[] wa1, int wa1_offset) {
        int t1 = 0;
        int t2;
        final int t0 = (t2 = l1 * ido);
        int t3 = ido << 1;
        for (int k = 0; k < l1; k++) {
            ch[t1 << 1] = cc[t1] + cc[t2];
            ch[(t1 << 1) + t3 - 1] = cc[t1] - cc[t2];
            t1 += ido;
            t2 += ido;
        }

        if (ido < 2) {
            return;
        }
        if (ido != 2) {
            wa1_offset--;// changed wa1[i - 2] to wa1[i - 1], wa1[i - 1] to wa1[i]
            final int end = ido + wa1_offset;// use offset to calculate end
            t1 = 0;
            t2 = t0;
            for (int k = 0; k < l1; k++) {
                t3 = t2;
                int t4 = (t1 << 1) + (ido << 1);
                int t5 = t1;
                int t6 = t1 + t1;
                for (int i = wa1_offset + 2; i < end; i += 2) {
                    t3 += 2;
                    t4 -= 2;
                    t5 += 2;
                    t6 += 2;
                    final float tr2 = wa1[i - 1] * cc[t3 - 1] + wa1[i] * cc[t3];
                    final float ti2 = wa1[i - 1] * cc[t3] - wa1[i] * cc[t3 - 1];
                    ch[t6] = cc[t5] + ti2;
                    ch[t4] = ti2 - cc[t5];
                    ch[t6 - 1] = cc[t5 - 1] + tr2;
                    ch[t4 - 1] = cc[t5 - 1] - tr2;
                }
                t1 += ido;
                t2 += ido;
            }

            if ((ido & 1) != 0) {
                return;
            }

        }
        t3 = (t2 = (t1 = ido) - 1);
        t2 += t0;
        for (int k = 0; k < l1; k++) {
            ch[t1] = -cc[t2];
            ch[t1 - 1] = cc[t3];
            t1 += ido << 1;
            t2 += ido;
            t3 += ido;
        }
    }

    private static final float hsqt2 = .70710678118654752f;

    private static void dradf4(final int ido, final int l1, final float[] cc, final float[] ch,
                               final float[] wa, int wa1, int wa2, int wa3) {
        // added: wa; wa1, w2, wa3 are changed from float[] to integer offset

        final int t0 = l1 * ido;

        int t1 = t0;
        int t4 = t1 << 1;
        int t2 = t1 + (t1 << 1);
        int t3 = 0;

        int t5, t6;
        for (int k = 0; k < l1; k++) {
            final float tr1 = cc[t1] + cc[t2];
            final float tr2 = cc[t3] + cc[t4];

            ch[t5 = t3 << 2] = tr1 + tr2;
            ch[(ido << 2) + t5 - 1] = tr2 - tr1;
            ch[(t5 += (ido << 1)) - 1] = cc[t3] - cc[t4];
            ch[t5] = cc[t2] - cc[t1];

            t1 += ido;
            t2 += ido;
            t3 += ido;
            t4 += ido;
        }

        if (ido < 2) {
            return;
        }
        if (ido != 2) {
            wa1--;// changed wa1[i - 2] to wa1[i - 1], wa1[i - 1] to wa1[i]
            wa2--;// changed wa2[i - 2] to wa2[i - 1], wa2[i - 1] to wa2[i]
            wa3--;// changed wa3[i - 2] to wa3[i - 1], wa3[i - 1] to wa3[i]
            t1 = 0;
            for (int k = 0; k < l1; k++) {
                t2 = t1;
                t4 = t1 << 2;
                t5 = (t6 = ido << 1) + t4;
                for (int i = 2; i < ido; i += 2) {
                    t3 = (t2 += 2);
                    t4 += 2;
                    t5 -= 2;

                    t3 += t0;
                    final float cr2 = wa[wa1 + i - 1] * cc[t3 - 1] + wa[wa1 + i] * cc[t3];
                    final float ci2 = wa[wa1 + i - 1] * cc[t3] - wa[wa1 + i] * cc[t3 - 1];
                    t3 += t0;
                    final float cr3 = wa[wa2 + i - 1] * cc[t3 - 1] + wa[wa2 + i] * cc[t3];
                    final float ci3 = wa[wa2 + i - 1] * cc[t3] - wa[wa2 + i] * cc[t3 - 1];
                    t3 += t0;
                    final float cr4 = wa[wa3 + i - 1] * cc[t3 - 1] + wa[wa3 + i] * cc[t3];
                    final float ci4 = wa[wa3 + i - 1] * cc[t3] - wa[wa3 + i] * cc[t3 - 1];

                    final float tr1 = cr2 + cr4;
                    final float tr4 = cr4 - cr2;
                    final float ti1 = ci2 + ci4;
                    final float ti4 = ci2 - ci4;

                    final float ti2 = cc[t2] + ci3;
                    final float ti3 = cc[t2] - ci3;
                    final float tr2 = cc[t2 - 1] + cr3;
                    final float tr3 = cc[t2 - 1] - cr3;

                    ch[t4 - 1] = tr1 + tr2;
                    ch[t4] = ti1 + ti2;

                    ch[t5 - 1] = tr3 - ti4;
                    ch[t5] = tr4 - ti3;

                    ch[t4 + t6 - 1] = ti4 + tr3;
                    ch[t4 + t6] = tr4 + ti3;

                    ch[t5 + t6 - 1] = tr2 - tr1;
                    ch[t5 + t6] = ti1 - ti2;
                }
                t1 += ido;
            }
            if ((ido & 1) != 0) {
                return;
            }
        }

        t2 = (t1 = t0 + ido - 1) + (t0 << 1);
        t3 = ido << 2;
        t4 = ido;
        t5 = ido << 1;
        t6 = ido;

        for (int k = 0; k < l1; k++) {
            final float ti1 = -hsqt2 * (cc[t1] + cc[t2]);
            final float tr1 = hsqt2 * (cc[t1] - cc[t2]);

            ch[t4 - 1] = tr1 + cc[t6 - 1];
            ch[t4 + t5 - 1] = cc[t6 - 1] - tr1;

            ch[t4] = ti1 - cc[t1 + t0];
            ch[t4 + t5] = ti1 + cc[t1 + t0];

            t1 += ido;
            t2 += ido;
            t4 += t3;
            t6 += ido;
        }
    }

    private static void dradfg(final int ido, final int ip, final int l1, final int idl1, final float[] cc,
                               final float[] c1, final float[] c2, final float[] ch, final float[] ch2,
                               final float[] wa, final int wa_offset) {
        // wa_offset added to fix wa
        final float arg = tpi / (float) ip;
        final float dcp = (float) Math.cos((double) arg);
        final float dsp = (float) Math.sin((double) arg);
        final int ipph = (ip + 1) >> 1;
        final int ipp2 = ip;
        final int idp2 = ido;
        final int nbd = (ido - 1) >> 1;
        final int t0 = l1 * ido;
        final int t10 = ip * ido;

        if (ido != 1) {//goto L119;
            for (int ik = 0; ik < idl1; ik++) {
                ch2[ik] = c2[ik];
            }

            int t1 = 0;
            for (int j = 1; j < ip; j++) {
                t1 += t0;
                int t2 = t1;
                for (int k = 0; k < l1; k++) {
                    ch[t2] = c1[t2];
                    t2 += ido;
                }
            }

            int is = -ido + wa_offset - 1;// fix for wa, added - 1
            t1 = 0;
            if (nbd > l1) {
                for (int j = 1; j < ip; j++) {
                    t1 += t0;
                    is += ido;
                    int t2 = -ido + t1;
                    for (int k = 0; k < l1; k++) {
                        int idij = is;// - 1;
                        t2 += ido;
                        int t3 = t2;
                        for (int i = 2; i < ido; i += 2) {
                            idij += 2;
                            t3 += 2;
                            ch[t3 - 1] = wa[idij - 1] * c1[t3 - 1] + wa[idij] * c1[t3];
                            ch[t3] = wa[idij - 1] * c1[t3] - wa[idij] * c1[t3 - 1];
                        }
                    }
                }
            } else {

                for (int j = 1; j < ip; j++) {
                    is += ido;
                    int idij = is;// - 1;
                    t1 += t0;
                    int t2 = t1;
                    for (int i = 2; i < ido; i += 2) {
                        idij += 2;
                        t2 += 2;
                        int t3 = t2;
                        for (int k = 0; k < l1; k++) {
                            ch[t3 - 1] = wa[idij - 1] * c1[t3 - 1] + wa[idij] * c1[t3];
                            ch[t3] = wa[idij - 1] * c1[t3] - wa[idij] * c1[t3 - 1];
                            t3 += ido;
                        }
                    }
                }
            }

            t1 = 0;
            int t2 = ipp2 * t0;
            if (nbd < l1) {
                for (int j = 1; j < ipph; j++) {
                    t1 += t0;
                    t2 -= t0;
                    int t3 = t1;
                    int t4 = t2;
                    for (int i = 2; i < ido; i += 2) {
                        t3 += 2;
                        t4 += 2;
                        int t5 = t3 - ido;
                        int t6 = t4 - ido;
                        for (int k = 0; k < l1; k++) {
                            t5 += ido;
                            t6 += ido;
                            c1[t5 - 1] = ch[t5 - 1] + ch[t6 - 1];
                            c1[t6 - 1] = ch[t5] - ch[t6];
                            c1[t5] = ch[t5] + ch[t6];
                            c1[t6] = ch[t6 - 1] - ch[t5 - 1];
                        }
                    }
                }
            } else {
                for (int j = 1; j < ipph; j++) {
                    t1 += t0;
                    t2 -= t0;
                    int t3 = t1;
                    int t4 = t2;
                    for (int k = 0; k < l1; k++) {
                        int t5 = t3;
                        int t6 = t4;
                        for (int i = 2; i < ido; i += 2) {
                            t5 += 2;
                            t6 += 2;
                            c1[t5 - 1] = ch[t5 - 1] + ch[t6 - 1];
                            c1[t6 - 1] = ch[t5] - ch[t6];
                            c1[t5] = ch[t5] + ch[t6];
                            c1[t6] = ch[t6 - 1] - ch[t5 - 1];
                        }
                        t3 += ido;
                        t4 += ido;
                    }
                }
            }
        }
//L119:
        for (int ik = 0; ik < idl1; ik++) {
            c2[ik] = ch2[ik];
        }

        int t1 = 0;
        int t2 = ipp2 * idl1;
        for (int j = 1; j < ipph; j++) {
            t1 += t0;
            t2 -= t0;
            int t3 = t1 - ido;
            int t4 = t2 - ido;
            for (int k = 0; k < l1; k++) {
                t3 += ido;
                t4 += ido;
                c1[t3] = ch[t3] + ch[t4];
                c1[t4] = ch[t4] - ch[t3];
            }
        }

        float ar1 = 1.f;
        float ai1 = 0.f;
        t1 = 0;
        t2 = ipp2 * idl1;
        int t3 = (ip - 1) * idl1;
        for (int l = 1; l < ipph; l++) {
            t1 += idl1;
            t2 -= idl1;
            final float ar1h = dcp * ar1 - dsp * ai1;
            ai1 = dcp * ai1 + dsp * ar1;
            ar1 = ar1h;
            int t4 = t1;
            int t5 = t2;
            int t6 = t3;
            int t7 = idl1;

            for (int ik = 0; ik < idl1; ik++) {
                ch2[t4++] = c2[ik] + ar1 * c2[t7++];
                ch2[t5++] = ai1 * c2[t6++];
            }

            final float dc2 = ar1;
            final float ds2 = ai1;
            float ar2 = ar1;
            float ai2 = ai1;

            t4 = idl1;
            t5 = (ipp2 - 1) * idl1;
            for (int j = 2; j < ipph; j++) {
                t4 += idl1;
                t5 -= idl1;

                final float ar2h = dc2 * ar2 - ds2 * ai2;
                ai2 = dc2 * ai2 + ds2 * ar2;
                ar2 = ar2h;

                t6 = t1;
                t7 = t2;
                int t8 = t4;
                int t9 = t5;
                for (int ik = 0; ik < idl1; ik++) {
                    ch2[t6++] += ar2 * c2[t8++];
                    ch2[t7++] += ai2 * c2[t9++];
                }
            }
        }

        t1 = 0;
        for (int j = 1; j < ipph; j++) {
            t1 += idl1;
            t2 = t1;
            for (int ik = 0; ik < idl1; ik++) {
                ch2[ik] += c2[t2++];
            }
        }

        if (ido < l1) {// goto L132;
// L132:
            for (int i = 0; i < ido; i++) {
                t1 = i;
                t2 = i;
                for (int k = 0; k < l1; k++) {
                    cc[t2] = ch[t1];
                    t1 += ido;
                    t2 += t10;
                }
            }
        } else {
            t1 = 0;
            t2 = 0;
            for (int k = 0; k < l1; k++) {
                t3 = t1;
                int t4 = t2;
                for (int i = 0; i < ido; i++) {
                    cc[t4++] = ch[t3++];
                }
                t1 += ido;
                t2 += t10;
            }

            //goto L135;
        }

//L135:
        t1 = 0;
        t2 = ido << 1;
        t3 = 0;
        int t4 = ipp2 * t0;
        for (int j = 1; j < ipph; j++) {

            t1 += t2;
            t3 += t0;
            t4 -= t0;

            int t5 = t1;
            int t6 = t3;
            int t7 = t4;

            for (int k = 0; k < l1; k++) {
                cc[t5 - 1] = ch[t6];
                cc[t5] = ch[t7];
                t5 += t10;
                t6 += ido;
                t7 += ido;
            }
        }

        if (ido == 1) {
            return;
        }
        if (nbd < l1) {//goto L141;
//L141:
            t1 = -ido;
            t3 = 0;
            t4 = 0;
            int t5 = ipp2 * t0;
            for (int j = 1; j < ipph; j++) {
                t1 += t2;
                t3 += t2;
                t4 += t0;
                t5 -= t0;
                for (int i = 2; i < ido; i += 2) {
                    int t6 = idp2 + t1 - i;
                    int t7 = i + t3;
                    int t8 = i + t4;
                    int t9 = i + t5;
                    for (int k = 0; k < l1; k++) {
                        cc[t7 - 1] = ch[t8 - 1] + ch[t9 - 1];
                        cc[t6 - 1] = ch[t8 - 1] - ch[t9 - 1];
                        cc[t7] = ch[t8] + ch[t9];
                        cc[t6] = ch[t9] - ch[t8];
                        t6 += t10;
                        t7 += t10;
                        t8 += ido;
                        t9 += ido;
                    }
                }
            }
        } else {
            t1 = -ido;
            t3 = 0;
            t4 = 0;
            int t5 = ipp2 * t0;
            for (int j = 1; j < ipph; j++) {
                t1 += t2;
                t3 += t2;
                t4 += t0;
                t5 -= t0;
                int t6 = t1;
                int t7 = t3;
                int t8 = t4;
                int t9 = t5;
                for (int k = 0; k < l1; k++) {
                    for (int i = 2; i < ido; i += 2) {
                        final int ic = idp2 - i;
                        cc[i + t7 - 1] = ch[i + t8 - 1] + ch[i + t9 - 1];
                        cc[ic + t6 - 1] = ch[i + t8 - 1] - ch[i + t9 - 1];
                        cc[i + t7] = ch[i + t8] + ch[i + t9];
                        cc[ic + t6] = ch[i + t9] - ch[i + t8];
                    }
                    t6 += t10;
                    t7 += t10;
                    t8 += ido;
                    t9 += ido;
                }
            }
        }
    }

    private static void drftf1(final int n, final float[] c, final float[] ch, final int[] ifac) {
        // float[] wa removed

        final float[] wa = ch;// fix wa = ch + n
        final int wa_offset = n - 1;// fix wa = ch + n
        final int nf = ifac[1];
        int na = 1;
        int l2 = n;
        int iw = n;

        for (int k1 = 0; k1 < nf; k1++) {
            final int kh = nf - k1;
            final int ip = ifac[kh + 1];
            final int l1 = l2 / ip;
            final int ido = n / l2;
            final int idl1 = ido * l1;
            iw -= (ip - 1) * ido;
            na = 1 - na;

            if (ip != 4) {// goto L102;
// L102:
                if (ip != 2) {//goto L104;
// L104:
                    if (ido == 1) {
                        na = 1 - na;
                    }
                    if (na != 0) {// goto L109;
// L109:
                        dradfg(ido, ip, l1, idl1, ch, ch, ch, c, c, wa, wa_offset + iw);// fix for wa+iw-1
                        na = 0;
                    } else {
                        dradfg(ido, ip, l1, idl1, c, c, c, ch, ch, wa, wa_offset + iw);// fix for wa+iw-1
                        na = 1;
                    }// goto L110;
                } else {
                    if (na != 0) {// goto L103;
// L103:
                        dradf2(ido, l1, ch, c, wa, wa_offset + iw);// fix for wa+iw-1
                    } else {
                        dradf2(ido, l1, c, ch, wa, wa_offset + iw);// fix for wa+iw-1
                    }
                }
            } else {
                final int ix2 = iw + ido;
                final int ix3 = ix2 + ido;
                if (na != 0) {
                    dradf4(ido, l1, ch, c, wa, wa_offset + iw, wa_offset + ix2, wa_offset + ix3);// fix for wa+iw-1,wa+ix2-1,wa+ix3-1
                } else {
                    dradf4(ido, l1, c, ch, wa, wa_offset + iw, wa_offset + ix2, wa_offset + ix3);// fix for wa+iw-1,wa+ix2-1,wa+ix3-1
                }
            }
// L110:
            l2 = l1;
        }

        if (na == 1) {
            return;
        }

        for (int i = 0; i < n; i++) {
            c[i] = ch[i];
        }
    }

    /*
        private static void dradb2(int ido, int l1, float[] cc, float[] ch,
                        float[] wa1, int wa1_offset) {// wa1_offset added to fix wa1
            int i, k, t0, t1, t2, t3, t4, t5, t6;
            float ti2, tr2;

            wa1_offset--;// change wa1[i - 2] to wa1[i - 1], wa1[i - 1] to wa1[i]

            t0 = l1 * ido;

            t1 = 0;
            t2 = 0;
            t3 = (ido << 1) - 1;
            for( k = 0; k < l1; k++ ) {
                ch[t1] = cc[t2] + cc[t3 + t2];
                ch[t1 + t0] = cc[t2] - cc[t3 + t2];
                t2 = (t1 += ido) << 1;
            }

            if( ido < 2 ) return;
            if( ido != 2 ) {//goto L105;

                t1 = 0;
                t2 = 0;
                for( k = 0; k < l1; k++ ) {
                    t3 = t1;
                    t5 = (t4 = t2) + (ido << 1);
                    t6 = t0 + t1;
                    for( i = 2; i < ido; i += 2 ) {
                        t3 += 2;
                        t4 += 2;
                        t5 -= 2;
                        t6 += 2;
                        ch[t3 - 1] = cc[t4 - 1] + cc[t5 - 1];
                        tr2 = cc[t4 - 1] - cc[t5 - 1];
                        ch[t3] = cc[t4] - cc[t5];
                        ti2 = cc[t4] + cc[t5];
                        ch[t6 - 1] = wa1[wa1_offset + i - 1] * tr2 - wa1[wa1_offset + i] * ti2;// fix for wa1
                        ch[t6] = wa1[wa1_offset + i - 1] * ti2 + wa1[wa1_offset + i] * tr2;// fix for wa1
                    }
                    t2 = (t1 += ido) << 1;
                }

                if( (ido & 1) != 0 )return;
            }

    //L105:
            t1 = ido - 1;
            t2 = ido - 1;
            for( k = 0; k < l1; k++ ) {
                ch[t1] = cc[t2] + cc[t2];
                ch[t1 + t0] = -(cc[t2 + 1] + cc[t2 + 1]);
                t1 += ido;
                t2 += ido << 1;
            }
        }
        private static final float taur = -.5f;
        private static final float taui = .8660254037844386f;
        private static void dradb3(int ido, int l1, float[] cc, float[] ch,
            float[] wa, int wa1, int wa2) {
            // added wa. float[] wa1 and wa2 changed to integer offsets to wa
            int i, k, t0, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10;
            float ci2, ci3, di2, di3, cr2, cr3, dr2, dr3, ti2, tr2;
            t0 = l1 * ido;

            t1 = 0;
            t2 = t0 << 1;
            t3 = ido << 1;
            t4 = ido + (ido << 1);
            t5 = 0;
            for( k = 0; k < l1; k++ ) {
                tr2 = cc[t3 - 1] + cc[t3 - 1];
                cr2 = cc[t5] + (taur * tr2);
                ch[t1] = cc[t5] + tr2;
                ci3 = taui * (cc[t3] + cc[t3]);
                ch[t1 + t0] = cr2 - ci3;
                ch[t1 + t2] = cr2 + ci3;
                t1 += ido;
                t3 += t4;
                t5 += t4;
            }

            if( ido == 1 ) return;
            wa1--;// change wa1[i - 2] to wa1[i - 1], wa1[i - 1] to wa1[i]
            wa2--;// change wa2[i - 2] to wa2[i - 1], wa2[i - 1] to wa2[i]

            t1 = 0;
            t3 = ido << 1;
            for( k = 0; k < l1; k++ ) {
                t7 = t1 + (t1 << 1);
                t6 = (t5 = t7 + t3);
                t8 = t1;
                t10 = (t9 = t1 + t0) + t0;

                for( i = 2; i < ido; i += 2 ) {
                    t5 += 2;
                    t6 -= 2;
                    t7 += 2;
                    t8 += 2;
                    t9 += 2;
                    t10 += 2;
                    tr2 = cc[t5 - 1] + cc[t6 - 1];
                    cr2 = cc[t7 - 1] + (taur * tr2);
                    ch[t8 - 1] = cc[t7 - 1] + tr2;
                    ti2 = cc[t5] - cc[t6];
                    ci2 = cc[t7] + (taur * ti2);
                    ch[t8] = cc[t7] + ti2;
                    cr3 = taui * (cc[t5 - 1] - cc[t6 - 1]);
                    ci3 = taui * (cc[t5] + cc[t6]);
                    dr2 = cr2 - ci3;
                    dr3 = cr2 + ci3;
                    di2 = ci2 + cr3;
                    di3 = ci2 - cr3;
                    ch[t9 - 1] = wa[wa1 + i - 1] * dr2 - wa[wa1 + i] * di2;// fix wa
                    ch[t9] = wa[wa1 + i - 1] * di2 + wa[wa1 + i] * dr2;// fix wa
                    ch[t10 - 1] = wa[wa2 + i - 1] * dr3 - wa[wa2 + i] * di3;// fix wa
                    ch[t10] = wa[wa2 + i - 1] * di3 + wa[wa2 + i] * dr3;// fix wa
                }
                t1 += ido;
            }
        }

        private static final float sqrt2 = 1.414213562373095f;

        private static void dradb4(int ido, int l1, float[] cc, float[] ch,
            float[] wa, int wa1, int wa2, int wa3) {
            // added wa. wa1, wa2, wa3 changed from float[] to integer offsets to wa
            int i, k, t0, t1, t2, t3, t4, t5, t6, t7, t8;
            float ci2, ci3, ci4, cr2, cr3, cr4, ti1, ti2, ti3, ti4, tr1, tr2, tr3, tr4;
            t0 = l1 * ido;

            t1 = 0;
            t2 = ido << 2;
            t3 = 0;
            t6 = ido << 1;
            for( k = 0; k < l1; k++ ) {
                t4 = t3 + t6;
                t5 = t1;
                tr3 = cc[t4 - 1] + cc[t4 - 1];
                tr4 = cc[t4] + cc[t4];
                tr1 = cc[t3] - cc[(t4 += t6) - 1];
                tr2 = cc[t3] + cc[t4 - 1];
                ch[t5] = tr2 + tr3;
                ch[t5 += t0] = tr1 - tr4;
                ch[t5 += t0] = tr2 - tr3;
                ch[t5 += t0] = tr1 + tr4;
                t1 += ido;
                t3 += t2;
            }

            if( ido < 2 ) return;
            if( ido != 2 ) {// goto L105;
                wa1--;// change wa1[i - 2] to wa1[i - 1], wa1[i - 1] to wa1[i]
                wa2--;// change wa2[i - 2] to wa2[i - 1], wa2[i - 1] to wa2[i]
                wa3--;// change wa3[i - 2] to wa3[i - 1], wa3[i - 1] to wa3[i]

                t1 = 0;
                for( k = 0; k < l1; k++ ) {
                    t5 = (t4 = (t3 = (t2 = t1 << 2) + t6)) + t6;
                    t7 = t1;
                    for( i = 2; i < ido; i += 2 ) {
                        t2 += 2;
                        t3 += 2;
                        t4 -= 2;
                        t5 -= 2;
                        t7 += 2;
                        ti1 = cc[t2] + cc[t5];
                        ti2 = cc[t2] - cc[t5];
                        ti3 = cc[t3] - cc[t4];
                        tr4 = cc[t3] + cc[t4];
                        tr1 = cc[t2 - 1] - cc[t5 - 1];
                        tr2 = cc[t2 - 1] + cc[t5 - 1];
                        ti4 = cc[t3 - 1] - cc[t4 - 1];
                        tr3 = cc[t3 - 1] + cc[t4 - 1];
                        ch[t7 - 1] = tr2 + tr3;
                        cr3 = tr2 - tr3;
                        ch[t7] = ti2 + ti3;
                        ci3 = ti2 - ti3;
                        cr2 = tr1 - tr4;
                        cr4 = tr1 + tr4;
                        ci2 = ti1 + ti4;
                        ci4 = ti1 - ti4;

                        ch[(t8 = t7 + t0) - 1] = wa[wa1 + i - 1] * cr2 - wa[wa1 + i] * ci2;
                        ch[t8] = wa[wa1 + i - 1] * ci2 + wa[wa1 + i] * cr2;
                        ch[(t8 += t0) - 1] = wa[wa2 + i - 1] * cr3 - wa[wa2 + i] * ci3;
                        ch[t8] = wa[wa2 + i - 1] * ci3 + wa[wa2 + i] * cr3;
                        ch[(t8 += t0) - 1] = wa[wa3 + i - 1] * cr4 - wa[wa3 + i] * ci4;
                        ch[t8] = wa[wa3 + i - 1] * ci4 + wa[wa3 + i] * cr4;
                    }
                    t1 += ido;
                }

                if( (ido & 1) != 0 ) return;
            }
    //L105:

            t1 = ido;
            t2 = ido << 2;
            t3 = ido - 1;
            t4 = ido + (ido << 1);
            for( k = 0; k < l1; k++ ) {
                t5 = t3;
                ti1 = cc[t1] + cc[t4];
                ti2 = cc[t4] - cc[t1];
                tr1 = cc[t1 - 1] - cc[t4 - 1];
                tr2 = cc[t1 - 1] + cc[t4 - 1];
                ch[t5] = tr2 + tr2;
                ch[t5 += t0] = sqrt2 * (tr1 - ti1);
                ch[t5 += t0] = ti2 + ti2;
                ch[t5 += t0] = -sqrt2 * (tr1 + ti1);

                t3 += ido;
                t1 += t2;
                t4 += t2;
            }
        }

        private static void dradbg(int ido, int ip, int l1, int idl1, float[] cc,
            float[] c1, float[] c2, float[] ch, float[] ch2, float[] wa, int wa_offset) {
            // wa_offset added to fix wa
            int idij, ipph, i, j, k, l, ik, is, t0, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10,
                t11, t12;
            float dc2, ai1, ai2, ar1, ar2, ds2;
            int nbd;
            float dcp, arg, dsp, ar1h, ar2h;
            int ipp2;

            t10 = ip * ido;
            t0 = l1 * ido;
            arg = tpi / (float)ip;
            dcp = (float)Math.cos( (double)arg );
            dsp = (float)Math.sin( (double)arg );
            nbd = (ido - 1) >> 1;
            ipp2 = ip;
            ipph = (ip + 1) >> 1;
            if( ido < l1 ) {// goto L103;
    // L103:
                t1 = 0;
                for( i = 0; i < ido; i++ ) {
                    t2 = t1;
                    t3 = t1;
                    for( k = 0; k < l1; k++ ) {
                        ch[t2] = cc[t3];
                        t2 += ido;
                        t3 += t10;
                    }
                    t1++;
                }
            } else {
                t1 = 0;
                t2 = 0;
                for( k = 0; k < l1; k++ ) {
                    t3 = t1;
                    t4 = t2;
                    for( i = 0; i < ido; i++ ) {
                        ch[t3] = cc[t4];
                        t3++;
                        t4++;
                    }
                    t1 += ido;
                    t2 += t10;
                }
                //goto L106;
            }

    //L106:
            t1 = 0;
            t2 = ipp2 * t0;
            t7 = (t5 = ido << 1);
            for( j = 1; j < ipph; j++ ) {
                t1 += t0;
                t2 -= t0;
                t3 = t1;
                t4 = t2;
                t6 = t5;
                for( k = 0; k < l1; k++ ) {
                    ch[t3] = cc[t6 - 1] + cc[t6 - 1];
                    ch[t4] = cc[t6] + cc[t6];
                    t3 += ido;
                    t4 += ido;
                    t6 += t10;
                }
                t5 += t7;
            }

            if( ido != 1 ) {// if( ido == 1 ) goto L116;
                if( nbd < l1 ) {// goto L112;
    // L112:
                    t1 = 0;
                    t2 = ipp2 * t0;
                    t7 = 0;
                    for( j = 1; j < ipph; j++ ) {
                        t1 += t0;
                        t2 -= t0;
                        t3 = t1;
                        t4 = t2;
                        t7 += (ido << 1);
                        t8 = t7;
                        t9 = t7;
                        for( i = 2; i < ido; i += 2 ) {
                            t3 += 2;
                            t4 += 2;
                            t8 += 2;
                            t9 -= 2;
                            t5 = t3;
                            t6 = t4;
                            t11 = t8;
                            t12 = t9;
                            for( k = 0; k < l1; k++ ) {
                                ch[t5 - 1] = cc[t11 - 1] + cc[t12 - 1];
                                ch[t6 - 1] = cc[t11 - 1] - cc[t12 - 1];
                                ch[t5] = cc[t11] - cc[t12];
                                ch[t6] = cc[t11] + cc[t12];
                                t5 += ido;
                                t6 += ido;
                                t11 += t10;
                                t12 += t10;
                            }
                        }
                    }
                } else {
                    t1 = 0;
                    t2 = ipp2 * t0;
                    t7 = 0;
                    for( j = 1; j < ipph; j++ ) {
                        t1 += t0;
                        t2 -= t0;
                        t3 = t1;
                        t4 = t2;

                        t7 += (ido << 1);
                        t8 = t7;
                        for( k = 0; k < l1; k++ ) {
                            t5 = t3;
                            t6 = t4;
                            t9 = t8;
                            t11 = t8;
                            for( i = 2; i < ido; i += 2 ) {
                                t5 += 2;
                                t6 += 2;
                                t9 += 2;
                                t11 -= 2;
                                ch[t5 - 1] = cc[t9 - 1] + cc[t11 - 1];
                                ch[t6 - 1] = cc[t9 - 1] - cc[t11 - 1];
                                ch[t5] = cc[t9] - cc[t11];
                                ch[t6] = cc[t9] + cc[t11];
                            }
                            t3 += ido;
                            t4 += ido;
                            t8 += t10;
                        }
                    }
                    //goto L116;
                }
            }

    //L116:
            ar1 = 1.f;
            ai1 = 0.f;
            t1 = 0;
            t9 = (t2 = ipp2 * idl1);
            t3 = (ip - 1) * idl1;
            for( l = 1; l < ipph; l++ ) {
                t1 += idl1;
                t2 -= idl1;

                ar1h = dcp * ar1 - dsp * ai1;
                ai1 = dcp * ai1 + dsp * ar1;
                ar1 = ar1h;
                t4 = t1;
                t5 = t2;
                t6 = 0;
                t7 = idl1;
                t8 = t3;
                for( ik = 0; ik < idl1; ik++ ) {
                    c2[t4++] = ch2[t6++] + ar1 * ch2[t7++];
                    c2[t5++] = ai1 * ch2[t8++];
                }
                dc2 = ar1;
                ds2 = ai1;
                ar2 = ar1;
                ai2 = ai1;

                t6 = idl1;
                t7 = t9-idl1;
                for( j = 2; j < ipph; j++ ) {
                    t6 += idl1;
                    t7 -= idl1;
                    ar2h = dc2 * ar2 - ds2 * ai2;
                    ai2 = dc2 * ai2 + ds2 * ar2;
                    ar2 = ar2h;
                    t4 = t1;
                    t5 = t2;
                    t11 = t6;
                    t12 = t7;
                    for( ik = 0; ik < idl1; ik++ ) {
                        c2[t4++] += ar2 * ch2[t11++];
                        c2[t5++] += ai2 * ch2[t12++];
                    }
                }
            }

            t1 = 0;
            for( j = 1; j < ipph; j++ ) {
                t1 += idl1;
                t2 = t1;
                for( ik = 0; ik < idl1; ik++ ) ch2[ik] += ch2[t2++];
            }

            t1 = 0;
            t2 = ipp2 * t0;
            for( j = 1; j < ipph; j++ ) {
                t1 += t0;
                t2 -= t0;
                t3 = t1;
                t4 = t2;
                for( k = 0; k < l1; k++ ) {
                    ch[t3] = c1[t3] - c1[t4];
                    ch[t4] = c1[t3] + c1[t4];
                    t3 += ido;
                    t4 += ido;
                }
            }

            if( ido != 1 ) {// if( ido == 1 ) goto L132;
                if( nbd < l1 ) {// goto L128;
    // L128:
                    t1 = 0;
                    t2 = ipp2 * t0;
                    for( j = 1; j < ipph; j++ ) {
                        t1 += t0;
                        t2 -= t0;
                        t3 = t1;
                        t4 = t2;
                        for( i = 2; i < ido; i += 2 ) {
                            t3 += 2;
                            t4 += 2;
                            t5 = t3;
                            t6 = t4;
                            for( k = 0; k < l1; k++ ) {
                                ch[t5 - 1] = c1[t5 - 1] - c1[t6];
                                ch[t6 - 1] = c1[t5 - 1] + c1[t6];
                                ch[t5] = c1[t5] + c1[t6 - 1];
                                ch[t6] = c1[t5] - c1[t6 - 1];
                                t5 += ido;
                                t6 += ido;
                            }
                        }
                    }
                } else {
                    t1 = 0;
                    t2 = ipp2 * t0;
                    for( j = 1; j < ipph; j++ ) {
                        t1 += t0;
                        t2 -= t0;
                        t3 = t1;
                        t4 = t2;
                        for( k = 0; k < l1; k++ ) {
                            t5 = t3;
                            t6 = t4;
                            for( i = 2; i < ido; i += 2 ) {
                                t5 += 2;
                                t6 += 2;
                                ch[t5 - 1] = c1[t5 - 1] - c1[t6];
                                ch[t6 - 1] = c1[t5 - 1] + c1[t6];
                                ch[t5] = c1[t5] + c1[t6 - 1];
                                ch[t6] = c1[t5] - c1[t6 - 1];
                            }
                            t3 += ido;
                            t4 += ido;
                        }
                    }
                    // goto L132;
                }
            }

    //L132:
            if( ido == 1 ) return;

            for( ik = 0; ik < idl1; ik++ ) c2[ik] = ch2[ik];

            t1 = 0;
            for( j = 1; j < ip; j++ ) {
                t2 = (t1 += t0);
                for( k = 0; k < l1; k++ ) {
                    c1[t2] = ch[t2];
                    t2 += ido;
                }
            }

            if( nbd>l1) {// goto L139;
    // L139:
                is = -ido + wa_offset - 1;// fix wa
                t1 = 0;
                for( j = 1; j < ip; j++ ) {
                    is += ido;
                    t1 += t0;
                    t2 = t1;
                    for( k = 0; k < l1; k++ ) {
                        idij = is;
                        t3 = t2;
                        for( i = 2; i < ido; i += 2 ) {
                            idij += 2;
                            t3 += 2;
                            c1[t3 - 1] = wa[idij - 1] * ch[t3 - 1] - wa[idij] * ch[t3];
                            c1[t3] = wa[idij - 1] * ch[t3] + wa[idij] * ch[t3 - 1];
                        }
                        t2 += ido;
                    }
                }
                //return;
            } else {
                is = -ido + wa_offset - 1;// fix wa
                t1 = 0;
                for( j = 1; j < ip; j++ ) {
                    is += ido;
                    t1 += t0;
                    idij = is;
                    t2 = t1;
                    for( i = 2; i < ido; i += 2 ) {
                        t2 += 2;
                        idij += 2;
                        t3 = t2;
                        for( k = 0; k < l1; k++ ) {
                            c1[t3 - 1] = wa[idij - 1] * ch[t3 - 1] - wa[idij] * ch[t3];
                            c1[t3] = wa[idij - 1] * ch[t3] + wa[idij] * ch[t3 - 1];
                            t3 += ido;
                        }
                    }
                }
                //return;
            }
        }

        private static void drftb1(final int n, float[] c, float[] ch, int[] ifac) {
            final float[] wa = ch;// fix wa + n
            int i, k1, l1, l2;
            int na;
            int nf, ip, iw, ix2, ix3, ido, idl1;

            nf = ifac[1];
            na = 0;
            l1 = 1;
            iw = 1;

            for( k1 = 0; k1 < nf; k1++ ) {
                ip = ifac[k1 + 2];
                l2 = ip * l1;
                ido = n / l2;
                idl1 = ido * l1;
                if( ip != 4 ) {// goto L103;
    // L103:
                    if( ip != 2 ) {// goto L106;
    // L106:
                        if( ip != 3 ) {// goto L109;
    // L109:
        The radix five case can be translated later.....
        if( ip != 5 ) goto L112;

        ix2 = iw + ido;
        ix3 = ix2 + ido;
        ix4 = ix3 + ido;
        if( na != 0 )
          dradb5( ido, l1, ch, c, wa + iw - 1, wa + ix2 - 1, wa + ix3 - 1, wa + ix4 - 1);
        else
          dradb5( ido, l1, c, ch, wa + iw - 1, wa + ix2 - 1, wa + ix3 - 1, wa + ix4 - 1);
        na = 1 - na;
        goto L115;

      L112:
                            if( na != 0 )
                                dradbg( ido, ip, l1, idl1, ch, ch, ch, c, c, wa, n + iw - 1 );// fix for wa
                            else
                                dradbg( ido, ip, l1, idl1, c, c, c, ch, ch, wa, n + iw - 1 );// fix for wa
                            if( ido == 1 ) na = 1 - na;
                        } else {
                            ix2 = iw + ido;
                            if( na != 0 )
                                dradb3( ido, l1, ch, c, wa, n + iw - 1, n + ix2 - 1 );// fix for wa
                            else
                                dradb3( ido, l1, c, ch, wa, n + iw - 1, n + ix2 - 1 );// fix for wa
                            na = 1 - na;
                            //goto L115;
                        }
                    } else {
                        if( na != 0)
                            dradb2( ido, l1, ch, c, wa, n + iw - 1 );// fix for wa
                        else
                            dradb2( ido, l1, c, ch, wa, n + iw - 1 );// fix for wa
                        na = 1 - na;
                        //goto L115;
                    }
                } else {
                    ix2 = iw + ido;
                    ix3 = ix2 + ido;

                    if( na != 0)
                        dradb4( ido, l1, ch, c, wa, n + iw - 1, n + ix2 - 1, n + ix3 - 1 );// fix for wa
                    else
                        dradb4( ido, l1, c, ch, wa, n + iw - 1, n + ix2 - 1, n + ix3 - 1 );// fix for wa
                    na = 1 - na;
                    //goto L115;
                }
    //L115:
                l1 = l2;
                iw += (ip - 1) * ido;
            }

            if( na == 0 ) return;

            for( i = 0; i < n; i++ ) c[i] = ch[i];
        }
    */
    static void drft_forward(final DRFTLookup l, final float[] data) {
        if (l.n == 1) {
            return;
        }
        //drftf1( l.n, data, l.trigcache, l.trigcache + l.n, l.splitcache );
        drftf1(l.n, data, l.trigcache, l.splitcache);// + l.n fixed in drftf1
    }

	/* FIXME never used drft_backward, drftb1, dradb2, dradb3, dradb4, dradbg
	static void drft_backward(DRFTLookup l, float[] data) {
		if( l.n == 1 ) return;
		//drftb1( l.n, data, l.trigcache, l.trigcache + l.n, l.splitcache );
		drftb1( l.n, data, l.trigcache, l.splitcache );// + l.n fixed in drftb1
	}
	 */

    static void drft_init(final DRFTLookup l, final int n) {
        l.n = n;
        l.trigcache = new float[3 * n];
        l.splitcache = new int[32];
        fdrffti(n, l.trigcache, l.splitcache);
    }
}
