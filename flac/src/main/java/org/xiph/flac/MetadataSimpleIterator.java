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

public class MetadataSimpleIterator extends MetadataBase {

	public static final String FLAC__Metadata_SimpleIteratorStatusString[] = {
			"FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK",
			"FLAC__METADATA_SIMPLE_ITERATOR_STATUS_ILLEGAL_INPUT",
			"FLAC__METADATA_SIMPLE_ITERATOR_STATUS_ERROR_OPENING_FILE",
			"FLAC__METADATA_SIMPLE_ITERATOR_STATUS_NOT_A_FLAC_FILE",
			"FLAC__METADATA_SIMPLE_ITERATOR_STATUS_NOT_WRITABLE",
			"FLAC__METADATA_SIMPLE_ITERATOR_STATUS_BAD_METADATA",
			"FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR",
			"FLAC__METADATA_SIMPLE_ITERATOR_STATUS_SEEK_ERROR",
			"FLAC__METADATA_SIMPLE_ITERATOR_STATUS_WRITE_ERROR",
			"FLAC__METADATA_SIMPLE_ITERATOR_STATUS_RENAME_ERROR",
			"FLAC__METADATA_SIMPLE_ITERATOR_STATUS_UNLINK_ERROR",
			"FLAC__METADATA_SIMPLE_ITERATOR_STATUS_MEMORY_ALLOCATION_ERROR",
			"FLAC__METADATA_SIMPLE_ITERATOR_STATUS_INTERNAL_ERROR"
		};

	/** 1 for initial offset, +4 for our own personal use */
	private static final int SIMPLE_ITERATOR_MAX_PUSH_DEPTH = (1+4);

	private RandomAccessFile file;
	private String filename, tempfile_path_prefix;
	private File stats;
	private boolean has_stats;
	private boolean is_writable;
	private int /* FLAC__Metadata_SimpleIteratorStatus */ status;
	private final long offset[] = new long[SIMPLE_ITERATOR_MAX_PUSH_DEPTH];
	private long first_offset; /* this is the offset to the STREAMINFO block */
	private int depth;
	/* this is the metadata block header of the current block we are pointing to: */
	private boolean is_last;
	private int /* FLAC__MetadataType */ type;
	private int length;

	public MetadataSimpleIterator()
	{
		file = null;
		filename = null;
		tempfile_path_prefix = null;
		has_stats = false;
		is_writable = false;
		status = FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK;
		first_offset = offset[0] = -1;
		depth = 0;
	}

	private final void free_guts_()
	{
		//FLAC__ASSERT(0 != iterator);

		if( null != this.file ) {
			try { this.file.close(); } catch( final IOException e ) {}
			this.file = null;
			if( this.has_stats ) {
				set_file_stats_( this.filename, this.stats );
			}
		}
		this.filename = null;
		this.tempfile_path_prefix = null;
	}

	public final void delete()
	{
		//FLAC__ASSERT(0 != iterator);

		free_guts_();
	}

	/** Get the current status of the iterator.  Call this after a function
	 *  returns \c false to get the reason for the error.  Also resets the status
	 *  to FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK.
	 *
	 * \assert
	 *    \code iterator != NULL \endcode
	 * @retval FLAC__Metadata_SimpleIteratorStatus
	 *    The current status of the iterator.
	 */
	public final int /* FLAC__Metadata_SimpleIteratorStatus */ status()
	{
		//FLAC__ASSERT(0 != iterator);

		final int /* FLAC__Metadata_SimpleIteratorStatus */ s = this.status;
		this.status = FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK;
		return s;
	}

	private static int seek_to_first_metadata_block_(final RandomAccessFile f)
	{
		return seek_to_first_metadata_block_cb_( f/*, (FLAC__IOCallback_Read)fread, fseek_wrapper_*/ );
	}

	private final boolean read_metadata_block_header_()
	{
		//FLAC__ASSERT(0 != iterator);
		//FLAC__ASSERT(0 != iterator->file);
		final metadata_block_header_helper h;

		if( null == (h = read_metadata_block_header_cb_( this.file/*, (FLAC__IOCallback_Read)fread, is_last, type, length*/)) ) {
			this.status = FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
			return false;
		}

		this.is_last = h.is_last;
		this.type = h.type;
		this.length = h.length;

		return true;
	}

	private final boolean prime_input_(final boolean read_only)
	{
		//FLAC__ASSERT(0 != iterator);
		try {
			if( read_only ) {
				this.file = new RandomAccessFile( this.filename, "r" );
				this.is_writable = false;
			} else {
				try {
					this.file = new RandomAccessFile( this.filename, "rw" );
				} catch(final FileNotFoundException e) {
					this.is_writable = false;
					this.file = new RandomAccessFile( this.filename, "r" );
				}
			}

			final int ret = seek_to_first_metadata_block_( this.file );
			switch( ret ) {
				case 0:
					this.depth = 0;
					this.first_offset = this.offset[this.depth] = this.file.getFilePointer();

					final boolean r = read_metadata_block_header_();
					/* The first metadata block must be a streaminfo. If this is not the
					 * case, the file is invalid and assumptions made elsewhere in the
					 * code are invalid */
					if( this.type != Format.FLAC__METADATA_TYPE_STREAMINFO ) {
						this.status = FLAC__METADATA_SIMPLE_ITERATOR_STATUS_BAD_METADATA;
						return false;
					}
					return r;
				case 1:
					this.status = FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
					return false;
				case 2:
					this.status = FLAC__METADATA_SIMPLE_ITERATOR_STATUS_SEEK_ERROR;
					return false;
				case 3:
					this.status = FLAC__METADATA_SIMPLE_ITERATOR_STATUS_NOT_A_FLAC_FILE;
					return false;
				default:
					//FLAC__ASSERT(0);
					return false;
			}
		} catch(final FileNotFoundException e) {
			this.status = FLAC__METADATA_SIMPLE_ITERATOR_STATUS_ERROR_OPENING_FILE;
			return false;
		} catch(final IOException e) {
			this.status = FLAC__METADATA_SIMPLE_ITERATOR_STATUS_ERROR_OPENING_FILE;
			return false;
		}
	}

	/** Initialize the iterator to point to the first metadata block in the
	 *  given FLAC file.
	 *
	 * @param file_name             The path to the FLAC file.
	 * @param read_only            If \c true, the FLAC file will be opened
	 *                             in read-only mode; if \c false, the FLAC
	 *                             file will be opened for edit even if no
	 *                             edits are performed.
	 * @param preserve_file_stats  If \c true, the owner and modification
	 *                             time will be preserved even if the FLAC
	 *                             file is written to.
	 * \assert
	 *    \code iterator != NULL \endcode
	 *    \code filename != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if a memory allocation error occurs, the file can't be
	 *    opened, or another error occurs, else \c true.
	 */
	public final boolean init(final String file_name, final boolean read_only, final boolean preserve_file_stats)
	{
		//String tempfile_path_prefix = null; /*@@@ search for comments near 'flac_rename(...)' for what it will take to finish implementing this */

		//FLAC__ASSERT(0 != iterator);
		//FLAC__ASSERT(0 != filename);

		free_guts_();

		if( ! read_only && preserve_file_stats ) {
			this.has_stats = (this.stats = get_file_stats_( file_name )) != null;
		}

		this.filename = file_name;
		//if( null != tempfile_path_prefix ) {
		//	this.tempfile_path_prefix = tempfile_path_prefix;
		//}

		return prime_input_( read_only );
	}

	/** Returns \c true if the FLAC file is writable.  If \c false, calls to
	 *  FLAC__metadata_simple_iterator_set_block() and
	 *  FLAC__metadata_simple_iterator_insert_block_after() will fail.
	 *
	 * \assert
	 *    \code iterator != NULL \endcode
	 * @retval FLAC__bool
	 *    See above.
	 */
	public final boolean is_writable()
	{
		//FLAC__ASSERT(0 != iterator);
		//FLAC__ASSERT(0 != iterator->file);

		return this.is_writable;
	}

	/** Moves the iterator forward one metadata block, returning \c false if
	 *  already at the end.
	 *
	 * \assert
	 *    \code iterator != NULL \endcode
	 *    \a iterator has been successfully initialized with
	 *    FLAC__metadata_simple_iterator_init()
	 * @retval FLAC__bool
	 *    \c false if already at the last metadata block of the chain, else
	 *    \c true.
	 */
	public final boolean next()
	{
		//FLAC__ASSERT(0 != iterator);
		//FLAC__ASSERT(0 != iterator->file);

		if( this.is_last ) {
			return false;
		}

		try {
			this.file.seek( this.file.getFilePointer() + this.length );
			this.offset[this.depth] = this.file.getFilePointer();
		} catch(final IOException e) {
			this.status = FLAC__METADATA_SIMPLE_ITERATOR_STATUS_SEEK_ERROR;
			return false;
		}
		return read_metadata_block_header_();
	}

	/** Moves the iterator backward one metadata block, returning \c false if
	 *  already at the beginning.
	 *
	 * \assert
	 *    \code iterator != NULL \endcode
	 *    \a iterator has been successfully initialized with
	 *    FLAC__metadata_simple_iterator_init()
	 * @retval FLAC__bool
	 *    \c false if already at the first metadata block of the chain, else
	 *    \c true.
	 */
	public final boolean prev()
	{
		//FLAC__ASSERT(0 != iterator);
		//FLAC__ASSERT(0 != iterator->file);

		final long[] off = this.offset;// java
		if( off[this.depth] == this.first_offset ) {
			return false;
		}

		try {
			final RandomAccessFile f = this.file;// java
			f.seek( this.first_offset );
			long this_offset = this.first_offset;
			if( ! read_metadata_block_header_() ) {
				return false;
			}

			/* we ignore any error from ftello() and catch it in fseeko() */
			while( f.getFilePointer() + (long)this.length < off[this.depth] ) {
				f.seek( f.getFilePointer() + (long)this.length );
				this_offset = f.getFilePointer();
				if( ! read_metadata_block_header_() ) {
					return false;
				}
			}
			off[this.depth] = this_offset;
			return true;
		} catch(final IOException e) {
			this.status = FLAC__METADATA_SIMPLE_ITERATOR_STATUS_SEEK_ERROR;
			return false;
		}
	}

	/** Returns a flag telling if the current metadata block is the last.
	 *
	 * \assert
	 *    \code iterator != NULL \endcode
	 *    \a iterator has been successfully initialized with
	 *    FLAC__metadata_simple_iterator_init()
	 * @retval FLAC__bool
	 *    \c true if the current metadata block is the last in the file,
	 *    else \c false.
	 */
	/*@@@@add to tests*/
	public final boolean is_last()
	{
		//FLAC__ASSERT(0 != iterator);
		//FLAC__ASSERT(0 != iterator->file);

		return this.is_last;
	}

	/** Get the offset of the metadata block at the current position.  This
	 *  avoids reading the actual block data which can save time for large
	 *  blocks.
	 *
	 * \assert
	 *    \code iterator != NULL \endcode
	 *    \a iterator has been successfully initialized with
	 *    FLAC__metadata_simple_iterator_init()
	 * @retval off_t
	 *    The offset of the metadata block at the current iterator position.
	 *    This is the byte offset relative to the beginning of the file of
	 *    the current metadata block's header.
	 */
	/*@@@@add to tests*/
	public final long get_block_offset()
	{
		//FLAC__ASSERT(0 != iterator);
		//FLAC__ASSERT(0 != iterator->file);

		return this.offset[this.depth];
	}

	/** Get the type of the metadata block at the current position.  This
	 *  avoids reading the actual block data which can save time for large
	 *  blocks.
	 *
	 * \assert
	 *    \code iterator != NULL \endcode
	 *    \a iterator has been successfully initialized with
	 *    FLAC__metadata_simple_iterator_init()
	 * @retval FLAC__MetadataType
	 *    The type of the metadata block at the current iterator position.
	 */
	public final int /* FLAC__MetadataType */ get_block_type()
	{
		//FLAC__ASSERT(0 != iterator);
		//FLAC__ASSERT(0 != iterator->file);

		return this.type;
	}

	/** Get the length of the metadata block at the current position.  This
	 *  avoids reading the actual block data which can save time for large
	 *  blocks.
	 *
	 * \assert
	 *    \code iterator != NULL \endcode
	 *    \a iterator has been successfully initialized with
	 *    FLAC__metadata_simple_iterator_init()
	 * @retval unsigned
	 *    The length of the metadata block at the current iterator position.
	 *    The is same length as that in the
	 *    <a href="http://xiph.org/flac/format.html#metadata_block_header">metadata block header</a>,
	 *    i.e. the length of the metadata body that follows the header.
	 */
	/*@@@@add to tests*/
	public final int get_block_length()
	{
		//FLAC__ASSERT(0 != iterator);
		//FLAC__ASSERT(0 != iterator->file);

		return this.length;
	}

	/** Get the application ID of the \c APPLICATION block at the current
	 *  position.  This avoids reading the actual block data which can save
	 *  time for large blocks.
	 *
	 * @param id        A pointer to a buffer of at least \c 4 bytes where
	 *                  the ID will be stored.
	 * \assert
	 *    \code iterator != NULL \endcode
	 *    \code id != NULL \endcode
	 *    \a iterator has been successfully initialized with
	 *    FLAC__metadata_simple_iterator_init()
	 * @retval FLAC__bool
	 *    \c true if the ID was successfully read, else \c false, in which
	 *    case you should check FLAC__metadata_simple_iterator_status() to
	 *    find out why.  If the status is
	 *    \c FLAC__METADATA_SIMPLE_ITERATOR_STATUS_ILLEGAL_INPUT, then the
	 *    current metadata block is not an \c APPLICATION block.  Otherwise
	 *    if the status is
	 *    \c FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR or
	 *    \c FLAC__METADATA_SIMPLE_ITERATOR_STATUS_SEEK_ERROR, an I/O error
	 *    occurred and the iterator can no longer be used.
	 */
	/*@@@@add to tests*/
	public final boolean get_application_id(final byte[] id)
	{
		final int id_bytes = Format.FLAC__STREAM_METADATA_APPLICATION_ID_LEN / 8;

		//FLAC__ASSERT(0 != iterator);
		//FLAC__ASSERT(0 != iterator->file);
		//FLAC__ASSERT(0 != id);

		if( this.type != Format.FLAC__METADATA_TYPE_APPLICATION ) {
			this.status = FLAC__METADATA_SIMPLE_ITERATOR_STATUS_ILLEGAL_INPUT;
			return false;
		}

		try {
			if( this.file.read( id, 0, id_bytes ) != id_bytes ) {
				this.status = FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
				return false;
			}
		} catch(final IOException e) {
			this.status = FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
			return false;
		}

		/* back up */
		try {
			this.file.seek( this.file.getFilePointer() - id_bytes );
		} catch(final IOException e){
			this.status = FLAC__METADATA_SIMPLE_ITERATOR_STATUS_SEEK_ERROR;
			return false;
		}

		return true;
	}

	private final boolean read_metadata_block_data_(final StreamMetadata block)
	{
		//FLAC__ASSERT(0 != iterator);
		//FLAC__ASSERT(0 != iterator->file);

		this.status = read_metadata_block_data_cb_( this.file, /*(FLAC__IOCallback_Read)fread, fseek_wrapper_,*/ block );

		return (this.status == FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK);
	}

	/** Get the metadata block at the current position.  You can modify the
	 *  block but must use FLAC__metadata_simple_iterator_set_block() to
	 *  write it back to the FLAC file.
	 *
	 *  You must call FLAC__metadata_object_delete() on the returned object
	 *  when you are finished with it.
	 *
	 * \assert
	 *    \code iterator != NULL \endcode
	 *    \a iterator has been successfully initialized with
	 *    FLAC__metadata_simple_iterator_init()
	 * @retval FLAC__StreamMetadata*
	 *    The current metadata block, or \c NULL if there was a memory
	 *    allocation error.
	 */
	public final StreamMetadata get_block()
	{
		final StreamMetadata block = StreamMetadata.metadata_new( this.type );

		//FLAC__ASSERT(0 != iterator);
		//FLAC__ASSERT(0 != iterator->file);

		if( null != block ) {
			block.is_last = this.is_last;
			block.length = this.length;

			if( ! read_metadata_block_data_( block ) ) {
				StreamMetadata.delete( block );
				return null;
			}

			/* back up to the beginning of the block data to stay consistent */
			try {
				this.file.seek( this.offset[this.depth] + Format.FLAC__STREAM_METADATA_HEADER_LENGTH );
			} catch(final IOException e) {
				this.status = FLAC__METADATA_SIMPLE_ITERATOR_STATUS_SEEK_ERROR;
				StreamMetadata.delete( block );
				return null;
			}
		} else {
			this.status = FLAC__METADATA_SIMPLE_ITERATOR_STATUS_MEMORY_ALLOCATION_ERROR;
		}

		return block;
	}

	private final boolean write_metadata_block_stationary_(final StreamMetadata block)
	{
		try {
			this.file.seek( this.offset[this.depth] );

			if( FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK != (this.status = write_metadata_block_header_( this.file, block )) ) {
				return false;
			}

			if( FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK != (this.status = write_metadata_block_data_( this.file, block )) ) {
				return false;
			}

			this.file.seek( this.offset[this.depth] );
		} catch(final IOException e) {
			this.status = FLAC__METADATA_SIMPLE_ITERATOR_STATUS_SEEK_ERROR;
			return false;
		}
		return read_metadata_block_header_();
	}

	private final boolean write_metadata_block_stationary_with_padding_(final StreamMetadata block, final int padding_length, final boolean padding_is_last)
	{
		StreamMetadata padding;

		try {
			this.file.seek( this.offset[this.depth] );

			block.is_last = false;

			if( FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK != (this.status = write_metadata_block_header_( this.file, block )) ) {
				return false;
			}

			if( FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK != (this.status = write_metadata_block_data_( this.file, block )) ) {
				return false;
			}

			if( null == (padding = StreamMetadata.metadata_new( Format.FLAC__METADATA_TYPE_PADDING )) ) {
				return false;// FIXME return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_MEMORY_ALLOCATION_ERROR instead boolean
			}

			padding.is_last = padding_is_last;
			padding.length = padding_length;

			if( FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK != (this.status = write_metadata_block_header_( this.file, padding )) ) {
				StreamMetadata.delete( padding );
				return false;
			}

			if( FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK != (this.status = write_metadata_block_data_( this.file, padding )) ) {
				StreamMetadata.delete( padding );
				return false;
			}

			StreamMetadata.delete( padding );

			this.file.seek( this.offset[this.depth] );

		} catch(final IOException e) {
			this.status = FLAC__METADATA_SIMPLE_ITERATOR_STATUS_SEEK_ERROR;
			return false;
		}
		return read_metadata_block_header_();
	}

	/* java: extracted in place
	private final boolean copy_file_prefix_(RandomAccessFile tempfile, String tempfilename, boolean append)
	{
		final long offset_end = append ? this.offset[this.depth] + (long)Format.FLAC__STREAM_METADATA_HEADER_LENGTH + (long)this.length : this.offset[this.depth];

		try {
			this.file.seek( 0 );
		} catch(IOException e) {
			this.status = FLAC__METADATA_SIMPLE_ITERATOR_STATUS_SEEK_ERROR;
			return false;
		}
		// java: changed
		//if( ! open_tempfile_( this.filename, this.tempfile_path_prefix, tempfile, tempfilename, &this.status ) ) {
		//	cleanup_tempfile_( tempfile, tempfilename );
		//	return false;
		//}
		tempfilename = get_tempfile_name_( this.filename, this.tempfile_path_prefix );
		try {
			tempfile = new RandomAccessFile( tempfilename, "rw" );
		} catch(FileNotFoundException e) {
			this.status = FLAC__METADATA_SIMPLE_ITERATOR_STATUS_ERROR_OPENING_FILE;
			cleanup_tempfile_( tempfile, tempfilename );
			return false;
		}

		if( MetadataSimpleIterator.FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK != (this.status = copy_n_bytes_from_file_( this.file, tempfile, offset_end )) ) {
			cleanup_tempfile_( tempfile, tempfilename );
			return false;
		}

		return true;
	}
	*/

	private final boolean copy_file_postfix_(final RandomAccessFile tempfile, final String tempfilename, final int fixup_is_last_code, final long fixup_is_last_flag_offset, final boolean backup)
	{
		final long save_offset = this.offset[this.depth];
		//FLAC__ASSERT(0 != *tempfile);

		try {
			this.file.seek( save_offset + Format.FLAC__STREAM_METADATA_HEADER_LENGTH + this.length );
		} catch(final IOException e) {
			cleanup_tempfile_( tempfile, tempfilename );
			this.status = FLAC__METADATA_SIMPLE_ITERATOR_STATUS_SEEK_ERROR;
			return false;
		}
		if( FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK != (this.status = copy_remaining_bytes_from_file_( this.file, tempfile )) ) {
			cleanup_tempfile_( tempfile, tempfilename );
			return false;
		}

		if( fixup_is_last_code != 0 ) {
			/*
			 * if code == 1, it means a block was appended to the end so
			 *   we have to clear the is_last flag of the previous block
			 * if code == -1, it means the last block was deleted so
			 *   we have to set the is_last flag of the previous block
			 */
			/* MAGIC NUMBERs here; we know the is_last flag is the high bit of the byte at this location */
			final byte x[] = new byte[1];
			try {
				tempfile.seek( fixup_is_last_flag_offset );
			} catch(final IOException e) {
				cleanup_tempfile_( tempfile, tempfilename );
				this.status = FLAC__METADATA_SIMPLE_ITERATOR_STATUS_SEEK_ERROR;
				return false;
			}
			try {
				if( tempfile.read( x, 0, 1 ) != 1 ) {
					cleanup_tempfile_( tempfile, tempfilename );
					this.status = FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
					return false;
				}
			} catch(final IOException e) {
				cleanup_tempfile_( tempfile, tempfilename );
				this.status = FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
				return false;
			}
			if( fixup_is_last_code > 0 ) {
				//FLAC__ASSERT(x & 0x80);
				x[0] &= 0x7f;
			}
			else {
				//FLAC__ASSERT(!(x & 0x80));
				x[0] |= 0x80;
			}
			try {
				tempfile.seek( fixup_is_last_flag_offset );
			} catch(final IOException e) {
				cleanup_tempfile_( tempfile, tempfilename );
				this.status = FLAC__METADATA_SIMPLE_ITERATOR_STATUS_SEEK_ERROR;
				return false;
			}
			try {
				tempfile.write( x, 0, 1 );
			} catch(final IOException e) {
				cleanup_tempfile_( tempfile, tempfilename );
				this.status = FLAC__METADATA_SIMPLE_ITERATOR_STATUS_WRITE_ERROR;
				return false;
			}
		}

		try { this.file.close(); } catch( final IOException e ) {}

		if( FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK != (this.status = transport_tempfile_( this.filename, tempfile, tempfilename )) ) {
			return false;
		}

		if( this.has_stats ) {
			set_file_stats_( this.filename, this.stats );
		}

		if( ! prime_input_( ! this.is_writable ) ) {
			return false;
		}
		if( backup ) {
			while( this.offset[this.depth] + Format.FLAC__STREAM_METADATA_HEADER_LENGTH + this.length < save_offset ) {
				if( ! next() ) {
					return false;
				}
			}
			return true;
		}
		else {
			/* move the iterator to it's original block faster by faking a push, then doing a pop_ */
			//FLAC__ASSERT(iterator->depth == 0);
			this.offset[0] = save_offset;
			this.depth++;
			return pop_();
		}
	}

	private final boolean rewrite_whole_file_(final StreamMetadata block, final boolean append)
	{

		int fixup_is_last_code = 0; /* 0 => no need to change any is_last flags */
		long fixup_is_last_flag_offset = -1;
		//FLAC__ASSERT(0 != block || append == false);

		if( this.is_last ) {
			if( append ) {
				fixup_is_last_code = 1; /* 1 => clear the is_last flag at the following offset */
				fixup_is_last_flag_offset = this.offset[this.depth];
			}
			else if( null == block ) {
				push_();
				if( ! prev() ) {
					pop_();
					return false;
				}
				fixup_is_last_code = -1; /* -1 => set the is_last the flag at the following offset */
				fixup_is_last_flag_offset = this.offset[this.depth];
				if( ! pop_() ) {
					return false;
				}
			}
		}

		/* java: extracted in place
		if( ! copy_file_prefix_( iterator, tempfile, tempfilename, append ) )
			return false;
		*/

		final long offset_end = append ? this.offset[this.depth] + Format.FLAC__STREAM_METADATA_HEADER_LENGTH + this.length : this.offset[this.depth];

		try {
			this.file.seek( 0 );
		} catch(final IOException e) {
			this.status = FLAC__METADATA_SIMPLE_ITERATOR_STATUS_SEEK_ERROR;
			return false;
		}
		// java: changed
		/*if( ! open_tempfile_( this.filename, this.tempfile_path_prefix, tempfile, tempfilename, &this.status ) ) {
			cleanup_tempfile_( tempfile, tempfilename );
			return false;
		}*/
		final String tempfilename = get_tempfile_name_( this.filename, this.tempfile_path_prefix );
		RandomAccessFile tempfile = null;
		try {
			tempfile = new RandomAccessFile( tempfilename, "rw" );
		} catch(final FileNotFoundException e) {
			this.status = FLAC__METADATA_SIMPLE_ITERATOR_STATUS_ERROR_OPENING_FILE;
			return false;
		}

		if( MetadataSimpleIterator.FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK != (this.status = copy_n_bytes_from_file_( this.file, tempfile, offset_end )) ) {
			cleanup_tempfile_( tempfile, tempfilename );
			return false;
		}

		if( null != block ) {
			if( FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK != (this.status = write_metadata_block_header_( tempfile, block )) ) {
				cleanup_tempfile_( tempfile, tempfilename );
				return false;
			}

			if( FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK != (this.status = write_metadata_block_data_( tempfile, block )) ) {
				cleanup_tempfile_( tempfile, tempfilename );
				return false;
			}
		}

		if( ! copy_file_postfix_( tempfile, tempfilename, fixup_is_last_code, fixup_is_last_flag_offset, block == null ) ) {
			return false;
		}

		if( append ) {
			return next();
		}

		return true;
	}

	private final void push_()
	{
		//FLAC__ASSERT(iterator->depth+1 < SIMPLE_ITERATOR_MAX_PUSH_DEPTH);
		this.offset[this.depth+1] = this.offset[this.depth];
		this.depth++;
	}

	private final boolean pop_()
	{
		//FLAC__ASSERT(iterator->depth > 0);
		this.depth--;
		try {
			this.file.seek( this.offset[this.depth] );
		} catch(final IOException e) {
			this.status = FLAC__METADATA_SIMPLE_ITERATOR_STATUS_SEEK_ERROR;
			return false;
		}

		return read_metadata_block_header_();
	}

	/** Write a block back to the FLAC file.  This function tries to be
	 *  as efficient as possible; how the block is actually written is
	 *  shown by the following:
	 *
	 *  Existing block is a STREAMINFO block and the new block is a
	 *  STREAMINFO block: the new block is written in place.  Make sure
	 *  you know what you're doing when changing the values of a
	 *  STREAMINFO block.
	 *
	 *  Existing block is a STREAMINFO block and the new block is a
	 *  not a STREAMINFO block: this is an error since the first block
	 *  must be a STREAMINFO block.  Returns \c false without altering the
	 *  file.
	 *
	 *  Existing block is not a STREAMINFO block and the new block is a
	 *  STREAMINFO block: this is an error since there may be only one
	 *  STREAMINFO block.  Returns \c false without altering the file.
	 *
	 *  Existing block and new block are the same length: the existing
	 *  block will be replaced by the new block, written in place.
	 *
	 *  Existing block is longer than new block: if use_padding is \c true,
	 *  the existing block will be overwritten in place with the new
	 *  block followed by a PADDING block, if possible, to make the total
	 *  size the same as the existing block.  Remember that a padding
	 *  block requires at least four bytes so if the difference in size
	 *  between the new block and existing block is less than that, the
	 *  entire file will have to be rewritten, using the new block's
	 *  exact size.  If use_padding is \c false, the entire file will be
	 *  rewritten, replacing the existing block by the new block.
	 *
	 *  Existing block is shorter than new block: if use_padding is \c true,
	 *  the function will try and expand the new block into the following
	 *  PADDING block, if it exists and doing so won't shrink the PADDING
	 *  block to less than 4 bytes.  If there is no following PADDING
	 *  block, or it will shrink to less than 4 bytes, or use_padding is
	 *  \c false, the entire file is rewritten, replacing the existing block
	 *  with the new block.  Note that in this case any following PADDING
	 *  block is preserved as is.
	 *
	 *  After writing the block, the iterator will remain in the same
	 *  place, i.e. pointing to the new block.
	 *
	 * @param block        The block to set.
	 * @param use_padding  See above.
	 * \assert
	 *    \code iterator != NULL \endcode
	 *    \a iterator has been successfully initialized with
	 *    FLAC__metadata_simple_iterator_init()
	 *    \code block != NULL \endcode
	 * @retval FLAC__bool
	 *    \c true if successful, else \c false.
	 */
	public final boolean set_block(final StreamMetadata block, boolean use_padding)
	{
		//FLAC__ASSERT_DECLARATION(off_t debug_target_offset = iterator->offset[iterator->depth];)
		//FLAC__ASSERT(0 != iterator);
		//FLAC__ASSERT(0 != iterator->file);
		//FLAC__ASSERT(0 != block);

		if( ! this.is_writable ) {
			this.status = FLAC__METADATA_SIMPLE_ITERATOR_STATUS_NOT_WRITABLE;
			return false;
		}

		if( this.type == Format.FLAC__METADATA_TYPE_STREAMINFO || block.type == Format.FLAC__METADATA_TYPE_STREAMINFO ) {
			if( this.type != block.type ) {
				this.status = FLAC__METADATA_SIMPLE_ITERATOR_STATUS_ILLEGAL_INPUT;
				return false;
			}
		}

		block.is_last = this.is_last;

		if( this.length == block.length) {
			return write_metadata_block_stationary_( block );
		} else if( this.length > block.length ) {
			if( use_padding && this.length >= Format.FLAC__STREAM_METADATA_HEADER_LENGTH + block.length ) {
				final boolean ret = write_metadata_block_stationary_with_padding_( block, this.length - Format.FLAC__STREAM_METADATA_HEADER_LENGTH - block.length, block.is_last );
				//FLAC__ASSERT(!ret || iterator->offset[iterator->depth] == debug_target_offset);
				//FLAC__ASSERT(!ret || ftello(iterator->file) == debug_target_offset + (off_t)FLAC__STREAM_METADATA_HEADER_LENGTH);
				return ret;
			}
			else {
				final boolean ret = rewrite_whole_file_( block, /*append=*/false );
				//FLAC__ASSERT(!ret || iterator->offset[iterator->depth] == debug_target_offset);
				//FLAC__ASSERT(!ret || ftello(iterator->file) == debug_target_offset + (off_t)FLAC__STREAM_METADATA_HEADER_LENGTH);
				return ret;
			}
		}
		else /* iterator->length < block->length */ {
			int padding_leftover = 0;
			boolean padding_is_last = false;
			if( use_padding ) {
				/* first see if we can even use padding */
				if( this.is_last ) {
					use_padding = false;
				}
				else {
					final int extra_padding_bytes_required = block.length - this.length;
					push_();
					if( ! next() ) {
						pop_();
						return false;
					}
					if( this.type != Format.FLAC__METADATA_TYPE_PADDING ) {
						use_padding = false;
					}
					else {
						if( Format.FLAC__STREAM_METADATA_HEADER_LENGTH + this.length == extra_padding_bytes_required ) {
							padding_leftover = 0;
							block.is_last = this.is_last;
						}
						else if( this.length < extra_padding_bytes_required ) {
							use_padding = false;
						} else {
							padding_leftover = Format.FLAC__STREAM_METADATA_HEADER_LENGTH + this.length - extra_padding_bytes_required;
							padding_is_last = this.is_last;
							block.is_last = false;
						}
					}
					if( ! pop_() ) {
						return false;
					}
				}
			}
			if( use_padding ) {
				if( padding_leftover == 0 ) {
					final boolean ret = write_metadata_block_stationary_( block );
					//FLAC__ASSERT(!ret || this.offset[iterator->depth] == debug_target_offset);
					//FLAC__ASSERT(!ret || ftello(iterator->file) == debug_target_offset + (off_t)FLAC__STREAM_METADATA_HEADER_LENGTH);
					return ret;
				}
				else {
					//FLAC__ASSERT(padding_leftover >= FLAC__STREAM_METADATA_HEADER_LENGTH);
					final boolean ret = write_metadata_block_stationary_with_padding_( block, padding_leftover - Format.FLAC__STREAM_METADATA_HEADER_LENGTH, padding_is_last );
					//FLAC__ASSERT(!ret || iterator->offset[iterator->depth] == debug_target_offset);
					//FLAC__ASSERT(!ret || ftello(iterator->file) == debug_target_offset + (off_t)FLAC__STREAM_METADATA_HEADER_LENGTH);
					return ret;
				}
			}
			else {
				final boolean ret = rewrite_whole_file_( block, /*append=*/false );
				//FLAC__ASSERT(!ret || iterator->offset[iterator->depth] == debug_target_offset);
				//FLAC__ASSERT(!ret || ftello(iterator->file) == debug_target_offset + (off_t)FLAC__STREAM_METADATA_HEADER_LENGTH);
				return ret;
			}
		}
	}

	/** This is similar to FLAC__metadata_simple_iterator_set_block()
	 *  except that instead of writing over an existing block, it appends
	 *  a block after the existing block.  \a use_padding is again used to
	 *  tell the function to try an expand into following padding in an
	 *  attempt to avoid rewriting the entire file.
	 *
	 *  This function will fail and return \c false if given a STREAMINFO
	 *  block.
	 *
	 *  After writing the block, the iterator will be pointing to the
	 *  new block.
	 *
	 * @param block        The block to set.
	 * @param use_padding  See above.
	 * \assert
	 *    \code iterator != NULL \endcode
	 *    \a iterator has been successfully initialized with
	 *    FLAC__metadata_simple_iterator_init()
	 *    \code block != NULL \endcode
	 * @retval FLAC__bool
	 *    \c true if successful, else \c false.
	 */
	public final boolean insert_block_after(final StreamMetadata block, boolean use_padding)
	{
		//FLAC__ASSERT_DECLARATION(off_t debug_target_offset = iterator->offset[iterator->depth] + FLAC__STREAM_METADATA_HEADER_LENGTH + iterator->length;)

		//FLAC__ASSERT(0 != iterator);
		//FLAC__ASSERT(0 != iterator->file);
		//FLAC__ASSERT(0 != block);

		if( ! this.is_writable ) {
			this.status = FLAC__METADATA_SIMPLE_ITERATOR_STATUS_NOT_WRITABLE;
			return false;
		}

		if( block.type == Format.FLAC__METADATA_TYPE_STREAMINFO ) {
			this.status = FLAC__METADATA_SIMPLE_ITERATOR_STATUS_ILLEGAL_INPUT;
			return false;
		}

		block.is_last = this.is_last;

		int padding_leftover = 0;
		boolean padding_is_last = false;
		if( use_padding ) {
			/* first see if we can even use padding */
			if( this.is_last ) {
				use_padding = false;
			}
			else {
				push_();
				if( ! next() ) {
					pop_();
					return false;
				}
				if( this.type != Format.FLAC__METADATA_TYPE_PADDING ) {
					use_padding = false;
				}
				else {
					if( this.length == block.length ) {
						padding_leftover = 0;
						block.is_last = this.is_last;
					}
					else if( this.length < Format.FLAC__STREAM_METADATA_HEADER_LENGTH + block.length ) {
						use_padding = false;
					} else {
						padding_leftover = this.length - block.length;
						padding_is_last = this.is_last;
						block.is_last = false;
					}
				}
				if( ! pop_() ) {
					return false;
				}
			}
		}
		if( use_padding ) {
			/* move to the next block, which is suitable padding */
			if( ! next() ) {
				return false;
			}
			if( padding_leftover == 0 ) {
				final boolean ret = write_metadata_block_stationary_( block );
				//FLAC__ASSERT(iterator->offset[iterator->depth] == debug_target_offset);
				//FLAC__ASSERT(ftello(iterator->file) == debug_target_offset + (off_t)FLAC__STREAM_METADATA_HEADER_LENGTH);
				return ret;
			}
			else {
				//FLAC__ASSERT(padding_leftover >= FLAC__STREAM_METADATA_HEADER_LENGTH);
				final boolean ret = write_metadata_block_stationary_with_padding_( block, padding_leftover - Format.FLAC__STREAM_METADATA_HEADER_LENGTH, padding_is_last );
				//FLAC__ASSERT(iterator->offset[iterator->depth] == debug_target_offset);
				//FLAC__ASSERT(ftello(iterator->file) == debug_target_offset + (off_t)FLAC__STREAM_METADATA_HEADER_LENGTH);
				return ret;
			}
		}
		else {
			final boolean ret = rewrite_whole_file_( block, /*append=*/true );
			//FLAC__ASSERT(iterator->offset[iterator->depth] == debug_target_offset);
			//FLAC__ASSERT(ftello(iterator->file) == debug_target_offset + (off_t)FLAC__STREAM_METADATA_HEADER_LENGTH);
			return ret;
		}
	}

	/** Deletes the block at the current position.  This will cause the
	 *  entire FLAC file to be rewritten, unless \a use_padding is \c true,
	 *  in which case the block will be replaced by an equal-sized PADDING
	 *  block.  The iterator will be left pointing to the block before the
	 *  one just deleted.
	 *
	 *  You may not delete the STREAMINFO block.
	 *
	 * @param use_padding  See above.
	 * \assert
	 *    \code iterator != NULL \endcode
	 *    \a iterator has been successfully initialized with
	 *    FLAC__metadata_simple_iterator_init()
	 * @retval FLAC__bool
	 *    \c true if successful, else \c false.
	 */
	public final boolean delete_block(final boolean use_padding)
	{
		//FLAC__ASSERT_DECLARATION(off_t debug_target_offset = iterator->offset[iterator->depth];)

		if( ! this.is_writable ) {
			this.status = FLAC__METADATA_SIMPLE_ITERATOR_STATUS_NOT_WRITABLE;
			return false;
		}

		if( this.type == Format.FLAC__METADATA_TYPE_STREAMINFO ) {
			this.status = FLAC__METADATA_SIMPLE_ITERATOR_STATUS_ILLEGAL_INPUT;
			return false;
		}

		if( use_padding ) {
			final StreamMetadata padding = StreamMetadata.metadata_new( Format.FLAC__METADATA_TYPE_PADDING );
			if( null == padding ) {
				this.status = FLAC__METADATA_SIMPLE_ITERATOR_STATUS_MEMORY_ALLOCATION_ERROR;
				return false;
			}
			padding.length = this.length;
			if( ! set_block( padding, false ) ) {
				StreamMetadata.delete( padding );
				return false;
			}
			StreamMetadata.delete( padding );
			if( ! prev() ) {
				return false;
			}
			//FLAC__ASSERT(iterator->offset[iterator->depth] + (off_t)FLAC__STREAM_METADATA_HEADER_LENGTH + (off_t)iterator->length == debug_target_offset);
			//FLAC__ASSERT(ftello(iterator->file) + (off_t)iterator->length == debug_target_offset);
			return true;
		}
		else {
			final boolean ret = rewrite_whole_file_( null, /*append=*/false );
			//FLAC__ASSERT(iterator->offset[iterator->depth] + (off_t)FLAC__STREAM_METADATA_HEADER_LENGTH + (off_t)iterator->length == debug_target_offset);
			//FLAC__ASSERT(ftello(iterator->file) + (off_t)iterator->length == debug_target_offset);
			return ret;
		}
	}
}
