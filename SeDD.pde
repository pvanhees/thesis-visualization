//Sequence Diversity Diagram v1.0.0  copyright Ryo Sakai

import processing.pdf.*;


import java.awt.Rectangle; 
import java.awt.geom.GeneralPath;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Collections;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.io.*;
import javax.swing.JTextArea;
import javax.swing.JOptionPane;
import javax.swing.JFrame;
import javax.swing.JPanel;


import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolViewer;
import org.jmol.util.Logger;
import org.openscience.jmol.app.jmolpanel.console.AppConsole;




int _STAGE_WIDTH = 1000;
int _STAGE_HEIGHT = 500;
int _MARGIN = 10;

int _TOP_MARGIN = _MARGIN *5;
int _LEFT_MARGIN = _MARGIN*10;
int _RIGHT_MARGIN = _MARGIN*5;
int _BOTTOM_MARGIN = _MARGIN*50;

int _SANKEY_SLIDER_HEIGHT = _MARGIN;
int _MATRIX_WIDTH;

float _TOP_RATIO = 0.45;
float _BOTTOM_RATIO =0.5;
float _SANKEY_RATIO = 0.90;
float _LEGEND_RATIO = 0.10;
float _VERTICAL_GAP_RATIO = 0.5;//gap is half of node height
float _HORIZONTAL_GAP_RATIO = 2; //gap is *2 of node width

int _SANKEY_WIDTH, _SANKEY_HEIGHT; //PGraphic size //real length
int _SANKEY_POS = 0;

Rectangle _DISPLAY_RECT;
Rectangle _SANKEY_RECT;
Rectangle _SANKEY_SLIDER_RECT;
Rectangle _SANKEY_SLIDER_BAR;
Rectangle _LEGEND_RECT;
Rectangle _BTN_RECT;
Rectangle _ALL_MATRIX_RECT;


int _legend_width = _MARGIN*15;


int _node_w = 10;
int _node_h = 10;
float _half_node_w = _node_w/2;
int _horizontal_gap = round( _MARGIN*2.5);
float _min_node_h = 0.5f;//0f;//0.5f; /////minimum node height
int _vertical_gap = _MARGIN/3;
int _text_margin = 2;


//Slider
boolean showSlider = false;
boolean slider_drag = false;
float slider_onPress = 0;



PFont font;
PFont small_font;

boolean record = false;
boolean loadPreprocessedMI = true;
boolean showZscore = true;


float zscore_min = -10;
float zscore_max = 10;
float zscore_increment = 0.1;
float mutual_z_min = Float.MAX_VALUE; //initial value
float mutual_z_max = Float.MIN_VALUE;


//log scaling
float base = 2.0f;
boolean isExponentialScaling = false;

//optimize cpu
int draw_counter = 0;
int draw_max = 60; //2 seconds = draw 90 times before it stops

//legend threshold
float _frequencyThreshold = 0; //global threshold for frequency

//flags
boolean _IS_PROTEIN_SEQUENCE = false;
boolean _FILE_SELECTED = false;
boolean _FILE_LOADING = false;
boolean _FILE_LOADED = false;


//6 group classification
float areaToDiameter(float area){
  float diameter = sqrt((area/PI))*2;
  return diameter;
}

void setup(){
    font = createFont("AndaleMono", 10);
    small_font = createFont("Supernatural1001", 10);
    textFont(font);
    smooth();
    frameRate(30);

    //get the display size
    _STAGE_WIDTH = displayWidth;
    _STAGE_HEIGHT = displayHeight;
    size(_STAGE_WIDTH, _STAGE_HEIGHT);
    //setup dataloading page
    setupDataLoadingPage();
}

void startLoadingData(){
    //determine how much data need to be loaded or processed

    //update status message
    status_msg = "";
    loadData();
    //calculate frequency
    preprocessData();
    //layout
    initLayout();
    //set stage size
    setStageSize();
    //determine display position
    updateLayout();
    //determine edges
    updateEdges();

    _FILE_LOADING = false;
    _FILE_LOADED = true;

    status_msg = "Done!";
}

String timestamp() {
  return String.format("%1$tm%1$td%1$tY_%1$tH%1$tM", Calendar.getInstance());
}






