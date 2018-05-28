package ea.showcase;

import ea.*;
import ea.Point;
import ea.actor.Rectangle;
import ea.collision.CollisionListener;
import ea.handle.Physics;
import ea.actor.*;
import ea.keyboard.KeyListener;
import ea.mouse.MouseButton;
import ea.keyboard.*;
import ea.mouse.MouseClickListener;

import java.awt.*;

/**
 * Eine kleine Sandbox, in der man ein paar Grundfunktionen der EA-Physik (4.0) ausprobieren kann.
 *
 * <h3>Nutzung der Simulation</h3>
 * <p>Die Simulation wird mit der Maus beeinflusst. Klicken setzt einen Angriffspunkt. Ein weiteres Klicken wirkt
 * an dem Angriffspunkt einen Impuls. Stärke und Richtung hängen von der Position der Maus relativ zum ersten Point
 * ab. Der entsprechende Vector ist sichtbar.</p>
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
        extends ShowcaseDemo
        implements CollisionListener, MouseClickListener, FrameUpdateListener {

    /**
     * Wird für die Schwerkraft-Berechnung genutzt
     */
    private static final Vector ERDBESCHLEUNIGUNG = new Vector(0, 9.81f);

    /**
     * Beschreiben die Maße des "Spielfelds"
     */
    private final int FIELD_WIDTH, FIELD_DEPTH;

    @Override
    public void onCollision(Actor colliding) {
        attackedLast = colliding;
    }

    @Override
    public void onCollisionEnd(Actor colliding) {
        if(attackedLast == colliding)
            attackedLast = null;
    }

    /**
     * Beschreibt die Zustände, in denen sich die Sandbox im Bezug auf Mausklick-Funktion befinden kann.
     */
    private enum KlickMode {
        ATTACK_POINT, DIRECTION_INTENSITY;
    }

    private Actor ground;
    private Actor attack;
    private Geometry[] walls = new Geometry[4];

    private Actor attackedLast = null;

    private Rectangle stange;
    private KlickMode klickMode = KlickMode.ATTACK_POINT;
    private Point lastAttack;
    private boolean hatSchwerkraft = false;

    public static int PPM=100;

    /**
     * Startet ein Sandbox-Fenster.
     */
    public ForceKlickEnvironment(Scene parent, int width, int height) {
        super(parent);
        FIELD_WIDTH=width;
        FIELD_DEPTH=height;
        getCamera().move(
                width/2,
                height/2
        );

        initialisieren();
    }

    /**
     *
     */
    public void initialisieren() {
        getWorldHandler().setPixelProMeter(PPM);
        //Info-Message
        //fenster.nachrichtSchicken("Elastizität +[W]/-[Q] | Masse +[U] / -[J] | [R]eset | [S]chwerkraft | [E]insperren");


        //Boden
        Rectangle boden = new Rectangle(FIELD_WIDTH, 10);
        boden.position.set(0, FIELD_DEPTH);
        add(boden);
        boden.setColor(Color.WHITE);
        boden.physics.setType(Physics.Type.STATIC);
        ground = walls[0] = boden;

        //Der Rest der Wände
        Rectangle links = new Rectangle( 10, FIELD_DEPTH);
        Rectangle rechts = new Rectangle(10, FIELD_DEPTH);
        rechts.position.set(FIELD_WIDTH-10, 0);
        Rectangle oben = new Rectangle(FIELD_WIDTH, 10);
        add(links, rechts, oben);
        walls[1] = links;
        walls[2] = rechts;
        walls[3] = oben;

        for(int i = 1; i <= 3; i++) {
            walls[i].setColor(Color.WHITE);
            walls[i].setVisible(false);
            walls[i].physics.setType(Physics.Type.PASSIVE);
        }


        //Vector-Visualisierung
        Rectangle stab = new Rectangle(100, 5);
        add(stab);
        stab.setColor(new Color(200, 50, 50));
        stange = stab;
        stange.setZIndex(-10);

        //Attack-Visualisierung
        Circle atv = new Circle(10);
        add(atv);
        atv.setColor(Color.RED);
        attack = atv;
        attack.setZIndex(-10);

        //Maus erstellen, Listener Anmelden.
        addMouseClickListener(this);
        addCollisionListener(this, attack);

        addFrameUpdateListener(this);
        addKeyListener(new KeyListener() {
            @Override
            public void onKeyDown(int code) {
                switch(code) {
                    case Key.E:
                        boolean wasActive = walls[1].isVisible();
                        Physics.Type newType = wasActive ? Physics.Type.PASSIVE :Physics.Type.STATIC;
                        for(int i = 0; i <= 3; i++) {
                            walls[i].setVisible(!wasActive);
                            walls[i].physics.setType(newType);
                        }
                        break;

                }
            }

            @Override
            public void onKeyUp(int i) {
                //DONT CARE
            }
        });
    }




    /**
     * Wird bei jedem Mausklick aufgerufen.
     * @param p Point des Mausklicks auf der Zeichenebene.
     */
    @Override
    public void onMouseDown(Point p, MouseButton mouseButton) {
        switch(klickMode) {
            case ATTACK_POINT:
                lastAttack = p;

                //Visualize Attack Point
                attack.position.set(p.verschobeneInstanz(new Vector(-5, -5)));
                attack.setVisible(true);

                //Prepare Vector Stick
                stange.setVisible(true);
                stange.position.set(p);


                klickMode = KlickMode.DIRECTION_INTENSITY;
                break;
            case DIRECTION_INTENSITY:

                if(lastAttack==null) {
                    klickMode = KlickMode.ATTACK_POINT;
                    return;
                }

                attack.setVisible(false);
                stange.setVisible(false);
                Vector distance = lastAttack.vectorFromThisTo(p);

                if(attackedLast != null && attackedLast.physics.getType() == Physics.Type.DYNAMIC) {
                    attackedLast.physics.applyImpulse(distance.multiply(1), lastAttack);
                    attackedLast = null;
                }


                klickMode = KlickMode.ATTACK_POINT;

                break;
        }
    }

    @Override
    public void onMouseUp(Point point, MouseButton mouseButton) {
        //Ignore
    }

    /**
     * Wird für jeden Frame der Scene exakt einmal aufgerufen.
     * @param ts    Die Zeit in Millisekunden, die seit dem letzten Frame-Update vergangen sind.
     */
    @Override
    public void onFrameUpdate(int ts) {
        //Visualisiere ggf. die Vectorstange
        if(klickMode == KlickMode.DIRECTION_INTENSITY) {
            Point pointer = getMousePosition();
            if(pointer==null || lastAttack == null)
                return;
            stange.setWidth(lastAttack.distanceTo(pointer));
            float rot = Vector.RECHTS.getAngle(lastAttack.vectorFromThisTo(pointer));
            if(Float.isNaN(rot))
                return;
            if(pointer.y < lastAttack.y)
                rot = (float)( Math.PI*2 - rot);
            stange.position.setRotation(rot);
        }
    }
}
