// IDataSerivceCallback.aidl
package com.matescorp.soyu.farmkinggate.service;

// Declare any non-default types here with import statements

interface IDataServiceCallback {
	oneway void valueChanged(long value);
}
