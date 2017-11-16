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

package com.ing.blockchain.zk;

import com.ing.blockchain.zk.dto.SecretOrderGroup;
import com.ing.blockchain.zk.dto.SquareProof;
import org.junit.Test;

import java.math.BigInteger;
import java.security.SecureRandom;

public class HPAKESquareTest {

    @Test
    public void trySquaring() {
        SecretOrderGroup group = RangeProofTests.EXAMPLE_GROUP;

        BigInteger N = group.getN();
        BigInteger g = group.getG();
        BigInteger h = group.getH();
        BigInteger x = new BigInteger("198741361684");
        BigInteger y = new BigInteger("65132818281239");
        BigInteger xSquare = x.multiply(x);
        BigInteger commitment = TTPGenerator.commit(group, xSquare, y).getCommitmentValue();

        SecureRandom random = new SecureRandom();
        SquareProof proof = HPAKESquare.calculateZeroKnowledgeProof(N, g, h, x, y, random);
        HPAKESquare.validateZeroKnowledgeProof(N, g, h, commitment, proof);
    }
}
