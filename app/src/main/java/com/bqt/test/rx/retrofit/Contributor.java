package com.bqt.test.rx.retrofit;

public class Contributor {
	public String login;
	public long contributions;
	
	public Contributor() {
	}
	
	public Contributor(String login, long contributions) {
		this.login = login;
		this.contributions = contributions;
	}
}