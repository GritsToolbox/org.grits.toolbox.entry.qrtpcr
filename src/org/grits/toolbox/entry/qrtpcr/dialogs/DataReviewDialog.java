package org.grits.toolbox.entry.qrtpcr.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.grits.toolbox.entry.qrtpcr.editor.content.GeneContentProvider;
import org.grits.toolbox.entry.qrtpcr.editor.content.PlateTableViewerSorter;
import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.GeneData;
import org.grits.toolbox.entry.qrtpcr.model.QrtPCRData;

public class DataReviewDialog extends Dialog {
	
	QrtPCRData data;
	private boolean changed = false;
	private Tree tree;
	TreeViewer treeViewer;
	
	List<Gene> toBeRemoved;
	private PlateTableViewerSorter sorter;
	private Double lowerThreshold;
	private Double stDevCutOff;

	public QrtPCRData getData() {
		return data;
	}

	public void setData(QrtPCRData data) {
		this.data = data;
	}

	public DataReviewDialog(Shell parent) {
		super(parent);
	}
	
	public void setLowerThreshold(Double lowerThreshold) {
		this.lowerThreshold = lowerThreshold;
	}
	
	public void setStDevCutOff(Double stDevCutOff) {
		this.stDevCutOff = stDevCutOff;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
	    Composite container = (Composite) super.createDialogArea(parent);
	    container.setLayout(new GridLayout(2, false));
	    
	    sorter = new PlateTableViewerSorter(true);
	    sorter.setLowerThreshold(lowerThreshold);
	    sorter.setStDevCutOff(stDevCutOff);
	    
	    treeViewer = new TreeViewer(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
	    tree = treeViewer.getTree();
	    tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
	    
	    tree.setHeaderVisible(true);
	    tree.setLinesVisible(true); 

	    treeViewer.setContentProvider(new GeneContentProvider());
	    
	    TreeViewerColumn geneIdColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
	    TreeColumn tblclmnGeneIdentifier = geneIdColumn.getColumn();
	    tblclmnGeneIdentifier.setWidth(200);
	    tblclmnGeneIdentifier.setText("Gene Identifier");
	    geneIdColumn.setLabelProvider(new ColumnLabelProvider() {
	    	@Override
	    	public String getText(Object element) {
		    	if (element instanceof Gene) 
	    			return ((Gene)element).getGeneIdentifier();
	    		else 
	    			return null;
	    	}
	    });
	    
	    TreeViewerColumn positionColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
	    TreeColumn tblclmnLocation = positionColumn.getColumn();
	    tblclmnLocation.setWidth(100);
	    tblclmnLocation.setText("Position");
	    positionColumn.setLabelProvider(new ColumnLabelProvider() {
	    	@Override
	    	public String getText(Object element) {
	    		if (element instanceof GeneData)
	    			return ((GeneData)element).getPosition().toString();
	    		return null;
	    	}
	    });
	    tblclmnLocation.addSelectionListener(getSelectionAdapter(tblclmnLocation, 0));
	    
	    toBeRemoved = new ArrayList<>();
	    
	    Button deleteButton = new Button(container, SWT.NONE);
	    deleteButton.setText("Delete");
	    deleteButton.setToolTipText("Remove selected rows");
	    deleteButton.addSelectionListener(new SelectionAdapter() {
	    	@Override
	    	public void widgetSelected(SelectionEvent e) {
	    		TreeItem[] selections = tree.getSelection();
	    		for (int i = 0; i < selections.length; i++) {
					TreeItem item = selections[i];
					Object element = item.getData();
					if (element instanceof Gene) {   // delete the whole gene only, not individual geneData
						toBeRemoved.add((Gene) element);
					}
	    		}
	    		
	    		if (toBeRemoved.size() > 0) {
	    			List<Gene> oldList = new ArrayList<>(data.getGenes());
	    			List<Gene> newList = removeGenes (oldList, toBeRemoved);
	    			changed = true;
	    			
	    			treeViewer.setInput(newList);
	    			treeViewer.refresh();
	    			treeViewer.expandAll();
	    		}
	    	}
	    });
	    
	    treeViewer.setInput(data.getGenes());
	    treeViewer.expandAll();
	    
	    treeViewer.setSorter(sorter);
		treeViewer.getTree().setSortColumn(tblclmnLocation);
		treeViewer.getTree().setSortDirection(SWT.UP);
	    return container;
	}
	
	@Override
	protected void okPressed() {
		// delete them permanently only when OK is clicked
		List<Gene> newList = removeGenes (data.getGenes(), toBeRemoved);
		changed = true;
		data.setGenes (newList);
		super.okPressed();
	}
	
	protected List<Gene> removeGenes(List<Gene> genes, List<Gene> toBeRemoved) {
		List<Gene> newGenes = new ArrayList<>();
		for (Gene gene : genes) {
			if (!toBeRemoved.contains(gene))
				newGenes.add(gene);
		}
		
		return newGenes;
	}

	private SelectionAdapter getSelectionAdapter(final TreeColumn column,
		      final int index) {
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
		      @Override
		      public void widgetSelected(SelectionEvent e) {
		        sorter.setColumn(index);
		        int dir = sorter.getDirection();
		        treeViewer.getTree().setSortDirection(dir);
		        treeViewer.getTree().setSortColumn(column);
		        treeViewer.refresh();
		      }
		};
		return selectionAdapter;
	}
	
	// overriding this methods allows you to set the
	// title of the custom dialog
	@Override
	protected void configureShell(Shell newShell) {
	    super.configureShell(newShell);
	    newShell.setText("Review dialog");
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(350, 300);
	}

	public boolean isChanged() {
		return changed ;
	}
}
