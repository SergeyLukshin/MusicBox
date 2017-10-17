package ru.adoon.mymusic.Classes;

public class RadioItem
{
    public RadioItem(String strName, String strURL, int iRadioID)
    {
        m_strName = strName;
        m_strURL = strURL;
        m_bCheck = false;
        m_iRadioID  = iRadioID;
    }
    public String m_strName;
    public String m_strURL;
    public boolean m_bCheck;
    public int m_iRadioID;
};