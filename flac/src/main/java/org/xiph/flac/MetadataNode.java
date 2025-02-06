package org.xiph.flac;

class MetadataNode {
	StreamMetadata data = null;
	MetadataNode prev = null, next = null;

	final void delete_()
	{
		//FLAC__ASSERT(0 != node);
		if( null != this.data )
			StreamMetadata.delete( this.data );
		//free(node);
	}
}
