package org.grits.toolbox.entry.qrtpcr.table.manager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.grits.toolbox.display.control.table.datamodel.GRITSColumnHeader;
import org.grits.toolbox.entry.qrtpcr.Config;
import org.grits.toolbox.entry.qrtpcr.manager.GeneListManagerEditor;
import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.TableData;

public class GeneListTableColumnPropertyAccessor implements IColumnPropertyAccessor<TableData> {

	List<GRITSColumnHeader> columnList;
	private GeneListManagerEditor editor;
	Map<TableData, Boolean> selectedMap = new LinkedHashMap<>();
	
	public GeneListTableColumnPropertyAccessor(GeneListManagerEditor editor, List<GRITSColumnHeader> columns) {
		this.editor = editor;
		this.columnList = columns;
	}
	
	@Override
	public int getColumnCount() {
		return columnList.size();
	}

	@Override
	public Object getDataValue(TableData element, int columnIndex) {
		String columnName = getColumnProperty(columnIndex);
		switch (columnName) {
		case Config.SELECTED:
			if (selectedMap.containsKey(element))
				return selectedMap.get(element);
			else
				selectedMap.put(element, new Boolean(false));
			return false;
		case Config.GENEID:
			if (element instanceof Gene) {
				return ((Gene) element).getGeneIdentifier();
			}
			else
				return null;
		case Config.GENESYMBOL:
			if (element instanceof Gene) {
				return ((Gene) element).getGeneSymbol();
			}
			else
				return null;
		case Config.ID:
			if (element instanceof Gene) {
				if (((Gene)element).getGeneIds() != null)
					return ((Gene) element).getGeneIdString();
				else 
					return null;
			}
			else
				return null;
		case Config.NAME:
			if (element instanceof Gene) {
				return ((Gene) element).getFullName();
			}
			else
				return null;
		case Config.FWPRIMER:
			if (element instanceof Gene) {
				return ((Gene) element).getForwardPrimer();
			}
			else
				return null;
		case Config.REVPRIMER:
			if (element instanceof Gene) {
				return ((Gene) element).getReversePrimer();
			}
			else
				return null;
		case Config.DESCRIPTION:
			if (element instanceof Gene) {
				return ((Gene) element).getDescription();
			}
			else
				return null;
		case Config.REFSEQ:
			if (element instanceof Gene) {
				return ((Gene) element).getRefSeqString();
			}
			else
				return null;
		case Config.GROUP:
			if (element instanceof Gene) {
				return ((Gene) element).getGroup();
			}
			else
				return null;
		case Config.DESIGNEDFOR:
			if (element instanceof Gene) {
				return ((Gene) element).getNotes();
			}
			else
				return null;
		case Config.ALIASES:
			if (element instanceof Gene) {
				return ((Gene) element).getAliasString();
			}
			else
				return null;
		case Config.LOCATION:
			if (element instanceof Gene) {
				return ((Gene) element).getLocationString();
			}
			else
				return null;
		case Config.SECONDARYREFSEQ:
			if (element instanceof Gene) {
				return ((Gene) element).getSecondaryRefSeqString();
			}
			else
				return null;
		case Config.CONTROL:
			if (element instanceof Gene) {
				return ((Gene) element).getIsCommon() ? "Control" : "";
			}
			else
				return null;
		}
		return null;		
	}

	@Override
	public void setDataValue(TableData element, int columnIndex, Object value) {
		String oldValue = null;
		if (element == null)
			return;
		
		String columnName = getColumnProperty(columnIndex);
		if (columnName.equals(Config.SELECTED)) { // this is boolean
			if (value != null && value instanceof Boolean) {
				if (selectedMap.containsKey(element))
					selectedMap.remove(element);
				selectedMap.put(element, (Boolean)value);	
			}
			return;
		} 
		if (value == null)  value ="";
		String newValue = (String)value;
		if (element instanceof Gene) {
			switch (columnName) {
			case Config.GENEID:
				oldValue = ((Gene) element).getGeneIdentifier();
				((Gene) element).setGeneIdentifier(newValue.trim());
				break;
			case Config.GENESYMBOL:
				oldValue = ((Gene) element).getGeneSymbol();
				((Gene) element).setGeneSymbol(newValue.trim());
				break;
			case Config.ID:
				if (((Gene)element).getGeneIds() != null)
					oldValue = ((Gene)element).getGeneIdString();
				((Gene)element).setGeneIds(newValue.trim());
				break;
			case Config.NAME:
				oldValue = ((Gene) element).getFullName();
				((Gene) element).setFullName(newValue.trim());  
				break;
			case Config.FWPRIMER:
				oldValue = ((Gene) element).getForwardPrimer();
				((Gene) element).setForwardPrimer(newValue.trim()); 
				break;
			case Config.REVPRIMER:
				oldValue = ((Gene) element).getReversePrimer();
				((Gene) element).setReversePrimer(newValue.trim());    
				break;
			case Config.DESCRIPTION:
				oldValue = ((Gene) element).getDescription();
				((Gene) element).setDescription(newValue.trim());    
				break;
			case Config.REFSEQ:
				oldValue = ((Gene) element).getRefSeqString();
				((Gene) element).setRefSeq(newValue.trim());    
				break;
			case Config.GROUP:
				oldValue = ((Gene) element).getGroup();
				((Gene) element).setGroup(newValue.trim());  
				break;
			case Config.DESIGNEDFOR:
				oldValue = ((Gene) element).getNotes();
				((Gene) element).setNotes(newValue.trim()); 
				break;
			case Config.ALIASES:
				oldValue = ((Gene) element).getAliasString();
				((Gene) element).setAliasString(newValue.trim());   
				break;
			case Config.LOCATION:
				oldValue = ((Gene) element).getLocationString();
				((Gene) element).setLocationString(newValue.trim()); 
				break;
			case Config.SECONDARYREFSEQ:
				oldValue = ((Gene) element).getSecondaryRefSeqString();
				((Gene) element).setSecondaryRefSeq(newValue.trim());    
				break;
			case Config.CONTROL:
				if (((Gene)element).getIsCommon()) {
					oldValue = "Control";
				}else 
					oldValue = "";
				if (newValue != null && newValue.equals("Control"))
					((Gene) element).setIsCommon(true);
				else 
					((Gene) element).setIsCommon(false);
				break;
			}
			
			if ((oldValue == null && !newValue.trim().isEmpty()) 
			    || (oldValue != null && !oldValue.equals(newValue))) {
		    	//mark dirty
		    	this.editor.markDirty();
			}
		}
	}

	@Override
	public int getColumnIndex(String column) {
		int index=0;
		for (GRITSColumnHeader simianColumnHeader : columnList) {
			if (simianColumnHeader.getKeyValue().equals(column))
				return index;
			index++;
		}
		return -1;
	}

	@Override
	public String getColumnProperty(int index) {
		return columnList.get(index).getKeyValue();
	}

	public void selectAll(List<TableData> rows) {
		for (TableData row: rows) {
			selectedMap.put(row, new Boolean(true));	
		}
		
	}
	
	public void clearAllSelections(List<TableData> rows) {
		for (TableData row: rows) {
			selectedMap.put(row, new Boolean(false));	
		}
	}

	public List<TableData> getSelected() {
		List<TableData> selected = new ArrayList<TableData>();
		for (Iterator<TableData> iterator = selectedMap.keySet().iterator(); iterator.hasNext();) {
			TableData row = (TableData) iterator.next();
			if (selectedMap.get(row) != null && selectedMap.get(row)) 
				selected.add(row);
		}
		return selected;
	}

	public void selectRow(TableData row) {
		if (selectedMap.get(row) != null) {
			selectedMap.put(row, new Boolean(true));
		}
		
	}

}
