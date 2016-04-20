package org.dita4publishers.epub.fontobfuscator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
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
	
	@Test
	public void testObfuscateFont() throws Throwable {
		InputStream inputFont = this.getClass().getClassLoader().getResourceAsStream("resources/fonts/font-file-plain.ttf");
		assertNotNull("Failed to get input font resource", inputFont);
		InputStream exemplarObfuscatedFont = this.getClass().getClassLoader().getResourceAsStream("resources/fonts/font-file-obfuscated.ttf");
		assertNotNull("Failed to get obfuscated font resource", exemplarObfuscatedFont);
		
		byte[] exemplarBytes = IOUtils.toByteArray(exemplarObfuscatedFont);
		
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		// NOTE: The key is from a test EPUB and is what was used to make the exemplar obfuscated
		// font.
		FontObfuscator.obfuscateFont(inputFont, outStream, "PXxxxxxxxx-XXX-X");
		byte[] generatedBytes = outStream.toByteArray();
		for (int i = 0; i < generatedBytes.length; i++) {
			byte b = generatedBytes[i];
			byte e = exemplarBytes[i];
			assertEquals("Byte sequence different at byte " + i, b, e);			
		}
	}

}
