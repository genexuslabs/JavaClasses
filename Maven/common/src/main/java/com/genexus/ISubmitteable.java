package com.genexus;

import java.sql.SQLException;

import com.genexus.common.classes.AbstractModelContext;

public interface ISubmitteable
{
	public void submit(int submitId, Object []submitParms, AbstractModelContext ctx);	
	public void submit(int submitId, Object []submitParms);
	public void submitReorg(int submitId, Object []submitParms) throws SQLException;
}
