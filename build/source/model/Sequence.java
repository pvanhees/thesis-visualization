package model;
public class Sequence{
		private char[] sequence;
		private String id;


		public Sequence(String id, char[] seq){
			this.id = id;
			this.sequence = seq;
		}
		
		public char[] getSequence() {
			return sequence;
		}
		
		public String getId() {
			return id;
		}
		
		@Override
		public String toString() {
			String sequence2 = new String(sequence);
			return sequence2;
		}
		
		//TODO check if this is correct with Ryo
		@Override
		public boolean equals(Object obj) {
			return this.id.equals(((Sequence)obj).getId()) && this.sequence.equals(((Sequence)obj).getSequence());
		}
		
//		@Override
//		public int hashCode() {
//			int hash = 1;
//			hash = hash * 31 + id.hashCode();
//			hash = hash * 31 + sequence.hashCode();
//			return hash;
//		}
	}