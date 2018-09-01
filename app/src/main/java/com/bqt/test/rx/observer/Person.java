package com.bqt.test.rx.observer;

import java.util.List;

public class Person {
	private String name;
	private int age;
	public List<String> loves;
	
	public Person(List<String> loves) {
		this.loves = loves;
	}
	
	public Person(String name, int age) {
		this.name = name;
		this.age = age;
	}
	
	@Override
	public String toString() {
		return "Person{" +
				"name='" + name + '\'' +
				", age=" + age +
				'}';
	}
}
