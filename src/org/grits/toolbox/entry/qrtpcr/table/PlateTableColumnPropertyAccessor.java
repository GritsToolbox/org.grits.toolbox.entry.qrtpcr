package org.grits.toolbox.entry.qrtpcr.table;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.grits.toolbox.display.control.table.datamodel.GRITSColumnHeader;
import org.grits.toolbox.entry.qrtpcr.Config;
import org.grits.toolbox.entry.qrtpcr.editor.QrtPCRTableEditor;
import org.grits.toolbox.entry.qrtpcr.model.CtHistory;
import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.GeneData;
import org.grits.toolbox.entry.qrtpcr.model.QrtPCRData;


public class PlateTableColumnPropertyAccessor<TableData> implements IColumnPropertyAccessor<TableData> {

	List<GRITSColumnHeader> propertyNames;
	Boolean rawData = true;
	Double lowerThreshold;
	private QrtPCRTableEditor editor;
	private QrtPCRData data;
	private Integer runId=0;
	
	Boolean masterTable = false;
	
	public PlateTableColumnPropertyAccessor(QrtPCRTableEditor editor, QrtPCRData qrtPCRData, List<GRITSColumnHeader> columns, Integer runId, Boolean rawData, Boolean masterTable, Double lowerThreshold) {
		this.editor = editor;
		this.rawData = rawData;
		this.propertyNames = columns;
		this.lowerThreshold = lowerThreshold;
		this.runId = runId;
		this.data = qrtPCRData;
		this.masterTable = masterTable;
	}
	
	@Override
	public int getColumnCount() {
		return propertyNames.size();
	}
	
	public void setMasterTable(Boolean masterTable) {
		this.masterTable = masterTable;
	}

	@Override
	public Object getDataValue(TableData element, int columnIndex) {
		String columnName = propertyNames.get(columnIndex).getKeyValue();
		switch (columnName) {
		case Config.WELL:
			if (element instanceof GeneData) {
				return ((GeneData) element).getPosition().toString();
			}
			else
				return null;
		case Config.GENEID:
			if (element instanceof GeneData) {
				//return ((GeneData) element).getGene().getGeneIdentifier();
				return null;
			} else if (element instanceof Gene) {
				return ((Gene) element).getGeneIdentifier();
			}
			break;
		case Config.CT:
			if (element instanceof GeneData) {
				Double ct0 = null;
				if (!rawData)
					ct0 = ((GeneData) element).getCt();
				else
					ct0 = ((GeneData) element).getOriginalCt();
				if (ct0 != null) {
					return String.valueOf(ct0);
				}
				else 
					return lowerThreshold + "";  //should not happen since we replace all nulls with lowerThreshold
			}
			else
				return null;
		case Config.GENESYMBOL:
			if (element instanceof Gene) {
				return ((Gene) element).getGeneSymbol();
			}
			else
				return null;
		case Config.STDEV:
			if (element instanceof Gene) {
				if (rawData)
					return String.valueOf(((Gene)element).getStandardDeviation(0, lowerThreshold, true));
				else
					return String.valueOf(((Gene)element).getStandardDeviation(0, lowerThreshold, false));
			} else
				return null;
		case Config.CT2:
			if (element instanceof GeneData) {
				Double d = ((GeneData) element).get2PowCt(rawData);
				if (d != null)
					return String.valueOf(d);
				else 
					return "";
			}
			else
				return null;
		case Config.NORMVALUE:
			if (element instanceof Gene) {
				if (rawData && data!= null) {
					return String.valueOf(data.findNormValue(0, true));
				} else
					return String.valueOf(((Gene) element).getNormValue(0));
			}
			return "";
		case Config.NORMALIZED:
			if (element instanceof GeneData) {
				Double d = ((GeneData) element).get2PowCt(rawData);
				if (d != null) {
					if (rawData && data != null)  // in order to handle cases where the control genes' values are averaged
						return String.valueOf (d / data.findNormValue(null, true) );
					else
						return String.valueOf( d / ((GeneData)element).getGene().getNormValue(0));
				}
			}
			return "";
		case Config.SCALER:
			if (element instanceof Gene) {
				if (rawData && data != null) 
					return String.valueOf(((Gene) element).getScaler(0, lowerThreshold, data.findNormValue(0, true)));
				else
					return String.valueOf(((Gene) element).getScaler(0, lowerThreshold, data.findNormValue(0, false)));
			}
			return "";
		case Config.ADJUSTED:
			if (element instanceof GeneData) {
				Double d = ((GeneData) element).get2PowCt(rawData);
				if (d != null) {
					Gene g = ((GeneData) element).getGene();
					if (rawData && data != null)  {// in order to handle cases where the control genes' values are averaged
						Double normValue = data.findNormValue(0, true);
						return String.valueOf((d / normValue) - g.getScaler(0, lowerThreshold, normValue));
					}
					else
						return String.valueOf( (d / g.getNormValue(0)) - g.getScaler(0, lowerThreshold, g.getNormValue(0)));
				}
			}
			return "";
		case Config.AVERAGE:
			if (element instanceof Gene) {
				Double d= null;
				if (rawData && data != null)
					d = ((Gene) element).getAdjustedAverage(0, true, lowerThreshold, data.findNormValue(0, true));
				else if (masterTable)
					d = ((Gene) element).getAdjustedAverage(((Gene) element).getRunId(), lowerThreshold, ((Gene)element).getNormValue(((Gene)element).getRunId()));
				else
					d = ((Gene) element).getAdjustedAverage(0, false, lowerThreshold, ((Gene)element).getNormValue(0));
				if (d != null) {
					return String.valueOf(d);
				}
			}
			return "";
		case Config.STDEVADJUSTED:
			if (element instanceof Gene) {
				Double d = null;
				if (rawData && data != null)
					d = ((Gene) element).getStDevForAdjusted(0, true, lowerThreshold, data.findNormValue(0, true));
				else if (masterTable) 
					d = ((Gene) element).getStDevForAdjusted(((Gene) element).getRunId(), lowerThreshold, ((Gene)element).getNormValue(((Gene)element).getRunId()));
				else
					d = ((Gene) element).getStDevForAdjusted(0, false, lowerThreshold, ((Gene)element).getNormValue(0));
				if (d != null) {
					return String.valueOf(d);
				}
			}
			return "";
		}
			
		
		return null;
	}

	@Override
	public void setDataValue(TableData element, int columnIndex, Object value) {
		String columnName = propertyNames.get(columnIndex).getKeyValue();
		if (columnName.equals(Config.CT)) {  // only this column is editable
			if (element != null && element instanceof GeneData) {
				Double oldValue = ((GeneData) element).getCt();
				Double newValue = null;
				if (value != null && !((String)value).trim().isEmpty())
					newValue = Double.parseDouble((String)value);
				((GeneData) element).setCt(newValue);
			    
			    if ((oldValue == null && newValue != null) 
			    	|| (oldValue != null && !oldValue.equals(newValue))) {
			    	((GeneData) element).addPreviousValue(oldValue, CtHistory.Reason.USERCHANGE.getReason());
			    	if (((GeneData)element).getGene().getIsControl()) {
			    		// then need to recalculate the normalized value
			    		if (this.data != null)
			    			data.calculateNormValue();
			    	}
			    	editor.updateFirstRun(((GeneData)element).getGene(), runId);
			    	editor.markDirty();
			    }
			}
		}
	}

	@Override
	public int getColumnIndex(String propertyName) {
		int index=0;
		for (GRITSColumnHeader simianColumnHeader : propertyNames) {
			if (simianColumnHeader.getKeyValue().equals(propertyName))
				return index;
			index++;
		}
		return -1;
	}

	@Override
	public String getColumnProperty(int columnIndex) {
		return propertyNames.get(columnIndex).getKeyValue();
	}

}
