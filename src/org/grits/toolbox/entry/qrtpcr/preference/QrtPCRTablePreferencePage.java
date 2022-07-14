package org.grits.toolbox.entry.qrtpcr.preference;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.grits.toolbox.display.control.table.dialog.GRITSTableColumnChooser;

@XmlRootElement(name="qrtPCRTablePreference")
public class QrtPCRTablePreferencePage extends PreferencePage {

	public static List<String> HIDDEN_COLUMNS = new ArrayList<String>();
	
	private boolean bNeedPaint = true;
	
	protected GRITSTableColumnChooser chooser = null;
	protected Group natContainer = null;
	protected QrtPCRPreference_NatBridge natBridge = null;	
	
	@Override
	protected Control createContents(Composite parent) {
		initNatTable(parent);
		
		parent.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {
				if ( bNeedPaint ) {	
					updateColumnChooserElements(natContainer, false);
					bNeedPaint = false;
				}
			}
		});
		
		return parent;
	}
	
	protected void createNatContainer(Composite container) {
		natContainer = new Group(container, SWT.NONE);
		natContainer.setText("");
		GridData gridData1 = GridDataFactory.fillDefaults().grab(true, false).create();
		gridData1.horizontalSpan = 4;		
		natContainer.setLayoutData(gridData1);		
		GridLayout layout = new GridLayout(4, false);
		//		layout.marginWidth = 10;
		layout.horizontalSpacing = 25;
		layout.marginLeft = 10;
		layout.marginTop = 10;
		natContainer.setLayout( layout );
	}
	
	protected void initNatTable(Composite container) {
		createNatContainer(container);
		natBridge = new QrtPCRPreference_NatBridge(new Composite(getShell(), SWT.NONE));
		natBridge.initializeComponents(false);
		addColumnChooserElements(natContainer);
	}
	
	protected void addColumnChooserElements(Composite container) {
		if( chooser == null) {
			chooser = new GRITSTableColumnChooser(
					container.getShell(),
					false, true, natBridge.getNatTable());		
			chooser.getColumnChooserDialog().populateDialogArea(container);
			GridData layout = (GridData) container.getLayoutData();
			layout.horizontalSpan = 4;
			chooser.addListenersOnColumnChooserDialog();
		}
	}
	
	protected void updateColumnChooserElements(Composite container, boolean _bDefault) {
		if ( natBridge == null || _bDefault) {
			natBridge = new QrtPCRPreference_NatBridge(new Composite(getShell(), SWT.NONE));
			natBridge.initializeComponents(_bDefault);
		}
		if( chooser == null) {
			chooser = new GRITSTableColumnChooser(
					container.getShell(),
					false, true, natBridge.getNatTable());			
			chooser.getColumnChooserDialog().populateDialogArea(container);
			chooser.addListenersOnColumnChooserDialog();			
		} else {			
			chooser.getHiddenColumnEntries().clear();
			chooser.getColumnChooserDialog().removeAllLeaves();
			chooser.reInit(natBridge.getNatTable());
		}
		chooser.populateDialog();
		GridData layout = (GridData) container.getLayoutData();
		layout.horizontalSpan = 4;
	}

	@Override
	protected void performDefaults()
	{
		updateColumnChooserElements(natContainer, true);
	}
	
	@Override
	public boolean performOk()
	{
		return save();
	}

	private boolean save() {
		natBridge.updatePreferences();
		return true;
	}

}
