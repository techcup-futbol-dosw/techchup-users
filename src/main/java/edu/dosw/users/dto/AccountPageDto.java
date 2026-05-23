package edu.dosw.users.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Envoltorio para la respuesta paginada de {@code GET /accounts} del Identity Service.
 *
 * <p>Mapea {@code AccountAdminPageResponse} (Identity) sin depender directamente de
 * ese tipo.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountPageDto {

    private List<AccountDto> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
