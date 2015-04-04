package edu.ua.fr;

import java.io.IOException;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) {
		try {
			Scanner scanner = new Scanner(System.in);

			// Training the recognizer
			Trainer trainer = new Trainer();
			trainer.trainRecognizer(scanner);

			// Testing the recognizer
			if (null != trainer) {
				boolean test = true;
				while (test) {
					Recognizer recognizer = new Recognizer();
					recognizer.recognize(scanner, trainer);
					
//					RecognizerAccuracy recognizerAccuracy = new RecognizerAccuracy();
//					recognizerAccuracy.recognize(scanner, trainer);

					System.out.println("Would you like to test another?(Y/N):");
					String choice = scanner.next();
					if (!choice.equalsIgnoreCase("Y")) {
						test = false;
						System.out.println(" Exiting ..... Thank You !!!");
					}
				}
			}

		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}
}
