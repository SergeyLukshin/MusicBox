package ru.adoon.mymusic.Classes;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Лукшин on 02.08.2017.
 */

public class RadioTrackInfo {
    public String m_strTitleTrack = "";
    public String m_strTitleExecutor = "";
    public int m_lastTime = 0;
    public Bitmap m_btm = null;
    public Bitmap m_btmIconRadio = null;
    public boolean m_bReadIconRadio = false;

    public RadioTrackInfo() {
        Clear();
    }

    public void Clear() {
        m_strTitleTrack = "";
        m_strTitleExecutor = "";
        m_lastTime = 0;
        m_btm = null;
    }

    public void GetTrackInfo(int radioid) {
        Clear();

        try {
            // publishProgress(myProgress);
            // http://101.ru/api/channel/getServers/200/channel/MP3/?dataFormat=json
            // http://101.ru/foot-radio-search/search/moscow
            // http://101.ru/api/channel/getTrackOnAir/569452/personal/?dataFormat=json
            URL url = new URL("http://101.ru/api/channel/getTrackOnAir/" + String.valueOf(radioid) + "/channel/?dataFormat=json");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000);
            urlConnection.connect();

            //BufferedReader reader= new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String strJson = "", str;
            // titleTrack, titleExecutor, duration, lastTime
            while ((str = reader.readLine()) != null) {
                strJson += str;
            }

            JSONObject dataJsonObj = null;
            String secondName = "";

            try {
                dataJsonObj = new JSONObject(strJson);
                int status = dataJsonObj.getInt("status");
                if (status == 1) {
                    JSONObject dataResult = dataJsonObj.getJSONObject("result");

                    JSONObject dataShort = dataResult.getJSONObject("short");
                    JSONObject dataStat = dataResult.getJSONObject("stat");

                    // title_executor

                    m_strTitleTrack = dataShort.getString("titleTrack");
                    m_strTitleExecutor = dataShort.getString("titleExecutor");
                    //mi.duration = Integer.valueOf(dataShort.getString("duration"));
                    m_lastTime = dataStat.getInt("lastTime");
                    if (m_lastTime >= -10) {
                        try {
                            JSONObject dataCover = dataShort.getJSONObject("cover");
                            String coverHTTP = "";
                            try {
                                coverHTTP = dataCover.getString("cover400");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (coverHTTP == "")
                                coverHTTP = dataCover.getString("coverHTTP");

                            URL urlBitmap = new URL(coverHTTP);
                            HttpURLConnection connectionBitmap = (HttpURLConnection) urlBitmap.openConnection();
                            connectionBitmap.setDoInput(true);
                            connectionBitmap.connect();
                            InputStream input = connectionBitmap.getInputStream();

                            Bitmap b = BitmapFactory.decodeStream(input);

                            int width = b.getWidth();
                            int height = b.getHeight();

                            if (width > 512 || height > 512) {

                                float aspectRatio = width / (float) height;
                                int new_width = 512;
                                int new_height = Math.round(new_width / aspectRatio);

                                m_btm = Bitmap.createScaledBitmap(b, new_width, new_height, false);
                            }
                            else {
                                m_btm = b;
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    else
                        Clear();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean SaveImage(Bitmap imageData, String filename, Context ctx) {
        //get path to external storage (SD card)

        String folder = "";
        String sdState = android.os.Environment.getExternalStorageState(); //Получаем состояние SD карты (подключена она или нет) - возвращается true и false соответственно
        if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
            folder = ctx.getCacheDir().toString();
        }
        else {
            folder = ctx.getExternalCacheDir().toString();
        }

        try {
            String filePath = folder + "/" + filename;
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);

            BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);

            //choose another format if PNG doesn't suit you
            imageData.compress(Bitmap.CompressFormat.PNG, 100, bos);

            bos.flush();
            bos.close();

        } catch (FileNotFoundException e) {
            Log.w("TAG", "Error saving image file: " + e.getMessage());
            return false;
        } catch (IOException e) {
            Log.w("TAG", "Error saving image file: " + e.getMessage());
            return false;
        }

        return true;
    }

    public Bitmap LoadImage(String filename, Context ctx) {
        //get path to external storage (SD card)

        Bitmap bmp = null;
        String folder = "";
        String sdState = android.os.Environment.getExternalStorageState(); //Получаем состояние SD карты (подключена она или нет) - возвращается true и false соответственно
        if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
            folder = ctx.getCacheDir().toString();
        }
        else {
            folder = ctx.getExternalCacheDir().toString();
        }

        try {
            String filePath = folder + "/" + filename;
            bmp = BitmapFactory.decodeFile(filePath);

        } catch (Exception e) {
            return null;
        }

        return bmp;
    }

    public void GetRadioInfoFromFile(int radioid, Context ctx) {
        m_btmIconRadio = LoadImage("channel_icon_" + String.valueOf(radioid) + ".png", ctx);
        if (m_btmIconRadio != null) m_bReadIconRadio = true;
    }

    public void GetRadioInfo(String strName, int radioid, Context ctx) {

        try {
            if (!m_bReadIconRadio) {

                m_btmIconRadio = LoadImage("channel_icon_" + String.valueOf(radioid) + ".png", ctx);

                if (m_btmIconRadio == null) {

                    Connection con = Jsoup.connect("http://101.ru/radio/channel/" + String.valueOf(radioid));
                    con.followRedirects(false);
                    Document doc = con.get();
                    Elements aElems = doc.select("img.logo");
                    for (Element aElem : aElems) {
                        String src = aElem.attr("src");

                        URL urlBitmap = new URL(src);
                        HttpURLConnection connectionBitmap = (HttpURLConnection) urlBitmap.openConnection();
                        connectionBitmap.setDoInput(true);
                        connectionBitmap.connect();
                        InputStream input = connectionBitmap.getInputStream();
                        m_btmIconRadio = BitmapFactory.decodeStream(input);
                        // сохраняем картинку на диск
                        SaveImage(m_btmIconRadio, "channel_icon_" + String.valueOf(radioid) + ".png", ctx);
                        break;
                    }

                    /*String strModifyName = strName;
                    strModifyName = strModifyName.replaceAll("[а-яА-ЯёЁ\\s]+", "");

                    Connection con = Jsoup.connect("http://101.ru/foot-radio-search/search/" + strModifyName);
                    con.followRedirects(false);
                    Document doc = con.get();
                    Elements aElems = doc.select("a.noajax");
                    for (Element aElem : aElems) {
                        String href = aElem.attr("href");
                        if (href.indexOf("/radio/channel/" + String.valueOf(radioid)) >= 0) {
                            Elements imgElems = aElem.select("img");
                            for (Element imgElem : imgElems) {
                                String src = imgElem.attr("src");

                                URL urlBitmap = new URL(src);
                                HttpURLConnection connectionBitmap = (HttpURLConnection) urlBitmap.openConnection();
                                connectionBitmap.setDoInput(true);
                                connectionBitmap.connect();
                                InputStream input = connectionBitmap.getInputStream();
                                m_btmIconRadio = BitmapFactory.decodeStream(input);
                                // сохраняем картинку на диск
                                SaveImage(m_btmIconRadio, "channel_icon_" + String.valueOf(radioid) + ".png", ctx);
                                break;
                            }
                            break;
                        }
                    }*/
                }

                m_bReadIconRadio = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
