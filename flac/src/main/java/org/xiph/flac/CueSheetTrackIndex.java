package org.xiph.flac;

/** FLAC CUESHEET track index structure.  (See the
 * <A HREF="../format.html#cuesheet_track_index">format specification</A> for
 * the full description of each field.)
 */
public class CueSheetTrackIndex {
	/** Offset in samples, relative to the track offset, of the index
	 * point.
	 */
	public long offset = 0;

	/** The index point number. */
	byte number = 0;// TODO check using. value must be less then 128! also check castings.

	CueSheetTrackIndex() {
	}

	CueSheetTrackIndex(final CueSheetTrackIndex cs) {
		copyFrom( cs );
	}

	private final void copyFrom(final CueSheetTrackIndex cs) {
		offset = cs.offset;
		number = cs.number;
	}

	static CueSheetTrackIndex[] array_new_(final int num_indices)
	{
		//FLAC__ASSERT(num_indices > 0);

		final CueSheetTrackIndex[] array = new CueSheetTrackIndex[num_indices];
		for( int i = 0; i < num_indices; i++ ) {
			array[i] = new CueSheetTrackIndex();
		}
		return array;
	}
}
