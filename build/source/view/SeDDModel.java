package view;

import java.awt.Rectangle;
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

	
	public SeDDModel(SequencingSample ss) {
		this.ss = ss;

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
	
	public Position[] calculateDimensions(){
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
	public String getCommonSequence(){
		String result = "";
		for(Position d :dimensions){
			result += d.getMostFrequentResidue();
		}
		return result;
	}
	
	public float log_map(float input, float i_min, float i_max, float o_min, float o_max, float b){
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
