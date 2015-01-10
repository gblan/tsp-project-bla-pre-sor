package pne.project.tsp.managers;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.DoubleParam;

import java.util.ArrayList;

import javax.swing.JWindow;

import pne.project.tsp.beans.Graph;
import pne.project.tsp.beans.NodeCouple;
import pne.project.tsp.utils.Stats;
import ps.project.tsp.vns.SolutionVNS;
import ps.project.tsp.vns.VNSAbstract;

public class GraphManager {
	private double solutionValue;
	private int resolutionDuration;

	public GraphManager() {
	}

	/**
	 * PS
	 */
	
	/**
	 * 
	 * @param g
	 * @param aleas : pourcentage d'aretes deterministes
	 */
	public ArrayList<Integer> resolutionTSP_vns(Graph g, int aleas, int nbScenario, int Kmax){
		double tmax = 100;		// ???
		if(aleas < 0){
			aleas = 0;
		}
		if(aleas > 100){
			aleas = 100;
		}
		
		// Initialisation de l'attribut qui va nous servir a enregistrer Glouton
		SolutionVNS solutionInitiale = new SolutionVNS(g);	// on passe en parametre le graphe de base
		
		// Enregistrement de de la solution gloutonne
		solutionInitiale.setPathChosen(solutionInitiale.gloutonAlgorithm());
		
		System.out.println("GM --> solInit = " + solutionInitiale.getPathChosen());
		
		// D�terministe
		if(aleas == 100){
			// R�solution avec la m�thode VNS du probl�me deterministe
			VNSAbstract vns = new VNSAbstract(Kmax);
			vns.getListSolutions().add(solutionInitiale);
			vns.vnsAlgorithm(solutionInitiale, tmax);
			
		}
		// Stochastique
		else{
			// Initialisation des ar�tes d�terministes
			initAretesDeterministes(g, aleas);
//			int a, b;
//			System.out.println("Apr�s initDeterminist :");
//			for( a=0; a<g.getNbNode(); a++){
//				for( b=0; b<g.getNbNode(); b++){
//					if(g.isEdgeStochastic(a, b)){
//						//System.out.println("(" + a + ", " + b + ") = S");
//					}
//					else{
//						System.out.println("(" + a + ", " + b + ") = D");
//					}
//				}
//			}
			
			// Calcul de l'�cart type des ar�tes stochastiques
			double ecartType = Stats.ecartType(g);
			
		//	System.out.println("ecartType=" + ecartType);
		
			//Graph g_s = genereScenario(g, ecartType);
			
//			System.out.println("Apr�s g�n�ration du sc�nario :");
//			for( a=0; a<g.getNbNode(); a++){
//				for( b=0; b<g.getNbNode(); b++){
//					if(g.isEdgeStochastic(a, b)){
//						System.out.println("(" + a + ", " + b + ") = S  |  g=" + g.getTabAdja()[a][b] + " - g_s=" + g_s.getTabAdja()[a][b]);
//					}
//					else{
//						System.out.println("(" + a + ", " + b + ") = D  |  g=" + g.getTabAdja()[a][b] + " - g_s=" + g_s.getTabAdja()[a][b]);
//					}
//				}
//			}
			
			
			
			/**
			 * Remarque : � enlever? car cela correspond a la r�solution du graphe deterministe
			 */
			// R�solution avec la m�thode VNS
			VNSAbstract vns = new VNSAbstract(Kmax);
			vns.getListSolutions().add(solutionInitiale);
			vns.vnsAlgorithm(solutionInitiale, tmax);
			
			SolutionVNS sol;
			Graph graph_scenario;
			for(int i=0; i<nbScenario; i++){
				
				// g�n�ration d'un sc�nario
				graph_scenario = genereScenario(g, ecartType);
				
				// initialisation d'un attribut solutionVNS qui contient le graphe du scenario i
				sol = new SolutionVNS(graph_scenario);
				
				// initialisation de la solution a l'aide de glouton appliqu� au graphe du scenario i
				sol.setPathChosen(sol.gloutonAlgorithm());
				
				// ajout dans VNS du scenario i
				vns.getListSolutions().add(sol);
				
				// r�solution du scenario i
				vns.vnsAlgorithm(sol, tmax);
		
			}
		}
		
		return solutionInitiale.getPathChosen();	// a modifier
	}
	
	/** Permet de g�n�rer un graphe correspondant a un scenario
	 * 
	 * @param g
	 * @param ecartType
	 * @return le graphe g�n�r�
	 */
	public Graph genereScenario(Graph g, double ecartType){
		double cij;
		//Graph graph_scenario = (Graph) g.clone();
		int nbNode = g.getNbNode();
		double tabGraph[][] = new double[nbNode][nbNode];
		
		for(int i=0; i<nbNode; i++){
			for(int j=0; j<nbNode; j++){
				// Dans le cas ou (i,j) est stochastique = on lui attribut une valeur
				if(i!=j && g.isEdgeStochastic(i, j)){
					cij = g.getTabAdja()[i][j];
					tabGraph[i][j] = Stats.getRandValueBetween(cij-3*ecartType, cij+3*ecartType);
				}
				else{
					tabGraph[i][j] = g.getTabAdja()[i][j];
				}
			}
		}
		return new Graph(tabGraph, nbNode, g.getTabStoch().clone(), g.getPercentageDeterminist());
	}
	
	/**
	 * D�termine de fa�on al�atoire les ar�tes d�terministes du graphe
	 * @param g
	 * @param aleas : pourcentage des ar�tes d�terministes (compris entre 0 et 100
	 */
	private void initAretesDeterministes(Graph g, int aleas) {
		int nbNodeDeterminist = g.getNbNode()*aleas/100;
		int edgeRandom;
		ArrayList<NodeCouple> listEdge = allEdge(g);
		
		// on met toutes les aretes en stochastique pour initialiser les aretes deterministes
		g.initTabStoch(true);
		
		for(int i=0; i<nbNodeDeterminist; i++){
			edgeRandom = (int) (Math.random()*listEdge.size());
			g.getTabStoch()[listEdge.get(edgeRandom).getN1()][listEdge.get(edgeRandom).getN2()] = false;
			listEdge.remove(edgeRandom);
		}
	}
	
	/** M�thode utile pour le tirage au sort des ar�tes d�terministes
	 *
	 * @param g
	 * @return une liste de toutes les ar�tes du graphe
	 */
	public ArrayList<NodeCouple> allEdge(Graph g){
		ArrayList<NodeCouple> listEdge = new ArrayList<NodeCouple>();
		for(int i=0; i<g.getNbNode(); i++){
			for(int j=0; j<g.getNbNode(); j++){
				if(i!=j && g.getTabAdja()[i][j] > 0){
					listEdge.add(new NodeCouple(i, j));
				}
			}
		}
		return listEdge;
	}

	
	
	// il faut r�soudre le graphe charg� (appel de VNS deterministe)
	
	// si le X% = 0 ==> fin
	
	// sinon
		// il faut tirer au sort les ar�tes deterministes
	
		// g�n�ration des K scenarios
	
			// calcul ecart type et tirage au sort des valeurs
	
			// VNS stochastique + p�nalit�
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * PNE
	 */
	
	/**
	 * Method called to write Linear program with graph in param and write it in
	 * file in param.
	 * 
	 * @param i_graph
	 * @param o_pathFileToExport
	 * @param objectiveValue
	 * @return
	 */
	public int[] writeLinearProgram(Graph i_graph, String o_pathModelToExport,
			String o_pathFileToExport) {
		int[] tabResult = new int[i_graph.getNbNode()];
		IloCplex cplex;
		try {
			cplex = new IloCplex();
			IloNumVar[][] x = new IloNumVar[i_graph.getNbNode()][];

			/* Variables Initialisation */
			String[][] varName = new String[i_graph.getNbNode()][i_graph
					.getNbNode()];
			initVarNameTab(i_graph.getNbNode(), varName);

			for (int i = 0; i < i_graph.getNbNode(); i++) {
				x[i] = cplex.boolVarArray(i_graph.getNbNode(), varName[i]);
			}

			/* Permet de ne renvoyer que des 1 et 0 */
			cplex.setParam(DoubleParam.EpInt, 0.0);
			if(i_graph.getNbNode()>=100){
//				cplex.setParam(IloCplex.IntParam.TimeLimit, 0.4);
				cplex.setParam(IloCplex.DoubleParam.TreLim, 0.4);
				cplex.setParam(IloCplex.IntParam.IntSolLim, 1);
//				cplex.setParam(IloCplex.IntParam.Threads, 1);
			}

			setObjectiveFonction(i_graph, cplex, x);
			setConstraintOuterEdge(i_graph, cplex, x);
			setConstraintInnerEdge(i_graph, cplex, x);

			long startTime = System.nanoTime();

			cplex.setOut(null);
			cplex.solve();

			for (int i = 0; i < i_graph.getNbNode(); i++) {
				tabResult[i] = searchIndiceJ(cplex.getValues(x[i]), i_graph.getNbNode());
			}
			while (addNewSubCycleConstraint(i_graph.getNbNode(), cplex, x, tabResult)) {
				cplex.setOut(null);
				cplex.solve();
				// Enregistrement du r�sultat dans tabResult
				tabResult = new int[i_graph.getNbNode()]; // pas besoin?
				for (int i = 0; i < i_graph.getNbNode(); i++) {
					tabResult[i] = searchIndiceJ(cplex.getValues(x[i]),
							i_graph.getNbNode());
				}
			}
			long stopTime = System.nanoTime();
			//System.out.println(((stopTime - startTime) / 1000000000)+ " seconds");
			this.resolutionDuration = (int) ((stopTime - startTime) / 1000000000);
			//System.out.println("valeur chemin optimal : " + cplex.getObjValue());
			solutionValue = cplex.getObjValue();
			cplex.exportModel(o_pathModelToExport);
			cplex.writeSolution(o_pathFileToExport);
			cplex.end();
		} catch (IloException e) {
			e.printStackTrace();
		}
		return tabResult;
	}

	
	
	/**
	 * 
	 * @param nbNode
	 * @param varName
	 */
	private static void initVarNameTab(int nbNode, String[][] varName) {
		for (int i = 0; i < nbNode; i++) {
			for (int j = 0; j < nbNode; j++) {
				varName[i][j] = "x" + i + ";" + j;
			}
		}
	}

	/**
	 * Write objective function
	 * 
	 * @param graph
	 * @param cplex
	 * @param x
	 */
	private static void setObjectiveFonction(Graph graph, IloCplex cplex,
			IloNumVar[][] x) {
		IloLinearNumExpr objectiveFunction;
		try {
			objectiveFunction = cplex.linearNumExpr();

			for (int i = 0; i < graph.getNbNode(); i++) {
				for (int j = 0; j < graph.getNbNode(); j++) {
					objectiveFunction.addTerm(graph.getTabAdja()[i][j], x[i][j]);
				}
			}
			cplex.addMinimize(objectiveFunction);
		} catch (IloException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Write the first constraint : each node have only one edge going out
	 * 
	 * @param graph
	 * @param cplex
	 * @param x
	 */
	private static void setConstraintOuterEdge(Graph graph, IloCplex cplex,
			IloNumVar[][] x) {
		try {
			for (int i = 0; i < graph.getNbNode(); i++) {
				IloLinearNumExpr expr = cplex.linearNumExpr();
				for (int j = 0; j < graph.getNbNode(); j++) {
					if (i != j) {
						expr.addTerm(1.0, x[i][j]);
					}
				}
				cplex.addEq(1.0, expr); 
			}

		} catch (IloException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Write the second constraint : each node have only one edge going in
	 * 
	 * @param graph
	 * @param cplex
	 * @param x
	 */
	private static void setConstraintInnerEdge(Graph graph, IloCplex cplex,
			IloNumVar[][] x) {
		try {
			for (int j = 0; j < graph.getNbNode(); j++) {
				IloLinearNumExpr expr = cplex.linearNumExpr();
				for (int i = 0; i < graph.getNbNode(); i++) {
					if (i != j) {
						expr.addTerm(1.0, x[i][j]);
					}
				}
				cplex.addEq(1.0, expr);
			}
		} catch (IloException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * add subcycles (if they exists)
	 * 
	 * @param nbNode
	 * @param cplex
	 * @param x
	 * @param tabResult
	 * @return true if it exists subCycles
	 */
	public static boolean addNewSubCycleConstraint(int nbNode, IloCplex cplex,
			IloNumVar[][] x, int[] tabResult) {
		int cpt = 0; // le nb de noeud dans la recherche d'un sous tours
		int i_saved = 0;
		int i = i_saved;
		int j;
		boolean hasSubCycle = false;

		ArrayList<NodeCouple> listVariables = new ArrayList<NodeCouple>();

		boolean[] nodeVisited = new boolean[nbNode];
		boolean[] nodeVisitedInSubCycle = new boolean[nbNode];
		for (int k = 0; k < nbNode; k++) {
			nodeVisited[k] = false;
			nodeVisitedInSubCycle[k] = false;
		}

		while (cpt < nbNode && i_saved != -1) {
			nodeVisited[i] = true;
			nodeVisitedInSubCycle[i] = true;
			j = tabResult[i];

			// si j = -1, ca veut dire que tous les noeuds xij pour j=0,...,n-1
			// sont = � 0
			if (j == -1) {
				return true;
			} else {
				cpt++;
				listVariables.add(new NodeCouple(i, j));
				// dans le cas ou on rencontre un sous-tour (cas ou on revient
				// sur le noeud i_saved)
				if (nodeVisitedInSubCycle[j] && cpt < nbNode) {
					try {
						// ajout de la contrainte
						IloLinearNumExpr expr = cplex.linearNumExpr();
						int pos = getNodeInList(listVariables, j);
						while (pos < cpt) {
							expr.addTerm(1.0, x[listVariables.get(pos).getN1()][listVariables.get(pos).getN2()]);
							pos++;
						}

						cplex.addLe(expr, cpt - 1);

						// mise a jour des variables
						cpt = 0;
						i_saved = nextNode(nodeVisited, nbNode);
						i = i_saved;
						listVariables.clear();
						hasSubCycle = true;
						for (int l = 0; l < nbNode; l++) {
							nodeVisitedInSubCycle[l] = false;
						}

					} catch (IloException e) {
						e.printStackTrace();
					}
				}

				else {
					i = j;
				}
			}
		}
		return hasSubCycle;
	}

	private static int getNodeInList(ArrayList<NodeCouple> listVariables, int j) {
		int n = listVariables.size();
		for (int i = 0; i < n; i++) {
			if (listVariables.get(i).getN1() == j) {
				return i;
			}
		}
		return -1; // error
	}

	/**
	 * On connait l'indice i, on cherche l'indice j tel que resultat[i][j] = 1
	 * @param tabResult
	 * @param nbNode
	 * @return
	 */
	public static int searchIndiceJ(double[] tabResult, int nbNode) {
		for (int j = 0; j < nbNode; j++) {
			if (tabResult[j] != 0) {
				return j;
			}
		}
		return -1; // error
	}

	/**
	 * 
	 * @param nodeVisite
	 * @param nbNode
	 * @return
	 */
	public static int nextNode(boolean[] nodeVisite, int nbNode) {
		for (int i = 0; i < nbNode; i++) {
			if (!nodeVisite[i]) {
				return i;
			}
		}
		return -1; // tous les noeuds ont �t� visit�
	}


	public double getSolutionValue() {
		return solutionValue;
	}

	public int getResolutionDuration() {
		return resolutionDuration;
	}

}
