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
 * A quick sort demonstration algorithm
 * SortAlgorithm.java
 *
 * @author James Gosling
 * @author Kevin A. Smith
 * @version @(#)QSortAlgorithm.java	1.3, 29 Feb 1996
 * extended with TriMedian and InsertionSort by Denis Ahrens
 * with all the tips from Robert Sedgewick (Algorithms in C++).
 * It uses TriMedian and InsertionSort for lists shorts than 4.
 * <fuhrmann@cs.tu-berlin.de>
 */
class FastQSortAlgorithm {

    private static void swap(int a[], int i, int j) {
        int T;
        T = a[i];
        a[i] = a[j];
        a[j] = T;
    }

    /**
     * This is a generic version of C.A.R Hoare's Quick Sort
     * algorithm.  This will handle arrays that are already
     * sorted, and arrays with duplicate keys.<BR>
     * <p>
     * If you think of a one dimensional array as going from
     * the lowest index on the left to the highest index on the right
     * then the parameters to this function are lowest index or
     * left and highest index or right.  The first time you call
     * this function it will be with the parameters 0, a.length - 1.
     *
     * @param a an integer array
     * @param l left boundary of array partition
     * @param r right boundary of array partition
     */
    private static void quickSort(int a[], int l, int r, Comparator cmp) {
        final int M = 4;
        int i;
        int j;
        int v;

        if ((r - l) > M) {
            i = (r + l) >>> 1;
            if (cmp.compare(a[l], a[i]) > 0) swap(a, l, i);// Tri-Median Methode!
            if (cmp.compare(a[l], a[r]) > 0) swap(a, l, r);
            if (cmp.compare(a[i], a[r]) > 0) swap(a, i, r);

            j = r - 1;
            swap(a, i, j);
            i = l;
            v = a[j];
            for (; ; ) {
                while (cmp.compare(a[++i], v) < 0) ;
                while (cmp.compare(a[--j], v) > 0) ;
                if (j < i) break;
                swap(a, i, j);
            }
            swap(a, i, r - 1);
            quickSort(a, l, j, cmp);
            quickSort(a, i + 1, r, cmp);
        }
    }

    private static void insertionSort(int a[], int lo0, int hi0, Comparator cmp) {
        int i;
        int j;
        int v;

        for (i = lo0 + 1; i <= hi0; i++) {
            v = a[i];
            j = i;
            while ((j > lo0) && cmp.compare(a[j - 1], v) > 0) {
                a[j] = a[j - 1];
                j--;
            }
            a[j] = v;
        }
    }

    static void sort(int a[], int fromInclusive, int toExclusive, Comparator cmp) {
        quickSort(a, fromInclusive, --toExclusive, cmp);
        insertionSort(a, fromInclusive, toExclusive, cmp);
    }
/*	// float inverse sort
	// used only in unused function Floor0.lpc_to_lsp
	private static void swap(float a[], int i, int j) {
		float T;
		T = a[i]; 
		a[i] = a[j];
		a[j] = T;
	}
	private static void quickInverseSort(float a[], int l, int r) {
		final int M = 4;
		int i;
		int j;
		float v;

		if( (r - l) > M ) {
			i = (r + l) >>> 1;
			if( a[l] < a[i] ) swap( a, l, i );// Tri-Median Methode!
			if( a[l] < a[r] ) swap( a, l, r );
			if( a[i] < a[r] ) swap( a, i, r );

			j = r - 1;
			swap( a, i, j );
			i = l;
			v = a[j];
			for( ; ; ) {
				while( a[++i] > v );
				while( a[--j] < v );
				if (j < i ) break;
				swap( a, i, j );
			}
			swap( a, i , r - 1 );
			quickInverseSort( a, l, j );
			quickInverseSort( a, i + 1, r );
		}
	}

	private static void insertionInverseSort(float a[], int lo0, int hi0) {
		int i;
		int j;
		float v;

		for( i = lo0 + 1; i <= hi0; i++ ) {
			v = a[i];
			j = i;
			while( (j > lo0) && (a[j - 1] < v) ) {
				a[j] = a[j - 1];
				j--;
			}
			a[j] = v;
		}
	}

	public static void inverseSort(float a[], int fromInclusive, int toExclusive) {
		quickInverseSort( a, fromInclusive, --toExclusive );
		insertionInverseSort( a, fromInclusive, toExclusive );
	}*/
}

