package org.grits.toolbox.entry.qrtpcr.manager.pages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.grits.toolbox.entry.qrtpcr.Config;
import org.grits.toolbox.entry.qrtpcr.ImageRegistry;
import org.grits.toolbox.entry.qrtpcr.ImageRegistry.QrtPCRImage;
import org.grits.toolbox.entry.qrtpcr.editor.IQrtPCRPart;
import org.grits.toolbox.entry.qrtpcr.manager.GeneListManagerEditor;
import org.grits.toolbox.entry.qrtpcr.manager.actions.SaveGeneListDialog;
import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.GeneList;
import org.grits.toolbox.entry.qrtpcr.util.FileUtils;

public class GeneListOverviewPage implements IQrtPCRPart {

	private static final Logger logger = Logger.getLogger(GeneListOverviewPage.class);
	private List<GeneList> masterLists;
	private List<GeneList> masterSubLists;
	private TableViewer masterTableViewer;
	private TableViewer subListTableViewer;
	private CTabItem cTabItem;
	
	@Inject
	public GeneListOverviewPage(@Named(GeneListManagerEditor.MASTER_LIST) List<GeneList> masterLists, 
			@Named(GeneListManagerEditor.MASTER_SUB_LIST) List<GeneList> masterSubLists,
			CTabItem cTabItem) {
		this.cTabItem = cTabItem;
		this.masterLists = masterLists;
		this.masterSubLists = masterSubLists;
	}


	@PostConstruct
	public void postConstruct(final MPart part)
	{
		
		ScrolledComposite scrolledComposite = new ScrolledComposite(cTabItem.getParent(), 
		SWT.H_SCROLL | SWT.V_SCROLL);
		GridData layoutData = new GridData();
		//scrolledComposite.setLayoutData(layoutData);
		scrolledComposite.setLayout(new GridLayout());
		Composite parent = new Composite(scrolledComposite, SWT.FILL);
		parent.setLayout(new GridLayout(1, false));
		
		Action downloadAction = new Action("Download file") {
			@Override
			public String getToolTipText() {
				return "Download the selected master gene list file";
			}
			
			@Override
			public ImageDescriptor getImageDescriptor() {
				return ImageRegistry.getImageDescriptor(QrtPCRImage.DOWNLOAD_ICON);
			}
			
			@Override
            public void run() {
				// find selected file
        		TableItem[] items = masterTableViewer.getTable().getSelection();
        		// since we allow single selection only, get the first one
        		if (items.length > 0) {
        			TableItem selected = items[0];
        			String filename = selected.getText(2);
        			downloadAction(Display.getCurrent().getActiveShell(), filename, false);
        		} else {
        			MessageDialog.openWarning(Display.getCurrent().getActiveShell(), "No selection", "Please select a master list to download");
        		}
            }
		};
		
		Action addAction = new Action("Add") {
			
			@Override
			public String getToolTipText() {
				return "Create a new (empty) master gene list";
			}
			
			@Override
			public ImageDescriptor getImageDescriptor() {
				return ImageRegistry.getImageDescriptor(QrtPCRImage.ADD_ICON);
			}
			
			@Override
			public void run() {
				SaveGeneListDialog dialog = new SaveGeneListDialog(Display.getCurrent().getActiveShell(), masterLists);
				dialog.create();
				dialog.getShell().setSize(550, 300);
				if (dialog.open() == Window.OK) {
					GeneList newGeneList = new GeneList();
					newGeneList.setListName(dialog.getName());
					newGeneList.setDescription(dialog.getDescription());
					newGeneList.setOrganism(dialog.getOrganism());
					newGeneList.setDateCreated(new Date());
					newGeneList.setGenes (new ArrayList<Gene>());
					
					try {
						String filename = FileUtils.saveMasterGeneListFile(newGeneList);
						newGeneList.setFilename(filename);
					} catch (IOException | JAXBException e) {
						logger.error("Error saving the new master list to a file", e);
						ErrorUtils.createErrorMessageBox(Display.getCurrent().getActiveShell(), "Error saving the new master list to a file. Reason: " + e.getMessage());
					}
					
					masterLists.add(newGeneList);
					masterTableViewer.refresh();
				}
			}
		};
		Action deleteAction = new Action("Delete") {
			
			@Override
			public ImageDescriptor getImageDescriptor() {
				return ImageRegistry.getImageDescriptor(QrtPCRImage.DELETE_ICON);
			}
			
			
	    	@SuppressWarnings("unchecked")
			@Override
	    	public void run() {
	    		if (masterTableViewer != null) {
	    			TableItem[] selections = masterTableViewer.getTable().getSelection();
	    			if (selections.length == 0)
	    				return;
	    			boolean delete = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), "Confirm Delete", "Are you sure you want to delete " + selections.length + " gene list");
	    			if (!delete)
	    				return;
	    			for (TableItem tableItem : selections) {
	    				Object selection = tableItem.getData();
	    				if (selection instanceof GeneList) {
	    					try {
								FileUtils.deleteFile(((GeneList) selection).getFilename(), false);
							} catch (IOException e) {
								logger.error("Could not delete master list", e);
								MessageDialog.openError(Display.getCurrent().getActiveShell(),"Error", "Could not delete the file. Reason: " + e.getMessage());
								return;
							}
	    					((List<GeneList>)masterTableViewer.getInput()).remove((GeneList)selection);
	    				}
	    			}
	    			masterTableViewer.refresh();
	    		}
	    	}
	    	
	    	@Override
	    	public String getToolTipText() {
	    		return "Delete selected items from the list";
	    	}
	    };
		
		Action importAction = new Action ("Import") {
			
			@Override
	    	public String getToolTipText() {
	    		return "Import a gene list file";
	    	}
	    	
	    	@Override
	    	public ImageDescriptor getImageDescriptor() {
	    		return ImageRegistry.getImageDescriptor(QrtPCRImage.IMPORT_ICON);
	    	}
	    	
			@Override
			public void run() {
				FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.OPEN);
				dialog.setFilterExtensions(new String[] {"*.xml"});
				String filename = dialog.open();
				if (filename != null) {
					try {
						GeneList newGeneList = FileUtils.importAndLoadGeneListFile(filename, false);
						masterLists.add(newGeneList);
						masterTableViewer.refresh();
					} catch (JAXBException e) {
						logger.error("Error loading the new gene list from the given file. The file is not in valid format", e);
						ErrorUtils.createErrorMessageBox(Display.getCurrent().getActiveShell(), "Error loading the new gene list from the given file. Reason: " + e.getMessage());
					} catch (IOException e) {
						logger.error("Error loading the new gene list from the given file.", e);
						ErrorUtils.createErrorMessageBox(Display.getCurrent().getActiveShell(), "Error loading the new gene list from the given file. Reason: " + e.getMessage());
					
					}
				}
			}
		};
		
		masterTableViewer = createTable (part, parent, masterLists, "Master Gene Lists", new Action[] {importAction, downloadAction, addAction, deleteAction}, false);
		
		Action downloadAction2 = new Action("Download file") {
			
			@Override
			public String getToolTipText() {
				return "Download the selected sub-list file";
			}
			
			@Override
			public ImageDescriptor getImageDescriptor() {
				return ImageRegistry.getImageDescriptor(QrtPCRImage.DOWNLOAD_ICON);
			}
			
			@Override
            public void run() {
        		
				// find selected file
        		TableItem[] items = subListTableViewer.getTable().getSelection();
        		// since we allow single selection only, get the first one
        		if (items.length > 0) {
        			TableItem selected = items[0];
        			String filename = selected.getText(2);
        			downloadAction(Display.getCurrent().getActiveShell(), filename, true);
        		} else {
        			MessageDialog.openWarning(Display.getCurrent().getActiveShell(), "No selection", "Please select a sublist to download");
        		}
            }
		};
		
		Action deleteAction2 = new Action("Delete") {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return ImageRegistry.getImageDescriptor(QrtPCRImage.DELETE_ICON);
			}
			
	    	@SuppressWarnings("unchecked")
			@Override
	    	public void run() {
	    		if (subListTableViewer != null) {
	    			TableItem[] selections = subListTableViewer.getTable().getSelection();
	    			if (selections.length == 0)
	    				return;
	    			boolean delete = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), "Confirm Delete", "Are you sure you want to delete " + selections.length + " gene list");
	    			if (!delete)
	    				return;
	    			for (TableItem tableItem : selections) {
	    				Object selection = tableItem.getData();
	    				if (selection instanceof GeneList) {
	    					try {
								FileUtils.deleteFile(((GeneList) selection).getFilename(), true);
							} catch (IOException e) {
								logger.error("Could not delete sub-list", e);
								MessageDialog.openError(Display.getCurrent().getActiveShell(),"Error", "Could not delete the file. Reason: " + e.getMessage());
								return;
							}
	    					((List<GeneList>)subListTableViewer.getInput()).remove((GeneList)selection);
	    				}
	    			}
	    			subListTableViewer.refresh();
	    		}
	    	}
	    	
	    	@Override
	    	public String getToolTipText() {
	    		return "Delete selected items from the list";
	    	}
	    };
	    
	    Action importAction2 = new Action ("Import") {
	    	@Override
	    	public String getToolTipText() {
	    		return "Import a gene list file";
	    	}
	    	
	    	@Override
	    	public ImageDescriptor getImageDescriptor() {
	    		return ImageRegistry.getImageDescriptor(QrtPCRImage.IMPORT_ICON);
	    	}
	    	
			@Override
			public void run() {
				FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.OPEN);
				dialog.setFilterExtensions(new String[] {"*.xml"});
				String filename = dialog.open();
				if (filename != null) {
					try {
						GeneList newGeneList = FileUtils.importAndLoadGeneListFile(filename, true);
						masterSubLists.add(newGeneList);
						subListTableViewer.refresh();
					} catch (JAXBException e) {
						logger.error("Error loading the new gene list from the given file. The file is not in valid format", e);
						ErrorUtils.createErrorMessageBox(Display.getCurrent().getActiveShell(), "Error loading the new gene list from the given file. Reason: " + e.getMessage());
					} catch (IOException e) {
						logger.error("Error loading the new gene list from the given file.", e);
						ErrorUtils.createErrorMessageBox(Display.getCurrent().getActiveShell(), "Error loading the new gene list from the given file. Reason: " + e.getMessage());
					
					}
				}
			}
		};
		
		subListTableViewer = createTable (part, parent, masterSubLists, "Sub-Lists", new Action[] {importAction2, downloadAction2, deleteAction2}, true);
		
		scrolledComposite.setContent(parent);
		scrolledComposite.setMinSize(parent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setExpandHorizontal(true);
		cTabItem.setControl(scrolledComposite);
	}
	
	private TableViewer createTable (MPart part, Composite parent, List<GeneList> geneList, String title, Action[] actions, final boolean sublist) {
		
		Composite sectionParentComposite = new Composite(parent, SWT.FILL);
		sectionParentComposite.setLayout(new TableWrapLayout());
		
	    Section section = new Section(sectionParentComposite, Section.TITLE_BAR | Section.EXPANDED);
	    section.setText(title); 
	    // Composite for storing the data
	    Composite client = new Composite(section, SWT.WRAP);
	    GridLayout layout = new GridLayout(1, true);
	    layout.marginWidth = 2;
	    layout.marginHeight = 2;
	    client.setLayout(layout);
	    
	    final TableViewer tableViewer2 = new TableViewer(client, SWT.BORDER |  SWT.FULL_SELECTION);
	    
	    ToolBarManager toolBarManager = new ToolBarManager();
	    for (Action action : actions) {
			toolBarManager.add(action);
		}
	    
	    Action openSelectedAction = new Action("Show selected") {
			@Override
	    	public String getToolTipText() {
	    		return "Open selected list";
	    	}
	    	
	    	@Override
	    	public ImageDescriptor getImageDescriptor() {
	    		return ImageRegistry.getImageDescriptor(QrtPCRImage.SHOW_SELECTED_ICON);
	    	}
	    	
			@Override
			public void run() {
				openSelectedList(part, tableViewer2, sublist);
			}
		};
		
		toolBarManager.add(openSelectedAction);
        ToolBar toolbar = toolBarManager.createControl(section); 
        section.setTextClient(toolbar);
		
		Table table = tableViewer2.getTable();
		GridData gd_table_2 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_table_2.heightHint = 300;
		table.setLayoutData(gd_table_2);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		TableWrapData sectionLayoutData = new TableWrapData(TableWrapData.FILL, TableWrapData.BOTTOM);
		sectionLayoutData.grabHorizontal = true;
		sectionLayoutData.grabVertical = true;
		section.setLayoutData(sectionLayoutData);
		section.setClient(client);
		
		GridData compositeLayoutData = new GridData(GridData.FILL_BOTH);
		compositeLayoutData.verticalSpan = 1;
		compositeLayoutData.horizontalSpan = 2;
		sectionParentComposite.setLayoutData(compositeLayoutData);
		
		TableViewerColumn listNameColumn = new TableViewerColumn(tableViewer2, SWT.NONE);
		TableColumn listName = listNameColumn.getColumn();
		listName.setWidth(100);
		listName.setText(Config.NAME);
		
		listNameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof GeneList) 
					return ((GeneList) element).getListName();
				else
					return null;
			}
		});
		
		listNameColumn.setEditingSupport(new TextEditingSupport(part, tableViewer2, listName));
		
		TableViewerColumn descriptionColumn = new TableViewerColumn(tableViewer2, SWT.NONE);
		TableColumn listDescription = descriptionColumn.getColumn();
		listDescription.setWidth(300);
		listDescription.setText(Config.DESCRIPTION);
		
		descriptionColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof GeneList) 
					return ((GeneList) element).getDescription();
				else
					return null;
			}
		});
		
		descriptionColumn.setEditingSupport(new TextEditingSupport(part, tableViewer2, listDescription));
		
		TableViewerColumn fileNameColumn = new TableViewerColumn(tableViewer2, SWT.NONE);
		TableColumn listFileName = fileNameColumn.getColumn();
		listFileName.setWidth(150);
		listFileName.setText("File Name");
		
		fileNameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof GeneList) 
					return ((GeneList) element).getFilename();
				else 
					return null;
			}
		});
		
		TableViewerColumn dateCreatedColumn = new TableViewerColumn(tableViewer2, SWT.NONE);
		TableColumn dateCreated = dateCreatedColumn.getColumn();
		dateCreated.setWidth(80);
		dateCreated.setText("Date Created");
		
		dateCreatedColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof GeneList) 
					if (((GeneList) element).getDateCreated() != null)
						return ((GeneList) element).getDateCreated().toString();
				return null;
			}
		});
		
		tableViewer2.setContentProvider(new ArrayContentProvider());
		tableViewer2.setInput(geneList);
		
		table.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				openSelectedList(part, tableViewer2, sublist);
			}

			@Override
			public void mouseDown(MouseEvent e) {
				
			}

			@Override
			public void mouseUp(MouseEvent e) {
				
			}
		
		});
		
		return tableViewer2;
	}
	
	private void openSelectedList (MPart part, TableViewer tableViewer2, boolean sublist) {
		GeneListManagerEditor editor = (GeneListManagerEditor) part.getObject();
		for(TableItem item : tableViewer2.getTable().getSelection()) {
			String listName = item.getText(0);
			
			CTabFolder cTabFolder = part.getContext().get(CTabFolder.class);
			if (sublist) {
				GeneList masterList = getMasterGeneList(((GeneList)item.getData()).getParentListId());
				if (masterList != null) {
					CTabItem cTabItem = new CTabItem(cTabFolder, SWT.NONE);
					cTabItem.setText("GeneList-" + listName);
					cTabItem.setShowClose(true);
					part.getContext().set(CTabItem.class, cTabItem);
					part.getContext().set(GeneListManagerEditor.GENE_LIST, masterList);
					part.getContext().set(GeneListManagerEditor.GENE_SUB_LIST, (GeneList)item.getData());
					MasterGeneListPage masterPageTab = ContextInjectionFactory.make(MasterGeneListPage.class, part.getContext());
					
					editor.getcTabItemToPartTabMap().put(cTabItem, masterPageTab);
					
				} else {
					CTabItem cTabItem = new CTabItem(cTabFolder, SWT.NONE);
					cTabItem.setText("GeneList-" + listName);
					cTabItem.setShowClose(true);
					part.getContext().set(CTabItem.class, cTabItem);
					part.getContext().set(GeneListManagerEditor.GENE_LIST, (GeneList)item.getData());
					part.getContext().set(GeneListManagerEditor.GENE_SUB_LIST, null);
					MasterGeneListPage masterPageTab = ContextInjectionFactory.make(MasterGeneListPage.class, part.getContext());
					
					editor.getcTabItemToPartTabMap().put(cTabItem, masterPageTab);
				}
			}else {
				CTabItem cTabItem = new CTabItem(cTabFolder, SWT.NONE);
				cTabItem.setText("GeneList-" + listName);
				cTabItem.setShowClose(true);
				part.getContext().set(CTabItem.class, cTabItem);
				part.getContext().set(GeneListManagerEditor.GENE_LIST, (GeneList)item.getData());
				part.getContext().set(GeneListManagerEditor.GENE_SUB_LIST, null);
				MasterGeneListPage masterPageTab = ContextInjectionFactory.make(MasterGeneListPage.class, part.getContext());

				editor.getcTabItemToPartTabMap().put(cTabItem, masterPageTab);
			}
			cTabFolder.setSelection(cTabFolder.getItemCount()-1);
			
		}
	}
	
	private void downloadAction (Shell shell, String filename, boolean sublist) {
		// open up a file dialog to download the file
		FileDialog fd = new FileDialog(shell, SWT.SAVE);
        fd.setText("Download");
        fd.setFileName(filename);
        fd.setOverwrite(true);
        String selected = fd.open();
		try {
			if (selected != null && selected.trim().length() != 0) {
				FileUtils.downloadFile (filename, selected, sublist);
			}
		} catch (IOException e1) {
			logger.error("Could not download file", e1);
			MessageDialog.openError(shell, "File Download Error", "Could not download the file. It has been removed from the configuration directory");
		}
	}

	public void addSubList(GeneList newSublist) {
		masterSubLists.add(newSublist);
		subListTableViewer.refresh();	
	}
	
	GeneList getMasterGeneList (String listName) {
		for (GeneList list : masterLists) {
			if(list.getListName().equals(listName))
				return list;
		}
		return null;
	}
}
