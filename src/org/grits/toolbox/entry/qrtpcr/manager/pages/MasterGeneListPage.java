package org.grits.toolbox.entry.qrtpcr.manager.pages;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.widgets.Section;
import org.grits.toolbox.entry.qrtpcr.Config;
import org.grits.toolbox.entry.qrtpcr.ImageRegistry;
import org.grits.toolbox.entry.qrtpcr.ImageRegistry.QrtPCRImage;
import org.grits.toolbox.entry.qrtpcr.editor.IQrtPCRPart;
import org.grits.toolbox.entry.qrtpcr.manager.GeneListManagerEditor;
import org.grits.toolbox.entry.qrtpcr.manager.actions.AddGeneToListAction;
import org.grits.toolbox.entry.qrtpcr.manager.actions.SaveGeneListAction;
import org.grits.toolbox.entry.qrtpcr.manager.actions.UpdateGenesAction;
import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.GeneList;
import org.grits.toolbox.entry.qrtpcr.model.TableData;
import org.grits.toolbox.entry.qrtpcr.model.view.GeneView;
import org.grits.toolbox.entry.qrtpcr.table.manager.ManagerNatTable;
import org.grits.toolbox.entry.qrtpcr.table.manager.ManagerTableBase;
import org.grits.toolbox.entry.qrtpcr.util.ExcelFileHandler;
import org.grits.toolbox.entry.qrtpcr.util.GeneUtils;

public class MasterGeneListPage implements IQrtPCRPart {
	GeneList geneList;
	GeneList subList;
	private TableViewer selectedTableViewer;
	
	private Action upAction;
	private Action downAction;
	private Action deleteAction;
	private SashForm sash;
	
	Boolean allSelected = false;  // unselected at first
	
	ManagerNatTable table;
	private CTabItem cTabItem;
	GeneListManagerEditor editor;
	
	
	@Inject
	public MasterGeneListPage(@Named(GeneListManagerEditor.GENE_LIST) GeneList masterList, 
			@Optional @Named(GeneListManagerEditor.GENE_SUB_LIST) GeneList subList, 
			CTabItem cTabItem) {
		this.cTabItem = cTabItem;
		this.geneList = masterList;
		this.subList = subList;
	}

	
	@PostConstruct
	public void postConstruct(final MPart part)
	{
		this.editor = (GeneListManagerEditor) part.getObject();
		ScrolledComposite scrolledComposite = new ScrolledComposite(cTabItem.getParent(), 
				SWT.H_SCROLL | SWT.V_SCROLL);
		GridData layoutData = new GridData();
		scrolledComposite.setLayoutData(layoutData);
		scrolledComposite.setLayout(new GridLayout());
		Composite parent = new Composite(scrolledComposite, SWT.FILL);
		
		FillLayout layout = new FillLayout();
		layout.marginHeight = 10;
		layout.marginWidth = 4;
		parent.setLayout(layout);

		createSash(parent);
		
		scrolledComposite.setContent(parent);
		scrolledComposite.setMinSize(parent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setExpandHorizontal(true);
		cTabItem.setControl(scrolledComposite);
	}
	
	private void createSash(final Composite parent) {
		sash = new SashForm(parent, SWT.VERTICAL);

		createMainTableSection(sash);
		addSelectedListArea(sash);
		table.setSelectedTableViewer(selectedTableViewer);
		sash.setWeights(new int[] { 10, 3 });
	}
	
	private void createMainTableSection(final Composite parent) {
		Section section = new Section(parent, Section.TITLE_BAR | Section.EXPANDED);
		section.setText(geneList.getListName() + " Genes");
	    // Composite for storing the data
	    final Composite client = new Composite(section, SWT.WRAP)  {
			public Point computeSize(int hint, int hint2, boolean changed) {
				return new Point(0, 0);
			}
		};
	    GridLayout layout = new GridLayout();
	    layout.numColumns = 1;
	    layout.marginWidth = 2;
	    layout.marginHeight = 2;
	    client.setLayout(layout);
	    
	    ToolBarManager toolBarManager = new ToolBarManager();
	    AddGeneToListAction addGeneAction = new AddGeneToListAction(getEditor());
	    
	    toolBarManager.add(new ControlContribution("Search") {
			
			@Override
			protected Control createControl(Composite parent) {
				Composite composite = new Composite(parent, SWT.NONE);
				composite.setLayout(new GridLayout(2, false));
				Label searchLabel = new Label(composite, SWT.NONE );
				searchLabel.setText("Search: ");
				final Text searchField = new Text(composite, SWT.BORDER);
				searchField.addKeyListener(new KeyAdapter() {
					public void keyReleased(KeyEvent e) {
						if (e.character == SWT.CR) {
							doSearch(searchField.getText());
						}
					}

					private void doSearch(String text) {
						// set the filter on the table and refresh table
						// clear selections before searching
						table.deSelectAll();
						table.filter(text);
					}
				});
				return composite;
			}
		});
	    
	    Action selectAllAction = new Action("Select All") {
        	@Override
            public void run() {
        		if (table != null) {
        			selectDeselectAll();
        		}
        	}
        	
        	@Override
        	public ImageDescriptor getImageDescriptor() {
        		return ImageRegistry.getImageDescriptor(QrtPCRImage.SELECT_ICON);
        	}
        	
        	@Override
        	public String getToolTipText() {
        		return "Select/Deselect all genes for the update (or copying)";
        	}
	    };
	    
	    toolBarManager.add(selectAllAction);

	    toolBarManager.add(addGeneAction);
	    
	    UpdateGenesAction updateAction = new UpdateGenesAction(this);
	    toolBarManager.add(updateAction);
	    
	    toolBarManager.add(new Action("Delete") {
	    	
	    	@Override
	    	public ImageDescriptor getImageDescriptor() {
	    		return ImageRegistry.getImageDescriptor(QrtPCRImage.DELETE_ICON);
	    	}
	    	
	    	@SuppressWarnings("unchecked")
			@Override
	    	public void run() {
	    		if (table != null) {
	    			List<TableData> selections = table.getSelected();
	    			if (selections.size() == 0)
	    				return;
	    			boolean delete = MessageDialog.openConfirm(parent.getShell(), "Confirm Delete", "Are you sure you want to delete " + selections.size() + " gene(s) from the list");
	    			if (!delete)
	    				return;
	    			for (TableData selection : selections) {
    					table.removeRow(selection);
    					// delete from selected as well
    					if (selectedTableViewer != null)
    						((List<Gene>)selectedTableViewer.getInput()).remove((Gene)selection);
	    			}
	    			if (selectedTableViewer != null)
	    				selectedTableViewer.refresh();
	    			((GeneListManagerEditor)getEditor()).markDirty();
	    		}
	    	}
	    	
	    	@Override
	    	public String getToolTipText() {
	    		return "Delete selected items from the list";
	    	}
	    });
	    
	    toolBarManager.add (new Action ("Copy To Selected") {
	    	
	    	@Override
	    	public String getToolTipText() {
	    		return "Copy selected genes to the list below";
	    	}
	    	
	    	@Override
	    	public ImageDescriptor getImageDescriptor() {
	    		return ImageRegistry.getImageDescriptor(QrtPCRImage.FORWARD_ICON);
	    	}
	    	
			@Override
	    	public void run() {
	    		if (selectedTableViewer != null) {
	    			GeneSelectionListContentProvider cp = (GeneSelectionListContentProvider) selectedTableViewer.getContentProvider();
	    			List<TableData> selections = table.getSelected();
	    			if (selections.size() == 0)
	    				return;
	    			for (TableData selection : selections) {
	    				if (selection instanceof Gene) {
	    					boolean found = false;
	    					// check if it is already in the list
	    					// if so, do not add again
	    					List<Gene> existing = cp.getGenes();
	    					for (Gene gene : existing) {
								if (gene.getGeneIdentifier().equals(((Gene) selection).getGeneIdentifier())) {
									found = true;
									break;
								}
							}
	    					if (found) {// skip this one
	    						// ask the user if they want to add a duplicate one
	    						boolean addDuplicate = MessageDialog.openQuestion(parent.getShell(), "Duplicate", "This gene is already in the list, do you want to add it again anyway?");
	    						if (addDuplicate) {
	    							// make a copy and add
	    							Gene newGene =GeneUtils.makeACopy((Gene) selection);
	    							existing.add(newGene);
	    						}
	    					} else 
	    						existing.add((Gene)selection);
	    				}
	    			}
	    			table.deSelectAll();
	    			selectedTableViewer.refresh();
	    		}
	    	}
	    });
	    
	    toolBarManager.add (new Action ("Export") {
	    	@Override
	    	public String getToolTipText() {
	    		return "Export the list into Excel";
	    	}
	    	
	    	@Override
	    	public ImageDescriptor getImageDescriptor() {
	    		return ImageRegistry.getImageDescriptor(QrtPCRImage.EXPORT_ICON);
	    	}
	    	
			@Override
	    	public void run() {
				if (table != null) {
					FileDialog fileDialog = new FileDialog(parent.getShell(), SWT.SAVE);
					// Set the text
	                fileDialog.setText("Select File");
	                // Set filter on .xls files
	                fileDialog.setFilterExtensions(new String[] { "*.xlsx" });
	                // Put in a readable name for the filter
	                fileDialog.setFilterNames(new String[] { "Excel (*.xlsx)" });
	                fileDialog.setFileName(geneList.getListName()+ ".xlsx");
	                fileDialog.setOverwrite(true);
	                // Open Dialog and save result of selection
	                String selected = fileDialog.open();
	                if (selected != null) {
	                	ExcelFileHandler.exportMasterGeneList(geneList.getListName(), table, selected);
	                }
				}
			}
	    });
	    
	    
        ToolBar toolbar = toolBarManager.createControl(section);
        section.setTextClient(toolbar);
        
        ManagerTableBase myTable = new ManagerTableBase((GeneListManagerEditor) getEditor());
        myTable.setGeneList(geneList);
        
        table = (ManagerNatTable) myTable.createControl(client);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,20));
        
		section.setClient(client);
		
		addGeneAction.setInput(geneList.getGenes());
		addGeneAction.setOrganism(geneList.getOrganism());
		
	}
	
	public GeneListManagerEditor getEditor() {
		return editor;
	}

	protected void selectDeselectAll() {
		if (!allSelected) {
			table.selectAll();
			allSelected = true;
		} else {
			table.deSelectAll();
			allSelected = false;
		}
	}
	
	public GeneList getSelectedGeneList () {
		GeneList selectedList = new GeneList();
		List<Gene> selectedGenes = new ArrayList<>();
		for (TableData row : table.getSelected()) {
			if (row instanceof Gene) // should be
				selectedGenes.add(((Gene)row));
		}
		selectedList.setGenes(selectedGenes);
		return selectedList;
	}
	
	public List<TableData> getDataFromTable () {
		if (table != null) {
			return table.getTreeList().subList(0, table.getTreeList().size());
		}
		return new ArrayList<TableData>();
	}
	
	public void clearSelection () {
		table.deSelectAll();
		allSelected = false;
	}
	
	int calculateHeightForTable (Composite parent, int weight) {
		int topHeight = parent.getShell().getSize().y - 220;
		int height = (int) Math.ceil((double)topHeight * weight / 13);
		
		return height;
	}

	private void addSelectedListArea (final Composite parent) {
		Section section = new Section(parent, Section.TITLE_BAR | Section.EXPANDED);
		section.setText("Selected Genes");
	    // Composite for storing the data
	    final Composite client = new Composite(section, SWT.WRAP) {
 			public Point computeSize(int hint, int hint2, boolean changed) {
 				return new Point(0, 0);
 			}
	 	};
	 		
	    GridLayout layout = new GridLayout();
	    layout.numColumns = 1;
	    layout.marginWidth = 2;
	    layout.marginHeight = 2;
	    client.setLayout(layout);
	    
	    ToolBarManager toolBarManager = new ToolBarManager();
	    
	    upAction = new Action ("Up") {
	    	@Override
	    	public ImageDescriptor getImageDescriptor() {
	    		return ImageRegistry.getImageDescriptor(QrtPCRImage.UP_ICON);
	    	}
	    	
	    	@Override
	    	public void run() {
	    		GeneSelectionListContentProvider cp = (GeneSelectionListContentProvider) selectedTableViewer.getContentProvider();
	    		cp.up(getElementList(), selectedTableViewer);
	    	}
	    	
	    	@Override
	    	public String getToolTipText() {
	    		return "Move the gene up in the list";
	    	}
	    };
	    
	    downAction = new Action ("Down") {
	    	
	    	@Override
	    	public String getToolTipText() {
	    		return "Move the gene down in the list";
	    	}
	    	
	    	@Override
	    	public ImageDescriptor getImageDescriptor() {
	    		return ImageRegistry.getImageDescriptor(QrtPCRImage.DOWN_ICON);
	    	}
	    	
	    	@Override
	    	public void run() {
	    		GeneSelectionListContentProvider cp = (GeneSelectionListContentProvider) selectedTableViewer.getContentProvider();
	    		cp.down(getElementList(), selectedTableViewer);
	    	}
	    };
	    
	    deleteAction = new Action ("Delete") {
	    	
	    	@Override
	    	public String getToolTipText() {
	    		return "Remove selected gene from the list";
	    	}
	    	
	    	@Override
	    	public ImageDescriptor getImageDescriptor() {
	    		return ImageRegistry.getImageDescriptor(QrtPCRImage.DELETE_ICON);
	    	}
	    	
			@Override
	    	public void run() {
				GeneSelectionListContentProvider cp = (GeneSelectionListContentProvider) selectedTableViewer.getContentProvider();
	    		List<?> selected = getElementList();
	    		for (Object object : selected) {
	    			((List<?>)cp.getGenes()).remove(object);
				}
	    		selectedTableViewer.refresh();
	    	}
	    };
	    
	    
	    
	    SaveGeneListAction saveAsAction = new SaveGeneListAction();
	    
	    upAction.setEnabled(false);
	    downAction.setEnabled(false);
	    deleteAction.setEnabled(false);
	    
	    toolBarManager.add(upAction);
	    toolBarManager.add(downAction);
	    
	    toolBarManager.add(deleteAction);
	    
	    toolBarManager.add(saveAsAction);
	    
	    toolBarManager.add (new Action ("Export") {
	    	@Override
	    	public String getToolTipText() {
	    		return "Export the list into Excel";
	    	}
	    	
	    	@Override
	    	public ImageDescriptor getImageDescriptor() {
	    		return ImageRegistry.getImageDescriptor(QrtPCRImage.EXPORT_ICON);
	    	}
	    	
			@Override
	    	public void run() {
				if (selectedTableViewer != null) {
					FileDialog fileDialog = new FileDialog(parent.getShell(), SWT.SAVE);
					// Set the text
	                fileDialog.setText("Select File");
	                // Set filter on .xls files
	                fileDialog.setFilterExtensions(new String[] { "*.xlsx" });
	                // Put in a readable name for the filter
	                fileDialog.setFilterNames(new String[] { "Excel (*.xlsx)" });
	                if (subList != null) fileDialog.setFileName(subList.getListName()+ ".xlsx");
	                fileDialog.setOverwrite(true);
	                // Open Dialog and save result of selection
	                String selected = fileDialog.open();
	                if (selected != null) {
	                	// get the name from the full path
	                	if (selected.contains("/")){
	                		String nameOnly = selected.substring(selected.lastIndexOf("/")+1);
	                		ExcelFileHandler.exportMasterGeneList(nameOnly, selectedTableViewer, selected);
	                	} else {
	                		ExcelFileHandler.exportMasterGeneList(selected, selectedTableViewer, selected);
	                	}
	                }
				}
			}
	    });
	    
	    ToolBar toolbar = toolBarManager.createControl(section);
        section.setTextClient(toolbar);
		
		selectedTableViewer = new TableViewer(client, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
		Table table = selectedTableViewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd.heightHint = calculateHeightForTable(parent, 3);
		table.setLayoutData(gd);
		
		section.setClient(client);
		
		TableViewerColumn orderColumn = new TableViewerColumn(selectedTableViewer, SWT.NONE);
		TableColumn order = orderColumn.getColumn();
		order.setText(Config.ORDER);
		order.setWidth(50);
		orderColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Gene) {
					GeneSelectionListContentProvider cp= (GeneSelectionListContentProvider) selectedTableViewer.getContentProvider();
					return String.valueOf(cp.getGenes().indexOf(element));
				}
				else
					return null;
			}
		});
		
		createColumns(selectedTableViewer);
		
		if (subList != null) {
			selectedTableViewer.setContentProvider(new GeneSelectionListContentProvider(subList.getGenes()));
			selectedTableViewer.setInput(subList.getGenes());
		}else {
			selectedTableViewer.setContentProvider(new GeneSelectionListContentProvider(new ArrayList<Gene>()));
			selectedTableViewer.setInput(new ArrayList<Gene>());
		}
		selectedTableViewer.addSelectionChangedListener(new GeneSelectionListAdapter());
		
		saveAsAction.setViewer(selectedTableViewer);
		saveAsAction.setEditor(getEditor());
		saveAsAction.setMasterList(geneList);
	}
	
	private void createColumns (TableViewer tableViewer) {
		TableViewerColumn geneIdColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn geneId = geneIdColumn.getColumn();
		//geneId.setWidth(100);
		geneId.setText(Config.GENEID);
		geneIdColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Gene) {
					return ((Gene) element).getGeneIdentifier();
				}
				else
					return null;
			}
			
		});
		
		geneId.pack();
		
		TableViewerColumn geneSymbolColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn geneSymbol = geneSymbolColumn.getColumn();
		//geneSymbol.setWidth(150);
		geneSymbol.setText(Config.GENESYMBOL);
		geneSymbolColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Gene) {
					return ((Gene) element).getGeneSymbol();
				}
				else
					return null;
			}
		});
	
		geneSymbol.pack();
		
		TableViewerColumn idColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn id = idColumn.getColumn();
		//id.setWidth(150);
		id.setText(Config.ID);
		idColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Gene) {
					if (((Gene)element).getGeneIds() != null)
						return ((Gene) element).getGeneIdString();
					else 
						return null;
				}
				else
					return null;
			}
		});
		
		id.pack();
		
		TableViewerColumn nameColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn fullName = nameColumn.getColumn();
		fullName.setText(Config.NAME);
		nameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Gene) {
					return ((Gene) element).getFullName();
				}
				else
					return null;
			}
			
			@Override
			public Color getBackground(Object element) {
				if (element instanceof GeneView) {
					if (((GeneView) element).isUpdated()) {
						if (((GeneView) element).getUpdatedFields().contains(Config.NAME))
							return Display.getDefault().getSystemColor(SWT.COLOR_RED);
					}
				}
				return super.getBackground(element);
			}
		});
		
		fullName.pack();
		
		TableViewerColumn designedForColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn designedFor = designedForColumn.getColumn();
		//designedFor.setWidth(150);
		designedFor.setText(Config.DESIGNEDFOR);
		designedForColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Gene) {
					return ((Gene) element).getNotes();
				}
				else
					return null;
			}
		});
		
		designedFor.pack();
		
		TableViewerColumn forwardPrimerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn forwardPrimer = forwardPrimerColumn.getColumn();
		//forwardPrimer.setWidth(150);
		forwardPrimer.setText(Config.FWPRIMER);
		forwardPrimerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Gene) {
					return ((Gene) element).getForwardPrimer();
				}
				else
					return null;
			}
		});
		
		forwardPrimer.pack();
		
		TableViewerColumn reversePrimerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn reversePrimer = reversePrimerColumn.getColumn();
		//reversePrimer.setWidth(150);
		reversePrimer.setText(Config.REVPRIMER);
		reversePrimerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Gene) {
					return ((Gene) element).getReversePrimer();
				}
				else
					return null;
			}
		});
		
		reversePrimer.pack();
		
		TableViewerColumn aliasColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn alias = aliasColumn.getColumn();
		alias.setText(Config.ALIASES);
		aliasColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Gene) {
					return ((Gene) element).getAliasString();
				}
				else
					return null;
			}
			
			@Override
			public Color getBackground(Object element) {
				if (element instanceof GeneView) {
					if (((GeneView) element).isUpdated()) {
						if (((GeneView) element).getUpdatedFields().contains(Config.ALIASES))
							return Display.getDefault().getSystemColor(SWT.COLOR_RED);
					}
				}
				return super.getBackground(element);
			}
		});
		
		alias.pack();
		
		TableViewerColumn descriptionColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn description = descriptionColumn.getColumn();
		//description.setWidth(150);
		description.setText(Config.DESCRIPTION);
		descriptionColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Gene) {
					return ((Gene) element).getDescription();
				}
				else
					return null;
			}
		});
		
		
		description.pack();
		
		TableViewerColumn groupColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn group = groupColumn.getColumn();
		//group.setWidth(150);
		group.setText(Config.GROUP);
		groupColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Gene) {
					return ((Gene) element).getGroup();
				}
				else
					return null;
			}
		});
	
		group.pack();
		
		TableViewerColumn locationColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn location = locationColumn.getColumn();
		location.setText(Config.LOCATION);
		locationColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Gene) {
					return ((Gene) element).getLocationString();
				}
				else
					return null;
			}
			@Override
			public Color getBackground(Object element) {
				if (element instanceof GeneView) {
					if (((GeneView) element).isUpdated()) {
						if (((GeneView) element).getUpdatedFields().contains(Config.LOCATION))
							return Display.getDefault().getSystemColor(SWT.COLOR_RED);
					}
				}
				return super.getBackground(element);
			}
		});
		
		location.pack();
		
		TableViewerColumn refSeqColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn refSeq = refSeqColumn.getColumn();
		//refSeq.setWidth(150);
		refSeq.setText(Config.REFSEQ);
		refSeqColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Gene) {
					return ((Gene) element).getRefSeqString();
				}
				else
					return null;
			}
			@Override
			public Color getBackground(Object element) {
				if (element instanceof GeneView) {
					if (((GeneView) element).isUpdated()) {
						if (((GeneView) element).getUpdatedFields().contains(Config.REFSEQ))
							return Display.getDefault().getSystemColor(SWT.COLOR_RED);
					}
				}
				return super.getBackground(element);
			}
		});
		
		refSeq.pack();
		
		TableViewerColumn refSeqColumn2 = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn refSeq2 = refSeqColumn2.getColumn();
		//refSeq.setWidth(150);
		refSeq2.setText(Config.SECONDARYREFSEQ);
		refSeqColumn2.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Gene) {
					return ((Gene) element).getSecondaryRefSeqString();
				}
				else
					return null;
			}
			@Override
			public Color getBackground(Object element) {
				if (element instanceof GeneView) {
					if (((GeneView) element).isUpdated()) {
						if (((GeneView) element).getUpdatedFields().contains(Config.SECONDARYREFSEQ))
							return Display.getDefault().getSystemColor(SWT.COLOR_RED);
					}
				}
				return super.getBackground(element);
			}
		});
		
		refSeq2.pack();
		
		TableViewerColumn controlColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn control = controlColumn.getColumn();
		//control.setWidth(150);
		control.setText(Config.CONTROL);
		controlColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Gene) {
					return ((Gene) element).getIsCommon() ? "Control" : "";
				}
				else
					return null;
			}
		});
		
		control.pack();
	}
	
	List<?> getElementList() {
		IStructuredSelection selection= (IStructuredSelection) selectedTableViewer.getSelection();
		List<?> elements= selection.toList();
		ArrayList<Object> elementList= new ArrayList<>();

		for (int i= 0; i < elements.size(); i++) {
			elementList.add(elements.get(i));
		}
		return elementList;
	}
	
	class GeneSelectionListAdapter implements ISelectionChangedListener {

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			IStructuredSelection selection= (IStructuredSelection) selectedTableViewer.getSelection();

			List<?> selections= selection.toList();
			GeneSelectionListContentProvider cp= (GeneSelectionListContentProvider) selectedTableViewer.getContentProvider();

			upAction.setEnabled(cp.canMoveUp(selections));
			downAction.setEnabled(cp.canMoveDown(selections));
			deleteAction.setEnabled(!selections.isEmpty());
		}
	}

	public void refresh(List<Gene> existing) {
		if (table != null) {
	        List<TableData>  allData = new ArrayList<TableData>();
	        for (Gene gene : existing) {
				allData.add(gene);
			}
	       // this.geneList.setGenes(existing);
			table.setTableData(allData);
			table.setRedraw(true);
			table.updateTable();
		}
	}
	
	public ManagerNatTable getTable() {
		return table;
	}
}
