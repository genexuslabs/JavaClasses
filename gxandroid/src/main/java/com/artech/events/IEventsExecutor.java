package com.artech.events;

import com.artech.base.services.IPropertiesObject;

public interface IEventsExecutor 
{
	
	// Call to UI
	public boolean run(String name, IPropertiesObject entity) ;
		
	// change  property of a control
	public boolean setControlProperty(String name, String property, Object value) ;
		
}
