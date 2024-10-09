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

package net.sourceforge.jaad.aac.tools;

/**
 * This class is part of JAAD ( jaadec.sourceforge.net ) that is distributed
 * under the Public Domain license. Code changes provided by the JCodec project
 * are distributed under FreeBSD license.
 * 
 * Tables of coefficients used for TNS.
 * The suffix indicates the values of coefCompress and coefRes.
 * @author in-somnia
 */
interface TNSTables {

	float[] TNS_COEF_1_3 = {
		0.00000000f, -0.43388373f, 0.64278758f, 0.34202015f,};
	float[] TNS_COEF_0_3 = {
		0.00000000f, -0.43388373f, -0.78183150f, -0.97492790f,
		0.98480773f, 0.86602539f, 0.64278758f, 0.34202015f,};
	float[] TNS_COEF_1_4 = {
		0.00000000f, -0.20791170f, -0.40673664f, -0.58778524f,
		0.67369562f, 0.52643216f, 0.36124167f, 0.18374951f,};
	float[] TNS_COEF_0_4 = {
		0.00000000f, -0.20791170f, -0.40673664f, -0.58778524f,
		-0.74314481f, -0.86602539f, -0.95105654f, -0.99452192f,
		0.99573416f, 0.96182561f, 0.89516330f, 0.79801720f,
		0.67369562f, 0.52643216f, 0.36124167f, 0.18374951f,};
	float[][] TNS_TABLES = {
		TNS_COEF_0_3, TNS_COEF_0_4, TNS_COEF_1_3, TNS_COEF_1_4
	};
}
