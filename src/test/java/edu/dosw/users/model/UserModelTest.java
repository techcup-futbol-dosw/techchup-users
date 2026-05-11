package edu.dosw.users.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the business methods of {@link UserModel}.
 *
 * <p>Verifies the behaviour of {@link UserModel#isActive()} for
 * different values of the {@code status} field (uppercase, lowercase, null)
 * and of {@link UserModel#getAge()} for different birth dates,
 * including a null date and a birth date set to today.</p>
 */
class UserModelTest {

    /**
     * Verifies that {@code isActive} returns {@code true} when the status
     * is {@code "ACTIVE"} in uppercase.
     */
    @Test
    void isActive_returnsTrue_whenStatusIsACTIVE() {
        UserModel model = UserModel.builder().status("ACTIVE").build();
        assertTrue(model.isActive());
    }

    /**
     * Verifies that {@code isActive} is case-insensitive and returns
     * {@code true} when the status is {@code "active"} in lowercase.
     */
    @Test
    void isActive_returnsTrue_whenStatusIsLowercase() {
        UserModel model = UserModel.builder().status("active").build();
        assertTrue(model.isActive());
    }

    /**
     * Verifies that {@code isActive} returns {@code false} when the status
     * is {@code "INACTIVE"}.
     */
    @Test
    void isActive_returnsFalse_whenStatusIsINACTIVE() {
        UserModel model = UserModel.builder().status("INACTIVE").build();
        assertFalse(model.isActive());
    }

    /**
     * Verifies that {@code isActive} returns {@code false} when the status
     * is {@code null}.
     */
    @Test
    void isActive_returnsFalse_whenStatusIsNull() {
        UserModel model = UserModel.builder().status(null).build();
        assertFalse(model.isActive());
    }

    /**
     * Verifies that {@code getAge} correctly calculates the number of full
     * years elapsed since the birth date.
     */
    @Test
    void getAge_returnsCorrectAge() {
        LocalDate birthDate = LocalDate.now().minusYears(20);
        UserModel model = UserModel.builder().birthDate(birthDate).build();
        assertEquals(20, model.getAge());
    }

    /**
     * Verifies that {@code getAge} returns {@code 0} when {@code birthDate}
     * is {@code null}.
     */
    @Test
    void getAge_returnsZero_whenBirthDateIsNull() {
        UserModel model = UserModel.builder().build();
        assertEquals(0, model.getAge());
    }

    /**
     * Verifies that {@code getAge} returns {@code 0} when the user was born
     * today (zero full years elapsed).
     */
    @Test
    void getAge_returnsZero_whenBornToday() {
        UserModel model = UserModel.builder().birthDate(LocalDate.now()).build();
        assertEquals(0, model.getAge());
    }
}