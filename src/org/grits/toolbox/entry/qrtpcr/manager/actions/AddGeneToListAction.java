package org.grits.toolbox.entry.qrtpcr.manager.actions;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.grits.toolbox.entry.qrtpcr.ImageRegistry;
import org.grits.toolbox.entry.qrtpcr.ImageRegistry.QrtPCRImage;
import org.grits.toolbox.entry.qrtpcr.manager.GeneListManagerEditor;
import org.grits.toolbox.entry.qrtpcr.model.Gene;

public class AddGeneToListAction extends Action {
	
	private static final Logger logger = Logger.getLogger(AddGeneToListAction.class);
	GeneListManagerEditor editor;
	String organism;
	List<Gene> input;
	
	public AddGeneToListAction(GeneListManagerEditor geneListManagerEditor) {
		this.editor = geneListManagerEditor;
	}
	
	public void setInput(List<Gene> input) {
		this.input = input;
	}
	
	public void setOrganism(String organism) {
		this.organism = organism;
	}
	
	@Override
	public ImageDescriptor getImageDescriptor() {
		return ImageRegistry.getImageDescriptor(QrtPCRImage.ADD_ICON);
	}
	
	@Override
	public String getText() {
		return "Add";
	}
	
	@Override
	public String getToolTipText() {
		return "Add a new gene to the list";
	}
	
	@Override
	public void run() {
		AddGeneDialog dialog = new AddGeneDialog(Display.getCurrent().getActiveShell(), input, organism);
		if (dialog.open() == Window.OK) {
			Gene gene = dialog.getGene();
			List<Gene> existing = input;
			for (Gene gene2 : existing) {
				if (gene2.getGeneIdentifier().equals(gene.getGeneIdentifier())) {
					logger.info("A gene with " + gene.getGeneIdentifier() + " already exists in the list. Not added!");
					ErrorUtils.createErrorMessageBox(Display.getCurrent().getActiveShell(), "A gene with this identifier already exists in the list");
					return;
				}
			}
			existing.add(gene);
			((GeneListManagerEditor)editor).refreshGeneListPage(existing);
			((GeneListManagerEditor)editor).markDirty();
		}
	}

}
