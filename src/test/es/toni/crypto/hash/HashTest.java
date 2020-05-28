package es.toni.crypto.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import es.toni.crytpo.hash.Hash;
import es.toni.crytpo.utils.Constantes;

class HashTest {

	@Test
	void cifrarHashErrorTextoNulo() {
		assertEquals(Constantes.DATOS_INCORRECTOS, Hash.cifrarHash(0,""));
	}
	
	@Test
	void cifrarHashErrorAlgoritmoIncorrecto() {
		assertEquals("Datos vacíos o incorrectos", Hash.cifrarHash(46,""));
	}	
	
	@Test
	void cifrarHashMD5() {
		assertEquals("49d4b2b5834719b6c30a5d0a2dae0723", Hash.cifrarHash(0,"asdf56191fcse322"));
	}
	
	@Test
	void cifrarHashSHA1() {
		assertEquals("821d51c514b0bbe8ab5ef132c361988f65f7722c", Hash.cifrarHash(1,"asdf56191fcse322"));
	}
	
	@Test
	void cifrarHashSHA256() {
		assertEquals("dfa416684527a21a09a9d86d63758250ae2a2fa9027398ff6bb64230c54e2a94", Hash.cifrarHash(2,"asdf56191fcse322"));
	}
	
	@Test
	void cifrarHashSHA512() {
		assertEquals("4527b716871c88f33d4f5039b35efb74f8748b5c76a6028eec4b9054990813acc9edcffdf3b0a11b029b5144526044764d19f38da60152ad393ee3f6add32434", Hash.cifrarHash(3,"asdf56191fcse322"));
	}
	
	@Test
	void cifrarHashSHA3256() {
		assertEquals("7ccf16c805fb412a765c2d9a205701dc635f819c1302a9dd609a50daaaba2be5", Hash.cifrarHash(4,"asdf56191fcse322"));
	}
	
	@Test
	void cifrarHashSHA3512() {
		assertEquals("215f01c08ed706aa60262d54c55a7e2c474467d53ba81cc1770fe2a336050c19da3e57756232aadc6b0b43b1b93c12d9c65d39409898794e8174b6c1e4a6c98c", Hash.cifrarHash(5,"asdf56191fcse322"));
	}

}
