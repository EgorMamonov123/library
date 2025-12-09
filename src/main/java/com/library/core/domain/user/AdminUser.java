package com.library.core.domain.user;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Пользователь с правами администратора
 */
@Entity
@DiscriminatorValue("ADMIN")
public class AdminUser extends User {

    public AdminUser() {
        super();
    }

    public AdminUser(String username, String password, String email,
                     String firstName, String lastName) {
        super(username, password, email, firstName, lastName, UserRole.ADMINISTRATOR);
    }

    @Override
    public boolean canPerformAction(String action) {
        // Администратор может выполнять любые действия
        return true;
    }

    @Override
    public String getDisplayName() {
        return getFirstName() + " " + getLastName() + " (Администратор)";
    }

    // Дополнительные методы для администратора
    public void unlockUserAccount(User user) {
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);
    }

    public void deactivateUser(User user) {
        user.setActive(false);
    }

    public void activateUser(User user) {
        user.setActive(true);
    }

    public void forcePasswordReset(User user) {
        user.setPasswordResetRequired(true);
    }
}