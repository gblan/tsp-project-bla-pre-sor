package ps.project.tsp.vns;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import pne.project.tsp.beans.Graph;
import pne.project.tsp.beans.NodeCouple;
import pne.project.tsp.utils.FileReader;

public class SolutionVNS {
	private Graph graph_scenario;
	private ArrayList<Integer> pathChosen;	// ex pour un graphe � 4 noeuds : 1-2-3-4
	private double pathCost;
	
	// penalite utile pour la partie stochastique
	private double[][] penaliteLambda;
	private double[][] penaliteRo;
	
	public SolutionVNS(Graph g, ArrayList<Integer> pathChosen, double pathCost){
		graph_scenario = g;
		this.pathChosen = pathChosen;
		this.pathCost = pathCost;
	}
	
	public SolutionVNS(Graph g, ArrayList<Integer> pathChosen){
		graph_scenario = g;
		this.pathChosen = pathChosen;
		this.pathCost = calculPathCost();
	}
	
	public SolutionVNS(Graph g){
		graph_scenario = g;
		pathChosen = new ArrayList<Integer>();
		pathCost = 0.0;
	}
	
	
	public SolutionVNS(Graph g, ArrayList<Integer> pathChosen, double pathCost, double[][] penaliteLambda, double[][] penaliteRo) {
		this.graph_scenario = g.clone();
		this.pathChosen = (ArrayList<Integer>) pathChosen.clone();
		this.pathCost = pathCost;
		if(penaliteLambda != null){
			this.penaliteLambda = penaliteLambda.clone();	
		}
		if(penaliteRo != null){
			this.penaliteRo = penaliteRo.clone();
		}
	}


	/**
	 * Faire les m�thodes pour savoir si circuit hamiltonien
	 * 		- sous m�thode sur les aretes entrantes
	 * 		- sous m�thode sur les aretes sortantes
	 * 		- sous m�thode pour les sous tours
	 */
	
	/**
	 *  Comparator used in glouton algorithm
	 */
	private Comparator<NodeCouple> compareNodeCouple = new Comparator<NodeCouple>() {

		@Override
		public int compare(NodeCouple arg0, NodeCouple arg1) {
			if (arg0.getCostEdge() < arg1.getCostEdge()) {
				return -1;
			} else if (arg0.getCostEdge() > arg1.getCostEdge()) {
				return 1;
			} else {
				return 0;
			}
		}
	};
	
	/**Algorithme de Glouton lanc� au tout d�but du programme
	 * 
	 * @param graph_scenario
	 * @return
	 */
	public ArrayList<Integer> gloutonAlgorithm() {
		ArrayList<NodeCouple> alNodeCouple = new ArrayList<NodeCouple>();
		boolean[] tabInner = new boolean[graph_scenario.getNbNode()];
		boolean[] tabOuter = new boolean[graph_scenario.getNbNode()];
		ArrayList<NodeCouple> tmpSolution = new ArrayList<NodeCouple>();
		
		/* recuperation de toutes les ar�tes*/
		for(int i=0;i<graph_scenario.getNbNode();i++){
			for(int j=0;j<graph_scenario.getNbNode();j++){
				if(i!=j && graph_scenario.getTabAdja()[i][j]>0){
					alNodeCouple.add(new NodeCouple(i, j, graph_scenario.getTabAdja()[i][j]));
				}
			}
		}
		
		/* tri des ar�tes selon le poids */
		Collections.sort(alNodeCouple, compareNodeCouple);
		
		/* construction de la solution gloutonne*/
		
		/* TODO verifier les sous tours*/
		for(NodeCouple e : alNodeCouple){
			
			/* aucun des 2 d�ja occupes*/
			if(!tabOuter[e.getN1()] && !tabInner[e.getN2()]){
				
				/* sous tours*/
				if((!tabInner[e.getN1()] || !tabOuter[e.getN2()])|| endOfFillingGloutonTab(tabInner, tabOuter) || !isSubCycle(e,tmpSolution)){
					tmpSolution.add(e);
					tabOuter[e.getN1()] = true;
					tabInner[e.getN2()] = true;
				}
			}			

		}
		
		
		
		/**
		 * Remarque : on souhaite que la solution soit de la forme 1-2-3-4 et non (1,2) (2,3) (3,4) (4,1)
		 */
		ArrayList<NodeCouple> result = sortSolution(tmpSolution);
//		System.out.println("result = " + result);

		ArrayList<Integer> solution = new ArrayList<Integer>();
		for(NodeCouple nc : result){
			solution.add(nc.getN1());
		}
//		System.out.println("solution : "+solution);
		
		return solution;
	}

	private ArrayList<NodeCouple> sortSolution(ArrayList<NodeCouple> param) {
		ArrayList<NodeCouple> result = new ArrayList<NodeCouple>();
		result.add(param.get(0));
		
		int valueToSearch = 0;
		
		while(result.size() < param.size()){
			for (NodeCouple nodeCouple : param) {
				if(nodeCouple.getN1() == result.get(result.size()-1).getN2()){
					result.add(nodeCouple);
				}
			}
		}
		
		return result;		
	}

	/**
	 * @param e
	 * @param result
	 * @return true if the param list<NodeCouple> contains a sub-cycle with the node e
	 */
	private boolean isSubCycle(NodeCouple e, ArrayList<NodeCouple> result) {
		/*Partir de e.getN2()
		 * et faire un tour jusqu'a retrouver e.getN1()
		 */
		int valueToFind = e.getN1();
		int nodeTmp = e.getN2();
		NodeCouple nodeCoupleTmp = null;
		
		/* trouver le premier */
		for (NodeCouple nodeCouple : result) 
		{
			if (nodeCouple.getN1()==nodeTmp)
			{
				if(nodeCouple.getN2()==valueToFind){
					return true;
				}
				nodeCoupleTmp = nodeCouple;
			}
		}	
		
		/* faire tout le tour.*/
		for (int i = 0; i < result.size(); i++) 
		{
			for (NodeCouple nodeCouple : result) 
			{

				if (nodeCouple.getN1()==nodeCoupleTmp.getN2())
				{
					if(nodeCouple.getN2()==valueToFind){
						return true;
					}
					nodeCoupleTmp = nodeCouple;
				}
			}			
		}

		return false;
	}

	/**
	 * @param tabInner
	 * @param tabOuter
	 * @return true if only one element is to add to the two tabs
	 */
	private boolean endOfFillingGloutonTab(boolean[] tabInner, boolean[] tabOuter) {
		int a=0,b=0;
		for (int i = 0; i < tabOuter.length; i++) {
			if(!tabInner[i]){
				a++;
			}
		}
		
		for (int i = 0; i < tabOuter.length; i++) {
			if(!tabOuter[i]){
				b++;
			}
		}
		return (a==1 && b==1);
	}

	public double getPathCost() {
		return pathCost;
	}
	
	public double calculPathCost(){
		double cost = 0.0;
		int n = pathChosen.size();
		for(int i=0; i<n-1; i++){
			cost+=graph_scenario.getTabAdja()[pathChosen.get(i)][pathChosen.get(i+1)];
		}
		cost+=graph_scenario.getTabAdja()[pathChosen.get(n-1)][pathChosen.get(0)];
		return cost;
	}
	
	/**
	 * Les penalites s'applique aux aretes deterministes de la solution
	 * @param solRef
	 * @return
	 */
	public double calculPathCostWithPenalty(SolutionVNS solRef){
		double cost = 0.0;		
		int n = pathChosen.size();
		int n1, n2;
		for(int i=0; i<n-1; i++){
			n1=pathChosen.get(i);
			n2=pathChosen.get(i+1);
			
			// cas ou c'est une arete deterministe --> il faut appliquer les penalites
			if(!graph_scenario.isEdgeStochastic(n1, n2)){
				cost = cost + graph_scenario.getTabAdja()[n1][n2] + penaliteLambda[n1][n2] + penaliteRo[n1][n2]/2 - penaliteRo[n1][n2]*areteDansSolution(solRef.getPathChosen(), n1, n2); 
			}
			
			// cas ou c'est une arete stochastique --> pas de penalite
			else{
				cost+=graph_scenario.getTabAdja()[n1][n2];
			}
		}
		n1 = pathChosen.get(n-1);
		n2 = pathChosen.get(0);
		
		// cas ou c'est une arete deterministe --> il faut appliquer les penalites
		if(!graph_scenario.isEdgeStochastic(n1, n2)){
			cost = Math.abs(cost + graph_scenario.getTabAdja()[n1][n2] + penaliteLambda[n1][n2] + penaliteRo[n1][n2]/2 + penaliteRo[n1][n2]*areteDansSolution(solRef.getPathChosen(), n1, n2)); 
		}
		// cas ou c'est une arete stochastique --> pas de penalite
		else{
			cost+=graph_scenario.getTabAdja()[n1][n2];
		}
		
		return cost;
	}
	
	/**
	 * Initialise les attributs des penalites en (i,j) pour les aretes deterministes
	 * @param graphInitial
	 */
	public void initPenalite(Graph graphInitial){
		int n = graph_scenario.getNbNode();
		penaliteLambda = new double[n][n];
		penaliteRo = new double[n][n];
		int i,j;
		double coutMax = graph_scenario.getCoutMax();
		
		/**
		 * !! Les penalites sappliquent a toutes les aretes deterministes
		 * 		--> c peut etre que pour les aretes deterministes de la solution
		 */
		
		// initialisation de lambda et de ro
		for(i=0; i<n; i++){
			for(j=0; j<n; j++){
				// on veut (i,j) deterministe
				if(i!=j && !graph_scenario.getTabStoch()[i][j]){
					penaliteLambda[i][j] = 2*coutMax;
					penaliteRo[i][j] = graphInitial.getTabAdja()[i][j]/2;
				}
				// (i,j) n'est pas deterministe : on attribut -1 par defaut
				else{
					penaliteLambda[i][j] = -1;
					penaliteRo[i][j] = -1;
				}
			}
		}
	}

	public void calculPenalite(SolutionVNS solReference, int beta){
		int i;
		
		// Cette partie --> les penalites s'appliquent a l'ensemble des aretes deterministes du GRAPHE
		// it�ration sur lambda et ro
		int j;
		int n = graph_scenario.getNbNode();
		for(i=0; i<n; i++){
			for(j=0; j<n; j++){
				// on veut (i,j) deterministe
				if(i!=j && !graph_scenario.getTabStoch()[i][j]){
					penaliteLambda[i][j] = Math.abs(penaliteLambda[i][j] + penaliteRo[i][j]*Math.abs(areteDansSolution(pathChosen, i, j) - areteDansSolution(solReference.getPathChosen(), i, j)));
					penaliteRo[i][j] *= beta;
				}
			}
		}
		
		/**
		// Cette partie --> les penalites s'appliquent a l'ensemble des aretes deterministes de la SOLUTION
		// it�ration sur lambda et de ro
		int n1, n2;
		int n = solReference.getPathChosen().size();
		for(i=0; i<n-1; i++){
			n1 = solReference.getPathChosen().get(i);
			n2 = solReference.getPathChosen().get(i+1);
			// on veut (n1,n2) deterministe
			if(!graph_scenario.getTabStoch()[n1][n2]){
				penaliteLambda[n1][n2] = penaliteLambda[n1][n2] + penaliteRo[n1][n2]*(areteDansSolution(pathChosen, n1, n2) - areteDansSolution(solReference.getPathChosen(), n1, n2));
				penaliteRo[n1][n2] *= beta;
			}
		}
		n1 = solReference.getPathChosen().get(n-1);
		n2 = solReference.getPathChosen().get(0);
		if(!graph_scenario.getTabStoch()[n1][n2]){
			penaliteLambda[n1][n2] = penaliteLambda[n1][n2] + penaliteRo[n1][n2]*(areteDansSolution(pathChosen, n1, n2) - areteDansSolution(solReference.getPathChosen(), n1, n2));
			penaliteRo[n1][n2] *= beta;
		}
		
		*/
	}
	
	public ArrayList<Integer> getPathChosen() {
		return pathChosen;
	}

	public Graph getGraph_scenario() {
		return graph_scenario;
	}
	
	public void setPathCost(double cost){
		pathCost = cost;
	}
	
	public void setPathChosen(ArrayList<Integer> pathChosen){
		this.pathChosen = pathChosen;
		this.pathCost = calculPathCost();
	}
	
	public SolutionVNS clone(){
		return new SolutionVNS(graph_scenario, pathChosen, pathCost, penaliteLambda, penaliteRo);
		
	}
	
	public static int areteDansSolution(ArrayList<Integer> sol, int i, int j){
		if(sol.size() == 0)	return 0;
		//System.out.println("Sol = " + sol);
		for(int a=0; a<sol.size()-1; a++){
			if(sol.get(a) == i){
				if(sol.get(a+1) == j)	return 1;
				return 0;
			}
		}
		if(sol.get(sol.size()-1) == i && sol.get(0) == j){
			return 1;
		}
		return 0;
	}

	public void setSolution(SolutionVNS sol_scenario) {
		pathChosen = sol_scenario.getPathChosen();
		pathCost = sol_scenario.getPathCost();
		
	}

	public double[][] getPenaliteLambda() {
		return penaliteLambda;
	}

	public void setPenaliteLambda(double[][] penaliteLambda) {
		this.penaliteLambda = penaliteLambda;
	}

	public double[][] getPenaliteRo() {
		return penaliteRo;
	}

	public void setPenaliteRo(double[][] penaliteRo) {
		this.penaliteRo = penaliteRo;
	}
}
