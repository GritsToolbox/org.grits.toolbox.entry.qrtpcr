package org.grits.toolbox.entry.qrtpcr.preference;

import javax.xml.bind.annotation.XmlRootElement;

import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.preference.share.PreferenceEntity;
import org.grits.toolbox.core.preference.share.PreferenceReader;
import org.grits.toolbox.core.preference.share.PreferenceWriter;
import org.grits.toolbox.display.control.table.datamodel.GRITSColumnHeader;
import org.grits.toolbox.entry.qrtpcr.Config;

@XmlRootElement(name="qrtPCRGeneListPreference")
public class QrtPCRGeneListTablePreference extends QrtPCRPreference {
	private static final String GENELIST_PREFERENCE_NAME = "org.grits.toolbox.display.control.table.preference.QrtPCRGeneListPreference";
	private static final String CURRENT_VERSION = "1.0";

	protected String getCurrentVersion() {
		return CURRENT_VERSION;
	}
	
	public static String getPreferenceID() {
		return GENELIST_PREFERENCE_NAME;
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
		
		if ( _sKey.equals(Config.ID ) ) {
			header = new GRITSColumnHeader(Config.ID, Config.ID);
			return header;
		}
		if ( _sKey.equals(Config.GENEID)) {
			header = new GRITSColumnHeader(Config.GENEID, Config.GENEID);
			return header;
		}
		if ( _sKey.equals(Config.GENESYMBOL) ) {
			header = new GRITSColumnHeader(Config.GENESYMBOL, Config.GENESYMBOL);
			return header;
		}
		if ( _sKey.equals(Config.NAME ) ) {
			return new GRITSColumnHeader(Config.NAME, Config.NAME);
		}
		if ( _sKey.equals(Config.DESCRIPTION ) ) {
			return new GRITSColumnHeader(Config.DESCRIPTION, Config.DESCRIPTION);
		}
		if ( _sKey.equals(Config.GROUP ) ) {
			header = new GRITSColumnHeader(Config.GROUP, Config.GROUP);
			return header;
		}
		if ( _sKey.equals(Config.ALIASES ) ) {
			header =new GRITSColumnHeader(Config.ALIASES, Config.ALIASES);
			return header;
		}
		if ( _sKey.equals(Config.FWPRIMER ) ) {
			return new GRITSColumnHeader(Config.FWPRIMER, Config.FWPRIMER);
		}
		if ( _sKey.equals(Config.REVPRIMER ) ) {
			return new GRITSColumnHeader(Config.REVPRIMER, Config.REVPRIMER);
		}
		if ( _sKey.equals(Config.REFSEQ ) ) {
			return new GRITSColumnHeader(Config.REFSEQ, Config.REFSEQ);
		}
		if ( _sKey.equals(Config.SECONDARYREFSEQ ) ) {
			return new GRITSColumnHeader(Config.SECONDARYREFSEQ, Config.SECONDARYREFSEQ);
		}
		if ( _sKey.equals(Config.DESIGNEDFOR ) ) {
			return new GRITSColumnHeader(Config.DESIGNEDFOR, Config.DESIGNEDFOR);
		}
		if ( _sKey.equals(Config.LOCATION ) ) {
			return new GRITSColumnHeader(Config.LOCATION, Config.LOCATION);
		}
		if ( _sKey.equals(Config.CONTROL ) ) {
			return new GRITSColumnHeader(Config.CONTROL, Config.CONTROL);
		}
		return super.getColumnHeader(_sKey);
	}
	
	public static PreferenceEntity getPreferenceEntity() throws UnsupportedVersionException {
		PreferenceEntity preferenceEntity = PreferenceReader.getPreferenceByName(GENELIST_PREFERENCE_NAME);
		return preferenceEntity;
	}
}
