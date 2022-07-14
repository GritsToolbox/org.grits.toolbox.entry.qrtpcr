package org.grits.toolbox.entry.qrtpcr.commands;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.io.ProjectFileHandler;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.datamodel.property.ReportsProperty;
import org.grits.toolbox.core.datamodel.util.DataModelSearch;
import org.grits.toolbox.core.service.IGritsDataModelService;
import org.grits.toolbox.core.service.IGritsUIService;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.grits.toolbox.entry.qrtpcr.Config;
import org.grits.toolbox.entry.qrtpcr.dialogs.QrtPCRMergeReportDialog;
import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.QrtPCRData;
import org.grits.toolbox.entry.qrtpcr.model.QrtPCRRun;
import org.grits.toolbox.entry.qrtpcr.model.QrtPCRTable;
import org.grits.toolbox.entry.qrtpcr.model.merge.MergeData;
import org.grits.toolbox.entry.qrtpcr.model.merge.MergedQrtPCR;
import org.grits.toolbox.entry.qrtpcr.property.QrtPCRMergeProperty;
import org.grits.toolbox.entry.qrtpcr.property.QrtPCRProperty;
import org.grits.toolbox.entry.qrtpcr.util.FileUtils;

public class CreateMergeReport {
	//log4J Logger
	private static final Logger logger = Logger.getLogger(CreateMergeReport.class);
	
	@Inject private static IGritsDataModelService gritsDataModelService = null;
	@Inject static IGritsUIService gritsUIService = null;

	@Execute
	public Object execute(@Named(IServiceConstants.ACTIVE_SELECTION) Object object,
			IEventBroker eventBroker, @Named (IServiceConstants.ACTIVE_SHELL) Shell shell) throws ExecutionException {
		
		StructuredSelection to = null;
		Entry selectedEntry = null;
		if(object instanceof Entry)
		{
			selectedEntry = (Entry) object;
		}
		else if (object instanceof StructuredSelection)
		{
			if(((StructuredSelection) object).getFirstElement() instanceof Entry)
			{
				to = (StructuredSelection) object;
			}
		}
		// try getting the last selection from the data model
		if(selectedEntry == null
				&& gritsDataModelService.getLastSelection() != null
				&& gritsDataModelService.getLastSelection().getFirstElement() instanceof Entry)
		{
			to = gritsDataModelService.getLastSelection();
		}

		
		if(to == null)
		{
			if (selectedEntry != null) {
				if(selectedEntry.getProperty().getType().equals(QrtPCRProperty.TYPE)) {
					List<Entry> entries = new ArrayList<Entry>();
					entries.add(selectedEntry);
					createMergeReportDialog(shell, entries, eventBroker);
				} else {
					createMergeReportDialog(shell,null, eventBroker);
				}
			}
			else {
				//then create dialog with empty list
				createMergeReportDialog(shell,null, eventBroker);
			}
		}
		else
		{
			//if more than one entry is selected, check if all entries are correctly chosen
			//Path contains all entries that are selected and other entries along the path to the its project parent.
			
			boolean correctEntries = true;
			List<Entry> entries = new ArrayList<Entry>();
			for (Iterator<?> iterator = to.iterator(); iterator.hasNext();) {
				Entry qrtPCREntry = (Entry) iterator.next();
				//if the right property
				if(qrtPCREntry.getProperty().getType().equals(QrtPCRProperty.TYPE))
				{
					entries.add(qrtPCREntry);	
				}
				else
				{
					//if not qrtPCRProperty then set the flag false;
					correctEntries = false;
					//and get out of for loop
					break;
				}
			}

			//check the flag
			if(correctEntries)
			{
				//then create the dialog with all the correctly selected entries.
				createMergeReportDialog(shell,entries, eventBroker);
			}
			else
			{
				//then create the dialog with empty list
				createMergeReportDialog(shell,null, eventBroker);
			}
		}
		return null;
	}

	private void createMergeReportDialog(Shell activeShell, List<Entry> entries, IEventBroker eventBroker) {
		QrtPCRMergeReportDialog dialog = new QrtPCRMergeReportDialog(PropertyHandler.getModalDialog(activeShell),entries);
		if(dialog.open() == 0)
		{
			//activeShell is closed already. Thus create a new shell for errors
			Shell modalDialog = PropertyHandler.getModalDialog(new Shell());

			//get the workspace location 
			String workspaceLocation = PropertyHandler.getVariable("workspace_location");
			//get the project name//at least one entry has to be there
			Entry projectEntry = DataModelSearch.findParentByType(dialog.getQrtPCREntryList().get(0), ProjectProperty.TYPE);
			String projectName = projectEntry.getDisplayName();
			
			Entry reportsEntry = null;
			//look for reports entry if not, then create a new one.
			for(Entry child: projectEntry.getChildren())
			{
				//reports is right under project entry
				if(child.getProperty().getType().equals(ReportsProperty.TYPE))
				{
					reportsEntry = child;
				}
			}

			//no reportsEntry is found
			if(reportsEntry == null)
			{
				//create a new reports entry
				reportsEntry = new Entry();
				reportsEntry.setDisplayName("reports");
				reportsEntry.setProperty(new ReportsProperty());

				//then create a new Reports Entry 
				try
				{
					PropertyHandler.getDataModel().setShow(false);
					gritsDataModelService.addEntry(projectEntry, reportsEntry);
					try
					{
						ProjectFileHandler.saveProject(projectEntry);
					} catch (IOException e)
					{
						logger.error("Something went wrong while saving project entry \n" + e.getMessage(),e);
						logger.fatal("Closing project entry \""
								+ projectEntry.getDisplayName() + "\"");
						gritsDataModelService.closeProject(projectEntry);
						throw e;
					}
				} catch(IOException e)
				{
					//need to log
					logger.error(e.getMessage(),e);
					//need to show an error msg
					ErrorUtils.createErrorMessageBox(modalDialog, e.getMessage(), e);
					return;
				}
			}

			//check if reports folder is in the project or not
			//if not create a new one
			File reportsFolder = new File(workspaceLocation+projectName+ File.separator +ReportsProperty.getFolder());
			if(!reportsFolder.exists())
			{
				if(!reportsFolder.mkdirs())
				{
					//need to log also
					logger.error("Cannot create the folder: " + reportsFolder.getAbsolutePath());
					//need to show en error msg
					ErrorUtils.createWarningMessageBox(modalDialog, "Error While Creating A Folder", "Cannot create the folder: " + reportsFolder.getAbsolutePath());
					return;
				}
			}

			//need to create qrtpcr merge folder for this merge..
			File mergeFolder = new File(reportsFolder+ File.separator + Config.folderName);
			if(!mergeFolder.exists())
			{
				if(!mergeFolder.mkdirs())
				{
					//need to log also
					logger.error("Cannot create the folder: " + mergeFolder.getAbsolutePath());
					//need to show en error msg
					ErrorUtils.createWarningMessageBox(modalDialog, "Error While Creating A Folder", "Cannot create the folder: " + mergeFolder.getAbsolutePath());
					return;
				}
			}
			
			// load the data from the selected entries and create the merged data
			Entry qrtPCRMergeEntry = dialog.createEntry();
			QrtPCRMergeProperty property = (QrtPCRMergeProperty) qrtPCRMergeEntry.getProperty();
			//create QrtPCR Entry and save the data in the file
			String qrtPCRMergeFileName = createQrtPCRMergeFile(mergeFolder, dialog.getQrtPCREntryList(), dialog.getName(), dialog.getDescription(), dialog.getListEntries());
			property.setFilename(qrtPCRMergeFileName);
			qrtPCRMergeEntry.setParent(reportsEntry);
			
			// saveEntry adds this entry to the project tree and saves the .project file
			try {
				gritsDataModelService.addEntry(reportsEntry, qrtPCRMergeEntry);
				try
				{
					ProjectFileHandler.saveProject(projectEntry);
				} catch (IOException e)
				{
					logger.error("Something went wrong while saving project entry \n" + e.getMessage(),e);
					logger.fatal("Closing project entry \""
							+ projectEntry.getDisplayName() + "\"");
					gritsDataModelService.closeProject(projectEntry);
					throw e;
				}
				if(qrtPCRMergeEntry != null)
				{
					eventBroker.send(IGritsDataModelService.EVENT_SELECT_ENTRY, qrtPCRMergeEntry);
					gritsUIService.openEntryInPart(qrtPCRMergeEntry);
				}
			} catch (IOException e) {
				//need to log also
				logger.error("Cannot add the merge report: " + qrtPCRMergeEntry.getDisplayName(), e);
				//need to show en error msg
				ErrorUtils.createErrorMessageBox(modalDialog, "Error while adding merge report",  e);
				return;
			}  	
		}
	}

	private String createQrtPCRMergeFile(File mergeFolder,
			List<Entry> qrtPCREntryList, String name, String description, Map<String, String> aliasList) {
		// create a unique file name inside the folder
        String qrtPCRMergeFileName = generateFileName (mergeFolder.list());
        
        try {
        	MergedQrtPCR mergeData = createMergedData(qrtPCREntryList, name, description, aliasList);
	        // save the merge report
	        ByteArrayOutputStream os = new ByteArrayOutputStream();
            JAXBContext context = JAXBContext.newInstance(MergedQrtPCR.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, PropertyHandler.GRITS_CHARACTER_ENCODING);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(mergeData, os);

            //write the serialized data to the folder
            FileWriter fileWriter = new FileWriter(mergeFolder.getAbsolutePath() 
                    + File.separator + qrtPCRMergeFileName);
            fileWriter.write(os.toString((String)marshaller.getProperty(Marshaller.JAXB_ENCODING)));
            fileWriter.close();
            os.close();
        } catch (FileNotFoundException | UnsupportedEncodingException
				| JAXBException e) {
			logger.error("Cannot create the merge report: " + name, e);
			//need to show en error msg
			ErrorUtils.createErrorMessageBox(Display.getCurrent().getActiveShell(), "Error while creating merge report",  e);
		} catch (IOException e) {
			logger.error("Cannot save the merge report: " + name, e);
			//need to show en error msg
			ErrorUtils.createErrorMessageBox(Display.getCurrent().getActiveShell(), "Error while save merge report",  e);
		}
		return qrtPCRMergeFileName;
	}
	
	private MergedQrtPCR createMergedData(List<Entry> qrtPCREntryList, String name, String description, Map<String, String> aliasList) throws FileNotFoundException, UnsupportedEncodingException, JAXBException {
		MergedQrtPCR mergeQrtPCR = new MergedQrtPCR();
		mergeQrtPCR.setName(name);
		mergeQrtPCR.setDescription(description);
		mergeQrtPCR.setAliasList (aliasList);
		
        
        Map<Gene, List<MergeData>> mergeDataMap = new HashMap<>();
        
        // go through each selected entry and add their genes to the merge report
        for (Entry entry : qrtPCREntryList) {	
			QrtPCRRun run = FileUtils.loadQrtPCRRun(entry, ((QrtPCRProperty)entry.getProperty()).getFilename());
			mergeQrtPCR.setStDevCutOff(run.getStDevCutOff());
			QrtPCRTable qrtPCRTable = run.getFirstRun();
			for (Iterator<String> iterator = qrtPCRTable.getPlateDataMap().keySet().iterator(); iterator.hasNext();) {
				String plateId= (String) iterator.next();
				QrtPCRData data = qrtPCRTable.getData(plateId);
				for (Gene gene : data.getGenes()) {
					List<MergeData> mergeDataList = null;
					for (Iterator<Gene> iterator2 = mergeDataMap.keySet().iterator(); iterator2
							.hasNext();) {
						Gene gene2 = (Gene) iterator2.next();
						if (gene.getGeneIdentifier().equals(gene2.getGeneIdentifier())) {
							mergeDataList = mergeDataMap.get(gene2);
							mergeDataMap.remove(gene2);
							break;
						}
					}
					
					if (mergeDataList == null) {
						mergeDataList = new ArrayList<>();
					}
					MergeData mergeData = new MergeData();
					mergeData.setEntryName(aliasList.get(entry.getDisplayName()));
					mergeData.setAverage(gene.getAdjustedAverage(gene.getRunId(), run.getThreshold(),gene.getNormValue(gene.getRunId())));
					mergeData.setStDev(gene.getStDevForAdjusted(gene.getRunId(), run.getThreshold(),gene.getNormValue(gene.getRunId())));
					mergeDataList.add(mergeData);
					mergeDataMap.put(gene, mergeDataList);
				}
			}
		}
        mergeQrtPCR.setQrtPCRGeneListMap(mergeDataMap);
		return mergeQrtPCR;     
	}

	public static String generateFileName(String[] existingNames)
    {
        String fileName = "";
        int randomLength = 0;
        do 
        {
            fileName = Config.entryNameExtension;
            while(randomLength < Config.FILE_NAME_RANDOM_CHARACTERS_LENGTH) 
            {
                int randomcharacter = (int) (Math.random()*10);
                randomLength++;
                fileName = fileName + randomcharacter;
            }
            fileName = fileName + Config.FILE_TYPE_OF_QRTPCR;
        }
        while (Arrays.asList(existingNames).contains(fileName));
        return fileName;
    }
}
