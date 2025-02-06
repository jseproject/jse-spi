package org.xiph.flac;

public class MetadataIterator {
	private MetadataChain chain = null;
	private MetadataNode current = null;

	/* java: use iterator = null
	public static void delete(MetadataIterator iterator)
	{
		//FLAC__ASSERT(0 != iterator);

		//free(iterator);
	}*/

	public final void init(final MetadataChain metadata_chain)
	{
		//FLAC__ASSERT(0 != iterator);
		//FLAC__ASSERT(0 != chain);
		//FLAC__ASSERT(0 != chain->head);

		this.chain = metadata_chain;
		this.current = metadata_chain.head;
	}

	public final boolean next()
	{
		//FLAC__ASSERT(0 != iterator);

		if( null == this.current || null == this.current.next ) {
			return false;
		}

		this.current = this.current.next;
		return true;
	}

	public final boolean prev()
	{
		//FLAC__ASSERT(0 != iterator);

		if( null == this.current || null == this.current.prev) {
			return false;
		}

		this.current = this.current.prev;
		return true;
	}

	public final int /* FLAC__MetadataType */ get_block_type()
	{
		//FLAC__ASSERT(0 != iterator);
		//FLAC__ASSERT(0 != iterator->current);
		//FLAC__ASSERT(0 != iterator->current->data);

		return this.current.data.type;
	}

	public final StreamMetadata get_block()
	{
		//FLAC__ASSERT(0 != iterator);
		//FLAC__ASSERT(0 != iterator->current);

		return this.current.data;
	}

	public final boolean set_block(final StreamMetadata block)
	{
		//FLAC__ASSERT(0 != iterator);
		//FLAC__ASSERT(0 != block);
		return delete_block( false ) &&
			insert_block_after( block );
	}

	public final boolean delete_block(final boolean replace_with_padding)
	{
		//FLAC__ASSERT(0 != iterator);
		//FLAC__ASSERT(0 != iterator->current);

		if( null == this.current.prev ) {
			//FLAC__ASSERT(iterator->current->data->type == FLAC__METADATA_TYPE_STREAMINFO);
			return false;
		}

		final MetadataNode save = this.current.prev;

		if( replace_with_padding ) {
			StreamMetadata.delete_data( this.current.data );
			this.current.data.type = Format.FLAC__METADATA_TYPE_PADDING;
		}
		else {
			this.chain.delete_node_( this.current );
		}

		this.current = save;
		return true;
	}

	private final void insert_node_(final MetadataNode node)
	{
		//FLAC__ASSERT(0 != node);
		//FLAC__ASSERT(0 != node->data);
		//FLAC__ASSERT(0 != iterator);
		//FLAC__ASSERT(0 != iterator->current);
		//FLAC__ASSERT(0 != iterator->chain);
		//FLAC__ASSERT(0 != iterator->chain->head);
		//FLAC__ASSERT(0 != iterator->chain->tail);

		node.data.is_last = false;

		node.prev = this.current.prev;
		node.next = this.current;

		if( null == node.prev ) {
			this.chain.head = node;
		} else {
			node.prev.next = node;
		}

		this.current.prev = node;

		this.chain.nodes++;
	}

	public final boolean insert_block_before(final StreamMetadata block)
	{
		//FLAC__ASSERT(0 != iterator);
		//FLAC__ASSERT(0 != iterator->current);
		//FLAC__ASSERT(0 != block);

		if( block.type == Format.FLAC__METADATA_TYPE_STREAMINFO ) {
			return false;
		}

		if( null == this.current.prev ) {
			//FLAC__ASSERT(iterator->current->data->type == FLAC__METADATA_TYPE_STREAMINFO);
			return false;
		}

		//try {
			final MetadataNode node = new MetadataNode();
			node.data = block;
			insert_node_( node );
			this.current = node;
			return true;
		//} catch(OutOfMemoryError e) {
		//	return false;
		//}
	}

	private final void insert_node_after_(final MetadataNode node)
	{
		//FLAC__ASSERT(0 != node);
		//FLAC__ASSERT(0 != node->data);
		//FLAC__ASSERT(0 != iterator);
		//FLAC__ASSERT(0 != iterator->current);
		//FLAC__ASSERT(0 != iterator->chain);
		//FLAC__ASSERT(0 != iterator->chain->head);
		//FLAC__ASSERT(0 != iterator->chain->tail);

		this.current.data.is_last = false;

		node.prev = this.current;
		node.next = this.current.next;

		if( null == node.next ) {
			this.chain.tail = node;
		} else {
			node.next.prev = node;
		}

		node.prev.next = node;

		this.chain.tail.data.is_last = true;

		this.chain.nodes++;
	}

	public final boolean insert_block_after(final StreamMetadata block)
	{
		MetadataNode node;

		//FLAC__ASSERT(0 != iterator);
		//FLAC__ASSERT(0 != iterator->current);
		//FLAC__ASSERT(0 != block);

		if( block.type == Format.FLAC__METADATA_TYPE_STREAMINFO ) {
			return false;
		}

		//try {
			node = new MetadataNode();
			node.data = block;
			insert_node_after_( node );
			this.current = node;
			return true;
		//} catch(OutOfMemoryError e) {
		//	return false;
		//}
	}

}
