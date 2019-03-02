package com.artech.base.services;

public interface IGxBusinessComponent
{
	/**
	 * Initializes a new entity with the default field values.
	 */
	public void initentity(IEntity androidEntity);

	/**
	 * Takes the key from the supplied entity, reads the BC from the database,
	 * and populates the entity with those values.
	 * @return True if the operation was successful, otherwise false.
	 */
	public boolean loadbcfromkey(IEntity androidEntity);

	/**
	 * Fills up the BC with the values from the supplied entity, saves the updated BC
	 * to the database, and repopulates the entity with any updated values.
	 * @return True if the operation was successful, otherwise false.
	 */
	public boolean savebcfromentity(IEntity androidEntity);

	/**
	 * Deletes the previously loaded BC from the database.
	 * @return True if the operation was successful, otherwise false.
	 */
	public boolean delete();
	
	/**
	* Returns the result of the last operation.
	*/
	public boolean success();
	
	// Add getMessages();
	public com.genexus.internet.MsgList getmessages();
	
	public String getbcname();

}
