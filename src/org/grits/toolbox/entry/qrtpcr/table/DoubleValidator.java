package org.grits.toolbox.entry.qrtpcr.table;

import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.validate.DataValidator;
import org.grits.toolbox.entry.qrtpcr.Config;
import org.grits.toolbox.entry.qrtpcr.model.TableData;

public class DoubleValidator extends DataValidator {
	
	private IColumnPropertyAccessor<TableData> columnPropertyAccessor;

	public DoubleValidator(IColumnPropertyAccessor<TableData> columnPropertyAccessor) {
		this.columnPropertyAccessor = columnPropertyAccessor;
	}

	@Override
	public boolean validate(
		int columnIndex, int rowIndex, Object newValue) {
		// only CT is editable
		String columnName = getColumnProperty(columnIndex);
		if (columnName != null && columnName.equals(Config.CT)) {
			try {
				Double.parseDouble((String)newValue);
			} catch (NumberFormatException e) {
				return false;
			}
			
			return true;
		}
		
		return true;
	}
	
	String getColumnProperty (int columnIndex) {
		return columnPropertyAccessor.getColumnProperty(columnIndex);
	}

}
