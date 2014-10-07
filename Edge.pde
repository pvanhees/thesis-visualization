class Edge implements Comparable{
	ArrayList<Sequence> sequences;
	Node from;
	Node to;
	String aa_edge;
	float frequency;


	float from_dx, from_dy, to_dx, to_dy;
	float thickness;

	GeneralPath path;


	// char type;
	// float from_dx, from_dy1, from_dy2;
	// float to_dx, to_dy1, to_dy2;


	Edge(Node from, Node to, String aa_edge){
		this.from = from;
		this.to = to;
		this.aa_edge  = aa_edge;
		sequences = new ArrayList<Sequence>();
	}

	void addSequence(Sequence seq){
		this.sequences.add(seq);
	}

	String toString(){
		return from.aminoAcid+" - "+to.aminoAcid+" ("+sequences.size()+") ("+from_dx+","+from_dy+") to ("+to_dx+","+to_dy +")";
	}

	// Edge(Node from, Node to, char type){
	// 	this.from = from;
	// 	this.to = to;
	// 	this.type  = type;
	// 	sequences = new ArrayList<Sequence>();

	// }

	//sort by the from_nodes
	//sort by the gap
	public int compareTo(Object obj) {
        Edge that = (Edge) obj;
        float this_from_y = this.from.dy;
        float that_from_y = that.from.dy;

        if(this_from_y < that_from_y){
        	return -1;
        }else if(this_from_y > that_from_y){
        	return 1;
        }else{
        	//check gap
        	float this_y_gap = this.from.dy - this.to.dy;
        	float that_y_gap = that.from.dy - that.to.dy;

        	if(this_y_gap < that_y_gap){
        		return 1;
        	}else if(this_y_gap > that_y_gap){
        		return -1;
        	}else{
        		//this should not happen
        		println("this should not happen - edge compareTo()");
        		return 0;
        	}


        }

    }

}
