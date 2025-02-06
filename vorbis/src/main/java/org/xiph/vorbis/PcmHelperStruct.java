package org.xiph.vorbis;

/**
 * Helper structure to return data
 */
public class PcmHelperStruct {
    public float[][] pcm = null;
    /**
     * offset to pcm. float val = pcm[i][pcmret]
     */
    public int pcmret = 0;
    public int samples = 0;
    /**
     * any info
     */
    public int data = 0;

    //
    final void clear() {
        pcm = null;
        pcmret = 0;
        samples = 0;
        data = 0;
    }
}
