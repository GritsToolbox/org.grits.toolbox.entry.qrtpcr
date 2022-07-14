package org.grits.toolbox.entry.qrtpcr.commands;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.service.IGritsDataModelService;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.grits.toolbox.entry.qrtpcr.model.QrtPCRRun;
import org.grits.toolbox.entry.qrtpcr.property.QrtPCRProperty;
import org.grits.toolbox.entry.qrtpcr.util.ExcelFileHandler;
import org.grits.toolbox.entry.qrtpcr.util.FileUtils;

public class ExportQrtPCRTable {

	private static final Logger logger = Logger.getLogger(ExportQrtPCRTable.class);
	@Inject private static IGritsDataModelService gritsDataModelService = null;
	
	@Execute
	public Object execute(@Named(IServiceConstants.ACTIVE_SELECTION) Object object,
			@Named (IServiceConstants.ACTIVE_SHELL) Shell shell) throws ExecutionException {
		logger.debug("BEGIN ExportQrtPCRTable");
        
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
        		String[] possibleSelections;
        		if (pcrRun.getRunIdTableMap().size() > 1)  { // reruns
        			possibleSelections = new String[] {"Master Table", "Plate Data", "Re-run Data"};
        		}
        		else {
        			possibleSelections = new String[] {"Master Table", "Plate Data"};
        		}
        		LabelProvider labelProvider = new LabelProvider() {
					@Override
					public String getText(Object element) {
						return (String)element;
					}
				};
				boolean exportMaster = false;
				boolean exportPlateData = false;
				boolean exportReruns = false;
        		ListSelectionDialog dialog = new ListSelectionDialog(shell, possibleSelections, new ArrayContentProvider(), labelProvider, "Please select the parts you would like to export");
				dialog.setTitle("Export Options");
				dialog.setInitialSelections(new String[] {"Plate Data"});
				if (dialog.open() == Window.OK) {
					Object[] selections = dialog.getResult();
					if (selections == null || selections.length == 0) {
						ErrorUtils.createWarningMessageBox(shell, "No Selection", "You have not selected anything to export");
						return null;
					}
					for (Object col : selections) {
						String sel= (String)col;
						if (sel.equals("Master Table"))
							exportMaster = true;
						if (sel.equals("Plate Data"))
							exportPlateData = true;
						if (sel.equals("Re-run Data"))
							exportReruns = true;
					}	
					// 2 - ask for a filename
	        		FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
	        		// Set the text
	                fileDialog.setText("Select File");
	                // Set filter on .xls files
	                fileDialog.setFilterExtensions(new String[] { "*.xlsx" });
	                // Put in a readable name for the filter
	                fileDialog.setFilterNames(new String[] { "Excel (*.xlsx)" });
	                fileDialog.setFileName(selectedEntry.getDisplayName() + ".xlsx");
	                fileDialog.setOverwrite(true);
	                // Open Dialog and save result of selection
	                String selected = fileDialog.open();
	                if (selected != null) {
	                	// 3 - export data into excel
	                	ExcelFileHandler.exportQrtPCRTable(pcrRun, selected, exportMaster, exportPlateData, exportReruns);
	                }
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
    		} catch (IOException e) {
    			logger.error("Error generating Excel file", e);
    			ErrorUtils.createErrorMessageBox(shell, "Error generating Excel file", e);
			}
    	} 
		
        logger.debug("END ExportQrtPCRTable");
		return null;
	}

}
