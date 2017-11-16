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

import java.io.Serializable;
import java.math.BigInteger;

public class RangeProof implements Serializable {

    private ECProof ecProof2;
    private SquareProof sqrProof3, sqrProof4;
    private BigInteger cPrime, cPrime1, cPrime2, cPrime3, x, y, u, v;

    public RangeProof(ECProof ecProof2, SquareProof sqrProof3, SquareProof sqrProof4,
                      BigInteger cPrime, BigInteger cPrime1, BigInteger cPrime2, BigInteger cPrime3,
                      BigInteger x, BigInteger y, BigInteger u, BigInteger v) {
        this.ecProof2 = ecProof2;       // Proof that cPrime hides the same number as c2, which is (b - m + 1)
        this.sqrProof3 = sqrProof3;     // Proof that cPrimePrime hides a square (w^2) in base cPrime
        this.sqrProof4 = sqrProof4;     // Proof that cPrime3 hides a square (m3 = m4^2)
        this.cPrime = cPrime;           // Commitment to (m - a + 1)(b - m + 1); which is (b - m + 1) in base c1
        this.cPrime1 = cPrime1;         // Commitment to m1
        this.cPrime2 = cPrime2;         // Commitment to m2
        this.cPrime3 = cPrime3;         // Commitment to m3, with m1 + m2 + m3 = w^2(m - a + 1)(b - m + 1)
        this.x = x;                     // Value hidden in commitment (cPrime1 * s + cPrime2 + cPrime3)
        this.y = y;                     // Value hidden in commitment (cPrime1 + cPrime2 * t + cPrime3)
        this.u = u;                     // Commitment key in commitment (cPrime1 * s + cPrime2 + cPrime3)
        this.v = v;                     // Commitment key in commitment (cPrime1 + cPrime2 * t + cPrime3)
    }

    public BigInteger getcPrime() {
        return cPrime;
    }

    public BigInteger getcPrime1() {
        return cPrime1;
    }

    public BigInteger getcPrime2() {
        return cPrime2;
    }

    public BigInteger getcPrime3() {
        return cPrime3;
    }

    public BigInteger getU() {
        return u;
    }

    public BigInteger getV() {
        return v;
    }

    public BigInteger getX() {
        return x;
    }

    public BigInteger getY() {
        return y;
    }

    public ECProof getEcProof2() {
        return ecProof2;
    }

    public SquareProof getSqrProof3() {
        return sqrProof3;
    }

    public SquareProof getSqrProof4() {
        return sqrProof4;
    }
}
