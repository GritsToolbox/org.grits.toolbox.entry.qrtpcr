package org.grits.toolbox.entry.qrtpcr.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.dialog.ProjectExplorerDialog;
import org.grits.toolbox.entry.qrtpcr.Config;
import org.grits.toolbox.entry.qrtpcr.preference.QrtPCRPreferencePage;
import org.grits.toolbox.entry.sample.property.SampleProperty;

public class QrtPCRDataEntryPage extends WizardPage {
	
	String entryName;
	Entry parentEntry;
	
	Text sampleNameText;
	private Text nameText;
	private ControlDecoration dec;
	
	Double threshold = QrtPCRPreferencePage.THRESHOLD_PREFERENCE;
	Double stDevCutOff = QrtPCRPreferencePage.STDEVCUTOFF_PREFERENCE;
	private ControlDecoration dec2;

	protected QrtPCRDataEntryPage(String pageName) {
		super(pageName);
	}

	public QrtPCRDataEntryPage(String string, Entry sampleEntry) {
		this(string);
		this.parentEntry = sampleEntry;
	}

	@Override
	public void createControl(final Composite parent) {
		
		this.setTitle("Create QrtPCR Entry");
		this.setMessage("Please select the sample and provide a name for the entry");
		
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		gridLayout.verticalSpacing = 10;
		
		Composite content = new Composite(parent, SWT.NONE);
		
		setControl(content);
		content.setLayout(gridLayout);
		
		/*
		 * First row starts: create sample textfield with a browse button
		 */
		GridData sampleNameData = new GridData();
		Label sampleNameLabel = new Label(content, SWT.NONE);
		sampleNameLabel.setText("Sample");
		sampleNameLabel.setLayoutData(sampleNameData);
		
		GridData projectnameTextData = new GridData();
		projectnameTextData.grabExcessHorizontalSpace = true;
		projectnameTextData.horizontalAlignment = GridData.FILL;
		projectnameTextData.horizontalSpan = 1;
		sampleNameText = new Text(content, SWT.BORDER);
		sampleNameText.setTextLimit(PropertyHandler.LABEL_TEXT_LIMIT);
		sampleNameText.setLayoutData(projectnameTextData);
		//for the first time if an entry was chosen by a user
		if(parentEntry != null)
		{
			sampleNameText.setText(parentEntry.getDisplayName());
		}
		sampleNameText.setEditable(false);
		
		// browse button
		GridData browseButtonData = new GridData();
		Button button = new Button(content, SWT.PUSH);
		button.setText("Browse");
		button.setLayoutData(browseButtonData);
		button.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent event) 
			{
				Shell newShell = new Shell(parent.getShell(),SWT.PRIMARY_MODAL | SWT.SHEET);
				ProjectExplorerDialog dlg = new ProjectExplorerDialog(newShell);
				// Set the parent as a filter
				dlg.addFilter(SampleProperty.TYPE);
				// Change the title bar text
				dlg.setTitle("Sample Selection");
				// Customizable message displayed in the dialog
				dlg.setMessage("Choose a sample");
				// Calling open() will open and run the dialog.
				if (dlg.open() == Window.OK) {
					Entry selected = dlg.getEntry();
					if (selected != null) {
						parentEntry = selected;
						// Set the text box as the sample text
						sampleNameText.setText(parentEntry.getDisplayName());
						if (nameText != null && nameText.getText() != null && !nameText.getText().trim().isEmpty())
							setPageComplete(true);
						else
							setPageComplete(false);
					}
				}
			}
		});
		
		//then add separator
		createSeparator(content, 4);
		
		/*
		 * Second row starts
		 */
		GridData nameData = new GridData();
		Label nameLabel = new Label(content, SWT.LEFT);
		nameLabel.setText("Name");
		nameLabel.setLayoutData(nameData);
		
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = 3;
		nameText = new Text(content, SWT.BORDER);
		nameText.setLayoutData(gridData);
		nameText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				setEntryName(nameText.getText());
				if (!nameText.getText().trim().isEmpty() && parentEntry != null)
					setPageComplete(true);
				else
					setPageComplete(false);
				
			}
		});
        nameText.addListener(SWT.Traverse, new Listener()
        { 
            @Override
            public void handleEvent(Event event)
            {
                if(event.detail == SWT.TRAVERSE_RETURN)
                {
                	setEntryName(nameText.getText());
                	if (!nameText.getText().trim().isEmpty() && parentEntry != null)
                		setPageComplete(true);
                	else
                		setPageComplete(false);
                	
                }
            }
        });
        
        createSeparator(content, 4);
        
        Label thresholdLabel = new Label(content, SWT.LEFT);
        thresholdLabel.setText(Config.THRESHOLD);
        
        Text thresholdText = new Text(content, SWT.BORDER);
        thresholdText.setLayoutData(gridData);
		thresholdText.setText(QrtPCRPreferencePage.THRESHOLD_PREFERENCE+"");
		
		// Create a control decoration for the control.
		dec = new ControlDecoration(thresholdText, SWT.TOP | SWT.LEFT);
		// Specify the decoration image and description
		Image image = JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_ERROR);
		dec.setImage(image);
		dec.setDescriptionText("Should enter a floating point number");
		dec.hide();
		
		thresholdText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				Text newText = (Text) e.widget;
				String newValue = newText.getText();
				if (newValue != null)
					newValue = newValue.trim();
				try {
					threshold = Double.parseDouble(newValue);
					dec.hide();
				} catch (NumberFormatException ex) {
					dec.show();
				}
				
			}
		});
		
		Label stDevLabel = new Label(content, SWT.LEFT);
		stDevLabel.setText(Config.STDEVCUTOFF);
		stDevLabel.setToolTipText("Standard deviation cut-off value. Entries having standard deviations more than this cutoff will be highlighted");
		Text stDevCutoffText = new Text(content, SWT.BORDER);
		stDevCutoffText.setLayoutData(gridData);
		stDevCutoffText.setText(QrtPCRPreferencePage.STDEVCUTOFF_PREFERENCE+"");
		
		// Create a control decoration for the control.
		dec2 = new ControlDecoration(stDevCutoffText, SWT.TOP | SWT.LEFT);
		// Specify the decoration image and description
		dec2.setImage(image);
		dec2.setDescriptionText("Should enter a floating point number");
		dec2.hide();
		
		stDevCutoffText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				Text newText = (Text) e.widget;
				String newValue = newText.getText();
				if (newValue != null)
					newValue = newValue.trim();
				try {
					stDevCutOff = Double.parseDouble(newValue);
					dec2.hide();
				} catch (NumberFormatException ex) {
					dec2.show();
				}
				
			}
		});
        
        setPageComplete(false);
	}

	protected Label createSeparator(Composite parent, int span) {
		GridData separatorData = new GridData();
		separatorData.grabExcessHorizontalSpace = true;
		separatorData.horizontalAlignment = GridData.FILL;
		separatorData.horizontalSpan = span;
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(separatorData);
		return separator;
	}
	
	@Override
	public IWizardPage getNextPage() {
		UploadqrtPCRDataDialogPageOne pageOne = (UploadqrtPCRDataDialogPageOne) getWizard().getPage("Plate and Gene Info");
		pageOne.setSampleEntry (parentEntry);
		((UploadDataWizard)getWizard()).setSampleEntry(parentEntry);
		return pageOne;
	}
	
	public String getEntryName() {
		return entryName;
	}

	public void setEntryName(String entryName) {
		this.entryName = entryName;
	}

	public Entry getParentEntry() {
		return parentEntry;
	}

	public void setParentEntry(Entry parentEntry) {
		this.parentEntry = parentEntry;
	}
	
	public Double getThreshold() {
		return threshold;
	}
	
	public Double getStDevCutOff() {
		return stDevCutOff;
	}
}
