# epub-font-obfuscator
Java utility to obfuscate fonts according to the EPUB 3 specification.

Implements the algorithm defined in the EPUB 3 specification: http://www.idpf.org/epub/30/spec/epub30-ocf.html#fobfus-algorithm

Uses code cribbed from this article: http://blogs.adobe.com/digitaleditions/2009/05/font_mangling_code_available_f.html

## Usage

From the command line do:

~~~~
java -jar epub-font-obfuscator.jar "some-epub-id" "fonts/myfont.ttf"
~~~~

or 

~~~~
java -jar epub-font-obfuscator.jar "some-epub-id" "fonts/myfont.ttf" "fonts/obfuscated"
~~~~

or

~~~~
java -jar epub-font-obfuscator.jar "some-epub-id" "fonts/myfont.ttf" "fonts/myfont-obfuscated.ttf"
~~~~

The arguments are:

* EPUB identifier (obfuscation key): This must be the value of the dc:identifier element that is referenced by the @unique-identifier attribute on the package element in the EPUB's OPF file, e.g. `<dc:identifier id="bookid">PXxxxxxxxx-XXX-X</dc:identifier>`
* Input font file: This is the font file to be obfuscated. All font types are supported (the obfuscation mechanism doesn't actually care what the font details are, it just modifies the first 1000 bytes).
* Result directory or file: The directory or file to write the obfuscated font to. If not specified, the obfuscated will be written to a directory named "obfuscated" under the directory containing the input font. Note that you cannot update the input font in place. If you specify a directory name the result has the same name as in the input font. If you specify a font name then that name is used for the result font.

Note that the obfuscation key just match what is in the EPUB that will contain the obfuscated font as EPUB readers use the EPUB identifier in order to unobfuscate any obfuscated fonts embedded in the EPUB. It also means that you must create new obfuscated versions of a font for each EPUB it is included in.

NOTE: You can unobfuscate a font by applying the obfuscator to it with the same key used to obfuscate it originally.
