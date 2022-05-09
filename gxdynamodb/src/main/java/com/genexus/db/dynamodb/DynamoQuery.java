package com.genexus.db.dynamodb;

import com.genexus.db.service.Query;

import java.util.Arrays;
import java.util.stream.Stream;

public class DynamoQuery extends Query{
	private String index;

	private boolean scanIndexForward = true;
	private static final String RANGE_KEY_INDEX = "RangeKey";
	private String partitionKey;
	public String[] keyFilters = EMPTY_ARR_STRING;

	@Override
	public DynamoQuery For(String tableName)
	{
		super.For(tableName);
		return this;
	}

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

	public DynamoQuery setKey(String partitionKey)
	{
		this.partitionKey = partitionKey;
		return this;
	}

	public DynamoQuery keyFilter(String[] keyFilters)
	{
		this.keyFilters = keyFilters;
		return this;
	}

	public String getPartitionKey(){ return partitionKey; }

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

	public Stream<String> getAllFilters()
	{
		return Stream.concat(Arrays.stream(keyFilters), Arrays.stream(filters));
	}
}
