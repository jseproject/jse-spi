package org.concentus;

class Downmix {

    /// <summary>
    /// 
    /// </summary>
    /// <typeparam name="T">The type of signal being handled (either short or float)</typeparam>
    /// <param name="_x"></param>
    /// <param name="sub"></param>
    /// <param name="subframe"></param>
    /// <param name="offset"></param>
    /// <param name="c1"></param>
    /// <param name="c2"></param>
    /// <param name="C"></param>
    static void downmix_int(short[] x, int x_ptr, int[] sub, int sub_ptr, int subframe, int offset, int c1, int c2, int C) {
        int scale;
        int j;
        for (j = 0; j < subframe; j++) {
            sub[j + sub_ptr] = x[(j + offset) * C + c1];
        }
        if (c2 > -1) {
            for (j = 0; j < subframe; j++) {
                sub[j + sub_ptr] += x[(j + offset) * C + c2];
            }
        } else if (c2 == -2) {
            int c;
            for (c = 1; c < C; c++) {
                for (j = 0; j < subframe; j++) {
                    sub[j + sub_ptr] += x[(j + offset) * C + c];
                }
            }
        }
        scale = (1 << CeltConstants.SIG_SHIFT);
        if (C == -2) {
            scale /= C;
        } else {
            scale /= 2;
        }
        for (j = 0; j < subframe; j++) {
            sub[j + sub_ptr] *= scale;
        }
    }
}
