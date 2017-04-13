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
public class ForceKlickEnvironment
        extends Game
        implements KlickReagierbar, KollisionsReagierbar {

    /**
     * Wird für die Schwerkraft-Berechnung genutzt
     */
    private static final Vektor ERDBESCHLEUNIGUNG = new Vektor(0, 9.81f);

    /**
     * Beschreiben die Maße des "Spielfelds"
     */
    private final int FIELD_WIDTH, FIELD_DEPTH;

    @Override
    public void kollision(Raum colliding) {
        attackedLast = colliding;
    }

    @Override
    public void kollisionBeendet(Raum colliding) {
        if(attackedLast == colliding)
            attackedLast = null;
    }

    /**
     * Beschreibt die Zustände, in denen sich die Sandbox im Bezug auf Mausklick-Funktion befinden kann.
     */
    private enum KlickMode {
        ATTACK_POINT, DIRECTION_INTENSITY;
    }

    private Raum ground;
    private Raum attack;
    private Geometrie[] walls = new Geometrie[4];

    private Raum attackedLast = null;

    private Rechteck stange;
    private KlickMode klickMode = KlickMode.ATTACK_POINT;
    private Punkt lastAttack;
    private boolean hatSchwerkraft = false;

    public static int PPM=100;

    /**
     * Startet ein Sandbox-Fenster.
     */
    public ForceKlickEnvironment(int width, int height, String title, boolean fullscreen) {
        super(width, height, title, fullscreen, true);
        FIELD_WIDTH=width;
        FIELD_DEPTH=height;
    }

    /**
     * In dieser Methode wird die gesamte Sandbox initialisiert.
     */
    @Override
    public void initialisieren() {
        ppmSetzen(PPM);
        //Info-Message
        //fenster.nachrichtSchicken("Elastizität +[W]/-[Q] | Masse +[U] / -[J] | [R]eset | [S]chwerkraft | [E]insperren");


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

        //Maus erstellen, Listener Anmelden.
        maus.standardCursorSetzen(Maus.TYP_FADENKREUZ);
        maus.klickReagierbarAnmelden(this);

        anmelden.kollisionsReagierbar(this, attack);
    }

    @Override
    public void tasteReagieren(int code) {
        switch(code) {
            case Taste.D: // Toggle Debug
                EngineAlpha.setDebug(!EngineAlpha.isDebug());
                break;
            case Taste.E:
                boolean wasActive = walls[1].sichtbar();
                Physik.Typ newType = wasActive ? Physik.Typ.PASSIV : Physik.Typ.STATISCH;
                for(int i = 0; i <= 3; i++) {
                    walls[i].sichtbarSetzen(!wasActive);
                    walls[i].physik.typ(newType);
                }
                break;
        }
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

                if(attackedLast != null && attackedLast.physik.typ() == Physik.Typ.DYNAMISCH) {
                    attackedLast.physik.impulsWirken(distance.multiplizieren(1), lastAttack);
                    attackedLast = null;
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
    }
}
