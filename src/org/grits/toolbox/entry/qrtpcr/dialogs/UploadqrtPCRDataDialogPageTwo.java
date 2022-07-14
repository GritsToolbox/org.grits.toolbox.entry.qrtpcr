package org.grits.toolbox.entry.qrtpcr.dialogs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.grits.toolbox.entry.qrtpcr.model.CtHistory;
import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.GeneData;
import org.grits.toolbox.entry.qrtpcr.model.QrtPCRData;
import org.grits.toolbox.entry.qrtpcr.model.QrtPCRTable;
import org.grits.toolbox.entry.qrtpcr.model.Well;
import org.grits.toolbox.entry.qrtpcr.preference.QrtPCRPreferencePage;
import org.grits.toolbox.entry.qrtpcr.util.FileUtils;

public class UploadqrtPCRDataDialogPageTwo extends WizardPage {
	
	private static final Logger logger = Logger.getLogger(UploadqrtPCRDataDialogPageTwo.class);
	
	private Table table;
	
	//Combo combo;
	
	String[] plateIds = new String[] {"Plate 1"};
	
	QrtPCRTable qrtPCRTable;
	Entry sampleEntry = null;

	private Button reviewButton;
	

	protected UploadqrtPCRDataDialogPageTwo(String pageName) {
		super(pageName);
		qrtPCRTable = new QrtPCRTable();
	}

	@Override
	public void createControl(final Composite parent) {
		this.setTitle("Upload Data Files");
		this.setMessage("Please upload data files for each plate and review data");
		
		Composite content = new Composite(parent, SWT.NONE);
		
		setControl(content);
		content.setLayout(new GridLayout(4, false));
		
		table = new Table(content, SWT.BORDER | SWT.FULL_SELECTION);
		GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_table.horizontalSpan = 4;
		table.setLayoutData(gd_table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		TableColumn tblclmnPlateid = new TableColumn(table, SWT.NONE);
		tblclmnPlateid.setWidth(100);
		tblclmnPlateid.setText("PlateID");
		
		TableColumn tblclmnDataFile = new TableColumn(table, SWT.NONE);
		tblclmnDataFile.setWidth(328);
		tblclmnDataFile.setText("Data File");
		
		TableColumn tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setWidth(150);
		
		setPageComplete(false);
	}


	protected boolean processFile(String plateId, String filename, Shell shell) {
		if (filename != null && filename.endsWith(".asy"))
			return readAsyFile (plateId, filename, shell);
		else if (filename != null && (filename.endsWith(".xls") || filename.endsWith(".xlsx")))
			return readExcelFile(plateId, filename, shell);
		return false;
	}
	
	/**
	 * read instrument files in excel format adhering the following: 
	 * <li> sheets are named (case insensitive) by the plate id such as "Plate 1", "Plate 1_xxxx", "plate1", "Plate1_xxxx"
	 * <li> each sheet contains two columns
	 * <li> first column is the position (Well)
	 * <li> second column contains the value (double or "N/A" or "none)
	 * <p> 
	 * any value which is "N/A"/"none" or above the chosen THRESHOLD is assigned to the THRESHOLD value
	 * 
	 * @param plateId id of the plate that we are extracting the values for
	 * @param filename name of the excel file
	 * @param shell shell to display messages
	 * @return true if the file is read successfully, false otherwise
	 */
	private boolean readExcelFile(String plateId, String filename, Shell shell) {
		// first column is the well position, second column is the ct
		// any non-numeric value (N/A, no-ct etc.) for ct column is assumed "null" and set to THRESHOLD_PREFERENCE
		try {
			String fileFolder = FileUtils.getFileFolder(sampleEntry);
			FileInputStream fileReader = new FileInputStream(new File(fileFolder + File.separator + filename));
			Workbook exampleWb = new XSSFWorkbook(fileReader);
			qrtPCRTable = ((UploadqrtPCRDataDialogPageOne)getWizard().getPreviousPage(this)).getTable();
			QrtPCRData data = qrtPCRTable.getData(plateId);
			qrtPCRTable.addInstrumentFile(plateId, filename);
			boolean sheetFound = false;
			for (int i = 0; i < exampleWb.getNumberOfSheets(); i++) {
				Sheet sheet = exampleWb.getSheetAt(i);
				Double ct0=null;
				Well pos=null;
				if (sheet.getSheetName().toLowerCase().startsWith(plateId.toLowerCase()) ||
					sheet.getSheetName().toLowerCase().startsWith(plateId.toLowerCase().replace(" ", ""))) { // correct sheet
					sheetFound = true;
					// first column Well
					// second column ct value
					for (Row row : sheet) {
						if (row.getRowNum() == 0)
							// header line
							continue;
						for (int columnNumber = 0; columnNumber < 2; columnNumber++) {
							Cell cell = row.getCell(columnNumber);
							if (cell == null || cell.getCellType() == CellType.BLANK) {	
								//cellEmptyError(sheet, row, columnNumber, passNumber);
								continue;
							} else if (cell.getCellType() == CellType.STRING && cell.getRichStringCellValue().equals("")) {
						    	//cellEmptyError(sheet, row, columnNumber, passNumber);
						    	continue;
							} else {
								if (cell.getColumnIndex() == 0) {  // Well column
									if (cell.getCellType() == CellType.STRING) {
										String name = cell.getRichStringCellValue().getString();
										if (!name.equals("")) {
											pos = new Well();
											pos.setX(name.trim().charAt(0));
											pos.setY(Integer.parseInt(name.trim().substring(1)));
										}
									}
								}
								if (cell.getColumnIndex() == 1) {  // Ct column
									if (cell.getCellType() == CellType.STRING) {
										String name = cell.getRichStringCellValue().getString();
										if (!name.equals("") && (name.equalsIgnoreCase("N/A") || name.equalsIgnoreCase("none"))) {
											// no value
											ct0 = null;
										} else if (!name.equals("")) {
											// parse the value as double
											try {
												ct0 = Double.parseDouble(name);
											} catch (NumberFormatException e1) {
												ct0 = null;
											}
										}
									} else if (cell.getCellType() == CellType.NUMERIC) {
										ct0 = cell.getNumericCellValue();
									}
								}
							}
						}
						
						GeneData geneData = findGeneDataForWell(pos, data);
						if (geneData == null) {
							// Error!
							logger.info("Error locating the genedata for position: " + pos + " Position must be empty!");
						}
						else {
							if (ct0 == null) {
								geneData.addPreviousValue(null, CtHistory.Reason.NULL.getReason());
								geneData.setCt(Double.valueOf(QrtPCRPreferencePage.THRESHOLD_PREFERENCE));
							} else if (ct0 > QrtPCRPreferencePage.THRESHOLD_PREFERENCE) {
								geneData.addPreviousValue(ct0, CtHistory.Reason.ABOVETHRESHOLD.getReason());
								geneData.setCt(Double.valueOf(QrtPCRPreferencePage.THRESHOLD_PREFERENCE));
							} else 
								geneData.setCt(ct0);
							geneData.setPlateId(plateId);
							geneData.setPosition(pos);
						}
						
						ct0 = null;
						pos = null;
					}
					break;
				}
			}
			
			if (!sheetFound) {
				logger.error("Cannot find matching sheet for plateid: " + plateId);
				MessageDialog.openError(shell, "Error", "Cannot find matching sheet for plateid: " + plateId);
				fileReader.close();
				exampleWb.close();
				return false;
			} 
			
			if (filename.indexOf(".") != -1) {
				String name = filename.substring(0, filename.indexOf("."));
				data.setName(name);
			} else {
				data.setName(filename);
			}
			
			// calculate norm value and scaler
			data.calculateNormValue();
			
			fileReader.close();
			exampleWb.close();
			return true;
		} catch (IOException e) {
			logger.error("Error reading the file: " + filename, e);
			MessageDialog.openError(shell, "Error", "Error reading the file: " + filename + ". Reason: " + e.getMessage());
		} 
		return false;
	}

	/**
	 * Read .asy files generated from the qrtpcr instrument
	 * 
	 * @param plateId id of the plate that we are extracting the values for
	 * @param filename name of the excel file
	 * @param shell shell to display messages
	 * @return true if the file is read successfully, false otherwise
	 */
	private boolean readAsyFile(String plateId, String filename, Shell shell) {
		try {
			String fileFolder = FileUtils.getFileFolder(sampleEntry);
			FileInputStream file = new FileInputStream(new File(fileFolder + File.separator + filename));
			BufferedReader lines = new BufferedReader(new InputStreamReader(file));
			String line;
			qrtPCRTable = ((UploadqrtPCRDataDialogPageOne)getWizard().getPreviousPage(this)).getTable();
			QrtPCRData data = qrtPCRTable.getData(plateId);
			qrtPCRTable.addInstrumentFile(plateId, filename);
			Double ct0=null;
			Double ctMean0=null;
			Double ctDev0=null;
			Well pos=null;
			String name= null;
			String createdDate = null;
			boolean ctFound = false;
			while ((line=lines.readLine())!=null) {
				if (line.startsWith("Name=")) {
					name = line.substring(line.indexOf("Name=") + 5).trim();
				}
				if (line.startsWith("CREATEDDATE=")) {
					createdDate = line.substring(line.indexOf("=")+1).trim();
				}
				
				if (line.startsWith("RUN_DYE")) {
					String dye = line.substring(line.indexOf("=") + 1).trim();
					if (dye.length() > 0)
						data.addDye(dye);
				}
				if (line.startsWith("CT0=")) {  
					ctFound=true;
					String ct0String = line.substring(line.indexOf("CT0=") + 4);
					if (!ct0String.trim().isEmpty()) 
						ct0 = Double.parseDouble(ct0String);
				}
				if (line.startsWith("CTMEAN0=")) {
					String ctMean0String = line.substring(line.indexOf("CTMEAN0=") + 8);
					if (ctMean0String.trim().isEmpty()) 
						ctMean0 = 35.0;
					else
						ctMean0 = Double.parseDouble(ctMean0String);
				}
				if (line.startsWith("CTDEV0=")) {
					String ctDev0String = line.substring(line.indexOf("CTDEV0=") + 7);
					if (ctDev0String.trim().isEmpty()) 
						ctDev0 = 0.0;
					else
						ctDev0 = Double.parseDouble(ctDev0String);
				}
				if (line.startsWith("POS=")) {
					pos = new Well();
					pos.setX(line.substring(line.indexOf("=")+ 1).charAt(0));
					pos.setY(Integer.parseInt(line.substring(line.indexOf("=") + 2)));
				}
				
				if (line.contains("<<Kinetic Data>>")) {
					// exit
					break;
				}
					
				if (ctFound && ctMean0 != null && ctDev0 != null && pos != null) {
					GeneData geneData = findGeneDataForWell(pos, data);
					if (geneData == null) {
						// Error!
						logger.info("Error locating the genedata for position: " + pos + " Position must be empty!");
					}
					else {
						if (ct0 == null) {
							geneData.addPreviousValue(null, CtHistory.Reason.NULL.getReason());
							geneData.setCt(Double.valueOf(QrtPCRPreferencePage.THRESHOLD_PREFERENCE));
						} else if (ct0 > QrtPCRPreferencePage.THRESHOLD_PREFERENCE) {
							geneData.addPreviousValue(ct0, CtHistory.Reason.ABOVETHRESHOLD.getReason());
							geneData.setCt(Double.valueOf(QrtPCRPreferencePage.THRESHOLD_PREFERENCE));
						} else 
							geneData.setCt(ct0);
						geneData.setCtDev(ctDev0.doubleValue());
						geneData.setCtMean(ctMean0.doubleValue());
						geneData.setPlateId(plateId);
						geneData.setPosition(pos);
					}
					
					ct0 = null;
					ctMean0 = null;
					ctDev0 = null;
					ctFound = false;
					pos = null;
				} 
			}
			
			if (name != null && name.length() > 0)
				data.setName(name);
			if (createdDate != null && createdDate.length() > 0) {
				try {
					data.setDateCreated(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).parse(createdDate));
				} catch (ParseException e) {
					logger.warn("Could not parse the date from the file", e);
				}
			}
			
			// calculate norm value and scaler
			data.calculateNormValue();
			
			lines.close();
			file.close();
		} catch (IOException e) {
			logger.error ("Error processing raw instrument file. ", e);
			ErrorUtils.createErrorMessageBox(shell, "Error processing raw instrument file.", e);
			return false;
		} catch (NumberFormatException e) {
			logger.error ("Error processing raw instrument file. ", e);
			ErrorUtils.createErrorMessageBox(shell, "Error processing raw instrument file.", e);
			return false;
		}
		return true;
	}
	
	GeneData findGeneDataForWell (Well pos, QrtPCRData data) {
		List<Gene> genes = data.getGenes();
		if (genes != null) {
			for (Gene gene : genes) {
				List<GeneData> geneDataList = gene.getDataMap().get(0); 
				for (GeneData geneData : geneDataList) {
					if (geneData.getPosition().equals(pos))
						return geneData;
				}
			}
		}
		return null;
	}

	protected void addTableRow(final Shell parent, final String plateId) {
		// check previous items, do not allow addition of duplicate items
		TableItem[] previuousItems = table.getItems();
		for (TableItem tableItem : previuousItems) {
			if (tableItem.getText(0) != null && tableItem.getText(0).equals(plateId)) {
				return;
			}
		}
		final TableItem item = new TableItem(table, SWT.READ_ONLY);
		item.setText(0, plateId);
		
		Button browseButton = new Button(table, SWT.PUSH);
		browseButton.setText("...");
		browseButton.pack();
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// open up a file browser
				FileDialog dialog = new FileDialog(parent.getShell(), SWT.OPEN | SWT.MULTI);
				String filename = dialog.open();
				if (filename != null) {
					String [] otherFiles = dialog.getFileNames();
					try {
						sampleEntry = ((UploadDataWizard)getWizard()).getSampleEntry();
						filename = FileUtils.copyFileIntoWorkspace(sampleEntry, filename);
						// check if the filename is valid and load the data from the file
						if (processFile (plateId, filename, parent.getShell()) ) {
							item.setText(1, filename);
							if (otherFiles.length == table.getItemCount()) {
								TableItem[] previuousItems = table.getItems();
								int i=1;
								for (TableItem tableItem : previuousItems) {
									if (!tableItem.equals(item)) {
										String path = otherFiles[i];
										if (!path.contains(File.separator))
											path = dialog.getFilterPath() + File.separator + path;
										String other = FileUtils.copyFileIntoWorkspace(sampleEntry, path);
										if (processFile (tableItem.getText(0), other, parent.getShell()) ) {
											tableItem.setText(1, other);
										}
										i++;
									}
								}
							}
						}
						boolean finished = true;
						TableItem[] previuousItems = table.getItems();
						for (TableItem tableItem : previuousItems) {
							if (tableItem.getText(1) == null ) {
								finished = false;
							}
						}
						if (finished) {
							// all plates are assigned raw data
							setPageComplete(true);
						}
						
					} catch (IOException e1) {
						logger.error ("Error processing raw instrument file. ", e1);
						ErrorUtils.createErrorMessageBox(parent.getShell(), "Error processing raw instrument file.", e1);
					}	
				}
			}
		});
		
			
		TableEditor editor = new TableEditor (table);
		
		reviewButton = new Button (table, SWT.PUSH);
		reviewButton.setText("Review Layout");
		reviewButton.pack();
		//reviewButton.setEnabled(false);
		reviewButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (qrtPCRTable.getData(plateId) == null) 
					return;
				DataReviewDialog dialog = new DataReviewDialog(parent);
				dialog.setData(qrtPCRTable.getData(plateId));
				dialog.setLowerThreshold(((UploadDataWizard)getWizard()).getThresholdSetting());
				dialog.setStDevCutOff(((UploadDataWizard)getWizard()).getCutOffSetting());
				if (dialog.open() == Window.OK) {
					if (dialog.isChanged()) {
						QrtPCRData newData = dialog.getData();
						qrtPCRTable.remove(plateId);
						qrtPCRTable.addData(plateId, newData);
					}
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		

		editor.minimumWidth = reviewButton.getSize ().x;
		editor.horizontalAlignment = SWT.CENTER;
		editor.setEditor(reviewButton, item, 2);
		
		TableEditor editor2 = new TableEditor (table);
		editor2.minimumWidth = browseButton.getSize().x;
		editor2.horizontalAlignment = SWT.RIGHT;
		editor2.setEditor(browseButton, item, 1);
	}

	public void setPlateIds(String[] plateIds) {
		if (plateIds != null && plateIds.length > 0) {
			for (String plateId : plateIds) {
				addTableRow(this.getShell(), plateId);
			}
		}
	}

	public QrtPCRTable getQrtPCRTable() {
		return qrtPCRTable;
	}
}
