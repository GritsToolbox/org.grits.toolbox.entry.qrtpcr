package org.grits.toolbox.entry.qrtpcr.table;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.grits.toolbox.display.control.table.datamodel.GRITSColumnHeader;
import org.grits.toolbox.entry.qrtpcr.Config;
import org.grits.toolbox.entry.qrtpcr.editor.QrtPCRTableEditor;
import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.QrtPCRData;
import org.grits.toolbox.entry.qrtpcr.model.TableData;
import org.grits.toolbox.entry.qrtpcr.preference.QrtPCRPreferencePage;

public class PlateTableBase {
	
	Boolean rawData=true;
	Double lowerThreshold=QrtPCRPreferencePage.THRESHOLD_PREFERENCE;
	Double stDevCutOff = QrtPCRPreferencePage.STDEVCUTOFF_PREFERENCE;
	
	QrtPCRData data;
	private QrtPCRTableEditor editor;
	private Integer runId;
	
	
	public PlateTableBase(QrtPCRTableEditor editor) {
		this.editor = editor;
	}

	public void setLowerThreshold(Double lowerThreshold) {
		this.lowerThreshold = lowerThreshold;
	}
	
	public void setStDevCutOff(Double stDevCutOff) {
		this.stDevCutOff = stDevCutOff;
	}
	
	public void setRawData(boolean rawData) {
		this.rawData = rawData;
	}
	
	public void setData(QrtPCRData data) {
		this.data = data;
	}
	
	

	public Control createControl(Composite parent) {
		List<List<GRITSColumnHeader>> columnList = new ArrayList<>();
		List<GRITSColumnHeader> firstLevelColumns = new ArrayList<>();
        addColumns(firstLevelColumns);
        columnList.add(firstLevelColumns);
        
        // Underlying data source
        List<TableData>  allData = new ArrayList<TableData>();
        for (Gene gene : data.getGenes()) {
			allData.add(gene);
			allData.addAll(gene.getDataMap().get(0));
		}
        
        QrtPCRNatTable natTable = new QrtPCRNatTable(parent, editor, columnList);
        natTable.setTableData(allData);
        natTable.setLowerThreshold(lowerThreshold);
        natTable.setStDevCutOff(stDevCutOff);
        natTable.setRawData(rawData);
        natTable.setRunId(runId);
        natTable.setQrtPCRData(data);
        
        natTable.initializeComponents();
        
        return natTable;
    }

	
	private void addColumns(List<GRITSColumnHeader> columnList) {
		GRITSColumnHeader header = new GRITSColumnHeader(Config.WELL, Config.WELL);
		columnList.add(header);
		header = new GRITSColumnHeader(Config.GENEID, Config.GENEID);
		columnList.add(header);
		header = new GRITSColumnHeader(Config.CT, Config.CT);
		columnList.add(header);
		header = new GRITSColumnHeader(Config.STDEV, Config.STDEV);
		columnList.add(header);
		header = new GRITSColumnHeader(Config.CT2, Config.CT2);
		columnList.add(header);
		header = new GRITSColumnHeader(Config.NORMVALUE, Config.NORMVALUE);
		columnList.add(header);
		header = new GRITSColumnHeader(Config.NORMALIZED, Config.NORMALIZED);
		columnList.add(header);
		header = new GRITSColumnHeader(Config.SCALER, Config.SCALER);
		columnList.add(header);
		header = new GRITSColumnHeader(Config.ADJUSTED, Config.ADJUSTED);
		columnList.add(header);
		header = new GRITSColumnHeader(Config.AVERAGE, Config.AVERAGE);
		columnList.add(header);
		header = new GRITSColumnHeader(Config.STDEVADJUSTED, Config.STDEVADJUSTED);
		columnList.add(header);	
	}

	public void setRunId(Integer runId) {
		this.runId = runId;
	}
}