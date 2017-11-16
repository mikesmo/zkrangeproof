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

import java.util.function.Supplier;

/**
 * Execution time measurement utility class.
 */
public class TimerUtil {
    private TimerUtil() {
    }

    public static <T> T timeAndLog(final String phase, Supplier<T> supplier) {
        final long startTime = System.nanoTime();
        try {
            return supplier.get();
        } finally {
            final long endTime = System.nanoTime();
            final long millis = (endTime - startTime) / 1000000L;
            logTime(phase, millis);
        }
    }

    private static void logTime(String label, long millisElapsed) {
        System.out.println(label + " took " + millisElapsed + " milliseconds");
    }
}
