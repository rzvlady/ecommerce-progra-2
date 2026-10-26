package com.sv.grupo1.ecommerce.services;

import com.sv.grupo1.backoffice.repository.RolRepository;
import com.sv.grupo1.ecommerce.dao.MunicipioRepository;
import com.sv.grupo1.ecommerce.dao.UsuarioContactoRepository;
import com.sv.grupo1.ecommerce.dao.UsuarioDireccionRepository;
import com.sv.grupo1.ecommerce.dao.UsuarioPerfilRepository;
import com.sv.grupo1.ecommerce.dao.UsuarioRepository;
import com.sv.grupo1.ecommerce.dto.RegistroUsuarioDTO;
import com.sv.grupo1.ecommerce.dto.UsuarioPerfilCompletoDTO;
import com.sv.grupo1.ecommerce.entities.catalogo.Departamento;
import com.sv.grupo1.ecommerce.entities.catalogo.Municipio;
import com.sv.grupo1.ecommerce.entities.seguridad.Rol;
import com.sv.grupo1.ecommerce.entities.seguridad.Usuario;
import com.sv.grupo1.ecommerce.entities.seguridad.UsuarioContacto;
import com.sv.grupo1.ecommerce.entities.seguridad.UsuarioDireccion;
import com.sv.grupo1.ecommerce.entities.seguridad.UsuarioPerfil;
import com.sv.grupo1.ecommerce.enums.GeneroEnum;
import com.sv.grupo1.ecommerce.enums.TipoContactoEnum;
import com.sv.grupo1.ecommerce.enums.TipoDireccionEnum;
import com.sv.grupo1.ecommerce.services.impl.UsuarioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UsuarioServiceTest {

    private UsuarioRepository usuarioRepository;
    private RolRepository rolRepository;
    private UsuarioPerfilRepository usuarioPerfilRepository;
    private UsuarioContactoRepository usuarioContactoRepository;
    private UsuarioDireccionRepository usuarioDireccionRepository;
    private MunicipioRepository municipioRepository;
    private PasswordEncoder passwordEncoder;
    private UsuarioServiceImpl usuarioService;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        rolRepository = mock(RolRepository.class);
        usuarioPerfilRepository = mock(UsuarioPerfilRepository.class);
        usuarioContactoRepository = mock(UsuarioContactoRepository.class);
        usuarioDireccionRepository = mock(UsuarioDireccionRepository.class);
        municipioRepository = mock(MunicipioRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();

        usuarioService = new UsuarioServiceImpl(
                usuarioRepository,
                rolRepository,
                usuarioPerfilRepository,
                usuarioContactoRepository,
                usuarioDireccionRepository,
                municipioRepository,
                passwordEncoder
        );
    }

    @Test
    void alRegistrarUsuarioCreaContactoCorreoPrincipalConMetadata() {
        RegistroUsuarioDTO dto = new RegistroUsuarioDTO();
        dto.setCorreoInicioSesion("cliente@test.com");
        dto.setContrasenia("123456");

        Rol rolCliente = new Rol();
        rolCliente.setNombre("ROLE_CLIENTE");

        when(usuarioRepository.existsByCorreoInicioSesionIgnoreCase("cliente@test.com")).thenReturn(false);
        when(rolRepository.findByNombre("ROLE_CLIENTE")).thenReturn(Optional.of(rolCliente));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario guardado = usuarioService.registrarUsuario(dto);

        assertNotNull(guardado.getMetadata());
        assertEquals("REGISTRO_WEB", guardado.getMetadata().get("origen"));
        assertNotNull(guardado.getRoles());
        assertNotNull(guardado.getRoles().get(0).getAuditoria());
        assertEquals("asignacion_rol", guardado.getRoles().get(0).getAuditoria().get("operacion"));
        assertNotNull(guardado.getContactos());
        assertEquals(1, guardado.getContactos().size());
        UsuarioContacto contacto = guardado.getContactos().get(0);
        assertEquals(TipoContactoEnum.CORREO.getCodigo(), contacto.getTipoContacto());
        assertTrue(contacto.getPrincipal());
        assertEquals("cliente@test.com", contacto.getValor());
        assertNotNull(contacto.getMetadata());
        assertEquals("REGISTRO_WEB", contacto.getMetadata().get("origen"));
    }

    @Test
    void registrarUltimoInicioSesionActualizaFechaEnRepositorio() {
        when(usuarioRepository.actualizarUltimoInicioSesionPorCorreo(eq("cliente@test.com"), any()))
                .thenReturn(1);

        usuarioService.registrarUltimoInicioSesion("cliente@test.com");

        verify(usuarioRepository, times(1))
                .actualizarUltimoInicioSesionPorCorreo(eq("cliente@test.com"), any());
    }

    @Test
    void guardarPerfilCompletoGarantizaUnSoloMovilPrincipalYRegistraMetadataAuditoria() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(10);
        usuario.setCorreoInicioSesion("cliente@test.com");

        UsuarioContacto movilExistentePrincipal = new UsuarioContacto();
        movilExistentePrincipal.setIdContacto(1);
        movilExistentePrincipal.setUsuario(usuario);
        movilExistentePrincipal.setTipoContacto(TipoContactoEnum.MOVIL.getCodigo());
        movilExistentePrincipal.setValor("7000-0001");
        movilExistentePrincipal.setPrincipal(true);

        UsuarioContacto movilExistenteSecundarioMalMarcado = new UsuarioContacto();
        movilExistenteSecundarioMalMarcado.setIdContacto(2);
        movilExistenteSecundarioMalMarcado.setUsuario(usuario);
        movilExistenteSecundarioMalMarcado.setTipoContacto(TipoContactoEnum.MOVIL.getCodigo());
        movilExistenteSecundarioMalMarcado.setValor("7000-0002");
        movilExistenteSecundarioMalMarcado.setPrincipal(true);

        Departamento depto = new Departamento();
        depto.setIdDepartamento(1);
        depto.setNombreDepartamento("San Salvador");

        Municipio municipio = new Municipio();
        municipio.setIdMunicipio(1);
        municipio.setNombreMunicipio("San Salvador");
        municipio.setDepartamento(depto);

        when(usuarioRepository.findByCorreoInicioSesionIgnoreCase("cliente@test.com")).thenReturn(Optional.of(usuario));
        when(usuarioPerfilRepository.existsByDuiIgnoreCaseAndUsuarioIdUsuarioNot("01234567-8", 10)).thenReturn(false);
        when(usuarioPerfilRepository.findByUsuarioIdUsuario(10)).thenReturn(Optional.empty());
        when(usuarioContactoRepository.findByUsuarioIdUsuarioAndTipoContacto(10, TipoContactoEnum.CORREO.getCodigo()))
                .thenReturn(List.of());
        when(usuarioContactoRepository.findByUsuarioIdUsuarioAndTipoContacto(10, TipoContactoEnum.MOVIL.getCodigo()))
                .thenReturn(List.of(movilExistentePrincipal, movilExistenteSecundarioMalMarcado));
        when(municipioRepository.findById(1)).thenReturn(Optional.of(municipio));
        when(usuarioDireccionRepository.findByUsuarioIdUsuarioOrderByIdDireccionAsc(10)).thenReturn(List.of());

        UsuarioPerfilCompletoDTO dto = new UsuarioPerfilCompletoDTO();
        dto.setNombres("Juan Carlos");
        dto.setApellidos("Pérez");
        dto.setDui("012345678");
        dto.setGenero(GeneroEnum.MASCULINO.getCodigo());
        dto.setFechaNacimiento(LocalDate.of(1995, 5, 15));
        dto.setTelefonoMovil("77778888");
        dto.setTieneWhatsapp(true);
        dto.setIdDepartamento(1);
        dto.setIdMunicipio(1);
        dto.setTipoDireccion(TipoDireccionEnum.CASA.getCodigo());
        dto.setDireccionLinea1("Col. Escalón, Pasaje 3 #14");
        dto.setCodigoPostal("1101");
        dto.setPuntoReferencia("Portón negro frente a parque");

        usuarioService.guardarPerfilCompleto("cliente@test.com", dto, "MI_PERFIL_ECOMMERCE");

        // Verificar Perfil
        ArgumentCaptor<UsuarioPerfil> perfilCaptor = ArgumentCaptor.forClass(UsuarioPerfil.class);
        verify(usuarioPerfilRepository).save(perfilCaptor.capture());
        assertEquals("01234567-8", perfilCaptor.getValue().getDui());
        assertEquals('M', perfilCaptor.getValue().getGenero());

        // Verificar que el móvil secundario se desmarcó como principal y el principal se actualizó
        assertFalse(movilExistenteSecundarioMalMarcado.getPrincipal());
        assertTrue(movilExistentePrincipal.getPrincipal());
        assertEquals("7777-8888", movilExistentePrincipal.getValor());
        assertEquals("cliente@test.com", movilExistentePrincipal.getMetadata().get("modificado_por"));
        assertEquals(true, movilExistentePrincipal.getMetadata().get("tiene_whatsapp"));

        // Verificar Dirección y su metadata de auditoría + punto de referencia
        ArgumentCaptor<UsuarioDireccion> dirCaptor = ArgumentCaptor.forClass(UsuarioDireccion.class);
        verify(usuarioDireccionRepository).save(dirCaptor.capture());
        UsuarioDireccion dirGuardada = dirCaptor.getValue();
        assertEquals('C', dirGuardada.getTipoDireccion());
        assertEquals("Portón negro frente a parque", dirGuardada.getMetadata().get("punto_referencia"));
        assertEquals(true, dirGuardada.getMetadata().get("principal"));
        assertEquals("cliente@test.com", dirGuardada.getMetadata().get("creado_por"));
    }
}
