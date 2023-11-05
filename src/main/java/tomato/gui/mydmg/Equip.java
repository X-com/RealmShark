package tomato.gui.mydmg;

import org.xml.sax.SAXException;
import util.StringXML;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Pet class
 */
public class Equip {

    private static final String PETS_XML_PATH = "assets/xml/equip.xml";
    public static HashMap<Integer, Weapon> weapons = new HashMap<>();

    static {
        load();
    }

    public static void load() {
        try {
            FileInputStream file = new FileInputStream(PETS_XML_PATH);
            String result = new BufferedReader(new InputStreamReader(file)).lines().collect(Collectors.joining("\n"));
            StringXML base = StringXML.getParsedXML(result);

            for (StringXML weaponList : base.children) {
                if (Objects.equals(weaponList.name, "Object")) {
                    Weapon w = new Weapon();

                    for (StringXML weaponInfo : weaponList.children) {
                        if (Objects.equals(weaponInfo.name, "type")) {
                            w.id = Integer.parseInt(weaponInfo.value.substring(2), 16);
                        } else if (Objects.equals(weaponInfo.name, "id")) {
                            w.name = weaponInfo.value;
                        } else if (Objects.equals(weaponInfo.name, "DisplayId")) {
                            w.displayName = weaponInfo.children.get(0).value;
                        } else if (Objects.equals(weaponInfo.name, "Labels")) {
                            w.labels = weaponInfo.children.get(0).value;
                        } else if (Objects.equals(weaponInfo.name, "RateOfFire")) {
                            w.rof = Float.parseFloat(weaponInfo.children.get(0).value);
                        } else if (Objects.equals(weaponInfo.name, "NumProjectiles")) {
                            w.numProj = Integer.parseInt(weaponInfo.children.get(0).value);
                        } else if (Objects.equals(weaponInfo.name, "Subattack")) {
                            Bullet b = new Bullet();
                            for (StringXML suba : weaponInfo.children) {
                                if (Objects.equals(suba.name, "projectileId")) {
                                    b.id = Integer.parseInt(suba.value);
                                } else if (Objects.equals(suba.name, "RateOfFire")) {
                                    b.rof = Float.parseFloat(suba.children.get(0).value);
                                } else if (Objects.equals(suba.name, "NumProjectiles")) {
                                    b.numProj = Integer.parseInt(suba.children.get(0).value);
                                }
                            }
                            w.bullets.add(b);
                        } else if (Objects.equals(weaponInfo.name, "Projectile")) {
                            Projectile p = new Projectile();
                            for (StringXML proj : weaponInfo.children) {
                                if (Objects.equals(proj.name, "id")) {
                                    p.id = Integer.parseInt(proj.value);
                                } else if (Objects.equals(proj.name, "MinDamage")) {
                                    String value = proj.children.get(0).value.replaceAll("\t", "");
                                    p.min = Integer.parseInt(value);
                                } else if (Objects.equals(proj.name, "MaxDamage")) {
                                    p.max = Integer.parseInt(proj.children.get(0).value);
                                }
                            }
                            w.projectiles.put(p.id, p);
                        } else if (Objects.equals(weaponInfo.name, "Texture")) {
                            for (StringXML tex : weaponInfo.children) {
                                if (Objects.equals(tex.name, "Index")) {
                                    String value = tex.children.get(0).value;
                                    if (value.startsWith("0x")) {
                                        w.imgIndex = Integer.parseInt(value.substring(2), 16);
                                    } else {
                                        w.imgIndex = Integer.parseInt(value);
                                    }
                                } else if (Objects.equals(tex.name, "File")) {
                                    w.imgFile = tex.children.get(0).value;
                                }
                            }
                        }
                    }
                    w.fix();
                    weapons.put(w.id, w);
                }
            }
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    public static Weapon get(int slot) {
        return weapons.get(slot);
    }
}
