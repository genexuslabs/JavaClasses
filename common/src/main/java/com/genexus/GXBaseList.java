package com.genexus;

import java.io.Serializable;
import java.util.Vector;
import com.genexus.internet.IGxJSONAble;
import com.genexus.internet.IGxJSONSerializable;

public abstract class GXBaseList<T> extends Vector<T> implements Serializable, IGxJSONAble, IGxJSONSerializable, IGXAssigned {
	private boolean IsAssigned;

	public GXBaseList() {
		IsAssigned = true;
	}

	public boolean getIsAssigned() {
		return this.IsAssigned;
	}
	public void setIsAssigned(boolean bAssigned) {
		this.IsAssigned = bAssigned;
	}
	public void removeAllItems() {
		super.clear();
		IsAssigned = true;
	}
	public byte removeItem(int index) {
		T item = null;
		if(index > 0 && index <= size()) {
			item = super.remove((int)index - 1);//Vector.remove(int)
			IsAssigned = true;
			return (byte)1;
		}
		return (byte)0;
	}
	public byte removeElement(double index) {
		if(index > 0 && index <= size()) {
			super.remove((int)index - 1);//Vector.remove(int)
			IsAssigned = true;
			return (byte)1;
		}
		else {
			return (byte)0;
		}
	}

	@SuppressWarnings("unchecked")
	public void addObject(Object obj){
		super.add((T)obj);
		IsAssigned = true;
	}
	@SuppressWarnings("unchecked")
	public void add(Object item, int index) {
		if(index < 1 || index > size()) {
			add((T)item); //this.add, GXBCLevelCollection.add for example
		}
		else {
			super.add(index - 1, (T)item); //Vector insert element
			IsAssigned = true;
		}
	}
	@SuppressWarnings("unchecked")
	public void addBase( Object item) {
		super.add((T)item);
		IsAssigned = true;
	}

	public boolean addRange( GXBaseList<T> baseList, Number index) {
		if (baseList.size() == 0)
			return true;

		boolean result;
		if (index == null) {
			result = addAll(baseList);
		}
		else {
			int nindex = index.intValue();
			if(nindex != 1 && (nindex < 0 || nindex > size() +1))
				return false;
			if (nindex == 0)
				nindex = 1;
			result = addAll(nindex -1, baseList);
		}
		IsAssigned = true;
		return result;
	}

	public boolean removeRange( int index, Number count) {
		int colSize = size();
		if(index <= 0 || index > colSize || (count != null && index + count.intValue() > colSize))
			return false;
		int toIndex;
		if (count == null)
			toIndex = colSize;
		else
			toIndex = count.intValue();
		super.removeRange(index -1, toIndex);
		IsAssigned = true;
		return true;
	}

	public boolean setElement( int index, T element) {
		if(index < 1 || index > size())
			return false;
		super.set(index -1, element);
		IsAssigned = true;
		return true;
	}
}



