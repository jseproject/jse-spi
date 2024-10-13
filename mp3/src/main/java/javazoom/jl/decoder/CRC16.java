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

package javazoom.jl.decoder;

/**
 * 16-Bit CRC checksum
 */
public final class CRC16
{
  private static	short polynomial=(short)0x8005;
  private			short crc;

  /**
   * Dummy Constructor
   */
  public CRC16()
  {
  	crc = (short) 0xFFFF;
  }

  /**
   * Feed a bitstring to the crc calculation (0 < length <= 32).
   */
  public void add_bits (int bitstring, int length)
  {
  	int bitmask = 1 << (length - 1);
  	do
	 if (((crc & 0x8000) == 0) ^ ((bitstring & bitmask) == 0 ))
	 {
		crc <<= 1;
		crc ^= polynomial;
	 }
	 else
		crc <<= 1;
  	while ((bitmask >>>= 1) != 0);
  }

  /**
   * Return the calculated checksum.
   * Erase it for next calls to add_bits().
   */
  public short	checksum()
  {
    short sum = crc;
    crc = (short) 0xFFFF;
    return sum;
  }

}
