/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 1999-2004 Marc Gimpel, Wimba S.A.
 * Copyright (c) 2002-2004 Xiph.org Foundation
 * Copyright (c) 2002-2004 Jean-Marc Valin
 * Copyright (c) 1993, 2002 David Rowe
 * Copyright (c) 1992-1994 Jutta Degener,
 *                         Carsten Bormann,
 *                         Berlin University of Technology
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
 * A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE FOUNDATION OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
