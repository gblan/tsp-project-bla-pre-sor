package pne.project.tsp.controler;

import pne.project.tsp.view.MainView;

public class MainControler {

	private MainView mv;

	public MainControler(MainView mv) {
		this.mv = mv;
	}
	
	/**
	 * Ajouter une m�thode qui permet de g�rer le cas o� on clique sur 
	 *     - Cr�er son PVC
	 *     - Charger un PVC
	 *     - Quitter
	 */

	public MainView getMv() {
		return mv;
	}

	public void setMv(MainView mv) {
		this.mv = mv;
	}
}
