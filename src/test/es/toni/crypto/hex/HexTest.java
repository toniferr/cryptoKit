package es.toni.crypto.hex;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import es.toni.crytpo.hex.HexCode;

class HexTest {

	@Test
	void cifrarHex() {
		assertEquals("6164663536313931666365333232", HexCode.cifrarStringToHex("adf56191fce322"));
	}
	
	@Test
	void descifrarHex() {
		assertEquals("adf56191fce322", HexCode.descifrarHexToString("6164663536313931666365333232"));
	}

}
