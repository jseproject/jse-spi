package org.xiph.speex;

import java.io.StreamCorruptedException;

/**
 * Speex Decoder inteface, used as a base for the Narrowband and sideband
 * decoders.
 * 
 * @author Jim Lawrence, helloNetwork.com
 * @author Marc Gimpel, Wimba S.A. (mgimpel@horizonwimba.com)
 */
public interface Decoder
{
  /**
   * Decode the given input bits.
   * @param bits - Speex bits buffer.
   * @param out - the decoded mono audio frame.
   * @return 1 if a terminator was found, 0 if not.
   * @throws StreamCorruptedException If there is an error detected in the
   * data stream.
   */
  public int decode(Bits bits, float[] out)
    throws StreamCorruptedException;
  
  /**
   * Decode the given bits to stereo.
   * @param data - float array of size 2*frameSize, that contains the mono
   * audio samples in the first half. When the function has completed, the
   * array will contain the interlaced stereo audio samples.
   * @param frameSize - the size of a frame of mono audio samples.
   */
  public void decodeStereo(float[] data, int frameSize);

  /**
   * Enables or disables perceptual enhancement.
   * @param enhanced
   */
  public void setPerceptualEnhancement(boolean enhanced);
  
  /**
   * Returns whether perceptual enhancement is enabled or disabled.
   * @return whether perceptual enhancement is enabled or disabled.
   */
  public boolean getPerceptualEnhancement();

  /**
   * Returns the size of a frame.
   * @return the size of a frame.
   */
  public int  getFrameSize();

  /**
   * Returns whether or not we are using Discontinuous Transmission encoding.
   * @return whether or not we are using Discontinuous Transmission encoding.
   */
  public boolean getDtx();

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
}
