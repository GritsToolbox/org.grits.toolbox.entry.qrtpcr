package org.grits.toolbox.entry.qrtpcr.manager.pages;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.grits.toolbox.entry.qrtpcr.model.Gene;

public class GeneSelectionListContentProvider implements IStructuredContentProvider {
	
	List<Gene> genes;
	
	public GeneSelectionListContentProvider(List<Gene> elements) {
		this.genes = elements;
	}
	
	@Override
	public Object[] getElements(Object inputElement) {
		return genes.toArray();
	}

	@Override
	public void dispose() {
		//do nothing
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// do nothing
	}
	
	public boolean canMoveDown(List<?> selectedElements) {
		int nSelected= selectedElements.size();
		for (int index= genes.size() - 1; index >= 0 && nSelected > 0; index--) {
			if (!selectedElements.contains(genes.get(index))) {
				return true;
			}
			nSelected--;
		}
		return false;
	}

	public boolean canMoveUp(List<?> selected) {
		int nSelected= selected.size();
		for (int index= 0; index < genes.size() && nSelected > 0; index++) {
			if (!selected.contains(genes.get(index))) {
				return true;
			}
			nSelected--;
		}
		return false;
	}
	
	private List<Gene> moveUp(List<Gene> elements, List<?> move) {
		List<Gene> result= new ArrayList<>(elements.size());
		Gene floating= null;
		for (int index= 0; index < elements.size(); index++) {
			Gene current= elements.get(index);
			if (move.contains(current)) {
				result.add(current);
			} else {
				if (floating != null) {
					result.add(floating);
				}
				floating= current;
			}
		}
		if (floating != null) {
			result.add(floating);
		}
		return result;
	}

	private List<Gene> reverse(List<Gene> list) {
		List<Gene> reverse= new ArrayList<>(list.size());
		for (int index= list.size() - 1; index >= 0; index--) {
			reverse.add(list.get(index));
		}
		return reverse;
	}

	public void setElements(List<Gene> elements, TableViewer table) {
		this.genes= new ArrayList<>(elements);
		if (table != null)
			table.refresh();
	}

	public void up(List<?> checked, TableViewer table) {
		if (checked.size() > 0) {
			setElements(moveUp(genes, checked), table);
			table.reveal(checked.get(0));
		}
		table.setSelection(new StructuredSelection(checked));
	}
	
	public void down(List<?> checked, TableViewer table) {
		if (checked.size() > 0) {
			setElements(reverse(moveUp(reverse(genes), checked)), table);
			table.reveal(checked.get(checked.size() - 1));
		}
		table.setSelection(new StructuredSelection(checked));
	}

	public List<Gene> getGenes() {
		return this.genes;
	}
	
	
	

}
