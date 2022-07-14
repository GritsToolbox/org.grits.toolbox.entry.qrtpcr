package org.grits.toolbox.entry.qrtpcr.editor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.img.ImageShare;
import org.grits.toolbox.entry.qrtpcr.Config;
import org.grits.toolbox.entry.qrtpcr.ImageRegistry;
import org.grits.toolbox.entry.qrtpcr.ImageRegistry.QrtPCRImage;
import org.grits.toolbox.entry.qrtpcr.commands.EvaluateData;
import org.grits.toolbox.entry.qrtpcr.model.QrtPCRData;
import org.grits.toolbox.entry.qrtpcr.model.QrtPCRRun;
import org.grits.toolbox.entry.qrtpcr.model.QrtPCRTable;

@SuppressWarnings("restriction")
public class OverviewPage implements IQrtPCRPart {
	
	private static final Logger logger = Logger.getLogger(OverviewPage.class);
	private static final Image CHECKED = ImageShare.CHECKBOX_ICON_YES.createImage();
	private static final Image UNCHECKED = ImageShare.CHECKBOX_ICON_NO.createImage();
	
	private Text text;
	private Table runTable;
	private Tree plateTree;
	private Button btnEvaluateData;
	private TableColumn tblclmnRunid;
	private TableViewerColumn tableViewerColumn_2;
	private TableColumn tblclmnPlatelayoutfile;
	private TableViewerColumn tableViewerColumn_3;
	private TableColumn tblclmnNumberOfReplicates;
	private TableViewerColumn tableViewerColumn_4;
	private TreeViewerColumn treeViewerColumn2;
	private TreeViewerColumn treeViewerColumn3;
	
	private List<TreeItem> itemsToShow = new ArrayList<>();
	private QrtPCRRun pcrRun;
	private Button currentDataOption;
	private Button rawDataOption;
	private Button revertDataOption;
	
	private double stDevCutOff;
	private Text stdevCuttOffText;
	
	private CTabItem cTabItem;
	
	
	@Inject private ECommandService commandService = null;
	@Inject private EHandlerService handlerService = null;
	
	@Inject
	public OverviewPage(QrtPCRRun pcrRun, CTabItem cTabItem)
	{
		this.pcrRun = pcrRun;
		this.cTabItem = cTabItem;
	}
	
	
	public void refreshInput (QrtPCRRun newRun) {
		this.pcrRun = newRun;
		itemsToShow = new ArrayList<>();
		setTables();
	}

	@PostConstruct
	protected void createFormContent(MPart part) {
		
		ScrolledComposite scrolledComposite = new ScrolledComposite(cTabItem.getParent(), 
				SWT.H_SCROLL | SWT.V_SCROLL);
		GridData layoutData = new GridData();
		scrolledComposite.setLayoutData(layoutData);
		scrolledComposite.setLayout(new GridLayout());
		Composite parent = new Composite(scrolledComposite, SWT.FILL);
		parent.setLayout(new GridLayout(1, true));
		
		createMetadataSection(parent);
		
		Composite sectionParentComposite = new Composite(parent, SWT.FILL);
		sectionParentComposite.setLayout(new TableWrapLayout());
		
		Section section0 = new Section(sectionParentComposite, Section.TITLE_BAR | Section.EXPANDED);
	    section0.setText("Input Gene List"); 
	    section0.setLayout(new TableWrapLayout());
		Composite topGroup = new Composite(section0, SWT.NONE);
		topGroup.setLayout(new GridLayout(1,true));
		
		ToolBarManager toolBarManager0 = new ToolBarManager();
        toolBarManager0.add(new Action("Download file") {
        	@Override
        	public ImageDescriptor getImageDescriptor() {
        		return ImageRegistry.getImageDescriptor(QrtPCRImage.DOWNLOAD_ICON);
        	}
        	
        	@Override
        	public String getToolTipText() {
        		return "Download input gene list file";
        	}
        	
        	@Override
            public void run() {
    			String filename = text.getText();
    			downloadAction(Display.getCurrent().getActiveShell(), filename, part);
            }
		});
        ToolBar toolbar0 = toolBarManager0.createControl(section0);
        section0.setTextClient(toolbar0);
				
		text = new Text(topGroup, SWT.BORDER);
		text.setEditable(false);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		TableWrapData sectionLayoutData = new TableWrapData(TableWrapData.FILL, TableWrapData.BOTTOM);
		sectionLayoutData.grabHorizontal = true;
		sectionLayoutData.grabVertical = true;
		section0.setLayoutData(sectionLayoutData);
		section0.setClient(topGroup);
		
		GridData compositeLayoutData = new GridData(GridData.FILL_BOTH);
		compositeLayoutData.verticalSpan = 1;
		compositeLayoutData.horizontalSpan = 2;
		sectionParentComposite.setLayoutData(compositeLayoutData);
		
		sectionParentComposite = new Composite(parent, SWT.FILL);
		sectionParentComposite.setLayout(new TableWrapLayout());
		
	    Section section = new Section(sectionParentComposite, Section.TITLE_BAR | Section.EXPANDED);
	    section.setText("Runs"); 
	    // Composite for storing the data
	    Composite client = new Composite(section, SWT.WRAP);
	    GridLayout layout = new GridLayout(1, true);
	    layout.marginWidth = 2;
	    layout.marginHeight = 2;
	    client.setLayout(layout);
	    
	    ToolBarManager toolBarManager = new ToolBarManager();
        toolBarManager.add(new Action("Download file") {
        	@Override
        	public ImageDescriptor getImageDescriptor() {
        		return ImageRegistry.getImageDescriptor(QrtPCRImage.DOWNLOAD_ICON);
        	}
        	
        	@Override
        	public String getToolTipText() {
        		return "Download selected plate layout file";
        	}
        	
        	@Override
            public void run() {
        		// find selected file
        		TableItem[] items = runTable.getSelection();
        		// since we allow single selection only, get the first one
        		if (items.length > 0) {
        			TableItem selected = items[0];
        			String filename = selected.getText(2);
        			downloadAction(Display.getCurrent().getActiveShell(), filename, part);
        		} else {
        			MessageDialog.openWarning(Display.getCurrent().getActiveShell(), "No selection", "Please select a run to download its layout file");
        		}
            }
		});
        ToolBar toolbar = toolBarManager.createControl(section);
        
        sectionLayoutData = new TableWrapData(TableWrapData.FILL, TableWrapData.BOTTOM);
		sectionLayoutData.grabHorizontal = true;
		sectionLayoutData.grabVertical = true;
		section.setLayoutData(sectionLayoutData);
        section.setTextClient(toolbar);
		
		TableViewer tableViewer1 = new TableViewer(client, SWT.BORDER |  SWT.FULL_SELECTION);
		runTable = tableViewer1.getTable();
		GridData gd_table_1 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_table_1.heightHint = 100;
		runTable.setLayoutData(gd_table_1);
		runTable.setHeaderVisible(true);
		runTable.setLinesVisible(true);
		section.setClient(client);
		
		compositeLayoutData = new GridData(GridData.FILL_BOTH);
		compositeLayoutData.verticalSpan = 1;
		compositeLayoutData.horizontalSpan = 2;
		sectionParentComposite.setLayoutData(compositeLayoutData);
		
		tableViewerColumn_2 = new TableViewerColumn(tableViewer1, SWT.NONE);
		tblclmnRunid = tableViewerColumn_2.getColumn();
		tblclmnRunid.setWidth(100);
		tblclmnRunid.setText("Run Id");
		
		tableViewerColumn_4 = new TableViewerColumn(tableViewer1, SWT.NONE);
		tblclmnNumberOfReplicates = tableViewerColumn_4.getColumn();
		tblclmnNumberOfReplicates.setWidth(100);
		tblclmnNumberOfReplicates.setText("Number of Replicates");
		
		tableViewerColumn_3 = new TableViewerColumn(tableViewer1, SWT.NONE);
		tblclmnPlatelayoutfile = tableViewerColumn_3.getColumn();
		tblclmnPlatelayoutfile.setWidth(700);
		tblclmnPlatelayoutfile.setText("Plate Layout File");
		
		sectionParentComposite = new Composite(parent, SWT.FILL);
		sectionParentComposite.setLayout(new TableWrapLayout());
		
		Section section2 = new Section(sectionParentComposite, Section.TITLE_BAR | Section.EXPANDED);
	    section2.setText("Plates"); 
	    // Composite for storing the data
	    Composite client2 = new Composite(section2, SWT.WRAP);
	    GridLayout layout2 = new GridLayout(1, true);
	    layout2.marginWidth = 2;
	    layout2.marginHeight = 2;
	    client2.setLayout(layout2);
	    
	    ToolBarManager toolBarManager2 = new ToolBarManager();
        toolBarManager2.add(new Action("Show Selected Data") {
        	
        	@Override
        	public String getToolTipText() {
        		return getText();
        	}
        	
        	@Override
        	public ImageDescriptor getImageDescriptor() {
        		return ImageRegistry.getImageDescriptor(QrtPCRImage.SHOW_SELECTED_ICON);
        	}
        	
        	@Override
            public void run() {
        		showSelectedItems(part);
            }
		});
        
        toolBarManager2.add(new Action("Download file") {
        	@Override
        	public ImageDescriptor getImageDescriptor() {
        		return ImageRegistry.getImageDescriptor(QrtPCRImage.DOWNLOAD_ICON);
        	}
        	
        	@Override
        	public String getToolTipText() {
        		return "Download selected instrument file";
        	}
        	
        	@Override
            public void run() {
        		// find selected file
        		TreeItem[] items = plateTree.getSelection();
        		// since we allow single selection only, get the first one
        		if (items.length > 0) {
        			TreeItem selected = items[0];
        			if (selected.getParentItem() != null) {// leaf node
        				MessageDialog.openWarning(Display.getCurrent().getActiveShell(), "Wrong selection", "Please select a plate file entry below to download its instrument file");
        			} else {
        				String filename = selected.getText(1);
        				downloadAction(Display.getCurrent().getActiveShell(), filename, part);
        			}
        		} else {
        			MessageDialog.openWarning(Display.getCurrent().getActiveShell(), "No selection", "Please select a plate file entry below to download its instrument file");
        		}
            }
		});
        ToolBar toolbar2 = toolBarManager2.createControl(section2);
        
        sectionLayoutData = new TableWrapData(TableWrapData.FILL, TableWrapData.BOTTOM);
		sectionLayoutData.grabHorizontal = true;
		sectionLayoutData.grabVertical = true;
		section2.setLayoutData(sectionLayoutData);
        section2.setTextClient(toolbar2);
		
		TreeViewer plateTableViewer = new TreeViewer(client2, SWT.BORDER | SWT.FULL_SELECTION);
		plateTree = plateTableViewer.getTree();
		GridData gd_table_2 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_table_2.heightHint = 300;
		plateTree.setLayoutData(gd_table_2);
		plateTree.setHeaderVisible(true);
		plateTree.setLinesVisible(true);
		section2.setClient(client2);
		
		compositeLayoutData = new GridData(GridData.FILL_BOTH);
		compositeLayoutData.verticalSpan = 1;
		compositeLayoutData.horizontalSpan = 2;
		sectionParentComposite.setLayoutData(compositeLayoutData);
		
		treeViewerColumn2 = new TreeViewerColumn(plateTableViewer, SWT.NONE);
		TreeColumn plateIdColum = treeViewerColumn2.getColumn();
		plateIdColum.setWidth(300);
		plateIdColum.setText("Plate Id");
		
		treeViewerColumn3 = new TreeViewerColumn(plateTableViewer, SWT.NONE);
		TreeColumn tblclmnFile = treeViewerColumn3.getColumn();
		tblclmnFile.setWidth(200);
		tblclmnFile.setText("File");
		
		Composite buttonGroup = new Composite(parent, SWT.NONE);
		GridData buttonGridData = new GridData();
		buttonGroup.setLayoutData(buttonGridData);
		buttonGridData.horizontalSpan = 1;
        buttonGroup.setLayout(new GridLayout(4,false));
		
		btnEvaluateData = new Button(buttonGroup, SWT.NONE);
		btnEvaluateData.setText("Evaluate Data");
		btnEvaluateData.setToolTipText("Apply rules to determine possible reruns and generate tabs with the modified data");
		btnEvaluateData.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
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
						stDevCutoffText.setText(pcrRun.getStDevCutOff() +"");
						stDevCutOff = pcrRun.getStDevCutOff();
						
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
					pcrRun.setStDevCutOff(stDevCutOff);
					stdevCuttOffText.setText(stDevCutOff + "");
					Map<String, Object> parameters = new HashMap<>();
					parameters.put(EvaluateData.DATAOPTION, getDataOptionSelection()+"");
					handlerService.executeHandler(
						commandService.createCommand(EvaluateData.COMMAND_ID, parameters));
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		currentDataOption = new Button(buttonGroup, SWT.RADIO);
		currentDataOption.setText("Use current data");
		currentDataOption.setToolTipText("Use current data to evaluate again. It may not make any changes unless you use a different \"maximum standard deviation\" value");
		currentDataOption.setSelection(true);
		
		rawDataOption = new Button(buttonGroup, SWT.RADIO);
		rawDataOption.setText("Use raw data");
		rawDataOption.setToolTipText("Start from scratch. It will override current evaluation changes");
		rawDataOption.setSelection(false);
		
		revertDataOption = new Button (buttonGroup, SWT.RADIO);
		revertDataOption.setText("Use reverted data");
		revertDataOption.setToolTipText("Revert previous evaluation changes, only keep manual changes and use that data to re-evaluate");
		revertDataOption.setSelection(false);
		
		setTables();
		QrtPCRTableEditor editor = part.getContext().get(QrtPCRTableEditor.class);
		
		plateTree.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				for(TreeItem item : plateTree.getSelection()) {
					if (item.getParentItem() != null)  { // only allow for leaf nodes
						// check if the item is already added
						if (item.getImage().equals(CHECKED)) {
							// item should be open already
							setChecked(item); // de select it to get it removed
							showSelectedItems(part);
							setChecked(item); // select it again to open it again
						}
						else {
							// add the item to be displayed list and open the page
							setChecked(item);
						}
						TreeItem parent = item.getParentItem();
						Integer runId = (Integer) parent.getData();
						String plateId = parent.getText(0);
						QrtPCRTable table = pcrRun.getRun(runId);
						String title = item.getText(0);
						Boolean raw = (Boolean)item.getData();
						part.getContext().set(QrtPCRData.class, table.getPlateDataMap().get(plateId));
						CTabItem plateTab = new CTabItem(cTabItem.getParent(), SWT.NONE);
						plateTab.setText(title);
						plateTab.setShowClose(true);
						part.getContext().set(QrtPCRData.class, table.getPlateDataMap().get(plateId));
						part.getContext().set(CTabItem.class, plateTab);
						part.getContext().set(QrtPCRTableEditor.LOWER_THRESHOLD_CONTEXT, pcrRun.getThreshold());
						part.getContext().set(QrtPCRTableEditor.STDEV_CONTEXT, pcrRun.getStDevCutOff());
						part.getContext().set(QrtPCRTableEditor.RAW_CONTEXT, raw);
						part.getContext().set(QrtPCRTableEditor.RUNID_CONTEXT, runId);
						PlateTablePage page = ContextInjectionFactory.make(
								PlateTablePage.class, part.getContext());
						editor.getcTabItemToPartTabMap().put(plateTab, page);
						editor.switchToLast();	
					}
				}
			}

			@Override
			public void mouseDown(MouseEvent e) {
				for(TreeItem item : plateTree.getSelection()) {
					if(item.getImage() != null) {
						if((e.x > item.getImageBounds(0).x) && (e.x < (item.getImageBounds(0).x + item.getImage().getBounds().width))) {
							if((e.y > item.getImageBounds(0).y) && (e.y < (item.getImageBounds(0).y + item.getImage().getBounds().height))) {
								setChecked(item);
							}
						}
					}
				}
			}

			@Override
			public void mouseUp(MouseEvent e) {
		}});
		
		scrolledComposite.setContent(parent);
		scrolledComposite.setMinSize(parent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setExpandHorizontal(true);
		cTabItem.setControl(scrolledComposite);
		
	}
	
	protected int getDataOptionSelection() {
		if (rawDataOption.getSelection())
			return 1;
		else if (revertDataOption.getSelection())
			return 2;
		return 0;
	}

	protected void createMetadataSection(Composite compositeTop) {
		Composite compositeSettings = new Composite(compositeTop, SWT.NONE);
		compositeSettings.setLayout(new GridLayout(2, false));
		
		Label thresholdLabel = new Label(compositeSettings, SWT.NONE);
		thresholdLabel.setText(Config.THRESHOLD);
		
		Text thresholdText = new Text(compositeSettings, SWT.BORDER | SWT.READ_ONLY);
		thresholdText.setText(pcrRun.getThreshold() + "");
		
		Label stdevCuttOffLabel = new Label(compositeSettings, SWT.NONE);
		stdevCuttOffLabel.setText(Config.STDEVCUTOFF);
		
		stdevCuttOffText = new Text(compositeSettings, SWT.BORDER | SWT.READ_ONLY);
		stdevCuttOffText.setText(pcrRun.getStDevCutOff() + "");
	}
	
	private void downloadAction (Shell shell, String filename, MPart part) {
		// open up a file dialog to download the file
		FileDialog fd = new FileDialog(shell, SWT.SAVE);
        fd.setText("Download");
        fd.setFileName(filename);
        fd.setOverwrite(true);
        String selected = fd.open();
		try {
			if (selected != null && selected.trim().length() != 0) {
				downloadFile (filename, selected, part);
			}
		} catch (IOException e1) {
			logger.error("Could not download file", e1);
			MessageDialog.openError(shell, "File Download Error", "Could not download the file. It has been removed from the workspace");
		}
	}
	
	protected void downloadFile(String file, String newPath, MPart part) throws IOException {
		String workspaceLocation = PropertyHandler.getVariable("workspace_location");
        String projectFolderLocation = workspaceLocation.substring(0, workspaceLocation.length()-1) 
                + File.separator;
        QrtPCRTableEditor editor = part.getContext().get(QrtPCRTableEditor.class);
        Entry qrtPCREntry = editor.getEntry();
        projectFolderLocation += qrtPCREntry.getParent().getParent().getDisplayName();
        String fileFolderLocation = projectFolderLocation
                + File.separator + Config.folderName + File.separator 
                + "files";
        
        File workspaceFile = new File(fileFolderLocation + File.separator + file);
        FileOutputStream out = new FileOutputStream(newPath);
        Files.copy(workspaceFile.toPath(), out);
		out.close();
	}
	
	private void showSelectedItems (MPart part) {
		QrtPCRTableEditor editor = part.getContext().get(QrtPCRTableEditor.class);
		//remove all pages, then add the ones selected
		CTabFolder folder = cTabItem.getParent();
		CTabItem[] items = folder.getItems();
		for (int i = 2; i < items.length; i++) {
			items[i].dispose();
		}
		for (TreeItem item : itemsToShow) {
			TreeItem parent = item.getParentItem();
			Integer runId = (Integer) parent.getData();
			String plateId = parent.getText(0);
			QrtPCRTable table = pcrRun.getRun(runId);
			String title = item.getText(0);
			Boolean raw = (Boolean)item.getData();
			CTabItem plateTab = new CTabItem(cTabItem.getParent(), SWT.NONE);
			plateTab.setText(title);
			plateTab.setShowClose(true);
			part.getContext().set(CTabItem.class, plateTab);
			part.getContext().set(QrtPCRData.class, table.getPlateDataMap().get(plateId));
			part.getContext().set(QrtPCRTableEditor.LOWER_THRESHOLD_CONTEXT, pcrRun.getThreshold());
			part.getContext().set(QrtPCRTableEditor.STDEV_CONTEXT, pcrRun.getStDevCutOff());
			part.getContext().set(QrtPCRTableEditor.RAW_CONTEXT, raw);
			part.getContext().set(QrtPCRTableEditor.RUNID_CONTEXT, runId);

			PlateTablePage page = ContextInjectionFactory.make(
					PlateTablePage.class, part.getContext());
			editor.getcTabItemToPartTabMap().put(plateTab, page);
			// switch to the last tab added
			editor.switchToLast();
		}
	}
	
	private void setChecked(TreeItem item) {
		if(item.getImage().equals(UNCHECKED)) {
			item.setImage(CHECKED);
			itemsToShow.add(item);
		}
		else {
			item.setImage(UNCHECKED);
			itemsToShow.remove(item);
		}
	}
	
	private void setTables () {
		runTable.setRedraw(false);
		runTable.removeAll();
		runTable.setRedraw(true);
		plateTree.setRedraw(false);
		plateTree.removeAll();
		plateTree.setRedraw(true);
		if (pcrRun != null) {
			text.setText(pcrRun.getGeneListFile());
			for (Iterator<Integer> iterator = pcrRun.getRunIdTableMap().keySet().iterator(); iterator.hasNext();) {
				Integer runId = (Integer) iterator.next();
				QrtPCRTable table = pcrRun.getRunIdTableMap().get(runId);
				QrtPCRData data = table.getPlateDataMap().values().iterator().next();
				int numberOfReplicates = data.getNumberOfReplicates();
				
				TableItem mainItem = new TableItem(runTable, SWT.READ_ONLY);
				mainItem.setText(0, runId + "");
				mainItem.setText(2, table.getPlateLayoutFile());
				mainItem.setText(1, numberOfReplicates + "");
				
				Map<String, String> fileMap = table.getInstrumentFileMap();
				for (Iterator<String> iterator1 = fileMap.keySet().iterator(); iterator1.hasNext();) {
					String plateId = (String) iterator1.next();
					String title = plateId;
					if (runId > 0)
						title = "rerun-" + runId + "-" + title;
					title += "-" + Config.RAWPLATE;
					
					TreeItem parent = new TreeItem (plateTree, SWT.NONE);
					parent.setText(0, plateId);
					parent.setText(1, fileMap.get(plateId));
					parent.setData(runId);
					
					TreeItem child1 = new TreeItem(parent, SWT.NONE);
					child1.setText(0, title);
					child1.setImage(UNCHECKED);
					child1.setText(1, "");
					child1.setData(true);
					
					if (!table.getOriginal()) {
						TreeItem child2 = new TreeItem(parent, SWT.NONE);
						title = plateId + "-" + Config.ANALYZEDPLATE;
						if (runId > 0)
							title = "rerun-" + runId + "-" + title;
						child2.setText(0, title);
						child2.setImage(UNCHECKED);
						child2.setText(1, "");
						child2.setData(false);
					}
					
					parent.setExpanded(true);
				}
			}			
		}
	}
	
	public void addPageToPlateTable (Integer runId, String plateId, String filename, boolean original) {
		// first need to find a parent with the given runId and plateId
		// if there is none, create the parent and add the child
		// if there is one, add a child
		boolean found = false;
		for (TreeItem item : plateTree.getItems()) {
			Integer runIdValue = (Integer) item.getData();
			String plateIdString = item.getText(0);
			
			if (runIdValue == runId && plateIdString.equals(plateId)) {
				// add the child
				TreeItem newChild = new TreeItem (item, SWT.NONE);
				String title = plateId;
				if (runId > 0)
					title = "rerun-" + runId + "-" + title;
				if (original) title += "-" + Config.RAWPLATE;
				else title = title + "-" + Config.ANALYZEDPLATE;
				newChild.setText(0, title);
				newChild.setText(1, "");
				newChild.setImage(UNCHECKED);
				newChild.setData(original);
				found = true;
				break;
			}
		}
		if (!found) {
			TreeItem parent = new TreeItem (plateTree, SWT.NONE);
			parent.setText(0, plateId);
			parent.setText(1, filename);
			parent.setData(runId);
			
			TreeItem child1 = new TreeItem(parent, SWT.NONE);
			String title = plateId;
			if (runId > 0)
				title = "rerun-" + runId + "-" + title;
			if (original) title += "-" + Config.RAWPLATE;
			else title = title + "-" + Config.ANALYZEDPLATE;
			child1.setText(0, title);
			child1.setImage(UNCHECKED);
			child1.setText(1, "");
			child1.setData(original);
			parent.setExpanded(true);
		}
		
		plateTree.redraw();
	}
	
	public void setFocus() {
		if (text != null)
			text.setFocus();
	}
}
