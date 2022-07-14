package org.grits.toolbox.entry.qrtpcr;

import org.eclipse.core.expressions.PropertyTester;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.entry.qrtpcr.property.QrtPCRProperty;

public class QrtPCREntryTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof Entry) {
			if (((Entry)receiver).getProperty().getType().equals(QrtPCRProperty.TYPE))
				return true;
		}
		return false;
	}

}
