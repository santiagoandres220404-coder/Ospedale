package packagee;

public class Response {
    private final StatusCode statusCode;
    private final String message;
    private final String data;

    public Response(StatusCode statusCode, String message, String data) {
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
    }

    public static Response ok(String message, String data) {
        return new Response(StatusCode.OK, message, data);
    }

    public static Response created(String message, String data) {
        return new Response(StatusCode.CREATED, message, data);
    }

    public static Response error(StatusCode statusCode, String message) {
        return new Response(statusCode, message, "{}");
    }

    public boolean isSuccess() {
        return statusCode == StatusCode.OK || statusCode == StatusCode.CREATED;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public String getData() {
        return data;
    }
}
