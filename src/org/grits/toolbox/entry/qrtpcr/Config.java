package org.grits.toolbox.entry.qrtpcr;

import java.net.URL;

import org.eclipse.core.runtime.Platform;

public class Config {

	public static final String folderName = "qrtPCR";
	public static final String entryNameExtension = "qrtPCR";
	public static final int FILE_NAME_RANDOM_CHARACTERS_LENGTH = 5;
	public static final String FILE_TYPE_OF_QRTPCR = ".xml";
	public static final String configFolderName="org.grits.toolbox.entry.qrtpcr";
	public static final String configSubFolder="sub-lists";
	public static final String configPlateLayoutSubFolder = "layouts";
	
	// column names
	public static final String SELECTED = "Select";
	public static final String ORDER = "order";
	public static final String NAME = "Name";
	public static final String ID = "ID";
	public static final String CT = "ct0";
	public static final String STDEV = "StDev";
	public static final String GENEID = "Gene Identifier";
	public static final String GENESYMBOL = "Gene Symbol";
	public static final String FWPRIMER = "Forward Primer";
	public static final String REVPRIMER = "Reverse Primer";
	public static final String DESCRIPTION = "Description";
	public static final String REFSEQ = "RefSeq";
	public static final String GROUP = "Group";
	public static final String AVERAGE = "Average";
	public static final String DESIGNEDFOR = "Notes";
	public static final String CONTROL = "Control?";
	public static final String ALIASES = "Aliases";
	public static final String LOCATION = "Genomic Location";
	public static final String SECONDARYREFSEQ = "Protein RefSeq";
	public static final String WELL = "Well";
	public static final String CT2 = "2^-ct";
	public static final String NORMVALUE = "Norm Value";
	public static final String NORMALIZED = "Normalized";
	public static final String SCALER = "Scaler";
	public static final String ADJUSTED = "Adjusted";
	public static final String STDEVADJUSTED = "St Dev - Adjusted";
	
	
	// labels
	public static final String RAWPLATE="raw";
	public static final String ANALYZEDPLATE = "analyzed";
	public static final URL FILE_URL = Platform.getBundle(Activator.PLUGIN_ID).getResource("files");
	public static final int DESCRIPTIONLENGTH = 10000;
	public static final String THRESHOLD="Maximum Cycles";
	public static final String STDEVCUTOFF = "Maximum Standard Deviation";
	
}
