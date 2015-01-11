package ps.project.tsp.vns;

import java.util.ArrayList;

import pne.project.tsp.beans.NodeCouple;

public class VNSStochastic extends VNSAbstract {

	public VNSStochastic(int Kmax) {
		super(Kmax);
	}
	
	public SolutionVNS getSolutionRef(){
		return getListSolutions().get(0);
	}
	
	public SolutionVNS getSolutionScenario(int k){
		return getListSolutions().get(k);
	}

	public void setSolutionScenario(int i, SolutionVNS sol_scenario) {
		// TODO Auto-generated method stub
		getSolutionScenario(i).setSolution(sol_scenario);
	}
	
	protected SolutionVNS k_transformation(SolutionVNS x, int k, ArrayList<ArrayList<NodeCouple>> listCombinaison){
		
		// liste des ar�tes interdites (contient les positions des ar�tes dans la solution x, et non les ar�tes
		// en elles-m�mes
		ArrayList<NodeCouple> combinaison = new ArrayList<NodeCouple>();

		int n = x.getPathChosen().size();	// taille du graphe
		int i, j;
		boolean nodePositionPresent[] = new boolean[n];		// permet d'�viter d'avoir des ar�tes d�j� mises
		
		/**
		 * On rep�te tant qu'on a pas trouver une meilleure solution que x ou alors qu'on ait tester 
		 * toutes les combinaisons
		 */
	
		do{
			// Initialisation de nodePositionPresent
			for(i=0; i<n; i++){
				nodePositionPresent[i] = false;
			}
			
			/**
			 * Tirer au hasard une combinaison parmi toutes les combinaisons
			 */
			int nbCombinaison = listCombinaison.size();
			j = (int) (Math.random() * nbCombinaison);
			combinaison = (ArrayList<NodeCouple>) listCombinaison.get(j).clone();
			listCombinaison.remove(j);
		
			SolutionVNS s = new SolutionVNS(x.getGraph_scenario());
			s.setPenaliteLambda(x.getPenaliteLambda().clone());
			s.setPenaliteRo(x.getPenaliteRo().clone());
			
			/**
			 * Construction de la nouvelle solution
			 */
	
			// on avance de la 1ere jusqu'� la 1�re ar�te interdite dans la solution x
			i = 0;
			while(i<combinaison.get(0).getN1()){
				s.getPathChosen().add(x.getPathChosen().get(i));
				i++;
			}
			
			i=0;
			j=k-1;
			int pos_val1, pos_val2;
			int changement;	// permet de savoir s'il faut incr�menter ou d�cr�menter
			while(i<=(k/2)+1){
				// Dans le cas o� on arrive � la fin et que k est impair
				if(i == j && k%2 != 0){
					s.getPathChosen().add(x.getPathChosen().get(combinaison.get(i).getN1()));
					
					// Enregistrement de la position Ai
					pos_val1 = combinaison.get(i).getN1();
					
					// Enregistrement de la position Bk
					pos_val2 = combinaison.get(k-1).getN2();
					
					// Ajout de Bk inclu jusqu'� la fin de la solution x qu'il reste
					if(pos_val2 != 0){
						for(i=pos_val2; i<x.getPathChosen().size(); i++){
							s.getPathChosen().add(x.getPathChosen().get(i));
						}
					}
					//System.out.println("s = " + s.getPathChosen());					
					break;
				}
				
				// ajout de Ai
				s.getPathChosen().add(x.getPathChosen().get(combinaison.get(i).getN1()));
				
				// Enregistrement des positions de Aj et Bj-1 car il faut enregistrer toutes les valeurs
				// comprises dedans
				pos_val1 = combinaison.get(j).getN1();
				pos_val2 = combinaison.get(j-1).getN2();
				
				// test pour savoir s'il faut incr�menter ou d�cr�menter pour aller de Aj � Bj-1
				if(pos_val1>pos_val2){
					changement = -1;
				}
				else{
					changement = 1;
				}
				
				// ajout de Aj inclu jusqu'� Bj-1 exclu
				while(pos_val1!=pos_val2){
					s.getPathChosen().add(x.getPathChosen().get(pos_val1));
					pos_val1+=changement;
				}

				// ajout de Bj-1
				s.getPathChosen().add(x.getPathChosen().get(pos_val2));

				// Enregistrement de Bi
				pos_val1 = combinaison.get(i).getN2();
				
				// Dans le cas ou on arrive � la fin et que k est pair
				if (i == (k / 2) - 1 && k % 2 == 0) {
					// Enregistrement de Bk
					pos_val2 = combinaison.get(k-1).getN2();
				
					// Ajout de Bk inclu jusqu'� la fin de la solution x qu'il reste
					if(pos_val2 != 0){
						for (i = pos_val2; i < x.getPathChosen().size(); i++) {
							s.getPathChosen().add(x.getPathChosen().get(i));
						}
					}
					break;
				}
				
				// Rajouter de Bi-> jusqu'� Ai+1 qui va �tre l'�tape suivante

				// Enregistrement de Ai+1
				pos_val2 = combinaison.get(i+1).getN1();
				
				// Ajout de Bi inclu jusqu'� Ai+1 exclu
				// Remarque : on sait que pos_val1 < pos_val2
				while (pos_val1 != pos_val2) {
					s.getPathChosen().add(x.getPathChosen().get(pos_val1));
					pos_val1++;
				}

				// on passe � "l'�tape suivante"
				i++;
				j--;
			}
			
			/**
			 * Calcul du cout total de la nouvelle solution
			 */

			s.setPathCost(s.calculPathCostWithPenalty(getSolutionRef()));
			
			/**
			 * Si la solution s recherch�e est meilleure que la solution initiale x, on renvoie s
			 */
			if(s.getPathCost()<x.getPathCost()){
				System.out.println("Changement ! pour k="+k + " et cout="+s.getPathCost());
				System.out.println("------combinaison choisie = " + combinaison);
				return s;
			}
			
		} while(listCombinaison.size()>0); // la condition d'arret : qd tt a �t� test�
		
		/**
		 * Si aucune meilleure solution n'a �t� trouv�, on renvoie x
		 */
		return x;
	}


}
