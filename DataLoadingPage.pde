int title_x, title_y;
PFont title_font;
PFont btn_font;

int _btn_width  = _MARGIN*10;
int _btn_height = _MARGIN*3;
int _file_name_width = _MARGIN *40;
int _content_x, _content_y;


Rectangle conf_file_rect;
Rectangle conf_btn, start_btn;
Rectangle example_btn;

boolean isValidConf = false;

//File
File conf_file = null;
String conf_content = "";
String conf_error = "";
int conf_content_line_count = 0;
int conf_error_line_count = 0;

String status_msg = ""; //loading messge




void setupDataLoadingPage(){
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


void drawDataLoadingPage(){
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
void drawDataLoader(){
	background(240);
	fill(120);
	textAlign(CENTER, CENTER);
	text("Loading the data... ", _STAGE_WIDTH/2, _STAGE_HEIGHT/2);
	text(status_msg, _STAGE_WIDTH/2, _STAGE_HEIGHT/2+_MARGIN*2);
}




//loading configuration
boolean check_conf_file(File f){
	boolean result = true;
	//reset
	conf_content = "";
	conf_error = "";

	int num_category = 0;
	int num_category_meta = 0;

	//parse the selected file
	String[] conf = loadStrings(f);
	for(String line : conf){
	    // println("debug: configuration:"+line);
	    String[] s = split(line.trim(), TAB);
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
	            String[] items = split(s[i].trim(), ",");
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
	        println("Error: unknown conf information:"+line);
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


class LoadingThread implements Runnable{
	void run(){
		_FILE_SELECTED = true;
		_FILE_LOADING = true;
		
		startLoadingData();
	}
}



