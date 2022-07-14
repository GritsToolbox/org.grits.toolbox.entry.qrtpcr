package org.grits.toolbox.entry.qrtpcr.property;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.grits.toolbox.core.datamodel.io.PropertyWriter;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.core.datamodel.property.PropertyDataFile;
import org.grits.toolbox.entry.qrtpcr.model.QrtPCRRun;
import org.grits.toolbox.entry.qrtpcr.model.merge.MergedQrtPCR;
import org.jdom.Attribute;
import org.jdom.Element;

public class QrtPCRPropertyWriter implements PropertyWriter{

	@Override
	public void write(Property a_property, Element a_propertyElement)
			throws IOException {
				
		Element experimentGroupElement = new Element("qrtpcr");
        a_propertyElement.addContent(experimentGroupElement);
		//add attributes
		if (a_property instanceof QrtPCRProperty) {
			QrtPCRProperty pp = (QrtPCRProperty)a_property;
			if(pp.getFile() != null && pp.getFile().getName() != null)
			{
				Element fileElement = new Element("file");
				List<Attribute> attributes = new ArrayList<Attribute>();
				attributes.add(new Attribute("name", pp.getFile().getName()));
				String version = pp.getFile().getVersion() == null ? 
						QrtPCRRun.CURRENT_VERSION : pp.getFile().getVersion();
				attributes.add(new Attribute("version", version));
				attributes.add(new Attribute("type", PropertyDataFile.DEFAULT_TYPE));
				fileElement.setAttributes(attributes);
				a_propertyElement.setContent(fileElement);
			}
			else {
				throw new IOException("Property could not be added as its file (or name) is null.");
			}
		} else if (a_property instanceof QrtPCRMergeProperty) {
			QrtPCRMergeProperty pp = (QrtPCRMergeProperty)a_property;
			if(pp.getFile() != null && pp.getFile().getName() != null)
			{
				Element fileElement = new Element("file");
				List<Attribute> attributes = new ArrayList<Attribute>();
				attributes.add(new Attribute("name", pp.getFile().getName()));
				String version = pp.getFile().getVersion() == null ? 
						MergedQrtPCR.CURRENT_VERSION : pp.getFile().getVersion();
				attributes.add(new Attribute("version", version));
				attributes.add(new Attribute("type", PropertyDataFile.DEFAULT_TYPE));
				fileElement.setAttributes(attributes);
				a_propertyElement.setContent(fileElement);
			}
			else {
				throw new IOException("Property could not be added as its file (or name) is null.");
			}
		}
		else 
			throw new IOException (" Property type is not supported");  
		
	}

}
