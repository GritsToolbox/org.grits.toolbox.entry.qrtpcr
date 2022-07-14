package org.grits.toolbox.entry.qrtpcr.preference;

import org.eclipse.swt.widgets.Composite;
import org.grits.toolbox.display.control.table.datamodel.GRITSColumnHeader;
import org.grits.toolbox.display.control.table.preference.TableViewerColumnSettings;
import org.grits.toolbox.entry.qrtpcr.Config;
import org.grits.toolbox.entry.qrtpcr.table.QrtPCRNatTable;
import org.grits.toolbox.entry.qrtpcr.table.merge.MergeNatTable;

public class QrtPCRMergePreference_NatBridge extends QrtPCRPreference_NatBridge {
	
	Boolean mergeTable = false;
	
	public QrtPCRMergePreference_NatBridge(Composite parent, Boolean mergeTable) {
		super(parent);
		if (mergeTable)
			this.natTable = new MergeNatTable(parent);
		else {
			this.natTable = new QrtPCRNatTable(parent);
			this.natTable.setMasterTable(true);
		}
	}
	
	@Override
	protected TableViewerColumnSettings getDefaultSettings() {
		TableViewerColumnSettings newSettings = new TableViewerColumnSettings();
		GRITSColumnHeader header = new GRITSColumnHeader(Config.GENEID, Config.GENEID);
		header.setIsGrouped(false);
		newSettings.addColumn(header);
		header = new GRITSColumnHeader(Config.GENESYMBOL, Config.GENESYMBOL);
		header.setIsGrouped(false);
		newSettings.addColumn(header);
		header = new GRITSColumnHeader(Config.AVERAGE, Config.AVERAGE);
		newSettings.addColumn(header);
		header = new GRITSColumnHeader(Config.STDEVADJUSTED, Config.STDEVADJUSTED);
		newSettings.addColumn(header);	
		return newSettings;
	}
	
	@Override
	protected QrtPCRPreference getCurrentTableViewerPreference() {
		if (mergeTable)
			return (QrtPCRPreference) QrtPCRMergeTablePreferenceLoader.getTableViewerPreference();
		else
			return (QrtPCRPreference) QrtPCRMasterTablePreferenceLoader.getTableViewerPreference();
	}
}
