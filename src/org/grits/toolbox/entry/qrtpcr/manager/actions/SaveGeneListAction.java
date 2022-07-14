package org.grits.toolbox.entry.qrtpcr.manager.actions;

import java.io.IOException;
import java.util.Date;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.grits.toolbox.entry.qrtpcr.ImageRegistry;
import org.grits.toolbox.entry.qrtpcr.ImageRegistry.QrtPCRImage;
import org.grits.toolbox.entry.qrtpcr.manager.GeneListManagerEditor;
import org.grits.toolbox.entry.qrtpcr.manager.pages.GeneSelectionListContentProvider;
import org.grits.toolbox.entry.qrtpcr.model.GeneList;
import org.grits.toolbox.entry.qrtpcr.util.FileUtils;

public class SaveGeneListAction extends Action {
	
	private static final Logger logger = Logger.getLogger(SaveGeneListAction.class);
	
	TableViewer viewer;

	private GeneListManagerEditor editor;

	private GeneList masterList;
	
	@Override
	public ImageDescriptor getImageDescriptor() {
		return ImageRegistry.getImageDescriptor(QrtPCRImage.SAVE_ICON);
	}
	
	@Override
	public String getText() {
		return "Save";
	}
	
	@Override
	public String getToolTipText() {
		return "Save genes as a sub-list";
	}
	
	public void setViewer(TableViewer viewer) {
		this.viewer = viewer;
	}
	
	@Override
	public void run() {
		Shell shell = Display.getCurrent().getActiveShell();
		
		if (this.viewer == null) {
			return;
		}
		
		GeneSelectionListContentProvider cp = (GeneSelectionListContentProvider) this.viewer.getContentProvider();
		if (cp.getGenes().isEmpty()) {
			MessageDialog.openInformation(shell, "Nothing to Save", "The list is empty. Please select genes from the left and try again");
			return;
		}
		// ask for a name and save it in the configuration directory
		SaveGeneListDialog dialog = new SaveGeneListDialog(shell);
		dialog.setOrganism(masterList.getOrganism());
		dialog.create();
		dialog.getShell().setSize(550, 300);
		if (dialog.open() == Window.OK) {
			GeneList newGeneList = new GeneList();
			newGeneList.setListName(dialog.getName());
			newGeneList.setDescription(dialog.getDescription());
			newGeneList.setOrganism(dialog.getOrganism());
			newGeneList.setDateCreated(new Date());
			newGeneList.setGenes(cp.getGenes());
			newGeneList.setParentListId(masterList.getListName());
			
			try {
				String filename = FileUtils.saveGeneSubListFile(newGeneList);
				newGeneList.setFilename(filename);
				((GeneListManagerEditor)editor).refreshOverview(newGeneList);
			} catch (IOException | JAXBException e) {
				logger.error("Error saving the new sub-list to a file", e);
				ErrorUtils.createErrorMessageBox(shell, "Error saving the new sub-list to a file. Reason: " + e.getMessage());
			}
		}
	}

	public void setEditor(GeneListManagerEditor geneListManagerEditor) {
		this.editor = geneListManagerEditor;
		
	}

	public void setMasterList(GeneList geneList) {
		this.masterList = geneList;
		
	}
}
