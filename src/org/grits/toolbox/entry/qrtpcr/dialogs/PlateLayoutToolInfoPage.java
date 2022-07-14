package org.grits.toolbox.entry.qrtpcr.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.GeneList;
import org.grits.toolbox.entry.qrtpcr.model.PlateLayout;
import org.grits.toolbox.entry.qrtpcr.model.Size;
import org.grits.toolbox.entry.qrtpcr.model.Well;
import org.grits.toolbox.entry.qrtpcr.util.FileUtils;

public class PlateLayoutToolInfoPage extends WizardPage implements ModifyListener {
	private static class ContentProvider implements IStructuredContentProvider {
		@SuppressWarnings("unchecked")
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof HashMap<?, ?>) {
				Set<Entry<Gene, Well>> entrySet = ((HashMap<Gene, Well>) inputElement).entrySet();
				Object[] array = new Object[entrySet.size()];
				int i=0;
				for (Map.Entry<Gene, Well> entry : entrySet) {
					array[i++] = entry;
				}
				return array;
			}
			return new Object[0];
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
	private static Logger logger = Logger.getLogger(PlateLayoutToolInfoPage.class);
	
	private Text inputGeneList;
	private Text replicatesText;
	private Text plateWidth;
	private Text plateHeight;
	private Text layoutNameText;
	private Text descriptionText;
	
	GeneList inputList;
	
	Map<Gene, Well> controlGeneLocations = new HashMap<>();
	PlateLayout layout;
	private Table table;
	protected Well[] defaultGeneLocations;
	private TableViewer controlTableViewer;
	private Button btnAutoGeneratePlate;

	private Button btnBrowse;
	
	/**
	 * @wbp.parser.constructor
	 */
	protected PlateLayoutToolInfoPage(String pageName) {
		super(pageName);
		
		layout = new PlateLayout();
		layout.setSize(new Size(12, 8));
		layout.setNumberOfReplicates(3);
		setPageComplete(false);
		defaultGeneLocations = new Well[] {new Well('A', 7), new Well('H', 7), new Well('H', 10)};
	}
	
	protected PlateLayoutToolInfoPage(String pageName, PlateLayout layout) {
		super(pageName);
		this.layout = layout;
		this.inputList = layout.getInputList();
		this.controlGeneLocations = layout.getControlGeneLocations();
		setPageComplete(false);
		defaultGeneLocations = new Well[] {new Well('A', 7), new Well('H', 7), new Well('H', 10)};
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite (parent, SWT.NONE);
		setControl(composite);
		composite.setLayout(new GridLayout(4, false));
		
		setMessage("Enter information about the layout");
		
		Label lblLayoutName = new Label(composite, SWT.NONE);
		lblLayoutName.setText("Plate Layout Name");
		new Label(composite, SWT.NONE);
		
		layoutNameText = new Text(composite, SWT.BORDER);
		layoutNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(composite, SWT.NONE);
		if (layout.getName() != null)
			layoutNameText.setText(layout.getName());
		layoutNameText.addModifyListener(this);
		
		
		Label lblDescription = new Label(composite, SWT.NONE);
		lblDescription.setText("Description");
		new Label(composite, SWT.NONE);
		
		descriptionText = new Text(composite, SWT.BORDER);
		GridData gd_descriptionText = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_descriptionText.heightHint = 30;
		descriptionText.setLayoutData(gd_descriptionText);
		new Label(composite, SWT.NONE);
		if (layout.getDescription() != null)
			descriptionText.setText(layout.getDescription());
		descriptionText.addModifyListener(this);
		
		Label lblInputGeneList = new Label(composite, SWT.NONE);
		lblInputGeneList.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblInputGeneList.setText("Input Gene List");
		new Label(composite, SWT.NONE);
		
		inputGeneList = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
		inputGeneList.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		btnBrowse = new Button(composite, SWT.NONE);
		btnBrowse.setText("Browse");
		btnBrowse.addSelectionListener(new SelectionListener() {
				@Override
			public void widgetSelected(SelectionEvent e) {
				List<GeneList> input = new ArrayList<GeneList>(); 
				try {
					input = FileUtils.getAllGeneLists(true);
				} catch (Exception ex) {
					logger.warn (" Cannot load the gene lists! ", ex);
				}
				CustomSelectionDialogWithDescription dialog = new CustomSelectionDialogWithDescription(Display.getCurrent().getActiveShell(), true, inputGeneList, input);
				dialog.initializeGeneListSelectionDialog();
				if (dialog.open() == Window.OK) {
					inputList = (GeneList)dialog.getSelection();
					layout.setInputList(inputList);
					
					// clear existing plate info
					layout.setPlates(null);
					controlGeneLocations = new HashMap<>();
					int i=0;
					for (Gene gene : inputList.getGenes()) {
						if (gene.getIsCommon()) {
							if (i < defaultGeneLocations.length) 
								controlGeneLocations.put(gene, defaultGeneLocations[i]);
							else 
								controlGeneLocations.put(gene, new Well('A', 1));
							i++;
						}
					}
					controlTableViewer.setInput(controlGeneLocations);
					controlTableViewer.refresh();
					layout.setControlGeneLocations(controlGeneLocations);
					
					btnAutoGeneratePlate.setEnabled(true);
					btnAutoGeneratePlate.setSelection(true);
					checkStatus();
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		if (layout.getInputList() != null) {
			inputGeneList.setText(layout.getInputList().getListName());
			btnBrowse.setEnabled(false); // cannot change the input list
		}
		
		Label lblNumberOfReplicates = new Label(composite, SWT.NONE);
		lblNumberOfReplicates.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblNumberOfReplicates.setText("Number of Replicates");
		new Label(composite, SWT.NONE);
		
		replicatesText = new Text(composite, SWT.BORDER);
		GridData gd_replicatesText = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_replicatesText.widthHint = 133;
		replicatesText.setLayoutData(gd_replicatesText);
		replicatesText.setText(layout.getNumberOfReplicates()+"");
		replicatesText.addModifyListener(this);
		new Label(composite, SWT.NONE);
		
		
		Label lblPlateSize = new Label(composite, SWT.NONE);
		lblPlateSize.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblPlateSize.setText("Plate Size");
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		
		Label lblWidth = new Label(composite, SWT.NONE);
		lblWidth.setText("width");
		
		plateWidth = new Text(composite, SWT.BORDER);
		plateWidth.setText(layout.getSize().getWidth() + "");
		GridData gd_plateWidth = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_plateWidth.widthHint = 133;
		plateWidth.setLayoutData(gd_plateWidth);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		plateWidth.addModifyListener(this);
		
		Label lblHeight = new Label(composite, SWT.NONE);
		lblHeight.setText("height");
		
		plateHeight = new Text(composite, SWT.BORDER);
		GridData gd_plateHeight = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_plateHeight.widthHint = 133;
		plateHeight.setLayoutData(gd_plateHeight);
		plateHeight.setText(layout.getSize().getHeight() + "");
		plateHeight.addModifyListener(this);
		new Label(composite, SWT.NONE);
		
		Label lblControlGenes = new Label(composite, SWT.NONE);
		lblControlGenes.setText("Control Genes");
		
		controlTableViewer = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION);
		table = controlTableViewer.getTable();
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		gd.heightHint=50;
		table.setLayoutData(gd);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		
		TableViewerColumn tableViewerColumn = new TableViewerColumn(controlTableViewer, SWT.NONE);
		TableColumn tblclmnControlGene = tableViewerColumn.getColumn();
		tblclmnControlGene.setWidth(100);
		tblclmnControlGene.setText("Control Gene");
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@SuppressWarnings("rawtypes")
			@Override
			public String getText(Object element) {
				if (element instanceof Map.Entry<?, ?>)
					return ((Gene)((Map.Entry) element).getKey()).getGeneIdentifier();
				return super.getText(element);
			}
		});
		
		TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(controlTableViewer, SWT.NONE);
		TableColumn tblclmnLocation = tableViewerColumn_1.getColumn();
		tblclmnLocation.setWidth(100);
		tblclmnLocation.setText("Location");
		tableViewerColumn_1.setLabelProvider(new ColumnLabelProvider() {
			@SuppressWarnings("rawtypes")
			@Override
			public String getText(Object element) {
				if (element instanceof Map.Entry<?, ?>)
					return controlGeneLocations.get(((Map.Entry) element).getKey()).toString();
					//return ((Map.Entry) element).getValue().toString();
				return super.getText(element);
			}
		});
		tableViewerColumn_1.setEditingSupport(new EditingSupport(controlTableViewer) {
			ComboBoxViewerCellEditor cellEditor;
			
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof Map.Entry<?, ?>) {
					Well well = ((Map.Entry<Gene,Well>) element).getValue();
					if (value instanceof Well) {
						if (value != null && !value.equals(well)) {
							((Map.Entry) element).setValue(value);
						}
					}
					controlTableViewer.update(element, null);
				}
			}
			
			@SuppressWarnings("rawtypes")
			@Override
			protected Object getValue(Object element) {
				if (element instanceof Map.Entry<?, ?>) {
					return controlGeneLocations.get(((Map.Entry) element).getKey());
				//	return ((Map.Entry) element).getValue();
				}
				return null;
			}
			
			@Override
			protected CellEditor getCellEditor(Object element) {
				cellEditor = new ComboBoxViewerCellEditor((Composite) getViewer().getControl(), SWT.READ_ONLY);
				cellEditor.setContentProvider(new ArrayContentProvider());
				cellEditor.setLabelProvider(new LabelProvider());
				cellEditor.setInput(generateGeneLocations());
				return cellEditor;
			}
			
			@Override
			protected boolean canEdit(Object element) {
				return true;
			}
		});
		controlTableViewer.setContentProvider(new ContentProvider());
		
		controlTableViewer.setInput(controlGeneLocations);
		
		new Label(composite, SWT.NONE);
		
		btnAutoGeneratePlate = new Button(composite, SWT.CHECK);
		btnAutoGeneratePlate.setAlignment(SWT.CENTER);
		btnAutoGeneratePlate.setSelection(true);
		btnAutoGeneratePlate.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
		btnAutoGeneratePlate.setText("Auto Generate Plate Layouts");
		
		if (layout.getPlates() != null) {
			btnAutoGeneratePlate.setEnabled(false);
			btnAutoGeneratePlate.setSelection(false);
		}
		new Label(composite, SWT.NONE);
	}	
	
	private void checkStatus () {
		canFlipToNextPage();
		// explicit call
		getWizard().getContainer().updateButtons();
	}
	
	@Override
	public boolean canFlipToNextPage() {
		// check all input
		if (layout != null) {
			if (layout.getName() == null) {
				return false;
			}
			if (inputList == null) {
				return false;
			}
			if (layout.getNumberOfReplicates() == null) {
				return false;
			}
			if (layout.getSize() == null || layout.getSize().getWidth() == -1 || layout.getSize().getHeight() == -1) {
				return false;
			}
			if (layout.getControlGeneLocations() == null) 
				return false;
			
			return true;
		}
		return false;
	}

	@Override
	public IWizardPage getNextPage() {
		int platesNo = calculateNumberOfPlates();
		List<List<Gene>> listOfGeneLists = generateGeneListsForEachPlate(platesNo);
		PlateLayoutDialog nextPage = null;
		for (int i = 0; i < platesNo; i++) {
			// check if there is a plate page already
			// if not, create it for the fist time here
			PlateLayoutDialog platePage = (PlateLayoutDialog)this.getWizard().getPage("Plate Page " + (i+1));
			if (platePage == null) {
				platePage = new PlateLayoutDialog("Plate Page " + (i+1), "Plate " + (i+1), null);
				platePage.setWizard(this.getWizard());
				platePage.setLayout (layout);
				if (btnAutoGeneratePlate.getSelection()) {
					platePage.layoutGenes(listOfGeneLists.get(i));
				}
				else {
					platePage.setGeneList(listOfGeneLists.get(i));
				}
				((PlateLayoutToolWizard)this.getWizard()).addPage(platePage);	
			} // TODO: need to think about what to do if inputList has changed and plate pages need to change now! - do not allow inputList changes
			if (i==0) {
				nextPage = platePage;
			}
			setPageComplete(true);
		}
		
		return nextPage;
	}
	
	private List<List<Gene>> generateGeneListsForEachPlate(int numOfPlates) {
		List<List<Gene>> listOfGenesLists = new ArrayList<>();
		int k=0;
		int numberOfControlGenes = controlGeneLocations.keySet().size();
		int numberOfGenesPerPlate = (int) (layout.getSize().getWidth() * layout.getSize().getHeight()) / layout.getNumberOfReplicates();
		List<Gene> inputGenes = inputList.getGenes();
		if (numberOfGenesPerPlate > inputGenes.size())
			numberOfGenesPerPlate = inputGenes.size();
		for (int i = 0; i < numOfPlates; i++) {
			List<Gene> plateGeneList = new ArrayList<>();
			for (int j=0; j < numberOfGenesPerPlate - numberOfControlGenes && k < inputGenes.size(); j++) {
				Gene gene = inputGenes.get(k++);
				if (gene.getIsCommon()) {// skip these, add them to all the plates 
					j--;
					continue;
				}
				plateGeneList.add(gene);
			}
			for (Iterator<Gene> iterator = controlGeneLocations.keySet().iterator(); iterator.hasNext();) {
				Gene gene = (Gene) iterator.next();
				plateGeneList.add(gene);
			}
			listOfGenesLists.add(plateGeneList);
		}
		return listOfGenesLists;
	}
	

	private int calculateNumberOfPlates () {
		int numberOfWells = layout.getSize().getWidth() * layout.getSize().getHeight();
		int numberOfGenes = inputList.getGenes().size() - controlGeneLocations.size();   // do not consider the control genes, add them later
		int numberOfReplicates = layout.getNumberOfReplicates();
		int numberOfPlates = (int) Math.ceil((double)(numberOfReplicates*numberOfGenes)/numberOfWells);
		
		int totalGenes = numberOfPlates*controlGeneLocations.keySet().size() + numberOfGenes;
		// check to make sure they will still fit in the numberOfPlates calculated
		if (totalGenes > (numberOfPlates * numberOfWells / numberOfReplicates) )
			// not enough
			numberOfPlates ++;
		
		return numberOfPlates;
	}

	@Override
	public void modifyText(ModifyEvent e) {
		Text newText = (Text) e.widget;
		String newValue = newText.getText();
		if (newValue != null)
			newValue = newValue.trim();
		if (e.getSource().equals(layoutNameText)) { 
			layout.setName(newValue);	
		} else if (e.getSource().equals(descriptionText)) {
			layout.setDescription(newValue);
		} else if (e.getSource().equals(replicatesText)) {
			try {
				int numberOfReplicates = Integer.parseInt(newValue);
				layout.setNumberOfReplicates(numberOfReplicates);
			} catch (NumberFormatException ex) {
				setMessage("Please enter a number for number of replicates");
				layout.setNumberOfReplicates(null);
			}
		} else if (e.getSource().equals(plateHeight)) {
			try {
				int height = Integer.parseInt(newValue);
				layout.getSize().setHeight(height);
			} catch (NumberFormatException ex) {
				setMessage("Please enter a valid number for plate height");
				layout.getSize().setHeight(-1);
			}
		} else if (e.getSource().equals(plateWidth)) {
			try {
				int width = Integer.parseInt(newValue);
				layout.getSize().setWidth(width);
			} catch (NumberFormatException ex) {
				setMessage("Please enter a valid number for plate width");
				layout.getSize().setWidth(-1);
			}
		}
		
		checkStatus();
	}
	
	List<Well> generateGeneLocations () {
		List<Well> locations = new ArrayList<>();
		if (plateHeight != null && plateWidth != null && replicatesText != null) {
			int height = Integer.parseInt(plateHeight.getText());
			int width = Integer.parseInt(plateWidth.getText());
			int replicates = Integer.parseInt(replicatesText.getText());
			
			Character character = 'A';
			int charPos = 1;
			int loc = 1;
			for (int i=1; i <= height; i++) {
				for (int j=1; j <= width; j++) {
					if (charPos == i && loc == j) {
						Well well = new Well();
						well.setX(character);
						well.setY(loc);
						locations.add(well);
						loc += replicates;
					}
				}
				character ++;
				charPos++;
				loc = 1;
			}
		}
		return locations;	
	}

	public PlateLayout getLayout() {
		return layout;
	}
	
	public void setLayout(PlateLayout layout) {
		this.layout = layout;
	}
	
	@Override
	public void setWizard(IWizard newWizard) {
		super.setWizard(newWizard);
		((PlateLayoutToolWizard)newWizard).layout = this.layout;
	}
	
}
