package org.grits.toolbox.entry.qrtpcr.property.versions;

import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.entry.qrtpcr.property.QrtPCRProperty;
import org.jdom.Element;

public class QrtPCRPropertyReaderVersion1 {

	public static Property read(Element propertyElement, QrtPCRProperty property) {
		return property;
	}

}
