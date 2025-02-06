package io.github.jseproject;

import javazoom.jl.decoder.Header;
import org.tritonus.share.TDebug;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Mp3AudioFileFormat extends ExtendedAudioFileFormat {

    private static final AudioFileFormat.Type[] TYPES = new AudioFileFormat.Type[] {
            Mp3FileFormatType.MP1, Mp3FileFormatType.MP2, Mp3FileFormatType.MP3
    };

    public static Mp3AudioFileFormat of(Header header, int byteLength, int mediaLength, byte[] id3v2, byte[] id3v1) throws UnsupportedAudioFileException {
        Map<String, Object> properties = new HashMap<>();
        properties.put("mp3.version", header.version_string());
        // version = 0 => MPEG2-LSF (Including MPEG2.5), version = 1 => MPEG1
        int version = header.version();
        if (version == 2) properties.put("mp3.version.mpeg", "2.5");
        else properties.put("mp3.version.mpeg", Integer.toString(2 - version));
        // layer = 1,2,3
        properties.put("mp3.version.layer", Integer.toString(header.layer()));
        int mode = header.mode();
        properties.put("mp3.mode", mode);
        properties.put("mp3.mode.extension", header.mode_extension());
        properties.put("mp3.channels", mode == 3 ? 1 : 2);
        boolean vbr = header.vbr();
        properties.put("mp3.vbr", vbr);
        properties.put("mp3.vbr.scale", header.vbr_scale());
        byte[] vbr_toc = header.vbr_toc();
        if (vbr_toc != null) properties.put("mp3.vbr.toc", vbr_toc.clone());
        int frameSize = header.calculate_framesize();
        if (frameSize < 0) throw new UnsupportedAudioFileException("Invalid frame size: " + frameSize);
        if (!vbr) properties.put("mp3.frame.size.bytes", frameSize);
        properties.put("mp3.frequency.hz", header.frequency());
        float frameRate = (float) (1.0 / header.ms_per_frame() * 1000.0);
        if (frameRate < 0) throw new UnsupportedAudioFileException("Invalid frame rate: " + frameRate);
        int totalFrames;
        if (mediaLength == AudioSystem.NOT_SPECIFIED) totalFrames = AudioSystem.NOT_SPECIFIED;
        else {
            totalFrames = header.max_number_of_frames(mediaLength);
            properties.put("duration", Math.round(header.total_ms(mediaLength)) * 1000L);
            properties.put("mp3.duration.milliseconds", Math.round(header.total_ms(mediaLength)));
            properties.put("mp3.duration.frames", totalFrames);
        }
        properties.put("mp3.bitrate", header.bitrate());
        properties.put("mp3.version.string", header.version_string() + " Layer " + header.layer_string());
        properties.put("mp3.copyright", header.copyright());
        properties.put("mp3.original", header.original());
        properties.put("mp3.crc", header.checksums());
        properties.put("mp3.padding", header.padding());
        if (id3v2 != null) parseID3v2(id3v2, properties);
        if (id3v1 != null && id3v1[0] == 'T' && id3v1[1] == 'A' && id3v1[2] == 'G') parseID3v1(id3v1, properties);
        return new Mp3AudioFileFormat(TYPES[header.layer() - 1], Mp3AudioFormat.of(header),
                totalFrames,
                byteLength,
                properties);
    }

    public Mp3AudioFileFormat(Type type, AudioFormat format, long frameLength, long byteLength, Map<String, Object> properties) {
        super(type, format, frameLength, byteLength, properties);
    }

    private static void parseID3v2(byte[] id3v2, Map<String, Object> properties) {
        if (TDebug.TraceAudioFileReader) TDebug.out("Parsing ID3v2");
        int size = id3v2.length;
        if (!"ID3".equals(new String(id3v2, 0, 3))) {
            if (TDebug.TraceAudioFileReader) TDebug.out("No ID3v2 header found");
            return;
        }
        int version = id3v2[3] & 0xFF;
        if (version < 2 || version > 4) {
            if (TDebug.TraceAudioFileReader) TDebug.out("Unsupported ID3v2 version: " + version);
            return;
        }
        properties.put("mp3.id3tag.v2.version", Integer.toString(version));
        try {
            if (TDebug.TraceAudioFileReader) {
                String tag;
                try {
                    tag = new String(id3v2, "ISO-8859-1");
                }
                catch (UnsupportedEncodingException e) {
                    tag = new String(id3v2);
                    TDebug.out("Cannot use ISO-8859-1");
                }
                TDebug.out("ID3v2 frame dump='" + tag + "'");
            }
            /* ID3 tags : http://www.unixgods.org/~tilo/ID3/docs/ID3_comparison.html */
            String value;
            for (int i = 10; i < id3v2.length && id3v2[i] > 0; i += size) {
                if (version == 3 || version == 4) {
                    // ID3v2.3 & ID3v2.4
                    String code = new String(id3v2, i, 4);
                    size = ((id3v2[i + 4] << 24) & 0xFF000000) | ((id3v2[i + 5] << 16) & 0x00FF0000)
                            | ((id3v2[i + 6] << 8) & 0x0000FF00) | ((id3v2[i + 7]) & 0x000000FF);
                    i += 10;
                    if (code.equals("TALB") || code.equals("TIT2") || code.equals("TYER")
                            || code.equals("TPE1") || code.equals("TCOP") || code.equals("COMM")
                            || code.equals("TCON") || code.equals("TRCK") || code.equals("TPOS")
                            || code.equals("TDRC") || code.equals("TCOM") || code.equals("TIT1")
                            || code.equals("TENC") || code.equals("TPUB") || code.equals("TPE2")
                            || code.equals("TLEN")) {
                        if (code.equals("COMM")) value = parseText(id3v2, i, size, 5);
                        else value = parseText(id3v2, i, size, 1);
                        if (value != null && !value.isEmpty()) {
                            if (code.equals("TALB")) properties.put("album", value);
                            else if (code.equals("TIT2")) properties.put("title", value);
                            else if (code.equals("TYER")) properties.put("date", parseDate(value));
                            // ID3v2.4 date fix.
                            else if (code.equals("TDRC")) properties.put("date", parseDate(value));
                            else if (code.equals("TPE1")) properties.put("author", value);
                            else if (code.equals("TCOP")) properties.put("copyright", value);
                            else if (code.equals("COMM")) properties.put("comment", value);
                            else if (code.equals("TCON")) properties.put("mp3.id3tag.genre", value);
                            else if (code.equals("TRCK")) properties.put("mp3.id3tag.track", value);
                            else if (code.equals("TPOS")) properties.put("mp3.id3tag.disc", value);
                            else if (code.equals("TCOM")) properties.put("mp3.id3tag.composer", value);
                            else if (code.equals("TIT1")) properties.put("mp3.id3tag.grouping", value);
                            else if (code.equals("TENC")) properties.put("mp3.id3tag.encoded", value);
                            else if (code.equals("TPUB")) properties.put("mp3.id3tag.publisher", value);
                            else if (code.equals("TPE2")) properties.put("mp3.id3tag.orchestra", value);
                            else if (code.equals("TLEN")) properties.put("mp3.id3tag.length", value);
                        }
                    }
                }
                else {
                    // ID3v2.2
                    String code = new String(id3v2, i, 3);
                    size = (id3v2[i + 3] << 16) + (id3v2[i + 4] << 8) + (id3v2[i + 5]);
                    i += 6;
                    if (code.equals("TAL") || code.equals("TT2") || code.equals("TP1")
                            || code.equals("TYE") || code.equals("TRK") || code.equals("TPA")
                            || code.equals("TCR") || code.equals("TCO") || code.equals("TCM")
                            || code.equals("COM") || code.equals("TT1") || code.equals("TEN")
                            || code.equals("TPB") || code.equals("TP2") || code.equals("TLE")) {
                        if (code.equals("COM")) value = parseText(id3v2, i, size, 5);
                        else value = parseText(id3v2, i, size, 1);
                        if (value != null && !value.isEmpty()) {
                            if (code.equals("TAL")) properties.put("album", value);
                            else if (code.equals("TT2")) properties.put("title", value);
                            else if (code.equals("TYE")) properties.put("date", parseDate(value));
                            else if (code.equals("TP1")) properties.put("author", value);
                            else if (code.equals("TCR")) properties.put("copyright", value);
                            else if (code.equals("COM")) properties.put("comment", value);
                            else if (code.equals("TCO")) properties.put("mp3.id3tag.genre", value);
                            else if (code.equals("TRK")) properties.put("mp3.id3tag.track", value);
                            else if (code.equals("TPA")) properties.put("mp3.id3tag.disc", value);
                            else if (code.equals("TCM")) properties.put("mp3.id3tag.composer", value);
                            else if (code.equals("TT1")) properties.put("mp3.id3tag.grouping", value);
                            else if (code.equals("TEN")) properties.put("mp3.id3tag.encoded", value);
                            else if (code.equals("TPB")) properties.put("mp3.id3tag.publisher", value);
                            else if (code.equals("TP2")) properties.put("mp3.id3tag.orchestra", value);
                            else if (code.equals("TLE")) properties.put("mp3.id3tag.length", value);
                        }
                    }
                }
            }
        }
        catch (RuntimeException e) {
            // Ignore all parsing errors.
            if (TDebug.TraceAudioFileReader) TDebug.out("Cannot parse ID3v2: " + e.getMessage());
        }
        if (TDebug.TraceAudioFileReader) TDebug.out("ID3v2 parsed");
    }

    private static final SimpleDateFormat YYYY_MM_DD_TIME_TZ = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
    private static final SimpleDateFormat YYYY_MM_TIME_TZ = new SimpleDateFormat("yyyy-MM'T'HH:mm:ssXXX");
    private static final SimpleDateFormat YYYY_TIME_TZ = new SimpleDateFormat("yyyy'T'HH:mm:ssXXX");
    private static final SimpleDateFormat YYYY_MM_DD_TIME = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private static final SimpleDateFormat YYYY_MM_TIME = new SimpleDateFormat("yyyy-MM'T'HH:mm:ss");
    private static final SimpleDateFormat YYYY_TIME = new SimpleDateFormat("yyyy'T'HH:mm:ss");
    private static final SimpleDateFormat YYYY_MM_DD = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat YYYY_MM = new SimpleDateFormat("yyyy-MM");
    private static final SimpleDateFormat YYYY = new SimpleDateFormat("yyyy");
    private static final SimpleDateFormat[] SIMPLE_DATE_FORMATS = new SimpleDateFormat[] {
            YYYY_MM_DD_TIME_TZ, YYYY_MM_TIME_TZ, YYYY_TIME_TZ,
            YYYY_MM_DD_TIME, YYYY_MM_TIME, YYYY_TIME,
            YYYY_MM_DD, YYYY_MM, YYYY,
    };
    private static Date parseDate(String dateString) {
        if (dateString == null) return null;
        else {
            if (dateString.endsWith("Z")) dateString = dateString.substring(0, dateString.length() - 1) + "+00:00";
            for (SimpleDateFormat format : SIMPLE_DATE_FORMATS) {
                try {
                    return format.parse(dateString);
                }
                catch (ParseException ignored) {
                }
            }
            return null;
        }
    }

    private static final String[] ID3V1_GENRE = new String[] {
            "Blues"
            , "Classic Rock"
            , "Country"
            , "Dance"
            , "Disco"
            , "Funk"
            , "Grunge"
            , "Hip-Hop"
            , "Jazz"
            , "Metal"
            , "New Age"
            , "Oldies"
            , "Other"
            , "Pop"
            , "R&B"
            , "Rap"
            , "Reggae"
            , "Rock"
            , "Techno"
            , "Industrial"
            , "Alternative"
            , "Ska"
            , "Death Metal"
            , "Pranks"
            , "Soundtrack"
            , "Euro-Techno"
            , "Ambient"
            , "Trip-Hop"
            , "Vocal"
            , "Jazz+Funk"
            , "Fusion"
            , "Trance"
            , "Classical"
            , "Instrumental"
            , "Acid"
            , "House"
            , "Game"
            , "Sound Clip"
            , "Gospel"
            , "Noise"
            , "AlternRock"
            , "Bass"
            , "Soul"
            , "Punk"
            , "Space"
            , "Meditative"
            , "Instrumental Pop"
            , "Instrumental Rock"
            , "Ethnic"
            , "Gothic"
            , "Darkwave"
            , "Techno-Industrial"
            , "Electronic"
            , "Pop-Folk"
            , "Eurodance"
            , "Dream"
            , "Southern Rock"
            , "Comedy"
            , "Cult"
            , "Gangsta"
            , "Top 40"
            , "Christian Rap"
            , "Pop/Funk"
            , "Jungle"
            , "Native American"
            , "Cabaret"
            , "New Wave"
            , "Psychadelic"
            , "Rave"
            , "Showtunes"
            , "Trailer"
            , "Lo-Fi"
            , "Tribal"
            , "Acid Punk"
            , "Acid Jazz"
            , "Polka"
            , "Retro"
            , "Musical"
            , "Rock & Roll"
            , "Hard Rock"
            , "Folk"
            , "Folk-Rock"
            , "National Folk"
            , "Swing"
            , "Fast Fusion"
            , "Bebob"
            , "Latin"
            , "Revival"
            , "Celtic"
            , "Bluegrass"
            , "Avantgarde"
            , "Gothic Rock"
            , "Progressive Rock"
            , "Psychedelic Rock"
            , "Symphonic Rock"
            , "Slow Rock"
            , "Big Band"
            , "Chorus"
            , "Easy Listening"
            , "Acoustic"
            , "Humour"
            , "Speech"
            , "Chanson"
            , "Opera"
            , "Chamber Music"
            , "Sonata"
            , "Symphony"
            , "Booty Brass"
            , "Primus"
            , "Porn Groove"
            , "Satire"
            , "Slow Jam"
            , "Club"
            , "Tango"
            , "Samba"
            , "Folklore"
            , "Ballad"
            , "Power Ballad"
            , "Rhythmic Soul"
            , "Freestyle"
            , "Duet"
            , "Punk Rock"
            , "Drum Solo"
            , "A Capela"
            , "Euro-House"
            , "Dance Hall"
            , "Goa"
            , "Drum & Bass"
            , "Club-House"
            , "Hardcore"
            , "Terror"
            , "Indie"
            , "BritPop"
            , "Negerpunk"
            , "Polsk Punk"
            , "Beat"
            , "Christian Gangsta Rap"
            , "Heavy Metal"
            , "Black Metal"
            , "Crossover"
            , "Contemporary Christian"
            , "Christian Rock"
            , "Merengue"
            , "Salsa"
            , "Thrash Metal"
            , "Anime"
            , "JPop"
            , "SynthPop"
    };

    private static void parseID3v1(byte[] id3v1, Map<String, Object> properties) {
        if (TDebug.TraceAudioFileReader) TDebug.out("Parsing ID3v1");
        String tag;
        try {
            tag = new String(id3v1, "ISO-8859-1");
        }
        catch (UnsupportedEncodingException e) {
            tag = new String(id3v1);
            if (TDebug.TraceAudioFileReader) TDebug.out("Cannot use ISO-8859-1");
        }
        if (TDebug.TraceAudioFileReader) TDebug.out("ID3v1 frame dump='" + tag + "'");
        int start = 3;
        String titlev1 = chopSubstring(tag, start, start += 30);
        String titlev2 = (String) properties.get("title");
        if ((titlev2 == null || titlev2.isEmpty()) && titlev1 != null) properties.put("title", titlev1);
        String artistv1 = chopSubstring(tag, start, start += 30);
        String artistv2 = (String) properties.get("author");
        if ((artistv2 == null || artistv2.isEmpty()) && artistv1 != null) properties.put("author", artistv1);
        String albumv1 = chopSubstring(tag, start, start += 30);
        String albumv2 = (String) properties.get("album");
        if ((albumv2 == null || albumv2.isEmpty()) && albumv1 != null) properties.put("album", albumv1);
        String yearv1 = chopSubstring(tag, start, start += 4);
        String yearv2 = (String) properties.get("date");
        if ((yearv2 == null || yearv2.isEmpty()) && yearv1 != null) properties.put("date", yearv1);
        String commentv1 = chopSubstring(tag, start, start + 28);
        String commentv2 = (String) properties.get("comment");
        if ((commentv2 == null || commentv2.isEmpty()) && commentv1 != null) properties.put("comment", commentv1);
        String trackv1 = Integer.toString(id3v1[126] & 0xFF);
        String trackv2 = (String) properties.get("mp3.id3tag.track");
        if (trackv2 == null || trackv2.isEmpty()) properties.put("mp3.id3tag.track", trackv1);
        int genrev1 = id3v1[127] & 0xFF;
        if (genrev1 >= 0 && genrev1 < ID3V1_GENRE.length) {
            String genrev2 = (String) properties.get("mp3.id3tag.genre");
            if ((genrev2 == null || genrev2.isEmpty())) properties.put("mp3.id3tag.genre", ID3V1_GENRE[genrev1]);
        }
        if (TDebug.TraceAudioFileReader) TDebug.out("ID3v1 parsed");
    }

    private static final String[] STRING_ENCODERS = new String[] { "ISO-8859-1", "UTF16", "UTF-16BE", "UTF-8" };
    private static String parseText(byte[] frames, int offset, int size, int skip) {
        String value = null;
        try {
            value = new String(frames, offset + skip, size - skip, STRING_ENCODERS[frames[offset]]);
            value = chopSubstring(value, 0, value.length());
        }
        catch (UnsupportedEncodingException e) {
            if (TDebug.TraceAudioFileReader) TDebug.out("ID3v2 encoding error: " + e.getMessage());
        }
        return value;
    }

    private static String chopSubstring(String string, int start, int end) {
        String str = null;
        // 11/28/04 - String encoding bug fix.
        try {
            str = string.substring(start, end);
            int i = str.indexOf('\0');
            if (i != -1) str = str.substring(0, i);
        }
        catch (StringIndexOutOfBoundsException e) {
            // Skip encoding issues.
            if (TDebug.TraceAudioFileReader) TDebug.out("Cannot chop substring: " + e.getMessage());
        }
        return str;
    }

}
