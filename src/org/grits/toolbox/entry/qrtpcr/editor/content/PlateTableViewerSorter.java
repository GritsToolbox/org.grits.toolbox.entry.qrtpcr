package org.grits.toolbox.entry.qrtpcr.editor.content;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.GeneData;

public class PlateTableViewerSorter extends ViewerSorter {
	
	private Boolean rawData;
	private int propertyIndex;
	private static final int DESCENDING = 1;
	private int direction = DESCENDING;
	
	Double lowerThreshold;
	Double stDevCutOff;

	public PlateTableViewerSorter(Boolean rawData) {
		this.rawData = rawData;
		this.propertyIndex = 0;
	    direction = DESCENDING;
	}
	
	public void setLowerThreshold(Double lowerThreshold) {
		this.lowerThreshold = lowerThreshold;
	}
	
	public void setStDevCutOff(Double stDevCutOff) {
		this.stDevCutOff = stDevCutOff;
	}
	
	@Override
	public int category(Object element) {
		if (element instanceof Gene) 
			return 0;
		else if (element instanceof GeneData)
			return 1;
		else return 2;
	}
	
	public int getDirection() {
	    return direction == 1 ? SWT.DOWN : SWT.UP;
	  }

	public void setColumn(int column) {
		if (column == this.propertyIndex) {
			// Same column as last sort; toggle the direction
			direction = -1 * direction;
		} else {
			// New column; do an ascending sort
			this.propertyIndex = column;
			direction = DESCENDING;
		}
	}
	
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		int c = 0;
		
		switch (this.propertyIndex) {
		case 0: // well
			if (e1 instanceof Gene && e2 instanceof Gene) {
				int i=0;
				for (GeneData geneData : ((Gene)e1).getDataMap().get(0)) {
					return compare(viewer, geneData, ((Gene)e2).getDataMap().get(0).get(i++));
				}
			}
			if (e1 instanceof GeneData && e2 instanceof GeneData) {
				c = ((GeneData)e1).getPosition().getX().compareTo(((GeneData)e2).getPosition().getX());
				if (c == 0)
					c = ((GeneData)e1).getPosition().getY().compareTo(((GeneData)e2).getPosition().getY());
			}
		case 1: // gene identifier
			if (e1 instanceof Gene && e2 instanceof Gene) {
				c = ((Gene)e1).getGeneIdentifier().compareTo(((Gene)e2).getGeneIdentifier());
			}
			break;
		case 3: // stdev
			if (e1 instanceof Gene && e2 instanceof Gene) {
				Double d1;
				if (rawData)
					d1 = ((Gene)e1).getStandardDeviation(0, lowerThreshold, true);
				else
					d1 = ((Gene)e1).getStandardDeviation(0, lowerThreshold, false);
				Double d2;
				if (rawData)
					d2 = ((Gene)e2).getStandardDeviation(0, lowerThreshold, true);
				else
					d2 = ((Gene)e2).getStandardDeviation(0, lowerThreshold, false);
				c = d1.compareTo(d2);
			}
			break;
		case 9 : // adjusted average
			if (e1 instanceof Gene && e2 instanceof Gene) {
				Double d1;
				if (rawData)
					d1 = ((Gene) e1).getAdjustedAverage(0, true, lowerThreshold, ((Gene)e1).getNormValue(0));
				else
					d1 = ((Gene) e1).getAdjustedAverage(0, false, lowerThreshold, ((Gene)e1).getNormValue(0));
				Double d2;
				if (rawData)
					d2 = ((Gene) e2).getAdjustedAverage(0, true, lowerThreshold,  ((Gene)e1).getNormValue(0));
				else
					d2 = ((Gene) e2).getAdjustedAverage(0, false, lowerThreshold,  ((Gene)e1).getNormValue(0));
				c = d1.compareTo(d2);
			}
			break;
		case 10 : // adjusted-stdev
			if (e1 instanceof Gene && e2 instanceof Gene) {
				Double d1;
				if (rawData)
					d1 = ((Gene) e1).getStDevForAdjusted(0, true, lowerThreshold,  ((Gene)e1).getNormValue(0));
				else 
					d1= ((Gene) e1).getStDevForAdjusted(0, false, lowerThreshold, ((Gene)e1).getNormValue(0));
				Double d2;
				if (rawData)
					d2 = ((Gene) e2).getStDevForAdjusted(0, true, lowerThreshold, ((Gene)e1).getNormValue(0));
				else 
					d2= ((Gene) e2).getStDevForAdjusted(0, false, lowerThreshold, ((Gene)e1).getNormValue(0));
				c = d1.compareTo(d2);
			}	
			break;
		}
		return direction == DESCENDING ? c : -c;
	}

}
