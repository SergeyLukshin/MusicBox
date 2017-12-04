package ru.adoon.mymusic.Classes;

public class FolderItem
{
    public FolderItem(String strName, String strURL)
    {
        m_strName = strName;
        m_strURL = strURL;
        m_bCheck = false;
    }
    public String m_strName;
    public String m_strURL;
    public boolean m_bCheck;
};