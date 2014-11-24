package pne.project.tsp.view;

import java.awt.Component;

import javax.swing.JTable;

import pne.project.tsp.beans.Graph;

public class TabView extends JTable {

	public TabView(Graph g) {
		super(new TabModel(g));
		setAutoCreateColumnsFromModel(true);
		setAutoscrolls(false);

		getColumnModel().getColumn(0).setCellRenderer(
				getTableHeader().getDefaultRenderer());
		//getColumnModel().getColumn(0).setPreferredWidth(150);
//		getColumnModel().getColumn(1).setPreferredWidth(150);
		setPreferredScrollableViewportSize(getPreferredSize());
		
		/*setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		for (int j=1; j < getColumnCount(); j+=1) {  
			getColumnModel().getColumn(j).setPreferredWidth(500);  
		}
		*/
		
		
		setDefaultRenderer(Object.class, new MatrixCellRenderer());

	}

	public JTable getAdjaMatrix() {
		return this;
	}

}