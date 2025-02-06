package org.xiph.speex;

/**
 * Miscellaneous functions
 * 
 * @author Marc Gimpel, Wimba S.A. (mgimpel@horizonwimba.com)
 */
public class Misc
{
  /**
   * Builds an Asymmetric "pseudo-Hamming" window.
   * @param windowSize
   * @param subFrameSize
   * @return an Asymmetric "pseudo-Hamming" window.
   */
  public static float[] window(final int windowSize, final int subFrameSize)
  {
    int i;
    int part1 = subFrameSize * 7 / 2;
    int part2 = subFrameSize * 5 / 2;
    float[] window = new float[windowSize];
    for (i=0; i<part1; i++)
      window[i]=(float) (0.54 - 0.46 * Math.cos(Math.PI * i / part1));
    for (i=0; i<part2; i++)
      window[part1+i]=(float) (0.54 + 0.46 * Math.cos(Math.PI * i / part2));
    return window;
  }
  
  /**
   * Create the window for autocorrelation (lag-windowing).
   * @param lpcSize
   * @param lagFactor
   * @return the window for autocorrelation.
   */
  public static float[] lagWindow(final int lpcSize, final float lagFactor)
  {
    float[] lagWindow = new float[lpcSize+1];
    for (int i=0; i<lpcSize+1; i++)
      lagWindow[i]=(float) Math.exp(-0.5 * (2*Math.PI*lagFactor*i) *
                                           (2*Math.PI*lagFactor*i));
    return lagWindow;
  }
}
