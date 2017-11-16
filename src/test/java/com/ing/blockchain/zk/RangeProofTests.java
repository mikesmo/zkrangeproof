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

import com.ing.blockchain.zk.dto.*;
import com.ing.blockchain.zk.exception.ZeroKnowledgeException;
import org.junit.Test;

import java.math.BigInteger;
import java.security.SecureRandom;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;

public class RangeProofTests {

    static final SecretOrderGroup EXAMPLE_GROUP = new SecretOrderGroup(
            new BigInteger("123763483659823661164839153854113"),
            new BigInteger("9978076495933337078596144096749"),
            new BigInteger("46959937887401751832025265468109"));

    private BigInteger[] toArray(RangeProof proof) {
        BigInteger[] res = new BigInteger[22];
        res[0] = proof.getcPrime();
        res[1] = proof.getcPrime1();
        res[2] = proof.getcPrime2();;
        res[3] = proof.getcPrime3();
        res[4] = proof.getSqrProof3().getF();
        res[5] = proof.getSqrProof3().getECProof().getC();
        res[6] = proof.getSqrProof3().getECProof().getD();
        res[7] = proof.getSqrProof3().getECProof().getD1();
        res[8] = proof.getSqrProof3().getECProof().getD2();
        res[9] = proof.getSqrProof4().getF();
        res[10] = proof.getSqrProof4().getECProof().getC();
        res[11] = proof.getSqrProof4().getECProof().getD();
        res[12] = proof.getSqrProof4().getECProof().getD1();
        res[13] = proof.getSqrProof4().getECProof().getD2();
        res[14] = proof.getEcProof2().getC();
        res[15] = proof.getEcProof2().getD();
        res[16] = proof.getEcProof2().getD1();
        res[17] = proof.getEcProof2().getD2();
        res[18] = proof.getU();
        res[19] = proof.getV();
        res[20] = proof.getX();
        res[21] = proof.getY();
        return res;
    }

    private RangeProof fromArray(BigInteger[] proof) {
        ECProof ecProof2 = new ECProof(proof[14], proof[15], proof[16], proof[17]);
        ECProof ecProof3 = new ECProof(proof[5], proof[6], proof[7], proof[8]);
        ECProof ecProof4 = new ECProof(proof[10], proof[11], proof[12], proof[13]);
        SquareProof sqrProof3 = new SquareProof(proof[4], ecProof3);
        SquareProof sqrProof4 = new SquareProof(proof[9], ecProof4);
        return new RangeProof(ecProof2, sqrProof3, sqrProof4, proof[0], proof[1], proof[2], proof[3], proof[20], proof[21], proof[18], proof[19]);
    }

    private void checkProofRejection(BigInteger[] fakeProofArray, Commitment c, ClosedRange range) {
        RangeProof fakeProof = fromArray(fakeProofArray);
        try {
            HPAKErangeProof.validateRangeProof(fakeProof, c, range);
            fail("No error at fake proof");
        } catch (ZeroKnowledgeException e) {
            System.out.println("Fake proof was rejected");
        }
    }

    @Test
    public void testValidRangeProof() throws Exception {
        BigInteger x = new BigInteger("50");

        TTPMessage message = TTPGenerator.generateTTPMessage(x, EXAMPLE_GROUP);
        ClosedRange range = ClosedRange.of("10", "100");

        RangeProof rangeProof = HPAKErangeProof.calculateRangeProof(message, range);

        HPAKErangeProof.validateRangeProof(rangeProof, message.getCommitment(), range);

        System.out.println("C = " + message.getCommitment().getCommitmentValue());
        BigInteger[] bigIntegers = toArray(rangeProof);
        for (int i = 0; i < bigIntegers.length; i++) {
            System.out.println(bigIntegers[i]);
        }
    }


    @Test
    public void testAllFieldsCheckedForRangeProof() throws Exception {
        BigInteger x = new BigInteger("50");

        TTPMessage message = TTPGenerator.generateTTPMessage(x, EXAMPLE_GROUP);
        ClosedRange range = ClosedRange.of("10", "100");
        BigInteger[] proof = toArray(HPAKErangeProof.calculateRangeProof(message, range));

        for (int i = 0; i < proof.length; i++) {
            System.out.println("Modifying field " + i);
            BigInteger realValue = proof[i];
            proof[i] = BigInteger.ONE;
            checkProofRejection(proof, message.getCommitment(), range);
            proof[i] = BigInteger.ZERO;
            checkProofRejection(proof, message.getCommitment(), range);
            proof[i] = message.getCommitment().getCommitmentValue();
            checkProofRejection(proof, message.getCommitment(), range);
            proof[i] = realValue;
        }
    }

    @Test (expected = ZeroKnowledgeException.class)
    public void testModifiedRange() throws Exception {
        BigInteger x = new BigInteger("50");

        TTPMessage message = TTPGenerator.generateTTPMessage(x, EXAMPLE_GROUP);
        ClosedRange range = ClosedRange.of("10", "100");
        RangeProof rangeProof = HPAKErangeProof.calculateRangeProof(message, range);

        ClosedRange fakeRange = ClosedRange.of("51", "100");
        HPAKErangeProof.validateRangeProof(rangeProof, message.getCommitment(), fakeRange);
    }

    @Test (expected = ZeroKnowledgeException.class)
    public void testRangeTooHigh() throws Exception {
        BigInteger x = new BigInteger("50");
        TTPMessage message = TTPGenerator.generateTTPMessage(x, EXAMPLE_GROUP);
        ClosedRange range = ClosedRange.of("51", "100");
        RangeProof rangeProof = HPAKErangeProof.calculateRangeProof(message, range);
        HPAKErangeProof.validateRangeProof(rangeProof, message.getCommitment(), range);
    }

    @Test (expected = ZeroKnowledgeException.class)
    public void testRangeTooLow() throws Exception {
        BigInteger x = new BigInteger("50");
        TTPMessage message = TTPGenerator.generateTTPMessage(x, EXAMPLE_GROUP);
        ClosedRange range = ClosedRange.of("10", "49");
        RangeProof rangeProof = HPAKErangeProof.calculateRangeProof(message, range);
        HPAKErangeProof.validateRangeProof(rangeProof, message.getCommitment(), range);
    }

    @Test
    public void testLargeValue() throws Exception {
        BigInteger largeValue = BigInteger.valueOf(2).pow(128);
        TTPMessage message = TTPGenerator.generateTTPMessage(largeValue, EXAMPLE_GROUP);

        // Large range
        ClosedRange range = ClosedRange.of(largeValue.shiftRight(10), largeValue.shiftLeft(10));
        RangeProof rangeProof = HPAKErangeProof.calculateRangeProof(message, range);
        HPAKErangeProof.validateRangeProof(rangeProof, message.getCommitment(), range);

        // Small range
        range = ClosedRange.of(largeValue, largeValue);
        rangeProof = HPAKErangeProof.calculateRangeProof(message, range);
        HPAKErangeProof.validateRangeProof(rangeProof, message.getCommitment(), range);
    }

    @Test
    public void testRangeProofMmaker() {
        BigInteger two = new BigInteger("2");
        BigInteger three = new BigInteger("3");
        for (int i = 1; i < 10; i++) {
            BigInteger requiredSum = three.modPow(BigInteger.valueOf(i), two.pow(i));
            BigInteger[] m = HPAKErangeProof.takeRandomM(requiredSum);
            // Verify : m1 + m2 + m4^2 = sum
            assertEquals(requiredSum, m[0].add(m[1]).add(m[2].multiply(m[2])));
        }
    }
}