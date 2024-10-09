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

/** FLAC PICTURE structure.  (See the
 * <A HREF="../format.html#metadata_block_picture">format specification</A>
 * for the full description of each field.)
 */
public class Picture extends StreamMetadata {
	static final String MIME_ENCODING = "US-ASCII";
	static final String DESCRIPTION_ENCODING = "UTF-8";

	/** The kind of picture stored. */
	int /* FLAC__StreamMetadata_Picture_Type */ picture_type = Format.FLAC__STREAM_METADATA_PICTURE_TYPE_UNDEFINED;// type is hiding the type field from type StreamMetadata, renamed to picture_type

	/** Picture data's MIME type, in ASCII printable characters
	 * 0x20-0x7e, NUL terminated.  For best compatibility with players,
	 * use picture data of MIME type \c image/jpeg or \c image/png.  A
	 * MIME type of '-->' is also allowed, in which case the picture
	 * data should be a complete URL.  In file storage, the MIME type is
	 * stored as a 32-bit length followed by the ASCII string with no NUL
	 * terminator, but is converted to a plain C string in this structure
	 * for convenience.
	 */
	String mime_type;

	/** Picture's description in UTF-8, NUL terminated.  In file storage,
	 * the description is stored as a 32-bit length followed by the UTF-8
	 * string with no NUL terminator, but is converted to a plain C string
	 * in this structure for convenience.
	 */
	String description = null;

	/** Picture's width in pixels. */
	int width = 0;

	/** Picture's height in pixels. */
	int height = 0;

	/** Picture's color depth in bits-per-pixel. */
	int depth = 0;

	/** For indexed palettes (like GIF), picture's number of colors (the
	 * number of palette entries), or \c 0 for non-indexed (i.e. 2^depth).
	 */
	int colors = 0;

	/** Length of binary picture data in bytes. */
	int data_length = 0;

	/** Binary picture data. */
	byte[] data = null;

	Picture() {
		super.type = Format.FLAC__METADATA_TYPE_PICTURE;
	}

	Picture(final Picture m) {
		super( m );
		picture_type = m.picture_type;
		mime_type = m.mime_type;
		description = m.description;
		width = m.width;
		depth = m.depth;
		colors = m.colors;
		data_length = m.data_length;
		data = m.data;// TODO check, C uses full copy
	}

	static boolean compare_block_data_(final Picture block1, final Picture block2)
	{
		if( block1.type != block2.type ) {
			return false;
		}
		if( block1.mime_type != block2.mime_type && (block1.mime_type == null || block2.mime_type == null || ! block1.mime_type.equals( block2.mime_type )) ) {
			return false;
		}
		if( block1.description != block2.description && (block1.description == null || block2.description == null || ! block1.description.equals( block2.description )) ) {
			return false;
		}
		if( block1.width != block2.width ) {
			return false;
		}
		if( block1.height != block2.height ) {
			return false;
		}
		if( block1.depth != block2.depth ) {
			return false;
		}
		if( block1.colors != block2.colors ) {
			return false;
		}
		if( block1.data_length != block2.data_length ) {
			return false;
		}
		if( block1.data != block2.data && (block1.data == null || block2.data == null || 0 != Format.memcmp( block1.data, 0, block2.data, 0, block1.data_length )) ) {
			return false;
		}
		return true;
	}

	public final boolean set_mime_type(final String mime, final boolean copy)
	{
		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_PICTURE);
		//FLAC__ASSERT(mime_type != NULL);

		final String old = this.mime_type;
		final int old_length = old != null ? old.length() : 0;
		final int new_length = mime.length();

		if( new_length > Format.SIZE_MAX ) {
			return false;
		}

		/* do the copy first so that if we fail we leave the object untouched */

		this.mime_type = mime;

		this.length -= old_length;
		this.length += new_length;
		return true;
	}

	public final boolean set_description(final String pic_description, final boolean copy)
	{
		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_PICTURE);
		//FLAC__ASSERT(description != NULL);

		final String old = this.description;
		final int old_length = old != null ? old.length() : 0;
		final int new_length = pic_description.length();

		if( new_length > Format.SIZE_MAX ) {
			return false;
		}

		/* do the copy first so that if we fail we leave the object untouched */

		this.description = pic_description;

		this.length -= old_length;
		this.length += new_length;
		return true;
	}

	public final boolean set_data(final byte[] d, final int size, final boolean copy)
	{
		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_PICTURE);
		//FLAC__ASSERT((data != NULL && length > 0) || (data == NULL && length == 0 && copy == false));

		/* do the copy first so that if we fail we leave the object untouched */
		if( copy ) {
			if( d != null ) {
				this.data = new byte[size];
				System.arraycopy( d, 0, this.data, 0, size );
			} else {
				this.data = null;
			}
		}
		else {
			this.data = d;
		}

		this.length -= this.data_length;
		this.data_length = size;
		this.length += size;
		return true;
	}

	/** @return null if legal, String if not legal */
	public final String /* boolean */ is_legal(/* const char **violation*/)
	{
		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_PICTURE);

		return format_picture_is_legal( /*, violation*/ );
	}

	/* @@@@ add to unit tests; it is already indirectly tested by the metadata_object tests */
	/** @return null if legal, String if not legal */
	public final String format_picture_is_legal()//, final String[] violation)
	{
		for( int p = 0; p < this.mime_type.length(); p++ ) {
			if( this.mime_type.charAt( p ) < 0x20 || this.mime_type.charAt( p ) > 0x7e ) {
				return "MIME type string must contain only printable ASCII characters (0x20-0x7e)";
			}
		}

		/* java: do not need, using String
		for( b = 0; picture.description[b] != 0; ) {
			int n = utf8len_( picture.description, b );
			if( n == 0 ) {
				return "description string must be valid UTF-8";
			}
			b += n;
		}
		 */
		return null;
	}
}
