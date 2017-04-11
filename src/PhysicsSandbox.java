import ea.*;

/**
 * Eine kleine Sandbox, in der man ein paar Grundfunktionen der EA-Physik (4.0) ausprobieren kann.
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
            box.farbeSetzen(new Farbe(200, 200, 200, 100));

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

    private static final Vektor SCHWERKRAFT = new Vektor(0, 9.81f);
    public static final int FIELD_WIDTH = 612;
    public static final int FIELD_DEPTH = 400;

    public static void main(String[] args) {
        //System.setProperty("sun.java2d.trace","log,timestamp,count,out:j2dlog.txt,verbose");
        new PhysicsSandbox();
    }

    private static final Punkt[] STARTINGPOINTS = new Punkt[] {
            new Punkt(260, 250),
            new Punkt(50, 60),
            new Punkt(400, 100)
    };

    private static enum KlickMode {
        ATTACK_POINT, DIRECTION_INTENSITY;
    }

    private boolean[] isInAttackRange = new boolean[3];
    private int lastAttackTarget = 0;

    private Raum[] testObjects = new Raum[3];
    private Raum ground;
    private Raum attack;
    private Geometrie[] walls = new Geometrie[4];


    private Rechteck stange;
    private InfoBox  box;

    private KlickMode klickMode = KlickMode.ATTACK_POINT;
    private Punkt lastAttack;
    private boolean hatSchwerkraft = false;

    public PhysicsSandbox() {
        super(612, 450 , "Physics Sandbox", true, true);
        //ppmSetzen(30);
    }


    @Override
    public void tasteReagieren(int code) {
        switch(code) {
            case Taste.R: //RESET
                resetSituation();
                break;
            case Taste.S: //SCHWERKRAFT-TOGGLE
                hatSchwerkraft = !hatSchwerkraft;
                for(Raum testObject : testObjects) {
                    testObject.physik.schwerkraft(hatSchwerkraft ? SCHWERKRAFT : Vektor.NULLVEKTOR);
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
            case Taste.Z:
                System.out.println("Zoom: " + kamera.getZoom());
                break;
            case Taste.P:
                kamera.verschieben(new Vektor(50, 50));
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

    @Override
    public void initialisieren() {

        //Info-Message
        //fenster.nachrichtSchicken("Elastizität +[W]/-[Q] | Masse +[U] / -[J] | [R]eset | [S]chwerkraft | [E]insperren");

        //Test-Objekte
        Rechteck rechteck = new Rechteck(10, 10, 100, 60);
        wurzel.add(rechteck);
        rechteck.farbeSetzen("Gelb");
        rechteck.physik.typ(Physik.Typ.DYNAMISCH);
        testObjects[0] = rechteck;

        Kreis kreis = new Kreis(10, 10, 50);
        wurzel.add(kreis);
        kreis.farbeSetzen("Lila");
        kreis.physik.typ(Physik.Typ.DYNAMISCH);
        testObjects[1] = kreis;

        Kreis kreis2 = new Kreis(10,10, 20);
        wurzel.add(kreis2);
        kreis2.farbeSetzen("gruen");
        kreis2.physik.typ(Physik.Typ.DYNAMISCH);
        testObjects[2] = kreis2;

        //Boden
        Rechteck boden = new Rechteck(0, FIELD_DEPTH, FIELD_WIDTH, 10);
        wurzel.add(boden);
        boden.farbeSetzen("Weiss");
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
            walls[i].farbeSetzen("weiss");
            walls[i].sichtbarSetzen(false);
            walls[i].physik.typ(Physik.Typ.PASSIV);
        }


        //Vektor-Visualisierung
        Rechteck stab = new Rechteck(0,0, 100, 5);
        wurzel.add(stab);
        stab.farbeSetzen(new Farbe(200, 50, 50));
        stange = stab;

        //Attack-Visualisierung
        Kreis atv = new Kreis(0,0, 10);
        wurzel.add(atv);
        atv.farbeSetzen("Rot");
        attack = atv;

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
