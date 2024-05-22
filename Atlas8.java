package Robos;

import robocode.AdvancedRobot;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.ScannedRobotEvent;
import robocode.BulletMissedEvent;
import robocode.util.Utils;
import java.awt.Color;

public class Atlas8 extends AdvancedRobot {

    private boolean movingForward = true;
    private static final int WALL_MARGIN = 60; // Distância mínima das paredes
    private int missedShots = 0; // Contador de disparos errados

    // Determina a potência do disparo baseado na distância usando lógica difusa
    public static double inferGunPower(double distance) {
        if (distance < 130) {
            return 5.0; // Potência de disparo 5 para distância menor que 130
        } else if (distance > 600) {
            return 1.0; // Potência de disparo 1 para distância maior que 600
        } else if (distance <= 200) {
            return 4.0; // Potência de disparo 4 para distância até 200
        } else if (distance <= 300) {
            return 3.0; // Potência de disparo 3 para distância até 300
        } else {
            return 2.0; // Potência de disparo 2 para distância até 600
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

            // Definir a cor do disparo com base na potência
            if (gunPower == 1.0) {
                setBulletColor(Color.green);
            } else if (gunPower == 2.0) {
                setBulletColor(Color.blue);
            } else if (gunPower == 3.0) {
                setBulletColor(Color.yellow);
            } else if (gunPower == 4.0) {
                setBulletColor(Color.orange);
            } else if (gunPower == 5.0) {
                setBulletColor(Color.red);
            }

            // Mirar e disparar
            double gunTurn = getHeading() - getGunHeading() + event.getBearing();
            turnGunRight(Utils.normalRelativeAngleDegrees(gunTurn));
            fire(gunPower);

            // Evitar tiros
            if (distance < 200 && gunPower > 2) {
                if (movingForward) {
                    setBack(300);
                    setTurnLeft(90);
                    movingForward = false;
                // } else {
                //     setAhead(200);
                //     movingForward = true;
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
