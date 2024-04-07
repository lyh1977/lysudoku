package org.ly.lysudoku.db;

import android.text.TextUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import org.ly.lysudoku.LysApplication;
import org.ly.lysudoku.R;
import org.ly.lysudoku.game.SudokuGame;
import org.ly.lysudoku.utils.LogUtil;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@XStreamAlias("OSDData")
public class OSDData {

    public static final String EASYFILE = "gs3_easy.xml";
    public static final String HARDFILE = "gs3_hard.xml";
    public static final String MEDIUMFILE = "gs3_medium.xml";
    public static final String VERYHARDFILE = "gs3_very_hard.xml";
    public static final String VERYHARDFILE2 = "gs1_very_hard.xml";
    public static final String VERYHARDFILE3 = "gs2_very_hard.xml";

    public static final String HELL = "sudocue_hell.xml";
    public static final String HELL2 = "sudocue_hell2.xml";
    @XStreamAlias("game")
    public class Game {
        @XStreamAsAttribute()
        String data;

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }

    public static ArrayList<String> LoadFromFile(String fileName) {
        try {

            InputStream is = LysApplication.getInstance().openAssFile(fileName);
            Reader r = new InputStreamReader(is, "UTF-8");
            ArrayList<String> rList = importXml2(r);

            return rList;
        } catch (IOException er) {
            LogUtil.e(er.getMessage());
            er.printStackTrace();
        } catch (Exception er) {
            LogUtil.e(er.getMessage());
            er.printStackTrace();
        }
        return null;
    }

    private static ArrayList<String> importXml2(Reader in) throws SudokuInvalidFormatException {
        ArrayList<String> rList = new ArrayList<>();
        BufferedReader inBR = new BufferedReader(in);
        try {
            String lins = inBR.readLine();
            do {
                if (TextUtils.isEmpty(lins) == false && lins.length() > 80) {
                    rList.addAll(Collections.singleton(lins));
                }
                lins = inBR.readLine();
            } while (TextUtils.isEmpty(lins) == false);
        } catch (IOException er) {
            LogUtil.e(er.getMessage());
        } catch (Exception er) {
            LogUtil.e(er.getMessage());
        }
        return rList;
    }

    private static ArrayList<String> importXml(Reader in) throws SudokuInvalidFormatException {
        ArrayList<String> rList = new ArrayList<>();
        BufferedReader inBR = new BufferedReader(in);
        /*
         * while((s=in.readLine())!=null){ Log.i(tag, "line: "+s); }
         */

        // parse xml
        XmlPullParserFactory factory;
        XmlPullParser xpp;
        try {
            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(false);
            xpp = factory.newPullParser();
            xpp.setInput(inBR);
            int eventType = xpp.getEventType();
            String rootTag;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    rootTag = xpp.getName();
                    if (rootTag.equals("OSDData")) {
                        String version = xpp.getAttributeValue(null, "version");
                        if (version == null) {
                            // no version provided, assume that it's version 1
                            ArrayList<String> game = importV1(xpp);
                            if (game.size() > 0) {
                                rList.addAll(game);
                            }
                        } else if (version.equals("2")) {
                            ArrayList<String> game = importV2(xpp);
                            if (game.size() > 0) {
                                rList.addAll(game);
                            }
                        } else {
                            //setError("Unknown version of data.");
                        }
                    } else {
                        // setError(mContext.getString(R.string.invalid_format));
                        return rList;
                    }
                }
                eventType = xpp.next();
            }
        } catch (XmlPullParserException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return rList;
    }

    private static ArrayList<String> importV2(XmlPullParser parser)
            throws XmlPullParserException, IOException, SudokuInvalidFormatException {
        ArrayList<String> game = new ArrayList<>();
        int eventType = parser.getEventType();
        String lastTag = "";
        SudokuImportParams importParams = new SudokuImportParams();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                lastTag = parser.getName();
                if (lastTag.equals("folder")) {
                    String name = parser.getAttributeValue(null, "name");
                    long created = parseLong(parser.getAttributeValue(null, "created"), System.currentTimeMillis());

                    //importFolder(name, created);
                } else if (lastTag.equals("game")) {
                    importParams.clear();
                    importParams.created = parseLong(parser.getAttributeValue(null, "created"), System.currentTimeMillis());
                    importParams.state = parseLong(parser.getAttributeValue(null, "state"), SudokuGame.GAME_STATE_NOT_STARTED);
                    importParams.time = parseLong(parser.getAttributeValue(null, "time"), 0);
                    importParams.lastPlayed = parseLong(parser.getAttributeValue(null, "last_played"), 0);
                    importParams.data = parser.getAttributeValue(null, "data");
                    importParams.note = parser.getAttributeValue(null, "note");
                    importParams.command_stack = parser.getAttributeValue(null, "command_stack");
                    if (TextUtils.isEmpty(importParams.data) == false && importParams.data.length() > 80)
                        game.add(importParams.data);
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                lastTag = "";
            } else if (eventType == XmlPullParser.TEXT) {
                if (lastTag.equals("name")) {
                }

            }
            eventType = parser.next();
        }
        return game;
    }

    private static long parseLong(String string, long defaultValue) {
        return string != null ? Long.parseLong(string) : defaultValue;
    }

    private static ArrayList<String> importV1(XmlPullParser parser)
            throws XmlPullParserException, IOException, SudokuInvalidFormatException {
        ArrayList<String> game = new ArrayList<>();
        int eventType = parser.getEventType();
        String lastTag = "";

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                lastTag = parser.getName();
                if (lastTag.equals("game")) {
                    //importGame(parser.getAttributeValue(null, "data"));
                    String gs = parser.getAttributeValue(null, "data");
                    if (TextUtils.isEmpty(gs) == false && gs.length() > 80)
                        game.add(gs);
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                lastTag = "";
            } else if (eventType == XmlPullParser.TEXT) {
                if (lastTag.equals("name")) {
                    //importFolder(parser.getText());
                    String gs = parser.getText();
                    if (TextUtils.isEmpty(gs) == false && gs.length() > 80)
                        game.add(parser.getText());
                }

            }
            eventType = parser.next();
        }
        return game;

    }

    public OSDData() {

    }

    private String name;
    private String level;

    //private Game[] game;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }
/*
    public Game[] getGame() {
        return game;
    }

    public void setGame(Game[] game) {
        this.game = game;
    }
*/
}
