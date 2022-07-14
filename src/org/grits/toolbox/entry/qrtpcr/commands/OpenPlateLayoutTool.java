package org.grits.toolbox.entry.qrtpcr.commands;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.entry.qrtpcr.dialogs.CustomSelectionDialogWithDescription;
import org.grits.toolbox.entry.qrtpcr.dialogs.PlateLayoutToolWizard;
import org.grits.toolbox.entry.qrtpcr.model.PlateLayout;
import org.grits.toolbox.entry.qrtpcr.util.FileUtils;

public class OpenPlateLayoutTool {

	PlateLayoutToolWizard wizard = null;
	
	@Execute
	public Object execute(@Named (IServiceConstants.ACTIVE_SHELL) Shell shell) throws ExecutionException {
		
		String[] dialogButtonLabels = new String[] { IDialogConstants.YES_LABEL,
                "Open Existing Layout", 
                IDialogConstants.CANCEL_LABEL};
		MessageDialog questionWithViewOption = new MessageDialog (shell, "Plate Layout Tool",
	            null, "Do you want to create a new one? Click \"Open Existing\" to modify an existing layout!", MessageDialog.QUESTION_WITH_CANCEL,
	            dialogButtonLabels, 0);
		
		int returnCode = questionWithViewOption.open();
		if (returnCode == 0) { // YES
			wizard = new PlateLayoutToolWizard();
		}
		else if (returnCode == 1) { 
			// open existing
			List<PlateLayout> input = new ArrayList<>();
			try {
				input = FileUtils.getAllPlateLayouts();
			} catch (IOException e) {
				Logger.getLogger(FileUtils.class).error ("Error loading existing plate layouts", e);
				MessageDialog.openError(shell, "Error", "Error loading existing plate layouts");
			}
			CustomSelectionDialogWithDescription dialog = new CustomSelectionDialogWithDescription(Display.getCurrent().getActiveShell(), false, null, input);
			dialog.initializeGeneListSelectionDialog();
			if (dialog.open() == Window.OK) {
				PlateLayout plateLayout = (PlateLayout)dialog.getSelection();
				wizard = new PlateLayoutToolWizard(plateLayout);
			}
		}
		else if (returnCode == 2) { // NO/Cancel
			questionWithViewOption.close();
		}
		
		if (wizard != null) {
			WizardDialog wizardDialog = new WizardDialog(shell,
			      wizard) {
				@Override
				protected void createButtonsForButtonBar(
						Composite parent) {
					super.createButtonsForButtonBar(parent);
					Button exportButton = createButton(parent, 30, "Export", false);
					exportButton.moveAbove(null);
					
					exportButton.addSelectionListener(new SelectionListener() {
						
						@Override
						public void widgetSelected(SelectionEvent e) {
							if (wizard.getLayout() == null || wizard.getLayout().getPlates() == null) {
								// not ready to export
								MessageDialog.openError(shell, "Not Ready", "Plate layout is not ready to be exported. Make sure you prepare the plates first!");
								return;
							}
							XSSFWorkbook  workbook = wizard.createWorkbookFromTable();
							FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
							// Set the text
			                fileDialog.setText("Select File");
			                // Set filter on .xls files
			                fileDialog.setFilterExtensions(new String[] { "*.xlsx" });
			                // Put in a readable name for the filter
			                fileDialog.setFilterNames(new String[] { "Excel (*.xlsx)" });
			                fileDialog.setFileName(wizard.getLayout().getName()+ ".xlsx");
			                fileDialog.setOverwrite(true);
			                // Open Dialog and save result of selection
			                String selected = fileDialog.open();
			                if (selected != null) {
			                	// 3 - export data into excel
			                	try {
			                        FileOutputStream fos = new FileOutputStream(selected);
			                        workbook.write(fos);
			                        fos.close();
			                        MessageDialog.openInformation(shell,
			                            "Save Workbook Successful",
			                            "Workbook saved to the file:\n\n" + selected);
			                    } catch (IOException ioe) {
			                        String msg = ioe.getMessage();
			                        MessageDialog.openError(shell, 
			                            "Save Workbook Failed",
			                            "Could not save workbook to the file:\n\n" + msg);
			                    }
			                }
						}
						
						@Override
						public void widgetDefaultSelected(SelectionEvent e) {
						}
					});
					
				}
			};
			wizardDialog.open();
		}
		
		return null;
	}

}
