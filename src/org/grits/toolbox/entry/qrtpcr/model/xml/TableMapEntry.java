package org.grits.toolbox.entry.qrtpcr.model.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.grits.toolbox.entry.qrtpcr.model.QrtPCRTable;

public class TableMapEntry {

	@XmlAttribute Integer runId;
	@XmlElement QrtPCRTable table;
	
	@SuppressWarnings("unused")
	private TableMapEntry() {} //Required by JAXB

	public TableMapEntry(Integer runId, QrtPCRTable table)
	{
	    this.runId   = runId;
	    this.table = table;
	}
}
