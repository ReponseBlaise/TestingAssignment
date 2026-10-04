# Mushimiyumukiza Blaise
# 26229
# Software Testing Techniques
# Organization X - Employee Management and Payroll System

Java implementation of the assignment scenario: managing employees, processing their
payments and producing payroll reports.

Everything is in **one file**, `EmployeeManagementSystem.java`, which holds both the
program and its 20 automated test cases. There is no user interface and nothing to
install beyond a JDK: running the file runs the tests.

## Build and run

The JDK is not on `PATH` on this machine, so use its full path:

```powershell
$jdk = "C:\Program Files\Android\openjdk\jdk-21.0.8\bin"

& "$jdk\javac.exe" -d build EmployeeManagementSystem.java
& "$jdk\java.exe" -cp build EmployeeManagementSystem
```

Expected output:

```
Total test cases : 20
Passed           : 20
Failed           : 0
Overall result   : ALL TESTS PASSED
```

The program exits with code 0 when every case passes and 1 otherwise.

## The program

| Part | Methods |
| --- | --- |
| Managing employees | `addEmployee`, `removeEmployee`, `getEmployee`, `getEmployeeCount` |
| Processing payments | `processPayment` |
| Reporting | `generatePayrollReport`, `getTotalPayroll`, `getAverageNetPay`, `getTopEarner` |

## Business rules

* Hourly rate = base salary / 160 (a standard month is 160 hours)
* Regular pay = hours worked x hourly rate
* Overtime pay = overtime hours x hourly rate x 1.5
* Gross pay = regular pay + overtime pay
* Bonus = 10% of gross pay for the Management department, otherwise 0
* Taxable pay = gross pay + bonus
* Tax = 25% of taxable pay when it is 5000 or more, otherwise 15%
* Net pay = taxable pay - tax

## Functional requirements

| ID | Requirement |
| --- | --- |
| FR1 | Add a new employee with an ID, name, department and base salary. |
| FR2 | Reject registration if the ID or name is empty, the salary is negative, or the ID already exists. |
| FR3 | Remove an employee by ID, and report when the ID does not exist. |
| FR4 | Compute net pay from base salary, overtime, a Management bonus and tax. |
| FR5 | Reject payment if the employee is not registered or if hours or overtime hours are negative. |
| FR6 | Produce a payroll report with each net pay, total payroll, average and top earner, or state that there are no employees. |
| FR7 | Report the number of registered employees. |

## Assumptions

The scenario does not state the overtime multiplier, the bonus percentage, the tax
brackets or the standard number of hours in a month. These were fixed as explicit
assumptions so that they could be tested:

* A standard month is 160 hours and overtime is paid at 1.5 times the hourly rate.
* Management receives a 10% bonus on gross pay.
* Taxable pay (gross pay plus bonus) of 5000 or more is taxed at 25%, below 5000 at 15%.
* The department name is matched without regard to letter case or surrounding spaces.
* A blank department is rejected, by the same rule as a blank ID or name.

## Test coverage

All 20 cases (10 black-box, 10 white-box) pass, each on a freshly created system so the
cases are independent. Every requirement FR1 to FR7 is covered by at least one case.

To check that the suite really detects faults, 13 faults were injected into the source
one at a time and the suite was re-run after each one. 9 of the 13 were detected.

Four faults survived, and each one points at a place where test selection can improve:

| Surviving fault | Why it survived |
| --- | --- |
| Bonus stops ignoring the letter case of the department | Every case spells the department exactly "Management" |
| Removing an employee leaves its payslip behind | No case checks payslip count after a removal |
| Average net pay divides by the wrong count | No case reads the average net pay |
| Top earner comparison uses `>=` instead of `>` | No case creates two equal net pays |

Section 7 of `Test_Case_Report.docx` describes the test that would close each gap.
