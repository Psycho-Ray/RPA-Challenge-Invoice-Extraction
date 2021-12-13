package dev.botcity.modules.InputForms;

import dev.botcity.excel.FakeExcel;
import dev.botcity.excel.Form;
import dev.botcity.main.NotFoundException;
import dev.botcity.modules.BaseModule;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;

public class InputFormsFaster extends BaseModule {
	@Override
	protected void preInit() {
		this.maxDelay = 10 * 1000;
		this.extraPath = "Input Forms/";
		this.debugPath = "";
	}

	@Override
	public void paste(String text, int waitAfter) {
		try {
			StringSelection selection = new StringSelection(text);
			Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
			clip.setContents(selection, selection);
			wait(10);
			controlV();
			wait(waitAfter);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String[] getFieldsOrder() {
		//Grabs the entire page content
		controlA(10);
		controlC(30);
		
		String raw = getClipboard();
		wait(10);
		
		//Parse the page content
		raw = raw.substring(raw.lastIndexOf("!")+4);
		return raw.split("\n");
	}
	
	public void fillForm(Form form) {
		//Gets the order of the fields
		String[] fields = getFieldsOrder();
		
		//Resets to the first field
		clickAt(500, 200);
		
		//Tab - Paste
		for (String field : fields) {
			//Tab
			tab(10);

			//Paste
			paste(form.map.get(field), 20);
		}
		
		//Enter to submit
		enter(20);
	}
	
	public void action() throws NotFoundException, IOException {
		//Opens the browser
		browse("https://www.rpachallenge.com/");

		//Click Start
		quickFind("Start");
		click();

		//Fill each of the 10 forms
		for (Form form : FakeExcel.getForms()) fillForm(form);
	}
}
