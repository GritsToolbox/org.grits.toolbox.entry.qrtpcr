package org.grits.toolbox.entry.qrtpcr.table;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.grits.toolbox.entry.qrtpcr.Config;
import org.grits.toolbox.entry.qrtpcr.editor.QrtPCRTableEditor;
import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.QrtPCRData;
import org.grits.toolbox.entry.qrtpcr.model.TableData;

public class GeneMenuConfiguration  extends AbstractUiBindingConfiguration {
	private Menu bodyMenu;
	private QrtPCRTableEditor editor;
	private QrtPCRData data;
	private Boolean raw;
	protected DataLayer bodyLayer;

	public GeneMenuConfiguration(QrtPCRTableEditor editor, QrtPCRData data, final NatTable natTable, DataLayer bodyLayer, Boolean raw) {
		this.editor = editor;
		this.data = data;
		this.raw = raw;
		this.bodyLayer= bodyLayer;
		this.bodyMenu = createBodyMenu(natTable).build();

		natTable.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				bodyMenu.dispose();
			}
		});
	}

    // construct the menu
	private PopupMenuBuilder createBodyMenu(NatTable natTable) {
		Menu menu = new Menu(natTable);

        final MenuItem item3 = new MenuItem(menu, SWT.PUSH);
        item3.setText("Mark/Unmark as Rerun");
        item3.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				Gene clickedGene = (Gene)item3.getData();
				if (clickedGene != null) {
					List<Gene> genes = data.getGenes();
					for (Gene gene : genes) {
						if (gene.equals(clickedGene)) {
							if (gene.getShouldRerun())
								gene.setShouldRerun(false);
							else
								gene.setShouldRerun(true);
							break;
						}
					}
					natTable.refresh();
					((QrtPCRTableEditor)editor).markDirty();
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
        
        final MenuItem item4 = new MenuItem(menu, SWT.PUSH);
        item4.setText("Mark as Control");
        item4.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				Gene clickedGene = (Gene)item4.getData();
				if (clickedGene != null) {
					List<Gene> genes = data.getGenes();
					for (Gene gene : genes) {
						if (gene.getIsControl()) {
							gene.setIsControl(false);
						}
						if (gene.equals(clickedGene)) {
							gene.setIsControl(true);
						}
					}
					// need to recalculate the normalized value
					data.calculateNormValue();
					natTable.refresh();
					((QrtPCRTableEditor)editor).markDirty();
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});

		return new PopupMenuBuilder(natTable, menu);
	}

	@Override
	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
        // create a specific mouse event matcher to get right clicks on 
        // the first column only
		MouseEventMatcher matcher = new MouseEventMatcher(SWT.NONE, GridRegion.BODY, 3) {

			@SuppressWarnings("unchecked")
			@Override
			public boolean matches(NatTable natTable, MouseEvent event, LabelStack regionLabels) {
				if (super.matches(natTable, event, regionLabels)) {
					int columnPosition = natTable.getColumnPositionByX(event.x);
					int columnIndex = LayerUtil.convertColumnPosition(natTable, columnPosition, bodyLayer);
					String column = ((QrtPCRNatTable) natTable).getColumnPropertyAccessor().getColumnProperty(columnIndex); 
					if (column.equals(Config.GENEID)) {  // gene identifier
						if (!raw) {
							// get row index
							int rowPosition = natTable.getRowPositionByY(event.y);
							int bodyRowPos = LayerUtil.convertRowPosition(natTable, rowPosition, bodyLayer);
							TableData tableData = ((ListDataProvider<TableData>)bodyLayer.getDataProvider()).getRowObject(bodyRowPos);
							if (tableData instanceof Gene) {
		    					bodyMenu.getItems()[0].setData(tableData);
		    					bodyMenu.getItems()[1].setData(tableData);
								return true;
							}
						}
					}
				}
				return false;
			}

		};

		uiBindingRegistry.registerMouseDownBinding(matcher, new CellPopupMenuAction(bodyMenu));
	}
}
