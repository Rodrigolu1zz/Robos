package Robos;

import robocode.AdvancedRobot;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;
import java.awt.Color;

public class Atlas2 extends AdvancedRobot {

    private boolean movingForward = true;
    private static final int WALL_MARGIN = 60; // Distância mínima das paredes

    // Funções de pertinência para a distância do oponente
    public static double close(double distance) {
        return Math.max(0, Math.min(1, (200 - distance) / 200));
    }

    public static double medium(double distance) {
        if (distance <= 100 || distance >= 400) {
            return 0;
        } else if (distance <= 200) {
            return (distance - 100) / 100.0;
        } else {
            return (400 - distance) / 200.0;
        }
    }

    public static double far(double distance) {
        return Math.max(0, Math.min(1, (distance - 200) / 200.0));
    }

    // Inferência fuzzy para determinar a potência do disparo
    public static double inferGunPower(double distance) {
        // Avaliar pertinência das entradas
        double closeDist = close(distance);
        double mediumDist = medium(distance);
        double farDist = far(distance);

        // Aplicar regras fuzzy
        double lowPower = closeDist;
        double mediumPower = mediumDist;
        double highPower = farDist;

        // Defuzzificação pelo método do centroide
        double numerator = (lowPower * 0.1) + (mediumPower * 2.55) + (highPower * 5);
        double denominator = lowPower + mediumPower + highPower;

        return denominator == 0 ? 0.1 : numerator / denominator;
    }

    @Override
    public void run() {
        // Inicialização do robô
        setAdjustGunForRobotTurn(true); // Permite que o canhão gire independentemente do robô
        setColors(Color.orange,Color.blue,Color.white,Color.yellow,Color.cyan); // Define as cores do robô

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
              	 setTurnLeft(90);
				 setBack(200);
                movingForward = false;
            } else {
              	 setTurnLeft(90);
				 setAhead(200);
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

