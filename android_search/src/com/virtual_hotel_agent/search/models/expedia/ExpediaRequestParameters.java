package com.virtual_hotel_agent.search.models.expedia;

public class ExpediaRequestParameters {
	public int mNumberOfAdultsParam;

	private int mNumberOfChildrenParam;
	private int mAgeChild1;
	private int mAgeChild2;
	private int mAgeChild3;


	private int mExpediaHotelId = -1; // last viewed hotel
	
	public String mArrivalDateParam;
	public String mDepartureDateParam;

	public void setArrivalDate(String paramFromEvatureResponse) {
		mArrivalDateParam = paramFromEvatureResponse;
	}


	public void setDepartueDate(String paramFromEvatureResponse) {
		mDepartureDateParam = paramFromEvatureResponse;
		
	}

	public void setHotelId(int id) {
		mExpediaHotelId = id;
	}

	public int getHotelId() {
		return mExpediaHotelId;
	}


	public int getNumberOfChildrenParam() {
		return mNumberOfChildrenParam;
	}


	public void setNumberOfChildrenParam(int mNumberOfChildrenParam) {
		this.mNumberOfChildrenParam = mNumberOfChildrenParam;
	}


	public int getAgeChild1() {
		return mAgeChild1;
	}


	public void setAgeChild1(int mAgeChild1) {
		this.mAgeChild1 = mAgeChild1;
	}


	public int getAgeChild2() {
		return mAgeChild2;
	}


	public void setAgeChild2(int mAgeChild2) {
		this.mAgeChild2 = mAgeChild2;
	}


	public int getAgeChild3() {
		return mAgeChild3;
	}


	public void setAgeChild3(int mAgeChild3) {
		this.mAgeChild3 = mAgeChild3;
	}

	
	public void setNumberOfAdults(int paramFromUI) {
		mNumberOfAdultsParam = paramFromUI;
	}
	
	public int getNumberOfAdults() {
		return mNumberOfAdultsParam;
	}
}
