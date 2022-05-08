/*
 * Copyright (c) 2014, Ericsson AB. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */

package com.matescorp.system.zaigle.Bluetooth;

import android.os.Message;
import android.os.Messenger;
import android.util.Log;

public class BTConfig {
    public static final int SP_BT_SEARCH = 101;
    public static final int SP_BT_SEARCH_CANCEL = 102;
    public static final int SP_BT_SEARCH_DEVICE = 103;
    public static final int SP_BT_SEARCH_LINK = 104;
    public static final int SP_BT_SEARCH_LINK_COMPLET = 105;
    public static final int MAIN_START = 100;


    public static final int SP_BT_CONNECT = 201;


    public static final int SP_BT_DEVICE_CONNECT = 301;
    public static final int SP_BT_DEVICE_DISCONNECT = 302;


    public static final int SP_BT_STATE_CONNECT = 401;

    public static final int SP_BT_SERVICE_START = 901;
    public static final int SP_BT_ACTIVITY_START = 902;
    public static final int SP_BT_ACTIVITY_END = 903;




    public static void sendMessage(Messenger m, int what, int arg1, int arg2, Object obj) {
        try {
            Message msg = Message.obtain(null, what, null);
            msg.arg1 = arg1;
            msg.arg2 = arg2;
            msg.obj = obj;
            m.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
