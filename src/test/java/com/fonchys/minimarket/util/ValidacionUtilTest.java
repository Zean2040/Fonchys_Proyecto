package com.fonchys.minimarket.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ValidacionUtil - Pruebas unitarias")
class ValidacionUtilTest {

    @Test
    @DisplayName("limpiarTexto: debe eliminar espacios extra")
    void limpiarTexto_debeNormalizarEspacios() {
        String resultado = ValidacionUtil.limpiarTexto("  Coca   Cola  ");
        assertThat(resultado).isEqualTo("Coca Cola");
    }

    @Test
    @DisplayName("limpiarTexto: debe retornar vacío con null o blank")
    void limpiarTexto_conBlank_debeRetornarVacio() {
        assertThat(ValidacionUtil.limpiarTexto("")).isEmpty();
        assertThat(ValidacionUtil.limpiarTexto("   ")).isEmpty();
    }

    @Test
    @DisplayName("esNumeroValido: debe validar números correctamente")
    void esNumeroValido_conNumeroValido_debeRetornarTrue() {
        assertThat(ValidacionUtil.esNumeroValido("2.50")).isTrue();
        assertThat(ValidacionUtil.esNumeroValido("100")).isTrue();
    }

    @Test
    @DisplayName("esNumeroValido: debe rechazar texto no numérico")
    void esNumeroValido_conTexto_debeRetornarFalse() {
        assertThat(ValidacionUtil.esNumeroValido("abc")).isFalse();
        assertThat(ValidacionUtil.esNumeroValido("")).isFalse();
    }

    @Test
    @DisplayName("parsearDecimal: debe convertir string a BigDecimal")
    void parsearDecimal_conValorValido_debeRetornarBigDecimal() {
        BigDecimal resultado = ValidacionUtil.parsearDecimal("2.50");
        assertThat(resultado).isEqualByComparingTo(new BigDecimal("2.50"));
    }

    @Test
    @DisplayName("parsearDecimal: debe retornar cero con valor inválido")
    void parsearDecimal_conValorInvalido_debeRetornarCero() {
        BigDecimal resultado = ValidacionUtil.parsearDecimal("abc");
        assertThat(resultado).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("validarNoNulo: debe lanzar excepción con nulo")
    void validarNoNulo_conNulo_debeLanzarExcepcion() {
        assertThatThrownBy(() -> ValidacionUtil.validarNoNulo(null, "El valor es nulo"))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("validarNoVacio: debe lanzar excepción con string vacío")
    void validarNoVacio_conVacio_debeLanzarExcepcion() {
        assertThatThrownBy(() -> ValidacionUtil.validarNoVacio("", "El valor está vacío"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("getRolesDisponibles: debe retornar lista inmutable con roles")
    void getRolesDisponibles_debeRetornarRolesCorrectos() {
        List<String> roles = ValidacionUtil.getRolesDisponibles();
        assertThat(roles).containsExactly("ADMIN", "CAJERO");
        assertThatThrownBy(() -> roles.add("OTRO"))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("capitalizarNombre: debe capitalizar correctamente")
    void capitalizarNombre_debeCapitalizarPrimerLetra() {
        assertThat(ValidacionUtil.capitalizarNombre("JUAN PEREZ")).isEqualTo("Juan perez");
        assertThat(ValidacionUtil.capitalizarNombre("")).isEmpty();
    }

    @Test
    @DisplayName("esEmailValido: debe validar formato de email")
    void esEmailValido_conFormatoValido_debeRetornarTrue() {
        assertThat(ValidacionUtil.esEmailValido("admin@fonchys.com")).isTrue();
        assertThat(ValidacionUtil.esEmailValido("noemail")).isFalse();
        assertThat(ValidacionUtil.esEmailValido("")).isFalse();
    }
}
