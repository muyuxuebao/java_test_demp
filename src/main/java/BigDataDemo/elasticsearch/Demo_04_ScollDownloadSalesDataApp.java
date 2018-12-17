package BigDataDemo.elasticsearch;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;

public class Demo_04_ScollDownloadSalesDataApp {


	/**
	 * 基于scroll的搜索
	 * 比如说，现在要下载大批量的数据，从es，放到excel中，我们说，月度，或者年度，销售记录，很多，比如几千条，几万条，几十万条
	 * 其实就要用到我们之前讲解的es scroll api，对大量数据批量的获取和处理
	 * @param args
	 * @throws Exception
	 */
	@SuppressWarnings({ "resource", "unchecked" })
	public static void main(String[] args) throws Exception {
		Settings settings = Settings.builder()
				.put("cluster.name", "yl_test-application")
				.put("client.transport.sniff", true)
				.build();

		TransportClient client = new PreBuiltTransportClient(settings)
				.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("192.168.55.170"), 9300))
				.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("192.168.55.170"), 9301))
				.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("192.168.55.170"), 9302));
	
		SearchResponse searchResponse = client.prepareSearch("car_shop") 
				.setTypes("sales")
				.setQuery(QueryBuilders.termQuery("brand.keyword", "宝马"))
				.setScroll(new TimeValue(60000))
				.setSize(1)
				.get();
		
		int batchCount = 0;
		
		do {
			for(SearchHit searchHit : searchResponse.getHits().getHits()) {
				System.out.println("batch: " + ++batchCount); 
				System.out.println(searchHit.getSourceAsString());  
				
				// 每次查询一批数据，比如1000行，然后写入本地的一个excel文件中
				
				// 如果说你一下子查询几十万条数据，不现实，jvm内存可能都会爆掉
			}
			
			searchResponse = client.prepareSearchScroll(searchResponse.getScrollId())
					.setScroll(new TimeValue(60000))
					.execute()
					.actionGet();
		} while(searchResponse.getHits().getHits().length != 0);
		
		client.close();
	}
	
}
