package org.grits.toolbox.entry.qrtpcr.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.display.control.table.datamodel.GRITSColumnHeader;
import org.grits.toolbox.entry.qrtpcr.Config;
import org.grits.toolbox.entry.qrtpcr.commands.EvaluateData;
import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.GeneData;
import org.grits.toolbox.entry.qrtpcr.model.GeneList;
import org.grits.toolbox.entry.qrtpcr.model.QrtPCRData;
import org.grits.toolbox.entry.qrtpcr.model.QrtPCRRun;
import org.grits.toolbox.entry.qrtpcr.model.QrtPCRTable;
import org.grits.toolbox.entry.qrtpcr.ncbi.NCBIGeneUtil;
import org.grits.toolbox.entry.qrtpcr.table.manager.ManagerNatTable;

public class ExcelFileHandler {
	
	private static XSSFCellStyle stylePink;
	private static XSSFCellStyle boldStyle;
	private static XSSFCellStyle stylePurple;
	private static XSSFCellStyle styleRed;
	private static XSSFCellStyle blueBackGround;
	private static XSSFCellStyle yellowCell;
	private static XSSFCellStyle styleRedFill;
	private static XSSFCellStyle styleOrangeFill;
	
	private static int DEFAULT_CONTROL_GENE_LOCATION = 9;
	
	private static Logger logger = Logger.getLogger(ExcelFileHandler.class);
	/** 
	 * to be used one time to get master gene list into GRITS 
	 * 
	 * @param filename full path of the excel file containing master gene lists
	 * @return
	 * @throws IOException
	 */
	public static List<GeneList> readMasterGeneLists (String filename, String organism) throws IOException {
		File masterFile = new File(filename);
        FileInputStream file = new FileInputStream(masterFile);
 
        List<GeneList> masterLists = new ArrayList<>();
        //Create Workbook instance holding reference to .xlsx file
        XSSFWorkbook workbook = new XSSFWorkbook(file);
        Iterator<Sheet> sheets = workbook.iterator();
        while (sheets.hasNext()) {
        	GeneList list = new GeneList();
        	Sheet sheet = sheets.next();
        	if (!sheet.getSheetName().contains("Gene List"))
        		continue;
        	list.setListName(sheet.getSheetName());
        	List<Gene> genes = readMasterGeneListFile(sheet, organism);
        	list.setGenes(genes);
        	list.setOrganism(organism);
        	masterLists.add(list);
        }
        
        workbook.close();
        file.close();
        return masterLists;
	}
	
	/**
	 * one time use to import from excel file into GRITS format
	 *  
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public static List<Gene> readMasterGeneListFile(String filename, String organism) throws IOException {
        FileInputStream file = new FileInputStream(new File(filename));
 
        //Create Workbook instance holding reference to .xlsx file
        XSSFWorkbook workbook = new XSSFWorkbook(file);
        Iterator<Sheet> sheets = workbook.iterator();
        List<Gene> genes = readMasterGeneListFile(sheets.next(), organism);
        workbook.close();
        file.close();
        return genes;
	}

	public static List<Gene> readMasterGeneListFile(Sheet sheet, String organism) throws IOException {
		List<Gene> geneList = new ArrayList<Gene>();
		
        //Iterate through each row one by one
        Iterator<Row> rowIterator = sheet.iterator();
        int i=0;
        while (rowIterator.hasNext())
        {
            Row row = rowIterator.next();
            if (i < 9) {
            	i++;    // skip the first few lines since actual data starts at line 11 or 9
            	continue;
            }
            
            boolean geneAdd = false;
            Gene gene = new Gene();
            //For each row, iterate through all the columns
            Iterator<Cell> cellIterator = row.cellIterator();
            boolean makeACopy = false;
            String prefixIdCopy=null; // to be copied
            String geneSymbolCopy=null;
            while (cellIterator.hasNext())
            {
                Cell cell = cellIterator.next();
                
                switch (cell.getColumnIndex()) {
                case 0: // group
                	String group = cell.getStringCellValue();
                	gene.setGroup(group);
                	if (group != null && group.toLowerCase().equalsIgnoreCase("housekeeping gene"))
                		gene.setIsCommon(true);
                	break;
                case 1: // designed For or pathway
                	if (cell.getCellType() == CellType.NUMERIC) {
                		gene.setNotes(cell.getNumericCellValue() + "");
                	}
                	else 
                		gene.setNotes(cell.getStringCellValue());
                	break;
                case 2: // Prefix_ID
                	String prefixId = cell.getStringCellValue();
                	if (prefixId != null && prefixId.length() > 0)
                		geneAdd = true; // get the genes with Prefix_ID only
                	else
                		break;
                	
                	if (prefixId.contains("/")) {
                		// alternative, make a copy of this gene with a different prefixId
                		gene.setGeneIdentifier(prefixId.substring(0, prefixId.indexOf("/")));
                		// get the prefix
                		int pindex = prefixId.indexOf("_");
                		if (pindex != -1) {
	                		String pre = prefixId.substring(0, pindex);
	                		prefixIdCopy = pre + "_" + prefixId.substring(prefixId.indexOf("/")+1);
	                		makeACopy = true;
                		}
                	}
                	else if (prefixId.contains("(")) {
                		gene.setGeneIdentifier(prefixId.substring(0, prefixId.indexOf("(")).trim());
                		makeACopy = true;
                		prefixIdCopy = prefixId.substring(prefixId.indexOf("(")+1, prefixId.indexOf(")"));
                	}
                	else {
                		gene.setGeneIdentifier(prefixId);
                	}
                	break;
                case 3: // old_name
                	String value = cell.getStringCellValue();
                	if (value != null && value.toLowerCase().contains("no mouse gene")) {
                		geneAdd = false;
                		break;
                	}
                	else {
                		gene.setAliasString(value.trim());
                	}
                	break;
                case 4: //Gene_ID
                	if (cell.getCellType() == CellType.NUMERIC) {
                		gene.addGeneId((int)cell.getNumericCellValue());
                	}
                	else {
	                	String geneId = cell.getStringCellValue();
	                	if (geneId != null && geneId.toLowerCase().startsWith("no hs gene")) { // need to skip this gene
	                		geneAdd=false;
	                		break;
	                	}
	                	try {
	                		gene.setGeneIds(geneId);
	                	} catch (NumberFormatException e) {
	                		logger.warn("gene id is not valid", e);
	                	}
                	}
                	break;
                case 5: // RefSeq
                	gene.addRefSeq(cell.getStringCellValue().trim());
                	break;
                case 6: // Previous/Alternative RefSeq
                	List<String> existing = gene.getRefSeq();
                	gene.setRefSeq(cell.getStringCellValue().trim());
                	List<String> alternatives = gene.getRefSeq();
                	gene.setRefSeq(existing);
                	for (String string : alternatives) {
						gene.addRefSeq(string);
					}
                	break;
                case 7: // Gene Symbol
                	String symbol = cell.getStringCellValue();
                	if (symbol != null && symbol.contains("/") && makeACopy) {
                		gene.setGeneSymbol(symbol.substring(0, symbol.indexOf("/")));
                		geneSymbolCopy = symbol.substring(symbol.indexOf("/")+1);
                	} else if (symbol.contains("(") && makeACopy) {
                		gene.setGeneSymbol(symbol.substring(0, symbol.indexOf("(")).trim());
                		geneSymbolCopy = symbol.substring(symbol.indexOf("(")+1, symbol.indexOf(")"));
                	}else
                		gene.setGeneSymbol(symbol);
                	break;
                case 8: // Forward Primer
                	gene.setForwardPrimer(cell.getStringCellValue());
                	break;
                case 9: // Reverse Primer
                	gene.setReversePrimer(cell.getStringCellValue());
                	break;
                case 10: // Description
                	gene.setDescription(cell.getStringCellValue());
                	break;
                default:
                	break;
                }
            }
            
            if (makeACopy && prefixIdCopy != null) {
            	Gene copyGene = GeneUtils.makeACopy(gene);
            	copyGene.setGeneIdentifier(prefixIdCopy);
            	if (geneSymbolCopy != null) 
            		copyGene.setGeneSymbol(geneSymbolCopy);
            	geneList.add(copyGene);
            }
            
            
            
            if (geneAdd) {
            	// get other available details from NCBI
                try {
    				getDetailsFromNCBI (gene, organism);
    			} catch (Exception e) {
    				logger.error("Could not get the details from NCBI for gene " + gene.getGeneSymbol(), e);
    			}
            	geneList.add(gene);
            }
        }
        
        return geneList;  
	}
	
	private static void getDetailsFromNCBI(Gene gene, String organism) throws MalformedURLException, Exception {
		NCBIGeneUtil util = new NCBIGeneUtil();
		if (gene.getGeneIds() != null) {
			List<String> aliasList = new ArrayList<>();
			List<String> locationList = new ArrayList<>();
			List<String> refSeqList = new ArrayList<>();
			List<String> secondaryRefSeqList = new ArrayList<>();
			String fullname=null;
			for (Integer geneId : gene.getGeneIds()) {
				Gene newGene = util.getDetailsFromNCBI(geneId);
				if (newGene != null) {
					aliasList.addAll(newGene.getAliases());
					locationList.addAll(newGene.getLocations());
					refSeqList.addAll(newGene.getRefSeq());
					secondaryRefSeqList.addAll(newGene.getSecondaryRefSeq());
					fullname = newGene.getFullName();
				}
			}
			if (gene.getRefSeq() != null)
				gene.getRefSeq().addAll(refSeqList);
			else
				gene.setRefSeq(refSeqList);
			gene.setSecondaryRefSeq(secondaryRefSeqList);
			gene.setLocations(locationList);
			if (gene.getAliases() != null)
				gene.getAliases().addAll(aliasList);
			else
				gene.setAliases(aliasList);
			gene.setFullName(fullname);
		} else {
			// search by symbol
			Integer geneId = util.getGeneIdFromNCBI(gene.getGeneSymbol(), organism);
			if (geneId != null) {
				Gene newGene = util.getDetailsFromNCBI(geneId);
				if (newGene != null) {
					if (gene.getAliases() != null)
						gene.getAliases().addAll(newGene.getAliases());
					else
						gene.setAliases(newGene.getAliases());
					gene.setLocations(newGene.getLocations());
					if (gene.getRefSeq() != null)
						gene.getRefSeq().addAll(newGene.getRefSeq());
					else
						gene.setRefSeq(newGene.getRefSeq());
					gene.setSecondaryRefSeq(newGene.getSecondaryRefSeq());
					gene.setFullName(newGene.getFullName());
				}
			}
		}
		
	}

	public static Map<String, List<String>> readRerunLayoutFile (Entry entry, String filename, int rep, String sheetName) throws IOException {
		String fileFolder = FileUtils.getFileFolder(entry);
        FileInputStream file = new FileInputStream(new File(fileFolder + File.separator + filename));
		 
        //Create Workbook instance holding reference to .xlsx file
        XSSFWorkbook workbook = new XSSFWorkbook(file);
 
        XSSFSheet sheet = null;
        if (sheetName != null) {
        	sheet = workbook.getSheet(sheetName);
        }
        
        if (sheet == null) {
        	workbook.close();
        	throw new IOException("Invalid file. Could not find plate layout sheet");
        }
        //Iterate through each rows one by one
        Iterator<Row> rowIterator = sheet.iterator();
		
		Map<String, List<String>> geneListMap = new TreeMap<String, List<String>>();
        Map<Integer, String> plateMap = new HashMap<>();
        
        int i=0;
        int numOfColumns = (int) (12/rep); // if #ofReplicates is 3, then there are 4 columns containing gene identifiers, if 4, there are 3 columns with gene identifiers
        int increment = numOfColumns + 3; // 3 empty columns, 2 at the beginning (PLATEID and position), one in between plates
        int plateIdIndex = 0;
        
        while (rowIterator.hasNext())
        {
            Row row = rowIterator.next();
            int j=0;
            plateIdIndex=0;
            Iterator<Cell> cellIterator = row.cellIterator();
            while (cellIterator.hasNext())
            {
                Cell cell = cellIterator.next();
                int columnIndex = cell.getColumnIndex();
            	if (i==0 && columnIndex == plateIdIndex ) { // first row
            		String plateId = cell.getStringCellValue();
                	plateMap.put (columnIndex, plateId);	
                	plateIdIndex += increment;
                }
            	else if (i != 0){
            		if (columnIndex >= j) 
            			// advance j
            			j += increment;
            		// find the plateId from the plateMap
            		if (columnIndex == (j-increment) || columnIndex == j-1 || columnIndex == j-increment+1)
            			// skip
            			continue;
                	String plateId = plateMap.get(j-increment);
                	if (plateId != null) {
	                	List<String> geneList = geneListMap.get(plateId);
	                	if (geneList == null)
	                		geneList = new ArrayList<>();
	                	String geneIdentifier = cell.getStringCellValue();
	                	if (geneIdentifier != null && geneIdentifier.trim().length() > 0 && !geneIdentifier.equalsIgnoreCase("empty")) {	
	                		if (geneIdentifier.contains("/")) {
                        		geneIdentifier = geneIdentifier.substring(0, geneIdentifier.indexOf("/"));
                        	}
                        	else if (geneIdentifier.contains("(")) {
                        		geneIdentifier = geneIdentifier.substring(0, geneIdentifier.indexOf("(")).trim();
                        	}
	                		geneList.add(cell.getStringCellValue());
	                		geneListMap.remove(plateId);
	                		geneListMap.put(plateId, geneList);
	                	}
                	}
            	}
            }
            i++;
        }
        
        file.close();
        workbook.close();
        return geneListMap;
	}
	
	public static List<String> readSheetsFromRerunLayoutFile(Entry entry, String filename) throws IOException {
		List<String> sheetList = new ArrayList<>();
		String fileFolder = FileUtils.getFileFolder(entry);
        FileInputStream file = new FileInputStream(new File(fileFolder + File.separator + filename));
		 
        //Create Workbook instance holding reference to .xlsx file
        XSSFWorkbook workbook = new XSSFWorkbook(file);
 
        //Get first/desired sheet from the workbook
        Sheet sheet= null;
        Iterator<Sheet> sheets = workbook.iterator();
        while (sheets.hasNext()) {
        	sheet = sheets.next();
        	Cell plateHeader = sheet.getRow(0).getCell(0);
        	if (plateHeader != null && (plateHeader.getStringCellValue().startsWith("PLATE")
        			|| plateHeader.getStringCellValue().startsWith("Plate")) ) {
        		sheetList.add(sheet.getSheetName());
        	}
        }
        
        file.close();
        workbook.close();
        return sheetList;
	}
	
	
	public static List<String> readSheetsFromLayoutFile (Entry entry, String filename) throws IOException {
		List<String> sheetList = new ArrayList<>();
		String fileFolder = FileUtils.getFileFolder(entry);
        FileInputStream file = new FileInputStream(new File(fileFolder + File.separator + filename));
		 
        //Create Workbook instance holding reference to .xlsx file
        XSSFWorkbook workbook = new XSSFWorkbook(file);
 
        //Get first/desired sheet from the workbook
        Sheet sheet= null;
        Iterator<Sheet> sheets = workbook.iterator();
        while (sheets.hasNext()) {
        	sheet = sheets.next();
            Cell plateHeader = sheet.getRow(0).getCell(1);
        	if (plateHeader != null && 
        			(plateHeader.getStringCellValue().startsWith("Plate")
        					|| plateHeader.getStringCellValue().startsWith("PLATE"))) 
        		sheetList.add(sheet.getSheetName());
        }
        
        file.close();
        workbook.close();
        return sheetList;
	}
	
	public static Map<String, List<String>> readLayoutFile (Entry entry, String filename, String sheetName) throws IOException {
		String fileFolder = FileUtils.getFileFolder(entry);
        FileInputStream file = new FileInputStream(new File(fileFolder + File.separator + filename));
		 
        //Create Workbook instance holding reference to .xlsx file
        XSSFWorkbook workbook = new XSSFWorkbook(file);
 
        XSSFSheet sheet = null;
        if (sheetName != null) {
        	sheet = workbook.getSheet(sheetName);
        }
 
        if (sheet == null) {
        	workbook.close();
        	throw new IOException("Invalid file. Could not find plate layout sheet");
        }
        //Iterate through each rows one by one
        Iterator<Row> rowIterator = sheet.iterator();
        
        Map<String, List<String>> geneListMap = new TreeMap<String, List<String>>();
        Map<Integer, String> plateMap = new HashMap<>();
        int i=0;
        while (rowIterator.hasNext())
        {
            Row row = rowIterator.next();
            int j=1;
            Iterator<Cell> cellIterator = row.cellIterator();
            while (cellIterator.hasNext())
            {
                Cell cell = cellIterator.next();
                int columnIndex = cell.getColumnIndex();
                if (columnIndex == j) {  // only interested in column 1, 5, 9, 13, 17 and so on
                	// need to read the first row for the list of plateIds
                    if (i==0) { // first row
                    	String plateId = cell.getStringCellValue();
                    	plateMap.put (cell.getColumnIndex(), plateId);	
                    }
                    else {
                    	// find the plateId from the plateMap
                    	String plateId = plateMap.get(cell.getColumnIndex());
                    	if (plateId != null) {
	                    	List<String> geneList = geneListMap.get(plateId);
	                    	if (geneList == null)
	                    		geneList = new ArrayList<>();
	                    	String geneIdentifier = cell.getStringCellValue();
	                    	if (geneIdentifier != null && geneIdentifier.trim().length() > 0 && !geneIdentifier.equalsIgnoreCase("empty")) {
	                    		if (geneIdentifier.contains("/")) {
	                        		geneIdentifier = geneIdentifier.substring(0, geneIdentifier.indexOf("/"));
	                        	}
	                        	else if (geneIdentifier.contains("(")) {
	                        		geneIdentifier = geneIdentifier.substring(0, geneIdentifier.indexOf("(")).trim();
	                        	}
	                    		geneList.add(geneIdentifier);
	                    		geneListMap.remove(plateId);
	                    		geneListMap.put(plateId, geneList);
	                    	}
                    	}
                    }
                    j +=4; // only interested in column 1, 5, 9, 13, 17 and so on
                }
            }
            i++;
        }
        
        file.close();
        workbook.close();
        
        return geneListMap;
	}
	
	public static void exportQrtPCRTable (QrtPCRRun pcrRun, String filename, boolean exportMaster, boolean exportPlateData, boolean exportReruns) throws IOException {
		
		//Blank workbook
        XSSFWorkbook workbook = new XSSFWorkbook();
        
        boldStyle  = workbook.createCellStyle();
        Font bold = workbook.createFont();
        bold.setBold(true);
        boldStyle.setFont(bold);
        
        stylePurple  = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.LAVENDER.index);
        stylePurple.setFont(font);
        
        stylePink  = workbook.createCellStyle();
        font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.ROSE.index);
        stylePink.setFont(font);
         
        styleRed = workbook.createCellStyle();
        Font redFont = workbook.createFont();
        redFont.setColor(IndexedColors.RED.index);
        styleRed.setFont(redFont);
        
        blueBackGround = workbook.createCellStyle();
        blueBackGround.setFillForegroundColor(IndexedColors.LIGHT_BLUE.index);
        blueBackGround.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        yellowCell = workbook.createCellStyle();
        yellowCell.setFillForegroundColor(IndexedColors.YELLOW.index);
        yellowCell.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        styleRedFill = workbook.createCellStyle();
        styleRedFill.setFillForegroundColor(IndexedColors.RED.index);
        styleRedFill.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        styleOrangeFill = workbook.createCellStyle();
        styleOrangeFill.setFillForegroundColor(IndexedColors.CORAL.index);
        styleOrangeFill.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        Map<Integer, QrtPCRTable> tableMap = pcrRun.getRunIdTableMap();
       
        for (Iterator<Integer> iterator = tableMap.keySet().iterator(); iterator.hasNext();) {
        	Integer runId = iterator.next();
			QrtPCRTable table = (QrtPCRTable) tableMap.get(runId);
			Set<String> plateIds = table.getPlateDataMap().keySet();
	        for (String plateId : plateIds) {
	        	QrtPCRData qrtPCRData = table.getData(plateId);
	        	qrtPCRData.calculateNormValue();
	        	if (table.getOriginal()) {
	            	// data needs evaluation
	            	EvaluateData.applyRules(qrtPCRData, 0, pcrRun.getThreshold(), pcrRun.getStDevCutOff());  // use current values
	            	qrtPCRData.calculateNormValue();
	            }
	        	String title = plateId;
	        	if (runId > 0) title = "Rerun-" + runId + "-" + plateId;
	            XSSFSheet sheet = workbook.createSheet(title);
	            int rownum = addHeaders (workbook, sheet, plateId, pcrRun.getThreshold(), pcrRun.getStDevCutOff());
	            int numReplicates = qrtPCRData.getNumberOfReplicates();
	            addData (sheet, qrtPCRData, rownum, numReplicates, DEFAULT_CONTROL_GENE_LOCATION, QrtPCRTable.defaultControlGene, false, pcrRun.getThreshold(), pcrRun.getStDevCutOff());
			}
	        
	        for (String plateId : plateIds) {
	        	QrtPCRData qrtPCRData = table.getData(plateId);
	        	String title = plateId + "-original";
	        	if (runId > 0) title = "Rerun-" + runId + "-" + plateId + "-original";
	            XSSFSheet rawDataSheet = workbook.createSheet(title);
	            int numReplicates = qrtPCRData.getNumberOfReplicates();
	            int rownum = addHeaders (workbook, rawDataSheet, plateId, pcrRun.getThreshold(), pcrRun.getStDevCutOff());
	            addData (rawDataSheet, qrtPCRData, rownum, numReplicates, DEFAULT_CONTROL_GENE_LOCATION, QrtPCRTable.defaultControlGene, true, pcrRun.getThreshold(), pcrRun.getStDevCutOff());
			}
	        if (!exportReruns)
	        	break;
		}
        
        if (exportMaster) {
        	List<Gene> geneList = QrtPCRRun.generateMasterTable(pcrRun.getFirstRun());
        	XSSFSheet masterTableSheet = workbook.createSheet("Master Table");
        	workbook.setSheetOrder("Master Table", 0);
        	addMasterTableHeaders(workbook, masterTableSheet);
        	addMasterTableData(masterTableSheet, geneList, pcrRun.getThreshold());
        }
        
        //Write the workbook in file system
        FileOutputStream out = new FileOutputStream(new File(filename));
        workbook.write(out);
        out.close();
        workbook.close();
	}
	
	private static void addMasterTableData(XSSFSheet sheet,
			List<Gene> geneList, Double lowerThreshold) {
		int rownum = 1;
		for (Gene gene : geneList) {
			Row row = sheet.createRow(rownum);
			if (gene.getIsControl() || gene.getIsCommon()) {
        		continue;
        	}
        	Cell cell = row.createCell(0);
        	cell.setCellValue(gene.getGeneIdentifier());
        	if (gene.getRunId() > 0) {
        		// rerun gene
        		cell.setCellStyle(styleOrangeFill);
        	}
        	cell = row.createCell(1);
        	cell.setCellValue(gene.getGeneSymbol());
        	cell = row.createCell(2);
        	cell.setCellValue(gene.getAdjustedAverage(gene.getRunId(), lowerThreshold, gene.getNormValue(gene.getRunId())));
        	cell = row.createCell(3);
        	cell.setCellValue(gene.getStDevForAdjusted(gene.getRunId(), lowerThreshold, gene.getNormValue(gene.getRunId())));
        	rownum++;
		}	
	}

	private static void addMasterTableHeaders(XSSFWorkbook workbook,
			XSSFSheet sheet) {
		Row row = sheet.createRow(0);
		Cell cell = row.createCell(0);
		cell.setCellValue("Gene Identifier");
		cell = row.createCell(1);
		cell.setCellValue("Gene Symbol");
		cell = row.createCell(2);
		cell.setCellValue("Average");
		cell = row.createCell(3);
		cell.setCellValue("StDev");
	}

	private static void addData (XSSFSheet sheet, QrtPCRData qrtPCRData, int rownum, int numReplicates, int controlGeneLocation, String controlGeneName, boolean raw, Double lowerThreshold, Double stdevCutOff) {
		for (Gene gene : qrtPCRData.getGenes()) {
        	gene.setNumOfReplicates(numReplicates);
        	Row row = sheet.createRow(rownum);
        	rownum++;
        	Cell cell = row.createCell(1);
        	cell.setCellValue(gene.getGeneIdentifier());
        	
        	if (gene.getIsControl()) {
        		controlGeneLocation = rownum;
        		controlGeneName = gene.getGeneIdentifier();
        	}
        	int i=0;
			for (GeneData geneData: gene.getDataMap().get(gene.getRunId())) { 
				cell = row.createCell(0);
				cell.setCellValue(geneData.getPosition().toString());
				cell = row.createCell(2);
				Double ct0;
				if (!raw)
					ct0 = geneData.getCt();
				else {
					ct0 = geneData.getOriginalCt();
				}
				if (ct0 == null) {
					cell.setCellValue(lowerThreshold);
					ct0 = lowerThreshold;
					cell.setCellStyle(stylePink);
				}
				else if (ct0 >= lowerThreshold) {
					if (raw) {
						cell.setCellValue(ct0);
					}
					else {
						ct0 = lowerThreshold;
						cell.setCellValue(ct0);
						cell.setCellStyle(stylePurple);
					}
				}
				else {
					cell.setCellValue (ct0);
					
					// check if the value is modified by calculations
					if (!raw && geneData.getPreviousValues() != null && 
							!geneData.getPreviousValues().isEmpty() &&
							geneData.getPreviousValues().get(0) != null)	
					{
						// there was an original value
						cell.setCellStyle (styleOrangeFill);
					}
				}
				
				// 2^-ct
				cell = row.createCell(4);
				cell.setCellValue (Math.pow (2, -1 * ct0));
				
				// Norm Value
				cell = row.createCell(5);
				Double normValue = null;
				normValue = qrtPCRData.findNormValue(0, raw);
				cell.setCellValue (normValue);
				cell.setCellStyle(blueBackGround);
				
				// Normalized
				cell = row.createCell(6);
				String formula = CellReference.convertNumToColString(cell.getColumnIndex() - 2) + String.valueOf (cell.getRowIndex()+1) + "/" +
						CellReference.convertNumToColString(cell.getColumnIndex() - 1) + String.valueOf (cell.getRowIndex()+1);
				cell.setCellFormula (formula);
				
				//Scaler
				cell = row.createCell(7);
				Double scaler = null;
				scaler = qrtPCRData.getScaler(lowerThreshold, raw);
				cell.setCellValue (scaler);
				cell.setCellStyle(yellowCell);
				
				// Adjusted
				cell = row.createCell(8);
				formula = CellReference.convertNumToColString(cell.getColumnIndex() - 2) + String.valueOf (cell.getRowIndex()+1) + "-" +
						CellReference.convertNumToColString(cell.getColumnIndex() - 1) + String.valueOf (cell.getRowIndex()+1);
				cell.setCellFormula (formula);
				i++;
				if (i == numReplicates) {
					// create the cell for stdDev
					cell = row.createCell(3);
					formula = "STDEV(";
					formula += CellReference.convertNumToColString(cell.getColumnIndex()-1) + String.valueOf(cell.getRowIndex()- numReplicates + 2);
					formula += ":";
					formula += CellReference.convertNumToColString(cell.getColumnIndex()-1) + String.valueOf(cell.getRowIndex()+1);
					formula += ")";
					cell.setCellFormula(formula);
					double stdev = gene.getStandardDeviation(lowerThreshold);  
					if (!raw && stdev >= stdevCutOff) {
						// marked for re-run ?
						if (gene.getShouldRerun()) {
							// paint the gene identifier cell's box red
							cell.setCellStyle(styleRed);
							Row idRow = sheet.getRow(cell.getRowIndex() - numReplicates +1);
							Cell idCell = idRow.getCell(1);
							idCell.setCellStyle(styleRedFill);
						} 
					}
					
					cell = row.createCell(9);
					formula = "AVERAGE(";
					formula += CellReference.convertNumToColString(cell.getColumnIndex()-1) + String.valueOf(cell.getRowIndex()- numReplicates + 2);
					formula += ":";
					formula += CellReference.convertNumToColString(cell.getColumnIndex()-1) + String.valueOf(cell.getRowIndex()+1);
					formula += ")";
					cell.setCellFormula(formula);
					
					cell = row.createCell(10);
					formula = "STDEV(";
					formula += CellReference.convertNumToColString(cell.getColumnIndex()-2) + String.valueOf(cell.getRowIndex()- numReplicates + 2);
					formula += ":";
					formula += CellReference.convertNumToColString(cell.getColumnIndex()-2) + String.valueOf(cell.getRowIndex()+1);
					formula += ")";
					cell.setCellFormula(formula);
					
				}
				if (i < numReplicates)
					row = sheet.createRow(rownum++);
			}
		}
        // add final row
        Row row = sheet.createRow(rownum++);
        Cell cell = row.createCell(1);
        cell.setCellValue("Avg " + controlGeneName);
        cell.setCellStyle(boldStyle);
        cell = row.createCell(2);
        String formula = "AVERAGE(" + CellReference.convertNumToColString(cell.getColumnIndex()) + controlGeneLocation + ":" +  CellReference.convertNumToColString(cell.getColumnIndex()) + String.valueOf(controlGeneLocation+numReplicates-1) + ")";
        cell.setCellFormula(formula);
        cell = row.createCell(4);
        cell.setCellValue (qrtPCRData.findNormValue(0, raw));
        cell = row.createCell(5);
        cell.setCellValue (qrtPCRData.findNormValue(0, raw));
        cell.setCellStyle(blueBackGround);
        cell = row.createCell(6);
		formula = CellReference.convertNumToColString(cell.getColumnIndex() - 2) + String.valueOf (cell.getRowIndex()+1) + "/" +
				CellReference.convertNumToColString(cell.getColumnIndex() - 1) + String.valueOf (cell.getRowIndex()+1);
		cell.setCellFormula (formula);
		
		//Scaler
		cell = row.createCell(7);
		cell.setCellValue (qrtPCRData.getScaler(lowerThreshold, raw));
		cell.setCellStyle(yellowCell);
		
		cell = row.createCell(8);
		formula = CellReference.convertNumToColString(cell.getColumnIndex() - 2) + String.valueOf (cell.getRowIndex()+1) + "-" +
				CellReference.convertNumToColString(cell.getColumnIndex() - 1) + String.valueOf (cell.getRowIndex()+1);
		cell.setCellFormula (formula);
	}

	/**
	 * 
	 * @param sheet
	 * @return the number of rows used for the headers
	 */
	private static int addHeaders(XSSFWorkbook workbook, XSSFSheet sheet, String plateId, Double lowerThreshold, Double stdevCutOff) {
        
		Row row = sheet.createRow(0);
		Cell cell = row.createCell(0);
		cell.setCellValue(plateId);
		cell = row.createCell(1);
		cell.setCellValue("Re-Run");
		cell.setCellStyle(styleRedFill);
		cell = row.createCell(2);
		cell.setCellValue(">=" + lowerThreshold + " in Bold Purple, empty values in Bold Pink");
		cell.setCellStyle(stylePurple);
		cell = row.createCell(3);
		cell.setCellValue(">=" + stdevCutOff + " in Red");
		cell.setCellStyle(styleRed);
		
		cell = row.createCell(9);
		sheet.addMergedRegion(new CellRangeAddress(
	            0, //first row (0-based)
	            0, //last row  (0-based)
	            9, //first column (0-based)
	            10  //last column  (0-based)
	    ));
		cell.setCellValue("Scaled Data");
		
		row = sheet.createRow(1);
		cell = row.createCell(0);
		cell.setCellValue("Well");
		cell = row.createCell(1);
		cell.setCellValue("Gene ID");
		cell = row.createCell(2);
		cell.setCellValue("Ct Value");
		cell = row.createCell(3);
		cell.setCellValue("Std Dev Cts");
		cell = row.createCell(4);
		cell.setCellValue("2^-ct");
		cell = row.createCell(5);
		cell.setCellValue("Norm Value");
		cell = row.createCell(6);
		cell.setCellValue("Normalized");
		cell = row.createCell(7);
		cell.setCellValue("Scaler");
		cell = row.createCell(8);
		cell.setCellValue("Adjusted");	
		cell = row.createCell(9);
		cell.setCellValue("Average");
		cell = row.createCell(10);
		cell.setCellValue("Stdev");
		return 2;
		
	}
	
	public static void exportMasterGeneList (String name, ManagerNatTable table, String filename) {
		if (table == null)
			return;
		Set<GRITSColumnHeader> columns = table.getTablePreference().getPreferenceSettings().getHeaders();
		
		DataLayer layer = table.getBodyDataLayer();
		
		// create a workbook
	    XSSFWorkbook wb = new XSSFWorkbook();
	    // shade the background of the header row
	    XSSFCellStyle headerStyle = wb.createCellStyle();
	    headerStyle.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
	    headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	    headerStyle.setBorderTop(BorderStyle.THIN);
	    headerStyle.setBorderBottom(BorderStyle.THIN);
	    headerStyle.setBorderLeft(BorderStyle.THIN);
	    headerStyle.setBorderRight(BorderStyle.THIN);
	    headerStyle.setAlignment(HorizontalAlignment.CENTER);
	    
	    XSSFCellStyle cellStyle = wb.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        
	    // add a worksheet
	    XSSFSheet sheet = wb.createSheet(name);
	    
	    // add header row
	    int rowIndex = 0;
	    int cellIndex = 0;
	    XSSFRow header = sheet.createRow((short) rowIndex++);
	    XSSFCell cell;
	    for (Iterator<GRITSColumnHeader> iterator = columns.iterator(); iterator.hasNext();) {
	    	GRITSColumnHeader column = (GRITSColumnHeader) iterator.next();
	    	// need to skip "Select" column if exists
	    	if (column.getKeyValue().equals(Config.SELECTED))
	    		continue;
	        cell = header.createCell(cellIndex++);
	        cell.setCellValue(column.getLabel());
	        cell.setCellStyle(headerStyle);
	    }
	    
	    // add data rows
	    for (int i=0; i < layer.getRowCount(); i++) {
	        // create a new row
	        XSSFRow row = sheet.createRow((short) rowIndex++);
	        cellIndex = 0;
	        int j=0;
	        for (Iterator<GRITSColumnHeader> iterator = columns.iterator(); iterator.hasNext();) {
	        	GRITSColumnHeader column = (GRITSColumnHeader) iterator.next();
		    	// need to skip "Select" column if exists
		    	if (column.getKeyValue().equals(Config.SELECTED)) {
		    		j++;
		    		continue;
		    	}
	            
	            // set the cell's value
	            Object val = layer.getDataValueByPosition(j, i);
	            if (val instanceof Boolean) {
	            	// checkbox column, skip it
	            	j++;
	            	continue;
	            }
	            
	            // create a new cell
	            cell = row.createCell(cellIndex++);
	            cell.setCellStyle(cellStyle);

	            String text = (String)val;
	            cell.setCellValue(text);
	            j++;
	        }
	    }

	    // autofit the columns
	    for (int i = 0; i < columns.size(); i++) {
	    	sheet.autoSizeColumn((short) i);
	    }
	    
	    try {
            FileOutputStream fos = new FileOutputStream(filename);
            wb.write(fos);
            fos.close();
            wb.close();
            MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
                "Export Gene List Successful",
                "Workbook saved to the file:\n\n" + filename);
        } catch (IOException ioe) {
            String msg = ioe.getMessage();
            MessageDialog.openError(Display.getCurrent().getActiveShell(), 
                "Export Gene List Failed",
                "Could not save workbook to the file:\n\n" + msg);
        }
	}

	public static void exportMasterGeneList (String name, TableViewer tableViewer, String filename) {
		if (tableViewer == null )
	    	return;
		
		// create a workbook
	    XSSFWorkbook wb = new XSSFWorkbook();
	    // shade the background of the header row
	    XSSFCellStyle headerStyle = wb.createCellStyle();
	    headerStyle.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
	    headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	    headerStyle.setBorderTop(BorderStyle.THIN);
	    headerStyle.setBorderBottom(BorderStyle.THIN);
	    headerStyle.setBorderLeft(BorderStyle.THIN);
	    headerStyle.setBorderRight(BorderStyle.THIN);
	    headerStyle.setAlignment(HorizontalAlignment.CENTER);
	    
	    XSSFCellStyle cellStyle = wb.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        
	    // add a worksheet
	    XSSFSheet sheet = wb.createSheet(name);
	    
	    // add header row
	    Table table = tableViewer.getTable();
	    TableColumn[] columns = table.getColumns();
	    int rowIndex = 0;
	    int cellIndex = 0;
	    XSSFRow header = sheet.createRow((short) rowIndex++);
	    XSSFCell cell;
	    for (TableColumn column : columns) {
	        cell = header.createCell(cellIndex++);
	        cell.setCellValue(column.getText());
	        cell.setCellStyle(headerStyle);
	    }
	    
	    // add data rows
	    TableItem[] items = table.getItems();
	    for (TableItem item : items) {
	        // create a new row
	        XSSFRow row = sheet.createRow((short) rowIndex++);
	        cellIndex = 0;

	        for (int i = 0; i < columns.length; i++) {
	            // create a new cell
	            cell = row.createCell(cellIndex++);
	            cell.setCellStyle(cellStyle);

	            // set the cell's value
	            String text = item.getText(i);
	            cell.setCellValue(text);
	        }
	    }

	    // autofit the columns
	    for (int i = 0; i < columns.length; i++) {
	    	sheet.autoSizeColumn((short) i);
	    }
	    
	    try {
            FileOutputStream fos = new FileOutputStream(filename);
            wb.write(fos);
            fos.close();
            wb.close();
            MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
                "Export Gene List Successful",
                "Workbook saved to the file:\n\n" + filename);
        } catch (IOException ioe) {
            String msg = ioe.getMessage();
            MessageDialog.openError(Display.getCurrent().getActiveShell(), 
                "Export Gene List Failed",
                "Could not save workbook to the file:\n\n" + msg);
        }
	}
	
	public static void main(String[] args) {
		try {
			List<GeneList> geneLists = ExcelFileHandler.readMasterGeneLists("/Users/sena/Desktop/2015_Human_Master_Gene_List.xlsx", "human");
			geneLists.addAll(ExcelFileHandler.readMasterGeneLists("/Users/sena/Desktop/2013_Mouse_Gene_List.xlsx", "mouse"));
			// save them back
			for (GeneList geneList : geneLists) {
				GeneUtils.cleanUpGenesForMasterGeneList(geneList);
	        	// save the merge report
		        ByteArrayOutputStream os = new ByteArrayOutputStream();
	            JAXBContext context = JAXBContext.newInstance(GeneList.class);
	            Marshaller marshaller = context.createMarshaller();
	            marshaller.setProperty(Marshaller.JAXB_ENCODING, PropertyHandler.GRITS_CHARACTER_ENCODING);
	            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	            marshaller.marshal(geneList, os);

	            //write the serialized data to the folder
	            FileWriter fileWriter = new FileWriter("/Users/sena/Desktop/" + geneList.getListName() + ".xml");
	            fileWriter.write(os.toString((String)marshaller.getProperty(Marshaller.JAXB_ENCODING)));
	            fileWriter.close();
	            os.close();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (PropertyException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

}
