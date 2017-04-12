import ea.*;

/**
 * Eine kleine Sandbox, in der man ein paar Grundfunktionen der EA-Physik (4.0) ausprobieren kann.
 *
 * <h3>Nutzung der Simulation</h3>
 * <p>Die Simulation wird mit der Maus beeinflusst. Klicken setzt einen Angriffspunkt. Ein weiteres Klicken wirkt
 * an dem Angriffspunkt einen Impuls. Stärke und Richtung hängen von der Position der Maus relativ zum ersten Punkt
 * ab. Der entsprechende Vektor ist sichtbar.</p>
 * <h3>Funktionen</h3>
 * <ul>
 *     <li>R Setzt die gesamte Simulation zurück. Alle Objekte verharren wieder in Ruhe an ihrer Ausgangsposition.</li>
 *     <li>S Aktiviert/Deaktiviert Schwerkraft in der Simulation.</li>
 *     <li>E Aktiviert/Deaktiviert Wände</li>
 *     <li>D Aktiviert/Deaktiviert den Debug-Modus (und stellt damit ein Raster, FPS etc. dar)</li>
 *     <li>I Aktiviert/Deaktiviert die Info-Box mit Infos zu den physikalischen Eigenschaften des zuletzt
 *     angeklickten Objekts.</li>
 *     <li>U und J erhöhen/reduzieren die Masse des zuöetzt angeklickten Objekts.</li>
 *     <li>W und Q erhöhen/reduzieren die Elastizität der Wände.</li>
 *     <li>1 und 2 zoomen rein/raus</li>
 * </ul>
 *
 * Created by andonie on 05.09.15.
 */
public class PhysicsSandbox
extends Game
implements KlickReagierbar {

    /**
     * Textbox für Infos
     */
    private static class InfoBox
    extends Knoten {
        private Rechteck box; //Hintergrund

        private Text[] texte; //Die Texte

        public InfoBox() {
            super();
            box = new Rechteck(0,0, 150, 100);
            add(box);
            box.setColor(new Farbe(200, 200, 200, 100));

            texte = new Text[5];
            for(int i = 0; i < texte.length; i++) {
                texte[i] = new Text("Text1", i==0? 30 : 5, 5 + i*20);
                texte[i].groesseSetzen(14);
                add(texte[i]);
                texte[i].farbeSetzen("rot");
            }
            texte[4].groesseSetzen(10);
        }

        public void setTexts(String... texts) {
            for(int i = 0; i < texts.length && i < this.texte.length; i++) {
                texte[i].inhaltSetzen(texts[i]);
            }
        }
    }

    /**
     * Wird für die Schwerkraft-Berechnung genutzt
     */
    private static final Vektor ERDBESCHLEUNIGUNG = new Vektor(0, 9.81f);

    /**
     * Beschreiben die Maße des "Spielfelds"
     */
    public static final int FIELD_WIDTH = 612, FIELD_DEPTH = 400;

    /**
     * Beschreibt die Anzahl an Test-Objekten im Spiel
     */
    private static final int NUMER_OF_TESTOBJECTS = 4;

    /**
     * Main-Methode startet die Sandbox.
     * @param args  Comman-Line-Arguments. Nicht relevant.
     */
    public static void main(String[] args) {
        //System.setProperty("sun.java2d.trace","log,timestamp,count,out:j2dlog.txt,verbose");
        new PhysicsSandbox();
        EngineAlpha.setVerbose(true);
    }

    /**
     * Die Startpunkte für die Test-Objekte
     */
    private static final Punkt[] STARTINGPOINTS = new Punkt[] {
            new Punkt(260, 250),
            new Punkt(50, 60),
            new Punkt(400, 100),
            new Punkt(50, 200)
    };

    /**
     * Beschreibt die Zustände, in denen sich die Sandbox im Bezug auf Mausklick-Funktion befinden kann.
     */
    private enum KlickMode {
        ATTACK_POINT, DIRECTION_INTENSITY;
    }

    /**
     * Beschreibt, ob das Test-Objekt mit dem jeweiligen Index gerade im Angriffspunkt liegt.
     */
    private boolean[] isInAttackRange = new boolean[NUMER_OF_TESTOBJECTS];

    /**
     * Der Index des zuletzt angeklickten Test-Objekts
     */
    private int lastAttackTarget = 0;

    private Raum[] testObjects = new Raum[NUMER_OF_TESTOBJECTS];
    private Raum ground;
    private Raum attack;
    private Geometrie[] walls = new Geometrie[NUMER_OF_TESTOBJECTS];

    private Knoten fixierungsGruppe;

    private Rechteck stange;
    private InfoBox  box;

    private KlickMode klickMode = KlickMode.ATTACK_POINT;
    private Punkt lastAttack;
    private boolean hatSchwerkraft = false;

    /**
     * Startet ein Sandbox-Fenster.
     */
    public PhysicsSandbox() {
        super(612, 450 , "Physics Sandbox", true, true);
        //ppmSetzen(30);
    }

    /**
     * In dieser Methode wird die gesamte Sandbox initialisiert.
     */
    @Override
    public void initialisieren() {

        //Info-Message
        //fenster.nachrichtSchicken("Elastizität +[W]/-[Q] | Masse +[U] / -[J] | [R]eset | [S]chwerkraft | [E]insperren");

        //Test-Objekte
        Rechteck rechteck = new Rechteck(10, 10, 100, 60);
        wurzel.add(rechteck);
        rechteck.setColor("Gelb");
        rechteck.physik.typ(Physik.Typ.DYNAMISCH);
        testObjects[0] = rechteck;

        fixierungsGruppe = new Knoten();
        wurzel.add(fixierungsGruppe);


        Kreis kreis = new Kreis(10, 10, 50);
        //wurzel.add(kreis);
        fixierungsGruppe.add(kreis);
        kreis.setColor("Lila");
        kreis.physik.typ(Physik.Typ.DYNAMISCH);
        testObjects[1] = kreis;

        Kreis kreis2 = new Kreis(0,0, 20);
        //wurzel.add(kreis2);
        fixierungsGruppe.add(kreis2);
        kreis2.setColor("gruen");
        kreis2.physik.typ(Physik.Typ.DYNAMISCH);
        //kreis2.physik.masse(50);
        testObjects[2] = kreis2;

        Polygon polygon = new Polygon(new Punkt(0,0), new Punkt(20, 30), new Punkt(10, 50),
                new Punkt(80, 10), new Punkt(120, 0));
        wurzel.add(polygon);
        //fixierungsGruppe.add(polygon);
        polygon.setColor("blau");
        polygon.physik.typ(Physik.Typ.DYNAMISCH);
        testObjects[3] = polygon;

        //Boden
        Rechteck boden = new Rechteck(0, FIELD_DEPTH, FIELD_WIDTH, 10);
        wurzel.add(boden);
        boden.setColor("Weiss");
        boden.physik.typ(Physik.Typ.STATISCH);
        ground = walls[0] = boden;

        //Der Rest der Wände
        Rechteck links = new Rechteck(0, 0, 10, FIELD_DEPTH);
        Rechteck rechts = new Rechteck(FIELD_WIDTH-10, 0, 10, FIELD_DEPTH);
        Rechteck oben = new Rechteck(0,0, FIELD_WIDTH, 10);
        wurzel.add(links, rechts, oben);
        walls[1] = links;
        walls[2] = rechts;
        walls[3] = oben;

        for(int i = 1; i <= 3; i++) {
            walls[i].setColor("weiss");
            walls[i].sichtbarSetzen(false);
            walls[i].physik.typ(Physik.Typ.PASSIV);
        }


        //Vektor-Visualisierung
        Rechteck stab = new Rechteck(0,0, 100, 5);
        wurzel.add(stab);
        stab.setColor(new Farbe(200, 50, 50));
        stange = stab;
        stange.zIndexSetzen(3);

        //Attack-Visualisierung
        Kreis atv = new Kreis(0,0, 10);
        wurzel.add(atv);
        atv.setColor("Rot");
        attack = atv;
        attack.zIndexSetzen(4);

        box = new InfoBox();
        wurzel.add(box);
        box.position.set(200, 30);


        //Test-Objekte zur Kollision Anmelden
        for(int i = 0; i < testObjects.length; i++) {
            final int key = i;
            KollisionsReagierbar kr = new KollisionsReagierbar() {
                @Override
                public void kollision(Raum colliding) {
                    isInAttackRange[lastAttackTarget = key] = true;
                }
                @Override
                public void kollisionBeendet(Raum collider) {
                    //Code = index d. Test-Objekts, das attack-range verlassen hat.
                    isInAttackRange[key] = false;
                }
            };
            anmelden.kollisionsReagierbar(kr, attack, testObjects[i]);
        }


        //Maus erstellen, Listener Anmelden.
        maus.standardCursorSetzen(Maus.TYP_FADENKREUZ);
        maus.klickReagierbarAnmelden(this);


        kamera.zoomSetzen(2);
        resetSituation();
    }

    /**
     * Setzt den Zustand der Sandbox zurück zur Ausgangsaufstellung.
     */
    private void resetSituation() {
        //Testobjekt zurücksetzen und in Ruhezustand bringen.
        for(int i = 0; i < testObjects.length; i++) {
            testObjects[i].physik.inRuheVersetzen();
            testObjects[i].position.set(STARTINGPOINTS[i]);
            testObjects[i].position.rotation(0);
        }


        //Attack zurücksetzen (falls gesetzt)
        attack.sichtbarSetzen(false);
        lastAttack = null;

        //Vektorstange zurücksetzen (falls aktiv)
        stange.sichtbarSetzen(false);
    }


    /**
     * Wird bei jedem Tastendruck aufgerufen.
     * @param code  Der Code der gedrückten Taste.
     */
    @Override
    public void tasteReagieren(int code) {
        switch(code) {
            case Taste.R: //RESET
                resetSituation();
                break;
            case Taste.S: //SCHWERKRAFT-TOGGLE
                hatSchwerkraft = !hatSchwerkraft;
                for(Raum testObject : testObjects) {
                    testObject.physik.schwerkraft(hatSchwerkraft ?
                            ERDBESCHLEUNIGUNG : Vektor.NULLVEKTOR);
                }
                //System.out.println("Schwerkraft: " + hatSchwerkraft + " - ");
                break;
            case Taste.E: //Toggle Environment
                boolean wasActive = walls[1].sichtbar();
                Physik.Typ newType = wasActive ? Physik.Typ.PASSIV : Physik.Typ.STATISCH;
                //System.out.println("Type = " + newType);
                for(int i = 1; i <= 3; i++) {
                    walls[i].sichtbarSetzen(!wasActive);
                    walls[i].physik.typ(newType);
                }
                break;
            case Taste.I: //Toggle Info Box
                box.sichtbarSetzen(!box.sichtbar());
                break;
            case Taste.D: //Toggle Debug
                EngineAlpha.setDebug(!EngineAlpha.isDebug());
                break;
            case Taste.U: //Increase Mass
                changeMass(10);
                break;
            case Taste.J: //Decrease Mass
                changeMass(-10);
                break;
            case Taste.W: //Elastizitaet der Wände erhöhen
                ground.physik.elastizitaet(ground.physik.elastizitaet() + 0.1f);
                System.out.println("Ela der Wand " + ground.physik.elastizitaet());
                break;
            case Taste.Q: //Elastizitaet der Wände erhöhen
                ground.physik.elastizitaet(ground.physik.elastizitaet() - 0.1f);
                System.out.println("Ela der Wand " + ground.physik.elastizitaet());
                break;
            case Taste._1: //Zoom Out
                kamera.zoomSetzen(kamera.getZoom()-0.1f);
                break;
            case Taste._2: //Zoom In
                kamera.zoomSetzen(kamera.getZoom()+0.1f);
                break;
            case Taste.B: //Toggle die Kreisfixierung
                if(fixierungsGruppe.isFixated()) {
                    fixierungsGruppe.freeAllElements();
                } else {
                    fixierungsGruppe.fixateAllElements();
                }
                break;
        }
    }

    /**
     * Ändert die Masse vom letzten Objekt, was im Attack-Punkt war/ist.
     * @param deltaM Die Masseänderung (positiv=mehr Masse, negativ=weniger Masse).
     */
    private void changeMass(int deltaM) {
        testObjects[lastAttackTarget].physik.masse(
                testObjects[lastAttackTarget].physik.masse()+deltaM);
    }

    /**
     * Wird bei jedem Mausklick aufgerufen.
     * @param p Punkt des Mausklicks auf der Zeichenebene.
     */
    @Override
    public void klickReagieren(Punkt p) {
        switch(klickMode) {
            case ATTACK_POINT:
                lastAttack = p;

                //Visualize Attack Point
                attack.position.set(p.verschobeneInstanz(new Vektor(-5, -5)));
                attack.sichtbarSetzen(true);

                //Prepare Vector Stick
                stange.sichtbarSetzen(true);
                stange.position.set(p);


                klickMode = KlickMode.DIRECTION_INTENSITY;
                break;
            case DIRECTION_INTENSITY:

                if(lastAttack==null) {
                    klickMode = KlickMode.ATTACK_POINT;
                    return;
                }

                attack.sichtbarSetzen(false);
                stange.sichtbarSetzen(false);
                Vektor distance = lastAttack.nach(p);

                for(int i = 0; i < testObjects.length; i++) {
                    if(isInAttackRange[i])
                        testObjects[i].physik.impulsWirken(distance.multiplizieren(10), lastAttack);
                }
                

                klickMode = KlickMode.ATTACK_POINT;

                break;
        }
    }

    /**
     * Wird jeden Frame des Spiels exakt einmal aufgerufen.
     * @param ts    Die Zeit in Sekunden, die seit dem letzten Frame-Update vergangen sind.
     */
    @Override
    public void frameUpdate(float ts) {
        //Visualisiere ggf. die Vektorstange
        if(klickMode == KlickMode.DIRECTION_INTENSITY) {
            Punkt pointer = maus.positionAufZeichenebene();
            if(pointer==null || lastAttack == null)
                return;
            stange.breiteSetzen(lastAttack.abstand(pointer));
            float rot = Vektor.RECHTS.winkelZwischen(lastAttack.nach(pointer));
            if(Float.isNaN(rot))
                return;
            if(pointer.y < lastAttack.y)
                rot = (float)( Math.PI*2 - rot);
            stange.position.rotation(rot);
        }

        //Update für die Textbox
        Vektor vel = testObjects[lastAttackTarget].physik.geschwindigkeit();
        String vx = Float.toString(vel.x), vy = Float.toString(vel.y);
        vx = vx.substring(0, vx.length() > 4 ? 4 : vx.length());
        vy = vy.substring(0, vy.length() > 4 ? 4 : vy.length());
        box.setTexts(
                "Objekt: " + (lastAttackTarget+1),
                "Masse: " + testObjects[lastAttackTarget].physik.masse(),
                "v: (" + vx + " | " + vy + ")",
                "Elastizität: " + testObjects[lastAttackTarget].physik.elastizitaet(),
                "Toggles: [D]ebug | [I]nfo Box");
    }
}
