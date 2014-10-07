class Model{
	int index;
	// ArrayList<Residue> residues;
	// String sequence = "";
	HashMap<Character, Chain> chain_map;

	Model(int index){
		this.index = index;
		chain_map = new HashMap<Character, Chain>();
		// residues = new ArrayList<Residue>();
	}

	void addAtom(Atom a, char aa, char chain_id, int resIndex){
		//find chain
		Chain c = (Chain) chain_map.get(chain_id);
		if(c == null){
			//new chain
			c = new Chain(chain_id);
			chain_map.put(new Character(chain_id), c);
		}
		//find residue
		Residue r = (Residue)c.residue_map.get(resIndex);
		if(r == null){
			//new residue
			r = new Residue(resIndex, aa);
			c.residue_map.put(new Integer(resIndex), r);
			c.sequence += aa;
		}
		//add atom
		r.addAtom(a);

		//revise how to store residues
		// if(residues.size() < resIndex){
		// 	//new residue
		// 	r = new Residue(resIndex, aa);
		// 	sequence += aa;
		// 	residues.add(r);
		// }else{
		// 	r = residues.get(resIndex-1);
		// }

		//add atom
		// r.addAtom(a);
	}

}

class Chain{
	HashMap<Integer, Residue>  residue_map;
	char name;
	String sequence = "";
	Chain(char name){
		this.name = name;
		residue_map = new HashMap<Integer, Residue>();
	}
}

class Residue{
	ArrayList<Atom> atoms;
	int index;  //from 1 - N;
	char aa;
	Atom center_atom = null;
	PVector center;

	Residue(int index, char aa){
		this.index = index;
		this.aa = aa;
		this.atoms = new ArrayList<Atom>();

	}

	void addAtom(Atom a){
		atoms.add(a);
	}
}

class Atom{
	char element;
	char chain;
	PVector pos;
	int index;

	Atom(char element,int index, char chain, float x, float y, float z){
		this.element = element;
		this.chain = chain;
		this.pos = new PVector(x, y, z);
		this.index = index;
	}
}
