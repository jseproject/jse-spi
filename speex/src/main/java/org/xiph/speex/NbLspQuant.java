package org.xiph.speex;

/**
 * LSP Quantisation and Unquantisation (narrowband)
 * 
 * @author Jim Lawrence, helloNetwork.com
 * @author Marc Gimpel, Wimba S.A. (mgimpel@horizonwimba.com)
 */
public class NbLspQuant
  extends LspQuant
{
  /**
   * Line Spectral Pair Quantification (narrowband).
   * @param lsp - Line Spectral Pairs table.
   * @param qlsp - Quantified Line Spectral Pairs table.
   * @param order
   * @param bits - Speex bits buffer.
   */
  public final void quant(float[] lsp, float[] qlsp, int order, Bits bits)
  {
    int i;
    float tmp1, tmp2;
    int id;
    float[] quant_weight = new float[MAX_LSP_SIZE];

    for (i=0;i<order;i++)
      qlsp[i]=lsp[i];
    quant_weight[0] = 1/(qlsp[1]-qlsp[0]);
    quant_weight[order-1] = 1/(qlsp[order-1]-qlsp[order-2]);
    for (i=1;i<order-1;i++) {
      tmp1 = 1/((.15f+qlsp[i]-qlsp[i-1])*(.15f+qlsp[i]-qlsp[i-1]));
      tmp2 = 1/((.15f+qlsp[i+1]-qlsp[i])*(.15f+qlsp[i+1]-qlsp[i]));
      quant_weight[i] = tmp1 > tmp2 ? tmp1 : tmp2;
    }
    for (i=0;i<order;i++)
      qlsp[i]-=(.25*i+.25);
    for (i=0;i<order;i++)
      qlsp[i]*=256;
    id = lsp_quant(qlsp, 0, cdbk_nb, NB_CDBK_SIZE, order);
    bits.pack(id, 6);

    for (i=0;i<order;i++)
      qlsp[i]*=2;
    id = lsp_weight_quant(qlsp, 0, quant_weight, 0, cdbk_nb_low1, NB_CDBK_SIZE_LOW1, 5);
    bits.pack(id, 6);

    for (i=0;i<5;i++)
      qlsp[i]*=2;
    id = lsp_weight_quant(qlsp, 0, quant_weight, 0, cdbk_nb_low2, NB_CDBK_SIZE_LOW2, 5);
    bits.pack(id, 6);
    id = lsp_weight_quant(qlsp, 5, quant_weight, 5, cdbk_nb_high1, NB_CDBK_SIZE_HIGH1, 5);
    bits.pack(id, 6);

    for (i=5;i<10;i++)
      qlsp[i]*=2;
    id = lsp_weight_quant(qlsp, 5, quant_weight, 5, cdbk_nb_high2, NB_CDBK_SIZE_HIGH2, 5);
    bits.pack(id, 6);

    for (i=0;i<order;i++)
      qlsp[i]*=.00097656;
    for (i=0;i<order;i++)
      qlsp[i]=lsp[i]-qlsp[i];
  }

  /**
   * Line Spectral Pair Unquantification (narrowband).
   * @param lsp - Line Spectral Pairs table.
   * @param order
   * @param bits - Speex bits buffer.
   */
  public final void unquant(float[] lsp, int order, Bits bits)
  {
    for (int i=0;i<order;i++) {
      lsp[i]=.25f*i+.25f;
    }
    unpackPlus(lsp, cdbk_nb, bits, 0.0039062f, 10, 0);
    unpackPlus(lsp, cdbk_nb_low1, bits, 0.0019531f, 5, 0);
    unpackPlus(lsp, cdbk_nb_low2, bits, 0.00097656f, 5, 0);
    unpackPlus(lsp, cdbk_nb_high1, bits, 0.0019531f, 5, 5);
    unpackPlus(lsp, cdbk_nb_high2, bits, 0.00097656f, 5, 5);
  } 
}
