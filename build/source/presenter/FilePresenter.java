package presenter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import model.SequencingSample;
import processing.core.PApplet;
import view.FileLoaderViewListener;

public class FilePresenter implements IFilePresenter, FileLoaderViewListener {
	
	private PApplet pApplet;
	
	private String status_msg = ""; //loading messge
	//flags
	private boolean _IS_PROTEIN_SEQUENCE = false;
	private boolean _FILE_SELECTED = false;
	private boolean _FILE_LOADING = false;
	private boolean _FILE_LOADED = false;

	//setting from configuration/
	private char[] all_aa_default;// = {'A', 'I', 'L', 'V', 'F','W','Y','N', 'C', 'Q', 'M', 'S', 'T','D', 'E','R', 'H', 'K','G','P','.', 'X'};
	private char[][] aa_group_default; // = {{ 'A', 'G','I', 'L', 'V'}, {'C','M','S','T'},{'P'},{'F','W', 'Y'},{'H', 'K', 'R'},{'D','E','N','Q'},{'.'}, {'X'}};
	private String[] _AA_Group;// = {"aliphatic", "OH or Sulfur", "cyclic", "aromatic", "basic", "acidic", "blank", "unknown"};
	private String[] sample_names;
	private String[] file_urls;
	private long[] sample_colors;

	//File
	private File conf_file = null;
	private String conf_content = "";
	private String conf_error = "";
	private int conf_content_line_count = 0;
	private int conf_error_line_count = 0;
	private boolean isValidConf;

	private SequencingSample[] samples;
	private HashMap<String, Character> aminoAcidMap; //amino acid codes

	private int _total_length;

	private int _total_variation;
	
	public FilePresenter(PApplet pApplet) {
		this.pApplet = pApplet;
	}

	@Override
	public void configButtonClicked() {
//		conf_file = new File(pApplet.dataPath("conf.txt"));
//		isValidConf = check_conf_file(conf_file);
//		Runnable loadingFile  = new LoadingThread();
//		new Thread(loadingFile).start(); 
	}

	@Override
	public void startButtonClicked() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exampleButtonClicked() {
		// TODO Auto-generated method stub
		
	}
	
//	private void startLoadingData(){
//		//determine how much data need to be loaded or processed
//
//		//update status message
//		status_msg = "";
//		loadData();
//		//calculate frequency
//		preprocessData();
//		//layout
//		initLayout();
//		//set stage size
//		setStageSize();
//		//determine display position
//		updateLayout();
//		//determine edges
//		updateEdges();
//
//		_FILE_LOADING = false;
//		_FILE_LOADED = true;
//
//		status_msg = "Done!";
//	}
//	
	
//	private void loadData(){
//		samples = new Sample[sample_names.length];
//		// samples = new Sample[2];
//		//load data
//		for(int i = 0; i<samples.length; i++){
//			status_msg = "loading "+sample_names[i]+" aligned sequences";
//			samples[i] = new Sample(file_urls[i], sample_names[i]);
//		}
//		// samples[0] = new Sample("panel-a.txt", "all");
//		// samples[0] = new Sample("panel-b.txt", "gram_negative");
//		// samples[1] = new Sample("panel-c.txt", "gram_positive");
//
//		_total_length = samples[0].getSeq_length();
//		_total_variation = all_aa_default.length;
//
//
//		System.out.println("debug: total_length = "+_total_length +" total_variation = "+ _total_variation);
//		// println(dataPath("panel-b.txt"));
//	}
//	
	public void preprocessData(){
		aminoAcidMap = setupAminoAcidMap();

		for(SequencingSample s: samples){
			// println("\tpreprcessData():"+s.name);
			status_msg = "preprocessing: calculating frequency for "+s.getName();
			s.calculateFrequency();


			//flags
			s.setPositionSelected(new boolean[_total_length]);
			Arrays.fill(s.getPositionSelected(), Boolean.FALSE);
		}
		// println("debug: end of preprocessData()");
	}

	public HashMap<String, Character> setupAminoAcidMap(){
		HashMap<String, Character> map = new HashMap<String, Character>();
		map.put("GLY", new Character('G'));
		map.put("ALA", new Character('A'));
		map.put("VAL", new Character('V'));
		map.put("LEU", new Character('L'));
		map.put("ILE", new Character('I'));
		map.put("PHE", new Character('F'));
		map.put("TYR", new Character('Y'));
		map.put("TRP", new Character('W'));
		map.put("PRO", new Character('P'));
		map.put("HIS", new Character('H'));
		map.put("LYS", new Character('K'));
		map.put("ARG", new Character('R'));
		map.put("SER", new Character('S'));
		map.put("THR", new Character('T'));
		map.put("GLU", new Character('E'));
		map.put("GLN", new Character('Q'));
		map.put("ASP", new Character('D'));
		map.put("ASN", new Character('N'));
		map.put("CYS", new Character('C'));
		map.put("MET", new Character('M'));
		map.put("MSE", new Character('M'));
		map.put("CSE", new Character('U'));
		map.put("SEC", new Character('U'));
		map.put("PYH", new Character('O'));
		map.put("PYL", new Character('O'));
		return map;
	}
	
	public void configurationFileSelection(File selection){
		if(selection == null){
			conf_file = null;
			isValidConf = false;
			conf_content = "";
			conf_error = "";
		}else{
			conf_file = selection;
			isValidConf = check_conf_file(conf_file);
		}
	}
	
	public boolean check_conf_file(File f){
		boolean result = true;
		//reset
		conf_content = "";
		conf_error = "";

		int num_category = 0;
		int num_category_meta = 0;

		//parse the selected file
		//changed from loadStrings(url) to buffered reader
		//				String[] lines = loadStrings(url);
		BufferedReader reader = null;
		List<String> conf = new ArrayList<>();
		try{
			reader = new BufferedReader(new FileReader(f));
			String line = null;
			while((line = reader.readLine()) != null){
				conf.add(line);
			}
		} catch(IOException e){
			System.out.println("IO exception when trying to load the file");
		} finally{
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}


		for(String line : conf){
			// println("debug: configuration:"+line);
			String[] s =  line.trim().split("\t");
			if(s[0].trim().equals("category")){
				all_aa_default = new char[s.length-1];
				for(int i = 1; i< s.length; i++){
					all_aa_default[i-1] = s[i].charAt(0);
					num_category++;
				}
			}else if(s[0].trim().equals("meta-category")){
				int numOfCategory = s.length-1;
				aa_group_default = new char[numOfCategory][];
				for(int i  = 1; i< s.length; i++){
					String[] items =s[i].trim().split( ",");
					aa_group_default[i-1] = new char[items.length];
					for(int j = 0; j < items.length; j++){
						aa_group_default[i-1][j] = items[j].charAt(0);
						num_category_meta++;
					}
				}
			}else if(s[0].trim().equals("meta-category-name")){
				_AA_Group = new String[s.length-1];
				for(int i = 1; i < s.length; i++){
					_AA_Group[i-1] = s[i].trim();
				}            
			}else if(s[0].trim().equals("sample-name")){
				sample_names = new String[s.length-1];
				for(int i = 1; i< s.length; i++){
					sample_names[i-1] = s[i].trim();
				}
			}else if(s[0].trim().equals("sample-file")){
				file_urls = new String[s.length-1];
				for(int i = 1; i< s.length; i++){
					//check in the data folder
					String url = s[i].trim();
					//TODO find better alternative and check  if this is right/works
					if(new File(pApplet.dataPath(url)).exists()){
						file_urls[i-1] = pApplet.dataPath(url);
					}else{
						file_urls[i-1] = url;
					}
				}
			}else if(s[0].trim().equals("sample-color")){
				sample_colors = new long[s.length-1];
				for(int i = 1; i< s.length; i++){
					sample_colors[i-1] = Long.decode("#" + s[i].trim());
				}
			}else if(s[0].trim().equals("isProtein")){
				if(s[1].trim().equals("true")){
					_IS_PROTEIN_SEQUENCE = true;
				}
			}else if(s[0].trim().startsWith("#")){
				//extra information
			}else {
				System.out.println("Error: unknown conf information:"+line);
			}
		}
		//generate conf content
		conf_content_line_count = 0;
		conf_error_line_count = 0;

		conf_content += "CONTENT:\n";
		conf_content_line_count++;
		conf_content += "category:"+Arrays.toString(all_aa_default)+"\n";
		conf_content_line_count++;
		conf_content += "metacategory:"+Arrays.deepToString(aa_group_default) +"\n";
		conf_content_line_count++;
		conf_content += "metacategory:"+Arrays.toString(_AA_Group) +"\n";
		conf_content_line_count++;
		conf_content += "sample:"+Arrays.toString(sample_names)+"\n";
		conf_content_line_count++;
		if(_IS_PROTEIN_SEQUENCE){
			conf_content += "protein sequence: Yes";
			conf_content_line_count++;
		}
		//check validity
		//number of category
		if(num_category != num_category_meta){
			conf_error += "The numbers of category in category and meta-category do not match. \n";
			conf_error_line_count++;
			result = false;
		}
		//number of meta-category
		if(aa_group_default.length != _AA_Group.length){
			conf_error += "The numbers of meta-category do not match. \n";
			conf_error_line_count++;
			result = false;
		}
		//check if file exist
		for(String url : file_urls){
			if(!new File(url).exists()){
				//check inside the data folder
				if(!new File(pApplet.dataPath(url)).exists()){
					conf_error += "The file "+url+" does not exist\n";
					conf_error_line_count++;
					result = false;
				}
			}		
		}
		return result;
	}
//	private class LoadingThread implements Runnable{
//		public void run(){
//			_FILE_SELECTED = true;
//			_FILE_LOADING = true;
//
//			startLoadingData();
//		}
//	}
}
