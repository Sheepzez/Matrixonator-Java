package main.java.model;

import java.time.LocalDate;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Main Matrix class for mathematical matrix operations.
 * @author Isaac Jordan
 */
public class Matrix {

  private StringProperty name;
  private final IntegerProperty numRows;
  private final IntegerProperty numCols;
  private final ObjectProperty<LocalDate> createdDate;
  private final ObjectProperty<double[][]> data;

  private Double determinant;
  private Matrix inverse;
  private RREFMatrix RREForm;
  private Matrix cofactor;

  /**
   * Default constructor. Creates an empty, unnamed matrix, with current date.
   * 
   * @throws Exception
   */
  public Matrix() {
    this(null, new double[0][0], null);
  }

  /**
   * Constructor with some initial data.
   * 
   * @param name
   * @param data
   * @throws Exception
   */
  public Matrix(String name, double[][] data, LocalDate date) {
    this.name = new SimpleStringProperty(name);
    this.data = new SimpleObjectProperty<double[][]>(data);
    numRows = new SimpleIntegerProperty(data.length);
    numCols = new SimpleIntegerProperty(data[0].length);
    if (date != null)
      createdDate = new SimpleObjectProperty<LocalDate>(date);
    else
      createdDate = new SimpleObjectProperty<LocalDate>(LocalDate.now());
  }

  // Getters/Setters
  // name
  public String getName() {
    return name.get();
  }

  public void setName(String name) {
    this.name.set(name);
  }

  public StringProperty nameProperty() {
    return name;
  }

  // numRows
  public int getNumRows() {
    return numRows.get();
  }

  public IntegerProperty numRowsProperty() {
    return numRows;
  }

  // numCols
  public int getNumCols() {
    return numCols.get();
  }

  public IntegerProperty numColsProperty() {
    return numCols;
  }

  // createdDateget
  public LocalDate getCreatedDate() {
    return createdDate.get();
  }

  public ObjectProperty<LocalDate> createdDateProperty() {
    return createdDate;
  }

  // data
  public double[][] getData() {
    return data.get();
  }

  public double[] getRow(int row) {
    return data.get()[row];
  }

  public double[] getCol(int colNum) {
    double[] column = new double[getNumRows()];
    for (int i = 0; i < getNumRows(); i++)
      column[i] = data.get()[i][colNum];
    return column;
  }

  public double getCell(int row, int col) {
    
    return data.get()[row][col];
  }

  public double[][] cloneData() {
    double[][] result = new double[getNumRows()][getNumCols()];
    for (int i = 0; i < getNumRows(); i++) {
      for (int j = 0; j < getNumCols(); j++) {
        result[i][j] = getData()[i][j];
      }
    }
    return result;
  }

  /**
   * Changes matrix's data to positive 0's and 10 decimal places.
   * 
   * @return
   */
  public Matrix normalise() {
    double[][] data = getData();
    for (int i = 0; i < getNumRows(); i++) {
      for (int j = 0; j < getNumCols(); j++) {
        if (data[i][j] == -0.0)
          data[i][j] = 0.0;
        // Round number to 10 decimal places.
        data[i][j] = Math.round(data[i][j] * 10000000000.0) / 10000000000.0;
      }
    }
    return this;
  }

  /**
   * Checks whether it is possible to multiply two matrices together.
   * 
   * @param A
   * @param B
   * @return
   */
  public static Boolean checkMultCompatibility(Matrix A, Matrix B) {
    if (A.getData().length != B.getData()[0].length)
      return false;
    return true;
  }

  /**
   * Uses naive method to calculate the product of two matrices.
   * Throws IllegalArgumentException if the matrices are not compatible.
   * 
   * @param A
   * @param B
   * @return
   */
  public static Matrix multiplyMatrices(Matrix A, Matrix B) {
    if (checkMultCompatibility(A, B)) {
      double[][] data = new double[A.getNumRows()][B.getNumCols()];
      int i = 0;
      int j = 0;
      int k = 0;
      while (i < A.getNumRows()) {
        while (j < B.getNumCols()) {
          while (k < B.getNumRows()) {
            data[i][j] = data[i][j] + A.getData()[i][k] * B.getData()[k][j];
            k += 1;
          }
          j += 1;
          k = 0;
        }
        i += 1;
        j = 0;
      }
      return (new Matrix(null, data, null));
    }
    throw new IllegalArgumentException("Matrices are not compatible");
  }

  /**
   * Multiples a matrix by a scalar.
   * @param c - A double value to multiple all entries of the matrix by.
   * @return
   */
  public Matrix multiplyScalar(double c) {
    for (int i = 0; i < getNumRows(); i++)
      for (int j = 0; j < getNumCols(); j++)
        getData()[i][j] *= c;
    return this;
  }

  /**
   * Raises matrix to the power n using naive method.
   * Can use a lot of resources if matrix, or n, is large.
   * @param n
   * @return
   */
  public Matrix toPower(int n) {
    Matrix resultMatrix = new Matrix(null, cloneData(), null);
    for (int i = 1; i < n; i++)
      resultMatrix = Matrix.multiplyMatrices(this, resultMatrix);
    return resultMatrix;
  }


  /**
   * Static method that adds two matrices A, and B. If the matrices cannot be added, an
   * IllegalArgumentException is thrown.
   *
   * @param A
   * @param B
   * @return
   */
  public static Matrix addMatrices(Matrix A, Matrix B) {
    if (A.getNumRows() == B.getNumRows() && A.getNumCols() == B.getNumCols()) {
      double[][] data = new double[A.getNumRows()][A.getNumCols()];
      for (int i = 0; i < A.getNumRows(); i++)
        for (int j = 0; j < A.getNumCols(); j++)
          data[i][j] = A.getData()[i][j] + B.getData()[i][j];
      return (new Matrix(null, data, null));
    } else
      throw new IllegalArgumentException("Matrices are not compatible.");
  }

  /**
   * Returns the nested array after having removed the values in the given row, and column.
   * http://en.wikipedia.org/wiki/Minor_(linear_algebra)
   * 
   * @param initialData
   * @param returnData - The object to fill with the result.
   * @param row - Which row's data to remove
   * @param column - Which column's data to remove.
   * @param numRows - How many rows initialData has.
   * @return
   */
  private static double[][] reduce(double[][] initialData, double[][] returnData, int row,
      int column, int numRows) {
    for (int h = 0, j = 0; h < numRows; h++) {
      if (h == row)
        continue;
      for (int i = 0, k = 0; i < numRows; i++) {
        if (i == column)
          continue;
        returnData[j][k] = initialData[h][i];
        k++;
      }
      j++;
    }
    return returnData;
  }

  /**
   * Returns the full cofactor matrix of this matrix.
   * Warning: This is very computationally heavy.
   * @return
   */
  public Matrix cofactorMatrix() {
    if (cofactor != null) {
      return cofactor;
    }
    double[][] data = cloneData();
    double[][] cofactorData = new double[getNumRows()][getNumCols()];
    double det;
    for (int i = 0; i < getNumRows(); i++) {
      for (int j = 0; j < getNumCols(); j++) {
        double[][] reduced = new double[getNumRows() - 1][getNumCols() - 1];
        det = determinant(reduce(data, reduced, i, j, getNumRows()));
        cofactorData[i][j] = ((i + j) % 2 == 0 ? det : -det);
      }
    }
    cofactor = new Matrix(null, cofactorData, null);
    return cofactor;
  }



  /**
   * Private static method that deals with the recursion of determinant calculation. Inspired by
   * http://en.wikibooks.org/wiki/Algorithm_Implementation/Linear_Algebra/Determinant_of_a_Matrix
   * 
   * @param data
   * @return
   */
  private static double determinant(double[][] data) {
    int numRows = data.length;
    double ret = 0;
    if (numRows == 2)
      return data[0][0] * data[1][1] - data[0][1] * data[1][0];

    if (numRows < 4) {
      double prod1 = 1, prod2 = 1;
      for (int i = 0; i < numRows; i++) {
        prod1 = 1;
        prod2 = 1;

        for (int j = 0; j < numRows; j++) {
          prod1 *= data[(j + i + 1) % numRows][j];
          prod2 *= data[(j + i + 1) % numRows][numRows - j - 1];
        }
        ret += prod1 - prod2;
      }
      return ret;
    }

    double[][] reduced = new double[numRows - 1][numRows - 1];
    for (int h = 0; h < numRows; h++) {
      if (data[h][0] == 0)
        continue;
      reduce(data, reduced, h, 0, numRows);
      if (h % 2 == 0)
        ret -= determinant(reduced) * data[h][0];
      if (h % 2 == 1)
        ret += determinant(reduced) * data[h][0];
    }
    return ret;
  }

  /**
   * Public method for determinant calculation.
   * 
   * @return
   */
  public double determinant() {
    if (determinant != null) {
      return determinant;
    }
    determinant = determinant(getData());
    return determinant;
  }

  /**
   * Swaps row1 and row2
   * @param A
   * @param row1
   * @param row2
   * @return
   */
  public static Matrix ERO1(Matrix A, int row1, int row2) {
    double[] temp = A.getData()[row1];
    A.getData()[row1] = A.getData()[row2];
    A.getData()[row2] = temp;
    return A;
  }

  /**
   * Multiply every element of row by scalar
   * @param A
   * @param row
   * @param scalar
   * @return
   */
  public static Matrix ERO2(Matrix A, int row, double scalar) {
    for (int i = 0; i < A.getNumCols(); i++) {
      A.getData()[row][i] *= scalar;
    }
    return A;
  }

  /**
   * Executes: row1 = row1 + scalar*row2
   * @param A
   * @param row1
   * @param row2
   * @param scalar
   * @return
   */
  public static Matrix ERO3(Matrix A, int row1, int row2, double scalar) {
    for (int i = 0; i < A.getNumCols(); i++) {
      A.getData()[row1][i] += scalar * A.getData()[row2][i];
    }
    return A;
  }

  /**
   * Returns the transpose of this matrix in a new Matrix object.
   * @return
   */
  public Matrix transpose() {
    double[][] data = new double[this.getNumCols()][this.getNumRows()];
    for (int i = 0; i < this.getNumRows(); i++) {
      data[i] = this.getCol(i);
    }
    return new Matrix(null, data, null);
  }

  /**
   * Returns the inverse of this matrix in a new matrix object.
   * The inverse is only calculated the first time this method is called,
   * and stored for subsequent calls.
   * @return
   */
  public Matrix inverse() {
    if (inverse != null) {
      return inverse;
  }
  double det = determinant();
  if (det != 0) {
    inverse = cofactorMatrix().transpose().multiplyScalar(1 / det).normalise();
    return inverse;
  } else
    return null;
  }
  
  /**
   * Returns the sum of all values on the main diagonal of this matrix.
   * @return
   */
  public double trace() {
    double total = 0;
    double[][] data = getData();
    for (int i = 0; i < getNumRows(); i++) {
      total += data[i][i];
    }
    return total;
  }
  
  public RREFMatrix reducedEchelonForm() {
    if (RREForm != null) {
      return RREForm;
    }
    RREForm = new RREFMatrix(this);
    return RREForm;
  }
  
  /**
   * Currently only works for 2x2 matrices.
   * http://en.wikipedia.org/wiki/Eigenvalue_algorithm#Direct_calculation
   * @return
   */
  public double[] eigenvalues() {
    if (getNumRows() == 2 && getNumCols() == 2){
      double lambda1 = (this.trace() + Math.sqrt(Math.pow(this.trace(), 2) - 4 * this.determinant()))/2;
      double lambda2 = (this.trace() - Math.sqrt(Math.pow(this.trace(), 2) - 4 * this.determinant()))/2;
      double[] returnData = {lambda1,lambda2};
      return returnData;
    } else {
      return null;
    }
  }

}