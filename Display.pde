
int color_alpha = 120;
int color_red = color(202, 0, 32);
int color_blue = color(5, 113, 176);

//layout elements
ArrayList<String> all_aa;
ArrayList<AA_Group> all_aa_group;
HashMap<String, Float> layout_y_map; //update
float[] layout_x;


//layout grid
PGraphics grids = null;

PGraphics mip_pg = null;
int _heatmap_cell_width = 8;
int _heatmap_cell_height = 8;


Rectangle[] aa_up_btns, aa_down_btns;
String[] aa_order;
Rectangle[] aa_group_up_btns, aa_group_down_btns;
Rectangle ind_btn_rect, group_btn_rect;
Rectangle pdf_btn, png_btn;

Rectangle[] pos_btns;
Rectangle pos_btn_area;
Rectangle[] mip_rects; //MIp rectangles per sample

float _circos_min_radian = 0;
float _circos_max_radian = TWO_PI;
float _circos_radius = 0;
float rotation = -HALF_PI;

//information content
float _min_info_content = Float.MAX_VALUE;//= 0;
float _max_info_content = Float.MIN_VALUE;//= 5;
float _threshold_info_content = 4;

//circos
float out_radius, in_radius, arc_radius; //arc is between in and out
String[] btn_labels = {"get common sequence", "load pdb", "update protein view"};


//legend
Rectangle[] _legend_label_rect;
int[] _legend_percentage = {100, 50, 10, 1};
String[] _legend_nb_label ={"Threshold", "Node Width", "Gap Width"};
NumberBox[] _legend_numberbox;


void setStageSize(){
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
		Sample s = samples[i];	
		s.toggle = new Rectangle(runningX, runningY, _legend_item_width, _node_h);
		runningY += _node_h+_MARGIN/2;
	}	
	//numberbox
	_legend_numberbox = new NumberBox[_legend_nb_label.length];
	runningY += _MARGIN;
	//frequency threshold
	_legend_numberbox[0] = new NumberBox(runningX, runningY, _legend_item_width, _node_h,_frequencyThreshold, 1f, 0f, _legend_nb_label[0] );
	_legend_numberbox[0].text_color = 0;
	runningY += _node_h+_MARGIN/2;
	//node width
	_legend_numberbox[1] = new NumberBox(runningX, runningY, _legend_item_width, _node_h,_node_w, 40f, 1f, _legend_nb_label[1]);
	_legend_numberbox[1].text_color = 0;
	runningY += _node_h+_MARGIN/2;
	//gap width
	_legend_numberbox[2] = new NumberBox(runningX, runningY, _legend_item_width, _node_h,_horizontal_gap, 50f, 1f, _legend_nb_label[2]);
	_legend_numberbox[2].text_color = 0;
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

void setup_aa_btns(){
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
		ArrayList<String> aa_array = group.aa;
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
		ArrayList<String> aa_array = group.aa;
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
void initLayout(){
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
			group.aa.add(""+aas[j]);
		}
		all_aa_group.add(group);
	}

	// println("debug: end of initLayout()");
}
//determine display position based on all_aa and all_aa_group
void updateLayout(){
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
		ArrayList<String> aas = group.aa;
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
void updateEdges(){
	//persample
	for(int i = 0; i < samples.length; i++){
		Sample sample = samples[i];
		status_msg = "updating the plot for "+sample.name;
		//assgin edge position
		sample.assignEdgePositions();
		// sample.createColorPG(color_cyan, 160);
		sample.createPGraphics(sample_colors[i], color_alpha);

		println("debug: end of updateEdges():"+sample.name);
	}
	println("debug: end of updateEdges()");
}



//sankey slider
void updateSlider(){
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
void bezierConnection(PGraphics pg, float x1, float y1, float x2, float y2, float o_r, float d_r, float cx, float cy, float radius) {
    //adjust fraction based on the distance
    float dist_radian = abs(o_r - d_r);
    if(dist_radian > PI){
    	dist_radian -= PI;
    }
    float fraction = map(dist_radian, 0, PI, 0.9, 0.1);
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
void bezierConnection_PDF(float x1, float y1, float x2, float y2, float o_r, float d_r, float cx, float cy, float radius) {
    //adjust fraction based on the distance
    float dist_radian = abs(o_r - d_r);
    if(dist_radian > PI){
    	dist_radian -= PI;
    }
    float fraction = map(dist_radian, 0, PI, 0.9, 0.1);
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

void draw(){
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
					samples[i].drawSelectedSequence(seq, sample_colors[i]);
				}
			}
		}else{
			// draw image twice
			for(int i = 0; i<samples.length; i++){
				if(samples[i].isShowing){
					image(samples[i].display_pg, _SANKEY_RECT.x, _SANKEY_RECT.y);
				}
			}
			for(int i = 0; i<samples.length; i++){
				if(samples[i].isShowing){
					image(samples[i].display_pg, _SANKEY_RECT.x, _SANKEY_RECT.y);
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

//pdf drawing
void drawPDF(PGraphics pdf, int e_w, int e_h){
	int e_sankey_width = e_w - _LEFT_MARGIN - _RIGHT_MARGIN -_LEGEND_RECT.width;
	int e_sankey_height = _SANKEY_HEIGHT;
	int e_sankey_x = _LEFT_MARGIN;
	int e_sankey_y = _TOP_MARGIN;
	int e_legend_x = e_w - _LEGEND_RECT.width - _RIGHT_MARGIN + _MARGIN;
	pdf.background(255);  
	//background
	pdf.fill(235);
	pdf.noStroke();
	//per category background
	// println("debug: group count ="+all_aa_group.size());
	for(int i = 0; i < all_aa_group.size(); i++){
		AA_Group group = all_aa_group.get(i);
		String first_aa = group.aa.get(0);
		String last_aa = group.aa.get(group.aa.size()-1);
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
		String first_aa = group.aa.get(0);
		float aa_y = getFloat((Float) layout_y_map.get(first_aa)); 
		pdf.text(group.name, _SANKEY_RECT.x - _text_margin, aa_y - _text_margin);
	}

	// draw image twice
	for(int i = 0; i<samples.length; i++){
		if(samples[i].isShowing){
			// samples[i].drawPDF(pdf, sample_colors[i], 200);
			pdf.image(samples[i].flow_pg, 0, 0);
		}
	}
	for(int i = 0; i<samples.length; i++){
		if(samples[i].isShowing){
			// samples[i].drawPDF(pdf, sample_colors[i], 200);
			pdf.image(samples[i].flow_pg, 0, 0);
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
		Sample s = samples[i];
		Rectangle rect = s.toggle;
		pdf.rectMode(CORNER);
		if(s.isShowing){
			pdf.fill(sample_colors[i]);
			pdf.noStroke();
		}else{
			pdf.noFill();
			pdf.stroke(sample_colors[i]);
			pdf.strokeWeight(1);
		}
		pdf.rect(e_legend_x, rect.y, rect.width, rect.height);
		pdf.fill(0);
		pdf.text(s.name, e_legend_x+rect.width+_MARGIN/2, rect.y);
	}

}
//background and selection
void drawBackgroundGrid(){
	// println("\tdrawBackgroundGrid()");
	//background grid
	fill(235);
	noStroke();
	//per category background
	// println("debug: group count ="+all_aa_group.size());
	for(int i = 0; i < all_aa_group.size(); i++){
		AA_Group group = all_aa_group.get(i);
		String first_aa = group.aa.get(0);
		String last_aa = group.aa.get(group.aa.size()-1);

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
				Sample s = samples[j];
				if(s.positionSelected[i]){
					int selection_bar_y = round(_SANKEY_RECT.y - _MARGIN*1.5 - (samples.length-j)*(selection_bar_weight+selection_bar_gap));
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
		String first_aa = group.aa.get(0);
		float aa_y = getFloat((Float) layout_y_map.get(first_aa)); 
		text(group.name, _SANKEY_RECT.x - _text_margin, aa_y - _text_margin);
	}

	// println("--- draw() end");
}

//buttons for line graph as well as circos
void drawBtns(){
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


float getFloat(Float f){
	return f.floatValue();
}


//legend and toggle button
void drawLegend(){
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
		Sample s = samples[i];
		Rectangle rect = s.toggle;
		rectMode(CORNER);
		if(s.isShowing){
			fill(sample_colors[i]);
			noStroke();
		}else{
			noFill();
			stroke(sample_colors[i]);
			strokeWeight(1);
		}
		rect(rect.x, rect.y, rect.width, rect.height);
		fill(0);
		text(s.name, rect.x+rect.width+_MARGIN/2, rect.y);
	}

	//number box
	for(NumberBox nb: _legend_numberbox){
		nb.display();
	}

}

void drawMouseOver(){
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

void drawArc(float dx, float dy, float in_r, float out_r, float s, float e){
	float kappa = 0.55228 * (e - s)/HALF_PI;
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

void drawTriangle(Rectangle rect, boolean isUP){
	if(isUP){
		triangle(rect.x, rect.y+rect.height, (float)rect.getCenterX(), rect.y, rect.x+rect.width, rect.y+rect.height);
	}else{
		triangle(rect.x, rect.y, rect.x+rect.width, rect.y, (float)rect.getCenterX(), rect.y+rect.height);
	}
}

void drawSlider(){
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



