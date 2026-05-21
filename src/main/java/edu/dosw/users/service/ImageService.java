package edu.dosw.users.service;

import edu.dosw.users.model.PlayerPhoto;
import org.springframework.web.multipart.MultipartFile;

/**
 * Servicio para almacenar, recuperar y eliminar fotos de perfil de jugadores.
 *
 * <p>Las implementaciones son responsables de persistir los bytes de la imagen fuera de
 * la base de datos relacional y retornar un identificador de foto que pueda referenciarse
 * desde el perfil deportivo.</p>
 *
 * @author CodeForge
 * @since 1.0
 */
public interface ImageService {

    /**
     * Almacena la imagen subida para el perfil deportivo indicado.
     *
     * @param file           archivo de imagen recibido del cliente
     * @param sportProfileId identificador del perfil deportivo propietario de la imagen
     * @return identificador generado de la foto almacenada
     * @throws java.io.UncheckedIOException si el archivo no puede leerse
     */
    String upload(MultipartFile file, Long sportProfileId);

    /**
     * Recupera una foto almacenada anteriormente por su identificador.
     *
     * @param photoId identificador de la foto a recuperar
     * @return documento {@link PlayerPhoto}, o {@code null} si no existe
     */
    PlayerPhoto getPhoto(String photoId);

    /**
     * Elimina una foto almacenada anteriormente.
     *
     * @param photoId identificador de la foto a eliminar
     */
    void delete(String photoId);
}
