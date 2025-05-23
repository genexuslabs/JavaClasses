package com.genexus.JWT.utils;

import java.util.ArrayList;
import java.util.List;


import com.genexus.securityapicommons.commons.SecurityAPIObject;
import com.genexus.securityapicommons.utils.SecurityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RevocationList extends SecurityAPIObject {

	private List<String> revocationList;

	private static final Logger logger = LogManager.getLogger(RevocationList.class);

	public RevocationList() {
		revocationList = new ArrayList<String>();
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - BEGIN ********/
	public boolean deleteFromRevocationList(String id) {
		for (int i = 0; i < revocationList.size(); i++) {
			if (SecurityUtils.compareStrings(id, revocationList.get(i))) {
				revocationList.remove(i);
				return true;
			}
		}
		this.error.setError("REL01", String.format("The id %s is not in the revocation list", id));
		logger.error(String.format("The id %s is not in the revocation list", id));
		return false;
	}

	public void addIDToRevocationList(String id) {
		revocationList.add(id);
	}

	public boolean isInRevocationList(String id) {
		for (String s : revocationList) {
			if (SecurityUtils.compareStrings(id, s)) {
				return true;
			}
		}
		return false;
	}
	/******** EXTERNAL OBJECT PUBLIC METHODS - END ********/
}
