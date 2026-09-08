import java.util.Scanner;

public class Bai2 {
  public static void main(String[] args) {
    Scanner sc = new Scanner(System.in);
    System.out.println("What is your name?");
    String str = sc.nextLine();
    System.out.println("Hi, I am: " + str);
    sc.close();
  }
}
