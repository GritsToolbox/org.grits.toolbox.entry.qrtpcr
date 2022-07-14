package org.grits.toolbox.entry.qrtpcr.model.xml;

import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.grits.toolbox.entry.qrtpcr.model.QrtPCRData;

public class MapAdapter extends XmlAdapter<MapEntry[], Map<String, QrtPCRData>> {
		 
	@Override
	public Map<String, QrtPCRData> unmarshal(MapEntry[] in) throws Exception {
		Map<String, QrtPCRData> r = new TreeMap<String, QrtPCRData>();
        for (MapEntry mapelement : in)
            r.put(mapelement.plateId, mapelement.qrtPCRData);
        return r;
	}
 
	@Override
	public MapEntry[] marshal(Map<String, QrtPCRData> map) throws Exception {
		MapEntry[] mapElements = new MapEntry[map.size()];
        int i = 0;
        for (Map.Entry<String, QrtPCRData> entry : map.entrySet())
            mapElements[i++] = new MapEntry(entry.getKey(), entry.getValue());

        return mapElements;
	}
}
