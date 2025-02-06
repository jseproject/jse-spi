package org.xiph.flac;

/** FLAC APPLICATION structure.  (c.f. <A HREF="../format.html#metadata_block_application">format specification</A>)
 */
public class Application extends StreamMetadata {
	final byte id[] = new byte[4];
	byte[] data = null;

	Application() {
		super.type = Format.FLAC__METADATA_TYPE_APPLICATION;
	}

	Application(final Application m) {
		super( m );
		System.arraycopy( m.id, 0, id, 0, id.length );
		data = m.data;
	}

	static boolean compare_block_data_(final Application block1, final Application block2, final int block_length)
	{
		//FLAC__ASSERT(block1 != NULL);
		//FLAC__ASSERT(block2 != NULL);
		//FLAC__ASSERT(block_length >= sizeof(block1->id));

		if( Format.memcmp( block1.id, 0, block2.id, 0, block1.id.length ) != 0 ) {
			return false;
		}
		if( block1.data != null && block2.data != null ) {
			return Format.memcmp( block1.data, 0, block2.data, 0, block_length - block1.id.length ) == 0;
		} else {
			return block1.data == block2.data;
		}
	}

	public final boolean set_data(final byte[] d, final int size, final boolean copy)
	{

		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_APPLICATION);
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

		this.length = Format.FLAC__STREAM_METADATA_APPLICATION_ID_LEN / 8 + size;
		return true;
	}
}
