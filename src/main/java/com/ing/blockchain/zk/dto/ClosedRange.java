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

import java.math.BigInteger;

/**
 * Closed interval of big integers.
 */
public class ClosedRange {
    private final BigInteger start;
    private final BigInteger end;

    private ClosedRange(final BigInteger lo, final BigInteger hi) {
        start = lo;
        end = hi;
    }

    public static ClosedRange of(final BigInteger lo, final BigInteger hi) {
        return new ClosedRange(lo, hi);
    }

    public static ClosedRange of(final String lo, final String hi) {
        return new ClosedRange(new BigInteger(lo), new BigInteger(hi));
    }

    public BigInteger getStart() {
        return start;
    }

    public BigInteger getEnd() {
        return end;
    }

    public boolean contains(final BigInteger n) {
        return start.compareTo(n) <=0 && n.compareTo(end) <= 0;
    }

    public String toString() {
        return String.format("[%s, %s]", start.toString(), end.toString());
    }
}
