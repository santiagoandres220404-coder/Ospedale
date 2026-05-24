package packagee;

import java.time.LocalDate;

public class UserController {
    private final HospitalStore store;

    public UserController() {
        store = HospitalStore.getInstance();
        store.loadUsersFromJson();
    }

    public Response login(String username, String password) {
        User user = store.findUserByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            return Response.error(StatusCode.UNAUTHORIZED, "Usuario o contrasena incorrectos.");
        }
        return Response.ok("Login exitoso.", store.serializeUser(user).toString());
    }

    public Response registerPatient(long id, String username, String firstname, String lastname,
            String password, String confirmation, String email, String birthdate, boolean gender,
            long phone, String address) {
        Response validation = validatePatient(id, username, password, confirmation, email, birthdate, phone, null);
        if (!validation.isSuccess()) {
            return validation;
        }
        Patient patient = new Patient(id, username, firstname, lastname, password, email,
                LocalDate.parse(birthdate), gender, phone, address);
        store.getUsers().add(patient);
        store.notifyObservers();
        return Response.created("Paciente registrado.", store.serializeUser(patient).toString());
    }

    public Response registerPatient(String id, String username, String firstname, String lastname,
            String password, String confirmation, String email, String birthdate, boolean gender,
            String phone, String address) {
        Long parsedId = parseLong(id, "El id debe ser numerico.");
        Long parsedPhone = parseLong(phone, "El telefono debe ser numerico.");
        if (parsedId == null || parsedPhone == null) {
            return Response.error(StatusCode.BAD_REQUEST, parsedId == null ? "El id debe ser numerico." : "El telefono debe ser numerico.");
        }
        return registerPatient(parsedId, username, firstname, lastname, password, confirmation, email,
                birthdate, gender, parsedPhone, address);
    }

    public Response updatePatient(long id, String username, String firstname, String lastname,
            String password, String confirmation, String email, String birthdate, boolean gender,
            long phone, String address) {
        Patient patient = store.findPatient(id);
        if (patient == null) {
            return Response.error(StatusCode.NOT_FOUND, "Paciente no encontrado.");
        }
        Response validation = validatePatient(id, username, password, confirmation, email, birthdate, phone, patient);
        if (!validation.isSuccess()) {
            return validation;
        }
        patient.setUsername(username);
        patient.setFirstname(firstname);
        patient.setLastname(lastname);
        patient.setPassword(password);
        patient.setEmail(email);
        patient.setBirthdate(LocalDate.parse(birthdate));
        patient.setGender(gender);
        patient.setPhone(phone);
        patient.setAddress(address);
        store.notifyObservers();
        return Response.ok("Paciente actualizado.", store.serializeUser(patient).toString());
    }

    public Response updatePatient(String id, String username, String firstname, String lastname,
            String password, String confirmation, String email, String birthdate, boolean gender,
            String phone, String address) {
        Long parsedId = parseLong(id, "El id debe ser numerico.");
        Long parsedPhone = parseLong(phone, "El telefono debe ser numerico.");
        if (parsedId == null || parsedPhone == null) {
            return Response.error(StatusCode.BAD_REQUEST, parsedId == null ? "El id debe ser numerico." : "El telefono debe ser numerico.");
        }
        return updatePatient(parsedId, username, firstname, lastname, password, confirmation, email,
                birthdate, gender, parsedPhone, address);
    }

    public Response registerDoctor(long id, String username, String firstname, String lastname,
            String password, String confirmation, Specialty specialty, String licenceNumber,
            String assignedOffice) {
        Response validation = validateDoctor(id, username, password, confirmation, licenceNumber, assignedOffice, null);
        if (!validation.isSuccess()) {
            return validation;
        }
        Doctor doctor = new Doctor(id, username, firstname, lastname, password, specialty, licenceNumber, assignedOffice);
        store.getUsers().add(doctor);
        store.notifyObservers();
        return Response.created("Doctor registrado.", store.serializeUser(doctor).toString());
    }

    public Response registerDoctor(String id, String username, String firstname, String lastname,
            String password, String confirmation, Specialty specialty, String licenceNumber,
            String assignedOffice) {
        Long parsedId = parseLong(id, "El id debe ser numerico.");
        if (parsedId == null) {
            return Response.error(StatusCode.BAD_REQUEST, "El id debe ser numerico.");
        }
        return registerDoctor(parsedId, username, firstname, lastname, password, confirmation, specialty,
                licenceNumber, assignedOffice);
    }

    public Response registerDoctor(String id, String username, String firstname, String lastname,
            String password, String confirmation, String specialty, String licenceNumber,
            String assignedOffice) {
        Specialty parsedSpecialty = parseSpecialty(specialty);
        if (parsedSpecialty == null) {
            return Response.error(StatusCode.BAD_REQUEST, "Debe seleccionar una especialidad valida.");
        }
        return registerDoctor(id, username, firstname, lastname, password, confirmation, parsedSpecialty,
                licenceNumber, assignedOffice);
    }

    public Response updateDoctor(long id, String username, String firstname, String lastname,
            String password, String confirmation, Specialty specialty, String licenceNumber,
            String assignedOffice) {
        Doctor doctor = store.findDoctor(id);
        if (doctor == null) {
            return Response.error(StatusCode.NOT_FOUND, "Doctor no encontrado.");
        }
        Response validation = validateDoctor(id, username, password, confirmation, licenceNumber, assignedOffice, doctor);
        if (!validation.isSuccess()) {
            return validation;
        }
        doctor.setUsername(username);
        doctor.setFirstname(firstname);
        doctor.setLastname(lastname);
        doctor.setPassword(password);
        doctor.setSpecialty(specialty);
        doctor.setLicenceNumber(licenceNumber);
        doctor.setAssignedOffice(assignedOffice);
        store.notifyObservers();
        return Response.ok("Doctor actualizado.", store.serializeUser(doctor).toString());
    }

    public Response updateDoctor(String id, String username, String firstname, String lastname,
            String password, String confirmation, String specialty, String licenceNumber,
            String assignedOffice) {
        Long parsedId = parseLong(id, "El id debe ser numerico.");
        Specialty parsedSpecialty = parseSpecialty(specialty);
        if (parsedId == null) {
            return Response.error(StatusCode.BAD_REQUEST, "El id debe ser numerico.");
        }
        if (parsedSpecialty == null) {
            return Response.error(StatusCode.BAD_REQUEST, "Debe seleccionar una especialidad valida.");
        }
        return updateDoctor(parsedId, username, firstname, lastname, password, confirmation, parsedSpecialty,
                licenceNumber, assignedOffice);
    }

    private Response validatePatient(long id, String username, String password, String confirmation,
            String email, String birthdate, long phone, User currentUser) {
        Response common = validateCommonUser(id, username, password, confirmation, currentUser);
        if (!common.isSuccess()) {
            return common;
        }
        if (!Validation.isValidPhone(phone)) {
            return Response.error(StatusCode.BAD_REQUEST, "El telefono debe tener exactamente 10 digitos.");
        }
        if (!Validation.isValidEmail(email)) {
            return Response.error(StatusCode.BAD_REQUEST, "El email debe seguir el formato XXXXX@XXXXX.com.");
        }
        if (Validation.parseDate(birthdate) == null) {
            return Response.error(StatusCode.BAD_REQUEST, "La fecha debe seguir el formato AAAA-MM-DD.");
        }
        return Response.ok("Validacion exitosa.", "{}");
    }

    private Response validateDoctor(long id, String username, String password, String confirmation,
            String licenceNumber, String assignedOffice, User currentUser) {
        Response common = validateCommonUser(id, username, password, confirmation, currentUser);
        if (!common.isSuccess()) {
            return common;
        }
        if (!Validation.isValidLicence(licenceNumber)) {
            return Response.error(StatusCode.BAD_REQUEST, "La licencia debe seguir el formato L-XXXXXXXXXX MTL.");
        }
        if (!Validation.isValidOffice(assignedOffice)) {
            return Response.error(StatusCode.BAD_REQUEST, "La oficina debe seguir el formato O-XXX.");
        }
        return Response.ok("Validacion exitosa.", "{}");
    }

    private Response validateCommonUser(long id, String username, String password, String confirmation, User currentUser) {
        if (!Validation.isValidUserId(id)) {
            return Response.error(StatusCode.BAD_REQUEST, "El id debe ser mayor que 0 y tener 12 digitos.");
        }
        if (!password.equals(confirmation)) {
            return Response.error(StatusCode.BAD_REQUEST, "La contrasena y la confirmacion no coinciden.");
        }
        User existingId = store.findUserById(id);
        if (existingId != null && existingId != currentUser) {
            return Response.error(StatusCode.CONFLICT, "Ya existe un usuario con ese id.");
        }
        User existingUsername = store.findUserByUsername(username);
        if (existingUsername != null && existingUsername != currentUser) {
            return Response.error(StatusCode.CONFLICT, "Ya existe un usuario con ese nombre de usuario.");
        }
        return Response.ok("Validacion exitosa.", "{}");
    }

    private Long parseLong(String value, String message) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Specialty parseSpecialty(String value) {
        try {
            return store.parseSpecialty(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
