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
import com.ing.blockchain.zk.util.BigIntUtil;
import com.ing.blockchain.zk.util.DigestUtil;
import org.bouncycastle.util.BigIntegers;

import java.math.BigInteger;
import java.security.SecureRandom;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;

/**
 * Range proof according to the algorithm described in Peng and Bao:
 *
 * Kun Peng and Bao Feng. "An efficient range proof scheme."
 * IEEE Second International Conference on Social Computing, 2010
 */
public class HPAKErangeProof {
    private static final BigInteger TWO = BigInteger.valueOf(2);

    // Security parameters
    public static final BigInteger k1 = TWO.pow(160);
    public static final BigInteger k2 = TWO.pow(2048);

    // Proof that two commitments hide the same secret is denoted as EL(x,r1,r2 | g1,h1,g2,h2 | y1,y2)
    private static ECProof EL(BigInteger x, BigInteger r1, BigInteger r2, BigInteger g1, BigInteger h1,
                              BigInteger g2, BigInteger h2, BigInteger y1, BigInteger y2, BigInteger N,
                              SecureRandom random) {
        return HPAKEEqualityConstraint.calculateZeroKnowledgeProof(N, g1, g2, h1, h2, x, r1, r2, random);
    }

    // Proof that a committed number x^2 is a square is denoted as SQR(x,r | g,h | y)
    private static SquareProof SQR(BigInteger x, BigInteger r, BigInteger g, BigInteger h, BigInteger y, BigInteger N,
                                   SecureRandom random) {
        return HPAKESquare.calculateZeroKnowledgeProof(N, g, h, x, r, random);
    }

    /**
     * @param ttpMessage secret message from the trusted third party
     * @param range the boundaries of the range
     * @return
     */
    public static RangeProof calculateRangeProof(TTPMessage ttpMessage, ClosedRange range) {

        Commitment commitment = ttpMessage.getCommitment();

        BigInteger N = commitment.getGroup().getN();
        BigInteger g = commitment.getGroup().getG();
        BigInteger h = commitment.getGroup().getH();
        BigInteger c = commitment.getCommitmentValue();

        BigInteger a = range.getStart();
        BigInteger b = range.getEnd();
        BigInteger m = ttpMessage.getX(); // number in range
        BigInteger r = ttpMessage.getY(); // commitment key

        SecureRandom random = new SecureRandom();

        // Step 1
        BigInteger c1 = BigIntUtil.divMod(c, g.modPow(a.subtract(ONE), N), N);
        BigInteger c2 = BigIntUtil.divMod(g.modPow(b.add(ONE), N), c, N);

        // Step 2
        BigInteger rPrime = BigIntegers.createRandomInRange(ZERO, k2, random);
        BigInteger cPrime = c1.modPow(b.subtract(m).add(ONE), N).multiply(h.modPow(rPrime, N)).mod(N);
        ECProof equalityProof2 = EL(b.subtract(m).add(ONE), r.negate(), rPrime, g, h, c1, h, c2, cPrime, N, random);

        // Step 3
        BigInteger w = BigIntegers.createRandomInRange(ONE, k2, random);
        BigInteger rPrimePrime = BigIntegers.createRandomInRange(ZERO, k2, random);
        BigInteger cPrimePrime = cPrime.modPow(w.multiply(w), N).multiply(h.modPow(rPrimePrime, N)).mod(N);
        SquareProof sqrProof3 = SQR(w, rPrimePrime, cPrime, h, cPrimePrime, N, random);

        // Step 4
        BigInteger[] m_ = takeRandomM(w.multiply(w).multiply(m.subtract(a).add(ONE)).multiply(b.subtract(m).add(ONE)));
        BigInteger[] r_ = takeRandomR(w.multiply(w).multiply(b.subtract(m).add(ONE).multiply(r).add(rPrime)).add(rPrimePrime), N);

        BigInteger m1 = m_[0];
        BigInteger m2 = m_[1];
        BigInteger m4 = m_[2];
        BigInteger m3 = m4.multiply(m4);

        BigInteger r1 = r_[0];
        BigInteger r2 = r_[1];
        BigInteger r3 = r_[2];

        BigInteger cPrime1 = g.modPow(m1, N).multiply(h.modPow(r1, N)).mod(N);
        BigInteger cPrime2 = g.modPow(m2, N).multiply(h.modPow(r2, N)).mod(N);
        BigInteger cPrime3 = BigIntUtil.divMod(cPrimePrime, cPrime1.multiply(cPrime2), N);

        SquareProof sqrProof4 = SQR(m4, r3, g, h, cPrime3, N, random);

        // Step 5 (modified to make the proof non-interactive)
        BigInteger s = DigestUtil.calculateHash(c1).mod(k1).add(ONE);
        BigInteger t = DigestUtil.calculateHash(c2).mod(k1).add(ONE);

        // Step 6
        BigInteger x = s.multiply(m1).add(m2).add(m3);
        BigInteger y = m1.add(t.multiply(m2)).add(m3);
        BigInteger u = s.multiply(r1).add(r2).add(r3);
        BigInteger v = r1.add(t.multiply(r2)).add(r3);

        return new RangeProof(equalityProof2, sqrProof3, sqrProof4, cPrime, cPrime1, cPrime2, cPrime3, x, y, u, v);
    }

    public static void validateRangeProof(RangeProof proof, Commitment commitment, ClosedRange range) {

        // Commitment
        BigInteger N = commitment.getGroup().getN();
        BigInteger g = commitment.getGroup().getG();
        BigInteger h = commitment.getGroup().getH();
        BigInteger c = commitment.getCommitmentValue();

        // Proof
        ECProof equalityProof = proof.getEcProof2();
        SquareProof sqrProof1 = proof.getSqrProof3();
        SquareProof sqrProof2 = proof.getSqrProof4();
        BigInteger cPrime = proof.getcPrime();
        BigInteger cPrime1 = proof.getcPrime1();
        BigInteger cPrime2 = proof.getcPrime2();
        BigInteger cPrime3 = proof.getcPrime3();
        BigInteger x = proof.getX();
        BigInteger y = proof.getY();
        BigInteger u = proof.getU();
        BigInteger v = proof.getV();

        // Derived information
        BigInteger c1 = BigIntUtil.divMod(c, g.modPow(range.getStart().subtract(ONE), N), N); // Check 6 in the paper
        BigInteger c2 = BigIntUtil.divMod(g.modPow(range.getEnd().add(ONE), N), c, N); // Check 7 in the paper
        BigInteger cPrimePrime = cPrime1.multiply(cPrime2).multiply(cPrime3).mod(N); // Check 8 in the paper
        BigInteger s = DigestUtil.calculateHash(c1).mod(k1).add(ONE);
        BigInteger t = DigestUtil.calculateHash(c2).mod(k1).add(ONE);

        try {
            HPAKEEqualityConstraint.validateZeroKnowledgeProof(N, g, c1, h, h, c2, cPrime, equalityProof);
        } catch (ZeroKnowledgeException e) {
            throw new ZeroKnowledgeException("Crypto Exception at EL check (3)", e);
        }

        try {
            HPAKESquare.validateZeroKnowledgeProof(N, cPrime, h, cPrimePrime, sqrProof1);
        } catch (ZeroKnowledgeException e) {
            throw new ZeroKnowledgeException("Crypto Exception at SQR check (4)", e);
        }

        try {
            HPAKESquare.validateZeroKnowledgeProof(N, g, h, cPrime3, sqrProof2);
        } catch (ZeroKnowledgeException e) {
            throw new ZeroKnowledgeException("Crypto Exception at SQR check (5)", e);
        }



        BigInteger nineLeft = cPrime1.modPow(s, N).multiply(cPrime2).multiply(cPrime3).mod(N);
        BigInteger nineRight = g.modPow(x, N).multiply(h.modPow(u, N)).mod(N);

        if (!nineLeft.equals(nineRight)) {
            throw new ZeroKnowledgeException("Crypto Exception at check (9)");
        }

        BigInteger tenLeft = cPrime1.multiply(cPrime2.modPow(t, N)).multiply(cPrime3).mod(N);
        BigInteger tenRight = g.modPow(y, N).multiply(h.modPow(v, N)).mod(N);
        if (!tenLeft.equals(tenRight)) {
            throw new ZeroKnowledgeException("Crypto Exception at check (10)");
        }

        if (x.compareTo(ZERO) <= 0) {
            throw new ZeroKnowledgeException("Crypto Exception at check (11)");
        }

        if (y.compareTo(ZERO) <= 0) {
            throw new ZeroKnowledgeException("Crypto Exception at check (12)");
        }
    }

    // Randomly choose m1, m2, m4 smaller than (non-negative) sum, such that m1 + m2 + m4^2 = sum
    static BigInteger[] takeRandomM(BigInteger sum) {
        SecureRandom random = new SecureRandom();
        BigInteger maxForM4 = BigIntUtil.floorSquareRoot(sum);
        BigInteger m4 = BigIntegers.createRandomInRange(ZERO, maxForM4, random);
        BigInteger remaining = sum.subtract(m4.multiply(m4));
        BigInteger m1 = BigIntegers.createRandomInRange(ZERO, remaining, random);
        BigInteger m2 = remaining.subtract(m1);
        return new BigInteger[]{m1, m2, m4};
    }

    // Randomly choose r1, r2, r3 so that r1 + r2 + r3 = sum
    private static BigInteger[] takeRandomR(BigInteger sum, BigInteger N) {
        SecureRandom random = new SecureRandom();
        BigInteger res0 = BigIntegers.createRandomInRange(N.negate(), N, random);
        BigInteger res1 = BigIntegers.createRandomInRange(N.negate(), N, random);
        BigInteger res2  = sum.subtract(res0).subtract(res1);
        return new BigInteger[]{ res0, res1, res2 };
    }
}
