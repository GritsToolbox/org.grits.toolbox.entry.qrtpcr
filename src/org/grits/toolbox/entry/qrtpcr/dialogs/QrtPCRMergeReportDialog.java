package org.grits.toolbox.entry.qrtpcr.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.dialog.ModalDialog;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.datamodel.util.DataModelSearch;
import org.grits.toolbox.core.utilShare.ListenerFactory;
import org.grits.toolbox.entry.qrtpcr.editor.QrtPCREntrySelectionAdapter;
import org.grits.toolbox.entry.qrtpcr.property.QrtPCRMergeProperty;
import org.grits.toolbox.entry.qrtpcr.property.QrtPCRProperty;

public class QrtPCRMergeReportDialog extends ModalDialog {

	private Label nameLabel;
	private Text nameText;
	private Label descriptionLabel;
	private Text descriptionText;
	private Label listLabel;
	private Table table;
	private QrtPCREntrySelectionAdapter qrtPCREntrySelectionAdapter;
	private List<Entry> qrtPCREntryList;
	private Button okButton;
	private Button cancelButton;
	private String name;
	private String description;
	
	private Map<String, String> listEntries = new HashMap<String, String>();
	private TableEditor editor;

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public List<Entry> getQrtPCREntryList() {
		return qrtPCREntryList;
	}

	public Map<String, String> getListEntries() {
		return listEntries;
	}

	public QrtPCRMergeReportDialog(Shell parentShell) {
		super(parentShell);
	}
	
	public QrtPCRMergeReportDialog(Shell parentShell, List<Entry> entries) {
		super(parentShell);
		if (entries == null) {
			this.qrtPCREntryList = new java.util.ArrayList<Entry>();
		} else {
			this.qrtPCREntryList = entries;
		}
	}
	
	@Override
	public void create() {
		super.create();
		setTitle("Merge qRT-PCR data");
		setMessage("");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		this.parent = parent;

		initGrid(parent);		
		createDisplayName(parent);
		createDescription(parent);

		createEmptyLine(parent);
		createListHeader(parent);		
		createList(parent);
		createAddAndDelButtons(parent);

		createEmptyLine(parent);
		createLineSeparator(parent);
		createMergeOKAndCancelButtons(parent);

		return parent;
	}

	private void createMergeOKAndCancelButtons(Composite parent) {
		// create a grdiData for OKButton
		Label dummy2 = new Label(parent, SWT.NONE);
		GridData gdDummy2 = new GridData();
		gdDummy2.horizontalSpan = 4;
		gdDummy2.grabExcessHorizontalSpace = true;
		dummy2.setLayoutData(gdDummy2);

		GridData cancelData = new GridData();
		cancelData.grabExcessHorizontalSpace = false;
		cancelData.horizontalAlignment = GridData.END;
		cancelData.horizontalSpan = 1;
		cancelButton = new Button(parent, SWT.PUSH);
		cancelButton.setText("Cancel");
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				cancelPressed();
			}
		});
		cancelButton.setLayoutData(cancelData);
		
		GridData okData = new GridData();
		okData.grabExcessHorizontalSpace = false;
		okData.horizontalAlignment = GridData.END;
		okData.horizontalSpan = 1;
		okButton = new Button(parent, SWT.PUSH);
		okButton.setText("   OK   ");
		okButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				okPressed();
				close();
			}
		});
		okButton.setLayoutData(okData);
		// check is ready to finish
		if (isReadyToFinish()) {
			okButton.setEnabled(true);
		} else {
			okButton.setEnabled(false);
		}

		
		new Label(parent, SWT.NONE);
		
	}

	private void createAddAndDelButtons(Composite parent) {
		// create a grdiData for OKButton
		Label dummy = new Label(parent, SWT.NONE);
		GridData gdDummy = new GridData();
		dummy.setLayoutData(gdDummy);

		Label dummy2 = new Label(parent, SWT.NONE);
		GridData gdDummy2 = new GridData();
		gdDummy2.horizontalSpan = 2;
		gdDummy2.grabExcessHorizontalSpace = true;
		dummy2.setLayoutData(gdDummy2);

		Label dummy3 = new Label(parent, SWT.NONE);
		GridData gdDummy3 = new GridData();
		dummy3.setLayoutData(gdDummy3);

		GridData gdAddBtn = new GridData();
		gdAddBtn.grabExcessHorizontalSpace = false;
		gdAddBtn.horizontalAlignment = GridData.END;
		gdAddBtn.horizontalSpan = 1;
		Button btnAddButton = new Button(parent, SWT.PUSH);
		btnAddButton.setText("  Add  ");
		qrtPCREntrySelectionAdapter = new QrtPCREntrySelectionAdapter(
				QrtPCRProperty.TYPE, "Select Entry",
				"Select a qRT-PCR Entry");
		qrtPCREntrySelectionAdapter.setParent(parent);
		qrtPCREntrySelectionAdapter.setList(table);
		qrtPCREntrySelectionAdapter.setListEntries(listEntries);
		qrtPCREntrySelectionAdapter.setEntries(qrtPCREntryList);
		btnAddButton.addSelectionListener(qrtPCREntrySelectionAdapter);
		btnAddButton.setLayoutData(gdAddBtn);

		Button deleteButton = new Button(parent, SWT.PUSH);
		GridData gdDelBtn = new GridData();
		gdDelBtn.grabExcessHorizontalSpace = false;
		gdDelBtn.horizontalAlignment = GridData.END;
		gdDelBtn.horizontalSpan = 1;
		deleteButton.setText("Delete");
		deleteButton.setLayoutData(gdDelBtn);
		deleteButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// then delete selected items from the list
				int iRemoveInx = table.getSelectionIndex();
				qrtPCREntryList.remove(iRemoveInx);
				table.remove(iRemoveInx);
				Control oldEditor = editor.getEditor();
				if (oldEditor != null) oldEditor.dispose();
				listEntries.clear();
				for (int i = 0; i < table.getItemCount(); i++) {
					String displayName = table.getItem(i).getText(0).trim();
					String alias = table.getItem(i).getText(1).trim();
					listEntries.put(displayName, alias);
				}

				// if list is empty should disable OKbutton
				if (listEntries.isEmpty()) {
					setPageComplete(false);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		new Label(parent, SWT.NONE);		
	}

	private void createList(Composite parent) {		
		TableViewer viewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
		table = viewer.getTable();
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true, 7, 1);
		data.heightHint = 200;
        table.setLayoutData(data);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        
        TableColumn tblclmnEntry = new TableColumn(table, SWT.NONE);
        tblclmnEntry.setText("QrtPCR Entry");
        tblclmnEntry.setWidth(150);
        
        TableColumn tblclmnAlias = new TableColumn(table, SWT.NONE);
        tblclmnAlias.setText("Alias (User defined)");
        tblclmnAlias.setWidth(200);
        
        setListData();
        
        editor = new TableEditor(table);
		editor.grabHorizontal = true;
		editor.horizontalAlignment = SWT.LEFT;
		editor.minimumWidth = 200;
		
		// update list
		table.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event e) {
				if (isReadyToFinish()) {
					setPageComplete(true);
				} else {
					setPageComplete(false);
				}
			}
		});
        
        table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Clean up any previous editor control
				Control oldEditor = editor.getEditor();
				if (oldEditor != null) oldEditor.dispose();
		
				// Identify the selected row
				final TableItem item = (TableItem)e.item;
				if (item == null) return;
		
				// The control that will be the editor must be a child of the Table
				Text newEditor = new Text(table, SWT.NONE);
				newEditor.setText(item.getText(1));
				newEditor.addModifyListener(new ModifyListener() {
					@Override
					public void modifyText(ModifyEvent me) {
						Text text = (Text)editor.getEditor();
						editor.getItem().setText(1, text.getText());
						listEntries.put( item.getText(0), text.getText() );
						if (isReadyToFinish()) {
							setPageComplete(true);
						} else {
							setPageComplete(false);
						}
					}
				});
				
				newEditor.selectAll();
				newEditor.setFocus();
				editor.setEditor(newEditor, item, 1);
			}
		});
	}

	private void createListHeader(Composite parent) {
		/*
		 * third row starts:List
		 */
		GridData listLabelData = new GridData();
		listLabel = new Label(parent, SWT.NONE);
		listLabelData.grabExcessHorizontalSpace = true;
		listLabelData.horizontalSpan = 7;
		listLabel.setText("QrtPCR Experiments");
		listLabel.setLayoutData(listLabelData);
		listLabel = setMandatoryLabel(listLabel);	
		
	}

	private void createEmptyLine(Composite parent) {
		GridData dummy = new GridData();
		Label dummyLbl = new Label(parent, SWT.NONE);
		dummy.grabExcessHorizontalSpace = true;
		dummy.horizontalSpan = 7;
		dummyLbl.setLayoutData(dummy);
	}

	private void createLineSeparator(Composite parent) {
		GridData dummy = new GridData(GridData.FILL_HORIZONTAL);
		Label separator = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);	    
		dummy.grabExcessHorizontalSpace = true;
		dummy.horizontalSpan = 7;
		separator.setLayoutData(dummy);
	}

	private void createDescription(Composite parent) {
		/*
		 * Second row starts:description with minimum size 80;
		 */
		GridData descriptionData = new GridData();
		descriptionData.grabExcessHorizontalSpace = false;
		descriptionLabel = new Label(parent, SWT.NONE);
		descriptionLabel.setText("Description");
		descriptionLabel.setLayoutData(descriptionData);

		GridData descriptionTextData = new GridData(GridData.FILL_BOTH);
		descriptionTextData.minimumHeight = 80;
		descriptionTextData.grabExcessHorizontalSpace = true;
		descriptionTextData.horizontalSpan = 6;
		descriptionText = new Text(parent, SWT.MULTI | SWT.V_SCROLL
				| SWT.BORDER);
		descriptionText.setLayoutData(descriptionTextData);
		descriptionText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (isReadyToFinish()) {
					setPageComplete(true);
				} else {
					setPageComplete(false);
				}
			}
		});
		descriptionText.addTraverseListener(ListenerFactory
				.getTabTraverseListener());
		descriptionText.addKeyListener(ListenerFactory.getCTRLAListener());		
	}

	private void createDisplayName(Composite parent) {
		/*
		 * First row starts:name
		 */
		GridData nameData = new GridData();
		nameData.grabExcessHorizontalSpace = false;
		nameLabel = new Label(parent, SWT.NONE);
		nameLabel.setText("Display Name");
		nameLabel = setMandatoryLabel(nameLabel);
		nameLabel.setLayoutData(nameData);

		GridData nameTextData = new GridData(GridData.FILL_HORIZONTAL);
		nameTextData.grabExcessHorizontalSpace = true;
		nameTextData.horizontalSpan = 6;
		nameText = new Text(parent, SWT.BORDER);
		nameText.setLayoutData(nameTextData);
		nameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (isReadyToFinish()) {
					setPageComplete(true);
				} else {
					setPageComplete(false);
				}
			}
		});
	}
	
	private void setListData() {
		for (int i = 0; i < qrtPCREntryList.size(); i++) {
			Entry entry = qrtPCREntryList.get(i);
			String displayName =entry.getDisplayName();

			listEntries.put(displayName, displayName);
			TableItem item = new TableItem(table, SWT.NONE);

			item.setText(0, displayName);

			item.setBackground(0, Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
			item.setText(1, displayName);
		}		
	}
	
	private boolean isReadyToFinish() {
		// check if list is not empty and interval is not empty and name is not
		// empty.
		if ( !nameText.getText().isEmpty()
				&& table.getItemCount() != 0) {
			// check if name is not too long and description is not too long
			if (!checkBasicLengthCheck(nameLabel, nameText, 0, 32)) {
				return false;
			} else {
				// if not then look for same name report
				if (this.qrtPCREntryList != null
						&& this.qrtPCREntryList.size() != 0) {
					for (Entry child : DataModelSearch.findParentByType(
							this.qrtPCREntryList.get(0), ProjectProperty.TYPE)
							.getChildren()) {
						if (child.getProperty().getType()
								.equals(QrtPCRMergeProperty.TYPE)) {
							// then look for the same name given by the user
							for (Entry child2 : child.getChildren()) {
								if (child2.getDisplayName().equals(
										nameText.getText())) {
									setError(nameLabel,
											"The name is in use. Please choose another name.");
									return false;
								}
							}
						}
					}
				}
			}
			if (!descriptionLabel.getText().isEmpty()) {
				// check if description is not empty, then should not go above
				// its limit
				if (!checkBasicLengthCheck(descriptionLabel, descriptionText,
						0, Integer.parseInt(PropertyHandler
								.getVariable("descriptionLength")))) {
					return false;
				}
			}
			
			List<String> alContains = new ArrayList<>();

			for (int i = 0; i < table.getItemCount(); i++) {
				String displayName = table.getItem(i).getText(0);
				String alias = table.getItem(i).getText(1);
				if( alias.equals("") ) {
					setErrorMessage("Alias for " + displayName + " cannot be blank.");
					return false;
				}
				if( alContains.contains(alias) ) {
					setErrorMessage("Duplicate aliases are not allowed.");
					return false;
				}
				alContains.add(alias);
			}
			setErrorMessage(null);
			return true;
		}
		return false;
	}

	protected void setPageComplete(boolean flag) {
		if (flag) {
			// then save inputs
			name = nameText.getText();
			description = descriptionText.getText();
		}
		okButton.setEnabled(flag);
		
	}

	private void initGrid(Composite parent) {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 7;
		gridLayout.verticalSpacing = 10;
		parent.setLayout(gridLayout);
	}

	@Override
	protected boolean isValidInput() {
		return true;
	}

	@Override
	public Entry createEntry() {
		Entry qrtPCRMergeReportEntry = new Entry();
		qrtPCRMergeReportEntry.setDisplayName(name);
		QrtPCRMergeProperty property = new QrtPCRMergeProperty();
		qrtPCRMergeReportEntry.setProperty(property);
		return qrtPCRMergeReportEntry;
	}

}
