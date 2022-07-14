package org.grits.toolbox.entry.qrtpcr.dialogs;

import org.eclipse.jface.wizard.Wizard;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.entry.qrtpcr.model.QrtPCRTable;
import org.grits.toolbox.entry.qrtpcr.preference.QrtPCRPreferencePage;

public class UploadDataWizard extends Wizard {
	
	QrtPCRDataEntryPage pageZero;
	UploadqrtPCRDataDialogPageOne pageOne;
	UploadqrtPCRDataDialogPageTwo pageTwo;
	
	QrtPCRTable table;
	Entry sampleEntry;
	String entryName;
	String masterListFile=null;
	
	Boolean forRerun = false;
	
	public QrtPCRTable getTable() {
		return table;
	}

	public Entry getSampleEntry() {
		return sampleEntry;
	}

	public void setSampleEntry(Entry sampleEntry) {
		this.sampleEntry = sampleEntry;
	}

	public String getEntryName() {
		return entryName;
	}

	public void setEntryName(String entryName) {
		this.entryName = entryName;
	}

	public Boolean getForRerun() {
		return forRerun;
	}

	public void setForReRerun(Boolean forRerun) {
		this.forRerun = forRerun;
	}

	public String getMasterListFile() {
		return masterListFile;
	}

	public void setMasterListFile(String masterListFile) {
		this.masterListFile = masterListFile;
	}

	public UploadDataWizard() {
		super();
	    setNeedsProgressMonitor(true);
	}
	
	@Override
	public String getWindowTitle() {
		return "Upload qrtPCR Data";
	}

	@Override
	public void addPages() {
		QrtPCRPreferencePage.loadPreferences();
		if (!forRerun)
			pageZero = new QrtPCRDataEntryPage("Entry info", sampleEntry);
		pageOne = new UploadqrtPCRDataDialogPageOne("Plate and Gene Info", forRerun);
		pageTwo = new UploadqrtPCRDataDialogPageTwo("Data file");
		if (!forRerun) addPage(pageZero);
		addPage(pageOne);
		addPage(pageTwo);
	}

	@Override
	public boolean performFinish() {
		if (!forRerun) {
			entryName = pageZero.getEntryName();
			sampleEntry = pageZero.getParentEntry();
			masterListFile = pageOne.getMasterListFile();
		}
		table = pageTwo.getQrtPCRTable();
		return true;
	}

	public Double getThresholdSetting() {
		if (pageZero != null)
			return pageZero.getThreshold();
		return QrtPCRPreferencePage.THRESHOLD_PREFERENCE;
	}

	public Double getCutOffSetting() {
		if (pageZero != null) 
			return pageZero.getStDevCutOff();
		return QrtPCRPreferencePage.STDEVCUTOFF_PREFERENCE;
	}
}
