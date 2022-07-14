package org.grits.toolbox.entry.qrtpcr.editor.content;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.grits.toolbox.entry.qrtpcr.model.Gene;

public class GeneContentProvider implements ITreeContentProvider {

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return ((List<?>) inputElement).toArray();
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof List) 
			return ((List<?>) parentElement).toArray();
		else if (parentElement instanceof Gene)
			return ((Gene) parentElement).getDataMap().get(0).toArray();
		return null;
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof List) {
			return ((List<?>) element).size() > 0;
		} else if (element instanceof Gene) {
			return ((Gene) element).getDataMap().get(0).size() > 0;
		} 
		return false;
	}

}
