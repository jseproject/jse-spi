package org.xiph.flac;

/** Header for a Rice partitioned residual.  (c.f. <A HREF="../format.html#partitioned_rice">format specification</A>)
 */
class PartitionedRice {
	/** The partition order, i.e. # of contexts = 2 ^ \a order. */
	int order = 0;

	/** The context's Rice parameters and/or raw bits. */
	PartitionedRiceContents contents = null;

	/*public PartitionedRice(PartitionedRiceContents c) {
		contents = c;
	}*/
}

