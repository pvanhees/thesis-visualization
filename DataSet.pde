// do one sample at a time
Sample[] samples;
int _total_length;
int _total_variation;

//setting from configuration/
char[] all_aa_default;// = {'A', 'I', 'L', 'V', 'F','W','Y','N', 'C', 'Q', 'M', 'S', 'T','D', 'E','R', 'H', 'K','G','P','.', 'X'};
char[][] aa_group_default; // = {{ 'A', 'G','I', 'L', 'V'}, {'C','M','S','T'},{'P'},{'F','W', 'Y'},{'H', 'K', 'R'},{'D','E','N','Q'},{'.'}, {'X'}};
String[] _AA_Group;// = {"aliphatic", "OH or Sulfur", "cyclic", "aromatic", "basic", "acidic", "blank", "unknown"};
String[] sample_names;
String[] file_urls;
int[] sample_colors;


HashMap<String, Rectangle> aa_rect_map;
HashMap aminoAcidMap; //amino acid codes



float _mean, _sd;


void loadData(){
    samples = new Sample[sample_names.length];
	// samples = new Sample[2];
    //load data
    for(int i = 0; i<samples.length; i++){
        status_msg = "loading "+sample_names[i]+" aligned sequences";
        samples[i] = new Sample(file_urls[i], sample_names[i]);
    }
    // samples[0] = new Sample("panel-a.txt", "all");
    // samples[0] = new Sample("panel-b.txt", "gram_negative");
    // samples[1] = new Sample("panel-c.txt", "gram_positive");

    _total_length = samples[0].seq_length;
    _total_variation = all_aa_default.length;


    println("debug: total_length = "+_total_length +" total_variation = "+ _total_variation);
    // println(dataPath("panel-b.txt"));
}

//calculate frequency
void preprocessData(){
    aminoAcidMap = setupAminoAcidMap();

    for(Sample s: samples){
        // println("\tpreprcessData():"+s.name);
        status_msg = "preprocessing: calculating frequency for "+s.name;
        s.calculateFrequency();

    
        //flags
        s.positionSelected = new boolean[_total_length];
        Arrays.fill(s.positionSelected, Boolean.FALSE);
    }
    // println("debug: end of preprocessData()");
}


float log2(float n){
    // return n*_line_weight;
    return (float)(Math.log(n)/Math.log(2));
}





float getMean(float[][] matrix){
    float sum = 0;
    for(float[] row : matrix){
        for(float f : row){
            sum += f;
        }
    }
    return sum/(matrix.length*matrix[0].length);
}

float getSD(float[][] matrix){
    return sqrt(getVariance(matrix));
}
float getVariance(float[][] matrix){
    int size = matrix.length*matrix[0].length;
    float mean = getMean(matrix);
    float temp = 0;
    for(float[] row : matrix){
        for(float f : row){
            temp += (f-mean)* (f- mean);
        }
    }
    return temp/size;
}

float log_map(float input, float i_min, float i_max, float o_min, float o_max, float b){
  float f = (input - i_min)/(i_max-i_min);
  float flog = pow(f, 1f/b);
  return flog*(o_max - o_min);
}

//pdb stuff
HashMap<String, Character> setupAminoAcidMap(){
  HashMap map = new HashMap<String, Character>();
  map.put("GLY", new Character('G'));
  map.put("ALA", new Character('A'));
  map.put("VAL", new Character('V'));
  map.put("LEU", new Character('L'));
  map.put("ILE", new Character('I'));
  map.put("PHE", new Character('F'));
  map.put("TYR", new Character('Y'));
  map.put("TRP", new Character('W'));
  map.put("PRO", new Character('P'));
  map.put("HIS", new Character('H'));
  map.put("LYS", new Character('K'));
  map.put("ARG", new Character('R'));
  map.put("SER", new Character('S'));
  map.put("THR", new Character('T'));
  map.put("GLU", new Character('E'));
  map.put("GLN", new Character('Q'));
  map.put("ASP", new Character('D'));
  map.put("ASN", new Character('N'));
  map.put("CYS", new Character('C'));
  map.put("MET", new Character('M'));
  map.put("MSE", new Character('M'));
  map.put("CSE", new Character('U'));
  map.put("SEC", new Character('U'));
  map.put("PYH", new Character('O'));
  map.put("PYL", new Character('O'));
  return map;
}

Character get1LetterCode(String code3){
  Character code1 = null;
  if(aminoAcidMap == null){
    aminoAcidMap = setupAminoAcidMap();
  }
  code1 = (Character) aminoAcidMap.get(code3);
  if (code1 == null) {
    println("Error: get1LetterCode()");
    return null;
  } else {
    return code1;
  }
}





