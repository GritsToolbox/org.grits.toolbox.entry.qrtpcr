package org.grits.toolbox.entry.qrtpcr.dialogs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.nebula.jface.gridviewer.GridTableViewer;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.widgets.Display;
import org.grits.toolbox.entry.qrtpcr.model.Plate;
import org.grits.toolbox.entry.qrtpcr.model.PlateLayout;
import org.grits.toolbox.entry.qrtpcr.util.FileUtils;

public class PlateLayoutToolWizard extends Wizard {
	private static Logger logger = Logger.getLogger(PlateLayoutToolWizard.class);
	
	private PlateLayoutToolInfoPage page;
	
	PlateLayout layout;
	boolean existing=false;

	public PlateLayoutToolWizard (PlateLayout layout) {
		this.layout = layout;
	}
	
	public PlateLayoutToolWizard() {
	}

	@Override
	public String getWindowTitle() {
		return "Plate Layout Tool";
	}
	
	public PlateLayout getLayout() {
		return layout;
	}
	
	@Override
	public void addPages() {
		if (this.layout != null) { // opening an existing layout
			existing = true;
			page = new PlateLayoutToolInfoPage("info page", layout);
			addPage(page);
			if (this.layout.getPlates() != null) {
				// the plates are already created, need to add pages here
				int i=1;
				for (Plate plate : this.layout.getPlates()) {
					PlateLayoutDialog platePage = new PlateLayoutDialog("Plate Page " + i, plate.getPlateId(), null);
					platePage.setLayout(layout);
					platePage.setPlate(plate);
					addPage(platePage);
					i++;
				}
			} else {
				this.setForcePreviousAndNextButtons(true);
			}
		}
		else {
			this.setForcePreviousAndNextButtons(true);
			page = new PlateLayoutToolInfoPage("info page");
			addPage(page);
		}
	}

	@Override
	public boolean performFinish() {
		layout = page.getLayout();
		
		if (!existing) { // check for duplicates
			String name = layout.getName();
			if (FileUtils.getAllPlateLayoutNames().contains(name)) {
				MessageDialog.openError(Display.getCurrent().getActiveShell(), "Duplicate", "A plate layout with this name already exists. Please choose a different one");
				logger.warn ("A plate layout with this name already exists. Please choose a different one");
				return false;
			}
		}
		IWizardPage[] pages = this.getPages();
		List<Plate> plates = new ArrayList<>();
		for (IWizardPage iWizardPage : pages) {
			// for each page, create a Plate object to be added into the layout
			if (iWizardPage instanceof PlateLayoutDialog) {
				Plate plate = ((PlateLayoutDialog)iWizardPage).convertInputDataIntoPlate();
				plates.add(plate);
			}
		}
		layout.setPlates(plates);
		try {
			FileUtils.savePlateLayoutFile(layout);
		} catch (JAXBException | IOException e) {
			logger.error("Error saving plate layout", e);
			MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error in Save", "Error saving plate layout. Reason: " + e.getMessage());
			return false;
		}
		return true;
	}
	
	@Override
	public boolean canFinish() {
		IWizardPage[] pages = this.getPages();
		boolean finish = true;
		for (IWizardPage iWizardPage : pages) {
			finish &= iWizardPage.isPageComplete();
		}
		return finish;
	}
	
	public XSSFWorkbook createWorkbookFromTable() {
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
        
		IWizardPage[] pages = this.getPages();
		for (IWizardPage iWizardPage : pages) {
			// for each page, create a Plate object to be added into the layout
			if (iWizardPage instanceof PlateLayoutDialog) {
				GridTableViewer tableViewer = ((PlateLayoutDialog)iWizardPage).getTableViewer();
			    // add a worksheet
			    XSSFSheet sheet = wb.createSheet(iWizardPage.getTitle());
			    if (tableViewer == null) // that plate is not ready yet!
			    	continue;
		
			    // add header row
			    Grid table = tableViewer.getGrid();
			    GridColumn[] columns = table.getColumns();
			    int rowIndex = 0;
			    int cellIndex = 1;
			    XSSFRow header = sheet.createRow((short) rowIndex++);
			    XSSFCell cell = header.createCell(0);
			    cell.setCellValue("");
			    for (GridColumn column : columns) {
			        cell = header.createCell(cellIndex++);
			        cell.setCellValue(column.getText());
			        cell.setCellStyle(headerStyle);
			    }
		
			    Character letter = 'A';
			    // add data rows
			    GridItem[] items = tableViewer.getGrid().getItems();
			    for (GridItem item : items) {
			        // create a new row
			        XSSFRow row = sheet.createRow((short) rowIndex++);
			        cellIndex = 0;
			        // row header
			        cell = row.createCell(cellIndex++);
			        cell.setCellValue(String.valueOf(letter));
			        cell.setCellStyle(headerStyle);
			        letter++;
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
			}
		}
	    return wb;
	}

}
