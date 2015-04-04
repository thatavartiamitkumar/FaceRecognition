package edu.ua.fr;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class Recognizer {

	public void recognize(Scanner scanner, Trainer trainer) throws IOException {

		double[][] meanImage = trainer.getMeanImage();
		RealMatrix weightVectorMatrix = trainer.getWeightVectorMatrix();
		int rank = trainer.getRank();
		RealMatrix eigenFaces = trainer.getEigenFaces();

		double[][] imageVector = new double[Constants.VECTOR_MATRIX_ROWS][1];

		System.out.println("Enter the path of the image file:");
		String path = scanner.next();
		File file = new File(path);

		Scanner fileScanner = new Scanner(file);

		String header = "";

		if (fileScanner.hasNext("P2"))
			header += fileScanner.nextLine() + "\n";
		else {
			fileScanner.close();

			throw new IOException();
		}

		if (fileScanner.hasNext("#.*"))
			header += fileScanner.nextLine() + "\n";

		// Reading the number of columns
		int columnN = fileScanner.nextInt();
		header += columnN + " ";
		// Reading the number of rows
		int rowN = fileScanner.nextInt();
		header += rowN + "\n";
		// Reading the maximum pixel value
		int maxG = fileScanner.nextInt();
		header += maxG + "\n";

		for (int k = 0; k < rowN * columnN; k++) {
			int pixelValue = fileScanner.nextInt();
			imageVector[k][0] = pixelValue;
		}

		// Subtracting the mean image pixels
		double[][] normalizedImageVector = new double[Constants.VECTOR_MATRIX_ROWS][1];
		for (int i = 0; i < Constants.VECTOR_MATRIX_ROWS; i++) {
			normalizedImageVector[i][0] = imageVector[i][0] - meanImage[i][0];
		}

		RealMatrix normalizedImageMatrix = MatrixUtils
				.createRealMatrix(normalizedImageVector);

		// Calculating the weight vector
		RealMatrix weightMatrix = MatrixUtils.createRealMatrix(rank, 1);

		for (int i = 0; i < rank; i++) {
			RealMatrix eigenFaceTranspose = eigenFaces.getColumnMatrix(i)
					.transpose();
			double weight = eigenFaceTranspose.multiply(normalizedImageMatrix)
					.getEntry(0, 0);// Result is a single values i.e. 1 x 1
									// matrix
			weightMatrix.setEntry(i, 0, weight);
		}

		RealVector weightVector = weightMatrix.getColumnVector(0);

		// Calculating the Euclidean distance with other weight vectors
		double[] distance = new double[Constants.IMAGE_COUNT];
		for (int i = 0; i < Constants.IMAGE_COUNT; i++) {
			distance[i] = weightVector.getDistance(weightVectorMatrix
					.getColumnVector(i));
		}

		// Finding the index of the smallest number
		double leastDistance = distance[0];
		int index = 0;
		for (int i = 1; i < distance.length; i++) {
			if (leastDistance > distance[i]) {
				leastDistance = distance[i];
				index = i;
			}
		}
		System.out.println("Least Distance:" + leastDistance);

		// Identifying the image based on the euclidean distance
		if (leastDistance <= trainer.getThreshold())
			System.out
					.println("Image found ("
							+ index
							+ "): s"
							+ ((index + Constants.IMAGES_PER_SUBJECT) / Constants.IMAGES_PER_SUBJECT)
							+ "_" + ((index) % Constants.IMAGES_PER_SUBJECT));
		else
			System.out.println("Image Unknown");
	}
}
