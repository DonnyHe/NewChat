package com.lodestreams.chat.common;

public class ConcreteObservable extends Observable {

	private static ConcreteObservable instance = null;
	private ConcreteObservable() {}
	public static synchronized ConcreteObservable getInstance() {
		if (instance == null) {
			instance = new ConcreteObservable();
		}
		return instance;
	}
	
	@Override
	public <T> void notifyObserver(T t, Object... objs) {
		// TODO Auto-generated method stub
		if (t == null) throw new NullPointerException();
		((Observer)t).update(objs);
	}

	@Override
	public void notifyObservers(Object... objs) {
		// TODO Auto-generated method stub
		for (Object obj : obserList) {
			this.notifyObserver(obj, objs);
		}
	}

	@Override
	public void notifyObserver(Class<?> cls, Object... objs) {
		// TODO Auto-generated method stub
		if (cls == null) throw new NullPointerException();
		Object mObject = null;
		/** search the instance */
		for (Object object : obserList) {
			if (object.getClass().getName().equals(cls.getName())) {
				mObject = object;
				break;
			}
		}
		if (mObject != null) this.notifyObserver(mObject, objs);
	}
}
