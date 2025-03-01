package org.xiph.speex;

/**
 * Speex Encoder interface, used as a base for the Narrowband and sideband
 * encoders.
 * 
 * @author Marc Gimpel, Wimba S.A. (mgimpel@horizonwimba.com)
 */
public interface Encoder
{
  /**
   * Encode the given input signal.
   * @param bits - Speex bits buffer.
   * @param in - the raw mono audio frame to encode.
   * @return 1 if successful.
   */
  public int encode(Bits bits, float[] in);

  /**
   * Returns the size in bits of an audio frame encoded with the current mode.
   * @return the size in bits of an audio frame encoded with the current mode.
   */
  public int getEncodedFrameSize();

  //--------------------------------------------------------------------------
  // Speex Control Functions
  //--------------------------------------------------------------------------

  /**
   * Returns the size of a frame.
   * @return the size of a frame.
   */
  public int  getFrameSize();

  /**
   * Sets the Quality (between 0 and 10).
   * @param quality - the desired Quality (between 0 and 10).
   */
  public void    setQuality(int quality);

  /**
   * Get the current Bit Rate.
   * @return the current Bit Rate.
   */
  public int     getBitRate();

//  public void    resetState();

  /**
   * Returns the Pitch Gain array.
   * @return the Pitch Gain array.
   */
  public float[] getPiGain();

  /**
   * Returns the excitation array.
   * @return the excitation array.
   */
  public float[] getExc();
  
  /**
   * Returns the innovation array.
   * @return the innovation array.
   */
  public float[] getInnov();

  /**
   * Sets the encoding submode.
   * @param mode
   */
  public void setMode(int mode);
  
  /**
   * Returns the encoding submode currently in use.
   * @return the encoding submode currently in use.
   */
  public int getMode();
  
  /**
   * Sets the bitrate.
   * @param bitrate
   */
  public void setBitRate(int bitrate);
  
  /**
   * Sets whether or not to use Variable Bit Rate encoding.
   * @param vbr
   */
  public void setVbr(boolean vbr);
  
  /**
   * Returns whether or not we are using Variable Bit Rate encoding.
   * @return whether or not we are using Variable Bit Rate encoding.
   */
  public boolean getVbr();
  
  /**
   * Sets whether or not to use Voice Activity Detection encoding.
   * @param vad
   */
  public void setVad(boolean vad);
  
  /**
   * Returns whether or not we are using Voice Activity Detection encoding.
   * @return whether or not we are using Voice Activity Detection encoding.
   */
  public boolean getVad();
  
  /**
   * Sets whether or not to use Discontinuous Transmission encoding.
   * @param dtx
   */
  public void setDtx(boolean dtx);
  
  /**
   * Returns whether or not we are using Discontinuous Transmission encoding.
   * @return whether or not we are using Discontinuous Transmission encoding.
   */
  public boolean getDtx();

  /**
   * Returns the Average Bit Rate used (0 if ABR is not turned on).
   * @return the Average Bit Rate used (0 if ABR is not turned on).
   */
  public int getAbr();
  
  /**
   * Sets the Average Bit Rate.
   * @param abr - the desired Average Bit Rate.
   */
  public void setAbr(int abr);

  /**
   * Sets the Varible Bit Rate Quality.
   * @param quality - the desired Varible Bit Rate Quality.
   */
  public void setVbrQuality(float quality);
  
  /**
   * Returns the Varible Bit Rate Quality.
   * @return the Varible Bit Rate Quality.
   */
  public float getVbrQuality();
  
  /**
   * Sets the algorithmic complexity.
   * @param complexity - the desired algorithmic complexity (between 1 and 10 - default is 3).
   */
  public void setComplexity(int complexity);
  
  /**
   * Returns the algorthmic complexity.
   * @return the algorthmic complexity.
   */
  public int getComplexity();
  
  /**
   * Sets the sampling rate.
   * @param rate - the sampling rate.
   */
  public void setSamplingRate(int rate);
    
  /**
   * Returns the sampling rate.
   * @return the sampling rate.
   */
  public int getSamplingRate();

  /**
   * Return LookAhead.
   * @return LookAhead.
   */
  public int getLookAhead();

  /**
   * Returns the relative quality.
   * @return the relative quality.
   */
  public float getRelativeQuality();
}
