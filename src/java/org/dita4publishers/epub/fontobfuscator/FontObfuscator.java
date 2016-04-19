/**
 * Copyright (c) 2016 DITA for Publishers Project. dita4publishers.org
 * 
 */
package org.dita4publishers.epub.fontobfuscator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Enumeration;

import javax.xml.transform.Source;

import org.apache.commons.io.FilenameUtils;
import org.xml.sax.InputSource;

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
			System.err.println("Usage: ");
			System.err.println("");
			System.err.println("FontObfuscator \"{EPUB Unique ID}\" \"{font file}\"" );
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
		String baseName = FilenameUtils.getBaseName(fontFile.getName());
		String ext = FilenameUtils.getExtension(fontFile.getName());
		File resultFontFile = new File(fontFile.getParentFile(), baseName + "_obfuscated." +  ext);
		if (!resultFontFile.canWrite()) {
			System.err.println("Cannot write to result font file \"" + resultFontFile.getAbsolutePath() + "\"");
			System.exit(1);
		}
		System.out.println("Obfuscating font " + fontFilePath + "...");
		try {
			FileInputStream inStream = new FileInputStream(fontFile);
			FileOutputStream outStream = new FileOutputStream(resultFontFile);
			FontObfuscator.obfuscateFont(inStream, outStream, opfUID);
			System.out.println("Font obfuscated.");			
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
			sha.update(temp.getBytes(), 0, temp.length());mask.write(sha.digest());
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

	public static void obfuscateFont(InputStream in, OutputStream out, String uid) throws IOException 
	{
		byte[] mask = makeXORMask(uid);
		try {
			byte[] buffer = new byte[4096];
			int len;
			boolean first = true;
			while ((len = in.read(buffer)) > 0) {
				if( first && mask != null ) {
					first = false;
					for( int i = 0 ; i < 1040 ; i++ ) {
						buffer[i] = (byte)(buffer[i] ^ mask[i%mask.length]);
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
}
