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
	}