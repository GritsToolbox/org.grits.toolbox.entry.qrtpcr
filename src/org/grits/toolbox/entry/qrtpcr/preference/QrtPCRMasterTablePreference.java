package org.grits.toolbox.entry.qrtpcr.preference;

import javax.xml.bind.annotation.XmlRootElement;

import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.preference.share.PreferenceEntity;
import org.grits.toolbox.core.preference.share.PreferenceReader;
import org.grits.toolbox.core.preference.share.PreferenceWriter;
import org.grits.toolbox.display.control.table.datamodel.GRITSColumnHeader;
import org.grits.toolbox.entry.qrtpcr.Config;

@XmlRootElement(name="qrtPCRMasterPreference")
public class QrtPCRMasterTablePreference extends QrtPCRPreference{
	private static final String MASTER_PREFERENCE_NAME = "org.grits.toolbox.display.control.table.preference.QrtPCRMasterTablePreference";
	private static final String CURRENT_VERSION = "1.0";

	protected String getCurrentVersion() {
		return QrtPCRMasterTablePreference.CURRENT_VERSION;
	}
	
	public static String getPreferenceID() {
		return MASTER_PREFERENCE_NAME;
	}
	
	@Override
	public boolean writePreference() {
		PreferenceEntity preferenceEntity = new PreferenceEntity(getPreferenceID());
		preferenceEntity.setVersion(getCurrentVersion());
		preferenceEntity.setValue(marshalXML());
		return PreferenceWriter.savePreference(preferenceEntity);
	}
	
	@Override
	public GRITSColumnHeader getColumnHeader(String _sKey) {	
		GRITSColumnHeader header=null;
		
		if (_sKey.equals(Config.GENEID)) {
			header = new GRITSColumnHeader(Config.GENEID, Config.GENEID);
			header.setIsGrouped(false);
			return header;
		}
		else if (_sKey.equals(Config.GENESYMBOL)) {
			header = new GRITSColumnHeader(Config.GENESYMBOL, Config.GENESYMBOL);
			header.setIsGrouped(false);
			return header;
		} else if (_sKey.equals(Config.AVERAGE)) {
			return new GRITSColumnHeader(Config.AVERAGE, Config.AVERAGE);
		} else if (_sKey.equals(Config.STDEVADJUSTED)) {
			return new GRITSColumnHeader(Config.STDEVADJUSTED, Config.STDEVADJUSTED);
		}
		
		return header;
	}
	
	public static PreferenceEntity getPreferenceEntity( ) throws UnsupportedVersionException {
		PreferenceEntity preferenceEntity = PreferenceReader.getPreferenceByName(getPreferenceID());
		return preferenceEntity;
	}
}
