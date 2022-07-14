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
import org.grits.toolbox.core.datamodel.property.ReportsProperty;
import org.grits.toolbox.core.datamodel.util.DataModelSearch;
import org.grits.toolbox.core.utilShare.DeleteUtils;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.grits.toolbox.entry.qrtpcr.Activator;
import org.grits.toolbox.entry.qrtpcr.Config;
import org.grits.toolbox.entry.qrtpcr.ImageRegistry;
import org.grits.toolbox.entry.qrtpcr.ImageRegistry.QrtPCRImage;
import org.grits.toolbox.entry.qrtpcr.commands.CreateMergeReport;

public class QrtPCRMergeProperty extends Property {
	
	private static final Logger logger = Logger.getLogger(QrtPCRMergeProperty.class);
	
	public static final String TYPE = "org.grits.toolbox.property.qrtpcr.merge";

	protected static PropertyWriter writer = new QrtPCRPropertyWriter();
	protected static ImageDescriptor imageDescriptor = ImageRegistry.getImageDescriptor(QrtPCRImage.PLUGIN_ICON);

	@Override
	public String getType() {
		return QrtPCRMergeProperty.TYPE;
	}

	@Override
	public PropertyWriter getWriter() {
		return QrtPCRMergeProperty.writer;
	}

	@Override
	public ImageDescriptor getImage() {
		return QrtPCRMergeProperty.imageDescriptor;
	}
	
	private static String getMergeQrtPCRLocaton (Entry entry) {
		String workspaceLocation = PropertyHandler.getVariable("workspace_location");
        String projectFolderLocation = workspaceLocation.substring(0, workspaceLocation.length()-1) 
                + File.separator
                + DataModelSearch.findParentByType(entry, ProjectProperty.TYPE).getDisplayName(); 
        String reportsfolder = projectFolderLocation + File.separator + ReportsProperty.getFolder();
        String qrtPCRFolderLocation = reportsfolder
                + File.separator
                + Config.folderName;
        return qrtPCRFolderLocation;
	}

	@Override
	public void delete(Entry entry) throws IOException {
		
        String fileLocation = getMergeQrtPCRLocaton(entry) 
                + File.separator 
                + getFilename();
        try
        {
            DeleteUtils.delete(new File(fileLocation));
        } catch (IOException e)
        {
            ErrorUtils.createErrorMessageBox(Display.getCurrent().getActiveShell(), "Cannot delete qrtPCR merge report", e);
            logger.error(Activator.PLUGIN_ID + " Cannot delete qrtPCR merge report", e);
        }
		
	}

	@Override
	public Object clone() {
		return null;
	}

	@Override
	public Property getParentProperty() {
		return null;
	}

	public String getFilename() {
		return getFile().getName();
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
			File currentQrtPCRFile = new File(QrtPCRMergeProperty.getMergeQrtPCRLocaton(currentEntry), getFilename());
			if(currentQrtPCRFile.exists())
			{
				File destinationFolder = new File(
						QrtPCRMergeProperty.getMergeQrtPCRLocaton(destinationEntry.getParent()));
				if(!destinationFolder.exists() || !destinationFolder.isDirectory()) 
				{
					destinationFolder.mkdir();
				}
				String qrtPCRFileName = CreateMergeReport.generateFileName (destinationFolder.list());
				File destinationFile = new File(destinationFolder, qrtPCRFileName);
				
				Files.copy(currentQrtPCRFile.toPath(), destinationFile.toPath());
				List<PropertyDataFile> dataFiles = new ArrayList<PropertyDataFile>();
				PropertyDataFile currentDataFile = getFile();
				dataFiles.add(new PropertyDataFile(destinationFile.getName(), 
						currentDataFile.getVersion(), currentDataFile.getType()));
				QrtPCRMergeProperty property = new QrtPCRMergeProperty();
				property.setDataFiles(dataFiles);
				property.setRemoved(!exists());
				property.setVersion(getVersion());
				property.setViewerRank(getViewerRank());
				destinationEntry.setProperty(property);
			}
			else throw new FileNotFoundException("Could not find merged qrtPCR file for selected merge report \"" 
					+ currentEntry.getDisplayName() + "\" in project \"" 
					+ DataModelSearch.findParentByType(currentEntry, ProjectProperty.TYPE).getDisplayName()
					+ "\"");
		} catch (FileNotFoundException ex)
		{
			throw ex;
		} catch (IOException ex)
		{
			throw new IOException("Error copying merged qrtPCR information.\n" + ex.getMessage(), ex);
		}
		
	}

	public void setFilename(String qrtPCRMergeFileName) {
		this.dataFiles = new ArrayList<>();
		this.dataFiles.add(new PropertyDataFile(qrtPCRMergeFileName));
	}

}
