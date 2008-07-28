/**
 * 
 */
package pivot.tools.explorer;

import pivot.tools.explorer.properties.AbstractTableEntryAdapter;
import pivot.tools.explorer.properties.PropertySheetColumn;
import pivot.wtk.Component;

class PropertyTableEntryAdapter extends AbstractTableEntryAdapter {
	
	public PropertyTableEntryAdapter(Component component, String propertyName) {
		super(component, propertyName);
	}

	@Override
	public Object get(String key) {
		
		switch( asColumn( key )) {
			case NAME : return getName();
			case VALUE: return getComponent().get(getName());
			default   : return null;
		}
	}

	@Override
	public Object put(String key, Object value) {
		return PropertySheetColumn.VALUE == asColumn( key )? getComponent().put(getName(), value): null;
	}
	
}