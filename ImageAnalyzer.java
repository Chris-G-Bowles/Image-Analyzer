//Image Analyzer

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Scanner;
import javax.imageio.ImageIO;

public class ImageAnalyzer {
	private static int grayscaleAmount;
	private static int totalImagesChecked = 0;
	private static int totalValidImages = 0;
	
	public static void main(String[] args) {
		System.out.println("* Image Analyzer *");
		if (args.length != 0 && args.length != 2) {
			error("This program's usage is as follows:\n" +
					"java ImageAnalyzer\n" +
					"java ImageAnalyzer <directory location> <palette size option>");
		}
		Scanner input = new Scanner(System.in);
		String directoryLocation;
		if (args.length == 0) {
			System.out.print("Enter a directory location to analyze: ");
			directoryLocation = input.nextLine();
		} else {
			directoryLocation = args[0];
		}
		File directory = new File(directoryLocation);
		if (!directory.isDirectory()) {
			error(directoryLocation + " is not a valid directory.");
		}
		String paletteSizeString;
		if (args.length == 0) {
			System.out.println("Select the palette size to analyze by:");
			System.out.println("1) 4 colors");
			System.out.println("2) 6 colors");
			System.out.println("3) 8 colors");
			System.out.print("Palette size option: ");
			paletteSizeString = input.nextLine();
		} else {
			paletteSizeString = args[1];
		}
		Scanner lineInput = new Scanner(paletteSizeString);
		if (!lineInput.hasNextInt()) {
			error("Invalid palette size input.");
		}
		int paletteSizeOption = lineInput.nextInt();
		if (paletteSizeOption < 1 || paletteSizeOption > 3) {
			error("Invalid palette size option.");
		}
		lineInput.close();
		grayscaleAmount = 255 / ((paletteSizeOption * 2) + 1);
		input.close();
		System.out.println("(Please wait a few seconds for the images to load.)");
		analyzeImagesFromDirectory(directory);
		System.out.println("Total images checked: " + totalImagesChecked);
		System.out.println("Total valid images: " + totalValidImages);
	}
	
	private static void analyzeImagesFromDirectory(File directory) {
		for (File file : directory.listFiles()) {
			if (file.isFile()) {
				totalImagesChecked++;
				BufferedImage image;
				try {
					image = ImageIO.read(file);
				} catch (Exception e) {
					image = null;
				}
				if (image == null) {
					System.out.println(file.getPath() + " does not contain a readable image, and is being skipped.");
					continue;
				}
				if (image.getWidth() < 1 || image.getWidth() > 256 || image.getWidth() % 8 != 0 ||
						image.getHeight() < 1 || image.getHeight() > 256 || image.getHeight() % 8 != 0) {
					System.out.println(file.getPath() + " has an invalid resolution of " +
							image.getWidth() + "x" + image.getHeight() + " pixels, and is being skipped.");
					continue;
				}
				if (!isValidImage(image, file.getPath())) {
					continue;
				}
				totalValidImages++;
			} else if (file.isDirectory()) {
				analyzeImagesFromDirectory(file);
			}
		}
	}
	
	private static boolean isValidImage(BufferedImage image, String fileLocation) {
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				long argbValue = image.getRGB(x, y);
				if (argbValue < 0) {
					argbValue += 4294967296L;
				}
				long alphaValue = argbValue / 16777216;
				long redValue = (argbValue / 65536) % 256;
				long greenValue = (argbValue / 256) % 256;
				long blueValue = argbValue % 256;
				if (alphaValue != 255 || redValue != greenValue || redValue != blueValue ||
						redValue % grayscaleAmount != 0) {
					System.out.println(fileLocation + " has an invalid ARGB value at (" + x + ", " + y + "): [" +
							alphaValue + ", " + redValue + ", " + greenValue + ", " + blueValue +
							"], and is being skipped.");
					return false;
				}
			}
		}
		return true;
	}
	
	private static void error(String message) {
		System.out.println("Error: " + message);
		System.exit(1);
	}
}
