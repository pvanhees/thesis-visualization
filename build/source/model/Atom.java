package model;
import processing.core.PVector;

public class Atom{
		private char element;
		private char chain;
		private PVector pos;
		private int index;

		public Atom(char element,int index, char chain, float x, float y, float z){
			this.element = element;
			this.chain = chain;
			this.pos = new PVector(x, y, z);
			this.index = index;
		}
	}