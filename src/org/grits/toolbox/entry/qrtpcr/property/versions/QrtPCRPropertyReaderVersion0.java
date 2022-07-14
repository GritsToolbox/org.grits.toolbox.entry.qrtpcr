package org.grits.toolbox.entry.qrtpcr.property.versions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.datamodel.io.PropertyReader;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.core.datamodel.property.PropertyDataFile;
import org.grits.toolbox.entry.qrtpcr.model.QrtPCRRun;
import org.grits.toolbox.entry.qrtpcr.property.QrtPCRProperty;
import org.jdom.Attribute;
import org.jdom.Element;

public class QrtPCRPropertyReaderVersion0 {

	public static Property read(Element propertyElement, QrtPCRProperty property) throws IOException, UnsupportedVersionException {
		Element qrtPCRElement = propertyElement.getChild("qrtpcr");
	    if ( qrtPCRElement == null )
	           throw new IOException("QrtPCR property misses element");
	    
	    Attribute fileNameElement = qrtPCRElement.getAttribute("filename");
	    if(fileNameElement != null)
	    {
	    	List<PropertyDataFile> dataFiles = new ArrayList<PropertyDataFile>();
			PropertyDataFile dataFile = new PropertyDataFile(fileNameElement.getValue(), 
					QrtPCRRun.CURRENT_VERSION, 
					PropertyDataFile.DEFAULT_TYPE);
	
			dataFiles.add(dataFile);
			property.setDataFiles(dataFiles);
	        PropertyReader.UPDATE_PROJECT_XML = true;
	        return property;
	    }
	    else
	    {
	    	throw new UnsupportedVersionException("QrtPCR property misses filename attribute. This version is not supported", "older than grits 1.0");
	    }
	}
}
