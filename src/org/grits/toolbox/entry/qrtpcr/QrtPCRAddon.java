package org.grits.toolbox.entry.qrtpcr;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.grits.toolbox.entry.qrtpcr.util.FileUtils;

public class QrtPCRAddon {
	
	private static final Logger logger = Logger.getLogger(QrtPCRAddon.class);

	@Inject
	@Optional
	public void applicationStarted(IEclipseContext eclipseContext)
	{
		try
		{
			logger.info("Loading QrtPCR Addon");

			eclipseContext.set(FileUtils.class,
					ContextInjectionFactory.make(FileUtils.class, eclipseContext));

			logger.info("QrtPCR Addon loaded");
		} catch (Exception ex)
		{
			logger.error(ex.getMessage(), ex);
		}
	}

}
