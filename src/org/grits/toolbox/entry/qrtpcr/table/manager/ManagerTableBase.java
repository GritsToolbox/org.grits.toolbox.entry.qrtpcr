package org.grits.toolbox.entry.qrtpcr.table.manager;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.grits.toolbox.display.control.table.datamodel.GRITSColumnHeader;
import org.grits.toolbox.entry.qrtpcr.Config;
import org.grits.toolbox.entry.qrtpcr.manager.GeneListManagerEditor;
import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.GeneList;
import org.grits.toolbox.entry.qrtpcr.model.TableData;

public class ManagerTableBase {
	
	GeneList geneList;
	private GeneListManagerEditor editor;
	
	public ManagerTableBase(GeneListManagerEditor editor) {
		this.editor = editor;
	}
	
	public void setGeneList(GeneList geneList) {
		this.geneList = geneList;
	}
	
	
	public Control createControl(Composite parent) {
		List<List<GRITSColumnHeader>> columnList = new ArrayList<>();
		List<GRITSColumnHeader> firstLevelColumns = new ArrayList<>();
        addColumns(firstLevelColumns);
        columnList.add(firstLevelColumns);
        
        // Underlying data source
        List<TableData>  allData = new ArrayList<TableData>();
        for (Gene gene : geneList.getGenes()) {
			allData.add(gene);
		}
        
        ManagerNatTable natTable = new ManagerNatTable(parent, editor, columnList);
        natTable.setTableData(allData);
        natTable.setGeneList(geneList);
       
        natTable.initializeComponents();
        
        return natTable;
    }

	
	private void addColumns(List<GRITSColumnHeader> columnList) {
		GRITSColumnHeader header = new GRITSColumnHeader(Config.SELECTED, Config.SELECTED);
		columnList.add(header);
	    header = new GRITSColumnHeader(Config.GENEID, Config.GENEID);
		columnList.add(header);
		header = new GRITSColumnHeader(Config.GENESYMBOL, Config.GENESYMBOL);
		columnList.add(header);
		header = new GRITSColumnHeader(Config.ID, Config.ID);
		columnList.add(header);
		header = new GRITSColumnHeader(Config.NAME, Config.NAME);
		columnList.add(header);
		header = new GRITSColumnHeader(Config.DESCRIPTION, Config.DESCRIPTION);
		columnList.add(header);
		header = new GRITSColumnHeader(Config.DESIGNEDFOR, Config.DESIGNEDFOR);
		columnList.add(header);
		header = new GRITSColumnHeader(Config.FWPRIMER, Config.FWPRIMER);
		columnList.add(header);
		header = new GRITSColumnHeader(Config.REVPRIMER, Config.REVPRIMER);
		columnList.add(header);
		header = new GRITSColumnHeader(Config.GROUP, Config.GROUP);
		columnList.add(header);
		header = new GRITSColumnHeader(Config.ALIASES, Config.ALIASES);
		columnList.add(header);
		header = new GRITSColumnHeader(Config.LOCATION, Config.LOCATION);
		columnList.add(header);
		header = new GRITSColumnHeader(Config.REFSEQ, Config.REFSEQ);
		columnList.add(header);
		header = new GRITSColumnHeader(Config.SECONDARYREFSEQ, Config.SECONDARYREFSEQ);
		columnList.add(header);	
		header = new GRITSColumnHeader(Config.CONTROL, Config.CONTROL);
		columnList.add(header);
	}
}
