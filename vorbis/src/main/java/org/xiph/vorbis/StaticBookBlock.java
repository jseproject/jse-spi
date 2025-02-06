package org.xiph.vorbis;

/**
 * careful with this; it's using static array sizing to make managing
 * all the modes a little less annoying.  If we use a residue backend
 * with > 12 partition types, or a different division of iteration,
 * this needs to be updated.
 */
public class StaticBookBlock {
    final StaticCodebook[][] books = new StaticCodebook[12][4];

    //
    public StaticBookBlock(final StaticCodebook[][] st_books) {
        for (int i = 0, length = st_books.length; i < length; i++) {
            System.arraycopy(st_books[i], 0, books[i], 0, st_books[i].length);
        }
    }
}
