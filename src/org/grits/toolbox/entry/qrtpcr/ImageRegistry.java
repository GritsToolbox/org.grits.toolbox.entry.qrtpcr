/**
 * 
 */
package org.grits.toolbox.entry.qrtpcr;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;

public class ImageRegistry
{
	private static Logger logger = Logger.getLogger(ImageRegistry.class);
	private static final String IMAGE_PATH = "icons" + File.separator;
	private static Map<QrtPCRImage, ImageDescriptor> imageCache = new HashMap<QrtPCRImage, ImageDescriptor>();

	public static ImageDescriptor getImageDescriptor(QrtPCRImage image)
	{
		logger.info("Get image from qrtpcr plugin : " + image);

		ImageDescriptor imageDescriptor = null;
		if(image != null)
		{
			imageDescriptor = imageCache.get(image);
			if(imageDescriptor == null)
			{
				logger.info("ImageDescriptor not found in cache");
				URL fullPathString = FileLocator.find(
						Platform.getBundle(Activator.PLUGIN_ID), new Path(IMAGE_PATH + image.iconName), null);

				logger.info("Loading image from url : " + fullPathString);
				if(fullPathString != null)
				{
					imageDescriptor = ImageDescriptor.createFromURL(fullPathString);
					imageCache.put(image, imageDescriptor);
				}
			}
		}
		else
			logger.error("Cannot load image from qrtpcr plugin (image name is null)");

		return imageDescriptor;
	}


	/**
	 ***********************************
	 *			Icons
	 ***********************************
	 */
	public enum QrtPCRImage
	{
		PLUGIN_ICON("qrtPCR.png"),
		ADD_ICON ("circle-add-icon.png"),
	    DELETE_ICON ("red-cross-icon.png"),
		DOWNLOAD_ICON ("download-arrow.png"),
		SAVE_ICON ("save-16.png"),
		UP_ICON ("uparrow.png"),
		DOWN_ICON ("downarrow.png"),
		FORWARD_ICON ("double-arrow-down.png"),
		EXPORT_ICON( "excel.png"),
		IMPORT_ICON("import.png"),
		SELECT_ICON ("selectall.png"),
		HISTOGRAM_ICON ("chart.png"),
		FOLDCHANGE_ICON ("statistics.png"),
		SHOW_SELECTED_ICON ("search.png"),
		UPDATE_ICON ("refresh-icon.png"),
		TICK_ICON("tick.png");
		

		private String iconName = null;
		private QrtPCRImage(String iconName)
		{
			this.iconName  = iconName;
		}
	}


}
