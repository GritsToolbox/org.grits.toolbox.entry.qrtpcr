package org.grits.toolbox.entry.qrtpcr.model.xml;

import javax.xml.bind.annotation.XmlElement;

import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.Well;

public class PlateLayoutMapEntry {
	
	@XmlElement Gene gene;
	@XmlElement Well well;
	
	public PlateLayoutMapEntry() {
	}
	
	public PlateLayoutMapEntry ( Well w, Gene g) {
		this.gene = g;
		this.well = w;
	}

}
