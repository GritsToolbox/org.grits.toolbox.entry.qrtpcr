package org.grits.toolbox.entry.qrtpcr.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.grits.toolbox.entry.qrtpcr.Config;
import org.grits.toolbox.entry.qrtpcr.editor.QrtPCRTableEditor;
import org.grits.toolbox.entry.qrtpcr.editor.dialogs.ShowPreviousValuesDialog;
import org.grits.toolbox.entry.qrtpcr.model.CtHistory;
import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.GeneData;
import org.grits.toolbox.entry.qrtpcr.model.QrtPCRData;
import org.grits.toolbox.entry.qrtpcr.model.TableData;

class ValueMenuConfiguration extends AbstractUiBindingConfiguration {

	private Menu bodyMenu;
	private QrtPCRTableEditor editor;
	private QrtPCRData data;
	private DataLayer bodyLayer;
	boolean raw = false;

	public ValueMenuConfiguration(QrtPCRTableEditor editor, QrtPCRData data, final NatTable natTable, DataLayer bodyLayer, boolean raw) {
		this.editor = editor;
		this.data = data;
		this.bodyLayer = bodyLayer;
		this.bodyMenu = createBodyMenu(natTable).build();
		this.raw = raw;

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

		 final MenuItem item2 = new MenuItem(menu, SWT.PUSH);
	        item2.setText("Show Previous Values");
	        item2.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					GeneData clickedGeneData = (GeneData)item2.getData();
					if (clickedGeneData != null) {
						// locate the data and show the dialog with the previous values
						// position of the cursor
						Point pt = Display.getCurrent().getCursorLocation();
						showPreviousValues (data, clickedGeneData, pt);
					}
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					
				}
			});
	        
	        final MenuItem item1_2 = new MenuItem(menu, SWT.PUSH);
	        item1_2.setText("Eliminate/Consider This Value");
	        item1_2.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					GeneData clickedGeneData = (GeneData)item1_2.getData();
					if (clickedGeneData != null) {
						eliminateThisDataPoint(data, natTable, clickedGeneData);
					}
				}
				
				public void widgetDefaultSelected(SelectionEvent e) {};
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
					if (column.equals(Config.CT)) { 
						if (!raw) {
							// get row index
							int rowPosition = natTable.getRowPositionByY(event.y);
							
							int bodyRowPos = LayerUtil.convertRowPosition(natTable, rowPosition, bodyLayer);
							TableData row = ((ListDataProvider<TableData>)bodyLayer.getDataProvider()).getRowObject(bodyRowPos);
							if (row instanceof GeneData) {
		    					bodyMenu.getItems()[0].setData(row);
		    					bodyMenu.getItems()[1].setData(row);
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
	
	protected void eliminateThisDataPoint(QrtPCRData data, NatTable natTable, GeneData clickedGeneData) {
		List<Gene> genes = data.getGenes();
		for (Gene gene : genes) {
			if (gene.equals(clickedGeneData.getGene())) {
				for (Iterator<Integer> iterator = gene.getDataMap().keySet().iterator(); iterator.hasNext();) {
					Integer runId= (Integer) iterator.next();
					List<GeneData> values = gene.getDataMap().get(runId);
					for (GeneData geneData : values) {
						if (geneData.equals(clickedGeneData)) {
							if (geneData.isEliminated())
								geneData.setEliminated(false);
							else  {
								geneData.setEliminated(true);
								if (gene.getIsControl()) {
									// need to recalculate normValue
									data.calculateNormValue();
								}
							}
							
							natTable.refresh();
							((QrtPCRTableEditor)editor).markDirty();
							break;
						}
					}
				}
			}
		}
	}

	protected void showPreviousValues(QrtPCRData data, GeneData clickedGeneData, Point curLoc) {
		HashMap<Integer, List<CtHistory>> previousValuesMap = new HashMap<>();
		List<Gene> genes = data.getGenes();
		int position = -1;
		for (Gene gene : genes) {
			if (gene.equals(clickedGeneData.getGene())) {
				for (Iterator<Integer> iterator = gene.getDataMap().keySet().iterator(); iterator.hasNext();) {
					Integer runId= (Integer) iterator.next();
					List<GeneData> values = gene.getDataMap().get(runId);
					int i=0;
					for (GeneData geneData : values) {
						if (geneData.equals(clickedGeneData) || i == position) {    // for reruns, the clickedGeneData's relative position is considered
							position = i;
							List<CtHistory> previousValues = new ArrayList<>();
							if (geneData.getCt() == null) {
								CtHistory current = new CtHistory();
								current.setCt(null);
								current.setReasonCode(CtHistory.Reason.NULL.getReason());
								//previousValues.add(Double.valueOf(QrtPCRRun.lowerThreshold));
								previousValues.add(current);
							}
							else {
								CtHistory current = new CtHistory();
								current.setCt(geneData.getCt());
								current.setReasonCode(CtHistory.Reason.CURRENT.getReason());
								previousValues.add(current);
							}
							if (geneData.getPreviousValues() != null) {
								List<CtHistory> existingPreviousValues = geneData.getPreviousValues();
								for (int k=existingPreviousValues.size()-1; k >=0; k--) {
									previousValues.add(existingPreviousValues.get(k));
								}
							}
							previousValuesMap.put(runId, previousValues);
						} 
						i++;
					}
					
				}
			}
		}
		
		ShowPreviousValuesDialog dialog = new ShowPreviousValuesDialog (Display.getCurrent().getActiveShell(), curLoc, previousValuesMap);
		dialog.open();
		
	}
}