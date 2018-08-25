package com.bqt.test.rx.simple;

public interface Watched {
	void addWatcher(Watcher watcher);
	
	void removeWatcher(Watcher watcher);
	
	void notifyWatchers(String str);
}