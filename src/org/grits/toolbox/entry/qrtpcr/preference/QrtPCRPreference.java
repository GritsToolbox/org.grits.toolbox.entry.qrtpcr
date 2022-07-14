package org.grits.toolbox.entry.qrtpcr.preference;

import javax.xml.bind.annotation.XmlRootElement;

import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.preference.share.PreferenceEntity;
import org.grits.toolbox.core.preference.share.PreferenceReader;
import org.grits.toolbox.core.preference.share.PreferenceWriter;
import org.grits.toolbox.display.control.table.datamodel.GRITSColumnHeader;
import org.grits.toolbox.display.control.table.preference.TableViewerPreference;
import org.grits.toolbox.entry.qrtpcr.Config;

@XmlRootElement(name="qrtPCRPreference")
public class QrtPCRPreference extends TableViewerPreference{
	private static final String PREFERENCE_NAME_ALL = "org.grits.toolbox.display.control.table.preference.QrtPCRPreference";
	private static final String CURRENT_VERSION = "1.0";

	protected String getCurrentVersion() {
		return QrtPCRPreference.CURRENT_VERSION;
	}
	
	public static String getPreferenceID() {
		return PREFERENCE_NAME_ALL;
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
		
		if ( _sKey.equals(Config.WELL ) ) {
			header = new GRITSColumnHeader(Config.WELL, Config.WELL);
			return header;
		}
		if ( _sKey.equals(Config.GENEID)) {
			header = new GRITSColumnHeader(Config.GENEID, Config.GENEID);
			return header;
		}
		if ( _sKey.equals(Config.CT) ) {
			header = new GRITSColumnHeader(Config.CT, Config.CT);
			return header;
		}
		if ( _sKey.equals(Config.STDEV ) ) {
			return new GRITSColumnHeader(Config.STDEV, Config.STDEV);
		}
		if ( _sKey.equals(Config.CT2 ) ) {
			return new GRITSColumnHeader(Config.CT2, Config.CT2);
		}
		if ( _sKey.equals(Config.NORMVALUE ) ) {
			header = new GRITSColumnHeader(Config.NORMVALUE, Config.NORMVALUE);
			return header;
		}
		if ( _sKey.equals(Config.NORMALIZED ) ) {
			header =new GRITSColumnHeader(Config.NORMALIZED, Config.NORMALIZED);
			return header;
		}
		if ( _sKey.equals(Config.SCALER ) ) {
			return new GRITSColumnHeader(Config.SCALER, Config.SCALER);
		}
		if ( _sKey.equals(Config.ADJUSTED ) ) {
			return new GRITSColumnHeader(Config.ADJUSTED, Config.ADJUSTED);
		}
		if ( _sKey.equals(Config.AVERAGE ) ) {
			return new GRITSColumnHeader(Config.AVERAGE, Config.AVERAGE);
		}
		if ( _sKey.equals(Config.STDEVADJUSTED ) ) {
			return new GRITSColumnHeader(Config.STDEVADJUSTED, Config.STDEVADJUSTED);
		}
		return super.getColumnHeader(_sKey);
	}
	
	public static PreferenceEntity getPreferenceEntity() throws UnsupportedVersionException {
		PreferenceEntity preferenceEntity = PreferenceReader.getPreferenceByName(PREFERENCE_NAME_ALL);
		return preferenceEntity;
	}

}
