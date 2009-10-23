/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pivot.web;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Library of utility methods useful in dealing with converting byte arrays
 * to and from strings of hexadecimal digits.
 * <br/>
 * Portions of code here are taken from Apache Tomcat.
 *
 * @see org.apache.catalina.util.HexUtils;
 */
public final class HexUtils {

    /** Hexadecimal standard chars. */
    public static final char[] hexadecimal = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'a', 'b', 'c', 'd', 'e', 'f' };

    /** Table for HEX to DEC byte translation */
    public static final int[] DEC = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 00, 01, 02, 03, 04, 05, 06, 07, 8, 9, -1, -1, -1,
        -1, -1, -1, -1, 10, 11, 12, 13, 14, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 10, 11, 12, 13, 14, 15, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, };

    /** Constructor for private usage */
    private HexUtils() {
    }

    /**
     * Convert a String of hexadecimal digits into the corresponding byte array
     * by encoding each two hexadecimal digits as a byte.
     *
     * @param digits Hexadecimal digits representation
     * @exception IllegalArgumentException if an invalid hexadecimal digit is
     * found, or the input string contains an odd number of hexadecimal digits
     */
    public static byte[] convert(String digits) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        for (int i = 0; i < digits.length(); i += 2) {
            char c1 = digits.charAt(i);
            if ((i + 1) >= digits.length()) {
                throw new IllegalArgumentException("bad hex input data");
            }

            char c2 = digits.charAt(i + 1);
            byte b = 0;
            if ((c1 >= '0') && (c1 <= '9')) {
                b += ((c1 - '0') * 16);
            }
            else if ((c1 >= 'a') && (c1 <= 'f')) {
                b += ((c1 - 'a' + 10) * 16);
            }
            else if ((c1 >= 'A') && (c1 <= 'F')) {
                b += ((c1 - 'A' + 10) * 16);
            }
            else {
                throw new IllegalArgumentException("bad hex input data");
            }

            if ((c2 >= '0') && (c2 <= '9')) {
                b += (c2 - '0');
            }
            else if ((c2 >= 'a') && (c2 <= 'f')) {
                b += (c2 - 'a' + 10);
            }
            else if ((c2 >= 'A') && (c2 <= 'F')) {
                b += (c2 - 'A' + 10);
            }
            else {
                throw new IllegalArgumentException("bad hex input data");
            }

            baos.write(b);
        }

        return (baos.toByteArray());
    }

    /**
     * Convert a byte array into a printable format containing a String of
     * hexadecimal digit characters (two per byte).
     *
     * @param bytes Byte array representation
     */
    public static String convert(byte bytes[]) {
        StringBuffer sb = new StringBuffer(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            sb.append(convertDigit(bytes[i] >> 4));
            sb.append(convertDigit(bytes[i] & 0x0f));
        }

        return (sb.toString());
    }

    /**
     * Convert 4 hex digits to an int, and return the number of converted bytes.
     *
     * @param hex Byte array containing exactly four hexadecimal digits
     * @exception IllegalArgumentException if an invalid hexadecimal digit is
     * included
     */
    public static int convert2Int(byte[] hex) {
        // assert valid data
        int len;
        if (hex.length < 4) {
            return 0;
        }
        if (DEC[hex[0]] < 0) {
            throw new IllegalArgumentException("bad hex input data");
        }

        len = DEC[hex[0]];
        len = len << 4;
        if (DEC[hex[1]] < 0) {
            throw new IllegalArgumentException("bad hex input data");
        }
        len += DEC[hex[1]];
        len = len << 4;
        if (DEC[hex[2]] < 0) {
            throw new IllegalArgumentException("bad hex input data");
        }
        len += DEC[hex[2]];
        len = len << 4;
        if (DEC[hex[3]] < 0) {
            throw new IllegalArgumentException("bad hex input data");
        }

        len += DEC[hex[3]];
        return len;
    }

    /**
     * [Private] Convert the specified value (0 .. 15) to the corresponding
     * hexadecimal digit.
     *
     * @param value Value to be converted
     */
    private static char convertDigit(int value) {
        value &= 0x0f;

        if (value >= 10) {
            return ((char) (value - 10 + 'a'));
        }

        return ((char) (value + '0'));
    }

    /**
     * Given a String and the encoding, returns it as a byte array.
     *
     * @param string the string
     * @param encoding the encoding, or if null a default will be used
     * @return the string transformed to byte array
     * @throws UnsupportedEncodingException
     */
    public static final byte[] toByteArray(final String string, final String encoding)
        throws UnsupportedEncodingException {
        byte[] bytes = null;
        if (encoding == null) {
            bytes = string.getBytes();
        }
        else {
            bytes = string.getBytes(encoding);
        }

        return bytes;
    }

}
