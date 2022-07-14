package org.grits.toolbox.entry.qrtpcr.preference;

import org.apache.log4j.Logger;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.preference.share.PreferenceEntity;
import org.grits.toolbox.display.control.table.preference.TableViewerPreference;

public class QrtPCRMasterTablePreferenceLoader {
	private static Logger logger = Logger.getLogger(QrtPCRMasterTablePreferenceLoader.class);

	public static TableViewerPreference getTableViewerPreference() {
		QrtPCRPreference preferences = null;
		try {
			PreferenceEntity preferenceEntity = QrtPCRMasterTablePreference.getPreferenceEntity(); 
			
			preferences = (QrtPCRMasterTablePreference) TableViewerPreference.getTableViewerPreference(preferenceEntity, QrtPCRMasterTablePreference.class);
		} catch (UnsupportedVersionException ex) {
			logger.error(ex.getMessage(), ex);
			
		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}		
		if( preferences == null ) { // well, either no preferences yet or some error. initialize to defaults and return
			preferences = new QrtPCRMasterTablePreference();
			preferences.setColumnSettings("");
		}
		return preferences;
	}
}
