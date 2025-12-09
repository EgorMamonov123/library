package com.library.core.domain.user;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Пользователь с правами библиотекаря
 */
@Entity
@DiscriminatorValue("LIBRARIAN")
public class LibrarianUser extends User {

    @Column(name = "employee_id", unique = true)
    private String employeeId;

    @Column(name = "department")
    private String department;

    @Column(name = "work_schedule")
    private String workSchedule;

    public LibrarianUser() {
        super();
    }

    public LibrarianUser(String username, String password, String email,
                         String firstName, String lastName, String employeeId) {
        super(username, password, email, firstName, lastName, UserRole.LIBRARIAN);
        this.employeeId = employeeId;
    }

    @Override
    public boolean canPerformAction(String action) {
        switch (action) {
            case "manage_books":
            case "manage_readers":
            case "process_loans":
            case "view_statistics":
            case "reserve_books":
            case "manage_fines":
                return true;
            case "manage_users":
            case "system_settings":
                return false; // Только администратор
            default:
                return false;
        }
    }

    @Override
    public String getDisplayName() {
        return getFirstName() + " " + getLastName() + " (Библиотекарь)";
    }

    // Дополнительные методы для библиотекаря
    public boolean canOverrideDueDate() {
        return true;
    }

    public boolean canWaiveFines() {
        return true;
    }

    public boolean canManageReservations() {
        return true;
    }

    // Getters and Setters
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getWorkSchedule() { return workSchedule; }
    public void setWorkSchedule(String workSchedule) { this.workSchedule = workSchedule; }
}