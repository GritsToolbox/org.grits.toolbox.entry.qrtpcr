package org.grits.toolbox.entry.qrtpcr.preference;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class QrtPCRMasterTablePreferencePage extends QrtPCRTablePreferencePage {

	@Override
	protected void initNatTable(Composite container) {
		createNatContainer(container);
		natBridge = new QrtPCRMergePreference_NatBridge(new Composite(getShell(), SWT.NONE), false);
		natBridge.initializeComponents(false);
		addColumnChooserElements(natContainer);
	}
	
	@Override
	protected void updateColumnChooserElements(Composite container, boolean _bDefault) {
		if ( natBridge == null || _bDefault) {
			natBridge = new QrtPCRMergePreference_NatBridge(new Composite(getShell(), SWT.NONE), false);
			natBridge.initializeComponents(_bDefault);
		}
		super.updateColumnChooserElements(container, _bDefault);
	}

}
