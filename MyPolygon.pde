import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;

class MyPolygon{
	GeneralPath trace;


	MyPolygon(float[] x, float[] y){
		trace = new GeneralPath();
		trace.moveTo(x[0], y[0]);
		for(int i = 1; i <x.length; i++){
			trace.lineTo(x[i], y[i]);
		}
		trace.closePath();
	}

	void render(){
    	PathIterator pi = trace.getPathIterator(null);
    	float[] pts = new float[2];
    	while (!pi.isDone()) {
			int type = pi.currentSegment(pts);
			if (type == PathIterator.SEG_MOVETO){
				beginShape();
				vertex(pts[0],pts[1]);
			}
			if (type == PathIterator.SEG_LINETO) { // LINETO
			  	vertex(pts[0],pts[1]);
			  	//println(pts[0]+","+pts[1]);
			}
			if (type == PathIterator.SEG_CLOSE) {
			  	endShape();
			}
			pi.next();
		}
	}
  





}
