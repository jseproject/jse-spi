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

/*!
  \file   version.c
  \brief  Version numbering for LAME.

  Contains functions which describe the version of LAME.

  \author A.L. Faber
  \ingroup libmp3lame
*/


// version.c

public class LAME_Version {
	private static final String LAME_URL = "http://lame.sf.net";

	private static final int LAME_MAJOR_VERSION =  3; /* Major version number */
	private static final int LAME_MINOR_VERSION = 100; /* Minor version number */
	// private static final int LAME_TYPE_VERSION  =  2; /* 0:alpha 1:beta 2:release */
	private static final int LAME_PATCH_VERSION =  0; /* Patch level */

	private static final int PSY_MAJOR_VERSION  = 1; /* Major version number */
	private static final int PSY_MINOR_VERSION  = 0; /* Minor version number */

	/* generic LAME version */
	private int major;
	private int minor;
	//int alpha;               /* 0 if not an alpha version                  */
	//int beta;                /* 0 if not a beta version                    */

	/* version of the psy model */
	private int psy_major;
	private int psy_minor;
	//int psy_alpha;           /* 0 if not an alpha version                  */
	//int psy_beta;            /* 0 if not a beta version                    */

	/* compile time features */
	// String features;    /* Don't make assumptions about the contents! */

	/** Get the LAME version string.
	 *
	 * @return a pointer to a string which describes the version of LAME.
	 */
	public static final String get_lame_version() {
		/* primary to write screen reports */
		/* Here we can also add informations about compile time configurations */

		return new StringBuilder().append( LAME_MAJOR_VERSION ).append('.').append( LAME_MINOR_VERSION ).append('.').append( LAME_PATCH_VERSION ).toString();
	}

	/** Get the short LAME version string.
	 * It's mainly for inclusion into the MP3 stream.
	 *
	 * @return a pointer to the short version of the LAME version string.
	 */
	static final String get_lame_short_version() {
		/* adding date and time to version string makes it harder for output
		validation */
		return new StringBuilder().append( LAME_MAJOR_VERSION ).append('.').append( LAME_MINOR_VERSION ).append('.').append( LAME_PATCH_VERSION ).toString();
	}

	/** Get the _very_ short LAME version string.
	/* It's used in the LAME VBR tag only.
	 *
	 * @return a pointer to the short version of the LAME version string.
	 */
	static final String get_lame_very_short_version() {
	    /* adding date and time to version string makes it harder for output
	       validation */
		final StringBuilder sb = new StringBuilder("LAME").append( LAME_MAJOR_VERSION ).append('.').append( LAME_MINOR_VERSION );
		if( LAME_PATCH_VERSION > 0 ) {
			sb.append('r').append( LAME_PATCH_VERSION );
			return sb.toString();
		}
		sb.append(' ');
		return sb.toString();
	}

	/** Get the _very_ short LAME version string.
	/* It's used in the LAME VBR tag only, limited to 9 characters max.
	 * Due to some 3rd party HW/SW decoders, it has to start with LAME.
	 *
	 * @return a pointer to the short version of the LAME version string.
	 */
	static final String get_lame_tag_encoder_short_version() {
		final StringBuilder sb = new StringBuilder("LAME").append( LAME_MAJOR_VERSION ).append('.').append( LAME_MINOR_VERSION );
		if( LAME_PATCH_VERSION > 0 ) {
			sb.append('r');
		}
		return sb.toString();
	}

	/** Get the version string for GPSYCHO.
	 *
	 * @return a pointer to a string which describes the version of GPSYCHO.
	 */
	static final String get_psy_version() {
		return new StringBuilder().append( PSY_MAJOR_VERSION ).append('.').append( PSY_MINOR_VERSION ).toString();
	}

	/** Get the URL for the LAME website.
	 * @return a pointer to a string which is a URL for the LAME website.
	 */
	public static final String get_lame_url() {
		return LAME_URL;
	}

	/** Get the numerical representation of the version.
	 *  Writes the numerical representation of the version of LAME and GPSYCHO into lvp.
	 * @param lvp
	 */
	public static final void get_lame_version_numerical(final LAME_Version lvp) {
		//String features = ""; /* obsolete */

		/* generic version */
		lvp.major = LAME_MAJOR_VERSION;
		lvp.minor = LAME_MINOR_VERSION;

		//lvp.alpha = 0;
		//lvp.beta = 0;

		/* psy version */
		lvp.psy_major = PSY_MAJOR_VERSION;
		lvp.psy_minor = PSY_MINOR_VERSION;
		//lvp.psy_alpha = PSY_ALPHA_VERSION;
		//lvp.psy_beta = PSY_BETA_VERSION;

		/* compile time features */
		/*@-mustfree@ */
		//lvp.features = features;
		/*@=mustfree@ */
	}

	public static final String get_lame_os_bitness() {
		/*
		final String arch = System.getenv("PROCESSOR_ARCHITECTURE");
		final String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");

		return arch.endsWith("64")
		                  || wow64Arch != null && wow64Arch.endsWith("64")
		                      ? "64bits" : "32bits";
		 */
		final String bitmode = System.getProperty("sun.arch.data.model", System.getProperty("com.ibm.vm.bitmode"));
		final String version = System.getProperty("java.vm.version", "");
		return bitmode == null ? (version.contains("_64") ? "64bits" : "32bits") : (bitmode.equals("64") ? "64bits" : "32bits");
	}

}