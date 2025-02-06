package net.sourceforge.lame;

class ID3TagSpec {
	/* private data members */
	int flags;
	int year;
	byte[] title;
	byte[] artist;
	byte[] album;
	byte[] comment;
	int track_id3v1;
	int genre_id3v1;
	byte[] albumart;
	//int albumart_size;// albumart.length
	int padding_size;
	int albumart_mimetype;
	/** the language of the frame's content, according to ISO-639-2 */
	String language = "";// char[4];
	FrameDataNode v2_head, v2_tail;
	//
	final void clear() {
		flags = 0;
		year = 0;
		title = null;
		artist = null;
		album = null;
		comment = null;
		track_id3v1 = 0;
		genre_id3v1 = 0;
		albumart = null;
		// albumart_size = 0;
		padding_size = 0;
		albumart_mimetype = 0;
		v2_head = null;
		v2_tail = null;
	}
}
