package com.genexus.JWT.claims;

public class Claim {

	private String key;
	private Object value;

	public Claim(String valueKey, Object valueOfValue) {
		key = valueKey;
		value = valueOfValue;
	}

	public Object getValue() {
		if (value instanceof String) {
			return (String) value;
		} else if (value instanceof Long) {
			return (long) value;
		} else if (value instanceof Integer) {
			return (int) value;
		} else if (value instanceof Double) {
			return (double) value;
		} else if (value instanceof Boolean) {
			return (boolean) value;
		} else {
			return null;
		}
	}

	public PrivateClaims getNestedClaims() {
		if (value instanceof PrivateClaims) {
			return (PrivateClaims) value;
		} else {
			return null;
		}
	}

	public String getKey() {
		return key;
	}
}
