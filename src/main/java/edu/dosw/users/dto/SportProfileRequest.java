package edu.dosw.users.dto;

import edu.dosw.users.enums.Position;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Payload accepted for sport profile creation and update requests.
 *
 * <p>The {@code userId} and {@code photoId} are resolved by the service
 * layer (from the path variable and the uploaded file respectively) and
 * are therefore not part of this request body.</p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SportProfileRequest {

    private Position position;
    private Integer dorsalNumber;
    private boolean available;
}