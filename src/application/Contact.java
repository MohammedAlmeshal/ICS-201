package application;

import java.io.Serializable;
import java.util.ArrayList;

public class Contact implements Serializable {

	
	private ArrayList<String> info;

	public ArrayList<String> getInfo() {
		return info;
	}

	public Contact(ArrayList<String> info) {
		this.info = info;
	}

}
