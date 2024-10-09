/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 2019 Alexey Kuznetsov
 * Copyright (c) 2002-2018 Xiph.Org Foundation
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Neither the name of the Xiph.org Foundation nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE FOUNDATION
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
