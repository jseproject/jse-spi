package org.xiph.flac;

/** Header for the entropy coding method.  (c.f. <A HREF="../format.html#residual">format specification</A>)
 */
class EntropyCodingMethod {

	int /*FLAC__EntropyCodingMethodType*/ type;
	//union {
		final PartitionedRice partitioned_rice = new PartitionedRice();
	//} data;
}
