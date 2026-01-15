package com.villadictos.app.config;

import com.villadictos.app.model.*;
import com.villadictos.app.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Carga datos iniciales en la base de datos si está vacía.
 * Solo se ejecuta una vez - después los datos persisten.
 */
@Component
@Order(1)
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final TipoHabitacionRepository tipoHabitacionRepository;
    private final HabitacionRepository habitacionRepository;
    private final SalaRepository salaRepository;
    private final TemporadaRepository temporadaRepository;
    private final ModeloReservaRepository modeloReservaRepository;
    private final ServicioRepository servicioRepository;
    private final ReservaRepository reservaRepository;
    private final BloqueoRepository bloqueoRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(
            UsuarioRepository usuarioRepository,
            TipoHabitacionRepository tipoHabitacionRepository,
            HabitacionRepository habitacionRepository,
            SalaRepository salaRepository,
            TemporadaRepository temporadaRepository,
            ModeloReservaRepository modeloReservaRepository,
            ServicioRepository servicioRepository,
            ReservaRepository reservaRepository,
            BloqueoRepository bloqueoRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.tipoHabitacionRepository = tipoHabitacionRepository;
        this.habitacionRepository = habitacionRepository;
        this.salaRepository = salaRepository;
        this.temporadaRepository = temporadaRepository;
        this.modeloReservaRepository = modeloReservaRepository;
        this.servicioRepository = servicioRepository;
        this.reservaRepository = reservaRepository;
        this.bloqueoRepository = bloqueoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Solo cargar datos si la base de datos está vacía
        if (usuarioRepository.count() > 0) {
            System.out.println("====== DataSeeder: Base de datos ya tiene datos, saltando seed ======");
            return;
        }

        System.out.println("====== DataSeeder: Cargando datos iniciales... ======");

        // 1. USUARIOS
        String hashedPassword = passwordEncoder.encode("password");

        Usuario admin = new Usuario();
        admin.setNombre("Admin Webmaster");
        admin.setEmail("admin@hotel.com");
        admin.setPasswordHash(hashedPassword);
        admin.setRol(Usuario.Rol.webmaster);
        admin.setTelefono("600111222");
        usuarioRepository.save(admin);

        Usuario recepcion = new Usuario();
        recepcion.setNombre("Laura Recepción");
        recepcion.setEmail("recepcion@hotel.com");
        recepcion.setPasswordHash(hashedPassword);
        recepcion.setRol(Usuario.Rol.recepcion);
        recepcion.setTelefono("600333444");
        usuarioRepository.save(recepcion);

        Usuario cliente1 = new Usuario();
        cliente1.setNombre("Juan Cliente");
        cliente1.setEmail("juan@gmail.com");
        cliente1.setPasswordHash(hashedPassword);
        cliente1.setRol(Usuario.Rol.cliente);
        cliente1.setTelefono("600555666");
        usuarioRepository.save(cliente1);

        Usuario cliente2 = new Usuario();
        cliente2.setNombre("María García");
        cliente2.setEmail("maria@gmail.com");
        cliente2.setPasswordHash(hashedPassword);
        cliente2.setRol(Usuario.Rol.cliente);
        cliente2.setTelefono("600777888");
        usuarioRepository.save(cliente2);

        System.out.println("  ✓ Usuarios creados");

        // 2. TIPOS DE HABITACIÓN
        TipoHabitacion individual = crearTipo("Individual", "Habitación con cama individual", 1,
                new BigDecimal("50.00"));
        TipoHabitacion dobleStd = crearTipo("Doble Estándar", "Habitación con cama doble", 2, new BigDecimal("80.00"));
        TipoHabitacion dobleSup = crearTipo("Doble Superior", "Habitación espaciosa", 2, new BigDecimal("110.00"));
        TipoHabitacion suiteJr = crearTipo("Suite Junior", "Suite con salón", 3, new BigDecimal("150.00"));
        TipoHabitacion suitePrem = crearTipo("Suite Premium", "Suite de lujo", 4, new BigDecimal("250.00"));
        System.out.println("  ✓ Tipos de habitación creados");

        // 3. HABITACIONES
        crearHabitacion("101", 1, "Calle", individual, false);
        crearHabitacion("102", 1, "Jardín", dobleStd, false);
        Habitacion h201 = crearHabitacion("201", 2, "Mar", dobleSup, true);
        Habitacion h202 = crearHabitacion("202", 2, "Mar", dobleSup, true);
        Habitacion h301 = crearHabitacion("301", 3, "Mar", suiteJr, true);
        Habitacion h303 = crearHabitacion("303", 3, "Mar", suitePrem, true);
        System.out.println("  ✓ Habitaciones creadas");

        // 4. SALAS
        Sala salaGran = crearSala("Gran Conferencia", 100, "Sala principal", "Proyector 4K, Sonido",
                new BigDecimal("500.00"));
        crearSala("Ejecutiva", 20, "Sala corporativa", "TV 65\", Videoconferencia", new BigDecimal("200.00"));
        crearSala("Privada", 10, "Sala pequeña", "TV 50\", WiFi", new BigDecimal("150.00"));
        System.out.println("  ✓ Salas creadas");

        // 5. TEMPORADAS
        Temporada tempBaja = crearTemporada("Temporada Baja", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 5, 31),
                new BigDecimal("1.00"));
        crearTemporada("Temporada Media", LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 30), new BigDecimal("1.25"));
        crearTemporada("Temporada Alta", LocalDate.of(2025, 7, 1), LocalDate.of(2025, 9, 15), new BigDecimal("1.50"));
        Temporada navidad = crearTemporada("Navidad", LocalDate.of(2025, 12, 21), LocalDate.of(2026, 1, 6),
                new BigDecimal("1.80"));
        System.out.println("  ✓ Temporadas creadas");

        // 6. MODELOS DE RESERVA
        ModeloReserva soloAloj = crearModelo("Solo Alojamiento", "Sin comidas", new BigDecimal("0.00"));
        ModeloReserva alojDesay = crearModelo("Alojamiento y Desayuno", "Incluye desayuno", new BigDecimal("15.00"));
        ModeloReserva mediaPen = crearModelo("Media Pensión", "Incluye desayuno y cena", new BigDecimal("35.00"));
        ModeloReserva pensComp = crearModelo("Pensión Completa", "Incluye desayuno, comida y cena",
                new BigDecimal("50.00"));
        System.out.println("  ✓ Modelos de reserva creados");

        // 7. SERVICIOS
        crearServicio("SPA - Masaje", "Masaje 60 minutos", new BigDecimal("60.00"), Servicio.TipoServicio.POR_USO);
        crearServicio("Gimnasio", "Acceso al gimnasio", new BigDecimal("10.00"), Servicio.TipoServicio.POR_DIA);
        crearServicio("Parking", "Plaza cubierta", new BigDecimal("15.00"), Servicio.TipoServicio.POR_DIA);
        crearServicio("Restaurante", "Comida", new BigDecimal("20.00"), Servicio.TipoServicio.POR_RESERVA);
        crearServicio("Transfer", "Aeropuerto", new BigDecimal("40.00"), Servicio.TipoServicio.POR_RESERVA);
        System.out.println("  ✓ Servicios creados");

        // 8. RESERVAS DE EJEMPLO
        crearReserva(cliente1, h201, null, mediaPen, navidad,
                LocalDate.of(2025, 12, 28), LocalDate.of(2026, 1, 2), 2, new BigDecimal("635.00"),
                Reserva.EstadoReserva.confirmada);
        crearReserva(cliente2, h303, null, pensComp, navidad,
                LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 10), 2, new BigDecimal("2065.00"),
                Reserva.EstadoReserva.confirmada);
        crearReserva(cliente1, null, salaGran, null, tempBaja,
                LocalDate.of(2026, 1, 15), LocalDate.of(2026, 1, 16), 80, new BigDecimal("500.00"),
                Reserva.EstadoReserva.confirmada);
        System.out.println("  ✓ Reservas creadas");

        // 9. BLOQUEOS
        crearBloqueo(h201, null, LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 12), "Limpieza profunda", recepcion);
        System.out.println("  ✓ Bloqueos creados");

        System.out.println("====== DataSeeder: Datos iniciales cargados correctamente ======");
    }

    private TipoHabitacion crearTipo(String nombre, String desc, int capacidad, BigDecimal precio) {
        TipoHabitacion tipo = new TipoHabitacion();
        tipo.setNombre(nombre);
        tipo.setDescripcion(desc);
        tipo.setCapacidadPersonas(capacidad);
        tipo.setPrecioBaseNoche(precio);
        return tipoHabitacionRepository.save(tipo);
    }

    private Habitacion crearHabitacion(String numero, int planta, String vistas, TipoHabitacion tipo,
            boolean destacada) {
        Habitacion h = new Habitacion();
        h.setNumeroHabitacion(numero);
        h.setPlanta(planta);
        h.setVistas(vistas);
        h.setTipoHabitacion(tipo);
        h.setDestacada(destacada);
        return habitacionRepository.save(h);
    }

    private Sala crearSala(String nombre, int aforo, String desc, String equip, BigDecimal precio) {
        Sala s = new Sala();
        s.setNombre(nombre);
        s.setAforoMax(aforo);
        s.setDescripcion(desc);
        s.setEquipamiento(equip);
        s.setPrecioBaseDia(precio);
        return salaRepository.save(s);
    }

    private Temporada crearTemporada(String nombre, LocalDate inicio, LocalDate fin, BigDecimal factor) {
        Temporada t = new Temporada();
        t.setNombre(nombre);
        t.setFechaInicio(inicio);
        t.setFechaFin(fin);
        t.setFactorPrecio(factor);
        return temporadaRepository.save(t);
    }

    private ModeloReserva crearModelo(String nombre, String desc, BigDecimal precio) {
        ModeloReserva m = new ModeloReserva();
        m.setNombre(nombre);
        m.setDescripcion(desc);
        m.setPrecioAdicionalNoche(precio);
        return modeloReservaRepository.save(m);
    }

    private void crearServicio(String nombre, String desc, BigDecimal precio, Servicio.TipoServicio tipo) {
        Servicio s = new Servicio();
        s.setNombre(nombre);
        s.setDescripcion(desc);
        s.setPrecio(precio);
        s.setTipo(tipo);
        servicioRepository.save(s);
    }

    private Reserva crearReserva(Usuario usuario, Habitacion hab, Sala sala, ModeloReserva modelo,
            Temporada temp, LocalDate inicio, LocalDate fin, int personas,
            BigDecimal precio, Reserva.EstadoReserva estado) {
        Reserva r = new Reserva();
        r.setUsuario(usuario);
        r.setHabitacion(hab);
        r.setSala(sala);
        r.setModeloReserva(modelo);
        r.setTemporada(temp);
        r.setFechaInicio(inicio);
        r.setFechaFin(fin);
        r.setNumPersonas(personas);
        r.setPrecioTotal(precio);
        r.setEstado(estado);
        return reservaRepository.save(r);
    }

    private void crearBloqueo(Habitacion hab, Sala sala, LocalDate inicio, LocalDate fin, String motivo,
            Usuario creador) {
        Bloqueo b = new Bloqueo();
        b.setHabitacion(hab);
        b.setSala(sala);
        b.setFechaInicio(inicio);
        b.setFechaFin(fin);
        b.setMotivo(motivo);
        b.setUsuarioCreador(creador);
        bloqueoRepository.save(b);
    }
}
