import ea.*;

/**
 * Eine einfache Demonstration der Engine-Physik durch eine Ball-Wurf-Simulation. Es wird ein Ball (durch Wirkung
 * eines Impulses) geworfen.
 *
 * <h3>Nutzung der Simulation</h3>
 *
 * <p>Die Simulation kann gesteuert werden durch:</p>
 * <ul>
 *     <li>S-Taste: Startet Simulation</li>
 *     <li>R-Taste: Setzt Simulation zurück</li>
 *     <li>Die Tasten Z und U ändern den Zoom auf die Umgebung (rudimentär implementiert)</li>
 *     <li>D-Taste: Toggelt den Debug-Modus (zeigt das Pixel-Raster)</li>
 * </ul>
 *
 * <h3>Anpassung der Parameter</h3>
 *
 * <p>Die Simulation arbeitet mit einigen physikalischen Parametern, die sich ändern lassen. Folgende Parameter sind
 * als Konstanten im Code definiert und können im angepasst werden:</p>
 * <ul>
 *     <li><code>DURCHMESSER</code>: Der Durchmesser des Kreises (hat keinen Einfluss auf die Masse.</li>
 *     <li><code>HOEHE_UEBER_BODEN</code>: Abstand zwischen dem untersten Punkt des Balls und dem Boden</li>
 *     <li><code>MASSE</code>: Masse des Balls</li>
 *     <li><code>IMPULS: Impuls, der auf den Ball angewandt wird.</code></li>
 *     <li><code>WINKEL</code>: Winkel, in dem der Impuls auf den Ball angewandt wird.
 *          0° = parallel zum Boden, 90° = gerade nach oben</li>
 * </ul>
 * Created by Michael on 11.04.2017.
 */
public class BallThrow
        extends Game
implements KollisionsReagierbar {

    public static void main(String[] args) {
        new BallThrow();
    }

    /**
     * Der Kreis. Auf ihn wird ein Impuls gewirkt.
     */
    private Kreis ball;

    /**
     * Der Boden.
     */
    private Rechteck boden;

    /**
     * Der Startzeitpunkt der Simulation. Für Zeitmessung
     */
    private long startzeit;

    /**
     * Die Konstanten für die Umsetzung der Simulation
     * Einheiten sind:
     * Distanz : Meter
     * Masse : KG
     * Impuls : Ns
     * Winkel : Grad (nicht Bogenmaß)
     */
    private static final float DURCHMESSER=0.2f, HOEHE_UEBER_BODEN=1f, MASSE=1f, IMPULS=10, WINKEL=60;

    /**
     * Die PPM-Berechnungskonstante
     */
    private static final int PIXELPROMETER=100;

    private static final float BODEN_TIEFE = 700, ABSTAND_LINKS=50;

    public BallThrow() {
        super(1200, 720, "EA Ballwurf-Simulation");
    }

    @Override
    public void initialisieren() {
        //Zuallererst die Größenordnung klar machen (muss vor Erstellung der Objekte geschehen)
        this.ppmSetzen(PIXELPROMETER);

        //Den Ball erstellen
        ball = new Kreis(0,0, DURCHMESSER*PIXELPROMETER);
        wurzel.add(ball);
        ball.farbeSetzen("Rot");
        ball.physik.typ(Physik.Typ.DYNAMISCH);
        ball.physik.masse(MASSE);
        ball.position.mittelpunktSetzen(ABSTAND_LINKS,
                BODEN_TIEFE-(HOEHE_UEBER_BODEN*PIXELPROMETER + 0.5f*DURCHMESSER*PIXELPROMETER));

        //Den Boden erstellen
        boden=new Rechteck(0,BODEN_TIEFE, 20000,20);
        wurzel.add(boden);
        boden.farbeSetzen("Weiss");
        boden.physik.typ(Physik.Typ.STATISCH);

        //Kollision zwischen Ball und Boden beobachten (Code ist uns egal, wir kennen nur einen Kollisionsfall)
        anmelden.kollisionsReagierbar(this, ball, boden);
    }

    /**
     * Wird bei jedem Tastendruck ausgeführt.
     * @param code Der eindeutige Code der gedrückten Taste.
     */
    @Override
    public void tasteReagieren(int code) {
        switch(code) {
            case Taste.S: //Starte die Simulation
                simulationStarten();
                break;
            case Taste.D: //Toggle den Debug Modus
                EngineAlpha.setDebug(!EngineAlpha.isDebug());
                break;
            case Taste.Z: //Zoom Out
                kamera.zoomSetzen(kamera.getZoom()-0.1f);
                break;
            case Taste.U: //Zoom In
                kamera.zoomSetzen(kamera.getZoom()+0.1f);
                break;
            case Taste.R: //Reset
                simulationZuruecksetzen();
                break;
        }
    }

    /**
     * Startet die Simulation, indem ein Impuls auf den Ball gewirkt wird. Ab diesem Moment beginnt die Zeitmessung
     */
    private void simulationStarten() {
        //Zeitmessung beginnen = Startzeit erheben
        startzeit=System.currentTimeMillis();

        //Schwerkraft auf den Ball wirken lassen
        ball.physik.schwerkraft(new Vektor(0, 9.81f));

        //Impuls berechnen und auf den Ball wirken lassen
        Vektor impuls = new Vektor((float)Math.cos(Math.toRadians(WINKEL))*IMPULS,
                (float)-Math.sin(Math.toRadians(WINKEL))*IMPULS);
        ball.physik.impulsWirken(impuls);
    }

    /**
     * Setzt die Simulation zurück. Die Schwerkraft auf den Ball wird deaktiviert, die Position des Balls wird
     * zurückgesetzt und der Ball wird in Ruhe versetzt.
     */
    private void simulationZuruecksetzen() {
        ball.physik.schwerkraft(new Vektor(0,0)); //Schwerkraft deaktivieren
        ball.position.mittelpunktSetzen(ABSTAND_LINKS, //Ballposition zurücksetzen
                BODEN_TIEFE-(HOEHE_UEBER_BODEN*PIXELPROMETER + 0.5f*DURCHMESSER*PIXELPROMETER));
        ball.physik.inRuheVersetzen(); //Ball in Ruhe versetzen
    }

    /**
     * Wird bei jeder Kollision zwischen <b>mit diesem Interface angemeldeten</b> <code>Raum</code>-Objekten
     * aufgerufen.
     * @param colliding  Das Kollidierende Objekt
     */
    @Override
    public void kollision(Raum colliding) {
        //Kollision bedeutet, dass der Ball auf den Boden gefallen ist => Zeitmessung abschließen
        long endzeit = System.currentTimeMillis();
        long zeitdifferenz = endzeit-startzeit;

        //Zurückgelegte Distanz seit Simulationsstart ausmessen (Pixel-Differenz ausrechnen und auf Meter umrechnen)
        float distanz = (ball.position.mittelPunkt().realX()-ABSTAND_LINKS) / PIXELPROMETER;

        //Messungen angeben
        System.out.println("Der Ball ist auf dem Boden aufgeschlagen. Seit Simulationsstart sind " +
         + (zeitdifferenz/1000) + " Sekunden und " + (zeitdifferenz%1000) + " Millisekunden vergangen.\n" +
                "Der Ball diese Distanz zurückgelegt: " + distanz + " m");
    }
}
