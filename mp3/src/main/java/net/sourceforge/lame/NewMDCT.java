/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 1999-2010 The LAME Project
 * Copyright (c) 1999-2008 JavaZOOM
 * Copyright (c) 2001-2002 Naoki Shibata
 * Copyright (c) 2001 Jonathan Dee
 * Copyright (c) 2000-2017 Robert Hegemann
 * Copyright (c) 2000-2008 Gabriel Bouvigne
 * Copyright (c) 2000-2005 Alexander Leidinger
 * Copyright (c) 2000 Don Melton
 * Copyright (c) 1999-2005 Takehiro Tominaga
 * Copyright (c) 1999-2001 Mark Taylor
 * Copyright (c) 1999 Albert L. Faber
 * Copyright (c) 1988, 1993 Ron Mayer
 * Copyright (c) 1998 Michael Cheng
 * Copyright (c) 1997 Jeff Tsay
 * Copyright (c) 1995-1997 Michael Hipp
 * Copyright (c) 1993-1994 Tobias Bading,
 *                         Berlin University of Technology
 *
 * - This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * - This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * - You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package net.sourceforge.lame;

/*
 *         Special Thanks to Patrick De Smet for your advices.
 */

// newmdct.c

class NewMDCT {
	private static final double SQRT2 = Math.sqrt( 2. );// 1.41421356237309504880

	private static final float enwindow[] = {
		(float)(-4.77e-07 * 0.740951125354959 / 2.384e-06), (float)(1.03951e-04 * 0.740951125354959 / 2.384e-06),
		(float)(9.53674e-04 * 0.740951125354959 / 2.384e-06), (float)(2.841473e-03 * 0.740951125354959 / 2.384e-06),
		(float)(3.5758972e-02 * 0.740951125354959 / 2.384e-06), (float)(3.401756e-03 * 0.740951125354959 / 2.384e-06), (float)(9.83715e-04 * 0.740951125354959 / 2.384e-06), (float)(9.9182e-05 * 0.740951125354959 / 2.384e-06), /* 15 */
		(float)(1.2398e-05 * 0.740951125354959 / 2.384e-06), (float)(1.91212e-04 * 0.740951125354959 / 2.384e-06),
		(float)(2.283096e-03 * 0.740951125354959 / 2.384e-06), (float)(1.6994476e-02 * 0.740951125354959 / 2.384e-06),
		(float)(-1.8756866e-02 * 0.740951125354959 / 2.384e-06), (float)(-2.630711e-03 * 0.740951125354959 / 2.384e-06),
		(float)(-2.47478e-04 * 0.740951125354959 / 2.384e-06), (float)(-1.4782e-05 * 0.740951125354959 / 2.384e-06),
		(float)(9.063471690191471e-01),
		(float)(1.960342806591213e-01),

		(float)(-4.77e-07 * 0.773010453362737 / 2.384e-06), (float)(1.05858e-04 * 0.773010453362737 / 2.384e-06),
		(float)(9.30786e-04 * 0.773010453362737 / 2.384e-06), (float)(2.521515e-03 * 0.773010453362737 / 2.384e-06),
		(float)(3.5694122e-02 * 0.773010453362737 / 2.384e-06), (float)(3.643036e-03 * 0.773010453362737 / 2.384e-06), (float)(9.91821e-04 * 0.773010453362737 / 2.384e-06), (float)(9.6321e-05 * 0.773010453362737 / 2.384e-06), /* 14 */
		(float)(1.1444e-05 * 0.773010453362737 / 2.384e-06), (float)(1.65462e-04 * 0.773010453362737 / 2.384e-06),
		(float)(2.110004e-03 * 0.773010453362737 / 2.384e-06), (float)(1.6112804e-02 * 0.773010453362737 / 2.384e-06),
		(float)(-1.9634247e-02 * 0.773010453362737 / 2.384e-06), (float)(-2.803326e-03 * 0.773010453362737 / 2.384e-06),
		(float)(-2.77042e-04 * 0.773010453362737 / 2.384e-06), (float)(-1.6689e-05 * 0.773010453362737 / 2.384e-06),
		(float)(8.206787908286602e-01),
		(float)(3.901806440322567e-01),

		(float)(-4.77e-07 * 0.803207531480645 / 2.384e-06), (float)(1.07288e-04 * 0.803207531480645 / 2.384e-06),
		(float)(9.02653e-04 * 0.803207531480645 / 2.384e-06), (float)(2.174854e-03 * 0.803207531480645 / 2.384e-06),
		(float)(3.5586357e-02 * 0.803207531480645 / 2.384e-06), (float)(3.858566e-03 * 0.803207531480645 / 2.384e-06), (float)(9.95159e-04 * 0.803207531480645 / 2.384e-06), (float)(9.3460e-05 * 0.803207531480645 / 2.384e-06), /* 13 */
		(float)(1.0014e-05 * 0.803207531480645 / 2.384e-06), (float)(1.40190e-04 * 0.803207531480645 / 2.384e-06),
		(float)(1.937389e-03 * 0.803207531480645 / 2.384e-06), (float)(1.5233517e-02 * 0.803207531480645 / 2.384e-06),
		(float)(-2.0506859e-02 * 0.803207531480645 / 2.384e-06), (float)(-2.974033e-03 * 0.803207531480645 / 2.384e-06),
		(float)(-3.07560e-04 * 0.803207531480645 / 2.384e-06), (float)(-1.8120e-05 * 0.803207531480645 / 2.384e-06),
		(float)(7.416505462720353e-01),
		(float)(5.805693545089249e-01),

		(float)(-4.77e-07 * 0.831469612302545 / 2.384e-06), (float)(1.08242e-04 * 0.831469612302545 / 2.384e-06),
		(float)(8.68797e-04 * 0.831469612302545 / 2.384e-06), (float)(1.800537e-03 * 0.831469612302545 / 2.384e-06),
		(float)(3.5435200e-02 * 0.831469612302545 / 2.384e-06), (float)(4.049301e-03 * 0.831469612302545 / 2.384e-06), (float)(9.94205e-04 * 0.831469612302545 / 2.384e-06), (float)(9.0599e-05 * 0.831469612302545 / 2.384e-06), /* 12 */
		(float)(9.060e-06 * 0.831469612302545 / 2.384e-06), (float)(1.16348e-04 * 0.831469612302545 / 2.384e-06),
		(float)(1.766682e-03 * 0.831469612302545 / 2.384e-06), (float)(1.4358521e-02 * 0.831469612302545 / 2.384e-06),
		(float)(-2.1372318e-02 * 0.831469612302545 / 2.384e-06), (float)(-3.14188e-03 * 0.831469612302545 / 2.384e-06),
		(float)(-3.39031e-04 * 0.831469612302545 / 2.384e-06), (float)(-1.9550e-05 * 0.831469612302545 / 2.384e-06),
		(float)(6.681786379192989e-01),
		(float)(7.653668647301797e-01),

		(float)(-4.77e-07 * 0.857728610000272 / 2.384e-06), (float)(1.08719e-04 * 0.857728610000272 / 2.384e-06),
		(float)(8.29220e-04 * 0.857728610000272 / 2.384e-06), (float)(1.399517e-03 * 0.857728610000272 / 2.384e-06),
		(float)(3.5242081e-02 * 0.857728610000272 / 2.384e-06), (float)(4.215240e-03 * 0.857728610000272 / 2.384e-06), (float)(9.89437e-04 * 0.857728610000272 / 2.384e-06), (float)(8.7261e-05 * 0.857728610000272 / 2.384e-06), /* 11 */
		(float)(8.106e-06 * 0.857728610000272 / 2.384e-06), (float)(9.3937e-05 * 0.857728610000272 / 2.384e-06),
		(float)(1.597881e-03 * 0.857728610000272 / 2.384e-06), (float)(1.3489246e-02 * 0.857728610000272 / 2.384e-06),
		(float)(-2.2228718e-02 * 0.857728610000272 / 2.384e-06), (float)(-3.306866e-03 * 0.857728610000272 / 2.384e-06),
		(float)(-3.71456e-04 * 0.857728610000272 / 2.384e-06), (float)(-2.1458e-05 * 0.857728610000272 / 2.384e-06),
		(float)(5.993769336819237e-01),
		(float)(9.427934736519954e-01),

		(float)(-4.77e-07 * 0.881921264348355 / 2.384e-06), (float)(1.08719e-04 * 0.881921264348355 / 2.384e-06),
		(float)(7.8392e-04 * 0.881921264348355 / 2.384e-06), (float)(9.71317e-04 * 0.881921264348355 / 2.384e-06),
		(float)(3.5007000e-02 * 0.881921264348355 / 2.384e-06), (float)(4.357815e-03 * 0.881921264348355 / 2.384e-06), (float)(9.80854e-04 * 0.881921264348355 / 2.384e-06), (float)(8.3923e-05 * 0.881921264348355 / 2.384e-06), /* 10 */
		(float)(7.629e-06 * 0.881921264348355 / 2.384e-06), (float)(7.2956e-05 * 0.881921264348355 / 2.384e-06),
		(float)(1.432419e-03 * 0.881921264348355 / 2.384e-06), (float)(1.2627602e-02 * 0.881921264348355 / 2.384e-06),
		(float)(-2.3074150e-02 * 0.881921264348355 / 2.384e-06), (float)(-3.467083e-03 * 0.881921264348355 / 2.384e-06),
		(float)(-4.04358e-04 * 0.881921264348355 / 2.384e-06), (float)(-2.3365e-05 * 0.881921264348355 / 2.384e-06),
		(float)(5.345111359507916e-01),
		(float)(1.111140466039205e+00),

		(float)(-9.54e-07 * 0.903989293123443 / 2.384e-06), (float)(1.08242e-04 * 0.903989293123443 / 2.384e-06),
		(float)(7.31945e-04 * 0.903989293123443 / 2.384e-06), (float)(5.15938e-04 * 0.903989293123443 / 2.384e-06),
		(float)(3.4730434e-02 * 0.903989293123443 / 2.384e-06), (float)(4.477024e-03 * 0.903989293123443 / 2.384e-06), (float)(9.68933e-04 * 0.903989293123443 / 2.384e-06), (float)(8.0585e-05 * 0.903989293123443 / 2.384e-06), /* 9 */
		(float)(6.676e-06 * 0.903989293123443 / 2.384e-06), (float)(5.2929e-05 * 0.903989293123443 / 2.384e-06),
		(float)(1.269817e-03 * 0.903989293123443 / 2.384e-06), (float)(1.1775017e-02 * 0.903989293123443 / 2.384e-06),
		(float)(-2.3907185e-02 * 0.903989293123443 / 2.384e-06), (float)(-3.622532e-03 * 0.903989293123443 / 2.384e-06),
		(float)(-4.38213e-04 * 0.903989293123443 / 2.384e-06), (float)(-2.5272e-05 * 0.903989293123443 / 2.384e-06),
		(float)(4.729647758913199e-01),
		(float)(1.268786568327291e+00),

		(float)(-9.54e-07 * 0.92387953251128675613 / 2.384e-06),
		(float)(1.06812e-04 * 0.92387953251128675613 / 2.384e-06),
		(float)(6.74248e-04 * 0.92387953251128675613 / 2.384e-06),
		(float)(3.3379e-05 * 0.92387953251128675613 / 2.384e-06),
		(float)(3.4412861e-02 * 0.92387953251128675613 / 2.384e-06),
		(float)(4.573822e-03 * 0.92387953251128675613 / 2.384e-06),
		(float)(9.54151e-04 * 0.92387953251128675613 / 2.384e-06),
		(float)(7.6771e-05 * 0.92387953251128675613 / 2.384e-06),
		(float)(6.199e-06 * 0.92387953251128675613 / 2.384e-06), (float)(3.4332e-05 * 0.92387953251128675613 / 2.384e-06),
		(float)(1.111031e-03 * 0.92387953251128675613 / 2.384e-06),
		(float)(1.0933399e-02 * 0.92387953251128675613 / 2.384e-06),
		(float)(-2.4725437e-02 * 0.92387953251128675613 / 2.384e-06),
		(float)(-3.771782e-03 * 0.92387953251128675613 / 2.384e-06),
		(float)(-4.72546e-04 * 0.92387953251128675613 / 2.384e-06),
		(float)(-2.7657e-05 * 0.92387953251128675613 / 2.384e-06),
		(float)(4.1421356237309504879e-01), /* tan(PI/8) */
		(float)(1.414213562373095e+00),

		(float)(-9.54e-07 * 0.941544065183021 / 2.384e-06), (float)(1.05381e-04 * 0.941544065183021 / 2.384e-06),
		(float)(6.10352e-04 * 0.941544065183021 / 2.384e-06), (float)(-4.75883e-04 * 0.941544065183021 / 2.384e-06),
		(float)(3.4055710e-02 * 0.941544065183021 / 2.384e-06), (float)(4.649162e-03 * 0.941544065183021 / 2.384e-06), (float)(9.35555e-04 * 0.941544065183021 / 2.384e-06), (float)(7.3433e-05 * 0.941544065183021 / 2.384e-06), /* 7 */
		(float)(5.245e-06 * 0.941544065183021 / 2.384e-06), (float)(1.7166e-05 * 0.941544065183021 / 2.384e-06),
		(float)(9.56535e-04 * 0.941544065183021 / 2.384e-06), (float)(1.0103703e-02 * 0.941544065183021 / 2.384e-06),
		(float)(-2.5527000e-02 * 0.941544065183021 / 2.384e-06), (float)(-3.914356e-03 * 0.941544065183021 / 2.384e-06),
		(float)(-5.07355e-04 * 0.941544065183021 / 2.384e-06), (float)(-3.0041e-05 * 0.941544065183021 / 2.384e-06),
		(float)(3.578057213145241e-01),
		(float)(1.546020906725474e+00),

		(float)(-9.54e-07 * 0.956940335732209 / 2.384e-06), (float)(1.02520e-04 * 0.956940335732209 / 2.384e-06),
		(float)(5.39303e-04 * 0.956940335732209 / 2.384e-06), (float)(-1.011848e-03 * 0.956940335732209 / 2.384e-06),
		(float)(3.3659935e-02 * 0.956940335732209 / 2.384e-06), (float)(4.703045e-03 * 0.956940335732209 / 2.384e-06), (float)(9.15051e-04 * 0.956940335732209 / 2.384e-06), (float)(7.0095e-05 * 0.956940335732209 / 2.384e-06), /* 6 */
		(float)(4.768e-06 * 0.956940335732209 / 2.384e-06), (float)(9.54e-07 * 0.956940335732209 / 2.384e-06),
		(float)(8.06808e-04 * 0.956940335732209 / 2.384e-06), (float)(9.287834e-03 * 0.956940335732209 / 2.384e-06),
		(float)(-2.6310921e-02 * 0.956940335732209 / 2.384e-06), (float)(-4.048824e-03 * 0.956940335732209 / 2.384e-06),
		(float)(-5.42164e-04 * 0.956940335732209 / 2.384e-06), (float)(-3.2425e-05 * 0.956940335732209 / 2.384e-06),
		(float)(3.033466836073424e-01),
		(float)(1.662939224605090e+00),

		(float)(-1.431e-06 * 0.970031253194544 / 2.384e-06), (float)(9.9182e-05 * 0.970031253194544 / 2.384e-06),
		(float)(4.62532e-04 * 0.970031253194544 / 2.384e-06), (float)(-1.573563e-03 * 0.970031253194544 / 2.384e-06),
		(float)(3.3225536e-02 * 0.970031253194544 / 2.384e-06), (float)(4.737377e-03 * 0.970031253194544 / 2.384e-06), (float)(8.91685e-04 * 0.970031253194544 / 2.384e-06), (float)(6.6280e-05 * 0.970031253194544 / 2.384e-06), /* 5 */
		(float)(4.292e-06 * 0.970031253194544 / 2.384e-06), (float)(-1.3828e-05 * 0.970031253194544 / 2.384e-06),
		(float)(6.61850e-04 * 0.970031253194544 / 2.384e-06), (float)(8.487225e-03 * 0.970031253194544 / 2.384e-06),
		(float)(-2.7073860e-02 * 0.970031253194544 / 2.384e-06), (float)(-4.174709e-03 * 0.970031253194544 / 2.384e-06),
		(float)(-5.76973e-04 * 0.970031253194544 / 2.384e-06), (float)(-3.4809e-05 * 0.970031253194544 / 2.384e-06),
		(float)(2.504869601913055e-01),
		(float)(1.763842528696710e+00),

		(float)(-1.431e-06 * 0.98078528040323 / 2.384e-06), (float)(9.5367e-05 * 0.98078528040323 / 2.384e-06),
		(float)(3.78609e-04 * 0.98078528040323 / 2.384e-06), (float)(-2.161503e-03 * 0.98078528040323 / 2.384e-06),
		(float)(3.2754898e-02 * 0.98078528040323 / 2.384e-06), (float)(4.752159e-03 * 0.98078528040323 / 2.384e-06), (float)(8.66413e-04 * 0.98078528040323 / 2.384e-06), (float)(6.2943e-05 * 0.98078528040323 / 2.384e-06), /* 4 */
		(float)(3.815e-06 * 0.98078528040323 / 2.384e-06), (float)(-2.718e-05 * 0.98078528040323 / 2.384e-06),
		(float)(5.22137e-04 * 0.98078528040323 / 2.384e-06), (float)(7.703304e-03 * 0.98078528040323 / 2.384e-06),
		(float)(-2.7815342e-02 * 0.98078528040323 / 2.384e-06), (float)(-4.290581e-03 * 0.98078528040323 / 2.384e-06),
		(float)(-6.11782e-04 * 0.98078528040323 / 2.384e-06), (float)(-3.7670e-05 * 0.98078528040323 / 2.384e-06),
		(float)(1.989123673796580e-01),
		(float)(1.847759065022573e+00),

		(float)(-1.907e-06 * 0.989176509964781 / 2.384e-06), (float)(9.0122e-05 * 0.989176509964781 / 2.384e-06),
		(float)(2.88486e-04 * 0.989176509964781 / 2.384e-06), (float)(-2.774239e-03 * 0.989176509964781 / 2.384e-06),
		(float)(3.2248020e-02 * 0.989176509964781 / 2.384e-06), (float)(4.748821e-03 * 0.989176509964781 / 2.384e-06), (float)(8.38757e-04 * 0.989176509964781 / 2.384e-06), (float)(5.9605e-05 * 0.989176509964781 / 2.384e-06), /* 3 */
		(float)(3.338e-06 * 0.989176509964781 / 2.384e-06), (float)(-3.9577e-05 * 0.989176509964781 / 2.384e-06),
		(float)(3.88145e-04 * 0.989176509964781 / 2.384e-06), (float)(6.937027e-03 * 0.989176509964781 / 2.384e-06),
		(float)(-2.8532982e-02 * 0.989176509964781 / 2.384e-06), (float)(-4.395962e-03 * 0.989176509964781 / 2.384e-06),
		(float)(-6.46591e-04 * 0.989176509964781 / 2.384e-06), (float)(-4.0531e-05 * 0.989176509964781 / 2.384e-06),
		(float)(1.483359875383474e-01),
		(float)(1.913880671464418e+00),

		(float)(-1.907e-06 * 0.995184726672197 / 2.384e-06), (float)(8.4400e-05 * 0.995184726672197 / 2.384e-06),
		(float)(1.91689e-04 * 0.995184726672197 / 2.384e-06), (float)(-3.411293e-03 * 0.995184726672197 / 2.384e-06),
		(float)(3.1706810e-02 * 0.995184726672197 / 2.384e-06), (float)(4.728317e-03 * 0.995184726672197 / 2.384e-06),
		(float)(8.09669e-04 * 0.995184726672197 / 2.384e-06), (float)(5.579e-05 * 0.995184726672197 / 2.384e-06),
		(float)(3.338e-06 * 0.995184726672197 / 2.384e-06), (float)(-5.0545e-05 * 0.995184726672197 / 2.384e-06),
		(float)(2.59876e-04 * 0.995184726672197 / 2.384e-06), (float)(6.189346e-03 * 0.995184726672197 / 2.384e-06),
		(float)(-2.9224873e-02 * 0.995184726672197 / 2.384e-06), (float)(-4.489899e-03 * 0.995184726672197 / 2.384e-06),
		(float)(-6.80923e-04 * 0.995184726672197 / 2.384e-06), (float)(-4.3392e-05 * 0.995184726672197 / 2.384e-06),
		(float)(9.849140335716425e-02),
		(float)(1.961570560806461e+00),

		(float)(-2.384e-06 * 0.998795456205172 / 2.384e-06), (float)(7.7724e-05 * 0.998795456205172 / 2.384e-06),
		(float)(8.8215e-05 * 0.998795456205172 / 2.384e-06), (float)(-4.072189e-03 * 0.998795456205172 / 2.384e-06),
		(float)(3.1132698e-02 * 0.998795456205172 / 2.384e-06), (float)(4.691124e-03 * 0.998795456205172 / 2.384e-06),
		(float)(7.79152e-04 * 0.998795456205172 / 2.384e-06), (float)(5.2929e-05 * 0.998795456205172 / 2.384e-06),
		(float)(2.861e-06 * 0.998795456205172 / 2.384e-06), (float)(-6.0558e-05 * 0.998795456205172 / 2.384e-06),
		(float)(1.37329e-04 * 0.998795456205172 / 2.384e-06), (float)(5.462170e-03 * 0.998795456205172 / 2.384e-06),
		(float)(-2.9890060e-02 * 0.998795456205172 / 2.384e-06), (float)(-4.570484e-03 * 0.998795456205172 / 2.384e-06),
		(float)(-7.14302e-04 * 0.998795456205172 / 2.384e-06), (float)(-4.6253e-05 * 0.998795456205172 / 2.384e-06),
		(float)(4.912684976946725e-02),
		(float)(1.990369453344394e+00),

		(float)(3.5780907e-02 * SQRT2 * 0.5 / 2.384e-06), (float)(1.7876148e-02 * SQRT2 * 0.5 / 2.384e-06),
		(float)(3.134727e-03 * SQRT2 * 0.5 / 2.384e-06), (float)(2.457142e-03 * SQRT2 * 0.5 / 2.384e-06),
		(float)(9.71317e-04 * SQRT2 * 0.5 / 2.384e-06), (float)(2.18868e-04 * SQRT2 * 0.5 / 2.384e-06),
		(float)(1.01566e-04 * SQRT2 * 0.5 / 2.384e-06), (float)(1.3828e-05 * SQRT2 * 0.5 / 2.384e-06),

		(float)(3.0526638e-02 / 2.384e-06), (float)(4.638195e-03 / 2.384e-06), (float)(7.47204e-04 / 2.384e-06),
		(float)(4.9591e-05 / 2.384e-06),
		(float)(4.756451e-03 / 2.384e-06), (float)(2.1458e-05 / 2.384e-06), (float)(-6.9618e-05 / 2.384e-06), /*    2.384e-06/2.384e-06 */
	};

	private static final int NS = 12;
	private static final int NL = 36;

	private static final float win[][] = {// [4][NL] = {
		{
			(float)2.382191739347913e-13,
			(float)6.423305872147834e-13,
			(float)9.400849094049688e-13,
			(float)1.122435026096556e-12,
			(float)1.183840321267481e-12,
			(float)1.122435026096556e-12,
			(float)9.400849094049690e-13,
			(float)6.423305872147839e-13,
			(float)2.382191739347918e-13,

			(float)5.456116108943412e-12,
			(float)4.878985199565852e-12,
			(float)4.240448995017367e-12,
			(float)3.559909094758252e-12,
			(float)2.858043359288075e-12,
			(float)2.156177623817898e-12,
			(float)1.475637723558783e-12,
			(float)8.371015190102974e-13,
			(float)2.599706096327376e-13,

			(float)-5.456116108943412e-12,
			(float)-4.878985199565852e-12,
			(float)-4.240448995017367e-12,
			(float)-3.559909094758252e-12,
			(float)-2.858043359288076e-12,
			(float)-2.156177623817898e-12,
			(float)-1.475637723558783e-12,
			(float)-8.371015190102975e-13,
			(float)-2.599706096327376e-13,

			(float)-2.382191739347923e-13,
			(float)-6.423305872147843e-13,
			(float)-9.400849094049696e-13,
			(float)-1.122435026096556e-12,
			(float)-1.183840321267481e-12,
			(float)-1.122435026096556e-12,
			(float)-9.400849094049694e-13,
			(float)-6.423305872147840e-13,
			(float)-2.382191739347918e-13,
		},
		{
			(float)2.382191739347913e-13,
			(float)6.423305872147834e-13,
			(float)9.400849094049688e-13,
			(float)1.122435026096556e-12,
			(float)1.183840321267481e-12,
			(float)1.122435026096556e-12,
			(float)9.400849094049688e-13,
			(float)6.423305872147841e-13,
			(float)2.382191739347918e-13,

			(float)5.456116108943413e-12,
			(float)4.878985199565852e-12,
			(float)4.240448995017367e-12,
			(float)3.559909094758253e-12,
			(float)2.858043359288075e-12,
			(float)2.156177623817898e-12,
			(float)1.475637723558782e-12,
			(float)8.371015190102975e-13,
			(float)2.599706096327376e-13,

			(float)-5.461314069809755e-12,
			(float)-4.921085770524055e-12,
			(float)-4.343405037091838e-12,
			(float)-3.732668368707687e-12,
			(float)-3.093523840190885e-12,
			(float)-2.430835727329465e-12,
			(float)-1.734679010007751e-12,
			(float)-9.748253656609281e-13,
			(float)-2.797435120168326e-13,

			(float)0.000000000000000e+00,
			(float)0.000000000000000e+00,
			(float)0.000000000000000e+00,
			(float)0.000000000000000e+00,
			(float)0.000000000000000e+00,
			(float)0.000000000000000e+00,
			(float)-2.283748241799531e-13,
			(float)-4.037858874020686e-13,
			(float)-2.146547464825323e-13,
		},
		{
			(float)1.316524975873958e-01, /* win[SHORT_TYPE] */
			(float)4.142135623730950e-01,
			(float)7.673269879789602e-01,

			(float)1.091308501069271e+00, /* tantab_l */
			(float)1.303225372841206e+00,
			(float)1.569685577117490e+00,
			(float)1.920982126971166e+00,
			(float)2.414213562373094e+00,
			(float)3.171594802363212e+00,
			(float)4.510708503662055e+00,
			(float)7.595754112725146e+00,
			(float)2.290376554843115e+01,

			(float)0.98480775301220802032, /* cx */
			(float)0.64278760968653936292,
			(float)0.34202014332566882393,
			(float)0.93969262078590842791,
			(float)-0.17364817766693030343,
			(float)-0.76604444311897790243,
			(float)0.86602540378443870761,
			(float)0.500000000000000e+00,

			(float)-5.144957554275265e-01, /* ca */
			(float)-4.717319685649723e-01,
			(float)-3.133774542039019e-01,
			(float)-1.819131996109812e-01,
			(float)-9.457419252642064e-02,
			(float)-4.096558288530405e-02,
			(float)-1.419856857247115e-02,
			(float)-3.699974673760037e-03,

			(float)8.574929257125442e-01, /* cs */
			(float)8.817419973177052e-01,
			(float)9.496286491027329e-01,
			(float)9.833145924917901e-01,
			(float)9.955178160675857e-01,
			(float)9.991605581781475e-01,
			(float)9.998991952444470e-01,
			(float)9.999931550702802e-01,
		},
		{
			(float)0.000000000000000e+00,
			(float)0.000000000000000e+00,
			(float)0.000000000000000e+00,
			(float)0.000000000000000e+00,
			(float)0.000000000000000e+00,
			(float)0.000000000000000e+00,
			(float)2.283748241799531e-13,
			(float)4.037858874020686e-13,
			(float)2.146547464825323e-13,

			(float)5.461314069809755e-12,
			(float)4.921085770524055e-12,
			(float)4.343405037091838e-12,
			(float)3.732668368707687e-12,
			(float)3.093523840190885e-12,
			(float)2.430835727329466e-12,
			(float)1.734679010007751e-12,
			(float)9.748253656609281e-13,
			(float)2.797435120168326e-13,

			(float)-5.456116108943413e-12,
			(float)-4.878985199565852e-12,
			(float)-4.240448995017367e-12,
			(float)-3.559909094758253e-12,
			(float)-2.858043359288075e-12,
			(float)-2.156177623817898e-12,
			(float)-1.475637723558782e-12,
			(float)-8.371015190102975e-13,
			(float)-2.599706096327376e-13,

			(float)-2.382191739347913e-13,
			(float)-6.423305872147834e-13,
			(float)-9.400849094049688e-13,
			(float)-1.122435026096556e-12,
			(float)-1.183840321267481e-12,
			(float)-1.122435026096556e-12,
			(float)-9.400849094049688e-13,
			(float)-6.423305872147841e-13,
			(float)-2.382191739347918e-13,
		}
	};
/*
	#define tantab_l (win[Encoder.SHORT_TYPE]+3)
	#define cx (win[Encoder.SHORT_TYPE]+12)
	#define ca (win[Encoder.SHORT_TYPE]+20)
	#define cs (win[Encoder.SHORT_TYPE]+28)
*/
/************************************************************************
*
* window_subband()
*
* PURPOSE:  Overlapping window on PCM samples
*
* SEMANTICS:
* 32 16-bit pcm samples are scaled to fractional 2's complement and
* concatenated to the end of the window buffer #x#. The updated window
* buffer #x# is then windowed by the analysis window #c# to produce the
* windowed sample #z#
*
************************************************************************/

	/** new IDCT routine written by Takehiro TOMINAGA */
	private static final int order[] = {
		0, 1, 16, 17, 8, 9, 24, 25, 4, 5, 20, 21, 12, 13, 28, 29,
		2, 3, 18, 19, 10, 11, 26, 27, 6, 7, 22, 23, 14, 15, 30, 31
	};

	/** returns sum_j=0^31 a[j]*cos(PI*j*(k+1/2)/32), 0<=k<32 */
	private static final void window_subband(final float[] x1, final int xoffset, final float a[/* SBLIMIT */]) {
		int wp = 10;// enwindow[wp]

		int x1i = xoffset;// java x1[x1i]
		int x2i = xoffset + 238 - 14 - 286;// x1[x2i]

		for( int i = -15 * 2; i < 0; i += 2 ) {

			float w = enwindow[wp + -10];
			float s = x1[x2i + -224] * w;
			float t = x1[x1i + 224] * w;
			w = enwindow[wp + -9];
			s += x1[x2i + -160] * w;
			t += x1[x1i + 160] * w;
			w = enwindow[wp + -8];
			s += x1[x2i + -96] * w;
			t += x1[x1i + 96] * w;
			w = enwindow[wp + -7];
			s += x1[x2i + -32] * w;
			t += x1[x1i + 32] * w;
			w = enwindow[wp + -6];
			s += x1[x2i + 32] * w;
			t += x1[x1i + -32] * w;
			w = enwindow[wp + -5];
			s += x1[x2i + 96] * w;
			t += x1[x1i + -96] * w;
			w = enwindow[wp + -4];
			s += x1[x2i + 160] * w;
			t += x1[x1i + -160] * w;
			w = enwindow[wp + -3];
			s += x1[x2i + 224] * w;
			t += x1[x1i + -224] * w;

			w = enwindow[wp + -2];
			s += x1[x1i + -256] * w;
			t -= x1[x2i + 256] * w;
			w = enwindow[wp + -1];
			s += x1[x1i + -192] * w;
			t -= x1[x2i + 192] * w;
			w = enwindow[wp + 0];
			s += x1[x1i + -128] * w;
			t -= x1[x2i + 128] * w;
			w = enwindow[wp + 1];
			s += x1[x1i + -64] * w;
			t -= x1[x2i + 64] * w;
			w = enwindow[wp + 2];
			s += x1[x1i + 0] * w;
			t -= x1[x2i + 0] * w;
			w = enwindow[wp + 3];
			s += x1[x1i + 64] * w;
			t -= x1[x2i + -64] * w;
			w = enwindow[wp + 4];
			s += x1[x1i + 128] * w;
			t -= x1[x2i + -128] * w;
			w = enwindow[wp + 5];
			s += x1[x1i + 192] * w;
			t -= x1[x2i + -192] * w;

			/*
			 * this multiplyer could be removed, but it needs more 256 FLOAT data.
			 * thinking about the data cache performance, I think we should not
			 * use such a huge table. tt 2000/Oct/25
			 */
			s *= enwindow[wp + 6];
			w = t - s;
			a[30 + i] = t + s;
			a[31 + i] = enwindow[wp + 7] * w;
			wp += 18;
			x1i--;
			x2i++;
		}
		{
			float t = x1[x1i + -16] * enwindow[wp + -10];
			float s = x1[x1i + -32] * enwindow[wp + -2];
			t += (x1[x1i + -48] - x1[x1i + 16]) * enwindow[wp + -9];
			s += x1[x1i + -96] * enwindow[wp + -1];
			t += (x1[x1i + -80] + x1[x1i + 48]) * enwindow[wp + -8];
			s += x1[x1i + -160] * enwindow[wp + 0];
			t += (x1[x1i + -112] - x1[x1i + 80]) * enwindow[wp + -7];
			s += x1[x1i + -224] * enwindow[wp + 1];
			t += (x1[x1i + -144] + x1[x1i + 112]) * enwindow[wp + -6];
			s -= x1[x1i + 32] * enwindow[wp + 2];
			t += (x1[x1i + -176] - x1[x1i + 144]) * enwindow[wp + -5];
			s -= x1[x1i + 96] * enwindow[wp + 3];
			t += (x1[x1i + -208] + x1[x1i + 176]) * enwindow[wp + -4];
			s -= x1[x1i + 160] * enwindow[wp + 4];
			t += (x1[x1i + -240] - x1[x1i + 208]) * enwindow[wp + -3];
			s -= x1[x1i + 224];

			final float u = s - t;
			final float v = s + t;

			t = a[14];
			s = a[15] - t;

			a[31] = v + t;  /* A0 */
			a[30] = u + s;  /* A1 */
			a[15] = u - s;  /* A2 */
			a[14] = v - t;  /* A3 */
		}
		{
			float xr = a[28] - a[0];
			a[0] += a[28];
			a[28] = xr * enwindow[wp + -2 * 18 + 7];
			xr = a[29] - a[1];
			a[1] += a[29];
			a[29] = xr * enwindow[wp + -2 * 18 + 7];

			xr = a[26] - a[2];
			a[2] += a[26];
			a[26] = xr * enwindow[wp + -4 * 18 + 7];
			xr = a[27] - a[3];
			a[3] += a[27];
			a[27] = xr * enwindow[wp + -4 * 18 + 7];

			xr = a[24] - a[4];
			a[4] += a[24];
			a[24] = xr * enwindow[wp + -6 * 18 + 7];
			xr = a[25] - a[5];
			a[5] += a[25];
			a[25] = xr * enwindow[wp + -6 * 18 + 7];

			xr = a[22] - a[6];
			a[6] += a[22];
			a[22] = xr * Util.SQRT2;
			xr = a[23] - a[7];
			a[7] += a[23];
			a[23] = xr * Util.SQRT2 - a[7];
			a[7] -= a[6];
			a[22] -= a[7];
			a[23] -= a[22];

			xr = a[6];
			a[6] = a[31] - xr;
			a[31] = a[31] + xr;
			xr = a[7];
			a[7] = a[30] - xr;
			a[30] = a[30] + xr;
			xr = a[22];
			a[22] = a[15] - xr;
			a[15] = a[15] + xr;
			xr = a[23];
			a[23] = a[14] - xr;
			a[14] = a[14] + xr;

			xr = a[20] - a[8];
			a[8] += a[20];
			a[20] = xr * enwindow[wp + -10 * 18 + 7];
			xr = a[21] - a[9];
			a[9] += a[21];
			a[21] = xr * enwindow[wp + -10 * 18 + 7];

			xr = a[18] - a[10];
			a[10] += a[18];
			a[18] = xr * enwindow[wp + -12 * 18 + 7];
			xr = a[19] - a[11];
			a[11] += a[19];
			a[19] = xr * enwindow[wp + -12 * 18 + 7];

			xr = a[16] - a[12];
			a[12] += a[16];
			a[16] = xr * enwindow[wp + -14 * 18 + 7];
			xr = a[17] - a[13];
			a[13] += a[17];
			a[17] = xr * enwindow[wp + -14 * 18 + 7];

			xr = -a[20] + a[24];
			a[20] += a[24];
			a[24] = xr * enwindow[wp + -12 * 18 + 7];
			xr = -a[21] + a[25];
			a[21] += a[25];
			a[25] = xr * enwindow[wp + -12 * 18 + 7];

			xr = a[4] - a[8];
			a[4] += a[8];
			a[8] = xr * enwindow[wp + -12 * 18 + 7];
			xr = a[5] - a[9];
			a[5] += a[9];
			a[9] = xr * enwindow[wp + -12 * 18 + 7];

			xr = a[0] - a[12];
			a[0] += a[12];
			a[12] = xr * enwindow[wp + -4 * 18 + 7];
			xr = a[1] - a[13];
			a[1] += a[13];
			a[13] = xr * enwindow[wp + -4 * 18 + 7];
			xr = a[16] - a[28];
			a[16] += a[28];
			a[28] = xr * enwindow[wp + -4 * 18 + 7];
			xr = -a[17] + a[29];
			a[17] += a[29];
			a[29] = xr * enwindow[wp + -4 * 18 + 7];

			xr = Util.SQRT2 * (a[2] - a[10]);
			a[2] += a[10];
			a[10] = xr;
			xr = Util.SQRT2 * (a[3] - a[11]);
			a[3] += a[11];
			a[11] = xr;
			xr = Util.SQRT2 * (-a[18] + a[26]);
			a[18] += a[26];
			a[26] = xr - a[18];
			xr = Util.SQRT2 * (-a[19] + a[27]);
			a[19] += a[27];
			a[27] = xr - a[19];

			xr = a[2];
			a[19] -= a[3];
			a[3] -= xr;
			a[2] = a[31] - xr;
			a[31] += xr;
			xr = a[3];
			a[11] -= a[19];
			a[18] -= xr;
			a[3] = a[30] - xr;
			a[30] += xr;
			xr = a[18];
			a[27] -= a[11];
			a[19] -= xr;
			a[18] = a[15] - xr;
			a[15] += xr;

			xr = a[19];
			a[10] -= xr;
			a[19] = a[14] - xr;
			a[14] += xr;
			xr = a[10];
			a[11] -= xr;
			a[10] = a[23] - xr;
			a[23] += xr;
			xr = a[11];
			a[26] -= xr;
			a[11] = a[22] - xr;
			a[22] += xr;
			xr = a[26];
			a[27] -= xr;
			a[26] = a[7] - xr;
			a[7] += xr;

			xr = a[27];
			a[27] = a[6] - xr;
			a[6] += xr;

			xr = Util.SQRT2 * (a[0] - a[4]);
			a[0] += a[4];
			a[4] = xr;
			xr = Util.SQRT2 * (a[1] - a[5]);
			a[1] += a[5];
			a[5] = xr;
			xr = Util.SQRT2 * (a[16] - a[20]);
			a[16] += a[20];
			a[20] = xr;
			xr = Util.SQRT2 * (a[17] - a[21]);
			a[17] += a[21];
			a[21] = xr;

			xr = -Util.SQRT2 * (a[8] - a[12]);
			a[8] += a[12];
			a[12] = xr - a[8];
			xr = -Util.SQRT2 * (a[9] - a[13]);
			a[9] += a[13];
			a[13] = xr - a[9];
			xr = -Util.SQRT2 * (a[25] - a[29]);
			a[25] += a[29];
			a[29] = xr - a[25];
			xr = -Util.SQRT2 * (a[24] + a[28]);
			a[24] -= a[28];
			a[28] = xr - a[24];

			xr = a[24] - a[16];
			a[24] = xr;
			xr = a[20] - xr;
			a[20] = xr;
			xr = a[28] - xr;
			a[28] = xr;

			xr = a[25] - a[17];
			a[25] = xr;
			xr = a[21] - xr;
			a[21] = xr;
			xr = a[29] - xr;
			a[29] = xr;

			xr = a[17] - a[1];
			a[17] = xr;
			xr = a[9] - xr;
			a[9] = xr;
			xr = a[25] - xr;
			a[25] = xr;
			xr = a[5] - xr;
			a[5] = xr;
			xr = a[21] - xr;
			a[21] = xr;
			xr = a[13] - xr;
			a[13] = xr;
			xr = a[29] - xr;
			a[29] = xr;

			xr = a[1] - a[0];
			a[1] = xr;
			xr = a[16] - xr;
			a[16] = xr;
			xr = a[17] - xr;
			a[17] = xr;
			xr = a[8] - xr;
			a[8] = xr;
			xr = a[9] - xr;
			a[9] = xr;
			xr = a[24] - xr;
			a[24] = xr;
			xr = a[25] - xr;
			a[25] = xr;
			xr = a[4] - xr;
			a[4] = xr;
			xr = a[5] - xr;
			a[5] = xr;
			xr = a[20] - xr;
			a[20] = xr;
			xr = a[21] - xr;
			a[21] = xr;
			xr = a[12] - xr;
			a[12] = xr;
			xr = a[13] - xr;
			a[13] = xr;
			xr = a[28] - xr;
			a[28] = xr;
			xr = a[29] - xr;
			a[29] = xr;

			xr = a[0];
			a[0] += a[31];
			a[31] -= xr;
			xr = a[1];
			a[1] += a[30];
			a[30] -= xr;
			xr = a[16];
			a[16] += a[15];
			a[15] -= xr;
			xr = a[17];
			a[17] += a[14];
			a[14] -= xr;
			xr = a[8];
			a[8] += a[23];
			a[23] -= xr;
			xr = a[9];
			a[9] += a[22];
			a[22] -= xr;
			xr = a[24];
			a[24] += a[7];
			a[7] -= xr;
			xr = a[25];
			a[25] += a[6];
			a[6] -= xr;
			xr = a[4];
			a[4] += a[27];
			a[27] -= xr;
			xr = a[5];
			a[5] += a[26];
			a[26] -= xr;
			xr = a[20];
			a[20] += a[11];
			a[11] -= xr;
			xr = a[21];
			a[21] += a[10];
			a[10] -= xr;
			xr = a[12];
			a[12] += a[19];
			a[19] -= xr;
			xr = a[13];
			a[13] += a[18];
			a[18] -= xr;
			xr = a[28];
			a[28] += a[3];
			a[3] -= xr;
			xr = a[29];
			a[29] += a[2];
			a[2] -= xr;
		}
	}

	/**------------------------------------------------------------------*
	/*                                                                   *
	/*   Function: Calculation of the MDCT                               *
	/*   In the case of long blocks (type 0,1,3) there are               *
	/*   36 coefficents in the time domain and 18 in the frequency       *
	/*   domain.                                                         *
	/*   In the case of short blocks (type 2) there are 3                *
	/*   transformations with short length. This leads to 12 coefficents *
	/*   in the time and 6 in the frequency domain. In this case the     *
	/*   results are stored side by side in the vector out[].            *
	/*                                                                   *
	/*   New layer3                                                      *
	/*                                                                   *
	/*-------------------------------------------------------------------*/
	private static final void mdct_short(final float[] inout, int offset) {
		for( int l = 0; l < 3; l++ ) {
			float ts0 = inout[offset + 2 * 3] * win[Encoder.SHORT_TYPE][0] - inout[offset + 5 * 3];
			float tc0 = inout[offset + 0 * 3] * win[Encoder.SHORT_TYPE][2] - inout[offset + 3 * 3];
			float tc1 = ts0 + tc0;
			float tc2 = ts0 - tc0;

			ts0 = inout[offset + 5 * 3] * win[Encoder.SHORT_TYPE][0] + inout[offset + 2 * 3];
			tc0 = inout[offset + 3 * 3] * win[Encoder.SHORT_TYPE][2] + inout[offset + 0 * 3];
			float ts1 = ts0 + tc0;
			float ts2 = -ts0 + tc0;

			tc0 = (inout[offset + 1 * 3] * win[Encoder.SHORT_TYPE][1] - inout[offset + 4 * 3]) * 2.069978111953089e-11f; /* tritab_s[1] */
			ts0 = (inout[offset + 4 * 3] * win[Encoder.SHORT_TYPE][1] + inout[offset + 1 * 3]) * 2.069978111953089e-11f; /* tritab_s[1] */

			inout[offset + 3 * 0] = tc1 * 1.907525191737280e-11f /* tritab_s[2] */  + tc0;
			inout[offset + 3 * 5] = -ts1 * 1.907525191737280e-11f /* tritab_s[0] */  + ts0;

			tc2 = tc2 * 0.86602540378443870761f * 1.907525191737281e-11f /* tritab_s[2] */ ;
			ts1 = ts1 * 0.5f * 1.907525191737281e-11f + ts0;
			inout[offset + 3 * 1] = tc2 - ts1;
			inout[offset + 3 * 2] = tc2 + ts1;

			tc1 = tc1 * 0.5f * 1.907525191737281e-11f - tc0;
			ts2 = ts2 * 0.86602540378443870761f * 1.907525191737281e-11f /* tritab_s[0] */ ;
			inout[offset + 3 * 3] = tc1 + ts2;
			inout[offset + 3 * 4] = tc1 - ts2;

			offset++;
		}
	}

	private static final void mdct_long(final float[] out, final int outoffset, final float[] in) {
		{
			/* 1,2, 5,6, 9,10, 13,14, 17 */
			final float tc1 = in[17] - in[9];
			final float tc3 = in[15] - in[11];
			final float tc4 = in[14] - in[12];
			final float ts5 = in[0] + in[8];
			float ts6 = in[1] + in[7];
			final float ts7 = in[2] + in[6];
			final float ts8 = in[3] + in[5];

			out[outoffset + 17] = (ts5 + ts7 - ts8) - (ts6 - in[4]);
			float st = (ts5 + ts7 - ts8) * win[Encoder.SHORT_TYPE][12+7] + (ts6 - in[4]);
			float ct = (tc1 - tc3 - tc4) * win[Encoder.SHORT_TYPE][12+6];
			out[outoffset + 5] = ct + st;
			out[outoffset + 6] = ct - st;

			final float tc2 = (in[16] - in[10]) * win[Encoder.SHORT_TYPE][12+6];
			ts6 = ts6 * win[Encoder.SHORT_TYPE][12+7] + in[4];
			ct = tc1 * win[Encoder.SHORT_TYPE][12+0] + tc2 + tc3 * win[Encoder.SHORT_TYPE][12+1] + tc4 * win[Encoder.SHORT_TYPE][12+2];
			st = -ts5 * win[Encoder.SHORT_TYPE][12+4] + ts6 - ts7 * win[Encoder.SHORT_TYPE][12+5] + ts8 * win[Encoder.SHORT_TYPE][12+3];
			out[outoffset + 1] = ct + st;
			out[outoffset + 2] = ct - st;

			ct = tc1 * win[Encoder.SHORT_TYPE][12+1] - tc2 - tc3 * win[Encoder.SHORT_TYPE][12+2] + tc4 * win[Encoder.SHORT_TYPE][12+0];
			st = -ts5 * win[Encoder.SHORT_TYPE][12+5] + ts6 - ts7 * win[Encoder.SHORT_TYPE][12+3] + ts8 * win[Encoder.SHORT_TYPE][12+4];
			out[outoffset + 9] = ct + st;
			out[outoffset + 10] = ct - st;

			ct = tc1 * win[Encoder.SHORT_TYPE][12+2] - tc2 + tc3 * win[Encoder.SHORT_TYPE][12+0] - tc4 * win[Encoder.SHORT_TYPE][12+1];
			st = ts5 * win[Encoder.SHORT_TYPE][12+3] - ts6 + ts7 * win[Encoder.SHORT_TYPE][12+4] - ts8 * win[Encoder.SHORT_TYPE][12+5];
			out[outoffset + 13] = ct + st;
			out[outoffset + 14] = ct - st;
		}
		{
			final float ts1 = in[8] - in[0];
			final float ts3 = in[6] - in[2];
			final float ts4 = in[5] - in[3];
			final float tc5 = in[17] + in[9];
			float tc6 = in[16] + in[10];
			final float tc7 = in[15] + in[11];
			final float tc8 = in[14] + in[12];

			out[outoffset + 0] = (tc5 + tc7 + tc8) + (tc6 + in[13]);
			float ct = (tc5 + tc7 + tc8) * win[Encoder.SHORT_TYPE][12+7] - (tc6 + in[13]);
			float st = (ts1 - ts3 + ts4) * win[Encoder.SHORT_TYPE][12+6];
			out[outoffset + 11] = ct + st;
			out[outoffset + 12] = ct - st;

			final float ts2 = (in[7] - in[1]) * win[Encoder.SHORT_TYPE][12+6];
			tc6 = in[13] - tc6 * win[Encoder.SHORT_TYPE][12+7];
			ct = tc5 * win[Encoder.SHORT_TYPE][12+3] - tc6 + tc7 * win[Encoder.SHORT_TYPE][12+4] + tc8 * win[Encoder.SHORT_TYPE][12+5];
			st = ts1 * win[Encoder.SHORT_TYPE][12+2] + ts2 + ts3 * win[Encoder.SHORT_TYPE][12+0] + ts4 * win[Encoder.SHORT_TYPE][12+1];
			out[outoffset + 3] = ct + st;
			out[outoffset + 4] = ct - st;

			ct = -tc5 * win[Encoder.SHORT_TYPE][12+5] + tc6 - tc7 * win[Encoder.SHORT_TYPE][12+3] - tc8 * win[Encoder.SHORT_TYPE][12+4];
			st = ts1 * win[Encoder.SHORT_TYPE][12+1] + ts2 - ts3 * win[Encoder.SHORT_TYPE][12+2] - ts4 * win[Encoder.SHORT_TYPE][12+0];
			out[outoffset + 7] = ct + st;
			out[outoffset + 8] = ct - st;

			ct = -tc5 * win[Encoder.SHORT_TYPE][12+4] + tc6 - tc7 * win[Encoder.SHORT_TYPE][12+5] - tc8 * win[Encoder.SHORT_TYPE][12+3];
			st = ts1 * win[Encoder.SHORT_TYPE][12+0] - ts2 + ts3 * win[Encoder.SHORT_TYPE][12+1] - ts4 * win[Encoder.SHORT_TYPE][12+2];
			out[outoffset + 15] = ct + st;
			out[outoffset + 16] = ct - st;
		}
	}

	private static final float wshort[] = win[Encoder.SHORT_TYPE];// java: tantal_l,ca,cs

	static final void mdct_sub48(final LAME_InternalFlags gfc, final float[] w0, final float[] w1) {
		final SessionConfig cfg = gfc.cfg;
		final EncStateVar esv = gfc.sv_enc;
		float[] wa = w0;// java
		int wk = 286;// w[wk]
		final III_GrInfo[][] tt = gfc.l3_side.tt;// java
		final float[][][][] esv_sb_sample = esv.sb_sample;// java
		final float[] amp_filter = esv.amp_filter;// java
		/* thinking cache performance, ch.gr loop is better than gr.ch loop */
		final int channels_out = cfg.channels_out;// java
		final int mode_gr = cfg.mode_gr;// java
		int ch = 0;
		do {
			final float[][][] esv_sb_sample_ch = esv_sb_sample[ch];// java
			for( int gr = 0; gr < mode_gr; gr++ ) {
				final III_GrInfo gi = tt[gr][ch];
				final float[] xr = gi.xr;// java
				int mdct_enc = 0;// xr[mdct_enc]
				final float[][] sb_sample = esv_sb_sample_ch[1 - gr];// java
				int samp = 0;// sb_sample[samp]

				int k = 0;
				do {
					//window_subband( wk, samp );
					//window_subband( wk + 32, samp + 32 );// FIXME dirty way for navigation on 2-dim array
					//samp += 64;
					window_subband( wa, wk, sb_sample[ samp ] );
					window_subband( wa, wk + 32, sb_sample[ ++samp ] );
					final float[] sb_sample1 = sb_sample[samp++];// java sb_sample[ samp - 1 ]
					wk += 64;
					/* Compensate for inversion in the analysis filter */
					int band = 1;
					do {
						//samp[band - 32] *= -1;// FIXME dirty way for navigation on 2-dim array
						sb_sample1[band] = -sb_sample1[band];
						band += 2;
					} while( band < 32 );
				} while( ++k < 18 / 2 );

				/*
				 * Perform imdct of 18 previous subband samples
				 * + 18 current subband samples
				 */
				for( int band = 0; band < 32; band++, mdct_enc += 18 ) {
					int type = gi.block_type;
					final int off = order[band];// java
					final float[][] band0 = esv_sb_sample_ch[gr];
					final float[][] band1 = esv_sb_sample_ch[1 - gr];
					if( gi.mixed_block_flag && band < 2 ) {
						type = 0;
					}
					if( amp_filter[band] < 1e-12f ) {
						int i = mdct_enc + 18;
						do {
							xr[--i] = 0;
						} while( i > mdct_enc );
					} else {
						if( amp_filter[band] < 1.0f ) {
							k = 0;
							do {
								// band1[k * 32] *= esv->amp_filter[band];// FIXME dirty way for navigation on 2-dim array
								band1[k][off] *= amp_filter[band];
							} while( ++k < 18 );
						}
						if( type == Encoder.SHORT_TYPE ) {
							int k3 = mdct_enc + (-NS / 4) * 3;
							for( k = -NS / 4; k < 0; k++, k3 += 3 ) {// FIXME dirty way for navigation on 2-dim arrays band0 and band1
								final float w = wshort[k + 3];
								xr[k3 + 9] = band0[9 + k][off] * w - band0[8 - k][off];
								xr[k3 + 18] = band0[14 - k][off] * w + band0[15 + k][off];
								xr[k3 + 10] = band0[15 + k][off] * w - band0[14 - k][off];
								xr[k3 + 19] = band1[2 - k][off] * w + band1[3 + k][off];
								xr[k3 + 11] = band1[3 + k][off] * w - band1[2 - k][off];
								xr[k3 + 20] = band1[8 - k][off] * w + band1[9 + k][off];
							}
							mdct_short( xr, mdct_enc );
						} else {
							final float[] wt = win[type];// java
							final float work[] = new float[18];
							k = -NL / 4;
							do {// FIXME dirty way for navigation on 2-dim arrays band0 and band1
								final float a = wt[k + 27] * band1[k + 9][off]
										+ wt[k + 36] * band1[8 - k][off];
								final float b = wt[k + 9] * band0[k + 9][off]
										- wt[k + 18] * band0[8 - k][off];
								final float w = wshort[3 + k + 9];
								work[k + 9] = a - b * w;
								work[k + 18] = a * w + b;
							} while( ++k < 0 );

							mdct_long( xr, mdct_enc, work );
						}
					}
					/* Perform aliasing reduction butterfly */
					if( type != Encoder.SHORT_TYPE && band != 0 ) {
						k = 7;
						do {
							final float w20 = wshort[20 + k];
							final float w28 = wshort[28 + k];
							final float bu = xr[mdct_enc + k] * w20 + xr[mdct_enc + -1 - k] * w28;
							final float bd = xr[mdct_enc + k] * w28 - xr[mdct_enc + -1 - k] * w20;

							xr[mdct_enc + -1 - k] = bu;
							xr[mdct_enc + k] = bd;
						} while( --k >= 0 );
					}
				}
			}
			wa = w1;
			wk = 286;
			if( mode_gr == 1 ) {
				// memcpy(esv.sb_sample[ch][0], esv.sb_sample[ch][1], 576 * sizeof(FLOAT));// FIXME dirty way to copy 2-dim array
				final float[][] dst = esv_sb_sample_ch[0];
				final float[][] src = esv_sb_sample_ch[1];
				int i = 17;// 18 * Encoder.SBLIMIT = 576
				do {
					System.arraycopy( src[i], 0, dst[i], 0, Encoder.SBLIMIT );
				} while( --i >= 0 );
			}
		} while( ++ch < channels_out );
	}
}