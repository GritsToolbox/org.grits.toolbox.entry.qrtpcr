package org.grits.toolbox.entry.qrtpcr.table;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections4.comparators.NullComparator;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsSortModel;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.grits.toolbox.entry.qrtpcr.Config;
import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.GeneData;
import org.grits.toolbox.entry.qrtpcr.model.TableData;
import org.grits.toolbox.entry.qrtpcr.table.merge.MergeReportColumnPropertyAccessor;

import ca.odell.glazedlists.SortedList;

public class PlateTableSortModel<T> extends GlazedListsSortModel<T> {

	public PlateTableSortModel(SortedList<T> sortedList, IColumnPropertyAccessor<T> columnAccessor,
			IConfigRegistry configRegistry, ILayer dataLayer) {
		super(sortedList, columnAccessor, configRegistry, dataLayer);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public List<Comparator> getComparatorsForColumnIndex(int columnIndex) {
		String columnName="";
		if (columnAccessor instanceof PlateTableColumnPropertyAccessor)
			columnName = ((PlateTableColumnPropertyAccessor)columnAccessor).getColumnProperty(columnIndex);
		else if (columnAccessor instanceof MergeReportColumnPropertyAccessor)
			columnName = ((MergeReportColumnPropertyAccessor) columnAccessor).getColumnProperty(columnIndex);
		if (columnName.equals(Config.WELL) || columnName.equals(Config.CT) || columnName.equals(Config.CT2)) {
			String column = columnName;
			Comparator<TableData> ownComparator = new Comparator<TableData>() {
				@Override
				public int compare(TableData e1, TableData e2) {
					
					int c=0;
					
					if (e1 instanceof Gene && e2 instanceof Gene) {
						int i=0;
						for (GeneData geneData : ((Gene)e1).getDataMap().get(0)) {
							return compare(geneData, ((Gene)e2).getDataMap().get(0).get(i++));
						}
					}
					if (e1 instanceof GeneData && e2 instanceof GeneData) {
						switch (column)  {
						case Config.WELL:
							c = ((GeneData)e1).getPosition().getX().compareTo(((GeneData)e2).getPosition().getX());
							if (c == 0)
								c = ((GeneData)e1).getPosition().getY().compareTo(((GeneData)e2).getPosition().getY());
							break;
						case Config.CT:
							c = ((GeneData)e1).getCt().compareTo(((GeneData)e2).getCt());
							if (c == 0) {
								c = ((GeneData)e1).getGene().getGeneIdentifier().compareTo(((GeneData)e2).getGene().getGeneIdentifier());
							}
							break;
						case Config.CT2:
							c = ((GeneData)e1).get2PowCt().compareTo(((GeneData)e2).get2PowCt());
							if (c == 0) {
								c = ((GeneData)e1).getGene().getGeneIdentifier().compareTo(((GeneData)e2).getGene().getGeneIdentifier());
							}
							break;
						}
					}
					
					return c;
				}
			};
			
			List<Comparator> comparators = new ArrayList<>();
			comparators.add(ownComparator);
			return comparators;
		}
		else if (columnName.equals(Config.NORMALIZED) || columnName.equals(Config.NORMVALUE) || columnName.equals(Config.ADJUSTED) || columnName.equals(Config.SCALER)) {  // no sorting on these columns
			List<Comparator> comparators = new ArrayList<>();
			comparators.add(new NullComparator<>());
			return comparators;
		}
		return super.getComparatorsForColumnIndex(columnIndex);
	}

}
