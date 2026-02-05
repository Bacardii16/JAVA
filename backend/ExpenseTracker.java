import java.util.*;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

/* ---------- Expense Class ---------- */
class Expense {
    double amount;
    String category;
    LocalDate date;

    Expense(double a, String c, LocalDate d) {
        amount = a;
        category = c;
        date = d;
    }
}

/* ---------- Main Class ---------- */
public class ExpenseTracker {

    static Scanner sc = new Scanner(System.in);
    static ArrayList<Expense> expenses = new ArrayList<>();
    static String currentUser;
    static File userFile;
    static double monthlyBudget = 0;
    static ArrayList<Double> savings = new ArrayList<>();

    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public static void main(String[] args) {

        new File("data").mkdir(); // user data folder

        loginOrRegister();
        loadUserData();

        int choice;
        do {
            System.out.println("\n===== EXPENSE TRACKER =====");
            System.out.println("1. Add Expense");
            System.out.println("2. Set Monthly Budget");
            System.out.println("3. Add Savings");
            System.out.println("4. View Budget Status");
            System.out.println("5. View Expenses by Date");
            System.out.println("6. Generate Monthly Excel Report");
            System.out.println("7. Exit");
            System.out.print("Enter choice: ");

            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1 -> addExpense();
                case 2 -> setBudget();
                case 3 -> addSavings();
                case 4 -> viewBudgetStatus();
                case 5 -> viewExpenseByDate();
                case 6 -> generateMonthlyReport();
                case 7 -> saveUserData();
                default -> System.out.println("Invalid choice!");
            }
        } while (choice != 7);

        System.out.println("Logged out successfully!");
    }

    /* ---------- LOGIN / REGISTER ---------- */
    static void loginOrRegister() {
        System.out.print("Enter Username: ");
        currentUser = sc.nextLine();

        userFile = new File("data/" + currentUser + ".txt");

        if (userFile.exists()) {
            System.out.print("Enter Password: ");
            String p = sc.nextLine();

            if (!checkPassword(p)) {
                System.out.println("❌ Invalid passcode!");
                System.exit(0);
            }
            System.out.println("Login successful!");
        } else {
            System.out.print("Create Password: ");
            String p = sc.nextLine();
            createUserFile(p);
            System.out.println("Account created successfully!");
        }
    }

    static boolean checkPassword(String pass) {
        try (Scanner fs = new Scanner(userFile)) {
            fs.nextLine(); // [USER]
            String line = fs.nextLine(); // password=xxx
            return line.split("=")[1].equals(pass);
        } catch (Exception e) {
            return false;
        }
    }

    static void createUserFile(String password) {
        try (FileWriter fw = new FileWriter(userFile)) {
            fw.write("[USER]\n");
            fw.write("password=" + password + "\n\n");
            fw.write("[BUDGET]\n0\n\n");
            fw.write("[SAVINGS]\n\n");
            fw.write("[EXPENSES]\n");
        } catch (IOException e) {
            System.out.println("Error creating user file!");
        }
    }

    /* ---------- LOAD USER DATA ---------- */
    static void loadUserData() {
        try (Scanner fs = new Scanner(userFile)) {
            while (fs.hasNextLine()) {
                String line = fs.nextLine();

                if (line.equals("[BUDGET]"))
                    monthlyBudget = Double.parseDouble(fs.nextLine());

                else if (line.equals("[SAVINGS]")) {
                    while (fs.hasNextLine()) {
                        line = fs.nextLine();
                        if (line.startsWith("[EXPENSES]")) break;
                        if (!line.isBlank()) savings.add(Double.parseDouble(line));
                    }
                }

                if (line.equals("[EXPENSES]")) {
                    while (fs.hasNextLine()) {
                        String e = fs.nextLine();
                        if (!e.isBlank()) {
                            String[] d = e.split(",");
                            expenses.add(new Expense(
                                    Double.parseDouble(d[2]),
                                    d[1],
                                    LocalDate.parse(d[0], formatter)
                            ));
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    /* ---------- SAVE USER DATA ---------- */
    static void saveUserData() {
        try (FileWriter fw = new FileWriter(userFile)) {
            fw.write("[USER]\npassword=****\n\n");
            fw.write("[BUDGET]\n" + monthlyBudget + "\n\n");
            fw.write("[SAVINGS]\n");
            for (double s : savings) fw.write(s + "\n");
            fw.write("\n[EXPENSES]\n");
            for (Expense e : expenses)
                fw.write(e.date.format(formatter) + "," + e.category + "," + e.amount + "\n");
        } catch (IOException e) {
            System.out.println("Error saving data!");
        }
    }

    /* ---------- FEATURES ---------- */
    static void addExpense() {
        System.out.print("Amount: ");
        double amt = sc.nextDouble();
        sc.nextLine();

        System.out.print("Category: ");
        String cat = sc.nextLine();

        System.out.print("Date (DD-MM-YYYY): ");
        LocalDate d = LocalDate.parse(sc.nextLine(), formatter);

        expenses.add(new Expense(amt, cat, d));
        System.out.println("Expense added!");
    }

    static void setBudget() {
        System.out.print("Enter Monthly Budget: ");
        monthlyBudget = sc.nextDouble();
        sc.nextLine();
        System.out.println("Budget saved!");
    }

    static void addSavings() {
        System.out.print("Enter Savings Amount: ");
        savings.add(sc.nextDouble());
        sc.nextLine();
        System.out.println("Savings added!");
    }

    static void viewBudgetStatus() {
        LocalDate now = LocalDate.now();

        double spent = expenses.stream()
                .filter(e -> e.date.getMonth() == now.getMonth()
                        && e.date.getYear() == now.getYear())
                .mapToDouble(e -> e.amount)
                .sum();

        double totalSavings = savings.stream().mapToDouble(Double::doubleValue).sum();
        double left = monthlyBudget - spent - totalSavings;

        System.out.println("\nBudget: ₹" + monthlyBudget);
        System.out.println("Spent: ₹" + spent);
        System.out.println("Savings: ₹" + totalSavings);
        System.out.println("Remaining: ₹" + left);
    }

    static void viewExpenseByDate() {
        System.out.print("Enter Date (DD-MM-YYYY): ");
        LocalDate d = LocalDate.parse(sc.nextLine(), formatter);

        double total = 0;
        for (Expense e : expenses) {
            if (e.date.equals(d)) {
                System.out.println(e.category + " : ₹" + e.amount);
                total += e.amount;
            }
        }
        System.out.println("Total: ₹" + total);
    }

    static void generateMonthlyReport() {
        System.out.print("Month (MM): ");
        int m = Integer.parseInt(sc.nextLine());
        System.out.print("Year (YYYY): ");
        int y = Integer.parseInt(sc.nextLine());

        String file = "monthly_report_" + currentUser + "_" + m + "_" + y + ".csv";

        try (FileWriter fw = new FileWriter(file)) {
            fw.write("Date,Category,Amount\n");
            for (Expense e : expenses) {
                if (e.date.getMonthValue() == m && e.date.getYear() == y)
                    fw.write(e.date + "," + e.category + "," + e.amount + "\n");
            }
            System.out.println("Report generated: " + file);
        } catch (IOException e) {
            System.out.println("Error generating report!");
        }
    }
}
