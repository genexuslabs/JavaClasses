package com.genexus.cloud.azure.events;

import com.genexus.cloud.serverless.azure.handler.AzureCosmosDBHandler;
import com.microsoft.azure.functions.ExecutionContext;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestAzureCosmosDBHandler {
	private AzureCosmosDBHandler cosmosDBFunction;
	private ExecutionContext context;


	@Test
	public void testCosmosDBFunction() throws Exception {

		cosmosDBFunction = new AzureCosmosDBHandler();
		List<Map<String, Object>> list = new ArrayList<>(2);

		Map<String, Object> map1 = new HashMap<>();
		map1.put("UserId", "ac7f12fd-9784-4c93-94cd-7e02a7b1cf4b");
		map1.put("UserName", "Jhon");

		Map<String, Object> map2 = new HashMap<>();
		map2.put("UserId", "bf3288ef-afe5-470e-8be6-2f55b4015f3b");
		map2.put("UserName", "Mary");

		list.add(map1);
		list.add(map2);

		context = new MockExecutionContext("TestCosmosDB","13e2d1f9-6838-4927-a6a8-0160e8601ab5");
		cosmosDBFunction.run(list,context);

		assertNotNull(context.getLogger());
		context.getLogger().info("Logger is not null");
	}

}
