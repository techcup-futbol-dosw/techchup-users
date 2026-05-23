package edu.dosw.users.client;

import edu.dosw.users.dto.AccountDto;

import java.util.List;

/**
 * Cliente hacia el Identity Service (consumido a través del API Gateway).
 *
 * <p>Todas las llamadas propagan el JWT del request actual, por lo que las
 * restricciones de autorización del Identity Service se aplican normalmente.</p>
 *
 * <p>Cuando el API Gateway no está disponible (entorno local / tests), la
 * implementación por defecto definida en {@code FallbackBeansConfig} retorna
 * {@code null} / lista vacía sin lanzar excepción.</p>
 */
public interface IdentityServiceClient {

    /**
     * Obtiene una cuenta por su identificador.
     *
     * @param id identificador de la cuenta en Identity Service
     * @return datos de la cuenta, o {@code null} si no existe o hay error
     */
    AccountDto getAccountById(Long id);

    /**
     * Retorna todas las cuentas registradas (paginación interna de 500 por llamada).
     *
     * <p>Requiere permiso {@code account:read:any} (rol ADMIN). Si el token del
     * request no lo tiene, Identity devuelve 403 y este método retorna lista vacía.</p>
     *
     * @return lista de cuentas, o lista vacía si hay error o sin permisos
     */
    List<AccountDto> getAllAccounts();
}
