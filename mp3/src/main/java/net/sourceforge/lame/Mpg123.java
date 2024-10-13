/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 1999-2010 The LAME Project
 * Copyright (c) 1999-2008 JavaZOOM
 * Copyright (c) 2001-2002 Naoki Shibata
 * Copyright (c) 2001 Jonathan Dee
 * Copyright (c) 2000-2017 Robert Hegemann
 * Copyright (c) 2000-2008 Gabriel Bouvigne
 * Copyright (c) 2000-2005 Alexander Leidinger
 * Copyright (c) 2000 Don Melton
 * Copyright (c) 1999-2005 Takehiro Tominaga
 * Copyright (c) 1999-2001 Mark Taylor
 * Copyright (c) 1999 Albert L. Faber
 * Copyright (c) 1988, 1993 Ron Mayer
 * Copyright (c) 1998 Michael Cheng
 * Copyright (c) 1997 Jeff Tsay
 * Copyright (c) 1995-1997 Michael Hipp
 * Copyright (c) 1993-1994 Tobias Bading,
 *                         Berlin University of Technology
 *
 * - This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * - This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * - You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package net.sourceforge.lame;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Init:
 * <pre>
 * Mpg123 decoder = new Mpg123();
 * decoder.InitMP3();
 * if( decoder.open( stream ) < 0 ) {
 *     decoder.ExitMP3();
 * }
 * </pre>
 *
 * Calling ExitMP3() do not closing the input stream.
 * The input stream must be closed manually;
 */
public class Mpg123 extends MpStrTag {
	public static final int MP3_ERR = -1;
	public static final int MP3_OK  = 0;
	public static final int MP3_NEED_MORE = 1;

	static final int SBLIMIT = 32;
	static final int SSLIMIT = 18;

	// private static final int MPG_MD_STEREO       = 0;// FIXME never uses
	static final int MPG_MD_JOINT_STEREO = 1;
	// private static final int MPG_MD_DUAL_CHANNEL = 2;// FIXME never uses
	static final int MPG_MD_MONO         = 3;

	static final int MAXFRAMESIZE = 2880;

	/* AF: ADDED FOR LAYER1/LAYER2 */
	static final int SCALE_BLOCK  = 12;

	/** Pre Shift fo 16 to 8 bit converter table */
	// private static final int AUSHIFT = 3;// FIXME never uses

	static final double M_SQRT2 = Math.sqrt( 2. );
	//----------------------------
	// java code for easy using
	private static final long MAX_U_32_NUM = 0xFFFFFFFFL;
	private static final int ENCDELAY = 576;
	//
	private static final int FORMAT_UNKNOWN = 0;
	private static final int FORMAT_MP1 = 1;
	private static final int FORMAT_MP2 = 2;
	private static final int FORMAT_MP3 = 3;
	//
	private static final byte sAbl2[] = { 0, 7, 7, 7, 0, 7, 0, 0, 0, 0, 0, 8, 8, 8, 8, 8 };
	// output stream parameters
	/** true if big endian output */
	private final boolean mIsBigEndian;
	/** true if signed output */
	private final boolean mIsSigned;
	/** mono sample size in bytes. supporting 1, 2, 3 bytes */
	private final int mBytesPerSample;
	//
	private InputStream mInputStream = null;
	private boolean mIsEofReached = false;
	private int mInputFormat = FORMAT_UNKNOWN;
	private byte[] mId3v2TagBuff = null;
	/** true if mpeg header was parsed and following data was computed */
	private boolean mIsHeaderParsed = false;
	/** Number of channels */
	private int mNumChannels = 0;
	/** Sample rate */
	private int mSampleRate = 0;
	/** Bitrate */
	private int mBitrate = 0;
	/** Number of samples in mp3 file. Computed only if mpglib detects a Xing VBR header */
	private long mTotalNumSamples = 0;
	//
	private static final int OUT_SIZE = 1152 * 2;// 4096;
	private final float mOutUnclipped[] = new float[OUT_SIZE];
	//
	private final byte mByteBuffer[] = new byte[1024];
	// pcm buffer
	/** buffer for interleaved samples */
	private float mBuffer[] = null;
	/** number samples allocated */
	private int mNumSamplesAllocated = 0;
	/** number samples used */
	private int mNumSamplesUsed = 0;
	/** number samples to ignore at the beginning */
	private int mSkipStart = 0;
	/** number samples to ignore at the end */
	private int mSkipEnd = 0;
	// end pcm buffer
	/**
	 * Constructor with parameters for output byte stream.
	 *
	 * @param sampleSize sample size in bits, 16, 24 or 8 bits.
	 * @param isSigned true for signed data
	 * @param isBigEndian true for big endian data
	 * @throws IllegalArgumentException throws if sample size not one of the 8, 16, 24.
	 */
	public Mpg123(final int sampleSize, final boolean isSigned, final boolean isBigEndian) throws IllegalArgumentException {
		if( sampleSize != 16 && sampleSize != 24 && sampleSize != 8 ) {
			throw new IllegalArgumentException("Unsupported sample size: " + sampleSize);
		}
		mBytesPerSample = sampleSize >> 3;
		mIsSigned = isSigned;
		mIsBigEndian = isBigEndian;
	}
	/**
	 *
	 * @return count of channels
	 */
	public int getChannelCount() {
		return mNumChannels;
	}
	/**
	 *
	 * @return sample rate
	 */
	public int getSampleRate() {
		return mSampleRate;
	}
	/**
	 *
	 * @return Number of samples in mp3 file if Xing VBR header was detected, otherwise 0.
	 */
	public long getTotalNumSamples() {
		return mTotalNumSamples;
	}
	private static final int getLenOfId3v2Tag(final byte[] buf, int offset) {
		final int b0 = (int)buf[offset++] & 127;
		final int b1 = (int)buf[offset++] & 127;
		final int b2 = (int)buf[offset++] & 127;
		final int b3 = (int)buf[offset  ] & 127;
		return (((((b0 << 7) + b1) << 7) + b2) << 7) + b3;
	}
	private final boolean isSyncwordMp123(final byte[] data) {
		if( (data[0] & 0xFF) != 0xFF ) {
			return false;//* first 8 bits must be '1'
		}
		if( (data[1] & 0xE0) != 0xE0 ) {
			return false;// next 3 bits are also
		}
		if( (data[1] & 0x18) == 0x08) {
			return false;// no MPEG-1, -2 or -2.5
		}
		switch( data[1] & 0x06 ) {
		default:
		case 0x00:// illegal Layer
			mInputFormat = FORMAT_UNKNOWN;
			return false;

		case 0x02:// Layer3
			mInputFormat = FORMAT_MP3;
			break;

		case 0x04:// Layer2
			mInputFormat = FORMAT_MP2;
			break;

		case 0x06:// Layer1
			mInputFormat = FORMAT_MP1;
			break;
		}
		if( (data[2] & 0xF0) == 0xF0 ) {
			return false;// bad bitrate
		}
		if( (data[2] & 0x0C) == 0x0C ) {
			return false;// no sample frequency with (32,44.1,48)/(1,2,4)
		}
		if( (data[1] & 0x18) == 0x18 && (data[1] & 0x06) == 0x04 && ((sAbl2[((int)data[2] & 0xff) >> 4] & (1 << (((int)data[3] & 0xff) >> 6))) != 0) ) {
			return false;
		}
		if( (data[3] & 3) == 2 ) {
			return false;// reserved enphasis mode
		}
		return true;
	}
	/**
	 * For lame_decode:  return code
	 * -1     error
	 *  0     ok, but need more data before outputing any samples
	 *  n     number of mono samples output.  either 576 or 1152 depending on MP3 file.
	 *
	 */
    private final int decodeHeaders(final byte[] buffer, final int len,
		final float[] p, final int psize)
	{
		final int processed_mono_samples[] = new int[1];// java: processed_bytes changed to processed_mono_samples

		mIsHeaderParsed = false;

		final int ret = decodeMP3_unclipped( buffer, len, p, psize, processed_mono_samples );
		/* three cases:
		 * 1. headers parsed, but data not complete
		 *       pmp.header_parsed==1
		 *       pmp.framesize=0
		 *       pmp.fsizeold=size of last frame, or 0 if this is first frame
		 *
		 * 2. headers, data parsed, but ancillary data not complete
		 *       pmp.header_parsed==1
		 *       pmp.framesize=size of frame
		 *       pmp.fsizeold=size of last frame, or 0 if this is first frame
		 *
		 * 3. frame fully decoded:
		 *       pmp.header_parsed==0
		 *       pmp.framesize=0
		 *       pmp.fsizeold=size of frame (which is now the last frame)
		 *
		 */
		if( this.header_parsed || this.fsizeold > 0 || this.framesize > 0 ) {
			mIsHeaderParsed = true;
			mNumChannels = this.fr.stereo;
			mSampleRate = freqs[this.fr.sampling_frequency];

	        // free format, we need the entire frame before we can determine
	        // the bitrate.  If we haven't gotten the entire frame, bitrate=0
			if( this.fsizeold > 0 ) {
				mBitrate = (int)(8 * (4 + this.fsizeold) * mSampleRate /
						(1.e3 * this.framesize) + 0.5);
			} else if( this.framesize > 0 ) {
				mBitrate = (int)(8 * (4 + this.framesize) * mSampleRate /
						(1.e3 * this.framesize) + 0.5);
			} else {
				mBitrate = tabsel_123[this.fr.lsf][this.fr.lay - 1][this.fr.bitrate_index];
			}

			if( this.num_frames > 0 ) {
				// Xing VBR header found and num_frames was set
				mTotalNumSamples = this.framesize * this.num_frames;
			}
		}

		switch( ret ) {
		case Mpg123.MP3_OK:
			return processed_mono_samples[0];

		case Mpg123.MP3_NEED_MORE:
			return 0;
		}

		return -1;
	}
	/**
	 * Inspect a input stream
	 * @param is a input stream.
	 * @return 0 ok, this is mpeg stream, -1 an error.
	 */
	public final int open(final InputStream is) {
		try {
			final byte buf[] = new byte[100];
			if( is.read( buf, 0, 4 ) != 4 ) {
				return -1;
			}
			// TODO java: how to detect not a mpeg stream?
			if( (buf[0] == 'R') && (buf[1] == 'I') && (buf[2] == 'F') && (buf[3] == 'F') ) {//&&
				//	(buf[8] == 'W') && (buf[9] == 'A') && (buf[10] == 'V') && (buf[11] == 'E') ) {
				return -1;// RIFF/WAV stream found
			}
			if( (buf[0] == '.') && (buf[1] == 's') && (buf[2] == 'n') && (buf[3] == 'd') ) {
				return -1;// AU stream found
			}
			if( (buf[0] == 'F') && (buf[1] == 'O') && (buf[2] == 'R') && (buf[3] == 'M') ) {//&&
				//(buf[8] == 'A') && (buf[9] == 'I') && (buf[10] == 'F') && (buf[11] == 'F')) {
				return -1;// AIFF stream found
			}
			if( ((buf[0] == 'M') | (buf[0] == 'm')) && ((buf[1] == 'A') | (buf[1] == 'a')) && ((buf[2] == 'C') | (buf[2] == 'c')) ) {
				return -1;// APE stream found
			}
			if( ((buf[0] == 'F') | (buf[0] == 'f')) && ((buf[1] == 'L') | (buf[1] == 'l')) && ((buf[2] == 'A') | (buf[2] == 'a')) && ((buf[3] == 'C') | (buf[3] == 'c')) ) {
				return -1;// FLAC stream found
			}
			if( buf[0] == 'O' && buf[1] == 'g' && buf[2] == 'g' && buf[3] == 'S' ) {
				return -1;// Ogg stream found
			}
			while( buf[0] == 'I' && buf[1] == 'D' && buf[2] == '3' ) {
				if( is.read( buf, 4, 6 ) != 6 ) {
					return -1;
				}
				final int len = getLenOfId3v2Tag( buf, 6 );
				if( mId3v2TagBuff == null ) {
					mId3v2TagBuff = new byte[ 10 + len ];
					System.arraycopy( buf, 0, mId3v2TagBuff, 0, 10 );
					if( is.read( mId3v2TagBuff, 10, len ) != len ) {
						return -1;
					}
				}
				if( is.read( buf, 0, 4 ) != 4 ) {
					return -1;
				}
			}
			if( buf[0] == 'A' && buf[1] == 'i' && buf[2] == 'D' && buf[3] == '\1' ) {
				if( is.read( buf, 0, 2 ) != 2 ) {
					return -1;
				}
				int aid_header = ((int)buf[0] & 0xff) + (((int)buf[1] & 0xff) << 8);
				//System.out.printf("Album ID found. length = %d\n", aid_header );
				// skip rest of AID, except for 6 bytes we have already read
				aid_header -= 6;
				// skip (aid_header - 6) bytes
				for( final int read = is.read( buf, 0, aid_header ); read >= 0; ) {
					aid_header -= read;
					if( aid_header <= 0 ) {
						break;
					}
				}
				// read 4 more bytes to set up buffer for MP3 header check
				if( is.read( buf, 0, 4 ) != 4 ) {
					return -1;
				}
			}
			int len = OUT_SIZE;
			while( ! isSyncwordMp123( buf ) ) {
				buf[0] = buf[1];
				buf[1] = buf[2];
				buf[2] = buf[3];
				if( is.read( buf, 3, 1 ) != 1 ) {
					return -1;
				}
				if( --len <= 0 ) {
					return -1;// no sync word in 1152 bytes
				}
			}
			boolean freeformat = false;
			if( (buf[2] & 0xf0) == 0 ) {
				//System.out.println("Input file is freeformat.");
				freeformat = true;
			}
			int ret = decodeHeaders( buf, 4, mOutUnclipped, OUT_SIZE );
			if( -1 == ret ) {
				return -1;
			}
			// repeat until we decode a valid mp3 header.
			while( ! mIsHeaderParsed ) {
				len = is.read( buf );
				if( len != buf.length ) {
					return -1;
				}
				ret = decodeHeaders( buf, len, mOutUnclipped, OUT_SIZE );
				if( -1 == ret ) {
					return -1;
				}
			}
			if( mBitrate == 0 && ! freeformat ) {
				// throw new IOException("fail to sync...");
				return -1;
			}
			// if totalframes > 0, mpglib found a Xing VBR header and computed nsamp & totalframes
			if( this.num_frames <= 0 ) {
				// set as unknown. Later, we will take a guess based on file size ant bitrate
				mTotalNumSamples = MAX_U_32_NUM;
			}
			if( mNumChannels != 2 && mNumChannels != 1 ) {
				// System.err.printf("Unsupported number of channels: %d\n", mNumChannels );
				return -1;
			}
			switch( mInputFormat ) {
			case FORMAT_MP3:
				mSkipStart = ENCDELAY + 528 + 1;
				if( this.enc_delay > -1 ) {
					mSkipStart = this.enc_delay + 528 + 1;
				}
				if( this.enc_padding > -1 ) {
					mSkipEnd = this.enc_padding - (528 + 1);
					mSkipEnd = mSkipEnd < 0 ? 0 : mSkipEnd;
				}
				break;
			case FORMAT_MP2:
			case FORMAT_MP1:
				mSkipStart = 240 + 1;
				break;
			}
			mSkipStart *= mNumChannels;
			mSkipEnd *= mNumChannels;
			mSkipStart = mSkipStart < 0 ? 0 : mSkipStart;
			mInputStream = is;
			return 0;
		} catch(final IOException e) {
			return -1;
		}
	}
	private final int addBuffer(final float[] a, final int read) {
		if( read < 0 ) {
			return mNumSamplesUsed - mSkipEnd;
		}
		if( mSkipStart >= read ) {
			mSkipStart -= read;
			return mNumSamplesUsed - mSkipEnd;
		}
		final int a_want = read - mSkipStart;
		if( a_want > 0 ) {
			final int b_need = (mNumSamplesUsed + a_want);
			if( mNumSamplesAllocated < b_need ) {
				mNumSamplesAllocated = b_need;
				mBuffer = mBuffer == null ? new float[b_need] : Arrays.copyOf( mBuffer, b_need );
			}
			System.arraycopy( a, mSkipStart, mBuffer, mNumSamplesUsed, a_want );
			mNumSamplesUsed = b_need;
		}
		mSkipStart = 0;
		return mNumSamplesUsed - mSkipEnd;
	}
	/**
	 *
	 * @param buffer0
	 * @param buffer1
	 * @param samples
	 * @param b
	 * @param off
	 * @param sampleSize
	 * @param isSigned
	 * @param isBigEndian
	 */
	private final void convert(final float[] buffer, final int samples, final byte[] b, int off) {
		if( mBytesPerSample == 2 ) {
			if( mIsSigned ) {
				if( mIsBigEndian ) {
					// signed, big endian
					for( int i = 0; i < samples; i++ ) {
						final float x = buffer[i];
						if( x > 32767.0f ) {// 0x7fff
							b[off++] = 0x7f;
							b[off++] = -1;
						} else if( x < -32768.0f ) {// -0x8000
							b[off++] = -0x80;
							b[off++] = 0;
						} else {
							final short v = (short)(x > 0 ? x + 0.5f : x - 0.5f);
							b[off++] = (byte)(v >> 8);
							b[off++] = (byte)(v);
						}
					}
					return;
				}
				// signed, little endian
				for( int i = 0; i < samples; i++ ) {
					final float x = buffer[i];
					if( x > 32767.0f ) {// 0x7fff
						b[off++] = -1;
						b[off++] = 0x7f;
					} else if( x < -32768.0f ) {// -0x8000
						b[off++] = 0;
						b[off++] = -0x80;
					} else {
						final short v = (short)(x > 0 ? x + 0.5f : x - 0.5f);
						b[off++] = (byte)(v);
						b[off++] = (byte)(v >> 8);
					}
				}
				return;
			}// if signed
			if( mIsBigEndian ) {
				// unsigned, big endian
				for( int i = 0; i < samples; i++ ) {
					final float x = buffer[i] + 32768f;
					if( x > 32767.0f ) {// 0x7fff
						b[off++] = 0x7f;
						b[off++] = -1;
					} else if( x < -32768.0f ) {// -0x8000
						b[off++] = -0x80;
						b[off++] = 0;
					} else {
						final short v = (short)(x > 0 ? x + 0.5f : x - 0.5f);
						b[off++] = (byte)(v >> 8);
						b[off++] = (byte)(v);
					}
				}
				return;
			}
			// unsigned, little endian
			for( int i = 0; i < samples; i++ ) {
				final float x = buffer[i] + 32768f;
				if( x > 32767.0f ) {// 0x7fff
					b[off++] = -1;
					b[off++] = 0x7f;
				} else if( x < -32768.0f ) {// -0x8000
					b[off++] = 0;
					b[off++] = -0x80;
				} else {
					final short v = (short)(x > 0 ? x + 0.5f : x - 0.5f);
					b[off++] = (byte)(v);
					b[off++] = (byte)(v >> 8);
				}
			}
			return;
		}
		if( mBytesPerSample == 3 ) {// TODO dither
			if( mIsSigned ) {
				if( mIsBigEndian ) {
					// signed, big endian
					for( int i = 0; i < samples; i++ ) {
						float x = buffer[i];
						if( x > 32767.0f ) {// 0x7fff
							b[off++] = 0x7f;
							b[off++] = -1;
							b[off++] = -1;
						} else if( x < -32768.0f ) {// -0x8000
							b[off++] = -0x80;
							b[off++] = 0;
							b[off++] = 0;
						} else {
							x *= 256f;
							final int v = (short)(x > 0 ? x + 0.5f : x - 0.5f);
							b[off++] = (byte)(v >> 16);
							b[off++] = (byte)(v >> 8);
							b[off++] = (byte)(v);
						}
					}
					return;
				}
				// signed, stereo, little endian
				for( int i = 0; i < samples; i++ ) {
					float x = buffer[i];
					if( x > 32767.0f ) {// 0x7fff
						b[off++] = -1;
						b[off++] = -1;
						b[off++] = 0x7f;
					} else if( x < -32768.0f ) {// -0x8000
						b[off++] = 0;
						b[off++] = 0;
						b[off++] = -0x80;
					} else {
						x *= 256f;
						final int v = (short)(x > 0 ? x + 0.5f : x - 0.5f);
						b[off++] = (byte)(v);
						b[off++] = (byte)(v >> 8);
						b[off++] = (byte)(v >> 16);
					}
				}
				return;
			}// if signed
			if( mIsBigEndian ) {
				// unsigned, stereo, big endian
				for( int i = 0; i < samples; i++ ) {
					float x = buffer[i] + 32768f;
					if( x > 32767.0f ) {// 0x7fff
						b[off++] = 0x7f;
						b[off++] = -1;
						b[off++] = -1;
					} else if( x < -32768.0f ) {// -0x8000
						b[off++] = -0x80;
						b[off++] = 0;
						b[off++] = 0;
					} else {
						x *= 256f;
						final int v = (short)(x > 0 ? x + 0.5f : x - 0.5f);
						b[off++] = (byte)(v >> 16);
						b[off++] = (byte)(v >> 8);
						b[off++] = (byte)(v);
					}
				}
				return;
			}
			// unsigned, stereo, little endian
			for( int i = 0; i < samples; i++ ) {
				float x = buffer[i] + 32768f;
				if( x > 32767.0f ) {// 0x7fff
					b[off++] = -1;
					b[off++] = -1;
					b[off++] = 0x7f;
				} else if( x < -32768.0f ) {// -0x8000
					b[off++] = 0;
					b[off++] = 0;
					b[off++] = -0x80;
				} else {
					x *= 256f;
					final int v = (short)(x > 0 ? x + 0.5f : x - 0.5f);
					b[off++] = (byte)(v);
					b[off++] = (byte)(v >> 8);
					b[off++] = (byte)(v >> 16);
				}
			}
			return;
		}
		if( mBytesPerSample == 1 ) {
			if( mIsSigned ) {
				// signed
				for( int i = 0; i < samples; i++ ) {
					final float x = buffer[i];
					if( x > 32767.0f ) {// 0x7fff
						b[off++] = 0x7f;
					} else if( x < -32768.0f ) {// -0x8000
						b[off++] = -0x80;
					} else {
						final short v = (short)(x > 0 ? x + 0.5f : x - 0.5f);
						b[off++] = (byte)(v >> 8);
					}
				}
				return;
			}
			// unsigned
			for( int i = 0; i < samples; i++ ) {
				final float x = buffer[i] + 32768f;
				if( x > 32767.0f ) {// 0x7fff
					b[off++] = 0x7f;
				} else if( x < -32768.0f ) {// -0x8000
					b[off++] = -0x80;
				} else {
					final short v = (short)(x > 0 ? x + 0.5f : x - 0.5f);
					b[off++] = (byte)(v >> 8);
				}
			}
			return;
		}
	}
	private final int getBuffer(final byte[] b, final int off, final int len) {
		final int samples_in_buffer = mNumSamplesUsed - mSkipEnd;
		if( samples_in_buffer <= 0 ) {
			return 0;
		}
		int take = len / mBytesPerSample;
		if( take > samples_in_buffer ) {
			take = samples_in_buffer;
		}
		if( take > 0 ) {
			convert( mBuffer, take, b, off );
			mNumSamplesUsed -= take;
			if( mNumSamplesUsed < 0 ) {
				mNumSamplesUsed = 0;
				return take * mBytesPerSample;
			}
			System.arraycopy( mBuffer, take, mBuffer, 0, mNumSamplesUsed );
			return take * mBytesPerSample;
		}
		return 0;
	}
	/**
	 *
	 * @param buffer a buffer to hold packed PCM data for return
	 * @param off the start offset in array <code>buffer</code>
	 * 			at which the data is written.
	 * @param len the byte length requested to be placed into buffer
	 */
	public final int read(final byte[] b, final int off, final int len) throws IOException {
		while( true ) {
			int used = getBuffer( b, off, len );
			if( used != 0 ) {
				return used;
			}
			if( mIsEofReached ) {
				return -1;
			}
			//
			final float[] out = mOutUnclipped;
			final byte[] byte_buff = mByteBuffer;
			int read;
			do {
				final int num_channels = mNumChannels;
				final int sample_rate = mSampleRate;
				//
				int num = 0;
				// read until we get a valid output frame
				while( (read = decodeHeaders( byte_buff, num, out, OUT_SIZE )) == 0 ) {
					num = mInputStream.read( byte_buff, 0, 1024 );
					if( num <= 0 ) {// java: len = -1 if eof
						num = 0;// java: len = -1 if eof
						// we are done reading the file, but check for buffered data
						read = decodeHeaders( byte_buff, num, out, OUT_SIZE );
						if( read <= 0 ) {
							read = -1;// done with file
						}
						break;
					}
				}
				// read < 0:  error, probably EOF
				// read = 0:  not possible with lame_decode_fromfile() ???
				// read > 0:  number of output samples
				if( read < 0 ) {
					int i = OUT_SIZE;
					do {
						out[--i] = 0;
					} while( i > 0 );
					read = 0;
					mIsEofReached = true;
				}

				if( num_channels != mNumChannels ) {
					throw new IOException("Error: number of channels has changed - not supported");
				}
				if( mSampleRate != sample_rate ) {
					throw new IOException("Error: sample frequency has changed - not supported");
				}
				used = addBuffer( out, read );
			} while( used <= 0 && read > 0 );
		}// while no data in the buffer
	}
}
