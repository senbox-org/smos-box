/*
 * Copyright 1998-2009 University Corporation for Atmospheric Research/Unidata
 *
 * Portions of this software were developed by the Unidata Program at the
 * University Corporation for Atmospheric Research.
 *
 * Access and use of this software shall impose the following obligations
 * and understandings on the user. The user is granted the right, without
 * any fee or cost, to use, copy, modify, alter, enhance and distribute
 * this software, and any derivative works thereof, and its supporting
 * documentation for any purpose whatsoever, provided that this entire
 * notice appears in all copies of the software, derivative works and
 * supporting documentation.  Further, UCAR requests that the user credit
 * UCAR/Unidata in any publications that result from the use of this
 * software or in any product that includes this software. The names UCAR
 * and/or Unidata, however, may not be used in any advertising or publicity
 * to endorse or promote any products or commercial entity unless specific
 * written permission is obtained from UCAR/Unidata. The user also
 * understands that UCAR/Unidata is not obligated to provide the user with
 * any support, consulting, training or assistance of any kind with regard
 * to the use, operation and performance of this software nor to provide
 * the user with any updates, revisions, new versions or "bug fixes."
 *
 * THIS SOFTWARE IS PROVIDED BY UCAR/UNIDATA "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL UCAR/UNIDATA BE LIABLE FOR ANY SPECIAL,
 * INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING
 * FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 * NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION
 * WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package ucar.nc2.iosp.smos.bufr;

import ucar.unidata.io.RandomAccessFile;

import java.io.IOException;

/**
 * A class that contains static methods for converting multiple
 * bytes into one float or integer.
 */

final public class BufrNumbers {

    // used to check missing values when value is packed with all 1's
    static private final long[] missing_value = new long[65];

    static {
        long accum = 0;
        for (int i = 0; i < 65; i++) {
            missing_value[i] = accum;
            //System.out.printf("BufrNumbers %2d : %20d = %s %n", i, accum, Long.toBinaryString(accum));
            accum = accum * 2 + 1;
        }
    }

    static public boolean isMissing(long raw, int bitWidth) {
        return (raw == BufrNumbers.missing_value[bitWidth]);
    }

    static long missingValue(int bitWidth) {
        return BufrNumbers.missing_value[bitWidth];
    }

    /**
     * if missing value is not defined use this value.
     */
    public static final int UNDEFINED = -9999;

    /**
     * Convert 2 bytes into a signed integer.
     *
     * @param raf the file to read
     * @return integer value
     * @throws IOException on IO problems
     */
    public static int int2(RandomAccessFile raf) throws IOException {
        int a = raf.read();
        int b = raf.read();

        return int2(a, b);
    }

    /**
     * convert 2 bytes to a signed integer.
     *
     * @param a first byte
     * @param b second byte
     * @return int
     */
    public static int int2(int a, int b) {
        if ((a == 0xff && b == 0xff)) { // all bits set to one
            return UNDEFINED;
        }

        return (1 - ((a & 128) >> 6)) * ((a & 127) << 8 | b);
    }

    /**
     * Convert 3 bytes into a signed integer.
     *
     * @param raf the file to read
     * @return integer value
     * @throws IOException on IO problems
     */
    public static int int3(RandomAccessFile raf) throws IOException {
        int a = raf.read();
        int b = raf.read();
        int c = raf.read();

        return int3(a, b, c);
    }

    /**
     * Convert 3 bytes to signed integer.
     *
     * @param a first byte
     * @param b second byte
     * @param c third byte
     * @return int
     */
    private static int int3(int a, int b, int c) {
        return (1 - ((a & 128) >> 6)) * ((a & 127) << 16 | b << 8 | c);
    }

    /**
     * Convert 2 bytes into an unsigned integer.
     *
     * @param raf the file to read
     * @return integer value
     * @throws IOException on IO problems
     */
    public static int uint2(RandomAccessFile raf) throws IOException {
        int a = raf.read();
        int b = raf.read();

        return uint2(a, b);
    }

    /**
     * convert 2 bytes to an unsigned integer.
     *
     * @param a first byte
     * @param b second byte
     * @return unsigned int
     */
    private static int uint2(int a, int b) {
        return a << 8 | b;
    }

    /**
     * Convert 3 bytes into an unsigned integer.
     *
     * @param raf the file to read
     * @return integer
     * @throws IOException on IO problems
     */
    public static int uint3(RandomAccessFile raf) throws IOException {
        int a = raf.read();
        int b = raf.read();
        int c = raf.read();

        return uint3(a, b, c);
    }

    /**
     * Convert 3 bytes into an unsigned int.
     *
     * @param a first byte
     * @param b second byte
     * @param c third byte
     * @return unsigned integer
     */
    public static int uint3(int a, int b, int c) {
        return a << 16 | b << 8 | c;
    }
}
