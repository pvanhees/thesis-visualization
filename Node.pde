class Node implements Comparable{
	ArrayList<Sequence> sequences;
	char aminoAcid;
	Position dimension;

    float frequency;

    float dy;
    float from_runningY = 0;
    float to_runningY = 0;


    


    //display
    // float height;
    // float dx, dy; //display position

    //height for display
    float neg_height;
    float pos_height;



	
	Node(Position dimension, char aa){
		this.dimension = dimension;
		aminoAcid = aa;
		sequences = new ArrayList<Sequence>();
	}

    void addSequence(Sequence seq){
        sequences.add(seq);
    }


    //set intial value
    void setRunningY(float n){
        from_runningY = n;
        to_runningY = n;
    }

	public int compareTo(Object obj) {
        // Node e = (Node) obj;

        // float this_0_per = (float)this.type_0_count / (float) this.sequences.size();
        // float that_0_per = (float)e.type_0_count / (float) e.sequences.size();


        // if(this_0_per > that_0_per){
        //     return -1;
        // }else if(this_0_per < that_0_per){
        //     return 1;
        // }else{
        //     //same percentage
        //     //check the 0 count
        //     if(this.type_0_count > e.type_0_count){
        //         return -1;
        //     }else if(this.type_0_count < e.type_0_count){
        //         return 1;
        //     }else{
        //         //same
        //         return 0;
        //     }
        // }
        // if(this.sequences.size() < e.sequences.size()){
        // 	return 1;
        // }else if(this.sequences.size() > e.sequences.size()){
        // 	return -1;
        // }else{
        	return 0;
        // }
    }


  

}
