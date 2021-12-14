package dev.botcity.ocr;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.TessAPI;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.marvinproject.framework.image.MarvinImage;
import org.marvinproject.framework.io.MarvinImageIO;
import org.marvinproject.plugins.collection.MarvinPluginCollection;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

public class BaseOCR {
	private static final String DATA_PATH = "./tessdata";
	private ITesseract tess;
	
	public BaseOCR() {
		//Change JNA encoding to UT8 (allows portuguese letters and symbols)
		System.setProperty("jna.encoding", "UTF8");
		
		//Set up the Tesseract for basic portuguese processing   
		tess = new Tesseract();
		tess.setDatapath(DATA_PATH);
		tess.setOcrEngineMode(TessAPI.TessOcrEngineMode.OEM_DEFAULT);
		tess.setLanguage("por");
	}
	
	protected static BufferedImage preProcess(BufferedImage rg) {
		//Init
		MarvinImage clearImage = new MarvinImage(rg);
		
		//Tenta melhorar a image
		MarvinPluginCollection.brightnessAndContrast(clearImage, -500, 0);
		MarvinPluginCollection.gaussianBlur(clearImage, clearImage, 2);
		MarvinPluginCollection.thresholding(clearImage, 140);
		
		//Salva a imagem em um arquivo para preview
		MarvinImageIO.saveImage(clearImage, "betterRG.png");
		
		return clearImage.getBufferedImage();
	}
	
	protected static BufferedImage getBufferedImage(byte[] image) {
		try { return ImageIO.read(new ByteArrayInputStream(image)); }
		catch (IOException i) { i.printStackTrace(); return null; }
	}
	
	protected static BufferedImage getBufferedImage(File image) {
		try { return ImageIO.read(image); }
		catch (IOException i) { i.printStackTrace(); return null; }
	}
	
	protected String process(BufferedImage image) {
		try { return tess.doOCR(image); }
		catch (TesseractException t) {
			t.printStackTrace();
			return null;
		}
	}
}
