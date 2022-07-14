package org.grits.toolbox.entry.qrtpcr.model.xml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.grits.toolbox.entry.qrtpcr.model.GeneData;


public class GeneDataMapAdapter extends XmlAdapter<GeneDataMapEntry[], Map<Integer, List<GeneData>>> {
	 
	@Override
	public Map<Integer, List<GeneData>> unmarshal(GeneDataMapEntry[] in) throws Exception {
		Map<Integer, List<GeneData>> r = new HashMap<Integer, List<GeneData>>();
        for (GeneDataMapEntry mapelement : in)
            r.put(mapelement.runId, mapelement.data);
        return r;
	}
 
	@Override
	public GeneDataMapEntry[] marshal(Map<Integer, List<GeneData>> map) throws Exception {
		if (map == null)
			return null;
		GeneDataMapEntry[] mapElements = new GeneDataMapEntry[map.size()];
        int i = 0;
        for (Map.Entry<Integer, List<GeneData>> entry : map.entrySet())
            mapElements[i++] = new GeneDataMapEntry(entry.getKey(), entry.getValue());

        return mapElements;
	}
}
