package com.genexus.internet;

public class OfficeMailerFactory implements IMailImplementationFactory {

	public IMailImplementation createImplementation() {
		return new OfficeMail();
	}

}
