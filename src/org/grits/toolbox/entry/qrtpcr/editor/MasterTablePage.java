package org.grits.toolbox.entry.qrtpcr.editor;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.widgets.Section;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.grits.toolbox.entry.qrtpcr.ImageRegistry;
import org.grits.toolbox.entry.qrtpcr.ImageRegistry.QrtPCRImage;
import org.grits.toolbox.entry.qrtpcr.model.ChartData;
import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.QrtPCRRun;
import org.grits.toolbox.entry.qrtpcr.model.QrtPCRTable;
import org.grits.toolbox.entry.qrtpcr.model.TableData;
import org.grits.toolbox.entry.qrtpcr.table.MasterTableBase;
import org.grits.toolbox.entry.qrtpcr.table.QrtPCRNatTable;
import org.grits.toolbox.entry.qrtpcr.view.HistogramView;

public class MasterTablePage implements IQrtPCRPart {

	private static final Logger logger = Logger.getLogger(MasterTablePage.class);
	
	QrtPCRTable qrtPCRTable;
	private List<Gene> geneList;
	
	Double lowerThreshold;
	Double stdevCutOff;
	QrtPCRTableEditor editor;

	private QrtPCRNatTable table;

	private CTabItem cTabItem;
	
	@Inject EPartService partService;
	@Inject private EModelService modelService;
	@Inject MApplication application;
	
	@Inject
	public MasterTablePage(QrtPCRTable table, CTabItem cTabItem, 
			@Named(QrtPCRTableEditor.LOWER_THRESHOLD_CONTEXT) Double l, 
			@Named(QrtPCRTableEditor.STDEV_CONTEXT) Double s)
	{
		this.qrtPCRTable = table;
		this.cTabItem = cTabItem;
		this.lowerThreshold = l;
		this.stdevCutOff = s;
	}
	
	@PostConstruct
	protected void createFormContent(MPart part) {
		ScrolledComposite scrolledComposite = new ScrolledComposite(cTabItem.getParent(), 
				SWT.H_SCROLL | SWT.V_SCROLL);
		GridData layoutData = new GridData();
		scrolledComposite.setLayoutData(layoutData);
		scrolledComposite.setLayout(new GridLayout());
		Composite parent = new Composite(scrolledComposite, SWT.FILL);
		parent.setLayout(new GridLayout(1, false));
		
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
	    
	    createLegend (client);
	    
	    ToolBarManager toolBarManager = new ToolBarManager();
	    Action chartAction = new Action("Histogram") {
	    	
	    	@Override
	    	public ImageDescriptor getImageDescriptor() {
	    		return ImageRegistry.getImageDescriptor(
	    				QrtPCRImage.HISTOGRAM_ICON);
	    	}
	    	
        	@Override
            public void run() {
        		
        		//Show a view
    			List<ChartData> dataList = new ArrayList<>();
    			String secondaryId = qrtPCRTable.getPlateDataMap().values().iterator().next().getName();
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
				if (geneList != null) {
					for (Gene gene : geneList) {
						if (isSelected(gene)) {
							ChartData data = new ChartData();
							data.setGeneIdentifier(gene.getGeneIdentifier());
							data.setGeneSymbol(gene.getGeneSymbol());
							data.setValue(gene.getAdjustedAverage(gene.getRunId(), lowerThreshold, gene.getNormValue(gene.getRunId())));
							data.setError(gene.getStDevForAdjusted(gene.getRunId(), lowerThreshold,gene.getNormValue(gene.getRunId())));
							dataList.add(data);
						}
					}
				}
				view.setPartName(part.getLabel());
				if (!dataList.isEmpty())
					view.initializeChart(dataList);
				else 
					ErrorUtils.createWarningMessageBox(Display.getCurrent().getActiveShell(), "No Selection", "Please select rows before displaying the histogram");
				
				
            }
        	
        	@Override
        	public String getToolTipText() {
        		return "Show average values in a histogram";
        	}
	    };
	    
	    Action selectAllAction = new Action("Select All") {
	    	
	    	@Override
	    	public ImageDescriptor getImageDescriptor() {
	    		return ImageRegistry.getImageDescriptor(QrtPCRImage.SELECT_ICON);
	    	}
	    	
	    	@Override
	    	public String getToolTipText() {
	    		return this.getText();
	    	}
	    	
        	@Override
            public void run() {
        		if (table != null) {
        			table.selectAll();
        		}
        	}
	    };
	  
	    toolBarManager.add(selectAllAction);
	  
        toolBarManager.add(chartAction);
        ToolBar toolbar = toolBarManager.createControl(section);
        section.setTextClient(toolbar);
        
        MasterTableBase myTable = new MasterTableBase(editor);
        myTable.setLowerThreshold(lowerThreshold);
        myTable.setStDevCutOff(stdevCutOff);
        
        geneList = QrtPCRRun.generateMasterTable(qrtPCRTable);
        
        myTable.setGeneList(geneList);
        
        table = (QrtPCRNatTable) myTable.createControl(client);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,20));
        
		section.setClient(client);
		
		GridData compositeLayoutData = new GridData(GridData.FILL_BOTH);
		compositeLayoutData.verticalSpan = 1;
		compositeLayoutData.horizontalSpan = 1;
		sectionParentComposite.setLayoutData(compositeLayoutData);
		
		scrolledComposite.setContent(parent);
		scrolledComposite.setMinSize(parent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setExpandHorizontal(true);
		cTabItem.setControl(scrolledComposite);
	}
	
	private void createLegend(Composite client) {
		Group legendGroup = new Group (client, SWT.NULL);
		legendGroup.setText("Legend");
		legendGroup.setLayout(new GridLayout (2, true));
		Text highlightText = new Text(legendGroup, SWT.BORDER);
		highlightText.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
		highlightText.setEnabled(false);
		highlightText.setEditable(false);
		Label label = new Label(legendGroup, SWT.NONE);
		label.setText("Rerun Genes");
	}

	boolean isSelected (Gene gene) {
		boolean selected = false;
		if (table != null) {
			return table.isSelected(gene);
		}
		return selected;
	}
	
	public void refreshInput () {
		if (table != null) table.refresh();
	}
	
	public void refreshInput (QrtPCRTable newTable) {
		this.qrtPCRTable = newTable;
		if (table != null) {
			List<Gene> geneList = QrtPCRRun.generateMasterTable(qrtPCRTable);
			List<TableData> tableData = new ArrayList<>();
			tableData.addAll(geneList);
			table.setTableData(tableData);
			table.updateTable();
			//tableViewer.setInput(geneList);
		//	tableViewer.refresh();
		}
	}

	public QrtPCRNatTable getTable() {
		return table;
	}

}
