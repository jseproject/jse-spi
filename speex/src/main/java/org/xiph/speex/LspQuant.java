package org.xiph.speex;

/**
 * Abstract class that is the base for the various LSP Quantisation and
 * Unquantisation methods.
 * 
 * @author Jim Lawrence, helloNetwork.com
 * @author Marc Gimpel, Wimba S.A. (mgimpel@horizonwimba.com)
 */
public abstract class LspQuant
  implements Codebook
{
  /** */
  public static final int MAX_LSP_SIZE       = 20;

  /**
   * Constructor
   */
  protected LspQuant()
  {
  }

  /**
   * Line Spectral Pair Quantification.
   * @param lsp - Line Spectral Pairs table.
   * @param qlsp - Quantified Line Spectral Pairs table.
   * @param order
   * @param bits - Speex bits buffer.
   */
  public abstract void quant(final float[] lsp,
                             final float[] qlsp,
                             final int order,
                             final Bits bits); 
  
  /**
   * Line Spectral Pair Unquantification.
   * @param lsp - Line Spectral Pairs table.
   * @param order
   * @param bits - Speex bits buffer.
   */
  public abstract void unquant(float[] lsp, int order, Bits bits); 
  
  /**
   * Read the next 6 bits from the buffer, and using the value read and the
   * given codebook, rebuild LSP table.
   * @param lsp
   * @param tab
   * @param bits - Speex bits buffer.
   * @param k
   * @param ti
   * @param li
   */
  protected void unpackPlus(final float[] lsp,
                            final int[] tab,
                            final Bits bits,
                            final float k,
                            final int ti,
                            final int li)
  {
    int id=bits.unpack(6);
    for (int i=0;i<ti;i++)
      lsp[i+li] += k * (float)tab[id*ti+i];
  }
  
  /**
   * LSP quantification
   * Note: x is modified
   * @param x
   * @param xs
   * @param cdbk
   * @param nbVec
   * @param nbDim
   * @return the index of the best match in the codebook
   * (NB x is also modified).
   */
  protected static int lsp_quant(final float[] x,
                                 final int xs,
                                 final int[] cdbk,
                                 final int nbVec,
                                 final int nbDim)
  {
    int i, j;
    float dist, tmp;
    float best_dist=0;
    int best_id=0;
    int ptr=0;
    for (i=0; i<nbVec; i++) {
      dist=0;
      for (j=0; j<nbDim; j++) {
        tmp=(x[xs+j]-cdbk[ptr++]);
        dist+=tmp*tmp;
      }
      if (dist<best_dist || i==0) {
        best_dist=dist;
        best_id=i;
      }
    }

    for (j=0; j<nbDim; j++)
      x[xs+j] -= cdbk[best_id*nbDim+j];
    
    return best_id;
  }

  /**
   * LSP weighted quantification
   * Note: x is modified
   * @param x
   * @param xs
   * @param weight
   * @param ws
   * @param cdbk
   * @param nbVec
   * @param nbDim
   * @return the index of the best match in the codebook
   * (NB x is also modified).
   */
  protected static int lsp_weight_quant(final float[] x,
                                        final int xs,
                                        final float[] weight,
                                        final int ws,
                                        final int[] cdbk,
                                        final int nbVec,
                                        final int nbDim)
  {
    int i,j;
    float dist, tmp;
    float best_dist=0;
    int best_id=0;
    int ptr=0;
    for (i=0; i<nbVec; i++) {
      dist=0;
      for (j=0; j<nbDim; j++) {
        tmp=(x[xs+j]-cdbk[ptr++]);
        dist+=weight[ws+j]*tmp*tmp;
      }
      if (dist<best_dist || i==0) {
        best_dist=dist;
        best_id=i;
      }
    }
    for (j=0; j<nbDim; j++)
      x[xs+j] -= cdbk[best_id*nbDim+j];
    return best_id;
  }
}
