/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.thinkaurelius.titan.diskstorage.util;

import java.lang.reflect.Constructor;

public class Hex
{
    /**
     * Used to build output as Hex
     */
    private static final char[] DIGITS_LOWER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * Used to build output as Hex
     */
    private static final char[] DIGITS_UPPER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static final Constructor<String> stringConstructor = getProtectedConstructor(String.class, int.class, int.class, char[].class);
    private final static byte[] charToByte = new byte[256];

    // package protected for use by ByteBufferUtil. Do not modify this array !!
    static final char[] byteToChar = new char[16];
    static
    {
        for (char c = 0; c < charToByte.length; ++c)
        {
            if (c >= '0' && c <= '9')
                charToByte[c] = (byte)(c - '0');
            else if (c >= 'A' && c <= 'F')
                charToByte[c] = (byte)(c - 'A' + 10);
            else if (c >= 'a' && c <= 'f')
                charToByte[c] = (byte)(c - 'a' + 10);
            else
                charToByte[c] = (byte)-1;
        }

        for (int i = 0; i < 16; ++i)
        {
            byteToChar[i] = Integer.toHexString(i).charAt(0);
        }
    }

    public static byte[] hexToBytes(String str)
    {
        if (str.length() % 2 == 1)
            throw new NumberFormatException("An hex string representing bytes must have an even length");

        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < bytes.length; i++)
        {
            byte halfByte1 = charToByte[str.charAt(i * 2)];
            byte halfByte2 = charToByte[str.charAt(i * 2 + 1)];
            if (halfByte1 == -1 || halfByte2 == -1)
                throw new NumberFormatException("Non-hex characters in " + str);
            bytes[i] = (byte)((halfByte1 << 4) | halfByte2);
        }
        return bytes;
    }

    public static String bytesToHex(byte... bytes)
    {
        char[] c = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++)
        {
            int bint = bytes[i];
            c[i * 2] = byteToChar[(bint & 0xf0) >> 4];
            c[1 + i * 2] = byteToChar[bint & 0x0f];
        }

        return wrapCharArray(c);
    }

    /**
     * Create a String from a char array with zero-copy (if available), using reflection to access a package-protected constructor of String.
     * */
    public static String wrapCharArray(char[] c)
    {
        if (c == null)
            return null;

        String s = null;

        if (stringConstructor != null)
        {
            try
            {
                s = stringConstructor.newInstance(0, c.length, c);
            }
            catch (Exception e)
            {
                // Swallowing as we'll just use a copying constructor
            }
        }
        return s == null ? new String(c) : s;
    }

    /**
     * Used to get access to protected/private constructor of the specified class
     * @param klass - name of the class
     * @param paramTypes - types of the constructor parameters
     * @return Constructor if successful, null if the constructor cannot be
     * accessed
     */
    public static Constructor getProtectedConstructor(Class klass, Class... paramTypes)
    {
        Constructor c;
        try
        {
            c = klass.getDeclaredConstructor(paramTypes);
            c.setAccessible(true);
            return c;
        }
        catch (Exception e)
        {
            return null;
        }
    }
    
    /**
     * Converts an array of bytes into an array of characters representing the hexadecimal values of each byte in order.
     * The returned array will be double the length of the passed array, as it takes two characters to represent any
     * given byte.
     * 
     * @param data
     *            a byte[] to convert to Hex characters
     * @return A char[] containing hexadecimal characters
     */
    public static char[] encodeHex(byte[] data) {
        return encodeHex(data, true);
    }

    /**
     * Converts an array of bytes into an array of characters representing the hexadecimal values of each byte in order.
     * The returned array will be double the length of the passed array, as it takes two characters to represent any
     * given byte.
     * 
     * @param data
     *            a byte[] to convert to Hex characters
     * @param toLowerCase
     *            <code>true</code> converts to lowercase, <code>false</code> to uppercase
     * @return A char[] containing hexadecimal characters
     * @since 1.4
     */
    public static char[] encodeHex(byte[] data, boolean toLowerCase) {
        return encodeHex(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }

    /**
     * Converts an array of bytes into an array of characters representing the hexadecimal values of each byte in order.
     * The returned array will be double the length of the passed array, as it takes two characters to represent any
     * given byte.
     * 
     * @param data
     *            a byte[] to convert to Hex characters
     * @param toDigits
     *            the output alphabet
     * @return A char[] containing hexadecimal characters
     * @since 1.4
     */
    protected static char[] encodeHex(byte[] data, char[] toDigits) {
        int l = data.length;
        char[] out = new char[l << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return out;
    }

    /**
     * Converts an array of bytes into a String representing the hexadecimal values of each byte in order. The returned
     * String will be double the length of the passed array, as it takes two characters to represent any given byte.
     * 
     * @param data
     *            a byte[] to convert to Hex characters
     * @return A String containing hexadecimal characters
     * @since 1.4
     */
    public static String encodeHexString(byte[] data) {
        return new String(encodeHex(data));
    }

}
