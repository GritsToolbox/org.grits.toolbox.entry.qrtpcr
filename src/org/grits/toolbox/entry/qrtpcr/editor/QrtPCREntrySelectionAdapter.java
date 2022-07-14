package org.grits.toolbox.entry.qrtpcr.editor;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.dialog.ProjectExplorerDialog;
import org.grits.toolbox.core.utilShare.EntrySelectionAdapter;
import org.grits.toolbox.entry.qrtpcr.property.QrtPCRProperty;

public class QrtPCREntrySelectionAdapter extends EntrySelectionAdapter{

	private Table entryDialogTable;
	private Map<String, String> listEntries;
	private List<Entry> entries;

	public QrtPCREntrySelectionAdapter(String a_propertyType,
			String a_dialogTitle, String a_dialogMessage) {
		super(a_propertyType, a_dialogTitle, a_dialogMessage);
	}

	public void setList(Table entryDialogTable) {
		this.entryDialogTable = entryDialogTable;
	}

	public void setListEntries(Map<String, String> listEntries2) {
		this.listEntries = listEntries2;
	}

	public void setEntries(java.util.List<Entry> entryList) {
		this.entries = entryList;
	}
	
	@Override
	public void widgetSelected(SelectionEvent event) 
	{
		Shell newShell = new Shell(parent.getShell(),SWT.PRIMARY_MODAL | SWT.SHEET);
		ProjectExplorerDialog dlg = new ProjectExplorerDialog(newShell);
		// Set a qrtpcr entry as a filter
		dlg.addFilter(QrtPCRProperty.TYPE);
		// Change the title bar text
		dlg.setTitle("qRT-PCR entry selection");
		// Customizable message displayed in the dialog
		dlg.setMessage("Choose an entry to add");
		// Calling open() will open and run the dialog.
		if (dlg.open() == Window.OK) {
			Entry entry = dlg.getEntry();
			if (entry != null) {
				String displayName = entry.getDisplayName();
				if(listEntries.isEmpty())
				{
					addToList(entry,displayName);
				}
				else if(!this.listEntries.containsKey(displayName))
				{
					addToList(entry,displayName);
				}
			}
		}
	}

	private void addToList(Entry entry, String displayName) {
		//update the hashMap entry history
		this.listEntries.put(displayName, displayName);
		//update list
		
		TableItem item = new TableItem(this.entryDialogTable, SWT.NONE);
		item.setText(0, displayName);
		item.setText(1, displayName);
		this.entryDialogTable.setSelection(this.entryDialogTable.getItemCount()-1);
		this.entryDialogTable.notifyListeners(SWT.Modify, null);
		//update the entries
		this.entries.add(entry);
	}
}
