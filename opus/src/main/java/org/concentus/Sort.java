package org.concentus;

class Sort {

    /// <summary>
    /// 
    /// </summary>
    /// <param name="a">(I/O) Unsorted / Sorted vector</param>
    /// <param name="idx">(O) Index vector for the sorted elements</param>
    /// <param name="L">(I) Vector length</param>
    /// <param name="K">(I) Number of correctly sorted positions</param>
    static void silk_insertion_sort_increasing(int[] a, int[] idx, int L, int K) {
        int value;
        int i, j;

        // Safety checks
        Inlines.OpusAssert(K > 0);
        Inlines.OpusAssert(L > 0);
        Inlines.OpusAssert(L >= K);

        // Write start indices in index vector
        for (i = 0; i < K; i++) {
            idx[i] = i;
        }

        // Sort vector elements by value, increasing order
        for (i = 1; i < K; i++) {
            value = a[i];

            for (j = i - 1; (j >= 0) && (value < a[j]); j--) {
                a[j + 1] = a[j];
                /* Shift value */
                idx[j + 1] = idx[j];
                /* Shift index */
            }

            a[j + 1] = value;
            /* Write value */
            idx[j + 1] = i;
            /* Write index */
        }

        // If less than L values are asked for, check the remaining values,
        // but only spend CPU to ensure that the K first values are correct
        for (i = K; i < L; i++) {
            value = a[i];

            if (value < a[K - 1]) {
                for (j = K - 2; (j >= 0) && (value < a[j]); j--) {
                    a[j + 1] = a[j];
                    /* Shift value */
                    idx[j + 1] = idx[j];
                    /* Shift index */
                }

                a[j + 1] = value;
                /* Write value */
                idx[j + 1] = i;
                /* Write index */
            }
        }
    }

    /// <summary>
    /// Insertion sort (fast for already almost sorted arrays):
    /// Best case:  O(n)   for an already sorted array
    /// Worst case: O(n^2) for an inversely sorted array
    /// </summary>
    /// <param name="a">(I/O) Unsorted / Sorted vector</param>
    /// <param name="L">(I) Vector length</param>
    static void silk_insertion_sort_increasing_all_values_int16(short[] a, int L) {
        // FIXME: Could just use Array.Sort(a.Array, a.Offset, L);

        short value;
        int i, j;

        // Safety checks
        Inlines.OpusAssert(L > 0);

        // Sort vector elements by value, increasing order
        for (i = 1; i < L; i++) {
            value = a[i];
            for (j = i - 1; (j >= 0) && (value < a[j]); j--) {
                a[j + 1] = a[j]; // Shift value
            }

            a[j + 1] = value; // Write value
        }
    }

    /* This function is only used by the fixed-point build */
    static void silk_insertion_sort_decreasing_int16(
            short[] a, /* I/O   Unsorted / Sorted vector                                   */
            int[] idx, /* O     Index vector for the sorted elements                       */
            int L, /* I     Vector length                                              */
            int K /* I     Number of correctly sorted positions                       */
    ) {
        int i, j;
        short value;

        /* Safety checks */
        Inlines.OpusAssert(K > 0);
        Inlines.OpusAssert(L > 0);
        Inlines.OpusAssert(L >= K);

        /* Write start indices in index vector */
        for (i = 0; i < K; i++) {
            idx[i] = i;
        }

        /* Sort vector elements by value, decreasing order */
        for (i = 1; i < K; i++) {
            value = a[i];
            for (j = i - 1; (j >= 0) && (value > a[j]); j--) {
                a[j + 1] = a[j];
                /* Shift value */
                idx[j + 1] = idx[j];
                /* Shift index */
            }
            a[j + 1] = value;
            /* Write value */
            idx[j + 1] = i;
            /* Write index */
        }

        /* If less than L values are asked for, check the remaining values, */
 /* but only spend CPU to ensure that the K first values are correct */
        for (i = K; i < L; i++) {
            value = a[i];
            if (value > a[K - 1]) {
                for (j = K - 2; (j >= 0) && (value > a[j]); j--) {
                    a[j + 1] = a[j];
                    /* Shift value */
                    idx[j + 1] = idx[j];
                    /* Shift index */
                }
                a[j + 1] = value;
                /* Write value */
                idx[j + 1] = i;
                /* Write index */
            }
        }
    }
}
