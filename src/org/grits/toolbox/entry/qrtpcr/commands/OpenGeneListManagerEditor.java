package org.grits.toolbox.entry.qrtpcr.commands;
import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.impl.PartStackImpl;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.grits.toolbox.core.dataShare.IGritsConstants;
import org.grits.toolbox.core.service.IGritsUIService;
import org.grits.toolbox.entry.qrtpcr.manager.GeneListManagerEditor;

@SuppressWarnings("restriction")
public class OpenGeneListManagerEditor {
	
	private Logger logger = Logger.getLogger(OpenGeneListManagerEditor.class);

	@Execute
	public Object execute(EModelService modelService, EPartService partService,
			MApplication application, IGritsUIService gritsUiService){
		logger.debug("START COMMAND : Open Gene List Manager Editor ...");
		MPart managerPart = partService.findPart(GeneListManagerEditor.ID);

		if(managerPart == null)
		{
			logger.debug("Gene list manager part not found. Creating Gene List Manager part");
			managerPart = partService.createPart(GeneListManagerEditor.ID);
	
			logger.debug("Adding gene list manager to partstack - e4.primaryDataStack");
			PartStackImpl partStackImpl = (PartStackImpl) modelService.find(
					IGritsUIService.PARTSTACK_PRIMARY_DATA, application);
			partStackImpl.getChildren().add(managerPart);
		}

		partService.showPart(managerPart, PartState.ACTIVATE);
		gritsUiService.selectPerspective(IGritsConstants.ID_DEFAULT_PERSPECTIVE);

        logger.debug("...END COMMAND : Open Gene List Manager Editor");
        return null;
	}

}
