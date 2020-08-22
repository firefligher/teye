package org.fir3.teye.util;

public class PowerOfTwo {
    /**
     * Returns whether the specified value is a power of two or not.
     *
     * @param value The value that will be tested.
     *
     * @return  Either <code>true</code>, if <code>value</code> is a power of
     *          two, otherwise <code>false</code>.
     */
    public static boolean isPowerOfTwo(int value) {
        int i = 1;

        while (value > i) {
            i *= 2;
        }

        return (value == i);
    }
}
