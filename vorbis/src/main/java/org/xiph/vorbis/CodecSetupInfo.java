package org.xiph.vorbis;

/**
 * codec_setup_info contains all the setup information specific to the
 * specific compression/decompression mode in progress (eg,
 * psychoacoustic settings, channel setup, options, codebook
 * etc).
 */
class CodecSetupInfo {
    /**
     * Vorbis supports only short and long blocks, but allows the
     * encoder to choose the sizes
     */
    final int[] blocksizes = new int[2];

    /**
     * modes are the primary means of supporting on-the-fly different
     * blocksizes, different channel mappings (LR or M/A),
     * different residue backends, etc.  Each mode consists of a
     * blocksize flag and a mapping (along with the mapping setup
     */
    int modes = 0;
    int maps = 0;
    int floors = 0;
    int residues = 0;
    int books = 0;
    /**
     * encode only
     */
    int psys = 0;

    final InfoMode[] mode_param = new InfoMode[64];
    final int[] map_type = new int[64];
    final InfoMapping[] map_param = new InfoMapping[64];
    final int[] floor_type = new int[64];
    final InfoFloor[] floor_param = new InfoFloor[64];
    final int[] residue_type = new int[64];
    final InfoResidue[] residue_param = new InfoResidue[64];
    final StaticCodebook[] book_param = new StaticCodebook[256];
    Codebook[] fullbooks;

    /**
     * encode only
     */
    final InfoPsy[] psy_param = new InfoPsy[4];
    final InfoPsyGlobal psy_g_param = new InfoPsyGlobal();

    final BitrateManagerInfo bi = new BitrateManagerInfo();
    /**
     * used only by vorbisenc.c.  It's a
     * highly redundant structure, but
     * improves clarity of program flow.
     */
    final HighLevelEncodeSetup hi = new HighLevelEncodeSetup();
    /**
     * painless downsample for decode. 1 or 0
     */
    int halfrate_flag = 0;// java: not boolean, because uses as int value
}
