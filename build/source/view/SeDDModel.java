package view;

import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import model.Edge;
import model.Node;
import model.Position;
import model.Sequence;
import model.SequencingSample;
import processing.core.PApplet;
import processing.core.PGraphics;

public class SeDDModel {
		
	private boolean isShowing = true;
	private boolean[] positionSelected;
	private Position[] dimensions;//0 to n-1

	
	private PGraphics display_pg; //test
	private PGraphics flow_pg;

	private Rectangle toggle = null; //legend
	
	private SequencingSample ss;
	private PApplet pApplet;

	
	public SeDDModel(SequencingSample ss, PApplet pApplet) {
		this.ss = ss;
		this.pApplet = pApplet;

		dimensions = calculateDimensions();

		//edges
		for(int j = 0; j < ss.getSequences().size(); j++){
			Sequence s = ss.getSequences().get(j);
			for(int k = 0; k <s.getSequence().length-1; k++){
				String aa_from = ""+ s.getSequence()[k];
				String aa_to = ""+s.getSequence()[k+1];
				Position d_from = dimensions[k];
				Position d_to = dimensions[k+1];
				Node node_from = (Node)d_from.getNode_map().get(aa_from);
				Node node_to = (Node)d_to.getNode_map().get(aa_to);

				String aa_edge = aa_from+"_"+aa_to;
				//find edge
				Edge edge = (Edge) d_from.getEdge_map().get(aa_edge);
				if(edge == null){
					edge = new Edge(node_from, node_to, aa_edge);
					d_from.getEdge_map().put(aa_edge, edge);
					d_from.getEdges().add(edge);
				}
				edge.addSequence(s);
			}
		}
	}
	
	private Position[] calculateDimensions(){
		Position[] dimensions = new Position[ss.getSeq_length()];
		for(int i = 0; i<ss.getSeq_length(); i++){
			Position d = new Position(i);
			//nodes
			for(int j = 0; j < ss.getSequences().size(); j++){
				Sequence s = ss.getSequences().get(j);
				String aa = ""+s.getSequence()[i];
				Node node  = d.getNode_map().get(aa);
				if(node == null){
					node = new Node(d, aa.charAt(0));
					d.getNode_map().put(aa, node);
					d.getNodes().add(node);
				}	
				node.addSequence(s);
			}
			dimensions[i] = d;
		}
		return dimensions;
	}
	
	//calculate frequency for node and edge
	public void calculateFrequency(){
		//per node
		for(Position d : dimensions){
			for(Node n : d.getNodes()){
				n.setFrequency((float)n.getSequences().size()/(float)ss.getTotal_count());
			}
			for(Edge e: d.getEdges()){
				e.setFrequency((float)e.getSequences().size()/(float)ss.getTotal_count());
			}
		}
	}
	
	//sort edges and assign positions
	public void assignEdgePositions(HashMap<String, Float> layout_y_map, float[] layout_x, float half_node_w, int node_h, boolean isExponentialScaling, float base){
		//assign y position for each node
		System.out.println("assignEdgePositions():"+ss.getName());
		//assigning y position per dimension, per node
		for(Position d : dimensions){
			for(Node n : d.getNodes()){
				Float f = (Float)layout_y_map.get(""+n.getAminoAcid());
				if(f == null){
					System.out.println("Float is null: "+n.getAminoAcid());

				}
				n.setDy(f.floatValue());
				n.setRunningY(n.getDy());
			}
		}
		//sort Edges based on y_gap
		for(Position d : dimensions){
			ArrayList<Edge> edges = d.getEdges();
			//sort by the from_node position
			//sort by the gap
			Collections.sort(edges);
		}
		//assign position for edges
		for(int i = 0; i< dimensions.length-1; i++){
			Position d = dimensions[i];
			float from_x = layout_x[i];
			float to_x = layout_x[i+1];
			float from_mid_x = from_x + half_node_w;
			float to_mid_x = to_x + half_node_w;

			//go through sorted edges
			ArrayList<Edge> edges = d.getEdges();
			for(int j = 0; j<edges.size(); j++){
				Edge e = edges.get(j);
				//filter  ----------------------------------------------- to be implemented
				float thickness = PApplet.map(e.getFrequency(), 0f, 1f, 0f, node_h);

				if(isExponentialScaling){
					thickness = log_map(e.getFrequency(), 0f, 1f, 0f, node_h, base);
				}

				e.setFrom_dx(from_mid_x);
				e.setFrom_dy(e.getFrom().getFrom_runningY());
				e.setTo_dx(to_mid_x);
				e.setTo_dy(e.getTo().getTo_runningY());
				e.setThickness(thickness);
				//increment
				e.getFrom().setFrom_runningY(e.getFrom().getFrom_runningY() + thickness);
				e.getTo().setTo_runningY(e.getTo().getTo_runningY() + thickness);
			}
		}
		// println("edge positions assigned!"+name);
	}
	
	//find the sequence based on most frequent residues
	private String getCommonSequence(){
		String result = "";
		for(Position d :dimensions){
			result += d.getMostFrequentResidue();
		}
		return result;
	}
	
	public void drawSelectedSequence(ArrayList<Sequence> seq, int c, Rectangle SANKEY_RECT, int node_h, float half_node_w, float node_w){
		pApplet.noStroke();
		pApplet.fill(c);
		for(int i = 0; i< getDimensions().length-1; i++){
			Position d = getDimensions()[i];
			//draw edges
			for(Edge e :d.getEdges()){
				if(e.getTo_dx() < SANKEY_RECT.x+SANKEY_RECT.width){
					//check overlap with seq
					ArrayList<Sequence> intersection = intersection(seq, e.getSequences());
					if(intersection.size()>0){
						float thickness = PApplet.map(intersection.size(), 0, (float) getSequencingSample().getTotal_count(), 0f, node_h);
						// if(isExponentialScaling){
						// 	thickness = log_map(intersection.size(), 0, (float)total_count, 0f, _node_h, base);
						// }

						if(i == 0){
							drawSankey2(pApplet.g, e.getFrom_dx() - half_node_w, e.getFrom_dy(), e.getTo_dx(), e.getTo_dy(), 0.5f, 0.5f, thickness, node_w, half_node_w);	
						}else if( i == getDimensions().length-2){
							drawSankey2(pApplet.g, e.getFrom_dx(), e.getFrom_dy(), e.getTo_dx() + half_node_w, e.getTo_dy(), 0.5f, 0.5f, thickness, half_node_w, node_w);	
						}else{
							drawSankey2(pApplet.g, e.getFrom_dx(), e.getFrom_dy(), e.getTo_dx(), e.getTo_dy(), 0.5f, 0.5f, thickness, half_node_w, half_node_w);	
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

		float f5 = PApplet.sq(p2 - p1) + PApplet.sq(q2 - q1);

		float f6 = PApplet.sqrt(f5 - PApplet.sq(f3 + f1));
		float f7 = ((q2 - q1) * f6 + isDownSlope * (p2 - p1) * (f3 + f1)) / f5;
		float f8 = ((p2 - p1) * f6 - isDownSlope * (q2 - q1) * (f3 + f1)) / f5;
		float rx1 = (p1 + isDownSlope * f1 * f7);
		float ry1 = (q1 - isDownSlope * f1 * f8);
		float rx2 = (p2 - isDownSlope * f3 * f7);
		float ry2 = (q2 + isDownSlope * f3 * f8);

		float f9 = PApplet.sqrt(f5 - PApplet.sq(f2 + f4));
		float f10 = ((q2 - q1) * f9 + isDownSlope * (p2 - p1) * (f2 + f4)) / f5;
		float f11 = ((p2 - p1) * f9 - isDownSlope * (q2 - q1) * (f2 + f4)) / f5;

		float rx3 = (p1 + isDownSlope * f2 * f10);
		float ry3 = (q1 - isDownSlope * f2 * f11);
		float rx4 = (p2 - isDownSlope * f4 * f10);
		float ry4 = (q2 + isDownSlope * f4 * f11);

		float angle = (2.0F * (PApplet.atan2(ry1 - y_from, rx1 - x_from) % PApplet.TWO_PI));
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
				float dx = p1 + f1 * PApplet.cos(f12 - isDownSlope * PApplet.PI / 2.0F);
				float dy = q1 + f1 * PApplet.sin(f12 - isDownSlope * PApplet.PI / 2.0F);
				pg.curveVertex(dx, dy);
				trace.lineTo(dx, dy);
			}
			pg.vertex(rx1, ry1);
			pg.vertex(rx2, ry2);
			trace.lineTo(rx1, ry1);
			trace.lineTo(rx2, ry2);
			for (f12 = angle; f12 > 0.0F; f12 -= isDownSlope * 0.02F) {
				float dx = p2 + f3 * PApplet.cos(f12 + isDownSlope * PApplet.PI / 2.0F);
				float dy = q2 + f3 * PApplet.sin(f12 + isDownSlope * PApplet.PI / 2.0F);
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
				float dx = p2 + f4 * PApplet.cos(f12 + isDownSlope * PApplet.PI / 2.0F);
				float dy = q2 + f4 * PApplet.sin(f12 + isDownSlope * PApplet.PI / 2.0F);
				pg.curveVertex(dx, dy);
				trace.lineTo(dx, dy);
			}
			pg.vertex(rx4, ry4);
			pg.vertex(rx3, ry3);
			for (f12 = angle; f12 > 0.0F; f12 -= isDownSlope * 0.02F) {
				float dx = p1 + f2 * PApplet.cos(f12 - isDownSlope * PApplet.PI / 2.0F);
				float dy = q1 + f2 * PApplet.sin(f12 - isDownSlope * PApplet.PI / 2.0F);
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
			for (f12 = PApplet.PI; f12 > PApplet.PI + angle; f12 -= 0.02F) {
				float dx = p1 + f1 * PApplet.cos(f12 - PApplet.HALF_PI);
				float dy = q1 + f1 * PApplet.sin(f12 - PApplet.HALF_PI);
				pg.curveVertex(dx , dy);
				trace.lineTo(dx, dy);
			}
			pg.vertex(rx1, ry1);
			pg.vertex(rx2, ry2);
			for (f12 = angle; f12 < 0.0F; f12 += 0.02F) {
				float dx = p2 + f3 * PApplet.cos(f12 - PApplet.HALF_PI);
				float dy = q2 + f3 * PApplet.sin(f12 - PApplet.HALF_PI);
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
				float dx = p2 + f4 * PApplet.cos(f12 - PApplet.HALF_PI);
				float dy = q2 + f4 * PApplet.sin(f12 - PApplet.HALF_PI);
				pg.curveVertex(dx, dy);
				trace.lineTo(dx, dy);
			}
			pg.vertex(rx4, ry4);
			pg.vertex(rx3, ry3);
			trace.lineTo(rx4, ry4);
			trace.lineTo(rx3, ry3);
			for (f12 = PApplet.PI + angle; f12 < PApplet.PI; f12 += 0.02F) {
				float dx = p1 + f2 * PApplet.cos(f12 - PApplet.HALF_PI);
				float dy = q1 + f2 * PApplet.sin(f12 - PApplet.HALF_PI);
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

	//draw edges
	public void createPGraphics(int c, int alpha, Rectangle SANKEY_RECT,int SANKEY_POS, 
			int SANKEY_WIDTH, int SANKEY_HEIGHT, float frequencyThreshold,
			float half_node_w, float node_w){
		setFlow_pg(pApplet.createGraphics(SANKEY_RECT.x+SANKEY_WIDTH, SANKEY_RECT.y+SANKEY_HEIGHT));
		getFlow_pg().beginDraw();
		// flow_pg.background(255);
		getFlow_pg().fill(c);
		getFlow_pg().noStroke();
		for(int i = 0; i< getDimensions().length-1; i++){
			Position d = getDimensions()[i];
			//draw edges
			for(Edge e :d.getEdges()){
				// println((i+1)+"-"+(i+2)+":"+e.toString());
				if(e.getSequences().size() > 0 && e.getFrequency() >= frequencyThreshold){
					getFlow_pg().fill(c, alpha);
					getFlow_pg().noStroke();
					if(i == 0){
						e.setPath(drawSankey2(getFlow_pg(), e.getFrom_dx() - half_node_w, e.getFrom_dy(), e.getTo_dx(), e.getTo_dy(), 0.5f, 0.5f, e.getThickness(), node_w, half_node_w));
					}else if( i == getDimensions().length-2){
						e.setPath(drawSankey2(getFlow_pg(), e.getFrom_dx(), e.getFrom_dy(), e.getTo_dx() + half_node_w, e.getTo_dy(), 0.5f, 0.5f, e.getThickness(), half_node_w, node_w));
					}else{
						e.setPath(drawSankey2(getFlow_pg(), e.getFrom_dx(), e.getFrom_dy(), e.getTo_dx(), e.getTo_dy(), 0.5f, 0.5f, e.getThickness(), half_node_w, half_node_w));
					}
				}
			}
		}
		getFlow_pg().endDraw();
		//embedding
		setDisplay_pg(pApplet.createGraphics(SANKEY_RECT.width, SANKEY_RECT.height));
		getDisplay_pg().beginDraw();
		getDisplay_pg().image(getFlow_pg(), -1*SANKEY_RECT.x-SANKEY_POS, -1* SANKEY_RECT.y);
		getDisplay_pg().endDraw();
	}
	
	public void updatePGraphics(Rectangle SANKEY_RECT, int SANKEY_POS){
		getDisplay_pg().clear();
		getDisplay_pg().beginDraw();
		getDisplay_pg().image(getFlow_pg(), -1*SANKEY_RECT.x-SANKEY_POS ,-1* SANKEY_RECT.y);
		getDisplay_pg().endDraw();
	}	
	private float log_map(float input, float i_min, float i_max, float o_min, float o_max, float b){
		float f = (input - i_min)/(i_max-i_min);
		float flog = PApplet.pow(f, 1f/b);
		return flog*(o_max - o_min);
	}

	public PGraphics getFlow_pg() {
		return flow_pg;
	}

	public void setFlow_pg(PGraphics flow_pg) {
		this.flow_pg = flow_pg;
	}

	public PGraphics getDisplay_pg() {
		return display_pg;
	}
	
	public void setDisplay_pg(PGraphics display_pg) {
		this.display_pg = display_pg;
	}
	
	public SequencingSample getSequencingSample() {
		return ss;
	}
	
	public boolean[] getPositionSelected() {
		return positionSelected;
	}
	
	public void setPositionSelected(boolean[] positionSelected) {
		this.positionSelected = positionSelected;
	}
	
	public void togglePositionSelected(int index){
		this.positionSelected[index] = !positionSelected[index];
	}
	
	public Rectangle getToggle() {
		return toggle;
	}
	
	public void setToggle(Rectangle toggle) {
		this.toggle = toggle;
	}
	
	public Position[] getDimensions() {
		return dimensions;
	}
	
	public boolean isShowing() {
		return isShowing;
	}
}
