package io.github.jseproject;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.spi.FormatConversionProvider;

import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.AudioFormats;

public class FlacFormatConversionProvider extends FormatConversionProvider {

    private static final AudioFormat.Encoding[]	EMPTY_ENCODING_ARRAY = new AudioFormat.Encoding[0];
    private static final AudioFormat[] EMPTY_FORMAT_ARRAY = new AudioFormat[0];

    private static final AudioFormat.Encoding[] SOURCE_ENCODINGS = new AudioFormat.Encoding[] { FlacEncoding.FLAC };
    private static final AudioFormat.Encoding[] TARGET_ENCODINGS = new AudioFormat.Encoding[] { AudioFormat.Encoding.PCM_SIGNED, AudioFormat.Encoding.PCM_UNSIGNED };
    private static final AudioFormat[] TARGET_FORMATS = new AudioFormat[40 /* 80 */];
    static {
        for (int channels = 0; channels < 8; channels ++) {
            TARGET_FORMATS[channels * 5] =
                    new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,
                            -1.0f, 8, channels + 1, (channels + 1), -1.0f, false);
            TARGET_FORMATS[channels * 5 + 1] =
                    new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                            -1.0f, 16, channels + 1, 2 * (channels + 1), -1.0f, true);
            TARGET_FORMATS[channels * 5 + 2] =
                    new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                            -1.0f, 16, channels + 1, 2 * (channels + 1), -1.0f, false);
            TARGET_FORMATS[channels * 5 + 3] =
                    new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,
                            -1.0f, 24, channels + 1, 3 * (channels + 1), -1.0f, true);
            TARGET_FORMATS[channels * 5 + 4] =
                    new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,
                            -1.0f, 24, channels + 1, 3 * (channels + 1), -1.0f, false);
        }
        /*
        for (int channels = 0; channels < 8; channels ++) {
            TARGET_FORMATS[channels * 10] =
                    new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                            -1.0f, 8, channels + 1, (channels + 1), -1.0f, false);
            TARGET_FORMATS[channels * 10 + 1] =
                    new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,
                            -1.0f, 8, channels + 1, (channels + 1), -1.0f, false);
            TARGET_FORMATS[channels * 10 + 2] =
                    new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                    -1.0f, 16, channels + 1, 2 * (channels + 1), -1.0f, true);
            TARGET_FORMATS[channels * 10 + 3] =
                    new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                    -1.0f, 16, channels + 1, 2 * (channels + 1), -1.0f, false);
            TARGET_FORMATS[channels * 10 + 4] =
                    new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,
                    -1.0f, 16, channels + 1, 2 * (channels + 1), -1.0f, true);
            TARGET_FORMATS[channels * 10 + 5] =
                    new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,
                    -1.0f, 16, channels + 1, 2 * (channels + 1), -1.0f, false);
            TARGET_FORMATS[channels * 10 + 6] =
                    new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                    -1.0f, 24, channels + 1, 3 * (channels + 1), -1.0f, true);
            TARGET_FORMATS[channels * 10 + 7] =
                    new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                    -1.0f, 24, channels + 1, 3 * (channels + 1), -1.0f, false);
            TARGET_FORMATS[channels * 10 + 8] =
                    new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,
                    -1.0f, 24, channels + 1, 3 * (channels + 1), -1.0f, true);
            TARGET_FORMATS[channels * 10 + 9] =
                    new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,
                    -1.0f, 24, channels + 1, 3 * (channels + 1), -1.0f, false);
        }
         */
    }

    @Override
    public AudioFormat.Encoding[] getSourceEncodings() {
        return SOURCE_ENCODINGS.clone();
    }

    @Override
    public AudioFormat.Encoding[] getTargetEncodings() {
        return TARGET_ENCODINGS.clone();
    }

    @Override
    public AudioFormat.Encoding[] getTargetEncodings(AudioFormat sourceFormat) {
        if (isAllowedSourceFormat(sourceFormat)) return getTargetEncodings();
        else return EMPTY_ENCODING_ARRAY;
    }

    static boolean checkFormat(AudioFormat format) {
        int sampleRate = (int) format.getSampleRate();
        if (sampleRate < 1 || sampleRate > 48000) return false;
        int channels = format.getChannels();
        if (channels < 1 || channels > 8) return false;
        switch (format.getSampleSizeInBits()) {
            case 8: case 16: case 24: break;
            default: return false;
        }
        return true;
    }

    protected boolean isAllowedSourceFormat(AudioFormat sourceFormat) {
        return checkFormat(sourceFormat) && isSourceEncodingSupported(sourceFormat.getEncoding());
    }

    protected boolean isAllowedTargetFormat(AudioFormat targetFormat) {
        return checkFormat(targetFormat) && isTargetEncodingSupported(targetFormat.getEncoding());
    }

    @Override
    public AudioFormat[] getTargetFormats(AudioFormat.Encoding targetEncoding, AudioFormat sourceFormat) {
        if (isConversionSupported(targetEncoding, sourceFormat)) return TARGET_FORMATS.clone();
        else return EMPTY_FORMAT_ARRAY;
    }

    @Override
    public AudioInputStream getAudioInputStream(AudioFormat.Encoding targetEncoding, AudioInputStream audioInputStream) {
        AudioFormat	sourceFormat = audioInputStream.getFormat();
        AudioFormat	targetFormat = new AudioFormat(targetEncoding,
                sourceFormat.getSampleRate(), sourceFormat.getSampleSizeInBits(), sourceFormat.getChannels(),
                sourceFormat.getFrameSize(), sourceFormat.getFrameRate(), sourceFormat.isBigEndian());
        if (TDebug.TraceAudioConverter) {
            TDebug.out("TFormatConversionProvider.getAudioInputStream(AudioFormat.Encoding, AudioInputStream):");
            TDebug.out("trying to convert to " + targetFormat);
        }
        return getAudioInputStream(targetFormat, audioInputStream);
    }

    @Override
    public AudioInputStream getAudioInputStream(AudioFormat targetFormat, AudioInputStream audioInputStream) {
        if (audioInputStream instanceof FlacAudioInputStream && isConversionSupported(targetFormat, audioInputStream.getFormat()))
            return new DecodedFlacAudioInputStream(targetFormat, (FlacAudioInputStream) audioInputStream);
        else throw new IllegalArgumentException("conversion not supported");
    }

    @Override
    public boolean isConversionSupported(AudioFormat.Encoding targetEncoding, AudioFormat sourceFormat) {
        return isAllowedSourceFormat(sourceFormat) && isTargetEncodingSupported(targetEncoding);
    }

    @Override
    public boolean isConversionSupported(AudioFormat targetFormat, AudioFormat sourceFormat) {
        if (TDebug.TraceAudioConverter) {
            TDebug.out(">TFormatConversionProvider.isConversionSupported(AudioFormat, AudioFormat):");
            TDebug.out("class: " + getClass().getName());
            TDebug.out("checking if conversion possible");
            TDebug.out("from: " + sourceFormat);
            TDebug.out("to: " + targetFormat);
        }
        for (AudioFormat handledFormat : getTargetFormats(targetFormat.getEncoding(), sourceFormat)) {
            if (TDebug.TraceAudioConverter) TDebug.out("checking against possible target format: " + handledFormat);
            if (handledFormat != null && AudioFormats.matches(handledFormat, targetFormat)
                    && isAllowedSourceFormat(sourceFormat) && isAllowedTargetFormat(targetFormat)) {
                if (TDebug.TraceAudioConverter) TDebug.out("<result=true");
                return true;
            }
        }
        if (TDebug.TraceAudioConverter) TDebug.out("<result=false");
        return false;
    }

}
