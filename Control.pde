float min_frequency  = 0.01; //1%
ArrayList<ArrayList<Sequence>> selected_sequences = new ArrayList<ArrayList<Sequence>>();

boolean sequences_selected = false;

boolean hovering_ind_btn = false;
boolean hovering_group_btn = false;
int hover_btn_index = -1;
boolean hovering_up_btn = false; //up or down
boolean showing_heatmap = false;


int hover_pos_index = -1;
int hover_sample_index = -1;
int pdb_sample_index = -1;

void mouseMoved(){
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
			for(Sample s:samples){
				selected_sequences.add(new ArrayList<Sequence>());
			}

			int index = getDimensionIndex(mouseX);
			if(index != -1){
				for(int i = 0; i < samples.length; i++){
					Sample s = samples[i];
					if(s.isShowing){
						Position d = s.dimensions[index]; 
						//search for the edge
						Edge selected_edge = null;
						outerloop:
						for(Edge e : d.edges){
							if(e.path.contains(mouseX, mouseY) && e.frequency >=_frequencyThreshold ){
								selected_edge = e;
								break outerloop;
							}
						}
						if(selected_edge != null){
							selected_sequences.get(i).addAll(selected_edge.sequences);
							println("debug: "+s.name+":"+selected_edge.sequences.size()+" sequences selected");						
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
			for(Sample s:samples){
				if(s.toggle.contains(mouseX, mouseY)){
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
void mousePressed(){
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
		pdb_sample_index = -1;
		slider_drag = false;
		if(hovering_ind_btn){
			String aa_to_move  = aa_order[hover_btn_index];
			if(hovering_up_btn){
				// println("move "+aa_to_move +" up!");
				AA_Group group = find_AA_Group(aa_to_move);
				if(group != null){
					int index = group.aa.indexOf(aa_to_move);
					int new_index  = max(0, index -1);
					Collections.swap(group.aa, index, new_index);
				}

			}else{
				// println("move "+aa_to_move +" down!");
				AA_Group group = find_AA_Group(aa_to_move);
				if(group != null){
					int index = group.aa.indexOf(aa_to_move);
					int new_index  = min(group.aa.size()-1, index+1);
					// int new_index  = min(group.aa.size()-1, hover_btn_index +1);
					Collections.swap(group.aa, index, new_index);
				}
			}
			setup_aa_btns();
			//determine display position
			updateLayout();
			//determine edges
			updateEdges();
			loop();
		}else if(hovering_group_btn){
			AA_Group aa = all_aa_group.get(hover_btn_index);

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
			Sample s = samples[hover_sample_index];
			s.positionSelected[hover_pos_index] = !s.positionSelected[hover_pos_index];
			loop();
			return;
		}else if(_LEGEND_RECT.contains(mouseX, mouseY)){
			//legend area
			//toggle button
			for(Sample s:samples){
				if(s.toggle.contains(mouseX, mouseY)){
					s.isShowing = !s.isShowing;
					draw_counter = 0;
					loop();
					return;
				}
			}
			//numberbox
			for(int i = 0; i < _legend_numberbox.length; i++){
				NumberBox nb = _legend_numberbox[i];
				if(nb.up_rect.contains(mouseX, mouseY)){
					//up
					if(i == 0){
						//treshold
						nb.v_current = min(nb.v_current+0.01f, nb.v_max);
						_frequencyThreshold = nb.v_current;
						updateEdges();
						draw_counter = 0;
						loop();
						return;
					}else if(i == 1){
						//node width
						nb.v_current = min(nb.v_current+1.0f, nb.v_max);
						_node_w = round(nb.v_current);
						_half_node_w = _node_w/2;
						updateLayout();
						updateEdges();
						draw_counter = 0;
						loop();
						return;
					}else if(i == 2){
						//gap width
						nb.v_current = min(nb.v_current+1.0f, nb.v_max);
						_horizontal_gap = round(nb.v_current);
						updateLayout();
						updateEdges();
						draw_counter = 0;
						loop();
						return;
					}


					// draw_counter = 0;
					// loop();
					// return;
				}else if(nb.down_rect.contains(mouseX, mouseY)){
					//down
					if(i == 0){
						//treshold
						nb.v_current = max(nb.v_current-0.01f, nb.v_min);
						_frequencyThreshold = nb.v_current;
						updateEdges();
						draw_counter = 0;
						loop();
						return;
					}else if(i == 1){
						//node width
						nb.v_current = max(nb.v_current-1.0f, nb.v_min);
						_node_w =round(nb.v_current);
						_half_node_w = _node_w/2;
						updateLayout();
						updateEdges();
						draw_counter = 0;
						loop();
						return;
					}else if(i == 2){
						//gap width
						nb.v_current = max(nb.v_current-1.0f, nb.v_min);
						_horizontal_gap =round(nb.v_current);
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

void mouseDragged(){
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
				for(Sample s:samples){
					s.updatePGraphics();
				}
			draw_counter = 0;
			loop();
		}
	}
}


void keyPressed(){
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


int getDimensionIndex(int mx){
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

AA_Group find_AA_Group(String aa){
	for(AA_Group group:all_aa_group){
		if(group.aa.contains(aa)){
			return group;
		}
	}
	return null;
}



void configurationFileSelection(File selection){
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

void pngFolderSelection(File selection){
	if(selection != null){
		//save the image
		// String path = selection.getAbsolutePath();
		// path+= "/Sedd_"+ timestamp()+".png";
		// saveFrame(path);
		// println("saving png done");
		String path = selection.getAbsolutePath();
		path+= "/Sedd_"+ timestamp()+".png";

		//determine export image size
		int e_img_width = _LEFT_MARGIN + _RIGHT_MARGIN + (_node_w*(_total_length+2)) + (_horizontal_gap*(_total_length-1)) + _LEGEND_RECT.width;
		int e_img_height = _SANKEY_HEIGHT + _TOP_MARGIN +_TOP_MARGIN;
		println("export image dimensions = "+ e_img_width +"  "+e_img_height);
		PGraphics pdf = createGraphics(e_img_width, e_img_height);
		pdf.beginDraw();
		//draw stuff
		drawPDF(pdf, e_img_width, e_img_height);

		pdf.dispose();
		pdf.endDraw();
		pdf.save(path);
	}
}

void pdfFolderSelection(File selection){
	if(selection != null){
		String path = selection.getAbsolutePath();
		path+= "/Sedd_"+ timestamp()+".pdf";

		//determine export image size
		int e_img_width = _LEFT_MARGIN + _RIGHT_MARGIN + (_node_w*(_total_length+2)) + (_horizontal_gap*(_total_length-1)) + _LEGEND_RECT.width;
		int e_img_height = _SANKEY_HEIGHT + _TOP_MARGIN +_TOP_MARGIN;
		println("export image dimensions = "+ e_img_width +"  "+e_img_height);
		PGraphics pdf = createGraphics(e_img_width, e_img_height, PDF, path);
		pdf.beginDraw();
		//draw stuff
		drawPDF(pdf, e_img_width, e_img_height);

		pdf.dispose();
		pdf.endDraw();
	}
}

