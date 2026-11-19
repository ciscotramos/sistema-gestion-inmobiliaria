package com.inmobiliaria.backend.monitoring;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

/** Cuerpo JSON que envía Alertmanager a un receptor webhook (versión 4 del formato). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AlertmanagerPayload(String status, String receiver, List<Alerta> alerts) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Alerta(String status,
                         Map<String, String> labels,
                         Map<String, String> annotations,
                         String startsAt,
                         String fingerprint) {

        public String etiqueta(String clave) {
            return labels == null ? null : labels.get(clave);
        }

        public String anotacion(String clave) {
            return annotations == null ? null : annotations.get(clave);
        }
    }
}
