package org.dita4publishers.epub.fontobfuscator;

import static org.junit.Assert.*;

import org.junit.Test;

public class FontObfuscatorTest {

	@Test
	public void testMakeObfuscationKey() {
		String uid1 = "Nospaces";
		String uid2 = "Spaces Between Each Word";
		String uid3 = "HasATab\tAfterTheTab";
		String uid4 = "HasANewLine\nAfterTheNewline";
		String uid5 = "HasALineFeed\rAfterTheLineFeed";
		
		String result = FontObfuscator.makeObfuscationKey(uid1);
		assertEquals("Input should match result", uid1,result);
		
		result = FontObfuscator.makeObfuscationKey(uid2);
		assertEquals("Spaces not removed", "SpacesBetweenEachWord", result);

		result = FontObfuscator.makeObfuscationKey(uid3);
		assertEquals("Tab not removed", "HasATabAfterTheTab", result);

		result = FontObfuscator.makeObfuscationKey(uid4);
		assertEquals("Newline not removed", "HasANewLineAfterTheNewline", result);

		result = FontObfuscator.makeObfuscationKey(uid5);
		assertEquals("Linefeed not removed", "HasALineFeedAfterTheLineFeed", result);

		result = FontObfuscator.makeObfuscationKey(uid1, uid5);
		assertEquals("Multiples not concatenated correctly", "Nospaces HasALineFeedAfterTheLineFeed", result);

		result = FontObfuscator.makeObfuscationKey(uid1, uid2, uid5);
		assertEquals("Multiples not concatenated correctly", "Nospaces SpacesBetweenEachWord HasALineFeedAfterTheLineFeed", result);
	}

}
