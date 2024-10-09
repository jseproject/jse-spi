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
 * vorbis_block is a single block of data to be processed as part of
 * the analysis/synthesis stream; it belongs to a specific logical
 * bitstream, but is independent from other vorbis_blocks belonging to
 * that logical bitstream.
 */
public class Block {
	/* used only to control memory
	private class AllocChain {
		private byte[] ptr;
		private AllocChain next;
	}*/
    /* necessary stream state for linking to the framing abstraction */
    /**
     * this is a pointer into local storage
     */
    float[][] pcm = null;
    final Buffer opb = new Buffer();

    int lW = 0;
    int W = 0;
    int nW = 0;
    int pcmend = 0;
    int mode = 0;

    boolean eofflag = false;
    long granulepos = 0;
    long sequence = 0;
    /**
     * For read-only access of configuration
     */
    DspState vd = null;

	/* local storage to avoid remallocing; it's up to the mapping to
	   structure it */
	/* used only to control memory
	private byte[] localstore = null;
	private int localtop   = 0;
	private int localalloc = 0;
	private int totaluse   = 0;
	private AllocChain reap = null;*/

    /* bitmetrics for the frame */
    int glue_bits = 0;
    int time_bits = 0;
    int floor_bits = 0;
    int res_bits = 0;

    /**
     * public void *internal;
     */
    BlockInternal m_internal = null;

    /* block abstraction setup *********************************************/
/*
	//#ifndef WORD_ALIGN
	//#define WORD_ALIGN 8
	private static final int WORD_ALIGN = 8;
	//#endif
	final int _alloc(int bytes) {
		bytes = (bytes + (WORD_ALIGN - 1)) & ~(WORD_ALIGN - 1);
		if( bytes + this.m_localtop > this.m_localalloc ) {
			// can't just _ogg_realloc... there are outstanding pointers
			if( this.m_localstore != null ) {
				AllocChain link = new AllocChain();
				this.m_totaluse += this.m_localtop;
				link.next = this.m_reap;
				//link.ptr = this.m_localstore;
				link.ptr = new byte[ this.m_localtop ];
				System.arraycopy( this.m_localstore, 0, link.ptr, 0, this.m_localtop );
				this.m_reap = link;
			}
			// highly conservative
			this.m_localalloc = bytes;
			this.m_localstore = new byte[this.m_localalloc];
			this.m_localtop = 0;
		}
		{
			//void *ret = (void *)(((char *)vb.localstore) + vb.localtop);
			int ret = this.m_localtop;
			this.m_localtop += bytes;
			return ret;
		}
	}//
*/

    /**
     * reap the chain, pull the ripcord
     */
	/*final void _ripcord() {
		// reap the chain
		AllocChain reap = this.m_reap;
		while( reap != null ) {
			AllocChain next = this.m_reap.next;
			//_ogg_free( reap.ptr );
			//memset( reap,0,sizeof(*reap) );
			//_ogg_free( reap );
			reap = next;
		}
		// consolidate storage
		if( this.m_totaluse != 0 ) {
			this.m_localstore = Arrays.copyOf( this.m_localstore, this.m_totaluse + this.m_localalloc );
			this.m_localalloc += this.m_totaluse;
			this.m_totaluse = 0;
		}

		// pull the ripcord
		this.m_localtop = 0;
		this.m_reap = null;
	}
*/
    public final int clear() {
        //int i;
        //BlockInternal vbi = this.m_internal;

        //_ripcord();
        //this.m_localstore = null;

        //if( vbi != null ) {
        //	for( i = 0; i < BlockInternal.PACKETBLOBS; i++ ) {
        //		Buffer.pack_writeclear( vbi.packetblob[i] );
        //		if( i != BlockInternal.PACKETBLOBS / 2 ) vbi.packetblob[i] = null;
        //	}
        //vbi = null;
        //}
        pcm = null;
        opb.clear();

        lW = 0;
        W = 0;
        nW = 0;
        pcmend = 0;
        mode = 0;

        eofflag = false;
        granulepos = 0;
        sequence = 0;

        vd = null;
		/*
		localstore = null;
		localtop   = 0;
		localalloc = 0;
		totaluse   = 0;
		reap       = null;
		 */
        /* bitmetrics for the frame */
        glue_bits = 0;
        time_bits = 0;
        floor_bits = 0;
        res_bits = 0;

        m_internal = null;
        return (0);
    }

    // analysis.c

    /**
     * decides between modes, dispatches to the appropriate mapping.
     */
    public final int analysis(final Packet op) {
        final int ret;
        // final BlockInternal vbi = this.m_internal;

        this.glue_bits = 0;
        this.time_bits = 0;
        this.floor_bits = 0;
        this.res_bits = 0;

        /* first things first.  Make sure encode is ready */
        final Buffer[] p = this.m_internal.packetblob;// java
        for (int i = 0; i < Info.PACKETBLOBS; i++) {
            p[i].pack_reset();
        }

		/* we only have one mapping type (0), and we let the mapping code
 		 itself figure out what soft mode to use.  This allows easier
 		 bitrate management */

        if ((ret = Codec._mapping_P[0].forward(this)) != 0) {
            return (ret);
        }

        if (op != null) {
            if (bitrate_managed()) {
				/* The app is using a bitmanaged mode... but not using the
 				 bitrate management interface. */
                return (Codec.OV_EINVAL);
            }

            op.packet_base = this.opb.pack_get_buffer();
            op.packet = 0;
            op.bytes = this.opb.pack_bytes();
            op.b_o_s = false;
            op.e_o_s = this.eofflag;
            op.granulepos = this.granulepos;
            op.packetno = this.sequence; /* for sake of completeness */
        }
        return (0);
    }

    // synthesis.c
    public final int synthesis(final Packet op) {
        final DspState vdsp = this.vd;
        final PrivateState b = vdsp != null ? vdsp.backend_state : null;
        final Info vi = vdsp != null ? vdsp.vi : null;
        final CodecSetupInfo ci = vi != null ? vi.codec_setup : null;
        final Buffer pb = this.opb;

        if (vdsp == null || b == null || vi == null || ci == null || pb == null) {
            return Codec.OV_EBADPACKET;
        }

        /* first things first.  Make sure decode is ready */
        //Block._ripcord( vb );
        pb.pack_readinit(op.packet_base, op.packet, op.bytes);

        /* Check the packet type */
        if (pb.pack_read(1) != 0) {
            /* Oops.  This is not an audio data packet */
            return (Codec.OV_ENOTAUDIO);
        }

        /* read our mode and pre/post windowsize */
        final int m = pb.pack_read(b.modebits);
        if (m == -1) {
            return (Codec.OV_EBADPACKET);
        }

        this.mode = m;
        final InfoMode mp = ci.mode_param[m];// java
        if (mp == null) {
            return (Codec.OV_EBADPACKET);
        }

        this.W = mp.blockflag;
        if (this.W != 0) {

			/* this doesn;t get mapped through mode selection as it's used
		   		only for window selection */
            this.lW = pb.pack_read(1);
            this.nW = pb.pack_read(1);
            if (this.nW == -1) {
                return (Codec.OV_EBADPACKET);
            }
        } else {
            this.lW = 0;
            this.nW = 0;
        }

        /* more setup */
        this.granulepos = op.granulepos;
        this.sequence = op.packetno;
        this.eofflag = op.e_o_s;

        /* alloc pcm passback storage */
        this.pcmend = ci.blocksizes[this.W];
        this.pcm = new float[vi.channels][this.pcmend];

        /* unpack_header enforces range checking */
        final int type = ci.map_type[mp.mapping];

        return (Codec._mapping_P[type].inverse(this, ci.map_param[mp.mapping]));
    }

    /**
     * used to track pcm position without actually performing decode.
     * Useful for sequential 'fast forward'
     */
    public final int synthesis_trackonly(final Packet op) {
        final DspState vdsp = this.vd;
        final PrivateState b = vdsp.backend_state;
        final Info vi = vdsp.vi;
        final CodecSetupInfo ci = vi.codec_setup;
        final Buffer pb = this.opb;

        /* first things first.  Make sure decode is ready */
        //Block._ripcord( vb );
        pb.pack_readinit(op.packet_base, op.packet, op.bytes);

        /* Check the packet type */
        if (pb.pack_read(1) != 0) {
            /* Oops.  This is not an audio data packet */
            return (Codec.OV_ENOTAUDIO);
        }

        /* read our mode and pre/post windowsize */
        final int m = pb.pack_read(b.modebits);
        if (m == -1) {
            return (Codec.OV_EBADPACKET);
        }

        this.mode = m;
        if (ci.mode_param[m] == null) {
            return (Codec.OV_EBADPACKET);
        }

        this.W = ci.mode_param[m].blockflag;
        if (this.W != 0) {
            this.lW = pb.pack_read(1);
            this.nW = pb.pack_read(1);
            if (this.nW == -1) {
                return (Codec.OV_EBADPACKET);
            }
        } else {
            this.lW = 0;
            this.nW = 0;
        }

        /* more setup */
        this.granulepos = op.granulepos;
        this.sequence = op.packetno;
        this.eofflag = op.e_o_s;

        /* no pcm */
        this.pcmend = 0;
        this.pcm = null;

        return (0);
    }

    // bitrate.c
    final boolean bitrate_managed() {
        // final DspState      vd = this.vd;
        // final PrivateState          b = vd.backend_state;
        final BitrateManagerState bm = this.vd.backend_state.bms;// b.bms;

        if (bm != null && bm.managed != 0) {
            return true;
        }
        return false;
    }

    /**
     * finish taking in the block we just processed
     */
    public final int bitrate_addblock() {
        final DspState vdsp = this.vd;
        final PrivateState b = vdsp.backend_state;
        final BitrateManagerState bm = b.bms;

        if (bm.managed == 0) {
            /* not a bitrate managed stream, but for API simplicity, we'll
             * buffer the packet to keep the code path clean */

            if (bm.vb != null) {
                return (-1); /* one has been submitted without being claimed */
            }
            bm.vb = this;
            return (0);
        }

        bm.vb = this;

        final Info vi = vdsp.vi;
        final CodecSetupInfo ci = vdsp.vi.codec_setup;
        final BitrateManagerInfo bi = ci.bi;

        int choice = (int) Math.rint(bm.avgfloat);
        final Buffer[] packetblob = this.m_internal.packetblob;
        int this_bits = packetblob[choice].pack_bytes() << 3;// * 8;
        final int min_target_bits = (this.W != 0 ? bm.min_bitsper * bm.short_per_long : bm.min_bitsper);
        final int max_target_bits = (this.W != 0 ? bm.max_bitsper * bm.short_per_long : bm.max_bitsper);
        final int samples = ci.blocksizes[this.W] >> 1;
        final int desired_fill = (int) (bi.reservoir_bits * bi.reservoir_bias);

        /* look ahead for avg floater */
        if (bm.avg_bitsper > 0) {
            double slew = 0.;
            final int avg_target_bits = (this.W != 0 ? bm.avg_bitsper * bm.short_per_long : bm.avg_bitsper);
            final double slewlimit = 15. / bi.slew_damp;

			/* choosing a new floater:
			   if we're over target, we slew down
			   if we're under target, we slew up

			   choose slew as follows: look through packetblobs of this frame
			   and set slew as the first in the appropriate direction that
			   gives us the slew we want.  This may mean no slew if delta is
			   already favorable.

			   Then limit slew to slew max */
            final int avg_reservoir = bm.avg_reservoir;// java
            if (avg_reservoir + (this_bits - avg_target_bits) > desired_fill) {
                while (choice > 0 && this_bits > avg_target_bits &&
                        avg_reservoir + (this_bits - avg_target_bits) > desired_fill) {
                    choice--;
                    this_bits = packetblob[choice].pack_bytes() << 3;// * 8;
                }
            } else if (avg_reservoir + (this_bits - avg_target_bits) < desired_fill) {
                while (choice + 1 < Info.PACKETBLOBS && this_bits < avg_target_bits &&
                        avg_reservoir + (this_bits - avg_target_bits) < desired_fill) {
                    choice++;
                    this_bits = packetblob[choice].pack_bytes() << 3;// * 8;
                }
            }

            slew = (int) (Math.rint(choice - bm.avgfloat) / samples * vi.rate);
            if (slew < -slewlimit) {
                slew = -slewlimit;
            }
            if (slew > slewlimit) {
                slew = slewlimit;
            }
            choice = (int) (Math.rint(bm.avgfloat += slew / vi.rate * samples));
            this_bits = packetblob[choice].pack_bytes() << 3;// * 8;
        }

        /* enforce min(if used) on the current floater (if used) */
        if (bm.min_bitsper > 0) {
            /* do we need to force the bitrate up? */
            if (this_bits < min_target_bits) {
                final int minmax_reservoir = bm.minmax_reservoir;// java
                while (minmax_reservoir - (min_target_bits - this_bits) < 0) {
                    choice++;
                    if (choice >= Info.PACKETBLOBS) {
                        break;
                    }
                    this_bits = packetblob[choice].pack_bytes() << 3;// * 8;
                }
            }
        }

        /* enforce max (if used) on the current floater (if used) */
        if (bm.max_bitsper > 0) {
            /* do we need to force the bitrate down? */
            if (this_bits > max_target_bits) {
                final int minmax_reservoir = bm.minmax_reservoir;// java
                while (minmax_reservoir + (this_bits - max_target_bits) > bi.reservoir_bits) {
                    choice--;
                    if (choice < 0) {
                        break;
                    }
                    this_bits = packetblob[choice].pack_bytes() << 3;// * 8;
                }
            }
        }

		/* Choice of packetblobs now made based on floater, and min/max
		   requirements. Now boundary check extreme choices */

        if (choice < 0) {
			/* choosing a smaller packetblob is insufficient to trim bitrate.
			   frame will need to be truncated */
            final int maxsize = (max_target_bits + (bi.reservoir_bits - bm.minmax_reservoir)) / 8;// java: can be negative, don't use >>
            bm.choice = choice = 0;

            if (packetblob[choice].pack_bytes() > maxsize) {

                packetblob[choice].pack_writetrunc(maxsize << 3);// * 8 );
                this_bits = packetblob[choice].pack_bytes() << 3;// * 8;
            }
        } else {
            int minsize = (min_target_bits - bm.minmax_reservoir + 7) / 8;// java: can be negative, don't use >>
            if (choice >= Info.PACKETBLOBS) {
                choice = Info.PACKETBLOBS - 1;
            }

            bm.choice = choice;

            /* prop up bitrate according to demand. pad this frame out with zeroes */
            minsize -= packetblob[choice].pack_bytes();
            while (minsize-- > 0) {
                packetblob[choice].pack_write(0, 8);
            }
            this_bits = packetblob[choice].pack_bytes() << 3;// * 8;

        }

        /* now we have the final packet and the final packet size.  Update statistics */
        /* min and max reservoir */
        if (bm.min_bitsper > 0 || bm.max_bitsper > 0) {

            if (max_target_bits > 0 && this_bits > max_target_bits) {
                bm.minmax_reservoir += (this_bits - max_target_bits);
            } else if (min_target_bits > 0 && this_bits < min_target_bits) {
                bm.minmax_reservoir += (this_bits - min_target_bits);
            } else {
                /* inbetween; we want to take reservoir toward but not past desired_fill */
                if (bm.minmax_reservoir > desired_fill) {
                    if (max_target_bits > 0) {/* logical bulletproofing against initialization state */
                        bm.minmax_reservoir += (this_bits - max_target_bits);
                        if (bm.minmax_reservoir < desired_fill) {
                            bm.minmax_reservoir = desired_fill;
                        }
                    } else {
                        bm.minmax_reservoir = desired_fill;
                    }
                } else {
                    if (min_target_bits > 0) { /* logical bulletproofing against initialization state */
                        bm.minmax_reservoir += (this_bits - min_target_bits);
                        if (bm.minmax_reservoir > desired_fill) {
                            bm.minmax_reservoir = desired_fill;
                        }
                    } else {
                        bm.minmax_reservoir = desired_fill;
                    }
                }
            }
        }

        /* avg reservoir */
        if (bm.avg_bitsper > 0) {
            final int avg_target_bits = (this.W != 0 ? bm.avg_bitsper * bm.short_per_long : bm.avg_bitsper);
            bm.avg_reservoir += this_bits - avg_target_bits;
        }

        return (0);
    }
}
