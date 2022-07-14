package org.grits.toolbox.entry.qrtpcr.table;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.grits.toolbox.display.control.table.datamodel.GRITSColumnHeader;
import org.grits.toolbox.entry.qrtpcr.Config;
import org.grits.toolbox.entry.qrtpcr.editor.QrtPCRTableEditor;
import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.TableData;
import org.grits.toolbox.entry.qrtpcr.preference.QrtPCRPreferencePage;

public class MasterTableBase {
	Double lowerThreshold=QrtPCRPreferencePage.THRESHOLD_PREFERENCE;
	Double stDevCutOff = QrtPCRPreferencePage.STDEVCUTOFF_PREFERENCE;
	
	List<Gene> geneList;
	private QrtPCRTableEditor editor;
	
	public MasterTableBase(QrtPCRTableEditor editor) {
		this.editor = editor;
	}
	
	public void setLowerThreshold(Double lowerThreshold) {
		this.lowerThreshold = lowerThreshold;
	}
	
	public void setStDevCutOff(Double stDevCutOff) {
		this.stDevCutOff = stDevCutOff;
	}
	
	public void setGeneList(List<Gene> geneList) {
		this.geneList = geneList;
	}
	
	public Control createControl(Composite parent) {
		List<List<GRITSColumnHeader>> columnList = new ArrayList<>();
		List<GRITSColumnHeader> firstLevelColumns = new ArrayList<>();
        addColumns(firstLevelColumns);
        columnList.add(firstLevelColumns);
        
        // Underlying data source
        List<TableData>  allData = new ArrayList<TableData>();
        for (Gene gene : geneList) {
        	if (gene.getIsCommon() || gene.getIsControl())
        		continue;
        	allData.add(gene);
		}
	
		QrtPCRNatTable natTable = new QrtPCRNatTable(parent, editor, columnList);
		natTable.setMasterTable(true);
        natTable.setTableData(allData);
        natTable.setLowerThreshold(lowerThreshold);
        natTable.setStDevCutOff(stDevCutOff);
        natTable.setRawData(false);
        
        natTable.initializeComponents();		    
        return natTable;
	}
	
	private void addColumns(List<GRITSColumnHeader> columnList) {
		GRITSColumnHeader header = new GRITSColumnHeader(Config.GENEID, Config.GENEID);
		columnList.add(header);
		header = new GRITSColumnHeader(Config.GENESYMBOL, Config.GENESYMBOL);
		columnList.add(header);
		header = new GRITSColumnHeader(Config.AVERAGE, Config.AVERAGE);
		columnList.add(header);
		header = new GRITSColumnHeader(Config.STDEVADJUSTED, Config.STDEVADJUSTED);
		columnList.add(header);	
	}
	
	
}
