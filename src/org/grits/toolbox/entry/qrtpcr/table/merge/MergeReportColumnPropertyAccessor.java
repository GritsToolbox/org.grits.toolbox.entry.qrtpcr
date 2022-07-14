package org.grits.toolbox.entry.qrtpcr.table.merge;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.grits.toolbox.display.control.table.datamodel.GRITSColumnHeader;
import org.grits.toolbox.entry.qrtpcr.Config;
import org.grits.toolbox.entry.qrtpcr.model.merge.MergeData;
import org.grits.toolbox.entry.qrtpcr.model.merge.MergeTableData;

public class MergeReportColumnPropertyAccessor<TableData> implements IColumnPropertyAccessor<TableData> {
	
	List<GRITSColumnHeader> propertyNames;
	List<String> aliasList;
	
	public MergeReportColumnPropertyAccessor(List<GRITSColumnHeader> columns, List<String> aliasList) {
		this.propertyNames = columns;
		this.aliasList = aliasList;
	}
	
	Integer getLastNotGroupedColumn () {
		int notGrouped = 0;
		for (GRITSColumnHeader col : propertyNames) {
			if (!col.isGrouped()) 
				notGrouped++;
		}
		return notGrouped;
	}

	@Override
	public int getColumnCount() {
		return propertyNames.size();
	}

	@Override
	public Object getDataValue(TableData element, int columnIndex) {
		String columnName = getColumnProperty(columnIndex);
		if (columnName == null) {
			return null;
		}
		List<MergeData> dataList = ((MergeTableData)element).getDataList();
		int index = (int) Math.floor( (columnIndex - getLastNotGroupedColumn() )/2);
		
		switch (columnName) {
		case Config.GENEID:
			return ((MergeTableData)element).getGene().getGeneIdentifier();
		case Config.GENESYMBOL:
			return ((MergeTableData)element).getGene().getGeneSymbol();
		case Config.AVERAGE:
			if (index >= 0 && index < dataList.size()) {
				return dataList.get(index).getAverage();
			}
			break;
		case Config.STDEVADJUSTED:
			if (index >= 0 && index < dataList.size()) {
				return dataList.get(index).getStDev();
			}
			break;
		}
		
		return null;
	}

	@Override
	public void setDataValue(TableData arg0, int arg1, Object arg2) {
		// the table is not editable
		// do nothing
	}

	@Override
	public int getColumnIndex(String columnName) {
		int index=0;
		for (GRITSColumnHeader simianColumnHeader : propertyNames) {
			if (simianColumnHeader.getKeyValue().equals(columnName))
				return index;
			index++;
		}
		return -1;
	}

	@Override
	public String getColumnProperty(int columnIndex) {
		if (columnIndex >= propertyNames.size())
			return null;
		GRITSColumnHeader header = propertyNames.get(columnIndex);
		if (header != null)
			return header.getKeyValue();
			
		return null;
	}

}
