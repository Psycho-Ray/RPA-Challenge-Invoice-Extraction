package dev.botcity.modules.InputForms;

import dev.botcity.excel.FakeExcel;
import dev.botcity.excel.Form;
import dev.botcity.main.NotFoundException;
import dev.botcity.modules.BaseModule;

import java.io.IOException;
import java.util.Map;

public class InputForms extends BaseModule {
	@Override
	protected void preInit() {
		this.maxDelay = 10 * 1000;
		this.extraPath = "Input Forms/";
		this.debugPath = "";
	}
	
	public void fillForm(Form form) throws NotFoundException, IOException {
		//Finds and fills each field according to the Sheet
		for (Map.Entry<String, String> entry : form.map.entrySet()) {
			quickFindText(entry.getKey(), 0.85);
			clickRelative(20, 40, 10);
			type(entry.getValue(), 20);
		}
		
		//Click submit
		quickFind("Submit", 0.8);
		click(20);
	}
	
	public void action() throws NotFoundException, IOException {
		//Opens the browser
		browse("https://www.rpachallenge.com/");

		//Click Start
		quickFind("Start", 0.7);
		click();

		//Fill each of the 10 forms
		for (Form form : FakeExcel.getForms()) fillForm(form);
	}
}
