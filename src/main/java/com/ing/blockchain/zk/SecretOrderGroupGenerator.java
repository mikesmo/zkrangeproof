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
import org.bouncycastle.util.BigIntegers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.security.SecureRandom;

import static com.ing.blockchain.zk.util.TimerUtil.timeAndLog;
import static java.math.BigInteger.ONE;

public class SecretOrderGroupGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecretOrderGroupGenerator.class);
    private static final BigInteger TWO = BigInteger.valueOf(2);

    private final int bitLength;
    private final int certainty;

    private static SecureRandom rnd = new SecureRandom();

    public SecretOrderGroupGenerator() {
        this(1024);
    }

    public SecretOrderGroupGenerator(int bitlength) {
        this(bitlength, 50);
    }

    public SecretOrderGroupGenerator(int bitlen, int cert) {
        bitLength = bitlen;
        certainty = cert;
    }

    public SecretOrderGroup generate() {

        BigInteger[] safePrimes = generateSafePrimes(bitLength, certainty);
        BigInteger[] generators = findGenerators(safePrimes);

        BigInteger N = safePrimes[0].multiply(safePrimes[1]);

        return new SecretOrderGroup(N, generators[0], generators[1]);
    }


    private static BigInteger[] generateSafePrimes(int bitlength, int certainty) {
        BigInteger P = timeAndLog("Generating safe prime 1", () -> generateSafePrime(bitlength - 1, certainty));

        BigInteger Q = timeAndLog("Generating safe prime 2", () -> {
            BigInteger result;
            do {
                result = generateSafePrime(bitlength - 1, certainty);
            } while (result.equals(P));

            return result;
        });

        return new BigInteger[]{P, Q};
    }

    /**
     * Generates a safe prime P such that (P - 1) / 2 is also prime.
     */
    private static BigInteger generateSafePrime(int bitlength, int certainty) {
        BigInteger bigPrime, smallPrime;
        int attempts = 0;
        do {
            attempts++;
            bigPrime = new BigInteger(bitlength, certainty, rnd);
            smallPrime = (bigPrime.subtract(ONE)).divide(TWO);

            if (attempts % 100 == 0) {
                LOGGER.debug("#attempts = " + attempts);
            }

            // check whether smallPrime is also prime, otherwise generate new bigPrime
        } while (!smallPrime.isProbablePrime(certainty));

        LOGGER.debug("Found safe prime after " + attempts + " attempts");
        return bigPrime;
    }

    // Find two generators of G_pq.
    // This is step 2 to 4 in the "Set-up procedure" in the paper from Fujisaki and Okamoto, page 19
    // Therefore the generators for G_pq are called b0, b1 instead of g, h
    private static BigInteger[] findGenerators(BigInteger[] safePrimes) {
        BigInteger P = safePrimes[0];
        BigInteger Q = safePrimes[1];
        BigInteger p = safePrimes[0].subtract(ONE).divide(TWO);
        BigInteger q = safePrimes[1].subtract(ONE).divide(TWO);
        BigInteger N = safePrimes[0].multiply(safePrimes[1]);

        // Step 2
        BigInteger g_p, g_q;

        g_p = findGeneratorForSafePrime(P);
        g_p = g_p.modPow(BigIntegers.createRandomInRange(ONE, p.subtract(ONE), rnd), P);

        g_q = findGeneratorForSafePrime(Q);
        g_q = g_q.modPow(BigIntegers.createRandomInRange(ONE, q.subtract(ONE), rnd), Q);

        // Step 3
        BigInteger[] bezout = extendedGCDBezout(P,Q);
        BigInteger b0 = g_p.multiply(bezout[1]).multiply(Q).add(g_q.multiply(bezout[0]).multiply(P)).mod(N);

        // Step 4
        BigInteger alpha;
        do {
            // We use min(p,q) as minimum for alpha, small numbers would make it easier to find log_b1(b0)
            alpha = BigIntegers.createRandomInRange(p.min(q), p.multiply(q), rnd);

        } while (alpha.mod(p).equals(BigInteger.ZERO) || alpha.mod(q).equals(BigInteger.ZERO));
        // At b1 := b0^alpha, alpha should not be a multiple of p or q, otherwise b1 only generates a small subgroup

        BigInteger b1 = b0.modPow(alpha, N);

        return new BigInteger[]{b0, b1};
    }

    /* For the given safe prime P, find a generator for a subgroup of order (P - 1) / 2. */
    private static BigInteger findGeneratorForSafePrime(BigInteger P) {
        // If P is a safe prime with p = (P - 1) / 2, then generated groups modulo P have order 1, 2, p or 2p
        // According to Fujisaki and Okamoto we need a group of order p.
        // To know that a generator does not have order 1 or 2, we check g^2 != 1
        // To know that a generator does not have order 2p, we check that g^p == 1

        BigInteger p = P.subtract(ONE).divide(TWO);
        BigInteger g;
        do {
            g = BigIntegers.createRandomInRange(TWO, P.subtract(TWO), rnd);
        } while (!g.modPow(p, P).equals(ONE) || g.modPow(TWO, P).equals(ONE));

        return g;
    }

    private static BigInteger[] extendedGCDBezout(BigInteger a, BigInteger b) {
        BigInteger s0 = BigInteger.ONE;
        BigInteger s1 = BigInteger.ZERO;
        BigInteger t0 = BigInteger.ZERO;
        BigInteger t1 = BigInteger.ONE;
        BigInteger r0 = a;
        BigInteger r1 = b;
        BigInteger temp;

        while (!r1.equals(BigInteger.ZERO)) {
            BigInteger[] quotient = r0.divideAndRemainder(r1);
            r0 = r1;
            r1 = quotient[1];

            temp = s1;
            s1 = s0.subtract(quotient[0].multiply(s1));
            s0 = temp;

            temp = t1;
            t1 = t0.subtract(quotient[0].multiply(t1));
            t0 = temp;
        }

        return new BigInteger[]{s0, t0};
    }
}
