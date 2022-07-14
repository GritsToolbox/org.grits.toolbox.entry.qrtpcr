package org.grits.toolbox.entry.qrtpcr.table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDoubleDisplayConverter;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.editor.TextCellEditor;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.tree.GlazedListTreeData;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.tree.GlazedListTreeRowModel;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.RowHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.stack.ColumnGroupBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionModel;
import org.eclipse.nebula.widgets.nattable.selection.SelectRowCommandHandler;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.command.ClearAllSelectionsCommand;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectAllCommand;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectRowsCommand;
import org.eclipse.nebula.widgets.nattable.sort.ISortModel;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;
import org.eclipse.nebula.widgets.nattable.tree.config.DefaultTreeLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.grits.toolbox.display.control.table.command.GRITSTableDisplayColumnChooserCommandHandler;
import org.grits.toolbox.display.control.table.datamodel.GRITSColumnHeader;
import org.grits.toolbox.display.control.table.datamodel.GRITSTableDataObject;
import org.grits.toolbox.display.control.table.preference.TableViewerColumnSettings;
import org.grits.toolbox.display.control.table.preference.TableViewerPreference;
import org.grits.toolbox.display.control.table.process.TableDataProcessor;
import org.grits.toolbox.display.control.table.tablecore.GRITSColumnHeaderDataProvider;
import org.grits.toolbox.display.control.table.tablecore.GRITSHeaderMenuConfiguration;
import org.grits.toolbox.display.control.table.tablecore.GRITSNatTableStyleConfiguration;
import org.grits.toolbox.display.control.table.tablecore.GRITSSingleClickConfiguration;
import org.grits.toolbox.display.control.table.tablecore.IGritsTable;
import org.grits.toolbox.entry.qrtpcr.Config;
import org.grits.toolbox.entry.qrtpcr.editor.IQrtPCRPart;
import org.grits.toolbox.entry.qrtpcr.editor.QrtPCRTableEditor;
import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.QrtPCRData;
import org.grits.toolbox.entry.qrtpcr.model.TableData;
import org.grits.toolbox.entry.qrtpcr.model.merge.MergeTableData;
import org.grits.toolbox.entry.qrtpcr.preference.QrtPCRColumnChooserHandler;
import org.grits.toolbox.entry.qrtpcr.preference.QrtPCRMasterTablePreferenceLoader;
import org.grits.toolbox.entry.qrtpcr.preference.QrtPCRPreferenceLoader;
import org.grits.toolbox.entry.qrtpcr.preference.QrtPCRPreferencePage;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TreeList;

public class QrtPCRNatTable extends NatTable implements IGritsTable {
	
	public static final Logger logger = Logger.getLogger(QrtPCRNatTable.class);
	
	public static final String EDITABLE = "editable";
	public static final String GREENBACKGROUND = "greenBackground";
	public static final String INTEGEREDITABLE = "integerEditable";
	public static final String COMBOEDITABLE = "comboEditable";
	public static final String REDBACKGROUND = "redBackground";
	public static final String REDFOREGROUND = "redForeground";
	public static final String DARDREDFOREGROUND = "darkRedForeground";
	public static final String MAGENTABACKGROUND = "magentaBackground";
	public static final String YELLOWBACKGROUND = "yellowBackground";
	public static final String LISTBACKGROUND = "listBackground";
	public static final String BOLDFONT = "boldFont";
	public static final String STRIKE = "strikeThrough";

	protected SelectionLayer selectionLayer;
	protected ColumnHeaderLayer columnHeaderLayer;
	protected ColumnGroupHeaderLayer columnGroupHeaderLayer;
	protected ColumnHideShowLayer columnHideShowLayer;
	protected DataLayer columnHeaderDataLayer;
	protected ColumnGroupModel columnGroupModel;
	protected TreeLayer treeLayer;
	protected GRITSColumnHeaderDataProvider columnHeaderDataProvider;
	protected IColumnPropertyAccessor<TableData> columnPropertyAccessor;
	
	TableViewerPreference preferences;	
	
	protected IQrtPCRPart editor;
	protected List<TableData> tableData;
	protected List<List<GRITSColumnHeader>> columnList;
	
	QrtPCRData qrtPCRData;
	Double lowerThreshold = QrtPCRPreferencePage.THRESHOLD_PREFERENCE;
	Double stDevCutOff = QrtPCRPreferencePage.STDEVCUTOFF_PREFERENCE;
	Boolean rawData = false;
	Integer runId = 0;
	
	Boolean masterTable = false;

	protected DataLayer bodyDataLayer;
	protected ListDataProvider<TableData> bodyDataProvider;

	protected EventList<TableData> eventList;

	protected SortedList<TableData> sortedList;

	protected TreeList<TableData> treeList;

	protected GridLayer gridLayer;

	protected ViewportLayer viewportLayer;
	
	public QrtPCRNatTable(Composite parent) {
		super(parent, false);
	}
	
	public QrtPCRNatTable(Composite parent, IQrtPCRPart editor,  List<List<GRITSColumnHeader>> columnList) {
		super(parent, false);
		this.columnList = columnList;
		this.editor = editor;
	}

	@Override
	public boolean hasColumnGroupHeader() {
		return columnList.size() > 1;
	}

	@Override
	public void performAutoResize() {
	}
	
	public void setLowerThreshold(Double lowerThreshold) {
		this.lowerThreshold = lowerThreshold;
	}
	
	public void setStDevCutOff(Double stDevCutOff) {
		this.stDevCutOff = stDevCutOff;
	}
	
	public void setRawData(Boolean rawData) {
		this.rawData = rawData;
	}
	
	public void setRunId(Integer runId) {
		this.runId = runId;
	}
	
	public void setQrtPCRData(QrtPCRData qrtPCRData) {
		this.qrtPCRData = qrtPCRData;
	}
	
	public void setTableData(List<TableData> data) {
		this.tableData = data;
	}
	
	public void setColumnList(List<List<GRITSColumnHeader>> columnList) {
		this.columnList = columnList;
	}
	
	public void setMasterTable(Boolean masterTable) {
		this.masterTable = masterTable;
	}
	
	public void initializeComponents() {
		ConfigRegistry configRegistry = new ConfigRegistry();
        addStyles(configRegistry);
        initColumnPropertyAccessor();
        // has to be done after columnPropertyAccessor is initialized
        initializeConfigRegistry(configRegistry);

        if (this.tableData == null) {
        	this.tableData = new ArrayList<>();
        }
        eventList = GlazedLists.eventList(this.tableData);
        sortedList = new SortedList<TableData>(eventList, null);

        // Column header layer
        columnHeaderDataProvider = new GRITSColumnHeaderDataProvider(columnList.get(columnList.size() -1));
        columnHeaderDataLayer = new DefaultColumnHeaderDataLayer(
                columnHeaderDataProvider);

        ISortModel sortModel = new PlateTableSortModel<TableData>(sortedList,
                columnPropertyAccessor, configRegistry, columnHeaderDataLayer);

        initializeTreeList(sortModel);
        GlazedListTreeData<TableData> treeData = new GlazedListTreeData<TableData>(treeList);

        // Body layer
        bodyDataProvider = new ListDataProvider<TableData>(
                treeList, columnPropertyAccessor);
        bodyDataLayer = new DataLayer(bodyDataProvider);
        
        GlazedListsEventLayer<TableData> glazedListsEventLayer = new GlazedListsEventLayer<TableData>(
                bodyDataLayer, treeList);
        
        ColumnReorderLayer columnReorderLayer = new ColumnReorderLayer(
                glazedListsEventLayer);
        columnHideShowLayer = new ColumnHideShowLayer(
                columnReorderLayer);

        RowHideShowLayer rowHideShowLayer = new RowHideShowLayer(
                columnHideShowLayer);

        // Switch the ITreeRowModel implementation between using native grid
        // Hide/Show or GlazedList TreeList Hide/Show
        // TreeLayer treeLayer = new TreeLayer(rowHideShowLayer, new
        // TreeRowModel<Datum>(treeData), true);
        treeLayer = new TreeLayer(rowHideShowLayer,
                new GlazedListTreeRowModel<TableData>(treeData), false);
		
		
		
        selectionLayer = new SelectionLayer(treeLayer);
    	// use a RowSelectionModel that will perform row selections and is able
 		// to identify a row via unique ID
 		selectionLayer.setSelectionModel(new RowSelectionModel<TableData>(selectionLayer, bodyDataProvider, new IRowIdAccessor<TableData>() {
 		
 		     @Override
 		     public Serializable getRowId(TableData rowObject) {
 		    	 if (rowObject instanceof Gene)
 		    		 return ((Gene)rowObject).getGeneIdentifier();
 		    	 else if (rowObject instanceof MergeTableData)
 		    		 return ((MergeTableData) rowObject).getGene().getGeneIdentifier();
 		    	 return null;
 		     }
 		
 		}));
 		
        viewportLayer = new ViewportLayer(selectionLayer);

        columnHeaderLayer = new ColumnHeaderLayer(
                columnHeaderDataLayer, viewportLayer, selectionLayer);

        ColumnOverrideLabelAccumulator labelAccumulator = new ColumnOverrideLabelAccumulator(
                columnHeaderDataLayer);
        columnHeaderDataLayer.setConfigLabelAccumulator(labelAccumulator);
        
        columnGroupModel = new ColumnGroupModel();
		ColumnGroupBodyLayerStack bodyLayer = new ColumnGroupBodyLayerStack(
				bodyDataLayer, columnGroupModel);

		columnGroupHeaderLayer = new ColumnGroupHeaderLayer(columnHeaderLayer,
				bodyLayer.getSelectionLayer(), columnGroupModel);	
		
		setColumnGroupIndices();
		
        SortHeaderLayer<TableData> sortHeaderLayer = new SortHeaderLayer<TableData>(
                columnGroupHeaderLayer, sortModel, false);

        // Row header layer
        DefaultRowHeaderDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(
                bodyDataProvider);
        DefaultRowHeaderDataLayer rowHeaderDataLayer = new DefaultRowHeaderDataLayer(
                rowHeaderDataProvider);
        RowHeaderLayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer,
                viewportLayer, selectionLayer);

        // Corner layer
        DefaultCornerDataProvider cornerDataProvider = new DefaultCornerDataProvider(
                columnHeaderDataProvider, rowHeaderDataProvider);
        DataLayer cornerDataLayer = new DataLayer(cornerDataProvider);
        CornerLayer cornerLayer = new CornerLayer(cornerDataLayer,
                rowHeaderLayer, sortHeaderLayer);

        // Grid
        gridLayer = new GridLayer(viewportLayer, sortHeaderLayer,
                rowHeaderLayer, cornerLayer);
       
        setLayer(gridLayer);
        
        initColumnChooserLayer();
        setConfigRegistry(configRegistry);
        initializePreferences();
        setPreferences(); 
        setConfigLabelAccumulator();
        
        finishNatTable();
	}
	
	protected void initializeTreeList(ISortModel sortModel) {
		PlateTreeFormat treeFormat = new PlateTreeFormat(sortModel);
        treeList = new TreeList<TableData>(sortedList,
                treeFormat, new MyExpansionModel());
		
	}

	protected void setPreferences() {
		try {
			updateViewFromPreferenceSettings();
		} catch( Exception e) {
			logger.error("no preference settings! Initializing preferences from the current view");
			logger.trace("Exception: ", e);
			loadViewerSettings(true);
		}
	}
	
	protected void finishNatTable() {
		bodyDataLayer.setColumnPercentageSizing(true);
        for (int i=0; i < columnList.get(0).size(); i++) {
        	bodyDataLayer.setColumnWidthPercentageByPosition(i, 100/columnList.get(0).size());   
		}
        
		this.addConfiguration(new GRITSNatTableStyleConfiguration());
        
		if (this.qrtPCRData != null && this.editor instanceof QrtPCRTableEditor) {
	        this.addConfiguration(new ValueMenuConfiguration((QrtPCRTableEditor) editor, qrtPCRData, this, bodyDataLayer, rawData));
	        this.addConfiguration(new GeneMenuConfiguration((QrtPCRTableEditor) editor, qrtPCRData, this, bodyDataLayer, rawData));
		}
        
        this.addConfiguration(new GRITSHeaderMenuConfiguration(this));
        this.addConfiguration(new DefaultTreeLayerConfiguration(treeLayer));
        this.addConfiguration(new GRITSSingleClickConfiguration(columnList.size() > 1)); 
        
        this.configure();
        this.refresh();
        GridDataFactory.fillDefaults().grab(true, true).applyTo(this);		
	}
	
	protected void initializeConfigRegistry(ConfigRegistry configRegistry) {
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, IEditableRule.ALWAYS_EDITABLE, DisplayMode.EDIT, EDITABLE);
        configRegistry.registerConfigAttribute( CellConfigAttributes.DISPLAY_CONVERTER, new DefaultDoubleDisplayConverter(), EDITABLE);
        TextCellEditor textCellEditor = new TextCellEditor();
        textCellEditor.setErrorDecorationEnabled(true);
        textCellEditor.setErrorDecorationText( "Enter a floating point number"); 
        textCellEditor.setDecorationPositionOverride(SWT.LEFT | SWT.TOP);
        configRegistry.registerConfigAttribute( EditConfigAttributes.CELL_EDITOR, textCellEditor, DisplayMode.EDIT, EDITABLE);
        configRegistry.registerConfigAttribute( EditConfigAttributes.DATA_VALIDATOR, new DoubleValidator(columnPropertyAccessor), DisplayMode.EDIT, EDITABLE);
	}
	
	protected void setConfigLabelAccumulator() {
		bodyDataLayer.setConfigLabelAccumulator(new PlateTableConfigLabelAccumulator(bodyDataProvider, columnPropertyAccessor, rawData, masterTable, lowerThreshold, stDevCutOff));
	}

	protected void initColumnPropertyAccessor() {
		if (editor == null) {
			// from the preferences
			// not editable
			columnPropertyAccessor = new PlateTableColumnPropertyAccessor<TableData>(null, null, columnList.get(columnList.size() -1), runId, rawData, masterTable, lowerThreshold);
		}
		if (editor instanceof QrtPCRTableEditor) 
			columnPropertyAccessor = new PlateTableColumnPropertyAccessor<TableData>((QrtPCRTableEditor) editor, this.qrtPCRData, columnList.get(columnList.size() -1), runId, rawData, masterTable, lowerThreshold);
	}

	protected void setColumnGroupIndices() {
		if (columnList.size() <= 1)
			return;
		String sLastExp = null;
		List<Integer> alGroupIndices = new ArrayList<Integer>();
		for (int i = 0; i < columnList.get(0).size(); i++) {
			Object oVal = columnList.get(0).get(i);
			if (oVal == null && sLastExp == null)
				continue;
			boolean bChanged = (oVal != null && (sLastExp == null || !oVal
					.toString().equals(sLastExp)));
			if (bChanged) {
				sLastExp = oVal.toString();
				alGroupIndices.add(i);
			}
			columnGroupHeaderLayer.addColumnsIndexesToGroup(sLastExp, i);
			// if ( bChanged )
			// columnGroupHeaderLayer.setGroupUnbreakable(i);
		}
		for (int i = 0; i < alGroupIndices.size(); i++) {
			columnGroupHeaderLayer.setGroupUnbreakable(alGroupIndices.get(i));
		}
	}
	
	public void loadViewerSettings(boolean _bForceRebuild) {
		// loadViewerSettings();
		if (getTablePreference().settingsNeedInitialization() || _bForceRebuild) {
			// then create a new object with empty value and save it in
			// preference.xml
			getTablePreference().setPreferenceSettings(getPreferenceSettingsFromCurrentView());
			getTablePreference().writePreference();
		}
	}
	
	public TableViewerColumnSettings getPreferenceSettingsFromCurrentView() {
		TableViewerColumnSettings newEntity = new TableViewerColumnSettings();
		if (this.columnHeaderDataLayer == null
				|| this.columnHeaderDataLayer.getColumnCount() == 0)
			return null;
		int iNumCols = this.columnHeaderDataLayer.getColumnCount();
		int iPos = 0;
		// just set the order as found in the columnlayer
		for (int i = 0; i < iNumCols; i++) {
			String headerLabel = (String) this.columnHeaderDataLayer.getDataValueByPosition(i, 0);
			GRITSColumnHeader header = new GRITSColumnHeader(headerLabel, this.columnHeaderDataProvider.getDataKey(i, 0));
			newEntity.setVisColInx(header, iPos++);
		}
		return newEntity;
	}
	
	protected void initColumnChooserLayer() {
		GRITSTableDisplayColumnChooserCommandHandler columnChooserCommandHandler = new QrtPCRColumnChooserHandler(this);		
		//columnHeaderLayer.registerCommandHandler(columnChooserCommandHandler);
		columnGroupHeaderLayer.registerCommandHandler(columnChooserCommandHandler);		
	}

	public void initializePreferences() {
		if (masterTable)
			setTablePreference (QrtPCRMasterTablePreferenceLoader.getTableViewerPreference());
		else
			setTablePreference(QrtPCRPreferenceLoader.getTableViewerPreference());
	}
	
	public void setTablePreference(TableViewerPreference pref) {
		this.preferences = pref;
	}

	public TableViewerPreference getTablePreference() {
		return preferences;
	}
	
	public DataLayer getBodyDataLayer() {
		return bodyDataLayer;
	}

	public SelectionLayer getSelectionLayer() {
		return selectionLayer;
	}

	public void setSelectionLayer(SelectionLayer selectionLayer) {
		this.selectionLayer = selectionLayer;
	}

	public ColumnHeaderLayer getColumnHeaderLayer() {
		return columnHeaderLayer;
	}

	public void setColumnHeaderLayer(ColumnHeaderLayer columnHeaderLayer) {
		this.columnHeaderLayer = columnHeaderLayer;
	}

	public ColumnGroupHeaderLayer getColumnGroupHeaderLayer() {
		return columnGroupHeaderLayer;
	}

	public void setColumnGroupHeaderLayer(ColumnGroupHeaderLayer columnGroupHeaderLayer) {
		this.columnGroupHeaderLayer = columnGroupHeaderLayer;
	}

	public ColumnHideShowLayer getColumnHideShowLayer() {
		return columnHideShowLayer;
	}

	public void setColumnHideShowLayer(ColumnHideShowLayer columnHideShowLayer) {
		this.columnHideShowLayer = columnHideShowLayer;
	}

	public DataLayer getColumnHeaderDataLayer() {
		return columnHeaderDataLayer;
	}

	public void setColumnHeaderDataLayer(DataLayer columnHeaderDataLayer) {
		this.columnHeaderDataLayer = columnHeaderDataLayer;
	}

	public ColumnGroupModel getColumnGroupModel() {
		return columnGroupModel;
	}

	public void setColumnGroupModel(ColumnGroupModel columnGroupModel) {
		this.columnGroupModel = columnGroupModel;
	}
	
	public void setColumnHeaderDataProvider(GRITSColumnHeaderDataProvider columnHeaderDataProvider2) {
		this.columnHeaderDataProvider = columnHeaderDataProvider2;
	}
	
	public void setTreeLayer(TreeLayer treeLayer) {
		this.treeLayer = treeLayer;
	}
	
	public TreeList<TableData> getTreeList() {
		return treeList;
	}
	
	public IColumnPropertyAccessor<TableData> getColumnPropertyAccessor() {
		return columnPropertyAccessor;
	}

	public void updatePreferenceSettingsFromCurrentView() {
		if (this.columnHeaderDataLayer == null|| this.columnHeaderDataLayer.getColumnCount() == 0)
			return;
		int iNumCols = this.columnHeaderDataLayer.getColumnCount();
		int iNewNumCols = 0;
		// first iterate over columns in the base column layer to count the
		// number of visible columns
		for (int iColInx = 0; iColInx < iNumCols; iColInx++) {
			boolean bHidden = this.columnHideShowLayer.isColumnIndexHidden(iColInx);
			if (bHidden)
				continue;
			iNewNumCols++;
		}

		// now iterate over the visible columns using the columnshowlayer and
		// set the preference value
		int iToPos = 0;
		for (int iVisPos = 0; iVisPos < iNewNumCols; iVisPos++) { // position
			// based on the column show  header layer
			int iColPos = LayerUtil.convertColumnPosition(
					this.columnHideShowLayer, iVisPos,
					this.columnHeaderDataLayer);
			String sHeaderKey = this.columnHeaderDataProvider.getDataKey(iColPos, 0);
			if (getTablePreference().getPreferenceSettings().hasColumn(sHeaderKey)) {
				GRITSColumnHeader header =getTablePreference().getPreferenceSettings().getColumnHeader(sHeaderKey);
				getTablePreference().getPreferenceSettings().setVisColInx(header, iToPos++);
			}
		}

		for (int iColInx = 0; iColInx < iNumCols; iColInx++) { // index based
			// off of column layer (all data)
			boolean bHidden = this.columnHideShowLayer.isColumnIndexHidden(iColInx);
			if (!bHidden)
				continue;
			int iColPos = this.columnHeaderDataLayer.getColumnPositionByIndex(iColInx);
			String sHeaderKey = this.columnHeaderDataProvider.getDataKey(iColPos, 0);
			if (getTablePreference().getPreferenceSettings().hasColumn(sHeaderKey)) {
				GRITSColumnHeader header = getTablePreference().getPreferenceSettings().getColumnHeader(sHeaderKey);
				getTablePreference().getPreferenceSettings().setVisColInx(header, -1);
			}
		}
	}
	
	public boolean updateViewFromPreferenceSettings() {
		if (this.columnHeaderDataLayer == null || this.columnHeaderDataLayer.getColumnCount() == 0)
			return false;
		int iNumCols = this.columnHeaderDataLayer.getColumnCount();
		int iNumNonHidden = 0;
		if (iNumCols == 0)
			return false;
		
		// check if there is selected column
		boolean bAddSelect = false;
		if (this.columnList.get(0) != null) {
			GRITSColumnHeader header = this.columnList.get(0).get(0);
			bAddSelect = header.getKeyValue().equals(Config.SELECTED);
		}
		
		this.columnHideShowLayer.showAllColumns(); // first show all columns
		ArrayList<Integer> alHiddenCols = new ArrayList<Integer>();
		for (int iColLayerPos = 0; iColLayerPos < iNumCols; iColLayerPos++) { 
			String sHeaderKey = this.columnHeaderDataProvider.getDataKey(iColLayerPos, 0);
			int iColShowLayerPos = LayerUtil.convertColumnPosition(
					this.columnHeaderDataLayer, iColLayerPos,
					this.columnHideShowLayer);
			if (getTablePreference().getPreferenceSettings().hasColumn(sHeaderKey)) {
				int iPrefColPos = getTablePreference()
						.getPreferenceSettings()
						.getVisColInx(sHeaderKey);
				if (iPrefColPos == -1) {
					alHiddenCols.add(iColShowLayerPos);
				} else {
					iNumNonHidden++;
				}
			}
		}
		this.columnHideShowLayer.hideColumnPositions(alHiddenCols);

		for (int iPrefColPos = 0; iPrefColPos < iNumNonHidden; iPrefColPos++) { // going in position order of the new PREFERENCES
			GRITSColumnHeader prefHeader = getTablePreference().getPreferenceSettings().getColumnAtVisColInx(iPrefColPos);
			if ( prefHeader == null )
				continue;
			for (int iFromPos = 0; iFromPos < this.columnHideShowLayer.getColumnCount(); iFromPos++) { // column position based
				int iColPos = LayerUtil.convertColumnPosition(this.columnHideShowLayer, iFromPos, this.columnHeaderDataLayer);
				String sThisHeaderKey = this.columnHeaderDataProvider.getDataKey(iColPos, 0);
				if (prefHeader.getKeyValue().equals(sThisHeaderKey)) {
					int iToPos = iPrefColPos;
					if (bAddSelect)
						iToPos++;
					if (iFromPos != iToPos) {
						ColumnReorderCommand command = new ColumnReorderCommand(this.columnHideShowLayer, iFromPos, iToPos);
						// System.out.println("Moving " + sHeaderText + " from "
						// + iFromPos + " to " + iToPos );
						this.columnHideShowLayer.doCommand(command);
					} else {
						// System.out.println("Staying put: " + sHeaderText +
						// " from " + iFromPos + " to " + iToPos );
					}
					break;
				}
			}

		}
		
		return true;
	}
	
	public void toggleExpandCollapse(boolean collapse) {
		@SuppressWarnings("unchecked")
		GlazedListTreeRowModel<TableData> treeModel = (GlazedListTreeRowModel<TableData>) treeLayer.getModel();
		for (int i=0; i < treeLayer.getRowCount(); i++) {
			if (collapse) {
				if (treeModel.isCollapsible(i) )
					treeModel.collapse(i);
			}
			else { // expand
				if (treeModel.isCollapsed(i))
					treeModel.expand(i);
			}
		}
	}
	
	protected void addStyles (ConfigRegistry configRegistry) {
		//define Styles and add them to the registry
        final Style redBackgroundStyle = new Style();
        redBackgroundStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, Display.getDefault().getSystemColor(SWT.COLOR_RED));
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,
        		redBackgroundStyle, 
        	    DisplayMode.NORMAL,
        	    REDBACKGROUND); 
        
        final Style redForegroundStyle = new Style();
        redForegroundStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, Display.getDefault().getSystemColor(SWT.COLOR_RED));
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,
        		redForegroundStyle, 
        	    DisplayMode.NORMAL,
        	    REDFOREGROUND); 
        
        final Style darkRedForegroundStyle = new Style();
        darkRedForegroundStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED));
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,
        		darkRedForegroundStyle, 
        	    DisplayMode.NORMAL,
        	    DARDREDFOREGROUND); 
        
        final Style magentaBackgroundStyle = new Style();
        magentaBackgroundStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, Display.getDefault().getSystemColor(SWT.COLOR_MAGENTA));
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,
        		magentaBackgroundStyle, 
        	    DisplayMode.NORMAL,
        	    MAGENTABACKGROUND); 
        
        final Style yellowBackgroundStyle = new Style();
        yellowBackgroundStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, Display.getDefault().getSystemColor(SWT.COLOR_YELLOW));
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,
        		yellowBackgroundStyle, 
        	    DisplayMode.NORMAL,
        	    YELLOWBACKGROUND); 
        
        final Style greenBackgroundStyle = new Style();
        greenBackgroundStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,
        		greenBackgroundStyle, 
        	    DisplayMode.NORMAL,
        	    GREENBACKGROUND); 
        
        final Style listBackgroundStyle = new Style();
        listBackgroundStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, Display.getDefault().getSystemColor(SWT.COLOR_LIST_SELECTION));
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,
        		listBackgroundStyle, 
        	    DisplayMode.NORMAL,
        	    LISTBACKGROUND); 
        
        final Style boldFontStyle = new Style();
        FontData[] fD = Display.getDefault().getSystemFont().getFontData();
		fD[0].setStyle(SWT.BOLD);
		Font boldFont = new Font(Display.getDefault(), fD);
		boldFontStyle.setAttributeValue(CellStyleAttributes.FONT, boldFont);
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,
        		boldFontStyle, 
        	    DisplayMode.NORMAL,
        	    BOLDFONT); 
        
        TextPainter painter = new TextPainter();
        painter.setStrikethrough(true);
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER,
        		painter, 
        	    DisplayMode.NORMAL,
        	    STRIKE); 
	}
	
	public static class MyExpansionModel implements TreeList.ExpansionModel<TableData> {
		@Override
		public boolean isExpanded(TableData element, List<TableData> path) {
			return true;
		}

		@Override
		public void setExpanded(TableData element, List<TableData> path,
			boolean expanded) {}
	}

	public void selectAll() {
		for(int i=0; i < treeList.size(); i++) {
			this.doCommand(new SelectRowsCommand(selectionLayer, 0, i, true, true));
		}
		//this.doCommand(new SelectAllCommand());  // this shows rows as selected but isSelected returns false for all of them. don't know when it started failing
	}
	
	public void deSelectAll() {
		this.doCommand(new ClearAllSelectionsCommand());
	}
	
	/**
	 * 
	 * @param row
	 * @return true if the given row is already selected
	 */
	public boolean isSelected (TableData row) {
		for(int i=0; i < treeList.size(); i++) {
			TableData tableData = treeList.get(i);
			if (tableData.equals(row)) {
				int rowPosition = LayerUtil.convertRowPosition(bodyDataLayer, i, selectionLayer);
				return selectionLayer.isRowPositionSelected(rowPosition);
			}
		}
		return false;
	}

	public void updateTable() {
		eventList.clear();
		for (TableData row : this.tableData) {
			eventList.add(row);
		}
		
		this.refresh();
	}
	
	public List<List<GRITSColumnHeader>> getColumnList() {
		return columnList;
	}

	@Override
	public TableDataProcessor getTableDataProcessor() {
		return null;
	}

	@Override
	public GRITSTableDataObject getGRITSTableDataObject() {
		return null;
	}
}
