/*
 * Copyright 2017 ING Bank N.V.
 * This file is part of the go-ethereum library.
 *
 * The go-ethereum library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The go-ethereum library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the go-ethereum library. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.ing.blockchain.zk.util;

import java.math.BigInteger;

public class BigIntUtil {

    private static final int DEFAULT_DIGITS_TO_SHOW = 4;

    private BigIntUtil() {
        throw new UnsupportedOperationException("utility class");
    }

    public static String shortBigInt(final BigInteger n, final int showDigits) {
        final String s = n.abs().toString();
        final String sign = n.signum() < 0 ? "-" : "";
        final int numChars = s.length();
        if (numChars <= (2 * showDigits)) {
            return String.format("%s%s", sign, s);
        } else {
            return String.format("%s%s…%s",
                    sign, s.substring(0, showDigits), s.substring(numChars - showDigits, numChars));
        }
    }

    public static String shortBigInt(final BigInteger n) {
        return shortBigInt(n, DEFAULT_DIGITS_TO_SHOW);
    }

    // Babylonian method for approximating square root
    private static BigInteger approximateSquareRoot(BigInteger input) {
        if (input.compareTo(BigInteger.ONE) == -1) {
            return BigInteger.ZERO;
        }

        BigInteger prev2 = BigInteger.ZERO;
        BigInteger prev = BigInteger.ZERO;
        BigInteger approx = BigInteger.ZERO.setBit(input.bitLength() / 2);

        // To improve approximation of a = sqrt(i), set it to (i / a + a) / 2
        // Stop when approx does not change or starts alternating
        while (!approx.equals(prev) && !approx.equals(prev2)) {
            prev2 = prev;
            prev = approx;
            approx = input.divide(approx).add(approx).shiftRight(1);
        }
        return approx;
    }

    public static BigInteger floorSquareRoot(BigInteger input) {
        if (input.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException("Negative number does not have square root");
        }
        BigInteger res = approximateSquareRoot(input);
        if (res.multiply(res).compareTo(input) >= 1) {
            res = res.subtract(BigInteger.ONE);
        }
        return res;
    }

    public static BigInteger divMod(BigInteger a, BigInteger b, BigInteger N) {
        return a.multiply(b.modInverse(N)).mod(N);
    }

}
