package com.genexus;

public interface ISessionInstances {
	IGXMailer getMailer();
	IGXFTPSafe getFTP();
	IXMLWriter getXMLWriter();
	IDelimitedFilesSafe getDelimitedFiles();
	void cleanup();
}
