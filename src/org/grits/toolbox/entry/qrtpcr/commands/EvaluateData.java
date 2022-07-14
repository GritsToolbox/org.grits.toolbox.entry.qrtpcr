package org.grits.toolbox.entry.qrtpcr.commands;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.service.IGritsDataModelService;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.grits.toolbox.entry.qrtpcr.editor.QrtPCRTableEditor;
import org.grits.toolbox.entry.qrtpcr.model.CtHistory;
import org.grits.toolbox.entry.qrtpcr.model.CtHistory.Reason;
import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.GeneData;
import org.grits.toolbox.entry.qrtpcr.model.QrtPCRData;
import org.grits.toolbox.entry.qrtpcr.model.QrtPCRRun;
import org.grits.toolbox.entry.qrtpcr.model.QrtPCRTable;
import org.grits.toolbox.entry.qrtpcr.property.QrtPCRProperty;
import org.grits.toolbox.entry.qrtpcr.util.FileUtils;
import org.grits.toolbox.entry.qrtpcr.util.GeneUtils;


public class EvaluateData {
	
	private static final Logger logger = Logger.getLogger(ExportQrtPCRTable.class);
	public static final String COMMAND_ID = "org.grits.toolbox.entry.qrtpcr.evaluateData";
	public static final String DATAOPTION = "org.grits.toolbox.entry.qrtpcr.commandparameter.dataOption";
	
	@Inject private static IGritsDataModelService gritsDataModelService = null;
	
	@Execute
	public Object execute(@Named(IServiceConstants.ACTIVE_SELECTION) Object object, 
			@Named(IServiceConstants.ACTIVE_PART) MPart part,  
			@Named (IServiceConstants.ACTIVE_SHELL) Shell shell, 
			@Optional @Named (DATAOPTION) String dataOptionParam) throws ExecutionException {
		// if it is executed from the menu, the dataOption is 0 (use current data)
		int dataOption = 0;
		if (dataOptionParam != null) {
			try {
				dataOption = Integer.parseInt(dataOptionParam);
			} catch (NumberFormatException e) {
				dataOption = 0;
			}
		}
		QrtPCRRun pcrRun=null;
		QrtPCRTableEditor editor=null;
		if (part != null && part.getObject() instanceof QrtPCRTableEditor ) {
			editor = (QrtPCRTableEditor) part.getObject();
			pcrRun = part.getContext().get(QrtPCRRun.class);
		}
		if (pcrRun != null) {
			for (Iterator<Integer> iterator = pcrRun.getRunIdTableMap().keySet().iterator(); iterator
					.hasNext();) {
				Integer runId = (Integer) iterator.next();
        		QrtPCRTable table = pcrRun.getRun(runId); 
			
				Map<String, QrtPCRData> plateMap = table.getPlateDataMap();
				Boolean original = table.getOriginal();
				
				for (Iterator<String> itr = plateMap.keySet().iterator(); itr.hasNext();) {
					String plateId = (String) itr.next();
					applyRules (plateMap.get(plateId), dataOption, pcrRun.getThreshold(), pcrRun.getStDevCutOff());
					if (runId > 0) {
						// update the original run with the new data after applying rules
						((QrtPCRTableEditor) editor).updateFirstRun (plateMap.get(plateId), runId);
					}
					if (original == null || original) { // add the pages for the first time 
						((QrtPCRTableEditor) editor).addPlatePage(runId, plateId, table.getInstrumentFileMap().get(plateId), false);
						table.setOriginal(false);
					}
				}
				part.getContext().set(QrtPCRRun.class, pcrRun);
				((QrtPCRTableEditor) editor).refreshInput(pcrRun);
				((QrtPCRTableEditor) editor).markDirty();
				
			}
		} else {
			// executed from the menu, find the selection and the entry
			// get the table info from the entry
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
	        
	        if (selectedEntry != null) {
	        	if (editor != null) {
		        	// if editor has unsaved content, warn the user
		        	if (editor.isDirty()) {
		        		MessageDialog.openWarning(shell, "Unsaved content", "You have unsaved data in the editor. Please make sure to save it before \"Evaluate\"");
		        		return null;
		        	}
		    		// 1 - load the table
		        	QrtPCRProperty prop = (QrtPCRProperty) selectedEntry.getProperty();
		        	String filename = prop.getFilename();
		        	try {
		        		QrtPCRRun run = FileUtils.loadQrtPCRRun (selectedEntry, filename);
		        		for (Iterator<Integer> iterator = run.getRunIdTableMap().keySet().iterator(); iterator
								.hasNext();) {
							Integer runId = (Integer) iterator.next();
			        		QrtPCRTable table = run.getRun(runId); 
			        		Boolean original = table.getOriginal();	
			        		Map<String, QrtPCRData> plateMap = table.getPlateDataMap();
		    				for (Iterator<String> itr = plateMap.keySet().iterator(); itr.hasNext();) {
		    					String plateId = (String) itr.next();
		    					applyRules (plateMap.get(plateId), dataOption, run.getThreshold(), run.getStDevCutOff());	
		    					if (runId > 0) {
		    						// update the original run with the new data after applying rules
		    						((QrtPCRTableEditor) editor).updateFirstRun (plateMap.get(plateId), runId);
		    					}
		    					if (original == null || original) { // add the pages for the first time 
			    					((QrtPCRTableEditor) editor).addPlatePage(runId, plateId, table.getInstrumentFileMap().get(plateId), false);
			    					table.setOriginal(false);
		    					}
		    				}
		    				((QrtPCRTableEditor) editor).markDirty();
			        		
		        		}
		        	} catch (JAXBException e) {
		            	logger.error("Error loading qrtPCR Data", e);
		            	ErrorUtils.createErrorMessageBox(shell, "Error loading qrtPCR Data", e);
		            } catch (FileNotFoundException e) {
		            	logger.error("Error locating qrtPCR Data", e);
		            	ErrorUtils.createErrorMessageBox(shell, "Error loading qrtPCR Data", e);
		    		} catch (UnsupportedEncodingException e) {
		    			logger.error("Error loading qrtPCR Data", e);
		    			ErrorUtils.createErrorMessageBox(shell, "Error loading qrtPCR Data", e);
		    		}
	        	}
	        }
	        else {
	        	MessageDialog.openError(shell, "No Selection", "Please select a qrt-PCR entry from the list first!");
        		return null;
	        }
		}
		return null;
	}
	
	public static boolean applyRules (QrtPCRData data, int dataOption, Double lowerThreshold, Double stDevCutOff) {
		try {
			List<Gene> genes = data.getGenes();
			if (dataOption == 1) {
				// revert the data to the original/raw values
				for (Gene gene : genes) {
					List<GeneData> geneDataList = gene.getDataMap().get(0);
					for (GeneData geneData : geneDataList) {
						CtHistory originalValue = null;
						List<CtHistory> previousValues = geneData.getPreviousValues();
						if (previousValues != null && !previousValues.isEmpty() ) {
							originalValue = previousValues.get(0);
							geneData.getPreviousValues().clear();
						}
					      
						if (originalValue != null) {
							geneData.setCt(originalValue.getCt());
						}
					}
				}
			} else if (dataOption == 2) {
				// remove previous evaluation changes, keep manual modifications
				for (Gene gene : genes) {
					List<GeneData> geneDataList = gene.getDataMap().get(0);
					for (GeneData geneData : geneDataList) {
						Double toKeep = geneData.getCt();
						Double previousCt;
						List<CtHistory> previousValues = geneData.getPreviousValues();
						List<CtHistory> toBeRemoved = new ArrayList<>();
						if (previousValues != null && !previousValues.isEmpty()) {
							previousCt = previousValues.get(previousValues.size()-1).getCt(); 
							for (int i=previousValues.size()-1; i >= 0; i--) {
								CtHistory ctHistory = previousValues.get(i);
								if (ctHistory.getReasonCode().equals(Reason.USERCHANGE.getReason()) ) {
									break;
								}
								if (ctHistory.getReasonCode().equals(Reason.AVERAGE.getReason())) {
									toKeep = previousCt;
									previousCt = ctHistory.getCt();
									toBeRemoved.add(ctHistory);
								}
							}
							previousValues.removeAll(toBeRemoved);
							geneData.setCt(toKeep);
						}   
					}
				}
			}
			
			// clean up genes' should re-run values, new evaluation will set it according to the new values and cut-offs
			for (Gene gene : genes) {
				gene.setShouldRerun(false);
			}
			
			boolean modified = false;
			// go over the data to determine re-runs and modified data
			for (Gene gene : genes) {
				gene.setNumOfReplicates(data.getNumberOfReplicates());
				double stdev = gene.getStandardDeviation(0, lowerThreshold);  
				if (stdev >= stDevCutOff) {
					gene.checkAndMarkForRerun(0, lowerThreshold, stDevCutOff);
					if (!gene.getShouldRerun()) {
						List<GeneData> modifiedList = GeneUtils.findBestValuesAndReplace (gene.getDataMap().get(0), data.getNumberOfReplicates(), lowerThreshold, stDevCutOff);
						if (modifiedList == null) {
							modified = true;
							gene.setShouldRerun(true);
						}
						else {
							int k =0;
							List<GeneData> original = gene.getDataMap().get(0);
							for (GeneData geneData2 : original) {
								GeneData geneDataNew = modifiedList.get(k);
								if (!geneData2.equals(geneDataNew)) {
									if (geneData2.getCt() != null)
										geneData2.addPreviousValue(geneData2.getCt(), CtHistory.Reason.AVERAGE.getReason());
									geneData2.setCt(geneDataNew.getCt());
									modified = true;
									break;
								}
								k++;
							}
						}
					} else {
						modified = true;
					}
				}
			}
			data.calculateNormValue();
	
			return modified;
		} catch (Exception e) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(), "Erorr", "Error evaluating data. Reason: " +  e.getMessage());
			logger.error("Exception evaluating data. " , e);
			return false;
		}
	}
}
