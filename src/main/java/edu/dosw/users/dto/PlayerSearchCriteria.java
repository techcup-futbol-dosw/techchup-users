package edu.dosw.users.dto;

import edu.dosw.users.enums.Gender;
import edu.dosw.users.enums.Position;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Optional filters used to search for players.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerSearchCriteria {

    private Position position;
    private Integer ageMin;
    private Integer ageMax;
    private Gender gender;
    private String name;
    private String identification;
    private Integer semester;
}
