import java.util.Scanner;

public class Bai6 {
  public static void main(String[] args) {
    Scanner sc = new Scanner(System.in);

    System.out.print("Tên sư tử: ");
    String tenSuTu = sc.nextLine();

    System.out.print("Cân nặng sư tử: ");
    double canNangSuTu = sc.nextDouble();

    System.out.print("Lượng thịt ăn mỗi ngày: ");
    double thit = sc.nextDouble();
    sc.nextLine();

    System.out.print("Tên rắn: ");
    String tenRan = sc.nextLine();

    System.out.print("Cân nặng rắn: ");
    double canNangRan = sc.nextDouble();

    System.out.print("Chiều dài rắn: ");
    double chieuDai = sc.nextDouble();
    sc.nextLine();

    System.out.print("Tên khỉ: ");
    String tenKhi = sc.nextLine();

    System.out.print("Cân nặng khỉ: ");
    double canNangKhi = sc.nextDouble();
    sc.nextLine();

    System.out.print("Thức ăn yêu thích: ");
    String thucAn = sc.nextLine();

    Lion a = new Lion(tenSuTu, canNangSuTu, thit);
    Snake b = new Snake(tenRan, canNangRan, chieuDai);
    Monkey c = new Monkey(tenKhi, canNangKhi, thucAn);

    System.out.println("\n--- THÔNG TIN ĐỘNG VẬT ---");
    a.xuat();
    b.xuat();
    c.xuat();
  }
}

class Animal {
  String name;
  double weight;

  Animal(String name, double weight) {
    this.name = name;
    this.weight = weight;
  }

  void xuat() {
    System.out.println(name + " - " + weight + " kg");
  }
}

class Lion extends Animal {
  double food;

  Lion(String name, double weight, double food) {
    super(name, weight);
    this.food = food;
  }

  void xuat() {
    System.out.println("Sư tử " + name + " nặng " + weight
        + " kg, mỗi ngày ăn " + food + " kg thịt.");
  }
}

class Snake extends Animal {
  double length;

  Snake(String name, double weight, double length) {
    super(name, weight);
    this.length = length;
  }

  void xuat() {
    System.out.println("Rắn " + name + " nặng " + weight
        + " kg, chiều dài " + length + " mét.");
  }
}

class Monkey extends Animal {
  String favoriteFood;

  Monkey(String name, double weight, String favoriteFood) {
    super(name, weight);
    this.favoriteFood = favoriteFood;
  }

  void xuat() {
    System.out.println("Khỉ " + name + " nặng " + weight
        + " kg, thích ăn " + favoriteFood + ".");
  }
}
