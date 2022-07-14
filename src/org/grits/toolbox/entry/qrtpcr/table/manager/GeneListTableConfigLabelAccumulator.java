package org.grits.toolbox.entry.qrtpcr.table.manager;

import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyResolver;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.grits.toolbox.entry.qrtpcr.Config;
import org.grits.toolbox.entry.qrtpcr.model.TableData;
import org.grits.toolbox.entry.qrtpcr.model.view.GeneView;
import org.grits.toolbox.entry.qrtpcr.table.QrtPCRNatTable;

public class GeneListTableConfigLabelAccumulator implements IConfigLabelAccumulator {

	private IColumnPropertyResolver columnPropertyAccessor;
	private ListDataProvider<TableData> bodyDataProvider;

	public GeneListTableConfigLabelAccumulator (ListDataProvider<TableData> bodyDataProvider, IColumnPropertyAccessor<TableData> columnPropertyAccessor) {
		this.bodyDataProvider = bodyDataProvider;
		this.columnPropertyAccessor = columnPropertyAccessor;
	}
	
	@Override
	public void accumulateConfigLabels(LabelStack configLabels, int columnIndex, int rowIndex) {
		TableData element =  bodyDataProvider.getRowObject(rowIndex);
		String columnName = getColumnProperty(columnIndex);
		
		if (columnName == null)
			return;
		switch (columnName) {
		case Config.SELECTED:
			configLabels.addLabel(ManagerNatTable.SELECTEDEDITABLE);
		case Config.GENEID:
			configLabels.addLabel(QrtPCRNatTable.EDITABLE);
			break;
		case Config.GENESYMBOL:
			configLabels.addLabel(QrtPCRNatTable.EDITABLE);
			break;
		case Config.ID:
			configLabels.addLabel(QrtPCRNatTable.INTEGEREDITABLE);
			break;
		case Config.NAME:
			configLabels.addLabel(QrtPCRNatTable.EDITABLE);
			if (element instanceof GeneView) {
				if (((GeneView) element).isUpdated()) {
					if (((GeneView) element).getUpdatedFields().contains(Config.NAME))
						configLabels.addLabel(QrtPCRNatTable.REDBACKGROUND);
				}
			}
			break;
		case Config.FWPRIMER:
			configLabels.addLabel(QrtPCRNatTable.EDITABLE);
			break;
		case Config.REVPRIMER:
			configLabels.addLabel(QrtPCRNatTable.EDITABLE);
			break;
		case Config.DESCRIPTION:
			configLabels.addLabel(QrtPCRNatTable.EDITABLE);
			break;
		case Config.REFSEQ:
			configLabels.addLabel(QrtPCRNatTable.EDITABLE);
			if (element instanceof GeneView) {
				if (((GeneView) element).isUpdated()) {
					if (((GeneView) element).getUpdatedFields().contains(Config.REFSEQ))
						configLabels.addLabel(QrtPCRNatTable.REDBACKGROUND);
				}
			}
			break;
		case Config.GROUP:
			configLabels.addLabel(QrtPCRNatTable.EDITABLE);
			break;
		case Config.DESIGNEDFOR:
			configLabels.addLabel(QrtPCRNatTable.EDITABLE);
			break;
		case Config.ALIASES:
			configLabels.addLabel(QrtPCRNatTable.EDITABLE);
			if (element instanceof GeneView) {
				if (((GeneView) element).isUpdated()) {
					if (((GeneView) element).getUpdatedFields().contains(Config.ALIASES))
						configLabels.addLabel(QrtPCRNatTable.REDBACKGROUND);
				}
			}
			break;
		case Config.LOCATION:
			configLabels.addLabel(QrtPCRNatTable.EDITABLE);
			if (element instanceof GeneView) {
				if (((GeneView) element).isUpdated()) {
					if (((GeneView) element).getUpdatedFields().contains(Config.LOCATION))
						configLabels.addLabel(QrtPCRNatTable.REDBACKGROUND);
				}
			}
			break;
		case Config.SECONDARYREFSEQ:
			configLabels.addLabel(QrtPCRNatTable.EDITABLE);
			if (element instanceof GeneView) {
				if (((GeneView) element).isUpdated()) {
					if (((GeneView) element).getUpdatedFields().contains(Config.SECONDARYREFSEQ))
						configLabels.addLabel(QrtPCRNatTable.REDBACKGROUND);
				}
			}	
			break;
		case Config.CONTROL:
			configLabels.addLabel(QrtPCRNatTable.COMBOEDITABLE);
			break;
		}
	}

	private String getColumnProperty(int columnIndex) {
		return columnPropertyAccessor.getColumnProperty(columnIndex);
	}

}
