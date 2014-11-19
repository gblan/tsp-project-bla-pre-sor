package pne.project.tsp.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File ;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog ;
import javax.swing.JFrame ;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * @author Nicolas Roussel (roussel@lri.fr)
 *
 */
public class Choix_fichier extends JDialog {
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * D�claration des diff�rentes variables
	 */
	
	// Correspond au r�pertoire
    private String directory = null ;
    
    // Correspond au fichier
    private String file = null ;
    
    // Correspond � une "boite" qui permet d'afficher un menu d�roulant
    private JComboBox combo =  new JComboBox();
    
    // Correspond � ce qu'il y a dans un item de la combobox
    private JList list = new JList();
    
    // Scroll pane est un panel qui contient une liste et affiche la barre a droite qui permet de monter et descendre
    private JScrollPane scrollpane;
    
    // D�claration des boutons Cancel et Open
    private JButton buttonCancel;
    private JButton buttonOpen;
    
    // D�claration des panels
    private JPanel panelButton;
    
    /**
     *  Constructeur de la classe FileSelector
     */
    
    public Choix_fichier(String title, String lbl_cancel, String lbl_ok) {
        super((JFrame)null, title, true /* modal */) ;
        
        // Initialisation des boutons Cancel et Open
        buttonCancel = new JButton(lbl_cancel);
		buttonOpen = new JButton(lbl_ok);
		
		// Initialisation de la combobox et de la list : on se place dans le dossier o� s'enregistre les fichiers IHM
        show(System.getProperty("user.dir")) ;
        
        // init permet l'affichage et appel les listeners 
        init();
        
        pack() ;
        setVisible(true) ;
    }
    
    /**
     * Listeners : cancel, open, combobox et le double clique
     */
    
    // Listener du bouton "Cancel" : ActionListener (car action sur le bouton)
    private ActionListener buttonCancelListener = new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("L'user a cliqu� sur Cancel");
			// Permet de quitter lors de l'appuie sur le bouton "Cancel"
			dispose();
		}
    };
 
    // Listener du bouton "Open" : ActionListener (car action sur le bouton)
    private ActionListener buttonOpenListener = new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// Mise a jour de l'item s�lectionn� et de la combobox
			updateItem();
		}	
    };
    
    // Listener de la ComboBox : ActionListener (car agit lorsqu'on s�lectionne un item)
    private ActionListener comboListener = new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// On met dans une variable l'item (=r�pertoire) s�lectionn�
			Object item = combo.getSelectedItem();
			
			// On enregistre sa position
			int position = combo.getSelectedIndex();
			
			// Appel d'une fonction qui met a jour la liste du nouveau r�pertoire
			System.out.println("On est dans le combolistener");
			System.out.println("item = " + (String)item + " et position = " + position);
			
			// Cr�ation du chemin
			String path;
			path = createPath(position);
			
			// On supprime les items pr�sents dans la combobox car ils sont copi� � chaque choix d'un nouvel item
			combo.removeAllItems();
			// On supprime la list pr�sente dans l'item pr�c�dent : pas n�c�ssaire?
			list.removeAll();
			
			// Update les nouveaux items pr�sent dans la Combobox
			show(path);
		}
    };
    
    
    // Listener de la list : MouseListener (car on va double-cliquer sur un �l�ment de la list)
    private MouseListener doubleClicListener = new MouseListener(){

		@Override
		// Cette m�thode est appel�e quand l'utilisateur a cliqu� (appuy� puis rel�ch�) sur le 	composant �cout�
		public void mouseClicked(MouseEvent mouse_event) {
			// Lorsqu'on double-clic
			if(mouse_event.getClickCount() == 2){
				// Mise a jour de l'item selectionn� et de la combobox
				updateItem();
			}
		}

		@Override
		// Cette m�thode est appel�e quand la souris entre dans la zone du composant �cout�
		public void mouseEntered(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		// Cette m�thode est appel�e quand la souris sort de la zone du composant �cout�
		public void mouseExited(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		// Cette m�thode est appel�e quand l'utilisateur appuie le bouton sur le composant �cout�
		public void mousePressed(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		// Cette m�thode est appel�e quand l'utilisateur rel�che le bouton sur le composant �cout�
		public void mouseReleased(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}
    	
    };
    
    
    
    /**
     * M�thodes permettant de mettre � jour au d�but et � chaque action la combobox et la list
     */
    
    
    
    /** Met a jour la ComboBox et la List : utilis� pour le listener de la ComboBox
     * pas utilis� pour le listener du button Open car on a pas besoin de tout (a partir de path.list)
     */
    Boolean show(String path) {
    	// dir est un fichier qui va nous permettre de tester si path existe et si c'est bien un r�pertoire
        File dir = new File(path) ;
        
        if (!dir.exists() || !dir.isDirectory()){
        	System.out.println("return false");
        	return false ;
       }
       
        // On s�lectionne le r�pertoire o� l'on est
        directory = dir.getAbsolutePath() ;
        
        // Affiche le r�pertoire o� on est la : dans le dossier IHM
        System.out.println("DIRECTORY = " + directory) ;
        
        file = null ;
      
        // Files : tableau de string qui contient les �l�ments contenu dans chaque r�pertoire
        String[] files = dir.list() ;
        
        if (files!=null) {
            for(int i=0; i<files.length; i++) {
            	
            	/** NB : Le constructeur File(chaine1, chaine2) ajoute la chaine2 � la chaine1
            	 *  en les s�parant d'un s�parateur (\ ou /)
            	 *  f va nous permettre de tester si pour l'�l�ment i, on a un fichier ou r�pertoire
            	 */
                File f = new File(path, files[i]); 
                
                /** f.isDirectory() : test si le fichier est un dossier */
                
                // Cas ou le fichier est un dossier
                if (f.isDirectory()){
                		files[i] = files[i]+File.separator ;
                		System.out.format("  directory : %s\n",files[i]);
                }
                // Cas ou le fichier est un fichier
                else{
                		System.out.format("  file: %s\n",files[i]) ;  
                }
            }
            // On ajoute a la liste list le tableau files contenant tous les fichiers/dossier pr�sent dans le r�pertoire
            list.setListData(files);
        }
     
        // dirs : tableau string qui corresponds aux r�pertoires � la "racine"
        String[] dirs = path.split("\\\\") ;
        
        // On attribue � "position" la position initiale (on aurait pu mettre position = 0)
        int position = combo.getSelectedIndex();
  
        // On regarde les r�pertoires "parents" (ce qui sont avant) et on les ajoute � la combobox
        for (String p : dirs) {
        	if (p.equals("")) continue ;
        	System.out.format("  parentdir: %s\n",p) ;
        	
        	// On ajoute l'item p (qui correspond � UN r�pertoire) dans le ComboBox
        	combo.addItem(p);
        	
        	// On incr�mente la position d�s qu'on ajoute un item � la comboBox
        	// Position va donc nous permettre de nous positionner sur le dernier item (qui a �t� choisi)
        	position++;
        }		
       
        // On place la combobox � l'item o� l'on se trouve : celui qu'on a choisi
        combo.setSelectedIndex(position);
        
        // A la fin du for, combo contient tous les r�pertoires qui sont dans le tableau dirs
        return true ;
    }
    
    /** Met � jour la nouvelle list et ajoute le nouvel item dans la combobox 
     * 	pour le listener du bouton open
     */
    public void updateItem(){
    	// �l�ment s�lectionn� : soit un fichier, soit un r�pertoire
    	String element = (String) list.getSelectedValue();
    	
    	// On enregistre sa position
    	int position = combo.getSelectedIndex();
    	
    	// Cr�ation du chemin
    	String path;
    	path = createPath(position);	// Cr�er le chemin jusqu'� l'item pr�c�dent
    	    	
    	/* Le path est compos� du chemin jusqu'� l'item. Il faut lui rajouter l'objet sur lequel 
    	 *  on vient de cliquer. Si c'est un dossier, il y aura dossier/
    	 */
    
    	path = path + File.separator + element;
    	
    	File f1 = new File(path);	// permet de tester si l'objet est un r�pertoire/fichier
    	
    	// Cas ou l'objet est un r�pertoire
    	if(f1.isDirectory()){
    		System.out.println("EST DIRECTORY");
    		
    		/* NE SERT A RIEN ?
    		 * On enleve le caract�re en trop qui est a la fin : le "/" (car on en a pas besoin)
    		 */
    		//	path = path.substring(0, path.length()-1);
    		
    		// Update les nouveaux items pr�sent dans la Combobox
    		// On supprime les �l�ments qui �t� pr�sent dans "l'ancienne" liste
    		list.removeAll();
    		
    		/* NE SERT A RIEN ?
    		 * On enl�ve le caract�re "/" � la fin de l'�l�ment
    		 */
    		//element = element.substring(0, element.length()-1);

    		// Fichier nous permettant de savoir de quel type est le path : s'il existe et si c'est bien un r�pertoire
    		File dir = new File(path) ;
    		
    		if (dir.exists() && dir.isDirectory()){
    			// files contient toute la liste pr�sente dans le nouveau r�pertoire
    			String[] files = dir.list() ;
    			
    			if (files!=null){
    				for(int i=0; i<files.length; i++) {
    					// f va nous permettre de savoir si l'objet de la liste � la position i est un dossier ou un fichier
    					File f = new File(path, files[i]);
    					// Si c'est un dossier, il faut lui rajouter le "/" � la fin de son nom pour indiquer que c'est un dossier
    					if (f.isDirectory()) files[i] = files[i]+File.separator ;
    				}
    			}
    			// Mise � jour de la liste : on lui attribue toutes les fichiers/dossiers qui sont dans le nouvel item
    			list.setListData(files);
    			
    			// On ajoute � la combobox le dossier dans lequel on est rentr�
    			/* combo.addItem(String) : rajoute un item dans la combobox en lui attribuant 
    			 * le nom pass� en param�tre
    			 */
    			combo.addItem(element);
    			// On se met sur le nouvel item (qui a pour nom le string contenu dans objet)
    			combo.setSelectedItem(element);	
    		}
    		
    	}
    	// L'objet est un fichier
    	else{
    		System.out.println("N'EST PAS DIRECTORY");
    		System.out.println("Vous avez s�lectionner le fichier : " + path);
    		dispose();
		}
    }
    

    // Cr�er le chemin pour un item choisi a partir de sa position
    public String createPath(int positionItem){
    	/**  NB:
    	 * File.separator = / pour Windows
		 * ComboBox.getItemAt(i) permet d'obtenir l'item a la position i
		 */
    	int i;
    	// On met le cas i=0 a part car sinon le chemin sera : C://etc... au lieu de C:/etc...
    	String path = (String)combo.getItemAt(0);
    	for(i=1; i<=positionItem; i++){
    			path = path + File.separator + combo.getItemAt(i);
    	}
    	return path;
    }
    
    /**
     * La m�thode init() permet de construire la fenetre en y ajoutant les diff�rents panels, bouton,
     * la combobox et la list qui sera inclut dans le scrollpane
     */
    public void init(){
    	
        // Initialisation du JScrollPane : panel qui contient la liste list et qui en plus a une barre de d�filement si n�c�ssaire
        scrollpane = new JScrollPane(list);
    	
    	// Initialisation du panelButton : on lui met les buttons Cancel et Open
    	panelButton = new JPanel();
    	
    	//Attribution des ActionListeners aux buttons Cancel et Open
    	combo.addActionListener(comboListener);
    	buttonCancel.addActionListener(buttonCancelListener);
    	buttonOpen.addActionListener(buttonOpenListener);
    	list.addMouseListener(doubleClicListener);
    	
    	/**
    	 * Cr�ation du panelButton qui contiendra : les boutons Cancel et Open et sa mise en forme
    	 * Utilisation de BoxLayout qui nous permettra d'aligner les boutons selon l'axe des X
    	 * Utilisation d'une horizontalGlue qui nous permet de placer les boutons sur la droite
    	 * Utilisation de rigideArea qui nous permet de mettre un espace entre les 2 boutons
    	 */
    	
    	// Pour le panel des Buttons, on utilise BoxLayout pour les aligner sur l'axe des X
		panelButton.setLayout(new BoxLayout(panelButton, BoxLayout.X_AXIS));
		
		// Cr�e une barre horizontal vide qui marche comme un ressort
		panelButton.add(Box.createHorizontalGlue());
		
		panelButton.add(buttonCancel);
		// Cr�e une zone vide qu'on position entre les 2 buttons
		panelButton.add(Box.createRigidArea(new Dimension(10, 0)));	/* 10 longueur, 0 hauteur */
		panelButton.add(buttonOpen);
		
		
		/**
		 *  Fenetre principal : on ajoute les diff�rents objets dans la fenetre principale
		 *  Utilisation d'un BorderLayout pour organiser les panels en haut (ComboBox), 
		 *  				au centre (ScrollPane) et en bas (panelButton)
		 */
		
		// Attribution � la fenetre de l'organisation : borderlayout
		setLayout(new BorderLayout());
		// On ajoute la comboBox en haut de la fenetre
		add(combo, BorderLayout.NORTH);
		// On ajoute le scrollpane contenant la list au centre de la fenetre
		add(scrollpane, BorderLayout.CENTER);
		// on ajoute le panel des boutons en bas de la fenetre
		add(panelButton, BorderLayout.SOUTH);
		
		
    }
    
    // Indique quel fichier nous avons choisi
    public String getFilePath() {
        if (directory==null || file==null) return null ;
        return directory+File.separator+file ;
    }
    
    static public void main(String args[]) {
        Choix_fichier fs = new Choix_fichier("File open...","Cancel","Open") ;
        System.out.println(fs.getFilePath()) ;
        System.exit(1) ;
    }
    
   /** RIEN A VOIR AVEC LE TP : 
    * 
    * EXEMPLE AVEC JFILECHOOSER QUI CONSTRUIT LUI MEME
    * 
    * 	// cr�ation de la bo�te de dialogue
   	*	JFileChooser dialogue = new JFileChooser();
    *	
   	*	// affichage
   	*	dialogue.showOpenDialog(null);
    *
   	*	// r�cup�ration du fichier s�lectionn�
   	*	System.out.println("Fichier choisi : " + dialogue.getSelectedFile()); 
    */
}