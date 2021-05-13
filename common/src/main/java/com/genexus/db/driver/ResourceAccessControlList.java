package com.genexus.db.driver;

public enum ResourceAccessControlList {
	Default,
	PublicRead,
	PublicReadWrite,
	Private;

	private static ResourceAccessControlList DEFAULT = PublicRead;

	public static ResourceAccessControlList parse(String acl) {
		ResourceAccessControlList resourceAcl = DEFAULT;
		acl = acl.toLowerCase();
		if (acl.toLowerCase().equals("publicread") ) {
			resourceAcl = ResourceAccessControlList.PublicRead;
		}
		if (acl.toLowerCase().equals("publicreadwrite") ) {
			resourceAcl = ResourceAccessControlList.PublicReadWrite;
		}
		if (acl.toLowerCase().equals("private") ) {
			resourceAcl = ResourceAccessControlList.Private;
		}
		return resourceAcl;
	}
}