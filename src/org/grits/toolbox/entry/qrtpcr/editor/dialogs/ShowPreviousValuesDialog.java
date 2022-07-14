package org.grits.toolbox.entry.qrtpcr.editor.dialogs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.entry.qrtpcr.model.CtHistory;

public class ShowPreviousValuesDialog extends Dialog {
	protected static final Double NULLVALUE = 50.0;
	Point location;
	Map<Integer, List<CtHistory>> previousValuesMap;
	private Text runIdText;
	private TableViewer tableViewer;

	public ShowPreviousValuesDialog(Shell parentShell, Point curLoc, HashMap<Integer, List<CtHistory>> previousValuesMap2) {
		super(parentShell);
		this.location = curLoc;
		this.previousValuesMap = previousValuesMap2;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite comp = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(comp, SWT.NONE);
        container.getShell().setText("Previous Values");
        GridData containerData = new GridData(SWT.FILL, SWT.FILL, false, false);
        containerData.widthHint = 200;
        container.setLayoutData(containerData);
        GridLayout layout = new GridLayout(1, false);
        container.setLayout(layout);
        
        for (Iterator<Integer> iterator = previousValuesMap.keySet().iterator(); iterator.hasNext();) {
			Integer runId = (Integer) iterator.next();
			
			Label label = new Label(container, SWT.NONE);
	        label.setText("Run Id");
	        label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

	        runIdText = new Text(container, SWT.READ_ONLY|SWT.FILL|SWT.BORDER);
	        GridData searchFieldTextGridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
	        searchFieldTextGridData.grabExcessHorizontalSpace = true;
	        runIdText.setLayoutData(searchFieldTextGridData);
	        runIdText.setText(runId +"");

	        Label label2 = new Label(container, SWT.NONE);
	        label2.setText("All Values");
	        label2.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

	        tableViewer = new TableViewer(container, SWT.BORDER|SWT.H_SCROLL | SWT.V_SCROLL);
	        GridData tableViewertGridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
	        tableViewertGridData.grabExcessHorizontalSpace = true;
	        tableViewertGridData.heightHint = 50;
	        tableViewer.getTable().setLayoutData(tableViewertGridData);
	        tableViewer.getTable().setHeaderVisible(true);
	        tableViewer.getTable().setLinesVisible(true);
	        
	        TableViewerColumn valueColumn = new TableViewerColumn(tableViewer, SWT.NONE);
	        valueColumn.getColumn().setText("Value");
	        valueColumn.getColumn().setWidth(50);
	        valueColumn.setLabelProvider(new ColumnLabelProvider() {
	        	@Override
	        	public String getText(Object element) {
	        		if (element instanceof CtHistory) {
						if (((CtHistory) element).getCt() == null)
							return "null";
						return ((CtHistory) element).getCt().toString();
	        		}
	        		return "";
	        	}
	        });
	        
	        TableViewerColumn reasonColumn = new TableViewerColumn(tableViewer, SWT.NONE);
	        reasonColumn.getColumn().setText("Reason");
	        reasonColumn.getColumn().setWidth(100);
	        reasonColumn.setLabelProvider(new ColumnLabelProvider() {
	        	@Override
	        	public String getText(Object element) {
	        		if (element instanceof CtHistory) {
						return ((CtHistory) element).getReasonCode();
	        		}
	        		return "";
	        	}
	        });
	        
	        tableViewer.setContentProvider(new ArrayContentProvider() {
	        	@SuppressWarnings("unchecked")
				@Override
	        	public Object[] getElements(Object inputElement) {
	        		if (inputElement instanceof List) {
	        			if (inputElement != null) {
	        				CtHistory[] contents = new CtHistory[((List<CtHistory>) inputElement).size()];
	        				int i=0;
	        				for (CtHistory value : (List<CtHistory>)inputElement) {
								contents[i++] = value;
							}
	        				
	        				return contents;
	        			}		
	        		}
	        		return super.getElements(inputElement);
	        	}
	        });
	        tableViewer.setInput(previousValuesMap.get(runId));
		}
        
        return comp;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// do not add any buttons
	}
	
	@Override
	protected Point getInitialLocation(Point initialSize) {
		return location;
	}
	
/*	@Override
	protected Point getInitialSize() {
		return new Point(50, 100);
	}*/

}
