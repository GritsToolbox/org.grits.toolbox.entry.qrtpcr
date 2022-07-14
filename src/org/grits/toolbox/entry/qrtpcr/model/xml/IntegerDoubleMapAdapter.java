package org.grits.toolbox.entry.qrtpcr.model.xml;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class IntegerDoubleMapAdapter extends XmlAdapter<NormValueMapEntry[], Map<Integer, Double>> {

	@Override
	public Map<Integer, Double> unmarshal(NormValueMapEntry[] v)
			throws Exception {
		Map<Integer, Double> r = new HashMap<Integer, Double>();
        for (NormValueMapEntry mapelement : v)
            r.put(mapelement.runId, mapelement.normValue);
        return r;
	}

	@Override
	public NormValueMapEntry[] marshal(Map<Integer, Double> v) throws Exception {
		if (v == null)
			return null;
		NormValueMapEntry[] mapElements = new NormValueMapEntry[v.size()];
        int i = 0;
        for (Map.Entry<Integer, Double> entry : v.entrySet())
            mapElements[i++] = new NormValueMapEntry(entry.getKey(), entry.getValue());

        return mapElements;
	}

}
