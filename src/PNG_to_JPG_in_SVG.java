/**
 * (C) 2017 NumberFour AG
 * Author: Jens von Pilgrim
 */

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.xml.bind.DatatypeConverter;

public class PNG_to_JPG_in_SVG {

	static String PNG_START = "png;base64,";
	static String JPG_START = "jpg;base64,";
	static JPEGImageWriteParam JPEG_PARAMS;
	static {
		JPEG_PARAMS = new JPEGImageWriteParam(null);
		JPEG_PARAMS.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		JPEG_PARAMS.setCompressionQuality(0.5f);
	}

	int sizeOld = 0;
	int sizeNew = 0;

	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.out.println("Run with specific file or directory to convert all SVG embedded PNGs to JPG");
			System.exit(1);
		}
		new PNG_to_JPG_in_SVG().run(args[0]);
	}

	void run(String rootFileName) {
		File f = new File(rootFileName);
		if (! f.exists()) {
			System.err.println("File " + f + " not found.");
			System.exit(3);
		}
		if (f.isFile()) {
			convert(Paths.get(f.toURI()));
			if (sizeOld == sizeNew) {
				System.out.println("File not changed.");
			}
		} else if (f.isDirectory()) {
			System.out.println("Convert all files in " + rootFileName);
			convertDir(f);
			int diff = sizeOld - sizeNew;
			int perc = ((sizeNew) * 100) / sizeOld;
			System.out.println("______________________________________________");
			System.out.println("Reduced total size from " + fsize(sizeOld) + " to " + fsize(sizeNew) + " by " + fsize(diff) + " -- " + perc + "%");
		}
	}

	public void convertDir(File dir) {
		for (File fi : dir.listFiles()) {
			if (fi.isFile() && fi.getName().endsWith(".svg") && !fi.isHidden()) {
				convert(Paths.get(fi.toURI()));
			} else if (fi.isDirectory()  && !fi.isHidden() ) {
				convertDir(fi);
			}
		}
	}

	public void convert(Path p) {
		try {
			String content = new String(Files.readAllBytes(p));

			int i = 0, offset = 0;
			StringBuffer strb = new StringBuffer();
			int crc = 0;
			while ((i = content.indexOf(PNG_START, offset)) >= 0) {
				strb.append(content.substring(offset, i));
				strb.append(JPG_START);
				offset = i + PNG_START.length();
				i = content.indexOf("\"", offset);
				if (i < 0) {
					throw new IllegalArgumentException("End of bitmap not found");
				}
				String pngBase64 = content.substring(offset, i);
				String jpgBase64 = convertPngToJpeg(pngBase64);
				strb.append(jpgBase64);
				crc += pngBase64.length() - jpgBase64.length();
				offset = i;
			}

			strb.append(content.substring(offset));
			String newContent = strb.toString();

			if (newContent.length() == 0) {
				System.out.println("No content in " + p);
			} else if (content.length() > newContent.length()) {
				sizeOld += content.length();
				sizeNew += newContent.length();
				int diff = content.length() - newContent.length();
				if (diff != crc) {
					System.err.println("Check failed: Raster diffs do not match file diff: " + crc + " != " + diff
							+ ", do not write " + p);
				} else {
					Files.write(p, newContent.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
					int perc = ((newContent.length()) * 100) / content.length();
					System.out.println(
							p + ": " + fsize(content.length()) + " -> " + fsize(newContent.length()) +
							" -- " + perc + "%");
				}
			}

		} catch (Exception e) {
			System.err.println("Error processing " + p + ": " + e.getMessage());
			e.printStackTrace();
			System.exit(2);
		}
	}

	private String convertPngToJpeg(String pngBase64) throws IOException {
		byte[] pngBinary = DatatypeConverter.parseBase64Binary(pngBase64);
		InputStream in = new ByteArrayInputStream(pngBinary);
		BufferedImage pngImage = ImageIO.read(in);

		int width = pngImage.getWidth(), height = pngImage.getHeight();
		BufferedImage jpgImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		Graphics2D g = jpgImage.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setBackground(Color.WHITE);
		g.clearRect(0, 0, width, height);
		g.drawImage(pngImage, 0, 0, width, height, null);
		g.dispose();

		final ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		writer.setOutput(ImageIO.createImageOutputStream(baos));
		writer.write(null, new IIOImage(jpgImage, null, null), JPEG_PARAMS);

		String jpgBase64 = DatatypeConverter.printBase64Binary(baos.toByteArray());
		return jpgBase64;
	}

	private static String fsize(int s) {
		if (s<1000*10) return s + " Bytes";
		s = (s+500)/1000;
		if (s<1000*10) return s + " KB";
		s = (s+500)/1000;
		return s+ " MB";
	}

}
