package org.grits.toolbox.entry.qrtpcr.editor;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.widgets.Section;
import org.grits.toolbox.entry.qrtpcr.Config;
import org.grits.toolbox.entry.qrtpcr.commands.EvaluateData;
import org.grits.toolbox.entry.qrtpcr.model.QrtPCRData;
import org.grits.toolbox.entry.qrtpcr.table.PlateTableBase;
import org.grits.toolbox.entry.qrtpcr.table.QrtPCRNatTable;

public class PlateTablePage implements IQrtPCRPart {
	protected static final int HORIZONTAL_BUFFER = 8;
	protected static final int VERTICAL_BUFFER = 12;
	
	QrtPCRData data;
	private boolean raw;
	private Integer runId;
	Double stDevCutOff;
	Double lowerThreshold;
	
	boolean expanded = true;
	
	private QrtPCRNatTable table;
	private CTabItem cTabItem;
	
	public QrtPCRNatTable getTable() {
		return table;
	}
	
	@Inject
	public PlateTablePage(QrtPCRData data, CTabItem cTabItem,
			@Named(QrtPCRTableEditor.LOWER_THRESHOLD_CONTEXT) Double l,
			@Named(QrtPCRTableEditor.STDEV_CONTEXT) Double stDev, 
			@Named(QrtPCRTableEditor.RAW_CONTEXT) Boolean raw,
			@Named(QrtPCRTableEditor.RUNID_CONTEXT) Integer runId)
	{
		this.data = data;
		this.cTabItem = cTabItem;
		this.lowerThreshold = l;
		this.stDevCutOff = stDev;
		this.raw = raw;
		this.runId = runId;
	}
	
	@PostConstruct
	protected void createFormContent(MPart part) {
		
		createTableContents(part.getContext().get(QrtPCRTableEditor.class), raw, data, runId);
	}
	
	public void createTableContents (final QrtPCRTableEditor editor, final boolean rawData, final QrtPCRData data, final Integer runId) {	
		ScrolledComposite scrolledComposite = new ScrolledComposite(cTabItem.getParent(), 
				SWT.H_SCROLL | SWT.V_SCROLL);
		GridData layoutData = new GridData();
		scrolledComposite.setLayoutData(layoutData);
		scrolledComposite.setLayout(new GridLayout());
		Composite parent = new Composite(scrolledComposite, SWT.FILL);
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
	    Action applyRulesAction = new Action("Apply Rules") {
        	@Override
            public void run() {
        		if (!rawData) {
        			Dialog stdevCutoffDialog = new Dialog(Display.getCurrent().getActiveShell()) {
    					private ControlDecoration dec2;
    					@Override
    					protected Control createDialogArea(Composite parent) {
    						Composite container = new Composite(parent, SWT.NONE);
    						GridLayout gridLayout = new GridLayout();
    						gridLayout.numColumns = 2;
    						gridLayout.verticalSpacing = 10;
    						container.setLayout(gridLayout);
    						Label stDevLabel = new Label(container, SWT.LEFT);
    						stDevLabel.setText(Config.STDEVCUTOFF);
    						stDevLabel.setToolTipText("Standard deviation cut-off value. Entries having standard deviations more than this cutoff will be highlighted");
    						Text stDevCutoffText = new Text(container, SWT.BORDER);
    						stDevCutoffText.setText(stDevCutOff +"");
    						
    						// Create a control decoration for the control.
    						dec2 = new ControlDecoration(stDevCutoffText, SWT.TOP | SWT.LEFT);
    						// Specify the decoration image and description
    						Image image = JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_ERROR);
    						dec2.setImage(image);
    						dec2.setDescriptionText("Should enter a floating point number");
    						dec2.hide();
    						
    						stDevCutoffText.addModifyListener(new ModifyListener() {

    							@Override
    							public void modifyText(ModifyEvent e) {
    								Text newText = (Text) e.widget;
    								String newValue = newText.getText();
    								if (newValue != null)
    									newValue = newValue.trim();
    								try {
    									stDevCutOff = Double.parseDouble(newValue);
    									dec2.hide();
    								} catch (NumberFormatException ex) {
    									dec2.show();
    								}
    								
    							}
    						});
    						
    						return container;
    					}
    				};
    				
    				if (stdevCutoffDialog.open() == Window.OK) {
						if (EvaluateData.applyRules(data, 0, lowerThreshold, stDevCutOff)) {   // use current values
							table.refresh();
							editor.markDirty();
						}
						editor.setOriginalForRun (runId);
    				}
				}
            }
        	
        	@Override
        	public String getToolTipText() {
        		return "Apply rules to determine possible reruns";
        	}
	    };
	    
	    Action collapseExpandAction = new Action("Collapse/Expand") {
	    	@Override
            public void run() {
	    		if (table != null) {
	    			table.toggleExpandCollapse(expanded);
	    			expanded = expanded ? false : true;
	    		}
	    	}
	    };
            
	    if (rawData) applyRulesAction.setEnabled(false);
        toolBarManager.add(applyRulesAction);
        toolBarManager.add(collapseExpandAction);
        ToolBar toolbar = toolBarManager.createControl(section);
        section.setTextClient(toolbar);
        
        if (!rawData) createLegend(client);
        
        PlateTableBase myTable = new PlateTableBase(editor);
        myTable.setData(data);
        myTable.setLowerThreshold(lowerThreshold);
        myTable.setRawData(rawData);
        myTable.setStDevCutOff(stDevCutOff);
        myTable.setRunId(runId);
        
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
	    FontData[] fD = Display.getDefault().getSystemFont().getFontData();
		fD[0].setStyle(SWT.BOLD);
		Font boldFont = new Font(Display.getDefault(), fD);
		
		Group legendGroup = new Group (client, SWT.NULL);
		legendGroup.setText("Legend");
		legendGroup.setLayout(new GridLayout (17, false));
		
		Label control = new Label(legendGroup, SWT.BOLD | SWT.BORDER);
		control.setText("Bold");
		control.setFont(boldFont);
		Label controllabel = new Label(legendGroup, SWT.BORDER);
		controllabel.setText("Control Gene");
		
		Label verticalSeparator = new Label (legendGroup, SWT.SEPARATOR | SWT.VERTICAL);
		GridData layoutData = new GridData();
		layoutData.heightHint = control.computeSize(SWT.DEFAULT, SWT.DEFAULT).y + 5;
		verticalSeparator.setLayoutData(layoutData);
		
		StyledText text = new StyledText(legendGroup, SWT.BORDER);
	    text.setText("xx.xx");
	    text.setEnabled(false);
		StyleRange style2 = new StyleRange();
	    style2.start = 0;
	    style2.length = 5;
	    style2.strikeout = true;
	    text.setStyleRange(style2);
	    text.setEditable(false);
	    Label eliminate = new Label(legendGroup, SWT.BORDER);
	    eliminate.setText("Eliminated");
	    
	    verticalSeparator = new Label (legendGroup, SWT.SEPARATOR | SWT.VERTICAL);
		verticalSeparator.setLayoutData(layoutData);
	    
	    Label rerun = new Label(legendGroup, SWT.BORDER);
	    rerun.setText("Gene");
	    rerun.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
	    Label rerunLabel = new Label (legendGroup, SWT.BORDER);
	    rerunLabel.setText("To be Rerun");
	    
	    verticalSeparator = new Label (legendGroup, SWT.SEPARATOR | SWT.VERTICAL);
		verticalSeparator.setLayoutData(layoutData);
	    
	    Label redForeground = new Label (legendGroup, SWT.BORDER);
	    redForeground.setText ("xx.xx");
	    redForeground.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
	    Label redLabel = new Label (legendGroup, SWT.BORDER);
	    redLabel.setText("StDev >= cutOff or user modified value");
	    
	    verticalSeparator = new Label (legendGroup, SWT.SEPARATOR | SWT.VERTICAL);
		verticalSeparator.setLayoutData(layoutData);
	    
	    Label darkRedForeground = new Label (legendGroup, SWT.BORDER);
	    darkRedForeground.setText ("xx.xx");
	    darkRedForeground.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED));
	    Label darkRedLabel = new Label (legendGroup, SWT.BORDER);
	    darkRedLabel.setText("ct0 >= lowerThreshold or absent");
	    
	    verticalSeparator = new Label (legendGroup, SWT.SEPARATOR | SWT.VERTICAL);
		verticalSeparator.setLayoutData(layoutData);
	    
	    Label magentaBackground = new Label (legendGroup, SWT.BORDER);
	    magentaBackground.setText ("xx.xx");
	    magentaBackground.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_MAGENTA));
	    Label magentaLabel = new Label (legendGroup, SWT.BORDER);
	    magentaLabel.setText("Value averaged by calculations");
	    
	}
	
	/** call this method if preferences change and the table needs to be redrawn 
	 * 
	 */
	public void refresh() {
		if (table != null) {
			table.refresh();
		}
	}
}
