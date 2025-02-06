package org.xiph.flac;

/** SeekPoint structure used in SEEKTABLE blocks.  (c.f. <A HREF="../format.html#seekpoint">format specification</A>)
 */
public class SeekPoint implements Comparable<SeekPoint> {
	/**  The sample number of the target frame. */
	long sample_number = 0;

	/** The offset, in bytes, of the target frame with respect to
	 * beginning of the first frame. */
	long stream_offset = 0;

	/** The number of samples in the target frame. */
	int frame_samples = 0;

	@Override
	public int compareTo(final SeekPoint r) {
		/** used as the sort predicate for qsort() */
	//static int seekpoint_compare_(final SeekPoint l, final SeekPoint r)
		/* we don't just 'return l->sample_number - r->sample_number' since the result (FLAC__int64) might overflow an 'int' */
		if( sample_number == r.sample_number ) {
			return 0;
		} else if( sample_number < r.sample_number ) {
			return -1;
		}
		// else
			return 1;
	}

	static final SeekPoint[] array_new_(final int num_points)
	{
		//FLAC__ASSERT(num_points > 0);

		try {
			final SeekPoint[] object_array = new SeekPoint[num_points];

			// if( object_array != null ) {// java: do not
				for( int i = 0; i < num_points; i++ ) {
					object_array[i].sample_number = Format.FLAC__STREAM_METADATA_SEEKPOINT_PLACEHOLDER;
					object_array[i].stream_offset = 0;
					object_array[i].frame_samples = 0;
				}
			// }

			return object_array;
		} catch (final OutOfMemoryError e) {
		}
		return null;
	}
}
