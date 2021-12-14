package dev.botcity.ocr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import dev.botcity.maestro_sdk.exception.BotMaestroException;

public class DadosInvoice extends BaseOCR {
	public String raw, number, date, companyName, totalDue;
	
	public DadosInvoice(String path, String fileName) throws IOException, BotMaestroException {
		this(FileUtils.readFileToByteArray(new File(path + fileName)));
	}
	
	public DadosInvoice(byte[] invoice) throws BotMaestroException {
		this(getBufferedImage(invoice));
	}
	
	public DadosInvoice(File invoice) throws BotMaestroException {
		this(getBufferedImage(invoice));
	}
	
	public DadosInvoice(BufferedImage invoice) throws BotMaestroException {
		//clearImage = preProcess(invoice);
		raw = process(invoice);
		parseInvoice();
	}
	
	public static Callable<DadosInvoice> caller(final String path, final String fileName, final int row) {
		class OCR_caller implements Callable<DadosInvoice> {			
			@Override
			public DadosInvoice call() throws IOException, BotMaestroException {
				System.out.println("Processing Invoice for row " + row);
				DadosInvoice dados = new DadosInvoice(path, fileName);
				System.out.println("The Invoice for row " + row + " has been processed succesfully");
				return dados;
			}
		}
		return new OCR_caller();
	}
	
	public static DadosInvoice getFromFuture(Future<DadosInvoice> future) throws BotMaestroException, IOException {
		try { return future.get(); }
		catch (InterruptedException e) {
			throw new BotMaestroException("OCR Thread interrupted");
		} catch (ExecutionException e) {
			//Expected Exceptions
			if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
			if (e.getCause() instanceof BotMaestroException) throw (BotMaestroException) e.getCause();
			
			//Unexpected Exceptions
			BotMaestroException wrapper = new BotMaestroException(e.getMessage());
			wrapper.setStackTrace(e.getStackTrace());
			throw wrapper;
		}
	}
	
	private void parseInvoice() throws BotMaestroException {
		//Check for OCR failure
		if (raw == null) throw new BotMaestroException("OCR Failure");
		
		//Regex
		if (raw.contains("Powered by")) {
			//Invoice Number
			Matcher matcher_number = Pattern.compile("Invoice\\s+[t#](\\d+)").matcher(raw);
			if (matcher_number.find()) number = matcher_number.group(1);
			
			//Invoice Data
			Matcher matcher_date = Pattern.compile("(\\d{4})(-\\d{2}-)(\\d{2})").matcher(raw);
			if (matcher_date.find()) {
				//From 2021-01-30 to 30-01-2021
				date = matcher_date.group(3) + matcher_date.group(2) + matcher_date.group(1);
			}
			
			//Company Name
			companyName = raw.substring(0, Math.min(raw.indexOf("INVOICE"), raw.indexOf("\n"))).trim();
			
			//Total Due
			Matcher matcher_totalDue = Pattern.compile("Total\\s*(\\d+)\\.?(\\d{2})").matcher(raw);
			if (matcher_totalDue.find()) totalDue = matcher_totalDue.group(1) + "." + matcher_totalDue.group(2);
		}
		else {
			//Invoice Number
			Matcher matcher_number = Pattern.compile("#\\s*.?\\s+(\\d+)\\s*").matcher(raw);
			if (matcher_number.find()) number = matcher_number.group(1);
			
			//Invoice Data
			Matcher matcher_date = Pattern.compile("Date:\\s*(\\w{3})\\s*(\\d{1,2},)\\s*(\\d{4})").matcher(raw);
			if (matcher_date.find()) {
				//From Jan 30, 2021 to 30-01-2021 
				date = matcher_date.group(1) + " " + matcher_date.group(2) + " " + matcher_date.group(3);
				date = LocalDate.parse(date, DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
			}
			
			//Company Name
			int pos = matcher_number.end();
			companyName = raw.substring(pos, raw.indexOf("\n", pos)).trim();
			
			//Total Due
			Matcher matcher_totalDue = Pattern.compile("Total:\\s*.?(\\d*),?(\\d{1,3}\\.\\d{2})").matcher(raw);
			if (matcher_totalDue.find()) totalDue = matcher_totalDue.group(1) + matcher_totalDue.group(2);
		}
		
		//Check for Regex Failure
		if (number == null || date == null || totalDue == null) throw new BotMaestroException("OCR Failure");
	}
}
