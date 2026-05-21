package edu.dosw.users.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Documento MongoDB que almacena la fotografía de un jugador.
 *
 * <p>Se persiste en la colección {@code player_photos} de MongoDB Atlas.
 * El campo {@link #id} generado por MongoDB es el valor almacenado en
 * {@code SportProfileEntity#photoId}, actuando como referencia cruzada entre
 * la base de datos relacional y el almacén de documentos.</p>
 *
 * @author CodeForge
 * @since 1.0
 */
@Document(collection = "player_photos")
@Getter
@Setter
@NoArgsConstructor
public class PlayerPhoto {

    /** Identificador único del documento en MongoDB (ObjectId como String). */
    @Id
    private String id;

    /** Identificador del perfil deportivo propietario de esta fotografía. */
    private Long sportProfileId;

    /** Tipo MIME de la imagen (p. ej. {@code "image/jpeg"}, {@code "image/png"}). */
    private String contentType;

    /** Contenido binario de la imagen. */
    private byte[] data;

    /** Fecha y hora en que se subió la fotografía. */
    private LocalDateTime uploadedAt;
}