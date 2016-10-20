package com.intelliinvest.data.signals;

import java.util.LinkedList;
import java.util.List;

import com.intelliinvest.data.model.QuandlStockPrice;
import com.intelliinvest.data.model.StockSignalsDTO;

public class SignalComponentHolder {
	private final LinkedList<QuandlStockPrice> quandlStockPrices = new LinkedList<QuandlStockPrice>();
	private final LinkedList<StockSignalsDTO> stockSignalsDTOs = new LinkedList<StockSignalsDTO>();

	Integer magicNumberADX = 45;
	Double magicNumberBolliger = .17;
	Integer magicNumberOscillator = 15;
	
	private final int ma;
	private final int stockSignalsDTOSize;
	
	public SignalComponentHolder(int ma, int stockSignalsDTOSize) {
		this.ma = ma;
		this.stockSignalsDTOSize = stockSignalsDTOSize;
	}
	
	public Integer getMagicNumberADX() {
		return magicNumberADX;
	}
	
	public void setMagicNumberADX(Integer magicNumberADX) {
		this.magicNumberADX = magicNumberADX;
	}
	
	public Double getMagicNumberBolliger() {
		return magicNumberBolliger;
	}
	
	public void setMagicNumberBolliger(Double magicNumberBolliger) {
		this.magicNumberBolliger = magicNumberBolliger;
	}
	
	public Integer getMagicNumberOscillator() {
		return magicNumberOscillator;
	}
	
	public void setMagicNumberOscillator(Integer magicNumberOscillator) {
		this.magicNumberOscillator = magicNumberOscillator;
	}
	
	public void addQuandlStockPrice(QuandlStockPrice quandlStockPrice){
		if(quandlStockPrices.size() >= ma){
			quandlStockPrices.removeFirst();
		}
		quandlStockPrices.addLast(quandlStockPrice);
	}
	
	public void addQuandlStockPrices(List<QuandlStockPrice> quandlStockPrices){
		for(QuandlStockPrice quandlStockPrice : quandlStockPrices){
			addQuandlStockPrice(quandlStockPrice);
		}
	}
	
	public void addStockSignalsDTO(StockSignalsDTO stockSignalsDTO){
		if(stockSignalsDTOs.size() >= stockSignalsDTOSize){
			stockSignalsDTOs.removeFirst();
		}
		stockSignalsDTOs.addLast(stockSignalsDTO);
	}
	
	public void addStockSignalsDTOs(List<StockSignalsDTO> stockSignalsDTOs){
		for(StockSignalsDTO stockSignalsDTO : stockSignalsDTOs){
			addStockSignalsDTO(stockSignalsDTO);
		}
	}
	
	public LinkedList<QuandlStockPrice> getQuandlStockPrices() {
		return quandlStockPrices;
	}
	
	public LinkedList<StockSignalsDTO> getStockSignalsDTOs() {
		return stockSignalsDTOs;
	}
	
	public int getStockSignalsDTOSize() {
		return stockSignalsDTOSize;
	}
	
	public int getMa() {
		return ma;
	}
}
