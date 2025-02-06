package org.xiph.speex;

/**
 * Abstract class that is the base for the various Codebook search methods.
 * 
 * @author Jim Lawrence, helloNetwork.com
 * @author Marc Gimpel, Wimba S.A. (mgimpel@horizonwimba.com)
 */
public abstract class CbSearch
{
  /**
   * Codebook Search Quantification.
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
  public abstract void quant(float[] target, float[] ak, float[] awk1, float[] awk2,
                             int p, int nsf, float[] exc, int es, float[] r,
                             Bits bits, int complexity); 

  /**
   * Codebook Search Unquantification.
   * @param exc - excitation array.
   * @param es - position in excitation array.
   * @param nsf - number of samples in subframe.
   * @param bits - Speex bits buffer.
   */
  public abstract void unquant(float[] exc, int es, int nsf, Bits bits); 
}
