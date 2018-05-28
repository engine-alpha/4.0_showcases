package ea.showcase;

import ea.*;
import ea.Scene;
import ea.Vector;
import ea.actor.ActorGroup;
import ea.actor.Circle;
import ea.actor.Polygon;
import ea.actor.Rectangle;
import ea.handle.Physics;
import ea.keyboard.Key;
import ea.keyboard.KeyListener;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.dynamics.joints.*;
import java.awt.Color;

/**
 * Einfaches Programm zur Demonstration von Joints in der Engine
 * Created by Michael on 12.04.2017.
 */
public class JointDemo
extends ForceKlickEnvironment
implements KeyListener {

    private boolean schwerkraftActive=false;


    private Rectangle wippe;
    private Polygon  basis;


    private Rectangle[] kette;


    private Circle ball;

    /**
     * Erstellt das Demo-Objekt
     */
    public JointDemo(Scene parent, int width, int height) {
        super(parent, width, height);
        initialisieren2();
        addKeyListener(this);
    }

    public void initialisieren2() {
        wippeBauen().position.set(new Point(500, 500));

        ketteBauen(15).position.move(new Vector(500, 00));

        leashBauen().position.move(100,100);

        hoverHolderBauen();

        ball = new Circle(100);
        add(ball);

        ball.setColor(Color.BLUE);

        ball.position.set(new Point(300, 200));
        ball.physics.setType(Physics.Type.DYNAMIC);
    }

    private ActorGroup hoverHolderBauen() {
        ActorGroup knoten = new ActorGroup();
        add(knoten);

        final int FACT = 2;

        Polygon halter = new Polygon(
                new Point(0*FACT,50*FACT), new Point(25*FACT, 75*FACT),
                new Point(50*FACT, 75*FACT), new Point(75*FACT, 50*FACT),
                new Point(75*FACT, 100*FACT), new Point(0*FACT,100*FACT)
        );
        knoten.add(halter);
        halter.setColor(Color.CYAN);
        halter.physics.setType(Physics.Type.DYNAMIC);

        Rectangle item = new Rectangle(35*FACT, 20*FACT);
        item.position.set(30*FACT, 0);
        knoten.add(item);
        item.setColor(Color.red);
        item.physics.setType(Physics.Type.DYNAMIC);


        knoten.position.move(new Vector(160, 200));

        halter.physics.createDistanceJoint(item, halter.position.getCenter().asVector(),
                item.position.getCenter().asVector());


        return knoten;
    }

    private ActorGroup leashBauen() {
        ActorGroup knoten = new ActorGroup();
        add(knoten);

        Circle kx = new Circle(30);
        knoten.add(kx);
        kx.setColor(Color.BLUE);
        kx.physics.setType(Physics.Type.DYNAMIC);

        Circle ky = new Circle(50);
        ky.position.set(50, 0);
        knoten.add(ky);
        ky.setColor(Color.GREEN);
        ky.physics.setType(Physics.Type.DYNAMIC);

        knoten.position.move(500,500);


        kx.physics.createRopeJoint(ky,
                //kx.position.mittelPoint().alsVector(),
                //ky.position.mittelPoint().alsVector(), 4);
                new Vector(15,15),
                new Vector(25,25), 4);

        return knoten;
    }

    private ActorGroup wippeBauen() {
        ActorGroup bauwerk = new ActorGroup();
        add(bauwerk);

        basis = new Polygon(new Point(0, 100), new Point(100, 100), new Point(50, 0));
        bauwerk.add(basis);
        basis.physics.setType(Physics.Type.STATIC);
        basis.setColor(Color.WHITE);

        wippe = new Rectangle( 500, 40);
        bauwerk.add(wippe);
        wippe.physics.setType(Physics.Type.DYNAMIC);

        wippe.position.setCenter(50, 0);

        wippe.setColor(Color.GRAY);

        Vector verzug = new Vector(100,100);

        wippe.position.move(verzug);
        basis.position.move(verzug);

        wippe.physics.createRevoluteJoint(basis, new Vector(50, 0).add(verzug));

        return bauwerk;
    }

    private ActorGroup ketteBauen(int kettenlaenge) {
        ActorGroup ketteK = new ActorGroup();
        add(ketteK);

        kette = new Rectangle[kettenlaenge];
        for(int i = 0; i < kette.length; i++) {
            kette[i] = new Rectangle(50, 10);
            Vector posrel = new Vector(45*i,30);
            ketteK.add(kette[i]);
            kette[i].position.move(posrel);
            kette[i].setColor(Color.GREEN);

            kette[i].physics.setType(i == 0 ? Physics.Type.STATIC : Physics.Type.DYNAMIC);

            if(i != 0) {
                RevoluteJoint rj = kette[i-1].physics.createRevoluteJoint(kette[i], new Vector(0, 5).add(posrel));

            }
        }

        Circle gewicht = new Circle(100);
        ketteK.add(gewicht);
        gewicht.setColor(Color.WHITE);

        gewicht.physics.setType(Physics.Type.DYNAMIC);
        gewicht.physics.setMass(40);

        Vector vektor = new Vector(45*kette.length, 35);
        gewicht.position.setCenter(new Point(vektor.getRealX(), vektor.getRealY()));
        gewicht.physics.createRevoluteJoint(kette[kette.length-1], vektor);

        return ketteK;
    }

    @Override
    public void onKeyDown(int code) {
        switch(code){
            case  Key.S:
                schwerkraftActive = !schwerkraftActive;
                ball.physics.setGravity(schwerkraftActive ? new Vector(0, 10) : Vector.NULLVECTOR);
                break;
        }
    }

    @Override
    public void onKeyUp(int i) {

    }
}
