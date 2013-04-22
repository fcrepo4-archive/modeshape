package org.modeshape.jcr.value.binary;

public class BasicStrategyHint implements StrategyHint {

	private final Object hint;

	public BasicStrategyHint(Object o) {
		this.hint = o;
	}
	@Override
	public Object getHint() {
		return this.hint;
	}
}
