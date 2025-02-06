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
