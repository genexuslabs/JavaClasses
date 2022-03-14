package com.genexus.db.dynamodb;
import com.genexus.db.service.*;

public class DynamoQuery extends Query{
	private String index;

	private boolean scanIndexForward = true;
	private static final String RANGE_KEY_INDEX = "RangeKey";
	private static final char[] indexTrimChars = new char[] { '(', ')' };

	@Override
	public DynamoQuery orderBy(String index)
	{
		index = index.trim();
		if(index.startsWith("(") && index.endsWith(")"))
		{
			scanIndexForward = false;
			index = index.substring(1, index.length()-1);
		}
		if (!RANGE_KEY_INDEX.equals(index))
			setIndex(index);
		return this;
	}

	public DynamoQuery(DataStoreHelperDynamoDB dataStoreHelper)
	{
		super(dataStoreHelper);
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public boolean isScanIndexForward() {
		return scanIndexForward;
	}

	public void setScanIndexForward(boolean scanIndexForward) {
		this.scanIndexForward = scanIndexForward;
	}
}
