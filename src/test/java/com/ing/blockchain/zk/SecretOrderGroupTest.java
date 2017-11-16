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
import org.junit.Test;

import java.math.BigInteger;

import static org.bouncycastle.pqc.math.linearalgebra.IntegerFunctions.isPrime;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

// Tests to check the generated Secret Order Groups.
// The generator is non-deterministic so we use loops to improve the chance of finding errors.
public class SecretOrderGroupTest {

    // Using small groups so we can calculate the secret order and factorizations
    private SecretOrderGroupGenerator groupGenerator = new SecretOrderGroupGenerator(9);

    private int calculateSecretOrder(int N, int generator) {
        long element = generator;
        for (int i = 1; i < N; i++) {
            element = (element * generator) % N;
            if (element == generator) {
                return i;
            }
        }
        throw new IllegalArgumentException("No order could be found");
    }

    private boolean groupContains(int N, int g, int x) {
        long element = g;
        for (int i = 1; i < N; i++) {
            element = (element * g) % N;
            if (element == x) {
                return true;
            }
        }
        return false;
    }

    private int[] calculateFactorization(int N) {
        for (int i = 2; i <= Math.sqrt(N); i++) {
            if (N % i == 0) {
                return new int[] {i, N / i};
            }
        }
        throw new IllegalArgumentException("No factorization could be found");
    }

    @Test
    public void testSafePrimes() {
        for (int i = 0; i < 100; i++) {
            SecretOrderGroup group = groupGenerator.generate();

            int[] factors = calculateFactorization(group.getN().intValue());

            // Factors of N should be two safe primes
            assertTrue(isPrime(factors[0]));
            assertTrue(isPrime(factors[1]));

            assertTrue(isPrime((factors[0] - 1) / 2));
            assertTrue(isPrime((factors[1] - 1) / 2));
        }
    }

    @Test
    public void testGeneratorOrder() {
        for (int i = 0; i < 200; i++) {
            SecretOrderGroup group = groupGenerator.generate();

            int primes[] = calculateFactorization(group.getN().intValue());

            int p = (primes[0] - 1) / 2;
            int q = (primes[1] - 1) / 2;

            int orderG = calculateSecretOrder(group.getN().intValue(), group.getG().intValue());
            int orderH = calculateSecretOrder(group.getN().intValue(), group.getH().intValue());

            assertEquals(p * q, orderG);
            assertEquals(p * q, orderH);
        }
    }

    @Test
    public void testHIsInG() {
        for (int t = 0; t < 200; t++) {
            SecretOrderGroup group = groupGenerator.generate();

            assertTrue(groupContains(group.getN().intValue(), group.getG().intValue(), group.getH().intValue()));
        }
    }
}
