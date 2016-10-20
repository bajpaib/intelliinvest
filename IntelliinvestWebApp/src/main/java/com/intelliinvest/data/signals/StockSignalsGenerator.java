package com.intelliinvest.data.signals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.intelliinvest.data.dao.MagicNumberRepository;
import com.intelliinvest.data.dao.QuandlEODStockPriceRepository;
import com.intelliinvest.data.dao.StockRepository;
import com.intelliinvest.data.dao.StockSignalsRepository;
import com.intelliinvest.data.model.MagicNumberData;
import com.intelliinvest.data.model.QuandlStockPrice;
import com.intelliinvest.data.model.Stock;
import com.intelliinvest.data.model.StockSignalsDTO;
import com.intelliinvest.util.DateUtil;
import com.intelliinvest.util.HttpUtil;

public class StockSignalsGenerator {
	private Logger logger = Logger.getLogger(StockSignalsGenerator.class);
	
	@Autowired
	private QuandlEODStockPriceRepository quandlEODStockPriceRepository;
	
	@Autowired
	private StockSignalsRepository stockSignalsRepository;
	
	@Autowired
	private StockRepository stockRepository;
	
	@Autowired
	private DateUtil dateUtil;
	
	@Autowired
	private MagicNumberRepository magicNumberRepository;
	
	public static void main(String[] args) throws Exception{
		StockSignalsGenerator signalComponentGenerator = new StockSignalsGenerator();
		String url = "https://www.quandl.com/api/v3/datasets/NSE/INFY.csv?api_key=yhwhU_RHkVxbTtFTff9t&start_date=2013-01-01&end_date=2016-09-30";
		String eodPricesAsString = HttpUtil.getFromUrlAsString(url);
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String[] eodPricesAsArray = eodPricesAsString.split("\n");
		LinkedList<QuandlStockPrice> quandlStockPrices = new LinkedList<QuandlStockPrice>();
		for (int i = 1; i < eodPricesAsArray.length; i++) {
			String eodPriceAsString = eodPricesAsArray[i];
			String[] eodPriceAsArray = eodPriceAsString.split(",");
			LocalDate eodDate = LocalDate.parse(eodPriceAsArray[0], dateFormat);
			double open = new Double(eodPriceAsArray[1]);
			double high = new Double(eodPriceAsArray[2]);
			double low = new Double(eodPriceAsArray[3]);
			double last = new Double(eodPriceAsArray[4]);
			double wap = 0d;
			double close = new Double(eodPriceAsArray[5]);
			int tradedQty = new Double(eodPriceAsArray[6]).intValue();
			double turnover = new Double(eodPriceAsArray[7]);

			QuandlStockPrice quandlStockPrice = new QuandlStockPrice("INFY", "NSE", "EQ", open, high, low, close, last, wap, tradedQty, turnover, eodDate, LocalDateTime.now());
			quandlStockPrices.push(quandlStockPrice);
			
		}
		SignalComponentHolder signalComponentHolder = new SignalComponentHolder(10, 3);
		Integer magicNumberADX = 45;
		Double magicNumberBolliger = .17;
		Integer magicNumberOscillator = 15;
		signalComponentHolder.setMagicNumberADX(magicNumberADX);
		signalComponentHolder.setMagicNumberBolliger(magicNumberBolliger);
		signalComponentHolder.setMagicNumberOscillator(magicNumberOscillator);
		signalComponentGenerator.generateSignalsInternal(signalComponentHolder, quandlStockPrices);
	}
	
	public Boolean generateSignals(Integer ma){
		for (Stock stockDetailData : stockRepository.getStocks()) {
			try {
				generateSignals(ma, stockDetailData.getSecurityId());
			}catch (Exception e) {
				logger.info("Error generating signal for " + stockDetailData.getSecurityId() + " with error "
						+ e.getMessage(), e);
				return false;
			}
		}
		return true;
	}
	
	public List<StockSignalsDTO> generateSignals(Integer ma, String stockCode){
		List<QuandlStockPrice> quandlStockPrices = quandlEODStockPriceRepository.getStockPricesFromDB(stockCode);
		SignalComponentHolder signalComponentHolder = new SignalComponentHolder(ma, 3);
		MagicNumberData magicNumberData = magicNumberRepository.getMagicNumber(stockCode);
		if(null==magicNumberData){
			logger.info("Setting default magic number for stock" + stockCode);
			magicNumberData = new MagicNumberData(stockCode, ma);
		}
		signalComponentHolder.setMagicNumberADX(magicNumberData.getMagicNumberADX());
		signalComponentHolder.setMagicNumberBolliger(magicNumberData.getMagicNumberBollinger());
		signalComponentHolder.setMagicNumberOscillator(magicNumberData.getMagicNumberOscillator());
		return generateSignalsInternal(signalComponentHolder, quandlStockPrices);
	}

	private List<StockSignalsDTO> generateSignalsInternal(SignalComponentHolder signalComponentHolder, List<QuandlStockPrice> quandlStockPrices) {
		List<StockSignalsDTO> stockSignalsDTOs = generateSignals(signalComponentHolder, quandlStockPrices, "ALL");
		try {
			stockSignalsRepository.updateStockSignals(signalComponentHolder.getMa(), stockSignalsDTOs);
		}catch (Exception e) {
			logger.info("Error updating signals for today with error " + e.getMessage(), e);
		}
		return stockSignalsDTOs;
	}
	 
	protected List<StockSignalsDTO> generateSignals(SignalComponentHolder signalComponentHolder, List<QuandlStockPrice> quandlStockPrices, String type) {
		List<StockSignalsDTO> stockSignalsDTOs = new ArrayList<StockSignalsDTO>();
		List<SignalComponentBuilder> signalComponentBuilders = new ArrayList<SignalComponentBuilder>();
		if("ADX".equals(type) || "BOL".equals(type) || "ALL".equals(type)){
			signalComponentBuilders.add(new BaseSignalComponentBuilder());
		}
		if("ADX".equals(type) || "ALL".equals(type)){
			signalComponentBuilders.add(new ADXSignalComponentBuilder());
		}
		if("BOL".equals(type) || "ALL".equals(type)){
			signalComponentBuilders.add(new BollingerSignalComponentBuilder());
		}
		if("OSC".equals(type) || "ALL".equals(type)){
			signalComponentBuilders.add(new OscillatorSignalComponentBuilder());
		}
		SignalComponentInitilaizer signalComponentInitilaizer = new SignalComponentInitilaizer();
		for(QuandlStockPrice quandlStockPrice : quandlStockPrices){
			signalComponentInitilaizer.init(signalComponentHolder, quandlStockPrice);
			for(SignalComponentBuilder signalComponentBuilder : signalComponentBuilders){
				signalComponentBuilder.generateSignal(signalComponentHolder);
			}
			stockSignalsDTOs.add(signalComponentHolder.getStockSignalsDTOs().getLast());
		}
		return stockSignalsDTOs;
	}
	
	public List<StockSignalsDTO> generateTodaysSignal(Integer ma){
		List<StockSignalsDTO> todaysStockSignalsDTOs = new ArrayList<StockSignalsDTO>();
		for (Stock stockDetailData : stockRepository.getStocks()) {
			try {
				StockSignalsDTO stockSignalsDTO = generateTodaysSignalInternal(ma, stockDetailData.getSecurityId());
				todaysStockSignalsDTOs.add(stockSignalsDTO);
			}catch (Exception e) {
				logger.info("Error generating signal for " + stockDetailData.getSecurityId() + " with error " + e.getMessage(), e);
			}
		}
		try {
			stockSignalsRepository.updateStockSignals(ma, todaysStockSignalsDTOs);
		}catch (Exception e) {
			logger.info("Error updating signals for today with error " + e.getMessage(), e);
		}
		return todaysStockSignalsDTOs;
	}
	
	public StockSignalsDTO generateTodaysSignal(Integer ma, String stockCode){
		StockSignalsDTO stockSignalsDTO = generateTodaysSignalInternal(ma, stockCode);
		try {
			stockSignalsRepository.updateStockSignals(ma, Collections.singletonList(stockSignalsDTO));
		}catch (Exception e) {
			logger.info("Error updating signals for today with error " + e.getMessage(), e);
		}
		return stockSignalsDTO;
	}
	
	private StockSignalsDTO generateTodaysSignalInternal(Integer ma, String stockCode){
		LocalDate businessDate = dateUtil.getLastBusinessDate();
		LocalDate startDate = dateUtil.substractBusinessDays(businessDate, -20);
		List<StockSignalsDTO> stockSignalsDTOs = stockSignalsRepository.getEODStockPriceddFromStartDate(startDate, stockCode, ma);
		List<QuandlStockPrice> quandlStockPrices = quandlEODStockPriceRepository.getStockPricesFromDB(stockCode, startDate, businessDate);
		if(quandlStockPrices.isEmpty()){
			throw new RuntimeException("EOD Prices not available");
		}
		QuandlStockPrice quandlStockPrice = quandlStockPrices.get(quandlStockPrices.size()-1);
		quandlStockPrices = quandlStockPrices.subList(0, quandlStockPrices.size()-1);
		SignalComponentHolder signalComponentHolder = new SignalComponentHolder(ma, 3);
		MagicNumberData magicNumberData = magicNumberRepository.getMagicNumber(stockCode);
		if(null==magicNumberData){
			magicNumberData = new MagicNumberData(stockCode, ma);
		}
		signalComponentHolder.setMagicNumberADX(magicNumberData.getMagicNumberADX());
		signalComponentHolder.setMagicNumberBolliger(magicNumberData.getMagicNumberBollinger());
		signalComponentHolder.setMagicNumberOscillator(magicNumberData.getMagicNumberOscillator());
		signalComponentHolder.addQuandlStockPrices(quandlStockPrices);
		signalComponentHolder.addStockSignalsDTOs(stockSignalsDTOs);
		return generateTodaysSignal(signalComponentHolder, quandlStockPrice);
	}

	private StockSignalsDTO generateTodaysSignal(SignalComponentHolder signalComponentHolder, QuandlStockPrice quandlStockPrice) {
		SignalComponentInitilaizer signalComponentInitilaizer = new SignalComponentInitilaizer();
		BaseSignalComponentBuilder baseSignalComponentBuilder = new BaseSignalComponentBuilder();
		ADXSignalComponentBuilder adxSignalComponentBuilder = new ADXSignalComponentBuilder();
		BollingerSignalComponentBuilder bollingerSignalComponentBuilder = new BollingerSignalComponentBuilder();
		OscillatorSignalComponentBuilder oscillatorSignalComponentBuilder = new OscillatorSignalComponentBuilder();
		signalComponentInitilaizer.init(signalComponentHolder, quandlStockPrice);
		baseSignalComponentBuilder.generateSignal(signalComponentHolder);
		adxSignalComponentBuilder.generateSignal(signalComponentHolder);
		bollingerSignalComponentBuilder.generateSignal(signalComponentHolder);
		oscillatorSignalComponentBuilder.generateSignal(signalComponentHolder);
		return signalComponentHolder.getStockSignalsDTOs().getLast();
	}
}

