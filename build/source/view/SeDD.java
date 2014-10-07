package view;
//import processing.pdf.*; 
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;

import model.AA_Group;
import model.Edge;
import model.Position;
import model.SequencingSample;
import model.Sequence;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PVector;

public class SeDD extends PApplet {

	//Sequence Diversity Diagram v1.0.0  copyright Ryo Sakai
	

	private static final long serialVersionUID = -956139002461462544L;
	private int _STAGE_WIDTH = 1000;
	private int _STAGE_HEIGHT = 500;
	private int _MARGIN = 10;

	private int _TOP_MARGIN = _MARGIN *5;
	private int _LEFT_MARGIN = _MARGIN*10;
	private int _RIGHT_MARGIN = _MARGIN*5;
//	private int _BOTTOM_MARGIN = _MARGIN*50;

	private int _SANKEY_SLIDER_HEIGHT = _MARGIN;
	private int _MATRIX_WIDTH;

	private float _TOP_RATIO = 0.45f;
	private float _BOTTOM_RATIO =0.5f;
	private float _SANKEY_RATIO = 0.90f;
	private float _LEGEND_RATIO = 0.10f;
	private float _VERTICAL_GAP_RATIO = 0.5f;//gap is half of node height
	private float _HORIZONTAL_GAP_RATIO = 2; //gap is *2 of node width

	private int _SANKEY_WIDTH, _SANKEY_HEIGHT; //PGraphic size //real length
	private int _SANKEY_POS = 0;

	private Rectangle _DISPLAY_RECT;
	private Rectangle _SANKEY_RECT;
	private Rectangle _SANKEY_SLIDER_RECT;
	private Rectangle _SANKEY_SLIDER_BAR;
	private Rectangle _LEGEND_RECT;
	private Rectangle _BTN_RECT;
//	private Rectangle _ALL_MATRIX_RECT;

	private float base = 2.0f;
	private boolean isExponentialScaling = false;
//	private int _legend_width = _MARGIN*15;


	private int _node_w = 10;
	private int _node_h = 10;
	private float _half_node_w = _node_w/2;
	private int _horizontal_gap = round( _MARGIN*2.5f);
//	private float _min_node_h = 0.5f;//0f;//0.5f; /////minimum node height
	private int _vertical_gap = _MARGIN/3;
	private int _text_margin = 2;


	//Slider
	private boolean showSlider = false;
	private boolean slider_drag = false;
	private float slider_onPress = 0;



	private PFont font;
//	private PFont small_font;

//	private boolean record = false;
//	private boolean loadPreprocessedMI = true;
	boolean showZscore = true;


//	private float zscore_min = -10;
//	private float zscore_max = 10;
//	private float zscore_increment = 0.1f;
//	private float mutual_z_min = Float.MAX_VALUE; //initial value
//	private float mutual_z_max = Float.MIN_VALUE;


	//log scaling
//	private float base = 2.0f;
//	private boolean isExponentialScaling = false;

	//optimize cpu
	private int draw_counter = 0;
	private int draw_max = 60; //2 seconds = draw 90 times before it stops

	//legend threshold
	private float _frequencyThreshold = 0; //global threshold for frequency

	//flags
	private boolean _IS_PROTEIN_SEQUENCE = false;
	private boolean _FILE_SELECTED = false;
	private boolean _FILE_LOADING = false;
	private boolean _FILE_LOADED = false;


	//6 group classification
//	private float areaToDiameter(float area){
//		float diameter = sqrt((area/PI))*2;
//		return diameter;
//	}

	public void setup(){
		font = createFont("AndaleMono", 10);
//		small_font = createFont("Supernatural1001", 10);
		textFont(font);
		smooth();
		frameRate(30);

		//get the display size
		_STAGE_WIDTH = displayWidth;
		_STAGE_HEIGHT = displayHeight;
		size(_STAGE_WIDTH, _STAGE_HEIGHT);
		//setup dataloading page
		setupDataLoadingPage();
	}

	private void startLoadingData(){
		//determine how much data need to be loaded or processed

		//update status message
		status_msg = "";
		loadData();
		//calculate frequency
		preprocessData();
		//layout
		initLayout();
		//set stage size
		setStageSize();
		//determine display position
		updateLayout();
		//determine edges
		updateEdges();

		_FILE_LOADING = false;
		_FILE_LOADED = true;

		status_msg = "Done!";
	}

	private String timestamp() {
		return String.format("%1$tm%1$td%1$tY_%1$tH%1$tM", Calendar.getInstance());
	}

//	private float min_frequency  = 0.01f; //1%
	private ArrayList<ArrayList<Sequence>> selected_sequences = new ArrayList<ArrayList<Sequence>>();

	private boolean sequences_selected = false;

	private boolean hovering_ind_btn = false;
	private boolean hovering_group_btn = false;
	private int hover_btn_index = -1;
	private boolean hovering_up_btn = false; //up or down
//	private boolean showing_heatmap = false;


	private int hover_pos_index = -1;
	private int hover_sample_index = -1;
//	private int pdb_sample_index = -1;

	public void mouseMoved(){
		redraw();
		if(!_FILE_SELECTED){
			cursor(ARROW);
			//file loading page
			if(conf_btn.contains(mouseX, mouseY)){
				cursor(HAND);
			}else if(start_btn.contains(mouseX, mouseY)){
				if(isValidConf){
					cursor(HAND);
				}
			}
		}else if(_FILE_LOADING){
			//loading file
		}else if(_FILE_LOADED){
			//file loaded
			sequences_selected = false;
			hovering_ind_btn = false;
			hovering_group_btn = false;
			hover_btn_index = -1;
			hovering_up_btn = false;
			hover_pos_index = -1;
			hover_sample_index = -1;
			cursor(ARROW);
			//selecting bundles
			if(_SANKEY_RECT.contains(mouseX, mouseY)){
				selected_sequences = new ArrayList<ArrayList<Sequence>>();
				for(int i = 0; i < samples.length; i++){
					selected_sequences.add(new ArrayList<Sequence>());
				}

				int index = getDimensionIndex(mouseX);
				if(index != -1){
					for(int i = 0; i < samples.length; i++){
						SeDDModel s = samples[i];
						if(s.isShowing()){
							Position d = s.getDimensions()[index]; 
							//search for the edge
							Edge selected_edge = null;
							outerloop:
								for(Edge e : d.getEdges()){
									if(e.getPath().contains(mouseX, mouseY) && e.getFrequency() >=_frequencyThreshold ){
										selected_edge = e;
										break outerloop;
									}
								}
							if(selected_edge != null){
								selected_sequences.get(i).addAll(selected_edge.getSequences());
								println("debug: "+s.getSequencingSample().getName()+":"+selected_edge.getSequences().size()+" sequences selected");						
								sequences_selected  = true;
							}
						}
					}

					if(sequences_selected){
						cursor(HAND);
					}
				}
				loop();
			}else if(_BTN_RECT.contains(mouseX,mouseY)){
				if(ind_btn_rect.contains(mouseX, mouseY)){
					// for(Rectangle rect : aa_up_btns){
					for(int i = 0; i < aa_up_btns.length; i++){
						Rectangle rect = aa_up_btns[i];
						if(rect.contains(mouseX, mouseY)){
							hovering_ind_btn = true;
							hover_btn_index = i;
							hovering_up_btn = true;
							cursor(HAND);
							loop();
							return;
						}
					}
					// for(Rectangle rect : aa_down_btns){
					for(int i = 0; i < aa_down_btns.length; i++){
						Rectangle rect = aa_down_btns[i];
						if(rect.contains(mouseX, mouseY)){
							hovering_ind_btn = true;
							hover_btn_index = i;
							cursor(HAND);
							loop();
							return;
						}
					}
					cursor(ARROW);
				}else if(group_btn_rect.contains(mouseX, mouseY)){
					// for(Rectangle rect : aa_group_up_btns){
					for(int i = 0; i < aa_group_up_btns.length; i++){
						Rectangle rect = aa_group_up_btns[i];
						if(rect.contains(mouseX, mouseY)){
							hovering_group_btn = true;
							hovering_up_btn = true;
							hover_btn_index = i;
							cursor(HAND);
							loop();
							return;
						}
					}
					// for(Rectangle rect : aa_group_down_btns){
					for(int i = 0; i < aa_group_down_btns.length; i++){
						Rectangle rect = aa_group_down_btns[i];
						if(rect.contains(mouseX, mouseY)){
							hovering_group_btn = true;
							hover_btn_index = i;
							cursor(HAND);
							loop();
							return;
						}
					}
				}
			}else if(_LEGEND_RECT.contains(mouseX, mouseY)){
				// println("legend_rect!!");
				//legend area
				for(SeDDModel s:samples){
					if(s.getToggle().contains(mouseX, mouseY)){
						cursor(HAND);
						draw_counter = 0;
						loop();
						return;
					}
				}
				//number box
				for(NumberBox nb: _legend_numberbox){
					if(nb.contains(mouseX, mouseY)){
						cursor(HAND);
						draw_counter = 0;
						loop();
						return;
					}
				}

				if(pdf_btn.contains(mouseX, mouseY)){
					cursor(HAND);
					draw_counter = 0;
					loop();
					return;
				}else if(png_btn.contains(mouseX, mouseY)){
					cursor(HAND);
					draw_counter = 0;
					loop();
					return;
				}
			}else if(_SANKEY_SLIDER_RECT.contains(mouseX, mouseY)){
				if(_SANKEY_SLIDER_BAR != null){
					cursor(HAND);
					draw_counter = 0;
					loop();
					return;
				}
			}
		}

	}
	public void mousePressed(){
		if(!_FILE_SELECTED){
			//file loading page
			if(conf_btn.contains(mouseX, mouseY)){
				//select configuration file
				selectInput("Select a configuration file:", "configurationFileSelection");
			}else if(start_btn.contains(mouseX, mouseY)){
				if(isValidConf){
					//need to run this on a thread
					Runnable loadingFile  = new LoadingThread();
					new Thread(loadingFile).start();   
					// redraw();
				}
			}else if(example_btn.contains(mouseX, mouseY)){
				conf_file = new File(dataPath("conf.txt"));
				isValidConf = check_conf_file(conf_file);
				Runnable loadingFile  = new LoadingThread();
				new Thread(loadingFile).start();   
			}
		}else if(_FILE_LOADING){
			//loading file
		}else if(_FILE_LOADED){
			//file loaded
//			pdb_sample_index = -1;
			slider_drag = false;
			if(hovering_ind_btn){
				String aa_to_move  = aa_order[hover_btn_index];
				if(hovering_up_btn){
					// println("move "+aa_to_move +" up!");
					AA_Group group = find_AA_Group(aa_to_move);
					if(group != null){
						int index = group.getAa().indexOf(aa_to_move);
						int new_index  = max(0, index -1);
						Collections.swap(group.getAa(), index, new_index);
					}

				}else{
					// println("move "+aa_to_move +" down!");
					AA_Group group = find_AA_Group(aa_to_move);
					if(group != null){
						int index = group.getAa().indexOf(aa_to_move);
						int new_index  = min(group.getAa().size()-1, index+1);
						// int new_index  = min(group.aa.size()-1, hover_btn_index +1);
						Collections.swap(group.getAa(), index, new_index);
					}
				}
				setup_aa_btns();
				//determine display position
				updateLayout();
				//determine edges
				updateEdges();
				loop();
			}else if(hovering_group_btn){
//				AA_Group aa = all_aa_group.get(hover_btn_index);

				if(hovering_up_btn){
					// println("move "+aa.name+ " up!");
					int new_index  = max(0, hover_btn_index -1);
					Collections.swap(all_aa_group, hover_btn_index, new_index);
				}else{
					// println("move "+aa.name+ " down!");
					int new_index  = min(all_aa_group.size()-1, hover_btn_index +1);
					Collections.swap(all_aa_group, hover_btn_index, new_index);
				}
				setup_aa_btns();
				//determine display position
				updateLayout();
				//determine edges
				updateEdges();
				loop();
			}else if(hover_sample_index != -1){
				//selecting sample and position
				SeDDModel s = samples[hover_sample_index];
				s.togglePositionSelected(hover_pos_index);
				loop();
				return;
			}else if(_LEGEND_RECT.contains(mouseX, mouseY)){
				//legend area
				//toggle button
				for(SeDDModel s:samples){
					if(s.getToggle().contains(mouseX, mouseY)){
						draw_counter = 0;
						loop();
						return;
					}
				}
				//numberbox
				for(int i = 0; i < _legend_numberbox.length; i++){
					NumberBox nb = _legend_numberbox[i];
					if(nb.getUp_rect().contains(mouseX, mouseY)){
						//up
						if(i == 0){
							//treshold
							nb.setV_current(min(nb.getV_current()+0.01f, nb.getV_max()));
							_frequencyThreshold = nb.getV_current();
							updateEdges();
							draw_counter = 0;
							loop();
							return;
						}else if(i == 1){
							//node width
							nb.setV_current(min(nb.getV_current()+1.0f, nb.getV_max()));
							_node_w = round(nb.getV_current());
							_half_node_w = _node_w/2;
							updateLayout();
							updateEdges();
							draw_counter = 0;
							loop();
							return;
						}else if(i == 2){
							//gap width
							nb.setV_current(min(nb.getV_current()+1.0f, nb.getV_max()));
							_horizontal_gap = round(nb.getV_current());
							updateLayout();
							updateEdges();
							draw_counter = 0;
							loop();
							return;
						}


						// draw_counter = 0;
						// loop();
						// return;
					}else if(nb.getDown_rect().contains(mouseX, mouseY)){
						//down
						if(i == 0){
							//treshold
							nb.setV_current(max(nb.getV_current()-0.01f, nb.getV_min()));
							_frequencyThreshold = nb.getV_current();
							updateEdges();
							draw_counter = 0;
							loop();
							return;
						}else if(i == 1){
							//node width
							nb.setV_current(max(nb.getV_current()-1.0f, nb.getV_min()));
							_node_w =round(nb.getV_current());
							_half_node_w = _node_w/2;
							updateLayout();
							updateEdges();
							draw_counter = 0;
							loop();
							return;
						}else if(i == 2){
							//gap width
							nb.setV_current(max(nb.getV_current()-1.0f, nb.getV_min()));
							_horizontal_gap =round(nb.getV_current());
							updateLayout();
							updateEdges();
							draw_counter = 0;
							loop();
							return;
						}
					}
				}

				if(pdf_btn.contains(mouseX, mouseY)){
					//export pdf
					selectFolder("Select a directory to save .pdf image", "pdfFolderSelection");
					return;
				}else if(png_btn.contains(mouseX, mouseY)){
					//export png
					selectFolder("Select a directory to save .png image", "pngFolderSelection");
					return;
				}
			}else if(_SANKEY_SLIDER_RECT.contains(mouseX, mouseY)){
				if(_SANKEY_SLIDER_BAR.contains(mouseX, mouseY)){
					slider_drag = true;
					slider_onPress = map(mouseX, _SANKEY_SLIDER_BAR.x, _SANKEY_SLIDER_BAR.x+_SANKEY_SLIDER_BAR.width, 0, 1);
					loop();
					return;
				}
			}else{
			}
		}

	}

	public void mouseDragged(){
		if(!_FILE_SELECTED){
			//file loading page
		}else if(_FILE_LOADING){
			//loading file
		}else if(_FILE_LOADED){
			//file loaded
			if(_SANKEY_SLIDER_BAR!= null && slider_drag){
				//update slider
				int tip_x = round(constrain(mouseX - slider_onPress*_SANKEY_SLIDER_BAR.width, _SANKEY_SLIDER_RECT.x, _SANKEY_SLIDER_RECT.x+_SANKEY_SLIDER_RECT.width - _SANKEY_SLIDER_BAR.width));
				_SANKEY_SLIDER_BAR.setLocation(tip_x, _SANKEY_SLIDER_RECT.y);
				//update position
				_SANKEY_POS = round(map(tip_x, _SANKEY_SLIDER_RECT.x, _SANKEY_SLIDER_RECT.x+_SANKEY_SLIDER_RECT.width- _SANKEY_SLIDER_BAR.width, 0, _SANKEY_WIDTH - _SANKEY_RECT.width));
				// println("dragging: "+_SANKEY_POS);
				for(SeDDModel s:samples){
					updatePGraphics(s);
				}
				draw_counter = 0;
				loop();
			}
		}
	}


	public void keyPressed(){
		if(!_FILE_SELECTED){
			//file loading page
		}else if(_FILE_LOADING){
			//loading file
		}else if(_FILE_LOADED){
			//file loaded
			if (key=='r' || key=='R'){ 
				// record = true;
				// redraw();
			}else if( key == 't' || key=='T'){
				//save tiff
				save(timestamp()+".tif");
			}else if( key == ' '){
				loop();
			}
		}
	}


private boolean check_conf_file(File f){
		boolean result = true;
		//reset
		conf_content = "";
		conf_error = "";

		int num_category = 0;
		int num_category_meta = 0;

		//parse the selected file
		//changed from loadStrings(url) to buffered reader
		String[] conf = loadStrings(f);


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
					if(new File(dataPath(url)).exists()){
						file_urls[i-1] = dataPath(url);
					}else{
						file_urls[i-1] = url;
					}
				}
			}else if(s[0].trim().equals("sample-color")){
				sample_colors = new int[s.length-1];
				for(int i = 1; i< s.length; i++){
					sample_colors[i-1] = unhex(s[i].trim());
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
				if(!new File(dataPath(url)).exists()){
					conf_error += "The file "+url+" does not exist\n";
					conf_error_line_count++;
					result = false;
				}
			}		
		}
		return result;
	}
	
	private int getDimensionIndex(int mx){
		// float width_range = _plot_width - 
		for(int i = 0; i < layout_x.length; i++){
			float start_x = layout_x[i] + _half_node_w;
			float end_x = start_x + _node_w + _horizontal_gap;
			if(start_x <= mx &&  mx < end_x){
				return i;
			}
		}

		return -1;
	}

	private AA_Group find_AA_Group(String aa){
		for(AA_Group group:all_aa_group){
			if(group.getAa().contains(aa)){
				return group;
			}
		}
		return null;
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

//	private void pngFolderSelection(File selection){
//		if(selection != null){
//			//save the image
//			// String path = selection.getAbsolutePath();
//			// path+= "/Sedd_"+ timestamp()+".png";
//			// saveFrame(path);
//			// println("saving png done");
//			String path = selection.getAbsolutePath();
//			path+= "/Sedd_"+ timestamp()+".png";
//
//			//determine export image size
//			int e_img_width = _LEFT_MARGIN + _RIGHT_MARGIN + (_node_w*(_total_length+2)) + (_horizontal_gap*(_total_length-1)) + _LEGEND_RECT.width;
//			int e_img_height = _SANKEY_HEIGHT + _TOP_MARGIN +_TOP_MARGIN;
//			println("export image dimensions = "+ e_img_width +"  "+e_img_height);
//			PGraphics pdf = createGraphics(e_img_width, e_img_height);
//			pdf.beginDraw();
//			//draw stuff
//			drawPDF(pdf, e_img_width, e_img_height);
//
//			pdf.dispose();
//			pdf.endDraw();
//			pdf.save(path);
//		}
//	}

//	private void pdfFolderSelection(File selection){
//		if(selection != null){
//			String path = selection.getAbsolutePath();
//			path+= "/Sedd_"+ timestamp()+".pdf";
//
//			//determine export image size
//			int e_img_width = _LEFT_MARGIN + _RIGHT_MARGIN + (_node_w*(_total_length+2)) + (_horizontal_gap*(_total_length-1)) + _LEGEND_RECT.width;
//			int e_img_height = _SANKEY_HEIGHT + _TOP_MARGIN +_TOP_MARGIN;
//			println("export image dimensions = "+ e_img_width +"  "+e_img_height);
//			PGraphics pdf = createGraphics(e_img_width, e_img_height, PDF, path);
//			pdf.beginDraw();
//			//draw stuff
//			drawPDF(pdf, e_img_width, e_img_height);
//
//			pdf.dispose();
//			pdf.endDraw();
//		}
//	}

	private int title_x, title_y;
	private PFont title_font;
	private PFont btn_font;

	private int _btn_width  = _MARGIN*10;
	private int _btn_height = _MARGIN*3;
	private int _file_name_width = _MARGIN *40;
	private int _content_x, _content_y;


	private Rectangle conf_file_rect;
	private Rectangle conf_btn, start_btn;
	private Rectangle example_btn;

	private boolean isValidConf = false;

	//File
	private File conf_file = null;
	private String conf_content = "";
	private String conf_error = "";
	private int conf_content_line_count = 0;
	private int conf_error_line_count = 0;

	private String status_msg = ""; //loading messge




	private void setupDataLoadingPage(){
		title_font = loadFont("OstrichSans-Black-48.vlw");
		btn_font = loadFont("LucidaSans-15.vlw");

		int runningX = _LEFT_MARGIN;
		int runningY = _TOP_MARGIN;

		//title
		title_x = runningX;
		title_y = runningY;
		runningY += _MARGIN*6;

		//configuration file
		runningY += _MARGIN*2;
		conf_file_rect = new Rectangle(runningX, runningY, _file_name_width, _btn_height);
		runningX += _file_name_width + _MARGIN;
		//conf btn
		conf_btn = new Rectangle(runningX, runningY, _btn_width, _btn_height);
		runningX += _btn_width + _MARGIN;
		//start btn
		start_btn = new Rectangle(runningX, runningY, _btn_width, _btn_height);
		runningX += _btn_width + _MARGIN*3;
		//example btn
		example_btn = new Rectangle(runningX, runningY, _btn_width, _btn_height);


		runningX = title_x;
		runningY += _btn_height + _MARGIN*5;
		//text information
		runningY += _MARGIN*2;
		_content_x = runningX;
		_content_y = runningY;
	}


	private void drawDataLoadingPage(){
		background(240);
		//title
		textFont(title_font);
		fill(120);
		textAlign(LEFT, TOP);
		text("Sequence Diversity Diagram", title_x, title_y);

		//file name area
		fill(255);
		stroke(120);
		strokeWeight(1);
		rect(conf_file_rect.x, conf_file_rect.y, conf_file_rect.width, conf_file_rect.height);


		//buttons
		fill(120);
		stroke(60);
		strokeWeight(1);
		fill(conf_btn.contains(mouseX, mouseY)? 120 : 180);
		rect(conf_btn.x, conf_btn.y, conf_btn.width, conf_btn.height);
		if(isValidConf){
			fill(start_btn.contains(mouseX, mouseY)? 120 : 180);
		}else{
			//inactive
			fill(220);
		}
		rect(start_btn.x, start_btn.y, start_btn.width, start_btn.height);
		fill(example_btn.contains(mouseX, mouseY)? 120 : 180);
		rect(example_btn.x, example_btn.y, example_btn.width, example_btn.height);



		//button text
		fill(255);
		noStroke();
		textFont(btn_font);
		textAlign(CENTER, CENTER);
		text("Select", (float)conf_btn.getCenterX(), (float) conf_btn.getCenterY());
		text("Start", (float)start_btn.getCenterX(), (float) start_btn.getCenterY());
		text("Example", (float)example_btn.getCenterX(), (float) example_btn.getCenterY());

		//text instruction
		fill(120);
		textAlign(LEFT, BOTTOM);
		text("Select a configuration file:", conf_file_rect.x, conf_file_rect.y);

		//selected file
		textAlign(LEFT, CENTER);
		fill(120);
		if(conf_file != null){
			String path = conf_file.getAbsolutePath();
			text(" ..."+path.substring(path.length()-45, path.length()), conf_file_rect.x, (float) conf_file_rect.getCenterY());
		}

		//content
		textAlign(LEFT, CENTER);
		fill(120);
		text(conf_content, _content_x, _content_y);

		int content_height = 15*conf_content_line_count +_MARGIN;
		fill(color_red);
		text(conf_error, _content_x, _content_y + content_height);


	}

	//while loading the data
	private void drawDataLoader(){
		background(240);
		fill(120);
		textAlign(CENTER, CENTER);
		text("Loading the data... ", _STAGE_WIDTH/2, _STAGE_HEIGHT/2);
		text(status_msg, _STAGE_WIDTH/2, _STAGE_HEIGHT/2+_MARGIN*2);
	}




	//loading configuration
	


	class LoadingThread implements Runnable{
		public void run(){
			_FILE_SELECTED = true;
			_FILE_LOADING = true;

			startLoadingData();
		}
	}



	// do one sample at a time
	private SeDDModel[] samples;
	private int _total_length;
	private int _total_variation;

	//setting from configuration/
	private char[] all_aa_default;// = {'A', 'I', 'L', 'V', 'F','W','Y','N', 'C', 'Q', 'M', 'S', 'T','D', 'E','R', 'H', 'K','G','P','.', 'X'};
	private char[][] aa_group_default; // = {{ 'A', 'G','I', 'L', 'V'}, {'C','M','S','T'},{'P'},{'F','W', 'Y'},{'H', 'K', 'R'},{'D','E','N','Q'},{'.'}, {'X'}};
	private String[] _AA_Group;// = {"aliphatic", "OH or Sulfur", "cyclic", "aromatic", "basic", "acidic", "blank", "unknown"};
	private String[] sample_names;
	private String[] file_urls;
	private int[] sample_colors;


//	private HashMap<String, Rectangle> aa_rect_map;
	private HashMap<String, Character> aminoAcidMap; //amino acid codes



//	private float _mean, _sd;


	private void loadData(){
		samples = new SeDDModel[sample_names.length];
		// samples = new Sample[2];
		//load data
		for(int i = 0; i<samples.length; i++){
			status_msg = "loading "+sample_names[i]+" aligned sequences";
			samples[i] = new SeDDModel(new SequencingSample(file_urls[i], sample_names[i], this), this);
		}
		// samples[0] = new Sample("panel-a.txt", "all");
		// samples[0] = new Sample("panel-b.txt", "gram_negative");
		// samples[1] = new Sample("panel-c.txt", "gram_positive");

		_total_length = samples[0].getSequencingSample().getSeq_length();
		_total_variation = all_aa_default.length;


		println("debug: total_length = "+_total_length +" total_variation = "+ _total_variation);
		// println(dataPath("panel-b.txt"));
	}

	//calculate frequency
	private void preprocessData(){
		aminoAcidMap = setupAminoAcidMap();

		for(SeDDModel s: samples){
			// println("\tpreprcessData():"+s.name);
			status_msg = "preprocessing: calculating frequency for "+s.getSequencingSample().getName();
			s.calculateFrequency();


			//flags
			s.setPositionSelected(new boolean[_total_length]);
			Arrays.fill(s.getPositionSelected(), Boolean.FALSE);
		}
		// println("debug: end of preprocessData()");
	}


//	private float log2(float n){
//		// return n*_line_weight;
//		return (float)(Math.log(n)/Math.log(2));
//	}

//	private float getMean(float[][] matrix){
//		float sum = 0;
//		for(float[] row : matrix){
//			for(float f : row){
//				sum += f;
//			}
//		}
//		return sum/(matrix.length*matrix[0].length);
//	}

//	private float getSD(float[][] matrix){
//		return sqrt(getVariance(matrix));
//	}
//	private float getVariance(float[][] matrix){
//		int size = matrix.length*matrix[0].length;
//		float mean = getMean(matrix);
//		float temp = 0;
//		for(float[] row : matrix){
//			for(float f : row){
//				temp += (f-mean)* (f- mean);
//			}
//		}
//		return temp/size;
//	}

	//pdb stuff
	private HashMap<String, Character> setupAminoAcidMap(){
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

//	private Character get1LetterCode(String code3){
//		Character code1 = null;
//		if(aminoAcidMap == null){
//			aminoAcidMap = setupAminoAcidMap();
//		}
//		code1 = (Character) aminoAcidMap.get(code3);
//		if (code1 == null) {
//			println("Error: get1LetterCode()");
//			return null;
//		} else {
//			return code1;
//		}
//	}

	private int color_alpha = 120;
	private int color_red = color(202, 0, 32);
//	private int color_blue = color(5, 113, 176);

	//layout elements
	private ArrayList<String> all_aa;
	private ArrayList<AA_Group> all_aa_group;
	private HashMap<String, Float> layout_y_map; //update
	private float[] layout_x;


	//layout grid
//	private PGraphics grids = null;

//	private PGraphics mip_pg = null;
//	private int _heatmap_cell_width = 8;
//	private int _heatmap_cell_height = 8;


	private Rectangle[] aa_up_btns, aa_down_btns;
	private String[] aa_order;
	private Rectangle[] aa_group_up_btns, aa_group_down_btns;
	private Rectangle ind_btn_rect, group_btn_rect;
	private Rectangle pdf_btn, png_btn;

//	private Rectangle[] pos_btns;
//	private Rectangle pos_btn_area;
	private Rectangle[] mip_rects; //MIp rectangles per sample

	private float _circos_min_radian = 0;
	private float _circos_max_radian = TWO_PI;
//	private float _circos_radius = 0;
	private float rotation = -HALF_PI;

	//information content
//	private float _min_info_content = Float.MAX_VALUE;//= 0;
//	private float _max_info_content = Float.MIN_VALUE;//= 5;
//	private float _threshold_info_content = 4;

	//circos
	private float out_radius, in_radius; //, arc_radius; //arc is between in and out
//	private String[] btn_labels = {"get common sequence", "load pdb", "update protein view"};


	//legend
	private Rectangle[] _legend_label_rect;
	private int[] _legend_percentage = {100, 50, 10, 1};
	private String[] _legend_nb_label ={"Threshold", "Node Width", "Gap Width"};
	private NumberBox[] _legend_numberbox;


	private void setStageSize(){
		status_msg = "setting up the display";

		int sankey_x = _LEFT_MARGIN;
		int sankey_y = _TOP_MARGIN;

		//drawing area
		_DISPLAY_RECT = new Rectangle(_LEFT_MARGIN, _TOP_MARGIN, displayWidth-_LEFT_MARGIN -_RIGHT_MARGIN, displayHeight-_TOP_MARGIN*2);
		//sankey area
		float temp_sankey_width = _DISPLAY_RECT.width * _SANKEY_RATIO;
		float temp_sankey_height = _DISPLAY_RECT.height * _TOP_RATIO;
		//determine node size
		int number_of_gap = _AA_Group.length - 1;
		float _total_height_ratio = (float)number_of_gap*_VERTICAL_GAP_RATIO + _total_variation;
		_node_h = round(temp_sankey_height/_total_height_ratio);
		//adjust _node_h
		if(_node_h > _MARGIN*2){
			_node_h = _MARGIN*2;
		}
		//vertical gap is half of node height
		_vertical_gap = round(_node_h*_VERTICAL_GAP_RATIO);
		//horizontal gap is *2 of node width
		number_of_gap = _total_length - 1;
		float _total_width_ratio = (float)number_of_gap*_HORIZONTAL_GAP_RATIO + _total_length + 2; //2 = label width
		float temp_width =  (float)temp_sankey_width/_total_width_ratio;
		if(temp_width < _MARGIN){
			//set it as minimum
			_node_w = _MARGIN;
			_horizontal_gap = _node_w*2;
		}else if(temp_width >_MARGIN*2){
			_node_w = _MARGIN*2;
			_horizontal_gap = _node_w*2;
		}else{
			_node_w = ceil(temp_width);
			_horizontal_gap = _node_w*2;
		}
		println("debug: _node_w="+_node_w +"  _node_h="+_node_h);
		//adjust height and width based on node size
		int sankey_height = _total_variation * _node_h + (_AA_Group.length-1)*_vertical_gap;
		int sankey_width = (_total_length+2) * _node_w + (_total_length -1)*_horizontal_gap;
		//PGraphic
		_SANKEY_HEIGHT = sankey_height;
		_SANKEY_WIDTH = sankey_width;
		if(temp_width < _MARGIN){
			//very long sequence
			_SANKEY_RECT = new Rectangle(sankey_x, sankey_y, round(temp_sankey_width), _SANKEY_HEIGHT);
			_SANKEY_SLIDER_RECT = new Rectangle(_SANKEY_RECT.x, _SANKEY_RECT.y+_SANKEY_RECT.height, _SANKEY_RECT.width, _SANKEY_SLIDER_HEIGHT);	
			showSlider = true;
			updateSlider();
		}else if( temp_width >_MARGIN*2){
			//very short sequence
			_SANKEY_RECT = new Rectangle(sankey_x, sankey_y, round(temp_sankey_width), _SANKEY_HEIGHT);
			_SANKEY_SLIDER_RECT = new Rectangle(_SANKEY_RECT.x, _SANKEY_RECT.y+_SANKEY_RECT.height, _SANKEY_RECT.width, _SANKEY_SLIDER_HEIGHT);	

		}else{
			_SANKEY_RECT = new Rectangle(sankey_x, sankey_y, _SANKEY_WIDTH, _SANKEY_HEIGHT);
			_SANKEY_SLIDER_RECT = new Rectangle(_SANKEY_RECT.x, _SANKEY_RECT.y+_SANKEY_RECT.height, _SANKEY_RECT.width, _SANKEY_SLIDER_HEIGHT);	
		}

		//button rect
		_BTN_RECT = new Rectangle(_SANKEY_RECT.x+_SANKEY_RECT.width, _SANKEY_RECT.y, _node_w*2, _SANKEY_RECT.height+_SANKEY_SLIDER_HEIGHT);

		//legend area
		_LEGEND_RECT = new Rectangle(_BTN_RECT.x+_BTN_RECT.width, _BTN_RECT.y, round(_DISPLAY_RECT.width*_LEGEND_RATIO - _node_w*2), _SANKEY_RECT.height+_SANKEY_SLIDER_HEIGHT+round(_DISPLAY_RECT.height*_BOTTOM_RATIO));
		int runningX = _LEGEND_RECT.x + _MARGIN;
		int runningY = _LEGEND_RECT.y;
		int _legend_item_width = _MARGIN*5;
		_legend_label_rect = new Rectangle[_legend_percentage.length];
		//label rectangle
		for(int i =  0; i< _legend_percentage.length; i++){
			_legend_label_rect[i] = new Rectangle(runningX, runningY, _legend_item_width, _node_h);
			runningY += _node_h+_MARGIN/2;
		}
		runningY+= _MARGIN;
		//sample toggle
		for(int i = 0; i < samples.length; i++){
			SeDDModel s = samples[i];	
			s.setToggle(new Rectangle(runningX, runningY, _legend_item_width, _node_h));
			runningY += _node_h+_MARGIN/2;
		}	
		//numberbox
		_legend_numberbox = new NumberBox[_legend_nb_label.length];
		runningY += _MARGIN;
		//frequency threshold
		_legend_numberbox[0] = new NumberBox(runningX, runningY, _legend_item_width, _node_h,_frequencyThreshold, 1f, 0f, _legend_nb_label[0] );
		_legend_numberbox[0].setText_color(0);
		runningY += _node_h+_MARGIN/2;
		//node width
		_legend_numberbox[1] = new NumberBox(runningX, runningY, _legend_item_width, _node_h,_node_w, 40f, 1f, _legend_nb_label[1]);
		_legend_numberbox[1].setText_color(0);
		runningY += _node_h+_MARGIN/2;
		//gap width
		_legend_numberbox[2] = new NumberBox(runningX, runningY, _legend_item_width, _node_h,_horizontal_gap, 50f, 1f, _legend_nb_label[2]);
		_legend_numberbox[2].setText_color(0);
		runningY += _node_h+_MARGIN;
		// runningY += _node_h+_MARGIN/2;
		//PDF export
		pdf_btn = new Rectangle(runningX, runningY,_legend_item_width, _node_h);
		runningY += _node_h+_MARGIN/2;
		png_btn = new Rectangle(runningX, runningY,_legend_item_width, _node_h);
		runningY += _node_h+_MARGIN;



		//setup  btns
		setup_aa_btns();
	}

	private void setup_aa_btns(){
		aa_up_btns = new Rectangle[all_aa.size()];
		aa_down_btns = new Rectangle[all_aa.size()];
		aa_order = new String[all_aa.size()]; //keep track of aa order in display

		int runningX = _BTN_RECT.x;
		float runningY = _BTN_RECT.y;
		int counter = 0;

		float btn_height = (float)_node_h/2;
		int btn_width = _node_w;

		//all indiviatual btn height
		ind_btn_rect  = new Rectangle(runningX, round(runningY), btn_width, _BTN_RECT.height);
		for(int i = 0; i< all_aa_group.size(); i++){
			AA_Group group = all_aa_group.get(i);
			ArrayList<String> aa_array = group.getAa();
			// println("group: "+group.name);
			for(int j = 0; j < aa_array.size(); j++){
				String aa = aa_array.get(j);
				// println("\t"+counter+":"+aa);
				aa_order[counter] = aa;
				aa_up_btns[counter] = new Rectangle(runningX, round(runningY), btn_width, round(btn_height));
				runningY += btn_height;
				aa_down_btns[counter] = new Rectangle(runningX, round(runningY), btn_width, round(btn_height));
				runningY += btn_height;
				counter ++;
			}
			//gap
			runningY +=  _vertical_gap;
		}


		//group btns
		aa_group_up_btns = new Rectangle[all_aa_group.size()];
		aa_group_down_btns = new Rectangle[all_aa_group.size()];
		runningX = round(_BTN_RECT.x +_node_w);
		runningY = _BTN_RECT.y;

		group_btn_rect = new Rectangle(runningX, round(runningY), btn_width, _BTN_RECT.height);
		for(int i = 0; i< all_aa_group.size(); i++){
			AA_Group group = all_aa_group.get(i);
			ArrayList<String> aa_array = group.getAa();
			float total_height = _node_h * aa_array.size();
			btn_height = total_height/2;
			aa_group_up_btns[i] = new Rectangle(runningX, round(runningY), btn_width, round(btn_height));
			runningY += btn_height;
			aa_group_down_btns[i] = new Rectangle(runningX, round(runningY), btn_width, round(btn_height));
			runningY += btn_height;
			runningY += _vertical_gap;
		}
		runningX += _node_w;
	}





	//initialise amino acid grouping
	private void initLayout(){
		status_msg = "initializing a layout";
		all_aa = new ArrayList<String>();
		all_aa_group = new ArrayList<AA_Group>();

		//add all amino acid
		for(int i = 0; i < all_aa_default.length; i++){
			all_aa.add(""+all_aa_default[i]);
		}
		//make amino acid groups
		for(int i = 0; i < _AA_Group.length; i++){
			AA_Group group = new AA_Group(_AA_Group[i]);
			char[] aas = aa_group_default[i]; 
			for(int j = 0; j< aas.length; j++){
				group.getAa().add(""+aas[j]);
			}
			all_aa_group.add(group);
		}

		// println("debug: end of initLayout()");
	}
	//determine display position based on all_aa and all_aa_group
	private void updateLayout(){
		println("--- updateLayout()");
		status_msg = "updating the layout";
		//reset
		layout_y_map = new HashMap<String, Float>();
		layout_x = new float[_total_length];
		int sankey_height = _total_variation * _node_h + (_AA_Group.length-1)*_vertical_gap;
		int sankey_width = (_total_length+2) * _node_w + (_total_length -1)*_horizontal_gap;
		//PGraphic
		_SANKEY_HEIGHT = sankey_height;
		_SANKEY_WIDTH = sankey_width;

		if(_SANKEY_WIDTH > _SANKEY_RECT.width){
			showSlider = true;
			updateSlider();
		}else{
			showSlider = false;
		}

		//amino acid positions (y)
		float runningY = _SANKEY_RECT.y;
		for(int i = 0; i< all_aa_group.size(); i++){
			AA_Group group = all_aa_group.get(i);
			ArrayList<String> aas = group.getAa();
			for(int j = 0; j< aas.size(); j++){
				layout_y_map.put(aas.get(j), new Float(runningY));
				runningY += _node_h;
			}
			runningY += _vertical_gap;
		}

		//x positions
		float runningX =_SANKEY_RECT.x + _node_w;
		for(int i = 0; i<_total_length; i++){
			layout_x[i] = runningX;
			runningX += _node_w+_horizontal_gap;
		}
		// println("debug: end of updateLayout()");
	}

	//make PGraphics per Sample
	//make GeneralPath for each Edge
	private void updateEdges(){
		//persample
		for(int i = 0; i < samples.length; i++){
			SeDDModel sample = samples[i];
			status_msg = "updating the plot for "+sample.getSequencingSample().getName();
			//assgin edge position
			sample.assignEdgePositions(layout_y_map, layout_x, _half_node_w, _node_h, isExponentialScaling, base);
			// sample.createColorPG(color_cyan, 160);
			createPGraphics(sample, sample_colors[i], color_alpha);

			println("debug: end of updateEdges():"+sample.getSequencingSample().getName());
		}
		println("debug: end of updateEdges()");
	}


	//draw edges
	private void createPGraphics(SeDDModel s, int c, int alpha){
		s.setFlow_pg(createGraphics(_SANKEY_RECT.x+_SANKEY_WIDTH, _SANKEY_RECT.y+_SANKEY_HEIGHT));
		s.getFlow_pg().beginDraw();
		// flow_pg.background(255);
		s.getFlow_pg().fill(c);
		s.getFlow_pg().noStroke();
		for(int i = 0; i< s.getDimensions().length-1; i++){
			Position d = s.getDimensions()[i];
			//draw edges
			for(Edge e :d.getEdges()){
				// println((i+1)+"-"+(i+2)+":"+e.toString());
				if(e.getSequences().size() > 0 && e.getFrequency() >= _frequencyThreshold){
					s.getFlow_pg().fill(c, alpha);
					s.getFlow_pg().noStroke();
					if(i == 0){
						e.setPath(drawSankey2(s.getFlow_pg(), e.getFrom_dx() - _half_node_w, e.getFrom_dy(), e.getTo_dx(), e.getTo_dy(), 0.5f, 0.5f, e.getThickness(), _node_w, _half_node_w));
					}else if( i == s.getDimensions().length-2){
						e.setPath(drawSankey2(s.getFlow_pg(), e.getFrom_dx(), e.getFrom_dy(), e.getTo_dx() + _half_node_w, e.getTo_dy(), 0.5f, 0.5f, e.getThickness(), _half_node_w, _node_w));
					}else{
						e.setPath(drawSankey2(s.getFlow_pg(), e.getFrom_dx(), e.getFrom_dy(), e.getTo_dx(), e.getTo_dy(), 0.5f, 0.5f, e.getThickness(), _half_node_w, _half_node_w));
					}
				}
			}
		}
		s.getFlow_pg().endDraw();
		//embedding
		s.setDisplay_pg(createGraphics(_SANKEY_RECT.width, _SANKEY_RECT.height));
		s.getDisplay_pg().beginDraw();
		s.getDisplay_pg().image(s.getFlow_pg(), -1*_SANKEY_RECT.x-_SANKEY_POS, -1* _SANKEY_RECT.y);
		s.getDisplay_pg().endDraw();
	}

	//sankey slider
	private void updateSlider(){
		float bar_ratio = (float)_SANKEY_RECT.width / (float)_SANKEY_WIDTH;
		int bar_width = round(_SANKEY_RECT.width*bar_ratio);
		int bar_x = _SANKEY_POS + _SANKEY_SLIDER_RECT.x;
		if(bar_x+bar_width >_SANKEY_SLIDER_RECT.x + _SANKEY_SLIDER_RECT.width){
			int diff = (bar_x+bar_width) - (_SANKEY_SLIDER_RECT.x+_SANKEY_SLIDER_RECT.width);
			_SANKEY_POS -= diff;
			bar_x = _SANKEY_POS + _SANKEY_SLIDER_RECT.x;
		}


		_SANKEY_SLIDER_BAR = new Rectangle(_SANKEY_POS+_SANKEY_SLIDER_RECT.x, _SANKEY_SLIDER_RECT.y, bar_width, _SANKEY_SLIDER_RECT.height);

		println("Debug: updateSlider(): bar ratio ="+bar_ratio);
	}



	// x1, y1, x2, y2, radian1, radian2
	private void bezierConnection(PGraphics pg, float x1, float y1, float x2, float y2, float o_r, float d_r, float cx, float cy, float radius) {
		//adjust fraction based on the distance
		float dist_radian = abs(o_r - d_r);
		if(dist_radian > PI){
			dist_radian -= PI;
		}
		float fraction = map(dist_radian, 0, PI, 0.9f, 0.1f);
		float handleRadius = radius * fraction;
		// float handleRadius = _radius * bezierFraction;
		float cx1 = cx + (handleRadius * cos(o_r + rotation));
		float cy1 = cy + (handleRadius * sin(o_r + rotation));
		float cx2 = cx + ((handleRadius) * cos(d_r + rotation));
		float cy2 = cy + ((handleRadius) * sin(d_r + rotation));

		pg.beginShape();
		pg.vertex(x1, y1);
		pg.bezierVertex(cx1, cy1, cx2, cy2, x2, y2);
		pg.endShape();
	}

	private void bezierConnection_PDF(float x1, float y1, float x2, float y2, float o_r, float d_r, float cx, float cy, float radius) {
		//adjust fraction based on the distance
		float dist_radian = abs(o_r - d_r);
		if(dist_radian > PI){
			dist_radian -= PI;
		}
		float fraction = map(dist_radian, 0, PI, 0.9f, 0.1f);
		float handleRadius = radius * fraction;
		// float handleRadius = _radius * bezierFraction;
		float cx1 = cx + (handleRadius * cos(o_r + rotation));
		float cy1 = cy + (handleRadius * sin(o_r + rotation));
		float cx2 = cx + ((handleRadius) * cos(d_r + rotation));
		float cy2 = cy + ((handleRadius) * sin(d_r + rotation));

		beginShape();
		vertex(x1, y1);
		bezierVertex(cx1, cy1, cx2, cy2, x2, y2);
		endShape();
	}

	public void draw(){
		// println("--- draw()");
		if(!_FILE_SELECTED){
			//file loading page
			drawDataLoadingPage();
		}else if(_FILE_LOADING){
			//loading file
			drawDataLoader();
		}else if(_FILE_LOADED){
			// if(record){
			// 	beginRecord(PDF, timestamp()+".pdf");
			// 	textFont(font);
			// }
			background(255);  
			//background
			drawBackgroundGrid();
			//draw buttons
			drawBtns();
			if(sequences_selected){
				for(int i = 0; i < selected_sequences.size(); i++){
					ArrayList<Sequence> seq = selected_sequences.get(i);
					if(seq.size() > 0){
						//draw sequences
//						samples[i].drawSelectedSequence(seq, sample_colors[i]);
						drawSelectedSequence(samples[i], seq, sample_colors[i]);
					}
				}
			}else{
				// draw image twice
				for(int i = 0; i<samples.length; i++){
					if(samples[i].isShowing()){
						image(samples[i].getDisplay_pg(), _SANKEY_RECT.x, _SANKEY_RECT.y);
					}
				}
				for(int i = 0; i<samples.length; i++){
					if(samples[i].isShowing()){
						image(samples[i].getDisplay_pg(), _SANKEY_RECT.x, _SANKEY_RECT.y);
					}
				}
			}

			drawMouseOver();
			drawLegend();
			//slider
			drawSlider();
			if(draw_counter > draw_max){
				println("stop looping ---------");
				noLoop();
				draw_counter = 0;
			}else{
				draw_counter ++;
			}
			// if(record){
			// 	endRecord();
			// 	record = false;
			// }
		}
	}
	
	

	private void drawSelectedSequence(SeDDModel sample, ArrayList<Sequence> seq, int c){
		noStroke();
		fill(c);
		for(int i = 0; i< sample.getDimensions().length-1; i++){
			Position d = sample.getDimensions()[i];
			//draw edges
			for(Edge e :d.getEdges()){
				if(e.getTo_dx() < _SANKEY_RECT.x+_SANKEY_RECT.width){
					//check overlap with seq
					ArrayList<Sequence> intersection = intersection(seq, e.getSequences());
					if(intersection.size()>0){
						float thickness = map(intersection.size(), 0, (float) sample.getSequencingSample().getTotal_count(), 0f, _node_h);
						// if(isExponentialScaling){
						// 	thickness = log_map(intersection.size(), 0, (float)total_count, 0f, _node_h, base);
						// }

						if(i == 0){
							drawSankey2(g, e.getFrom_dx() - _half_node_w, e.getFrom_dy(), e.getTo_dx(), e.getTo_dy(), 0.5f, 0.5f, thickness, _node_w, _half_node_w);	
						}else if( i == sample.getDimensions().length-2){
							drawSankey2(g, e.getFrom_dx(), e.getFrom_dy(), e.getTo_dx() + _half_node_w, e.getTo_dy(), 0.5f, 0.5f, thickness, _half_node_w, _node_w);	
						}else{
							drawSankey2(g, e.getFrom_dx(), e.getFrom_dy(), e.getTo_dx(), e.getTo_dy(), 0.5f, 0.5f, thickness, _half_node_w, _half_node_w);	
						}		
					}
				}
			}
		}
	}
	
	private ArrayList<Sequence> intersection(ArrayList<Sequence> list1, ArrayList<Sequence> list2) {
		ArrayList<Sequence> list = new ArrayList<Sequence>();
		for (Sequence t : list1) {
			if(list2.contains(t)) {
				list.add(t);
			}
		}
		return list;
	}
	
	private GeneralPath drawSankey2(PGraphics pg, float x_f, float y_from, float x_t, float y_to, float min_r_left, float min_r_right, float thickness, float from_offset, float to_offset){
		GeneralPath trace = new GeneralPath();
		float x_from = x_f + from_offset;
		float x_to = x_t - to_offset;

		int isDownSlope = 0; //1 = down slope, 2 = up slope
		if (y_from < y_to){
			isDownSlope = 1;
		}else if(y_from > y_to){
			isDownSlope = -1;
		}else{
			//flat line
			isDownSlope = -1;
		}
		float f1 = min_r_left + thickness / 2 + isDownSlope * (thickness / 2);
		float f2 = min_r_left + thickness / 2 - isDownSlope * (thickness / 2);
		float f3 = min_r_right + thickness / 2 - isDownSlope * (thickness / 2);
		float f4 = min_r_right + thickness / 2 + isDownSlope * (thickness / 2);

		float p1 = x_from;
		float q1 = y_from + thickness / 2 + isDownSlope * thickness / 2 + isDownSlope * min_r_left;
		float p2 = x_to;
		float q2 = y_to + thickness / 2 - isDownSlope * thickness / 2 - isDownSlope * min_r_right;
		// indentation
		// int i1 = x_from + thickness / 5;
		// int i2 = y_from + thickness / 2;
		// int i3 = x_to + thickness / 5;
		// int i4 = y_to + thickness / 2;

		float f5 = sq(p2 - p1) + sq(q2 - q1);

		float f6 = sqrt(f5 - sq(f3 + f1));
		float f7 = ((q2 - q1) * f6 + isDownSlope * (p2 - p1) * (f3 + f1)) / f5;
		float f8 = ((p2 - p1) * f6 - isDownSlope * (q2 - q1) * (f3 + f1)) / f5;
		float rx1 = (p1 + isDownSlope * f1 * f7);
		float ry1 = (q1 - isDownSlope * f1 * f8);
		float rx2 = (p2 - isDownSlope * f3 * f7);
		float ry2 = (q2 + isDownSlope * f3 * f8);

		float f9 = sqrt(f5 - sq(f2 + f4));
		float f10 = ((q2 - q1) * f9 + isDownSlope * (p2 - p1) * (f2 + f4)) / f5;
		float f11 = ((p2 - p1) * f9 - isDownSlope * (q2 - q1) * (f2 + f4)) / f5;

		float rx3 = (p1 + isDownSlope * f2 * f10);
		float ry3 = (q1 - isDownSlope * f2 * f11);
		float rx4 = (p2 - isDownSlope * f4 * f10);
		float ry4 = (q2 + isDownSlope * f4 * f11);

		float angle = (2.0F * (atan2(ry1 - y_from, rx1 - x_from) % TWO_PI));
		// pg.fill(0);
		// pg.noStroke();
		pg.smooth();
		pg.beginShape();

		float f12;
		if (isDownSlope == 1) {
			pg.vertex(x_f, y_from);
			pg.vertex(x_from, y_from);
			trace.moveTo(x_f, y_from);
			trace.lineTo(x_from, y_from);
			for (f12 = 0.0F; f12 < angle; f12 += 0.02F) {
				float dx = p1 + f1 * cos(f12 - isDownSlope * PI / 2.0F);
				float dy = q1 + f1 * sin(f12 - isDownSlope * PI / 2.0F);
				pg.curveVertex(dx, dy);
				trace.lineTo(dx, dy);
			}
			pg.vertex(rx1, ry1);
			pg.vertex(rx2, ry2);
			trace.lineTo(rx1, ry1);
			trace.lineTo(rx2, ry2);
			for (f12 = angle; f12 > 0.0F; f12 -= isDownSlope * 0.02F) {
				float dx = p2 + f3 * cos(f12 + isDownSlope * PI / 2.0F);
				float dy = q2 + f3 * sin(f12 + isDownSlope * PI / 2.0F);
				pg.curveVertex(dx, dy);
				trace.lineTo(dx, dy);
			}
			pg.vertex(x_to, y_to);
			pg.vertex(x_t, y_to);
			pg.vertex(x_t, y_to+thickness);	
			pg.vertex(x_to, y_to + thickness);

			trace.lineTo(x_to, y_to);
			trace.lineTo(x_t, y_to);
			trace.lineTo(x_t, y_to+thickness);
			trace.lineTo(x_to, y_to +thickness);
			// pg.vertex(i3, i4);

			for (f12 = 0.0F; f12 < angle; f12 += isDownSlope * 0.02F) {
				float dx = p2 + f4 * cos(f12 + isDownSlope * PI / 2.0F);
				float dy = q2 + f4 * sin(f12 + isDownSlope * PI / 2.0F);
				pg.curveVertex(dx, dy);
				trace.lineTo(dx, dy);
			}
			pg.vertex(rx4, ry4);
			pg.vertex(rx3, ry3);
			for (f12 = angle; f12 > 0.0F; f12 -= isDownSlope * 0.02F) {
				float dx = p1 + f2 * cos(f12 - isDownSlope * PI / 2.0F);
				float dy = q1 + f2 * sin(f12 - isDownSlope * PI / 2.0F);
				pg.curveVertex(dx, dy);
				trace.lineTo(dx, dy);
			}
			// pg.vertex(i1, i2);
			pg.vertex(x_from, y_from+thickness);
			pg.vertex(x_f, y_from+thickness);
			trace.lineTo(x_from, y_from+thickness);
			trace.lineTo(x_f, y_from+thickness);

			pg.endShape();
			trace.closePath();
		}else {
			pg.vertex(x_f, y_from);
			pg.vertex(x_from, y_from);
			trace.moveTo(x_f, y_from);
			trace.lineTo(x_from, y_from);
			for (f12 = PI; f12 > PI + angle; f12 -= 0.02F) {
				float dx = p1 + f1 * cos(f12 - HALF_PI);
				float dy = q1 + f1 * sin(f12 - HALF_PI);
				pg.curveVertex(dx , dy);
				trace.lineTo(dx, dy);
			}
			pg.vertex(rx1, ry1);
			pg.vertex(rx2, ry2);
			for (f12 = angle; f12 < 0.0F; f12 += 0.02F) {
				float dx = p2 + f3 * cos(f12 - HALF_PI);
				float dy = q2 + f3 * sin(f12 - HALF_PI);
				pg.curveVertex(dx, dy);
				trace.lineTo(dx, dy);
			}
			pg.vertex(x_to, y_to);
			pg.vertex(x_t, y_to);
			// pg.vertex(i3, i4);
			pg.vertex(x_t, y_to + thickness);
			pg.vertex(x_to, y_to + thickness);

			trace.lineTo(x_to, y_to);
			trace.lineTo(x_t, y_to);
			trace.lineTo(x_t, y_to + thickness);
			trace.lineTo(x_to, y_to + thickness);
			for (f12 = 0.0F; f12 > angle; f12 -= 0.02F) {
				float dx = p2 + f4 * cos(f12 - HALF_PI);
				float dy = q2 + f4 * sin(f12 - HALF_PI);
				pg.curveVertex(dx, dy);
				trace.lineTo(dx, dy);
			}
			pg.vertex(rx4, ry4);
			pg.vertex(rx3, ry3);
			trace.lineTo(rx4, ry4);
			trace.lineTo(rx3, ry3);
			for (f12 = PI + angle; f12 < PI; f12 += 0.02F) {
				float dx = p1 + f2 * cos(f12 - HALF_PI);
				float dy = q1 + f2 * sin(f12 - HALF_PI);
				pg.curveVertex(dx,dy);
				trace.lineTo(dx, dy);
			}
			// pg.vertex(i1, i2);
			pg.vertex(x_from, y_from+thickness);
			pg.vertex(x_f, y_from+thickness);

			trace.lineTo(x_from, y_from+thickness);
			trace.lineTo(x_f, y_from+thickness);
			pg.endShape();
			trace.closePath();
		}
		return trace;
	}
	
	private void updatePGraphics(SeDDModel s){
		s.getDisplay_pg().clear();
		s.getDisplay_pg().beginDraw();
		s.getDisplay_pg().image(s.getFlow_pg(), -1*_SANKEY_RECT.x-_SANKEY_POS ,-1* _SANKEY_RECT.y);
		s.getDisplay_pg().endDraw();
	}

	//pdf drawing
	private void drawPDF(PGraphics pdf, int e_w, int e_h){
		int e_sankey_width = e_w - _LEFT_MARGIN - _RIGHT_MARGIN -_LEGEND_RECT.width;
//		int e_sankey_height = _SANKEY_HEIGHT;
		int e_sankey_x = _LEFT_MARGIN;
//		int e_sankey_y = _TOP_MARGIN;
		int e_legend_x = e_w - _LEGEND_RECT.width - _RIGHT_MARGIN + _MARGIN;
		pdf.background(255);  
		//background
		pdf.fill(235);
		pdf.noStroke();
		//per category background
		// println("debug: group count ="+all_aa_group.size());
		for(int i = 0; i < all_aa_group.size(); i++){
			AA_Group group = all_aa_group.get(i);
			String first_aa = group.getAa().get(0);
			String last_aa = group.getAa().get(group.getAa().size()-1);
			float first_y = getFloat((Float) layout_y_map.get(first_aa));
			float last_y = getFloat((Float) layout_y_map.get(last_aa));
			float group_height = last_y - first_y + _node_h;
			pdf.rect(e_sankey_x, first_y, e_sankey_width, group_height);
		}

		//dimensions label
		for(int i = 0; i < layout_x.length; i++){	
			pdf.fill(0);
			pdf.textAlign(CENTER, BOTTOM);
			pdf.text(i+1, layout_x[i]+(_node_w/2), _SANKEY_RECT.y);
		}
		pdf.rectMode(CORNER);
		//aa label
		pdf.fill(0);
		for(int i = 0; i<all_aa.size(); i++){
			String aa = all_aa.get(i);
			float aa_y = getFloat((Float) layout_y_map.get(aa)); 
			pdf.textAlign(LEFT, TOP);
			pdf.text(aa, e_sankey_x, aa_y - _text_margin);
			pdf.textAlign(RIGHT, TOP);
			pdf.text(aa, e_sankey_x+e_sankey_width, aa_y - _text_margin);
		}
		//aa group label
		pdf.fill(0);
		pdf.textAlign(RIGHT, TOP);
		for(int i = 0; i < all_aa_group.size(); i++){
			AA_Group group = all_aa_group.get(i);
			String first_aa = group.getAa().get(0);
			float aa_y = getFloat((Float) layout_y_map.get(first_aa)); 
			pdf.text(group.getName(), _SANKEY_RECT.x - _text_margin, aa_y - _text_margin);
		}

		// draw image twice
		for(int i = 0; i<samples.length; i++){
			if(samples[i].isShowing()){
				// samples[i].drawPDF(pdf, sample_colors[i], 200);
				pdf.image(samples[i].getFlow_pg(), 0, 0);
			}
		}
		for(int i = 0; i<samples.length; i++){
			if(samples[i].isShowing()){
				// samples[i].drawPDF(pdf, sample_colors[i], 200);
				pdf.image(samples[i].getFlow_pg(), 0, 0);
			}
		}

		// drawLegend();
		pdf.fill(0);
		pdf.textAlign(LEFT, TOP);

		for(int i =  0; i< _legend_percentage.length; i++){
			Rectangle rect = _legend_label_rect[i];
			float real_height = map(_legend_percentage[i], 0, 100, 0, _node_h);
			pdf.rectMode(CORNER);
			pdf.fill(180);
			pdf.noStroke();
			pdf.rect(e_legend_x, rect.y, rect.width, real_height);
			pdf.fill(0);
			pdf.text(_legend_percentage[i]+"%", e_legend_x+rect.width+_MARGIN/2, rect.y);
		}

		for(int i = 0; i < samples.length; i++){
			SeDDModel s = samples[i];
			Rectangle rect = s.getToggle();
			pdf.rectMode(CORNER);
			if(s.isShowing()){
				pdf.fill(sample_colors[i]);
				pdf.noStroke();
			}else{
				pdf.noFill();
				pdf.stroke(sample_colors[i]);
				pdf.strokeWeight(1);
			}
			pdf.rect(e_legend_x, rect.y, rect.width, rect.height);
			pdf.fill(0);
			pdf.text(s.getSequencingSample().getName(), e_legend_x+rect.width+_MARGIN/2, rect.y);
		}

	}

	//background and selection
	private void drawBackgroundGrid(){
		// println("\tdrawBackgroundGrid()");
		//background grid
		fill(235);
		noStroke();
		//per category background
		// println("debug: group count ="+all_aa_group.size());
		for(int i = 0; i < all_aa_group.size(); i++){
			AA_Group group = all_aa_group.get(i);
			String first_aa = group.getAa().get(0);
			String last_aa = group.getAa().get(group.getAa().size()-1);

			float first_y = getFloat((Float) layout_y_map.get(first_aa));
			float last_y = getFloat((Float) layout_y_map.get(last_aa));
			float group_height = last_y - first_y + _node_h;
			rect(_SANKEY_RECT.x, first_y, _SANKEY_RECT.width, group_height);
		}

		//dimensions label
		for(int i = 0; i < layout_x.length; i++){
			if(_SANKEY_RECT.x+_SANKEY_POS< layout_x[i] && layout_x[i] < _SANKEY_RECT.x+_SANKEY_RECT.width +_SANKEY_POS){
				//draw selection highlight
				int selection_bar_weight = 3;
				int selection_bar_gap = 1;
				for(int j = samples.length-1; j >= 0; j--){
					SeDDModel s = samples[j];
					if(s.getPositionSelected()[i]){
						int selection_bar_y = round(_SANKEY_RECT.y - _MARGIN*1.5f - (samples.length-j)*(selection_bar_weight+selection_bar_gap));
						stroke(sample_colors[j]);
						strokeWeight(3);
						strokeCap(SQUARE);
						noFill();
						line(layout_x[i]- _horizontal_gap/2 - _SANKEY_POS, selection_bar_y, layout_x[i]+_node_w+ _horizontal_gap/2 - _SANKEY_POS, selection_bar_y);
					}
				}
				fill(0);
				textAlign(CENTER, BOTTOM);
				text(i+1, layout_x[i]+(_node_w/2) - _SANKEY_POS, _SANKEY_RECT.y);
			}
		}
		rectMode(CORNER);
		//aa label
		fill(0);
		for(int i = 0; i<all_aa.size(); i++){
			String aa = all_aa.get(i);
			// Float f = (Float) layout_y_map.get(aa);
			float aa_y = getFloat((Float) layout_y_map.get(aa)); 
			textAlign(LEFT, TOP);
			text(aa, _SANKEY_RECT.x, aa_y - _text_margin);
			textAlign(RIGHT, TOP);
			text(aa, _SANKEY_RECT.x+_SANKEY_RECT.width, aa_y - _text_margin);
		}
		//aa group label
		fill(0);
		textAlign(RIGHT, TOP);
		for(int i = 0; i < all_aa_group.size(); i++){
			AA_Group group = all_aa_group.get(i);
			String first_aa = group.getAa().get(0);
			float aa_y = getFloat((Float) layout_y_map.get(first_aa)); 
			text(group.getName(), _SANKEY_RECT.x - _text_margin, aa_y - _text_margin);
		}

		// println("--- draw() end");
	}

	//buttons for line graph as well as circos
	private void drawBtns(){
		//group btn outline
		strokeWeight(1);
		for(Rectangle rect : aa_group_up_btns){
			stroke(200);
			fill(235);
			rect(rect.x, rect.y, rect.width, rect.height);
		}
		for(Rectangle rect : aa_group_down_btns){
			stroke(200);
			fill(235);
			rect(rect.x, rect.y, rect.width, rect.height);
		}
		for(Rectangle rect : aa_up_btns){
			stroke(200);
			fill(235);
			rect(rect.x, rect.y, rect.width, rect.height);
		}
		for(Rectangle rect : aa_down_btns){
			stroke(200);
			fill(235);
			rect(rect.x, rect.y, rect.width, rect.height);
		}

		//mouse over action
		noStroke();
		fill(120);
		if(hovering_ind_btn){
			if(hovering_up_btn){
				// fill(120);
				// rect(aa_up_btns[hover_btn_index].x, aa_up_btns[hover_btn_index].y, aa_up_btns[hover_btn_index].width, aa_up_btns[hover_btn_index].height);
				// fill(255);
				drawTriangle(aa_up_btns[hover_btn_index], true);
			}else{
				drawTriangle(aa_down_btns[hover_btn_index], false);
			}
		}else if(hovering_group_btn){
			if(hovering_up_btn){
				drawTriangle(aa_group_up_btns[hover_btn_index], true);
			}else{
				drawTriangle(aa_group_down_btns[hover_btn_index], false);
			}
		}

		//export buttons
		stroke(120);
		fill(180);
		rect(pdf_btn.x, pdf_btn.y, pdf_btn.width, pdf_btn.height);
		rect(png_btn.x, png_btn.y, png_btn.width, png_btn.height);
		//btn text
		fill(255);
		textAlign(CENTER, CENTER);
		text("PDF", (float)pdf_btn.getCenterX(), (float)pdf_btn.getCenterY());
		text("PNG", (float)png_btn.getCenterX(), (float)png_btn.getCenterY());


		textFont(font);
	}


	private float getFloat(Float f){
		return f.floatValue();
	}


	//legend and toggle button
	private void drawLegend(){
		fill(0);
		textAlign(LEFT, TOP);

		for(int i =  0; i< _legend_percentage.length; i++){
			Rectangle rect = _legend_label_rect[i];
			float real_height = map(_legend_percentage[i], 0, 100, 0, _node_h);
			rectMode(CORNER);
			fill(180);
			noStroke();
			rect(rect.x, rect.y, rect.width, real_height);
			fill(0);
			text(_legend_percentage[i]+"%", rect.x+rect.width+_MARGIN/2, rect.y);
		}

		for(int i = 0; i < samples.length; i++){
			SeDDModel s = samples[i];
			Rectangle rect = s.getToggle();
			rectMode(CORNER);
			if(s.isShowing()){
				fill(sample_colors[i]);
				noStroke();
			}else{
				noFill();
				stroke(sample_colors[i]);
				strokeWeight(1);
			}
			rect(rect.x, rect.y, rect.width, rect.height);
			fill(0);
			text(s.getSequencingSample().getName(), rect.x+rect.width+_MARGIN/2, rect.y);
		}

		//number box
		for(NumberBox nb: _legend_numberbox){
			nb.display();
		}
	}

	private void drawMouseOver(){
		if(hover_sample_index != -1){
			if(hover_pos_index != -1){
				//circos draw highlight
				Rectangle rect = mip_rects[hover_sample_index];
				//circos
				float center_x = (float) rect.getCenterX();
				float center_y = rect.y + ((float)_MATRIX_WIDTH/2);
				float radian = map(hover_pos_index, 0, _total_length, _circos_min_radian, _circos_max_radian) +rotation;
				// float dx = center_x + cos(radian + rotation)*(out_radius);
				// float dy = center_y + sin(radian + rotation)*(out_radius);


				//arc highlight
				noFill();
				stroke(60);
				strokeWeight(2);
				float r_increment = _circos_max_radian/_total_length;
				float from_radian = radian - r_increment/2;
				float to_radian = radian + r_increment/2;
				drawArc(center_x, center_y, in_radius, out_radius+_MARGIN, from_radian, to_radian);

				//arc line highlihgt
				// noFill();
				// stroke(60);
				// strokeWeight(2);
				// float r_increment = _circos_max_radian/_total_length;
				// float from_radian = radian - r_increment/2;
				// float to_radian = radian + r_increment/2;
				// arc(center_x, center_y, (out_radius+_MARGIN)*2, (out_radius+_MARGIN)*2, from_radian, to_radian);
			}
		}
	}

	private void drawArc(float dx, float dy, float in_r, float out_r, float s, float e){
		float kappa = 0.55228f * (e - s)/HALF_PI;
		float kappa_angle = atan(kappa);
		float kappa_radius_in = (float)in_r/cos(kappa_angle);
		float kappa_radius_out = (float)out_r/cos(kappa_angle);
		//anchor point
		PVector anc_1 = new PVector(dx + cos(s)*in_r, dy + sin(s)*in_r);
		PVector anc_2 = new PVector(dx + cos(s)*out_r, dy + sin(s)*out_r);
		PVector anc_3 = new PVector(dx + cos(e)*out_r, dy + sin(e)*out_r);
		PVector anc_4 = new PVector(dx + cos(e)*in_r, dy + sin(e)*in_r);

		PVector c1a = new PVector(dx + cos(s +kappa_angle)*kappa_radius_out, dy + sin(s+kappa_angle)*kappa_radius_out);
		PVector c1b = new PVector(dx + cos(e -kappa_angle)*kappa_radius_out, dy + sin(e-kappa_angle)*kappa_radius_out);

		//between 4 and 1 === in
		PVector c2a = new PVector(dx + cos(e -kappa_angle)*kappa_radius_in, dy + sin(e-kappa_angle)*kappa_radius_in);
		PVector c2b = new PVector(dx + cos(s +kappa_angle)*kappa_radius_in, dy + sin(s+kappa_angle)*kappa_radius_in);


		beginShape();
		vertex(anc_1.x, anc_1.y);
		vertex(anc_2.x, anc_2.y);
		bezierVertex(c1a.x, c1a.y, c1b.x, c1b.y, anc_3.x, anc_3.y);
		// vertex(anc_3.x, anc_3.y);
		vertex(anc_4.x, anc_4.y);
		// vertex(anc_1.x, anc_1.y);
		bezierVertex(c2a.x, c2a.y, c2b.x, c2b.y, anc_1.x, anc_1.y);
		endShape();
	}

	private void drawTriangle(Rectangle rect, boolean isUP){
		if(isUP){
			triangle(rect.x, rect.y+rect.height, (float)rect.getCenterX(), rect.y, rect.x+rect.width, rect.y+rect.height);
		}else{
			triangle(rect.x, rect.y, rect.x+rect.width, rect.y, (float)rect.getCenterX(), rect.y+rect.height);
		}
	}

	private void drawSlider(){
		if(showSlider){
			// noStroke();
			// fill(220);
			// rect(_SANKEY_SLIDER_RECT.x, _SANKEY_SLIDER_RECT.y, _SANKEY_SLIDER_RECT.width, _SANKEY_SLIDER_RECT.height);
			// fill(120);
			fill(_SANKEY_SLIDER_BAR.contains(mouseX, mouseY)?120:180);
			noStroke();
			rect(_SANKEY_SLIDER_BAR.x, _SANKEY_SLIDER_BAR.y, _SANKEY_SLIDER_BAR.width, _SANKEY_SLIDER_BAR.height, 5, 5, 5, 5);
		}
	}
	
	
	private class MyPolygon{
		private GeneralPath trace;


		private MyPolygon(float[] x, float[] y){
			trace = new GeneralPath();
			trace.moveTo(x[0], y[0]);
			for(int i = 1; i <x.length; i++){
				trace.lineTo(x[i], y[i]);
			}
			trace.closePath();
		}

		private void render(){
			PathIterator pi = trace.getPathIterator(null);
			float[] pts = new float[2];
			while (!pi.isDone()) {
				int type = pi.currentSegment(pts);
				if (type == PathIterator.SEG_MOVETO){
					beginShape();
					vertex(pts[0],pts[1]);
				}
				if (type == PathIterator.SEG_LINETO) { // LINETO
					vertex(pts[0],pts[1]);
					//println(pts[0]+","+pts[1]);
				}
				if (type == PathIterator.SEG_CLOSE) {
					endShape();
				}
				pi.next();
			}
		}
	}
	
	private class NumberBox{
		private int dw;
		private Rectangle num_rect, up_rect, down_rect;
		private float v_current, v_max, v_min;
		private String name;

		private int text_color = 60;

		private NumberBox(int dx, int dy, int dw, int dh, float v_current, float v_max, float v_min, String name){
			this.dw = dw;
			this.v_current = v_current;
			this.v_max = v_max;
			this.v_min = v_min;
			this.name = name;

			float btn_size = (float)ceil(dh /2);
			num_rect = new Rectangle(dx, dy, Math.round(dw-btn_size), dh);
			up_rect = new Rectangle(Math.round(dx+dw-btn_size), dy, Math.round(btn_size), Math.round(btn_size));
			down_rect = new Rectangle(Math.round(dx+dw-btn_size), Math.round(dy+dh-btn_size), Math.round(btn_size), Math.round(btn_size));

			// println("debug: numberbox created: min ="+this.v_min+"  max = "+this.v_max+"  current="+v_current);
		}


		private void display(){
			rectMode(CORNER);
			fill(255);
			stroke(120);
			strokeWeight(1);
			rect(num_rect.x, num_rect.y, num_rect.width, num_rect.height);
			//btns
			stroke(60);
			fill(up_rect.contains(mouseX, mouseY)? 120: 180);
			rect(up_rect.x, up_rect.y, up_rect.width, up_rect.height);
			fill(down_rect.contains(mouseX, mouseY)? 120: 180);
			rect(down_rect.x, down_rect.y, down_rect.width, down_rect.height);
			fill(255);
			drawTraiange(up_rect, true);
			drawTraiange(down_rect, false);

			//draw value
			fill(60);
			textFont(font);
			textAlign(RIGHT, CENTER);
			text(nf(v_current,0, 2), num_rect.x+num_rect.width -2, (float)num_rect.getCenterY());


			//label
			fill(text_color);
			textFont(font);
			textAlign(LEFT, CENTER);
			text(name, num_rect.x + dw +_MARGIN/2, (float)num_rect.getCenterY());

		}

		private void drawTraiange(Rectangle rect, boolean isUP){
			// fill(240);
			noStroke();
			if(isUP){
				triangle((float) rect.getCenterX(), rect.y+1, rect.x+rect.width -1, rect.y+rect.height-1, rect.x+1, rect.y+rect.height-1);
			}else{
				triangle((float) rect.getCenterX(), rect.y+rect.height-1, rect.x+rect.width -1, rect.y+1, rect.x+1, rect.y+1);
			}
		}

		private boolean contains(int mouseX, int mouseY){
			if(up_rect.x <= mouseX && mouseX <= up_rect.x+up_rect.width){
				if(up_rect.y <= mouseY && mouseY <= down_rect.y+down_rect.height ){
					return true;
				}
			}
			return false;
		}
		
		private float getV_current() {
			return v_current;
		}
		
		private void setV_current(float v_current) {
			this.v_current = v_current;
		}
		
		private float getV_max() {
			return v_max;
		}
		
		private float getV_min() {
			return v_min;
		}
		
		private Rectangle getUp_rect() {
			return up_rect;
		}
		
		private Rectangle getDown_rect() {
			return down_rect;
		}
		
		private void setText_color(int text_color) {
			this.text_color = text_color;
		}
	}
	
	
	
//	private class Sample{
//			private String name;
//			private ArrayList<Sequence> sequences;
//			private int seq_length;
//			private int total_count;
//			private Position[] dimensions;//0 to n-1
//
//
//
//			private PGraphics flow_pg;
//			private PGraphics getFlow_pg() {
//				return flow_pg;
//			}
//
//
//			private PGraphics display_pg; //test
//
//			private PGraphics getDisplay_pg() {
//				return display_pg;
//			}
//
//
//
//			//flags
//			private boolean isShowing = true;
//
//			private boolean[] positionSelected;
//			private HashSet<String> unique_sequence;
//
//
//			//buttons
//			private Rectangle toggle = null; //legend
//
//
//
//			//constructor and loading data
//			private Sample(String url, String name){
//				// println("Sample.contstructor: url ="+url+"  name="+name);
//				this.name = name;
//				sequences = new ArrayList<Sequence>();
//				unique_sequence = new HashSet<String>();
//				int non_unique_counter = 0;
//				//load data
//				String[] lines = loadStrings(url);
//				seq_length = lines[0].length();
//
//				//Sequence
//				//no sequence id 
//				for(int i = 0; i<lines.length; i++){
//					String line = lines[i].trim();
//					// println("\t"+line);
//					unique_sequence.add(line);
//					char[] array = line.toCharArray();
//					Sequence s = new Sequence(""+i, array);
//					sequences.add(s);
//				}
//
//				System.out.println("---- "+name+ "-----");
//				System.out.println("number of non_unique sequence removed = "+non_unique_counter);
//				System.out.println("number of unique sequence = "+unique_sequence.size());
//				System.out.println("sequence length = "+seq_length);
//				//dimensions
//				dimensions = new Position[seq_length];
//				for(int i = 0; i<seq_length; i++){
//					Position d = new Position(i);
//					//nodes
//					for(int j = 0; j < sequences.size(); j++){
//						Sequence s = sequences.get(j);
//						String aa = ""+s.getSequence()[i];
//						Node node  = (Node) d.getNode_map().get(aa);
//						if(node == null){
//							node = new Node(d, aa.charAt(0));
//							d.getNode_map().put(aa, node);
//							d.getNodes().add(node);
//						}	
//						node.addSequence(s);
//					}
//					dimensions[i] = d;
//				}
//
//
//				//edges
//				for(int j = 0; j < sequences.size(); j++){
//					Sequence s = sequences.get(j);
//					for(int k = 0; k <s.getSequence().length-1; k++){
//						String aa_from = ""+ s.getSequence()[k];
//						String aa_to = ""+s.getSequence()[k+1];
//						Position d_from = dimensions[k];
//						Position d_to = dimensions[k+1];
//						Node node_from = (Node)d_from.getNode_map().get(aa_from);
//						Node node_to = (Node)d_to.getNode_map().get(aa_to);
//
//						String aa_edge = aa_from+"_"+aa_to;
//						//find edge
//						Edge edge = (Edge) d_from.getEdge_map().get(aa_edge);
//						if(edge == null){
//							edge = new Edge(node_from, node_to, aa_edge);
//							d_from.getEdge_map().put(aa_edge, edge);
//							d_from.getEdges().add(edge);
//						}
//						edge.addSequence(s);
//					}
//				}
//
//
//				total_count = sequences.size();
//				// println("debgu: "+name+" has "+sequences.size()+" sequences!");
//				System.out.println("debug:"+name+"'s data is loaded ---- ");
//			}
//
//			//calculate frequency for node and edge
//			private void calculateFrequency(){
//				//per node
//				for(Position d : dimensions){
//					for(Node n : d.getNodes()){
//						n.setFrequency((float)n.getSequences().size()/(float)total_count);
//					}
//					for(Edge e: d.getEdges()){
//						e.setFrequency((float)e.getSequences().size()/(float)total_count);
//					}
//				}
//			}
//
//
//			//sort edges and assign positions
//			private void assignEdgePositions(){
//				//assign y position for each node
//				System.out.println("assignEdgePositions():"+name);
//				//assigning y position per dimension, per node
//				for(Position d : dimensions){
//					for(Node n : d.getNodes()){
//						Float f = (Float)layout_y_map.get(""+n.getAminoAcid());
//						if(f == null){
//							System.out.println("Float is null: "+n.getAminoAcid());
//
//						}
//						n.setDy(getFloat(f));
//						n.setRunningY(n.getDy());
//					}
//				}
//				//sort Edges based on y_gap
//				for(Position d : dimensions){
//					ArrayList<Edge> edges = d.getEdges();
//					//sort by the from_node position
//					//sort by the gap
//					Collections.sort(edges);
//				}
//				//assign position for edges
//				for(int i = 0; i< dimensions.length-1; i++){
//					Position d = dimensions[i];
//					float from_x = layout_x[i];
//					float to_x = layout_x[i+1];
//					float from_mid_x = from_x + _half_node_w;
//					float to_mid_x = to_x + _half_node_w;
//
//					//go through sorted edges
//					ArrayList<Edge> edges = d.getEdges();
//					for(int j = 0; j<edges.size(); j++){
//						Edge e = edges.get(j);
//						//filter  ----------------------------------------------- to be implemented
//						float thickness = map(e.getFrequency(), 0f, 1f, 0f, _node_h);
//
//						if(isExponentialScaling){
//							thickness = log_map(e.getFrequency(), 0f, 1f, 0f, _node_h, base);
//						}
//
//						e.setFrom_dx(from_mid_x);
//						e.setFrom_dy(e.getFrom().getFrom_runningY());
//						e.setTo_dx(to_mid_x);
//						e.setTo_dy(e.getTo().getTo_runningY());
//						e.setThickness(thickness);
//						//increment
//						e.getFrom().setFrom_runningY(e.getFrom().getFrom_runningY() + thickness);
//						e.getTo().setTo_runningY(e.getTo().getTo_runningY() + thickness);
//					}
//				}
//				// println("edge positions assigned!"+name);
//			}
//
//
//			//draw edges
//			private void createPGraphics(int c, int alpha){
//				flow_pg = createGraphics(_SANKEY_RECT.x+_SANKEY_WIDTH, _SANKEY_RECT.y+_SANKEY_HEIGHT);
//				flow_pg.beginDraw();
//				// flow_pg.background(255);
//				flow_pg.fill(c);
//				flow_pg.noStroke();
//				for(int i = 0; i< dimensions.length-1; i++){
//					Position d = dimensions[i];
//					//draw edges
//					for(Edge e :d.getEdges()){
//						// println((i+1)+"-"+(i+2)+":"+e.toString());
//						if(e.getSequences().size() > 0 && e.getFrequency() >= _frequencyThreshold){
//							flow_pg.fill(c, alpha);
//							flow_pg.noStroke();
//							if(i == 0){
//								e.setPath(drawSankey2(flow_pg, e.getFrom_dx() - _half_node_w, e.getFrom_dy(), e.getTo_dx(), e.getTo_dy(), 0.5f, 0.5f, e.getThickness(), _node_w, _half_node_w));
//							}else if( i == dimensions.length-2){
//								e.setPath(drawSankey2(flow_pg, e.getFrom_dx(), e.getFrom_dy(), e.getTo_dx() + _half_node_w, e.getTo_dy(), 0.5f, 0.5f, e.getThickness(), _half_node_w, _node_w));
//							}else{
//								e.setPath(drawSankey2(flow_pg, e.getFrom_dx(), e.getFrom_dy(), e.getTo_dx(), e.getTo_dy(), 0.5f, 0.5f, e.getThickness(), _half_node_w, _half_node_w));
//							}
//						}
//					}
//				}
//				flow_pg.endDraw();
//				//embedding
//				display_pg = createGraphics(_SANKEY_RECT.width, _SANKEY_RECT.height);
//				display_pg.beginDraw();
//				display_pg.image(flow_pg, -1*_SANKEY_RECT.x-_SANKEY_POS, -1* _SANKEY_RECT.y);
//				display_pg.endDraw();
//			}
//
//			private void updatePGraphics(){
//				display_pg.clear();
//				display_pg.beginDraw();
//				display_pg.image(flow_pg, -1*_SANKEY_RECT.x-_SANKEY_POS ,-1* _SANKEY_RECT.y);
//				display_pg.endDraw();
//			}
//
//			//drawign pdf
//			private void drawPDF(PGraphics pdf, int c, int alpha){
//				pdf.fill(c);
//				pdf.noStroke();
//				for(int i = 0; i< dimensions.length-1; i++){
//					Position d = dimensions[i];
//					//draw edges
//					for(Edge e :d.getEdges()){
//						// println((i+1)+"-"+(i+2)+":"+e.toString());
//						if(e.getSequences().size() > 0 && e.getFrequency() >= _frequencyThreshold){
//							pdf.fill(c, alpha);
//							pdf.noStroke();
//							if(i == 0){
//								e.setPath(drawSankey2(pdf, e.getFrom_dx() - _half_node_w, e.getFrom_dy(), e.getTo_dx(), e.getTo_dy(), 0.5f, 0.5f, e.getThickness(), _node_w, _half_node_w));	
//							}else if( i == dimensions.length-2){
//								e.setPath(drawSankey2(pdf, e.getFrom_dx(), e.getFrom_dy(), e.getTo_dx() + _half_node_w, e.getTo_dy(), 0.5f, 0.5f, e.getThickness(), _half_node_w, _node_w));
//							}else{
//								e.setPath(drawSankey2(pdf, e.getFrom_dx(), e.getFrom_dy(), e.getTo_dx(), e.getTo_dy(), 0.5f, 0.5f, e.getThickness(), _half_node_w, _half_node_w));
//							}
//						}
//					}
//				}
//			}
//
//
//			//sankey
//			private GeneralPath drawSankey2(PGraphics pg, float x_f, float y_from, float x_t, float y_to, float min_r_left, float min_r_right, float thickness, float from_offset, float to_offset){
//				GeneralPath trace = new GeneralPath();
//				float x_from = x_f + from_offset;
//				float x_to = x_t - to_offset;
//
//				int isDownSlope = 0; //1 = down slope, 2 = up slope
//				if (y_from < y_to){
//					isDownSlope = 1;
//				}else if(y_from > y_to){
//					isDownSlope = -1;
//				}else{
//					//flat line
//					isDownSlope = -1;
//				}
//				float f1 = min_r_left + thickness / 2 + isDownSlope * (thickness / 2);
//				float f2 = min_r_left + thickness / 2 - isDownSlope * (thickness / 2);
//				float f3 = min_r_right + thickness / 2 - isDownSlope * (thickness / 2);
//				float f4 = min_r_right + thickness / 2 + isDownSlope * (thickness / 2);
//
//				float p1 = x_from;
//				float q1 = y_from + thickness / 2 + isDownSlope * thickness / 2 + isDownSlope * min_r_left;
//				float p2 = x_to;
//				float q2 = y_to + thickness / 2 - isDownSlope * thickness / 2 - isDownSlope * min_r_right;
//				// indentation
//				// int i1 = x_from + thickness / 5;
//				// int i2 = y_from + thickness / 2;
//				// int i3 = x_to + thickness / 5;
//				// int i4 = y_to + thickness / 2;
//
//				float f5 = sq(p2 - p1) + sq(q2 - q1);
//
//				float f6 = sqrt(f5 - sq(f3 + f1));
//				float f7 = ((q2 - q1) * f6 + isDownSlope * (p2 - p1) * (f3 + f1)) / f5;
//				float f8 = ((p2 - p1) * f6 - isDownSlope * (q2 - q1) * (f3 + f1)) / f5;
//				float rx1 = (p1 + isDownSlope * f1 * f7);
//				float ry1 = (q1 - isDownSlope * f1 * f8);
//				float rx2 = (p2 - isDownSlope * f3 * f7);
//				float ry2 = (q2 + isDownSlope * f3 * f8);
//
//				float f9 = sqrt(f5 - sq(f2 + f4));
//				float f10 = ((q2 - q1) * f9 + isDownSlope * (p2 - p1) * (f2 + f4)) / f5;
//				float f11 = ((p2 - p1) * f9 - isDownSlope * (q2 - q1) * (f2 + f4)) / f5;
//
//				float rx3 = (p1 + isDownSlope * f2 * f10);
//				float ry3 = (q1 - isDownSlope * f2 * f11);
//				float rx4 = (p2 - isDownSlope * f4 * f10);
//				float ry4 = (q2 + isDownSlope * f4 * f11);
//
//				float angle = (2.0F * (atan2(ry1 - y_from, rx1 - x_from) % TWO_PI));
//				// pg.fill(0);
//				// pg.noStroke();
//				pg.smooth();
//				pg.beginShape();
//
//				float f12;
//				if (isDownSlope == 1) {
//					pg.vertex(x_f, y_from);
//					pg.vertex(x_from, y_from);
//					trace.moveTo(x_f, y_from);
//					trace.lineTo(x_from, y_from);
//					for (f12 = 0.0F; f12 < angle; f12 += 0.02F) {
//						float dx = p1 + f1 * cos(f12 - isDownSlope * PI / 2.0F);
//						float dy = q1 + f1 * sin(f12 - isDownSlope * PI / 2.0F);
//						pg.curveVertex(dx, dy);
//						trace.lineTo(dx, dy);
//					}
//					pg.vertex(rx1, ry1);
//					pg.vertex(rx2, ry2);
//					trace.lineTo(rx1, ry1);
//					trace.lineTo(rx2, ry2);
//					for (f12 = angle; f12 > 0.0F; f12 -= isDownSlope * 0.02F) {
//						float dx = p2 + f3 * cos(f12 + isDownSlope * PI / 2.0F);
//						float dy = q2 + f3 * sin(f12 + isDownSlope * PI / 2.0F);
//						pg.curveVertex(dx, dy);
//						trace.lineTo(dx, dy);
//					}
//					pg.vertex(x_to, y_to);
//					pg.vertex(x_t, y_to);
//					pg.vertex(x_t, y_to+thickness);	
//					pg.vertex(x_to, y_to + thickness);
//
//					trace.lineTo(x_to, y_to);
//					trace.lineTo(x_t, y_to);
//					trace.lineTo(x_t, y_to+thickness);
//					trace.lineTo(x_to, y_to +thickness);
//					// pg.vertex(i3, i4);
//
//					for (f12 = 0.0F; f12 < angle; f12 += isDownSlope * 0.02F) {
//						float dx = p2 + f4 * cos(f12 + isDownSlope * PI / 2.0F);
//						float dy = q2 + f4 * sin(f12 + isDownSlope * PI / 2.0F);
//						pg.curveVertex(dx, dy);
//						trace.lineTo(dx, dy);
//					}
//					pg.vertex(rx4, ry4);
//					pg.vertex(rx3, ry3);
//					for (f12 = angle; f12 > 0.0F; f12 -= isDownSlope * 0.02F) {
//						float dx = p1 + f2 * cos(f12 - isDownSlope * PI / 2.0F);
//						float dy = q1 + f2 * sin(f12 - isDownSlope * PI / 2.0F);
//						pg.curveVertex(dx, dy);
//						trace.lineTo(dx, dy);
//					}
//					// pg.vertex(i1, i2);
//					pg.vertex(x_from, y_from+thickness);
//					pg.vertex(x_f, y_from+thickness);
//					trace.lineTo(x_from, y_from+thickness);
//					trace.lineTo(x_f, y_from+thickness);
//
//					pg.endShape();
//					trace.closePath();
//				}else {
//					pg.vertex(x_f, y_from);
//					pg.vertex(x_from, y_from);
//					trace.moveTo(x_f, y_from);
//					trace.lineTo(x_from, y_from);
//					for (f12 = PI; f12 > PI + angle; f12 -= 0.02F) {
//						float dx = p1 + f1 * cos(f12 - HALF_PI);
//						float dy = q1 + f1 * sin(f12 - HALF_PI);
//						pg.curveVertex(dx , dy);
//						trace.lineTo(dx, dy);
//					}
//					pg.vertex(rx1, ry1);
//					pg.vertex(rx2, ry2);
//					for (f12 = angle; f12 < 0.0F; f12 += 0.02F) {
//						float dx = p2 + f3 * cos(f12 - HALF_PI);
//						float dy = q2 + f3 * sin(f12 - HALF_PI);
//						pg.curveVertex(dx, dy);
//						trace.lineTo(dx, dy);
//					}
//					pg.vertex(x_to, y_to);
//					pg.vertex(x_t, y_to);
//					// pg.vertex(i3, i4);
//					pg.vertex(x_t, y_to + thickness);
//					pg.vertex(x_to, y_to + thickness);
//
//					trace.lineTo(x_to, y_to);
//					trace.lineTo(x_t, y_to);
//					trace.lineTo(x_t, y_to + thickness);
//					trace.lineTo(x_to, y_to + thickness);
//					for (f12 = 0.0F; f12 > angle; f12 -= 0.02F) {
//						float dx = p2 + f4 * cos(f12 - HALF_PI);
//						float dy = q2 + f4 * sin(f12 - HALF_PI);
//						pg.curveVertex(dx, dy);
//						trace.lineTo(dx, dy);
//					}
//					pg.vertex(rx4, ry4);
//					pg.vertex(rx3, ry3);
//					trace.lineTo(rx4, ry4);
//					trace.lineTo(rx3, ry3);
//					for (f12 = PI + angle; f12 < PI; f12 += 0.02F) {
//						float dx = p1 + f2 * cos(f12 - HALF_PI);
//						float dy = q1 + f2 * sin(f12 - HALF_PI);
//						pg.curveVertex(dx,dy);
//						trace.lineTo(dx, dy);
//					}
//					// pg.vertex(i1, i2);
//					pg.vertex(x_from, y_from+thickness);
//					pg.vertex(x_f, y_from+thickness);
//
//					trace.lineTo(x_from, y_from+thickness);
//					trace.lineTo(x_f, y_from+thickness);
//					pg.endShape();
//					trace.closePath();
//				}
//				return trace;
//			}
//
//
//			private void drawSelectedSequence(ArrayList<Sequence> seq, int c){
//				noStroke();
//				fill(c);
//				for(int i = 0; i< dimensions.length-1; i++){
//					Position d = dimensions[i];
//					//draw edges
//					for(Edge e :d.getEdges()){
//						if(e.getTo_dx() < _SANKEY_RECT.x+_SANKEY_RECT.width){
//							//check overlap with seq
//							ArrayList<Sequence> intersection = intersection(seq, e.getSequences());
//							if(intersection.size()>0){
//								float thickness = map(intersection.size(), 0, (float)total_count, 0f, _node_h);
//								// if(isExponentialScaling){
//								// 	thickness = log_map(intersection.size(), 0, (float)total_count, 0f, _node_h, base);
//								// }
//
//								if(i == 0){
//									drawSankey2(g, e.getFrom_dx() - _half_node_w, e.getFrom_dy(), e.getTo_dx(), e.getTo_dy(), 0.5f, 0.5f, thickness, _node_w, _half_node_w);	
//								}else if( i == dimensions.length-2){
//									drawSankey2(g, e.getFrom_dx(), e.getFrom_dy(), e.getTo_dx() + _half_node_w, e.getTo_dy(), 0.5f, 0.5f, thickness, _half_node_w, _node_w);	
//								}else{
//									drawSankey2(g, e.getFrom_dx(), e.getFrom_dy(), e.getTo_dx(), e.getTo_dy(), 0.5f, 0.5f, thickness, _half_node_w, _half_node_w);	
//								}		
//							}
//						}
//					}
//				}
//			}
//
//			private ArrayList<Sequence> intersection(ArrayList<Sequence> list1, ArrayList<Sequence> list2) {
//				ArrayList<Sequence> list = new ArrayList<Sequence>();
//				for (Sequence t : list1) {
//					if(list2.contains(t)) {
//						list.add(t);
//					}
//				}
//				return list;
//			}
//
//
//
//
//
//			//find the sequence based on most frequent residues
//			private String getCommonSequence(){
//				String result = "";
//				for(Position d :dimensions){
//					result += d.getMostFrequentResidue();
//				}
//				return result;
//			}
//
//			private Rectangle getToggle() {
//				return toggle;
//			}
//			
//			private void setToggle(Rectangle toggle) {
//				this.toggle = toggle;
//			}
//			
//			private boolean isShowing() {
//				return isShowing;
//			}
//			
//			private void toggleShowing(){
//				isShowing = !isShowing;
//			}
//			
//			private String getName() {
//				return name;
//			}
//			
//			private boolean[] getPositionSelected() {
//				return positionSelected;
//			}
//			
//			private void setPositionSelected(boolean[] positionSelected) {
//				this.positionSelected = positionSelected;
//			}
//			
//			private void togglePositionSelected(int index){
//				this.positionSelected[index] = !this.positionSelected[index];
//			}
//
//			private int getSeq_length() {
//				return seq_length;
//			}
//			
//			private Position[] getDimensions() {
//				return dimensions;
//			}
//		}

	static public void main(String[] passedArgs) {
		String[] appletArgs = new String[] { "view.SeDD" };
		if (passedArgs != null) {
			PApplet.main(concat(appletArgs, passedArgs));
		} else {
			PApplet.main(appletArgs);
		}
	}
}
