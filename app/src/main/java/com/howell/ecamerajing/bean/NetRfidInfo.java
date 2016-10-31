package com.howell.ecamerajing.bean;
/**
 * @author 
 *
 * 类说明:包名和类名不要改
 */
public class NetRfidInfo {
	private int id;
	private int attenuation;	//衰减值0-31
	private int type;
	private int rangeBeg;
	private int rangeEnd;
	
	public NetRfidInfo() {
		super();
	}
	public NetRfidInfo(int id, int attenuation, int type, int rangeBeg,
			int rangeEnd) {
		super();
		this.id = id;
		this.attenuation = attenuation;
		this.type = type;
		this.rangeBeg = rangeBeg;
		this.rangeEnd = rangeEnd;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getAttenuation() {
		return attenuation;
	}
	public void setAttenuation(int attenuation) {
		this.attenuation = attenuation;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getRangeBeg() {
		return rangeBeg;
	}
	public void setRangeBeg(int rangeBeg) {
		this.rangeBeg = rangeBeg;
	}
	public int getRangeEnd() {
		return rangeEnd;
	}
	public void setRangeEnd(int rangeEnd) {
		this.rangeEnd = rangeEnd;
	}
	@Override
	public String toString() {
		return "NetRfidInfo [id=" + id + ", attenuation=" + attenuation
				+ ", type=" + type + ", rangeBeg=" + rangeBeg + ", rangeEnd="
				+ rangeEnd + "]";
	}
	
	
}
