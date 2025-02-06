package org.xiph.vorbis;

/**
 * psychoacoustic setup
 */
public class InfoPsy {
    int blockflag = 0;

    float ath_adjatt = 0.0f;
    float ath_maxatt = 0.0f;

    final float[] tone_masteratt = new float[LookPsy.P_NOISECURVES];
    float tone_centerboost = 0.0f;
    float tone_decay = 0.0f;
    float tone_abs_limit = 0.0f;
    float[] toneatt = new float[LookPsy.P_BANDS];

    boolean noisemaskp = false;// FIXME noisemaskp never uses
    float noisemaxsupp = 0.0f;
    float noisewindowlo = 0.0f;
    float noisewindowhi = 0.0f;
    int noisewindowlomin = 0;
    int noisewindowhimin = 0;
    int noisewindowfixed = 0;
    float[][] noiseoff = new float[LookPsy.P_NOISECURVES][LookPsy.P_BANDS];
    float[] noisecompand = new float[LookPsy.NOISE_COMPAND_LEVELS];

    float max_curve_dB = 0.0f;

    boolean normal_p = false;
    int normal_start = 0;
    int normal_partition = 0;
    double normal_thresh = 0.0;

    //
    InfoPsy() {
    }

    InfoPsy(final InfoPsy vip) {
        blockflag = vip.blockflag;

        ath_adjatt = vip.ath_adjatt;
        ath_maxatt = vip.ath_maxatt;

        System.arraycopy(vip.tone_masteratt, 0, tone_masteratt, 0, vip.tone_masteratt.length);
        tone_centerboost = vip.tone_centerboost;
        tone_decay = vip.tone_decay;
        tone_abs_limit = vip.tone_abs_limit;
        System.arraycopy(vip.toneatt, 0, toneatt, 0, vip.toneatt.length);

        noisemaskp = vip.noisemaskp;
        noisemaxsupp = vip.noisemaxsupp;
        noisewindowlo = vip.noisewindowlo;
        noisewindowhi = vip.noisewindowhi;
        noisewindowlomin = vip.noisewindowlomin;
        noisewindowhimin = vip.noisewindowhimin;
        noisewindowfixed = vip.noisewindowfixed;
        for (int i = 0; i < vip.noiseoff.length; i++) {
            System.arraycopy(vip.noiseoff[i], 0, noiseoff[i], 0, vip.noiseoff[i].length);
        }
        System.arraycopy(noisecompand, 0, noisecompand, 0, noisecompand.length);

        max_curve_dB = vip.max_curve_dB;

        normal_p = vip.normal_p;
        normal_start = vip.normal_start;
        normal_partition = vip.normal_partition;
        normal_thresh = vip.normal_thresh;
    }

    public InfoPsy(final int i_blockflag, final float f_ath_adjatt, final float f_ath_maxatt,
                   final float[] f_tone_masteratt, final float f_tone_centerboost, final float f_tone_decay,
                   final float f_tone_abs_limit, final float[] f_toneatt,
                   final boolean i_noisemaskp, final float f_noisemaxsupp,
                   final float f_noisewindowlo, final float f_noisewindowhi,
                   final int i_noisewindowlomin, final int i_noisewindowhimin, final int i_noisewindowfixed,
                   final float[][] f_noiseoff, final float[] noisecompand, final float f_max_curve_dB,
                   final boolean i_normal_p, final int i_normal_start, final int i_normal_partition,
                   final double d_normal_thresh) {
        blockflag = i_blockflag;

        ath_adjatt = f_ath_adjatt;
        ath_maxatt = f_ath_maxatt;

        System.arraycopy(f_tone_masteratt, 0, tone_masteratt, 0, f_tone_masteratt.length);
        tone_centerboost = f_tone_centerboost;
        tone_decay = f_tone_decay;
        tone_abs_limit = f_tone_abs_limit;
        System.arraycopy(f_toneatt, 0, toneatt, 0, f_toneatt.length);

        noisemaskp = i_noisemaskp;
        noisemaxsupp = f_noisemaxsupp;
        noisewindowlo = f_noisewindowlo;
        noisewindowhi = f_noisewindowhi;
        noisewindowlomin = i_noisewindowlomin;
        noisewindowhimin = i_noisewindowhimin;
        noisewindowfixed = i_noisewindowfixed;
        for (int i = 0, length = f_noiseoff.length; i < length; i++) {
            System.arraycopy(f_noiseoff[i], 0, noiseoff[i], 0, f_noiseoff[i].length);
        }
        System.arraycopy(noisecompand, 0, noisecompand, 0, noisecompand.length);

        max_curve_dB = f_max_curve_dB;

        normal_p = i_normal_p;
        normal_start = i_normal_start;
        normal_partition = i_normal_partition;
        normal_thresh = d_normal_thresh;
    }
}
