package model;
import java.util.HashMap;

public class Chain{
		private HashMap<Integer, Residue>  residue_map;
		private char name;
		private String sequence = "";

		public Chain(char name){
			this.name = name;
			residue_map = new HashMap<Integer, Residue>();
		}
		
		public HashMap<Integer, Residue> getResidue_map() {
			return residue_map;
		}
		
		public String getSequence() {
			return sequence;
		}
		
		public void setSequence(String sequence) {
			this.sequence = sequence;
		}
	}