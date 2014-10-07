package model;
import java.util.ArrayList;

import processing.core.PVector;

public class Residue{
		private ArrayList<Atom> atoms;
		private int index;  //from 1 - N;
		private char aa;
		private Atom center_atom = null;
		private PVector center;

		public Residue(int index, char aa){
			this.index = index;
			this.aa = aa;
			this.atoms = new ArrayList<Atom>();
		}

		public void addAtom(Atom a){
			atoms.add(a);
		}
	}