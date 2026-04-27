package edu.dosw.users.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserProfileModelTest {

    @Test
    void isActive_returnsTrue_whenStatusIsACTIVE() {
        UserProfileModel model = UserProfileModel.builder().status("ACTIVE").build();
        assertTrue(model.isActive());
    }

    @Test
    void isActive_returnsTrue_whenStatusIsLowercase() {
        UserProfileModel model = UserProfileModel.builder().status("active").build();
        assertTrue(model.isActive());
    }

    @Test
    void isActive_returnsFalse_whenStatusIsINACTIVE() {
        UserProfileModel model = UserProfileModel.builder().status("INACTIVE").build();
        assertFalse(model.isActive());
    }

    @Test
    void isActive_returnsFalse_whenStatusIsNull() {
        UserProfileModel model = UserProfileModel.builder().status(null).build();
        assertFalse(model.isActive());
    }

    @Test
    void getAge_returnsCorrectAge() {
        LocalDate birthDate = LocalDate.now().minusYears(20);
        UserProfileModel model = UserProfileModel.builder().birthDate(birthDate).build();
        assertEquals(20, model.getAge());
    }

    @Test
    void getAge_returnsZero_whenBirthDateIsNull() {
        UserProfileModel model = UserProfileModel.builder().build();
        assertEquals(0, model.getAge());
    }

    @Test
    void getAge_returnsZero_whenBornToday() {
        UserProfileModel model = UserProfileModel.builder().birthDate(LocalDate.now()).build();
        assertEquals(0, model.getAge());
    }
}