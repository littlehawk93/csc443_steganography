import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;

/**
 * This class extracts embedded text from a stegged image
 * when using a key
 */
public class StegExtractor {

	private static final int UNCONVENTIONAL_565 = -1;
	
	private final Random random;
	private final BufferedImage image;
	
	/**
	 * Constructor
	 * @param inFile The input file of the image to extract text from
	 * @param key The key used for embedding the text
	 */
	public StegExtractor(final File inFile, final long key) throws Exception {
		
		this.random = new Random(key);
		this.image = ImageIO.read(inFile);
	}
	
	/*
	 * Extracts the stegged text from the image
	 */
	public final void extractMessage() throws Exception {
		
		int pCount = 0;
		switch(image.getType()) {
		
		case BufferedImage.TYPE_3BYTE_BGR:
		case BufferedImage.TYPE_INT_RGB:
		case BufferedImage.TYPE_INT_BGR:
			pCount = 3;
			break;
			
		case BufferedImage.TYPE_4BYTE_ABGR:
		case BufferedImage.TYPE_4BYTE_ABGR_PRE:
		case BufferedImage.TYPE_INT_ARGB:
		case BufferedImage.TYPE_INT_ARGB_PRE:
			pCount = 4;
			break;
			
		case BufferedImage.TYPE_USHORT_565_RGB:
			pCount = UNCONVENTIONAL_565;
			break;
			
		default:
			throw new IllegalStateException();
		}
		if(pCount > 0) {
			extractConventional(pCount);
		}else {
			extract2Byte565();
		}
	}
	
	/*
	 * Extracts text from a conventional 3 Byte RGB or 4 Byte ARGB image
	 */
	private final void extractConventional(final int pCount) throws Exception {
		
		final int size = image.getWidth() * image.getHeight();
		final ArrayList<Integer> indices = new ArrayList<Integer>(size);
		for(int i=0;i<size;i++) {
			indices.add(i);
		}
		char c = 0;
		int pIndex, rIndex, cCount = 0, pixel;
		rIndex = random.nextInt(indices.size());
		pIndex = indices.get(rIndex);
		indices.remove(rIndex);
		pixel = image.getRGB(pIndex % image.getWidth(), pIndex / image.getWidth());
		while(true) {
			for(int i=0;i<8;i++) {
				if(cCount >= pCount) {
					if(indices.size() < 1) {
						System.out.print(c);
						return;
					}
					rIndex = random.nextInt(indices.size());
					pIndex = indices.get(rIndex);
					indices.remove(rIndex);
					pixel = image.getRGB(pIndex % image.getWidth(), pIndex / image.getWidth());
					cCount = 0;
				}
				c <<= 1;
				c |= ((pixel & (1 << (8 * (pCount - cCount - 1)))) == 0) ? 0 : 1;
				cCount++;
			}
			if(c == 0) {
				return;
			}
			System.out.print(c);
			c = 0;
		}
	}
	
	/*
	 * Extracts text from a 2 Byte 565 RGB image
	 */
	private final void extract2Byte565() throws Exception {
		
		final int size = image.getWidth() * image.getHeight();
		final ArrayList<Integer> indices = new ArrayList<Integer>(size);
		for(int i=0;i<size;i++) {
			indices.add(i);
		}
		char c = 0;
		int pIndex, rIndex, cCount = 0, pixel, temp;
		rIndex = random.nextInt(indices.size());
		pIndex = indices.get(rIndex);
		indices.remove(rIndex);
		pixel = image.getRGB(pIndex % image.getWidth(), pIndex / image.getWidth());
		while(true) {
			for(int i=0;i<8;i++) {
				if(cCount >= 3) {
					if(indices.size() < 1) {
						System.out.print(c);
						return;
					}
					rIndex = random.nextInt(indices.size());
					pIndex = indices.get(rIndex);
					indices.remove(rIndex);
					pixel = image.getRGB(pIndex % image.getWidth(), pIndex / image.getWidth());
					cCount = 0;
				}
				c <<= 1;
				temp = 1;
				temp <<= (cCount == 0) ? 11 : (cCount == 1) ? 5 : 0;
				c |= ((pixel & temp) == 0) ? 0 : 1;
				cCount++;
			}
			if(c == 0) {
				return;
			}
			System.out.print(c);
			c = 0;
		}
	}
}
