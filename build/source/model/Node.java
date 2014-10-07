package model;
import java.util.ArrayList;

public class Node implements Comparable{
		private ArrayList<Sequence> sequences;
		private char aminoAcid;
		private Position dimension;

		private float frequency;

		private float dy;
		private float from_runningY = 0;
		private float to_runningY = 0;

		//display
		// float height;
		// float dx, dy; //display position

		//height for display
		private float neg_height;
		private float pos_height;

		public Node(Position dimension, char aa){
			this.dimension = dimension;
			aminoAcid = aa;
			sequences = new ArrayList<Sequence>();
		}

		public void addSequence(Sequence seq){
			sequences.add(seq);
		}


		//set intial value
		public void setRunningY(float n){
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

		public char getAminoAcid() {
			return aminoAcid;
		}

		public float getDy() {
			return dy;
		}
		
		public void setDy(float dy) {
			this.dy = dy;
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
		
		public float getTo_runningY() {
			return to_runningY;
		}
		
		public void setTo_runningY(float to_runningY) {
			this.to_runningY = to_runningY;
		}
		
		public float getFrom_runningY() {
			return from_runningY;
		}
		
		public void setFrom_runningY(float from_runningY) {
			this.from_runningY = from_runningY;
		}
	}