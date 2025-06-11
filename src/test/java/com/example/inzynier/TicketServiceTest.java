package com.example.inzynier;

import com.example.inzynier.repositories.CoachRepository;
import com.example.inzynier.repositories.TrainingRepository;
import com.example.inzynier.services.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class TicketServiceTest {

    @InjectMocks
    private TicketService classUnderTest;
    @Mock
    private CoachRepository coachRepository;
    @Mock
    private TrainingRepository trainingRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testValidateTrainingInfo_TrainingTooFarInFuture() {
        // Given
//        final Long coachId = 2L;
//        final String trainingDate = LocalDateTime.now().plusMonths(2).format(formatter);
//
//        // When
//        Long result = classUnderTest.validateTrainingInfo(coachId, trainingDate);
//
//        // Then
//        assertEquals(0L, result, "The training should not be valid if it is more than one month in the future.");
    }

    @Test
    void testValidateTrainingInfo_CoachIsAvailable() {
        // Given
//        final Long coachId = 2L;
//        final String trainingDate = formatter.format(LocalDateTime.now().with(DayOfWeek.MONDAY).plusWeeks(1).withHour(5));
//
//        // When
//        Long result = classUnderTest.validateTrainingInfo(coachId, trainingDate);
//
//        // Then
//        assertEquals(0L, result, "The training should be valid");
    }


}
