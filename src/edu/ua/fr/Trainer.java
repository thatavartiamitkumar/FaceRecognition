package edu.ua.fr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.stat.correlation.Covariance;

public class Trainer {

	private String header = "";
	private double[][] faceVectorMatrix;
	private double meanImage[][];
	private RealMatrix weightVectorMatrix;
	private RealMatrix eigenFaces;
	private int rank;
	private double threshold;

	public void trainRecognizer(Scanner scanner) throws IOException {
		// Prepare the training set by converting each image into a column
		// vector
		prepareTrainingSet();

		// Calculating the average vector (determining the common features
		// in all images)
		meanImage = new double[Constants.VECTOR_MATRIX_ROWS][Constants.IMAGE_COUNT];
		for (int i = 0; i < Constants.VECTOR_MATRIX_ROWS; i++) {
			double sum = 0;

			for (int j = 0; j < Constants.IMAGE_COUNT; j++) {
				sum += faceVectorMatrix[i][j];
			}

			for (int k = 0; k < Constants.IMAGE_COUNT; k++) {
				meanImage[i][k] = sum / Constants.IMAGE_COUNT;
			}
		}

		// Saving the mean image as a PGM file
		// File meanImageFile = new File(
		// "E:/Mahesh/Workspace/Eclipse/FaceRecognition/resources/meanImage_"
		// + Constants.SUBJECT_COUNT + "_"
		// + Constants.IMAGES_PER_SUBJECT + ".pgm");
		// FileWriter fileWriter = new
		// FileWriter(meanImageFile.getAbsoluteFile(),
		// true);
		// BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		// PrintWriter printWriter = new PrintWriter(bufferedWriter);
		// printWriter.write(header);
		//
		// for (int m = 0; m < Constants.VECTOR_MATRIX_ROWS; m++) {
		// printWriter.write((int) meanImage[m][0] + " ");
		// // printWriter.write((int)normalizedFaces.getEntry(m, 0) + " ");
		// }
		// printWriter.flush();
		// printWriter.close();

		// Normalizing the face images by removing the common features. The
		// images would then be left with their unique features
		double[][] normalizedImageMatrix = new double[Constants.VECTOR_MATRIX_ROWS][Constants.IMAGE_COUNT];
		for (int i = 0; i < Constants.VECTOR_MATRIX_ROWS; i++) {
			for (int j = 0; j < Constants.IMAGE_COUNT; j++) {
				normalizedImageMatrix[i][j] = faceVectorMatrix[i][j]
						- meanImage[i][j];
			}
		}

		// Saving the first 5 normalized images
		// for (int y = 0; y < 5; y++) {
		// File normImageFile = new File(
		// "E:/Mahesh/Workspace/Eclipse/FaceRecognition/resources/normImage_"
		// + y + "_" + Constants.SUBJECT_COUNT + "_"
		// + Constants.IMAGES_PER_SUBJECT + ".pgm");
		// FileWriter normFileWriter = new FileWriter(
		// normImageFile.getAbsoluteFile(), true);
		// BufferedWriter normBufferedWriter = new BufferedWriter(
		// normFileWriter);
		// PrintWriter normPrintWriter = new PrintWriter(normBufferedWriter);
		// normPrintWriter.write(header);
		//
		// for (int m = 0; m < Constants.VECTOR_MATRIX_ROWS; m++) {
		// normPrintWriter.write((int) normalizedImageMatrix[m][y] + " ");
		// // printWriter.write((int)normalizedFaces.getEntry(m, 0) + " ");
		// }
		// normPrintWriter.flush();
		// normPrintWriter.close();
		// }

		// Creating a RealMatrix object of the normalized images (A)
		RealMatrix normalizedFacesMatrix = MatrixUtils
				.createRealMatrix(normalizedImageMatrix);

		// Calculating the covariance
		Covariance covariance = new Covariance(normalizedFacesMatrix);
		RealMatrix covarianceMatrix = covariance.getCovarianceMatrix();

		// Finding the eigen vectors (eigenfaces - Vi). Columns of the V
		// matrix represent the eigenvectors.
		EigenDecomposition eigenDecomposition = new EigenDecomposition(
				covarianceMatrix);
		RealMatrix reducedEigenFaces = eigenDecomposition.getV();

		// Finding the eigenfaces in original dimension (Ui = A.Vi)
		eigenFaces = MatrixUtils.createRealMatrix(Constants.VECTOR_MATRIX_ROWS,
				Constants.IMAGE_COUNT);
		for (int i = 0; i < reducedEigenFaces.getColumnDimension(); i++) {
			RealVector eigenFace = normalizedFacesMatrix.multiply(
					reducedEigenFaces.getColumnMatrix(i)).getColumnVector(0);

			// Converting into unit vector
			eigenFace = eigenFace.unitVector();

			eigenFaces.setColumnVector(i, eigenFace);
		}

		System.out.println("Reduced Eigenfaces: "
				+ reducedEigenFaces.getRowDimension() + " x "
				+ reducedEigenFaces.getColumnDimension());
		System.out.println("Eigenfaces: " + eigenFaces.getRowDimension()
				+ " x " + eigenFaces.getColumnDimension());

		// Selecting the best K eigen faces
		System.out
				.println("How many eigen faces would you like consider? (out of "
						+ eigenFaces.getColumnDimension() + "): ");
		rank = scanner.nextInt();

		// Calculating the weight vectors for images
		weightVectorMatrix = MatrixUtils.createRealMatrix(rank,
				Constants.IMAGE_COUNT);
		for (int i = 0; i < Constants.IMAGE_COUNT; i++) {
			for (int j = 0; j < rank; j++) {
				RealMatrix eigenFaceTranspose = eigenFaces.getColumnMatrix(j)
						.transpose();
				double weight = eigenFaceTranspose.multiply(
						normalizedFacesMatrix.getColumnMatrix(i))
						.getEntry(0, 0); // Result is a single value i.e. 1 x 1
											// matrix
				weightVectorMatrix.setEntry(j, i, weight);
			}
		}

		System.out.println("Weights: " + weightVectorMatrix.getRowDimension()
				+ " x " + weightVectorMatrix.getColumnDimension());

		// Reconstructing Images from Eigenfaces using the weights
//		RealMatrix reconstructedImagesMatrix = MatrixUtils.createRealMatrix(
//				Constants.VECTOR_MATRIX_ROWS, Constants.IMAGE_COUNT);
//		for (int k = 0; k < Constants.IMAGE_COUNT; k++) {
//
//			// Creating a column matrix
//			RealMatrix image = MatrixUtils.createRealMatrix(
//					Constants.VECTOR_MATRIX_ROWS, 1);
//
//			for (int i = 0; i < rank; i++) {
//				double weight = weightVectorMatrix.getEntry(i, k);
//				RealMatrix eigenFace = eigenFaces.getColumnMatrix(i);
//				for (int j = 0; j < eigenFace.getRowDimension(); j++)
//					eigenFace.multiplyEntry(j, 0, weight);
//
//				image = image.add(eigenFace);
//			}
//
//			// Storing the reconstructed image
//			reconstructedImagesMatrix.setColumnMatrix(k, image);
//		}

		// Saving the first 5 reconstructed images as PGM files
		// for (int z = 0; z < 5; z++) {
		// File reconsImageFile = new File(
		// "E:/Mahesh/Workspace/Eclipse/FaceRecognition/resources/ReconsFace_"
		// + z + "_" + rank + "_" + Constants.SUBJECT_COUNT
		// + "_" + Constants.IMAGES_PER_SUBJECT + ".pgm");
		// FileWriter reconsFileWriter = new FileWriter(
		// reconsImageFile.getAbsoluteFile(), true);
		// BufferedWriter reconsBufferedWriter = new BufferedWriter(
		// reconsFileWriter);
		// PrintWriter reconsPrintWriter = new PrintWriter(
		// reconsBufferedWriter);
		// reconsPrintWriter.write(header);
		//
		// for (int m = 0; m < Constants.VECTOR_MATRIX_ROWS; m++) {
		// reconsPrintWriter.write((int) reconstructedImagesMatrix
		// .getEntry(m, z) + " ");
		// }
		// reconsPrintWriter.flush();
		// reconsPrintWriter.close();
		// }

		// Calculating the euclidean distance of each vector to the rest others
		RealMatrix distances = MatrixUtils.createRealMatrix(
				weightVectorMatrix.getColumnDimension(),
				weightVectorMatrix.getColumnDimension());
		for (int i = 0; i < weightVectorMatrix.getColumnDimension(); i++) {
			RealVector currentVector = weightVectorMatrix.getColumnVector(i);
			for (int j = 0; j < weightVectorMatrix.getColumnDimension(); j++) {
				if (i != j)
					distances
							.setEntry(i, j, currentVector
									.getDistance(weightVectorMatrix
											.getColumnVector(j)));
				else
					distances.setEntry(i, j, 99999d);
			}
		}

		// Creating a vector of the minimum values of the euclidean distances
		RealVector minDistanceVector = MatrixUtils
				.createRealVector(new double[weightVectorMatrix
						.getColumnDimension()]);
		for (int i = 0; i < distances.getRowDimension(); i++) {
			minDistanceVector.setEntry(i, distances.getRowVector(i)
					.getMinValue());
		}

		// Taking the threshold to be the maximum value of the minimum euclidean
		// distances of each image to other images
		threshold = minDistanceVector.getMaxValue();

		System.out.println("Threshold:" + threshold);
		System.out
		.println("Threshold Index ("
				+ minDistanceVector.getMaxIndex()
				+ "): s"
				+ ((minDistanceVector.getMaxIndex() + Constants.IMAGES_PER_SUBJECT) / Constants.IMAGES_PER_SUBJECT)
				+ "_" + ((minDistanceVector.getMaxIndex()) % Constants.IMAGES_PER_SUBJECT));
		System.out
		.println("Image ("
				+ distances.getRowVector(minDistanceVector.getMaxIndex()).getMinIndex()
				+ "): s"
				+ ((distances.getRowVector(minDistanceVector.getMaxIndex()).getMinIndex() + Constants.IMAGES_PER_SUBJECT) / Constants.IMAGES_PER_SUBJECT)
				+ "_" + ((distances.getRowVector(minDistanceVector.getMaxIndex()).getMinIndex()) % Constants.IMAGES_PER_SUBJECT));
	}

	/**
	 * Method to prepare a training set by converting all the images into column
	 * vectors and storing as a matrix
	 * 
	 * @throws IOException
	 */
	private void prepareTrainingSet() throws IOException {
		Scanner fileScanner = null;
		faceVectorMatrix = new double[Constants.VECTOR_MATRIX_ROWS][Constants.IMAGE_COUNT];
		int imageCount = 0;

		// Reading the training images and creating a face vector matrix
		for (int i = 1; i <= Constants.SUBJECT_COUNT; i++) {
			for (int j = 0; j < Constants.IMAGES_PER_SUBJECT; j++) {
				header = "";
				File file = new File(Constants.TRAINING_PATH + i + "_" + j
						+ ".pgm");

				fileScanner = new Scanner(file);

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
					faceVectorMatrix[k][imageCount] = pixelValue;
				}
				imageCount++;
			}
		}

		// Closing the scanner
		fileScanner.close();
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public double[][] getFaceVectorMatrix() {
		return faceVectorMatrix;
	}

	public void setFaceVectorMatrix(double[][] faceVectorMatrix) {
		this.faceVectorMatrix = faceVectorMatrix;
	}

	public double[][] getMeanImage() {
		return meanImage;
	}

	public void setMeanImage(double[][] meanImage) {
		this.meanImage = meanImage;
	}

	public RealMatrix getWeightVectorMatrix() {
		return weightVectorMatrix;
	}

	public void setWeightVectorMatrix(RealMatrix weightVectorMatrix) {
		this.weightVectorMatrix = weightVectorMatrix;
	}

	public RealMatrix getEigenFaces() {
		return eigenFaces;
	}

	public void setEigenFaces(RealMatrix eigenFaces) {
		this.eigenFaces = eigenFaces;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

}
