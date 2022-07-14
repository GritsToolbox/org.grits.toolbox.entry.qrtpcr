package org.grits.toolbox.entry.qrtpcr.table.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.grits.toolbox.entry.qrtpcr.Config;
import org.grits.toolbox.entry.qrtpcr.manager.GeneListManagerEditor;
import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.GeneList;
import org.grits.toolbox.entry.qrtpcr.model.TableData;
import org.grits.toolbox.entry.qrtpcr.model.view.GeneView;
import org.grits.toolbox.entry.qrtpcr.table.CellPopupMenuAction;

import ca.odell.glazedlists.TreeList;

public class ChangeMenuConfiguration extends AbstractUiBindingConfiguration {
	private Menu bodyMenu;
	private GeneListManagerEditor editor;
	GeneList data;
	protected DataLayer bodyLayer;
	
	public ChangeMenuConfiguration(GeneListManagerEditor editor, GeneList data, final NatTable natTable, DataLayer bodyLayer) {
		this.editor = editor;
		this.data = data;
		this.bodyLayer= bodyLayer;
		this.bodyMenu = createBodyMenu(natTable).build();

		natTable.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				bodyMenu.dispose();
			}
		});
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
					if (natTable instanceof ManagerNatTable) {
						int columnIndex = LayerUtil.convertColumnPosition(natTable, columnPosition, bodyLayer);
						String column = ((ManagerNatTable) natTable).getColumnPropertyAccessor().getColumnProperty(columnIndex); 
						int rowPosition = natTable.getRowPositionByY(event.y);
						int bodyRowPos = LayerUtil.convertRowPosition(natTable, rowPosition, bodyLayer);
						TableData tableData = ((ListDataProvider<TableData>)bodyLayer.getDataProvider()).getRowObject(bodyRowPos);
						if (tableData instanceof GeneView) {
							if (((GeneView)tableData).isUpdated() && (
		    						(column.equals(Config.NAME) && ((GeneView)tableData).getUpdatedFields().contains(Config.NAME)) ||
		    						(column.equals(Config.ALIASES) && ((GeneView)tableData).getUpdatedFields().contains(Config.ALIASES)) ||
		    						(column.equals(Config.LOCATION) && ((GeneView)tableData).getUpdatedFields().contains(Config.LOCATION)) ||
		    						(column.equals(Config.REFSEQ) && ((GeneView)tableData).getUpdatedFields().contains(Config.REFSEQ)) ||
		    						(column.equals(Config.GENESYMBOL) && ((GeneView)tableData).getUpdatedFields().contains(Config.GENESYMBOL)) ||
		    						(column.equals(Config.SECONDARYREFSEQ) && ((GeneView)tableData).getUpdatedFields().contains(Config.SECONDARYREFSEQ))
		    						)) {
								bodyMenu.getItems()[0].setData(tableData);
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
	
	
	// construct the menu
	private PopupMenuBuilder createBodyMenu(NatTable natTable) {
		Menu menu = new Menu(natTable);

        final MenuItem item1 = new MenuItem(menu, SWT.PUSH);
        item1.setText("Accept Change");
	    item1.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					GeneView geneView = (GeneView)item1.getData();
					Integer columnPosition = (Integer) menu.getData("columnIndex");
					int columnIndex = LayerUtil.convertColumnPosition(natTable, columnPosition, bodyLayer);
					String column = ((ManagerNatTable) natTable).getColumnPropertyAccessor().getColumnProperty(columnIndex);  
					geneView.removeUpdatedField(column);
					List<TableData> dataFromTable = ((ManagerNatTable)natTable).getTreeList().subList(0, ((ManagerNatTable)natTable).getTreeList().size());
					List<Gene> geneList = new ArrayList<Gene>();
					for (TableData td : dataFromTable) {
						geneList.add((Gene)td);
					}
					ListIterator<Gene> listIterator = geneList.listIterator();
				    while (listIterator.hasNext()) {
				    	Gene gene2 = listIterator.next();
				    	if (gene2.getGeneIdentifier().equals(geneView.getGeneIdentifier())) {
				    		listIterator.set(geneView);
				    		updateInGeneList(geneView);
				    		break;
				    	}
				    }
				    
				    editor.refreshGeneListPage(geneList);
					((GeneListManagerEditor)editor).markDirty();
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {	
				}
	    });
	    
	    final MenuItem item2 = new MenuItem(menu, SWT.PUSH);
        item2.setText("Reject Change");
	    item2.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					GeneView geneView = (GeneView)item1.getData();
					Integer columnPosition = (Integer) menu.getData("columnIndex");
					int columnIndex = LayerUtil.convertColumnPosition(natTable, columnPosition, bodyLayer);
					String column = ((ManagerNatTable) natTable).getColumnPropertyAccessor().getColumnProperty(columnIndex); 
					geneView.removeUpdatedField(column);
					Gene original = findInGeneList(geneView);
					if (original == null) {
						MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error", "Cannot revert back to the original.");
						return;
					}
					switch (column) {
						case Config.NAME:
							geneView.setFullName(original.getFullName());
							break;
						case Config.ALIASES:
							geneView.setAliases(original.getAliases());
							break;
						case Config.LOCATION:
							geneView.setLocations(original.getLocations());
							break;
						case Config.GENESYMBOL:
							geneView.setGeneSymbol(original.getGeneSymbol());
							break;
						case Config.REFSEQ:
							geneView.setRefSeq(original.getRefSeq());
							break;
						case Config.SECONDARYREFSEQ:
							geneView.setSecondaryRefSeq(original.getSecondaryRefSeq());
							break;
					}
					List<TableData> dataFromTable = ((ManagerNatTable)natTable).getTreeList().subList(0, ((ManagerNatTable)natTable).getTreeList().size());
					List<Gene> geneList = new ArrayList<Gene>();
					for (TableData td : dataFromTable) {
						geneList.add((Gene)td);
					}
					ListIterator<Gene> listIterator = geneList.listIterator();
				    while (listIterator.hasNext()) {
				    	Gene gene2 = listIterator.next();
				    	if (gene2.getGeneIdentifier().equals(geneView.getGeneIdentifier())) {
				    		listIterator.set(geneView);
				    		break;
				    	}
				    }
				    editor.refreshGeneListPage(geneList);
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
	    });
	    
	    final MenuItem item3 = new MenuItem(menu, SWT.PUSH);
        item3.setText("Accept All Changes");
	    item3.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (natTable instanceof ManagerNatTable) {
						TreeList<TableData> treeList = ((ManagerNatTable) natTable).getTreeList();
						List<Gene> geneList = new ArrayList<Gene>();
						for (TableData tableData: treeList) {
							geneList.add((Gene)tableData);
							if (tableData instanceof GeneView) {
								GeneView geneView = (GeneView)tableData;
								geneView.setUpdated(false);		
							}
						}
						data.setGenes(geneList);
						editor.refreshGeneListPage(geneList);
						((GeneListManagerEditor)editor).markDirty();
					}
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {	
				}
	    });
	    
	    return new PopupMenuBuilder(natTable, menu);
	}
	
	
	private void updateInGeneList(GeneView geneView) {
		if (data != null) {
			ListIterator<Gene> listIterator = data.getGenes().listIterator();
		    while (listIterator.hasNext()) {
		    	Gene gene2 = listIterator.next();
		    	if (gene2.getGeneIdentifier().equals(geneView.getGeneIdentifier())) {
		    		listIterator.set(geneView);
		    		break;
		    	}
		    }
		}
	}
	
	Gene findInGeneList (Gene gene) {
		if (data != null) {
			for (Gene g : data.getGenes()) {
				if (g.getGeneIdentifier().equals(gene.getGeneIdentifier()))
					return g;
			}
		}
		return null;
	}
}
