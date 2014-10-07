package model;
import java.util.ArrayList;
import java.util.HashMap;

import org.jmol.util.Rectangle;

public class Position{
		private int index;
		private ArrayList<Node> nodes;
		private HashMap<String, Node> node_map;
		private ArrayList<Edge> edges;
		private HashMap<String, Edge> edge_map; //key = "A_A"


		//display
		private float gap_size;
		private float height;

		private Rectangle label_rect;

		private float uncertainty;
		private float informationContent;

		public Position(int i){
			index = i;
			nodes = new ArrayList<Node>();
			edges = new ArrayList<Edge>();
			node_map = new HashMap<String, Node>();
			edge_map = new HashMap<String, Edge>();
		}

		public ArrayList<Edge> getAllEdgesStartsWith(String aa){
			ArrayList<Edge> result = new ArrayList<Edge>();
			for(String key : edge_map.keySet()){
				if(key.startsWith(aa)){
					Edge e = (Edge) edge_map.get(key);
					result.add(e);
				}
			}

			//sorthing to be implemented...
			return result;
		}

		public String getMostFrequentResidue(){
			String result =  "";
			int count = 0;
			for(Node n : nodes){
				if(count < n.getSequences().size()){
					count = n.getSequences().size();
					result = ""+n.getAminoAcid();
				}
			}
			return result;
		}
		
		public ArrayList<Edge> getEdges() {
			return edges;
		}
		
		public HashMap<String, Node> getNode_map() {
			return node_map;
		}
		
		public ArrayList<Node> getNodes() {
			return nodes;
		}
		
		public HashMap<String, Edge> getEdge_map() {
			return edge_map;
		}
	}