package com.genexus.xml.ws;

public class Holder<T>{
	jakarta.xml.ws.Holder<T> holder;

	public Holder() {
		holder = new jakarta.xml.ws.Holder();
	}

	public T getValue() {
		return holder.value;
	}

	public void setValue(T value) {
		holder.value = value;
	}

	public jakarta.xml.ws.Holder<T> getWrappedClass() {
		return holder;
	}

}
