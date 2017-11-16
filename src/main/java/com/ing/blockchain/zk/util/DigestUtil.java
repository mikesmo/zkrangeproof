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

package com.ing.blockchain.zk.util;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.util.Arrays;

import java.math.BigInteger;

public class DigestUtil {
    private DigestUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    private static void update(org.bouncycastle.crypto.Digest digest, BigInteger ... bigIntegers) {
        for (BigInteger bigInt : bigIntegers) {
            byte[] encodedBigInt = bigInt.toByteArray();
            update(digest, encodedBigInt);
            Arrays.fill(encodedBigInt, (byte) 0);
        }
    }

    public static BigInteger calculateHash(BigInteger ... bigIntegers) {
        Digest digest = new SHA256Digest();
        DigestUtil.update(digest, bigIntegers);

        byte[] output = new byte[digest.getDigestSize()];
        digest.doFinal(output, 0);

        return new BigInteger(output);
    }

    private static void update(org.bouncycastle.crypto.Digest digest, byte[] buffer) {
        digest.update(buffer, 0, buffer.length);
    }
}
