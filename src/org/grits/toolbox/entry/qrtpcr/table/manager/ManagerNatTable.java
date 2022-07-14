package org.grits.toolbox.entry.qrtpcr.table.manager;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultBooleanDisplayConverter;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.editor.CheckBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.ComboBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.TextCellEditor;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.painter.cell.CheckBoxPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ComboBoxPainter;
import org.eclipse.nebula.widgets.nattable.sort.ISortModel;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.viewport.command.ShowRowInViewportCommand;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.grits.toolbox.display.control.table.datamodel.GRITSColumnHeader;
import org.grits.toolbox.entry.qrtpcr.editor.IQrtPCRPart;
import org.grits.toolbox.entry.qrtpcr.manager.GeneListManagerEditor;
import org.grits.toolbox.entry.qrtpcr.manager.pages.GeneSelectionListContentProvider;
import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.GeneList;
import org.grits.toolbox.entry.qrtpcr.model.TableData;
import org.grits.toolbox.entry.qrtpcr.preference.QrtPCRGeneListTablePreferenceLoader;
import org.grits.toolbox.entry.qrtpcr.table.PlateTreeFormat;
import org.grits.toolbox.entry.qrtpcr.table.QrtPCRNatTable;
import org.grits.toolbox.entry.qrtpcr.util.GeneUtils;

import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.TreeList;
import ca.odell.glazedlists.matchers.TextMatcherEditor;

public class ManagerNatTable extends QrtPCRNatTable  implements MouseListener{
	
	public static final String SELECTEDEDITABLE = "Selected";

	TableViewer selectedTableViewer;
	GeneList geneList;

	private FilterList<TableData> filterList;
	TextMatcherEditor<TableData> matcher;
	
	public ManagerNatTable(Composite parent) {
		super(parent);
	}
	
	public ManagerNatTable(Composite parent, IQrtPCRPart editor, List<List<GRITSColumnHeader>> columnList) {
		super(parent, editor, columnList);
	}
	
	public void setSelectedTableViewer(TableViewer selectedTableViewer) {
		this.selectedTableViewer = selectedTableViewer;
	}
	
	public void setGeneList(GeneList geneList) {
		this.geneList = geneList;
	}
	
	@Override
	protected void initializeConfigRegistry(ConfigRegistry configRegistry) {
		// all fields are editable but ID and CONTROL requires a different editor
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, IEditableRule.ALWAYS_EDITABLE, DisplayMode.EDIT, EDITABLE);
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, IEditableRule.ALWAYS_EDITABLE, DisplayMode.EDIT, INTEGEREDITABLE);
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, IEditableRule.ALWAYS_EDITABLE, DisplayMode.EDIT, COMBOEDITABLE);
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, IEditableRule.ALWAYS_EDITABLE, DisplayMode.EDIT, SELECTEDEDITABLE);
        configRegistry.registerConfigAttribute( EditConfigAttributes.CELL_EDITOR, new TextCellEditor(), DisplayMode.EDIT, EDITABLE);
        
        TextCellEditor textCellEditor2 = new TextCellEditor();
        textCellEditor2.setErrorDecorationEnabled(true);
        textCellEditor2.setErrorDecorationText( "Enter an integer or integers separated by a comma or / only"); 
        textCellEditor2.setDecorationPositionOverride(SWT.LEFT | SWT.TOP);
        configRegistry.registerConfigAttribute( EditConfigAttributes.CELL_EDITOR, textCellEditor2, DisplayMode.EDIT, INTEGEREDITABLE);
        configRegistry.registerConfigAttribute( EditConfigAttributes.DATA_VALIDATOR, new IntegerValidator(columnPropertyAccessor), DisplayMode.EDIT, INTEGEREDITABLE);
        
        //register a combobox editor for DisplayMode.EDIT 
        configRegistry.registerConfigAttribute( EditConfigAttributes.CELL_EDITOR, new ComboBoxCellEditor(Arrays.asList(new String[] {"Control", "[No]"} )), DisplayMode.EDIT, COMBOEDITABLE); 
        configRegistry.registerConfigAttribute( CellConfigAttributes.CELL_PAINTER, new ComboBoxPainter(), DisplayMode.EDIT, COMBOEDITABLE);  
        
	}
	
	@Override
	protected void initializeTreeList(ISortModel sortModel) {
		PlateTreeFormat treeFormat = new PlateTreeFormat(sortModel);
		filterList = new FilterList<>(sortedList);
		TextFilterator<TableData> textFilterator = new TextFilterator<TableData>()
        {
			public void getFilterStrings(List<String> arg0, TableData arg1)
			{
				if (arg1 instanceof Gene) {
					Gene p = (Gene) arg1;
					arg0.add(p.getGeneIdentifier().toLowerCase() );
					if (p.getGeneSymbol() != null)
						arg0.add(p.getGeneSymbol().toLowerCase() );
				    if (p.getRefSeqString() != null)
				    	arg0.add( p.getRefSeqString().toLowerCase());
				    if (p.getGeneIdString() != null)
				    	arg0.add(p.getGeneIdString().toLowerCase());
				}
			}
        };
        
		matcher = new TextMatcherEditor<TableData>(textFilterator );
        matcher.setMode( TextMatcherEditor.CONTAINS );
		filterList.setMatcherEditor( matcher );
        treeList = new TreeList<TableData>(filterList,
                treeFormat, new MyExpansionModel());
	}
	
	@Override
	protected void finishNatTable() {
		super.finishNatTable();
		this.getConfigRegistry().registerConfigAttribute( CellConfigAttributes.CELL_PAINTER, new CheckBoxPainter(), DisplayMode.NORMAL, SELECTEDEDITABLE);  
		this.getConfigRegistry().registerConfigAttribute( CellConfigAttributes.DISPLAY_CONVERTER, new DefaultBooleanDisplayConverter(), DisplayMode.NORMAL, SELECTEDEDITABLE);  
		this.getConfigRegistry().registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, new CheckBoxCellEditor(), DisplayMode.EDIT, SELECTEDEDITABLE);
		
		if (geneList != null && editor instanceof GeneListManagerEditor) {
			this.addConfiguration(new ChangeMenuConfiguration((GeneListManagerEditor)editor, geneList, this, bodyDataLayer));
		}
		this.configure();
		this.addMouseListener(this);
	}
	
	@Override
	protected void setConfigLabelAccumulator() {
		bodyDataLayer.setConfigLabelAccumulator(new GeneListTableConfigLabelAccumulator(bodyDataProvider, columnPropertyAccessor));
		bodyDataLayer.registerCommandHandler(
		        new DeleteRowCommandHandler<TableData>(bodyDataProvider.getList()));
	}
	
	@Override
	protected void initColumnPropertyAccessor() {
		if (editor == null) { // from the preferences
			// from the preferences
			// not editable
			columnPropertyAccessor = new GeneListTableColumnPropertyAccessor(null, columnList.get(columnList.size() -1));
		}
		else if (editor instanceof GeneListManagerEditor) 
			columnPropertyAccessor = new GeneListTableColumnPropertyAccessor((GeneListManagerEditor) editor, columnList.get(columnList.size() -1));
	}
	
	@Override
	public void initializePreferences() {
		setTablePreference(QrtPCRGeneListTablePreferenceLoader.getTableViewerPreference());
	}

	public void filter(String searchString) {
		matcher.setFilterText(new String[] {searchString.toLowerCase()});
		if (this.gridLayer.getBodyLayer().getRowCount() > 0)
			this.doCommand(new ShowRowInViewportCommand(gridLayer.getBodyLayer(), 1));
	}
	
	@Override
	public void selectAll() {
		((GeneListTableColumnPropertyAccessor)columnPropertyAccessor).selectAll(bodyDataProvider.getList());
		this.repaintColumn(0);
		this.update();
	}
	
	@Override
	public void deSelectAll() {
		((GeneListTableColumnPropertyAccessor)columnPropertyAccessor).clearAllSelections(bodyDataProvider.getList());
		this.repaintColumn(0);
		this.update();
	}
	
	public List<TableData> getSelected() {
		return ((GeneListTableColumnPropertyAccessor)columnPropertyAccessor).getSelected();
	}

	public void removeRow(TableData row) {
		int i=0;
		int rowIndex = -1;
		for (TableData tableData : treeList) {
			if (tableData.equals(row))
				rowIndex = i;
			i++;
		}
		this.doCommand(new DeleteRowCommand(this.bodyDataLayer, rowIndex));
	
	}
	
	public void selectRow(TableData row) {
		((GeneListTableColumnPropertyAccessor)columnPropertyAccessor).selectRow(row);
		this.repaintColumn(0);
		this.update();
	}

	@Override
	public void mouseDoubleClick(MouseEvent e) {
		GeneSelectionListContentProvider cp = (GeneSelectionListContentProvider) selectedTableViewer.getContentProvider();
		// find the clicked item
		int rowPos = this.getRowPositionByY(e.y);
		
		//transform the NatTable row position to the row position of the body layer stack
		int bodyRowPos = LayerUtil.convertRowPosition(this, rowPos, bodyDataLayer);
		TableData item = bodyDataProvider.getRowObject(bodyRowPos);
		
		Gene selection = (Gene) item;
		boolean found = false;
		// check if it is already in the list
		// if so, do not add again
		List<Gene> existing = cp.getGenes();
		for (Gene gene : existing) {
			if (gene.getGeneIdentifier().equals(((Gene) selection).getGeneIdentifier())) {
				found = true;
				break;
			}
		}
		if (found) {// skip this one
			// ask the user if they want to add a duplicate one
			boolean addDuplicate = MessageDialog.openQuestion(Display.getCurrent().getActiveShell(), "Duplicate", "This gene is already in the list, do you want to add it again anyway?");
			if (addDuplicate) {
				// make a copy and add
				Gene newGene =GeneUtils.makeACopy((Gene) selection);
				existing.add(newGene);
				this.selectRow(item);
			}
		} else {
			existing.add((Gene)selection);
			this.selectRow(item);
		}	
		
		selectedTableViewer.refresh();
	}

	@Override
	public void mouseDown(MouseEvent e) {
	}

	@Override
	public void mouseUp(MouseEvent e) {
	}
}
