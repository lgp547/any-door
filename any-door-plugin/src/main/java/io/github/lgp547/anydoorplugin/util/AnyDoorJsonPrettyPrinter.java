package io.github.lgp547.anydoorplugin.util;

import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

public class AnyDoorJsonPrettyPrinter extends DefaultPrettyPrinter {

	public AnyDoorJsonPrettyPrinter() {
		super("");
		_objectFieldValueSeparatorWithSpaces = ": ";
		_arrayIndenter = NopIndenter.instance;
	}

	public AnyDoorJsonPrettyPrinter(AnyDoorJsonPrettyPrinter base) {
		super(base, new SerializedString(""));
		_objectFieldValueSeparatorWithSpaces = ": ";
	}


	@Override
	public DefaultPrettyPrinter createInstance() {
		return new AnyDoorJsonPrettyPrinter(this);
	}
}
