package com.howell.ecamerajing.bean;
/**
 * @author 霍之昊 
 *
 * 类说明
 */
public class Pagination {
	//需要用户填写
    public int page_size; //每页多少条记录，0:无分页
    public int page_no; //页号, from 0

    //返回
    public int total_size; //总共多少条记录
    public int cur_size; //当前页多少条记录
    public int page_count; //总共多少页
	public Pagination(int page_size, int page_no, int total_size, int cur_size,
			int page_count) {
		super();
		this.page_size = page_size;
		this.page_no = page_no;
		this.total_size = total_size;
		this.cur_size = cur_size;
		this.page_count = page_count;
	}
	public Pagination() {
		super();
	}
	public Pagination(int page_size, int page_no) {
		super();
		this.page_size = page_size;
		this.page_no = page_no;
	}
	
	public int getPage_size() {
		return page_size;
	}
	public void setPage_size(int page_size) {
		this.page_size = page_size;
	}
	public int getPage_no() {
		return page_no;
	}
	public void setPage_no(int page_no) {
		this.page_no = page_no;
	}
	public int getTotal_size() {
		return total_size;
	}
	public void setTotal_size(int total_size) {
		this.total_size = total_size;
	}
	public int getCur_size() {
		return cur_size;
	}
	public void setCur_size(int cur_size) {
		this.cur_size = cur_size;
	}
	public int getPage_count() {
		return page_count;
	}
	public void setPage_count(int page_count) {
		this.page_count = page_count;
	}
	@Override
	public String toString() {
		return "Pagination [page_size=" + page_size + ", page_no=" + page_no
				+ ", total_size=" + total_size + ", cur_size=" + cur_size
				+ ", page_count=" + page_count + "]";
	}
    
    
}
