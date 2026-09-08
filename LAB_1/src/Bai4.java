import java.util.Scanner;

public class Bai4 {
  public static void main(String[] args) {
    Scanner sc = new Scanner(System.in);
    System.out.println(">> Kiem tra so chan le <<");
    System.out.println("Vui long nhap so de kiem tra: ");
    int number = sc.nextInt();
    if(number % 2 == 0) {
      System.out.println("So chan la: " + number);
    } else {
      System.out.println("So le la: " + number);
    }
    sc.close();
  }
}
