package com.howell.ecamerajing.bean;

public class SystimeInfo {

	private short wYear;
	private short wMonth;
	private short wDayofWeek;
	private short wDay;
	private short wHour;
	private short wMinute;
	private short wSecond;
	private short wMilliseconds;
	public short getwYear() {
		return wYear;
	}
	public void setwYear(short wYear) {
		this.wYear = wYear;
	}
	public short getwMonth() {
		return wMonth;
	}
	public void setwMonth(short wMonth) {
		this.wMonth = wMonth;
	}
	public short getwDayofWeek() {
		return wDayofWeek;
	}
	public void setwDayofWeek(short wDayofWeek) {
		this.wDayofWeek = wDayofWeek;
	}
	public short getwDay() {
		return wDay;
	}
	public void setwDay(short wDay) {
		this.wDay = wDay;
	}
	public short getwHour() {
		return wHour;
	}
	public void setwHour(short wHour) {
		this.wHour = wHour;
	}
	public short getwMinute() {
		return wMinute;
	}
	public void setwMinute(short wMinute) {
		this.wMinute = wMinute;
	}
	public short getwSecond() {
		return wSecond;
	}
	public void setwSecond(short wSecond) {
		this.wSecond = wSecond;
	}
	public short getwMilliseconds() {
		return wMilliseconds;
	}
	public void setwMilliseconds(short wMilliseconds) {
		this.wMilliseconds = wMilliseconds;
	}
	@Override
	public String toString() {
		return "SystimeInfo [wYear=" + wYear + ", wMonth=" + wMonth + ", wDayofWeek=" + wDayofWeek + ", wDay=" + wDay
				+ ", wHour=" + wHour + ", wMinute=" + wMinute + ", wSecond=" + wSecond + ", wMilliseconds="
				+ wMilliseconds + "]";
	}
	
}
