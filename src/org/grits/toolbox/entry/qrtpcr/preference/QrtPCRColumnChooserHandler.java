package org.grits.toolbox.entry.qrtpcr.preference;

import org.grits.toolbox.display.control.table.command.GRITSTableDisplayColumnChooserCommand;
import org.grits.toolbox.display.control.table.command.GRITSTableDisplayColumnChooserCommandHandler;
import org.grits.toolbox.display.control.table.tablecore.GRITSTable;
import org.grits.toolbox.display.control.table.tablecore.IGritsTable;
import org.grits.toolbox.entry.qrtpcr.table.QrtPCRNatTable;

public class QrtPCRColumnChooserHandler extends GRITSTableDisplayColumnChooserCommandHandler {

	public QrtPCRColumnChooserHandler(boolean sortAvalableColumns, IGritsTable parent) {
		super(sortAvalableColumns, parent);
	}
	
	public QrtPCRColumnChooserHandler(QrtPCRNatTable natTable) {
		this(false, natTable);
	}

	@Override
	public boolean doCommand(GRITSTableDisplayColumnChooserCommand command) {
		boolean bRes = super.doCommand(command);
		boolean bReset = true;
		if( bRes ) {
			bReset = !updatePreferences();
		} 		
		if( bReset ){
			resetPreferences();
		}
		this.gritsTable.performAutoResize();			
		return true;

	}
	
	protected boolean updatePreferences() {
		if (this.gritsTable instanceof QrtPCRNatTable)
		if ( ( (QrtPCRNatTable) this.gritsTable).getTablePreference() == null || 
				! GRITSTable.updatePreferencesFromColumnChooser.getUpdate() )
			return false;
		( (QrtPCRNatTable) this.gritsTable).updatePreferenceSettingsFromCurrentView();
		
		( (QrtPCRNatTable) this.gritsTable).getTablePreference().writePreference();	
		return true;
	}

	protected void resetPreferences() {
		this.gritsTable.updateViewFromPreferenceSettings();
	}
	
	
	@Override
	public Class<GRITSTableDisplayColumnChooserCommand> getCommandClass() {
		return GRITSTableDisplayColumnChooserCommand.class;
	}

}
