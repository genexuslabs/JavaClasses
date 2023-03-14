package com.genexus.db.cosmosdb;

import com.genexus.db.service.Query;

import java.util.ArrayList;
import java.util.List;

public class CosmosDBQuery extends Query {
	public CosmosDBQuery(DataStoreHelperCosmosDB dataStoreHelper) {
		super(dataStoreHelper);
	}

	private String partitionKey;
	public String getPartitionKey() {
		return partitionKey;
	}
	public String index;
	public String getIndex() {
		return index;
	}
	public void setIndex(String index) {this.index = index;}
	private List<String> keyFilters = new ArrayList<>();
	public List<String> getKeyFilters() {
		return this.keyFilters;
	}
	public void setKeyFilters(List<String> keyFilters) {
		this.keyFilters = keyFilters;
	}
	public CosmosDBQuery setKey(String partitionKey)
	{
		this.partitionKey = partitionKey;
		return this;
	}


	@Override
	public CosmosDBQuery For(String tableName)
	{
		super.For(tableName);
		return this;
	}

	@Override
	public CosmosDBQuery filter(String[] filters)
	{
		super.filter(filters);
		return this;
	}

	@Override
	public CosmosDBQuery select(String[] columns) {
		super.select(columns);
		return this;
	}

	@Override
	public CosmosDBQuery orderBy(String[] orders)
	{
		super.orderBy(orders);
		return this;
	}

	@Override
	public CosmosDBQuery set(String name, String value)
	{
		super.set(name, value);
		return this;
	}
}
