package org.xiph.speex;

/**
 * LSP Quantisation and Unquantisation (high)
 * 
 * @author Jim Lawrence, helloNetwork.com
 * @author Marc Gimpel, Wimba S.A. (mgimpel@horizonwimba.com)
 */
public class HighLspQuant
  extends LspQuant
{
  /**
   * Line Spectral Pair Quantification (high).
   * @param lsp - Line Spectral Pairs table.
   * @param qlsp - Quantified Line Spectral Pairs table.
   * @param order
   * @param bits - Speex bits buffer.
   */
  public final void quant(final float[] lsp,
                          final float[] qlsp,
                          final int order,
                          final Bits bits)
  {
    int i;
    float tmp1, tmp2;
    int id;
    float[] quant_weight = new float[MAX_LSP_SIZE];

    for (i=0;i<order;i++)
      qlsp[i]=lsp[i];
    quant_weight[0] = 1/(qlsp[1]-qlsp[0]);
    quant_weight[order-1] = 1/(qlsp[order-1]-qlsp[order-2]);
    for (i=1;i<order-1;i++)
    {
      tmp1 = 1/(qlsp[i]-qlsp[i-1]);
      tmp2 = 1/(qlsp[i+1]-qlsp[i]);
      quant_weight[i] = tmp1 > tmp2 ? tmp1 : tmp2;
    }

    for (i=0;i<order;i++)
      qlsp[i]-=(.3125*i+.75);
    for (i=0;i<order;i++)
      qlsp[i]*=256;
    id = lsp_quant(qlsp, 0, high_lsp_cdbk, 64, order);
    bits.pack(id, 6);

    for (i=0;i<order;i++)
      qlsp[i]*=2;
    id = lsp_weight_quant(qlsp, 0, quant_weight, 0, high_lsp_cdbk2, 64, order);
    bits.pack(id, 6);

    for (i=0;i<order;i++)
      qlsp[i]*=0.0019531;
    for (i=0;i<order;i++)
      qlsp[i]=lsp[i]-qlsp[i];
  }

  /**
   * Line Spectral Pair Unquantification (high).
   * @param lsp - Line Spectral Pairs table.
   * @param order
   * @param bits - Speex bits buffer.
   */
  public final void unquant(final float[] lsp,
                            final int order,
                            final Bits bits)
  {
    for (int i=0;i<order;i++) {
      lsp[i]=.3125f*i+.75f;
    }
    unpackPlus(lsp, high_lsp_cdbk, bits, 0.0039062f, order, 0);
    unpackPlus(lsp, high_lsp_cdbk2, bits, 0.0019531f, order, 0);
  } 
}
