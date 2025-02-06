package org.xiph.speex;

/**
 * Main Speex Encoder class.
 * This class encodes the given PCM 16bit samples into Speex packets.
 *
 * @author Marc Gimpel, Wimba S.A. (mgimpel@horizonwimba.com)
 */
public class SpeexEncoder
{
  /**
   * Version of the Speex Encoder
   */
  public static final String VERSION = "Java Speex Encoder v0.9.7";

  private Encoder encoder;
  private Bits    bits;
  private float[] rawData;
  private int     sampleRate;
  private int     channels;
  private int     frameSize;

  /**
   * Constructor
   */
  public SpeexEncoder()
  {
    bits = new Bits();
  }

  /**
   * Initialisation
   * @param mode       the mode of the encoder (0=NB, 1=WB, 2=UWB).
   * @param quality    the quality setting of the encoder (between 0 and 10).
   * @param sampleRate the number of samples per second.
   * @param channels   the number of audio channels (1=mono, 2=stereo, ...).
   * @return true if initialisation successful.
   */
  public boolean init(final int mode,
                      final int quality,
                      final int sampleRate,
                      final int channels)
  {
    switch (mode) {
      case 0:
        encoder = new NbEncoder();
        ((NbEncoder)encoder).nbinit();
        break;
//Wideband
      case 1:
        encoder = new SbEncoder();
        ((SbEncoder)encoder).wbinit();
        break;
      case 2:
        encoder = new SbEncoder();
        ((SbEncoder)encoder).uwbinit();
        break;
//*/
      default:
        return false;
    }

    /* initialize the speex decoder */
    encoder.setQuality(quality);

    /* set decoder format and properties */
    this.frameSize  = encoder.getFrameSize();
    this.sampleRate = sampleRate;
    this.channels   = channels;
    rawData         = new float[channels*frameSize];

    bits.init();
    return true;
  }

  /**
   * Returns the Encoder being used (Narrowband, Wideband or Ultrawideband).
   * @return the Encoder being used (Narrowband, Wideband or Ultrawideband).
   */
  public Encoder getEncoder()
  {
    return encoder;
  }

  /**
   * Returns the sample rate.
   * @return the sample rate.
   */
  public int getSampleRate()
  {
    return sampleRate;
  }

  /**
   * Returns the number of channels.
   * @return the number of channels.
   */
  public int getChannels()
  {
    return channels;
  }

  /**
   * Returns the size of a frame.
   * @return the size of a frame.
   */
  public int getFrameSize()
  {
    return frameSize;
  }

  /**
   * Pull the decoded data out into a byte array at the given offset
   * and returns the number of bytes of encoded data just read.
   * @param data
   * @param offset
   * @return the number of bytes of encoded data just read.
   */
  public int getProcessedData(final byte[] data, final int offset)
  {
    int size = bits.getBufferSize();
    System.arraycopy(bits.getBuffer(), 0, data, offset, size);
    bits.init();
    return size;
  }

  /**
   * Returns the number of bytes of encoded data ready to be read.
   * @return the number of bytes of encoded data ready to be read.
   */
  public int getProcessedDataByteSize()
  {
    return bits.getBufferSize();
  }

  /**
   * This is where the actual encoding takes place
   * @param data
   * @param offset
   * @param len
   * @return true if successful.
   */
  public boolean processData(final byte[] data,
                             final int offset,
                             final int len)
  {
    // converty raw bytes into float samples
    mapPcm16bitLittleEndian2Float(data, offset, rawData, 0, len/2);
    // encode the bitstream
    return processData(rawData, len/2);
  }

  /**
   * Encode an array of shorts.
   * @param data
   * @param offset
   * @param numShorts
   * @return true if successful.
   */
  public boolean processData(final short[] data,
                             final int offset,
                             final int numShorts)
  {
    int numSamplesRequired = channels * frameSize;
    if (numShorts != numSamplesRequired) {
      throw new IllegalArgumentException("SpeexEncoder requires " + numSamplesRequired + " samples to process a Frame, not " + numShorts);
    }
    // convert shorts into float samples,
    for (int i=0; i<numShorts; i++) {
      rawData[i] = (float) data[offset + i ];
    }
    // encode the bitstream
    return processData(rawData, numShorts);
  }

  /**
   * Encode an array of floats.
   * @param data
   * @param numSamples
   * @return true if successful.
   */
  public boolean processData(final float[] data, final int numSamples)
  {
    int numSamplesRequired = channels * frameSize;
    if (numSamples != numSamplesRequired) {
      throw new IllegalArgumentException("SpeexEncoder requires " + numSamplesRequired + " samples to process a Frame, not " + numSamples );
    }
    // encode the bitstream
    if (channels==2) {
      Stereo.encode(bits, data, frameSize);
    }
    encoder.encode(bits, data);
    return true;
  }

  /**
   * Converts a 16 bit linear PCM stream (in the form of a byte array)
   * into a floating point PCM stream (in the form of an float array).
   * Here are some important details about the encoding:
   * <ul>
   * <li> Java uses big endian for shorts and ints, and Windows uses little Endian.
   *      Therefore, shorts and ints must be read as sequences of bytes and
   *      combined with shifting operations.
   * </ul>
   * @param pcm16bitBytes - byte array of linear 16-bit PCM formated audio.
   * @param offsetInput
   * @param samples - float array to receive the 16-bit linear audio samples.
   * @param offsetOutput
   * @param length
   */
  public static void mapPcm16bitLittleEndian2Float(final byte[] pcm16bitBytes,
                                                   final int offsetInput,
                                                   final float[] samples,
                                                   final int offsetOutput,
                                                   final int length)
  {
    if (pcm16bitBytes.length - offsetInput < 2 * length) {
      throw new IllegalArgumentException("Insufficient Samples to convert to floats");
    }
    if (samples.length - offsetOutput < length) {
      throw new IllegalArgumentException("Insufficient float buffer to convert the samples");
    }
    for (int i = 0; i < length; i++) {
      samples[offsetOutput+i] = (float)((pcm16bitBytes[offsetInput+2*i] & 0xff) | (pcm16bitBytes[offsetInput+2*i+1] << 8)); // no & 0xff at the end to keep the sign
    }
  }
}
