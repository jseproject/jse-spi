package org.xiph.vorbis;

/**
 * psychoacoustic setup
 */
public class InfoPsyGlobal {
    int eighth_octave_lines = 0;

    /* for block long/short tuning; encode only */
    final float[] preecho_thresh = new float[EnvelopeLookup.VE_BANDS];
    final float[] postecho_thresh = new float[EnvelopeLookup.VE_BANDS];
    float stretch_penalty = 0.0f;
    float preecho_minenergy = 0.0f;

    float ampmax_att_per_sec = 0.0f;

    /* channel coupling config */
    final int[] coupling_pkHz = new int[Info.PACKETBLOBS];
    final int[][] coupling_pointlimit = new int[2][Info.PACKETBLOBS];
    final int[] coupling_prepointamp = new int[Info.PACKETBLOBS];
    final int[] coupling_postpointamp = new int[Info.PACKETBLOBS];
    final int[][] sliding_lowpass = new int[2][Info.PACKETBLOBS];

    //
    InfoPsyGlobal() {
    }

    public InfoPsyGlobal(final int eol, final float[] pre_thresh, final float[] post_thresh,
                         final float penalty, final float preecho, final float ampmax,
                         final int[] pkHz, final int[][] pointlimit, final int[] prepointamp, final int[] postpointamp,
                         final int[][] lowpass) {
        eighth_octave_lines = eol;
        System.arraycopy(pre_thresh, 0, preecho_thresh, 0, pre_thresh.length);
        System.arraycopy(post_thresh, 0, postecho_thresh, 0, post_thresh.length);
        stretch_penalty = penalty;
        preecho_minenergy = preecho;
        ampmax_att_per_sec = ampmax;
        System.arraycopy(pkHz, 0, coupling_pkHz, 0, pkHz.length);
        for (int i = 0; i < pointlimit.length; i++) {
            System.arraycopy(pointlimit[i], 0, coupling_pointlimit[i], 0, pointlimit[i].length);
        }
        System.arraycopy(prepointamp, 0, coupling_prepointamp, 0, prepointamp.length);
        System.arraycopy(postpointamp, 0, coupling_postpointamp, 0, postpointamp.length);
        for (int i = 0; i < lowpass.length; i++) {
            System.arraycopy(lowpass[i], 0, sliding_lowpass[i], 0, lowpass[i].length);
        }
    }

    final void copyFrom(final InfoPsyGlobal g) {
        eighth_octave_lines = g.eighth_octave_lines;
        System.arraycopy(g.preecho_thresh, 0, preecho_thresh, 0, preecho_thresh.length);
        System.arraycopy(g.postecho_thresh, 0, postecho_thresh, 0, postecho_thresh.length);
        stretch_penalty = g.stretch_penalty;
        preecho_minenergy = g.preecho_minenergy;
        ampmax_att_per_sec = g.ampmax_att_per_sec;
        System.arraycopy(g.coupling_pkHz, 0, coupling_pkHz, 0, coupling_pkHz.length);
        for (int i = 0; i < coupling_pointlimit.length; i++) {
            System.arraycopy(g.coupling_pointlimit[i], 0, coupling_pointlimit[i], 0, coupling_pointlimit.length);
        }
        System.arraycopy(g.coupling_prepointamp, 0, coupling_prepointamp, 0, coupling_prepointamp.length);
        System.arraycopy(g.coupling_postpointamp, 0, coupling_postpointamp, 0, coupling_postpointamp.length);
        for (int i = 0; i < sliding_lowpass.length; i++) {
            System.arraycopy(g.sliding_lowpass[i], 0, sliding_lowpass[i], 0, sliding_lowpass.length);
        }
    }
}
