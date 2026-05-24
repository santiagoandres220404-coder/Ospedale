package packagee;

import org.json.JSONArray;
import org.json.JSONObject;

public class CatalogController {
    private final HospitalStore store;

    public CatalogController() {
        store = HospitalStore.getInstance();
        store.loadUsersFromJson();
    }

    public Response getPatients() {
        JSONArray array = new JSONArray();
        for (User user : store.getUsers()) {
            if (user instanceof Patient) {
                array.put(store.serializeUser(user));
            }
        }
        return Response.ok("Pacientes cargados.", array.toString());
    }

    public Response getUser(String id) {
        try {
            User user = store.findUserById(Long.parseLong(id));
            if (user == null) {
                return Response.error(StatusCode.NOT_FOUND, "Usuario no encontrado.");
            }
            return Response.ok("Usuario encontrado.", store.serializeUser(user).toString());
        } catch (NumberFormatException ex) {
            return Response.error(StatusCode.BAD_REQUEST, "El id debe ser numerico.");
        }
    }

    public Response getDoctors() {
        JSONArray array = new JSONArray();
        for (User user : store.getUsers()) {
            if (user instanceof Doctor) {
                array.put(store.serializeUser(user));
            }
        }
        return Response.ok("Doctores cargados.", array.toString());
    }

    public Response getSpecialties() {
        JSONArray array = new JSONArray();
        for (Specialty specialty : Specialty.values()) {
            JSONObject item = new JSONObject();
            item.put("name", specialty.name());
            item.put("label", store.displaySpecialty(specialty));
            array.put(item);
        }
        return Response.ok("Especialidades cargadas.", array.toString());
    }
}
