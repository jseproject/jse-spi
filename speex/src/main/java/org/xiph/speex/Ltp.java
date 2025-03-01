package org.xiph.speex;

/**
 * Abstract class that is the base for the various LTP (Long Term Prediction)
 * Quantisation and Unquantisation methods.
 * 
 * @author Jim Lawrence, helloNetwork.com
 * @author Marc Gimpel, Wimba S.A. (mgimpel@horizonwimba.com)
 */
public abstract class Ltp
{
  /**
   * Long Term Prediction Quantification.
   * @return pitch
   */
  public abstract int quant(float[] target, float[] sw, int sws, float[] ak, float[] awk1, float[] awk2,
                            float[] exc, int es, int start, int end, float pitch_coef, int p, 
                            int nsf, Bits bits, float[] exc2, int e2s, float[] r, int complexity);

  /**
   * Long Term Prediction Unquantification.
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
  public abstract int unquant(float[] exc, int es, int start, float pitch_coef,  
                              int nsf, float[] gain_val, Bits bits,
                              int count_lost, int subframe_offset, float last_pitch_gain);

  /**
   * Calculates the inner product of the given vectors.
   * @param x - first vector.
   * @param xs - offset of the first vector.
   * @param y - second vector.
   * @param ys - offset of the second vector.
   * @param len - length of the vectors.
   * @return the inner product of the given vectors.
   */
  protected static float inner_prod(float[] x, int xs, float[] y, int ys, int len)
  {
    int i;
    float sum1=0,sum2=0,sum3=0,sum4=0;
    for (i=0;i<len;)
    {
      sum1 += x[xs+i]*y[ys+i];
      sum2 += x[xs+i+1]*y[ys+i+1];
      sum3 += x[xs+i+2]*y[ys+i+2];
      sum4 += x[xs+i+3]*y[ys+i+3];
      i+=4;
    }
    return sum1+sum2+sum3+sum4;
  }

  /**
   * Find the n-best pitch in Open Loop.
   * @param sw
   * @param swIdx
   * @param start
   * @param end
   * @param len
   * @param pitch
   * @param gain
   * @param N
   */
  protected static void open_loop_nbest_pitch(final float[] sw,
                                              final int swIdx,
                                              final int start,
                                              final int end,
                                              final int len,
                                              final int[] pitch,
                                              final float[] gain,
                                              final int N)
  {
    int i, j, k;
    /*float corr=0;*/
    /*float energy;*/
    float[] best_score;
    float e0;
    float[] corr, energy, score;

    best_score = new float[N];
    corr = new float[end-start+1];
    energy = new float[end-start+2];
    score = new float[end-start+1];
    for (i=0; i<N; i++) {
      best_score[i]=-1;
      gain[i]=0;
      pitch[i]=start;
    }
    energy[0]=inner_prod(sw, swIdx-start, sw, swIdx-start, len);
    e0=inner_prod(sw, swIdx, sw, swIdx, len);
    for (i=start; i<=end; i++) {
      /* Update energy for next pitch*/
      energy[i-start+1] = energy[i-start] + sw[swIdx-i-1]*sw[swIdx-i-1] - sw[swIdx-i+len-1]*sw[swIdx-i+len-1];
      if (energy[i-start+1] < 1)
        energy[i-start+1]=1; 
    }
    for (i=start; i<=end; i++) {
      corr[i-start]=0;
      score[i-start]=0;
    }

    for (i=start; i<=end; i++) {
      /* Compute correlation*/
      corr[i-start]=inner_prod(sw, swIdx, sw, swIdx-i, len);
      score[i-start]=corr[i-start]*corr[i-start]/(energy[i-start]+1);
    }
    for (i=start; i<=end; i++) {
      if (score[i-start] > best_score[N-1]) {
        float g1, g;
        g1 = corr[i-start]/(energy[i-start]+10);
        g = (float) Math.sqrt(g1*corr[i-start]/(e0+10));
        if (g>g1)
          g=g1;
        if (g<0)
          g=0;
        for (j=0; j<N; j++) {
          if (score[i-start] > best_score[j]) {
            for (k=N-1; k>j; k--) {
              best_score[k]=best_score[k-1];
              pitch[k]=pitch[k-1];
              gain[k] = gain[k-1];
            }
            best_score[j]=score[i-start];
            pitch[j]=i;
            gain[j]=g;
            break;
          }
        }
      }
    }
  }
}
