package org.grits.toolbox.entry.qrtpcr.table.manager;

import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.validate.DataValidator;
import org.grits.toolbox.entry.qrtpcr.Config;
import org.grits.toolbox.entry.qrtpcr.model.TableData;

public class IntegerValidator extends DataValidator {
	
	private IColumnPropertyAccessor<TableData> columnPropertyAccessor;

	public IntegerValidator(IColumnPropertyAccessor<TableData> columnPropertyAccessor) {
		this.columnPropertyAccessor = columnPropertyAccessor;
	}

	@Override
	public boolean validate(
		int columnIndex, int rowIndex, Object newValue) {
		String columnName = getColumnProperty(columnIndex);
		if (columnName != null && columnName.equals(Config.ID)) {
			if (newValue != null) {
				String stringValue = (String)newValue;
				if (!stringValue.isEmpty()) {
					String splitChar = ",";
					if (stringValue.contains("/"))
						splitChar = "/";
					String[] geneIdNumbers = stringValue.split(splitChar);
					for (String geneId : geneIdNumbers) {
						try {
							Integer.parseInt(geneId.trim());
						} catch (NumberFormatException e) {
							return false;
						}
					}
						
				} 
			}
		}

		return true;
	}
	
	String getColumnProperty (int columnIndex) {
		return columnPropertyAccessor.getColumnProperty(columnIndex);
	}

}
