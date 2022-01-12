package com.genexus.xml.ws;

public class Holder<T>{
	javax.xml.ws.Holder<T> holder;

	public Holder() {
		holder = new javax.xml.ws.Holder();
	}

	public T getValue() {
		return holder.value;
	}

	public void setValue(T value) {
		holder.value = value;
	}

	public javax.xml.ws.Holder<T> getWrappedClass() {
		return holder;
	}

}
