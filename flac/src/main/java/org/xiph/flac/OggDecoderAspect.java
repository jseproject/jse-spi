/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 2000-2009 Josh Coalson
 * Copyright (c) 2011-2022 Xiph.Org Foundation
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Neither the name of the Xiph.Org Foundation nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE FOUNDATION OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.xiph.flac;

import java.io.IOException;

@SuppressWarnings("unused")// for version_minor
class OggDecoderAspect extends OggMapping {

	//typedef enum {// java: used IOException and -1 as End Of Stream
		//static final int FLAC__OGG_DECODER_ASPECT_READ_STATUS_OK = 0;
		//static final int FLAC__OGG_DECODER_ASPECT_READ_STATUS_END_OF_STREAM = 1;
		static final int FLAC__OGG_DECODER_ASPECT_READ_STATUS_LOST_SYNC = 2;
		static final int FLAC__OGG_DECODER_ASPECT_READ_STATUS_NOT_FLAC = 3;
		static final int FLAC__OGG_DECODER_ASPECT_READ_STATUS_UNSUPPORTED_MAPPING_VERSION = 4;
		//static final int FLAC__OGG_DECODER_ASPECT_READ_STATUS_ABORT = 5;
		static final int FLAC__OGG_DECODER_ASPECT_READ_STATUS_ERROR = 6;
		static final int FLAC__OGG_DECODER_ASPECT_READ_STATUS_MEMORY_ALLOCATION_ERROR = 7;
	//} FLAC__OggDecoderAspectReadStatus;

	/* these are storage for values that can be set through the API */
	private boolean use_first_serial_number = false;
	private int serial_number;

	/* these are for internal state related to Ogg decoding */
	private final OggStreamState stream_state = new OggStreamState();
	private final OggSyncState sync_state = new OggSyncState();
	private int version_major = 0, version_minor = 0;
	private boolean need_serial_number = false;
	private boolean end_of_stream = false;
	/** only if true will the following vars be valid */
	private boolean have_working_page = false;
	private final OggPage working_page = new OggPage();
	/** only if true will the following vars be valid */
	private boolean have_working_packet = false;
	/** as we work through the packet we will move working_packet.packet forward and working_packet.bytes down */
	private final OggPacket working_packet = new OggPacket();

	/***********************************************************************
	 *
	 * Public class methods
	 *
	 ***********************************************************************/

	final boolean init()
	{
		/* we will determine the serial number later if necessary */
		if( this.stream_state.init( this.serial_number ) != 0 ) {
			return false;
		}

		this.sync_state.init();

		this.version_major = ~(0);
		this.version_minor = ~(0);

		this.need_serial_number = this.use_first_serial_number;

		this.end_of_stream = false;
		this.have_working_page = false;

		return true;
	}

	final void finish()
	{
		this.sync_state.clear();
		this.stream_state.clear();
	}

	final void set_serial_number(final int value)
	{
		this.use_first_serial_number = false;
		this.serial_number = value;
	}

	final void set_defaults()
	{
		this.use_first_serial_number = true;
	}

	final void flush()
	{
		this.stream_state.reset();
		this.sync_state.reset();
		this.end_of_stream = false;
		this.have_working_page = false;
	}

	final void reset()
	{
		flush();

		if( this.use_first_serial_number ) {
			this.need_serial_number = true;
		}
	}

	private static final int OGG_BYTES_CHUNK = 8192;
	// java: changed
	/**
     * Reads up to <code>bytes</code> bytes of data from this file into an
     * array of bytes. This method blocks until at least one byte of input
     * is available.
     * <p>
     *
     * @param      buffer the buffer into which the data is read.
     * @param      bytes  the maximum number of bytes read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end of
     *             the file has been reached.
     * @exception  IOException If the first byte cannot be read for any reason
     * other than end of file, or if the random access file has been closed, or if
     * some other I/O error occurs.
     * @exception  NullPointerException If <code>b</code> is <code>null</code>.
     * @exception  IndexOutOfBoundsException If <code>offset</code> is negative,
     * <code>bytes</code> is negative, or <code>bytes</code> is greater than
     * <code>buffer.length - offset</code>
     */
	final int /*FLAC__OggDecoderAspectReadStatus*/ read_callback_wrapper(final byte buffer[], int bytes,
																		 final OggDecoderAspectReadCallbackProxy read_callback, final StreamDecoder decoder/*, final Object client_data*/)
			throws IOException
	{
		final int bytes_requested = bytes;
		int offset = 0;

		/*
		 * The FLAC decoding API uses pull-based reads, whereas Ogg decoding
		 * is push-based.  In libFLAC, when you ask to decode a frame, the
		 * decoder will eventually call the read callback to supply some data,
		 * but how much it asks for depends on how much free space it has in
		 * its internal buffer.  It does not try to grow its internal buffer
		 * to accommodate a whole frame because then the internal buffer size
		 * could not be limited, which is necessary in embedded applications.
		 *
		 * Ogg however grows its internal buffer until a whole page is present;
		 * only then can you get decoded data out.  So we can't just ask for
		 * the same number of bytes from Ogg, then pass what's decoded down to
		 * libFLAC.  If what libFLAC is asking for will not contain a whole
		 * page, then we will get no data from ogg_sync_pageout(), and at the
		 * same time cannot just read more data from the client for the purpose
		 * of getting a whole decoded page because the decoded size might be
		 * larger than libFLAC's internal buffer.
		 *
		 * Instead, whenever this read callback wrapper is called, we will
		 * continually request data from the client until we have at least one
		 * page, and manage pages internally so that we can send pieces of
		 * pages down to libFLAC in such a way that we obey its size
		 * requirement.  To limit the amount of callbacks, we will always try
		 * to read in enough pages to return the full number of bytes
		 * requested.
		 */
		bytes = 0;
		final OggPacket ogg_packet = this.working_packet;// java
		while( bytes < bytes_requested && ! this.end_of_stream ) {
			if( this.have_working_page ) {
				if( this.have_working_packet ) {
					int n = bytes_requested - bytes;
					if( ogg_packet.bytes <= n ) {
						/* the rest of the packet will fit in the buffer */
						n = ogg_packet.bytes;
						System.arraycopy( ogg_packet.packet_base, ogg_packet.packet, buffer, offset, n );
						bytes += n;
						offset += n;
						this.have_working_packet = false;
					}
					else {
						/* only n bytes of the packet will fit in the buffer */
						System.arraycopy( ogg_packet.packet_base, ogg_packet.packet, buffer, offset, n );
						bytes += n;
						offset += n;
						ogg_packet.packet += n;
						ogg_packet.bytes -= n;
					}
				}
				else {
					/* try and get another packet */
					final int ret = this.stream_state.packetout( ogg_packet );
					if( ret > 0 ) {
						this.have_working_packet = true;
						/* if it is the first header packet, check for magic and a supported Ogg FLAC mapping version */
						if( ogg_packet.bytes > 0 && ogg_packet.packet_base[ogg_packet.packet] == FLAC__OGG_MAPPING_FIRST_HEADER_PACKET_TYPE) {
							final byte[] packet = ogg_packet.packet_base;
							int b = ogg_packet.packet;
							final int header_length =
								FLAC__OGG_MAPPING_PACKET_TYPE_LENGTH +
										FLAC__OGG_MAPPING_MAGIC_LENGTH +
										FLAC__OGG_MAPPING_VERSION_MAJOR_LENGTH +
										FLAC__OGG_MAPPING_VERSION_MINOR_LENGTH +
										FLAC__OGG_MAPPING_NUM_HEADERS_LENGTH;
							if( ogg_packet.bytes < header_length ) {
								return FLAC__OGG_DECODER_ASPECT_READ_STATUS_NOT_FLAC;
							}
							b += FLAC__OGG_MAPPING_PACKET_TYPE_LENGTH;
							int i = FLAC__OGG_MAPPING_MAGIC_LENGTH;
							b += i;
							do {
								if( packet[--b] != FLAC__OGG_MAPPING_MAGIC[--i] ) {
									return FLAC__OGG_DECODER_ASPECT_READ_STATUS_NOT_FLAC;
								}
							} while( i > 0 );
							b += FLAC__OGG_MAPPING_MAGIC_LENGTH;
							this.version_major = ((int)packet[b]) & 0xff;
							b += FLAC__OGG_MAPPING_VERSION_MAJOR_LENGTH;
							this.version_minor = ((int)packet[b]) & 0xff;
							if( this.version_major != 1 ) {
								return FLAC__OGG_DECODER_ASPECT_READ_STATUS_UNSUPPORTED_MAPPING_VERSION;
							}
							ogg_packet.packet += header_length;
							ogg_packet.bytes -= header_length;
						}
					}
					else if( ret == 0 ) {
						this.have_working_page = false;
					}
					else { /* ret < 0 */
						/* lost sync, we'll leave the working page for the next call */
						return bytes;// return FLAC__OGG_DECODER_ASPECT_READ_STATUS_LOST_SYNC;
					}
				}
			}
			else {
				/* try and get another page */
				final int ret = this.sync_state.pageout( this.working_page );
				if( ret > 0 ) {
					/* got a page, grab the serial number if necessary */
					if( this.need_serial_number ) {
						this.stream_state.serialno = this.serial_number = this.working_page.serialno();
						this.need_serial_number = false;
					}
					if( this.stream_state.pagein( this.working_page ) == 0 ) {
						this.have_working_page = true;
						this.have_working_packet = false;
					}
					/* else do nothing, could be a page from another stream */
				}
				else if( ret == 0 ) {
					/* need more data */
					int ogg_bytes_to_read = bytes_requested - bytes;
					if( ogg_bytes_to_read < OGG_BYTES_CHUNK ) {
						ogg_bytes_to_read = OGG_BYTES_CHUNK;
					}
					final int oggbuf_offset = this.sync_state.buffer( ogg_bytes_to_read );
					final byte[] oggbuf = this.sync_state.data;

					if( oggbuf_offset < 0 ) {
						return FLAC__OGG_DECODER_ASPECT_READ_STATUS_MEMORY_ALLOCATION_ERROR;
					}
					else {
						ogg_bytes_to_read = read_callback.ogg_read_callback( decoder, oggbuf, oggbuf_offset, ogg_bytes_to_read/*, client_data*/ );
						if( ogg_bytes_to_read < 0 ) {
							this.end_of_stream = true;
						}
						/*
						switch( read_callback.ogg_read_callback( decoder, oggbuf, oggbuf_offset, ogg_bytes_to_read, client_data ) ) {
							case FLAC__OGG_DECODER_ASPECT_READ_STATUS_OK:
								break;
							case FLAC__OGG_DECODER_ASPECT_READ_STATUS_END_OF_STREAM:
								this.end_of_stream = true;
								break;
							case FLAC__OGG_DECODER_ASPECT_READ_STATUS_ABORT:
								return FLAC__OGG_DECODER_ASPECT_READ_STATUS_ABORT;
							default:
								//FLAC__ASSERT(0);
						}
						*/
						if( this.sync_state.wrote( ogg_bytes_to_read ) < 0 ) {
							/* double protection; this will happen if the read callback returns more bytes than the max requested, which would overflow Ogg's internal buffer */
							//FLAC__ASSERT(0);
							return FLAC__OGG_DECODER_ASPECT_READ_STATUS_ERROR;
						}
					}
				}
				else { /* ret < 0 */
					/* lost sync, bail out */
					return FLAC__OGG_DECODER_ASPECT_READ_STATUS_LOST_SYNC;
				}
			}
		}

		if( this.end_of_stream && bytes == 0 ) {
			return -1;//FLAC__OGG_DECODER_ASPECT_READ_STATUS_END_OF_STREAM;
		}

		return bytes;//FLAC__OGG_DECODER_ASPECT_READ_STATUS_OK;
	}
}
