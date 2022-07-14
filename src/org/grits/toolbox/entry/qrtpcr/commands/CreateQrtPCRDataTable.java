package org.grits.toolbox.entry.qrtpcr.commands;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.io.ProjectFileHandler;
import org.grits.toolbox.core.service.IGritsDataModelService;
import org.grits.toolbox.core.service.IGritsUIService;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.grits.toolbox.entry.qrtpcr.Activator;
import org.grits.toolbox.entry.qrtpcr.Config;
import org.grits.toolbox.entry.qrtpcr.dialogs.UploadDataWizard;
import org.grits.toolbox.entry.qrtpcr.model.QrtPCRRun;
import org.grits.toolbox.entry.qrtpcr.model.QrtPCRTable;
import org.grits.toolbox.entry.qrtpcr.property.QrtPCRProperty;
import org.grits.toolbox.entry.sample.property.SampleProperty;

public class CreateQrtPCRDataTable {
	
	private static final Logger logger = Logger.getLogger(CreateQrtPCRDataTable.class);
	
	@Inject private static IGritsDataModelService gritsDataModelService = null;
	@Inject static IGritsUIService gritsUIService = null;
	
	@Execute
	public Object execute(@Named(IServiceConstants.ACTIVE_SELECTION) Object object,
			IEventBroker eventBroker, @Named (IServiceConstants.ACTIVE_SHELL) Shell shell) {
		logger.debug("BEGIN CreateQrtPCRDataTable");
        
        Entry qrtPCREntry = null;
        
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
					|| !SampleProperty.TYPE.equals(selectedEntry.getProperty().getType()))
			{
				selectedEntry = null;
			}
		}

		//create a new dialog to create a new experiment
		qrtPCREntry = createNewQrtPCRTable(shell, selectedEntry);
        
        if(qrtPCREntry != null)
		{
			eventBroker.send(IGritsDataModelService.EVENT_SELECT_ENTRY, qrtPCREntry);
			gritsUIService.openEntryInPart(qrtPCREntry);
		}
		
        logger.debug("END CreateQrtPCRDataTable");
		return qrtPCREntry;
	}

	private Entry createNewQrtPCRTable(Shell shell, Entry sample) {
		UploadDataWizard wizard = new UploadDataWizard();
		wizard.setSampleEntry(sample);
		WizardDialog wizardDialog = new WizardDialog(shell,
			      wizard);
		if (wizardDialog.open() == Window.OK) {
			logger.debug("qrtPCR Wizard: Ok pressed");
		 
			try {
				sample = wizard.getSampleEntry();
				QrtPCRTable table = wizard.getTable();
				
				QrtPCRRun originalRun = new QrtPCRRun();
				originalRun.setGeneListFile(wizard.getMasterListFile());
				originalRun.addRun(table);
				originalRun.setThreshold(wizard.getThresholdSetting());
				originalRun.setStDevCutOff(wizard.getCutOffSetting());
				//create QrtPCR Entry and save the data in the file
				String qrtPCRFileName = createQrtPCRFile(sample, wizard.getEntryName(), originalRun);
			   
				// create an entry
				Entry qrtPCREntry = new Entry();
				qrtPCREntry.setDisplayName(wizard.getEntryName());
			
				QrtPCRProperty property = new QrtPCRProperty();
				property.setFilename(qrtPCRFileName);
			
				qrtPCREntry.setProperty(property);
				qrtPCREntry.setParent(sample);
			   
				gritsDataModelService.addEntry(sample, qrtPCREntry);
				try
				{
					ProjectFileHandler.saveProject(sample.getParent());
				} catch (IOException e)
				{
					logger.error("Something went wrong while saving project entry \n" + e.getMessage(),e);
					logger.fatal("Closing project entry \""
							+ sample.getParent().getDisplayName() + "\"");
					gritsDataModelService.closeProject(sample.getParent());
					throw e;
				}
				
				return qrtPCREntry;
			} catch (IOException e)
            {
                logger.error(Activator.PLUGIN_ID + " " + e.getMessage() ,e);
                ErrorUtils.createErrorMessageBox(shell, e.getMessage(),e);
            }
		} else {
			 logger.debug("qrtPCR Wizard: Cancel pressed");
		}
		
		return null;
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
	
	public static String createQrtPCRFile(Entry selectedSampleEntry, String qrtPCRName, QrtPCRRun originalRun) throws IOException
    {
        // get the experiment folder
        File qrtPCRFolder = getQrtPCRDirectory(selectedSampleEntry);
        // create a unique file name inside the folder
        String qrtPCRFileName = generateFileName (qrtPCRFolder.list());
        createFileContent(selectedSampleEntry, qrtPCRFolder, qrtPCRFileName, qrtPCRName, originalRun);
        return qrtPCRFileName;
    }
	
    private static void createFileContent(Entry selectedSampleEntry,
			File qrtPCRFolder, String qrtPCRFileName, String qrtPCRName,
			QrtPCRRun originalRun) throws IOException {
    	 // serialize the qrtPCR data to xml
        try
        {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            JAXBContext context = JAXBContext.newInstance(QrtPCRRun.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, PropertyHandler.GRITS_CHARACTER_ENCODING);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(originalRun, os);

            //write the serialized data to the folder
            FileWriter fileWriter = new FileWriter(qrtPCRFolder.getAbsolutePath() 
                    + File.separator + qrtPCRFileName);
            fileWriter.write(os.toString((String)marshaller.getProperty(Marshaller.JAXB_ENCODING)));
            fileWriter.close();
            os.close();
        } catch (JAXBException e) 
        {
        	throw new IOException("Error creating new file", e);
        } catch (IOException e)
        {
            throw e;
        } 
		
	}

	public static File getQrtPCRDirectory(Entry selectedSampleEntry)
    {
    	Entry projectEntry = selectedSampleEntry.getParent();
        String workspaceLocation = PropertyHandler.getVariable("workspace_location");
        String projectFolderLocation = workspaceLocation.substring(0, workspaceLocation.length()-1) 
                + File.separator
                + projectEntry.getDisplayName();
        String qrtPCRFolderLocation = projectFolderLocation
                + File.separator
                + Config.folderName;
        File qrtPCRFolder = new File(qrtPCRFolderLocation);
        if(!qrtPCRFolder.exists() || !qrtPCRFolder.isDirectory()) 
        {
        	qrtPCRFolder.mkdir();
        }
        return qrtPCRFolder;
    }

}
