package org.grits.toolbox.entry.qrtpcr.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.grits.toolbox.core.dataShare.IGritsConstants;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.datamodel.util.DataModelSearch;
import org.grits.toolbox.entry.qrtpcr.Config;
import org.grits.toolbox.entry.qrtpcr.model.GeneList;
import org.grits.toolbox.entry.qrtpcr.model.PlateLayout;
import org.grits.toolbox.entry.qrtpcr.model.QrtPCRRun;

public class FileUtils {
	
	@Inject @Named (IGritsConstants.WORKSPACE_LOCATION)
	private static String workspaceLocation;
	@Inject @Named (IGritsConstants.CONFIG_LOCATION)
	private static String configLocation;
	
	
	public static QrtPCRRun loadQrtPCRRun(Entry entry, String filename) throws FileNotFoundException, UnsupportedEncodingException, JAXBException {
        String projectFolderLocation = workspaceLocation.substring(0, workspaceLocation.length()-1) 
                + File.separator
                + entry.getParent().getParent().getDisplayName();
        String qrtPCRFolderLocation = projectFolderLocation
                + File.separator
                + Config.folderName;
  
        //file with absolute path
        String fileLocation= qrtPCRFolderLocation 
                + File.separator 
                + filename;
        File qrtPCRFile = new File(fileLocation);
        
        FileInputStream inputStream = new FileInputStream(qrtPCRFile.getAbsolutePath());
        InputStreamReader reader = new InputStreamReader(inputStream, PropertyHandler.GRITS_CHARACTER_ENCODING);
        JAXBContext context = JAXBContext.newInstance(QrtPCRRun.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        QrtPCRRun run = (QrtPCRRun) unmarshaller.unmarshal(reader);
        return run;
	}
	
	public static void saveQrtPCRRun(QrtPCRRun run, String fileLocation) throws IOException, JAXBException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();   
        JAXBContext context = JAXBContext.newInstance(QrtPCRRun.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, PropertyHandler.GRITS_CHARACTER_ENCODING);
        marshaller.marshal(run, os);
       
        FileWriter fileWriter = new FileWriter(fileLocation);
        fileWriter.write(os.toString((String) marshaller.getProperty(Marshaller.JAXB_ENCODING)));
        fileWriter.close();
        os.close();
         
	}
	
	/**
	 * copy the given file into the workspace and give a unique name to the file (if there already exists a file with that name in the workspace)
	 * @param entry Sample Entry
	 * @param filename
	 * @return the generated filename in the workspace
	 * @throws IOException 
	 */
	public static String copyFileIntoWorkspace (Entry entry, String filename) throws IOException {
        String projectFolderLocation = workspaceLocation.substring(0, workspaceLocation.length()-1) 
                + File.separator
                + DataModelSearch.findParentByType(entry, ProjectProperty.TYPE);
        String qrtPCRFolderLocation = projectFolderLocation
                + File.separator
                + Config.folderName;
        String fileFolderLocation = qrtPCRFolderLocation + File.separator + "files";
        
        File fileFolder = new File (fileFolderLocation);
        if (!fileFolder.exists())
        	fileFolder.mkdirs();
        
        File originalFile = new File(filename);
		File destinationFile = new File (fileFolderLocation + File.separator + originalFile.getName());
		while (destinationFile.exists()) {
			String file = generateUniqueFileName(destinationFile.getName(), fileFolder.list());
			destinationFile = new File (fileFolderLocation + File.separator +  file);
		}
		FileOutputStream workspaceFile;
		workspaceFile = new FileOutputStream(destinationFile);
		Files.copy(originalFile.toPath(), workspaceFile);
		workspaceFile.close();
		
		return destinationFile.getName();
	}
	
	public static String getFileFolder (Entry entry) {
        String projectFolderLocation = workspaceLocation.substring(0, workspaceLocation.length()-1) 
                + File.separator
                + DataModelSearch.findParentByType(entry, ProjectProperty.TYPE);
        String qrtPCRFolderLocation = projectFolderLocation
                + File.separator
                + Config.folderName;
        String fileFolderLocation = qrtPCRFolderLocation + File.separator + "files";
        
        return fileFolderLocation;
	}
	
	public static String generateUniqueFileName (String filename, String[] existingNames) {
		if (filename != null) {
			String name = filename;
			String extension = "";
			if (filename.contains(".")) {
				extension = filename.substring(filename.lastIndexOf("."));
				name = filename.substring(0, filename.lastIndexOf("."));
			}
			int i;
			if ((i = name.indexOf ("(")) != -1) {
				String counter = name.substring(i+1, name.indexOf(")"));
				int count = Integer.parseInt(counter);
				count ++;
				
				String newFilename = name.substring(0, name.indexOf("("));
				newFilename += "(" + count + ")";
				newFilename += extension;
				return newFilename;
			} else {
				String newFilename = "";
				int count = 1;
				do {
					// there is no "(version)", generate the first version
					newFilename = name + "(" + count + ")";
					newFilename += extension;
					count++;
				} while (Arrays.asList(existingNames).contains(newFilename));
				return newFilename;
			}
		}
		
		return null;
	}
	
	 public static void copyMasterGeneListsFromJar() throws IOException {
    	String configFolderLocation = configLocation + File.separator + Config.configFolderName;
    	File configFolder = new File (configFolderLocation);
    	if (!configFolder.exists())
    		configFolder.mkdirs();
    	
    	URL resourceFileUrl = FileLocator.toFileURL(Config.FILE_URL);
    	if (resourceFileUrl == null) {
			throw new IOException ("There are no master gene lists defined in GRITS!");
		}
        String originalJarFilePath = resourceFileUrl.getPath();
        File originalJarFile = new File(originalJarFilePath);
        if (originalJarFile.isDirectory()) { // it should be
        	File[] files = originalJarFile.listFiles();
        	for (File file : files) {
        		FileOutputStream configFile = new FileOutputStream(configFolderLocation + File.separator + file.getName());
                Files.copy(file.toPath(), configFile);
                configFile.close();
			}
        }  
	 }
	 
	 public static String saveGeneSubListFile (GeneList geneList) throws IOException, JAXBException{
		 String configFolderLocation = configLocation + File.separator + Config.configFolderName;
		 File configFolder = new File (configFolderLocation);
		 if (!configFolder.exists())
			 configFolder.mkdirs();
		 String sublistFolderLocation = configFolderLocation + File.separator + Config.configSubFolder;
		 File sublistFolder = new File (sublistFolderLocation);
		 if (!sublistFolder.exists())
			 sublistFolder.mkdirs();
		 
		 // need to generate unique file name
		 String filename = generateUniqueFileName (geneList.getListName(), sublistFolder.list());
		 // before saving, clean up data related attributes (such as runId, shouldRerun etc.)
		 GeneUtils.cleanUpGenesForMasterGeneList(geneList);
		 geneList.setFilename(filename);
     	 // save the gene list
		 ByteArrayOutputStream os = new ByteArrayOutputStream();
		 JAXBContext context = JAXBContext.newInstance(GeneList.class);
		 Marshaller marshaller = context.createMarshaller();
		 marshaller.setProperty(Marshaller.JAXB_ENCODING, PropertyHandler.GRITS_CHARACTER_ENCODING);
		 marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		 marshaller.marshal(geneList, os);
		
		 //write the serialized data to the folder
		 FileWriter fileWriter = new FileWriter(sublistFolderLocation + File.separator + filename + ".xml");
		 fileWriter.write(os.toString((String)marshaller.getProperty(Marshaller.JAXB_ENCODING)));
		 fileWriter.close();
		 os.close();	
		 
		 return filename +".xml";
	 }

	public static String saveMasterGeneListFile(GeneList geneList)throws IOException, JAXBException{
		 String configFolderLocation = configLocation + File.separator + Config.configFolderName;
		 File configFolder = new File (configFolderLocation);
		 if (!configFolder.exists())
			 configFolder.mkdirs();
		 
		 // need to generate unique file name
		 String filename = generateUniqueFileName (geneList.getListName(), configFolder.list());
		 // before saving, clean up data related attributes (such as runId, shouldRerun etc.)
		 GeneUtils.cleanUpGenesForMasterGeneList(geneList);
		 geneList.setFilename(filename);
     	 // save the gene list
		 ByteArrayOutputStream os = new ByteArrayOutputStream();
		 JAXBContext context = JAXBContext.newInstance(GeneList.class);
		 Marshaller marshaller = context.createMarshaller();
		 marshaller.setProperty(Marshaller.JAXB_ENCODING, PropertyHandler.GRITS_CHARACTER_ENCODING);
		 marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		 marshaller.marshal(geneList, os);
		
		 //write the serialized data to the folder
		 FileWriter fileWriter = new FileWriter(configFolderLocation + File.separator + filename + ".xml");
		 fileWriter.write(os.toString((String)marshaller.getProperty(Marshaller.JAXB_ENCODING)));
		 fileWriter.close();
		 os.close();	
		 
		 return filename +".xml";
	}
	
	public static GeneList importAndLoadGeneListFile (String filePath, boolean sublist) throws IOException, JAXBException{
		String configFolderLocation = configLocation + File.separator + Config.configFolderName;
		if (sublist)
			configFolderLocation = configFolderLocation + File.separator + Config.configSubFolder;
		File configFolder = new File (configFolderLocation);
		if (!configFolder.exists())
			configFolder.mkdirs();
		
		File originalFile = new File (filePath);
		if (!originalFile.exists())
			throw new IOException ("The file path is not valid. The file " + filePath + " does not exists");
		
		// try to load to see if it is a valid gene list file
		FileInputStream inputStream = new FileInputStream(originalFile.getAbsolutePath());
        InputStreamReader reader = new InputStreamReader(inputStream, PropertyHandler.GRITS_CHARACTER_ENCODING);
        JAXBContext context = JAXBContext.newInstance(GeneList.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        GeneList geneList = (GeneList) unmarshaller.unmarshal(reader);
		
        // if successful copy into the config directory
		String newFileName = generateUniqueFileName(originalFile.getName(), configFolder.list());
		FileOutputStream configFile = new FileOutputStream(configFolderLocation + File.separator + newFileName);
        Files.copy(originalFile.toPath(), configFile);
        configFile.close();
        
        return geneList;
	}
	
	public static void downloadFile(String file, String newPath, boolean sublist) throws IOException {
		String configFolderLocation = configLocation + File.separator + Config.configFolderName;
		if (sublist)
			configFolderLocation += File.separator + Config.configSubFolder;
        File masterListFile = new File(configFolderLocation + File.separator + file);
        FileOutputStream out = new FileOutputStream(newPath);
        Files.copy(masterListFile.toPath(), out);
		out.close();
	}
	
	public static void deleteFile(String file, boolean sublist) throws IOException {
		String configFolderLocation = configLocation + File.separator + Config.configFolderName;
		if (sublist)
			configFolderLocation += File.separator + Config.configSubFolder;
        File masterListFile = new File(configFolderLocation + File.separator + file);
        masterListFile.delete();
	}
	
	public static List<GeneList> getAllGeneLists (boolean sublist) throws IOException, JAXBException {
		List<GeneList> allGeneLists = new ArrayList<>();
		String configFolderLocation = configLocation + File.separator + Config.configFolderName;
		if (sublist)
			configFolderLocation = configFolderLocation + File.separator + Config.configSubFolder;
    	File configFolder = new File (configFolderLocation);
    	List<File> masterGeneListFiles = new ArrayList<File>();
    	if (configFolder.exists()) {
	    	masterGeneListFiles.addAll(Arrays.asList(configFolder.listFiles()));
    	}
    	
    	for (File file : masterGeneListFiles) {
			if (file.isDirectory() || file.isHidden())
				continue;
			// try to load to see if it is a valid gene list file
			FileInputStream inputStream = new FileInputStream(file.getAbsolutePath());
	        InputStreamReader reader = new InputStreamReader(inputStream, PropertyHandler.GRITS_CHARACTER_ENCODING);
	        JAXBContext context = JAXBContext.newInstance(GeneList.class);
	        Unmarshaller unmarshaller = context.createUnmarshaller();
	        GeneList geneList = (GeneList) unmarshaller.unmarshal(reader);
	        allGeneLists.add(geneList);
		}
    	
    	return allGeneLists;
	}

	public static GeneList loadGeneListFile(String masterListFile, boolean sublist) throws IOException, JAXBException {
		String configFolderLocation = configLocation + File.separator + Config.configFolderName;
		if (sublist)
			configFolderLocation = configFolderLocation + File.separator + Config.configSubFolder;
		File configFolder = new File (configFolderLocation);
		
		File originalFile = new File (configFolder + File.separator + masterListFile);
		// try to load to see if it is a valid gene list file
		FileInputStream inputStream = new FileInputStream(originalFile.getAbsolutePath());
        InputStreamReader reader = new InputStreamReader(inputStream, PropertyHandler.GRITS_CHARACTER_ENCODING);
        JAXBContext context = JAXBContext.newInstance(GeneList.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        GeneList geneList = (GeneList) unmarshaller.unmarshal(reader);
        
        return geneList;
	}
	
	public static List<String> getAllPlateLayoutNames () {
		List<String> plateLayoutNames = new ArrayList<>();
		List<PlateLayout> allLayouts;
		try {
			allLayouts = getAllPlateLayouts();
			for (PlateLayout plateLayout : allLayouts) {
				plateLayoutNames.add(plateLayout.getName());
			}
		} catch (IOException e) {
			Logger.getLogger(FileUtils.class).error ("Error loading plate layout file", e);
		}
		
		return plateLayoutNames;
	}
	
	public static List<PlateLayout> getAllPlateLayouts() throws IOException {
		List<PlateLayout> allPlateLayouts = new ArrayList<>();
		String configFolderLocation = configLocation + File.separator + Config.configFolderName + 
				File.separator + Config.configPlateLayoutSubFolder;
		File configFolder = new File (configFolderLocation);
    	List<File> plateLayoutFiles = new ArrayList<File>();
    	if (configFolder.exists()) {
    		plateLayoutFiles.addAll(Arrays.asList(configFolder.listFiles()));
    	}
    	
    	for (File file : plateLayoutFiles) {
			if (file.isDirectory() || file.isHidden())
				continue;
			// try to load to see if it is a valid gene list file
			try {
				FileInputStream inputStream = new FileInputStream(file.getAbsolutePath());
		        InputStreamReader reader = new InputStreamReader(inputStream, PropertyHandler.GRITS_CHARACTER_ENCODING);
		        JAXBContext context = JAXBContext.newInstance(PlateLayout.class);
		        Unmarshaller unmarshaller = context.createUnmarshaller();
		        PlateLayout layout = (PlateLayout) unmarshaller.unmarshal(reader);
		        allPlateLayouts.add(layout);
			} catch (JAXBException e) {
				Logger.getLogger(FileUtils.class).error ("Invalid Plate Layout file. Skipping!", e);
				continue;
			}
		}
		
		return allPlateLayouts;
	}
	
	/**
	 * 
	 * @param name
	 * @param layout
	 * @return the filename for the plate layout
	 * @throws JAXBException 
	 * @throws IOException 
	 */
	public static String savePlateLayoutFile (PlateLayout layout) throws JAXBException, IOException {
		 String configFolderLocation = configLocation + File.separator + Config.configFolderName;
		 File configFolder = new File (configFolderLocation);
		 if (!configFolder.exists())
			 configFolder.mkdirs();
		 String configSubFolderLocation = configFolderLocation + File.separator + Config.configPlateLayoutSubFolder;
		 File configSubFolder = new File (configSubFolderLocation);
		 if (!configSubFolder.exists())
			 configSubFolder.mkdirs();
		 
		 if (layout.getFilename() == null) {
			 // need to generate unique file name
			 String filename = generateUniqueFileName (layout.getName(), configSubFolder.list());
			 layout.setFilename(filename + ".xml");
		 }
    	 // save the plate layout
		 ByteArrayOutputStream os = new ByteArrayOutputStream();
		 JAXBContext context = JAXBContext.newInstance(PlateLayout.class);
		 Marshaller marshaller = context.createMarshaller();
		 marshaller.setProperty(Marshaller.JAXB_ENCODING, PropertyHandler.GRITS_CHARACTER_ENCODING);
		 marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		 marshaller.marshal(layout, os);
		
		 //write the serialized data to the folder
		 FileWriter fileWriter = new FileWriter(configSubFolderLocation + File.separator + layout.getFilename());
		 fileWriter.write(os.toString((String)marshaller.getProperty(Marshaller.JAXB_ENCODING)));
		 fileWriter.close();
		 os.close();	
		 
		 return layout.getFilename();
	}

	public static String getPlateLayoutFolder() {
		String configFolderLocation = configLocation + File.separator + Config.configFolderName;
		String configSubFolderLocation = configFolderLocation + File.separator + Config.configPlateLayoutSubFolder;
		return configSubFolderLocation;
	}
}
