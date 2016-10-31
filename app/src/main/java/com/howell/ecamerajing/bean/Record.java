package com.howell.ecamerajing.bean;
/**
 * @author 
 *
 * 类说明:一段开始时间和结束时间的实体类，包名和类名不要改
 */
public class Record {
	private short begYear;
	private short begMonth;
	private short begDay;
	private short begHour;
	private short begMinute;
	private short begSecond;
	private short endYear;
	private short endMonth;
	private short endDay;
	private short endHour;
	private short endMinute;
	private short endSecond;
	public Record() {
		super();
	}
	public Record(short begYear, short begMonth, short begDay, short begHour,
			short begMinute, short begSecond, short endYear, short endMonth,
			short endDay, short endHour, short endMinute, short endSecond) {
		super();
		this.begYear = begYear;
		this.begMonth = begMonth;
		this.begDay = begDay;
		this.begHour = begHour;
		this.begMinute = begMinute;
		this.begSecond = begSecond;
		this.endYear = endYear;
		this.endMonth = endMonth;
		this.endDay = endDay;
		this.endHour = endHour;
		this.endMinute = endMinute;
		this.endSecond = endSecond;
	}
	public short getBegYear() {
		return begYear;
	}
	public void setBegYear(short begYear) {
		this.begYear = begYear;
	}
	public short getBegMonth() {
		return begMonth;
	}
	public void setBegMonth(short begMonth) {
		this.begMonth = begMonth;
	}
	public short getBegDay() {
		return begDay;
	}
	public void setBegDay(short begDay) {
		this.begDay = begDay;
	}
	public short getBegHour() {
		return begHour;
	}
	public void setBegHour(short begHour) {
		this.begHour = begHour;
	}
	public short getBegMinute() {
		return begMinute;
	}
	public void setBegMinute(short begMinute) {
		this.begMinute = begMinute;
	}
	public short getBegSecond() {
		return begSecond;
	}
	public void setBegSecond(short begSecond) {
		this.begSecond = begSecond;
	}
	public short getEndYear() {
		return endYear;
	}
	public void setEndYear(short endYear) {
		this.endYear = endYear;
	}
	public short getEndMonth() {
		return endMonth;
	}
	public void setEndMonth(short endMonth) {
		this.endMonth = endMonth;
	}
	public short getEndDay() {
		return endDay;
	}
	public void setEndDay(short endDay) {
		this.endDay = endDay;
	}
	public short getEndHour() {
		return endHour;
	}
	public void setEndHour(short endHour) {
		this.endHour = endHour;
	}
	public short getEndMinute() {
		return endMinute;
	}
	public void setEndMinute(short endMinute) {
		this.endMinute = endMinute;
	}
	public short getEndSecond() {
		return endSecond;
	}
	public void setEndSecond(short endSecond) {
		this.endSecond = endSecond;
	}
	@Override
	public String toString() {
		return "Record [begYear=" + begYear + ", begMonth=" + begMonth
				+ ", begDay=" + begDay + ", begHour=" + begHour
				+ ", begMinute=" + begMinute + ", begSecond=" + begSecond
				+ ", endYear=" + endYear + ", endMonth=" + endMonth
				+ ", endDay=" + endDay + ", endHour=" + endHour
				+ ", endMinute=" + endMinute + ", endSecond=" + endSecond + "]";
	}
	
}
