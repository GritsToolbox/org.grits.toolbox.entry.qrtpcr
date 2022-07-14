package org.grits.toolbox.entry.qrtpcr.model.xml;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.Well;

public class PlateLayoutMapAdapter extends XmlAdapter<PlateLayoutMapEntry[], Map<Well, Gene>> {

	@Override
	public Map<Well, Gene> unmarshal(PlateLayoutMapEntry[] v) throws Exception {
		Map<Well, Gene> r = new HashMap<Well, Gene>();
        for (PlateLayoutMapEntry mapelement : v) {
            r.put( mapelement.well, mapelement.gene);
        }
        return r;
	}

	@Override
	public PlateLayoutMapEntry[] marshal(Map<Well, Gene> v) throws Exception {
		PlateLayoutMapEntry[] mapElements = new PlateLayoutMapEntry[v.size()];
		int i = 0;
        for (Map.Entry<Well, Gene> entry : v.entrySet()) {
            mapElements[i++] = new PlateLayoutMapEntry(entry.getKey(), entry.getValue());
        }
		
		return mapElements;
	}

}
