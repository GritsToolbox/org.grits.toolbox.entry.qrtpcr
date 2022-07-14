package org.grits.toolbox.entry.qrtpcr.editor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.grits.toolbox.core.dataShare.IGritsConstants;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.preference.share.IGritsPreferenceStore;
import org.grits.toolbox.core.preference.share.PreferenceEntity;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.grits.toolbox.display.control.table.preference.TableViewerPreference;
import org.grits.toolbox.entry.qrtpcr.Activator;
import org.grits.toolbox.entry.qrtpcr.Config;
import org.grits.toolbox.entry.qrtpcr.model.CtHistory;
import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.GeneData;
import org.grits.toolbox.entry.qrtpcr.model.QrtPCRData;
import org.grits.toolbox.entry.qrtpcr.model.QrtPCRRun;
import org.grits.toolbox.entry.qrtpcr.model.QrtPCRTable;
import org.grits.toolbox.entry.qrtpcr.preference.QrtPCRGeneListTablePreference;
import org.grits.toolbox.entry.qrtpcr.preference.QrtPCRMasterTablePreference;
import org.grits.toolbox.entry.qrtpcr.preference.QrtPCRMergeTablePreference;
import org.grits.toolbox.entry.qrtpcr.preference.QrtPCRPreference;
import org.grits.toolbox.entry.qrtpcr.property.QrtPCRProperty;
import org.grits.toolbox.entry.qrtpcr.table.QrtPCRNatTable;
import org.grits.toolbox.entry.qrtpcr.util.FileUtils;

public class QrtPCRTableEditor implements IQrtPCRPart{
	
	private static final Logger logger = Logger.getLogger(QrtPCRTableEditor.class);
	public static final String ID = "org.grits.toolbox.entry.qrtpcr.editor";
	public static final String LOWER_THRESHOLD_CONTEXT = "lowerThresholdContext";
	public static final String STDEV_CONTEXT = "stDevContext";
	public static final String RAW_CONTEXT = "rawContext";
	public static final String RUNID_CONTEXT = "runIdContext";
	
	private QrtPCRRun pcrRun;
	private String fileLocation = null;

	OverviewPage overviewPage;
	private MasterTablePage masterTablePage;
	
	Composite parent;
	
	private CTabFolder cTabFolder = null;
	private Map<CTabItem, IQrtPCRPart> cTabItemToPartTabMap =
			new HashMap<CTabItem, IQrtPCRPart>();
	
	Entry entry;
	
	@Inject private MDirtyable dirtyable = null;
	@Inject IGritsPreferenceStore gritsPreferenceStore;

	@Inject
	public QrtPCRTableEditor(@Named(IServiceConstants.ACTIVE_SELECTION) Entry entry,
			@Named(IServiceConstants.ACTIVE_PART) MPart part, 
			@Named(IGritsConstants.WORKSPACE_LOCATION) String workspaceLocation) {
		
		try {	
			this.entry = entry;
            String projectFolderLocation = workspaceLocation.substring(0, workspaceLocation.length()-1) 
                    + File.separator
                    + entry.getParent().getParent().getDisplayName();
            String qrtPCRFolderLocation = projectFolderLocation
                    + File.separator
                    + Config.folderName;
            QrtPCRProperty qPCRProperty = ((QrtPCRProperty) entry.getProperty());
            // name of the qrtPCR data xml file 
            String fileName = qPCRProperty.getFilename();
      
            //file with absolute path
            fileLocation= qrtPCRFolderLocation 
                    + File.separator 
                    + fileName;
            File experimentFile = new File(fileLocation);
            FileInputStream inputStream = new FileInputStream(experimentFile.getAbsolutePath());
            InputStreamReader reader = new InputStreamReader(inputStream, PropertyHandler.GRITS_CHARACTER_ENCODING);
            JAXBContext context = JAXBContext.newInstance(QrtPCRRun.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            pcrRun = (QrtPCRRun) unmarshaller.unmarshal(reader);
            part.getContext().set(QrtPCRRun.class, pcrRun);
            reader.close();
            inputStream.close();
            part.setLabel(entry.getDisplayName());
		//	setPartName(entry.getDisplayName());  
		} catch (IOException e) {
			ErrorUtils.createErrorMessageBox(Display.getCurrent().getActiveShell(), "Cannot create the  QrtPCR Table", e);
			logger.error(Activator.PLUGIN_ID + " Cannot create the QrtPCR Table", e);
		} catch (JAXBException e) {
			ErrorUtils.createErrorMessageBox(Display.getCurrent().getActiveShell(), "Cannot Load the  QrtPCR Table", e);
			logger.warn(Activator.PLUGIN_ID + " Cannot Load the  QrtPCR Table", e);
		}
	}

	@Persist
	public void doSave() {
		if (updateFile())
			resetDirtyFlag();
	}
	
	private boolean updateFile()
    {
        boolean updated = false;
        try {
        	FileUtils.saveQrtPCRRun(pcrRun, fileLocation);
        	updated = true;
        } catch (IOException e) {
            logger.error("The changes made could not be written to the file.", e);
            MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error Writing File", 
                    "The changes made could not be written to the file.");
        } catch (JAXBException e) {
            logger.error("The changes made could not be serialized as xml.", e);
            MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error Parsing File", 
                    "The changes made could not be serialized to xml.");
        }
        return updated;
    }

	@Focus
	public void onFocus() {
		if (overviewPage != null)
			overviewPage.setFocus();
	}
	
	public void refreshInput (QrtPCRRun newRun) {
		this.pcrRun = newRun;
		if (masterTablePage != null) 
        	masterTablePage.refreshInput(newRun.getFirstRun());
		if (overviewPage != null)
			overviewPage.refreshInput(newRun);
	}
	
	@PostConstruct
	public void postConstruct(Composite parent, final MPart part)
	{
		this.parent = parent;
		parent.setLayout(new FillLayout());
		logger.info("Creating tabs");
		cTabFolder = new CTabFolder(parent, SWT.NONE);
		cTabFolder.setTabPosition(SWT.BOTTOM);
		cTabFolder.setSimple(false);
		part.getContext().set(CTabFolder.class, cTabFolder);
		part.getContext().set(QrtPCRTableEditor.class, this);
		try
		{
			
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
			cTabFolder.setSelection(0);
			cTabFolder.notifyListeners(SWT.Selection, new Event());
		}
		catch (Exception e)
		{
			logger.fatal("Error while adding tabs to the qrtpcr editor.\n" + e.getMessage(), e);
			MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error Creating Page", 
					"Some unexpected error occurred while opening the editor. "
							+ "Please contact developers for further information/help.");
			logger.fatal(e.getMessage(), e);
		}

		logger.info("END   : Creating QrtPCRTableEditor. ");
	}

	protected void addPages(MPart part) {
		logger.info("Adding overview tabs");
		CTabItem cTabItem = new CTabItem(cTabFolder, SWT.NONE);
		cTabItem.setText("Overview");
		cTabItem.setShowClose(false);
		part.getContext().set(QrtPCRRun.class, pcrRun);
		part.getContext().set(CTabItem.class, cTabItem);
		overviewPage = ContextInjectionFactory.make(
				OverviewPage.class, part.getContext());
		getcTabItemToPartTabMap().put(cTabItem, overviewPage);

		cTabItem = new CTabItem(cTabFolder, SWT.NONE);
		cTabItem.setText("Master Table");
		cTabItem.setShowClose(false);
		part.getContext().set(QrtPCRTable.class, pcrRun.getFirstRun());
		part.getContext().set(CTabItem.class, cTabItem);
		part.getContext().set(LOWER_THRESHOLD_CONTEXT, pcrRun.getThreshold());
		part.getContext().set(STDEV_CONTEXT, pcrRun.getStDevCutOff());
		
		masterTablePage = ContextInjectionFactory.make (MasterTablePage.class, part.getContext());
        getcTabItemToPartTabMap().put(cTabItem, masterTablePage);
		
	}
	
	public void addPlatePage (Integer runId, String plateId, String filename, boolean raw) {
		if (overviewPage != null) {
			overviewPage.addPageToPlateTable(runId, plateId, filename, raw);
		}
	}
	
	public int getNumberOfPages() { return cTabFolder.getItemCount();};


    private void resetDirtyFlag()
    {
        dirtyable.setDirty(false); 
    }

    public void markDirty()
    {
        dirtyable.setDirty(true);
        if (masterTablePage != null) {
        	masterTablePage.refreshInput();
        }
    }

	public void setOriginalForRun(Integer runId) {
		if (runId != null) {
			QrtPCRTable table = this.pcrRun.getRun(runId);
			table.setOriginal(false);
		}
	}
	
	public void updateFirstRun(QrtPCRData qrtPCRData, Integer runId) {
		if (runId == 0)
			return; // no need to update, it is already reflected on the first run
		QrtPCRTable firstRun = pcrRun.getFirstRun();
		for (Iterator<QrtPCRData> iterator = firstRun.getPlateDataMap().values().iterator(); iterator.hasNext();) {
			QrtPCRData data = (QrtPCRData) iterator.next();
			for (Gene gene2: qrtPCRData.getGenes()) {
				for (Gene gene : data.getGenes()) {
					if (gene.getGeneIdentifier().equals(gene2.getGeneIdentifier())) {
						List<GeneData> geneDataList = gene.getDataMap().get(runId);
						if (geneDataList == null) // control genes and common genes do not have multiple run values
							continue;
						int i=0;
						for (GeneData geneData : geneDataList) {
							Double newValue = gene2.getDataMap().get(0).get(i++).getCt();
							if (newValue != null && !newValue.equals(geneData.getCt())) {
								geneData.addPreviousValue(geneData.getCt(), CtHistory.Reason.RERUN.getReason());
								geneData.setCt(newValue);
							}
						}
						break;
					}
				}
			}
		}
	}
	
	public void updateFirstRun(Gene gene2, Integer runId) {
		if (runId == 0)
			return; // no need to update, it is already reflected on the first run
		QrtPCRTable firstRun = pcrRun.getFirstRun();
		for (Iterator<QrtPCRData> iterator = firstRun.getPlateDataMap().values().iterator(); iterator.hasNext();) {
			QrtPCRData data = (QrtPCRData) iterator.next();
			for (Gene gene : data.getGenes()) {
				if (gene.getGeneIdentifier().equals(gene2.getGeneIdentifier())) {
					List<GeneData> geneDataList = gene.getDataMap().get(runId);
					if (geneDataList == null) // control genes and common genes do not have multiple run values
						continue;
					int i=0;
					for (GeneData geneData : geneDataList) {
						geneData.addPreviousValue(geneData.getCt(), CtHistory.Reason.RERUN.getReason());
						geneData.setCt(gene2.getDataMap().get(0).get(i++).getCt());
					}
					break;
				}
			}
		}
	}

	public void switchToLast() {
		cTabFolder.setSelection(getNumberOfPages()-1);
	}

	public Entry getEntry() {
		return entry;
	}

	public void setEntry(Entry entry) {
		this.entry = entry;
	}
	
	@Optional @Inject
	public void updatePreferences(@UIEventTopic(IGritsPreferenceStore.EVENT_TOPIC_PREF_VALUE_CHANGED)
	 					String preferenceName)
	{
	 	if(QrtPCRPreference.getPreferenceID().equals(preferenceName)) {
	 		PreferenceEntity preferenceEntity;
			try {
				preferenceEntity = gritsPreferenceStore.getPreferenceEntity(preferenceName);
			
				QrtPCRPreference updatePref = (QrtPCRPreference) TableViewerPreference.getTableViewerPreference(preferenceEntity, QrtPCRPreference.class);
				updateColumnPreferences(updatePref);
			} catch (UnsupportedVersionException e) {
				logger.error("Error updating column visibility", e);
			}
		} else if (QrtPCRMergeTablePreference.getPreferenceID().equals(preferenceName)) {
			PreferenceEntity preferenceEntity;
			try {
				preferenceEntity = gritsPreferenceStore.getPreferenceEntity(preferenceName);
			
				QrtPCRMergeTablePreference updatePref = (QrtPCRMergeTablePreference) TableViewerPreference.getTableViewerPreference(preferenceEntity, QrtPCRMergeTablePreference.class);
				updateColumnPreferences(updatePref);
			} catch (UnsupportedVersionException e) {
				logger.error("Error updating column visibility", e);
			}
		} else if (QrtPCRGeneListTablePreference.getPreferenceID().equals(preferenceName)) {
			PreferenceEntity preferenceEntity;
			try {
				preferenceEntity = gritsPreferenceStore.getPreferenceEntity(preferenceName);
			
				QrtPCRGeneListTablePreference updatePref = (QrtPCRGeneListTablePreference) TableViewerPreference.getTableViewerPreference(preferenceEntity, QrtPCRGeneListTablePreference.class);
				updateColumnPreferences(updatePref);
			} catch (UnsupportedVersionException e) {
				logger.error("Error updating column visibility after preference change", e);
			}
		} else if (QrtPCRMasterTablePreference.getPreferenceID().equals(preferenceName)) {
			PreferenceEntity preferenceEntity;
			try {
				preferenceEntity = gritsPreferenceStore.getPreferenceEntity(preferenceName);
			
				QrtPCRMasterTablePreference updatePref = (QrtPCRMasterTablePreference) TableViewerPreference.getTableViewerPreference(preferenceEntity, QrtPCRMasterTablePreference.class);
				updateColumnPreferences(updatePref);
			} catch (UnsupportedVersionException e) {
				logger.error("Error updating column visibility after preference change", e);
			}
		}
	}

	private void updateColumnPreferences(QrtPCRPreference updatePref) {
		IQrtPCRPart activePage = getcTabItemToPartTabMap().get(cTabFolder.getSelection());
		if (activePage instanceof PlateTablePage) {
			updateColumnPreferences(((PlateTablePage) activePage).getTable(), (QrtPCRPreference) ((PlateTablePage) activePage).getTable().getTablePreference(), updatePref);
		}
		else if (activePage instanceof MasterTablePage) {
			updateColumnPreferences(((MasterTablePage) activePage).getTable(), (QrtPCRPreference) ((MasterTablePage) activePage).getTable().getTablePreference(), updatePref);
		}
	}
	
	protected void updateColumnPreferences( QrtPCRNatTable table, QrtPCRPreference curPref, QrtPCRPreference updatePref ) {
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

	public void setcTabItemToPartTabMap(Map<CTabItem, IQrtPCRPart> cTabItemToPartTabMap) {
		this.cTabItemToPartTabMap = cTabItemToPartTabMap;
	}

	public boolean isDirty() {
		return dirtyable.isDirty();
	}	
}
