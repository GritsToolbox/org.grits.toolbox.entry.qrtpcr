package org.grits.toolbox.entry.qrtpcr.property;

import java.io.IOException;

import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.datamodel.io.PropertyReader;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.entry.qrtpcr.property.versions.MergePropertyReaderVersion0;
import org.grits.toolbox.entry.qrtpcr.property.versions.MergePropertyReaderVersion1;
import org.jdom.Element;

public class QrtPCRMergePropertyReader extends PropertyReader {

	@Override
	public Property read(Element propertyElement) throws IOException, UnsupportedVersionException {
		QrtPCRMergeProperty property = new QrtPCRMergeProperty();

		PropertyReader.addGenericInfo(propertyElement, property);

		if(property.getVersion() == null)
		{
			return MergePropertyReaderVersion0.read(propertyElement, property);
		}
		else if(property.getVersion().equals("1.0"))
		{
			return MergePropertyReaderVersion1.read(propertyElement, property);
		}
		else 
			throw new UnsupportedVersionException("This version is currently not supported.",
					property.getVersion());
	}

}
