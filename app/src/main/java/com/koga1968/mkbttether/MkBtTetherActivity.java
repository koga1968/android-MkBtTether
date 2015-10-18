package com.koga1968.mkbttether;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.koga1968.mkbttether.utils.BtPanConnectListener;
import com.koga1968.mkbttether.utils.BtControl;

import java.lang.reflect.InvocationTargetException;

/**
 * bluetooth PAN access
 *
 * Created by koga1968 on 15/04/20.
 */
public class MkBtTetherActivity extends Activity implements BtPanConnectListener {
    private BtControl m_btControl = null;
    private TextView m_tvLog = null;
    private Switch m_swBt = null;
    private Switch m_swBtTether = null;

    private void setBtStatus() {
        try {
            if (m_btControl == null) {
                m_btControl = new BtControl(this, getApplicationContext());
            }
            addLog(m_btControl.getLastError());
            setBtOn(m_btControl.isBtOn());
            setTetheringOn(m_btControl.isTetheringOn());
        } catch (Exception e) {
            m_btControl = null;
            setBtOn(false);
            setTetheringOn(false);
            setLog(e.toString());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_tether);
        m_tvLog = (TextView)findViewById(R.id.txt_status);
        m_swBt = (Switch)findViewById(R.id.sw_bt_enabled);
        m_swBtTether = (Switch)findViewById(R.id.sw_bt_tether_enable);

        try {
            m_btControl = new BtControl(this, getApplicationContext());
        } catch (ClassNotFoundException e) {
            m_btControl = null;
            setBtOn(false);
            setTetheringOn(false);
            setLog(e.toString());
        }
        findViewById(R.id.btn_finish)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
        findViewById(R.id.btn_clear)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        m_tvLog.setText("");
                    }
                });
        findViewById(R.id.btn_update)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setBtStatus();
                    }
                });
        m_swBt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setBtOn(isChecked);
            }
        });
        m_swBtTether.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setTetheringOn(isChecked);
            }
        });
    }

    private String strONorOFF(boolean b) {
        if (b) {
            return "OFF -> ON";
        } else {
            return "ON -> OFF";
        }
    }

    private String strIsONorOFF(boolean b) {
        if (b) {
            return "ON";
        } else {
            return "OFF";
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        addLog("onWindowFocusChanged: " + hasFocus);
        if (hasFocus) {
            updateStatus();
        }
    }

    /**
     * update current status(bluetooth and Tethering)
     */
    private void updateStatus() {
        try {
            boolean b = m_btControl.isBtOn();
            setBtOn(b);
            addLog("Bluetooth: " + strIsONorOFF(m_btControl.isBtOn()));
            if (b) {
                setTetheringOn(m_btControl.isTetheringOn());
            } else {
                setTetheringOn(false);
            }
        } catch (Exception e) {
            addLog(e.toString());
            addLog(m_btControl.getLastError());
        }
    }

    /**
     * set log to TextView
     * @param log log string
     */
    private void setLog(String log) {
        m_tvLog.setText(log);
    }

    /**
     * add log to TextView
     * @param log log string
     */
    private void addLog(String log) {
        if (log.length() > 0) {
            m_tvLog.setText(log + "\n" + m_tvLog.getText());
        }
    }

    /**
     * set Bluetooth ON or OFF
     * @param bOn setting status
     */
    private void setBtOn(boolean bOn) {
        addLog("setBtOn: " + strONorOFF(bOn));
        if (!bOn) {
            setTetheringOn(false);
        }
        if (m_btControl.isBtOn() != bOn) {
            if (m_btControl.setBtOn(bOn)) {
                m_swBtTether.setChecked(false);
            } else {
                bOn = false;
            }
            m_swBt.setChecked(bOn);
        }
        m_swBt.setChecked(bOn);
        m_swBtTether.setEnabled(m_btControl.isBtOn());
    }

    /**
     * set Tethering ON or OFF
     * @param bOn setting status
     */
    private void setTetheringOn(boolean bOn) {
        addLog("setTetheringOn: " + strONorOFF(bOn));
        try {
            if (m_btControl.isTetheringOn() == bOn) {
                m_swBtTether.setChecked(bOn);
            } else {
                m_swBtTether.setChecked(m_btControl.setTetheringOn(bOn));
                addLog(m_btControl.getLastError());
            }
            m_swBtTether.setEnabled(m_btControl.isBtOn());
        } catch (InvocationTargetException e) {
            addLog(e.toString());
        }
    }

    @Override
    public void OnConnected(boolean b) {
        m_swBtTether.setEnabled(b);
        if (b) {
            addLog("Bluetooth Enabled");
        } else {
            addLog("Bluetooth Disabled");
            setTetheringOn(false);
        }
    }
}
