package org.grits.toolbox.entry.qrtpcr.manager.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.grits.toolbox.entry.qrtpcr.Config;
import org.grits.toolbox.entry.qrtpcr.ImageRegistry;
import org.grits.toolbox.entry.qrtpcr.ImageRegistry.QrtPCRImage;
import org.grits.toolbox.entry.qrtpcr.manager.GeneListManagerEditor;
import org.grits.toolbox.entry.qrtpcr.manager.pages.MasterGeneListPage;
import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.GeneList;
import org.grits.toolbox.entry.qrtpcr.model.TableData;
import org.grits.toolbox.entry.qrtpcr.model.view.GeneView;
import org.grits.toolbox.entry.qrtpcr.ncbi.NCBIGeneUtil;

public class UpdateGenesAction extends Action {
	private static final Logger logger = Logger.getLogger(UpdateGenesAction.class);
	
	public UpdateGenesAction(MasterGeneListPage page) {
		this.page = page;
	}

	MasterGeneListPage page;

	private GeneList masterList;

	private List<Gene> tableGeneList;

	
	@Override
	public ImageDescriptor getImageDescriptor() {
		return ImageRegistry.getImageDescriptor(QrtPCRImage.UPDATE_ICON);
	}
	
	@Override
	public String getText() {
		return "Update";
	}
	
	@Override
	public String getToolTipText() {
		return "Update genes' data from NCBI";
	}
	
	@Override
	public void run() {
		Shell shell = Display.getCurrent().getActiveShell();
		
		if (this.page == null)  {
			return;
		}
		
		masterList = page.getSelectedGeneList();
		List<TableData>input = page.getDataFromTable();
		if (input == null) {
			return;
		}
		tableGeneList = new ArrayList<>();
		for (TableData gene : input) {
			tableGeneList.add((Gene)gene);
		}
		if (masterList == null || masterList.getGenes() == null || masterList.getGenes().isEmpty()) {
			MessageDialog.openInformation(shell, "No Selection", "Please make sure to select genes to update!");
			return;
		}
		List<Gene>  removedGenes = new ArrayList<Gene>();
		ProcessGenes update = new ProcessGenes(removedGenes);  
    	ProgressMonitorDialog monitor = new ProgressMonitorDialog(shell);
    	try {
			monitor.run(true, true, update);
		} catch (InvocationTargetException e) {
			logger.error("Error getting gene data from NCBI" , e);
			MessageDialog.openError(shell, "Error", "Error getting gene data from NCBI");
		} catch (InterruptedException e) {
			logger.warn("Interrupted", e);
		}
    	if (monitor.getReturnCode() != Window.CANCEL) {
    		page.clearSelection();  // need to clean up the selected lists
    		page.refresh(tableGeneList);
    		if (!removedGenes.isEmpty()) {
    			LabelProvider labelProvider = new LabelProvider() {
    				@Override
    				public String getText(Object element) {
    					if (element instanceof Gene)
    						return ((Gene) element).getGeneIdentifier();
    					return null;
    				}
    			};
    			ListSelectionDialog selectionDialog = new ListSelectionDialog(shell, removedGenes, new ArrayContentProvider(), 
    					labelProvider, 
    					"These genes no longer exist in NCBI.\nDe-select the genes you want to keep anyway.\nOtherwise, they will be removed from the list");
    			selectionDialog.setInitialElementSelections(removedGenes);
    			if (selectionDialog.open() == Window.OK) {
    				Object[] remove = selectionDialog.getResult();
    				for (Object object : remove) {
    					tableGeneList.remove((Gene)object);
					}
    				
    				if (remove.length > 0) {
	    				page.refresh(tableGeneList);
	    				((GeneListManagerEditor)page.getEditor()).markDirty();
    				}
    			}
    		}
			
    	}
		
	}

	class ProcessGenes implements IRunnableWithProgress {

		List<Gene> missingGenes;
		
		public ProcessGenes(List<Gene> removedGenes) {
			this.missingGenes = removedGenes;
		}

		@Override
		public void run(IProgressMonitor monitor)
				throws InvocationTargetException, InterruptedException {
			NCBIGeneUtil ncbiSearch = new NCBIGeneUtil();
			
			monitor.beginTask("Retrieving updates from NCBI", masterList.getGenes().size());
			int i=0;
			for (Gene gene : masterList.getGenes()) {
				monitor.subTask("Retrieving " + i + " of " + masterList.getGenes().size());
				List<Integer> geneIds = gene.getGeneIds();
				if (geneIds != null && !geneIds.isEmpty()) {
					Gene newGene;
					try {
						newGene = ncbiSearch.getDetailsFromNCBI(geneIds.get(0));
						if (newGene == null) {
							// it does not exist anymore, add it to the removed genes list
							this.missingGenes.add(gene);
							monitor.worked(1);
							i++;
							continue;
						}
						newGene.setGeneIdentifier(gene.getGeneIdentifier());
						newGene.setDescription(gene.getDescription());
						newGene.setForwardPrimer(gene.getForwardPrimer());
						newGene.setReversePrimer(gene.getReversePrimer());
						newGene.setGroup(gene.getGroup());
						newGene.setGeneIds(gene.getGeneIds());
						newGene.setIsCommon(gene.getIsCommon());
						newGene.setIsControl(gene.getIsControl());
						newGene.setNotes(gene.getNotes());
						// check if there are really any updates
						GeneView geneView = markUpdated(gene, newGene);
						if (geneView == null) {
							monitor.worked(1);
							i++;
							continue;
						}
						// find the gene in the tableViewer and update
						// List iterator needed for replacing elements
					    ListIterator<Gene> listIterator = tableGeneList.listIterator();
					    while (listIterator.hasNext()) {
					    	Gene gene2 = listIterator.next();
					    	if (gene2.getGeneIdentifier().equals(gene.getGeneIdentifier())) {
					    		listIterator.set(geneView);
					    		break;
					    	}
					    }
					} catch (Exception e) {
						logger.error("Error getting gene data from NCBI for " + i + "th gene", e);
					}
					
				}
				monitor.worked(1);
				i++;
				
				// Check if the user pressed "cancel"
	            if(monitor.isCanceled())
	            {
	                monitor.done();
	                return;
	            }
			}
			monitor.done();
		}
	};

	public GeneView markUpdated(Gene gene, Gene newGene) {
		boolean updated = false;
		GeneView geneView = new GeneView(newGene);
		if (!gene.getGeneSymbol().equals(newGene.getGeneSymbol())) {
			geneView.addUpdatedField(Config.GENESYMBOL);
			updated= true;
		}
		if (!gene.getLocationString().equals(newGene.getLocationString())) {
			geneView.addUpdatedField(Config.LOCATION);
			updated= true;
		}
		if (!gene.getAliasString().equals(newGene.getAliasString())) {
			geneView.addUpdatedField(Config.ALIASES);
			updated= true;
		}
		if (gene.getFullName() != null && !gene.getFullName().equals(newGene.getFullName())) {
			geneView.addUpdatedField(Config.NAME);
			updated= true;
		}
		if (!gene.getRefSeqString().equals(newGene.getRefSeqString())) {
			geneView.addUpdatedField(Config.REFSEQ);
			updated= true;
		}
		if (!gene.getSecondaryRefSeqString().equals(newGene.getSecondaryRefSeqString())) {
			geneView.addUpdatedField(Config.SECONDARYREFSEQ);
			updated= true;
		}
		if (updated) {
			geneView.setUpdated(true);
			return geneView;
		}
		return null;
	}

/*	public void setInput(List<Gene> genes) {
		this.input = genes;
		
	}*/
}
