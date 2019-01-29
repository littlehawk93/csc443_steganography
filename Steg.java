import java.io.File;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

/**
 * Stegonagraphy Program
 * 
 * Embeds text in an image (BMP or PNG) using stegonagraphy
 * @version 1.0
 */
public class Steg {
	
	private static File inFile;
	private static File  outFile;
	
	/*
	 * Main method
	 * Collect parameters from the command line
	 * and initialize the image and key for embedding / extraction
	 */
	public static void main(final String[] args) {
		if(args.length < 1) {
			printUsage();
			System.exit(0);
		}
		inFile = null;
		boolean embed = true;
		String strkey = null;
		InputStream stdin = System.in;
		for(int i=0;i<args.length;i++) {
			if(args[i].equals("-k") && i + 1 < args.length) {
				strkey = args[i+1];
				i++;
			}else if(args[i].equals("-f") && i + 1 < args.length) {
				inFile = new File(args[i+1]);
				i++;
			}else if(args[i].equals("-d")) {
				embed = false;
			}else if(args[i].equals("-i") && i + 1 < args.length) {
				try {
					stdin = new BufferedInputStream(new FileInputStream(new File(args[i+1])));
				}catch(final Exception e) {
					stdin = System.in;
				}
			}
		}
		long key = 0;
		if(strkey != null) {
			key |= strkey.hashCode();
			key <<= 32;
			key |= new StringBuilder(strkey).reverse().toString().hashCode();
		}
		if(inFile == null) {
			System.err.println("No Input File provided");
			printUsage();
			System.exit(1);
		}else if(!inFile.exists()) {
			System.err.println("Input File does not exist");
			System.exit(2);
		}else if(!inFile.getName().substring(inFile.getName().lastIndexOf('.')).equalsIgnoreCase(".BMP") && !inFile.getName().substring(inFile.getName().lastIndexOf('.')).equalsIgnoreCase(".PNG")) {
			System.err.println("Wrong Image File Type Provided (Must be a PNG or BMP File");
			System.exit(3);
		}else {
			outFile = new File("_" + inFile.getName());
			int count = 2;
			while(outFile.exists()) {
				outFile = new File(String.format("%s_%s",count,inFile.getName()));
				count++;
			}
			if(embed) {
				try {
					final StegEmbedder embedder = new StegEmbedder(inFile, key, outFile, stdin);
					embedder.embedMessage();
				}catch(final Exception e) {
					System.err.println("An error occured during message embedding.");
					e.printStackTrace();
				}
			}else {
				try {
					final StegExtractor extractor = new StegExtractor(inFile, key);
					extractor.extractMessage();
				}catch(final Exception e) {
					System.err.println("An error occured during message extraction.");
				}
			}
		}
	}
	
	/*
	 * Print the usage of the program
	 */
	private static final void printUsage() {
		
		System.out.println("steg -d -f [file-path] -k [key] -i [file-path] -o [file-path]");
		System.out.println("\t -d : Run in decrypt / extraction mode");
		System.out.println("\t -f : The file path of the input image (PNG or BMP");
		System.out.println("\t -i : Input File to read for message (optional)");
		System.out.println("\t -k : The key used for storing the message");
		System.out.println("\t      If no key is provided, default key is used");
		System.out.println();
		System.out.println("Stegged file will be saved in the directory this executable is stored in with an underscore prepended to the file name");
	}
}