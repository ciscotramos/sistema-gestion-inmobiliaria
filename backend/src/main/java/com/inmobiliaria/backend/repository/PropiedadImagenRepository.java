package com.inmobiliaria.backend.repository;

import com.inmobiliaria.backend.model.PropiedadImagen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropiedadImagenRepository extends JpaRepository<PropiedadImagen, Integer> {

    List<PropiedadImagen> findByPropiedadIdOrderByOrdenAscIdAsc(Integer propiedadId);

    Optional<PropiedadImagen> findByPropiedadIdAndId(Integer propiedadId, Integer id);

    Optional<PropiedadImagen> findByPropiedadIdAndEsPrincipalTrue(Integer propiedadId);
}
