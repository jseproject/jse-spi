package org.concentus;

import java.lang.reflect.Array;

class Arrays<T> {

    static int[][] InitTwoDimensionalArrayInt(int x, int y) {
        int[][] returnVal = new int[x][];
        for (int c = 0; c < x; c++) {
            returnVal[c] = new int[y];
        }
        return returnVal;
    }

    static float[][] InitTwoDimensionalArrayFloat(int x, int y) {
        float[][] returnVal = new float[x][];
        for (int c = 0; c < x; c++) {
            returnVal[c] = new float[y];
        }
        return returnVal;
    }

    static short[][] InitTwoDimensionalArrayShort(int x, int y) {
        short[][] returnVal = new short[x][];
        for (int c = 0; c < x; c++) {
            returnVal[c] = new short[y];
        }
        return returnVal;
    }

    static byte[][] InitTwoDimensionalArrayByte(int x, int y) {
        byte[][] returnVal = new byte[x][];
        for (int c = 0; c < x; c++) {
            returnVal[c] = new byte[y];
        }
        return returnVal;
    }

    static byte[][][] InitThreeDimensionalArrayByte(int x, int y, int z) {
        byte[][][] returnVal = new byte[x][][];
        for (int c = 0; c < x; c++) {
            returnVal[c] = new byte[y][];
            for (int a = 0; a < y; a++) {
                returnVal[c][a] = new byte[z];
            }
        }
        return returnVal;
    }

    static void MemSet(byte[] array, byte value) {
        for (int c = 0; c < array.length; c++) {
            array[c] = value;
        }
    }

    static void MemSet(short[] array, short value) {
        for (int c = 0; c < array.length; c++) {
            array[c] = value;
        }
    }

    static void MemSet(int[] array, int value) {
        for (int c = 0; c < array.length; c++) {
            array[c] = value;
        }
    }

    static void MemSet(float[] array, float value) {
        for (int c = 0; c < array.length; c++) {
            array[c] = value;
        }
    }

    static void MemSet(byte[] array, byte value, int length) {
        for (int c = 0; c < length; c++) {
            array[c] = value;
        }
    }

    static void MemSet(short[] array, short value, int length) {
        for (int c = 0; c < length; c++) {
            array[c] = value;
        }
    }

    static void MemSet(int[] array, int value, int length) {
        for (int c = 0; c < length; c++) {
            array[c] = value;
        }
    }

    static void MemSet(float[] array, float value, int length) {
        for (int c = 0; c < length; c++) {
            array[c] = value;
        }
    }

    static void MemSetWithOffset(byte[] array, byte value, int offset, int length) {
        for (int c = offset; c < offset + length; c++) {
            array[c] = value;
        }
    }

    static void MemSetWithOffset(short[] array, short value, int offset, int length) {
        for (int c = offset; c < offset + length; c++) {
            array[c] = value;
        }
    }

    static void MemSetWithOffset(int[] array, int value, int offset, int length) {
        for (int c = offset; c < offset + length; c++) {
            array[c] = value;
        }
    }

    // Hooray for generic programming in Java
    static void MemMove(byte[] array, int src_idx, int dst_idx, int length) {
        if (src_idx == dst_idx || length == 0) {
            return;
        }

        // Do regions overlap?
        if (src_idx + length > dst_idx || dst_idx + length > src_idx) {
            // Take extra precautions
            if (dst_idx < src_idx) {
                // Copy forwards
                for (int c = 0; c < length; c++) {
                    array[c + dst_idx] = array[c + src_idx];
                }
            } else {
                // Copy backwards
                for (int c = length - 1; c >= 0; c--) {
                    array[c + dst_idx] = array[c + src_idx];
                }
            }
        } else {
            // Memory regions cannot overlap; just do a fast copy
            System.arraycopy(array, src_idx, array, dst_idx, length);
        }
    }

    static void MemMove(short[] array, int src_idx, int dst_idx, int length) {
        if (src_idx == dst_idx || length == 0) {
            return;
        }

        // Do regions overlap?
        if (src_idx + length > dst_idx || dst_idx + length > src_idx) {
            // Take extra precautions
            if (dst_idx < src_idx) {
                // Copy forwards
                for (int c = 0; c < length; c++) {
                    array[c + dst_idx] = array[c + src_idx];
                }
            } else {
                // Copy backwards
                for (int c = length - 1; c >= 0; c--) {
                    array[c + dst_idx] = array[c + src_idx];
                }
            }
        } else {
            // Memory regions cannot overlap; just do a fast copy
            System.arraycopy(array, src_idx, array, dst_idx, length);
        }
    }

    static void MemMove(int[] array, int src_idx, int dst_idx, int length) {
        if (src_idx == dst_idx || length == 0) {
            return;
        }

        // Do regions overlap?
        if (src_idx + length > dst_idx || dst_idx + length > src_idx) {
            // Take extra precautions
            if (dst_idx < src_idx) {
                // Copy forwards
                for (int c = 0; c < length; c++) {
                    array[c + dst_idx] = array[c + src_idx];
                }
            } else {
                // Copy backwards
                for (int c = length - 1; c >= 0; c--) {
                    array[c + dst_idx] = array[c + src_idx];
                }
            }
        } else {
            // Memory regions cannot overlap; just do a fast copy
            System.arraycopy(array, src_idx, array, dst_idx, length);
        }
    }
}
