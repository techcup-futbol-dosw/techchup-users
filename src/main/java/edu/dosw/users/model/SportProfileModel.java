package edu.dosw.users.model;

import edu.dosw.users.enums.Position;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class SportProfileModel {

    private Long id;
    private Long userId;
    private Position position;
    private Integer dorsalNumber;
    private String photoId;
    private boolean available;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}