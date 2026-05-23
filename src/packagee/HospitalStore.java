package packagee;

import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class HospitalStore {
    private static final HospitalStore INSTANCE = new HospitalStore();

    private final ArrayList<User> users;
    private final ArrayList<Appointment> appointments;
    private final ArrayList<Hospitalization> hospitalizations;
    private boolean loaded;

    private HospitalStore() {
        users = new ArrayList<>();
        appointments = new ArrayList<>();
        hospitalizations = new ArrayList<>();
    }

    public static HospitalStore getInstance() {
        return INSTANCE;
    }

    public void loadUsersFromJson() {
        if (loaded) {
            return;
        }
        try {
            InputStream input = new FileInputStream("json/users.json");
            JSONObject root = new JSONObject(new JSONTokener(input));
            JSONArray jsonUsers = root.getJSONArray("users");
            for (int i = 0; i < jsonUsers.length(); i++) {
                JSONObject item = jsonUsers.getJSONObject(i);
                addUserFromJson(item);
            }
            loaded = true;
        } catch (Exception ex) {
            users.clear();
            users.add(new Administrator(100000000001L, "admin_root", "Carlos", "Mendoza", "Admin@1234"));
            loaded = true;
        }
    }

    private void addUserFromJson(JSONObject item) {
        String type = item.getString("type");
        long id = item.getLong("id");
        String username = item.getString("username");
        String firstname = item.getString("firstname");
        String lastname = item.getString("lastname");
        String password = item.getString("password");
        if ("admin".equals(type)) {
            users.add(new Administrator(id, username, firstname, lastname, password));
        } else if ("patient".equals(type)) {
            users.add(new Patient(id, username, firstname, lastname, password,
                    item.getString("email"), LocalDate.parse(item.getString("birthdate")),
                    item.getBoolean("gender"), item.getLong("phone"), item.getString("address")));
        } else if ("doctor".equals(type)) {
            users.add(new Doctor(id, username, firstname, lastname, password,
                    parseSpecialty(item.getString("specialty")), item.getString("licenceNumber"),
                    item.getString("assignedOffice")));
        }
    }

    public Specialty parseSpecialty(String value) {
        if ("ORTHOPEDICS".equals(value)) {
            return Specialty.TRAUMATOLOGY_ORTHOPEDICS;
        }
        if ("GYNECOLOGY".equals(value)) {
            return Specialty.GYNECOLOGY_OBSTETRICS;
        }
        return Specialty.valueOf(value);
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public ArrayList<Appointment> getAppointments() {
        return appointments;
    }

    public ArrayList<Hospitalization> getHospitalizations() {
        return hospitalizations;
    }

    public User findUserByUsername(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    public User findUserById(long id) {
        for (User user : users) {
            if (user.getId() == id) {
                return user;
            }
        }
        return null;
    }

    public Patient findPatient(long id) {
        User user = findUserById(id);
        return user instanceof Patient ? (Patient) user : null;
    }

    public Doctor findDoctor(long id) {
        User user = findUserById(id);
        return user instanceof Doctor ? (Doctor) user : null;
    }

    public Appointment findAppointment(String id) {
        for (Appointment appointment : appointments) {
            if (appointment.getId().equals(id)) {
                return appointment;
            }
        }
        return null;
    }

    public Hospitalization findHospitalization(String id) {
        for (Hospitalization hospitalization : hospitalizations) {
            if (hospitalization.getId().equals(id)) {
                return hospitalization;
            }
        }
        return null;
    }

    public JSONObject serializeUser(User user) {
        JSONObject json = new JSONObject();
        json.put("id", user.getId());
        json.put("username", user.getUsername());
        json.put("firstname", user.getFirstname());
        json.put("lastname", user.getLastname());
        if (user instanceof Administrator) {
            json.put("type", "admin");
        } else if (user instanceof Patient) {
            Patient patient = (Patient) user;
            json.put("type", "patient");
            json.put("email", patient.getEmail());
            json.put("birthdate", patient.getBirthdate().toString());
            json.put("gender", patient.isGender());
            json.put("phone", patient.getPhone());
            json.put("address", patient.getAddress());
        } else if (user instanceof Doctor) {
            Doctor doctor = (Doctor) user;
            json.put("type", "doctor");
            json.put("specialty", doctor.getSpecialty().name());
            json.put("licenceNumber", doctor.getLicenceNumber());
            json.put("assignedOffice", doctor.getAssignedOffice());
        }
        return json;
    }

    public JSONObject serializeAppointment(Appointment appointment) {
        JSONObject json = new JSONObject();
        json.put("id", appointment.getId());
        json.put("patientId", appointment.getPatient().getId());
        json.put("doctorId", appointment.getDoctor().getId());
        json.put("doctor", appointment.getDoctor().getFirstname() + " " + appointment.getDoctor().getLastname());
        json.put("specialty", appointment.getSpecialty().name());
        json.put("datetime", appointment.getDatetime().toString());
        json.put("type", appointment.isType() ? "In-person" : "Remote");
        json.put("status", appointment.getStatus().name());
        return json;
    }

    public JSONObject serializeHospitalization(Hospitalization hospitalization) {
        JSONObject json = new JSONObject();
        json.put("id", hospitalization.getId());
        json.put("patientId", hospitalization.getPatient().getId());
        json.put("doctorId", hospitalization.getDoctor().getId());
        json.put("date", hospitalization.getDate().toString());
        json.put("reason", hospitalization.getReason());
        json.put("roomType", hospitalization.getRoomType().name());
        json.put("observations", hospitalization.getObservations());
        json.put("status", hospitalization.getStatus().name());
        return json;
    }
}
