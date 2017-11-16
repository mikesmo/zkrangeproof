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

import com.ing.blockchain.zk.dto.Commitment;
import com.ing.blockchain.zk.dto.SecretOrderGroup;
import com.ing.blockchain.zk.dto.TTPMessage;
import org.bouncycastle.util.BigIntegers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.security.SecureRandom;

import static java.math.BigInteger.ONE;

public class TTPGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecretOrderGroup.class);
    private static final BigInteger TWO = BigInteger.valueOf(2);

    // Security parameter that determines size of key in generated commitments
    public static final int s = 552;

    public static TTPMessage generateTTPMessage(BigInteger secretValue) {
        System.out.println("Generating Secret Order Group.");
        SecretOrderGroup group = new SecretOrderGroupGenerator().generate();
        return generateTTPMessage(secretValue, group);
    }

    public static TTPMessage generateTTPMessage(BigInteger secretValue, SecretOrderGroup group) {
        System.out.println("Generating TTP Message");

        BigInteger secretRandom = TTPGenerator.generateKey(group.getN(), new SecureRandom());
        Commitment commitment = commit(group, secretValue, secretRandom);

        return new TTPMessage(commitment, secretValue, secretRandom);
    }

    /**
     * Computes a Fujisaki-Okamoto commitment.
     *
     * Fujisaki, E., & Okamoto, T. (1997). Statistical zero knowledge protocols to prove modular polynomial relations.
     * In Advances in Cryptology-CRYPTO'97: 17th Annual International Cryptology Conference, Santa Barbara, California,
     * USA, August 1997. Proceedings (p. 16). Springer Berlin/Heidelberg.
     *
     * @param valueToHide the value to hide
     * @param key         the commitment key
     * @return the commitment value
     */
    public static Commitment commit(final SecretOrderGroup group, final BigInteger valueToHide, final BigInteger key) {
        final BigInteger N = group.getN();
        final BigInteger g = group.getG();
        final BigInteger h = group.getH();
        final BigInteger commitment = g.modPow(valueToHide, N).multiply(h.modPow(key, N)).mod(N); // g^m*h^r mod N

        return new Commitment(group, commitment);
    }

    // Generate a random value between - 2(power s) * N + 1 and 2(power s) * N - 1.
    // This range is used for generating commitment keys.
    public static BigInteger generateKey(BigInteger N, SecureRandom random) {
        BigInteger integerMax = TWO.pow(s).multiply(N).subtract(ONE);
        return BigIntegers.createRandomInRange(integerMax.negate(), integerMax, random);
    }
}
