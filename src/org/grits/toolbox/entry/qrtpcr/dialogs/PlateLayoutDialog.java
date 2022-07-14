package org.grits.toolbox.entry.qrtpcr.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.nebula.jface.gridviewer.GridTableViewer;
import org.eclipse.nebula.jface.gridviewer.GridViewerColumn;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.grits.toolbox.entry.qrtpcr.ImageRegistry;
import org.grits.toolbox.entry.qrtpcr.ImageRegistry.QrtPCRImage;
import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.Plate;
import org.grits.toolbox.entry.qrtpcr.model.PlateLayout;
import org.grits.toolbox.entry.qrtpcr.model.Well;
import org.grits.toolbox.entry.qrtpcr.model.view.GeneView;

public class PlateLayoutDialog extends WizardPage {

	private static final Image PLACED = ImageRegistry.getImageDescriptor(QrtPCRImage.TICK_ICON).createImage();

	private Grid firstTable;
	private Grid secondTable;
	
	List<GeneView> geneViewList = new ArrayList<>();
	
	Integer width = 12;
	Integer height = 8;
	
	PlateLayout layout;
	XSSFSheet plateSheet;
	
	boolean shown = false;

	private GridTableViewer listTableViewer;

	private List<GeneView[]> inputData = new ArrayList<>();

	private GridTableViewer tableViewer;

	public void setWidth(Integer width) {
		this.width = width;
	}
	
	public void setHeight(Integer height) {
		this.height = height;
	}
	
	public List<GeneView> getGeneViewList() {
		return geneViewList;
	}
	
	protected PlateLayoutDialog(String pageName, String title,
			ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}
	
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout(2, false));

        Composite firstTableComposite = new Composite(container, SWT.NONE);
        firstTableComposite.setLayout(new GridLayout(1, true));
        this.firstTable = createTable(firstTableComposite);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(this.firstTable);

        Composite secondTableComposite = new Composite(container, SWT.NONE);
        secondTableComposite.setLayout(new GridLayout(1, true));
        this.secondTable = createPlateMapTable(secondTableComposite, height, width);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(this.secondTable);
       
        setControl(container);
	}
	
	@Override
	public boolean isPageComplete() {
		boolean allPlaced = true;
		for (GeneView geneView : geneViewList) {  
			allPlaced |= geneView.isPlaced(); // we can leave plates empty, so if any gene is placed we can go to the next page
		}
		/*if (allPlaced) {
			replacePlate();
		}*/
		
		
		return allPlaced;
	}
	
	@Override
	public void setVisible(boolean visible) {
		if (visible) 
			shown = true;
		if (shown && !visible) { // closing down
			replacePlate ();
			List<GeneView> notYetPlaced = new ArrayList<>();
			for (GeneView geneView : geneViewList) {  
				if (!geneView.isPlaced())
					notYetPlaced.add(geneView);
			}
			// add the genes in notYetPlaced to the next page's genelist
			addToNextPage(notYetPlaced);
			shown = false;
		}
		super.setVisible(visible);
	}
	
	
	private void replacePlate() {
		Plate existing = null;
		if (this.layout.getPlates() != null) {
			for (Plate plate : this.layout.getPlates()) {
				if (plate.getPlateId().equals(this.getTitle())) {
					existing = plate;
				}
			}
		}
		Plate plate = convertInputDataIntoPlate();
		if (existing != null)
			this.layout.getPlates().remove(existing);
	    
		this.layout.addPlate(plate);
	}

	private void addToNextPage(List<GeneView> notYetPlaced) {
		IWizardPage nextPage = this.getNextPage();
		if (nextPage != null && nextPage instanceof PlateLayoutDialog) {
			((PlateLayoutDialog)nextPage).getGeneViewList().addAll(notYetPlaced);
			((PlateLayoutDialog)nextPage).refreshGeneList();
		}
	}
	
	private void refreshGeneList() {
		if (this.listTableViewer != null) {
			this.listTableViewer.setInput(this.geneViewList);
			this.listTableViewer.refresh();
			((Composite)this.listTableViewer.getControl()).layout();
		}
	}

	private Grid createPlateMapTable(Composite parent, int rows, int columns) {
		String[][] data = new String[rows][columns];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				data[i][j] = "";
			}
		}
		
		tableViewer = new GridTableViewer(parent, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		Grid grid = tableViewer.getGrid();
	    grid.setHeaderVisible(true);
	    grid.setRowHeaderVisible(true);
	    grid.setCellSelectionEnabled(true);
	    grid.setLinesVisible(true);
	    
	    final char index = 'A';
			
	    tableViewer.setRowHeaderLabelProvider(new CellLabelProvider() {
	    	
	    	@Override
	    	public void update(ViewerCell cell) {
	    		char newIndex = (char) (index + ((GridItem)cell.getItem()).getRowIndex());
	    		cell.setText(newIndex + "");
	    	}
	    });
	    
	    for (int i = 0; i < columns; i++) {
			GridViewerColumn column = new GridViewerColumn(tableViewer, SWT.NONE);
			column.getColumn().setText(i+1 + "");
			column.getColumn().setWidth(80);
			column.setLabelProvider(new CellLabelProvider() {
				
				@Override
				public void update(ViewerCell cell) {
					int columnIndex = cell.getColumnIndex();
					cell.setText(((GeneView)((GeneView[])cell.getElement())[columnIndex]).getGeneIdentifier());
					
				}
			});
			
		} 
	    
	    if (inputData.isEmpty()) {
		    for (int i = 0; i < rows; i++) {
		    	GeneView[] geneViewListForRow = new GeneView[columns];
		    	for(int j=0; j < columns; j++) {
		    		geneViewListForRow[j] = new GeneView();
		    		geneViewListForRow[j].setGeneIdentifier("");
		    		geneViewListForRow[j].setGeneSymbol("");
		    	}
				inputData.add(geneViewListForRow);
			}
	    }
	    
	    tableViewer.setContentProvider(new ArrayContentProvider() {
	    	@SuppressWarnings("rawtypes")
			@Override
	    	public Object[] getElements(Object inputElement) {
	    		if (inputElement instanceof ArrayList) {
	    			return ((ArrayList) inputElement).toArray();
	    		}
	    		return super.getElements(inputElement);
	    	}
	    });
	    
	    tableViewer.setInput(inputData);
		
		Transfer[] transfer = { TextTransfer.getInstance() };
		DragAndDropSupport2 dndSupport = new DragAndDropSupport2(tableViewer, data, inputData);
		int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;
		tableViewer.addDropSupport(operations, transfer, dndSupport);
		grid.addMenuDetectListener(new MenuDetectListener() {
			
			@Override
			public void menuDetected(MenuDetectEvent e) {
				final Point curLoc = Display.getCurrent().map(null, tableViewer.getControl(), new Point(e.x, e.y));
		        
		        Menu menu = new Menu (tableViewer.getGrid());
		        final MenuItem item = new MenuItem(menu, SWT.PUSH);
		        item.setText("Delete Cell");
		        item.addSelectionListener(new SelectionListener() {
		        	@Override
		        	public void widgetSelected(SelectionEvent e) {
		        		Point position = (Point)item.getData();
						if (position != null) {
							GeneView[] geneViews = inputData.get(position.y);
							if (geneViews != null) {
								if (position.x <= geneViews.length) {
									geneViews[position.x-1] = new GeneView();
									geneViews[position.x-1].setGeneIdentifier("");
									geneViews[position.x-1].setGeneSymbol("");
									tableViewer.refresh();
									tableViewer.getGrid().setRedraw(true);
								}
							}
						}
		        	}

					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
					}
		        });
		        
		        GridItem[] items  = tableViewer.getGrid().getItems();
	        	boolean menuShown = false;
	        	Point position = null;
	        	int rowNumber = 0;
	        	for (GridItem gridItem : items) {
	        		for (int colId : tableViewer.getGrid().getColumnOrder()) {
	    				Rectangle rect = gridItem.getBounds(colId);
		    			if (rect.contains(curLoc)) { 
	    					tableViewer.getControl().setMenu(menu);
			    			position = new Point (colId+1, rowNumber);
			    			menu.getItems()[0].setData(position);
			    			menuShown = true;
			    			break;
						}
	        		}
	        		rowNumber++;
	        	}
	        	
	        	if (!menuShown) {
	        		tableViewer.getControl().setMenu(null);
	        	}
					
			}
		});

		return grid;
	}

	private Grid createTable(Composite parent) {
       
		listTableViewer = new GridTableViewer(parent, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		Grid grid = listTableViewer.getGrid();
	    grid.setHeaderVisible(true);
	    
	    GridColumn column1 = new GridColumn(grid, SWT.NONE);
	    column1.setText("Gene Identifier");
	    column1.setWidth(150);
	    
	    listTableViewer.setContentProvider(new ArrayContentProvider());
	    listTableViewer.setLabelProvider(new LabelProvider() {
	    	@Override
	    	public String getText(Object element) {
	    		if (element instanceof GeneView) {
	    			return ((Gene) element).getGeneIdentifier();
	    		}
	    		return super.getText(element);
	    	}
	    	
	    	@Override
	    	public Image getImage(Object element) {
	    		if (element instanceof GeneView) {
	    			if (((GeneView) element).isPlaced())
	    				return PLACED;
	    		}
	    		return super.getImage(element);
	    	}
	    });
	    
	    listTableViewer.setInput(geneViewList);
	    
        // add DnD support
        DragAndDropSupport dndSupport = new DragAndDropSupport(listTableViewer);
        Transfer[] transfer = { TextTransfer.getInstance() };
        listTableViewer.addDragSupport(DND.DROP_COPY, transfer, dndSupport);

        return grid;
    }
	
	public void setGeneList(List<Gene> list) {
		this.geneViewList.clear();
		for (Gene gene : list) {
			this.geneViewList.add(new GeneView(gene));
		}
	}

	public void layoutGenes(List<Gene> list) {
		setGeneList(list);
		inputData.clear();
	    for (int i = 0; i < height; i++) {
	    	GeneView[] geneViewListForRow = new GeneView[width];
	    	int j=0;
	    	while (j < width) {
	    		for (int k=0; k < layout.getNumberOfReplicates() && j < width; k++) {
		    		if (k == 0) {
		    			GeneView geneView = getGeneForLocation (i, j);
			    		if (geneView != null)
			    			geneViewListForRow[j] = geneView;
			    		else { 
			    			geneViewListForRow[j] = new GeneView();
			    			geneViewListForRow[j].setGeneIdentifier("");
			    			geneViewListForRow[j].setGeneSymbol("");
			    		}
			    		
		    		} 
		    		else {
		    			geneViewListForRow[j] = geneViewListForRow[j-1];
		    		}
		    		j++;
	    		}
	    	}
			inputData.add(geneViewListForRow);
	    }
	}
	
	private GeneView getGeneForLocation (int row, int column) {
		Character xChar = 'A';
		
		for (int i=0; i < row; i++)
			xChar++;
		
		Well well = new Well(xChar, column+1);
		for (Iterator<Gene> iterator = layout.getControlGeneLocations().keySet().iterator(); iterator.hasNext();) {
			Gene gene = iterator.next();
			Well controlGeneLocation = layout.getControlGeneLocations().get(gene);
			if (controlGeneLocation.equals(well)) {
				for (GeneView geneView : geneViewList) {
					if (geneView.getGeneIdentifier().equals(gene.getGeneIdentifier())) {
						if (!geneView.isPlaced()) { 
							geneView.setPlaced(true);
							return geneView;
						} else // already placed somewhere else, no need to put it to this requested location again
							return null;
					}
				}
				
			}
		}
		
		// if not the control gene, find the first unplaced gene and return it
		for (GeneView geneView : geneViewList) {
			if (geneView.isPlaced())
				continue;
			geneView.setPlaced(true);
			return geneView;
		}
		
		return null;
	}
	
	class DragAndDropSupport2 implements DropTargetListener{
		
		private final GridTableViewer grid;

		private String[][] data;
		List<GeneView[]> inputData;
		GeneView draggedGene=null;

		private static final String DATA_SEPARATOR = "|";

		public DragAndDropSupport2(GridTableViewer grid2, String[][] data, List<GeneView[]> inputData) {
			this.grid = grid2;
			this.data = data;
			this.inputData = inputData;
		}

		@Override
		public void dragEnter(DropTargetEvent event) {
		    event.detail = DND.DROP_COPY; 
		}
		
		@Override
		public void dragLeave(DropTargetEvent event) {}
		
		@Override
		public void dragOperationChanged(DropTargetEvent event) {}
		
		@Override
		public void dragOver(DropTargetEvent event) {}
		
		@Override
		public void drop(DropTargetEvent event) {
		    String[] geneData = (event.data != null ? event.data.toString().split(
		            "\\" + DATA_SEPARATOR) : new String[] {});
		    
		    if (geneData.length > 0 ) {  
		    	int[] rowIndexList = null;
		    	int[] columnIndexList = null;
		    	if (this.grid.getGrid().getSelectionCount() == 0) {
		    		// find the cell (Point) from event's coordinates
		    		Point cell = findTargetCell(event);
		    		if (cell != null) {
		    			if (cell.x < data.length && cell.y < data[0].length)
		    			this.data[cell.x][cell.y] = geneData[0];
		    			rowIndexList = new int[1];
		    			rowIndexList[0] = cell.x;
		    			columnIndexList = new int[1];
		    			columnIndexList[0] = cell.y;
		    		} else {
		    			event.detail = DND.DROP_NONE;
		    		}
	            } else { 
	            	// find the cell (Point) from event's coordinates
		    		Point cell = findTargetCell(event);
		    		if (cell != null)
		    			this.data[cell.x][cell.y] = geneData[0];
		    		// also drop onto other selected cells
	            	int cellSelections = this.grid.getGrid().getCellSelectionCount();
	            	Point[] cells = this.grid.getGrid().getCellSelection();
	            	int index = 0;
	            	rowIndexList = new int[cells.length+1];
	            	columnIndexList = new int[cells.length +1];
	            	if (cell != null) {
	            		rowIndexList[index] = cell.x;
	            		columnIndexList[index] = cell.y;
	            		index++;
	            	}
	            	for (int i = 0; i < cellSelections; i++) {
                		int columnIndex = cells[i].x;
                		int rowIndex = cells[i].y;
                		rowIndexList[index] = rowIndex;
                		columnIndexList[index++] = columnIndex;
                		if (rowIndex < data.length && columnIndex < data[0].length)
                			this.data[rowIndex][columnIndex] = geneData[0];
                		else
                			event.detail = DND.DROP_NONE;
	            	}
	            }
		    	
		    	if (event.detail != DND.DROP_NONE) {
				    for (int i = 0; i < rowIndexList.length; i++) {
			    		int row = rowIndexList[i];
				    	for (int j=0; j < columnIndexList.length; j++) {
				    		int column = columnIndexList[j];
			    			inputData.get(row)[column].setGeneIdentifier(geneData[0]);
			    			inputData.get(row)[column].setGeneSymbol(geneData[1]);
			    			inputData.get(row)[column].setIsCommon(Boolean.parseBoolean(geneData[2]));
				    	}
					}
		    	}
			    this.grid.setSelection(null);
			    this.grid.setInput(inputData);
		    	this.grid.refresh();
		 
		    }
		}

        private Point findTargetCell(DropTargetEvent event) {
        	Point coordinates = new Point(event.x, event.y);
        	coordinates = grid.getControl().toControl(coordinates);
        	GridItem[] items = grid.getGrid().getItems();
        	int columns = grid.getGrid().getColumnCount();
        	for (GridItem gridItem : items) {
        		int rowIndex = gridItem.getRowIndex();
        		for (int i=0; i < columns; i++) {
        			Rectangle bounds = gridItem.getBounds(i);
        			if (bounds == null) {
        				return null;
        			}
        			int diffY = coordinates.y - bounds.y;
        			int diffX = coordinates.x - bounds.x;
        			if (diffY > 0 && diffY < bounds.height && diffX > 0 && diffX < bounds.width) {
        				return new Point(rowIndex, i);
        			}
        		}
			}
			return null;
		}

		@Override
        public void dropAccept(DropTargetEvent event) {}
		
	}

    class DragAndDropSupport implements DragSourceListener {

        private final GridTableViewer grid;
      
        private GeneView draggedGene;

        private static final String DATA_SEPARATOR = "|";

        public DragAndDropSupport(GridTableViewer g) {
            this.grid = g;
        }

        @Override
        public void dragStart(DragSourceEvent event) {
            if (this.grid.getGrid().getSelectionCount() == 0) {
                event.doit = false;
            } 
        }

        @Override
        public void dragSetData(DragSourceEvent event) {
        	IStructuredSelection selection = (IStructuredSelection) this.grid.getSelection();
        	Object selected = selection.getFirstElement();
            
            if (!selection.isEmpty()) {
            	if (selected instanceof GeneView) {
	                this.draggedGene = (GeneView) selected;
	                StringBuilder builder = new StringBuilder();
	                builder.append(this.draggedGene.getGeneIdentifier()).append(DATA_SEPARATOR)
	                        .append(this.draggedGene.getGeneSymbol()).append(DATA_SEPARATOR)
	                        .append(this.draggedGene.getIsCommon());
	                event.data = builder.toString();
            	}
            }
        }
        
        
        @Override
        public void dragFinished(DragSourceEvent event) {
        	if (event.detail == DND.DROP_NONE)
        		return;
            this.draggedGene.setPlaced(true);  
            this.draggedGene = null;

            // clear selection
            this.grid.setSelection(null);

            this.grid.refresh();
        }

    }
    
    public Plate convertInputDataIntoPlate () {
    	Plate plate = new Plate();
    	plate.setPlateId(this.getTitle());
    	Map<Well, Gene> plateMap = new HashMap<Well, Gene> ();
    	Character charPos = 'A';
    	for (GeneView[] row : inputData) {
    		Integer column = 1;
			for (GeneView geneView: row) {
				Well well = new Well(charPos, column);
				if (geneView.getGeneIdentifier() != null && geneView.getGeneIdentifier().trim().length() > 0)
					plateMap.put(well, (Gene)geneView);
				column ++;
			}
			charPos++;
		}
    	plate.setPlateMap(plateMap);
    	return plate;
    }

    /**
     * sets the layout and updates the size to use for the plate table based on the layout's size
     * 
     @param layout
     */
	public void setLayout(PlateLayout layout) {
		this.layout = layout;
		setWidth(layout.getSize().getWidth());
		setHeight(layout.getSize().getHeight());
	}

	public void setPlate(Plate plate) {
		// convert Plate into inputData and geneViewList
		Map<Well, Gene> plateMap = plate.getPlateMap();
		
		int rows = layout.getSize().getHeight();
		int columns = layout.getSize().getWidth();
		Character charPos = 'A';
		for (int i = 0; i < rows; i++) {
			int column = 1;
	    	GeneView[] geneViewListForRow = new GeneView[columns];
	    	for(int j=0; j < columns; j++) {
	    		Well well = new Well(charPos, column);
	    		Gene gene = plateMap.get(well);
	    		if (gene == null) {
	    			geneViewListForRow[j] = new GeneView();
	    			geneViewListForRow[j].setGeneIdentifier("");
	    			geneViewListForRow[j].setGeneSymbol("");
	    		}
	    		else {
	    			geneViewListForRow[j] = new GeneView(gene);
	    			if (!geneExistsInGeneViewList(gene)) {
		    			geneViewList.add(geneViewListForRow[j]);
		    			geneViewListForRow[j].setPlaced(true);
	    			}
	    		}
	    		column++;	
	    	}
	    	charPos++;
			inputData.add(geneViewListForRow);
		}
		
	}
	
	boolean geneExistsInGeneViewList (Gene gene) {
		for (GeneView geneView : this.geneViewList) {
			if (geneView.getGeneIdentifier().equals(gene.getGeneIdentifier()))
				return true;
			
		}
		return false;
	}

	public GridTableViewer getTableViewer() {
		return this.tableViewer;
	}
}
