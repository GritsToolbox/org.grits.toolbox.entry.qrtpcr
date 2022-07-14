package org.grits.toolbox.entry.qrtpcr.manager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.grits.toolbox.core.dataShare.IGritsConstants;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.grits.toolbox.entry.qrtpcr.Config;
import org.grits.toolbox.entry.qrtpcr.editor.IQrtPCRPart;
import org.grits.toolbox.entry.qrtpcr.manager.pages.GeneListOverviewPage;
import org.grits.toolbox.entry.qrtpcr.manager.pages.MasterGeneListPage;
import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.GeneList;
import org.grits.toolbox.entry.qrtpcr.preference.QrtPCRPreference;
import org.grits.toolbox.entry.qrtpcr.table.manager.ManagerNatTable;
import org.grits.toolbox.entry.qrtpcr.util.FileUtils;
import org.grits.toolbox.entry.qrtpcr.util.GeneUtils;

public class GeneListManagerEditor implements IQrtPCRPart{
	
	public static String ID="org.grits.toolbox.entry.qrtpcr.managerEditor";
	
	private static final Logger logger = Logger.getLogger(GeneListManagerEditor.class);
	
	public static final String MASTER_LIST = "masterList";
	public static final String MASTER_SUB_LIST = "masterSubList";
	public static final String GENE_SUB_LIST = "geneSubList";
	public static final String GENE_LIST = "geneList";
	
	List<GeneList> masterLists;
	List<GeneList> masterSubLists;
	
	List<File> masterGeneListFiles;
	List<File> masterGeneSubListFiles;

	@Inject private MDirtyable dirtyable = null;

	private GeneListOverviewPage overviewPage;
	
	private Map<CTabItem, IQrtPCRPart> cTabItemToPartTabMap =
			new HashMap<CTabItem, IQrtPCRPart>();
	private CTabFolder cTabFolder;
	
	@Inject
	public GeneListManagerEditor(@Named(IGritsConstants.CONFIG_LOCATION) String configLocation) {
		String configFolderLocation = configLocation + File.separator + Config.configFolderName;
    	File configFolder = new File (configFolderLocation);
    	if (!configFolder.exists()) {
			try {
				FileUtils.copyMasterGeneListsFromJar();
			} catch (IOException e) {
				logger.warn("There are no master gene list files defined in the system", e);
				MessageDialog.openWarning(Display.getCurrent().getActiveShell(), "There are no master gene list files defined in the system", e.getMessage());
			}
    	}
    	masterGeneListFiles = new ArrayList<File>();
    	masterGeneListFiles.addAll(Arrays.asList(configFolder.listFiles()));
	
	
		configFolderLocation = configLocation + File.separator + Config.configFolderName
				+ File.separator + Config.configSubFolder;
    	configFolder = new File (configFolderLocation);
    	masterGeneSubListFiles = new ArrayList<File>();
    	if (configFolder.exists()) {
	    	masterGeneSubListFiles.addAll(Arrays.asList(configFolder.listFiles()));
    	}
	}

	protected void addPages(MPart part) {
		if (masterLists == null)
			return;
		
		CTabItem cTabItem = new CTabItem(cTabFolder, SWT.NONE);
		cTabItem.setText("Overview");
		cTabItem.setShowClose(false);
		part.getContext().set(CTabItem.class, cTabItem);
		part.getContext().set(MASTER_LIST, masterLists);
		part.getContext().set(MASTER_SUB_LIST, masterSubLists);
		overviewPage = ContextInjectionFactory.make(
				GeneListOverviewPage.class, part.getContext());
		getcTabItemToPartTabMap().put(cTabItem, overviewPage);
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent, final MPart part)
	{
		logger.info("Creating tabs");
		cTabFolder = new CTabFolder(parent, SWT.NONE);
		cTabFolder.setTabPosition(SWT.BOTTOM);
		//cTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		cTabFolder.setSimple(false);
		part.getContext().set(CTabFolder.class, cTabFolder);
		
		masterLists = new ArrayList<GeneList>();
		masterSubLists = new ArrayList<>();
		try {
			loadFiles (masterLists, masterGeneListFiles);
			loadFiles (masterSubLists, masterGeneSubListFiles);
			//setPartName(input.getName());
		} catch (FileNotFoundException e) {
			logger.error("Cannot find the gene list", e);
			ErrorUtils.createErrorMessageBox(Display.getCurrent().getActiveShell(), "Cannot find the gene list");
		} catch (UnsupportedEncodingException e) {
			logger.error("Cannot retrieve the gene list", e);
			ErrorUtils.createErrorMessageBox(Display.getCurrent().getActiveShell(), "Cannot retrieve the gene list");
		} catch (JAXBException e) {
			logger.error("Cannot retrieve the gene list", e);
			ErrorUtils.createErrorMessageBox(Display.getCurrent().getActiveShell(), "Cannot retrieve the gene list");
		} catch (IOException e) {
			logger.error("Cannot retrieve the gene list", e);
			ErrorUtils.createErrorMessageBox(Display.getCurrent().getActiveShell(), "Cannot retrieve the gene list");
		}
		
		cTabFolder.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				changeTab(e);
			}

			private void changeTab(SelectionEvent e)
			{
				int selectionIndex = cTabFolder.getSelectionIndex();

				logger.info("Tab changed to " + selectionIndex);
				selectionIndex = selectionIndex < 0 ? 0 : selectionIndex;
				IQrtPCRPart currentTab = getcTabItemToPartTabMap().get(cTabFolder.getSelection());
				part.getContext().set(IQrtPCRPart.class, currentTab);

				// an added notification for the selected tab for specialized action
				cTabFolder.getSelection().notifyListeners(SWT.Selection, new Event());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				changeTab(e);
			}
		});

		addPages(part);
		
		int selectionIndex = getcTabItemToPartTabMap().size()-1;
		cTabFolder.setSelection(selectionIndex);
		cTabFolder.notifyListeners(SWT.Selection, new Event());
	}
	
	private void loadFiles (List<GeneList> geneLists, List<File> files) throws JAXBException, IOException {
		for (File file : files) {
			FileInputStream inputStream;
			if (file.isDirectory())  // skip directories
				continue;
			if (file.isHidden()) // skip hidden files
				continue;
			inputStream = new FileInputStream(file.getAbsolutePath());
            InputStreamReader reader = new InputStreamReader(inputStream, PropertyHandler.GRITS_CHARACTER_ENCODING);
            JAXBContext context = JAXBContext.newInstance(GeneList.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            GeneList geneList = (GeneList) unmarshaller.unmarshal(reader);
            geneList.setFilename(file.getName());
            if (geneList.getGenes() == null) 
            	// set it to an empty list
            	geneList.setGenes(new ArrayList<Gene>());
            geneLists.add(geneList);
            reader.close();
            inputStream.close();
		}
	}

	@Persist
	public void doSave() {
		if (masterLists == null)
			// nothing to save
			return;
		String configFolderLocation = PropertyHandler.getVariable("configuration_location") + File.separator + Config.configFolderName;
		for (GeneList geneList : masterLists) {
			try {
				// before saving, clean up data related attributes (such as runId, shouldRerun etc.)
				GeneUtils.cleanUpGenesForMasterGeneList(geneList);
	        	// save the gene list
		        ByteArrayOutputStream os = new ByteArrayOutputStream();
	            JAXBContext context = JAXBContext.newInstance(GeneList.class);
	            Marshaller marshaller = context.createMarshaller();
	            marshaller.setProperty(Marshaller.JAXB_ENCODING, PropertyHandler.GRITS_CHARACTER_ENCODING);
	            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	            marshaller.marshal(geneList, os);

	            //write the serialized data to the folder
	            FileWriter fileWriter = new FileWriter(configFolderLocation + File.separator + geneList.getFilename());
	            fileWriter.write(os.toString((String)marshaller.getProperty(Marshaller.JAXB_ENCODING)));
	            fileWriter.close();
	            os.close();
	        	
	        } catch (IOException e) {
	            logger.error("The changes made could not be written to the file.", e);
	            MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error Writing File", 
	                    "The changes made could not be written to the file.");
	        } catch (JAXBException e) {
	            logger.error("The changes made could not be serialized as xml.", e);
	            MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error Parsing File", 
	                    "The changes made could not be serialized to xml.");
	        }
		}
		
		resetDirtyFlag();
	}

	public boolean isDirty()
    {
        return dirtyable.isDirty();
    }

    private void resetDirtyFlag()
    {
       dirtyable.setDirty(false);
    }

    public void markDirty()
    {
    	dirtyable.setDirty(true);;
    }

	public void refreshOverview(GeneList newSublist) {
		overviewPage.addSubList(newSublist);
	}
	
	public void refreshGeneListPage (List<Gene> existing) {
		IQrtPCRPart activePage = getcTabItemToPartTabMap().get(cTabFolder.getSelection());
		if (activePage instanceof MasterGeneListPage) {
			((MasterGeneListPage) activePage).refresh(existing);
		}
	}
	
	public void updateColumnPreferences(QrtPCRPreference updatePref) {
		IQrtPCRPart activePage = getcTabItemToPartTabMap().get(cTabFolder.getSelection());
		if (activePage instanceof MasterGeneListPage) {
			updateColumnPreferences(((MasterGeneListPage) activePage).getTable(), (QrtPCRPreference) ((MasterGeneListPage) activePage).getTable().getTablePreference(), updatePref);
		}
	}
	
	protected void updateColumnPreferences( ManagerNatTable table, QrtPCRPreference curPref, QrtPCRPreference updatePref ) {
		if( curPref.getClass().equals(updatePref.getClass())) {
			// don't update if not changed!
			if( ! updatePref.getColumnSettings().equals(curPref.getColumnSettings()) ) {
				table.setTablePreference( updatePref );
				table.updateViewFromPreferenceSettings();						
			}
		}
	}	
	
	public Map<CTabItem, IQrtPCRPart> getcTabItemToPartTabMap() {
		return cTabItemToPartTabMap;
	}
}
