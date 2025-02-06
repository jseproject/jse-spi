package davaguine.jmac.util;

/**
 * Contains internal state of the MD5 class
 */

class MD5State {
    /**
     * 128-byte state
     */
    int state[];

    /**
     * 64-bit character count (could be true Java long?)
     */
    int count[];

    /**
     * 64-byte buffer (512 bits) for storing to-be-hashed characters
     */
    byte buffer[];

    public MD5State() {
        buffer = new byte[64];
        count = new int[2];
        state = new int[4];

        state[0] = 0x67452301;
        state[1] = 0xefcdab89;
        state[2] = 0x98badcfe;
        state[3] = 0x10325476;

        count[0] = count[1] = 0;
    }

    /**
     * Create this State as a copy of another state
     */
    public MD5State(MD5State from) {
        this();

        int i;

        for (i = 0; i < buffer.length; i++)
            this.buffer[i] = from.buffer[i];

        for (i = 0; i < state.length; i++)
            this.state[i] = from.state[i];

        for (i = 0; i < count.length; i++)
            this.count[i] = from.count[i];
    }
}
