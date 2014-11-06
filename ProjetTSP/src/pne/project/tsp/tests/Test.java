package pne.project.tsp.tests;

import pne.project.tsp.beans.Graph;
import pne.project.tsp.managers.GraphManager;
import pne.project.tsp.utils.BoundsGraph;
import pne.project.tsp.utils.TSPReader;
import pne.project.tsp.utils.XmlReader;
import pne.project.tsp.view.GraphView;

public class Test {
	
	public static void main(String[] args) {
		BoundsGraph boundsGraph = new BoundsGraph();
		 Graph g1 = XmlReader.buildGraphFromXml("data/XML/st70.xml");
//		 GraphManager.writeLinearProgram(g1,"D:/results.txt");
		double[][] nodePositions = TSPReader.getPositionsFromTsp("data/TSP/st70.tsp", boundsGraph);
		
		GraphView gv = new GraphView(nodePositions, boundsGraph, g1);
		gv.buildCardLayout();
	}


}