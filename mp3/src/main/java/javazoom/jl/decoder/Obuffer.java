/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 2008 Christopher G. Jennings
 * Copyright (c) 1999-2010 JavaZOOM
 * Copyright (c) 1999 Mat McGowan
 * Copyright (c) 1997 Jeff Tsay
 * Copyright (c) 1993-1994 Tobias Bading
 * Copyright (c) 1991 MPEG Software Simulation Group
 *
 * - This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * - This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Library General Public License for more details.
 *
 * - You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package javazoom.jl.decoder;

/**
 * Base Class for audio output.
 */
public abstract class Obuffer
{
  public static final int	OBUFFERSIZE = 2 * 1152;	// max. 2 * 1152 samples per frame
  public static final int   MAXCHANNELS = 2;        // max. number of channels

  /**
   * Takes a 16 Bit PCM sample.
   */
  public abstract void append(int channel, short value);

  /**
   * Accepts 32 new PCM samples. 
   */
	public void appendSamples(int channel, float[] f)
	{
	    short s;
	    for (int i=0; i<32;)
	    {
		  	s = clip(f[i++]);
			append(channel, s); 
	    }
	}

  /**
   * Clip Sample to 16 Bits
   */
  private final short clip(float sample)
  {
	return ((sample > 32767.0f) ? 32767 :
           ((sample < -32768.0f) ? -32768 :
			  (short) sample));
  }
  
  /**
   * Write the samples to the file or directly to the audio hardware.
   */
  public abstract void write_buffer(int val);
  public abstract void close();

  /**
   * Clears all data in the buffer (for seeking).
   */
  public abstract void clear_buffer();

  /**
   * Notify the buffer that the user has stopped the stream.
   */
  public abstract void set_stop_flag();
}
