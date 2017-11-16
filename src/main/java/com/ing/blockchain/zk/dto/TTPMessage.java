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

/**
 * The message from the Trusted 3rd Party (e.g. a government or employer) to the prover. This message contains:
 * <ol>
 *     <li>The Commitment: the public part that can be published. This should be signed by the Trusted 3rd Party.</li>
 *     <li>X, The secret number to hide in the proof (e.g. someones age)</li>
 *     <li>Y, The secret key</li>
 * </ol>
 */
public class TTPMessage implements Serializable {

    private final Commitment commitment;
    private final BigInteger y;
    private final BigInteger x;


    public TTPMessage(Commitment commitment, BigInteger x, BigInteger y) {
        this.commitment = commitment;
        this.y = y;
        this.x = x;
    }

    public Commitment getCommitment() {
        return commitment;
    }

    /**
     * Returns the number to hide (e.g. the age) called x in the whitepaper.
     * @return x, the number to hide.
     */
    public BigInteger getX() {
        return x;
    }

    /**
     * Returns the secret key called y in the whitepaper.
     * @return y, the secret key.
     */
    public BigInteger getY() {
        return y;
    }
}
