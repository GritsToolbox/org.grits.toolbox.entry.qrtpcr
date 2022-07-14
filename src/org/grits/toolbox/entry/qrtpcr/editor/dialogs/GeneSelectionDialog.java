package org.grits.toolbox.entry.qrtpcr.editor.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.entry.qrtpcr.dialogs.CustomSelectionDialogWithDescription;
import org.grits.toolbox.entry.qrtpcr.editor.MergeReportEditor;
import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.GeneList;
import org.grits.toolbox.entry.qrtpcr.util.FileUtils;

public class GeneSelectionDialog extends TitleAreaDialog {

	MergeReportEditor editor;
	private Text pathwayText;
	private Button btnBrowse;
	private Button btnSelectBasedOn;
	protected static Logger logger = Logger.getLogger(GeneSelectionDialog.class);
	
	public GeneSelectionDialog(Shell parentShell, MergeReportEditor mergeReportEditor) {
		super(parentShell);
		this.editor = mergeReportEditor;
	}
	
	@Override
	public void create() {
        super.create();
        setTitle("Gene Selection");
        setMessage("Please choose one of the following");
    }
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite control = (Composite) super.createDialogArea(parent);
		control.setLayout(new GridLayout(3, false));
		new Label(control, SWT.NONE);
		new Label(control, SWT.NONE);
		
		final Button btnSelectAll = new Button(control, SWT.RADIO);
		btnSelectAll.setText("Select All");
		btnSelectAll.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (btnSelectAll.getSelection()) {
					btnSelectBasedOn.setSelection(false);
					editor.selectAll();
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		new Label(control, SWT.NONE);
		new Label(control, SWT.NONE);
		
		btnSelectBasedOn = new Button(control, SWT.RADIO);
		btnSelectBasedOn.setText("Select based on a Pathway");
		btnSelectBasedOn.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (btnSelectBasedOn.getSelection()) {
					btnBrowse.setSelection(true);
					btnSelectAll.setSelection(false);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		
		pathwayText = new Text(control, SWT.BORDER);
		pathwayText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		btnBrowse = new Button(control, SWT.NONE);
		btnBrowse.setText("Browse");
		btnBrowse.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				btnSelectBasedOn.setSelection(true);
				btnSelectAll.setSelection(false);
				editor.clearSelection();
				List<GeneList> input = new ArrayList<GeneList>(); 
				try {
					input = FileUtils.getAllGeneLists(true);
				} catch (Exception ex) {
					logger .warn (" Cannot load the gene lists! ", ex);
				}
				CustomSelectionDialogWithDescription dialog = new CustomSelectionDialogWithDescription(Display.getCurrent().getActiveShell(), true, pathwayText, input);
				dialog.initializeGeneListSelectionDialog();
				if (dialog.open() == Window.OK) {
					GeneList inputList = (GeneList)dialog.getSelection();
					if (inputList != null && inputList.getGenes() != null) {
						editor.setGeneList(inputList);
						for (Gene gene : inputList.getGenes()) {
							editor.selectGene(gene);
						}
					}
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				btnSelectBasedOn.setSelection(true);
				List<GeneList> input = new ArrayList<GeneList>(); 
				try {
					input = FileUtils.getAllGeneLists(true);
				} catch (Exception ex) {
					logger .warn (" Cannot load the gene lists! ", ex);
				}
				CustomSelectionDialogWithDescription dialog = new CustomSelectionDialogWithDescription(Display.getCurrent().getActiveShell(), true, pathwayText, input);
				dialog.initializeGeneListSelectionDialog();
				if (dialog.open() == Window.OK) {
					GeneList inputList = (GeneList)dialog.getSelection();
					if (inputList != null && inputList.getGenes() != null) {
						editor.setGeneList(inputList);
						for (Gene gene : inputList.getGenes()) {
							editor.selectGene(gene);
						}
					}
				}
			}
		});
		
		return control;
	}

	protected boolean checkInList(String geneIdentifier, GeneList inputList) {
		for (Gene gene : inputList.getGenes()) {
			if (gene.getGeneIdentifier().equals(geneIdentifier))
				return true;
		}
		return false;
	}

}
