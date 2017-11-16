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

import org.junit.Test;

import java.math.BigInteger;

import static com.ing.blockchain.zk.util.BigIntUtil.shortBigInt;
import static java.math.BigInteger.ONE;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link BigIntUtil}.
 */
public class BigIntUtilTests {

    @Test
    public void testShortNumbers() {
        assertThat(shortBigInt(BigInteger.ZERO), is("0"));
        assertThat(shortBigInt(ONE), is("1"));
        assertThat(shortBigInt(ONE.negate()), is("-1"));
    }

    @Test
    public void testPositiveNumbers() {
        assertThat(shortBigInt(new BigInteger("1234"), 2), is("1234"));
        assertThat(shortBigInt(new BigInteger("12345"), 2), is("12…45"));
        assertThat(shortBigInt(new BigInteger("123456789"), 2), is("12…89"));
    }

    @Test
    public void testNegativeNumbers() {
        assertThat(shortBigInt(new BigInteger("-1234"), 2), is("-1234"));
        assertThat(shortBigInt(new BigInteger("-12345"), 2), is("-12…45"));
        assertThat(shortBigInt(new BigInteger("-123456789"), 2), is("-12…89"));
    }

    private void verifyFloorSqrt(BigInteger input) {
        BigInteger output = BigIntUtil.floorSquareRoot(input);
        assertTrue(output.multiply(output).compareTo(input) <= 0);
        assertTrue(output.add(ONE).multiply((output.add(ONE))).compareTo(input) > 0);
    }

    @Test
    public void testSqrtNotExact() {
        verifyFloorSqrt(new BigInteger("3532489572394857329447523980653295732948564327985632875639876287562389756897326587936589721618"));
    }

    @Test
    public void testSqrtRounding() {
        BigInteger sqrt = new BigInteger("123079857394502345");
        BigInteger square = sqrt.multiply(sqrt);
        verifyFloorSqrt(square);
        verifyFloorSqrt(square.subtract(ONE));
        verifyFloorSqrt(square.add(ONE));
    }

    @Test
    public void testSqrtSmall() {
        for (int i = 0; i < 25; i++) {
            verifyFloorSqrt(BigInteger.valueOf(i));
        }
    }

    @Test
    public void testSqrtLarge() {
        verifyFloorSqrt(BigInteger.valueOf(2).pow(10000));
        verifyFloorSqrt(BigInteger.valueOf(2).pow(10000).subtract(ONE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSqrtNegative() {
        verifyFloorSqrt(new BigInteger("-1"));
    }
}
