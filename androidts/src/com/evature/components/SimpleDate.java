package com.evature.components;

public class SimpleDate {
	public int year;
	public int month;
	public int day;
	
	public SimpleDate(int year, int month, int day) {
		this.year = year;
		this.month = month;
		this.day = day;
	}
	
	public boolean equals(SimpleDate date) {
		return (year == date.year && month == date.month && day == date.day);
	}
	
}
