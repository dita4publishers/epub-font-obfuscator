/**
 * Copyright (c) 2016 DITA for Publishers Project. dita4publishers.org
 * 
 */
package org.dita4publishers.epub.fontobfuscator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

/**
 * Application class for applying EPUB3 font obfuscation to font
 * files.
 * 
 * See http://www.idpf.org/epub/30/spec/epub30-ocf.html#fobfus-algorithm
 * 
 * 
 *
 */
public class FontObfuscator {

	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.println("\nUsage: \n");
			System.err.println("FontObfuscator \"{EPUB Unique ID}\" \"{font file}\" [{destination file}]" );
			System.err.println("\nThe EPUB Unique ID must be the value of the dc:identifier element\n"
					+ "from the EPUB's OPF file.");
			System.err.println("\nIf destination file is not specified, the obfuscated font will be in a \n"
					+ "directory named \"obfuscated\" under the input font's directory.\n");
			System.exit(1);
		}
		String opfUID = args[0];
		String fontFilePath = args[1];
		
		Path pwd = Paths.get("");
		System.out.println("pwd=\"" + pwd.toAbsolutePath() + "\"");
		File pwdDir = pwd.toAbsolutePath().toFile();
		File fontFile = new File(pwdDir,fontFilePath);
		if (!fontFile.exists()) {
			System.err.println("Failed to find font file \"" + fontFile.getAbsolutePath() + "\"");
			System.exit(1);
		}

		File resultFontFile = null;

		if (args.length > 2) {
			String destinationFilePath = args[2];
			resultFontFile = new File(destinationFilePath);
			if (!resultFontFile.isAbsolute()) {
				resultFontFile = new File(pwdDir, destinationFilePath);
			}
		} else {
			File obfuscatedDir = new File(fontFile.getParentFile(), "obfuscated");
			resultFontFile = new File(obfuscatedDir, fontFile.getName());
		}

		System.out.println("Obfuscating font " + fontFilePath + " ...");
		
		try {
			resultFontFile.getParentFile().mkdirs();
			FileInputStream inStream = new FileInputStream(fontFile);
			FileOutputStream outStream = new FileOutputStream(resultFontFile);
			String obfuscationKey = makeObfuscationKey(opfUID);
			System.out.println("Using obfuscation key \"" + obfuscationKey + "\"");
			FontObfuscator.obfuscateFont(inStream, outStream, obfuscationKey);
			System.out.println("Font obfuscated to " + resultFontFile.getAbsolutePath());			
		} catch (Exception e) {
			System.err.println(e.getClass().getSimpleName() + ": " + e.getMessage());
			System.exit(1);
		}
		System.exit(0);
	}


	private static byte[] makeXORMask(String opfUID) {
		if(opfUID == null) return null;
		
		ByteArrayOutputStream mask = new ByteArrayOutputStream();

		/** 
		 * This starts with the "unique-identifier", strips the whitespace, and applies SHA1 hash 
		 * giving a 20 byte key that we can apply to the font file.
		 * 
		 * See: http://www.idpf.org/epub/30/spec/epub30-ocf.html#fobfus-keygen
		 **/
		try {
			Security.addProvider(
					new com.sun.crypto.provider.SunJCE());
			MessageDigest sha = MessageDigest.getInstance("SHA-1");
			String temp = opfUID.trim();
			sha.update(temp.getBytes("UTF-8"), 0, temp.length());mask.write(sha.digest());
		} catch (NoSuchAlgorithmException e) {
			System.err.println("No such Algorithm (really, did I misspell SHA-1?");
			System.err.println(e.toString());return null;
		} catch (IOException e) {
			System.err.println("IO Exception. check out mask.write...");
			System.err.println(e.toString());return null;
		}
		if (mask.size() != 20) {
			System.err.println("makeXORMask should give 20 byte mask, but isn't");
			return null;
		}
		return mask.toByteArray();
	}
	

	/** Implements the Obfuscation Algorithm from
	 * http://www.openebook.org/doc_library/informationaldocs/FontManglingSpec.html
	 **/

	public static void obfuscateFont(InputStream in, OutputStream out, String obfuscationKey) throws IOException 
	{
		byte[] mask = makeXORMask(obfuscationKey);
		try {
			byte[] buffer = new byte[4096];
			int len;
			boolean first = true;
			while ((len = in.read(buffer)) > 0) {
				if( first && mask != null ) {
					first = false;
					for( int i = 0 ; i < 1040 ; i++ ) {
						buffer[i] = (byte)(buffer[i] ^ mask[i % mask.length]);
					}
				}
				out.write(buffer, 0, len);
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		out.close();
	}


	/**
	 * Create an EPUB font obfuscation key from one or more strings according to the rules
	 * defined in the EPUB 3 spec, 4.3 Generating the Obfuscation Key 
	 * (http://www.idpf.org/epub/30/spec/epub30-ocf.html#fobfus-keygen)
	 * 
	 * Squeezes out any whitespace in each UID and then concatenates the result
	 * using single space characters as the separator.
	 * 
	 * @param baseString The string to convert into a key.
	 * @return obfuscation key string
	 */
	public static String makeObfuscationKey(String... UIDs) {
		StringBuilder buf = new StringBuilder();
		String sep = "";
		for (String uid : UIDs) {
			String keyPart = uid.replaceAll("[\\s\\t\\n\\r]", "");
			buf.append(sep).append(keyPart);
			sep = " ";
		}
		
		return buf.toString();		
	}
}
