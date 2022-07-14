package org.grits.toolbox.entry.qrtpcr.table.merge;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.grits.toolbox.display.control.table.datamodel.GRITSColumnHeader;
import org.grits.toolbox.entry.qrtpcr.Config;
import org.grits.toolbox.entry.qrtpcr.editor.MergeReportEditor;
import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.TableData;
import org.grits.toolbox.entry.qrtpcr.model.merge.MergeTableData;
import org.grits.toolbox.entry.qrtpcr.model.merge.MergedQrtPCR;
import org.grits.toolbox.entry.qrtpcr.preference.QrtPCRPreferencePage;

public class MergeReportTableBase {
	Double lowerThreshold=QrtPCRPreferencePage.THRESHOLD_PREFERENCE;
	Double stDevCutOff = QrtPCRPreferencePage.STDEVCUTOFF_PREFERENCE;
	
	MergedQrtPCR mergeData;
	
	private MergeReportEditor editor;
	
	public MergeReportTableBase(MergeReportEditor editor) {
		this.editor = editor;
	}
	
	public void setLowerThreshold(Double lowerThreshold) {
		this.lowerThreshold = lowerThreshold;
	}
	
	public void setStDevCutOff(Double stDevCutOff) {
		this.stDevCutOff = stDevCutOff;
	}
	
	public void setMergeData(MergedQrtPCR mergeData) {
		this.mergeData = mergeData;
	}
	
	public Control createControl(Composite parent) {
		List<List<GRITSColumnHeader>> columnList = new ArrayList<>();
		addHeaders(columnList, mergeData.getAliasList());
        
        // Underlying data source
        List<TableData>  allData = new ArrayList<TableData>();
        for (Gene gene: mergeData.getQrtPCRGeneListMap().keySet()) {
        	if (gene.getIsCommon() || gene.getIsControl())
        		continue;
        	MergeTableData myTableData = new MergeTableData(gene, mergeData.getQrtPCRGeneListMap().get(gene));
        	allData.add(myTableData);
        }
        
        List<String> aliasses = new ArrayList<>();
        for (String string : mergeData.getAliasList().values()) {
			aliasses.add(string);
		}
	
        MergeNatTable natTable = new MergeNatTable(parent, editor, columnList, aliasses);
		natTable.setMasterTable(true);
        natTable.setTableData(allData);
        natTable.setLowerThreshold(lowerThreshold);
        natTable.setStDevCutOff(stDevCutOff);
        natTable.setRawData(false);
        
        natTable.initializeComponents();		    
        return natTable;
	}
	
	private void addHeaders(List<List<GRITSColumnHeader>> columnList, Map<String, String> aliasList) {
		List<GRITSColumnHeader> firstLevelColumns = new ArrayList<>();
		GRITSColumnHeader header = new GRITSColumnHeader("", "Info");
		firstLevelColumns.add(header);
		firstLevelColumns.add(header);
		for (String key: aliasList.keySet()) {
			header = new GRITSColumnHeader(aliasList.get(key), aliasList.get(key));
			firstLevelColumns.add(header);
			firstLevelColumns.add(header);
		}
		
		List<GRITSColumnHeader> secondLevelColumns = new ArrayList<>();
		header = new GRITSColumnHeader(Config.GENEID, Config.GENEID);
		header.setIsGrouped(false);
		secondLevelColumns.add(header);
		header = new GRITSColumnHeader(Config.GENESYMBOL, Config.GENESYMBOL);
		header.setIsGrouped(false);
		secondLevelColumns.add(header);
		for (int i=0; i < aliasList.size(); i++) {
			header = new GRITSColumnHeader(Config.AVERAGE, Config.AVERAGE);
			secondLevelColumns.add(header);
			header = new GRITSColumnHeader(Config.STDEVADJUSTED, Config.STDEVADJUSTED);
			secondLevelColumns.add(header);	
		}
		columnList.add(firstLevelColumns);
		columnList.add(secondLevelColumns);
		
	}
}
