/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 2011-2019 The JCodec Project
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.sourceforge.jaad.aac.sbr;

/**
 * This class is part of JAAD ( jaadec.sourceforge.net ) that is distributed
 * under the Public Domain license. Code changes provided by the JCodec project
 * are distributed under FreeBSD license. 
 * 
 * @author in-somnia
 */
interface SBRConstants {

	int[] startMinTable = {7, 7, 10, 11, 12, 16, 16,
		17, 24, 32, 35, 48};
	int[] offsetIndexTable = {5, 5, 4, 4, 4, 3, 2, 1, 0,
		6, 6, 6};
	int[][] OFFSET = {
		{-8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7}, //16000
		{-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 9, 11, 13}, //22050
		{-5, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 9, 11, 13, 16}, //24000
		{-6, -4, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 9, 11, 13, 16}, //32000
		{-4, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 9, 11, 13, 16, 20}, //44100-64000
		{-2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 9, 11, 13, 16, 20, 24}, //>64000
		{0, 1, 2, 3, 4, 5, 6, 7, 9, 11, 13, 16, 20, 24, 28, 33}
	};
	int EXTENSION_ID_PS = 2;
	int MAX_NTSRHFG = 40; //maximum of number_time_slots * rate + HFGen. 16*2+8
	int MAX_NTSR = 32; //max number_time_slots * rate, ok for DRM and not DRM mode
	int MAX_M = 49; //maximum value for M
	int MAX_L_E = 5; //maximum value for L_E
	int EXT_SBR_DATA = 13;
	int EXT_SBR_DATA_CRC = 14;
	int FIXFIX = 0;
	int FIXVAR = 1;
	int VARFIX = 2;
	int VARVAR = 3;
	int LO_RES = 0;
	int HI_RES = 1;
	int NO_TIME_SLOTS_960 = 15;
	int NO_TIME_SLOTS = 16;
	int RATE = 2;
	int NOISE_FLOOR_OFFSET = 6;
	int T_HFGEN = 8;
	int T_HFADJ = 2;
}
