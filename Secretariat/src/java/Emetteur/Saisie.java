package Emetteur;

import java.io.*;
public class Saisie {

	public static int lectureEntier() {
		int nombrelu = 0;

		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(isr);

		try {
			String tampon = br.readLine();
			nombrelu = Integer.parseInt(tampon);
		} catch (IOException e) {
			System.out.println("Exception entree/sortie : " + e.getMessage());
		}
		return nombrelu;
	}

	public static String lectureChaine() {
		String chaine = "";

		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(isr);

		try {
			chaine = br.readLine();
		} catch (IOException e) {

			System.out.println("Exception entree/sortie : " + e.getMessage());
		}
		return chaine;
	}

	public static double lectureReel() {
		double wreel = 0;

		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(isr);

		try {

			String tampon = br.readLine();
			wreel = Double.valueOf(tampon).doubleValue();

		} catch (IOException e) {
			System.out.println("Exception entree/sortie : " + e.getMessage());
		}
		return wreel;
	}

}
