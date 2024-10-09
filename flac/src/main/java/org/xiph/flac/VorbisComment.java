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

import java.util.Arrays;

// java: FLAC__StreamMetadata_VorbisComment_Entry replaced by the String
/** FLAC VORBIS_COMMENT structure.  (c.f. <A HREF="../format.html#metadata_block_vorbis_comment">format specification</A>)
 */
public class VorbisComment extends StreamMetadata {
	static final String ENCODING = "UTF-8";

	String vendor_string = null;
	String[] comments = null;
	int num_comments = 0;// TODO use comments.length

	VorbisComment() {
		super.type = Format.FLAC__METADATA_TYPE_VORBIS_COMMENT;
	}

	VorbisComment(final VorbisComment m) {
		copyFrom( m );
	}
	private final void copyFrom(final VorbisComment m) {
		super.copyFrom( m );
		vendor_string = m.vendor_string;
		comments = entry_array_copy_( m.comments, comments.length );
	}

	private static String[] entry_array_new_(final int num_comments)
	{
		//FLAC__ASSERT(num_comments > 0);

		return new String[num_comments];
	}

	/* java: do not need
	static void entry_array_delete_(FLAC__StreamMetadata_VorbisComment_Entry *object_array, unsigned num_comments)
	{
		unsigned i;

		FLAC__ASSERT(object_array != NULL && num_comments > 0);

		for(i = 0; i < num_comments; i++)
			if(0 != object_array[i].entry)
				free(object_array[i].entry);

		if(0 != object_array)
			free(object_array);
	}
	*/

	private static String[] entry_array_copy_(final String[] object_array, final int num_comments)
	{
		//FLAC__ASSERT(object_array != NULL);
		//FLAC__ASSERT(num_comments > 0);

		try {
			final String[] return_array = entry_array_new_( num_comments );

			if( return_array != null ) {
				for( int i = 0; i < num_comments; i++ ) {
					return_array[i] = object_array[i];
				}
			}

			return return_array;
		} catch (final OutOfMemoryError e) {
		}
		return null;
	}

	final void calculate_length_()
	{
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_VORBIS_COMMENT);

		this.length = (Format.FLAC__STREAM_METADATA_VORBIS_COMMENT_ENTRY_LENGTH_LEN) / 8;
		this.length += this.vendor_string.length();
		this.length += (Format.FLAC__STREAM_METADATA_VORBIS_COMMENT_NUM_COMMENTS_LEN) / 8;
		for( int i = 0, ie = this.num_comments; i < ie; i++ ) {
			this.length += (Format.FLAC__STREAM_METADATA_VORBIS_COMMENT_ENTRY_LENGTH_LEN / 8);
			final String comment = this.comments[i];// java
			if( comment != null ) {
				this.length += comment.length();
			}
		}
	}

	private final boolean set_entry_(final String[] dest, final int offset, final String src, final boolean copy)
	{// TODO return void and use OutOfMemoryError exception?
		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(dest != NULL);
		//FLAC__ASSERT(src != NULL);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_VORBIS_COMMENT);
		//FLAC__ASSERT((src->entry != NULL && src->length > 0) || (src->entry == NULL && src->length == 0));

		if( src != null ) {
			if( copy ) {
				/* do the copy first so that if we fail we leave the dest object untouched */
				dest[offset] = new String( src );
			} else {
				dest[offset] = src;
			}
		}
		else {
			/* the src is null */
			dest[offset] = src;
		}

		calculate_length_();
		return true;
	}

	private final int find_entry_from_(final int offset, final String field_name, final int field_name_length)
	{
		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_VORBIS_COMMENT);
		//FLAC__ASSERT(field_name != NULL);

		for( int i = offset; i < this.num_comments; i++ ) {
			if( entry_matches( this.comments[i], field_name, field_name_length ) ) {
				return i;
			}
		}

		return -1;
	}

	static boolean compare_block_data_(final VorbisComment block1, final VorbisComment block2)
	{
		if( block1.vendor_string != null && block2.vendor_string != null ) {
			if( ! block1.vendor_string.equals( block2.vendor_string ) ) {
				return false;
			}
		}
		else if( block1.vendor_string != block2.vendor_string ) {
			return false;
		}

		if( block1.num_comments != block2.num_comments ) {
			return false;
		}

		for( int i = 0; i < block1.num_comments; i++ ) {
			if( block1.comments[i] != null && block2.comments[i] != null ) {
				if( ! block1.comments[i].equals( block2.comments[i] ) ) {
					return false;
				}
			}
			else if( block1.comments[i] != block2.comments[i] ) {
				return false;
			}
		}
		return true;
	}

	public final boolean set_vendor_string(final String entry, final boolean copy)
	{
		//if( ! entry_value_is_legal( entry.entry, entry.length ) )
		//	return false;
		//return object.vorbiscomment_set_entry_( object.vendor_string, entry, copy );
		if( copy ) {
			/* do the copy first so that if we fail we leave the dest object untouched */
			this.vendor_string = new String( entry );
			return true;// FIXME why length do not calculating?
		}
		this.vendor_string = entry;
		calculate_length_();
		return true;
	}

	public final boolean resize_comments(final int new_num_comments)
	{
		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_VORBIS_COMMENT);

		if( this.comments == null ) {
			//FLAC__ASSERT(object->data.vorbis_comment.num_comments == 0);
			if( new_num_comments == 0 ) {
				return true;
			} else {
				if( (this.comments = entry_array_new_( new_num_comments )) == null ) {
					return false;
				}
				for( int i = 0; i < new_num_comments; i++ ) {
					comments[ i ] = new String();
				}
			}
		}
		else {
			/* overflow check */
			if( new_num_comments > Format.SIZE_MAX ) {
				return false;
			}

			//FLAC__ASSERT(object->data.vorbis_comment.num_comments > 0);

			/* if shrinking, free the truncated entries */
			if( new_num_comments < this.num_comments ) {
				for( int i = new_num_comments; i < this.num_comments; i++ ) {
					this.comments[i] = null;
				}
			}

			if( new_num_comments == 0 ) {
				this.comments = null;
			}
			else if( null == (this.comments = Arrays.copyOf( this.comments, new_num_comments )) ) {
				return false;
			}

			/* if growing, zero all the length/pointers of new elements */
			if( new_num_comments > this.num_comments ) {
				for( int i = this.num_comments; i < new_num_comments; i++) {
					this.comments[i] = new String();
				}
			}
		}

		this.num_comments = new_num_comments;

		calculate_length_();
		return true;
	}

	public final boolean set_comment(final int comment_num, final String entry, final boolean copy)
	{
		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(comment_num < object->data.vorbis_comment.num_comments);

		//if( ! entry_is_legal( entry.entry, entry.length ) )// java: do not need
		//	return false;
		return set_entry_( this.comments, comment_num, entry, copy );
	}

	public final boolean insert_comment(final int comment_num, final String entry, final boolean copy)
	{
		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_VORBIS_COMMENT);
		//FLAC__ASSERT(comment_num <= object->data.vorbis_comment.num_comments);

		/* java: do not need
		if( ! entry_is_legal( entry.entry, entry.length ) )
			return false;
		*/

		if( ! resize_comments( this.num_comments + 1 ) ) {
			return false;
		}

		/* move all comments >= comment_num forward one space */
		System.arraycopy( this.comments, comment_num, this.comments, comment_num + 1, this.num_comments - 1 - comment_num );
		this.comments[comment_num] = null;

		/* reuse newly added empty comment */
		final String temp = this.comments[ this.num_comments - 1 ];
		System.arraycopy( this.comments, comment_num, this.comments, comment_num + 1, this.num_comments - 1 - comment_num );
		this.comments[comment_num] = temp;

		return set_comment( comment_num, entry, copy );
	}

	public final boolean append_comment(final String entry, final boolean copy)
	{
		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_VORBIS_COMMENT);
		return insert_comment( this.num_comments, entry, copy );
	}

	public final boolean replace_comment(String entry, final boolean all, final boolean copy)
	{
		//FLAC__ASSERT(entry.entry != NULL && entry.length > 0);

		//if( !entry_is_legal(entry.entry, entry.length))
		//	return false;

		{
			final int field_name_length = entry.indexOf('=');

			if( field_name_length < 0 ) {
				return false; /* double protection */
			}

			int i = find_entry_from_( 0, entry, field_name_length );
			if( i >= 0 ) {
				int indx = i;
				if( ! set_comment( indx, entry, copy ) ) {
					return false;
				}
				entry = this.comments[indx];
				indx++; /* skip over replaced comment */
				if( all && indx < this.num_comments ) {
					i = find_entry_from_( indx, entry, field_name_length );
					while( i >= 0 ) {
						indx = i;
						if( ! delete_comment( indx ) ) {
							return false;
						}
						if( indx < this.num_comments ) {
							i = find_entry_from_( indx, entry, field_name_length );
						} else {
							i = -1;
						}
					}
				}
				return true;
			} else {
				return this.append_comment( entry, copy );
			}
		}
	}

	public final boolean delete_comment(final int comment_num)
	{
		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_VORBIS_COMMENT);
		//FLAC__ASSERT(comment_num < object->data.vorbis_comment.num_comments);

		/* free the comment at comment_num */
		this.comments[comment_num] = null;

		/* move all comments > comment_num backward one space */
		final int n1 = this.num_comments - 1;// java
		System.arraycopy( this.comments, comment_num + 1, this.comments, comment_num, n1 - comment_num );
		this.comments[n1] = null;

		return this.resize_comments( n1 );
	}

	/** @return StreamMetadata_VorbisComment_Entry or null, if field_name or field_value is null */
	public static String /* boolean*/ entry_from_name_value_pair(
			final String /* FLAC__StreamMetadata_VorbisComment_Entry */ field_name,
			final String /* FLAC__StreamMetadata_VorbisComment_Entry */ field_value)
	{
		//FLAC__ASSERT(entry != NULL);
		//FLAC__ASSERT(field_name != NULL);
		//FLAC__ASSERT(field_value != NULL);

		/* java: do not need
		if(!entry_name_is_legal(field_name))
			return false;
		if(!entry_value_is_legal((const FLAC__byte *)field_value, (unsigned)(-1)))
			return false;
		*/
		if( field_name != null && field_value != null ) {
			return field_name + "=" + field_value;
		}

		return null;
	}

	/** @return String[2] with field_name and field_value, null if error */
	public static String[] /* boolean */ entry_to_name_value_pair(final String entry/*, char **field_name, char **field_value*/)
	{
		//FLAC__ASSERT(entry.entry != NULL && entry.length > 0);
		//FLAC__ASSERT(field_name != NULL);
		//FLAC__ASSERT(field_value != NULL);

		//if( ! entry_is_legal( entry.entry, entry.length ) )
		//	return false;

		int p = entry.indexOf('=');
		if( p < 0 ) {
			return null; /* double protection */
		}
		final String[] ret = new String[2];
		ret[0] = entry.substring( 0, p );
		ret[1] = entry.substring( ++p );

		return ret;
	}

	public static boolean entry_matches(final String entry, final String field_name, final int field_name_length)
	{
		//FLAC__ASSERT(entry.entry != NULL && entry.length > 0);
		{
			final int p = entry.indexOf('=');
			if( p < 0 || p != field_name_length ) {
				return false;
			}
			return entry.substring( 0, p ).equalsIgnoreCase( field_name );
		}
	}

	public final int find_entry_from(final int offset, final String field_name)
	{
		//FLAC__ASSERT(field_name != NULL);

		return find_entry_from_( offset, field_name, field_name.length() );
	}

	public final int remove_entry_matching(final String field_name)
	{
		final int field_name_length = field_name.length();

		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_VORBIS_COMMENT);

		for( int i = 0, ncomments = this.num_comments; i < ncomments; i++ ) {
			if( entry_matches( this.comments[i], field_name, field_name_length ) ) {
				if( ! delete_comment( i ) ) {
					return -1;
				}// else {
					return 1;
				//}
			}
		}

		return 0;
	}

	public final int remove_entries_matching(final String field_name)
	{
		boolean ok = true;
		int matching = 0;
		final int field_name_length = field_name.length();

		//FLAC__ASSERT(0 != object);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_VORBIS_COMMENT);

		/* must delete from end to start otherwise it will interfere with our iteration */
		for( int i = this.num_comments - 1; ok && i >= 0; i-- ) {
			if( entry_matches( this.comments[i], field_name, field_name_length ) ) {
				matching++;
				ok &= delete_comment( i );
			}
		}

		return ok ? matching : -1;
	}

	// metadata_iterators.c
	public boolean metadata_get_tags(final String filename)
	{
		//FLAC__ASSERT(0 != filename);
		//FLAC__ASSERT(0 != tags);
		final VorbisComment tags;
		tags = (VorbisComment) get_one_metadata_block_( filename, Format.FLAC__METADATA_TYPE_VORBIS_COMMENT );
		if( tags != null ) {
			copyFrom( tags );
		}

		return null != tags;
	}
}
