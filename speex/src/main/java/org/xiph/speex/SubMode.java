package org.xiph.speex;

/**
 * Speex SubMode
 * 
 * @author Jim Lawrence, helloNetwork.com
 * @author Marc Gimpel, Wimba S.A. (mgimpel@horizonwimba.com)
 */
public class SubMode
{
  /** Set to -1 for "normal" modes, otherwise encode pitch using a global pitch and allowing a +- lbr_pitch variation (for low not-rates)*/
  public int      lbr_pitch;
  /** Use the same (forced) pitch gain for all sub-frames */
  public int      forced_pitch_gain;
  /** Number of bits to use as sub-frame innovation gain */
  public int      have_subframe_gain;
  /** Apply innovation quantization twice for higher quality (and higher bit-rate)*/
  public int      double_codebook;
  /** LSP quantization/unquantization function */
  public LspQuant lsqQuant;
  /** Long-term predictor (pitch) un-quantizer */
  public Ltp      ltp;
  /** Codebook Search un-quantizer*/
  public CbSearch innovation;
  /** Enhancer constant */
  public float    lpc_enh_k1;
  /** Enhancer constant */
  public float    lpc_enh_k2;
  /** Gain of enhancer comb filter */
  public float    comb_gain;
  /** Number of bits per frame after encoding*/
  public int      bits_per_frame;
   
  /**
   * Constructor
   */
  public SubMode(final int      lbr_pitch,
                 final int      forced_pitch_gain, 
                 final int      have_subframe_gain, 
                 final int      double_codebook,   
                 final LspQuant lspQuant,    
                 final Ltp      ltp,         
                 final CbSearch innovation,
                 final float    lpc_enh_k1,
                 final float    lpc_enh_k2,
                 final float    comb_gain,
                 final int      bits_per_frame)
  {
    this.lbr_pitch          = lbr_pitch;
    this.forced_pitch_gain  = forced_pitch_gain;
    this.have_subframe_gain = have_subframe_gain;
    this.double_codebook    = double_codebook;
    this.lsqQuant           = lspQuant;
    this.ltp                = ltp;
    this.innovation         = innovation;
    this.lpc_enh_k1         = lpc_enh_k1;
    this.lpc_enh_k2         = lpc_enh_k2;
    this.comb_gain          = comb_gain;
    this.bits_per_frame     = bits_per_frame;
  }
}
