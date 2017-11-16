contract PrecompiledRangeProof {
	function validate(uint lower, uint upper, string commitment, string proof) constant returns (bool);
}
