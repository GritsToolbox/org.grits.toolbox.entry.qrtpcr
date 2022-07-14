package org.grits.toolbox.entry.qrtpcr.table;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.grits.toolbox.entry.qrtpcr.Config;
import org.grits.toolbox.entry.qrtpcr.model.CtHistory;
import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.GeneData;
import org.grits.toolbox.entry.qrtpcr.model.TableData;
import org.grits.toolbox.entry.qrtpcr.model.merge.MergeTableData;

public class PlateTableConfigLabelAccumulator implements IConfigLabelAccumulator {

	private ListDataProvider<TableData> bodyDataProvider;
	Boolean rawData;
	Double lowerThreshold;
	Double stDevCutOff;
	
	Boolean masterTable=false;
	
	private IColumnPropertyAccessor<TableData> columnPropertyAccessor;

	public PlateTableConfigLabelAccumulator(ListDataProvider<TableData> bodyDataProvider2, IColumnPropertyAccessor<TableData> columnPropertyAccessor, Boolean rawData, Boolean masterTable, Double lowerThreshold, 
			Double stDevCutOff) {
		this.bodyDataProvider = bodyDataProvider2;
		this.rawData = rawData;
		this.lowerThreshold = lowerThreshold;
		this.stDevCutOff = stDevCutOff;
		this.columnPropertyAccessor = columnPropertyAccessor;
		this.masterTable = masterTable;
	}


	@Override
	public void accumulateConfigLabels(LabelStack configLabels, int columnIndex, int rowIndex) {
		TableData element =  bodyDataProvider.getRowObject(rowIndex);
		String columnName = getColumnProperty(columnIndex);
		if (columnName == null)
			return;
		switch (columnName) {
		case Config.GENEID:  // gene_id
			if (element instanceof Gene && !rawData) {
				if (masterTable) {
					if (((Gene) element).getRunId() > 0) // there is re-run data
						configLabels.addLabel (QrtPCRNatTable.GREENBACKGROUND);
				}
				else if (((Gene) element).getShouldRerun()) {
		        	configLabels.addLabel (QrtPCRNatTable.REDBACKGROUND);
		        }      
		    }
			if (element instanceof Gene)
				if (((Gene) element).getIsControl())
					configLabels.addLabel (QrtPCRNatTable.BOLDFONT);
			if (element instanceof MergeTableData) {
				if (((MergeTableData) element).getGene().getRunId() > 0) // there is re-run data
					configLabels.addLabel (QrtPCRNatTable.GREENBACKGROUND);
			}
			break;
		case Config.CT: // CT
			if (element instanceof GeneData && !rawData) {
				if (((GeneData) element).isEliminated()) {
					configLabels.addLabel(QrtPCRNatTable.STRIKE);
				}	
				else {
					configLabels.addLabel(QrtPCRNatTable.EDITABLE);
				}
			}
			
			if (element instanceof GeneData && !rawData) {
				Double ct0 = ((GeneData) element).getCt();
				if (ct0 != null) {
					List<CtHistory> prev = ((GeneData) element).getPreviousValues();
					// if prev value is null, different color
					if (prev != null && !prev.isEmpty() && prev.get(0) != null && prev.get(0).getCt() == null)
						configLabels.addLabel(QrtPCRNatTable.DARDREDFOREGROUND);
					else if (ct0 >= lowerThreshold)
						configLabels.addLabel(QrtPCRNatTable.REDFOREGROUND);
				} 
			}
			
			if (element instanceof GeneData && !rawData 
					&& ((GeneData) element).getPreviousValues() != null
					&& !((GeneData) element).getPreviousValues().isEmpty()) {
				// only when it is modified by calculations (not our replacement with lowerThreshold)
				// color it purple
				List<CtHistory> prev = ((GeneData) element).getPreviousValues();
				if (prev.size() == 1 && 
						((prev.get(0) != null && prev.get(0).getCt() != null &&  prev.get(0).getCt() >= lowerThreshold) ||
						prev.get(0) != null && prev.get(0).getCt() == null)) {
					// do nothing, original background
				}
				else
					configLabels.addLabel(QrtPCRNatTable.MAGENTABACKGROUND);
			}
			break;
		case Config.STDEV: // stDev
			if (element instanceof Gene && !rawData) {
				double stDev = ((Gene) element).getStandardDeviation(0, lowerThreshold, false);
				if (stDev >= stDevCutOff) {
					configLabels.addLabel(QrtPCRNatTable.REDFOREGROUND);
				}
			}
			break;
		case Config.SCALER:
			configLabels.addLabel(QrtPCRNatTable.LISTBACKGROUND);
			break;
		case Config.NORMALIZED:
			configLabels.addLabel(QrtPCRNatTable.YELLOWBACKGROUND);
			break;
		}

	}

	public String getColumnProperty(int columnIndex) {
		return columnPropertyAccessor.getColumnProperty(columnIndex);
	}
}
