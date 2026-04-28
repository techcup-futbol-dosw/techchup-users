package edu.dosw.users.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the business methods of {@link UserProfileModel}.
 *
 * <p>Verifies the behaviour of {@link UserProfileModel#isActive()} for
 * different values of the {@code status} field (uppercase, lowercase, null)
 * and of {@link UserProfileModel#getAge()} for different birth dates,
 * including a null date and a birth date set to today.</p>
 */
class UserProfileModelTest {

    /**
     * Verifies that {@code isActive} returns {@code true} when the status
     * is {@code "ACTIVE"} in uppercase.
     */
    @Test
    void isActive_returnsTrue_whenStatusIsACTIVE() {
        UserProfileModel model = UserProfileModel.builder().status("ACTIVE").build();
        assertTrue(model.isActive());
    }

    /**
     * Verifies that {@code isActive} is case-insensitive and returns
     * {@code true} when the status is {@code "active"} in lowercase.
     */
    @Test
    void isActive_returnsTrue_whenStatusIsLowercase() {
        UserProfileModel model = UserProfileModel.builder().status("active").build();
        assertTrue(model.isActive());
    }

    /**
     * Verifies that {@code isActive} returns {@code false} when the status
     * is {@code "INACTIVE"}.
     */
    @Test
    void isActive_returnsFalse_whenStatusIsINACTIVE() {
        UserProfileModel model = UserProfileModel.builder().status("INACTIVE").build();
        assertFalse(model.isActive());
    }

    /**
     * Verifies that {@code isActive} returns {@code false} when the status
     * is {@code null}.
     */
    @Test
    void isActive_returnsFalse_whenStatusIsNull() {
        UserProfileModel model = UserProfileModel.builder().status(null).build();
        assertFalse(model.isActive());
    }

    /**
     * Verifies that {@code getAge} correctly calculates the number of full
     * years elapsed since the birth date.
     */
    @Test
    void getAge_returnsCorrectAge() {
        LocalDate birthDate = LocalDate.now().minusYears(20);
        UserProfileModel model = UserProfileModel.builder().birthDate(birthDate).build();
        assertEquals(20, model.getAge());
    }

    /**
     * Verifies that {@code getAge} returns {@code 0} when {@code birthDate}
     * is {@code null}.
     */
    @Test
    void getAge_returnsZero_whenBirthDateIsNull() {
        UserProfileModel model = UserProfileModel.builder().build();
        assertEquals(0, model.getAge());
    }

    /**
     * Verifies that {@code getAge} returns {@code 0} when the user was born
     * today (zero full years elapsed).
     */
    @Test
    void getAge_returnsZero_whenBornToday() {
        UserProfileModel model = UserProfileModel.builder().birthDate(LocalDate.now()).build();
        assertEquals(0, model.getAge());
    }
}