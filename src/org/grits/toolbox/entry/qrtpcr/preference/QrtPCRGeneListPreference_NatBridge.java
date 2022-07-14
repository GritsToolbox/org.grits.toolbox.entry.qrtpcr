package org.grits.toolbox.entry.qrtpcr.preference;

import org.eclipse.swt.widgets.Composite;
import org.grits.toolbox.display.control.table.datamodel.GRITSColumnHeader;
import org.grits.toolbox.display.control.table.preference.TableViewerColumnSettings;
import org.grits.toolbox.entry.qrtpcr.Config;
import org.grits.toolbox.entry.qrtpcr.table.manager.ManagerNatTable;

public class QrtPCRGeneListPreference_NatBridge extends QrtPCRPreference_NatBridge {

	public QrtPCRGeneListPreference_NatBridge(Composite parent) {
		super(parent);
		this.natTable = new ManagerNatTable(parent);
	}
	
	@Override
	protected TableViewerColumnSettings getDefaultSettings() {
		TableViewerColumnSettings newSettings = new TableViewerColumnSettings();
		
		GRITSColumnHeader header = new GRITSColumnHeader(Config.GENEID, Config.GENEID);
		newSettings.addColumn(header);
		header = new GRITSColumnHeader(Config.GENESYMBOL, Config.GENESYMBOL);
		newSettings.addColumn(header);
		header = new GRITSColumnHeader(Config.ID, Config.ID);
		newSettings.addColumn(header);
		header = new GRITSColumnHeader(Config.NAME, Config.NAME);
		newSettings.addColumn(header);
		header = new GRITSColumnHeader(Config.DESCRIPTION, Config.DESCRIPTION);
		newSettings.addColumn(header);
		header = new GRITSColumnHeader(Config.DESIGNEDFOR, Config.DESIGNEDFOR);
		newSettings.addColumn(header);
		header = new GRITSColumnHeader(Config.FWPRIMER, Config.FWPRIMER);
		newSettings.addColumn(header);
		header = new GRITSColumnHeader(Config.REVPRIMER, Config.REVPRIMER);
		newSettings.addColumn(header);
		header = new GRITSColumnHeader(Config.GROUP, Config.GROUP);
		newSettings.addColumn(header);
		header = new GRITSColumnHeader(Config.ALIASES, Config.ALIASES);
		newSettings.addColumn(header);
		header = new GRITSColumnHeader(Config.LOCATION, Config.LOCATION);
		newSettings.addColumn(header);
		header = new GRITSColumnHeader(Config.REFSEQ, Config.REFSEQ);
		newSettings.addColumn(header);
		header = new GRITSColumnHeader(Config.SECONDARYREFSEQ, Config.SECONDARYREFSEQ);
		newSettings.addColumn(header);
		header = new GRITSColumnHeader(Config.CONTROL, Config.CONTROL);
		newSettings.addColumn(header);
		return newSettings;
	}
	
	@Override
	protected QrtPCRPreference getCurrentTableViewerPreference() {
		return (QrtPCRPreference) QrtPCRGeneListTablePreferenceLoader.getTableViewerPreference();
	}

}
