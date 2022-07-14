package org.grits.toolbox.entry.qrtpcr.dialogs;

import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ListDialog;
import org.grits.toolbox.entry.qrtpcr.model.GeneList;
import org.grits.toolbox.entry.qrtpcr.model.PlateLayout;

public class CustomSelectionDialogWithDescription extends ListDialog {
	Object selection;
	Text name;
	Text description;
	
	Boolean sublist;
	List<?> input;
	
	public CustomSelectionDialogWithDescription(Shell parent, boolean sublist, Text name, List<?> input) {
		super(parent);
		this.sublist = sublist;
		this.name = name;
		this.input = input;
	}
	
	public Text getName() {
		return name;
	}
	
	public Text getDescription() {
		return description;
	}
	
	public Object getSelection() {
		return selection;
	}
	
	@Override
	protected void okPressed() {
		super.okPressed();
		Object[] result = getResult();
		for (int i = 0; i < result.length; i++) {
			selection = result[i];
			if (name != null) {
				if (selection instanceof GeneList)
					name.setText(((GeneList) selection).getListName());
				else if (selection instanceof PlateLayout)
					name.setText(((PlateLayout) selection).getName());
			}
		}
	}
	
	@Override
	protected Control createDialogArea(Composite container) {
		Composite parent =  (Composite)super.createDialogArea(container);
		// add a textarea to display the description of the selected protocols
		GridData descriptionTextData = new GridData();
		descriptionTextData.minimumHeight = 80;
		descriptionTextData.grabExcessHorizontalSpace = true;
		descriptionTextData.grabExcessVerticalSpace = true;
		descriptionTextData.horizontalAlignment = GridData.FILL;
		descriptionTextData.horizontalSpan = 3;
		description = new Text(parent, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
		description.setLayoutData(descriptionTextData);
		description.setEditable(false);
		
		this.getTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				Object object = event.getSelection();
				Object node = ((StructuredSelection)object).getFirstElement();
				selection = node;
				if (node instanceof GeneList) {
					if (((GeneList)node).getDescription() != null)
						description.setText(((GeneList)node).getDescription());
					else description.setText("");
					
				} else if (node instanceof PlateLayout) {
					if (((PlateLayout) node).getDescription() != null)
						description.setText(((PlateLayout) node).getDescription());
					else
						description.setText("");
				}
				
			}
		});
		
		return parent;
	}
	
	public void initializeGeneListSelectionDialog() {
		this.setContentProvider(new ArrayContentProvider());
		this.setTitle("Selection");
		this.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof GeneList)
					return ((GeneList) element).getListName();
				else if (element instanceof PlateLayout)
					return ((PlateLayout) element).getName();
				return null;
			}
		});
		
		this.setInput(input);
	}
	
}
