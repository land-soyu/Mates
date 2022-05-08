// IDataService.aidl
package com.matescorp.soyu.farmkinggate.service;

import com.matescorp.soyu.farmkinggate.service.IDataServiceCallback;

interface IDataService {

    boolean registerCallback(IDataServiceCallback callback);
    boolean unregisterCallback(IDataServiceCallback callback);
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);
}
