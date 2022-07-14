package org.grits.toolbox.entry.qrtpcr.commands;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.service.IGritsDataModelService;
import org.grits.toolbox.core.service.IGritsUIService;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.grits.toolbox.entry.qrtpcr.dialogs.UploadDataWizard;
import org.grits.toolbox.entry.qrtpcr.editor.QrtPCRTableEditor;
import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.GeneData;
import org.grits.toolbox.entry.qrtpcr.model.QrtPCRData;
import org.grits.toolbox.entry.qrtpcr.model.QrtPCRRun;
import org.grits.toolbox.entry.qrtpcr.model.QrtPCRTable;
import org.grits.toolbox.entry.qrtpcr.property.QrtPCRProperty;
import org.grits.toolbox.entry.qrtpcr.util.FileUtils;

public class AddReRunData {
	
	private static final Logger logger = Logger.getLogger(AddReRunData.class);

	@Inject private static IGritsDataModelService gritsDataModelService = null;
	@Inject static IGritsUIService gritsUIService = null;

	@Execute
	public Object execute(@Named(IServiceConstants.ACTIVE_SELECTION) Object object,
			@Named (IServiceConstants.ACTIVE_SHELL) Shell shell, 
			@Named(IServiceConstants.ACTIVE_PART) MPart part) throws ExecutionException {
		logger.debug("BEGIN AddReRunData");
        
        Entry selectedEntry = null;
		if(object instanceof Entry)
		{
			selectedEntry = (Entry) object;
		}
		else if (object instanceof StructuredSelection)
		{
			if(((StructuredSelection) object).getFirstElement() instanceof Entry)
			{
				selectedEntry = (Entry) ((StructuredSelection) object).getFirstElement();
			}
		}
		// try getting the last selection from the data model
		if(selectedEntry == null
				&& gritsDataModelService.getLastSelection() != null
				&& gritsDataModelService.getLastSelection().getFirstElement() instanceof Entry)
		{
			selectedEntry = (Entry) gritsDataModelService.getLastSelection().getFirstElement();
		}

		if(selectedEntry != null)
		{
			if(selectedEntry.getProperty() == null 
					|| !QrtPCRProperty.TYPE.equals(selectedEntry.getProperty().getType()))
			{
				selectedEntry = null;
			}
		}
        
        if (selectedEntry != null ) {
    		// 1 - load the table
        	QrtPCRProperty prop = (QrtPCRProperty) selectedEntry.getProperty();
        	String filename = prop.getFilename();
        	try {
        		QrtPCRRun pcrRun = FileUtils.loadQrtPCRRun(selectedEntry, filename); 
        		
        		UploadDataWizard wizard = new UploadDataWizard();
        		wizard.setSampleEntry(selectedEntry.getParent());
        		wizard.setForReRerun(true);
        		wizard.setMasterListFile(pcrRun.getGeneListFile());
        		wizard.setEntryName(selectedEntry.getDisplayName());
        		WizardDialog wizardDialog = new WizardDialog(shell,
        			      wizard);
        		if (wizardDialog.open() == Window.OK) {
        			logger.debug("qrtPCR Wizard - Add Re-run: Ok pressed");
        			QrtPCRTable originalTable = pcrRun.getFirstRun();
        			QrtPCRTable newTable = wizard.getTable();
        			newTable.setOriginal(true);   
        			pcrRun.addRun(newTable);
        			// find all genes in the newTable that matches with the ones in the old table and add re-run values
        			addRerunValues (shell, originalTable, newTable);
        			if (part != null && part.getObject() instanceof QrtPCRTableEditor) {
        				for (Iterator<?> iterator = newTable.getPlateDataMap().keySet().iterator(); iterator
								.hasNext();) {
							String plateId = (String) iterator.next();
							((QrtPCRTableEditor) part.getObject()).addPlatePage(pcrRun.getRunId(), plateId, newTable.getInstrumentFileMap().get(plateId), true);
							//((QrtPCRTableEditor) editor).addPlatePage(pcrRun.getRunId(), plateId, newTable.getInstrumentFileMap().get(plateId), false);
						}
        				part.getContext().set(QrtPCRRun.class, pcrRun);
        				((QrtPCRTableEditor) part.getObject()).refreshInput(pcrRun);
        				((QrtPCRTableEditor) part.getObject()).markDirty();
        			}
        		}
        	} catch (JAXBException e) {
            	logger.error("Error adding qrtPCR rerun Data", e);
            	ErrorUtils.createErrorMessageBox(shell, "Error adding qrtPCR rerun Data", e);
            } catch (FileNotFoundException e) {
            	logger.error("Error adding qrtPCR rerun Data", e);
            	ErrorUtils.createErrorMessageBox(shell, "Error adding qrtPCR rerun Data", e);
    		} catch (UnsupportedEncodingException e) {
    			logger.error("Error loading qrtPCR Data", e);
    			ErrorUtils.createErrorMessageBox(shell, "Error adding qrtPCR rerun Data", e);
    		} 
        }
        logger.debug("END AddReRunData");
        
        return selectedEntry;
	}

	private void addRerunValues(Shell shell, QrtPCRTable table, QrtPCRTable newTable) {
		List<String> errorGenes = new ArrayList<>();
		for (Iterator<?> iterator = newTable.getPlateDataMap().values().iterator(); iterator.hasNext();) {
			QrtPCRData data = (QrtPCRData) iterator.next();
			data.calculateNormValue();
			List<Gene> reRunGenes = data.getGenes();
			// for each gene in the re-run plates, find the corresponding gene in the existing table
			for (Gene gene : reRunGenes) {
				if (!addReRunToGene (gene, table)) {
					errorGenes.add(gene.getGeneIdentifier());
				}
			}
		};
		
		if (!errorGenes.isEmpty()) {
			logger.warn("Cannot find corresponding genes for: " + errorGenes.toArray());
        	ErrorUtils.createErrorMessageBox(shell, "Cannot find corresponding genes for: " + errorGenes.toArray());
		}
	}

	private boolean addReRunToGene(Gene reRunGene, QrtPCRTable table) {
		for (Iterator<QrtPCRData> iterator = table.getPlateDataMap().values().iterator(); iterator.hasNext();) {
			QrtPCRData data = (QrtPCRData) iterator.next();
			List<Gene> genes = data.getGenes();
			for (Gene gene : genes) {
				if (reRunGene.getGeneIdentifier() != null && gene.getGeneIdentifier() != null &&
						gene.getGeneIdentifier().equals(reRunGene.getGeneIdentifier())) {
					if (!gene.getIsControl() && !gene.getIsCommon()) {
						List<GeneData> newGeneDataList = reRunGene.getDataMap().get(0);
						List<GeneData> newGeneDataCopy = new ArrayList<>();
						// set the gene in GeneData
						for (GeneData geneData : newGeneDataList) {
							geneData.setGene(reRunGene);
							GeneData geneDataCopy = new GeneData(geneData);
							newGeneDataCopy.add (geneDataCopy);
							geneDataCopy.setGene(gene);
						}
						gene.addGeneDataList(newGeneDataCopy);
						gene.getNormValueMap().put(gene.getRunId(), reRunGene.getNormValue(0));
						gene.setReRun(true);
						return true;
					} else // do not change the value for the control gene
						return true;
				}
			}
		}
		return false;
	}
}
