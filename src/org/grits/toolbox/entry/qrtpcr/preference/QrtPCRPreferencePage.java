package org.grits.toolbox.entry.qrtpcr.preference;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.preference.share.PreferenceEntity;
import org.grits.toolbox.core.preference.share.PreferenceReader;
import org.grits.toolbox.core.preference.share.PreferenceWriter;
import org.grits.toolbox.entry.qrtpcr.Config;

public class QrtPCRPreferencePage extends PreferencePage {
	
	private static final Logger logger = Logger.getLogger(QrtPCRPreferencePage.class);
	
	public static Double THRESHOLD_PREFERENCE=35.0;
	public static Double STDEVCUTOFF_PREFERENCE=0.5;

	public static String PREFERENCE_NAME_THRESHOLD = QrtPCRPreferencePage.class.getName() + ".threshold";
	public static String PREFERENCE_NAME_STDEVCUTOFF = QrtPCRPreferencePage.class.getName() + ".stdevcutoff";
	
	

	private Text thresholdText;
	private Text stDevCutoffText;
	private ControlDecoration dec;
	private ControlDecoration dec2;
	
	public static void loadPreferences() {
		initThreshold();
		initStDevCutOff();
	}
	
	private static void initThreshold()
	{
		try
		{
			try
			{
				PreferenceEntity preferenceEntity = PreferenceReader.getPreferenceByName(PREFERENCE_NAME_THRESHOLD);
				if(preferenceEntity != null)
				{
					THRESHOLD_PREFERENCE = getDoubleValue(preferenceEntity.getValue());
				}
			} catch (UnsupportedVersionException uEx)
			{
				logger.error("This version is not supported!", uEx);
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
			logger.error("Error getting the Preference variable for Number Of Components", ex);
		}
	}
	
	private static void initStDevCutOff()
	{
		try
		{
			try
			{
				PreferenceEntity preferenceEntity = PreferenceReader.getPreferenceByName(PREFERENCE_NAME_STDEVCUTOFF);
				if(preferenceEntity != null)
				{
					STDEVCUTOFF_PREFERENCE = getDoubleValue(preferenceEntity.getValue());
				}
			} catch (UnsupportedVersionException uEx)
			{
				logger.error("This version is not supported!", uEx);
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
			logger.error("Error getting the Preference variable for Number Of Components", ex);
		}
	}
	
	private static double getDoubleValue(String value)
	{
		double doubleValue = 1;
		try
		{
			doubleValue = value == null ? doubleValue : Double.parseDouble(value);
		} catch (NumberFormatException ex)
		{
			logger.error("Could not parse the threshold/stdevcutoff value from the workspace. " 
					+ value + "\n" + ex.getMessage(), ex);
		}
		return doubleValue;
	}
	
	
	
	@Override
	protected Control createContents(Composite parent) {
		loadPreferences();
		
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginRight = 8;
		layout.verticalSpacing = 20;
		layout.horizontalSpacing = 20;
		layout.numColumns = 2;
		container.setLayout(layout);

		Label thresholdLabel = new Label(container, SWT.BOLD | SWT.LEFT);
		thresholdLabel.setText(Config.THRESHOLD);
		thresholdLabel.setToolTipText("Default value for the threshold. If any of the values are bigger than the threshold they will be set to the threshold");
		thresholdText = new Text(container, SWT.BORDER|SWT.BOLD);
		thresholdText.setText(THRESHOLD_PREFERENCE+"");
		
		// Create a control decoration for the control.
		dec = new ControlDecoration(thresholdText, SWT.CENTER);
		// Specify the decoration image and description
		Image image = JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_ERROR);
		dec.setImage(image);
		dec.setDescriptionText("Should enter a positive floating point");
		dec.hide();
		
		thresholdText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				Text newText = (Text) e.widget;
				String newValue = newText.getText();
				if (newValue != null)
					newValue = newValue.trim();
				try {
					Double newDouble = Double.parseDouble(newValue);
					if (newDouble <= 0)
						dec.show();
					else {
						THRESHOLD_PREFERENCE = newDouble;
						dec.hide();
					}
				} catch (NumberFormatException ex) {
					dec.show();
				}
				
			}
		});
		
		Label stDevLabel = new Label(container, SWT.BOLD | SWT.LEFT);
		stDevLabel.setText(Config.STDEVCUTOFF);
		stDevLabel.setToolTipText("Standard Deviation cut-off value. Entries having standard deviations more than this cutoff will be highlighted");
		stDevCutoffText = new Text(container, SWT.BORDER|SWT.BOLD);
		stDevCutoffText.setText(STDEVCUTOFF_PREFERENCE+"");
		
		// Create a control decoration for the control.
		dec2 = new ControlDecoration(stDevCutoffText, SWT.CENTER);
		// Specify the decoration image and description
		dec2.setImage(image);
		dec2.setDescriptionText("Should enter a positive floating point number");
		dec2.hide();
		
		stDevCutoffText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				Text newText = (Text) e.widget;
				String newValue = newText.getText();
				if (newValue != null)
					newValue = newValue.trim();
				try {
					Double newDouble = Double.parseDouble(newValue);
					if (newDouble <= 0)
						dec2.show();
					else {
						STDEVCUTOFF_PREFERENCE = newDouble;
						dec2.hide();
					}
				} catch (NumberFormatException ex) {
					dec2.show();
				}
				
			}
		});
		
		return container;
	}
	
	
	@Override
	public boolean performOk()
	{
		return save();
	}

	private boolean save() {
		PreferenceEntity preferenceEntity1 = new PreferenceEntity(PREFERENCE_NAME_THRESHOLD);
		preferenceEntity1.setValue(THRESHOLD_PREFERENCE + "");
		PreferenceEntity preferenceEntity2 = new PreferenceEntity(PREFERENCE_NAME_STDEVCUTOFF);
		preferenceEntity2.setValue(STDEVCUTOFF_PREFERENCE + "");
		return PreferenceWriter.savePreference(preferenceEntity1) 
				&& PreferenceWriter.savePreference(preferenceEntity2);
	}
	
	@Override
	protected void performDefaults()
	{
		thresholdText.setText("35.0");
		stDevCutoffText.setText("0.5");
	}

}
