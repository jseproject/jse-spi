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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class MetadataChain extends MetadataBase implements
		StreamDecoderReadCallback,
		StreamDecoderWriteCallback,
		StreamDecoderMetadataCallback,
		StreamDecoderErrorCallback
{
	//typedef enum {
		/** The chain is in the normal OK state */
		private static final int FLAC__METADATA_CHAIN_STATUS_OK = 0;

		/** The data passed into a function violated the function's usage criteria */
		private static final int FLAC__METADATA_CHAIN_STATUS_ILLEGAL_INPUT = 1;

		/** The chain could not open the target file */
		private static final int FLAC__METADATA_CHAIN_STATUS_ERROR_OPENING_FILE = 2;

		/** The chain could not find the FLAC signature at the start of the file */
		private static final int FLAC__METADATA_CHAIN_STATUS_NOT_A_FLAC_FILE = 3;

		/** The chain tried to write to a file that was not writable */
		private static final int FLAC__METADATA_CHAIN_STATUS_NOT_WRITABLE = 4;

		/** The chain encountered input that does not conform to the FLAC metadata specification */
		private static final int FLAC__METADATA_CHAIN_STATUS_BAD_METADATA = 5;

		/** The chain encountered an error while reading the FLAC file */
		private static final int FLAC__METADATA_CHAIN_STATUS_READ_ERROR = 6;

		/** The chain encountered an error while seeking in the FLAC file */
		private static final int FLAC__METADATA_CHAIN_STATUS_SEEK_ERROR = 7;

		/** The chain encountered an error while writing the FLAC file */
		private static final int FLAC__METADATA_CHAIN_STATUS_WRITE_ERROR = 8;

		/** The chain encountered an error renaming the FLAC file */
		private static final int FLAC__METADATA_CHAIN_STATUS_RENAME_ERROR = 9;

		/** The chain encountered an error removing the temporary file */
		private static final int FLAC__METADATA_CHAIN_STATUS_UNLINK_ERROR = 10;

		/** Memory allocation failed */
		private static final int FLAC__METADATA_CHAIN_STATUS_MEMORY_ALLOCATION_ERROR = 11;

		/** The caller violated an assertion or an unexpected error occurred */
		private static final int FLAC__METADATA_CHAIN_STATUS_INTERNAL_ERROR = 12;

		/** One or more of the required callbacks was NULL */
		private static final int FLAC__METADATA_CHAIN_STATUS_INVALID_CALLBACKS = 13;

		/** FLAC__metadata_chain_write() was called on a chain read by
		 *   FLAC__metadata_chain_read_with_callbacks()/FLAC__metadata_chain_read_ogg_with_callbacks(),
		 *   or
		 *   FLAC__metadata_chain_write_with_callbacks()/FLAC__metadata_chain_write_with_callbacks_and_tempfile()
		 *   was called on a chain read by
		 *   FLAC__metadata_chain_read()/FLAC__metadata_chain_read_ogg().
		 *   Matching read/write methods must always be used. */
		private static final int FLAC__METADATA_CHAIN_STATUS_READ_WRITE_MISMATCH = 14;

		/** FLAC__metadata_chain_write_with_callbacks() was called when the
		 *   chain write requires a tempfile; use
		 *   FLAC__metadata_chain_write_with_callbacks_and_tempfile() instead.
		 *   Or, FLAC__metadata_chain_write_with_callbacks_and_tempfile() was
		 *   called when the chain write does not require a tempfile; use
		 *   FLAC__metadata_chain_write_with_callbacks() instead.
		 *   Always check FLAC__metadata_chain_check_if_tempfile_needed()
		 *   before writing via callbacks. */
		private static final int FLAC__METADATA_CHAIN_STATUS_WRONG_WRITE_CALL = 15;

	//} FLAC__Metadata_ChainStatus;

	private String filename = null; /* will be NULL if using callbacks */
	private boolean is_ogg = false;
	MetadataNode head = null;
	MetadataNode tail = null;
	int nodes = 0;
	private int /* FLAC__Metadata_ChainStatus */ status = FLAC__METADATA_CHAIN_STATUS_OK;
	private long first_offset = 0, last_offset = 0;
	/*
	 * This is the length of the chain initially read from the FLAC file.
	 * it is used to compare against the current length to decide whether
	 * or not the whole file has to be rewritten.
	 */
	private long initial_length = 0;
	/* @@@ hacky, these are currently only needed by ogg reader */
	//Object /* FLAC__IOHandle */ handle = null;
	//IOCallback_Read read_cb = null;
	private RandomAccessFile handle;// java: FLAC__IOHandle and FLAC__IOCallback_Read are changed to file.read

	private final void clear() {
		filename = null;
		is_ogg = false;
		head = null;
		tail = null;
		nodes = 0;
		status = FLAC__METADATA_CHAIN_STATUS_OK;
		first_offset = 0;
		last_offset = 0;
		initial_length = 0;
		handle = null;
	}

	public final void delete()
	{
		//FLAC__ASSERT(0 != chain);

		clear_();

		//free(chain);
	}

	private final void clear_()
	{
		MetadataNode node, next;

		//FLAC__ASSERT(0 != chain);

		for( node = this.head; node != null; ) {
			next = node.next;
			node.delete_();
			node = next;
		}

		this.filename = null;

		this.clear();
	}

	private final void append_node_(final MetadataNode node)
	{
		//FLAC__ASSERT(0 != chain);
		//FLAC__ASSERT(0 != node);
		//FLAC__ASSERT(0 != node->data);

		node.next = node.prev = null;
		node.data.is_last = true;
		if( null != this.tail ) {
			this.tail.data.is_last = false;
		}

		if( null == this.head ) {
			this.head = node;
		} else {
			//FLAC__ASSERT(0 != chain.tail);
			this.tail.next = node;
			node.prev = this.tail;
		}
		this.tail = node;
		this.nodes++;
	}

	private final void remove_node_(final MetadataNode node)
	{
		//FLAC__ASSERT(0 != chain);
		//FLAC__ASSERT(0 != node);

		if( node == this.head ) {
			this.head = node.next;
		} else {
			node.prev.next = node.next;
		}

		if( node == this.tail ) {
			this.tail = node.prev;
		} else {
			node.next.prev = node.prev;
		}

		if( null != this.tail ) {
			this.tail.data.is_last = true;
		}

		this.nodes--;
	}

	final void delete_node_(final MetadataNode node)
	{
		remove_node_( node );
		node.delete_();
	}

	private final long calculate_length_()
	{
		MetadataNode node;
		long length = 0;
		for( node = this.head; node != null; node = node.next ) {
			length += (Format.FLAC__STREAM_METADATA_HEADER_LENGTH + node.data.length);
		}
		return length;
	}

	/* return true iff node and node->next are both padding */
	private final boolean merge_adjacent_padding_(final MetadataNode node)
	{
		if( node.data.type == Format.FLAC__METADATA_TYPE_PADDING && null != node.next && node.next.data.type == Format.FLAC__METADATA_TYPE_PADDING ) {
			final int growth = Format.FLAC__STREAM_METADATA_HEADER_LENGTH + node.next.data.length;
			node.data.length += growth; /* new block size can be greater than max metadata block size, but it'll be fixed later in chain_prepare_for_write_() */

			delete_node_( node.next );
			return true;
		} else {
			return false;
		}
	}

	/** Returns the new length of the chain, or 0 if there was an error. */
	/* WATCHOUT: This can get called multiple times before a write, so
	 * it should still work when this happens.
	 */
	/* WATCHOUT: Make sure to also update the logic in
	 * FLAC__metadata_chain_check_if_tempfile_needed() if the logic here changes.
	 */
	private final long prepare_for_write_(final boolean use_padding)
	{
		long current_length = calculate_length_();

		if( use_padding ) {
			/* if the metadata shrank and the last block is padding, we just extend the last padding block */
			if( current_length < this.initial_length && this.tail.data.type == Format.FLAC__METADATA_TYPE_PADDING ) {
				final long delta = this.initial_length - current_length;
				this.tail.data.length += delta;
				current_length += delta;
				//FLAC__ASSERT(current_length == chain->initial_length);
			}
			/* if the metadata shrank more than 4 bytes then there's room to add another padding block */
			else if( current_length + Format.FLAC__STREAM_METADATA_HEADER_LENGTH <= this.initial_length ) {
				StreamMetadata padding;
				MetadataNode node;
				padding = StreamMetadata.metadata_new( Format.FLAC__METADATA_TYPE_PADDING );
				padding.length = (int)(this.initial_length - (Format.FLAC__STREAM_METADATA_HEADER_LENGTH + current_length));
				node = new MetadataNode();
				node.data = padding;
				append_node_( node );
				current_length = calculate_length_();
				//FLAC__ASSERT(current_length == chain->initial_length);
			}
			/* if the metadata grew but the last block is padding, try cutting the padding to restore the original length so we don't have to rewrite the whole file */
			else if( current_length > this.initial_length ) {
				final long delta = current_length - this.initial_length;
				if( this.tail.data.type == Format.FLAC__METADATA_TYPE_PADDING ) {
					/* if the delta is exactly the size of the last padding block, remove the padding block */
					if( (long)this.tail.data.length + (long) Format.FLAC__STREAM_METADATA_HEADER_LENGTH == delta ) {
						delete_node_( this.tail );
						current_length = calculate_length_();
						//FLAC__ASSERT(current_length == chain.initial_length);
					}
					/* if there is at least 'delta' bytes of padding, trim the padding down */
					else if( this.tail.data.length >= delta ) {
						this.tail.data.length -= delta;
						current_length -= delta;
						//FLAC__ASSERT(current_length == chain->initial_length);
					}
				}
			}
		}

		/* check sizes of all metadata blocks; reduce padding size if necessary */
		{
			for(MetadataNode node = this.head; node != null; node = node.next ) {
				if( node.data.length >= (1 << Format.FLAC__STREAM_METADATA_LENGTH_LEN) ) {
					if( node.data.type == Format.FLAC__METADATA_TYPE_PADDING ) {
						node.data.length = (1 << Format.FLAC__STREAM_METADATA_LENGTH_LEN) - 1;
						current_length = calculate_length_();
					} else {
						this.status = FLAC__METADATA_CHAIN_STATUS_BAD_METADATA;
						return 0;
					}
				}
			}
		}

		return current_length;
	}

	/****************************************************************************
	 *
	 * Local function definitions
	 *
	 ***************************************************************************/

	private final boolean read_cb_(final RandomAccessFile iohandle)// java: changed
			//IOHandle handle, IOCallback_Read read_cb, IOCallback_Seek seek_cb, IOCallback_Tell tell_cb)
	{
		//FLAC__ASSERT(0 != chain);

		/* we assume we're already at the beginning of the file */

		switch( seek_to_first_metadata_block_cb_( iohandle /*handle, read_cb, seek_cb*/ ) ) {
			case 0:
				break;
			case 1:
				this.status = FLAC__METADATA_CHAIN_STATUS_READ_ERROR;
				return false;
			case 2:
				this.status = FLAC__METADATA_CHAIN_STATUS_SEEK_ERROR;
				return false;
			case 3:
				this.status = FLAC__METADATA_CHAIN_STATUS_NOT_A_FLAC_FILE;
				return false;
			default:
				//FLAC__ASSERT(0);
				return false;
		}

		{
			try {
				final long pos = iohandle.getFilePointer();
				this.first_offset = pos;
			} catch(final IOException e) {
				this.status = FLAC__METADATA_CHAIN_STATUS_READ_ERROR;
				return false;
			}
		}

		{
			metadata_block_header_helper h;
			do {
				final MetadataNode node = new MetadataNode();

				if( null == (h = read_metadata_block_header_cb_( iohandle/*, handle, read_cb, is_last, type, length */)) ) {
					node.delete_();
					this.status = FLAC__METADATA_CHAIN_STATUS_READ_ERROR;
					return false;
				}

				node.data = StreamMetadata.metadata_new( h.type );
				if( null == node.data ) {
					node.delete_();
					this.status = FLAC__METADATA_CHAIN_STATUS_MEMORY_ALLOCATION_ERROR;
					return false;
				}

				node.data.is_last = h.is_last;
				node.data.length = h.length;

				this.status = get_equivalent_status_( read_metadata_block_data_cb_( iohandle, /*handle, read_cb, seek_cb,*/ node.data ) );
				if( this.status != FLAC__METADATA_CHAIN_STATUS_OK ) {
					node.delete_();
					return false;
				}
				append_node_( node );
			} while( ! h.is_last );
		}

		{
			try {
				final long pos = iohandle.getFilePointer();
				this.last_offset = pos;
			} catch(final IOException e) {
				this.status = FLAC__METADATA_CHAIN_STATUS_READ_ERROR;
				return false;
			}
		}

		this.initial_length = calculate_length_();

		return true;
	}

	private final boolean read_ogg_cb_(final RandomAccessFile iohandle)// java: changed
			//IOHandle handle, IOCallback_Read read_cb)
	{
		//FLAC__ASSERT(0 != chain);

		/* we assume we're already at the beginning of the file */

		//chain.handle = handle;
		//chain.read_cb = read_cb;
		this.handle = iohandle;
		final StreamDecoder decoder = new StreamDecoder();
		decoder.set_metadata_respond_all();
		if( decoder.init_ogg_stream(
				this,// chain_read_ogg_read_cb_,
				/*seek_callback=*/null,
				/*tell_callback=*/null,
				/*length_callback=*/null,
				/*eof_callback=*/null,
				this,// chain_read_ogg_write_cb_,
				this,// chain_read_ogg_metadata_cb_,
				this//,// chain_read_ogg_error_cb_,
				/* this */ ) != StreamDecoder.FLAC__STREAM_DECODER_INIT_STATUS_OK ) {
			decoder.delete();
			this.status = FLAC__METADATA_CHAIN_STATUS_INTERNAL_ERROR; /*@@@ maybe needs better error code */
			return false;
		}

		this.first_offset = 0; /*@@@ wrong; will need to be set correctly to implement metadata writing for Ogg FLAC */

		if( ! decoder.process_until_end_of_metadata() ) {
			this.status = FLAC__METADATA_CHAIN_STATUS_INTERNAL_ERROR; /*@@@ maybe needs better error code */
		}
		if( this.status != FLAC__METADATA_CHAIN_STATUS_OK ) {
			decoder.delete();
			return false;
		}

		decoder.delete();

		this.last_offset = 0; /*@@@ wrong; will need to be set correctly to implement metadata writing for Ogg FLAC */

		this.initial_length = calculate_length_();

		if( this.initial_length == 0 ) {
			/* Ogg FLAC file must have at least streaminfo and vorbis comment */
			this.status = FLAC__METADATA_CHAIN_STATUS_BAD_METADATA;
			return false;
		}

		return true;
	}

	private final boolean rewrite_metadata_in_place_cb_(final RandomAccessFile iohandle)// java: changed
			//FLAC__IOHandle handle, FLAC__IOCallback_Write write_cb, FLAC__IOCallback_Seek seek_cb)
	{
		//FLAC__ASSERT(0 != chain);
		//FLAC__ASSERT(0 != chain->head);

		try {
			iohandle.seek( this.first_offset/*, SEEK_SET*/ );
		} catch(final IOException e) {
			this.status = FLAC__METADATA_CHAIN_STATUS_SEEK_ERROR;
			return false;
		}

		for(MetadataNode node = this.head; node != null; node = node.next ) {
			if( ! write_metadata_block_header_cb_( iohandle/*, write_cb*/, node.data ) ) {
				this.status = FLAC__METADATA_CHAIN_STATUS_WRITE_ERROR;
				return false;
			}
			if( ! write_metadata_block_data_cb_( iohandle/*, write_cb*/, node.data ) ) {
				this.status = FLAC__METADATA_CHAIN_STATUS_WRITE_ERROR;
				return false;
			}
		}

		/*FLAC__ASSERT(fflush(), ftello() == chain->last_offset);*/

		this.status = FLAC__METADATA_CHAIN_STATUS_OK;
		return true;
	}

	private final boolean rewrite_metadata_in_place_()
	{
		RandomAccessFile file = null;
		boolean ret;

		//FLAC__ASSERT(0 != chain->filename);

		try {
			file = new RandomAccessFile( this.filename, "rw" );

			/* chain_rewrite_metadata_in_place_cb_() sets chain->status for us */
			ret = rewrite_metadata_in_place_cb_( file/*, (FLAC__IOCallback_Write)fwrite, fseek_wrapper_*/ );

		} catch(final FileNotFoundException e) {
			this.status = FLAC__METADATA_CHAIN_STATUS_ERROR_OPENING_FILE;
			return false;
		} finally {
			if( file != null ) {
				try { file.close(); } catch( final IOException e ) {}
			}
		}

		return ret;
	}

	private final boolean rewrite_file_(final String tempfile_path_prefix)
	{
		RandomAccessFile f = null, tempfile = null;
		int /* FLAC__Metadata_SimpleIteratorStatus */ ret;

		//FLAC__ASSERT(0 != chain);
		//FLAC__ASSERT(0 != chain->filename);
		//FLAC__ASSERT(0 != chain->head);
		// java: changed
		/*if(!open_tempfile_(chain->filename, tempfile_path_prefix, &tempfile, &tempfilename, &status)) {
			chain->status = get_equivalent_status_(status);
			cleanup_tempfile_(&tempfile, &tempfilename);
			return false;
		}*/
		final String tempfilename = get_tempfile_name_( this.filename, tempfile_path_prefix );
		try {
			tempfile = new RandomAccessFile( tempfilename, "rw" );
		} catch(final FileNotFoundException e) {
			this.status = get_equivalent_status_( FLAC__METADATA_SIMPLE_ITERATOR_STATUS_ERROR_OPENING_FILE );
			cleanup_tempfile_( tempfile, tempfilename );
			return false;
		}
		try {
			/* copy the file prefix (data up to first metadata block */
			f = new RandomAccessFile( this.filename, "r" );
			if( FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK != (ret = copy_n_bytes_from_file_( f, tempfile, this.first_offset )) ) {
				this.status = get_equivalent_status_( ret );
				cleanup_tempfile_( tempfile, tempfilename );
				return false;
			}

			/* write the metadata */
			for(MetadataNode node = this.head; node != null; node = node.next ) {
				if( FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK != (ret = write_metadata_block_header_( tempfile, node.data )) ) {
					this.status = get_equivalent_status_( ret );
					cleanup_tempfile_( tempfile, tempfilename );
					return false;
				}
				if( FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK != (ret = write_metadata_block_data_( tempfile, node.data )) ) {
					this.status = get_equivalent_status_( ret );
					cleanup_tempfile_( tempfile, tempfilename );
					return false;
				}
			}
			/*FLAC__ASSERT(fflush(), ftello() == chain->last_offset);*/

			/* copy the file postfix (everything after the metadata) */
			f.seek( this.last_offset );

			if( FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK != (ret = copy_remaining_bytes_from_file_( f, tempfile )) ) {
				cleanup_tempfile_( tempfile, tempfilename );
				this.status = get_equivalent_status_( ret );
				return false;
			}

			/* move the tempfile on top of the original */
			if( FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK != (ret = transport_tempfile_( this.filename, tempfile, tempfilename )) ) {
				return false;
			}
		} catch(final FileNotFoundException e) {
			this.status = FLAC__METADATA_CHAIN_STATUS_ERROR_OPENING_FILE;
			return false;
		} catch(final IOException e) {
			cleanup_tempfile_( tempfile, tempfilename );
			this.status = FLAC__METADATA_CHAIN_STATUS_SEEK_ERROR;
			return false;
		} finally {
			if( f != null ) {
				try { f.close(); } catch( final IOException e ) {}
			}
		}
		return true;
	}

	/** @return FLAC__Metadata_SimpleIteratorStatus */
	private static int copy_n_bytes_from_file_cb_(
			final RandomAccessFile handle,// java: changed
			//FLAC__IOCallback_Read read_cb,
			final RandomAccessFile temp_handle,// java: changed
			//FLAC__IOCallback_Write temp_write_cb,
			long bytes)//, FLAC__Metadata_SimpleIteratorStatus *status)
	{
		final byte buffer[] = new byte[8192];

		//FLAC__ASSERT(bytes >= 0);
		while( bytes > 0 ) {
			final int n = Math.min( buffer.length, (int)bytes );
			try {
				if( handle.read( buffer, 0, n ) != n ) {
					return /* *status = */ FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
					//return false;
				}
			} catch(final IOException e) {
				return /* *status = */ FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
				//return false;
			}
			try {
				temp_handle.write( buffer, 0, n );
			} catch(final IOException e) {
				return /* *status = */ FLAC__METADATA_SIMPLE_ITERATOR_STATUS_WRITE_ERROR;
				//return false;
			}
			bytes -= n;
		}

		return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK;
	}

	/** @return FLAC__Metadata_SimpleIteratorStatus */
	private static int /* FLAC__Metadata_SimpleIteratorStatus */ copy_remaining_bytes_from_file_cb_(
			final RandomAccessFile handle,// java: changed
			//FLAC__IOCallback_Read read_cb, FLAC__IOCallback_Eof eof_cb,
			final RandomAccessFile temp_handle)
			//FLAC__IOCallback_Write temp_write_cb,
			//FLAC__Metadata_SimpleIteratorStatus *status)
	{
		final byte buffer[] = new byte[8192];
		int n;

		while( true ) {
			try {
				n = handle.read( buffer, 0, buffer.length );
				if( n < 0 ) {
					break;
				}
			} catch(final IOException e) {
				return /* *status = */ FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
				//return false;
			}
			try {
				if( n > 0 ) {
					temp_handle.write( buffer, 0, n );
				}
			} catch(final IOException e) {
				return /* *status = */ FLAC__METADATA_SIMPLE_ITERATOR_STATUS_WRITE_ERROR;
				//return false;
			}
		}

		return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK;// true;
	}

	/** assumes 'handle' is already at beginning of file */
	private final boolean rewrite_file_cb_(// FLAC__IOHandle handle,
										   final RandomAccessFile iohandle,// java: changed
										   //FLAC__IOCallback_Read read_cb, FLAC__IOCallback_Seek seek_cb, FLAC__IOCallback_Eof eof_cb,
										   final RandomAccessFile temp_handle)// java: changed
			//FLAC__IOCallback_Write temp_write_cb)
	{
		int /* FLAC__Metadata_SimpleIteratorStatus */ ret;

		//FLAC__ASSERT(0 != chain);
		//FLAC__ASSERT(0 == chain->filename);
		//FLAC__ASSERT(0 != chain->head);

		/* copy the file prefix (data up to first metadata block */
		if( FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK != (ret = copy_n_bytes_from_file_cb_( iohandle,/* read_cb,*/ temp_handle,/* temp_write_cb,*/ this.first_offset )) ) {
			this.status = get_equivalent_status_( ret );
			return false;
		}

		/* write the metadata */
		for(MetadataNode node = this.head; node != null; node = node.next ) {
			if( ! write_metadata_block_header_cb_( temp_handle/*, temp_write_cb*/, node.data ) ) {
				this.status = FLAC__METADATA_CHAIN_STATUS_WRITE_ERROR;
				return false;
			}
			if( ! write_metadata_block_data_cb_( temp_handle/*, temp_write_cb*/, node.data ) ) {
				this.status = FLAC__METADATA_CHAIN_STATUS_WRITE_ERROR;
				return false;
			}
		}
		/*FLAC__ASSERT(fflush(), ftello() == chain->last_offset);*/

		/* copy the file postfix (everything after the metadata) */
		try {
			iohandle.seek( this.last_offset );
		} catch(final IOException e) {
			this.status = FLAC__METADATA_CHAIN_STATUS_SEEK_ERROR;
			return false;
		}
		if( FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK != (ret = copy_remaining_bytes_from_file_cb_( iohandle/*, read_cb, eof_cb*/, temp_handle/*, temp_write_cb*/ )) ) {
			this.status = get_equivalent_status_( ret );
			return false;
		}

		return true;
	}

	public final int /* FLAC__Metadata_ChainStatus */ status()
	{
		//FLAC__ASSERT(0 != chain);

		final int /* FLAC__Metadata_ChainStatus */ s = this.status;
		this.status = FLAC__METADATA_CHAIN_STATUS_OK;
		return s;
	}

	private final boolean read_(final String file_name, final boolean isogg)
	{
		RandomAccessFile file = null;
		boolean ret;

		//FLAC__ASSERT(0 != chain);
		//FLAC__ASSERT(0 != filename);

		clear_();

		this.filename = file_name;

		this.is_ogg = isogg;

		try {
			file = new RandomAccessFile( file_name, "r" );

			/* the function also sets chain->status for us */
			ret = isogg ?
				read_ogg_cb_( file/*, (FLAC__IOCallback_Read)fread*/ ) :
				read_cb_( file/*, (FLAC__IOCallback_Read)fread, fseek_wrapper_, ftell_wrapper_*/ );
		} catch(final FileNotFoundException e) {
			this.status = FLAC__METADATA_CHAIN_STATUS_ERROR_OPENING_FILE;
			return false;
		} finally {
			if( file != null ) {
				try { file.close(); } catch( final IOException e ) {}
			}
		}
		return ret;
	}

	public final boolean read(final String file_name)
	{
		return read_( file_name, /*is_ogg=*/false );
	}

	/*@@@@add to tests*/
	public final boolean read_ogg(final String file_name)
	{
		return read_( file_name, /*is_ogg=*/true );
	}

	private final boolean read_with_callbacks_(
			final RandomAccessFile iohandle,// java: changed
			//FLAC__IOCallbacks callbacks,
			final boolean isogg)
	{
		//FLAC__ASSERT(0 != chain);

		clear_();

		// if( null == callbacks.read || null == callbacks.seek || null == callbacks.tell ) {
		if( null == iohandle ) {// java: just simulating, to use that status
			this.status = FLAC__METADATA_CHAIN_STATUS_INVALID_CALLBACKS;
			return false;
		}

		this.is_ogg = isogg;

		/* rewind */
		try {
			iohandle.seek( 0 );
		} catch(final IOException e) {
			this.status = FLAC__METADATA_CHAIN_STATUS_SEEK_ERROR;
			return false;
		}

		/* the function also sets chain->status for us */
		final boolean ret = isogg ?
			read_ogg_cb_( iohandle/*, callbacks.read*/ ) :
			read_cb_( iohandle/*, callbacks.read, callbacks.seek, callbacks.tell*/ );

		return ret;
	}

	public final boolean read_with_callbacks(
			final RandomAccessFile iohandle)// java: changed
			//FLAC__IOCallbacks callbacks)
	{
		return read_with_callbacks_( iohandle/*, callbacks*/, /*is_ogg=*/false );
	}

	/*@@@@add to tests*/
	public final boolean read_ogg_with_callbacks(
			final RandomAccessFile iohandle)// java: changed
			//FLAC__IOCallbacks callbacks)
	{
		return read_with_callbacks_( iohandle/*, callbacks*/, /*is_ogg=*/true );
	}

	// typedef enum {
		private static final int LBS_NONE = 0;
		private static final int LBS_SIZE_CHANGED = 1;
		private static final int LBS_BLOCK_ADDED = 2;
		private static final int LBS_BLOCK_REMOVED = 3;
	// } LastBlockState;

	public final boolean check_if_tempfile_needed(final boolean use_padding)
	{
		/* This does all the same checks that are in chain_prepare_for_write_()
		 * but doesn't actually alter the chain.  Make sure to update the logic
		 * here if chain_prepare_for_write_() changes.
		 */
		/*LastBlockState*/int lbs_state = LBS_NONE;
		long lbs_size = 0;// FIXME unsigned must be changed to FLAC__off_t

		//FLAC__ASSERT(0 != chain);

		long current_length = calculate_length_();

		if( use_padding ) {
			final MetadataNode node = this.tail;
			/* if the metadata shrank and the last block is padding, we just extend the last padding block */
			if( current_length < this.initial_length && node.data.type == Format.FLAC__METADATA_TYPE_PADDING ) {
				lbs_state = LBS_SIZE_CHANGED;
				lbs_size = node.data.length + (this.initial_length - current_length);
			}
			/* if the metadata shrank more than 4 bytes then there's room to add another padding block */
			else if( current_length + (long) Format.FLAC__STREAM_METADATA_HEADER_LENGTH <= this.initial_length ) {
				lbs_state = LBS_BLOCK_ADDED;
				lbs_size = this.initial_length - (current_length + (long) Format.FLAC__STREAM_METADATA_HEADER_LENGTH);
			}
			/* if the metadata grew but the last block is padding, try cutting the padding to restore the original length so we don't have to rewrite the whole file */
			else if( current_length > this.initial_length ) {
				final long delta = current_length - this.initial_length;
				if( node.data.type == Format.FLAC__METADATA_TYPE_PADDING ) {
					/* if the delta is exactly the size of the last padding block, remove the padding block */
					if( (long)node.data.length + (long) Format.FLAC__STREAM_METADATA_HEADER_LENGTH == delta ) {
						lbs_state = LBS_BLOCK_REMOVED;
						lbs_size = 0;
					}
					/* if there is at least 'delta' bytes of padding, trim the padding down */
					else if( (long)node.data.length >= delta ) {
						lbs_state = LBS_SIZE_CHANGED;
						lbs_size = node.data.length - delta;
					}
				}
			}
		}

		current_length = 0;
		/* check sizes of all metadata blocks; reduce padding size if necessary */
		{
			for(MetadataNode node = this.head; node != null ; node = node.next ) {
				long block_len = node.data.length;
				if( node == this.tail ) {
					if( lbs_state == LBS_BLOCK_REMOVED ) {
						continue;
					} else if( lbs_state == LBS_SIZE_CHANGED ) {
						block_len = lbs_size;
					}
				}
				if( block_len >= (1 << Format.FLAC__STREAM_METADATA_LENGTH_LEN) ) {
					if( node.data.type == Format.FLAC__METADATA_TYPE_PADDING ) {
						block_len = (1 << Format.FLAC__STREAM_METADATA_LENGTH_LEN) - 1;
					} else {
						return false /* the return value doesn't matter */;
					}
				}
				current_length += (Format.FLAC__STREAM_METADATA_HEADER_LENGTH + block_len);
			}

			if(lbs_state == LBS_BLOCK_ADDED) {
				/* test added padding block */
				long block_len = lbs_size;
				if( block_len >= (1 << Format.FLAC__STREAM_METADATA_LENGTH_LEN) ) {
					block_len = (1 << Format.FLAC__STREAM_METADATA_LENGTH_LEN) - 1;
				}
				current_length += (Format.FLAC__STREAM_METADATA_HEADER_LENGTH + block_len);
			}
		}

		return (current_length != this.initial_length);
	}

	public final boolean write(final boolean use_padding, final boolean preserve_file_stats)
	{
		File stats = null;
		final String tempfile_path_prefix = null;

		//FLAC__ASSERT(0 != chain);

		if( this.is_ogg ) { /* cannot write back to Ogg FLAC yet */
			this.status = FLAC__METADATA_CHAIN_STATUS_INTERNAL_ERROR;
			return false;
		}

		if( null == this.filename ) {
			this.status = FLAC__METADATA_CHAIN_STATUS_READ_WRITE_MISMATCH;
			return false;
		}

		final long current_length = prepare_for_write_( use_padding );

		/* a return value of 0 means there was an error; chain->status is already set */
		if( 0 == current_length ) {
			return false;
		}

		if( preserve_file_stats ) {
			stats = get_file_stats_( this.filename );
		}

		if( current_length == this.initial_length ) {
			if( ! rewrite_metadata_in_place_() ) {
				return false;
			}
		}
		else {
			if( ! rewrite_file_( tempfile_path_prefix ) ) {
				return false;
			}

			/* recompute lengths and offsets */
			{
				this.initial_length = current_length;
				this.last_offset = this.first_offset;
				for(MetadataNode node = this.head; node != null; node = node.next ) {
					this.last_offset += (Format.FLAC__STREAM_METADATA_HEADER_LENGTH + node.data.length);
				}
			}
		}

		if( preserve_file_stats ) {
			set_file_stats_( this.filename, stats );
		}

		return true;
	}

	public final boolean write_with_callbacks(final boolean use_padding,
											  final RandomAccessFile iohandle)// java: changed
			//FLAC__IOCallbacks callbacks)
	{
		//FLAC__ASSERT(0 != chain);

		if( this.is_ogg) { /* cannot write back to Ogg FLAC yet */
			this.status = FLAC__METADATA_CHAIN_STATUS_INTERNAL_ERROR;
			return false;
		}

		if( null != this.filename ) {
			this.status = FLAC__METADATA_CHAIN_STATUS_READ_WRITE_MISMATCH;
			return false;
		}

		/*if( null == callbacks.write || null == callbacks.seek ) {
			chain.status = FLAC__METADATA_CHAIN_STATUS_INVALID_CALLBACKS;
			return false;
		}*/

		if( check_if_tempfile_needed( use_padding ) ) {
			this.status = FLAC__METADATA_CHAIN_STATUS_WRONG_WRITE_CALL;
			return false;
		}

		final long current_length = prepare_for_write_( use_padding );

		/* a return value of 0 means there was an error; chain->status is already set */
		if( 0 == current_length ) {
			return false;
		}

		//FLAC__ASSERT(current_length == chain->initial_length);

		return rewrite_metadata_in_place_cb_( iohandle/*, callbacks.write, callbacks.seek*/ );
	}

	public final boolean write_with_callbacks_and_tempfile(final boolean use_padding,
														   final RandomAccessFile iohandle,// java: changed
														   //FLAC__IOCallbacks callbacks,
														   final RandomAccessFile temp_handle)//,
			//FLAC__IOCallbacks temp_callbacks)
	{
		//FLAC__ASSERT(0 != chain);

		if( this.is_ogg ) { /* cannot write back to Ogg FLAC yet */
			this.status = FLAC__METADATA_CHAIN_STATUS_INTERNAL_ERROR;
			return false;
		}

		if( null != this.filename ) {
			this.status = FLAC__METADATA_CHAIN_STATUS_READ_WRITE_MISMATCH;
			return false;
		}

		/*if( null == callbacks.read || null == callbacks.seek || null == callbacks.eof ) {
			chain.status = FLAC__METADATA_CHAIN_STATUS_INVALID_CALLBACKS;
			return false;
		}
		if( null == temp_callbacks.write ) {
			chain.status = FLAC__METADATA_CHAIN_STATUS_INVALID_CALLBACKS;
			return false;
		}*/

		if( ! check_if_tempfile_needed( use_padding ) ) {
			this.status = FLAC__METADATA_CHAIN_STATUS_WRONG_WRITE_CALL;
			return false;
		}

		final long current_length = prepare_for_write_( use_padding );

		/* a return value of 0 means there was an error; chain->status is already set */
		if( 0 == current_length ) {
			return false;
		}

		//FLAC__ASSERT(current_length != chain->initial_length);

		/* rewind */
		try {
			iohandle.seek( 0 );
		} catch(final IOException e) {
			this.status = FLAC__METADATA_CHAIN_STATUS_SEEK_ERROR;
			return false;
		}

		if( ! rewrite_file_cb_( iohandle/*, callbacks.read, callbacks.seek, callbacks.eof*/, temp_handle/*, temp_callbacks.write*/ ) ) {
			return false;
		}

		/* recompute lengths and offsets */
		{
			this.initial_length = current_length;
			this.last_offset = this.first_offset;
			for(MetadataNode node = this.head; node != null; node = node.next ) {
				this.last_offset += (Format.FLAC__STREAM_METADATA_HEADER_LENGTH + node.data.length);
			}
		}

		return true;
	}

	public final void merge_padding()
	{
		//FLAC__ASSERT(0 != chain);

		for(MetadataNode node = this.head; node != null; ) {
			if( ! merge_adjacent_padding_( node ) ) {
				node = node.next;
			}
		}
	}

	public final void sort_padding()
	{
		MetadataNode node, save;
		int i;

		//FLAC__ASSERT(0 != chain);

		/*
		 * Don't try and be too smart... this simple algo is good enough for
		 * the small number of nodes that we deal with.
		 */
		for( i = 0, node = this.head; i < this.nodes; i++ ) {
			if( node.data.type == Format.FLAC__METADATA_TYPE_PADDING ) {
				save = node.next;
				remove_node_( node );
				append_node_( node );
				node = save;
			}
			else {
				node = node.next;
			}
		}

		merge_padding();
	}

	@Override// StreamDecoderReadCallback, chain_read_ogg_read_cb_
	public int dec_read_callback(final StreamDecoder decoder, final byte buffer[], final int offset, final int bytes/*, final Object client_data*/) throws IOException {

		// final MetadataChain chain = (MetadataChain)client_data;// java: this

		if( bytes > 0 && this.status == FLAC__METADATA_CHAIN_STATUS_OK ) {
			return this.handle.read( buffer, 0, bytes );
		}
		throw new IOException();
	}

	@Override// StreamDecoderWriteCallback, chain_read_ogg_write_cb_
	public int dec_write_callback(final StreamDecoder decoder, final Frame frame, final int buffer[][], final int offset/*, final Object client_data*/)
	{
		//(void)decoder, (void)frame, (void)buffer, (void)client_data;
		return StreamDecoder.FLAC__STREAM_DECODER_WRITE_STATUS_ABORT;
	}

	@Override// StreamDecoderMetadataCallback, chain_read_ogg_metadata_cb_
	public void dec_metadata_callback(final StreamDecoder decoder, final StreamMetadata metadata/*, final Object client_data*/) throws IOException {

		// final MetadataChain chain = (MetadataChain)client_data;// java: this

		//(void)decoder;

		try {
			final MetadataNode node = new MetadataNode();

			node.data = StreamMetadata.metadata_clone( metadata );
			if( null == node.data ) {
				node.delete_();
				this.status = FLAC__METADATA_CHAIN_STATUS_MEMORY_ALLOCATION_ERROR;
				return;
			}

			this.append_node_( node );
		} catch(final OutOfMemoryError e) {
			this.status = FLAC__METADATA_CHAIN_STATUS_MEMORY_ALLOCATION_ERROR;
			return;
		}
	}

	@Override// StreamDecoderErrorCallback, chain_read_ogg_error_cb_
	public void dec_error_callback(final StreamDecoder decoder, final int it_status/*, final Object client_data*/) {
		// final MetadataChain chain = (MetadataChain)client_data;// java: this
		//(void)decoder, (void)status;
		this.status = FLAC__METADATA_CHAIN_STATUS_INTERNAL_ERROR; /*@@@ maybe needs better error code */
	}

	private static int /* FLAC__Metadata_ChainStatus */ get_equivalent_status_(final int /* FLAC__Metadata_SimpleIteratorStatus */ status)
	{
		switch( status ) {
			case FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK:
				return FLAC__METADATA_CHAIN_STATUS_OK;
			case FLAC__METADATA_SIMPLE_ITERATOR_STATUS_ILLEGAL_INPUT:
				return FLAC__METADATA_CHAIN_STATUS_ILLEGAL_INPUT;
			case FLAC__METADATA_SIMPLE_ITERATOR_STATUS_ERROR_OPENING_FILE:
				return FLAC__METADATA_CHAIN_STATUS_ERROR_OPENING_FILE;
			case FLAC__METADATA_SIMPLE_ITERATOR_STATUS_NOT_A_FLAC_FILE:
				return FLAC__METADATA_CHAIN_STATUS_NOT_A_FLAC_FILE;
			case FLAC__METADATA_SIMPLE_ITERATOR_STATUS_NOT_WRITABLE:
				return FLAC__METADATA_CHAIN_STATUS_NOT_WRITABLE;
			case FLAC__METADATA_SIMPLE_ITERATOR_STATUS_BAD_METADATA:
				return FLAC__METADATA_CHAIN_STATUS_BAD_METADATA;
			case FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR:
				return FLAC__METADATA_CHAIN_STATUS_READ_ERROR;
			case FLAC__METADATA_SIMPLE_ITERATOR_STATUS_SEEK_ERROR:
				return FLAC__METADATA_CHAIN_STATUS_SEEK_ERROR;
			case FLAC__METADATA_SIMPLE_ITERATOR_STATUS_WRITE_ERROR:
				return FLAC__METADATA_CHAIN_STATUS_WRITE_ERROR;
			case FLAC__METADATA_SIMPLE_ITERATOR_STATUS_RENAME_ERROR:
				return FLAC__METADATA_CHAIN_STATUS_RENAME_ERROR;
			case FLAC__METADATA_SIMPLE_ITERATOR_STATUS_UNLINK_ERROR:
				return FLAC__METADATA_CHAIN_STATUS_UNLINK_ERROR;
			case FLAC__METADATA_SIMPLE_ITERATOR_STATUS_MEMORY_ALLOCATION_ERROR:
				return FLAC__METADATA_CHAIN_STATUS_MEMORY_ALLOCATION_ERROR;
			case FLAC__METADATA_SIMPLE_ITERATOR_STATUS_INTERNAL_ERROR:
			default:
				return FLAC__METADATA_CHAIN_STATUS_INTERNAL_ERROR;
		}
	}
}
