package edu.ua.fr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class RecognizerAccuracy {

	public void recognize(Scanner scanner, Trainer trainer) throws IOException {

		double[][] meanImage = trainer.getMeanImage();
		RealMatrix weightVectorMatrix = trainer.getWeightVectorMatrix();
		int rank = trainer.getRank();
		RealMatrix eigenFaces = trainer.getEigenFaces();

		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("Log Sheet");

		HSSFRow row0 = sheet.createRow(0);
		HSSFCell cell00 = row0.createCell(0, HSSFCell.CELL_TYPE_STRING);
		cell00.setCellValue("Threshold:");
		HSSFCell cell01 = row0.createCell(1, HSSFCell.CELL_TYPE_STRING);
		cell01.setCellValue(trainer.getThreshold());

		HSSFRow row1 = sheet.createRow(1);
		HSSFCell cell0 = row1.createCell(0, HSSFCell.CELL_TYPE_STRING);
		cell0.setCellValue("Probe");
		HSSFCell cell1 = row1.createCell(1, HSSFCell.CELL_TYPE_STRING);
		cell1.setCellValue("Match");
		HSSFCell cell2 = row1.createCell(2, HSSFCell.CELL_TYPE_STRING);
		cell2.setCellValue("Min Distance::Index");
		HSSFCell cell3 = row1.createCell(3, HSSFCell.CELL_TYPE_STRING);
		cell3.setCellValue("Max Distance::Index");
		HSSFCell cell4 = row1.createCell(4, HSSFCell.CELL_TYPE_NUMERIC);
		cell4.setCellValue("Avg Distance");
		HSSFCell cell5 = row1.createCell(5, HSSFCell.CELL_TYPE_STRING);
		cell5.setCellValue("Second Least:: Index");
		HSSFCell cell6 = row1.createCell(6, HSSFCell.CELL_TYPE_STRING);
		cell6.setCellValue("Third Least:: Index");
		HSSFCell cell7 = row1.createCell(7, HSSFCell.CELL_TYPE_STRING);
		cell7.setCellValue("Fourth Least:: Index");
		HSSFCell cell8 = row1.createCell(8, HSSFCell.CELL_TYPE_STRING);
		cell8.setCellValue("Fifth Least:: Index");

		HSSFFont font = workbook.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

		HSSFCellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setFont(font);
		cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		cell0.setCellStyle(cellStyle);
		cell1.setCellStyle(cellStyle);
		cell2.setCellStyle(cellStyle);
		cell3.setCellStyle(cellStyle);
		cell4.setCellStyle(cellStyle);
		cell5.setCellStyle(cellStyle);
		cell6.setCellStyle(cellStyle);
		cell7.setCellStyle(cellStyle);
		cell8.setCellStyle(cellStyle);
		cell00.setCellStyle(cellStyle);

		// Reading all the files under the testing folder
		File folder = new File(
				"E:/Mahesh/Workspace/Eclipse/FaceRecognition/resources/testing/");
		File[] listOfFiles = folder.listFiles();

		for (int f = 0; f < listOfFiles.length; f++) {

			double[][] imageVector = new double[Constants.VECTOR_MATRIX_ROWS][1];

			// System.out.println("Enter the path of the image file:");
			// String path = scanner.next();
			File file = listOfFiles[f];

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
				normalizedImageVector[i][0] = imageVector[i][0]
						- meanImage[i][0];
			}

			RealMatrix normalizedImageMatrix = MatrixUtils
					.createRealMatrix(normalizedImageVector);

			// Calculating the weight vector
			RealMatrix weightMatrix = MatrixUtils.createRealMatrix(rank, 1);

			for (int i = 0; i < rank; i++) {
				RealMatrix eigenFaceTranspose = eigenFaces.getColumnMatrix(i)
						.transpose();
				double weight = eigenFaceTranspose.multiply(
						normalizedImageMatrix).getEntry(0, 0);// Result is a
																// single values
																// i.e. 1 x 1
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
			double maxDistance = distance[0];
			double avgDistance = 0;
			int index = 0;
			int maxIndex = 0;

			int secondLeastI = 0;
			int thirdLeastI = 0;
			int fourthLeastI = 0;
			int fifthLeastI = 0;

			double secondLeastD = distance[0];
			double thirdLeastD = distance[0];
			double fourthLeastD = distance[0];
			double fifthLeastD = distance[0];

			for (int i = 1; i < distance.length; i++) {
				if (leastDistance > distance[i]) {
					fifthLeastD = fourthLeastD;
					fourthLeastD = thirdLeastD;
					thirdLeastD = secondLeastD;
					secondLeastD = leastDistance;
					leastDistance = distance[i];

					fifthLeastI = fourthLeastI;
					fourthLeastI = thirdLeastI;
					thirdLeastI = secondLeastI;
					secondLeastI = index;
					index = i;

				}
				if (maxDistance < distance[i]) {
					maxDistance = distance[i];
					maxIndex = i;
				}
				avgDistance += distance[i];
			}
//			System.out.println("Least Distance:" + leastDistance);

			String match = "s"
					+ ((index + Constants.IMAGES_PER_SUBJECT) / Constants.IMAGES_PER_SUBJECT)
					+ "_" + ((index) % Constants.IMAGES_PER_SUBJECT);

			// Identifying the image based on the euclidean distance
//			System.out.println("Image found (" + index + "): " + match);

			HSSFRow currentRow = sheet.createRow(f + 2); // Two rows are already
															// created
			String fileName = file.getPath().substring(
					file.getPath().lastIndexOf("\\") + 1,
					file.getPath().indexOf("."));

			HSSFCell probeCell = currentRow.createCell(0);
			HSSFCell matchCell = currentRow.createCell(1);
			HSSFCell minCell = currentRow.createCell(2);
			HSSFCell maxCell = currentRow.createCell(3);
			HSSFCell avgCell = currentRow.createCell(4);
			HSSFCell secLeast = currentRow.createCell(5);
			HSSFCell thirdLeast = currentRow.createCell(6);
			HSSFCell fourthLeast = currentRow.createCell(7);
			HSSFCell fifthLeast = currentRow.createCell(8);

			probeCell.setCellValue(fileName);
			matchCell.setCellValue(match);
			minCell.setCellValue(leastDistance + " :: " + index);
			maxCell.setCellValue(maxDistance + " :: " + maxIndex);
			avgCell.setCellValue(avgDistance / distance.length);
			secLeast.setCellValue(secondLeastD + " :: " + secondLeastI);
			thirdLeast.setCellValue(thirdLeastD + " :: " + thirdLeastI);
			fourthLeast.setCellValue(fourthLeastD + " :: " + fourthLeastI);
			fifthLeast.setCellValue(fifthLeastD + " :: " + fifthLeastI);
		}

		try {
			FileOutputStream fileOutputStream = new FileOutputStream(new File(
					"E:/Mahesh/Workspace/Eclipse/FaceRecognition/resources/TestResults_"
							+ Constants.SUBJECT_COUNT + "_"
							+ Constants.IMAGES_PER_SUBJECT + "_"
							+ trainer.getRank() + ".xls"));
			workbook.write(fileOutputStream);
			fileOutputStream.close();

			System.out.println("Log written successfully");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
