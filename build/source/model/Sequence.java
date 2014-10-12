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
		
		//TODO check if this is correct with Ryo
		@Override
		public boolean equals(Object obj) {
			return this.id.equals(((Sequence)obj).getId()) && this.sequence.equals(((Sequence)obj).getSequence());
		}
	}