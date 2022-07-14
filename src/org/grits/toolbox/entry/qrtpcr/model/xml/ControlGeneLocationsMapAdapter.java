package org.grits.toolbox.entry.qrtpcr.model.xml;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.Well;

public class ControlGeneLocationsMapAdapter extends XmlAdapter<PlateLayoutMapEntry[], Map<Gene, Well>> {
	@Override
	public Map<Gene, Well> unmarshal(PlateLayoutMapEntry[] v) throws Exception {
		Map<Gene, Well> r = new HashMap<>();
        for (PlateLayoutMapEntry mapelement : v) {
            r.put(mapelement.gene,  mapelement.well);
        }
        return r;
	}

	@Override
	public PlateLayoutMapEntry[] marshal(Map<Gene, Well> v) throws Exception {
		PlateLayoutMapEntry[] mapElements = new PlateLayoutMapEntry[v.size()];
		int i = 0;
        for (Map.Entry<Gene, Well> entry : v.entrySet()) {
            mapElements[i++] = new PlateLayoutMapEntry(entry.getValue(), entry.getKey());
        }
		
		return mapElements;
	}

}
