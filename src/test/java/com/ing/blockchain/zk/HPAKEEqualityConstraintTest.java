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

import com.ing.blockchain.zk.dto.ECProof;
import com.ing.blockchain.zk.dto.SecretOrderGroup;
import org.junit.Test;

import java.math.BigInteger;
import java.security.SecureRandom;

public class HPAKEEqualityConstraintTest {

    @Test
    public void tryEqualityConstraint() {
        SecretOrderGroup group = RangeProofTests.EXAMPLE_GROUP;

        BigInteger p = group.getN();

        BigInteger[] g = new BigInteger[2];
        g[0] = group.getG();
        g[1] = g[0].modPow(new BigInteger("781654"), p);
        BigInteger[] h = new BigInteger[2];
        h[0] = group.getH();
        h[1] = g[0].modPow(new BigInteger("1574788963525"), p);
        BigInteger x = new BigInteger("198741361684"); // secret value

        SecureRandom random = new SecureRandom();
        BigInteger[] y = new BigInteger[2];
        y[0] = new BigInteger("8547456354654");
        y[1] = new BigInteger("1547896325698523");

        SecretOrderGroup group2 = new SecretOrderGroup(group.getN(), g[1], h[1]);
        BigInteger E = TTPGenerator.commit(group, x, y[0]).getCommitmentValue();
        BigInteger F = TTPGenerator.commit(group2, x, y[1]).getCommitmentValue();

        ECProof proof = HPAKEEqualityConstraint.calculateZeroKnowledgeProof(p, g[0], g[1], h[0], h[1], x, y[0], y[1], random);

        HPAKEEqualityConstraint.validateZeroKnowledgeProof(p, g[0], g[1], h[0], h[1], E, F, proof);
    }
}
