package pne.project.tsp.managers;

import java.util.ArrayList;
import java.util.HashMap;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;


import pne.project.tsp.beans.Graph;

public class GraphManager {
	
	

	/**
	 * Method called to write Linear program with graph in param and write it in file in param.
	 * @param i_graph
	 * @param o_pathFileToExport
	 */
	public static void writeLinearProgram(Graph i_graph,String o_pathModelToExport,String o_pathFileToExport) {
		IloCplex cplex;
		try {
			cplex = new IloCplex();

			IloNumVar[][] x = new IloNumVar[i_graph.getNbNode()][];
			
			/* Variables Initialisation*/
			String[][] varName = new String[i_graph.getNbNode()][i_graph.getNbNode()];
			initVarNameTab(i_graph.getNbNode(),varName);

			for (int i = 0; i < i_graph.getNbNode(); i++) {
				x[i] = cplex.boolVarArray(i_graph.getNbNode(),varName[i]);
			}
			
			/**
			 * QU'EST CE QUE FAIT : numVarArray?
			 */
			IloNumVar[] u = cplex.numVarArray(i_graph.getNbNode(), 0, Double.MAX_VALUE);

			setObjectiveFonction(i_graph, cplex, x);
			setConstraintOuterEdge(i_graph, cplex, x);
			setConstraintInnerEdge(i_graph, cplex, x);
			//setConstraintSubCycle(i_graph, cplex,x,u);
					
			cplex.exportModel(o_pathModelToExport);
			
			cplex.solve();
			
//			cplex.writeSolution(o_pathFileToExport);
			
			/* RESULTS*/
			double[][] tabResult = new double[i_graph.getNbNode()][i_graph.getNbNode()];
			for(int i=0;i<i_graph.getNbNode();i++){
				tabResult[i] = cplex.getValues(x[i]);
			}			
			
			System.out.println("Avant la m�thode des plans coupants : ");
			for(int i=0;i<i_graph.getNbNode();i++){
				for(int j=0;j<i_graph.getNbNode();j++){
					if(tabResult[i][j]==1){
						System.out.println("ar�te "+x[i][j]);
					}
				}			
			}
			
			int cpt=0;
			while(cpt < 2 && addNewSubCycleConstraint(i_graph.getNbNode(), cplex, x, tabResult)){
				cpt++;
				cplex.exportModel(o_pathModelToExport);
				cplex.solve();

				// Enregistrement du r�sultat dans tabResult
				tabResult = new double[i_graph.getNbNode()][i_graph.getNbNode()];
				for(int i=0;i<i_graph.getNbNode();i++){
					tabResult[i] = cplex.getValues(x[i]);
				}			
				
				
				// Affichage
				System.out.println("RESULTAT FINAL : ");
				for(int i=0;i<i_graph.getNbNode();i++){
					for(int j=0;j<i_graph.getNbNode();j++){
						if(tabResult[i][j]==1){
							System.out.println("ar�te "+x[i][j]);
						}
					}			
				}
				
				
			}
		
			
			
			cplex.end();
			System.out.println("cpt=" + cpt);
		} catch (IloException e) {
			e.printStackTrace();
		}
	}
	
	
	
	// renvoie vrai si il existe des sous tours, faux sinon
	public static boolean addNewSubCycleConstraint(int nbNode,IloCplex cplex,IloNumVar[][] x, double[][] tabResult){
		int cpt = 0;	// le nb de noeud dans la recherche d'un sous tours
		int i_saved = 0;
		int i = i_saved;
		int j;
		int indice_j;
		boolean hasSubCycle = false;
		
		// de type <i, j> pour avoir une liste [(i1, j1), (i2, j2), ...]
		HashMap<Integer, Integer> listVariables = new HashMap<Integer, Integer>();
		
		while(cpt<nbNode && i_saved<nbNode){
			j = searchIndiceJ(tabResult, i, nbNode);
			//System.out.println("Pour i_saved="+i_saved+" | ("+i+","+j+") et cpt="+cpt);
			
			// si j = -1, ca veut dire que tous les noeuds xij pour j=0,...,n-1 sont = � 0
			if(j == -1){
				i_saved++;
				i = i_saved;
			}
			else{
				cpt++;
				listVariables.put(i, j);
				// dans le cas ou on rencontre un sous-tour
				if(j == i_saved && cpt<nbNode){
					try {
						//System.out.println("HashMap = " + listVariables);
						//System.out.print("Sous-tours! : ");
						// ajout de la contrainte
						IloLinearNumExpr expr = cplex.linearNumExpr();
						for(Integer indice_i : listVariables.keySet()){
							indice_j = listVariables.get(indice_i);
							//System.out.print(" (" + indice_i + "," + indice_j + ")");
							
							expr.addTerm(1.0, x[indice_i][indice_j]);
							// on ne regarde plus les variables comprises dans le sous-tour
							tabResult[indice_i][indice_j] = 0;
						}
						//System.out.println("");
						cplex.addLe(expr, cpt-1);
						
						// mise a jour des variables
						cpt=0;
						i_saved++;
						i = i_saved;
						listVariables.clear();
						hasSubCycle = true;
						
					} catch (IloException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				// dans le cas ou on ne rencontre pas de sous-tour : on continue notre recherche
				else{
					i = j;
				}
			}
		}
		
		if(hasSubCycle == false){
			System.out.println("Solution optimale !");
			System.out.print("Chemin = ");
			for(Integer indice_i : listVariables.keySet()){
				indice_j = listVariables.get(indice_i);
				System.out.print("(" + indice_i + "," + indice_j + ") ; ");
			}
			System.out.println("");
		}
		return hasSubCycle;
	}
	
	// On connait l'indice i, on cherche l'indice j tel que resultat[i][j] = 1
	public static int searchIndiceJ(double[][] tabResult, int indiceI, int nbNode){
		for(int j=0; j<nbNode; j++){
			if(tabResult[indiceI][j] == 1){
				return j;
			}
		}
		return -1;	// error
	}

	private static void initVarNameTab(int nbNode, String[][] varName) {
		for(int i=0;i<nbNode;i++){
			for(int j=0;j<nbNode;j++){
				varName[i][j]="x"+i+";"+j;
			}
		}
	}

	/**
	 * Write objective function
	 * @param graph
	 * @param cplex
	 * @param x
	 */
	private static void setObjectiveFonction(Graph graph, IloCplex cplex, IloNumVar[][] x) {
		IloLinearNumExpr objectiveFunction;
		try {
			objectiveFunction = cplex.linearNumExpr();

			for (int i = 0; i < graph.getNbNode(); i++) {
				for (int j = 0; j < graph.getNbNode(); j++) {
						//System.out.println("(" + graph.getTabAdja()[i][j] + ", " + x[i][j] + ")");
						objectiveFunction.addTerm(graph.getTabAdja()[i][j],	x[i][j]);
				}
			}

			cplex.addMinimize(objectiveFunction);
		} catch (IloException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Write the first constraint : each node have only one edge going out
	 * @param graph
	 * @param cplex
	 * @param x
	 */
	private static void setConstraintOuterEdge(Graph graph, IloCplex cplex, IloNumVar[][] x) {
		try {
			for (int i = 0; i < graph.getNbNode(); i++) {
				IloLinearNumExpr expr = cplex.linearNumExpr();
				for (int j = 0; j < graph.getNbNode(); j++) {
					if (i != j) {
						expr.addTerm(1.0, x[i][j]);	// somme
					}
				}
				cplex.addEq(1.0, expr);	// l'�galit� = 1 (d'ou addEq, et le 1.0 car on dit que =1)
				
			}
		
		} catch (IloException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Write the second constraint : each node have only one edge going in
	 * @param graph
	 * @param cplex
	 * @param x
	 */
	private static void setConstraintInnerEdge(Graph graph, IloCplex cplex, IloNumVar[][] x) {
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
	
	
	
	
	// A REGARDER !

	/**
	 * Write the third constraint : the path chozen does'nt contains sub-cycle in it.
	 * @param graph
	 * @param cplex
	 * @param x
	 * @param u 
	 */
	
	private static void setConstraintSubCycle(Graph graph, IloCplex cplex, IloNumVar[][] x, IloNumVar[] u) {
		try {
			for (int i = 1; i < graph.getNbNode(); i++) {
				for (int j = 1; j < graph.getNbNode(); j++) {
					if (i != j) {
						IloLinearNumExpr expr = cplex.linearNumExpr();
						expr.addTerm(1.0, u[i]);
						expr.addTerm(-1.0, u[j]);
						expr.addTerm(graph.getNbNode()-1, x[i][j]);
						cplex.addLe(expr, graph.getNbNode()-2);
					}
				}
			}
		} catch (IloException e) {
			e.printStackTrace();
		}	
	}
	
	
	
	
	
	/**
	 * Return true if "i" is the indice i of a variable in tabConst
	 * 
	 * @param tabConst
	 * @param i
	 * @return
	 */
//	private static boolean contains_i(ArrayList<IloNumVar> tabConst, int i){
//		for(IloNumVar x : tabConst){
//			if(Integer.parseInt(x.getName().substring(1).split(";")[0]) == i){
//				return true;
//			}
//		}
//		return false;
//	}
	
	
	/** Contrainte n�3 (sous-tours) : on va regarder pour chaque xij tous les sous-tours possibles 
	 * 								  en partant de cette variable
	 * 		
	 * 
	 * @param graph
	 * @param cplex
	 * @param x
	 * 
	 * */
	 
//	private static void setConstraintSubCycle(Graph graph, IloCplex cplex, IloNumVar[][] x) {
//		int i,j;
//		ArrayList<IloNumVar> tabConst = new ArrayList<IloNumVar>();
//		for(i=0;i<graph.getNbNode()-1;i++){
//			for(j=i+1;j<graph.getNbNode();j++){
//				tabConst.clear();	// on efface la liste des variables car on passe a un indice qui n'a rien a voir avec les indices precedents
//				setConstraintSubCycleRecursif(graph,cplex,tabConst,x,i,j,i);
//				
//			}
//		}
//		
//	}

	// init_I permet de connaitre quel est le dernier indice i
//	private static void setConstraintSubCycleRecursif(Graph graph, IloCplex cplex, 
//			ArrayList<IloNumVar> tabConst, IloNumVar[][] x, int i, int j, int init_I) {
//				
//		//System.out.println("On regarde (i=" + i + ", j=" + j + ")");
//		try{
//			if(graph.getTabAdja()[i][j]!=0 && graph.getTabAdja()[j][init_I]!=0 && (tabConst.size()+2)<graph.getNbNode()){
//				IloLinearNumExpr expr = cplex.linearNumExpr();
//				for(IloNumVar strExpr : tabConst){
//					expr.addTerm(1.0, strExpr);	
//				}
//				expr.addTerm(1.0, x[i][j]);
//				expr.addTerm(1.0, x[j][init_I]);
//				
//				System.out.println("l'expression de la contrainte : " + expr + "<=" + (tabConst.size()+1));
//				cplex.addLe(expr, (tabConst.size()+1));
//				
//				tabConst.add(x[i][j]);
////				System.out.println("tabConst="+tabConst);
//				i=j;
//				for(j=init_I+1;j<graph.getNbNode();j++){
//					
//					if((tabConst.size()+2)>=graph.getNbNode()){
////						System.out.println("On est ds le break pour (i=" + i + ", j=" + j +") et taille tabConst=" + (tabConst.size()) + " et tabConst = " + tabConst);
//						break;
//					}
//					
//					if(contains_i(tabConst, j) == false && j!=i){
//						setConstraintSubCycleRecursif(graph,cplex,tabConst,x,i,j,init_I);
//						//System.out.println("avant remove1 pour (i=" + i + ", j=" + j + ": " + tabConst);
//						//tabConst.remove((tabConst.size()-1));
//						//System.out.println("apres remove1 pour (i=" + i + ", j=" + j + ") : " + tabConst);
//					}
//					
//				}
////				System.out.println("avant remove2: " + tabConst);
//				tabConst.remove((tabConst.size()-1));
////				System.out.println("apres remove2 pour (i=" + i + ", j=" + j + ") : " + tabConst);
//
//			}
//		}catch(IloException e){
//			
//		}
//	}
	
	
	/*
	 Ancienne version de l'algo contrainte 3
	 
	private static void setConstraintSubCycle(Graph graph, IloCplex cplex, IloNumVar[][] x) {
		int i,j;
		ArrayList<IloNumVar> tabConst = new ArrayList<IloNumVar>();
		for(i=1;i<=graph.getNbNode();i++){
			for(j=i+1;i<graph.getNbNode();j++){
				
				tabConst.clear();
				setConstraintSubCycleRecursif(graph,cplex,tabConst,x,i,j,i,2);
				
			}
		}
		
	}
	
	private static void setConstraintSubCycleRecursif(Graph graph, IloCplex cplex, 
			ArrayList<IloNumVar> tabConst, IloNumVar[][] x, int i, int j, int init_I, int nbNodeST) {
		try{
			if(graph.getTabAdja()[i][j]!=0 && graph.getTabAdja()[j][init_I]!=0 && nbNodeST<graph.getNbNode()){
				IloLinearNumExpr expr = cplex.linearNumExpr();
				for(IloNumVar strExpr : tabConst){
					expr.addTerm(1.0, strExpr);	
				}
				expr.addTerm(1.0, x[i][j]);
				expr.addTerm(1.0, x[j][init_I]);
				
				System.out.println(expr + "<=" + (nbNodeST-1));
				cplex.addLe(expr, nbNodeST-1);
				
				tabConst.add(x[i][j]);
				nbNodeST++;
				i=j;
				for(j=1;j<=graph.getNbNode();j++){
					
					if(nbNodeST>=graph.getNbNode()){

						break;
					}
					
					if(j!=i && j > init_I){
						setConstraintSubCycleRecursif(graph,cplex,tabConst,x,i,j,init_I,nbNodeST);
						System.out.println(tabConst);

						tabConst.remove(tabConst.size()-1);
					}
					
				}
				System.out.println(tabConst);
				tabConst.remove(tabConst.size()-1);

			}
		}catch(IloException e){
			
		}
	}
	*/


}