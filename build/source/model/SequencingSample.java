package model;

import java.util.ArrayList;
import java.util.HashSet;

import processing.core.PApplet;

public class SequencingSample{
	private String name;
	private ArrayList<Sequence> sequences;
	private int seq_length;
	private int total_count;

	private HashSet<String> unique_sequence;

	//constructor and loading data
	public SequencingSample(String url, String name, PApplet pApplet){
		// println("Sample.contstructor: url ="+url+"  name="+name);
		this.name = name;
		sequences = new ArrayList<Sequence>();
		unique_sequence = new HashSet<String>();
		int non_unique_counter = 0;
		//load data
		String[] lines = pApplet.loadStrings(url);
		seq_length = lines[0].length();

		//Sequence
		//no sequence id 
		for(int i = 0; i<lines.length; i++){
			String line = lines[i].trim();
			// println("\t"+line);
			unique_sequence.add(line);
			char[] array = line.toCharArray();
			Sequence s = new Sequence(""+i, array);
			sequences.add(s);
		}

		PApplet.println("---- "+name+ "-----");
		PApplet.println("number of non_unique sequence removed = "+non_unique_counter);
		PApplet.println("number of unique sequence = "+unique_sequence.size());
		PApplet.println("sequence length = "+seq_length);

		total_count = sequences.size();
		// println("debgu: "+name+" has "+sequences.size()+" sequences!");
		System.out.println("debug:"+name+"'s data is loaded ---- ");
	}

	public String getName() {
		return name;
	}

	public int getSeq_length() {
		return seq_length;
	}

	public int getTotal_count() {
		return total_count;
	}
	
	public ArrayList<Sequence> getSequences() {
		return sequences;
	}
}