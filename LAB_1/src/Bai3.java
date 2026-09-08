import java.util.Scanner;

public class Bai3 {
  public static void main(String[] args){
    Scanner sc = new Scanner(System.in);
    System.out.println("Vui long nhap vao so thu nhat: ");
    int numberOne = sc.nextInt();
    System.out.println("Vui long nhap so thu hai: ");
    int numberTwo = sc.nextInt();

    int sum = numberOne + numberTwo;
    System.out.println("Tong la: [" + numberOne + "+" + numberTwo + "] = "+sum);

    sc.close();
  }
}
