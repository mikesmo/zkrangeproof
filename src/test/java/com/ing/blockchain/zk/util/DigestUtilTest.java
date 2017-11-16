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

import org.junit.Test;

import java.math.BigInteger;

import static junit.framework.TestCase.assertEquals;

public class DigestUtilTest {

    @Test
    public void testEmptyDigest() {
        BigInteger hash = DigestUtil.calculateHash();
        assertEquals(new BigInteger("-12804752987762098394035772686106585063470084017442529046078187006797464553387"), hash);
        assertEquals(253, hash.bitLength());
    }

    @Test
    public void testDigestOneInput() {
        BigInteger hash = DigestUtil.calculateHash(BigInteger.valueOf(123456789));
        BigInteger hash2 = DigestUtil.calculateHash(BigInteger.valueOf(123456789));
        assertEquals(hash, hash2);

        assertEquals(new BigInteger("-32566173019136770954889697245562197770837950954265880648197772104152813006879"), hash);
        assertEquals(255, hash.bitLength());
    }

    @Test
    public void testDigestTwoInputs() {
        BigInteger largeInteger = new BigInteger("20905485153255974750600830283139712767405035066172127447413526262122898097752829902691919420016794244099612526431387099905077116995490485444167190551980224865082320241670546533063409921082864323224863076823319894932240914571396941354556281385023649535909639921239646795929610627460276589386330363348840105387073757406261480377763345436612442076323102518362946991582624513737241437269968051355243751819094759669539075841991633425362795570590507959822047022497500292880734028347273355847985904992235033659931679254742902977502890883426551960403450937665750386501228142099266824028488862959626463948822181376617128628357");
        BigInteger largeInteger2 = new BigInteger("5711912074763938920844020768820827016918638588776093786691324830937965710562669998102969607754216881533101753509522661181935679768137553251696427895001308210043958162362474454915118307661021406997989560047755201343617470288619030784987198511772840498354380632474664457429003510207310347179884080000294301502325103527312780599913053243627156705417875172756769585807691558680079741149166677442267851492473670184071199725213912264373214980177804010561543807969309223405291240876888702197126709861726023144260487044339708816278182396486957437256069194438047922679665536060592545457448379589893428429445378466414731324407");
        BigInteger hash2 = DigestUtil.calculateHash(largeInteger, largeInteger2);
        assertEquals(new BigInteger("-19913561841364303941087968013056854925409568225408501509608065500928998362191"), hash2);
    }

    @Test
    public void testDigestInputZero() {
        BigInteger largeInteger = new BigInteger("-12804752987762098394035772686106585063470084017442529046078187006797464553387");
        BigInteger hash1 = DigestUtil.calculateHash(largeInteger);
        BigInteger hash2 = DigestUtil.calculateHash(largeInteger, BigInteger.ZERO);
        assertEquals(new BigInteger("42501291392743695431878740634531681500576291905345320922138468513085152597078"), hash1);
        assertEquals(new BigInteger("15883972681192226692985775791134838032083357469997719966667816890574670352896"), hash2);
    }
}
