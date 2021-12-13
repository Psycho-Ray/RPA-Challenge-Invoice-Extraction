package dev.botcity.excel;

import java.util.HashMap;
import java.util.Map;

public class Form {
	public String name, lastName, company, role;
	public String address, email, phone;
	public Map<String, String> map = new HashMap<String, String>();
	
	public Form(String[] row) {
		//Regular access
		name = row[0];
		lastName = row[1];
		company = row[2];
		role = row[3];
		address = row[4];
		email = row[5];
		phone = row[6];
		
		//Map Access
		map.put("First Name", name);
		map.put("Last Name", lastName);
		map.put("Company Name", company);
		map.put("Role in Company", role);
		map.put("Address", address);
		map.put("Email", email);
		map.put("Phone Number", phone);
	}
}
