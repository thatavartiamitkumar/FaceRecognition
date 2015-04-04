package edu.ua.fr;


public interface Constants {

	String TRAINING_PATH = "E:/Mahesh/Workspace/Eclipse/FaceRecognition/resources/training/s";	

	int IMAGE_ROWS = 112; // Number of rows in each image	
	int IMAGE_COLS = 92; // Number of columns in each image 

	int SUBJECT_COUNT = 39; // Number of different subjects(persons) 
	int IMAGES_PER_SUBJECT = 8; // Number of distinct images per subject 
	int IMAGE_COUNT = SUBJECT_COUNT * IMAGES_PER_SUBJECT; // Total number of images used for training

	int VECTOR_MATRIX_ROWS = IMAGE_ROWS * IMAGE_COLS; // Number of rows in each face vector

	


//	File testFile = new File(Constants.TRAINING_PATH + "_test.pgm");
//	FileWriter fileWriter = new FileWriter(testFile.getAbsoluteFile(),
//			true);
//	BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
//	PrintWriter printWriter = new PrintWriter(bufferedWriter);
//	printWriter.write(header);
//
//	for (int m = 0; m < Constants.VECTOR_MATRIX_ROWS; m++) {
//		printWriter.write((int) normalizedImageMatrix[m][0] + " ");
//		printWriter.write((int)normalizedFaces.getEntry(m, 0) + " ");	
//	}
//	printWriter.flush();
//	printWriter.close();
}
