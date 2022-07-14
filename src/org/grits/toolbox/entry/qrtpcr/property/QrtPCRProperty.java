package org.grits.toolbox.entry.qrtpcr.property;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.io.PropertyWriter;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.core.datamodel.property.PropertyDataFile;
import org.grits.toolbox.core.datamodel.util.DataModelSearch;
import org.grits.toolbox.core.utilShare.DeleteUtils;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.grits.toolbox.entry.qrtpcr.Activator;
import org.grits.toolbox.entry.qrtpcr.Config;
import org.grits.toolbox.entry.qrtpcr.ImageRegistry;
import org.grits.toolbox.entry.qrtpcr.ImageRegistry.QrtPCRImage;
import org.grits.toolbox.entry.qrtpcr.commands.CreateQrtPCRDataTable;

public class QrtPCRProperty extends Property{
	
	private static final Logger logger = Logger.getLogger(QrtPCRProperty.class);
	
	public static final String TYPE = "org.grits.toolbox.property.qrtpcr";
    protected static PropertyWriter writer = new QrtPCRPropertyWriter();
	protected static ImageDescriptor imageDescriptor = ImageRegistry.getImageDescriptor(QrtPCRImage.PLUGIN_ICON);
    

	@Override
	public String getType() {
		return QrtPCRProperty.TYPE;
	}

	@Override
	public PropertyWriter getWriter() {
		return QrtPCRProperty.writer;
	}

	@Override
	public ImageDescriptor getImage() {
		return QrtPCRProperty.imageDescriptor;
	}
	
	private static String getQrtPCRLocation (Entry entry) {
		String workspaceLocation = PropertyHandler.getVariable("workspace_location");
        String projectFolderLocation = workspaceLocation.substring(0, workspaceLocation.length()-1) 
                + File.separator
                + DataModelSearch.findParentByType(entry, ProjectProperty.TYPE).getDisplayName(); 
        String experimentGroupFolderLocation = projectFolderLocation
                + File.separator
                + Config.folderName;
       
		return experimentGroupFolderLocation;
	}

	@Override
	public void delete(Entry entry) throws IOException {
		
        String fileLocation = getQrtPCRLocation(entry) 
                + File.separator 
                + getFilename();
        try
        {
            DeleteUtils.delete(new File(fileLocation));
        } catch (IOException e)
        {
            ErrorUtils.createErrorMessageBox(Display.getCurrent().getActiveShell(), "Cannot delete qrtPCR data", e);
            logger.error(Activator.PLUGIN_ID + " Cannot delete qrtPCR data", e);
        }
		
	}

	@Override
	public Object clone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Property getParentProperty() {
		return null;
	}

	public String getFilename() {
		return getFile().getName();
	}
	
	public void setFilename(String filename) {
		this.dataFiles = new ArrayList<>();
		this.dataFiles.add(new PropertyDataFile(filename));
	}

	public PropertyDataFile getFile() {
		PropertyDataFile qrtpcrFile = null;
		for(PropertyDataFile dataFile : dataFiles)
		{
			if(PropertyDataFile.DEFAULT_TYPE.equals(dataFile.getType()))
			{
				qrtpcrFile = dataFile;
				break;
			}
		}
		return qrtpcrFile;
	}

	@Override
	public void makeACopy(Entry currentEntry, Entry destinationEntry)
			throws IOException {
		try
		{
			File currentQrtPCRFile = new File(QrtPCRProperty.getQrtPCRLocation(currentEntry), getFilename());
			if(currentQrtPCRFile.exists())
			{
				File destinationFolder = new File(
						QrtPCRProperty.getQrtPCRLocation(destinationEntry.getParent()));
				if(!destinationFolder.exists() || !destinationFolder.isDirectory()) 
				{
					destinationFolder.mkdir();
				}
				String qrtPCRFileName = CreateQrtPCRDataTable.generateFileName (destinationFolder.list());
				File destinationFile = new File(destinationFolder, qrtPCRFileName);
				
				Files.copy(currentQrtPCRFile.toPath(), destinationFile.toPath());
				List<PropertyDataFile> dataFiles = new ArrayList<PropertyDataFile>();
				PropertyDataFile currentDataFile = getFile();
				dataFiles.add(new PropertyDataFile(destinationFile.getName(), 
						currentDataFile.getVersion(), currentDataFile.getType()));
				QrtPCRProperty property = new QrtPCRProperty();
				property.setDataFiles(dataFiles);
				property.setRemoved(!exists());
				property.setVersion(getVersion());
				property.setViewerRank(getViewerRank());
				destinationEntry.setProperty(property);
			}
			else throw new FileNotFoundException("Could not find qrtPCR file for selected entry \"" 
					+ currentEntry.getDisplayName() + "\" in project \"" 
					+ DataModelSearch.findParentByType(currentEntry, ProjectProperty.TYPE).getDisplayName()
					+ "\"");
		} catch (FileNotFoundException ex)
		{
			throw ex;
		} catch (IOException ex)
		{
			throw new IOException("Error copying  qrtPCR information.\n" + ex.getMessage(), ex);
		}
	}

}
