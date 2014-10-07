class NumberBox{
	int dx, dy, dw, dh;
	Rectangle num_rect, up_rect, down_rect;
	float v_current, v_max, v_min;
	String name;

	int text_color = 60;

	NumberBox(int dx, int dy, int dw, int dh, float v_current, float v_max, float v_min, String name){
		this.dx = dx;
		this.dy = dy;
		this.dw = dw;
		this.dh = dh;
		this.v_current = v_current;
		this.v_max = v_max;
		this.v_min = v_min;
		this.name = name;

		float btn_size = (float)ceil(dh /2);
		num_rect = new Rectangle(dx, dy, round(dw-btn_size), dh);
		up_rect = new Rectangle(round(dx+dw-btn_size), dy, round(btn_size), round(btn_size));
		down_rect = new Rectangle(round(dx+dw-btn_size), round(dy+dh-btn_size), round(btn_size), round(btn_size));

		// println("debug: numberbox created: min ="+this.v_min+"  max = "+this.v_max+"  current="+v_current);
	}


	void display(){
		rectMode(CORNER);
		fill(255);
		stroke(120);
		strokeWeight(1);
		rect(num_rect.x, num_rect.y, num_rect.width, num_rect.height);
		//btns
		stroke(60);
		fill(up_rect.contains(mouseX, mouseY)? 120: 180);
		rect(up_rect.x, up_rect.y, up_rect.width, up_rect.height);
		fill(down_rect.contains(mouseX, mouseY)? 120: 180);
		rect(down_rect.x, down_rect.y, down_rect.width, down_rect.height);
		fill(255);
		drawTraiange(up_rect, true);
		drawTraiange(down_rect, false);

		//draw value
		fill(60);
		textFont(font);
		textAlign(RIGHT, CENTER);
		text(nf(v_current,0, 2), num_rect.x+num_rect.width -2, (float)num_rect.getCenterY());


		//label
		fill(text_color);
		textFont(font);
		textAlign(LEFT, CENTER);
		text(name, num_rect.x + dw +_MARGIN/2, (float)num_rect.getCenterY());

	}

	void drawTraiange(Rectangle rect, boolean isUP){
		// fill(240);
		noStroke();
		if(isUP){
			triangle((float) rect.getCenterX(), rect.y+1, rect.x+rect.width -1, rect.y+rect.height-1, rect.x+1, rect.y+rect.height-1);
		}else{
			triangle((float) rect.getCenterX(), rect.y+rect.height-1, rect.x+rect.width -1, rect.y+1, rect.x+1, rect.y+1);
		}
	}

	boolean contains(int mouseX, int mouseY){
		if(up_rect.x <= mouseX && mouseX <= up_rect.x+up_rect.width){
			if(up_rect.y <= mouseY && mouseY <= down_rect.y+down_rect.height ){
				return true;
			}
		}
		return false;
	}
}
