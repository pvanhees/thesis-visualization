package model;

import java.util.ArrayList;

public class AA_Group{
	private String name;
	private ArrayList<String> aa;

	public AA_Group(String name){
		this.name = name;
		this.aa = new ArrayList<String>();
	}

	public ArrayList<String> getAa() {
		return aa;
	}
	
	public String getName() {
		return name;
	}
}