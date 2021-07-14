package com.genexus.db.driver;

public enum ResourceAccessControlList {
	Default,
	PublicRead,
	PublicReadWrite,
	Private;

	public static ResourceAccessControlList parse(String acl) {
		ResourceAccessControlList resourceAcl = ResourceAccessControlList.PublicRead;

		if (acl.equalsIgnoreCase("publicread") ) {
			resourceAcl = ResourceAccessControlList.PublicRead;
		}
		if (acl.equalsIgnoreCase("publicreadwrite") ) {
			resourceAcl = ResourceAccessControlList.PublicReadWrite;
		}
		if (acl.equalsIgnoreCase("private") ) {
			resourceAcl = ResourceAccessControlList.Private;
		}
		return resourceAcl;
	}
}