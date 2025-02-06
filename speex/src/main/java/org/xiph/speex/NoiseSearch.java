package org.xiph.speex;

/**
 * Noise codebook search
 * 
 * @author Jim Lawrence, helloNetwork.com
 * @author Marc Gimpel, Wimba S.A. (mgimpel@horizonwimba.com)
 */
public class NoiseSearch
  extends CbSearch
{
  /**
   * Codebook Search Quantification (Noise).
   * @param target   target vector
   * @param ak       LPCs for this subframe
   * @param awk1     Weighted LPCs for this subframe
   * @param awk2     Weighted LPCs for this subframe
   * @param p        number of LPC coeffs
   * @param nsf      number of samples in subframe
   * @param exc      excitation array.
   * @param es       position in excitation array.
   * @param r
   * @param bits     Speex bits buffer.
   * @param complexity
   */
  public final void quant(float[] target, float[] ak, float[] awk1, float[] awk2,
                          int p, int nsf, float[] exc, int es, float[] r,
                          Bits bits, int complexity)
  {
    int i;
    float[] tmp=new float[nsf];
    Filters.residue_percep_zero(target, 0, ak, awk1, awk2, tmp, nsf, p);

    for (i=0;i<nsf;i++)
      exc[es+i]+=tmp[i];
    for (i=0;i<nsf;i++)
      target[i]=0;
  }

  /**
   * Codebook Search Unquantification (Noise).
   * @param exc - excitation array.
   * @param es - position in excitation array.
   * @param nsf - number of samples in subframe.
   * @param bits - Speex bits buffer.
   */
  public final void unquant(float[] exc, int es, int nsf, Bits bits)
  {
    for (int i=0; i<nsf; i++) {
      exc[es+i]+= (float) (3.0*(Math.random()-.5));
    }
  }
}
