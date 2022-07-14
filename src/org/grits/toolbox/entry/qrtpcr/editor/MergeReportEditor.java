package org.grits.toolbox.entry.qrtpcr.editor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
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
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.forms.widgets.Section;
import org.grits.toolbox.core.dataShare.IGritsConstants;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.datamodel.property.ReportsProperty;
import org.grits.toolbox.core.datamodel.util.DataModelSearch;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.grits.toolbox.entry.qrtpcr.Activator;
import org.grits.toolbox.entry.qrtpcr.Config;
import org.grits.toolbox.entry.qrtpcr.ImageRegistry;
import org.grits.toolbox.entry.qrtpcr.ImageRegistry.QrtPCRImage;
import org.grits.toolbox.entry.qrtpcr.editor.dialogs.GeneSelectionDialog;
import org.grits.toolbox.entry.qrtpcr.model.ChartData;
import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.GeneList;
import org.grits.toolbox.entry.qrtpcr.model.merge.MergeData;
import org.grits.toolbox.entry.qrtpcr.model.merge.MergedQrtPCR;
import org.grits.toolbox.entry.qrtpcr.preference.QrtPCRPreference;
import org.grits.toolbox.entry.qrtpcr.property.QrtPCRMergeProperty;
import org.grits.toolbox.entry.qrtpcr.table.QrtPCRNatTable;
import org.grits.toolbox.entry.qrtpcr.table.merge.MergeNatTable;
import org.grits.toolbox.entry.qrtpcr.table.merge.MergeReportTableBase;
import org.grits.toolbox.entry.qrtpcr.view.HistogramView;

public class MergeReportEditor implements IQrtPCRPart {
	
	private static final Logger logger = Logger.getLogger(MergeReportEditor.class);

	@Inject private EPartService partService;
	@Inject EModelService modelService;
	@Inject MApplication application;
	
	private String fileLocation;
	
	MergedQrtPCR mergeData;
	private GeneList pathway;
	
	MergeNatTable table;
	String partName;
	
	public String getPartName() {
		return partName;
	}

	@Inject
	public MergeReportEditor(@Named(IServiceConstants.ACTIVE_SELECTION) Entry entry,
			@Named(IServiceConstants.ACTIVE_PART) MPart part, 
			@Named(IGritsConstants.WORKSPACE_LOCATION) String workspaceLocation) {
		
		try {
			//get the project name//at least one entry has to be there
			Entry projectEntry = DataModelSearch.findParentByType(entry, ProjectProperty.TYPE);
            String qrtPCRFolderLocation = workspaceLocation+projectEntry.getDisplayName()+File.separator +ReportsProperty.getFolder()
                    + File.separator
                    + Config.folderName;
            QrtPCRMergeProperty qPCRProperty = ((QrtPCRMergeProperty) entry.getProperty());
            // name of the qrtPCR data xml file 
            String fileName = qPCRProperty.getFilename();
      
            //file with absolute path
            fileLocation= qrtPCRFolderLocation 
                    + File.separator 
                    + fileName;
            File mergeFile = new File(fileLocation);
            FileInputStream inputStream = new FileInputStream(mergeFile.getAbsolutePath());
            InputStreamReader reader = new InputStreamReader(inputStream, PropertyHandler.GRITS_CHARACTER_ENCODING);
            JAXBContext context = JAXBContext.newInstance(MergedQrtPCR.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            mergeData = (MergedQrtPCR) unmarshaller.unmarshal(reader);
            reader.close();
            inputStream.close();
			part.setLabel(entry.getDisplayName());
			this.partName = part.getLabel();
		} catch (IOException e) {
			ErrorUtils.createErrorMessageBox(Display.getCurrent().getActiveShell(), "Cannot load the merge QrtPCR Table", e);
			logger.error(Activator.PLUGIN_ID + " Cannot create the merge QrtPCR Table", e);
		} catch (JAXBException e) {
			ErrorUtils.createErrorMessageBox(Display.getCurrent().getActiveShell(), "Cannot load the merge QrtPCR Table", e);
			logger.warn(Activator.PLUGIN_ID + " Cannot load the merge QrtPCR Table", e);
		}
	}
	
	@PostConstruct
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout()); 
		Composite composite = new Composite(parent, SWT.NONE); 

        initializeComponents(composite);
	}

	@Persist
	protected void updateProjectProperty() {   
        try {
        	// save the merge report
	        ByteArrayOutputStream os = new ByteArrayOutputStream();
            JAXBContext context = JAXBContext.newInstance(MergedQrtPCR.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, PropertyHandler.GRITS_CHARACTER_ENCODING);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(mergeData, os);

            //write the serialized data to the folder
            FileWriter fileWriter = new FileWriter(fileLocation);
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

	protected void initializeComponents(Composite parent) {
		parent.setLayout(new GridLayout(1, true));
		
		Composite sectionParentComposite = new Composite(parent, SWT.FILL);
		sectionParentComposite.setLayout(new FillLayout());
		
		Section section = new Section(sectionParentComposite, Section.TITLE_BAR | Section.EXPANDED);
		section.setText("Data");
	    
		// Composite for storing the data
	    Composite client = new Composite(section, SWT.WRAP);
	    GridLayout layout = new GridLayout();
	    layout.numColumns = 1;
	    layout.marginWidth = 2;
	    layout.marginHeight = 2;
	    client.setLayout(layout);
	    
	    ToolBarManager toolBarManager = new ToolBarManager();
	    Action chartAction = new Action("Histogram") {
        	@Override
            public void run() {
        		//Show a view
    			ChartData[][] dataList = new ChartData[mergeData.getQrtPCRGeneListMap().size()][mergeData.getAliasList().size()];
    			String secondaryId = getPartName();
    			MPart part = partService.createPart("qrtPCR-histogram");
    			part.setLabel(secondaryId);
    			List<MPartStack> stacks = modelService.findElements(application, "org.grits.toolbox.core.partstack.histogramview",
    					MPartStack.class, null);
    			if (stacks.size() < 1) {
    				MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error ", 
    						"Part stack not found. Is the following ID correct?" + "org.grits.toolbox.core.partstack.histogramview");
    				return;
    			} 
    			stacks.get(0).getChildren().add(part);
    			stacks.get(0).setVisible(true);
    			// activates the part
    			partService.showPart(part, PartState.ACTIVATE);
    			
    			HistogramView view = (HistogramView) part.getObject();
    			int i=0;
    			if (pathway != null) { // we need to follow the order given in the pathway
    				i = getGenesInPathwayOrder (dataList, null);
    			}
    			else {
        			for (Iterator<Gene> iterator = mergeData.getQrtPCRGeneListMap().keySet().iterator(); iterator
							.hasNext();) {
						Gene gene = (Gene) iterator.next();
						if (!isSelected(gene))
							continue;
						List<MergeData> mergeDataList = mergeData.getQrtPCRGeneListMap().get(gene);
						int j=0;
						if (mergeDataList.size() == mergeData.getAliasList().size()) {
							for (MergeData mergeData2 : mergeDataList) {
								ChartData data = new ChartData();
								data.setGeneIdentifier(gene.getGeneIdentifier());
								data.setGeneSymbol(gene.getGeneSymbol());
								data.setValue(mergeData2.getAverage());
								data.setError(mergeData2.getStDev());
								dataList[i][j] = data;
								j++;
							}
						}
						else
							continue;
						
						i++;
						
					} 
    			}
				view.setPartName(getPartName());
				
				if (i == 0) {
					// nothing is selected
					ErrorUtils.createWarningMessageBox(Display.getCurrent().getActiveShell(), "No Data", "You haven't selected any data for the histogram!");
				} else {
					// need to truncate the null entries
					ChartData[][] newDataList = Arrays.copyOf(dataList, i);
        			view.initializeChart((String[]) mergeData.getAliasList().values().toArray(new String[mergeData.getAliasList().size()]), newDataList, "Average", true);
				}
					
            }
        	
        	@Override
        	public String getToolTipText() {
        		return "Show average values in a histogram";
        	}
        	
        	@Override
        	public ImageDescriptor getImageDescriptor() {
        		return ImageRegistry.getImageDescriptor(QrtPCRImage.HISTOGRAM_ICON);
        	}
	    };
	    
	    Action foldChangeChartAction = new Action("Fold Change") {
        	@Override
            public void run() {
        		
        		ListDialog dialog = new ListDialog(Display.getCurrent().getActiveShell());
        		
        		dialog.setAddCancelButton(true);
        		dialog.setContentProvider(new ArrayContentProvider());
        	    dialog.setLabelProvider(new LabelProvider());
        	    dialog.setInput(mergeData.getAliasList().values());
        	    dialog.setTitle("Select the control experiment for Fold Change chart");
        	    if (dialog.open() == Window.OK) {
        	    	Object[] result = dialog.getResult();
        	    	if (result == null || result.length == 0)
        	    		return;
        	    	String controlAlias = (String)result[0];
        	    	mergeData.setControlAlias(controlAlias);
        	    } else {
        	    	return;
        	    }
        		//Show a view
        		
    			int controlIndex=0;
    			// find control
    			String controlAlias = mergeData.getControlAlias();
    			if (controlAlias != null) {
    				int k=0;
    				for (String alias : mergeData.getAliasList().values()) {
						if (alias.equals(controlAlias)) {
							controlIndex = k;
							break;
						}
						k++;
					}
    			} else {
    				controlAlias = mergeData.getAliasList().values().iterator().next();
    			}
    			
    			ChartData[][] dataList = new ChartData[mergeData.getQrtPCRGeneListMap().size()][mergeData.getAliasList().size()-1];
    			String secondaryId = getPartName();
    			MPart part = partService.createPart("qrtPCR-histogram");
    			part.setLabel(secondaryId+"Fold Change");
    			List<MPartStack> stacks = modelService.findElements(application, "org.grits.toolbox.core.partstack.histogramview",
    					MPartStack.class, null);
    			if (stacks.size() < 1) {
    				MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error ", 
    						"Part stack not found. Is the following ID correct?" + "org.grits.toolbox.core.partstack.histogramview");
    				return;
    			} 
    			stacks.get(0).getChildren().add(part);
    			stacks.get(0).setVisible(true);
    			// activates the part
    			partService.showPart(part, PartState.ACTIVATE);
    			
    			HistogramView view = (HistogramView) part.getObject();
    			int i=0;
    			if (pathway != null) {   // we need to follow the order given in the pathway
    				i = getGenesInPathwayOrder (dataList, controlIndex);
    			}
    			else {  // follows the selection order 
        			for (Iterator<Gene> iterator = mergeData.getQrtPCRGeneListMap().keySet().iterator(); iterator
							.hasNext();) {
						Gene gene = (Gene) iterator.next();
						if (!isSelected(gene))
							continue;
						List<MergeData> mergeDataList = mergeData.getQrtPCRGeneListMap().get(gene);
						int j=0;
						int dataIndex = 0;
						if (mergeDataList.size() == mergeData.getAliasList().size()) {
							for (MergeData mergeData : mergeDataList) {
								if (j != controlIndex) {	
									ChartData data = new ChartData();
									data.setGeneIdentifier(gene.getGeneIdentifier());
									data.setGeneSymbol(gene.getGeneSymbol());
									data.setValue(calculateFoldChange (mergeData, mergeDataList.get(controlIndex)));
									data.setError(calculateFoldChangeError (mergeData, mergeDataList.get(controlIndex)));
									dataList[i][dataIndex] = data;
									dataIndex++;
								}
								j++;
							}
						} else 
							continue;
						i++;
					} 
    			}
				view.setPartName(getPartName()+"-Fold Change");
				if (i == 0) {
					// nothing is selected
					ErrorUtils.createWarningMessageBox(Display.getCurrent().getActiveShell(), "No Data", "You haven't selected any data for the histogram!");
				} else {
					// need to truncate the null entries
					ChartData[][] newDataList = Arrays.copyOf(dataList, i);
					// remove the control alias from the list
					Map<String, String> newAliasList = new HashMap<String, String>();
					for (Map.Entry<String, String> aliasEntry : mergeData.getAliasList().entrySet()) {
						if (!aliasEntry.getValue().equals(controlAlias))
							newAliasList.put(aliasEntry.getKey(), aliasEntry.getValue());
					}
					view.initializeChart((String[]) newAliasList.values().toArray(new String[newAliasList.size()]), newDataList, "Fold Change", false);
				}
				
            }

			@Override
        	public String getToolTipText() {
        		return "Show average values as a fold change graph";
        	}
        	
        	@Override
        	public ImageDescriptor getImageDescriptor() {
        		return ImageRegistry.getImageDescriptor(QrtPCRImage.FOLDCHANGE_ICON);
        	}
	    };
	    
	    Action selectAllAction = new Action("Select All") {
        	@Override
            public void run() {
        		if (table != null) {
        			GeneSelectionDialog dialog = new GeneSelectionDialog(Display.getCurrent().getActiveShell(), MergeReportEditor.this);
        			dialog.open();
        		}
        	}
        	
        	@Override
        	public ImageDescriptor getImageDescriptor() {
        		return ImageRegistry.getImageDescriptor(QrtPCRImage.SELECT_ICON);
        	}
        	
        	@Override
        	public String getToolTipText() {
        		return "Select genes for the graph";
        	}
	    };
	  
	    toolBarManager.add(selectAllAction);
        toolBarManager.add(chartAction);
        toolBarManager.add(foldChangeChartAction);
        ToolBar toolbar = toolBarManager.createControl(section);
        section.setTextClient(toolbar);
		
        MergeReportTableBase myTable = new MergeReportTableBase(this);
        myTable.setMergeData(mergeData);
        
        table = (MergeNatTable) myTable.createControl(client);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,20));
        
		section.setClient(client);
		
		GridData compositeLayoutData = new GridData(GridData.FILL_BOTH);
		compositeLayoutData.verticalSpan = 1;
		compositeLayoutData.horizontalSpan = 1;
		sectionParentComposite.setLayoutData(compositeLayoutData);

	}
	
	/**
	 * extract the data from MergeData into the ChartData matrix for each gene in the given pathway
	 * this ensures the correct order given in the pathway is followed when generating the histogram or fold-change chart
	 * 
	 * @param dataList the matrix to be filled
	 * @param controlIndex null if working on histogram, or the index of the control experiment if working on fold-change chart
	 * 
	 * @return the actual number of genes placed in the dataList
	 */
	private int getGenesInPathwayOrder(ChartData[][] dataList, Integer controlIndex) {
		int i=0;
		
		for (Gene gene : this.pathway.getGenes()) {
			boolean found = false;
			// find the gene from MergeData and extract its information into chartData
			for (Iterator<Gene> iterator = mergeData.getQrtPCRGeneListMap().keySet().iterator(); iterator
					.hasNext();) {
				Gene gene2 = (Gene) iterator.next();
				if (!gene.getGeneIdentifier().equals(gene2.getGeneIdentifier())) 
					continue;
				List<MergeData> mergeDataList = mergeData.getQrtPCRGeneListMap().get(gene2);
				int j=0;
				int dataIndex = 0;
				if (mergeDataList.size() == mergeData.getAliasList().size()) {
					for (MergeData mergeData2 : mergeDataList) {
						if (controlIndex == null) {
							ChartData data = new ChartData();
							data.setGeneIdentifier(gene.getGeneIdentifier());
							data.setGeneSymbol(gene.getGeneSymbol());
							data.setValue(mergeData2.getAverage());
							data.setError(mergeData2.getStDev());
							dataList[i][j] = data;
							j++;
						} else {
							if (j != controlIndex) {
								ChartData data = new ChartData();
								data.setGeneIdentifier(gene.getGeneIdentifier());
								data.setGeneSymbol(gene.getGeneSymbol());
								data.setValue(mergeData2.getAverage());
								data.setError(mergeData2.getStDev());
								dataList[i][dataIndex] = data;
								dataIndex++;
							}
							j++;
						}
					}
				}
				else
					continue;
				found = true;
				break;
			}
			if (found)  // if the gene from the pathway does not exist in all the experiments, prevent addition of null data
				i++;
			
		}
		
		
		return i;
	}
	
	protected Double calculateFoldChangeError(MergeData mergeData,
			MergeData mergeDataControl) {
		Double foldChange = mergeData.getStDev() / mergeDataControl.getStDev();
		if (foldChange < 1) {
			foldChange = -1 * (mergeDataControl.getStDev() / mergeData.getStDev());
		}
		return foldChange;
	}

	protected Double calculateFoldChange(MergeData mergeData,
			MergeData mergeDataControl) {
		Double foldChange = mergeData.getAverage() / mergeDataControl.getAverage();
		if (foldChange < 1) {
			foldChange = -1 * (mergeDataControl.getAverage() / mergeData.getAverage());
		}
		return foldChange;
	}
	
	public void selectAll() {
		if (table != null) {
			table.selectAll();
		}
	}
	
	boolean isSelected (Gene gene) {
		boolean selected = false;
		if (table != null) {
			selected =  table.isSelected(gene);
		}
		return selected;
	}
	

	@Focus
	public void setFocus() {
		if (table != null)
			table.setFocus();
	}
	

	public void clearSelection() {
		if (table != null) {
			table.deSelectAll();
		}
	}

	public void setGeneList(GeneList inputList) {
		this.pathway = inputList;
	}

	public void selectGene(Gene gene) {
		if (table != null)
			table.selectGene(gene);
		
	}

	public void updateColumnPreferences(QrtPCRPreference updatePref) {
		
		updateColumnPreferences(table, (QrtPCRPreference)table.getTablePreference(), updatePref);
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
}
