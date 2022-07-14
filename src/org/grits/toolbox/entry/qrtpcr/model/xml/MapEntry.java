package org.grits.toolbox.entry.qrtpcr.model.xml;

import javax.xml.bind.annotation.XmlElement;

import org.grits.toolbox.entry.qrtpcr.model.QrtPCRData;


public class MapEntry {

	@XmlElement String plateId;
	@XmlElement QrtPCRData qrtPCRData;
	
	@SuppressWarnings("unused")
	private MapEntry() {} //Required by JAXB

	public MapEntry(String key, QrtPCRData value)
	{
	    this.plateId   = key;
	    this.qrtPCRData = value;
	}
}
