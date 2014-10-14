package model;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Edge implements Comparable<Edge>{
		private ArrayList<Sequence> sequences;
		private Node from;
		private Node to;
		private String aa_edge;
		private float frequency;
		private Map<Edge,Integer> intersectionAmount;


		private float from_dx, from_dy, to_dx, to_dy;

		private GeneralPath path;


		// char type;
		// float from_dx, from_dy1, from_dy2;
		// float to_dx, to_dy1, to_dy2;


		public Edge(Node from, Node to, String aa_edge){
			this.from = from;
			this.to = to;
			this.aa_edge  = aa_edge;
			this.sequences = new ArrayList<Sequence>();
			intersectionAmount = new HashMap<>();
		}

		public void addSequence(Sequence seq){
			this.getSequences().add(seq);
		}

		public String toString(){
			return from.getAminoAcid()+" - "+to.getAminoAcid()+" ("+getSequences().size()+") ("+from_dx+","+from_dy+") to ("+to_dx+","+to_dy +")";
		}
		
//		@Override
//		public int hashCode() {
//			int hash = 1;
//			hash = hash * 31 + sequences.hashCode();
//			hash = hash * 31 + from.hashCode();
//			hash = hash * 31 + to.hashCode();
//			return hash;
//		}
//		
//		@Override
//		public boolean equals(Object obj) {
//			if(obj == null) return false;
//			if(!(obj instanceof Edge)) return false; 
//			Edge e = (Edge) obj;
//			return e.getFrom().equals(this.getFrom()) && e.getTo().equals(this.getTo()) && e.getSequences().equals(this.getSequences());
//		}

		// Edge(Node from, Node to, char type){
		// 	this.from = from;
		// 	this.to = to;
		// 	this.type  = type;
		// 	sequences = new ArrayList<Sequence>();

		// }

		//sort by the from_nodes
		//sort by the gap
		@Override
		public int compareTo(Edge obj) {
			Edge that = (Edge) obj;
			float this_from_y = this.from.getDy();
			float that_from_y = that.from.getDy();

			if(this_from_y < that_from_y){
				return -1;
			}else if(this_from_y > that_from_y){
				return 1;
			}else{
				//check gap
				float this_y_gap = this.from.getDy() - this.to.getDy();
				float that_y_gap = that.from.getDy() - that.to.getDy();

				if(this_y_gap < that_y_gap){
					return 1;
				}else if(this_y_gap > that_y_gap){
					return -1;
				}else{
					//this should not happen
					System.out.println("this should not happen - edge compareTo()");
					return 0;
				}


			}

		}

		public ArrayList<Sequence> getSequences() {
			return sequences;
		}
		
		public float getFrequency() {
			return frequency;
		}
		
		public void setFrequency(float frequency) {
			this.frequency = frequency;
		}
		
		public GeneralPath getPath() {
			return path;
		}
		
		public void setPath(GeneralPath path) {
			this.path = path;
		}
		
		public float getFrom_dx() {
			return from_dx;
		}

		public void setFrom_dx(float from_dx) {
			this.from_dx = from_dx;
		}
		
		public float getFrom_dy() {
			return from_dy;
		}
		
		public void setFrom_dy(float from_dy) {
			this.from_dy = from_dy;
		}
		
		public float getTo_dx() {
			return to_dx;
		}
		
		public void setTo_dx(float to_dx) {
			this.to_dx = to_dx;
		}
		
		public float getTo_dy() {
			return to_dy;
		}
		
		public void setTo_dy(float to_dy) {
			this.to_dy = to_dy;
		}
		
		public Node getFrom() {
			return from;
		}
		
		public Node getTo() {
			return to;
		}
		
		public int getSequenceAmount(){
			return sequences.size();
		}
		
		public int getIntersectionAmount(Edge e) {
			Integer i = intersectionAmount.get(e);
			if(i == null) return 0;
			else return i;
		}
		
		public void setIntersectionAmount(Edge edge, int amount) {
			intersectionAmount.put(edge, (Integer)amount);
		}
	}