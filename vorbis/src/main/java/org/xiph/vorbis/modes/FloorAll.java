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

package org.xiph.vorbis.modes;

import org.xiph.vorbis.InfoFloor1;
import org.xiph.vorbis.StaticCodebook;
import org.xiph.vorbis.books.FloorBooks;

/**
 * key floor settings
 */

public class FloorAll {

    private static final StaticCodebook _floor_128x4_books[] = {
            FloorBooks._huff_book_line_128x4_class0,
            FloorBooks._huff_book_line_128x4_0sub0,
            FloorBooks._huff_book_line_128x4_0sub1,
            FloorBooks._huff_book_line_128x4_0sub2,
            FloorBooks._huff_book_line_128x4_0sub3,
    };

    private static final StaticCodebook _floor_256x4_books[] = {
            FloorBooks._huff_book_line_256x4_class0,
            FloorBooks._huff_book_line_256x4_0sub0,
            FloorBooks._huff_book_line_256x4_0sub1,
            FloorBooks._huff_book_line_256x4_0sub2,
            FloorBooks._huff_book_line_256x4_0sub3,
    };

    private static final StaticCodebook _floor_128x7_books[] = {
            FloorBooks._huff_book_line_128x7_class0,
            FloorBooks._huff_book_line_128x7_class1,

            FloorBooks._huff_book_line_128x7_0sub1,
            FloorBooks._huff_book_line_128x7_0sub2,
            FloorBooks._huff_book_line_128x7_0sub3,
            FloorBooks._huff_book_line_128x7_1sub1,
            FloorBooks._huff_book_line_128x7_1sub2,
            FloorBooks._huff_book_line_128x7_1sub3,
    };

    private static final StaticCodebook _floor_256x7_books[] = {
            FloorBooks._huff_book_line_256x7_class0,
            FloorBooks._huff_book_line_256x7_class1,

            FloorBooks._huff_book_line_256x7_0sub1,
            FloorBooks._huff_book_line_256x7_0sub2,
            FloorBooks._huff_book_line_256x7_0sub3,
            FloorBooks._huff_book_line_256x7_1sub1,
            FloorBooks._huff_book_line_256x7_1sub2,
            FloorBooks._huff_book_line_256x7_1sub3,
    };

    private static final StaticCodebook _floor_128x11_books[] = {
            FloorBooks._huff_book_line_128x11_class1,
            FloorBooks._huff_book_line_128x11_class2,
            FloorBooks._huff_book_line_128x11_class3,

            FloorBooks._huff_book_line_128x11_0sub0,
            FloorBooks._huff_book_line_128x11_1sub0,
            FloorBooks._huff_book_line_128x11_1sub1,
            FloorBooks._huff_book_line_128x11_2sub1,
            FloorBooks._huff_book_line_128x11_2sub2,
            FloorBooks._huff_book_line_128x11_2sub3,
            FloorBooks._huff_book_line_128x11_3sub1,
            FloorBooks._huff_book_line_128x11_3sub2,
            FloorBooks._huff_book_line_128x11_3sub3,
    };

    private static final StaticCodebook _floor_128x17_books[] = {
            FloorBooks._huff_book_line_128x17_class1,
            FloorBooks._huff_book_line_128x17_class2,
            FloorBooks._huff_book_line_128x17_class3,

            FloorBooks._huff_book_line_128x17_0sub0,
            FloorBooks._huff_book_line_128x17_1sub0,
            FloorBooks._huff_book_line_128x17_1sub1,
            FloorBooks._huff_book_line_128x17_2sub1,
            FloorBooks._huff_book_line_128x17_2sub2,
            FloorBooks._huff_book_line_128x17_2sub3,
            FloorBooks._huff_book_line_128x17_3sub1,
            FloorBooks._huff_book_line_128x17_3sub2,
            FloorBooks._huff_book_line_128x17_3sub3,
    };

    private static final StaticCodebook _floor_256x4low_books[] = {
            FloorBooks._huff_book_line_256x4low_class0,
            FloorBooks._huff_book_line_256x4low_0sub0,
            FloorBooks._huff_book_line_256x4low_0sub1,
            FloorBooks._huff_book_line_256x4low_0sub2,
            FloorBooks._huff_book_line_256x4low_0sub3,
    };

    private static final StaticCodebook _floor_1024x27_books[] = {
            FloorBooks._huff_book_line_1024x27_class1,
            FloorBooks._huff_book_line_1024x27_class2,
            FloorBooks._huff_book_line_1024x27_class3,
            FloorBooks._huff_book_line_1024x27_class4,

            FloorBooks._huff_book_line_1024x27_0sub0,
            FloorBooks._huff_book_line_1024x27_1sub0,
            FloorBooks._huff_book_line_1024x27_1sub1,
            FloorBooks._huff_book_line_1024x27_2sub0,
            FloorBooks._huff_book_line_1024x27_2sub1,
            FloorBooks._huff_book_line_1024x27_3sub1,
            FloorBooks._huff_book_line_1024x27_3sub2,
            FloorBooks._huff_book_line_1024x27_3sub3,
            FloorBooks._huff_book_line_1024x27_4sub1,
            FloorBooks._huff_book_line_1024x27_4sub2,
            FloorBooks._huff_book_line_1024x27_4sub3,
    };

    private static final StaticCodebook _floor_2048x27_books[] = {
            FloorBooks._huff_book_line_2048x27_class1,
            FloorBooks._huff_book_line_2048x27_class2,
            FloorBooks._huff_book_line_2048x27_class3,
            FloorBooks._huff_book_line_2048x27_class4,

            FloorBooks._huff_book_line_2048x27_0sub0,
            FloorBooks._huff_book_line_2048x27_1sub0,
            FloorBooks._huff_book_line_2048x27_1sub1,
            FloorBooks._huff_book_line_2048x27_2sub0,
            FloorBooks._huff_book_line_2048x27_2sub1,
            FloorBooks._huff_book_line_2048x27_3sub1,
            FloorBooks._huff_book_line_2048x27_3sub2,
            FloorBooks._huff_book_line_2048x27_3sub3,
            FloorBooks._huff_book_line_2048x27_4sub1,
            FloorBooks._huff_book_line_2048x27_4sub2,
            FloorBooks._huff_book_line_2048x27_4sub3,
    };

    private static final StaticCodebook _floor_512x17_books[] = {
            FloorBooks._huff_book_line_512x17_class1,
            FloorBooks._huff_book_line_512x17_class2,
            FloorBooks._huff_book_line_512x17_class3,

            FloorBooks._huff_book_line_512x17_0sub0,
            FloorBooks._huff_book_line_512x17_1sub0,
            FloorBooks._huff_book_line_512x17_1sub1,
            FloorBooks._huff_book_line_512x17_2sub1,
            FloorBooks._huff_book_line_512x17_2sub2,
            FloorBooks._huff_book_line_512x17_2sub3,
            FloorBooks._huff_book_line_512x17_3sub1,
            FloorBooks._huff_book_line_512x17_3sub2,
            FloorBooks._huff_book_line_512x17_3sub3,
    };

    private static final StaticCodebook _floor_Xx0_books[] = {
            null
    };

    protected static final StaticCodebook _floor_books[][] = {// [11]
            _floor_128x4_books,
            _floor_256x4_books,
            _floor_128x7_books,
            _floor_256x7_books,
            _floor_128x11_books,
            _floor_128x17_books,
            _floor_256x4low_books,
            _floor_1024x27_books,
            _floor_2048x27_books,
            _floor_512x17_books,
            _floor_Xx0_books,
    };

    protected static final InfoFloor1 _floor[] = {// [11]
            /** 0: 128 x 4 */
            new InfoFloor1(
                    1, new int[]{0}, new int[]{4}, new int[]{2}, new int[]{0},
                    new int[][]{{1, 2, 3, 4}},
                    4, new int[]{0, 128, 33, 8, 16, 70},

                    60, 30, 500, 1.f, 18.f, 128
            ),
            /** 1: 256 x 4 */
            new InfoFloor1(
                    1, new int[]{0}, new int[]{4}, new int[]{2}, new int[]{0},
                    new int[][]{{1, 2, 3, 4}},
                    4, new int[]{0, 256, 66, 16, 32, 140},

                    60, 30, 500, 1.f, 18.f, 256
            ),
            /** 2: 128 x 7 */
            new InfoFloor1(
                    2, new int[]{0, 1}, new int[]{3, 4}, new int[]{2, 2}, new int[]{0, 1},
                    new int[][]{{-1, 2, 3, 4}, {-1, 5, 6, 7}},
                    4, new int[]{0, 128, 14, 4, 58, 2, 8, 28, 90},

                    60, 30, 500, 1.f, 18.f, 128
            ),
            /** 3: 256 x 7 */
            new InfoFloor1(
                    2, new int[]{0, 1}, new int[]{3, 4}, new int[]{2, 2}, new int[]{0, 1},
                    new int[][]{{-1, 2, 3, 4}, {-1, 5, 6, 7}},
                    4, new int[]{0, 256, 28, 8, 116, 4, 16, 56, 180},

                    60, 30, 500, 1.f, 18.f, 256
            ),
            /** 4: 128 x 11 */
            new InfoFloor1(
                    4, new int[]{0, 1, 2, 3}, new int[]{2, 3, 3, 3}, new int[]{0, 1, 2, 2}, new int[]{-1, 0, 1, 2},
                    new int[][]{{3}, {4, 5}, {-1, 6, 7, 8}, {-1, 9, 10, 11}},

                    2, new int[]{0, 128, 8, 33, 4, 16, 70, 2, 6, 12, 23, 46, 90},

                    60, 30, 500, 1, 18.f, 128
            ),
            /** 5: 128 x 17 */
            new InfoFloor1(
                    6, new int[]{0, 1, 1, 2, 3, 3}, new int[]{2, 3, 3, 3}, new int[]{0, 1, 2, 2}, new int[]{-1, 0, 1, 2},
                    new int[][]{{3}, {4, 5}, {-1, 6, 7, 8}, {-1, 9, 10, 11}},
                    2, new int[]{0, 128, 12, 46, 4, 8, 16, 23, 33, 70, 2, 6, 10, 14, 19, 28, 39, 58, 90},

                    60, 30, 500, 1, 18.f, 128
            ),
            /** 6: 256 x 4 (low bitrate version) */
            new InfoFloor1(
                    1, new int[]{0}, new int[]{4}, new int[]{2}, new int[]{0},
                    new int[][]{{1, 2, 3, 4}},
                    4, new int[]{0, 256, 66, 16, 32, 140},

                    60, 30, 500, 1.f, 18.f, 256
            ),
            /** 7: 1024 x 27 */
            new InfoFloor1(
                    8, new int[]{0, 1, 2, 2, 3, 3, 4, 4}, new int[]{3, 4, 3, 4, 3}, new int[]{0, 1, 1, 2, 2}, new int[]{-1, 0, 1, 2, 3},
                    new int[][]{{4}, {5, 6}, {7, 8}, {-1, 9, 10, 11}, {-1, 12, 13, 14}},
                    2, new int[]{0, 1024, 93, 23, 372, 6, 46, 186, 750, 14, 33, 65, 130, 260, 556,
                    3, 10, 18, 28, 39, 55, 79, 111, 158, 220, 312, 464, 650, 850},

                    60, 30, 500, 3, 18.f, 1024
            ),
            /** 8: 2048 x 27 */
            new InfoFloor1(
                    8, new int[]{0, 1, 2, 2, 3, 3, 4, 4}, new int[]{3, 4, 3, 4, 3}, new int[]{0, 1, 1, 2, 2}, new int[]{-1, 0, 1, 2, 3},
                    new int[][]{{4}, {5, 6}, {7, 8}, {-1, 9, 10, 11}, {-1, 12, 13, 14}},
                    2, new int[]{0, 2048, 186, 46, 744, 12, 92, 372, 1500, 28, 66, 130, 260, 520, 1112,
                    6, 20, 36, 56, 78, 110, 158, 222, 316, 440, 624, 928, 1300, 1700},

                    60, 30, 500, 3, 18.f, 2048
            ),
            /** 9: 512 x 17 */
            new InfoFloor1(
                    6, new int[]{0, 1, 1, 2, 3, 3}, new int[]{2, 3, 3, 3}, new int[]{0, 1, 2, 2}, new int[]{-1, 0, 1, 2},
                    new int[][]{{3}, {4, 5}, {-1, 6, 7, 8}, {-1, 9, 10, 11}},
                    2, new int[]{0, 512, 46, 186, 16, 33, 65, 93, 130, 278,
                    7, 23, 39, 55, 79, 110, 156, 232, 360},

                    60, 30, 500, 1, 18.f, 512
            ),
            /** 10: X x 0 (LFE floor; edge posts only) */
            new InfoFloor1(
                    0, new int[]{0}, new int[]{0}, new int[]{0}, new int[]{-1},
                    new int[][]{{-1}},
                    2, new int[]{0, 12},
                    60, 30, 500, 1.f, 18.f, 10
            ),

    };
}
