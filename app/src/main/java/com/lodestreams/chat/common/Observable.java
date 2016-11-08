package com.lodestreams.chat.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class Observable {
	
	public final List<Observer> obserList = new ArrayList<Observer>();
	
	/** Attach Observer
	 * <b>Notice:</b> ob can't be null ,or it will throw NullPointerException
	 * */
	public <T extends Observer> void registerObserver(T ob) {
		if (ob == null) throw new NullPointerException();
		synchronized (obserList) {
			if (!obserList.contains(ob)) {
				obserList.add(ob);
			}
		}
	}

	/** Unattach Observer
	 * <b>Notice:</b> obName can't be null ,or it will throw NullPointerException<br>
	 * <b>It reverses with attachObserver() method</b>
	 * */
	public <T extends Observer> void unRegisterObserver(T ob) {
		if (ob == null) throw new NullPointerException();
		this.unRegisterObserver(ob.getClass());
	}
	
	/** Unattach Observer
	 * <b>Notice:</b> cls can't be null ,or it will throw NullPointerException<br>
	 * <b>It reverses with attachObserver() method</b>
	 * */
	public void unRegisterObserver(Class<?> cls) {
		if(cls == null) throw new NullPointerException();
		synchronized(obserList){
			Iterator<? extends Observer> iterator = obserList.iterator();
			while(iterator.hasNext()) {
				if(iterator.next().getClass().getName().equals(cls.getName())){
					iterator.remove();
					break;
				}
			}
		}
	}
	
	/** detach all observers */
	public void unRegisterAll() {
		synchronized(obserList) {
			obserList.clear();
		}
	}
	
	/** Ruturn the size of observers */
	public int countObservers() {
		synchronized(obserList) {
			return obserList.size();
		}
	}
	
	/**
	 * notify all observer
	 * @param objs
	 */
	public abstract void notifyObservers(Object... objs);
	
	/**
	 * notify one certain observer
	 * @param cls
	 * @param objs
	 */
	public abstract	void notifyObserver(Class<?> cls, Object... objs);
	
	/**
	 * notify one certain observer
	 * @param cls
	 * @param objs
	 */
	public abstract <T> void notifyObserver(T t, Object... objs);
}
