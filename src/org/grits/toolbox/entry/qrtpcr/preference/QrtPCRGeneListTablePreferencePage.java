package org.grits.toolbox.entry.qrtpcr.preference;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class QrtPCRGeneListTablePreferencePage  extends QrtPCRTablePreferencePage {
	@Override
	protected void initNatTable(Composite container) {
		createNatContainer(container);
		natBridge = new QrtPCRGeneListPreference_NatBridge(new Composite(getShell(), SWT.NONE));
		natBridge.initializeComponents(false);
		addColumnChooserElements(natContainer);
	}
	
	@Override
	protected void updateColumnChooserElements(Composite container, boolean _bDefault) {
		if ( natBridge == null || _bDefault) {
			natBridge = new QrtPCRGeneListPreference_NatBridge(new Composite(getShell(), SWT.NONE));
			natBridge.initializeComponents(_bDefault);
		}
		super.updateColumnChooserElements(container, _bDefault);
	}
}
