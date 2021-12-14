package dev.botcity.modules.InvoiceExtraction;

import dev.botcity.framework.bot.UIElement;
import dev.botcity.maestro_sdk.exception.BotMaestroException;
import dev.botcity.main.NotFoundException;
import dev.botcity.modules.BaseModule;
import dev.botcity.ocr.DadosInvoice;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TableRow {
	public String ID;
	public String invoice;
	public LocalDate dueDate;
	public Future<DadosInvoice> dadosInvoice;

	public TableRow(String ID, LocalDate dueDate) {
		this.ID = ID;
		this.dueDate = dueDate;
	}

	public TableRow(String ID, LocalDate dueDate, String invoice) {
		this.ID = ID;
		this.dueDate = dueDate;
		this.invoice = invoice;
	}
}

public class InvoiceExtraction extends BaseModule {
	//Init
	protected String downloadFolder = "Invoices/";
	private final int pages = 3, rowsPerPage = 4, totalRows = pages * rowsPerPage;
	public TableRow[] rows;

	@Override
	//To be executed before Init
	protected void preInit() {
		this.maxDelay = 10 * 1000;
		this.extraPath = "Invoice Extraction/";
		this.debugPath = "";
	}

	public InvoiceExtraction() {
		super();
	}
	
	public void clearDownloadFolder() {
		//Lists all files on the download folder that match the bot's usage
		System.out.println("Deleting old files on the Bot's download folder... ");
		File folder = new File(downloadFolder);
		File[] files = folder.listFiles((dir, name) -> name.matches("\\d{1,2}" + "(?:\\s\\(\\d\\))?.jpg"));
		
		//Deletes them
		if (files != null) for (File file : files) file.delete();
		
		//Deletes Challenge Result.csv
		new File(downloadFolder + "Challenge Result.csv").delete();
	}

	public String downloadInvoice(UIElement download) throws BotMaestroException, IOException {
		//Init
		setCurrentElement(download);

		//Downloads the Invoice image from a certain row
		rightClick();
		wait(400);
		type("k", 1000);
		
		//Waits for the popup, then copies the file name
		controlC(10);
		String name = getClipboard();
		
		//Confirm
		keyEnter(500);
		keyEsc();
		
		return name;
	}
	
	public void readTable() throws BotMaestroException, IOException, NotFoundException {
		//Init
		rows = new TableRow[totalRows];
		ExecutorService executor = Executors.newCachedThreadPool();
		quickFind("NextPage", 0.70);
		UIElement nextPage = getLastElement();
		
		//For each page...
		for (int row, page = 0; page < pages; page++) {
			//Raw Content
			controlA(10);
			controlC(10);
			String raw = getClipboard();
			raw = raw.substring(raw.lastIndexOf("Invoice") + 8);
			Matcher matcher_row = Pattern.compile("\\s*(\\d{1,2})\\s+(\\w+)\\s+(\\d{2}-\\d{2}-\\d{4})").matcher(raw);
			List<UIElement> downloads = quickFindAll("Download", 0.75);

			//For each row...
			for (int i=0; i<rowsPerPage; i++) {
				//Parses the row content
				if (matcher_row.find()) {
					row = Integer.parseInt(matcher_row.group(1));
					String id = matcher_row.group(2);
					LocalDate due = LocalDate.parse(matcher_row.group(3), DateTimeFormatter.ofPattern("dd-MM-yyyy"));
					rows[row-1] = new TableRow(id, due);
				}
				else throw new BotMaestroException("Failed to read the a table row");
				
				//Skips this row if it's not due
				if (rows[row-1].dueDate.isAfter(LocalDate.now())) continue;
				
				//Downloads the Invoice
				System.out.println("Row " + row + " is due");
				rows[row-1].invoice = downloadInvoice(downloads.get(i));
				
				//Asynchronous OCR
				Callable<DadosInvoice> callable = DadosInvoice.caller(downloadFolder, rows[row-1].invoice, row);
				rows[row-1].dadosInvoice = executor.submit(callable);
			}
			
			//Next Page
			if (page < pages-1) {
				setCurrentElement(nextPage);
				click();
			}
		}
		
		executor.shutdown();
	}
	
	public String compoundCSV() throws IOException, BotMaestroException {
		//Init
		StringBuilder result = new StringBuilder("ID,DueDate,InvoiceNo,InvoiceDate,CompanyName,TotalDue\n");
		
		//For each row
		for (int i=0; i<12; i++) {
			//Only checks Invoices whose due date has expired or will expire today
			if (rows[i].dadosInvoice != null) {
				//Inserts data from the table
				result.append(rows[i].ID).append(",");
				result.append(rows[i].dueDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).append(",");
				
				//Appends data from the OCR asynchronous execution
				DadosInvoice ocr = DadosInvoice.getFromFuture(rows[i].dadosInvoice);
				result.append(ocr.number).append(",");
				result.append(ocr.date).append(",");
				result.append(ocr.companyName).append(",");
				result.append(ocr.totalDue).append("\n");
			}
		}
		
		System.out.println("\n" + result);
		return result.toString();
	}

	public void action() throws IOException, BotMaestroException, NotFoundException {
		//Init
		System.out.println("Starting the Invoice Extractor Challenge!");
		browse("https://rpachallengeocr.azurewebsites.net/");
		clearDownloadFolder();
		
		//Starts the InputForms
		quickFindText("Start", 180);
		click();
		
		//Reads the table content, which includes downloading the images
		readTable();
		
		//Prepares to click the Submit Button
		quickFindText("FasterSubmit");
		
		//Reads the .jpg files using OCR
		String result = compoundCSV();
		FileUtils.writeStringToFile(new File(downloadFolder + "Challenge Result.csv"), result, "UTF-8");
		
		//Clicks the Submit Button 
		click();
		
		//Submits the resulting file
		wait(400);
		paste("Challenge Result.csv");
		enter();
		
		System.out.println();
	}
}