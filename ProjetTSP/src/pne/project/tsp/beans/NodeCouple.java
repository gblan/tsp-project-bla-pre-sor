package pne.project.tsp.beans;

import java.util.ArrayList;

/**
 * 
 * Cette classe permet de repr�senter un couple de noeud (i, j) Utile lors de la
 * sauvegarde des r�sultats renvoy�s par CPLEX
 *
 */

public class NodeCouple implements Comparable<NodeCouple> {
	private int n1;
	private int n2;
	private double costEdge;

	/**
	 * Constructor used in sub-tour method and VNS algorithm 
	 * @param n1
	 * @param n2
	 */
	public NodeCouple(int n1, int n2) {
		this.n1 = n1;
		this.n2 = n2;
	}
	
	/**
	 * Constructor used in glouton algorithm
	 * @param node1
	 * @param node2
	 * @param costEdge
	 */
	public NodeCouple(int node1, int node2, double costEdge) {
		this.n1 = node1;
		this.n2 = node2;
		this.costEdge = costEdge;
	}

	/**
	 * Permet de savoir si dans une liste de NodeCouple, celle ci contient un node couple dont elle a
	 * le meme n1 pass� en parametre
	 * @param list
	 * @param n1
	 * @return la position du nodecouple dans la liste s'il contient n1, -1 si non
	 */
	public static int listContainsN1(ArrayList<NodeCouple> list, int n1){
		int position = 0;
		for(NodeCouple nc : list){
			if(nc.getN1() == n1){
				return position;
			}
			position++;
		}
		return -1;
	}
	
	/**
	 * Permet de savoir si dans une liste de NodeCouple, celle ci contient un node couple dont elle a
	 * le meme n2 pass� en parametre
	 * @param list
	 * @param n2
	 * @return la position du nodecouple dans la liste s'il contient n2, -1 si non
	 */
	public static int listContainsN2(ArrayList<NodeCouple> list, int n2){
		int position = 0;
		for(NodeCouple nc : list){
			if(nc.getN2() == n2){
				return position;
			}
			position++;
		}
		return -1;
	}
	
	public int getN1() {
		return n1;
	}

	public void setN1(int n1) {
		this.n1 = n1;
	}

	public int getN2() {
		return n2;
	}

	public void setN2(int n2) {
		this.n2 = n2;
	}
	
	public double getCostEdge() {
		return costEdge;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result += prime * result + n1;
		result += prime * result + n2;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NodeCouple other = (NodeCouple) obj;
		if (n1 != other.n1)
			return false;
		if (n2 != other.n2)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "NodeCouple [" + n1 + ", " + n2 + "]";
	}

	@Override
	public int compareTo(NodeCouple nc) {
		// TODO Auto-generated method stub
		if (n1 < nc.getN1()) {
			return -1;
		} else if (n1 == nc.getN1()) {
			return 0;
		}
		return 1;
	}

}
