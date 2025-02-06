package org.xiph.vorbis;

/**
 * Interface to use while sorting
 */
interface Comparator {
    /**
     * @return to sort in increasing order:<br>
     * < 0, if a < b<br>
     * 0, if a == b<br>
     * > 0, if a > b
     */
    int compare(int a, int b);
}
