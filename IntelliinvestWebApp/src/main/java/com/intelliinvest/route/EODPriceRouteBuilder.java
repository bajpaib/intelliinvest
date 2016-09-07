package com.intelliinvest.route;

import java.io.IOException;
import java.util.concurrent.Executors;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.bindy.csv.BindyCsvDataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.intelliinvest.data.model.EODStockPrice;
import com.intelliinvest.route.processor.EODStockPriceImporterProcessor;
import com.intelliinvest.route.processor.EODStockPricesEnrichProcessor;
import com.intelliinvest.route.processor.EODStockPricesUpdateDBProcessor;
import com.intelliinvest.route.processor.EODStockPricesUpdateProcessor;
import com.intelliinvest.route.processor.GetStockCodesProcessor;
import com.intelliinvest.route.processor.PopulateEODDatesProcessor;
import com.intelliinvest.route.processor.StockPricesUpdateEODProcessor;
import com.intelliinvest.route.strategy.EODBackloadPriceAggregationStrategy;
import com.intelliinvest.route.strategy.EODPriceAggregationStrategy;

@Component
public class EODPriceRouteBuilder extends RouteBuilder{
	
	public static String LOAD_EOD_PRICES = "direct:loadEODPrices";
	public static String BACKLOAD_EOD_PRICES = "direct:backloadEODPrices";
	public static String SCHEDULE_LOAD_EOD_PRICES = "quartz2://quandl/eodTimer?cron=0+%d+%d+?+*+*";
	
	private final EODPriceAggregationStrategy eodPriceAggregationStrategy;
	private final EODBackloadPriceAggregationStrategy eodBackloadPriceAggregationStrategy;
	
	private final BindyCsvDataFormat bindy = new BindyCsvDataFormat(EODStockPrice.class);
	
	@Autowired
	public EODPriceRouteBuilder(EODPriceAggregationStrategy eodPriceAggregationStrategy, EODBackloadPriceAggregationStrategy eodBackloadPriceAggregationStrategy) {
		this.eodPriceAggregationStrategy = eodPriceAggregationStrategy;
		this.eodBackloadPriceAggregationStrategy = eodBackloadPriceAggregationStrategy;
	}
	
	@Override
	public void configure() throws Exception {
		
//		int dailyEODPriceRefreshStartHour = new Integer(
//		IntelliInvestStore.properties.getProperty("daily.eod.price.refresh.start.hr"));
//		int dailyEODPriceRefreshStartMin = new Integer(
//		IntelliInvestStore.properties.getProperty("daily.eod.price.refresh.start.min"));

		int dailyEODPriceRefreshStartHour = 1;
		int dailyEODPriceRefreshStartMin = 30;
		
		onException(IllegalArgumentException.class).setBody().simple("No data when retrieving prices from quandl for ${header.code}  with error ${exchangeProperty.CamelExceptionCaught}" ).to("log:NoQuandlData").end();
		onException(IOException.class).maximumRedeliveries(2).redeliveryDelay(30000).setBody().simple("Problem retrieving data from quandl for ${header.code}  with error ${exchangeProperty.CamelExceptionCaught}" ).to("log:ProblemRetrieveQuandl").end();
		onException(Exception.class).to(LogRouteBuilder.LOG_ERROR).end();
		
		from(String.format(SCHEDULE_LOAD_EOD_PRICES, dailyEODPriceRefreshStartMin, dailyEODPriceRefreshStartHour))
			.to(LOAD_EOD_PRICES)
		.end();
		
		from(LOAD_EOD_PRICES)
			.process(GetStockCodesProcessor.PROCESSOR_NAME)
			.process(PopulateEODDatesProcessor.PROCESSOR_NAME)
			.split(body(), eodPriceAggregationStrategy).parallelProcessing()
	        	.executorService(Executors.newFixedThreadPool(20))
	        	.setHeader("code").simple("${body}")
				.process(EODStockPriceImporterProcessor.PROCESSOR_NAME)
				.unmarshal(bindy)
				.process(EODStockPricesEnrichProcessor.PROCESSOR_NAME)
			.end()
			.process(EODStockPricesUpdateProcessor.PROCESSOR_NAME)
			.process(StockPricesUpdateEODProcessor.PROCESSOR_NAME)
		.end();
			
		from(BACKLOAD_EOD_PRICES)
			.process(GetStockCodesProcessor.PROCESSOR_NAME)
			.split(body(), eodBackloadPriceAggregationStrategy).parallelProcessing()
	        	.executorService(Executors.newFixedThreadPool(1))
	        	.setHeader("code").simple("${body}")
				.process(EODStockPriceImporterProcessor.PROCESSOR_NAME)
		    	.unmarshal(bindy)
		    	.process(EODStockPricesEnrichProcessor.PROCESSOR_NAME)
		    	.process(EODStockPricesUpdateDBProcessor.PROCESSOR_NAME)
		    .end()
		.end();
	}

//	@SuppressWarnings("deprecation")
//	public static void main(String[] args) throws Exception{
//		CamelContext context = new DefaultCamelContext(new SimpleRegistry());
//		EODPriceRouteBuilder eodPriceRouteBuilder = new EODPriceRouteBuilder(new EODPriceAggregationStrategy());
//		SimpleRegistry simpleRegistry = (SimpleRegistry)((PropertyPlaceholderDelegateRegistry)context.getRegistry()).getRegistry();
//		MongoTemplate mongoTemplate = new MongoTemplate(new Mongo("localhost", 27017), "test");
//		mongoTemplate.getDb().dropDatabase();
//		mongoTemplate.insert(getStocks(), Stock.class);
//		
//		EODPriceImporter eodPriceImporter = new QuandlEODPriceImporter(new IntelliInvestRepositoryDB(mongoTemplate));
//		
//		EODPriceRepository eodPriceRepository = new EODPriceRepositoryCache(new EODPriceRepositoryDB(mongoTemplate));
//		LivePriceRepository livePriceRepository = new LivePriceRepositoryCache(new LivePriceRepositoryDB(mongoTemplate));
//		EODHistoryPriceRepository eodHistoryPriceRepository = new EODHistoryPriceRepositoryDB(mongoTemplate);
//		StockRepository stockRepository = new StockRepositoryCache(new StockRepositoryDB(mongoTemplate));
//		StockPriceRepository stockPriceRepository = new StockPriceRepositoryCache(stockRepository, eodPriceRepository, livePriceRepository);
//		
//		EODStockPriceService eodStockPriceService = new EODStockPriceService(eodPriceRepository, eodHistoryPriceRepository);
//		StockService stockService = new StockService(stockRepository);
//		StockPriceService stockPriceService = new StockPriceService(stockPriceRepository);
//		
//		simpleRegistry.put(EODStockPricesEnrichProcessor.PROCESSOR_NAME, new EODStockPricesEnrichProcessor(context));
//		simpleRegistry.put(EODStockPriceImporterProcessor.PROCESSOR_NAME, new EODStockPriceImporterProcessor(context, eodPriceImporter));
//		simpleRegistry.put(EODStockPricesUpdateProcessor.PROCESSOR_NAME, new EODStockPricesUpdateProcessor(context, eodStockPriceService));
//		simpleRegistry.put(EODStockPricesUpdateDBProcessor.PROCESSOR_NAME, new EODStockPricesUpdateDBProcessor(context, eodStockPriceService));
//		simpleRegistry.put(PopulateEODDatesProcessor.PROCESSOR_NAME, new PopulateEODDatesProcessor(context));
//		simpleRegistry.put(StockPricesUpdateEODProcessor.PROCESSOR_NAME, new StockPricesUpdateEODProcessor(context, stockPriceService));
//		simpleRegistry.put(GetStockCodesProcessor.PROCESSOR_NAME, new GetStockCodesProcessor(context, stockService));
//		
//		context.addRoutes(eodPriceRouteBuilder);
//		context.addRoutes(new LogRouteBuilder());
//		context.getTypeConverterRegistry().addTypeConverters(new Converter());
//		context.start();
//		loadFromQuandl(context);
//		context.stop();
//	}
//
//	private static void loadFromQuandl(CamelContext context) {
//		DefaultExchange exchange = new DefaultExchange(context);
//		exchange.setProperty("startDate", "2016-08-01");
//		exchange.setProperty("endDate", "2016-08-01");
//		context.createProducerTemplate().send(LOAD_EOD_PRICES, exchange);
//		System.out.println(exchange.getIn().getBody());
//	}
//	
//	public static List<Stock> getStocks(){
//		List<Stock> stocks = new ArrayList<Stock>();
//		stocks.add(new Stock("INFY", "INFY", false, true,new Date()));
//		stocks.add(new Stock("ABAN", "ABAN", false, true,new Date()));
//		return stocks;
//	}
}


