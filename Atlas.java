package Robos;
import robocode.*;
/**
 * Autores: Rodrigo Luiz Antão de Andrade Santos - 01466505
 * 			 Lucas Gabriel -
 * 			 Diego Francisco -	
 * 
 * Nome da Equipe: Olimpo
 * Nome do Robô: Atlas
 */
public class Atlas extends AdvancedRobot {


    // Funções de pertinência para a distância do oponente
    public static double close(double distance) {
        return Math.max(0, Math.min(1, (200 - distance) / 200));
    }

    public static double medium(double distance) {
        if (distance <= 100 || distance >= 400) {
            return 0;
        } else if (distance <= 200) {
            return (distance - 100) / 100;
        } else {
            return (400 - distance) / 200;
        }
    }

    public static double far(double distance) {
        return Math.max(0, Math.min(1, (distance - 200) / 200));
    }

    // Funções de pertinência para a potência do disparo
    public static double lowPower(double power) {
        return Math.max(0, Math.min(1, (2 - power) / 2));
    }

    public static double mediumPower(double power) {
        if (power <= 1 || power >= 3) {
            return 0;
        } else if (power <= 2) {
            return (power - 1) / 1;
        } else {
            return (3 - power) / 1;
        }
    }

    public static double highPower(double power) {
        return Math.max(0, Math.min(1, (power - 2) / 2));
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
        double numerator = (lowPower * 1) + (mediumPower * 2) + (highPower * 3);
        double denominator = lowPower + mediumPower + highPower;

        return denominator == 0 ? 1 : numerator / denominator;
    }

    @Override
    public void run() {
        // Inicialização do robô
        setAdjustGunForRobotTurn(true); // Permite que o canhão gire independentemente do robô

        while (true) {
            turnRadarRight(360); // Continuamente escaneia o ambiente
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        double distance = event.getDistance(); // Distância do oponente
        double gunPower = inferGunPower(distance); // Determina a potência do disparo

        // Mirar e disparar
        turnGunRight(getHeading() - getGunHeading() + event.getBearing());
        fire(gunPower);
    }
}


