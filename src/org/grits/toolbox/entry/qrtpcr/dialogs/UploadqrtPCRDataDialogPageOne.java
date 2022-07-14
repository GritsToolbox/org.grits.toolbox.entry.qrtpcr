package org.grits.toolbox.entry.qrtpcr.dialogs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.GeneData;
import org.grits.toolbox.entry.qrtpcr.model.Plate;
import org.grits.toolbox.entry.qrtpcr.model.PlateLayout;
import org.grits.toolbox.entry.qrtpcr.model.QrtPCRData;
import org.grits.toolbox.entry.qrtpcr.model.QrtPCRTable;
import org.grits.toolbox.entry.qrtpcr.model.Well;
import org.grits.toolbox.entry.qrtpcr.util.FileUtils;
import org.grits.toolbox.entry.qrtpcr.util.GeneUtils;


public class UploadqrtPCRDataDialogPageOne extends WizardPage {
	

	private static final Logger logger = Logger.getLogger(UploadqrtPCRDataDialogPageOne.class);
	
	protected UploadqrtPCRDataDialogPageOne(String pageName, boolean forRerun) {
		super(pageName);
		this.forRerun = forRerun;
	}

	Entry sampleEntry=null;
	boolean forRerun = false;
	private Text plateLayout=null;
	private Combo controlGeneCombo = null;
	boolean replicatesValid = true;
	
	String[] plateIds;
	QrtPCRTable table;
	
//	GeneList masterGeneList;
	Text selectedDescription;
	
	PlateLayout layout = null;

	@Override
	public void createControl(final Composite parent) {
		
		this.setTitle("Select a plate layout and upload files");
		this.setMessage("Please select a plate layout");
		final Composite content = new Composite(parent, SWT.NONE);

		content.setLayout(new GridLayout(3, false));
		new Label(content, SWT.NONE);
		new Label(content, SWT.NONE);
		new Label(content, SWT.NONE);
		
		Label lblPlatelayoutFile = new Label(content, SWT.NONE);
		lblPlatelayoutFile.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblPlatelayoutFile.setText("PlateLayout File");
		
		plateLayout = new Text(content, SWT.BORDER);
		GridData gd_plateLayout = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		plateLayout.setLayoutData(gd_plateLayout);
		plateLayout.setEditable(false);
		
		Button btnNewButton_1 = new Button(content, SWT.NONE);
		btnNewButton_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// open existing
				List<PlateLayout> input = new ArrayList<>();
				try {
					input = FileUtils.getAllPlateLayouts();
				} catch (IOException ex) {
					Logger.getLogger(FileUtils.class).error ("Error loading existing plate layouts", ex);
					MessageDialog.openError(parent.getShell(), "Error", "Error loading existing plate layouts");
				}
				CustomSelectionDialogWithDescription dialog = new CustomSelectionDialogWithDescription(Display.getCurrent().getActiveShell(), false, null, input);
				dialog.initializeGeneListSelectionDialog();
				
				if (dialog.open() == org.eclipse.jface.window.Window.OK) {
					layout = (PlateLayout)dialog.getSelection();
					try {
						if (forRerun)
							sampleEntry = ((UploadDataWizard)getWizard()).getSampleEntry();
						String filename = FileUtils.getPlateLayoutFolder() + File.separator + layout.getFilename();
						String newFilename = FileUtils.copyFileIntoWorkspace(sampleEntry, filename);
						plateLayout.setText(newFilename);
						if (processFiles (layout, newFilename, parent.getShell())) {
							setPageComplete(true);
						}
					} catch (IOException e1) {
						logger.error ("Cannot copy the file into the workspace", e1);
						ErrorUtils.createErrorMessageBox(getShell(), "Cannot copy the file into the workspace",e1);
						return;
					}
				}
				else
					setPageComplete(false);
			}
		});
		btnNewButton_1.setText("Browse");
		//controlGeneCombo.setEnabled(false);
		
		setControl(content);
		new Label(content, SWT.NONE);
		
		controlGeneCombo = new Combo(content, SWT.NONE);
		controlGeneCombo.setItems(new String[] {"<Select Control>"});
		controlGeneCombo.select(0);
		new Label(content, SWT.NONE);
		setPageComplete(false);
	}
	
	protected List<String> findCommonGenes() {
		Map <String, QrtPCRData> dataMap = this.table.getPlateDataMap();
		
		List<String> commonGenes = new ArrayList<>();
		for (QrtPCRData data : dataMap.values()) {
			List<Gene> genes = data.getGenes();
			for (Gene gene : genes) {
				if (gene.getIsCommon())
					if (!commonGenes.contains(gene.getGeneIdentifier()))
						commonGenes.add(gene.getGeneIdentifier());
			}
		}
						
		return commonGenes;
	}
	
	
	private void setupCommonGeneCombo () {
		// find the common genes in all plates to help user select the control gene
		int defaultSelection = -1;
		List<String> common = findCommonGenes();
		if (common.contains(QrtPCRTable.defaultControlGene)) // select this by default
			defaultSelection = common.indexOf(QrtPCRTable.defaultControlGene);
		controlGeneCombo.setEnabled(true);
		controlGeneCombo.setItems(common.toArray(new String[common.size()]));
		controlGeneCombo.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				setControlGene(controlGeneCombo.getText());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				setControlGene(controlGeneCombo.getText());
			}
		});
		if (defaultSelection != -1)  {
			controlGeneCombo.select(defaultSelection);
			setControlGene(controlGeneCombo.getText());
		}
	}
	
	private void setControlGene(String geneIdentifier) {
		Map <String, QrtPCRData> dataMap = this.table.getPlateDataMap();
		for (QrtPCRData data : dataMap.values()) {
			List<Gene> genes = data.getGenes();
			for (Gene gene : genes) {
				if (gene.getGeneIdentifier().equals(geneIdentifier)) {
					gene.setIsControl(true);
					break;
				}
			}
		}
	}

	@Override
	public IWizardPage getNextPage() {
		UploadqrtPCRDataDialogPageTwo pageTwo = (UploadqrtPCRDataDialogPageTwo) getWizard().getPage("Data file");
		pageTwo.setPlateIds(plateIds);
		return pageTwo;
	}
	
	private boolean processFiles(PlateLayout layout, String plateLayoutFileName, Shell shell) {
		table = new QrtPCRTable();
		table.setPlateLayoutFile(plateLayoutFileName);
		plateIds = new String[layout.getPlates().size()];  // to be used in the second page
		int i=0;
		for (Plate plate : layout.getPlates()) {
			String plateId = plate.getPlateId();
			plateIds[i++] = plateId;
			QrtPCRData data = new QrtPCRData();
			List<Gene> geneList = new ArrayList<>();
			data.setNumberOfReplicates(layout.getNumberOfReplicates());
			for (Well well: plate.getPlateMap().keySet()) {
				Gene gene = plate.getPlateMap().get(well);
				Gene existing = geneExistsInList(gene, geneList);
				if (existing == null) {
					Gene geneForGeneList = GeneUtils.makeACopy(gene);
					geneList.add(geneForGeneList);
					GeneData geneData = new GeneData();
					geneData.setGene(geneForGeneList);
					geneData.setPlateId(plateId);
					geneData.setPosition(well);
					List<GeneData> geneDataList = new ArrayList<>();
					geneDataList.add (geneData);
					geneForGeneList.addGeneDataList(geneDataList);
					geneForGeneList.setNumOfReplicates(1);
				} else {
					List<GeneData> geneDataList = existing.getDataMap().get(0);
					GeneData geneData = new GeneData();
					geneData.setGene(existing);
					geneData.setPlateId(plateId);
					geneData.setPosition(well);
					geneDataList.add (geneData);
					existing.setNumOfReplicates(geneDataList.size());
				}
			}
			// sort the geneList before setting it
			Collections.sort(geneList);
			data.setGenes(geneList);
			table.addData(plateId, data);
		}
			
		setupCommonGeneCombo();
		return true;
	}
	
	Gene geneExistsInList (Gene gene, List<Gene> list) {
		for (Gene existing : list) {
			if (existing.getGeneIdentifier().equals(gene.getGeneIdentifier()))
				return existing;
		}
		return null;
	}
	
	public String getMasterListFile() {
		return this.layout.getInputList().getFilename();
	}
	
	public String getPlateLayout() {
		return plateLayout.getText();
	}

	public String[] getPlateIds() {
		return plateIds;
	}

	public QrtPCRTable getTable() {
		return table;
	}

	public void setTable(QrtPCRTable table) {
		this.table = table;
	}

	public void setSampleEntry(Entry parentEntry) {
		sampleEntry = parentEntry;
	}
}
