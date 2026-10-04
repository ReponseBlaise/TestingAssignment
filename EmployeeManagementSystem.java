import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Organization X - Employee Management and Payroll System.
 *
 * One file holding both the program under test and its 20 automated test cases
 * (10 black-box, 10 white-box). Run it to execute the tests.
 *
 * Pay rules:
 *   hourly rate = salary / 160;  overtime = overtime hours * hourly rate * 1.5
 *   gross pay   = regular pay + overtime pay
 *   bonus       = 10% of gross pay for Management, else 0
 *   taxable pay = gross pay + bonus
 *   tax         = 25% of taxable pay when it is >= 5000, else 15%
 *   net pay     = taxable pay - tax
 */
public class EmployeeManagementSystem {

    static final double STANDARD_MONTHLY_HOURS = 160.0;
    static final double OVERTIME_MULTIPLIER = 1.5;
    static final double MANAGEMENT_BONUS_RATE = 0.10;
    static final double HIGH_TAX_THRESHOLD = 5000.0;
    static final double HIGH_TAX_RATE = 0.25;
    static final double LOW_TAX_RATE = 0.15;
    static final String MANAGEMENT_DEPARTMENT = "Management";
    static final String EMPTY_REPORT = "No employees to report.";

    static final class Employee {
        final String id, name, department;
        final double salary;

        Employee(String id, String name, String department, double salary) {
            this.id = id;
            this.name = name;
            this.department = department;
            this.salary = salary;
        }
    }

    static final class Payment {
        final String id;
        final double hourly, regular, overtime, gross, bonus, taxable, taxRate, tax, net;

        Payment(Employee e, double hours, double overtimeHours) {
            this.id = e.id;
            this.hourly = e.salary / STANDARD_MONTHLY_HOURS;
            this.regular = hours * hourly;
            this.overtime = overtimeHours * hourly * OVERTIME_MULTIPLIER;
            this.gross = regular + overtime;
            this.bonus = isManagement(e.department) ? gross * MANAGEMENT_BONUS_RATE : 0.0;
            this.taxable = gross + bonus;
            this.taxRate = taxable >= HIGH_TAX_THRESHOLD ? HIGH_TAX_RATE : LOW_TAX_RATE;
            this.tax = taxable * taxRate;
            this.net = taxable - tax;
        }
    }

    private final Map<String, Employee> employees = new LinkedHashMap<String, Employee>();
    private final Map<String, Payment> payments = new LinkedHashMap<String, Payment>();

    static boolean isManagement(String department) {
        return department != null && MANAGEMENT_DEPARTMENT.equalsIgnoreCase(department.trim());
    }

    static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    boolean addEmployee(String id, String name, String department, double salary) {
        String key = trim(id);
        if (key.isEmpty()) {
            throw new IllegalArgumentException("Employee ID is required.");
        }
        if (trim(name).isEmpty()) {
            throw new IllegalArgumentException("Employee name is required.");
        }
        String dept = trim(department);
        if (dept.isEmpty()) {
            throw new IllegalArgumentException("Department is required.");
        }
        if (salary < 0) {
            throw new IllegalArgumentException("Salary cannot be negative.");
        }
        if (employees.containsKey(key)) {
            return false;
        }
        employees.put(key, new Employee(key, trim(name), dept, salary));
        return true;
    }

    boolean removeEmployee(String id) {
        String key = trim(id);
        if (key.isEmpty() || !employees.containsKey(key)) {
            return false;
        }
        employees.remove(key);
        payments.remove(key);
        return true;
    }

    Employee getEmployee(String id) {
        Employee e = employees.get(trim(id));
        if (e == null) {
            throw new NoSuchElementException("Employee '" + id + "' is not registered.");
        }
        return e;
    }

    int getEmployeeCount() {
        return employees.size();
    }

    int getPaymentCount() {
        return payments.size();
    }

    Payment processPayment(String id, double hours, double overtimeHours) {
        if (hours < 0) {
            throw new IllegalArgumentException("Hours cannot be negative.");
        }
        if (overtimeHours < 0) {
            throw new IllegalArgumentException("Overtime hours cannot be negative.");
        }
        Payment payment = new Payment(getEmployee(id), hours, overtimeHours);
        payments.put(payment.id, payment);
        return payment;
    }

    double getTotalPayroll() {
        double total = 0.0;
        for (Payment p : payments.values()) {
            total += p.net;
        }
        return round2(total);
    }

    double getAverageNetPay() {
        return payments.isEmpty() ? 0.0 : round2(getTotalPayroll() / payments.size());
    }

    Employee getTopEarner() {
        Employee top = null;
        double highest = Double.NEGATIVE_INFINITY;
        for (Employee e : employees.values()) {
            Payment p = payments.get(e.id);
            if (p != null && p.net > highest) {
                highest = p.net;
                top = e;
            }
        }
        return top;
    }

    String generatePayrollReport() {
        if (employees.isEmpty()) {
            return EMPTY_REPORT;
        }
        StringBuilder r = new StringBuilder();
        r.append("PAYROLL REPORT - Organization X").append(System.lineSeparator());
        r.append("--------------------------------------------------").append(System.lineSeparator());
        for (Employee e : employees.values()) {
            Payment p = payments.get(e.id);
            r.append(p == null
                    ? String.format("%-8s %-22s %-16s %12s%n", e.id, e.name, e.department, "NOT PAID")
                    : String.format("%-8s %-22s %-16s %12.2f%n", e.id, e.name, e.department, p.net));
        }
        r.append("--------------------------------------------------").append(System.lineSeparator());
        r.append("Employees registered : ").append(employees.size()).append(System.lineSeparator());
        r.append("Payments processed    : ").append(payments.size()).append(System.lineSeparator());
        r.append(String.format("Total payroll        : %.2f%n", getTotalPayroll()));
        r.append(String.format("Average net pay       : %.2f%n", getAverageNetPay()));
        Employee top = getTopEarner();
        r.append(top == null
                ? "Top earner           : none - no payment has been processed"
                : String.format("Top earner: %s (%s) at %.2f", top.name, top.id, payments.get(top.id).net));
        return r.toString();
    }

    // ------------------------------------------------------------------
    // Test suite: 10 black-box cases followed by 10 white-box cases.
    // ------------------------------------------------------------------

    static int passed, failed;

    public static void main(String[] args) {
        System.out.println("PART A - BLACK-BOX (FUNCTIONAL) TEST CASES");
        blackBox();
        System.out.println();
        System.out.println("PART B - WHITE-BOX (STRUCTURAL) TEST CASES");
        whiteBox();
        System.out.println();
        System.out.println("SUMMARY");
        System.out.println("---------------------------------------------");
        System.out.println("Total test cases : " + (passed + failed));
        System.out.println("Passed           : " + passed);
        System.out.println("Failed           : " + failed);
        System.out.println("Overall result   : " + (failed == 0 ? "ALL TESTS PASSED" : "FAILURES FOUND"));
        if (failed > 0) {
            System.exit(1);
        }
    }

    static EmployeeManagementSystem system(String id, String name, String dept, double salary) {
        EmployeeManagementSystem s = new EmployeeManagementSystem();
        s.addEmployee(id, name, dept, salary);
        return s;
    }

    static void blackBox() {
        t("BB-01", "FR1", "Add E001, Alice Uwase, Engineering, 3000", "Added; count = 1", () -> {
            EmployeeManagementSystem s = new EmployeeManagementSystem();
            ok(s.addEmployee("E001", "Alice Uwase", "Engineering", 3000), "should be added");
            eq(1, s.getEmployeeCount(), "count");
        });

        t("BB-02", "FR2", "Add E001 a second time", "Refused; count stays 1", () -> {
            EmployeeManagementSystem s = system("E001", "Alice Uwase", "Engineering", 3000);
            ok(!s.addEmployee("E001", "Someone Else", "Finance", 1), "duplicate should be refused");
            eq(1, s.getEmployeeCount(), "count");
        });

        t("BB-03", "FR2", "Add employee with empty ID", "Rejected with an error", () ->
                rejected(() -> new EmployeeManagementSystem()
                        .addEmployee("", "Alice Uwase", "Engineering", 3000)));

        t("BB-04", "FR2", "Add employee with salary -100", "Rejected with an error", () ->
                rejected(() -> new EmployeeManagementSystem()
                        .addEmployee("E002", "Alice Uwase", "Engineering", -100)));

        t("BB-05", "FR3, FR7", "Remove existing employee E001", "Removed; count = 0", () -> {
            EmployeeManagementSystem s = system("E001", "Alice Uwase", "Engineering", 3000);
            ok(s.removeEmployee("E001"), "should be removed");
            eq(0, s.getEmployeeCount(), "count");
        });

        t("BB-06", "FR3", "Remove unknown ID E999", "Reported as not found", () -> {
            EmployeeManagementSystem s = system("E001", "Alice Uwase", "Engineering", 3000);
            ok(!s.removeEmployee("E999"), "unknown ID should not be removed");
            eq(1, s.getEmployeeCount(), "E001 must be untouched");
        });

        t("BB-07", "FR4", "Engineering, salary 3000, 160 h, no overtime", "Net pay 2550.00", () -> {
            EmployeeManagementSystem x = system("E001", "Alice Uwase", "Engineering", 3000);
            Payment p = x.processPayment("E001", 160, 0);
            eq(0.0, p.bonus, "no bonus");
            eq(2550.00, p.net, "net pay");
        });

        t("BB-08", "FR4", "Management, salary 6000, 160 h, no overtime", "Net pay 4950.00", () -> {
            EmployeeManagementSystem x = new EmployeeManagementSystem();
            x.addEmployee("E002", "Bob Maziga", "Management", 6000);
            Payment p = x.processPayment("E002", 160, 0);
            eq(600.00, p.bonus, "bonus");
            eq(4950.00, p.net, "net pay");
        });

        t("BB-09", "FR5", "Pay employee E999 who is not registered", "Rejected: not found", () ->
                rejected(() -> new EmployeeManagementSystem().processPayment("E999", 160, 0)));

        t("BB-10", "FR6", "Report with no employees", "\"No employees to report.\"", () ->
                eq(EMPTY_REPORT, new EmployeeManagementSystem().generatePayrollReport(), "report"));
    }

    static void whiteBox() {
        t("WB-01", "FR2", "ID check: ID made only of spaces", "Rejected with an error", () ->
                rejected(() -> new EmployeeManagementSystem()
                        .addEmployee("   ", "Alice Uwase", "Engineering", 3000)));

        t("WB-02", "FR2", "Name check: name made only of spaces", "Rejected with an error", () ->
                rejected(() -> new EmployeeManagementSystem()
                        .addEmployee("E001", "   ", "Engineering", 3000)));

        t("WB-03", "FR2", "Salary check: salary -0.01 (just below zero)", "Rejected with an error", () ->
                rejected(() -> new EmployeeManagementSystem()
                        .addEmployee("E001", "Alice Uwase", "Engineering", -0.01)));

        t("WB-04", "FR2", "Duplicate check: add the same ID twice", "Second add refused", () -> {
            EmployeeManagementSystem x = new EmployeeManagementSystem();
            ok(x.addEmployee("E001", "Alice Uwase", "Engineering", 3000), "first add");
            ok(!x.addEmployee("E001", "Alice Uwase", "Engineering", 3000), "second add refused");
            eq(1, x.getEmployeeCount(), "count");
        });

        t("WB-05", "FR3", "Removal: remove E001 twice", "First succeeds, second fails", () -> {
            EmployeeManagementSystem x = new EmployeeManagementSystem();
            x.addEmployee("E001", "Alice Uwase", "Engineering", 3000);
            ok(x.removeEmployee("E001"), "first removal");
            ok(!x.removeEmployee("E001"), "second removal must fail");
            eq(0, x.getEmployeeCount(), "count");
        });

        t("WB-06", "FR5", "Hours check: valid hours, overtime -1", "Rejected with an error", () -> {
            EmployeeManagementSystem x = new EmployeeManagementSystem();
            x.addEmployee("E001", "Alice Uwase", "Engineering", 3000);
            rejected(() -> x.processPayment("E001", 160, -1));
        });

        t("WB-07", "FR4", "Tax limit: gross pay exactly 5000", "25% tax; net 3750.00", () -> {
            EmployeeManagementSystem x = new EmployeeManagementSystem();
            x.addEmployee("E001", "Alice Uwase", "Engineering", 5000);
            Payment p = x.processPayment("E001", 160, 0);
            eq(0.25, p.taxRate, "tax rate");
            eq(3750.00, p.net, "net pay");
        });

        t("WB-08", "FR4", "Bonus without high tax: Management, salary 3500", "Bonus, 15% tax; net 3272.50", () -> {
            EmployeeManagementSystem x = new EmployeeManagementSystem();
            x.addEmployee("E003", "Carol Mukamana", "Management", 3500);
            Payment p = x.processPayment("E003", 160, 0);
            eq(350.00, p.bonus, "bonus");
            eq(3850.00, p.taxable, "taxable pay");
            eq(0.15, p.taxRate, "tax rate");
            eq(3272.50, p.net, "net pay");
        });

        t("WB-09", "FR4", "Overtime: salary 3000, 160 h, 10 overtime", "Net pay 2789.06", () -> {
            EmployeeManagementSystem x = new EmployeeManagementSystem();
            x.addEmployee("E001", "Alice Uwase", "Engineering", 3000);
            Payment p = x.processPayment("E001", 160, 10);
            eq(281.25, p.overtime, "overtime pay");
            eq(2789.06, p.net, "net pay");
        });

        t("WB-10", "FR6", "Top earner: Alice (3000) and Bob (4000) both paid", "Top earner Bob, 3400.00", () -> {
            EmployeeManagementSystem x = new EmployeeManagementSystem();
            x.addEmployee("E001", "Alice Uwase", "Engineering", 3000);
            x.addEmployee("E002", "Bob Maziga", "Engineering", 4000);
            x.processPayment("E001", 160, 0);
            x.processPayment("E002", 160, 0);
            Employee top = x.getTopEarner();
            eq("E002", top.id, "top earner id");
            eq(3400.00, x.getTotalPayroll() - 2550.00, "Bob's net pay");
            ok(x.generatePayrollReport().contains("Bob Maziga"), "report must name the top earner");
        });
    }

    static void t(String id, String fr, String input, String expected, Runnable body) {
        try {
            body.run();
            passed++;
            System.out.println("[" + id + "] " + fr + "  " + input);
            System.out.println("    Expected: " + expected);
            System.out.println("    Actual  : observed as expected");
            System.out.println("    Result  : PASS");
        } catch (Throwable e) {
            failed++;
            System.out.println("[" + id + "] " + fr + "  " + input);
            System.out.println("    Expected: " + expected);
            System.out.println("    Actual  : " + e);
            System.out.println("    Result  : FAIL");
        }
        System.out.println();
    }

    static void ok(boolean condition, String what) {
        if (!condition) {
            throw new AssertionError(what + ": expected true");
        }
    }

    static void eq(double expected, double actual, String what) {
        if (Math.abs(expected - actual) > 0.005) {
            throw new AssertionError(what + ": expected " + expected + " but was " + actual);
        }
    }

    static void eq(int expected, int actual, String what) {
        if (expected != actual) {
            throw new AssertionError(what + ": expected " + expected + " but was " + actual);
        }
    }

    static void eq(String expected, String actual, String what) {
        if (!expected.equals(actual)) {
            throw new AssertionError(what + ": expected \"" + expected + "\" but was \"" + actual + "\"");
        }
    }

    static void rejected(Runnable body) {
        try {
            body.run();
        } catch (RuntimeException e) {
            return;
        }
        throw new AssertionError("expected an error but the call was accepted");
    }
}