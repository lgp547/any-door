package io.github.lgp547.anydoorplugin.util;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

public class AnyDoorJsonPrettyPrinter extends DefaultPrettyPrinter {

	public AnyDoorJsonPrettyPrinter() {
		super();
		_objectFieldValueSeparatorWithSpaces = ": ";
		_arrayIndenter = NopIndenter.instance;
	}

	@Override
	public DefaultPrettyPrinter createInstance() {
		return new AnyDoorJsonPrettyPrinter();
	}
}
