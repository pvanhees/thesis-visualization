package model;
import java.util.HashMap;

public class Model{
		private int index;
		// ArrayList<Residue> residues;
		// String sequence = "";
		private HashMap<Character, Chain> chain_map;

		public Model(int index){
			this.index = index;
			chain_map = new HashMap<Character, Chain>();
			// residues = new ArrayList<Residue>();
		}

		public void addAtom(Atom a, char aa, char chain_id, int resIndex){
			//find chain
			Chain c = (Chain) chain_map.get(chain_id);
			if(c == null){
				//new chain
				c = new Chain(chain_id);
				chain_map.put(new Character(chain_id), c);
			}
			//find residue
			Residue r = (Residue)c.getResidue_map().get(resIndex);
			if(r == null){
				//new residue
				r = new Residue(resIndex, aa);
				c.getResidue_map().put(new Integer(resIndex), r);
				c.setSequence(c.getSequence() + aa);
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