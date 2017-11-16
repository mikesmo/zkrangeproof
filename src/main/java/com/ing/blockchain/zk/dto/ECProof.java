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

public class ECProof implements Serializable {
    private BigInteger c, D, D1, D2;

    public ECProof(BigInteger c, BigInteger D, BigInteger D1, BigInteger D2) {
        this.c = c;   // Challenge for prover
        this.D = D;   // Number that hides the secret value (proved to be the same in both commitments)
        this.D1 = D1; // Number that hides the random value in commitment 1
        this.D2 = D2; // Number that hides the random value in commitment 2
    }

    public BigInteger getC() {
        return c;
    }

    public BigInteger getD() {
        return D;
    }

    public BigInteger getD1() {
        return D1;
    }

    public BigInteger getD2() {
        return D2;
    }
}