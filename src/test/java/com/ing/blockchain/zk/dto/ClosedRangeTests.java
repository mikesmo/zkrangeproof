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

package com.ing.blockchain.zk.dto;

import com.ing.blockchain.zk.dto.ClosedRange;
import org.junit.Test;

import java.math.BigInteger;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link ClosedRange}.
 */
public class ClosedRangeTests {
    @Test
    public void canCreateRangeFromStrings() {
        final ClosedRange r = ClosedRange.of("25", "75");

        assertThat(r.getStart(), is(new BigInteger("25")));
        assertThat(r.getEnd(), is(new BigInteger("75")));
    }

    @Test
    public void canCreateEmptyRange() {
        final ClosedRange r = ClosedRange.of("3", "2");

        assertThat(r.getStart(), is(new BigInteger("3")));
        assertThat(r.getEnd(), is(new BigInteger("2")));
    }

    @Test
    public void testLowerBound() {
        final ClosedRange r = ClosedRange.of("25", "75");

        assertFalse(r.contains(new BigInteger("24")));
        assertTrue(r.contains(new BigInteger("25")));
        assertTrue(r.contains(new BigInteger("26")));
    }

    @Test
    public void testUpperBound() {
        final ClosedRange r = ClosedRange.of("25", "75");

        assertTrue(r.contains(new BigInteger("74")));
        assertTrue(r.contains(new BigInteger("75")));
        assertFalse(r.contains(new BigInteger("76")));
    }

    @Test
    public void testStringRepresentation() {
        final ClosedRange r = ClosedRange.of("25", "75");

        assertThat(r.toString(), is("[25, 75]"));
    }
}
