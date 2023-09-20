package org.grits.toolbox.entry.qrtpcr.manager.actions;

import java.util.List;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.PojoProperties;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.grits.toolbox.entry.qrtpcr.Config;
import org.grits.toolbox.entry.qrtpcr.model.GeneList;

public class SaveGeneListDialog extends FormDialog {

	private List<GeneList> existing;

	public SaveGeneListDialog(Shell shell) {
		super(shell);
	}
	
	public SaveGeneListDialog(Shell shell, List<GeneList> existingLists) {
		super(shell);
		this.existing = existingLists;
	}
	
	
	Text nameText;
	Text descriptionText;
	private ControlDecoration dec;
	private Label errorLabel;
	private String name;
	private String description;
	private String organism;
	private Text organismText;
	private ControlDecoration dec2;
	
	@Override
	protected void createFormContent(IManagedForm mform) {
		mform.getForm().setText("Please give a name, organism and description (optional)");
		ScrolledForm scrolledForm = mform.getForm();
		TableWrapLayout tableWrapLayout = new TableWrapLayout();
		tableWrapLayout.numColumns = 2;
		scrolledForm.getBody().setLayout(tableWrapLayout);
		
		FormToolkit toolkit = mform.getToolkit();
		
		Label nameLabel = toolkit.createLabel(scrolledForm.getBody(), "Name *", SWT.NONE);
		TableWrapData twd_nameLabel = new TableWrapData(TableWrapData.RIGHT, TableWrapData.TOP, 1, 1);
		twd_nameLabel.align = TableWrapData.LEFT;
		nameLabel.setLayoutData(twd_nameLabel);
		
		nameText = toolkit.createText(scrolledForm.getBody(), "", SWT.NONE);
		nameText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP, 1, 1));
		
		Label organismLabel = toolkit.createLabel(scrolledForm.getBody(), "Organism *", SWT.NONE);
		TableWrapData twd = new TableWrapData(TableWrapData.RIGHT, TableWrapData.TOP, 1, 1);
		twd.align = TableWrapData.LEFT;
		organismLabel.setLayoutData(twd);
		
		organismText = toolkit.createText(scrolledForm.getBody(), "", SWT.NONE);
		organismText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP, 1, 1));
		
		if (this.organism != null)
			organismText.setText(organism);
		
		// Create a control decoration for the control.
		dec = new ControlDecoration(nameText, SWT.TOP | SWT.LEFT);
		// Specify the decoration image and description
		Image image = JFaceResources.getImage(DLG_IMG_MESSAGE_ERROR);
		dec.setImage(image);
		dec.setDescriptionText("Name cannot be left empty");
		dec.hide();
		
		// Create a control decoration for the control.
		dec2 = new ControlDecoration(organismText, SWT.TOP | SWT.LEFT);
		dec2.setImage(image);
		dec2.setDescriptionText("Organism cannot be left empty");
		dec2.hide();
		
		Label lblDescription = toolkit.createLabel(scrolledForm.getBody(), "Description", SWT.NONE);
		lblDescription.setLayoutData(new TableWrapData(TableWrapData.RIGHT, TableWrapData.TOP, 1, 1));
		descriptionText = toolkit.createText(scrolledForm.getBody(), "", SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		descriptionText.setTextLimit(Config.DESCRIPTIONLENGTH);
		TableWrapData twd_description = new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP, 1, 1);
		twd_description.heightHint = 60;
		descriptionText.setLayoutData(twd_description);
		// have to do this for Windows environment, works on Mac without the listener
		descriptionText.addKeyListener( new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.stateMask == SWT.CTRL && e.keyCode == 'a') {
					((Text)e.widget).selectAll();
				}
			}
		});
		
		errorLabel = toolkit.createLabel(scrolledForm.getBody(), "", SWT.NONE);
		errorLabel.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.BOTTOM, 3, 2));
		toolkit.paintBordersFor(scrolledForm.getBody());
	}
	
	class StringRequiredValidator implements IValidator {
		 
	    private final String errorText;
	    private final String type;
	    private final ControlDecoration controlDecoration;
	 
	    public StringRequiredValidator(String errorText, String type, 
	        ControlDecoration controlDecoration) {
	        super();
	        this.errorText = errorText;
	        this.type = type;
	        this.controlDecoration = controlDecoration;
	    }
	 
	    public IStatus validate(Object value) {
	        if (value instanceof String) {
	            String text = (String) value;
	            if (text.trim().length() == 0) {
	                controlDecoration.show();
	                return ValidationStatus
	                        .error(errorText);
	            }
	            // check for uniqueness as well
	            if (type.equals("Name") && existing != null) {
	            	for (GeneList geneList : existing) {
						if (geneList.getListName().equals(text)) {
							controlDecoration.show();
							return ValidationStatus.error("The name already exists");
						}	
					}
	            }
	            
	        }
	        controlDecoration.hide();
	        errorLabel.setText("");
	        return Status.OK_STATUS;
	    }
	}
	
	@Override
	protected void okPressed() {
		name = nameText.getText();
		description = descriptionText.getText();
		organism = organismText.getText();
		
		DataBindingContext dataBindingContext = new DataBindingContext();
		Binding binding = dataBindingContext.bindValue(
				WidgetProperties.text(SWT.Modify).observe(nameText),
			    PojoProperties.value(SaveGeneListDialog.class, "name").observe(this),
			    new UpdateValueStrategy()
			        .setAfterConvertValidator(new StringRequiredValidator(
			             "Name cannot be left empty", "Name",
			              dec)),
			    null);
		
		DataBindingContext dataBindingContext2 = new DataBindingContext();
		Binding binding2 = dataBindingContext2.bindValue(
				WidgetProperties.text(SWT.Modify).observe(organismText),
			    PojoProperties.value(SaveGeneListDialog.class, "organism").observe(this),
			    new UpdateValueStrategy()
			        .setAfterConvertValidator(new StringRequiredValidator(
			             "Organism cannot be left empty", "Organism",
			              dec2)),
			    null);
		
		if (((Status)binding.getValidationStatus().getValue()).isOK() &&
			((Status)binding2.getValidationStatus().getValue()).isOK()) {
			super.okPressed();
		}
		else {
			// display errorMessage
			errorLabel.setForeground(new Display().getSystemColor(SWT.COLOR_RED));;
			errorLabel.setText(((Status)binding.getValidationStatus().getValue()).getMessage());
		}
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setOrganism(String organism) {
		this.organism = organism;
	}
	
	public String getOrganism() {
		return organism;
	}

}
