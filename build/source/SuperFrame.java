import java.awt.BorderLayout;
import java.awt.Frame;

import processing.core.PApplet;
import view.SeDD;


public class SuperFrame extends Frame{
	
	public SuperFrame() {
		super("test");
		
		setLayout(new BorderLayout());
		PApplet embed = new SeDD();
		add(embed, BorderLayout.CENTER);
		
		embed.init();
	}

}
