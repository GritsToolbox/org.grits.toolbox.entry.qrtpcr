package org.grits.toolbox.entry.qrtpcr.manager.pages;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.entry.qrtpcr.Config;
import org.grits.toolbox.entry.qrtpcr.manager.GeneListManagerEditor;
import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.GeneList;

public class TextEditingSupport extends EditingSupport {

	TableViewer viewer;
	TableColumn column;
	private TextCellEditor textCellEditor;
	
	private TextCellEditor integerCellEditor;
	private ComboBoxViewerCellEditor comboCellEditor;
	private MPart part;
	
	public TextEditingSupport(MPart part, TableViewer viewer, TableColumn column) {
		super(viewer);
		this.viewer = viewer;
		this.column = column;
		this.part = part;
		this.textCellEditor = new TextCellEditor(viewer.getTable());
		this.integerCellEditor = new TextCellEditor(viewer.getTable());
        ((Text)this.integerCellEditor.getControl()).setTextLimit(PropertyHandler.LABEL_TEXT_LIMIT);
        ControlDecoration controlDecoration = new ControlDecoration(integerCellEditor.getControl(), SWT.CENTER);
        integerCellEditor.setValidator(new IntegerValidator(controlDecoration));
        
        this.comboCellEditor = new ComboBoxViewerCellEditor((Composite) viewer.getControl());
        comboCellEditor.setLabelProvider(new LabelProvider());
        comboCellEditor.setContentProvider(new  IStructuredContentProvider() {
			
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			
			@Override
			public void dispose() {
			}
			
			@Override
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof String[])
					return (Object[])inputElement;
				return null;
			}
		});
        
        comboCellEditor.setInput(new String[] {"Control", "[No]"});
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		if (column.getText().equals(Config.ID))
			return integerCellEditor;
		else if (column.getText().equals(Config.CONTROL))
			return comboCellEditor;
		else
			return textCellEditor;
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) {
		if (element == null)
			return "";
		if (element instanceof Gene) {
			if (column.getText().equals(Config.GENEID)) {
				return ((Gene) element).getGeneIdentifier();
			}
			else if (column.getText().equals(Config.GENESYMBOL)) {
				return ((Gene) element).getGeneSymbol();
			}
			else if (column.getText().equals(Config.DESCRIPTION)) {
				return ((Gene) element).getDescription();
			}
			else if (column.getText().equals(Config.FWPRIMER)) {
				return ((Gene) element).getForwardPrimer();
			}
			else if (column.getText().equals(Config.REVPRIMER)) {
				return ((Gene) element).getReversePrimer();
			}
			else if (column.getText().equals(Config.GROUP)) {
				return ((Gene) element).getGroup();
			}
			else if (column.getText().equals(Config.REFSEQ)) {
				return ((Gene) element).getRefSeqString();
			}
			else if (column.getText().equals(Config.SECONDARYREFSEQ)) {
				if (((Gene) element).getSecondaryRefSeq() == null)
					return "";
				return ((Gene) element).getSecondaryRefSeqString();
			}
			else if (column.getText().equals(Config.NAME)) {
				if (((Gene) element).getFullName() == null)
					return "";
				return ((Gene) element).getFullName();
			}
			else if (column.getText().equals(Config.ALIASES)) {
				if (((Gene) element).getAliases() == null)
					return "";
				return ((Gene) element).getAliasString();
			}
			else if (column.getText().equals(Config.LOCATION)) {
				if (((Gene) element).getLocations() == null)
					return "";
				return ((Gene) element).getLocationString();
			}
			else if (column.getText().equals(Config.ID)) {
				if (((Gene)element).getGeneIds() != null)	
					return ((Gene) element).getGeneIdString();
			}
			else if (column.getText().equals(Config.DESIGNEDFOR)) {
				return ((Gene) element).getNotes();
			}
			else if (column.getText().equals(Config.CONTROL)) {
				if (((Gene)element).getIsCommon()) {
					return "Control";
				}else
					return "";
			}
		}
		else if (element instanceof GeneList) {
			if (column.getText().equals(Config.NAME)) {
				return ((GeneList) element).getListName();
			}else if (column.getText().equals(Config.DESCRIPTION)) {
				if (((GeneList) element).getDescription() == null)
					return "";
				return ((GeneList) element).getDescription();
			}
		}
		return "";
	}

	@Override
	protected void setValue(Object element, Object value) {
		String oldValue = null;
		String newValue = (String)value;
		if (element instanceof Gene) {
			
			if (column.getText().equals(Config.GENEID)) {
				oldValue = ((Gene) element).getGeneIdentifier();
				((Gene) element).setGeneIdentifier(newValue.trim());
			}
			else if (column.getText().equals(Config.GENESYMBOL)) {
				oldValue = ((Gene) element).getGeneSymbol();
				((Gene) element).setGeneSymbol(newValue.trim());
			}
			else if (column.getText().equals(Config.DESCRIPTION)) {
				oldValue = ((Gene) element).getDescription();
				((Gene) element).setDescription(newValue.trim());    
			}
			else if (column.getText().equals(Config.FWPRIMER)) {
				oldValue = ((Gene) element).getForwardPrimer();
				((Gene) element).setForwardPrimer(newValue.trim());    
			}
			else if (column.getText().equals(Config.REVPRIMER)) {
				oldValue = ((Gene) element).getReversePrimer();
				((Gene) element).setReversePrimer(newValue.trim());    
			}
			else if (column.getText().equals(Config.GROUP)) {
				oldValue = ((Gene) element).getGroup();
				((Gene) element).setGroup(newValue.trim());    
			}
			else if (column.getText().equals(Config.REFSEQ)) {
				oldValue = ((Gene) element).getRefSeqString();
				((Gene) element).setRefSeq(newValue.trim());    
			}
			else if (column.getText().equals(Config.SECONDARYREFSEQ)) {
				oldValue = ((Gene) element).getSecondaryRefSeqString();
				((Gene) element).setSecondaryRefSeq(newValue.trim());    
			}
			else if (column.getText().equals(Config.LOCATION)) {
				oldValue = ((Gene) element).getLocationString();
				((Gene) element).setLocationString(newValue.trim());    
			}
			else if (column.getText().equals(Config.ALIASES)) {
				oldValue = ((Gene) element).getAliasString();
				((Gene) element).setAliasString(newValue.trim());    
			}
			else if (column.getText().equals(Config.NAME)) {
				oldValue = ((Gene) element).getFullName();
				((Gene) element).setFullName(newValue.trim());    
			}
			else if (column.getText().equals(Config.ID)) {
				if (((Gene)element).getGeneIds() != null)
					oldValue = ((Gene)element).getGeneIdString();
				((Gene)element).setGeneIds(newValue.trim());
			}
			else if (column.getText().equals(Config.DESIGNEDFOR)) {
				oldValue = ((Gene) element).getNotes();
				((Gene) element).setNotes(newValue.trim());    
			}
			else if (column.getText().equals(Config.CONTROL)) {
				if (((Gene)element).getIsCommon()) {
					oldValue = "Control";
				}else 
					oldValue = "";
				if (newValue != null && newValue.equals("Control"))
					((Gene) element).setIsCommon(true);
				else 
					((Gene) element).setIsCommon(false);
			}
		}
		else if (element instanceof GeneList) {
			if (column.getText().equals(Config.NAME)) {
				oldValue = ((GeneList) element).getListName();
				((GeneList) element).setListName(newValue.trim());
			}else if (column.getText().equals(Config.DESCRIPTION)) {
				oldValue = ((GeneList) element).getDescription();
				((GeneList) element).setDescription(newValue.trim());
			}
		}
		
		viewer.update(element, null);
		if ((oldValue == null && !newValue.trim().isEmpty()) 
		    || (oldValue != null && !oldValue.equals(newValue))) {
	    	//mark dirty
			((GeneListManagerEditor)part.getObject()).markDirty();
		}
	}

	public class IntegerValidator implements ICellEditorValidator {
		private ControlDecoration controlDecoration;

		public IntegerValidator(ControlDecoration controlDecoration) {
			this.controlDecoration = controlDecoration;
		}

		@Override
		public String isValid(Object value) {
			String inValidMessage = null;
			if (value != null) {
				String stringValue = (String)value;
				if (!stringValue.isEmpty()) {
					String splitChar = ",";
					if (stringValue.contains("/"))
						splitChar = "/";
					String[] geneIdNumbers = stringValue.split(splitChar);
					for (String geneId : geneIdNumbers) {
						try {
							Integer.parseInt(geneId.trim());
						} catch (NumberFormatException e) {
							inValidMessage = "Not a valid value. Id should be a number";
							break;
						}
					}
						
				} 
			}

	        Image errorImage;
	        if(inValidMessage != null)
	        {
	            errorImage = FieldDecorationRegistry.getDefault()
	                    .getFieldDecoration(FieldDecorationRegistry.DEC_ERROR)
	                    .getImage();
	            this.controlDecoration.setMarginWidth(2);
	            this.controlDecoration.setImage(errorImage);
	            this.controlDecoration.setDescriptionText(inValidMessage);
	            this.controlDecoration.show();
	        }
	        else
	        {
	            this.controlDecoration.hide();
	        }
	        return inValidMessage;
		}

	}
}
