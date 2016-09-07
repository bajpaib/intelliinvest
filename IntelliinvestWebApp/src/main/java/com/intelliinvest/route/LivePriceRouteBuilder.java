package com.intelliinvest.route;

import java.io.IOException;
import java.util.concurrent.Executors;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.intelliinvest.route.processor.GetStockCodesProcessor;
import com.intelliinvest.route.processor.LiveStockPriceImporterProcessor;
import com.intelliinvest.route.processor.LiveStockPricesUpdateProcessor;
import com.intelliinvest.route.processor.StockPricesUpdateLiveProcessor;
import com.intelliinvest.route.strategy.LivePriceAggregationStrategy;
import com.intelliinvest.util.SubListIterator;

@Component
public class LivePriceRouteBuilder extends RouteBuilder{
	
//	public static String LIVE_SCHEDULE_QUARTZ_EXPRESSION = "quartz2://livePriceTimer?cron=0+0/5+8-16+?+*+MON_FRI";
	public static String SCHEDULE_LOAD_LIVE_PRICES = "quartz2://livePriceTimer?cron=0+0/5+8-16+?+*+*";
	public static String LOAD_LIVE_PRICES = "direct:loadLivePrices";

	private final LivePriceAggregationStrategy livePriceAggregationStrategy;

	@Autowired
	public LivePriceRouteBuilder(LivePriceAggregationStrategy livePriceAggregationStrategy) {
		this.livePriceAggregationStrategy = livePriceAggregationStrategy;
	}

	@Override
	public void configure() throws Exception {
		
		onException(IOException.class).handled(true).setBody().simple("Problem retrieving data from google for ${body} with error ${exchangeProperty.CamelExceptionCaught}" ).to("log:ProblemRetrieveGoogle").end();
		onException(Exception.class).handled(true).to(LogRouteBuilder.LOG_ERROR).end();
		
		from(SCHEDULE_LOAD_LIVE_PRICES)
			.to(LOAD_LIVE_PRICES)
		.end();
		
		from(LOAD_LIVE_PRICES)
			.process(GetStockCodesProcessor.PROCESSOR_NAME)
			.bean(SubListIterator.class, "create(${body}, 10)")
			.split(body(), livePriceAggregationStrategy).parallelProcessing().executorService(Executors.newFixedThreadPool(1))
        		.process(LiveStockPriceImporterProcessor.PROCESSOR_NAME)
        	.end()
        	.process(LiveStockPricesUpdateProcessor.PROCESSOR_NAME)
			.process(StockPricesUpdateLiveProcessor.PROCESSOR_NAME)
		.end();
	}
	
//	@SuppressWarnings("deprecation")
//	public static void main(String[] args) throws Exception {
//		CamelContext context = new DefaultCamelContext(new SimpleRegistry());
//		LivePriceRouteBuilder livePriceRouteBuilder = new LivePriceRouteBuilder(new LivePriceAggregationStrategy());
//		SimpleRegistry simpleRegistry = (SimpleRegistry)((PropertyPlaceholderDelegateRegistry)context.getRegistry()).getRegistry();
//		MongoTemplate mongoTemplate = new MongoTemplate(new Mongo("localhost", 27017), "test");
//		mongoTemplate.getDb().dropDatabase();
//		mongoTemplate.insert(getStocks(), Stock.class);
//		
//		GoogleLivePriceImporter livePriceImporter = new GoogleLivePriceImporter();
//		LivePriceRepository livePriceRepository = new LivePriceRepositoryCache(new LivePriceRepositoryDB(mongoTemplate));
//		EODPriceRepository eodPriceRepository = new EODPriceRepositoryCache(new EODPriceRepositoryDB(mongoTemplate));
//		StockRepository stockRepository = new StockRepositoryCache(new StockRepositoryDB(mongoTemplate));
//		StockPriceRepository stockPriceRepository = new StockPriceRepositoryCache(stockRepository, eodPriceRepository, livePriceRepository);
//		
//		LiveStockPriceService liveStockPriceService = new LiveStockPriceService(livePriceRepository);
//		StockService stockService = new StockService(stockRepository);
//		StockPriceService stockPriceService = new StockPriceService(stockPriceRepository);
//		
//		simpleRegistry.put(LiveStockPriceImporterProcessor.PROCESSOR_NAME, new LiveStockPriceImporterProcessor(context, livePriceImporter));
//		simpleRegistry.put(LiveStockPricesUpdateProcessor.PROCESSOR_NAME, new LiveStockPricesUpdateProcessor(context, liveStockPriceService));
//		simpleRegistry.put(StockPricesUpdateLiveProcessor.PROCESSOR_NAME, new StockPricesUpdateLiveProcessor(context, stockPriceService));
//		simpleRegistry.put(GetStockCodesProcessor.PROCESSOR_NAME, new GetStockCodesProcessor(context, stockService));
//		
//		context.addRoutes(livePriceRouteBuilder);
//		context.addRoutes(new LogRouteBuilder());
//		context.start();
//		
//		fetchFromGoogle(context);
//		context.stop();
//	}
//	
//	public static void fetchFromGoogle(CamelContext context) throws Exception{
//		DefaultExchange exchange = new DefaultExchange(context);
//		exchange.getIn().setBody(getStocks());
//		context.createProducerTemplate().send(LOAD_LIVE_PRICES, exchange);
//		System.out.println(exchange.getIn().getBody());
//	}
//	
//	public static List<Stock> getStocks(){
//		List<Stock> stocks = new ArrayList<Stock>();
//		stocks.add(new Stock("INFY", "INFY", false, true,new Date()));
//		stocks.add(new Stock("ABAN", "ABAN", false, true,new Date()));
//		stocks.add(new Stock("AAAA", "AAAA", false, true,new Date()));
//		return stocks;
//	}
}
