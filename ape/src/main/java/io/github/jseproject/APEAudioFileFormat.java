package io.github.jseproject;

import davaguine.jmac.decoder.IAPEDecompress;
import davaguine.jmac.info.APETag;
import davaguine.jmac.info.APETagField;
import davaguine.jmac.info.WaveFormat;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class APEAudioFileFormat extends ExtendedAudioFileFormat {

    public static APEAudioFileFormat of(IAPEDecompress decoder) throws IOException {
        Map<String, Object> properties = new HashMap<>();
        properties.put("duration", decoder.getApeInfoLengthMs() * 1000L);
        properties.put("ape.duration.milliseconds", decoder.getApeInfoLengthMs());
        properties.put("ape.duration.frames", decoder.getApeInfoTotalFrames());
        properties.put("ape.duration.blocks", decoder.getApeInfoTotalBlocks());
        properties.put("ape.version", decoder.getApeInfoFileVersion());
        properties.put("ape.bitrate", decoder.getApeInfoAverageBitrate());
        properties.put("ape.compression.level", decoder.getApeInfoCompressionLevel());
        properties.put("ape.peak.level", decoder.getApeInfoPeakLevel());
        properties.put("ape.format.flags", decoder.getApeInfoFormatFlags());
        properties.put("ape.file.bytes", decoder.getApeInfoApeTotalBytes());
        properties.put("ape.frame.blocks", decoder.getApeInfoBlocksPerFrame());
        properties.put("ape.frame.final.blocks", decoder.getApeInfoFinalFrameBlocks());
        properties.put("ape.channels", decoder.getApeInfoChannels());
        properties.put("ape.frequency.hz", decoder.getApeInfoSampleRate());
        //properties.put("ape.block.size.bits", decoder.getApeInfoBitsPerSample());
        properties.put("ape.block.size.bytes", decoder.getApeInfoBytesPerSample());
        properties.put("ape.block.align", decoder.getApeInfoBlockAlign());
        properties.put("ape.wav.header.bytes", decoder.getApeInfoWavHeaderBytes());
        properties.put("ape.wav.duration.bytes", decoder.getApeInfoWavDataBytes());
        properties.put("ape.wav.terminating.bytes", decoder.getApeInfoWavTerminatingBytes());
        properties.put("ape.wav.file.bytes", decoder.getApeInfoWavTotalBytes());
        properties.put("ape.wav.bitrate", decoder.getApeInfoDecompressedBitrate());
        WaveFormat waveFormat = decoder.getApeInfoWaveFormatEx();
        properties.put("ape.wav.channels", (int) waveFormat.nChannels);
        properties.put("ape.wav.format.type", (int) waveFormat.wFormatTag);
        properties.put("ape.wav.block.align", (int) waveFormat.nBlockAlign);
        properties.put("ape.wav.block.size.bytes", (int) waveFormat.wBitsPerSample / 8);
        //properties.put("ape.wav.block.size.bits", (int) waveFormat.wBitsPerSample);
        properties.put("ape.wav.frequency.hz", waveFormat.nSamplesPerSec);
        if (decoder.getApeInfoIoSource().isLocal()) {
            APETag tag = decoder.getApeInfoTag();
            properties.put("ape.tag.version", tag.GetAPETagVersion());
            properties.put("author", tag.GetFieldString(APETag.APE_TAG_FIELD_ARTIST));
            properties.put("title", tag.GetFieldString(APETag.APE_TAG_FIELD_TITLE));
            properties.put("copyright", tag.GetFieldString(APETag.APE_TAG_FIELD_COPYRIGHT));
            properties.put("date", parseDate(tag.GetFieldString(APETag.APE_TAG_FIELD_YEAR)));
            properties.put("comment", tag.GetFieldString(APETag.APE_TAG_FIELD_COMMENT));
            properties.put("album", tag.GetFieldString(APETag.APE_TAG_FIELD_ALBUM));
            properties.put("track", tag.GetFieldString(APETag.APE_TAG_FIELD_TRACK));
            properties.put("genre", tag.GetFieldString(APETag.APE_TAG_FIELD_GENRE));
            for (int i = 0; i < tag.numFields(); i ++) {
                APETagField field = tag.GetTagField(i);
                Object value;
                if (field != null) {
                    byte[] b = field.GetFieldValue();
                    int boundary = 0;
                    int index = b.length - 1;
                    while (index >= 0 && b[index] == 0) {
                        index --;
                        boundary --;
                    }
                    if (index < 0) value = b;
                    else {
                        if (field.GetIsUTF8Text() || (tag.GetAPETagVersion() < 2000)) {
                            if (tag.GetAPETagVersion() >= 2000)
                                value = new String(b, 0, b.length + boundary, "UTF-8");
                            else value = new String(b, 0, b.length + boundary, "US-ASCII");
                        }
                        else value = new String(b, 0, b.length + boundary, "UTF-16");
                    }
                    properties.put("ape.tag." + field.GetFieldName(), value);
                }
            }
        }
        return new APEAudioFileFormat(APEFileFormatType.APE, APEAudioFormat.of(decoder),
                decoder.getApeInfoTotalFrames(), decoder.getApeInfoApeTotalBytes(), properties);
    }

    private static final SimpleDateFormat YYYY = new SimpleDateFormat("yyyy");
    private static Date parseDate(String dateString) {
        if (dateString == null) return null;
        else {
            try {
                return YYYY.parse(dateString);
            } catch (ParseException e) {
                return null;
            }
        }
    }

    public APEAudioFileFormat(Type type, AudioFormat format, long frameLength, long byteLength, Map<String, Object> properties) {
        super(type, format, frameLength, byteLength, properties);
    }

}
