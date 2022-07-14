package org.grits.toolbox.entry.qrtpcr.property.versions;

import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.entry.qrtpcr.property.QrtPCRMergeProperty;
import org.jdom.Element;

public class MergePropertyReaderVersion1 {

	public static Property read(Element propertyElement,
			QrtPCRMergeProperty property) {
		return property;
	}

}
