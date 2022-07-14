package org.grits.toolbox.entry.qrtpcr.property;

import java.io.IOException;

import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.datamodel.io.PropertyReader;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.entry.qrtpcr.property.versions.QrtPCRPropertyReaderVersion0;
import org.grits.toolbox.entry.qrtpcr.property.versions.QrtPCRPropertyReaderVersion1;
import org.jdom.Element;

public class QrtPCRPropertyReader extends PropertyReader {

	@Override
	public Property read(Element propertyElement) throws IOException, UnsupportedVersionException {
		QrtPCRProperty property = new QrtPCRProperty();

        PropertyReader.addGenericInfo(propertyElement, property);

		if(property.getVersion() == null)
		{
			return QrtPCRPropertyReaderVersion0.read(propertyElement, property);
		}
		else if(property.getVersion().equals("1.0"))
		{
			return QrtPCRPropertyReaderVersion1.read(propertyElement, property);
		}
		else 
			throw new UnsupportedVersionException("This version is currently not supported.",
					property.getVersion());
	}

}
