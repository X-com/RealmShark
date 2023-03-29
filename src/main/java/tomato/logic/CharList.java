package tomato.logic;

import org.xml.sax.SAXException;
import util.StringXML;
import util.Util;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * Used for character data from realm servers and converting to useful data.
 */
public class CharList {

    /**
     * Requests character list data from realm servers using the current access token of the logged in user.
     *
     * @param accessToken Access token of the currently logged in user.
     * @return Char list data as XML string.
     */
    public static String getChartList(String accessToken) throws IOException {
        String encoded = URLEncoder.encode(accessToken, "UTF-8");
        String s1 = "https://www.realmofthemadgod.com/char/list?";
        String s2 = "do_login=true&accessToken=" + encoded + "&game_net=Unity&play_platform=Unity&game_net_user_id";

        URL obj = new URL(s1 + s2);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
        con.setDoOutput(true);
        con.setRequestMethod("POST");

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = s2.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // print result
//            System.out.println(response);
            return response.toString();
        } else {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine).append("\n");
            }
            in.close();
//            System.out.println(response);
        }
        return null;
    }

    /**
     * Converts XML data to array of Character data.
     *
     * @param r XML string to be parsed.
     * @return List of Character data parsed from the XML string.
     */
    public static ArrayList<Character> getCharList(String r) {
        StringXML base;
        ArrayList<Character> listChars = new ArrayList<>();

        if (r == null) {
            r = "";
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(Util.resourceFilePath("temp"), StandardCharsets.UTF_8));
                String line;
                while ((line = br.readLine()) != null) {
                    r += line;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        try {
            base = StringXML.getParsedXML(r);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
            return null;
        }

        for (StringXML xml : base) {
            if (Objects.equals(xml.name, "Char")) {
                Character character = new Character();
                for (StringXML info : xml) {
                    for (StringXML v : info) {
                        switch (info.name) {
                            case "ObjectType":
                                character.classNum = Integer.parseInt(v.value);
                                character.setClassString();
                                break;
                            case "Equipment":
                                character.equipment = Arrays.stream(v.value.split(",")).mapToInt(Integer::parseInt).toArray();
                                break;
                            case "EquipQS":
                                character.equipQS = v.value.split(",");
                                break;
                            case "Level":
                                character.level = Integer.parseInt(v.value);
                                break;
                            case "Texture":
                                character.skin = Integer.parseInt(v.value);
                                break;
                            case "CreationDate":
                                character.date = v.value;
                                break;
                            case "HasBackpack":
                                character.backpack = v.value.equals("1");
                                break;
                            case "Has3Quickslots":
                                character.qs3 = v.value.equals("1");
                                break;
                            case "HitPoints":
                                character.hp = Integer.parseInt(v.value);
                                break;
                            case "MagicPoints":
                                character.mp = Integer.parseInt(v.value);
                                break;
                            case "Attack":
                                character.atk = Integer.parseInt(v.value);
                                break;
                            case "Defense":
                                character.def = Integer.parseInt(v.value);
                                break;
                            case "Speed":
                                character.spd = Integer.parseInt(v.value);
                                break;
                            case "Dexterity":
                                character.dex = Integer.parseInt(v.value);
                                break;
                            case "HpRegen":
                                character.vit = Integer.parseInt(v.value);
                                break;
                            case "MpRegen":
                                character.wis = Integer.parseInt(v.value);
                                break;
                            case "Seasonal":
                                character.seasonal = v.value.equals("True");
                                break;
                            case "Exp":
                                character.exp = Long.parseLong(v.value);
                                break;
                            case "CurrentFame":
                                character.fame = Long.parseLong(v.value);
                        }
                    }
                }
                listChars.add(character);
            } else if (Objects.equals(xml.name, "PowerUpStats")) {
                for (StringXML info : xml) {
                    if (Objects.equals(info.name, "ClassStats")) {
                        int clazz = Integer.parseInt(info.children.get(0).value);
                        int[] exalts = Arrays.stream(info.children.get(1).value.split(",")).mapToInt(Integer::parseInt).toArray();
                        Character.exalts.put(clazz, exalts);
                    }
                }
            }
        }
        return listChars;
    }
}
