package Robos;

import robocode.AdvancedRobot;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.ScannedRobotEvent;
import robocode.BulletMissedEvent;
import robocode.util.Utils;
import java.awt.Color;

public class Atlas6 extends AdvancedRobot {

    private boolean movingForward = true;
    private static final int WALL_MARGIN = 60; // Distância mínima das paredes
    private int missedShots = 0; // Contador de disparos errados

    // Determina a potência do disparo baseado na distância
    public static double inferGunPower(double distance) {
        if (distance > 200) {
            return 1.0; // Potência de disparo 1 para distância longa
        } else {
            return 5.0; // Potência de disparo 5 para distância curta
        }
    }

    @Override
    public void run() {
        // Inicialização do robô
        setAdjustGunForRobotTurn(true); // Permite que o canhão gire independentemente do robô
        setColors(Color.orange, Color.blue, Color.white, Color.yellow, Color.cyan); // Define as cores do robô

        // Inicializa movimento
        setAhead(40000);
        movingForward = true;

        while (true) {
            turnRadarRight(360); // Continuamente escaneia o ambiente
            avoidWalls(); // Evitar colisão com paredes
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        if (missedShots <= 5) {
            double distance = event.getDistance(); // Distância do oponente
            double gunPower = inferGunPower(distance); // Determina a potência do disparo

            // Mirar e disparar
            double gunTurn = getHeading() - getGunHeading() + event.getBearing();
            turnGunRight(Utils.normalRelativeAngleDegrees(gunTurn));
            fire(gunPower);

            // Evitar tiros
            if (distance < 200 && gunPower > 2) {
                if (movingForward) {
                    setBack(200);
                    movingForward = false;
                } else {
                    setAhead(200);
                    movingForward = true;
                }
            }
        }
    }

    @Override
    public void onBulletMissed(BulletMissedEvent event) {
        // Incrementar contador de tiros errados
        missedShots++;
        if (missedShots > 5) {
            // Movimentar 400 pixels para evitar ser um alvo fácil
            if (movingForward) {
                setBack(300);
                movingForward = false;
            } else {
                setAhead(300);
                movingForward = true;
            }
            missedShots = 0; // Resetar contador de tiros errados
        }
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        // Movimento evasivo ao ser atingido por um tiro
        if (movingForward) {
            setBack(100);
            movingForward = false;
        } else {
            setAhead(100);
            movingForward = true;
        }
    }

    @Override
    public void onHitRobot(HitRobotEvent event) {
        // Reagir à colisão com outro robô
        if (event.isMyFault()) {
            if (movingForward) {
                setBack(50);
                movingForward = false;
            } else {
                setAhead(50);
                movingForward = true;
            }
        }
    }

    // Método utilitário para normalizar o ângulo de disparo
    public double normalizeBearing(double angle) {
        while (angle > 180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }

    // Método para evitar colisão com paredes
    public void avoidWalls() {
        double x = getX();
        double y = getY();
        double heading = getHeading();

        // Verifica se o robô está muito perto das paredes e ajusta o movimento
        if (x < WALL_MARGIN || x > getBattleFieldWidth() - WALL_MARGIN ||
            y < WALL_MARGIN || y > getBattleFieldHeight() - WALL_MARGIN) {
            if (movingForward) {
                setBack(100);
                movingForward = false;
            } else {
                setAhead(100);
                movingForward = true;
            }
            // Gira para longe da parede
            turnRight(normalizeBearing(90 - heading));
        }
    }
}
