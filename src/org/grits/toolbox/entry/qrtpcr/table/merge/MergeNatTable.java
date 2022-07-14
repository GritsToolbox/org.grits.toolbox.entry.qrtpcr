package org.grits.toolbox.entry.qrtpcr.table.merge;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel.ColumnGroup;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.swt.widgets.Composite;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.grits.toolbox.display.control.table.datamodel.GRITSColumnHeader;
import org.grits.toolbox.display.control.table.preference.TableViewerColumnSettings;
import org.grits.toolbox.entry.qrtpcr.editor.IQrtPCRPart;
import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.TableData;
import org.grits.toolbox.entry.qrtpcr.model.merge.MergeTableData;
import org.grits.toolbox.entry.qrtpcr.preference.QrtPCRMergeTablePreferenceLoader;
import org.grits.toolbox.entry.qrtpcr.table.QrtPCRNatTable;

public class MergeNatTable extends QrtPCRNatTable {
	
	List<String> aliasList;
	private ArrayList<Integer> alFirstGroupIndices;
	
	public MergeNatTable(Composite parent) {
		super(parent);
	}

	public MergeNatTable(Composite parent, IQrtPCRPart editor, List<List<GRITSColumnHeader>> columnList, List<String> aliasList) {
		super(parent, editor, columnList);
		this.aliasList = aliasList;
	}

	@Override
	protected void initColumnPropertyAccessor() {
		this.columnPropertyAccessor = new MergeReportColumnPropertyAccessor<TableData>(columnList.get(columnList.size() -1), this.aliasList);
	}
	
	@Override
	public boolean isSelected(TableData gene) {
		for(int i=0; i < treeList.size(); i++) {
			TableData tableData = treeList.get(i);
			if (tableData instanceof MergeTableData) {
				Gene fromRow = ((MergeTableData) tableData).getGene();
				if (fromRow.equals(gene)) {
					int rowPosition = LayerUtil.convertRowPosition(bodyDataLayer, i, selectionLayer);
					return selectionLayer.isRowPositionSelected(rowPosition);
				}
			}
		}
		return false;
	}

	public void selectGene(Gene gene) {
		for(int i=0; i < treeList.size(); i++) {
			TableData tableData = treeList.get(i);
			if (tableData instanceof MergeTableData) {
				Gene fromRow = ((MergeTableData) tableData).getGene();
				if (fromRow.getGeneIdentifier().equals(gene.getGeneIdentifier())) {
					int rowPosition = LayerUtil.convertRowPosition(bodyDataLayer, i, selectionLayer);
					selectionLayer.selectRow(0, rowPosition, false, true);  // withControlMask is true to allow multiple selection
				}
			}
		}
		
	}

	@Override
	public void initializePreferences() {
		setTablePreference(QrtPCRMergeTablePreferenceLoader.getTableViewerPreference());
	}
	
	@Override
	public TableViewerColumnSettings getPreferenceSettingsFromCurrentView() {
		if ( this.columnGroupModel == null || this.columnGroupModel .isEmpty() ) 
			return super.getPreferenceSettingsFromCurrentView();

		if ( getFirstGroupIndices() == null )
			discoverGroups(this.columnGroupModel);

		TableViewerColumnSettings newEntity = new TableViewerColumnSettings();
		if (getFirstGroupIndices().size() < 2) {
			logger.log(Level.WARN, "Not enough column groups to update visibility");
			return newEntity;
		}

		int iPos = 0;
		for (int i = 0; i < 2; i++) {
			ColumnGroup group = this.columnGroupModel.getColumnGroupByIndex(getFirstGroupIndices().get(i));
			List<Integer> members = group.getMembers();
			for (int j = 0; j < members.size(); j++) {
				int iColLayerInx = members.get(j);
				int iColLayerPos = this.columnHeaderDataLayer.getColumnPositionByIndex(iColLayerInx);
				String headerLabel = (String) this.columnHeaderDataLayer.getDataValueByPosition(iColLayerPos, 0);
				GRITSColumnHeader header = new GRITSColumnHeader(headerLabel, this.columnHeaderDataProvider.getDataKey(iColLayerPos, 0));
			//	SimianColumnHeader header = (SimianColumnHeader) this.columnHeaderDataLayer.getDataValueByPosition(iColLayerPos, 0);
				newEntity.setVisColInx(header, iPos++);
			}
		}
		return newEntity;
	}
	
	/**
	 * updates the positions in the nattable based on current preferences
	 */
	@Override
	public boolean updateViewFromPreferenceSettings() {
		boolean bTotalSuccess = true;
		try {
			if ( this.columnGroupModel == null || this.columnGroupModel.isEmpty() ) {
				super.updateViewFromPreferenceSettings();
				return bTotalSuccess;
			}
			if (this.columnHeaderDataLayer == null || this.columnHeaderDataLayer.getColumnCount() == 0)
				return false;
			int iNumCols = this.columnHeaderDataLayer.getColumnCount();
			if (iNumCols == 0)
				return false;

			this.columnHideShowLayer.showAllColumns(); // first show all columns
			ArrayList<Integer> alHiddenCols = new ArrayList<Integer>();
			if ( getFirstGroupIndices() == null )
				discoverGroups(this.columnGroupModel);

			for( int i = 0; i < getFirstGroupIndices().size(); i++ ) {
				ColumnGroup group = this.columnGroupModel.getColumnGroupByIndex(getFirstGroupIndices().get(i));  // change 07/31/2013. No longer using "Exp" text so relying on order
				List<Integer> members = group.getMembers();
				for( int j = 0; j < members.size(); j++ ) {  // I believe members are index based
					int iColLayerInx = members.get(j);
					int iColLayerPos = this.columnHeaderDataLayer.getColumnPositionByIndex(iColLayerInx);
					String sHeaderKey = this.columnHeaderDataProvider.getDataKey(iColLayerPos, 0);
					int iColShowLayerPos = LayerUtil.convertColumnPosition(this.columnHeaderDataLayer, iColLayerPos, this.columnHideShowLayer);
					if (getTablePreference().getPreferenceSettings()
							.hasColumn(sHeaderKey)) {
						int iPrefColPos = getTablePreference()
								.getPreferenceSettings()
								.getVisColInx(sHeaderKey);
						if ( iPrefColPos == -1 ) {
							alHiddenCols.add(iColShowLayerPos);
						} 
					} else { // not there????
						alHiddenCols.add(iColShowLayerPos);
						logger.warn("Header: " + sHeaderKey + " not found in preferences!");
					}
				}
			}
			this.columnHideShowLayer.hideColumnPositions(alHiddenCols);
		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
			throw ex;
		}
		if( ! bTotalSuccess ) {
			// something was wrong with the preferences!
			logger.error("There is an error in the preference settings.");
			ErrorUtils.createErrorMessageBox(getShell(), "Column preference settings were invalid. You may want to reconfigure them");
			updatePreferenceSettingsFromCurrentView();
			getTablePreference().writePreference();
		}
		
		return bTotalSuccess;
	}
	
	@Override
	// updates preference object based on current column settings in nattable
	public void updatePreferenceSettingsFromCurrentView() {
		if ( this.columnGroupModel == null || this.columnGroupModel.isEmpty() ) {
			super.updatePreferenceSettingsFromCurrentView();
			return;
		}
		if (this.columnHeaderDataLayer == null || this.columnHeaderDataLayer.getColumnCount() == 0)
			return;

		if ( getFirstGroupIndices() == null )
			discoverGroups(this.columnGroupModel);

		setVisibilityOfGroups(this.columnHeaderDataLayer, this.columnHideShowLayer, this.columnGroupModel, 0);
		setVisibilityOfGroups(this.columnHeaderDataLayer, this.columnHideShowLayer, this.columnGroupModel, 1);

	}
	
	private void setVisibilityOfGroups(DataLayer columnLayer, 
			ColumnHideShowLayer columnShowLayer, ColumnGroupModel groupModel, int iGroupNum ) {
		// i suppose it is possible for users to rearrange the groups and put the "first" group somewhere else. Well, we 
		// aren't supporting that. the first groups will always be first, thus I will order them separately
		int iAdder = iGroupNum > 0 ? groupModel.getColumnGroupByIndex(0).getSize() : 0;

		ColumnGroup group = groupModel.getColumnGroupByIndex(getFirstGroupIndices().get(iGroupNum));	
		List<Integer> members = group.getMembers();
		int iNewNumCols = 0;
		for( int iMemInx = 0; iMemInx < members.size(); iMemInx++ ) {
			int iColInx = members.get(iMemInx);
			boolean bHidden = columnShowLayer.isColumnIndexHidden(iColInx);	
			if( ! bHidden ) {
				iNewNumCols++;
			}
		}

		// now iterate over the visible columns using the columnshowlayer and
		// set the preference value
		int iToPos = iAdder;
		for (int iVisPos = 0; iVisPos < iNewNumCols; iVisPos++) { // position
			// based on the column show  header layer
			int iColPos = LayerUtil.convertColumnPosition(
					this.columnHideShowLayer, iVisPos + iAdder,
					this.columnHeaderDataLayer);
			String sHeaderKey = this.columnHeaderDataProvider.getDataKey(iColPos, 0);
			if (getTablePreference().getPreferenceSettings().hasColumn(sHeaderKey)) {
				GRITSColumnHeader header = getTablePreference().getPreferenceSettings().getColumnHeader(sHeaderKey);
				getTablePreference().getPreferenceSettings().setVisColInx(header, iToPos++);
			}
		}

		for (int iMemInx = 0; iMemInx < members.size(); iMemInx++) { // index based
			// off of column layer (all data)
			int iColInx = members.get(iMemInx);
			boolean bHidden = columnShowLayer.isColumnIndexHidden(iColInx);	
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

	public ArrayList<Integer> getFirstGroupIndices() {
		return alFirstGroupIndices;
	}
    
	public void discoverGroups( ColumnGroupModel groupModel ) {
		this.alFirstGroupIndices = new ArrayList<Integer>();
		if ( groupModel == null || groupModel.isEmpty() ) 
			return;
		
		ColumnGroup prevGroup = null;
		int iTotalGroups = groupModel.size();
		int iGrpCnt = 0;
		int i = 0;
		while( iGrpCnt < iTotalGroups ) {
			ColumnGroup group = groupModel.getColumnGroupByIndex(i++);  // change 07/31/2013. No longer using "Exp" text so relying on order
			if ( prevGroup != null && prevGroup.equals(group) ) {
				// HACK to get around their bad API:  if we've seen this group continue.
				continue;
			}
			prevGroup = group;
			iGrpCnt++;
			this.alFirstGroupIndices.add(i-1);
		}	
	}

}
