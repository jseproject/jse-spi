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
 * The <code>SampleBuffer</code> class implements an output buffer
 * that provides storage for a fixed size block of samples. 
 */
public class SampleBuffer extends Obuffer
{
  private short[] 		buffer;
  private int[] 		bufferp;
  private int 			channels;
  private int			frequency;
  
  /**
   * Constructor
   */
  public SampleBuffer(int sample_frequency, int number_of_channels)
  {
  	buffer = new short[OBUFFERSIZE];
	bufferp = new int[MAXCHANNELS];
	channels = number_of_channels;
	frequency = sample_frequency;
	
	for (int i = 0; i < number_of_channels; ++i) 
		bufferp[i] = (short)i;
	
  }

  public int getChannelCount()
  {
	return this.channels;  
  }
  
  public int getSampleFrequency()
  {
	  return this.frequency;
  }
  
  public short[] getBuffer()
  {
	return this.buffer;  
  }
  
  public int getBufferLength()
  {
	  return bufferp[0];
  }
  
  /**
   * Takes a 16 Bit PCM sample.
   */
  public void append(int channel, short value)
  {
	buffer[bufferp[channel]] = value;
	bufferp[channel] += channels;	  	
  }
  
	public void appendSamples(int channel, float[] f)
	{
	    int pos = bufferp[channel];
		
		short s;
		float fs;
	    for (int i=0; i<32;)
	    {
		  	fs = f[i++];
			fs = (fs>32767.0f ? 32767.0f 
						   : (fs < -32767.0f ? -32767.0f : fs));
			
			s = (short)fs;
			buffer[pos] = s;
			pos += channels;
	    }
		
		bufferp[channel] = pos;
	}
  
  
  /**
   * Write the samples to the file (Random Acces).
   */
  public void write_buffer(int val)
  {
				  
	//for (int i = 0; i < channels; ++i) 
	//	bufferp[i] = (short)i;

  }

  public void close()
  {}
  
  /**
   *
   */
  public void clear_buffer()
  {
	for (int i = 0; i < channels; ++i) 
		bufferp[i] = (short)i;
  }

  /**
   *
   */
  public void set_stop_flag()
  {}
}
