package org.grits.toolbox.entry.qrtpcr.model.xml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.merge.MergeData;

public class MergeDataMapAdapter extends XmlAdapter<MergeDataEntry[], Map<Gene, List<MergeData>>> {

	@Override
	public Map<Gene, List<MergeData>> unmarshal(MergeDataEntry[] v) throws Exception {
		Map<Gene, List<MergeData>> r = new HashMap<Gene, List<MergeData>>();
        for (MergeDataEntry mapelement : v)
            r.put(mapelement.gene, mapelement.mergeData);
        return r;
	}

	@Override
	public MergeDataEntry[] marshal(Map<Gene, List<MergeData>> map) throws Exception {
		MergeDataEntry[] mapElements = new MergeDataEntry[map.size()];
        int i = 0;
        for (Map.Entry<Gene, List<MergeData>> entry : map.entrySet())
            mapElements[i++] = new MergeDataEntry(entry.getKey(), entry.getValue());

        return mapElements;
	}

}
