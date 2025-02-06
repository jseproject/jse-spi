package org.concentus;

class CeltPitchXCorr {

    static int pitch_xcorr(
            int[] _x,
            int[] _y,
            int[] xcorr,
            int len,
            int max_pitch) {
        int i;
        int maxcorr = 1;
        Inlines.OpusAssert(max_pitch > 0);
        BoxedValueInt sum0 = new BoxedValueInt(0);
        BoxedValueInt sum1 = new BoxedValueInt(0);
        BoxedValueInt sum2 = new BoxedValueInt(0);
        BoxedValueInt sum3 = new BoxedValueInt(0);
        for (i = 0; i < max_pitch - 3; i += 4) {
            sum0.Val = 0;
            sum1.Val = 0;
            sum2.Val = 0;
            sum3.Val = 0;
            Kernels.xcorr_kernel(_x, _y, i, sum0, sum1, sum2, sum3, len);
            xcorr[i] = sum0.Val;
            xcorr[i + 1] = sum1.Val;
            xcorr[i + 2] = sum2.Val;
            xcorr[i + 3] = sum3.Val;
            sum0.Val = Inlines.MAX32(sum0.Val, sum1.Val);
            sum2.Val = Inlines.MAX32(sum2.Val, sum3.Val);
            sum0.Val = Inlines.MAX32(sum0.Val, sum2.Val);
            maxcorr = Inlines.MAX32(maxcorr, sum0.Val);
        }
        /* In case max_pitch isn't a multiple of 4, do non-unrolled version. */
        for (; i < max_pitch; i++) {
            int inner_sum = Kernels.celt_inner_prod(_x, 0, _y, i, len);
            xcorr[i] = inner_sum;
            maxcorr = Inlines.MAX32(maxcorr, inner_sum);
        }
        return maxcorr;
    }

    static int pitch_xcorr(
            short[] _x,
            int _x_ptr,
            short[] _y,
            int _y_ptr,
            int[] xcorr,
            int len,
            int max_pitch) {
        int i;
        int maxcorr = 1;
        Inlines.OpusAssert(max_pitch > 0);
        BoxedValueInt sum0 = new BoxedValueInt(0);
        BoxedValueInt sum1 = new BoxedValueInt(0);
        BoxedValueInt sum2 = new BoxedValueInt(0);
        BoxedValueInt sum3 = new BoxedValueInt(0);
        for (i = 0; i < max_pitch - 3; i += 4) {
            sum0.Val = 0;
            sum1.Val = 0;
            sum2.Val = 0;
            sum3.Val = 0;
            Kernels.xcorr_kernel(_x, _x_ptr, _y, _y_ptr + i, sum0, sum1, sum2, sum3, len);

            xcorr[i] = sum0.Val;
            xcorr[i + 1] = sum1.Val;
            xcorr[i + 2] = sum2.Val;
            xcorr[i + 3] = sum3.Val;
            sum0.Val = Inlines.MAX32(sum0.Val, sum1.Val);
            sum2.Val = Inlines.MAX32(sum2.Val, sum3.Val);
            sum0.Val = Inlines.MAX32(sum0.Val, sum2.Val);
            maxcorr = Inlines.MAX32(maxcorr, sum0.Val);
        }
        /* In case max_pitch isn't a multiple of 4, do non-unrolled version. */
        for (; i < max_pitch; i++) {
            int inner_sum = Kernels.celt_inner_prod(_x, _x_ptr, _y, _y_ptr + i, len);
            xcorr[i] = inner_sum;
            maxcorr = Inlines.MAX32(maxcorr, inner_sum);
        }
        return maxcorr;
    }

    static int pitch_xcorr(
            short[] _x,
            short[] _y,
            int[] xcorr,
            int len,
            int max_pitch) {
        int i;
        int maxcorr = 1;
        Inlines.OpusAssert(max_pitch > 0);
        BoxedValueInt sum0 = new BoxedValueInt(0);
        BoxedValueInt sum1 = new BoxedValueInt(0);
        BoxedValueInt sum2 = new BoxedValueInt(0);
        BoxedValueInt sum3 = new BoxedValueInt(0);
        for (i = 0; i < max_pitch - 3; i += 4) {
            sum0.Val = 0;
            sum1.Val = 0;
            sum2.Val = 0;
            sum3.Val = 0;
            Kernels.xcorr_kernel(_x, 0, _y, i, sum0, sum1, sum2, sum3, len);

            xcorr[i] = sum0.Val;
            xcorr[i + 1] = sum1.Val;
            xcorr[i + 2] = sum2.Val;
            xcorr[i + 3] = sum3.Val;
            sum0.Val = Inlines.MAX32(sum0.Val, sum1.Val);
            sum2.Val = Inlines.MAX32(sum2.Val, sum3.Val);
            sum0.Val = Inlines.MAX32(sum0.Val, sum2.Val);
            maxcorr = Inlines.MAX32(maxcorr, sum0.Val);
        }
        /* In case max_pitch isn't a multiple of 4, do non-unrolled version. */
        for (; i < max_pitch; i++) {
            int inner_sum = Kernels.celt_inner_prod(_x, _y, i, len);
            xcorr[i] = inner_sum;
            maxcorr = Inlines.MAX32(maxcorr, inner_sum);
        }
        return maxcorr;
    }
}
