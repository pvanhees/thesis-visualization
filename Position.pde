class Position{
	int index;
	ArrayList<Node> nodes;
	HashMap<String, Node> node_map;
    ArrayList<Edge> edges;
    HashMap<String, Edge> edge_map; //key = "A_A"


	//display
	float gap_size;
	float height;

	Rectangle label_rect;

	float uncertainty;
	float informationContent;

	Position(int i){
		index = i;
		nodes = new ArrayList<Node>();
        edges = new ArrayList<Edge>();
        node_map = new HashMap<String, Node>();
        edge_map = new HashMap<String, Edge>();
	}

	ArrayList<Edge> getAllEdgesStartsWith(String aa){
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

	String getMostFrequentResidue(){
		String result =  "";
		int count = 0;
		for(Node n : nodes){
			if(count < n.sequences.size()){
				count = n.sequences.size();
				result = ""+n.aminoAcid;
			}
		}
		return result;
	}



}
