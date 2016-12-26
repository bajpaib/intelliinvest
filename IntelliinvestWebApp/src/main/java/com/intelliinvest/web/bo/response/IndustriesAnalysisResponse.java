package com.intelliinvest.web.bo.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.intelliinvest.data.model.IndustryFundamentalAnalysis;

public class IndustriesAnalysisResponse {

	private List<IndustryFundamentalAnalysis> industriesFundamentalAnalysis;
	private boolean success;
	private String message;

	public List<IndustryFundamentalAnalysis> getIndustriesFundamentalAnalysis() {
		if (industriesFundamentalAnalysis == null)
			industriesFundamentalAnalysis = new ArrayList<>();
		return industriesFundamentalAnalysis;
	}

	public void setIndustriesFundamentalAnalysis(List<IndustryFundamentalAnalysis> industriesFundamentalAnalysis) {
		this.industriesFundamentalAnalysis = industriesFundamentalAnalysis;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "IndustriesAnalysisResponse [industriesFundamentalAnalysis=" + industriesFundamentalAnalysis
				+ ", success=" + success + ", message=" + message + "]";
	}

}
