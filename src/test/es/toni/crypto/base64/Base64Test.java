package es.toni.crypto.base64;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import es.toni.crytpo.base64.Base64;

class Base64Test {

	@Test
	void cifrarBase64() {
		assertEquals("YXNkZjU2MTkxZmNzZTMyMg==", Base64.cifrarBase64("asdf56191fcse322"));
	}
	
	@Test
	void descifrarBase64() {
		assertEquals("asdf56191fcse322", Base64.descifrarBase64("YXNkZjU2MTkxZmNzZTMyMg=="));
	}

}
