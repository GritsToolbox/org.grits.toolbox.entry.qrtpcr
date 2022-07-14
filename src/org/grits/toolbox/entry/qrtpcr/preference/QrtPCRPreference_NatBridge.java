package org.grits.toolbox.entry.qrtpcr.preference;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Composite;
import org.grits.toolbox.display.control.table.datamodel.GRITSColumnHeader;
import org.grits.toolbox.display.control.table.preference.TableViewerColumnSettings;
import org.grits.toolbox.entry.qrtpcr.Config;
import org.grits.toolbox.entry.qrtpcr.editor.QrtPCRTableEditor;
import org.grits.toolbox.entry.qrtpcr.table.QrtPCRNatTable;

public class QrtPCRPreference_NatBridge {
	private static final Logger logger = Logger.getLogger(QrtPCRPreference_NatBridge.class);
	protected QrtPCRNatTable natTable = null;
	
	public QrtPCRPreference_NatBridge(Composite parent) {
		this.natTable = new QrtPCRNatTable(parent);
	}
	
	public void initializeComponents( boolean _bDefault ) {
		try {
			initializePreferences(_bDefault);
			List<GRITSColumnHeader> columns = new ArrayList<>();
			columns.addAll(natTable.getTablePreference().getPreferenceSettings().getHeaders());
			List<List<GRITSColumnHeader>> columnList = new ArrayList<>();
	        columnList.add(columns);
			natTable.setColumnList(columnList);
			natTable.initializeComponents();
		} catch( Exception e ) {
			logger.error("Unable to initialize QrtPCRPreference_NatBridge.", e);
		}		
	}
	
	protected void initializePreferences( boolean _bDefaults ) throws Exception {
		natTable.initializePreferences();		
		if ( _bDefaults || natTable.getTablePreference().getPreferenceSettings() == null ) {
			TableViewerColumnSettings tvcs = getDefaultSettings();
			natTable.getTablePreference().setPreferenceSettings(tvcs);
			natTable.getTablePreference().setColumnSettings(natTable.getTablePreference().toString());
		}
	}
	
	protected TableViewerColumnSettings getDefaultSettings() {
		TableViewerColumnSettings newSettings = new TableViewerColumnSettings();
		GRITSColumnHeader header = new GRITSColumnHeader(Config.WELL, Config.WELL);
		newSettings.addColumn(header);
		header = new GRITSColumnHeader(Config.GENEID, Config.GENEID);
		newSettings.addColumn(header);
		header = new GRITSColumnHeader(Config.CT, Config.CT);
		newSettings.addColumn(header);
		header = new GRITSColumnHeader(Config.STDEV, Config.STDEV);
		newSettings.addColumn(header);
		header = new GRITSColumnHeader(Config.CT2, Config.CT2);
		newSettings.addColumn(header);
		header = new GRITSColumnHeader(Config.NORMVALUE, Config.NORMVALUE);
		newSettings.addColumn(header);
		header = new GRITSColumnHeader(Config.NORMALIZED, Config.NORMALIZED);
		newSettings.addColumn(header);
		header = new GRITSColumnHeader(Config.SCALER, Config.SCALER);
		newSettings.addColumn(header);
		header = new GRITSColumnHeader(Config.ADJUSTED, Config.ADJUSTED);
		newSettings.addColumn(header);
		header = new GRITSColumnHeader(Config.AVERAGE, Config.AVERAGE);
		newSettings.addColumn(header);
		header = new GRITSColumnHeader(Config.STDEVADJUSTED, Config.STDEVADJUSTED);
		newSettings.addColumn(header);	
		return newSettings;
	}


	public QrtPCRNatTable getNatTable() {
		return natTable;
	}
	
	public void updatePreferences() {
		natTable.updatePreferenceSettingsFromCurrentView();
		natTable.getTablePreference().writePreference();
		// update will be triggered automatically through events
	//	QrtPCRPreference newPref = getCurrentTableViewerPreference();
	//	updateView(newPref);
	}
	
	protected QrtPCRPreference getCurrentTableViewerPreference() {
		return (QrtPCRPreference) QrtPCRPreferenceLoader.getTableViewerPreference();
	}
	
	/*protected void updateView( QrtPCRPreference newPref ) {
		QrtPCRTableEditor.updateColumnPreferencesForEditors( newPref );		
	}*/
}
