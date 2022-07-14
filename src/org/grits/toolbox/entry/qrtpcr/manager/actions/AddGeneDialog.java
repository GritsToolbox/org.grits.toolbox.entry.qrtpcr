package org.grits.toolbox.entry.qrtpcr.manager.actions;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.ncbi.NCBIGeneUtil;

public class AddGeneDialog extends FormDialog {
	
	private static Logger logger = Logger.getLogger(AddGeneDialog.class);
	
	//TableViewer viewer;
	
	private Text geneIdentifier;
	private Text geneSymbol;
	private Text description;
	private Text group;
	private Text fwPrimer;
	private Text revPrimer;
	private Text refSeq;
	private Gene gene;
	private Text secondaryRefSeq;
	private Text location;
	private Text aliases;
	private Text fullName;
	private Text geneId;

	private String organism;

	private List<Gene> geneList;

	public AddGeneDialog(Shell shell, List<Gene> input, String organism) {
		super(shell);
		gene = new Gene();
		this.geneList = input;
		this.organism = organism;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		getButton(IDialogConstants.OK_ID).setEnabled(false);
	}
	
	@Override
	protected void createFormContent(IManagedForm mform) {
		super.createFormContent(mform);
		mform.getForm().getBody().setLayout(new GridLayout(3, false));
		mform.getForm().setText("Enter Gene Information");
		
		final NCBIGeneUtil util = new NCBIGeneUtil();
		
		Label lblGeneIdentifier = new Label(mform.getForm().getBody(), SWT.NONE);
		lblGeneIdentifier.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		mform.getToolkit().adapt(lblGeneIdentifier, true, true);
		lblGeneIdentifier.setText("Identifier");
		
		geneIdentifier = new Text(mform.getForm().getBody(), SWT.BORDER);
		geneIdentifier.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		mform.getToolkit().adapt(geneIdentifier, true, true);
		
		geneIdentifier.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				gene.setGeneIdentifier(geneIdentifier.getText());
				if (geneIdentifier.getText().trim().length() > 0)
					getButton(IDialogConstants.OK_ID).setEnabled(true);
				else {
					getButton(IDialogConstants.OK_ID).setEnabled(false);
				}
			}
		});
		new Label(mform.getForm().getBody(), SWT.NONE);
		
		Label lblGeneSymbol = new Label(mform.getForm().getBody(), SWT.NONE);
		lblGeneSymbol.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		mform.getToolkit().adapt(lblGeneSymbol, true, true);
		lblGeneSymbol.setText("Gene Symbol");
		
		geneSymbol = new Text(mform.getForm().getBody(), SWT.BORDER);
		geneSymbol.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		mform.getToolkit().adapt(geneSymbol, true, true);
		
		geneSymbol.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				gene.setGeneSymbol(geneSymbol.getText());
			}
		});
		
		Button btnSearchNcbi = new Button(mform.getForm().getBody(), SWT.NONE);
		mform.getToolkit().adapt(btnSearchNcbi, true, true);
		btnSearchNcbi.setText("Search NCBI");
		
		btnSearchNcbi.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				String symbol = geneSymbol.getText();
				if (symbol != null && symbol.length() > 0) {
					try {
						Integer id = util.getGeneIdFromNCBI(symbol, organism);
						if (id == null) {
							MessageDialog.openInformation(getParentShell(), "Not Found", "Cannot find the information at NCBI gene database");
						}
						else {
							List<Integer> geneIdList = new ArrayList<>();
							geneIdList.add(id);
							gene.setGeneIds(geneIdList);
							geneId.setText(gene.getGeneIdString());
							Gene newGene = util.getDetailsFromNCBI(id);
							if (newGene != null) {
								gene.setFullName(newGene.getFullName());
								fullName.setText(gene.getFullName());
								gene.setLocations(newGene.getLocations());
								location.setText(gene.getLocationString());
								gene.setAliases(newGene.getAliases());
								aliases.setText(gene.getAliasString());
								gene.setRefSeq(newGene.getRefSeq());
								refSeq.setText(gene.getRefSeqString());
								gene.setSecondaryRefSeq(newGene.getSecondaryRefSeq());
								secondaryRefSeq.setText(gene.getSecondaryRefSeqString());
							}
							else {
								MessageDialog.openInformation(getParentShell(), "Not Found", "Details cannot be retrieved from NCBI gene database");
							}
						}
					} catch (MalformedURLException e1) {
						logger.error("NCBI gene search url is wrong.", e1);
						MessageDialog.openError(getParentShell(), "Error", "NCBI gene search URL is wrong. Please inform the developers!");
					} catch (Exception e1) {
						logger.error("NCBI gene search failed", e1);
						MessageDialog.openError(getParentShell(), "Error", "NCBI gene search URL failed.");
					}
				}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		Label lblGeneId = new Label(mform.getForm().getBody(), SWT.NONE);
		lblGeneId.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		mform.getToolkit().adapt(lblGeneId, true, true);
		lblGeneId.setText("Gene Id");
		
		geneId = new Text(mform.getForm().getBody(), SWT.BORDER);
		geneId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		mform.getToolkit().adapt(geneId, true, true);
		geneId.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				gene.setGeneIds(geneId.getText());
			}
		});
		
		Button btnSearchNcbi_1 = new Button(mform.getForm().getBody(), SWT.NONE);
		mform.getToolkit().adapt(btnSearchNcbi_1, true, true);
		btnSearchNcbi_1.setText("Search NCBI");
		btnSearchNcbi_1.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				String id = geneId.getText();
				if (id != null && id.length() > 0) {
					try {
						Integer idInt = Integer.parseInt(id);
						Gene newGene = util.getDetailsFromNCBI(idInt);
						if (newGene != null) {
							gene.setGeneSymbol(newGene.getGeneSymbol());
							geneSymbol.setText(gene.getGeneSymbol());
							gene.setFullName(newGene.getFullName());
							fullName.setText(gene.getFullName());
							gene.setLocations(newGene.getLocations());
							location.setText(gene.getLocationString());
							gene.setAliases(newGene.getAliases());
							aliases.setText(gene.getAliasString());
							gene.setRefSeq(newGene.getRefSeq());
							refSeq.setText(gene.getRefSeqString());
							gene.setSecondaryRefSeq(newGene.getSecondaryRefSeq());
							secondaryRefSeq.setText(gene.getSecondaryRefSeqString());
						}
						else {
							MessageDialog.openInformation(getParentShell(), "Not Found", "Details cannot be retrieved from NCBI gene database");
						}
					} catch (NumberFormatException e1) {
						MessageDialog.openError(getParentShell(), "Error", "Gene Id should be an integer!");
					} catch (MalformedURLException e1) {
						logger.error("NCBI gene search url is wrong.", e1);
						MessageDialog.openError(getParentShell(), "Error", "NCBI gene search URL is wrong. Please inform the developers!");
					} catch (Exception e1) {
						logger.error("NCBI gene search failed", e1);
						MessageDialog.openError(getParentShell(), "Error", "NCBI gene search URL failed.");
					}
				}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		Label lblFullName = new Label(mform.getForm().getBody(), SWT.NONE);
		lblFullName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		mform.getToolkit().adapt(lblFullName, true, true);
		lblFullName.setText("Full Name");
		
		fullName = new Text(mform.getForm().getBody(), SWT.BORDER);
		fullName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		mform.getToolkit().adapt(fullName, true, true);
		new Label(mform.getForm().getBody(), SWT.NONE);
		
		fullName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				gene.setFullName(fullName.getText());
			}
		});
		
		Label lblAliases = new Label(mform.getForm().getBody(), SWT.NONE);
		lblAliases.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		mform.getToolkit().adapt(lblAliases, true, true);
		lblAliases.setText("Aliases");
		
		aliases = new Text(mform.getForm().getBody(), SWT.BORDER);
		aliases.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		mform.getToolkit().adapt(aliases, true, true);
		new Label(mform.getForm().getBody(), SWT.NONE);
		
		aliases.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				String aliasText = aliases.getText();
				if (aliasText != null && aliasText.trim().length() > 0) 
					gene.setAliasString(aliasText);
			}
		});
		
		Label lblGenomicLocation = new Label(mform.getForm().getBody(), SWT.NONE);
		lblGenomicLocation.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		mform.getToolkit().adapt(lblGenomicLocation, true, true);
		lblGenomicLocation.setText("Genomic Location");
		
		location = new Text(mform.getForm().getBody(), SWT.BORDER);
		location.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		mform.getToolkit().adapt(location, true, true);
		new Label(mform.getForm().getBody(), SWT.NONE);
		
		location.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				String locationText = location.getText();
				if (locationText != null && locationText.trim().length() > 0) {
					gene.setLocationString(locationText);
				}
			}
		});
		
		Label lblDescription = new Label(mform.getForm().getBody(), SWT.NONE);
		lblDescription.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		mform.getToolkit().adapt(lblDescription, true, true);
		lblDescription.setText("Description");
		
		description = new Text(mform.getForm().getBody(), SWT.BORDER | SWT.MULTI);
		GridData gd_description = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_description.heightHint = 57;
		description.setLayoutData(gd_description);
		mform.getToolkit().adapt(description, true, true);
		
		description.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				gene.setDescription(description.getText());
			}
		});
		new Label(mform.getForm().getBody(), SWT.NONE);
		
		Label lblGroup = new Label(mform.getForm().getBody(), SWT.NONE);
		lblGroup.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		mform.getToolkit().adapt(lblGroup, true, true);
		lblGroup.setText("Group");
		
		group = new Text(mform.getForm().getBody(), SWT.BORDER);
		group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		mform.getToolkit().adapt(group, true, true);
		
		group.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				gene.setGroup(group.getText());
			}
		});
		new Label(mform.getForm().getBody(), SWT.NONE);
		
		Label lblForwardPrimer = new Label(mform.getForm().getBody(), SWT.NONE);
		lblForwardPrimer.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		mform.getToolkit().adapt(lblForwardPrimer, true, true);
		lblForwardPrimer.setText("Forward Primer");
		
		fwPrimer = new Text(mform.getForm().getBody(), SWT.BORDER);
		fwPrimer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		mform.getToolkit().adapt(fwPrimer, true, true);
		
		fwPrimer.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				gene.setForwardPrimer(fwPrimer.getText());
			}
		});
		new Label(mform.getForm().getBody(), SWT.NONE);
		
		Label lblReversePrimer = new Label(mform.getForm().getBody(), SWT.NONE);
		lblReversePrimer.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		mform.getToolkit().adapt(lblReversePrimer, true, true);
		lblReversePrimer.setText("Reverse Primer");
		
		revPrimer = new Text(mform.getForm().getBody(), SWT.BORDER);
		revPrimer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		mform.getToolkit().adapt(revPrimer, true, true);
		
		revPrimer.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				gene.setReversePrimer(revPrimer.getText());
			}
		});
		new Label(mform.getForm().getBody(), SWT.NONE);
		
		Label lblRefseq = new Label(mform.getForm().getBody(), SWT.NONE);
		lblRefseq.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		mform.getToolkit().adapt(lblRefseq, true, true);
		lblRefseq.setText("RefSeq");
		
		refSeq = new Text(mform.getForm().getBody(), SWT.BORDER);
		refSeq.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		mform.getToolkit().adapt(refSeq, true, true);
		
		Button btnSearchNcbi_2 = new Button(mform.getForm().getBody(), SWT.NONE);
		mform.getToolkit().adapt(btnSearchNcbi_2, true, true);
		btnSearchNcbi_2.setText("Search NCBI");
		btnSearchNcbi_2.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				String refSeqNo = refSeq.getText();
				if (refSeqNo != null && refSeqNo.length() > 0) {
					if (refSeqNo.contains(",")) {
						// get the first one
						refSeqNo = refSeqNo.substring(0, refSeqNo.indexOf(","));
					}
					try {
						Integer id = util.getGeneIdFromNCBIByRefSeq(refSeqNo, organism);
						if (id == null) {
							MessageDialog.openInformation(getParentShell(), "Not Found", "Cannot find the information at NCBI gene database by this refSeq number: " + refSeqNo);
						}
						else {
							List<Integer> geneIdList = new ArrayList<>();
							geneIdList.add(id);
							gene.setGeneIds(geneIdList);
							geneId.setText(gene.getGeneIdString());
							Gene newGene = util.getDetailsFromNCBI(id);
							if (newGene != null) {
								gene.setGeneSymbol(newGene.getGeneSymbol());
								geneSymbol.setText(gene.getGeneSymbol());
								gene.setFullName(newGene.getFullName());
								fullName.setText(gene.getFullName());
								gene.setLocations(newGene.getLocations());
								location.setText(gene.getLocationString());
								gene.setAliases(newGene.getAliases());
								aliases.setText(gene.getAliasString());
								gene.setRefSeq(newGene.getRefSeq());
								refSeq.setText(gene.getRefSeqString());
								gene.setSecondaryRefSeq(newGene.getSecondaryRefSeq());
								secondaryRefSeq.setText(gene.getSecondaryRefSeqString());
							}
							else {
								MessageDialog.openInformation(getParentShell(), "Not Found", "Details cannot be retrieved from NCBI gene database");
							}
						}
					} catch (MalformedURLException e1) {
						logger.error("NCBI gene search url is wrong.", e1);
						MessageDialog.openError(getParentShell(), "Error", "NCBI gene search URL is wrong. Please inform the developers!");
					} catch (Exception e1) {
						logger.error("NCBI gene search failed", e1);
						MessageDialog.openError(getParentShell(), "Error", "NCBI gene search URL failed.");
					}
				}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		refSeq.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				String refSeqText = refSeq.getText();
				if (refSeqText != null && refSeqText.trim().length() > 0) {
					gene.setRefSeq(refSeqText);
				}
			}
		});
		
		
		Label lblProteinRefseq = new Label(mform.getForm().getBody(), SWT.NONE);
		lblProteinRefseq.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		mform.getToolkit().adapt(lblProteinRefseq, true, true);
		lblProteinRefseq.setText("Protein RefSeq");
		
		secondaryRefSeq = new Text(mform.getForm().getBody(), SWT.BORDER);
		secondaryRefSeq.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		mform.getToolkit().adapt(secondaryRefSeq, true, true);
		new Label(mform.getForm().getBody(), SWT.NONE);
		new Label(mform.getForm().getBody(), SWT.NONE);
		
		secondaryRefSeq.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				String refSeqText = secondaryRefSeq.getText();
				if (refSeqText != null && refSeqText.trim().length() > 0) {
					gene.setSecondaryRefSeq(refSeqText);
				}
			}
		});
		
		Label lblNote = new Label(mform.getForm().getBody(), SWT.NONE);
		lblNote.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		mform.getToolkit().adapt(lblNote, true, true);
		lblNote.setText("If you are entering multiple values, please separate them with a comma");
	}
	
	public Gene getGene() {
		return gene;
	}
	
	@Override
	protected void okPressed() {
		boolean exists = false;
		for (Gene gene2 : geneList) {
			if (gene2.getGeneIdentifier().equals(gene.getGeneIdentifier())) {
				logger.info("A gene with identifier " + gene.getGeneIdentifier() + " already exists in the list");
				MessageDialog.openWarning(Display.getCurrent().getActiveShell(), "Warning", "A gene with this identifier already exists!");
				exists = true;
			}
			if (gene2.getGeneSymbol().equals(gene.getGeneSymbol())) {
				logger.info("A gene with symbol " + gene.getGeneSymbol() + " already exists in the list");
				boolean addDuplicate = MessageDialog.openQuestion(Display.getCurrent().getActiveShell(), "Warning", "A gene with this symbol already exists! Do you still want to add the gene?");
				exists = !addDuplicate;
			}
		}
		if (!exists)
			super.okPressed();
	};
	
}
