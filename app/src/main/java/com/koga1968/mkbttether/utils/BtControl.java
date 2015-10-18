package com.koga1968.mkbttether.utils;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothProfile.ServiceListener;
import android.content.Context;
import android.content.Intent;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * bluetooth PAN access
 *
 * Created by koga1968 on 15/04/20.
 */
public class BtControl {
    private BluetoothAdapter m_btAdapter = null;
    private Class<?> m_classBtPan = null;
    private Object m_btPan = null;
    private Activity m_activity = null;
    private String m_lastError = "";
    private BtPanConnectListener m_listener = null;

    public BtControl(Activity a, Context c) throws ClassNotFoundException {
        clearLastError();
        m_activity = a;
        m_listener = (BtPanConnectListener)a;
        m_btAdapter = BluetoothAdapter.getDefaultAdapter();
        m_classBtPan = Class.forName("android.bluetooth.BluetoothPan");

//        for (Method m : ms) {
//            Log.d("methods", m.getName());
//        }

        Constructor<?> ct = null;
        try {
            ct = m_classBtPan.getDeclaredConstructor(Context.class, ServiceListener.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            setLastError("BtControl: " + e.getMessage());
        }
        if (ct == null) {
            setLastError("ct is NULL");
            return;
        }
        ct.setAccessible(true);
        try {
            m_btPan = ct.newInstance(c, new ServiceListener() {
                @Override
                public void onServiceConnected(int profile, BluetoothProfile proxy) {
                    m_listener.OnConnected(true);
                }

                @Override
                public void onServiceDisconnected(int profile) {
                    m_listener.OnConnected(false);
                }
            });
//        } catch (InstantiationException e) {
//        } catch (IllegalAccessException e) {
//        } catch (IllegalArgumentException e) {
//        } catch (InvocationTargetException e) {
        } catch (Exception e) {
            e.printStackTrace();
            setLastError("BtControl: " + e.getMessage());
        }
    }

    private void clearLastError() {
        m_lastError = "";
    }
    private void setLastError(String str) {
        m_lastError = str;
    }
    public String getLastError() {
        String m = m_lastError;
        clearLastError();
        return m;
    }

    public boolean isBtOn() {
        clearLastError();
        boolean ret = false;
        if (m_btAdapter == null) {
            ret = false;
        } else if (m_btAdapter.isEnabled()) {
            ret = true;
        }

        return ret;
    }

    public boolean isTetheringOn() throws InvocationTargetException {
        clearLastError();
        if (m_btAdapter == null) {
            return false;
        }
        if (!m_btAdapter.isEnabled()) {
            return false;
        }

        boolean isEnabled = false;
        Class noParams[] = {};

        Method m = null;
        try {
            m = m_classBtPan.getDeclaredMethod("isTetheringOn", noParams);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            setLastError("isTetheringOn: " + e.toString());
        }
        if (m != null) {
            try {
                isEnabled = (boolean) m.invoke(m_btPan, (Object[]) null);
                setLastError("isTetheringOn ret " + (isEnabled ? "true" : "false"));
//            } catch (IllegalAccessException e) {
//            } catch (IllegalArgumentException e) {
//            } catch (NullPointerException e) {
            } catch (InvocationTargetException e) {
                throw(e);
            } catch (Exception e) {
                e.printStackTrace();
                setLastError("isTetheringOn: " + e.toString());
            }
        }
        return isEnabled;
    }

    public boolean setBtOn(boolean bOn) {
        clearLastError();
        if (m_btAdapter == null) {
            return false;
        }
        if (bOn) {
            m_activity.startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
        } else {
            m_btAdapter.disable();
        }
        return true;
    }

    public boolean setTetheringOn(boolean bOn) throws InvocationTargetException {
        clearLastError();
        if (m_btAdapter == null) {
            return false;
        }
        if (!m_btAdapter.isEnabled()) {
            return false;
        }
        boolean result = false;
        Method m = null;
        Class[] paramType = new Class[]{boolean.class};
        Object[] param = new Object[]{bOn};
        try {
            m = m_classBtPan.getDeclaredMethod("setBluetoothTethering", paramType);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            setLastError(e.toString());
        }
        try {
            if (m != null) {
                m.invoke(m_btPan, param);
                result = true;
            }
//        } catch (IllegalAccessException e) {
//        } catch (IllegalArgumentException e) {
//        } catch (NullPointerException e) {
        } catch (InvocationTargetException e) {
            throw(e);
        } catch (Exception e) {
            e.printStackTrace();
            setLastError(e.toString());
        }

        return result;
    }
}
