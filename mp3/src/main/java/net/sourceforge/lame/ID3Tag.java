package net.sourceforge.lame;

import java.nio.charset.Charset;

/*
 * HISTORY: This source file is part of LAME (see http://www.mp3dev.org)
 * and was originally adapted by Conrad Sanderson <c.sanderson@me.gu.edu.au>
 * from mp3info by Ricardo Cerqueira <rmc@rccn.net> to write only ID3 version 1
 * tags.  Don Melton <don@blivet.com> COMPLETELY rewrote it to support version
 * 2 tags and be more conformant to other standards while remaining flexible.
 *
 * NOTE: See http://id3.org/ for more information about ID3 tag formats.
 */

// id3tag.c

public class ID3Tag {
	private static final int CHANGED_FLAG  = (1 << 0);
	private static final int ADD_V2_FLAG   = (1 << 1);
	private static final int V1_ONLY_FLAG  = (1 << 2);
	private static final int V2_ONLY_FLAG  = (1 << 3);
	private static final int SPACE_V1_FLAG = (1 << 4);
	private static final int PAD_V2_FLAG   = (1 << 5);

	// enum {
		static final int MIMETYPE_NONE = 0;
		private static final int MIMETYPE_JPEG = 1;
		private static final int MIMETYPE_PNG  = 2;
		private static final int MIMETYPE_GIF  = 3;
	//};

	private static final String genre_names[] = {
		/*
		 * NOTE: The spelling of these genre names is identical to those found in
		 * Winamp and mp3info.
		 */
		"Blues", "Classic Rock", "Country", "Dance", "Disco", "Funk", "Grunge",
		"Hip-Hop", "Jazz", "Metal", "New Age", "Oldies", "Other", "Pop", "R&B",
		"Rap", "Reggae", "Rock", "Techno", "Industrial", "Alternative", "Ska",
		"Death Metal", "Pranks", "Soundtrack", "Euro-Techno", "Ambient", "Trip-Hop",
		"Vocal", "Jazz+Funk", "Fusion", "Trance", "Classical", "Instrumental",
		"Acid", "House", "Game", "Sound Clip", "Gospel", "Noise", "Alternative Rock",
		"Bass", "Soul", "Punk", "Space", "Meditative", "Instrumental Pop",
		"Instrumental Rock", "Ethnic", "Gothic", "Darkwave", "Techno-Industrial",
		"Electronic", "Pop-Folk", "Eurodance", "Dream", "Southern Rock", "Comedy",
		"Cult", "Gangsta", "Top 40", "Christian Rap", "Pop/Funk", "Jungle",
		"Native US", "Cabaret", "New Wave", "Psychedelic", "Rave",
		"Showtunes", "Trailer", "Lo-Fi", "Tribal", "Acid Punk", "Acid Jazz",
		"Polka", "Retro", "Musical", "Rock & Roll", "Hard Rock", "Folk",
		"Folk-Rock", "National Folk", "Swing", "Fast Fusion", "Bebob", "Latin",
		"Revival", "Celtic", "Bluegrass", "Avantgarde", "Gothic Rock",
		"Progressive Rock", "Psychedelic Rock", "Symphonic Rock", "Slow Rock",
		"Big Band", "Chorus", "Easy Listening", "Acoustic", "Humour", "Speech",
		"Chanson", "Opera", "Chamber Music", "Sonata", "Symphony", "Booty Bass",
		"Primus", "Porn Groove", "Satire", "Slow Jam", "Club", "Tango", "Samba",
		"Folklore", "Ballad", "Power Ballad", "Rhythmic Soul", "Freestyle", "Duet",
		"Punk Rock", "Drum Solo", "A Cappella", "Euro-House", "Dance Hall",
		"Goa", "Drum & Bass", "Club-House", "Hardcore", "Terror", "Indie",
		"BritPop", "Negerpunk", "Polsk Punk", "Beat", "Christian Gangsta",
		"Heavy Metal", "Black Metal", "Crossover", "Contemporary Christian",
		"Christian Rock", "Merengue", "Salsa", "Thrash Metal", "Anime", "JPop",
		"SynthPop"
	};

	private static final int genre_alpha_map[] = {
		123, 34, 74, 73, 99, 20, 40, 26, 145, 90, 116, 41, 135, 85, 96, 138, 89, 0,
		107, 132, 65, 88, 104, 102, 97, 136, 61, 141, 32, 1, 112, 128, 57, 140, 2,
		139, 58, 3, 125, 50, 22, 4, 55, 127, 122, 120, 98, 52, 48, 54, 124, 25, 84,
		80, 115, 81, 119, 5, 30, 36, 59, 126, 38, 49, 91, 6, 129, 79, 137, 7, 35,
		100, 131, 19, 33, 46, 47, 8, 29, 146, 63, 86, 71, 45, 142, 9, 77, 82, 64,
		133, 10, 66, 39, 11, 103, 12, 75, 134, 13, 53, 62, 109, 117, 23, 108, 92,
		67, 93, 43, 121, 15, 68, 14, 16, 76, 87, 118, 17, 78, 143, 114, 110, 69, 21,
		111, 95, 105, 42, 37, 24, 56, 44, 101, 83, 94, 106, 147, 113, 18, 51, 130,
		144, 60, 70, 31, 72, 27, 28
	};

	private static final int GENRE_INDEX_OTHER = 12;

	private static final int FRAME_ID(final char a, final char b, final char c, final char d ) {
		return ( ((int)a << 24)
			| ((int)b << 16)
			| ((int)c <<  8)
			| ((int)d <<  0) );
	}

	//typedef enum UsualStringIDs {
		private static final int ID_TITLE = FRAME_ID('T', 'I', 'T', '2');
		private static final int ID_ARTIST = FRAME_ID('T', 'P', 'E', '1');
		private static final int ID_ALBUM = FRAME_ID('T', 'A', 'L', 'B');
		private static final int ID_GENRE = FRAME_ID('T', 'C', 'O', 'N');
		private static final int ID_ENCODER = FRAME_ID('T', 'S', 'S', 'E');
		private static final int ID_PLAYLENGTH = FRAME_ID('T', 'L', 'E', 'N');
		private static final int ID_COMMENT = FRAME_ID('C', 'O', 'M', 'M'); /* full text string */
	//} UsualStringIDs;

	//typedef enum NumericStringIDs {
		//private static final int ID_DATE = FRAME_ID('T', 'D', 'A', 'T'); /* "ddMM" */// FIXME never uses ID_VSLT
		//private static final int ID_TIME = FRAME_ID('T', 'I', 'M', 'E'); /* "hhmm" */// FIXME never uses ID_VSLT
		//private static final int ID_TPOS = FRAME_ID('T', 'P', 'O', 'S'); /* '0'-'9' and '/' allowed */// FIXME never uses ID_VSLT
		private static final int ID_TRACK = FRAME_ID('T', 'R', 'C', 'K'); /* '0'-'9' and '/' allowed */
		private static final int ID_YEAR = FRAME_ID('T', 'Y', 'E', 'R'); /* "yyyy" */
	//} NumericStringIDs;

	//typedef enum MiscIDs {
		private static final int ID_TXXX = FRAME_ID('T', 'X', 'X', 'X');
		private static final int ID_WXXX = FRAME_ID('W', 'X', 'X', 'X');
		private static final int ID_SYLT = FRAME_ID('S', 'Y', 'L', 'T');
		private static final int ID_APIC = FRAME_ID('A', 'P', 'I', 'C');
		private static final int ID_GEOB = FRAME_ID('G', 'E', 'O', 'B');
		private static final int ID_PCNT = FRAME_ID('P', 'C', 'N', 'T');
		private static final int ID_AENC = FRAME_ID('A', 'E', 'N', 'C');
		private static final int ID_LINK = FRAME_ID('L', 'I', 'N', 'K');
		private static final int ID_ENCR = FRAME_ID('E', 'N', 'C', 'R');
		private static final int ID_GRID = FRAME_ID('G', 'R', 'I', 'D');
		private static final int ID_PRIV = FRAME_ID('P', 'R', 'I', 'V');
		// private static final int ID_USLT = FRAME_ID('U', 'S', 'L', 'T'); /* full text string */// FIXME never uses ID_VSLT
		private static final int ID_USER = FRAME_ID('U', 'S', 'E', 'R'); /* full text string */
		private static final int ID_PCST = FRAME_ID('P', 'C', 'S', 'T'); /* iTunes Podcast indicator, only presence important */
		private static final int ID_WFED = FRAME_ID('W', 'F', 'E', 'D'); /* iTunes Podcast URL as TEXT FRAME !!! violates standard */
	//} MiscIDs;


	private static final int frame_id_matches(final int id, final int mask) {
		int result = 0, window = 0xff;
		for( int i = 0; i < 4; ++i, window <<= 8 ) {
			final int mw = (mask & window);
			final int iw = (id & window);
			if( mw != 0 && mw != iw ) {
				result |= iw;
			}
		}
		return result;
	}

	private static final boolean isFrameIdMatching(final int id, final int mask) {
		return frame_id_matches( id, mask ) == 0;
	}

	private static final boolean test_tag_spec_flags(final InternalFlags gfc, final int tst) {
		return (gfc.tag_spec.flags & tst) != 0;
	}

	private static final boolean is_internal_flags_null(final GlobalFlags gfp)
	{
		return (gfp == null || gfp.internal_flags == null);
	}

	private static final void copyV1ToV2(final GlobalFlags gfp, final int frame_id, final String s) {
		final InternalFlags gfc = gfp != null ? gfp.internal_flags : null;
		if( gfc != null ) {
			final int flags = gfc.tag_spec.flags;
			id3v2_add_lng( gfp, frame_id, null, s, false );
			gfc.tag_spec.flags = flags;
		}
	}

	private static final void id3v2AddLameVersion(final GlobalFlags gfp) {
		final String b = Version.get_lame_os_bitness();
		final String v = Version.get_lame_version();
		final String u = Version.get_lame_url();

		String buffer;
		if( b.length() > 0 ) {
			buffer = String.format("LAME %s version %s (%s)", b, v, u );
		} else {
			buffer = String.format("LAME version %s (%s)", v, u );
		}
		copyV1ToV2( gfp, ID_ENCODER, buffer );
	}

	private static final void id3v2AddAudioDuration(final GlobalFlags gfp, double ms) {
		final SessionConfig cfg = gfp.internal_flags.cfg; /* caller checked pointers */
			final double max_ulong = (double) Util.MAX_U_32_NUM;
			long playlength_ms;

			ms *= 1000.;
			ms /= (double)cfg.samplerate;
			if( ms > max_ulong ) {
				playlength_ms = (long)max_ulong;
			} else if( ms < 0 ) {
				playlength_ms = 0;
			} else {
				playlength_ms = (long)ms;
			}
			copyV1ToV2( gfp, ID_PLAYLENGTH, Long.toString( playlength_ms ) );
		}

	public static final void id3tag_genre_list(final ID3TagHandler h, final Object cookie) {
		if( h != null ) {
			for( int i = 0; i < genre_names.length; ++i ) {
				if( i < genre_alpha_map.length ) {
					final int j = genre_alpha_map[i];
					h.handle( j, genre_names[j], cookie );
				}
			}
		}
	}

	private static final int GENRE_NUM_UNKNOWN = 255;

	public static final void id3tag_init(final GlobalFlags gfp) {
		if( is_internal_flags_null( gfp ) ) {
			return;
		}
		final InternalFlags gfc = gfp.internal_flags;
		gfc.free_id3tag();
		gfc.tag_spec.clear();
		gfc.tag_spec.genre_id3v1 = GENRE_NUM_UNKNOWN;
		gfc.tag_spec.padding_size = 128;
		id3v2AddLameVersion( gfp );
	}

	public static final void id3tag_add_v2(final GlobalFlags gfp) {
		if( is_internal_flags_null( gfp ) ) {
			return;
		}
		final InternalFlags gfc = gfp.internal_flags;
		gfc.tag_spec.flags &= ~V1_ONLY_FLAG;
		gfc.tag_spec.flags |= ADD_V2_FLAG;
	}

	public static final void id3tag_v1_only(final GlobalFlags gfp) {
		if( is_internal_flags_null( gfp ) ) {
			return;
		}
		final InternalFlags gfc = gfp.internal_flags;
		gfc.tag_spec.flags &= ~(ADD_V2_FLAG | V2_ONLY_FLAG);
		gfc.tag_spec.flags |= V1_ONLY_FLAG;
	}

	public static final void id3tag_v2_only(final GlobalFlags gfp) {
		if( is_internal_flags_null( gfp ) ) {
			return;
		}
		final InternalFlags gfc = gfp.internal_flags;
		gfc.tag_spec.flags &= ~V1_ONLY_FLAG;
		gfc.tag_spec.flags |= V2_ONLY_FLAG;
	}

	public static final void id3tag_space_v1(final GlobalFlags gfp) {
		if( is_internal_flags_null( gfp ) ) {
			return;
		}
		final InternalFlags gfc = gfp.internal_flags;
		gfc.tag_spec.flags &= ~V2_ONLY_FLAG;
		gfc.tag_spec.flags |= SPACE_V1_FLAG;
	}

	public static final void id3tag_pad_v2(final GlobalFlags gfp) {
		id3tag_set_pad( gfp, 128 );
	}

	public static final void id3tag_set_pad(final GlobalFlags gfp, final int n) {
		if( is_internal_flags_null( gfp ) ) {
			return;
		}
		final InternalFlags gfc = gfp.internal_flags;
		gfc.tag_spec.flags &= ~V1_ONLY_FLAG;
		gfc.tag_spec.flags |= PAD_V2_FLAG;
		gfc.tag_spec.flags |= ADD_V2_FLAG;
		gfc.tag_spec.padding_size = n;
	}

	/* private static final boolean hasUcs2ByteOrderMarker(final char bom) {
		return ( bom == 0xFFFE || bom == 0xFEFF );
	} */

	private static final boolean maybeLatin1(final String text) {
		if( text != null ) {
			for( int i = 0, ie = text.length(); i < ie; i++ ) {
				if( text.charAt( i ) > 0x00fe ) {
					return false;
				}
			}
		}
		return true;
	}
/*
	private static final int sloppySearchGenre(String genre) {
		for( int i = 0; i < genre_names.length; ++i ) {
			if( sloppyCompared( genre, genre_names[i] ) ) {// TODO java: make sloppy search genre
				return i;
			}
		}
		return genre_names.length;
	}
*/
	private static final int searchGenre(final String genre) {
		for( int i = 0; i < genre_names.length; ++i ) {
			if( 0 == genre.compareToIgnoreCase( genre_names[i] ) ) {
				return i;
			}
		}
		return genre_names.length;
	}

	private static final int lookupGenre(final String genre) {
		/* is the input a string or a valid number? */
		try {
			final int num = Integer.parseInt( genre );
			if( (num < 0) || (num >= genre_names.length) ) {
				return -1; /* number unknown */
			}
			return num;
		} catch(final NumberFormatException ne) {
			final int num = searchGenre( genre );
			/*if( num == genre_names.length ) {
				num = sloppySearchGenre( genre );
			}*/
			if( num == genre_names.length ) {
				return -2; /* no common genre text found */
			}
			return num;
		}
	}

	public static final int id3tag_set_genre(final GlobalFlags gfp, final String text) {
		final InternalFlags gfc = gfp.internal_flags;
		int   ret;
		if( text == null ) {
			return -3;
		}
		/*if( !hasUcs2ByteOrderMarker(text[0]) ) {
			return -3;
		}*/
		if( maybeLatin1( text ) ) {
			final int num = lookupGenre( text );
			if( num == -1 ) {
				return -1;
			} /* number out of range */
			if( num >= 0 ) {           /* common genre found  */
				gfc.tag_spec.flags |= CHANGED_FLAG;
				gfc.tag_spec.genre_id3v1 = num;
				copyV1ToV2( gfp, ID_GENRE, genre_names[num] );
				return 0;
			}
		}
		ret = id3v2_add( gfp, ID_GENRE, null, null, text, true );
		if( ret == 0 ) {
			gfc.tag_spec.flags |= CHANGED_FLAG;
			gfc.tag_spec.genre_id3v1 = GENRE_INDEX_OTHER;
		}
		return ret;
	}

	/*
	Some existing options for ID3 tag can be specified by --tv option
	as follows.
	--tt <value>, --tv TIT2=value
	--ta <value>, --tv TPE1=value
	--tl <value>, --tv TALB=value
	--ty <value>, --tv TYER=value
	--tn <value>, --tv TRCK=value
	--tg <value>, --tv TCON=value
	(although some are not exactly same)*/

	public static final int id3tag_set_albumart(final GlobalFlags gfp, final byte[] image, final int size) {
		if (is_internal_flags_null(gfp)) {
			return 0;
		}
		final InternalFlags gfc = gfp.internal_flags;
		int     mimetype = MIMETYPE_NONE;

		if( image != null ) {
			/* determine MIME type from the actual image data */
			if( 2 < size && image[0] == 0xFF && image[1] == 0xD8 ) {
				mimetype = MIMETYPE_JPEG;
			} else if( 4 < size && image[0] == 0x89 && image[1] == 'P' && image[2] == 'N' && image[3] == 'G' ) {
				mimetype = MIMETYPE_PNG;
			} else if( 4 < size && image[0] == 'G' && image[1] == 'I' && image[2] == 'F' && image[3] == '8' ) {
				mimetype = MIMETYPE_GIF;
			} else {
				return -1;
			}
		}
		if( gfc.tag_spec.albumart != null ) {
			gfc.tag_spec.albumart = null;
			// gfc.tag_spec.albumart_size = 0;
			gfc.tag_spec.albumart_mimetype = MIMETYPE_NONE;
		}
		if( size < 1 || mimetype == MIMETYPE_NONE ) {
			return 0;
		}
		gfc.tag_spec.albumart = image;
		// gfc.tag_spec.albumart_size = size;
		gfc.tag_spec.albumart_mimetype = mimetype;
		gfc.tag_spec.flags |= CHANGED_FLAG;
		id3tag_add_v2( gfp );
		return 0;
	}

	private static final int set_4_byte_value(final byte[] bytes, final int offset, int value) {
		for( int i = 3 + offset; i >= offset; --i ) {
			bytes[i] = (byte)value;
			value >>= 8;
		}
		return offset + 4;
	}

	private static final int toID3v2TagId(final String s) {
		if( s.length() == 0 ) {
			return 0;
		}
		int x = 0;
		for( int i = 0, ie = (4 <= s.length() ? 4 : s.length()); i < ie; ++i ) {
			final char c = s.charAt( i );
			final int u = 0x0ff & c;
			x <<= 8;
			x |= u;
			if( c < 'A' || 'Z' < c ) {
				if( c < '0' || '9' < c ) {
					return 0;
				}
			}
		}
		return x;
	}

/*
	private static final uint32_t toID3v2TagId_ucs2(final unsigned short const *s) {
		unsigned int i, x = 0;
		unsigned short bom = 0;
		if( s == 0 ) {
			return 0;
		}
		bom = s[0];
		if( hasUcs2ByteOrderMarker(bom) ) {
			++s;
		}
		for( i = 0; i < 4 && s[i] != 0; ++i ) {
			unsigned short const c = toLittleEndian(bom, s[i]);
			if( c < 'A' || 'Z' < c ) {
			if( c < '0' || '9' < c ) {
			return 0;
			}
			}
			x <<= 8;
			x |= c;
		}
		return x;
	}
*/

	private static final boolean isMultiFrame(final int frame_id) {
		return ( frame_id == ID_TXXX || frame_id == ID_WXXX
				|| frame_id == ID_COMMENT || frame_id == ID_SYLT
				|| frame_id == ID_APIC || frame_id == ID_GEOB
				|| frame_id == ID_PCNT || frame_id == ID_AENC
				|| frame_id == ID_LINK || frame_id == ID_ENCR
				|| frame_id == ID_GRID || frame_id == ID_PRIV );
	}

	private static final FrameDataNode findNode(final ID3TagSpec tag, final int frame_id, final FrameDataNode last) {
		FrameDataNode node = last != null ? last.nxt : tag.v2_head;
		while( node != null ) {
			if( node.fid == frame_id ) {
				return node;
			}
			node = node.nxt;
		}
		return null;
	}

	private static final void appendNode(final ID3TagSpec tag, final FrameDataNode node) {
		if( tag.v2_tail == null || tag.v2_head == null ) {
			tag.v2_head = node;
			tag.v2_tail = node;
			return;
		}// else {
			tag.v2_tail.nxt = node;
			tag.v2_tail = node;
		//}
	}

	private static final String setLang(String src) {
		if( src == null || src.length() == 0 ) {
			return "eng";
		}
		for( int i = src.length(); i < 3; ++i ) {
			src += " ";
		}
		return src;
	}

	private static final boolean isSameLang(final String l1, String l2) {
		if( l2 == null || l2.length() == 0 ) {
			l2 = "eng";
		}
		return l1.compareToIgnoreCase( l2 ) == 0;
	}

	private static final boolean isSameDescriptor(final FrameDataNode node, final byte[] dsc, final boolean isUnicode) {
		if( node.enc != isUnicode || dsc == null ) {
			return false;
		}
		for( int i = 0; i < node.dsc.length; ++i ) {
			if( node.dsc[i] != dsc[i] ) {
				return false;
			}
		}
		return true;
	}

	private static final int id3v2_add(final GlobalFlags gfp, final int frame_id, final String lng, final String desc, final String text, final boolean isUnicode) {
		final InternalFlags gfc = gfp != null ? gfp.internal_flags : null;
		if( gfc != null ) {
			byte[] bdesc = null;
			if( desc != null ) {
				bdesc = Charset.forName("UTF-16LE").encode( desc ).put(0, (byte) 0xFF).put(1, (byte) 0xFE).array();
			}
			FrameDataNode node = findNode( gfc.tag_spec, frame_id, null );
			final String lang = setLang( lng );
			if( isMultiFrame( frame_id ) ) {
				while( node != null ) {
					if( isSameLang( node.lng, lang ) ) {
						if( isSameDescriptor( node, bdesc, isUnicode ) ) {
							break;
						}
					}
					node = findNode( gfc.tag_spec, frame_id, node );
				}
			}
			if( node == null ) {
				node = new FrameDataNode();
				appendNode( gfc.tag_spec, node );
			}
			node.fid = frame_id;
			node.lng = setLang( lang );
			node.enc = true;
			node.dsc = bdesc;
			node.txt = Charset.forName("UTF-16LE").encode( text ).put(0, (byte) 0xFF).put(1, (byte) 0xFE).array();
			gfc.tag_spec.flags |= (CHANGED_FLAG | ADD_V2_FLAG);
			return 0;
		}
		return -255;
	}

	private static final String id3v2_get_language(final GlobalFlags gfp)
	{
		final InternalFlags gfc = gfp != null ? gfp.internal_flags : null;
		if( gfc != null ) {
			return gfc.tag_spec.language;
		}
		return null;
	}

	private static final int id3v2_add_lng(final GlobalFlags gfp, final int frame_id, final String desc, final String text, final boolean isEncode)
	{
	    final String lang = id3v2_get_language( gfp );
	    return id3v2_add( gfp, frame_id, lang, desc, text, isEncode );
	}

	private static final int id3tag_set_userinfo(final GlobalFlags gfp, final int id, final String fieldvalue, final boolean isEncode) {
		int rc = -7;
		final int a = fieldvalue.indexOf('=');
		if( a >= 0 ) {
			rc = id3v2_add_lng( gfp, id, fieldvalue.substring( 0, a ), fieldvalue.substring( a + 1 ), isEncode );
		}
		return rc;
	}

	public static final int id3tag_set_textinfo(final GlobalFlags gfp, final String id, final String text, final boolean isEncode) {
		final int frame_id = toID3v2TagId( id );
		if( frame_id == 0 ) {
			return -1;
		}
		if( is_internal_flags_null( gfp ) ) {
			return 0;
		}
		if( text == null ) {
			return 0;
		}
		/* if( ! hasUcs2ByteOrderMarker( (char)((btext[0] & 0xff) | (btext[1] << 8)) ) ) {
			return -3;  // BOM missing
		}*/
		if( frame_id == ID_TXXX || frame_id == ID_WXXX || frame_id == ID_COMMENT ) {
			return id3tag_set_userinfo( gfp, frame_id, text, isEncode );
		}
		if( frame_id == ID_GENRE ) {
			return id3tag_set_genre( gfp, text );
		}
		if( frame_id == ID_PCST ) {
			return id3v2_add_lng( gfp, frame_id, null, text, isEncode );
		}
		if( frame_id == ID_USER ) {
			return id3v2_add_lng( gfp, frame_id, text, null, isEncode );
		}
		if( frame_id == ID_WFED ) {
			return id3v2_add_lng( gfp, frame_id, text, null, isEncode ); /* iTunes expects WFED to be a text frame */
		}
		if( isFrameIdMatching( frame_id, FRAME_ID( 'T', '\0', '\0', '\0' ) )
				|| isFrameIdMatching( frame_id, FRAME_ID( 'W', '\0', '\0', '\0' ) ) ) {
			return id3v2_add_lng( gfp, frame_id, null, text, isEncode );
		}
		return -255;        /* not supported by now */
	}

	public static final int id3tag_set_comment(final GlobalFlags gfp, final String lang, final String desc, final String text, final boolean isEncode) {
		if( is_internal_flags_null( gfp ) ) {
			return 0;
		}
		return id3v2_add( gfp, ID_COMMENT, lang, desc, text, isEncode );
	}

	public static final void id3tag_set_title(final GlobalFlags gfp, final String title) {
		final InternalFlags gfc = gfp != null ? gfp.internal_flags : null;
		if( gfc != null && title != null && title.length() != 0 ) {
			gfc.tag_spec.title = title.getBytes();// java: using default charset
			gfc.tag_spec.flags |= CHANGED_FLAG;
			copyV1ToV2( gfp, ID_TITLE, title );
		}
	}

	public static final void id3tag_set_artist(final GlobalFlags gfp, final String artist) {
		final InternalFlags gfc = gfp != null ? gfp.internal_flags : null;
		if( gfc != null && artist != null && artist.length() != 0 ) {
			gfc.tag_spec.artist = artist.getBytes();// java: using default charset
			gfc.tag_spec.flags |= CHANGED_FLAG;
			copyV1ToV2( gfp, ID_ARTIST, artist );
		}
	}

	public static final void id3tag_set_album(final GlobalFlags gfp, final String album) {
		final InternalFlags gfc = gfp != null ? gfp.internal_flags : null;
		if( gfc != null && album != null && album.length() != 0 ) {
			gfc.tag_spec.album = album.getBytes();// java: using default charset
			gfc.tag_spec.flags |= CHANGED_FLAG;
			copyV1ToV2( gfp, ID_ALBUM, album );
		}
	}

	public static final void id3tag_set_year(final GlobalFlags gfp, final String year) {
		final InternalFlags gfc = gfp != null ? gfp.internal_flags : null;
		if( gfc != null && year != null && year.length() > 0 ) {
			int num = Integer.parseInt( year );
			if( num < 0 ) {
				num = 0;
			}
			/* limit a year to 4 digits so it fits in a version 1 tag */
			if( num > 9999 ) {
				num = 9999;
			}
			if( num != 0 ) {
				gfc.tag_spec.year = num;
				gfc.tag_spec.flags |= CHANGED_FLAG;
			}
			copyV1ToV2( gfp, ID_YEAR, year );
		}
	}

	public static final void id3tag_set_comment(final GlobalFlags gfp, final String comment) {
		final InternalFlags gfc = gfp != null ? gfp.internal_flags : null;
		if( gfc != null && comment != null && comment.length() != 0 ) {
			gfc.tag_spec.comment = comment.getBytes();// java: using default charset
			gfc.tag_spec.flags |= CHANGED_FLAG;
			{
				final int flags = gfc.tag_spec.flags;
				id3v2_add_lng( gfp, ID_COMMENT, "", comment, false );
				gfc.tag_spec.flags = flags;
			}
		}
	}

	public static final int id3tag_set_track(final GlobalFlags gfp, final String track) {
		final InternalFlags gfc = gfp != null ? gfp.internal_flags : null;
		int ret = 0;

		if( gfc != null && track != null && track.length() != 0 ) {
			int num = 0;
			try { num = Integer.parseInt( track );
			} catch(final NumberFormatException ne) {
			}
			/* check for valid ID3v1 track number range */
			if( num < 1 || num > 255 ) {
				num = 0;
				ret = -1;   /* track number out of ID3v1 range, ignored for ID3v1 */
				gfc.tag_spec.flags |= (CHANGED_FLAG | ADD_V2_FLAG);
			}
			if( num != 0 ) {
				gfc.tag_spec.track_id3v1 = num;
				gfc.tag_spec.flags |= CHANGED_FLAG;
			}
			/* Look for the total track count after a "/", same restrictions */
			final int trackcount = track.indexOf('/');
			if( trackcount >= 0 ) {
				gfc.tag_spec.flags |= (CHANGED_FLAG | ADD_V2_FLAG);
			}
			copyV1ToV2( gfp, ID_TRACK, track );
		}
		return ret;
	}

/*
	private static final const char* nextUpperAlpha(const char* p, char x) {
		char c;
		for(c = toupper(*p); *p != 0; c = toupper(*++p) ) {
			if( 'A' <= c && c <= 'Z' ) {
				if( c != x ) {
					return p;
				}
			}
		}
		return p;
	}
*/
/*
	private static final int sloppyCompared(const char* p, const char* q) {
		char cp, cq;
		p = nextUpperAlpha(p, 0);
		q = nextUpperAlpha(q, 0);
		cp = toupper(*p);
		cq = toupper(*q);
		while (cp == cq ) {
			if( cp == 0 ) {
				return 1;
			}
			if( p[1] == '.' ) { // some abbrevation
				while (*q && *q++ != ' ' ) {
				}
			}
			p = nextUpperAlpha(p, cp);
			q = nextUpperAlpha(q, cq);
			cp = toupper(*p);
			cq = toupper(*q);
		}
		return 0;
	}
*/

	private static final int sizeOfNode(final FrameDataNode node) {
		int n = 0;
		if( node != null ) {
			n = 10;         /* header size */
			n += 1;         /* text encoding flag */
			if( node.dsc.length > 0 ) {
				n += node.dsc.length + 1;
				if( node.enc ) {
					n++;// if encoded, uses 2 bytes per symbol, separator = 2 bytes
				}
			}
			n += node.txt.length;
		}
		return n;
	}

	private static final int sizeOfCommentNode(final FrameDataNode node) {
		int n = 0;
		if( node != null ) {
			n = 10;         /* header size */
			n += 1;         /* text encoding flag */
			n += 3;         /* language */
			//if( node.dsc.length > 0 ) {// FIXME why not check for zero ?
				n += node.dsc.length + 1;// +1 : separator
				if( node.enc ) {
					n++;// if encoded, uses 2 bytes per symbol, separator = 2 bytes
				}
			//}
			n += node.txt.length;
		}
		return n;
	}

	private static final int sizeOfWxxxNode(final FrameDataNode node) {
		int n = 0;
		if( node != null ) {
			n = 10;         /* header size */
			if( node.dsc.length > 0 ) {
				n += 1;         /* text encoding flag */
				n += node.dsc.length + 1;// +1 : separator
				if( node.enc ) {
					n++;// if encoded, uses 2 bytes per symbol, separator = 2 bytes
				}
			}
			if( node.txt.length > 0 ) {
				n += node.txt.length;// java: we are writing with BOM
				//if( hasUcs2ByteOrderMarker( bom ) ) {
				//	n -= 2;
				//}
			}
			/* if( node.txt.dim > 0 ) {
				switch (node.txt.enc ) {
				default:
				case 0:
					n += node.txt.dim;
					break;
				case 1:
					n += node.txt.dim - 1; // UCS2 . Latin1, skip BOM // FIXME writeLoBytes uses if( hasUcs2ByteOrderMarker(bom) ), so existing BOM must be checked!
					break;
				}
			}*/
		}
		return n;
	}

	private static final int writeChars(final byte[] buffer, int frame, final byte[] str, final int n) {
		for( int i = 0; i < n; i++ ) {
			buffer[frame++] = str[i];
		}
		return frame;
	}

	private static final int set_frame_comment(final byte[] buffer, int frame, final FrameDataNode node) {
		final int n = sizeOfCommentNode( node );
		if( n > 10 ) {
			frame = set_4_byte_value( buffer, frame, node.fid );
			frame = set_4_byte_value( buffer, frame, (n - 10) );
			/* clear 2-byte header flags */
			buffer[frame++] = 0;
			buffer[frame++] = 0;
			/* encoding descriptor byte */
			buffer[frame++] = (byte)(node.enc ? 1 : 0);
			/* 3 bytes language */
			buffer[frame++] = (byte)node.lng.charAt( 0 );
			buffer[frame++] = (byte)node.lng.charAt( 1 );
			buffer[frame++] = (byte)node.lng.charAt( 2 );
			/* descriptor with zero byte(s) separator */
			frame = writeChars( buffer, frame, node.dsc, node.dsc.length );
			buffer[frame++]= 0;
			if( node.enc ) {
				buffer[frame++]= 0;
			}
			/* comment full text */
			frame = writeChars( buffer, frame, node.txt, node.txt.length );
		}
		return frame;
	}


	private static final int set_frame_custom2(final byte[] buffer, int frame, final FrameDataNode node) {
		final int n = sizeOfNode( node );
		if( n > 10 ) {
			frame = set_4_byte_value( buffer, frame, node.fid );
			frame = set_4_byte_value( buffer, frame, (n - 10) );
			/* clear 2-byte header flags */
			buffer[frame++] = 0;
			buffer[frame++] = 0;
			/* clear 1 encoding descriptor byte to indicate ISO-8859-1 format */
			buffer[frame++] = (byte)(node.enc ? 1 : 0);
			if( node.dsc.length > 0 ) {
				frame = writeChars( buffer, frame, node.dsc, node.dsc.length );
				buffer[frame++] = 0;
				if( node.enc ) {
					buffer[frame++] = 0;
				}
			}
			frame = writeChars( buffer, frame, node.txt, node.txt.length );
		}
		return frame;
	}

	private static final int set_frame_wxxx(final byte[] buffer, int frame, final FrameDataNode node) {
		final int n = sizeOfWxxxNode( node );
		if( n > 10 ) {
			frame = set_4_byte_value( buffer, frame, node.fid );
			frame = set_4_byte_value( buffer, frame, (n - 10) );
			/* clear 2-byte header flags */
			buffer[frame++] = 0;
			buffer[frame++] = 0;
			if( node.dsc.length > 0 ) {
				/* clear 1 encoding descriptor byte to indicate ISO-8859-1 format */
				buffer[frame++] = (byte)(node.enc ? 1 : 0);
				frame = writeChars( buffer, frame, node.dsc, node.dsc.length );
				buffer[frame++] = 0;
				if( node.enc ) {
					buffer[frame++] = 0;
				}
			}
			// java: we are writing with BOM
			frame = writeChars( buffer, frame, node.txt, node.txt.length );
		}
		return frame;
	}

	private static final int set_frame_apic(final byte[] buffer, int frame, final byte[] mimetype, final byte[] data)//, final int size)
	{
		/* ID3v2.3 standard APIC frame:
		 *     <Header for 'Attached picture', ID: "APIC">
		 *     Text encoding    $xx
		 *     MIME type        <text string> $00
		 *     Picture type     $xx
		 *     Description      <text string according to encoding> $00 (00)
		 *     Picture data     <binary data>
		 */
		if( mimetype != null && data != null && data.length != 0 ) {// size != 0 ) {
			frame = set_4_byte_value( buffer, frame, FRAME_ID('A', 'P', 'I', 'C') );
			frame = set_4_byte_value( buffer, frame, (4 + mimetype.length + data.length) );// size) );
			/* clear 2-byte header flags */
			buffer[frame++] = 0;
			buffer[frame++] = 0;
			/* clear 1 encoding descriptor byte to indicate ISO-8859-1 format */
			buffer[frame++] = 0;
			/* copy mime_type */
			for( int i = 0; i < mimetype.length; i++ ) {
				buffer[frame++] = mimetype[i];
			}
			buffer[frame++] = 0;
			/* set picture type to 0 */
			buffer[frame++] = 0;
			/* empty description field */
			buffer[frame++] = 0;
			/* copy the image data */
			for( int i = 0, size = data.length; i < size; i++ ) {
				buffer[frame++] = data[i];
			}
		}
		return frame;
	}

	public static final int id3tag_set_fieldvalue(final GlobalFlags gfp, final String fieldvalue, final boolean isEncode) {
		if( is_internal_flags_null( gfp ) ) {
			return 0;
		}
		if( fieldvalue != null && fieldvalue.length() != 0 ) {
			// FIXME dirty code...
			if( fieldvalue.length() < 5 || fieldvalue.charAt( 4 ) != '=' ) {
				return -1;
			}
			final int frame_id = toID3v2TagId( fieldvalue );
			if( frame_id != 0 ) {
				//fid[0] = (frame_id >> 24) & 0x0ff;
				//fid[1] = (frame_id >> 16) & 0x0ff;
				//fid[2] = (frame_id >> 8) & 0x0ff;
				//fid[3] = frame_id & 0x0ff;
				final int rc = id3tag_set_textinfo( gfp, fieldvalue.substring( 0, 4 ), fieldvalue.substring( 5 ), isEncode );
				return rc;
			}
		}
		return -1;
	}

	private static final byte[] mime_jpeg = "image/jpeg".getBytes();
	private static final byte[] mime_png = "image/png".getBytes();
	private static final byte[] mime_gif = "image/gif".getBytes();

	public static final int lame_get_id3v2_tag(final GlobalFlags gfp, final byte[] buffer, final int size) {
		if( is_internal_flags_null( gfp ) ) {
			return 0;
		}
		final InternalFlags gfc = gfp.internal_flags;
		if( test_tag_spec_flags( gfc, V1_ONLY_FLAG ) ) {
			return 0;
		}
		{
			boolean usev2 = test_tag_spec_flags( gfc, ADD_V2_FLAG | V2_ONLY_FLAG );
			/* calculate length of four fields which may not fit in verion 1 tag */
			final int title_length = gfc.tag_spec.title != null ? gfc.tag_spec.title.length : 0;
			final int artist_length = gfc.tag_spec.artist != null ? gfc.tag_spec.artist.length : 0;
			final int album_length = gfc.tag_spec.album != null ? gfc.tag_spec.album.length : 0;
			final int comment_length = gfc.tag_spec.comment != null ? gfc.tag_spec.comment.length : 0;
			/* write tag if explicitly requested or if fields overflow */
			if( (title_length > 30)
				|| (artist_length > 30)
				|| (album_length > 30)
				|| (comment_length > 30)
				|| (gfc.tag_spec.track_id3v1 != 0 && (comment_length > 28)) ) {
				usev2 = true;
			}
			if( usev2 ) {
				if( gfp.num_samples != Util.MAX_U_32_NUM ) {
					id3v2AddAudioDuration( gfp, gfp.num_samples );
				}

				/* calulate size of tag starting with 10-byte tag header */
				int tag_size = 10;
				byte[] albumart_mime = null;
				if( gfc.tag_spec.albumart != null ) {// && gfc.tag_spec.albumart_size != 0 ) {
					switch( gfc.tag_spec.albumart_mimetype ) {
					case MIMETYPE_JPEG:
						albumart_mime = mime_jpeg;
						break;
					case MIMETYPE_PNG:
						albumart_mime = mime_png;
						break;
					case MIMETYPE_GIF:
						albumart_mime = mime_gif;
						break;
					}
					if( albumart_mime != null ) {
						tag_size += 10 + 4 + albumart_mime.length + gfc.tag_spec.albumart.length;// gfc.tag_spec.albumart_size;
					}
				}
				{
					final ID3TagSpec tag = gfc.tag_spec;
					if( tag.v2_head != null ) {
						for(FrameDataNode node = tag.v2_head; node != null; node = node.nxt ) {
							if( node.fid == ID_COMMENT || node.fid == ID_USER ) {
								tag_size += sizeOfCommentNode( node );
							} else if( isFrameIdMatching( node.fid, FRAME_ID('W','\0','\0','\0') ) ) {
								tag_size += sizeOfWxxxNode( node );
							} else {
								tag_size += sizeOfNode( node );
							}
						}
					}
				}
				if( test_tag_spec_flags( gfc, PAD_V2_FLAG ) ) {
					/* add some bytes of padding */
					tag_size += gfc.tag_spec.padding_size;
				}
				if( size < tag_size ) {
					return tag_size;
				}
				if( buffer == null ) {
					return 0;
				}
				int p = 0;// buffer[p]
				/* set tag header starting with file identifier */
				buffer[p++] = 'I';
				buffer[p++] = 'D';
				buffer[p++] = '3';
				/* set version number word */
				buffer[p++] = 3;
				buffer[p++] = 0;
				/* clear flags byte */
				buffer[p++] = 0;
				/* calculate and set tag size = total size - header size */
				final int adjusted_tag_size = tag_size - 10;
				/* encode adjusted size into four bytes where most significant
				 * bit is clear in each byte, for 28-bit total */
				buffer[p++] = (byte) ((adjusted_tag_size >> 21) & 0x7f);
				buffer[p++] = (byte) ((adjusted_tag_size >> 14) & 0x7f);
				buffer[p++] = (byte) ((adjusted_tag_size >> 7) & 0x7f);
				buffer[p++] = (byte) (adjusted_tag_size & 0x7f);

				/*
				 * NOTE: The remainder of the tag (frames and padding, if any)
				 * are not "unsynchronized" to prevent false MPEG audio headers
				 * from appearing in the bitstream.  Why?  Well, most players
				 * and utilities know how to skip the ID3 version 2 tag by now
				 * even if they don't read its contents, and it's actually
				 * very unlikely that such a false "sync" pattern would occur
				 * in just the simple text frames added here.
				 */

				/* set each frame in tag */
				{
					final ID3TagSpec tag = gfc.tag_spec;
					if( tag.v2_head != null ) {
						for(FrameDataNode node = tag.v2_head; node != null; node = node.nxt ) {
							if( node.fid == ID_COMMENT || node.fid == ID_USER ) {
								p = set_frame_comment( buffer, p, node );
							} else if( isFrameIdMatching( node.fid, FRAME_ID('W','\0','\0','\0') ) ) {
								p = set_frame_wxxx( buffer, p, node );
							} else {
								p = set_frame_custom2( buffer, p, node );
							}
						}
					}
				}
				if( albumart_mime != null ) {
					p = set_frame_apic( buffer, p, albumart_mime, gfc.tag_spec.albumart );// gfc.tag_spec.albumart_size );
				}
				/* clear any padding bytes */
				while( p < tag_size ) {
					buffer[p++] = 0;
				}
				return tag_size;
			}
		}
		return 0;
	}

	static final int id3tag_write_v2(final GlobalFlags gfp) {
		if( is_internal_flags_null( gfp ) ) {
			return 0;
		}
		final InternalFlags gfc = gfp.internal_flags;
		if( test_tag_spec_flags( gfc, V1_ONLY_FLAG ) ) {
			return 0;
		}
		if( test_tag_spec_flags( gfc, CHANGED_FLAG ) ) {
			final int n = lame_get_id3v2_tag( gfp, null, 0 );
			final byte[] tag = new byte[ n ];
			final int tag_size = lame_get_id3v2_tag( gfp, tag, n );
			if( tag_size > n ) {
				return -1;
			} else {
				/* write tag directly into bitstream at current position */
				for( int i = 0; i < tag_size; ++i ) {
					Bitstream.add_dummy_byte( gfc, tag[i], 1 );
				}
			}
			return tag_size; /* ok, tag should not exceed 2GB */
		}
		return 0;
	}

	/**
	 *
	 * @return java: return new offset
	 */
	private static final int set_text_field(final byte[] field, int foffset, final byte[] text, int size, final int pad) {
		int toffset = 0;
		final int length = text.length;
		while( size-- != 0 ) {
			if( text != null && toffset < length ) {
				field[foffset++] = text[ toffset++ ];
			} else {
				field[foffset++] = (byte)pad;
			}
		}
		return foffset;
	}

	public static final int lame_get_id3v1_tag(final GlobalFlags gfp, final byte[] buffer, final int size) {
		if( gfp == null ) {
			return 0;
		}
		final int tag_size = 128;
		if( size < tag_size ) {
			return tag_size;// FIXME why return tag_size?
		}
		final InternalFlags gfc = gfp.internal_flags;
		if( gfc == null ) {
			return 0;
		}
		if( buffer == null ) {
			return 0;
		}
		if( test_tag_spec_flags( gfc, V2_ONLY_FLAG ) ) {
			return 0;
		}
		if( test_tag_spec_flags( gfc, CHANGED_FLAG ) ) {
			int p = 0;// buffer[p]
			final int  pad = test_tag_spec_flags( gfc, SPACE_V1_FLAG ) ? ' ' : 0;

			/* set tag identifier */
			buffer[p++] = 'T';
			buffer[p++] = 'A';
			buffer[p++] = 'G';
			/* set each field in tag */
			p = set_text_field( buffer, p, gfc.tag_spec.title, 30, pad );
			p = set_text_field( buffer, p, gfc.tag_spec.artist, 30, pad );
			p = set_text_field( buffer, p, gfc.tag_spec.album, 30, pad );
			p = set_text_field( buffer, p, gfc.tag_spec.year != 0 ? Integer.toString( gfc.tag_spec.year ).getBytes() : null, 4, pad );
			/* limit comment field to 28 bytes if a track is specified */
			p = set_text_field( buffer, p, gfc.tag_spec.comment, gfc.tag_spec.track_id3v1 != 0 ? 28 : 30, pad );
			if( gfc.tag_spec.track_id3v1 != 0 ) {
				/* clear the next byte to indicate a version 1.1 tag */
				buffer[p++] = 0;
				buffer[p++] = (byte)gfc.tag_spec.track_id3v1;
			}
			buffer[p++] = (byte)gfc.tag_spec.genre_id3v1;
			return tag_size;
		}
		return 0;
	}

	static final int id3tag_write_v1(final GlobalFlags gfp) {
		if( is_internal_flags_null( gfp ) ) {
			return 0;
		}
		final InternalFlags gfc = gfp.internal_flags;
		final byte tag[] = new byte[128];

		final int n = lame_get_id3v1_tag( gfp, tag, 128 /*tag.length*/);
		if( n > 128 /*tag.length*/) {
			return 0;
		}
		/* write tag directly into bitstream at current position */
		for( int i = 0; i < n; ++i ) {
			Bitstream.add_dummy_byte( gfc, tag[i], 1 );
		}
		return n;     /* ok, tag has fixed size of 128 bytes, well below 2GB */
	}
}