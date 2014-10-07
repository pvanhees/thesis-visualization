class Sample{
	String name;
	ArrayList<Sequence> sequences;
	int seq_length;
	int total_count;
	Position[] dimensions;//0 to n-1


	float[][] zscore;

	PGraphics color_pg;
	PGraphics flow_pg;
	PGraphics display_pg; //test

	int[] distribution;

	//flags
	boolean isShowing = true;
	boolean hasMIp = false;

	boolean[] positionSelected;
	HashSet<String> unique_sequence;


	//buttons
	Rectangle[] btn_rects;
	NumberBox mip_numberbox;
	Rectangle toggle = null; //legend


	//constructor and loading data
	Sample(String url, String name){
		// println("Sample.contstructor: url ="+url+"  name="+name);
		this.name = name;
		sequences = new ArrayList<Sequence>();
		unique_sequence = new HashSet<String>();
		int non_unique_counter = 0;
		//load data
		String[] lines = loadStrings(url);
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
		
		println("---- "+name+ "-----");
		println("number of non_unique sequence removed = "+non_unique_counter);
		println("number of unique sequence = "+unique_sequence.size());
		println("sequence length = "+seq_length);
		//dimensions
		dimensions = new Position[seq_length];
		for(int i = 0; i<seq_length; i++){
			Position d = new Position(i);
			//nodes
			for(int j = 0; j < sequences.size(); j++){
				Sequence s = sequences.get(j);
				String aa = ""+s.sequence[i];
				Node node  = (Node) d.node_map.get(aa);
				if(node == null){
					node = new Node(d, aa.charAt(0));
					d.node_map.put(aa, node);
					d.nodes.add(node);
				}	
				node.addSequence(s);
			}
			dimensions[i] = d;
		}


		//edges
	 	for(int j = 0; j < sequences.size(); j++){
	 		Sequence s = sequences.get(j);
	 		for(int k = 0; k <s.sequence.length-1; k++){
	 			String aa_from = ""+ s.sequence[k];
	 			String aa_to = ""+s.sequence[k+1];
	 			Position d_from = dimensions[k];
	 			Position d_to = dimensions[k+1];
	 			Node node_from = (Node)d_from.node_map.get(aa_from);
	 			Node node_to = (Node)d_to.node_map.get(aa_to);

	 			String aa_edge = aa_from+"_"+aa_to;
	 			//find edge
	 			Edge edge = (Edge) d_from.edge_map.get(aa_edge);
	 			if(edge == null){
	 				edge = new Edge(node_from, node_to, aa_edge);
	 				d_from.edge_map.put(aa_edge, edge);
	 				d_from.edges.add(edge);
	 			}
	 			edge.addSequence(s);
	 		}
	 	}

	 	
 		total_count = sequences.size();
 		// println("debgu: "+name+" has "+sequences.size()+" sequences!");
	 	println("debug:"+name+"'s data is loaded ---- ");
	}

	//calculate frequency for node and edge
	void calculateFrequency(){
		//per node
		for(Position d : dimensions){
			for(Node n : d.nodes){
				n.frequency = (float)n.sequences.size()/(float)total_count;
			}
			for(Edge e: d.edges){
				e.frequency = (float)e.sequences.size()/(float)total_count;
			}
		}
	}


	//sort edges and assign positions
	void assignEdgePositions(){
		//assign y position for each node
		println("assignEdgePositions():"+name);
		//assigning y position per dimension, per node
		for(Position d : dimensions){
			for(Node n : d.nodes){
				Float f = (Float)layout_y_map.get(""+n.aminoAcid);
				if(f == null){
					println("Float is null: "+n.aminoAcid);
					
				}
				n.dy = getFloat(f);
				n.setRunningY(n.dy);
			}
		}
		//sort Edges based on y_gap
		for(Position d : dimensions){
			ArrayList<Edge> edges = d.edges;
			//sort by the from_node position
			//sort by the gap
			Collections.sort(edges);
		}
		//assign position for edges
		for(int i = 0; i< dimensions.length-1; i++){
			Position d = dimensions[i];
			float from_x = layout_x[i];
			float to_x = layout_x[i+1];
			float from_mid_x = from_x + _half_node_w;
			float to_mid_x = to_x + _half_node_w;

			//go through sorted edges
			ArrayList<Edge> edges = d.edges;
			for(int j = 0; j<edges.size(); j++){
				Edge e = edges.get(j);
				//filter  ----------------------------------------------- to be implemented
				float thickness = map(e.frequency, 0f, 1f, 0f, _node_h);

				if(isExponentialScaling){
					thickness = log_map(e.frequency, 0f, 1f, 0f, _node_h, base);
				}

				e.from_dx = from_mid_x;
				e.from_dy = e.from.from_runningY;
				e.to_dx = to_mid_x;
				e.to_dy = e.to.to_runningY;
				e.thickness = thickness;
				//increment
				e.from.from_runningY += thickness;
				e.to.to_runningY += thickness;
			}
		}
		// println("edge positions assigned!"+name);
	}

	
	//draw edges
	void createPGraphics(int c, int alpha){
		flow_pg = createGraphics(_SANKEY_RECT.x+_SANKEY_WIDTH, _SANKEY_RECT.y+_SANKEY_HEIGHT);
		flow_pg.beginDraw();
		// flow_pg.background(255);
		flow_pg.fill(c);
		flow_pg.noStroke();
		for(int i = 0; i< dimensions.length-1; i++){
			Position d = dimensions[i];
			//draw edges
			for(Edge e :d.edges){
				// println((i+1)+"-"+(i+2)+":"+e.toString());
				if(e.sequences.size() > 0 && e.frequency >= _frequencyThreshold){
					flow_pg.fill(c, alpha);
					flow_pg.noStroke();
					if(i == 0){
						e.path = drawSankey2(flow_pg, e.from_dx - _half_node_w, e.from_dy, e.to_dx, e.to_dy, 0.5f, 0.5f, e.thickness, _node_w, _half_node_w);	
					}else if( i == dimensions.length-2){
						e.path = drawSankey2(flow_pg, e.from_dx, e.from_dy, e.to_dx + _half_node_w, e.to_dy, 0.5f, 0.5f, e.thickness, _half_node_w, _node_w);	
					}else{
						e.path = drawSankey2(flow_pg, e.from_dx, e.from_dy, e.to_dx, e.to_dy, 0.5f, 0.5f, e.thickness, _half_node_w, _half_node_w);	
					}
				}
			}
		}
		flow_pg.endDraw();
		//embedding
		display_pg = createGraphics(_SANKEY_RECT.width, _SANKEY_RECT.height);
		display_pg.beginDraw();
		display_pg.image(flow_pg, -1*_SANKEY_RECT.x-_SANKEY_POS, -1* _SANKEY_RECT.y);
		display_pg.endDraw();
	}

	void updatePGraphics(){
		display_pg.clear();
		display_pg.beginDraw();
		display_pg.image(flow_pg, -1*_SANKEY_RECT.x-_SANKEY_POS ,-1* _SANKEY_RECT.y);
		display_pg.endDraw();
	}

	//drawign pdf
	void drawPDF(PGraphics pdf, int c, int alpha){
		pdf.fill(c);
		pdf.noStroke();
		for(int i = 0; i< dimensions.length-1; i++){
			Position d = dimensions[i];
			//draw edges
			for(Edge e :d.edges){
				// println((i+1)+"-"+(i+2)+":"+e.toString());
				if(e.sequences.size() > 0 && e.frequency >= _frequencyThreshold){
					pdf.fill(c, alpha);
					pdf.noStroke();
					if(i == 0){
						e.path = drawSankey2(pdf, e.from_dx - _half_node_w, e.from_dy, e.to_dx, e.to_dy, 0.5f, 0.5f, e.thickness, _node_w, _half_node_w);	
					}else if( i == dimensions.length-2){
						e.path = drawSankey2(pdf, e.from_dx, e.from_dy, e.to_dx + _half_node_w, e.to_dy, 0.5f, 0.5f, e.thickness, _half_node_w, _node_w);	
					}else{
						e.path = drawSankey2(pdf, e.from_dx, e.from_dy, e.to_dx, e.to_dy, 0.5f, 0.5f, e.thickness, _half_node_w, _half_node_w);	
					}
				}
			}
		}
	}


	//sankey
	GeneralPath drawSankey2(PGraphics pg, float x_f, float y_from, float x_t, float y_to, float min_r_left, float min_r_right, float thickness, float from_offset, float to_offset){
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


	void drawSelectedSequence(ArrayList<Sequence> seq, int c){
		noStroke();
		fill(c);
		for(int i = 0; i< dimensions.length-1; i++){
			Position d = dimensions[i];
			//draw edges
			for(Edge e :d.edges){
				if(e.to_dx < _SANKEY_RECT.x+_SANKEY_RECT.width){
					//check overlap with seq
					ArrayList<Sequence> intersection = intersection(seq, e.sequences);
					if(intersection.size()>0){
						float thickness = map(intersection.size(), 0, (float)total_count, 0f, _node_h);
						// if(isExponentialScaling){
						// 	thickness = log_map(intersection.size(), 0, (float)total_count, 0f, _node_h, base);
						// }

						if(i == 0){
							drawSankey2(g, e.from_dx - _half_node_w, e.from_dy, e.to_dx, e.to_dy, 0.5f, 0.5f, thickness, _node_w, _half_node_w);	
						}else if( i == dimensions.length-2){
							drawSankey2(g, e.from_dx, e.from_dy, e.to_dx + _half_node_w, e.to_dy, 0.5f, 0.5f, thickness, _half_node_w, _node_w);	
						}else{
							drawSankey2(g, e.from_dx, e.from_dy, e.to_dx, e.to_dy, 0.5f, 0.5f, thickness, _half_node_w, _half_node_w);	
						}		
					}
				}
			}
		}
	}

	ArrayList<Sequence> intersection(ArrayList<Sequence> list1, ArrayList<Sequence> list2) {
		ArrayList<Sequence> list = new ArrayList<Sequence>();
		for (Sequence t : list1) {
		   if(list2.contains(t)) {
		       list.add(t);
		   }
		}
		return list;
	}


	


	//find the sequence based on most frequent residues
	String getCommonSequence(){
		String result = "";
		for(Position d :dimensions){
			result += d.getMostFrequentResidue();
		}
		return result;
	}



}
