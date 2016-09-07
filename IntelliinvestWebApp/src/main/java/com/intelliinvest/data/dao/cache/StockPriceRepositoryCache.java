package com.intelliinvest.data.dao.cache;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.intelliinvest.data.dao.EODPriceRepository;
import com.intelliinvest.data.dao.LivePriceRepository;
import com.intelliinvest.data.dao.StockPriceRepository;
import com.intelliinvest.data.dao.StockRepository;
import com.intelliinvest.data.model.EODStockPrice;
import com.intelliinvest.data.model.LiveStockPrice;
import com.intelliinvest.data.model.StockPrice;

@Component("stockPriceRepository")
class StockPriceRepositoryCache implements StockPriceRepository, Cache<StockPrice>{
	private static Logger LOGGER = LoggerFactory.getLogger(StockPriceRepositoryCache.class);
	
	private final StockRepository stockRepository;
	private final EODPriceRepository eodPriceRepository;
	private final LivePriceRepository livePriceRepository;
	
	private Map<String, StockPrice> stockPriceCache = new ConcurrentHashMap<String, StockPrice>();

	@Autowired
	public StockPriceRepositoryCache(StockRepository stockRepository, 
								EODPriceRepository eodPriceRepository, 
								LivePriceRepository livePriceRepository) {
		this.stockRepository = stockRepository;
		this.eodPriceRepository = eodPriceRepository;
		this.livePriceRepository = livePriceRepository;
		init();
	}
	
	public void init() {
		initStocks();
		updateStockPrices(eodPriceRepository.getEODStockPrices(EODStockPrice.DEFAULT_EXCHANGE), livePriceRepository.getLiveStockPrices());
	}
	
	public void updateCache(Collection<StockPrice> items) {
	}
	private void initStocks(){
		for(String stock : stockRepository.getStockCodes()){
			stockPriceCache.put(stock, new StockPrice(stock));
		}
	}

	/* (non-Javadoc)
	 * @see com.intelliinvest.data.dao.StockPriceRepository#getStockPrice(java.lang.String)
	 */
	public StockPrice getStockPrice(String code){
		LOGGER.debug("Inside getStockPricefor code {}", code);
		return stockPriceCache.get(code);
	}

	/* (non-Javadoc)
	 * @see com.intelliinvest.data.dao.StockPriceRepository#getStockPrices()
	 */
	public Collection<StockPrice> getStockPrices(){
		LOGGER.debug("Inside getStockPrices()");
		return stockPriceCache.values();
	}

	/* (non-Javadoc)
	 * @see com.intelliinvest.data.dao.StockPriceRepository#updateStockPrices(java.util.Collection, java.util.Collection)
	 */
	public void updateStockPrices(Collection<EODStockPrice> eodStockPrices, Collection<LiveStockPrice> liveStockPrices) {
		updateEODStockPrices(eodStockPrices);
		updateLiveStockPrices(liveStockPrices);
	}
	
	/* (non-Javadoc)
	 * @see com.intelliinvest.data.dao.StockPriceRepository#updateEODPrices(java.util.Collection)
	 */
	public void updateEODStockPrices(Collection<EODStockPrice> eodStockPrices) {
		LOGGER.debug("Inside updateEODPrices()");
		for (EODStockPrice eodStockPrice : eodStockPrices) {
			StockPrice stockPrice = stockPriceCache.get(eodStockPrice.getSymbol());
			if(null!=stockPrice){
				stockPrice.setEodDate(eodStockPrice.getEodDate());
				stockPrice.setEodPrice(eodStockPrice.getClose());
				stockPrice.setUpdateDate(eodStockPrice.getUpdateDate());
			}else{
				LOGGER.info("Not able to find stock in stock price cache to load EOD Stock Price {}" , eodStockPrice);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.intelliinvest.data.dao.StockPriceRepository#updateLiveStockPrices(java.util.Collection)
	 */
	public void updateLiveStockPrices(Collection<LiveStockPrice> liveStockPrices) {
		LOGGER.debug("Inside updateLivePrices() {}", liveStockPrices);
		for (LiveStockPrice liveStockPrice : liveStockPrices) {
			if(null!=liveStockPrice.getCode()){
				StockPrice stockPrice = stockPriceCache.get(liveStockPrice.getCode());
				if(null!=stockPrice){
					stockPrice.setCp(liveStockPrice.getChangePercent());
					stockPrice.setCurrentPrice(liveStockPrice.getPrice());
					stockPrice.setUpdateDate(liveStockPrice.getLastUpdated());
				}else{
					LOGGER.info("Not able to find stock in stock price cache to load live Stock Price {}" , liveStockPrice);
				}
			}
		}
	}

}
