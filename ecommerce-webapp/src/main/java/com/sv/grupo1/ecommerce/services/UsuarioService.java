package com.sv.grupo1.ecommerce.services;

import com.sv.grupo1.ecommerce.dto.RegistroUsuarioDTO;
import com.sv.grupo1.ecommerce.entities.seguridad.Usuario;

/*
 * @author alexemestica
 * */
public interface UsuarioService {

    Usuario registrarUsuario(RegistroUsuarioDTO dto);

}
