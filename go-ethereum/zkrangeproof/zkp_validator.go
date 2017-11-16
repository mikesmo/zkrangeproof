// Copyright 2017 ING Bank N.V.
// This file is part of the go-ethereum library.
//
// The go-ethereum library is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// The go-ethereum library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with the go-ethereum library. If not, see <http://www.gnu.org/licenses/>.

package zkrangeproof

import (
  "math/big"
  "fmt"
  "crypto/sha256"
  "github.com/ethereum/go-ethereum/byteconversion"
)

var k1 = new(big.Int).SetBit(big.NewInt(0), 160, 1) // 2^160, security parameter that should match prover

func ValidateRangeProof(lowerLimit *big.Int,
						upperLimit *big.Int,
						commitment[] *big.Int,
						proof [] *big.Int) bool {

	if len(commitment) < 4 || len(proof) < 22 {
		return false
	}

	c := commitment[0] // firstC
	N := commitment[1] // modulo
	g := commitment[2] // generator
	h := commitment[3] // cyclic group generator

	if (N.Cmp(big.NewInt(0)) <= 0) {
		fmt.Println("Invalid group")
		return false
	}

	equalityProof2 := proof[14:18]
	squareProof3 := proof[4:9]
	squareProof4 := proof[9:14]

	cPrime := proof[0]
	cPrime1 := proof[1]
	cPrime2 := proof[2]
	cPrime3 := proof[3]

	u := proof[18]
	v := proof[19]
	x := proof[20]
	y := proof[21]

	// Derived information
	tmp := ModPow(g, Sub(lowerLimit, big.NewInt(1)), N)
	c1 := Mod(Multiply(ModInverse(tmp, N), c), N) 
	c2 := Mod(Multiply(ModPow(g, Add(upperLimit, big.NewInt(1)), N), ModInverse(c, N)), N)

	cPrimePrime := Mod(Multiply(Multiply(cPrime1, cPrime2), cPrime3), N)

 	if (!HPAKEEqualityConstraintValidateZeroKnowledgeProof (N, g, c1,
             												h, h, c2, cPrime, equalityProof2)) {
 		fmt.Println("HPAKEEqualityConstraint failure")
 		return false;
 	}

	s := Add(Mod(CalculateHash(c1, nil), k1), big.NewInt(1))
	t := Add(Mod(CalculateHash(c2, nil), k1), big.NewInt(1))

	if (!HPAKESquareValidateZeroKnowledgeProof (N, cPrime, h, cPrimePrime, squareProof3)) {
		fmt.Println("HPAKESquare failure at first square")
		return false
	}

	if (!HPAKESquareValidateZeroKnowledgeProof (N, g, h, cPrime3, squareProof4)) {
		fmt.Println("HPAKESquare failure at second square")
		return false
	}

 	nineLeft := Mod(Multiply(Multiply(ModPow(cPrime1, s, N), cPrime2), cPrime3), N)
 	nineRight := Mod(Multiply(ModPow(g, x, N), ModPow(h, u, N)), N)

    if nineLeft.Cmp(nineRight) != 0 {
    	fmt.Println("Failure: nineLeft != nineRight")
    	return false
    }

    tenLeft := Mod(Multiply(Multiply(cPrime1, ModPow(cPrime2, t, N)), cPrime3), N)
    tenRight := Mod(Multiply(ModPow(g, y, N), ModPow(h, v, N)), N)

    if tenLeft.Cmp(tenRight) != 0 {
    	fmt.Println("Failure: tenLeft != tenRight")
	   	return false
    }

	if x.Cmp(big.NewInt(0)) <=0 {
		fmt.Println("Failure: x <= 0")
		return false
	}

	if y.Cmp(big.NewInt(0)) <=0 {
		fmt.Println("Failure: y <= 0")
		return false
	}

	return true;
}


func HPAKESquareValidateZeroKnowledgeProof (
             N *big.Int,
             g *big.Int,
             h *big.Int,
             E *big.Int,
             sqProof[] *big.Int) bool {

	F := sqProof[0]
	ecProof := sqProof[1:]

	return HPAKEEqualityConstraintValidateZeroKnowledgeProof(N, g, F, h, h, F, E, ecProof)
}


func HPAKEEqualityConstraintValidateZeroKnowledgeProof (
             N *big.Int,
             g1 *big.Int,
             g2 *big.Int,
             h1 *big.Int,
             h2 *big.Int,
             E *big.Int,
             F *big.Int,
             ecProof[] *big.Int) bool {

	C := ecProof[0]
	D := ecProof[1]
	D1 := ecProof[2]
	D2 := ecProof[3]

	W1 := Mod(Multiply(Multiply(ModPow(g1, D, N), ModPow(h1, D1, N)), ModPow(E, Multiply(C, new(big.Int).SetInt64(-1)), N)), N)
	W2 := Mod(Multiply(Multiply(ModPow(g2, D, N), ModPow(h2, D2, N)), ModPow(F, Multiply(C, new(big.Int).SetInt64(-1)), N)), N)

	return C.Cmp(CalculateHash(W1, W2)) == 0
}


func CalculateHash(b1 *big.Int, b2 *big.Int) *big.Int {

	digest := sha256.New()
	digest.Write(byteconversion.ToByteArray(b1))
	if (b2 != nil) {
		digest.Write(byteconversion.ToByteArray(b2))
	}
	output := digest.Sum(nil)
	tmp := output[0: len(output)]
	return byteconversion.FromByteArray(tmp)
}

/**
 * Returns base**exponent mod |modulo| also works for negative exponent (contrary to big.Int.Exp)
 */						
func ModPow(base *big.Int, exponent *big.Int, modulo *big.Int) *big.Int {

	var returnValue *big.Int

	if exponent.Cmp(big.NewInt(0)) >=0 {
		returnValue = new(big.Int).Exp(base, exponent, modulo)
	} else {
		// Exp doesn't support negative exponent so instead:
		// use positive exponent than take inverse (modulo)..
		returnValue =  ModInverse(new(big.Int).Exp(base, new(big.Int).Abs(exponent), modulo), modulo)
	}
	return returnValue
}

func Add(x *big.Int, y *big.Int) * big.Int {
	return new(big.Int).Add(x, y)
}

func Sub(x *big.Int, y *big.Int) * big.Int {
	return new(big.Int).Sub(x, y)
}

func Mod(base *big.Int, modulo *big.Int) * big.Int {
	return new(big.Int).Mod(base, modulo)
}

func Multiply(factor1 *big.Int, factor2 *big.Int) *big.Int {
	return new(big.Int).Mul(factor1, factor2)
}

func ModInverse(base *big.Int, modulo *big.Int) *big.Int {
	return new(big.Int).ModInverse(base, modulo)
}