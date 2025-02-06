package org.xiph.flac;

/** FLAC PADDING structure.  (c.f. <A HREF="../format.html#metadata_block_padding">format specification</A>)
 */
public class Padding extends StreamMetadata {
	/** Conceptually this is an empty struct since we don't store the
	 * padding bytes.  Empty structs are not allowed by some C compilers,
	 * hence the dummy.
	 */
	public Padding() {
		super.type = Format.FLAC__METADATA_TYPE_PADDING;
	}
	Padding(final Padding m) {
		super( m );
	}
}
