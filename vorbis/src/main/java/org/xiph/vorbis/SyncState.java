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

import java.util.Arrays;

/**
 * From C to Java changes:<br>
 * <code>int ogg_sync_destroy(SyncState oy)</code>
 * -> <code>SyncState oy = null</code>
 */
public class SyncState {
    public byte[] data = null;
    int storage = 0;
    int fill = 0;
    int returned = 0;

    boolean unsynced = false;
    int headerbytes = 0;
    int bodybytes = 0;

    //
    private final void m_clear() {
        data = null;
        storage = 0;
        fill = 0;
        returned = 0;
        unsynced = false;
        headerbytes = 0;
        bodybytes = 0;
    }
    // framing.c
    /** DECODING PRIMITIVES: packet streaming layer */

    /** This has two layers to place more of the multi-serialno and paging
     control in the application's hands. First, we expose a data buffer
     using ogg_sync_buffer(). The app either copies into the
     buffer, or passes it directly to read(), etc. We then call
     ogg_sync_wrote() to tell how many bytes we just added.

     Pages are returned (pointers into the buffer in ogg_sync_state)
     by ogg_sync_pageout(). The page is then submitted to
     ogg_stream_pagein() along with the appropriate
     ogg_stream_state* (ie, matching serialno). We then get raw
     packets out calling ogg_stream_packetout() with a
     ogg_stream_state. */

    /**
     * initialize the struct to a known state
     */
    public final void init() {// return changed to void
        //if( oy != null ) {
        // FIXME is this a bug? may be after clear()?
        storage = -1; /* used as a readiness flag */
        m_clear();
        //}
        //return (0);
    }

    /**
     * clear non-flat storage within
     */
    public final void clear() {// return changed to void
        //if( oy != null ) {
        m_clear();
        //}
        //return 0;
    }

    public final int check() {
        if (storage < 0) {
            return -1;
        }
        return 0;
    }

    /**
     * @return index to SyncState.m_data or -1.
     */
    public final int buffer(final int size) {
        if (check() != 0) {
            return -1;//return null;
        }

        /* first, clear out any space that has been previously returned */
        if (this.returned != 0) {
            this.fill -= this.returned;
            if (this.fill > 0) {
                System.arraycopy(this.data, this.returned, this.data, 0, this.fill);
            }
            this.returned = 0;
        }

        if (size > this.storage - this.fill) {
            /* We need to extend the internal buffer */
            final int newsize = size + this.fill + 4096; /* an extra page to be nice */

            //try {
            byte[] ret;
            if (this.data != null) {
                ret = Arrays.copyOf(this.data, newsize);
            } else {
                ret = new byte[newsize];
            }
            this.data = ret;
            this.storage = newsize;
            //} catch( OutOfMemoryError e ) {
            //	clear();
            //	return -1;//return null;
            //}
        }

        /* expose a segment at least as large as requested at the fill mark */
        return this.fill;//return ((byte *)oy.data + oy.fill);
    }

    public final int wrote(int bytes) {
        if (check() != 0) {
            return -1;
        }
        bytes += this.fill;
        if (bytes > this.storage) {
            return -1;
        }
        this.fill = bytes;
        return 0;
    }

    private final int fail(final int page, int bytes) {
        int i, next;
        this.headerbytes = 0;
        this.bodybytes = 0;

        /* search for possible capture */
        next = -1;
        bytes += page;
        final byte[] d = this.data;// java
        for (i = page + 1; i < bytes; i++) {
            if (d[i] == 'O') {
                next = i;
                break;
            }
        }
        if (next < 0) {
            next = this.fill;
        }

        this.returned = next;
        return (page - next);
    }

    /**
     * sync the stream. This is meant to be useful for finding page
     * boundaries.
     * <p>
     * return values for this:
     * -n) skipped n bytes
     * 0) page not ready; more data (no bytes skipped)
     * n) page synced at current location; page length n bytes
     */

    public final int pageseek(final Page og) {
        if (check() != 0) {
            return 0;
        }

        int page = this.returned;
        int bytes = this.fill - this.returned;
        final byte[] d = this.data;// java
        if (this.headerbytes == 0) {
            if (bytes < 27) {
                return (0); /* not enough for a header */
            }

            /* verify capture pattern */
            if (d[page] != 'O' || d[page + 1] != 'g' ||
                    d[page + 2] != 'g' || d[page + 3] != 'S') {
                return fail(page, bytes);
            }

            final int header_bytes = (((int) d[page + 26]) & 0xff) + 27;
            if (bytes < header_bytes) {
                return (0);/* not enough for header + seg table */
            }

            /* count up body length in the segment table */
            for (int i = page + 27, ie = page + header_bytes; i < ie; i++) {
                this.bodybytes += (((int) d[i]) & 0xff);
            }
            this.headerbytes = header_bytes;
        }

        if (this.bodybytes + this.headerbytes > bytes) {
            return (0);
        }

        /* The whole test page is buffered. Verify the checksum */
        {
            /* Grab the checksum bytes, set the header field to zero */
            final byte chksum[] = new byte[4];
            final Page log = new Page();

            System.arraycopy(d, page + 22, chksum, 0, 4);
            d[page + 22] = d[page + 23] = d[page + 24] = d[page + 25] = 0;

            /* set up a temp page struct and recompute the checksum */
            log.header_base = d;
            log.header = page;
            log.header_len = this.headerbytes;
            log.body_base = d;
            log.body = page + this.headerbytes;
            log.body_len = this.bodybytes;
            log.checksum_set();

            /* Compare */
            if (chksum[0] != d[page + 22] || chksum[1] != d[page + 23] ||
                    chksum[2] != d[page + 24] || chksum[3] != d[page + 25]) {
				/* D'oh. Mismatch! Corrupt page (or miscapture and not a page
					at all) */
                /* replace the computed checksum with the one actually read in */
                d[page + 22] = chksum[0];
                d[page + 23] = chksum[1];
                d[page + 24] = chksum[2];
                d[page + 25] = chksum[3];

                /* Bad checksum. Lose sync */
                return fail(page, bytes);
            }
        }

        /* yes, have a whole page all ready to go */
        {
            /** index for oy.data */
            page = this.returned;
            if (og != null) {
                og.header_base = d;
                og.header = page;
                og.header_len = this.headerbytes;
                og.body_base = d;
                og.body = page + this.headerbytes;
                og.body_len = this.bodybytes;
            }

            this.unsynced = false;
            this.returned += (bytes = this.headerbytes + this.bodybytes);
            this.headerbytes = 0;
            this.bodybytes = 0;
            return (bytes);
        }
    }

    /**
     * sync the stream and get a page. Keep trying until we find a page.
     * Suppress 'sync errors' after reporting the first.
     * <p>
     * return values:
     * -1) recapture (hole in data)
     * 0) need more data
     * 1) page returned
     * <p>
     * Returns pointers into buffered data; invalidated by next call to
     * _stream, _clear, _init, or _buffer
     */
    public final int pageout(final Page og) {

        if (check() != 0) {
            return 0;
        }

		/* all we need to do is verify a page at the head of the stream
		buffer. If it doesn't verify, we look for the next potential
		frame */

        for (; ; ) {
            final int ret = pageseek(og);
            if (ret > 0) {
                /* have a page */
                return (1);
            }
            if (ret == 0) {
                /* need more data */
                return (0);
            }

            /* head did not start a synced page... skipped some bytes */
            if (!this.unsynced) {
                this.unsynced = true;
                return (-1);
            }

            /* loop. keep looking */

        }
    }

    /**
     * clear things to an initial state. Good to call, eg, before seeking
     */
    public final int reset() {
        if (check() != 0) {
            return -1;
        }

        fill = 0;
        returned = 0;
        unsynced = false;
        headerbytes = 0;
        bodybytes = 0;
        return 0;
    }
}
