package org.xiph.speex;

/**
 * Long Term Prediction Quantisation and Unquantisation (Forced Pitch)
 * 
 * @author Jim Lawrence, helloNetwork.com
 * @author Marc Gimpel, Wimba S.A. (mgimpel@horizonwimba.com)
 */
public class LtpForcedPitch
  extends Ltp
{
  /**
   * Long Term Prediction Quantification (Forced Pitch).
   * @return pitch
   */
  public final int quant(float[] target, float[] sw, int sws, float[] ak, float[] awk1, float[] awk2,
                         float[] exc, int es, int start, int end, float pitch_coef, int p, 
                         int nsf, Bits bits, float[] exc2, int e2s, float[] r, int complexity)
  {
    int i;
    if (pitch_coef>.99f)
      pitch_coef=.99f;
    for (i=0;i<nsf;i++) {
      exc[es+i]=exc[es+i-start]*pitch_coef;
    }
    return start;
  }

  /**
   * Long Term Prediction Unquantification (Forced Pitch).
   * @param exc - Excitation
   * @param es - Excitation offset
   * @param start - Smallest pitch value allowed
   * @param pitch_coef - Voicing (pitch) coefficient
   * @param nsf - Number of samples in subframe
   * @param gain_val
   * @param bits - Speex bits buffer.
   * @param count_lost
   * @param subframe_offset
   * @param last_pitch_gain
   * @return pitch
   */
  public final int unquant(float[] exc, int es, int start, float pitch_coef,  
                           int nsf, float[] gain_val, Bits bits,
                           int count_lost, int subframe_offset, float last_pitch_gain)
  {
    int i;
    if (pitch_coef>.99f) {
      pitch_coef=.99f;
    }
    for (i=0;i<nsf;i++) {
      exc[es+i]=exc[es+i-start]*pitch_coef;
    }
    gain_val[0] = gain_val[2] = 0;
    gain_val[1] = pitch_coef;
    return start;
  }
}
