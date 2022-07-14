package org.grits.toolbox.entry.qrtpcr.model.xml;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.grits.toolbox.entry.qrtpcr.model.QrtPCRTable;

public class IntegerQrtPCRTableMapAdapter extends
		XmlAdapter<TableMapEntry[], Map<Integer, QrtPCRTable>> {

	@Override
	public Map<Integer, QrtPCRTable> unmarshal(TableMapEntry[] v)
			throws Exception {
		Map<Integer, QrtPCRTable> r = new HashMap<Integer, QrtPCRTable>();
        for (TableMapEntry mapelement : v)
            r.put(mapelement.runId, mapelement.table);
        return r;
	}

	@Override
	public TableMapEntry[] marshal(Map<Integer, QrtPCRTable> v)
			throws Exception {
		TableMapEntry[] mapElements = new TableMapEntry[v.size()];
        int i = 0;
        for (Map.Entry<Integer, QrtPCRTable> entry : v.entrySet())
            mapElements[i++] = new TableMapEntry(entry.getKey(), entry.getValue());

        return mapElements;
	}

}
