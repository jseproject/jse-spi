package io.github.jseproject;

import com.beatofthedrum.wv.Defines;
import com.beatofthedrum.wv.WavPackContext;
import com.beatofthedrum.wv.WavPackUtils;
import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.convert.TAsynchronousFilteredAudioInputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;

public class DecodedWavPackAudioInputStream extends TAsynchronousFilteredAudioInputStream {

    private WavPackAudioInputStream audioInputStream;
    private WavPackContext context;
    private int[] sampleBuffer = null;
    private byte[] pcmBuffer = null;

    public DecodedWavPackAudioInputStream(AudioFormat outputFormat, WavPackAudioInputStream inputStream) {
        super(outputFormat, AudioSystem.NOT_SPECIFIED);
        if (TDebug.TraceAudioConverter) TDebug.out(">DecodedWavPackAudioInputStream(AudioFormat, AudioInputStream)");
        audioInputStream = inputStream;
        this.context = audioInputStream.context;
    }

    @Override
    public void execute() {
        if (TDebug.TraceAudioConverter) TDebug.out("execute(): begin");
        if (sampleBuffer == null) {
            sampleBuffer = new int[Defines.SAMPLE_BUFFER_SIZE];
            pcmBuffer = new byte[Defines.SAMPLE_BUFFER_SIZE * 4];
        }
        long samplesUnpacked = WavPackUtils.UnpackSamples(context, sampleBuffer,
                Defines.SAMPLE_BUFFER_SIZE / format.getChannels());
        if (context.error) {
            if (TDebug.TraceAudioConverter) TDebug.out(context.error_message);
            getCircularBuffer().close();
        }
        if (samplesUnpacked > 0) {
            samplesUnpacked = samplesUnpacked * format.getChannels();
            formatSamples(samplesUnpacked);
            getCircularBuffer().write(pcmBuffer, 0, (int) samplesUnpacked * WavPackUtils.GetBytesPerSample(context));
        }
        else getCircularBuffer().close();
        if (TDebug.TraceAudioConverter) TDebug.out("execute(): end");
    }

    // Reformat samples from longs in big-endian mode to
    // little-endian data with (possibly) less than 4 bytes / sample.
    private void formatSamples(long samples) {
        int temp;
        int counter = 0;
        int counter2 = 0;
        switch (WavPackUtils.GetBytesPerSample(context)) {
            case 1:
                while (samples > 0) {
                    pcmBuffer[counter] = (byte) (0x00FF & (sampleBuffer[counter] + 128));
                    counter ++;
                    samples --;
                }
                break;
            case 2:
                while (samples > 0) {
                    temp = sampleBuffer[counter2];
                    pcmBuffer[counter] = (byte) temp;
                    counter ++;
                    pcmBuffer[counter] = (byte) (temp >>> 8);
                    counter ++;
                    counter2 ++;
                    samples --;
                }
                break;
            case 3:
                while (samples > 0) {
                    temp = sampleBuffer[counter2];
                    pcmBuffer[counter] = (byte) temp;
                    counter ++;
                    pcmBuffer[counter] = (byte) (temp >>> 8);
                    counter ++;
                    pcmBuffer[counter] = (byte) (temp >>> 16);
                    counter ++;
                    counter2 ++;
                    samples --;
                }
                break;
            case 4:
                while (samples > 0) {
                    temp = sampleBuffer[counter2];
                    pcmBuffer[counter] = (byte) temp;
                    counter ++;
                    pcmBuffer[counter] = (byte) (temp >>> 8);
                    counter ++;
                    pcmBuffer[counter] = (byte) (temp >>> 16);
                    counter ++;
                    pcmBuffer[counter] = (byte) (temp >>> 24);
                    counter ++;
                    counter2 ++;
                    samples --;
                }
                break;
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        audioInputStream.close();
        audioInputStream = null;
        context = null;
        sampleBuffer = null;
        pcmBuffer = null;
    }

}
