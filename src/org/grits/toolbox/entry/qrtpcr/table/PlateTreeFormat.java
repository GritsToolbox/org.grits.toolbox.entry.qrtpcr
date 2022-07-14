package org.grits.toolbox.entry.qrtpcr.table;

import java.util.Comparator;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.sort.ISortModel;
import org.eclipse.nebula.widgets.nattable.tree.SortableTreeComparator;
import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.GeneData;
import org.grits.toolbox.entry.qrtpcr.model.TableData;
import org.grits.toolbox.entry.qrtpcr.model.merge.MergeTableData;

import ca.odell.glazedlists.TreeList;

public class PlateTreeFormat implements TreeList.Format<TableData>{

        private final ISortModel sortModel;
        
        public PlateTreeFormat(ISortModel model) {
        	this.sortModel = model;
		}

        @Override
        public void getPath(List<TableData> path, TableData element) {
        	
        	if (element instanceof Gene) {
        		path.add(element);
        	}
        	else if (element instanceof GeneData) {
        		path.add(element);
        		path.add(((GeneData) element).getGene());
        	}
        	else if (element instanceof MergeTableData)
        		path.add(element);
            java.util.Collections.reverse(path);
        }

        @Override
        public boolean allowsChildren(TableData element) {
            return true;
        }
        

        @Override
        public Comparator<TableData> getComparator(int depth) {
            Comparator<TableData> test = new Comparator<TableData>() {
            	
				@Override
            	public int compare(TableData e1, TableData e2) {
			        	
		        	int c=0;
					
					if (e1 instanceof Gene && e2 instanceof Gene) {
						int i=0;
						if (((Gene)e1).getDataMap() != null && !((Gene)e1).getDataMap().isEmpty()) {
							for (GeneData geneData : ((Gene)e1).getDataMap().get(0)) {
								return compare(geneData, ((Gene)e2).getDataMap().get(0).get(i++));
							}
						} else {
							c =  ((Gene) e1).compareTo((Gene) e2);
						}
					}
					if (e1 instanceof GeneData && e2 instanceof GeneData) {
						c = ((GeneData)e1).getPosition().getX().compareTo(((GeneData)e2).getPosition().getX());
						if (c == 0)
							c = ((GeneData)e1).getPosition().getY().compareTo(((GeneData)e2).getPosition().getY());
					}
					
					if (e1 instanceof MergeTableData && e2 instanceof MergeTableData)
						c =  ((MergeTableData) e1).getGene().compareTo(((MergeTableData) e2).getGene());
					
					return c;
			        	
            	}
            };
            
            return new SortableTreeComparator<TableData>(
                    test,
                    this.sortModel);
        }
}
